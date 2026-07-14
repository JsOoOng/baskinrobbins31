-- 1. 아이스크림 맛 데이터
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

-- 2. 카테고리 데이터 (active 컬럼 추가)
INSERT INTO categories (category_name, display_order, active) VALUES
('아이스크림', 1, 1),
('아이스크림 케이크', 2, 1),
('음료', 3, 1),
('디저트', 4, 1);

-- 3. 상품 데이터
INSERT INTO products (category_id, product_name, description, base_price, discount_rate, is_display, created_at) VALUES
(1, '싱글레귤러', '한 가지 맛을 듬뿍 맛볼 수 있는 사이즈', 3500, 0.00, 1, NOW()),
(1, '싱글킹', '좋아하는 맛 하나를 더 크게 맛보는 사이즈', 4300, 0.00, 1, NOW()),
(1, '더블주니어', '두 가지 맛을 조금씩 맛보는 사이즈', 4700, 0.00, 1, NOW()),
(1, '더블레귤러', '두 가지 맛을 듬뿍 맛보는 사이즈', 6700, 0.00, 1, NOW()),
(1, '파인트', '세 가지 맛을 골라 먹는 사이즈', 8900, 0.00, 1, NOW()),
(1, '쿼터', '네 가지 맛을 골라 먹는 사이즈', 17000, 0.00, 1, NOW()),
(1, '패밀리', '다섯 가지 맛을 나눠 먹는 사이즈', 24000, 0.00, 1, NOW()),
(1, '하프갤런', '여섯 가지 맛을 넉넉하게 나눠 먹는 사이즈', 29000, 0.00, 1, NOW());

-- 4. 상품 옵션 데이터
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

-- 6. 지점별 판매 상품 매핑
INSERT INTO store_products (store_id, product_id, is_sold_out, is_deleted, store_product_price) VALUES
(1, 1, 0, 0, 3500),
(1, 2, 0, 0, 4300),
(1, 3, 0, 0, 4700),
(1, 4, 0, 0, 6700),
(1, 5, 0, 0, 8900),
(1, 6, 0, 0, 17000),
(1, 7, 0, 0, 24000),
(1, 8, 0, 0, 29000);

-- 7. 지점별 아이스크림 맛 재고 매핑
INSERT INTO store_flavors (store_id, flavor_id, is_sold_out) VALUES
(1, 1, 0), (1, 2, 0), (1, 3, 0), (1, 4, 0), (1, 5, 0),
(1, 6, 0), (1, 7, 0), (1, 8, 0), (1, 9, 0), (1, 10, 0),
(1, 11, 0), (1, 12, 0), (1, 13, 0), (1, 14, 0), (1, 15, 0),
(1, 16, 0), (1, 17, 0), (1, 18, 0), (1, 19, 0), (1, 20, 0),
(1, 21, 0), (1, 22, 0), (1, 23, 0), (1, 24, 0), (1, 25, 0),
(1, 26, 0), (1, 27, 0), (1, 28, 0), (1, 29, 0), (1, 30, 0),
(1, 31, 0), (1, 32, 0), (1, 33, 0), (1, 34, 0), (1, 35, 0),
(1, 36, 0), (1, 37, 0);

-- 8. BANNERS (배너)
INSERT INTO banners (title, image_url, is_active) VALUES
('여름 신메뉴 출시!', '/images/banners/summer_new.png', 1),
('이달의 맛 31% 할인', '/images/banners/flavor_of_month.png', 1);

-- 9. USERS (회원)
INSERT INTO users (phone, point_balance, created_at) VALUES
('010-1111-2222', 1500, NOW()),
('010-3333-4444', 0, NOW());

-- 10. EMPLOYEES (직원)
INSERT INTO employees (store_id, login_id, password, name, role, status) VALUES
(1, 'emp001', '1234', '김점장', 'MANAGER', 'EMPLOYED'),
(1, 'emp002', '1234', '이알바', 'STAFF', 'EMPLOYED');

