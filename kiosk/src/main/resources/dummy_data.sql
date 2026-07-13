-- 1. 아이스크림 맛 데이터 (사용자 제공)
INSERT INTO icecream_flavors (flavor_name, image_url, is_active) VALUES
('31요거트', '/images/flavors/31_yogurt.png', 1),
('그린티', '/images/flavors/green_tea.png', 1),
('나주배 소르베', '/images/flavors/naju_pear_sorbet.png', 1),
('너는 참 달고나', '/images/flavors/you_are_so_dalgona.png', 1),
('뉴욕 치즈케이크', '/images/flavors/new_york_cheesecake.png', 1),
('딸기연유퐁당', '/images/flavors/strawberry_condensed_milk.png', 1),
('레인보우샤베트', '/images/flavors/rainbow_sherbet.png', 1),
('말랑꿀떡모찌', '/images/flavors/sweet_honey_mochi.png', 1),
('민트초코', '/images/flavors/mint_chocolate_chip.png', 1),
('바람과 함께 사라지다', '/images/flavors/Twinberry_CheeseCake.png', 1),
('베리베리 스트로베리', '/images/flavors/very_berry_strawberry.png', 1),
('봉쥬르 마카롱', '/images/flavors/bonjour_macaron.png', 1),
('블루베리 파나코타', '/images/flavors/blueberry_panna_cotta.png', 1),
('사랑에 빠진 딸기', '/images/flavors/love_struck_strawberry.png', 1),
('소금 우유 아이스크림', '/images/flavors/salted_milk_ice_cream.png', 1),
('슈팅스타', '/images/flavors/shooting_star.png', 1),
('아몬드 봉봉', '/images/flavors/almond_bonbon.png', 1),
('아이스도코바나나', '/images/flavors/ice_tokyo_banana.png', 1),
('아이스초코도쿄바나나', '/images/flavors/ice_choco_tokyo_banana.png', 1),
('아이스호떡', '/images/flavors/ice_hotteok.png', 1),
('애플 민트', '/images/flavors/apple_mint.png', 1),
('엄마는 외계인', '/images/flavors/Puss_in_Boots.png', 1),
('에스프레소 앤 크림', '/images/flavors/espresso_n_cream.png', 1),
('오레오 쿠키 앤 크림', '/images/flavors/oreo_cookies_n_cream.png', 1),
('이상한 나라의 솜사탕', '/images/flavors/cotton_candy_wonderland.png', 1),
('자모카 아몬드 훠지', '/images/flavors/jamoca_almond_fudge.png', 1),
('체리쥬빌레', '/images/flavors/cherries_jubilee.png', 1),
('초코 아몬드 봉봉', '/images/flavors/choco_almond_bonbon.png', 1),
('초코나무 숲', '/images/flavors/choco_tree_forest.png', 1),
('초콜릿 무스', '/images/flavors/chocolate_mousse.png', 1),
('초콜릿 칩', '/images/flavors/chocolate_chip.png', 1),
('춘식이의 고구마암', '/images/flavors/chunsik_sweet_potato.png', 1),
('치토스 밀크쉐이크 아이스크림', '/images/flavors/cheetos_milkshake.png', 1),
('키세스 브라우니 초콜릿', '/images/flavors/kisses_brownie_chocolate.png', 1),
('팥있는 말차당', '/images/flavors/red_bean_matcha.png', 1),
('피스타치오 아몬드', '/images/flavors/pistachio_almond.png', 1),
('허니 크런치', '/images/flavors/honey_crunch.png', 1);

-- 2. 카테고리 데이터
INSERT INTO categories (category_name, display_order) VALUES
('아이스크림', 1),
('아이스크림 케이크', 2),
('음료', 3),
('디저트', 4);

