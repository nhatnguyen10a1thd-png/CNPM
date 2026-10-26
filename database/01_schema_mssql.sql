-- =========================================================================
-- HỆ THỐNG QUẢN LÝ CHUỖI CỬA HÀNG MỸ PHẨM LUNEA (COSMETICS STORE CHAIN)
-- TẬP TIN DDL TẠO TOÀN BỘ CẤU TRÚC BẢNG, KHÓA CHÍNH, KHÓA NGOẠI & CHỈ MỤC
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: MICROSOFT SQL SERVER 2019 / 2022
-- =========================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'lunea')
BEGIN
    CREATE DATABASE lunea COLLATE Latin1_General_100_CI_AS_SC_UTF8;
END
GO

USE lunea;
GO

-- XÓA CÁC BẢNG NẾU ĐÃ TỒN TẠI (THEO THỨ TỰ RÀNG BUỘC PHỤ THUỘC)
IF OBJECT_ID('dbo.return_items', 'U') IS NOT NULL DROP TABLE dbo.return_items;
IF OBJECT_ID('dbo.return_requests', 'U') IS NOT NULL DROP TABLE dbo.return_requests;
IF OBJECT_ID('dbo.inventory_adjustments', 'U') IS NOT NULL DROP TABLE dbo.inventory_adjustments;
IF OBJECT_ID('dbo.stock_transfer_items', 'U') IS NOT NULL DROP TABLE dbo.stock_transfer_items;
IF OBJECT_ID('dbo.stock_transfers', 'U') IS NOT NULL DROP TABLE dbo.stock_transfers;
IF OBJECT_ID('dbo.purchase_order_items', 'U') IS NOT NULL DROP TABLE dbo.purchase_order_items;
IF OBJECT_ID('dbo.purchase_orders', 'U') IS NOT NULL DROP TABLE dbo.purchase_orders;
IF OBJECT_ID('dbo.reviews', 'U') IS NOT NULL DROP TABLE dbo.reviews;
IF OBJECT_ID('dbo.wishlist_items', 'U') IS NOT NULL DROP TABLE dbo.wishlist_items;
IF OBJECT_ID('dbo.wishlists', 'U') IS NOT NULL DROP TABLE dbo.wishlists;
IF OBJECT_ID('dbo.order_reservations', 'U') IS NOT NULL DROP TABLE dbo.order_reservations;
IF OBJECT_ID('dbo.order_items', 'U') IS NOT NULL DROP TABLE dbo.order_items;
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID('dbo.carts', 'U') IS NOT NULL DROP TABLE dbo.carts;
IF OBJECT_ID('dbo.inventories', 'U') IS NOT NULL DROP TABLE dbo.inventories;
IF OBJECT_ID('dbo.product_images', 'U') IS NOT NULL DROP TABLE dbo.product_images;
IF OBJECT_ID('dbo.sku_attribute_values', 'U') IS NOT NULL DROP TABLE dbo.sku_attribute_values;
IF OBJECT_ID('dbo.skus', 'U') IS NOT NULL DROP TABLE dbo.skus;
IF OBJECT_ID('dbo.product_attribute_values', 'U') IS NOT NULL DROP TABLE dbo.product_attribute_values;
IF OBJECT_ID('dbo.products', 'U') IS NOT NULL DROP TABLE dbo.products;
IF OBJECT_ID('dbo.vouchers', 'U') IS NOT NULL DROP TABLE dbo.vouchers;
IF OBJECT_ID('dbo.attributes', 'U') IS NOT NULL DROP TABLE dbo.attributes;
IF OBJECT_ID('dbo.categories', 'U') IS NOT NULL DROP TABLE dbo.categories;
IF OBJECT_ID('dbo.brands', 'U') IS NOT NULL DROP TABLE dbo.brands;
IF OBJECT_ID('dbo.suppliers', 'U') IS NOT NULL DROP TABLE dbo.suppliers;
IF OBJECT_ID('dbo.audit_logs', 'U') IS NOT NULL DROP TABLE dbo.audit_logs;
IF OBJECT_ID('dbo.password_reset_otps', 'U') IS NOT NULL DROP TABLE dbo.password_reset_otps;
IF OBJECT_ID('dbo.registration_otps', 'U') IS NOT NULL DROP TABLE dbo.registration_otps;
IF OBJECT_ID('dbo.beauty_profiles', 'U') IS NOT NULL DROP TABLE dbo.beauty_profiles;
IF OBJECT_ID('dbo.customer_addresses', 'U') IS NOT NULL DROP TABLE dbo.customer_addresses;
IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL DROP TABLE dbo.customers;
IF OBJECT_ID('dbo.employee_stores', 'U') IS NOT NULL DROP TABLE dbo.employee_stores;
IF OBJECT_ID('dbo.employee_roles', 'U') IS NOT NULL DROP TABLE dbo.employee_roles;
IF OBJECT_ID('dbo.employees', 'U') IS NOT NULL DROP TABLE dbo.employees;
IF OBJECT_ID('dbo.stores', 'U') IS NOT NULL DROP TABLE dbo.stores;
IF OBJECT_ID('dbo.accounts', 'U') IS NOT NULL DROP TABLE dbo.accounts;
IF OBJECT_ID('dbo.role_permissions', 'U') IS NOT NULL DROP TABLE dbo.role_permissions;
IF OBJECT_ID('dbo.permissions', 'U') IS NOT NULL DROP TABLE dbo.permissions;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;
GO

