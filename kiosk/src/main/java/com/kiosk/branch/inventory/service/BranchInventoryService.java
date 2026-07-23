package com.kiosk.branch.inventory.service;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.repository.BranchInventoryMapper;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderItem;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.StoreProduct;
import com.kiosk.headquarter.service.InventoryShortageAlertService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] BranchInventoryService
 *
 * <p>역할: 지점 운영의 재고 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> BranchInventoryMapper, StoreProductMapper, InventoryShortageAlertService -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BranchInventoryService {

	private final BranchInventoryMapper inventoryMapper;

	private final StoreProductMapper storeProductMapper;

	/*
	 * 재고 부족 감지 및 기존 부족 알람 갱신 서비스
	 */
	private final InventoryShortageAlertService inventoryShortageAlertService;

	/*
	 * 주문 완료 후 재고 차감
	 */
	public void decrease(Order order) {

		validateOrder(order);

		if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
			return;
		}

		for (OrderItem orderItem : order.getOrderItems()) {

			if (orderItem == null || orderItem.getProduct() == null) {
				continue;
			}

			Integer orderQuantity = orderItem.getQuantity();

			if (orderQuantity == null || orderQuantity <= 0) {
				throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
			}

			var product = orderItem.getProduct();

			Collection<InventoryItem> inventoryItems = product.getInventoryItems();

			if (inventoryItems == null || inventoryItems.isEmpty()) {
				throw new IllegalArgumentException("상품에 연결된 재고 품목이 없습니다.");
			}

			/*
			 * 상품에 연결된 재고 품목 중 하나라도 재고가 0 이하이면 해당 상품을 품절 처리합니다.
			 */
			boolean soldOut = false;

			for (InventoryItem item : inventoryItems) {

				if (item == null) {
					continue;
				}

				StoreInventory inventory = inventoryMapper.findByStore_IdAndItem_Id(order.getStore().getId(),

						item.getId()).orElseThrow(() -> new IllegalArgumentException("해당 지점의 재고 정보가 없습니다."));

				/*
				 * 주문 수량만큼 재고 차감
				 *
				 * 자동 보충은 수행하지 않습니다.
				 */
				inventory.decreaseStock(orderQuantity);

				/*
				 * 재고 부족 감지
				 *
				 * currentStock < minStock → 부족 알람 생성 또는 기존 알람 갱신
				 *
				 * currentStock >= minStock → 새 알람 생성하지 않음
				 */
				inventoryShortageAlertService.detectOrRefreshShortage(inventory);

				/*
				 * 자동 보충을 제거했으므로 재고 차감 직후 실제 수량으로 품절 여부를 판단합니다.
				 */
				Integer currentStock = inventory.getCurrentStock();

				if (currentStock == null || currentStock <= 0) {
					soldOut = true;
				}
			}

			/*
			 * 해당 지점에서 판매 중인 상품 조회
			 */
			StoreProduct storeProduct = storeProductMapper.findByStoreIdAndProductId(order.getStore().getId(),

					product.getId()).orElseThrow(() -> new IllegalArgumentException("지점 판매 상품 정보가 없습니다."));

			/*
			 * 연결된 재고 중 하나라도 0 이하라면 상품 품절 처리
			 */
			storeProduct.changeSoldOut(soldOut);
		}
	}

	/*
	 * 주문 기본값 검증
	 */
	private void validateOrder(Order order) {

		if (order == null) {
			throw new IllegalArgumentException("주문 정보가 없습니다.");
		}

		if (order.getStore() == null) {
			throw new IllegalArgumentException("주문 지점 정보가 없습니다.");
		}

		if (order.getStore().getId() == null) {
			throw new IllegalArgumentException("주문 지점 번호가 없습니다.");
		}
	}
}