-- 3. 상품 데이터 (본사 등록 원본 메뉴)
-- 아이스크림 카테고리 (category_id = 1)
INSERT INTO products (category_id, product_name, description, base_price, discount_rate, is_display, created_at) VALUES
(1, '싱글레귤러', '한 가지 맛을 듬뿍 맛볼 수 있는 사이즈', 3500, 0.00, 1, NOW()),
(1, '싱글킹', '좋아하는 맛 하나를 더 크게 맛보는 사이즈', 4300, 0.00, 1, NOW()),
(1, '더블주니어', '두 가지 맛을 조금씩 맛보는 사이즈', 4700, 0.00, 1, NOW()),
(1, '더블레귤러', '두 가지 맛을 듬뿍 맛보는 사이즈', 6700, 0.00, 1, NOW()),
(1, '파인트', '세 가지 맛을 골라 먹는 사이즈', 8900, 0.00, 1, NOW()),
(1, '쿼터', '네 가지 맛을 골라 먹는 사이즈', 17000, 0.00, 1, NOW()),
(1, '패밀리', '다섯 가지 맛을 나눠 먹는 사이즈', 24000, 0.00, 1, NOW()),
(1, '하프갤런', '여섯 가지 맛을 넉넉하게 나눠 먹는 사이즈', 29000, 0.00, 1, NOW());

-- 4. 상품 옵션 데이터 (선택 가능한 맛 개수 등)
INSERT INTO product_options (product_id, option_type, option_name, extra_price, max_flavor_count) VALUES
(1, 'SIZE', '싱글레귤러 컵', 0, 1),
(1, 'SIZE', '싱글레귤러 콘', 0, 1),
(2, 'SIZE', '싱글킹 컵', 0, 1),
(2, 'SIZE', '싱글킹 콘', 0, 1),
(3, 'SIZE', '더블주니어 컵', 0, 2),
(3, 'SIZE', '더블주니어 콘', 0, 2),
(4, 'SIZE', '더블레귤러 컵', 0, 2),
(4, 'SIZE', '더블레귤러 콘', 0, 2),
(5, 'CONTAINER', '파인트 (용기)', 0, 3),
(6, 'CONTAINER', '쿼터 (용기)', 0, 4),
(7, 'CONTAINER', '패밀리 (용기)', 0, 5),
(8, 'CONTAINER', '하프갤런 (용기)', 0, 6);

-- 5. 지점 데이터
INSERT INTO stores (store_name, business_number, region, address, phone, business_hours, store_status, created_at) VALUES
('배스킨라빈스 강남점', '123-45-67890', '서울', '서울특별시 강남구 역삼동', '02-123-4567', '10:00 - 23:00', 'OPEN', NOW());

-- 6. 지점별 판매 상품 매핑 (강남점)
INSERT INTO store_products (store_id, product_id, is_sold_out, is_deleted, store_product_price) VALUES
(1, 1, 0, 0, 3500),
(1, 2, 0, 0, 4300),
(1, 3, 0, 0, 4700),
(1, 4, 0, 0, 6700),
(1, 5, 0, 0, 8900),
(1, 6, 0, 0, 17000),
(1, 7, 0, 0, 24000),
(1, 8, 0, 0, 29000);

-- 7. 지점별 아이스크림 맛 재고 매핑 (강남점에 모든 맛 할당)
-- SELECT flavor_id FROM icecream_flavors 를 이용하여 1~37번 맛을 강남점에 매핑
INSERT INTO store_flavors (store_id, flavor_id, is_sold_out) VALUES
(1, 1, 0), (1, 2, 0), (1, 3, 0), (1, 4, 0), (1, 5, 0),
(1, 6, 0), (1, 7, 0), (1, 8, 0), (1, 9, 0), (1, 10, 0),
(1, 11, 0), (1, 12, 0), (1, 13, 0), (1, 14, 0), (1, 15, 0),
(1, 16, 0), (1, 17, 0), (1, 18, 0), (1, 19, 0), (1, 20, 0),
(1, 21, 0), (1, 22, 0), (1, 23, 0), (1, 24, 0), (1, 25, 0),
(1, 26, 0), (1, 27, 0), (1, 28, 0), (1, 29, 0), (1, 30, 0),
(1, 31, 0), (1, 32, 0), (1, 33, 0), (1, 34, 0), (1, 35, 0),
(1, 36, 0), (1, 37, 0);
