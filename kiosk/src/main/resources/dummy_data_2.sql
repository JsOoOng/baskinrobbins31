-- 1. BANNERS (배너)
INSERT INTO banners (title, image_url, is_active) VALUES
('여름 신메뉴 출시!', '/images/banners/summer_new.png', 1),
('이달의 맛 31% 할인', '/images/banners/flavor_of_month.png', 1);

-- 2. USERS (회원)
INSERT INTO users (phone, point_balance, created_at) VALUES
('010-1111-2222', 1500, NOW()),
('010-3333-4444', 0, NOW());

-- 3. EMPLOYEES (직원 - 강남점 store_id=1 기준)
INSERT INTO employees (store_id, login_id, password, name, role, status) VALUES
(1, 'emp001', '1234', '김점장', 'MANAGER', 'EMPLOYED'),
(1, 'emp002', '1234', '이알바', 'STAFF', 'EMPLOYED');

-- 4. HEADQUARTER_ADMINS (본사 관리자)
INSERT INTO headquarter_admins (login_id, password, name, department, role, status, created_at) VALUES
('admin01', 'admin123', '박본사', '가맹관리팀', 'SUPER_ADMIN', 'ACTIVE', NOW()),
('admin02', 'admin123', '최관리', '재고관리팀', 'ADMIN', 'ACTIVE', NOW());

-- 5. KIOSKS (키오스크 - 강남점 store_id=1 기준)
INSERT INTO kiosks (store_id, kiosk_number, device_serial, kiosk_status, created_at) VALUES
(1, 1, 'SN-A1B2C3D4', 'ONLINE', NOW()),
(1, 2, 'SN-E5F6G7H8', 'ONLINE', NOW());

-- 6. INVENTORY_ITEMS (본사 등록 물품/소모품)
INSERT INTO inventory_items (item_name, unit, unit_price) VALUES
('아이스크림 콘(스탠다드)', 'BOX', 15000),
('아이스크림 컵(싱글)', 'BOX', 12000),
('포장용 드라이아이스', 'BOX', 20000),
('핑크스푼', 'BOX', 5000);

-- 7. STORE_INVENTORY (지점 재고 현황)
INSERT INTO store_inventory (store_id, item_id, current_stock, last_updated) VALUES
(1, 1, 10, NOW()),
(1, 2, 15, NOW()),
(1, 3, 5, NOW()),
(1, 4, 30, NOW());

-- 8. STORE_EXPENSES (지점 지출 내역)
INSERT INTO store_expenses (store_id, employee_id, expense_category, payment_method, expense_date, amount, description, receipt_url, created_at) VALUES
(1, 1, 'INGREDIENTS', 'CARD', '2026-07-01', 50000, '드라이아이스 및 스푼 추가 구매', NULL, NOW()),
(1, 1, 'UTILITY', 'TRANSFER', '2026-07-10', 150000, '전기요금 납부', NULL, NOW());

-- 9. RESTOCK_REQUESTS (발주 요청)
INSERT INTO restock_requests (store_id, item_id, admin_id, request_quantity, status, requested_at) VALUES
(1, 3, 2, 5, 'WAITING', NOW()),
(1, 1, NULL, 10, 'WAITING', NOW());

-- 10. INQUIRIES (문의 내역)
INSERT INTO inquiries (inquiry_type, store_id, admin_id, title, content, answer, status, created_at) VALUES
('STORE_TO_HEAD', 1, 1, '포스기 오류 관련 문의', '결제 시 화면이 멈추는 증상이 있습니다.', '접수되었습니다. 기술팀 배정하겠습니다.', 'WAITING', NOW());

-- 11. ORDERS (주문 기록)
INSERT INTO orders (store_id, kiosk_id, user_id, order_number, order_type, dry_ice_mins, order_status, total_price, created_at) VALUES
(1, 1, 1, 101, 'HERE', 0, 'COMPLETED', 6700, NOW()),
(1, 2, NULL, 102, 'TOGO', 30, 'WAITING', 17000, NOW());

-- 12. ORDER_ITEMS (주문 상품)
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 4, 1, 6700),  -- 더블레귤러 (order_id=1, product_id=4)
(2, 6, 1, 17000); -- 쿼터 (order_id=2, product_id=6)

-- 13. ORDER_ITEM_FLAVORS (주문 상품의 선택된 맛)
INSERT INTO order_item_flavors (order_item_id, flavor_id, quantity) VALUES
(1, 1, 1), -- 더블레귤러: 31요거트
(1, 9, 1), -- 더블레귤러: 민트초코
(2, 1, 1), -- 쿼터: 31요거트
(2, 2, 1), -- 쿼터: 그린티
(2, 3, 1), -- 쿼터: 나주배 소르베
(2, 4, 1); -- 쿼터: 너는 참 달고나

-- 14. ORDER_ITEM_OPTIONS (주문 상품의 추가 옵션)
INSERT INTO order_item_options (order_item_id, option_id) VALUES
(1, 7), -- 더블레귤러 컵 (option_id=7)
(2, 10); -- 쿼터 용기 (option_id=10)

-- 15. ORDER_STATUS_HISTORY (주문 상태 변경 이력)
INSERT INTO order_status_history (order_id, store_id, order_status, employee_id, created_at) VALUES
(1, 1, 'WAITING', NULL, NOW()),
(1, 1, 'COMPLETED', 2, NOW()),
(2, 1, 'WAITING', NULL, NOW());

-- 16. PAYMENTS (결제 내역)
INSERT INTO payments (order_id, payment_method, base_amount, coupon_discount, point_used, final_amount, payment_status, payment_date) VALUES
(1, 'CARD', 6700, 0, 0, 6700, 'PAID', NOW()),
(2, 'E_PAY', 17000, 0, 0, 17000, 'PAID', NOW());
