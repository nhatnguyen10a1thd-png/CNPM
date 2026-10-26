-- =========================================================================
-- HỆ THỐNG QUẢN LÝ CHUỖI CỬA HÀNG MỸ PHẨM LUNEA (COSMETICS STORE CHAIN)
-- TẬP TIN DDL TẠO TOÀN BỘ CẤU TRÚC BẢNG, KHÓA CHÍNH, KHÓA NGOẠI & CHỈ MỤC
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: POSTGRESQL 15 / 16 (DOCKER COMPOSE & RENDER CLOUD)
-- =========================================================================

-- XÓA BẢNG CŨ NẾU TỒN TẠI
DROP TABLE IF EXISTS return_items CASCADE;
DROP TABLE IF EXISTS return_requests CASCADE;
DROP TABLE IF EXISTS inventory_adjustments CASCADE;
DROP TABLE IF EXISTS stock_transfer_items CASCADE;
DROP TABLE IF EXISTS stock_transfers CASCADE;
DROP TABLE IF EXISTS purchase_order_items CASCADE;
DROP TABLE IF EXISTS purchase_orders CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS wishlist_items CASCADE;
DROP TABLE IF EXISTS wishlists CASCADE;
DROP TABLE IF EXISTS order_reservations CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS inventories CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS sku_attribute_values CASCADE;
DROP TABLE IF EXISTS skus CASCADE;
DROP TABLE IF EXISTS product_attribute_values CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS vouchers CASCADE;
DROP TABLE IF EXISTS attributes CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS brands CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS password_reset_otps CASCADE;
DROP TABLE IF EXISTS registration_otps CASCADE;
DROP TABLE IF EXISTS beauty_profiles CASCADE;
DROP TABLE IF EXISTS customer_addresses CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS employee_stores CASCADE;
DROP TABLE IF EXISTS employee_roles CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS stores CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- 1. BẢNG VAI TRÒ
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 2. BẢNG QUYỀN HẠN
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255)
);

-- 3. BẢNG PHÂN QUYỀN VAI TRÒ
CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE
);

-- 4. BẢNG TÀI KHOẢN
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. BẢNG CHI NHÁNH SHOWROOM
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    province VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 6. BẢNG NHÂN VIÊN
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    work_email VARCHAR(150),
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 7. BẢNG VAI TRÒ NHÂN VIÊN
CREATE TABLE employee_roles (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id)
);

-- 8. BẢNG NHÂN VIÊN - CHI NHÁNH
CREATE TABLE employee_stores (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE
);

-- 9. BẢNG KHÁCH HÀNG
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE,
    gender VARCHAR(20),
    loyalty_points INT NOT NULL DEFAULT 0,
    join_date DATE
);

-- 10. BẢNG ĐỊA CHỈ KHÁCH HÀNG
CREATE TABLE customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    receiver_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address_detail VARCHAR(255) NOT NULL,
    ward VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    province VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

-- 11. BẢNG HỒ SƠ LÀM ĐẸP
CREATE TABLE beauty_profiles (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    skin_type VARCHAR(255),
    skin_concerns VARCHAR(255),
    care_needs VARCHAR(255),
    preferences VARCHAR(255),
    price_range VARCHAR(100),
    updated_at TIMESTAMP
);

-- 12. BẢNG OTP ĐĂNG KÝ
CREATE TABLE registration_otps (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_reg_otp_email ON registration_otps(email);

-- 13. BẢNG OTP QUÊN MẬT KHẨU
CREATE TABLE password_reset_otps (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    identifier VARCHAR(150) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_otp_account ON password_reset_otps(account_id);

-- 14. BẢNG NHẬT KÝ KIỂM TOÁN
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100),
    target_id VARCHAR(100),
    content TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. BẢNG NHÀ CUNG CẤP
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(150),
    phone VARCHAR(20),
    email VARCHAR(150),
    address VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 16. BẢNG THƯƠNG HIỆU
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 17. BẢNG DANH MỤC MỸ PHẨM
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    parent_id BIGINT REFERENCES categories(id),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 18. BẢNG THUỘC TÍNH
CREATE TABLE attributes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    attribute_group VARCHAR(30) NOT NULL
);

-- 19. BẢNG SẢN PHẨM MỸ PHẨM
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL REFERENCES brands(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    origin VARCHAR(100),
    main_ingredients TEXT,
    benefits TEXT,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 20. BẢNG THUỘC TÍNH SẢN PHẨM
CREATE TABLE product_attribute_values (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id),
    "value" VARCHAR(100) NOT NULL
);

-- 21. BẢNG BIẾN THỂ SKU
CREATE TABLE skus (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku_code VARCHAR(50) NOT NULL UNIQUE,
    variant_name VARCHAR(150),
    price NUMERIC(18,2) NOT NULL,
    list_price NUMERIC(18,2),
    barcode VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX idx_skus_prod_id ON skus(product_id);

-- 22. BẢNG THUỘC TÍNH SKU
CREATE TABLE sku_attribute_values (
    id BIGSERIAL PRIMARY KEY,
    sku_id BIGINT NOT NULL REFERENCES skus(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id),
    "value" VARCHAR(255) NOT NULL
);

-- 23. BẢNG ẢNH SẢN PHẨM
CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku_id BIGINT REFERENCES skus(id),
    url VARCHAR(500) NOT NULL,
    public_id VARCHAR(200),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_img_prod_id ON product_images(product_id);

-- 24. BẢNG TỒN KHO
CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    on_hand_qty INT NOT NULL DEFAULT 0,
    reserved_qty INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 5,
    updated_at TIMESTAMP,
    CONSTRAINT uq_inv_store_sku UNIQUE (store_id, sku_id)
);

