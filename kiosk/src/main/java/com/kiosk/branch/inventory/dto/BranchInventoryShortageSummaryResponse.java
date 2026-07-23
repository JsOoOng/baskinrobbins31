package com.kiosk.branch.inventory.dto;

import java.util.List;

import com.kiosk.entity.InventoryShortageAlert;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchInventoryShortageSummaryResponse {

    /*
     * 미확인 부족 알림 존재 여부
     */
    private Boolean hasAlert;

    /*
     * 지점 화면에 표시할 문구
     *
     * 재고 번호나 알람 번호는
     * 문구에 포함하지 않습니다.
     */
    private String title;
    private String message;

    private Integer storeId;
    private Integer itemCount;

    /*
     * 부족 품목 상세 목록
     */
    private List<
            BranchInventoryShortageItemResponse
    > items;

    /*
     * 확인 버튼 클릭 후 이동할
     * Vue Router 이름
     */
    private String routeName;

    public static BranchInventoryShortageSummaryResponse from(
            Integer storeId,
            List<InventoryShortageAlert> alerts
    ) {

        List<BranchInventoryShortageItemResponse>
                items =
                alerts == null
                        ? List.of()
                        : alerts.stream()
                                .map(
                                        BranchInventoryShortageItemResponse
                                                ::from
                                )
                                .toList();

        boolean hasAlert =
                !items.isEmpty();

        return BranchInventoryShortageSummaryResponse
                .builder()
                .hasAlert(
                        hasAlert
                )
                .title(
                        hasAlert
                                ? "부족한 재고가 있습니다."
                                : null
                )
                .message(
                        hasAlert
                                ? "재고 관리 화면에서 부족 품목을 확인해주세요."
                                : null
                )
                .storeId(
                        storeId
                )
                .itemCount(
                        items.size()
                )
                .items(
                        items
                )
                .routeName(
                        "branch-inventory"
                )
                .build();
    }
}