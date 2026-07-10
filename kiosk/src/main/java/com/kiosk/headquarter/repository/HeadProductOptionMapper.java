package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.ProductOption;
import com.kiosk.entity.enums.OptionType;

@Repository
public interface HeadProductOptionMapper extends JpaRepository<ProductOption, Integer> {

    List<ProductOption> findByProduct_IdOrderByIdDesc(Integer productId);

    Optional<ProductOption> findByProduct_IdAndId(Integer productId, Integer optionId);

    boolean existsByProduct_IdAndOptionTypeAndOptionName(
            Integer productId,
            OptionType optionType,
            String optionName
    );
}