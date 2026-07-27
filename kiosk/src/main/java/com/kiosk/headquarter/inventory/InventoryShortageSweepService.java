package com.kiosk.headquarter.inventory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.notification.service.BranchNotificationService;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;
import com.kiosk.headquarter.repository.StoreFlavorRepository;
import com.kiosk.headquarter.service.HeadNotificationService;
import com.kiosk.headquarter.service.InventoryShortageAlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryShortageSweepService {

    private final HeadStoreInventoryMapper inventoryRepository;
    private final StoreFlavorRepository storeFlavorRepository;
    private final InventoryShortageAlertService inventoryShortageAlertService;
    private final HeadNotificationService headNotificationService;
    private final BranchNotificationService branchNotificationService;

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    @Transactional
    public void detectExistingShortages() {
        inventoryRepository.findAll()
                .forEach(inventoryShortageAlertService::detectOrRefreshShortage);

        for (StoreFlavor flavor : storeFlavorRepository.findAll()) {
            if (flavor.getContainer() == null || flavor.getMinStock() == null
                    || flavor.getContainer() > flavor.getMinStock()) {
                continue;
            }

            String referenceKey = "flavor-" + flavor.getId()
                    + "-" + flavor.getContainer() + "-" + flavor.getMinStock();
            String message = String.format(
                    "%s의 %s 맛 재고가 부족합니다. 현재 %d통, 최소 %d통",
                    flavor.getStore().getStoreName(),
                    flavor.getFlavor().getFlavorName(),
                    flavor.getContainer(),
                    flavor.getMinStock());

            headNotificationService.createNotificationOnce(
                    com.kiosk.entity.enums.NotificationCategory.INVENTORY,
                    com.kiosk.entity.enums.NotificationType.LOW_STOCK,
                    "아이스크림 맛 재고 부족",
                    message,
                    "head-store-flavors",
                    referenceKey);
            branchNotificationService.createOnce(
                    flavor.getStore().getId(),
                    "FLAVOR_LOW_STOCK",
                    referenceKey,
                    "아이스크림 맛 재고 부족",
                    message);
        }
        log.debug("상품·맛 최소 재고 부족 전체 점검 완료");
    }
}
