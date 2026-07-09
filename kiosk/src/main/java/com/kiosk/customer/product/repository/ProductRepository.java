package com.kiosk.customer.product.repository;

<<<<<<< Updated upstream
=======
import java.util.List;
import java.util.Optional;

>>>>>>> Stashed changes
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 특정 지점의 특정 카테고리에 속한 상품 리스트와 해당 지점의 품절 여부를 함께 조회
    // (이 결과를 가지고 앞서 만든 ProductListResponse DTO로 변환하게 됩니다.)
    @Query("SELECT p, sp.isSoldOut FROM Product p JOIN StoreProduct sp ON p.productId = sp.productId " +
           "WHERE sp.storeId = :storeId AND p.categoryId = :categoryId AND p.isDisplay = true")
    List<Object[]> findProductsWithSoldOutStatus(@Param("storeId") Long storeId, @Param("categoryId") Long categoryId);

<<<<<<< Updated upstream
	Object findById(Long valueOf);
=======
	Optional<Product> findById(Long valueOf);
>>>>>>> Stashed changes
}