-- 25. BẢNG MÃ GIẢM GIÁ (VOUCHERS)
CREATE TABLE vouchers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255),
    description TEXT,
    discount_type VARCHAR(20) NOT NULL,
    discount_value NUMERIC(18,2) NOT NULL,
    max_discount_amount NUMERIC(18,2),
    min_order_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    usage_limit INT NOT NULL DEFAULT 1000,
    used_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 26. BẢNG GIỎ HÀNG
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    updated_at TIMESTAMP
);

-- 27. BẢNG CHI TIẾT GIỎ HÀNG
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    quantity INT NOT NULL
);

-- 28. BẢNG ĐƠN HÀNG
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    fulfillment_store_id BIGINT NOT NULL REFERENCES stores(id),
    receiver_name VARCHAR(150) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    ward VARCHAR(100),
    district VARCHAR(100),
    province VARCHAR(100),
    delivery_method VARCHAR(20) NOT NULL DEFAULT 'HOME_DELIVERY',
    subtotal NUMERIC(18,2) NOT NULL,
    voucher_id BIGINT REFERENCES vouchers(id),
    discount_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    shipping_fee NUMERIC(18,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL DEFAULT 'COD',
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP
);
CREATE INDEX idx_orders_cust ON orders(customer_id);
CREATE INDEX idx_orders_store ON orders(fulfillment_store_id);

-- 29. BẢNG CHI TIẾT ĐƠN HÀNG
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    product_name VARCHAR(200) NOT NULL,
    variant_name VARCHAR(150),
    unit_price NUMERIC(18,2) NOT NULL,
    quantity INT NOT NULL,
    line_total NUMERIC(18,2) NOT NULL
);

-- 30. BẢNG GIỮ HÀNG THEO ĐƠN
CREATE TABLE order_reservations (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    store_id BIGINT NOT NULL REFERENCES stores(id),
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- 31. BẢNG DANH SÁCH YÊU THÍCH
CREATE TABLE wishlists (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 32. BẢNG SẢN PHẨM YÊU THÍCH
CREATE TABLE wishlist_items (
    id BIGSERIAL PRIMARY KEY,
    wishlist_id BIGINT NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 33. BẢNG ĐÁNH GIÁ SẢN PHẨM
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    order_id BIGINT REFERENCES orders(id),
    order_item_id BIGINT REFERENCES order_items(id),
    rating INT NOT NULL,
    content TEXT,
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_reviews_prod ON reviews(product_id);

-- 34. BẢNG ĐƠN ĐẶT HÀNG NHẬP KHO
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    receiving_store_id BIGINT NOT NULL REFERENCES stores(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL REFERENCES employees(id),
    total_amount NUMERIC(18,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP
);

-- 35. BẢNG CHI TIẾT NHẬP KHO
CREATE TABLE purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    quantity INT NOT NULL,
    unit_cost NUMERIC(18,2) NOT NULL,
    line_total NUMERIC(18,2) NOT NULL
);

-- 36. BẢNG PHIẾU ĐIỀU CHUYỂN KHO
CREATE TABLE stock_transfers (
    id BIGSERIAL PRIMARY KEY,
    from_store_id BIGINT NOT NULL REFERENCES stores(id),
    to_store_id BIGINT NOT NULL REFERENCES stores(id),
    status VARCHAR(20) NOT NULL,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP,
    received_at TIMESTAMP
);

-- 37. BẢNG CHI TIẾT ĐIỀU CHUYỂN KHO
CREATE TABLE stock_transfer_items (
    id BIGSERIAL PRIMARY KEY,
    stock_transfer_id BIGINT NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    quantity INT NOT NULL
);

-- 38. BẢNG ĐIỀU CHỈNH TỒN KHO
CREATE TABLE inventory_adjustments (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NOT NULL REFERENCES stores(id),
    sku_id BIGINT NOT NULL REFERENCES skus(id),
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    adjusted_by BIGINT NOT NULL REFERENCES employees(id),
    adjusted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 39. BẢNG YÊU CẦU ĐỔI TRẢ
CREATE TABLE return_requests (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    refund_amount NUMERIC(18,2),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 40. BẢNG CHI TIẾT ĐỔI TRẢ
CREATE TABLE return_items (
    id BIGSERIAL PRIMARY KEY,
    return_request_id BIGINT NOT NULL REFERENCES return_requests(id) ON DELETE CASCADE,
    order_item_id BIGINT NOT NULL REFERENCES order_items(id),
    quantity INT NOT NULL,
    refund_amount NUMERIC(18,2),
    detail_reason VARCHAR(255)
);