-- 1. BẢNG VAI TRÒ
CREATE TABLE dbo.roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL
);
GO

-- 2. BẢNG QUYỀN HẠN
CREATE TABLE dbo.permissions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(100) NOT NULL UNIQUE,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(255) NULL
);
GO

-- 3. BẢNG PHÂN QUYỀN VAI TRÒ
CREATE TABLE dbo.role_permissions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_id BIGINT NOT NULL CONSTRAINT fk_rp_role REFERENCES dbo.roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL CONSTRAINT fk_rp_perm REFERENCES dbo.permissions(id) ON DELETE CASCADE
);
GO

-- 4. BẢNG TÀI KHOẢN HỆ THỐNG
CREATE TABLE dbo.accounts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    email NVARCHAR(150) NULL UNIQUE,
    phone NVARCHAR(20) NULL UNIQUE,
    account_type NVARCHAR(20) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 5. BẢNG CHI NHÁNH SHOWROOM
CREATE TABLE dbo.stores (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    province NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20) NULL,
    open_time TIME(7) NULL,
    close_time TIME(7) NULL,
    latitude DECIMAL(10, 7) NULL,
    longitude DECIMAL(10, 7) NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
GO

-- 6. BẢNG NHÂN VIÊN
CREATE TABLE dbo.employees (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE CONSTRAINT fk_emp_account REFERENCES dbo.accounts(id) ON DELETE CASCADE,
    full_name NVARCHAR(150) NOT NULL,
    work_email NVARCHAR(150) NULL,
    phone NVARCHAR(20) NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
GO

-- 7. BẢNG VAI TRÒ NHÂN VIÊN
CREATE TABLE dbo.employee_roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    employee_id BIGINT NOT NULL CONSTRAINT fk_er_emp REFERENCES dbo.employees(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL CONSTRAINT fk_er_role REFERENCES dbo.roles(id)
);
GO

-- 8. BẢNG NHÂN VIÊN - CHI NHÁNH
CREATE TABLE dbo.employee_stores (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    employee_id BIGINT NOT NULL CONSTRAINT fk_es_emp REFERENCES dbo.employees(id) ON DELETE CASCADE,
    store_id BIGINT NOT NULL CONSTRAINT fk_es_store REFERENCES dbo.stores(id),
    is_primary BIT NOT NULL DEFAULT 0
);
GO

-- 9. BẢNG KHÁCH HÀNG
CREATE TABLE dbo.customers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE CONSTRAINT fk_cust_account REFERENCES dbo.accounts(id) ON DELETE CASCADE,
    full_name NVARCHAR(150) NOT NULL,
    birth_date DATE NULL,
    gender NVARCHAR(20) NULL,
    loyalty_points INT NOT NULL DEFAULT 0,
    join_date DATE NULL
);
GO

