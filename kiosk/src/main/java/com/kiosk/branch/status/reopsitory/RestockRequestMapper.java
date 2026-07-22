package com.kiosk.branch.status.reopsitory;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RestockRequestMapper {

    void deleteByStoreFlavorId(
        Integer storeFlavorId
    );

}