-- 11. HEADQUARTER_ADMINS (본사 관리자)
INSERT INTO headquarter_admins (login_id, password, name, department, role, status, created_at) VALUES
('admin01', 'admin123', '박본사', '가맹관리팀', 'SUPER_ADMIN', 'ACTIVE', NOW()),
('admin02', 'admin123', '최관리', '재고관리팀', 'ADMIN', 'ACTIVE', NOW());

-- 12. KIOSKS (키오스크)
INSERT INTO kiosks (store_id, kiosk_number, device_serial, kiosk_status, created_at) VALUES
(1, 1, 'SN-A1B2C3D4', 'ONLINE', NOW()),
(1, 2, 'SN-E5F6G7H8', 'ONLINE', NOW());

-- 13. INVENTORY_ITEMS (본사 등록 물품/소모품)
INSERT INTO inventory_items (item_name, unit, unit_price) VALUES
('아이스크림 콘(스탠다드)', 'BOX', 15000),
('아이스크림 컵(싱글)', 'BOX', 12000),
('포장용 드라이아이스', 'BOX', 20000),
('핑크스푼', 'BOX', 5000);

-- 14. STORE_INVENTORY (지점 재고 현황)
INSERT INTO store_inventory (store_id, item_id, current_stock, last_updated) VALUES
(1, 1, 10, NOW()),
(1, 2, 15, NOW()),
(1, 3, 5, NOW()),
(1, 4, 30, NOW());

-- 15. STORE_EXPENSES (지점 지출 내역)
INSERT INTO store_expenses (store_id, employee_id, expense_category, payment_method, expense_date, amount, description, receipt_url, created_at) VALUES
(1, 1, 'INGREDIENTS', 'CARD', '2026-07-01', 50000, '드라이아이스 및 스푼 추가 구매', NULL, NOW()),
(1, 1, 'UTILITY', 'TRANSFER', '2026-07-10', 150000, '전기요금 납부', NULL, NOW());

-- 16. RESTOCK_REQUESTS (발주 요청)
INSERT INTO restock_requests (store_id, item_id, admin_id, request_quantity, status, requested_at) VALUES
(1, 3, 2, 5, 'WAITING', NOW()),
(1, 1, NULL, 10, 'WAITING', NOW());

-- 17. INQUIRIES (문의 내역)
INSERT INTO inquiries (inquiry_type, store_id, admin_id, title, content, answer, status, created_at) VALUES
('STORE_TO_HEAD', 1, 1, '포스기 오류 관련 문의', '결제 시 화면이 멈추는 증상이 있습니다.', '접수되었습니다. 기술팀 배정하겠습니다.', 'WAITING', NOW());

-- 18. ORDERS (주문 기록 - total_price 완전히 제거됨)
INSERT INTO orders (store_id, kiosk_id, user_id, order_number, order_type, dry_ice_mins, order_status, created_at) VALUES
(1, 1, 1, 101, 'HERE', 0, 'COMPLETED', NOW()),
(1, 2, NULL, 102, 'TOGO', 30, 'WAITING', NOW());

-- 19. ORDER_ITEMS (주문 상품)
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 4, 1, 6700), -- 더블레귤러 (order_id=1, product_id=4)
(2, 6, 1, 17000); -- 쿼터 (order_id=2, product_id=6)

-- 20. ORDER_ITEM_FLAVORS (주문 상품의 선택된 맛)
INSERT INTO order_item_flavors (order_item_id, flavor_id, quantity) VALUES
(1, 1, 1), -- 더블레귤러: 31요거트
(1, 9, 1), -- 더블레귤러: 민트초코
(2, 1, 1), -- 쿼터: 31요거트
(2, 2, 1), -- 쿼터: 그린티
(2, 3, 1), -- 쿼터: 나주배 소르베
(2, 4, 1); -- 쿼터: 너는 참 달고나