-- 10. BẢNG ĐỊA CHỈ KHÁCH HÀNG
CREATE TABLE dbo.customer_addresses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL CONSTRAINT fk_addr_cust REFERENCES dbo.customers(id) ON DELETE CASCADE,
    receiver_name NVARCHAR(150) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    address_detail NVARCHAR(255) NOT NULL,
    ward NVARCHAR(100) NOT NULL,
    district NVARCHAR(100) NULL,
    province NVARCHAR(100) NOT NULL,
    is_default BIT NOT NULL DEFAULT 0
);
GO

-- 11. BẢNG HỒ SƠ LÀM ĐẸP (BEAUTY PROFILE)
CREATE TABLE dbo.beauty_profiles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE CONSTRAINT fk_bp_cust REFERENCES dbo.customers(id) ON DELETE CASCADE,
    skin_type NVARCHAR(255) NULL,
    skin_concerns NVARCHAR(255) NULL,
    care_needs NVARCHAR(255) NULL,
    preferences NVARCHAR(255) NULL,
    price_range NVARCHAR(100) NULL,
    updated_at DATETIME2 NULL
);
GO

-- 12. BẢNG OTP ĐĂNG KÝ
CREATE TABLE dbo.registration_otps (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(150) NOT NULL,
    otp_code NVARCHAR(10) NOT NULL,
    expiry_time DATETIME2 NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE INDEX idx_reg_otp_email ON dbo.registration_otps(email);
GO

-- 13. BẢNG OTP QUÊN MẬT KHẨU
CREATE TABLE dbo.password_reset_otps (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id BIGINT NOT NULL CONSTRAINT fk_otp_acc REFERENCES dbo.accounts(id) ON DELETE CASCADE,
    identifier NVARCHAR(150) NOT NULL,
    otp_code NVARCHAR(10) NOT NULL,
    expiry_time DATETIME2 NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE INDEX idx_otp_account ON dbo.password_reset_otps(account_id);
GO

-- 14. BẢNG NHẬT KÝ KIỂM TOÁN (AUDIT LOGS)
CREATE TABLE dbo.audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    employee_id BIGINT NOT NULL CONSTRAINT fk_audit_emp REFERENCES dbo.employees(id),
    action NVARCHAR(100) NOT NULL,
    target_type NVARCHAR(100) NULL,
    target_id NVARCHAR(100) NULL,
    content NVARCHAR(MAX) NULL,
    ip_address NVARCHAR(45) NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 15. BẢNG NHÀ CUNG CẤP
CREATE TABLE dbo.suppliers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    contact_person NVARCHAR(150) NULL,
    phone NVARCHAR(20) NULL,
    email NVARCHAR(150) NULL,
    address NVARCHAR(255) NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
GO

-- 16. BẢNG THƯƠNG HIỆU
CREATE TABLE dbo.brands (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL UNIQUE,
    description NVARCHAR(MAX) NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
GO

-- 17. BẢNG DANH MỤC MỸ PHẨM
CREATE TABLE dbo.categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    parent_id BIGINT NULL CONSTRAINT fk_cat_parent REFERENCES dbo.categories(id),
    description NVARCHAR(500) NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
GO

-- 18. BẢNG THUỘC TÍNH (LOẠI DA & NHU CẦU)
CREATE TABLE dbo.attributes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(120) NOT NULL,
    attribute_group NVARCHAR(30) NOT NULL
);
GO

-- 19. BẢNG SẢN PHẨM MỸ PHẨM
CREATE TABLE dbo.products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    brand_id BIGINT NOT NULL CONSTRAINT fk_prod_brand REFERENCES dbo.brands(id),
    category_id BIGINT NOT NULL CONSTRAINT fk_prod_cat REFERENCES dbo.categories(id),
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX) NULL,
    origin NVARCHAR(100) NULL,
    main_ingredients NVARCHAR(MAX) NULL,
    benefits NVARCHAR(MAX) NULL,
    is_featured BIT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 20. BẢNG LIÊN KẾT THUỘC TÍNH SẢN PHẨM
CREATE TABLE dbo.product_attribute_values (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL CONSTRAINT fk_pav_prod REFERENCES dbo.products(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL CONSTRAINT fk_pav_attr REFERENCES dbo.attributes(id),
    [value] NVARCHAR(100) NOT NULL
);
GO

-- 21. BẢNG BIẾN THỂ SKU
CREATE TABLE dbo.skus (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL CONSTRAINT fk_sku_prod REFERENCES dbo.products(id) ON DELETE CASCADE,
    sku_code NVARCHAR(50) NOT NULL UNIQUE,
    variant_name NVARCHAR(150) NULL,
    price DECIMAL(18,2) NOT NULL,
    list_price DECIMAL(18,2) NULL,
    barcode NVARCHAR(100) NULL UNIQUE,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX idx_skus_prod_id ON dbo.skus(product_id);
GO

-- 22. BẢNG THUỘC TÍNH BIẾN THỂ SKU
CREATE TABLE dbo.sku_attribute_values (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sku_id BIGINT NOT NULL CONSTRAINT fk_sav_sku REFERENCES dbo.skus(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL CONSTRAINT fk_sav_attr REFERENCES dbo.attributes(id),
    [value] NVARCHAR(255) NOT NULL
);
GO

-- 23. BẢNG ẢNH SẢN PHẨM
CREATE TABLE dbo.product_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL CONSTRAINT fk_img_prod REFERENCES dbo.products(id) ON DELETE CASCADE,
    sku_id BIGINT NULL CONSTRAINT fk_img_sku REFERENCES dbo.skus(id),
    url NVARCHAR(500) NOT NULL,
    public_id NVARCHAR(200) NULL,
    is_primary BIT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_img_prod_id ON dbo.product_images(product_id);
GO

-- 24. BẢNG TỒN KHO ĐA CHI NHÁNH
CREATE TABLE dbo.inventories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    store_id BIGINT NOT NULL CONSTRAINT fk_inv_store REFERENCES dbo.stores(id),
    sku_id BIGINT NOT NULL CONSTRAINT fk_inv_sku REFERENCES dbo.skus(id),
    on_hand_qty INT NOT NULL DEFAULT 0,
    reserved_qty INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 5,
    updated_at DATETIME2 NULL
);
CREATE UNIQUE INDEX uq_inv_store_sku ON dbo.inventories(store_id, sku_id);
GO

-- 25. BẢNG MÃ GIẢM GIÁ (VOUCHERS)
CREATE TABLE dbo.vouchers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(255) NULL,
    description NVARCHAR(MAX) NULL,
    discount_type NVARCHAR(20) NOT NULL,
    discount_value DECIMAL(18,2) NOT NULL,
    max_discount_amount DECIMAL(18,2) NULL,
    min_order_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    valid_from DATETIME2 NOT NULL,
    valid_to DATETIME2 NOT NULL,
    usage_limit INT NOT NULL DEFAULT 1000,
    used_count INT NOT NULL DEFAULT 0,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NULL
);
GO

-- 26. BẢNG GIỎ HÀNG
CREATE TABLE dbo.carts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE CONSTRAINT fk_cart_cust REFERENCES dbo.customers(id) ON DELETE CASCADE,
    updated_at DATETIME2 NULL
);
GO

-- 27. BẢNG CHI TIẾT GIỎ HÀNG
CREATE TABLE dbo.cart_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id BIGINT NOT NULL CONSTRAINT fk_ci_cart REFERENCES dbo.carts(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL CONSTRAINT fk_ci_sku REFERENCES dbo.skus(id),
    quantity INT NOT NULL
);
GO

-- 28. BẢNG ĐƠN HÀNG
CREATE TABLE dbo.orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_code NVARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL CONSTRAINT fk_ord_cust REFERENCES dbo.customers(id),
    fulfillment_store_id BIGINT NOT NULL CONSTRAINT fk_ord_store REFERENCES dbo.stores(id),
    receiver_name NVARCHAR(150) NOT NULL,
    receiver_phone NVARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(500) NOT NULL,
    ward NVARCHAR(100) NULL,
    district NVARCHAR(100) NULL,
    province NVARCHAR(100) NULL,
    delivery_method NVARCHAR(20) NOT NULL DEFAULT 'HOME_DELIVERY',
    subtotal DECIMAL(18,2) NOT NULL,
    voucher_id BIGINT NULL CONSTRAINT fk_ord_voucher REFERENCES dbo.vouchers(id),
    discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    shipping_fee DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method NVARCHAR(20) NOT NULL DEFAULT 'COD',
    note NVARCHAR(500) NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    confirmed_at DATETIME2 NULL,
    completed_at DATETIME2 NULL,
    cancelled_at DATETIME2 NULL
);
CREATE INDEX idx_orders_cust ON dbo.orders(customer_id);
CREATE INDEX idx_orders_store ON dbo.orders(fulfillment_store_id);
GO

