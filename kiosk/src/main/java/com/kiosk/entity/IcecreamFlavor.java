package com.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] IcecreamFlavor
 *
 * <p>역할: 아이스크림 맛 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 icecream_flavors 테이블 매핑을 통해 이 객체를 저장·조회한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(
        name = "icecream_flavors",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_icecream_flavor_name",
                        columnNames = "flavor_name"
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(
        access = AccessLevel.PROTECTED
)
@AllArgsConstructor
public class IcecreamFlavor {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "flavor_id")
    private Integer id;

    @Column(
            name = "flavor_name",
            length = 50,
            nullable = false
    )
    private String flavorName;

    @Builder.Default
    @Column(
            name = "is_active",
            nullable = false
    )
    private Boolean isActive = true;

    /*
     * 프론트 입력 maxlength와 맞추기 위해
     * 기존 255에서 500으로 변경합니다.
     *
     * 기존 DB에 image_url이 NULL인 데이터가 있으므로
     * 당장은 nullable=false를 넣지 않습니다.
     * 신규 등록은 Service에서 필수 검증합니다.
     */
    @Column(
            name = "image_url",
            length = 500
    )
    private String imageUrl;

    /*
     * 아이스크림 맛 신규 생성
     */
    public static IcecreamFlavor create(
            String flavorName,
            String imageUrl
    ) {

        return IcecreamFlavor.builder()
                .flavorName(
                        normalizeRequired(
                                flavorName,
                                "맛 이름을 입력해주세요."
                        )
                )
                .imageUrl(
                        normalizeRequired(
                                imageUrl,
                                "맛 이미지 URL을 입력해주세요."
                        )
                )
                .isActive(true)
                .build();
    }

    /*
     * 아이스크림 맛 수정
     */
    public void updateFlavor(
            String flavorName,
            Boolean isActive,
            String imageUrl
    ) {

        this.flavorName =
                normalizeRequired(
                        flavorName,
                        "맛 이름을 입력해주세요."
                );

        this.imageUrl =
                normalizeRequired(
                        imageUrl,
                        "맛 이미지 URL을 입력해주세요."
                );

        if (isActive != null) {
            this.isActive = isActive;
        }
    }

    /*
     * 아이스크림 맛 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /*
     * 필수 문자열 공통 정리
     */
    private static String normalizeRequired(
            String value,
            String errorMessage
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    errorMessage
            );
        }

        return value.trim();
    }
}