-- 21. ORDER_ITEM_OPTIONS (주문 상품의 추가 옵션)
INSERT INTO order_item_options (order_item_id, option_id) VALUES
(1, 7), -- 더블레귤러 컵 (option_id=7)
(2, 10); -- 쿼터 용기 (option_id=10)

-- 22. ORDER_STATUS_HISTORY (주문 상태 변경 이력)
INSERT INTO order_status_history (order_id, store_id, order_status, employee_id, created_at) VALUES
(1, 1, 'WAITING', NULL, NOW()),
(1, 1, 'COMPLETED', 2, NOW()),
(2, 1, 'WAITING', NULL, NOW());

-- 23. PAYMENTS (결제 내역)
INSERT INTO payments (order_id, payment_method, base_amount, coupon_discount, point_used, final_amount, payment_status, payment_date) VALUES
(1, 'CARD', 6700, 0, 0, 6700, 'PAID', NOW()),
(2, 'E_PAY', 17000, 0, 0, 17000, 'PAID', NOW());

-- 24. 카테고리 추가 (커피 카테고리가 없는 경우 추가, active 포함)
INSERT INTO categories (category_name, display_order, active)
SELECT '커피', 5, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE category_name = '커피');

-- 25. 추가 제품 등록 (디저트, 음료, 커피)
INSERT INTO products (category_id, product_name, description, base_price, discount_rate, is_display, created_at) VALUES
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '소금 우유 아이스 모찌', '달콤 짭짤한 소금 우유 맛 아이스 모찌', 3000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '그린티 아이스 모찌', '녹차의 진한 맛을 담은 모찌', 3000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '체리쥬빌레 마카롱', '아이스크림 마카롱', 3500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '디저트' LIMIT 1), '바닐라 아이스크림 롤', '부드러운 바닐라 아이스크림 롤', 2000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '엄마는 외계인 블라스트', '초코볼이 가득한 시그니처 블라스트', 5500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '아몬드 봉봉 블라스트', '아몬드 봉봉 아이스크림으로 만든 블라스트', 5500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '딸기 연유 쉐이크', '달콤한 딸기와 연유의 만남', 5000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '음료' LIMIT 1), '오레오 쉐이크', '오레오 쿠키가 듬뿍', 5000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '카푸치노 블라스트 오리지널', '커피 아이스크림 블라스트', 4500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '아이스 아메리카노', '시원한 아메리카노', 2500, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '카페라떼', '부드러운 카페라떼', 3000, 0.00, 1, NOW()),
((SELECT category_id FROM categories WHERE category_name = '커피' LIMIT 1), '연유 라떼', '달콤한 연유가 들어간 라떼', 3500, 0.00, 1, NOW());

-- 26. 추가 제품 옵션 매핑
INSERT INTO product_options (product_id, option_type, option_name, extra_price, max_flavor_count)
SELECT product_id, 'SIZE', '기본 레귤러', 0, 0
FROM products
WHERE product_name IN (
'소금 우유 아이스 모찌', '그린티 아이스 모찌', '체리쥬빌레 마카롱', '바닐라 아이스크림 롤',
'엄마는 외계인 블라스트', '아몬드 봉봉 블라스트', '딸기 연유 쉐이크', '오레오 쉐이크',
'카푸치노 블라스트 오리지널', '아이스 아메리카노', '카페라떼', '연유 라떼'
);

-- 27. 지점 판매 상품으로 추가 매핑 (강남점 store_id=1 기준)
INSERT INTO store_products (store_id, product_id, is_sold_out, is_deleted, store_product_price)
SELECT 1, product_id, 0, 0, base_price
FROM products
WHERE product_name IN (
'소금 우유 아이스 모찌', '그린티 아이스 모찌', '체리쥬빌레 마카롱', '바닐라 아이스크림 롤',
'엄마는 외계인 블라스트', '아몬드 봉봉 블라스트', '딸기 연유 쉐이크', '오레오 쉐이크',
'카푸치노 블라스트 오리지널', '아이스 아메리카노', '카페라떼', '연유 라떼'
);