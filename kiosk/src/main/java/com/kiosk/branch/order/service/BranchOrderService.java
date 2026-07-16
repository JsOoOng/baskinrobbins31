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

        OrderStatus newStatus = OrderStatus.valueOf(status);

        order.changeOrderStatus(newStatus);
        
        if(newStatus == OrderStatus.PREPARING){

            inventoryService.decrease(order);

        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .store(order.getStore())
                .orderStatus(newStatus)
                .build();

        orderStatusHistoryMapper.save(history);
    }
}