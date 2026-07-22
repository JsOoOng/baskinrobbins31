package com.kiosk.branch.status.reopsitory;


import org.apache.ibatis.annotations.Mapper;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreInventory;

@Mapper
public interface BranchRestockMapper {


    StoreInventory findStoreInventoryById(
            Integer storeInventoryId
    );


    StoreFlavor findStoreFlavorById(
            Integer storeFlavorId
    );

}