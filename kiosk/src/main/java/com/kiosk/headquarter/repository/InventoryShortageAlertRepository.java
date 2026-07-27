package com.kiosk.headquarter.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryShortageAlert;
import com.kiosk.entity.enums.InventoryShortageAlertStatus;

/**
 * [코드 흐름 안내] InventoryShortageAlertRepository
 *
 * <p>역할: 본사 관리의 재고 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(inventory_shortage_alerts) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface InventoryShortageAlertRepository
        extends JpaRepository<
                InventoryShortageAlert,
                Integer
        > {

    /*
     * 특정 지점 재고에 연결된
     * 활성 알람 한 건 조회
     *
     * RESOLVED를 제외한 상태 목록을 전달합니다.
     */
    Optional<InventoryShortageAlert>
            findFirstByStoreInventory_IdAndAlertStatusInOrderByDetectedAtDescIdDesc(
                    Integer storeInventoryId,
                    Collection<
                            InventoryShortageAlertStatus
                    > alertStatuses
            );

    /*
     * 본사 재고 현황에서 사용할
     * 활성 부족 알람 전체 조회
     *
     * Store, InventoryItem,
     * StoreInventory를 함께 조회합니다.
     */
    @EntityGraph(
            attributePaths = {
                    "store",
                    "item",
                    "storeInventory"
            }
    )
    List<InventoryShortageAlert>
            findAllByAlertStatusInOrderByDetectedAtDescIdDesc(
                    Collection<
                            InventoryShortageAlertStatus
                    > alertStatuses
            );

    /*
     * 특정 지점의 활성 알람 조회
     *
     * 지점 관리자 화면에서 사용합니다.
     */
    @EntityGraph(
            attributePaths = {
                    "store",
                    "item",
                    "storeInventory"
            }
    )
    List<InventoryShortageAlert>
            findAllByStore_IdAndAlertStatusInOrderByDetectedAtDescIdDesc(
                    Integer storeId,
                    Collection<
                            InventoryShortageAlertStatus
                    > alertStatuses
            );

    /*
     * 특정 지점의 특정 알람 조회
     *
     * 지점 관리자가 알람을 확인할 때
     * 다른 지점의 알람에 접근하지 못하게 합니다.
     */
    @EntityGraph(
            attributePaths = {
                    "store",
                    "item",
                    "storeInventory"
            }
    )
    Optional<InventoryShortageAlert>
            findByIdAndStore_Id(
                    Integer alertId,
                    Integer storeId
            );
    
    /*
     * 알람 한 건과 관련 지점·품목·재고를
     * 함께 조회합니다.
     */
    @Override
    @EntityGraph(
            attributePaths = {
                    "store",
                    "item",
                    "storeInventory"
            }
    )
    Optional<InventoryShortageAlert> findById(
            Integer alertId
    );
}