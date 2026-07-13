-- 1. 카테고리 확인 및 추가 (커피 카테고리가 없는 경우 추가)
INSERT INTO categories (category_name, display_order) 
SELECT '커피', 5 FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name = '커피');

-- 2. 제품 추가 (디저트, 음료, 커피)
-- 2.1 디저트
INSERT INTO products (category_id, product_name, description, base_price, discount_rate, is_display, created_at) VALUES
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '소금 우유 아이스 모찌', '달콤 짭짤한 소금 우유 맛 아이스 모찌', 3000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '그린티 아이스 모찌', '녹차의 진한 맛을 담은 모찌', 3000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '체리쥬빌레 마카롱', '아이스크림 마카롱', 3500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '바닐라 아이스크림 롤', '부드러운 바닐라 아이스크림 롤', 2000, 0.00, 1, NOW());

-- 2.2 음료
INSERT INTO products (category_id, product_name, description, base_price, discount_rate, is_display, created_at) VALUES
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '엄마는 외계인 블라스트', '초코볼이 가득한 시그니처 블라스트', 5500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '아몬드 봉봉 블라스트', '아몬드 봉봉 아이스크림으로 만든 블라스트', 5500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '딸기 연유 쉐이크', '달콤한 딸기와 연유의 만남', 5000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '오레오 쉐이크', '오레오 쿠키가 듬뿍', 5000, 0.00, 1, NOW());

-- 2.3 커피
INSERT INTO products (category_id, product_name, description, base_price, discount_rate, is_display, created_at) VALUES
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '카푸치노 블라스트 오리지널', '커피 아이스크림 블라스트', 4500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '아이스 아메리카노', '시원한 아메리카노', 2500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '카페라떼', '부드러운 카페라떼', 3000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '연유 라떼', '달콤한 연유가 들어간 라떼', 3500, 0.00, 1, NOW());

-- 3. 제품 옵션 매핑
-- 단품류(모찌, 음료, 커피 등)는 추가로 아이스크림 맛을 고르지 않으므로 max_flavor_count를 0으로 설정합니다.
INSERT INTO product_options (product_id, option_type, option_name, extra_price, max_flavor_count)
SELECT product_id, 'SIZE', '기본 레귤러', 0, 0 
FROM products 
WHERE product_name IN (
  '소금 우유 아이스 모찌', '그린티 아이스 모찌', '체리쥬빌레 마카롱', '바닐라 아이스크림 롤', 
  '엄마는 외계인 블라스트', '아몬드 봉봉 블라스트', '딸기 연유 쉐이크', '오레오 쉐이크', 
  '카푸치노 블라스트 오리지널', '아이스 아메리카노', '카페라떼', '연유 라떼'
);

-- 4. 지점 판매 상품으로 매핑 (강남점 store_id=1 기준)
-- 강남점에 위에서 추가한 12가지 메뉴를 판매 상태로 등록
INSERT INTO store_products (store_id, product_id, is_sold_out, is_deleted, store_product_price)
SELECT 1, product_id, 0, 0, base_price
FROM products 
WHERE product_name IN (
  '소금 우유 아이스 모찌', '그린티 아이스 모찌', '체리쥬빌레 마카롱', '바닐라 아이스크림 롤', 
  '엄마는 외계인 블라스트', '아몬드 봉봉 블라스트', '딸기 연유 쉐이크', '오레오 쉐이크', 
  '카푸치노 블라스트 오리지널', '아이스 아메리카노', '카페라떼', '연유 라떼'
);