-- 29. BẢNG CHI TIẾT ĐƠN HÀNG
CREATE TABLE dbo.order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL CONSTRAINT fk_oi_ord REFERENCES dbo.orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL CONSTRAINT fk_oi_sku REFERENCES dbo.skus(id),
    product_name NVARCHAR(200) NOT NULL,
    variant_name NVARCHAR(150) NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(18,2) NOT NULL
);
GO

-- 30. BẢNG GIỮ HÀNG TỒN KHO THEO ĐƠN
CREATE TABLE dbo.order_reservations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL CONSTRAINT fk_res_ord REFERENCES dbo.orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL CONSTRAINT fk_res_sku REFERENCES dbo.skus(id),
    store_id BIGINT NOT NULL CONSTRAINT fk_res_store REFERENCES dbo.stores(id),
    quantity INT NOT NULL,
    status NVARCHAR(20) NOT NULL
);
GO

-- 31. BẢNG DANH SÁCH YÊU THÍCH
CREATE TABLE dbo.wishlists (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE CONSTRAINT fk_wl_cust REFERENCES dbo.customers(id) ON DELETE CASCADE,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 32. BẢNG SẢN PHẨM YÊU THÍCH
CREATE TABLE dbo.wishlist_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    wishlist_id BIGINT NOT NULL CONSTRAINT fk_wli_wl REFERENCES dbo.wishlists(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL CONSTRAINT fk_wli_prod REFERENCES dbo.products(id),
    added_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 33. BẢNG ĐÁNH GIÁ SẢN PHẨM
CREATE TABLE dbo.reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id BIGINT NOT NULL CONSTRAINT fk_rev_cust REFERENCES dbo.customers(id),
    product_id BIGINT NOT NULL CONSTRAINT fk_rev_prod REFERENCES dbo.products(id),
    order_id BIGINT NULL CONSTRAINT fk_rev_ord REFERENCES dbo.orders(id),
    order_item_id BIGINT NULL CONSTRAINT fk_rev_oi REFERENCES dbo.order_items(id),
    rating INT NOT NULL,
    content NVARCHAR(MAX) NULL,
    moderation_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE INDEX idx_reviews_prod ON dbo.reviews(product_id);
GO

-- 34. BẢNG ĐƠN ĐẶT HÀNG NHẬP KHO (PURCHASE ORDERS)
CREATE TABLE dbo.purchase_orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    supplier_id BIGINT NOT NULL CONSTRAINT fk_po_sup REFERENCES dbo.suppliers(id),
    receiving_store_id BIGINT NOT NULL CONSTRAINT fk_po_store REFERENCES dbo.stores(id),
    status NVARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL CONSTRAINT fk_po_emp REFERENCES dbo.employees(id),
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    confirmed_at DATETIME2 NULL
);
GO

-- 35. BẢNG CHI TIẾT NHẬP KHO
CREATE TABLE dbo.purchase_order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL CONSTRAINT fk_poi_po REFERENCES dbo.purchase_orders(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL CONSTRAINT fk_poi_sku REFERENCES dbo.skus(id),
    quantity INT NOT NULL,
    unit_cost DECIMAL(18,2) NOT NULL,
    line_total DECIMAL(18,2) NOT NULL
);
GO

-- 36. BẢNG PHIẾU ĐIỀU CHUYỂN KHO (STOCK TRANSFERS)
CREATE TABLE dbo.stock_transfers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    from_store_id BIGINT NOT NULL CONSTRAINT fk_st_from REFERENCES dbo.stores(id),
    to_store_id BIGINT NOT NULL CONSTRAINT fk_st_to REFERENCES dbo.stores(id),
    status NVARCHAR(20) NOT NULL,
    employee_id BIGINT NOT NULL CONSTRAINT fk_st_emp REFERENCES dbo.employees(id),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    shipped_at DATETIME2 NULL,
    received_at DATETIME2 NULL
);
GO

