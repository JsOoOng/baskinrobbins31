package com.kiosk.headquarter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.store.HeadStoreResponse;
import com.kiosk.headquarter.dto.store.HeadStoreUpdateRequest;

import java.util.List;

@Mapper
public interface HeadStoreMapper {

    List<HeadStoreResponse> findAllStores();

    HeadStoreResponse findStoreById(@Param("storeId") Integer storeId);

    int updateStore(HeadStoreUpdateRequest request);
}