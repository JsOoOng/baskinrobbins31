package com.kiosk.branch.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.service.BranchInventoryService;
import com.kiosk.branch.order.dto.BranchOrderDetailResponse;
import com.kiosk.branch.order.dto.BranchOrderListResponse;
import com.kiosk.branch.order.repository.BranchOrderMapper;
import com.kiosk.branch.order.repository.BranchOrderStatusHistoryMapper;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderStatusHistory;
import com.kiosk.entity.Payment;
import com.kiosk.entity.enums.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchOrderService {

    private final BranchOrderMapper orderMapper;
    private final BranchOrderStatusHistoryMapper orderStatusHistoryMapper;
    private final BranchInventoryService inventoryService;
    
    // 주문 목록 조회
    public List<BranchOrderListResponse> getOrders(Integer storeId) {

        List<Order> orders =
                orderMapper.findByStore_IdOrderByCreatedAtDesc(storeId);

        return orders.stream()
                .map(BranchOrderListResponse::from)
                .toList();
    }

    // 주문 상세 조회
    public BranchOrderDetailResponse getOrderDetail(Integer orderId) {

        Order order = orderMapper.findWithItemsById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        Payment payment = order.getPayment();

        // Lazy Loading 방지
        order.getOrderItems().forEach(item -> {
            item.getOrderItemFlavors().size();
            item.getOrderItemOptions().size();
        });

        List<BranchOrderDetailResponse.ItemResponse> items =
                order.getOrderItems()
                        .stream()
                        .map(item -> {

                            List<BranchOrderDetailResponse.OptionResponse> options =
                                    item.getOrderItemOptions()
                                            .stream()
                                            .map(option ->
                                                    BranchOrderDetailResponse.OptionResponse.builder()
                                                            .optionType(option.getOption().getOptionType().name())
                                                            .optionName(option.getOption().getOptionName())
                                                            .extraPrice(option.getOption().getExtraPrice())
                                                            .build()
                                            )
                                            .toList();

                            List<BranchOrderDetailResponse.FlavorResponse> flavors =
                                    item.getOrderItemFlavors()
                                            .stream()
                                            .map(flavor ->
                                                    BranchOrderDetailResponse.FlavorResponse.builder()
                                                            .flavorName(flavor.getFlavor().getFlavorName())
                                                            .quantity(flavor.getQuantity())
                                                            .build()
                                            )
                                            .toList();

                            return BranchOrderDetailResponse.ItemResponse.builder()
                                    .productName(item.getProduct().getProductName())
                                    .quantity(item.getQuantity())
                                    .unitPrice(item.getUnitPrice())
                                    .options(options)
                                    .flavors(flavors)
                                    .build();
                        })
                        .toList();

        BranchOrderDetailResponse.BranchOrderDetailResponseBuilder builder =
                BranchOrderDetailResponse.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .orderType(order.getOrderType().name())
                        .orderStatus(order.getOrderStatus().name())
                        .dryIceCount(order.getDryIceCount())
                        .items(items);


        if (payment != null) {

            builder
                    .baseAmount(payment.getBaseAmount())
                    .couponDiscount(payment.getCouponDiscount())
                    .pointUsed(payment.getPointUsed())
                    .finalAmount(payment.getFinalAmount())
                    .paymentMethod(
                            payment.getPaymentMethod() != null
                                    ? payment.getPaymentMethod().name()
                                    : null
                    )
                    .paymentStatus(
                            payment.getPaymentStatus() != null
                                    ? payment.getPaymentStatus().name()
                                    : null
                    )
                    .paymentDate(payment.getPaymentDate());

        }

        return builder.build();
    }

    // 주문 상태 변경
    @Transactional
    public void changeStatus(Integer orderId, String status) {

        Order order = orderMapper.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        OrderStatus currentStatus = order.getOrderStatus();

        OrderStatus newStatus;

        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 주문 상태입니다.");
        }


        /*
         * 상태 변경 규칙
         *
         * WAITING(대기)
         *  -> PREPARING(준비)
         *  -> CANCELED(취소)
         *
         * PREPARING(준비)
         *  -> COMPLETED(완료)
         *
         * COMPLETED(완료)
         *  -> 변경 불가
         *
         * CANCELED(취소)
         *  -> 변경 불가
         */


        // 완료 또는 취소 상태는 변경 불가
        if(
            currentStatus == OrderStatus.COMPLETED ||
            currentStatus == OrderStatus.CANCELED
        ) {

            throw new RuntimeException(
                    "완료 또는 취소된 주문은 상태 변경이 불가능합니다."
            );

        }


        // 대기 -> 준비 / 취소만 가능
        if(currentStatus == OrderStatus.WAITING) {

            if(
                newStatus != OrderStatus.PREPARING &&
                newStatus != OrderStatus.CANCELED
            ) {

                throw new RuntimeException(
                        "대기 상태에서는 준비 또는 취소만 가능합니다."
                );

            }

        }


        // 준비 -> 완료만 가능
        if(currentStatus == OrderStatus.PREPARING) {

            if(newStatus != OrderStatus.COMPLETED) {

                throw new RuntimeException(
                        "준비 상태에서는 완료만 가능합니다."
                );

            }

        }


        // 상태 변경
        order.changeOrderStatus(newStatus);



        // 준비 단계에서 재고 차감
        if(newStatus == OrderStatus.PREPARING){

            inventoryService.decrease(order);

        }


        // 상태 변경 이력 저장
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .store(order.getStore())
                .orderStatus(newStatus)
                .build();


        orderStatusHistoryMapper.save(history);
    }
}