-- 37. BẢNG CHI TIẾT ĐIỀU CHUYỂN KHO
CREATE TABLE dbo.stock_transfer_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    stock_transfer_id BIGINT NOT NULL CONSTRAINT fk_sti_st REFERENCES dbo.stock_transfers(id) ON DELETE CASCADE,
    sku_id BIGINT NOT NULL CONSTRAINT fk_sti_sku REFERENCES dbo.skus(id),
    quantity INT NOT NULL
);
GO

-- 38. BẢNG ĐIỀU CHỈNH TỒN KHO KIỂM KÊ
CREATE TABLE dbo.inventory_adjustments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    store_id BIGINT NOT NULL CONSTRAINT fk_ia_store REFERENCES dbo.stores(id),
    sku_id BIGINT NOT NULL CONSTRAINT fk_ia_sku REFERENCES dbo.skus(id),
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reason NVARCHAR(255) NOT NULL,
    adjusted_by BIGINT NOT NULL CONSTRAINT fk_ia_emp REFERENCES dbo.employees(id),
    adjusted_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 39. BẢNG YÊU CẦU ĐỔI TRẢ HÀNG
CREATE TABLE dbo.return_requests (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL CONSTRAINT fk_rr_ord REFERENCES dbo.orders(id),
    customer_id BIGINT NOT NULL CONSTRAINT fk_rr_cust REFERENCES dbo.customers(id),
    reason NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NULL,
    refund_amount DECIMAL(18,2) NULL,
    status NVARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 40. BẢNG CHI TIẾT ĐỔI TRẢ HÀNG
CREATE TABLE dbo.return_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    return_request_id BIGINT NOT NULL CONSTRAINT fk_ri_rr REFERENCES dbo.return_requests(id) ON DELETE CASCADE,
    order_item_id BIGINT NOT NULL CONSTRAINT fk_ri_oi REFERENCES dbo.order_items(id),
    quantity INT NOT NULL,
    refund_amount DECIMAL(18,2) NULL,
    detail_reason NVARCHAR(255) NULL
);
GO

PRINT N'>>> [LUNEA DDL] Đã tạo thành công toàn bộ 40 bảng cơ sở dữ liệu trên Microsoft SQL Server!';
GO
