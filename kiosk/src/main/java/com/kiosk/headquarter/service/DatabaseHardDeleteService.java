package com.kiosk.headquarter.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 쉬운주석: 부모 DB 행을 지우기 전에 그 행을 붙잡고 있는 자식 행부터 찾아 지우는 전담 객체다.
 * 실제 DB 외래키 설명서를 읽기 때문에 지점에 연결된 직원·주문·재고 등의 삭제 순서를 자동으로 정한다.
 */
@Component
@RequiredArgsConstructor
public class DatabaseHardDeleteService {

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    private final JdbcTemplate jdbcTemplate;
    // 쉬운주석: 여러 관리자가 동시에 삭제해도 표 구조 메모가 서로 엉키지 않는 안전한 상자를 씁니다.
    private final Map<String, String> primaryKeyCache = new ConcurrentHashMap<>();
    private final Map<String, List<ForeignKeyReference>> referenceCache = new ConcurrentHashMap<>();

    public int deleteTree(String tableName, String primaryKeyColumn, Object id) {
        validateIdentifier(tableName);
        validateIdentifier(primaryKeyColumn);
        return deleteRow(tableName, primaryKeyColumn, id, new HashSet<>());
    }

    private int deleteRow(
            String tableName,
            String primaryKeyColumn,
            Object id,
            Set<DeleteKey> visited
    ) {
        // 쉬운주석: 같은 행을 여러 연결선에서 다시 만나도 한 번만 처리해 무한 반복을 막는다.
        if (!visited.add(new DeleteKey(tableName, String.valueOf(id)))) {
            return 0;
        }

        for (ForeignKeyReference reference : findReferences(tableName)) {
            Object referencedValue = findReferencedValue(
                    tableName, primaryKeyColumn, reference.referencedColumn(), id
            );
            if (referencedValue == null) {
                continue;
            }

            String childPrimaryKey = findPrimaryKey(reference.childTable());
            List<Object> childIds = jdbcTemplate.queryForList(
                    "SELECT " + quote(childPrimaryKey)
                            + " FROM " + quote(reference.childTable())
                            + " WHERE " + quote(reference.childColumn()) + " = ?",
                    Object.class,
                    referencedValue
            );

            for (Object childId : childIds) {
                deleteRow(reference.childTable(), childPrimaryKey, childId, visited);
            }
        }

        // 쉬운주석: 모든 자식 행이 사라진 다음에야 현재 부모 행을 실제 DELETE한다.
        return jdbcTemplate.update(
                "DELETE FROM " + quote(tableName)
                        + " WHERE " + quote(primaryKeyColumn) + " = ?",
                id
        );
    }

    private Object findReferencedValue(
            String tableName,
            String primaryKeyColumn,
            String referencedColumn,
            Object id
    ) {
        List<Object> values = jdbcTemplate.queryForList(
                "SELECT " + quote(referencedColumn)
                        + " FROM " + quote(tableName)
                        + " WHERE " + quote(primaryKeyColumn) + " = ?",
                Object.class,
                id
        );
        return values.isEmpty() ? null : values.getFirst();
    }

    private List<ForeignKeyReference> findReferences(String parentTable) {
        return referenceCache.computeIfAbsent(parentTable, table ->
                jdbcTemplate.query(
                        """
                        SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_COLUMN_NAME
                        FROM information_schema.KEY_COLUMN_USAGE
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND REFERENCED_TABLE_SCHEMA = DATABASE()
                          AND REFERENCED_TABLE_NAME = ?
                        """,
                        (resultSet, rowNumber) -> new ForeignKeyReference(
                                checkedIdentifier(resultSet.getString("TABLE_NAME")),
                                checkedIdentifier(resultSet.getString("COLUMN_NAME")),
                                checkedIdentifier(resultSet.getString("REFERENCED_COLUMN_NAME"))
                        ),
                        table
                )
        );
    }

    private String findPrimaryKey(String tableName) {
        return primaryKeyCache.computeIfAbsent(tableName, table -> {
            List<String> columns = jdbcTemplate.queryForList(
                    """
                    SELECT COLUMN_NAME
                    FROM information_schema.KEY_COLUMN_USAGE
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND CONSTRAINT_NAME = 'PRIMARY'
                    ORDER BY ORDINAL_POSITION
                    """,
                    String.class,
                    table
            );
            if (columns.size() != 1) {
                throw new IllegalStateException(
                        table + " 테이블은 단일 기본키가 아니어서 자동 삭제할 수 없습니다."
                );
            }
            return checkedIdentifier(columns.getFirst());
        });
    }

    private String quote(String identifier) {
        validateIdentifier(identifier);
        return "`" + identifier + "`";
    }

    private String checkedIdentifier(String identifier) {
        validateIdentifier(identifier);
        return identifier;
    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !SAFE_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("안전하지 않은 DB 식별자입니다.");
        }
    }

    private record ForeignKeyReference(
            String childTable,
            String childColumn,
            String referencedColumn
    ) {
    }

    private record DeleteKey(String tableName, String id) {
    }
}
