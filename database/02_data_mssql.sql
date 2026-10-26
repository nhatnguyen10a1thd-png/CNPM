-- =========================================================================
-- HỆ THỐNG QUẢN LÝ CHUỖI CỬA HÀNG MỸ PHẨM LUNEA
-- TẬP LỆNH DML NẠP TOÀN BỘ DỮ LIỆU MẪU CHUẨN (MICROSOFT SQL SERVER)
-- ĐẢM BẢO MỖI DANH MỤC CÓ TỐI THIỂU 15 SẢN PHẨM (TỔNG CỘNG 240 SẢN PHẨM)
-- =========================================================================
USE lunea;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: ROLES (5 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.roles ON;
GO

INSERT INTO dbo.roles (id, name, description) VALUES
    (1, N'ADMIN', N'Quản trị viên toàn quyền hệ thống'),
    (2, N'MANAGER', N'Quản lý chi nhánh showroom'),
    (3, N'ORDER_STAFF', N'Nhân viên điều phối và xử lý đơn hàng'),
    (4, N'WAREHOUSE_STAFF', N'Nhân viên quản lý kho và điều chuyển'),
    (5, N'CUSTOMER', N'Khách hàng mua sắm LUNEA');
GO

SET IDENTITY_INSERT dbo.roles OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: PERMISSIONS (14 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.permissions ON;
GO

INSERT INTO dbo.permissions (id, code, name, description) VALUES
    (1, N'CATALOG_MANAGE', N'Quản lý danh mục & sản phẩm', N'Toàn quyền thêm/sửa/xóa sản phẩm và thương hiệu'),
    (2, N'ORDER_VIEW', N'Xem đơn hàng', N'Xem danh sách và chi tiết đơn hàng trong phạm vi chi nhánh'),
    (3, N'ORDER_CONFIRM', N'Xác nhận đơn hàng', N'Xác nhận và chuyển trạng thái đơn hàng sang đóng gói'),
    (4, N'ORDER_CANCEL', N'Hủy đơn hàng', N'Hủy đơn hàng khi khách yêu cầu hoặc thiếu hàng'),
    (5, N'ORDER_ASSIGN_STORE', N'Phân bổ chi nhánh giao hàng', N'Chỉ định chi nhánh xử lý và xuất kho giao hàng'),
    (6, N'INVENTORY_VIEW', N'Xem tồn kho', N'Xem số lượng tồn kho thực tế và khả dụng đa chi nhánh'),
    (7, N'INVENTORY_ADJUST', N'Điều chỉnh tồn kho', N'Cân bằng kho và kiểm kê định kỳ'),
    (8, N'PURCHASE_MANAGE', N'Quản lý nhập hàng', N'Tạo và duyệt đơn đặt hàng từ nhà cung cấp'),
    (9, N'TRANSFER_MANAGE', N'Quản lý chuyển kho', N'Tạo và nhận phiếu điều chuyển giữa các chi nhánh'),
    (10, N'REPORT_VIEW', N'Xem báo cáo doanh thu', N'Xem thống kê doanh số và hiệu suất kinh doanh'),
    (11, N'CUSTOMER_VIEW', N'Xem thông tin khách hàng', N'Tra cứu hồ sơ làm đẹp và lịch sử mua hàng'),
    (12, N'EMPLOYEE_MANAGE', N'Quản lý nhân viên', N'Thêm và phân quyền nhân sự chi nhánh'),
    (13, N'PROMOTION_MANAGE', N'Quản lý khuyến mãi', N'Thiết lập voucher giảm giá và mã freeship'),
    (14, N'REVIEW_MODERATE', N'Kiểm duyệt đánh giá', N'Duyệt hoặc ẩn bình luận đánh giá của khách hàng');
GO

SET IDENTITY_INSERT dbo.permissions OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: ROLE_PERMISSIONS (37 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.role_permissions ON;
GO

INSERT INTO dbo.role_permissions (id, role_id, permission_id) VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 1, 3),
    (4, 1, 4),
    (5, 1, 5),
    (6, 1, 6),
    (7, 1, 7),
    (8, 1, 8),
    (9, 1, 9),
    (10, 1, 10),
    (11, 1, 11),
    (12, 1, 12),
    (13, 1, 13),
    (14, 1, 14),
    (15, 2, 1),
    (16, 2, 2),
    (17, 2, 3),
    (18, 2, 4),
    (19, 2, 5),
    (20, 2, 6),
    (21, 2, 7),
    (22, 2, 8),
    (23, 2, 9),
    (24, 2, 10),
    (25, 2, 11),
    (26, 2, 13),
    (27, 2, 14),
    (28, 3, 2),
    (29, 3, 3),
    (30, 3, 4),
    (31, 3, 5),
    (32, 3, 6),
    (33, 3, 11),
    (34, 4, 6),
    (35, 4, 7),
    (36, 4, 8),
    (37, 4, 9);
GO

SET IDENTITY_INSERT dbo.role_permissions OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: ACCOUNTS (6 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.accounts ON;
GO

INSERT INTO dbo.accounts (id, username, password_hash, email, phone, account_type, status) VALUES
    (1, N'admin', N'$2a$10$SxfvGvEM8Q3/Q6S5XQsXu.vmjoUF02k6/LTZLOBciadcDkRBI5ktm', N'admin@lunea.test', N'0900000001', N'EMPLOYEE', N'ACTIVE'),
    (2, N'order_hcm', N'$2a$10$SxfvGvEM8Q3/Q6S5XQsXu.vmjoUF02k6/LTZLOBciadcDkRBI5ktm', N'order.hcm@lunea.test', N'0900000002', N'EMPLOYEE', N'ACTIVE'),
    (3, N'order_hn', N'$2a$10$SxfvGvEM8Q3/Q6S5XQsXu.vmjoUF02k6/LTZLOBciadcDkRBI5ktm', N'order.hn@lunea.test', N'0900000003', N'EMPLOYEE', N'ACTIVE'),
    (4, N'warehouse_hcm', N'$2a$10$SxfvGvEM8Q3/Q6S5XQsXu.vmjoUF02k6/LTZLOBciadcDkRBI5ktm', N'warehouse.hcm@lunea.test', N'0900000004', N'EMPLOYEE', N'ACTIVE'),
    (5, N'khach01@lunea.test', N'$2a$10$SxfvGvEM8Q3/Q6S5XQsXu.vmjoUF02k6/LTZLOBciadcDkRBI5ktm', N'khach01@lunea.test', N'0901234567', N'CUSTOMER', N'ACTIVE'),
    (6, N'khach02@lunea.test', N'$2a$10$SxfvGvEM8Q3/Q6S5XQsXu.vmjoUF02k6/LTZLOBciadcDkRBI5ktm', N'khach02@lunea.test', N'0909876543', N'CUSTOMER', N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.accounts OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: STORES (3 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.stores ON;
GO

INSERT INTO dbo.stores (id, name, address, province, phone, open_time, close_time, latitude, longitude, status) VALUES
    (1, N'LUNEA Showroom Flagship Quận 1', N'72 Lê Thánh Tôn, Phường Bến Nghé, Quận 1', N'Hồ Chí Minh', N'02838221234', N'08:30:00', N'22:00:00', 10.776889, 106.700806, N'ACTIVE'),
    (2, N'LUNEA Showroom Tràng Tiền', N'24 Tràng Tiền, Phường Tràng Tiền, Quận Hoàn Kiếm', N'Hà Nội', N'02439365678', N'08:30:00', N'21:30:00', 21.025345, 105.856234, N'ACTIVE'),
    (3, N'LUNEA Showroom Bạch Đằng', N'180 Bạch Đằng, Phường Hải Châu 1, Quận Hải Châu', N'Đà Nẵng', N'02363829999', N'08:30:00', N'21:30:00', 16.068892, 108.223541, N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.stores OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: EMPLOYEES (4 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.employees ON;
GO

INSERT INTO dbo.employees (id, account_id, full_name, work_email, phone, status) VALUES
    (1, 1, N'Nguyễn Quản Trị Hệ Thống', N'admin@lunea.test', N'0900000001', N'ACTIVE'),
    (2, 2, N'Trần Minh Quân (Đơn hàng HCM)', N'order.hcm@lunea.test', N'0900000002', N'ACTIVE'),
    (3, 3, N'Lê Thu Hà (Đơn hàng Hà Nội)', N'order.hn@lunea.test', N'0900000003', N'ACTIVE'),
    (4, 4, N'Phạm Văn Kho (Thủ kho HCM)', N'warehouse.hcm@lunea.test', N'0900000004', N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.employees OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: EMPLOYEE_ROLES (4 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.employee_roles ON;
GO

INSERT INTO dbo.employee_roles (id, employee_id, role_id) VALUES
    (1, 1, 1),
    (2, 2, 3),
    (3, 3, 3),
    (4, 4, 4);
GO

SET IDENTITY_INSERT dbo.employee_roles OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: EMPLOYEE_STORES (4 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.employee_stores ON;
GO

INSERT INTO dbo.employee_stores (id, employee_id, store_id, is_primary) VALUES
    (1, 1, 1, 1),
    (2, 2, 1, 1),
    (3, 3, 2, 1),
    (4, 4, 1, 1);
GO

SET IDENTITY_INSERT dbo.employee_stores OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: CUSTOMERS (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.customers ON;
GO

INSERT INTO dbo.customers (id, account_id, full_name, birth_date, gender, loyalty_points, join_date) VALUES
    (1, 5, N'Lê Hoàng Yến', N'1998-05-15', N'FEMALE', 150, N'2025-01-10'),
    (2, 6, N'Nguyễn Tuấn Anh', N'1995-11-20', N'MALE', 80, N'2025-02-14');
GO

SET IDENTITY_INSERT dbo.customers OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: CUSTOMER_ADDRESSES (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.customer_addresses ON;
GO

INSERT INTO dbo.customer_addresses (id, customer_id, receiver_name, phone, address_detail, ward, district, province, is_default) VALUES
    (1, 1, N'Lê Hoàng Yến', N'0901234567', N'Số 45 Đường Lê Duẩn', N'Phường Bến Nghé', N'Quận 1', N'Hồ Chí Minh', 1),
    (2, 2, N'Nguyễn Tuấn Anh', N'0909876543', N'Số 12 Ngõ 88 Phố Huế', N'Phường Hàng Bài', N'Quận Hoàn Kiếm', N'Hà Nội', 1);
GO

SET IDENTITY_INSERT dbo.customer_addresses OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: BEAUTY_PROFILES (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.beauty_profiles ON;
GO

INSERT INTO dbo.beauty_profiles (id, customer_id, skin_type, skin_concerns, care_needs, preferences, price_range, updated_at) VALUES
    (1, 1, N'Da Dầu, Da Nhạy Cảm', N'Mụn ẩn, Thâm mụn, Lỗ chân lông to', N'Dưỡng Sáng Mờ Thâm, Trị Mụn', N'Mỹ phẩm thuần chay hữu cơ lành tính', N'300.000đ - 800.000đ', N'2026-03-01 10:00:00'),
    (2, 2, N'Da Khô, Da Hỗn Hợp', N'Thiếu ẩm, Nứt nẻ mùa đông', N'Cấp Ẩm Chuyên Sâu, Phục Hồi Da', N'Dược mỹ phẩm dịu nhẹ không hương liệu', N'400.000đ - 1.200.000đ', N'2026-03-05 14:30:00');
GO

SET IDENTITY_INSERT dbo.beauty_profiles OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: SUPPLIERS (5 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.suppliers ON;
GO

INSERT INTO dbo.suppliers (id, name, contact_person, phone, email, address, status) VALUES
    (1, N'Công Ty TNHH Mỹ Phẩm Quốc Tế L''Oréal Việt Nam', N'Phạm Thị Bích Hạnh', N'02839111888', N'contact@loreal.vn', N'Tầng 12 Vincom Center, 72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP. HCM', N'ACTIVE'),
    (2, N'Công Ty Cổ Phần Nature Story (Cocoon Việt Nam)', N'Nguyễn Ngọc Điệp', N'02838328228', N'partner@cocoonvietnam.com', N'38D Tân Kỳ Tân Quý, Phường Tây Thạnh, Quận Tân Phú, TP. HCM', N'ACTIVE'),
    (3, N'Công Ty TNHH Amorepacific Việt Nam (Laneige & Innisfree)', N'Trần Bảo Long', N'02838247000', N'sales@amorepacific.vn', N'Tòa nhà Saigon Centre, 65 Lê Lợi, Bến Nghé, Quận 1, TP. HCM', N'ACTIVE'),
    (4, N'Công Ty TNHH Shiseido Cosmetics Việt Nam (Anessa)', N'Lê Hoàng Phúc', N'02839104200', N'order@shiseido.vn', N'Tòa nhà Diamond Plaza, 34 Lê Duẩn, Bến Nghé, Quận 1, TP. HCM', N'ACTIVE'),
    (5, N'Công Ty Cổ Phần Phân Phối Dược Mỹ Phẩm Minh Khang', N'Vũ Đình Cường', N'02437668989', N'info@minhkhangpharma.com', N'Số 15 Lô B Đại lộ Thăng Long, Mễ Trì, Nam Từ Liêm, Hà Nội', N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.suppliers OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: BRANDS (30 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.brands ON;
GO

INSERT INTO dbo.brands (id, name, description, status) VALUES
    (1, N'Cocoon Vietnam', N'Thương hiệu mỹ phẩm thuần chay 100% từ thực vật Việt Nam như bí đao, cà phê Đắk Lắk, bưởi, hoa hồng.', N'ACTIVE'),
    (2, N'La Roche-Posay', N'Thương hiệu dược mỹ phẩm hàng đầu từ Pháp được hơn 90.000 chuyên gia da liễu toàn cầu khuyên dùng.', N'ACTIVE'),
    (3, N'The Ordinary', N'Thương hiệu chăm sóc da nổi tiếng Canada tiên phong với nồng độ hoạt chất tinh khiết minh bạch giá hợp lý.', N'ACTIVE'),
    (4, N'Paula''s Choice', N'Dược mỹ phẩm cao cấp của Mỹ nổi tiếng với các sản phẩm tẩy da chết BHA/AHA và Retinol khoa học.', N'ACTIVE'),
    (5, N'Innisfree', N'Thương hiệu mỹ phẩm tự nhiên hàng đầu Hàn Quốc với chiết xuất từ thiên nhiên tươi đẹp đảo Jeju.', N'ACTIVE'),
    (6, N'CeraVe', N'Dược mỹ phẩm phát triển bởi chuyên gia da liễu Mỹ chứa phức hợp 3 Ceramide thiết yếu phục hồi da.', N'ACTIVE'),
    (7, N'Anessa', N'Thương hiệu chống nắng chuyên nghiệp số 1 Nhật Bản thuộc tập đoàn Shiseido suốt 21 năm liên tiếp.', N'ACTIVE'),
    (8, N'L''Oréal Paris', N'Tập đoàn mỹ phẩm và làm đẹp lớn nhất thế giới từ Pháp với công nghệ làm đẹp tiên tiến bậc nhất.', N'ACTIVE'),
    (9, N'Laneige', N'Thương hiệu mỹ phẩm cao cấp Hàn Quốc dẫn đầu xu hướng ẩm mượt căng mọng Water Bank và cushion mỏng mịn.', N'ACTIVE'),
    (10, N'MAC Cosmetics', N'Thương hiệu trang điểm chuyên nghiệp đẳng cấp quốc tế của Mỹ tôn vinh vẻ đẹp cá tính thời thượng.', N'ACTIVE'),
    (11, N'Maybelline New York', N'Thương hiệu trang điểm số 1 thế giới với những dòng mascara, cushion và son lì dẫn đầu xu hướng New York.', N'ACTIVE'),
    (12, N'Vichy', N'Dược mỹ phẩm Pháp sử dụng 100% nước khoáng núi lửa ngàn năm tuổi giàu 15 khoáng chất quý phục hồi da.', N'ACTIVE'),
    (13, N'Estée Lauder', N'Tập đoàn mỹ phẩm cao cấp sang trọng của Mỹ với tinh chất chống lão hóa ban đêm huyền thoại ANR.', N'ACTIVE'),
    (14, N'Bioderma', N'Thương hiệu dược mỹ phẩm Pháp sáng chế công nghệ nước tẩy trang Micellar Water Sensibio H2O số 1 toàn cầu.', N'ACTIVE'),
    (15, N'Klairs', N'Thương hiệu mỹ phẩm thuần chay lành tính Hàn Quốc chăm sóc an toàn cho làn da nhạy cảm nhất.', N'ACTIVE'),
    (16, N'Simple', N'Thương hiệu chăm sóc da số 1 Vương quốc Anh dịu nhẹ thuần khiết không cồn không hương liệu nhân tạo.', N'ACTIVE'),
    (17, N'Eucerin', N'Dược mỹ phẩm chuyên khoa da liễu hơn 100 năm tuổi của Đức được tin dùng tại các bệnh viện da liễu.', N'ACTIVE'),
    (18, N'Kiehl''s', N'Thương hiệu dược mỹ phẩm cao cấp New York từ năm 1851 kết hợp tinh hoa thảo mộc và khoa học hiện đại.', N'ACTIVE'),
    (19, N'Sulwhasoo', N'Mỹ phẩm thảo dược Đông y Hoàng gia cao cấp Hàn Quốc với chiết xuất Nhân sâm quý báu chống lão hóa.', N'ACTIVE'),
    (20, N'Dior', N'Nhà mốt làm đẹp thời trang xa xỉ bậc nhất nước Pháp với các tuyệt tác son môi, kem nền và nước hoa sang trọng.', N'ACTIVE'),
    (21, N'Chanel', N'Biểu tượng thời trang và nước hoa xa xỉ trường tồn nước Pháp với những hương thơm quý phái say đắm.', N'ACTIVE'),
    (22, N'YSL', N'Thương hiệu mỹ phẩm xa xỉ Yves Saint Laurent Pháp nổi bật với phong cách quyến rũ, cá tính và sắc sảo.', N'ACTIVE'),
    (23, N'Rom&nd', N'Thương hiệu trang điểm Hàn Quốc cực thịnh với các bảng màu son tint bóng căng mọng và phấn mắt ngọt ngào.', N'ACTIVE'),
    (24, N'3CE Stylenanda', N'Thương hiệu thời trang trang điểm phong cách Hàn Quốc tạo nên trào lưu son kem lì và má hồng tươi tắn.', N'ACTIVE'),
    (25, N'Sunplay Skin Aqua', N'Thương hiệu chống nắng dưỡng da dạng sữa lỏng nhẹ giá mềm bán chạy số 1 tại các nhà thuốc Việt Nam.', N'ACTIVE'),
    (26, N'DHC', N'Thương hiệu thực phẩm chức năng và dầu tẩy trang Oliu nguyên chất hàng đầu Nhật Bản an toàn cho làn da.', N'ACTIVE'),
    (27, N'Moroccanoil', N'Thương hiệu chăm sóc tóc chuyên nghiệp cao cấp Israel tiên phong với tinh dầu Argan dưỡng tóc bóng mượt.', N'ACTIVE'),
    (28, N'Tsubaki', N'Thương hiệu chăm sóc tóc chuẩn Salon Nhật Bản chiết xuất từ hoa trà đỏ phục hồi tóc hư tổn bóng bẩy.', N'ACTIVE'),
    (29, N'Bath & Body Works', N'Thương hiệu chăm sóc cơ thể và xịt thơm Body Mist số 1 nước Mỹ với hàng trăm mùi hương quyến rũ.', N'ACTIVE'),
    (30, N'Victoria''s Secret', N'Thương hiệu thời trang và nước hoa cơ thể gợi cảm lãng mạn trứ danh nước Mỹ thu hút mọi ánh nhìn.', N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.brands OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: CATEGORIES (20 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.categories ON;
GO

INSERT INTO dbo.categories (id, name, parent_id, description, status) VALUES
    (1, N'Chăm sóc da', NULL, N'Các sản phẩm dưỡng da mặt chuyên sâu từ bước làm sạch đến đặc trị', N'ACTIVE'),
    (2, N'Trang điểm', NULL, N'Mỹ phẩm trang điểm cao cấp tôn vinh đường nét tự nhiên và cá tính', N'ACTIVE'),
    (3, N'Chăm sóc tóc & Cơ thể', NULL, N'Các sản phẩm chăm sóc toàn thân, tẩy da chết và nuôi dưỡng mái tóc suôn mượt', N'ACTIVE'),
    (4, N'Nước hoa', NULL, N'Hương thơm quý phái và xịt thơm thư giãn lưu giữ nét cuốn hút riêng', N'ACTIVE'),
    (5, N'Tinh chất & Serum', 1, N'Serum đặc trị dưỡng ẩm, mờ thâm nám, chống lão hóa và phục hồi', N'ACTIVE'),
    (6, N'Kem dưỡng ẩm', 1, N'Kem dưỡng cấp nước, khóa ẩm sâu và củng cố màng bảo vệ da', N'ACTIVE'),
    (7, N'Sữa rửa mặt', 1, N'Sữa rửa mặt dạng gel, bọt dịu nhẹ làm sạch sâu không khô căng', N'ACTIVE'),
    (8, N'Kem chống nắng', 1, N'Kem và sữa chống nắng phổ rộng bảo vệ da trước tia UVA/UVB và ánh sáng xanh', N'ACTIVE'),
    (9, N'Nước hoa hồng & Toner', 1, N'Toner cân bằng độ pH, cấp ẩm tức thì và se khít lỗ chân lông', N'ACTIVE'),
    (10, N'Tẩy trang', 1, N'Nước, dầu và sáp tẩy trang làm sạch 99% bụi mịn và lớp makeup', N'ACTIVE'),
    (11, N'Tẩy tế bào chết', 1, N'Tẩy tế bào chết vật lý và hóa học AHA/BHA làm mịn màng bề mặt da', N'ACTIVE'),
    (12, N'Mặt nạ dưỡng da', 1, N'Mặt nạ giấy, mặt nạ đất sét thanh lọc và mặt nạ ngủ tái sinh', N'ACTIVE'),
    (13, N'Son môi', 2, N'Son thỏi lì, son kem nhung, son tint bóng và son dưỡng phục hồi môi', N'ACTIVE'),
    (14, N'Kem nền & Cushion', 2, N'Cushion phấn nước và kem nền kiềm dầu che phủ hoàn hảo tự nhiên', N'ACTIVE'),
    (15, N'Phấn phủ & Má hồng', 2, N'Phấn phủ kiềm dầu lâu trôi và phấn má tươi tắn bắt sáng rạng ngời', N'ACTIVE'),
    (16, N'Trang điểm mắt & Mascara', 2, N'Mascara làm dài cong mi, kẻ mắt chống trôi và bảng phấn mắt cuốn hút', N'ACTIVE'),
    (17, N'Tẩy tế bào chết cơ thể', 3, N'Tẩy da chết toàn thân hạt cà phê, muối khoáng và đường hạt làm sáng da', N'ACTIVE'),
    (18, N'Chăm sóc tóc & Dầu gội', 3, N'Dầu gội không sulfate, dầu xả, serum mọc tóc và dầu dưỡng phục hồi tóc', N'ACTIVE'),
    (19, N'Nước hoa cao cấp', 4, N'Nước hoa Eau de Parfum lưu hương 8-12 giờ với các tầng hương sang trọng', N'ACTIVE'),
    (20, N'Xịt thơm toàn thân Body Mist', 4, N'Xịt thơm cơ thể và tóc dịu ngọt mang lại cảm giác thư giãn sảng khoái suốt ngày dài', N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.categories OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: ATTRIBUTES (10 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.attributes ON;
GO

INSERT INTO dbo.attributes (id, name, attribute_group) VALUES
    (1, N'Da Dầu', N'SKIN_TYPE'),
    (2, N'Da Khô', N'SKIN_TYPE'),
    (3, N'Da Nhạy Cảm', N'SKIN_TYPE'),
    (4, N'Da Hỗn Hợp', N'SKIN_TYPE'),
    (5, N'Da Mụn', N'SKIN_TYPE'),
    (6, N'Trị Mụn', N'CONCERN'),
    (7, N'Dưỡng Ẩm Chuyên Sâu', N'CONCERN'),
    (8, N'Dưỡng Sáng Mờ Thâm', N'CONCERN'),
    (9, N'Chống Lão Hóa', N'CONCERN'),
    (10, N'Phục Hồi Da', N'CONCERN');
GO

SET IDENTITY_INSERT dbo.attributes OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: PRODUCTS (240 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.products ON;
GO

INSERT INTO dbo.products (id, brand_id, category_id, name, description, origin, main_ingredients, benefits, is_featured, status, created_at) VALUES
    (1, 1, 5, N'Serum Bí Đao Cocoon 7% Niacinamide Trị Mụn & Mờ Thâm', N'Serum bí đao Cocoon kết hợp dịch chiết bí đao, rau má và tràm trà giúp kháng khuẩn, làm thông thoáng lỗ chân lông, kiềm dầu thừa rõ rệt.', N'Việt Nam', N'Bí đao, Rau má, Tràm trà, 7% Niacinamide, TECA', N'Kiểm soát dầu thừa, giảm sưng đỏ nốt mụn, làm mờ vết thâm mụn và ngăn ngừa mụn tái phát.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (2, 3, 5, N'Tinh Chất The Ordinary Niacinamide 10% + Zinc 1%', N'Công thức serum kinh điển chứa vitamin B3 nồng độ cao kết hợp kẽm khoáng giúp thanh lọc da, giảm bít tắc bã nhờn và đều màu da.', N'Canada', N'10% Niacinamide, 1% Zinc PCA, Pentylene Glycol', N'Cân bằng bã nhờn, thu nhỏ lỗ chân lông, làm dịu da mụn và cải thiện bề mặt da mịn màng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (3, 2, 5, N'Serum Phục Hồi Tái Tạo Da La Roche-Posay Hyalu B5', N'Dưỡng chất chuyên sâu với phức hợp Hyaluronic Acid đa phân tử và Vitamin B5 đậm đặc giúp làm đầy nếp nhăn và tái tạo hàng rào da.', N'Pháp', N'Vitamin B5 (Panthenol), Dual Hyaluronic Acid, Madecassoside', N'Cấp ẩm chuyên sâu, phục hồi màng bảo vệ da bị tổn thương sau mụn hoặc liệu trình da liễu.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (4, 5, 5, N'Serum Cấp Ẩm Chuyên Sâu Innisfree Green Tea Seed Hyaluronic', N'Công nghệ thẩm thấu nano giúp dưỡng chất từ mầm trà xanh hữu cơ đảo Jeju khóa ẩm suốt 24 giờ cho làn da mọng nước.', N'Hàn Quốc', N'Trà xanh hữu cơ Beauty Green Tea, 5 loại Hyaluronic Acid', N'Cấp nước tức thì, giải cứu làn da khô ráp, củng cố độ đàn hồi và tạo độ bóng khỏe tự nhiên.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (5, 8, 5, N'Serum Siêu Cấp Ẩm & Sáng Mịn Da L''Oréal Paris Pure Hyaluronic Acid 1.5%', N'Serum dưỡng ẩm bán chạy toàn cầu với 1.5% Hyaluronic Acid phân tử siêu vi thẩm thấu sâu đa tầng biểu bì.', N'Pháp', N'1.5% Hyaluronic Acid đa phân tử, Glycerin, Calcium Pantothenate', N'Làm đầy nếp nhăn li ti, giúp da căng mọng rạng rỡ và giữ ẩm suốt ngày dài.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (6, 13, 5, N'Serum Chống Lão Hóa Ban Đêm Estée Lauder Advanced Night Repair', N'Tinh chất ban đêm bán chạy số 1 tại Mỹ ứng dụng công nghệ Chronolux Power Signal phục hồi tối đa dấu hiệu tuổi tác.', N'Mỹ', N'Chronolux Power Signal, Men Bifida Ferment Lysate, Tripeptide-32', N'Giảm nếp nhăn rõ rệt, săn chắc đàn hồi và đánh thức làn da tươi trẻ rạng rỡ sau mỗi giấc ngủ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (7, 15, 5, N'Serum Dưỡng Sáng Da Mờ Thâm Nám Klairs Freshly Juiced Vitamin Drop', N'Serum Vitamin C tươi nồng độ 5% chiết xuất từ cam quýt hữu cơ an toàn cho làn da nhạy cảm dễ kích ứng.', N'Hàn Quốc', N'5% Pure Vitamin C (Ascorbic Acid), Chiết xuất Rau má Centella', N'Làm mờ vết thâm sau mụn, ức chế melanin, làm đều màu da và kích thích sản sinh collagen.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (8, 18, 5, N'Tinh Chất Tái Tạo Da Kiehl''s Retinol Skin-Renewing Daily Micro-Dose', N'Công thức vi liều chuẩn xác kết hợp Retinol nguyên chất, Peptide và Ceramide giúp da tái tạo mịn màng không bong tróc.', N'Mỹ', N'Pure Retinol Micro-Dose, Niacinamide, Peptide, Ceramide', N'Tăng sinh collagen, xóa mờ nếp nhăn, thu nhỏ lỗ chân lông và làm mịn kết cấu da.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (9, 12, 5, N'Dưỡng Chất Khoáng Cô Đặc Phục Hồi Da Vichy Minéral 89 Booster', N'Chứa đến 89% nước khoáng núi lửa cô đặc giàu khoáng chất kết hợp 0.4% Hyaluronic Acid tự nhiên.', N'Pháp', N'89% Vichy Thermal Water, Hyaluronic Acid, Citric Acid', N'Củng cố hàng rào bảo vệ da, tái tạo sức sống cho làn da xỉn màu và mất nước.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (10, 17, 5, N'Tinh Chất Giảm Thâm Nám Sáng Da Eucerin Spotless Brightening Booster Serum', N'Hoạt chất Thiamidol độc quyền được chứng minh lâm sàng ức chế enzyme Tyrosinase gây thâm nám từ gốc.', N'Đức', N'Thiamidol, Pro-Hyaluron đa tầng, Glucosylrutin', N'Giảm đốm nâu và thâm nám đến 75% sau 2 tuần, mang lại làn da trắng hồng rạng rỡ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (11, 4, 5, N'Tinh Chất Cấp Ẩm Chống Lão Hóa Paula''s Choice Hyaluronic Acid Booster', N'Serum đậm đặc chứa Hyaluronic Acid kết hợp Ceramide củng cố màng ẩm tức thì cho làn da mất nước.', N'Mỹ', N'Hyaluronic Acid phân tử nhỏ, Ceramide NP, Panthenol', N'Khôi phục độ ẩm ẩm mượt, xóa mờ nếp nhăn li ti và làm đầy rãnh cười.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (12, 19, 5, N'Tinh Chất Khởi Đầu Đánh Thức Làn Da Sulwhasoo First Care Activating Serum', N'Hợp chất Jaum Activator từ 5 loại thảo mộc Đông y quý hiếm tối ưu hóa khả năng hấp thụ dưỡng chất.', N'Hàn Quốc', N'Jaum Activator, Rễ Địa Hoàng, Hoa Mẫu Đơn, Hạt Sen Trắng', N'Kích hoạt tuần hoàn máu dưới da, tái sinh năng lượng và tăng cường hàng rào ẩm tự nhiên.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (13, 2, 5, N'Serum Trị Mụn Chuyên Sâu La Roche-Posay Effaclar Serum 3 Tác Động', N'Phức hợp 3 acid AHA + BHA + LHA kết hợp Niacinamide đặc trị mụn sưng viêm, mụn ẩn và vết thâm mụn dai dẳng.', N'Pháp', N'1.5% Salicylic Acid (BHA), 3.5% Glycolic Acid (AHA), 0.45% LHA, Niacinamide', N'Tẩy tế bào chết sâu trong nang lông, giảm viêm sưng mụn mủ và se khít lỗ chân lông.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (14, 16, 5, N'Serum Cấp Nước Tươi Mát Simple Booster Serum 3% Hyaluronic Acid & B5', N'Công thức lành tính thuần chay không cồn, không paraben, cấp nước tức thì cho làn da nhạy cảm mất nước.', N'Anh', N'3% Hyaluronic Acid, Vitamin B5, Pentavitin tự nhiên', N'Dưỡng ẩm mịn mượt, giảm khô ráp bong tróc mà hoàn toàn không gây nhờn dính.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (15, 20, 5, N'Serum Trẻ Hóa Tế Bào Cao Cấp Dior Capture Totale Le Sérum', N'Tuyệt tác chống lão hóa từ vườn hoa Dior Granville giúp phục hồi mật độ và độ săn chắc của làn da sau 4 giờ.', N'Pháp', N'Chiết xuất Hoa Longoza lên men, H.A. phân tử kép, Squalane thực vật', N'Làm mờ nếp nhăn sâu, nâng cơ và đem lại làn da trẻ trung tràn đầy sinh khí.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (16, 2, 6, N'Kem Dưỡng Ẩm Phục Hồi Da La Roche-Posay Cicaplast Baume B5+', N'Kem dưỡng phục hồi màng ẩm đa năng chứa Tribioma và 5% Panthenol làm dịu kích ứng và phục hồi sau 1 giờ.', N'Pháp', N'Tribioma, 5% Panthenol, Madecassoside, Zinc Gluconate', N'Làm dịu vết đỏ, giảm ngứa ngáy, phục hồi da tổn thương sau lăn kim, laser hoặc trị mụn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (17, 6, 6, N'Sữa Dưỡng Ẩm Chuyên Sâu CeraVe Moisturizing Lotion Da Khô', N'Sữa dưỡng ẩm dịu nhẹ dạng lỏng chứa 3 Ceramide thiết yếu ứng dụng công nghệ nhả chậm MVE suốt 24 giờ.', N'Mỹ', N'3 Ceramide thiết yếu (1, 3, 6-II), Hyaluronic Acid, Công nghệ MVE', N'Phục hồi lớp màng bảo vệ tự nhiên, dưỡng ẩm sâu không gây bết rít cho mặt và cơ thể.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (18, 15, 6, N'Kem Dưỡng Ẩm Làm Dịu Da Khô & Nhạy Cảm Klairs Rich Moist Soothing Cream', N'Kem dưỡng ẩm lành tính từ nấm men Beta-Glucan và bơ hạt mỡ hạ nhiệt tức thì cho làn da căng rát.', N'Hàn Quốc', N'Chiết xuất Beta-Glucan, Dầu Jojoba hữu cơ, Bơ hạt mỡ', N'Cung cấp độ ẩm tức thì và duy trì độ ẩm bền vững cho làn da khô nhạy cảm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (19, 5, 6, N'Kem Dưỡng Cấp Nước Trà Xanh Innisfree Green Tea Seed Hyaluronic Cream', N'Kem dưỡng dạng gel cream tươi mát chứa mầm trà xanh hữu cơ giúp khóa chặt lớp ẩm suốt cả ngày.', N'Hàn Quốc', N'Beauty Green Tea, 5 loại Hyaluronic Acid, Ceramide trà xanh', N'Khóa ẩm đa tầng, nuôi dưỡng làn da căng mọng bóng bẩy chuẩn phong cách Hàn Quốc.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (20, 18, 6, N'Kem Dưỡng Ẩm Huyền Thoại Kiehl''s Ultra Facial Cream Suốt 24 Giờ', N'Kem dưỡng ẩm số 1 của Kiehl''s kết cấu siêu mỏng nhẹ với chiết xuất đạm Glycoprotein từ băng Bắc Cực.', N'Mỹ', N'Antarcticine (Đạm băng Bắc Cực), Squalane ô liu, Dầu quả bơ', N'Cấp ẩm sâu suốt 24 giờ kể cả trong điều kiện thời tiết khô hanh khắc nghiệt nhất.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (21, 9, 6, N'Kem Dưỡng Cấp Nước Khóa Ẩm Laneige Water Bank Blue Hyaluronic Cream', N'Công nghệ Blue Hyaluronic Acid siêu phân tử nhỏ hơn 2000 lần thẩm thấu siêu tốc vào hạ bì.', N'Hàn Quốc', N'Blue Hyaluronic Acid lên men sâu, Beta-glucan, Ceramide', N'Khắc phục 10 dấu hiệu da mất nước, giảm mẩn đỏ và phục hồi da nhạy cảm sau mụn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (22, 1, 6, N'Thạch Bí Đao Cocoon Cấp Ẩm Kiềm Dầu Mụn 100ml', N'Thạch dưỡng ẩm thuần chay kết cấu gel trong veo tan như nước trên da, kiểm soát nhờn suốt 24h.', N'Việt Nam', N'Chiết xuất bí đao, 5% Niacinamide, Tinh dầu tràm trà, L-Arginine', N'Kiềm dầu, làm dịu vết mụn sưng, dưỡng ẩm mỏng nhẹ không bít tắc nang lông.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (23, 17, 6, N'Kem Dưỡng Ẩm Cân Bằng Dịu Nhẹ Eucerin Lipo-Balance Da Khô Nhạy Cảm', N'Công thức chứa Ceramide-3 và Biotin giúp củng cố lớp lipid biểu bì ngăn mất nước qua da.', N'Đức', N'Ceramide-3, Biotin, Dầu bơ hạt mỡ, Vitamin E', N'Giảm khô ráp ngứa ngáy đến 90%, tăng khả năng đề kháng cho da nhạy cảm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (24, 16, 6, N'Kem Dưỡng Ẩm Dịu Nhẹ Simple Kind To Skin Replenishing Rich Moisturiser', N'Kem dưỡng lành tính với Pro-Vitamin B5 và Vitamin E nuôi dưỡng da suốt 12 giờ không bí da.', N'Anh', N'Pro-Vitamin B5, Vitamin E, Dầu hạt lưu ly borage seed oil', N'Khóa ẩm nhẹ nhàng, cân bằng sinh thái da, an toàn tuyệt đối cho mẹ bầu và da nhạy cảm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (25, 14, 6, N'Kem Dưỡng Kiềm Dầu Thu Nhỏ Lỗ Chân Lông Bioderma Sébium Pore Refiner', N'Hợp chất Fluidactiv độc quyền điều hòa lượng bã nhờn ngăn ngừa bít tắc lỗ chân lông.', N'Pháp', N'Phức hợp Fluidactiv, Axit Agaric, Bột khoáng kiềm dầu', N'Làm mịn bề mặt da, kiềm dầu đến 8 giờ và tạo hiệu ứng mịn lì tự nhiên.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (26, 8, 6, N'Kem Dưỡng Chống Lão Hóa Ban Đêm L''Oréal Paris Revitalift Laser X3', N'Công thức Pro-Xylane 3% nồng độ cao kích thích tổng hợp sợi collagen làm mờ nếp nhăn rõ rệt.', N'Pháp', N'3% Pro-Xylane đậm đặc, Adenosine, LHA nhẹ nhàng', N'Tái tạo bề mặt da, nâng cơ săn chắc và phục hồi độ đàn hồi sau 4 tuần.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (27, 19, 6, N'Kem Dưỡng Nhân Sâm Hoàng Gia Sulwhasoo Concentrated Ginseng Renewing Cream', N'Chiết xuất Ginsenomics cô đặc gấp 6000 lần từ sâm quý Hàn Quốc tái sinh mật độ cấu trúc tế bào da.', N'Hàn Quốc', N'Ginsenomics, Chiết xuất củ sâm đỏ 6 năm tuổi, Peptides sâm', N'Xóa mờ rãnh nhăn sâu, tăng độ săn chắc và đem lại làn da sáng mịn căng tràn sức sống.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (28, 4, 6, N'Kem Dưỡng Ẩm Đêm Cho Da Dầu Mụn Paula''s Choice Clear Oil-Free Moisturizer', N'Kem dưỡng không chứa dầu khoáng kết hợp Niacinamide và Ceramide cấp ẩm mà không gây mụn.', N'Mỹ', N'Niacinamide, Ceramide, Chiết xuất Cam thảo, Blueberry', N'Làm dịu nốt mụn đỏ, cấp ẩm dịu mát và tăng cường hàng rào kháng khuẩn cho da.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (29, 12, 6, N'Gel Dưỡng Ẩm Khóa Nước Suốt 48 Giờ Vichy Aqualia Thermal Gel Cream', N'Chứa nước khoáng núi lửa Vichy và đường thực vật Mannose cấp nước sâu từng tế bào biểu bì.', N'Pháp', N'Vichy Thermal Water, Sodium Hyaluronate, Đường Mannose', N'Khóa ẩm suốt 48 giờ, tạo màng mỏng mịn như sương mát lạnh trên làn da thiếu nước.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (30, 2, 6, N'Kem Dưỡng Làm Giảm Mụn Đầu Đen La Roche-Posay Effaclar K(+)', N'Kết hợp LHA, Salicylic Acid và phức hợp Carnosine + Vitamin E chống oxy hóa bã nhờn suốt 8 giờ.', N'Pháp', N'LHA, Salicylic Acid, Carnosine, Airlicium', N'Loại bỏ mụn đầu đen, kiềm dầu thừa, làm mịn màng kết cấu da gồ ghề sau 2 tuần.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (31, 1, 7, N'Sữa Rửa Mặt Dịu Nhẹ Cocoon Cà Phê Đắk Lắk Sạch Sâu', N'Chiết xuất cà phê Đắk Lắk nguyên chất kết hợp dầu mù u và acid amin làm sạch dịu nhẹ không khô căng.', N'Việt Nam', N'Chiết xuất cà phê Đắk Lắk, Dầu mù u, Axit Amin, Vitamin B5', N'Làm sạch sâu bã nhờn và bụi bẩn, giữ màng ẩm tự nhiên ở độ pH chuẩn 5.5.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (32, 6, 7, N'Sữa Rửa Mặt Tạo Bọt CeraVe Foaming Facial Cleanser Cho Da Dầu Mụn', N'Sữa rửa mặt dạng gel tạo bọt mịn cuốn trôi dầu thừa và cặn trang điểm mà không làm căng rát da.', N'Mỹ', N'3 Ceramide thiết yếu, Niacinamide, Hyaluronic Acid', N'Thanh lọc tuyến bã nhờn, bảo vệ hàng rào lipid tự nhiên của biểu bì.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (33, 2, 7, N'Gel Rửa Mặt Làm Sạch Sâu Giảm Nhờn La Roche-Posay Effaclar Purifying Foaming Gel', N'Gel rửa mặt chuyên biệt cho da dầu nhạy cảm với kẽm PCA kháng khuẩn và nước khoáng La Roche-Posay.', N'Pháp', N'Nước khoáng La Roche-Posay, Zinc PCA, Citric Acid', N'Loại bỏ tạp chất và dầu thừa tận sâu chân lông, giảm bóng nhờn suốt ngày dài.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (34, 5, 7, N'Sữa Rửa Mặt Trà Xanh Dưỡng Ẩm Innisfree Green Tea Hydrating Amino Cleansing Foam', N'Bọt amino acid siêu mịn từ trà xanh hữu cơ làm sạch êm ái mà vẫn giữ lại độ ẩm mượt tự nhiên.', N'Hàn Quốc', N'Chiết xuất trà xanh Beauty Green Tea, Phức hợp Amino Acid', N'Làm sạch dịu mát, đánh thức làn da sảng khoái tràn đầy năng lượng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (35, 16, 7, N'Gel Rửa Mặt Dịu Nhẹ Simple Kind To Skin Refreshing Facial Wash 100% Soap Free', N'Gel rửa mặt quốc dân không chứa xà phòng, không chất tạo màu nhân tạo an toàn cho mọi làn da.', N'Anh', N'Pro-Vitamin B5, Vitamin E, Nước tinh khiết lọc 3 lần', N'Rửa sạch nhẹ dịu, duy trì lớp màng bảo vệ tự nhiên không gây kích ứng đỏ rát.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (36, 6, 7, N'Sữa Rửa Mặt Dưỡng Ẩm CeraVe Hydrating Facial Cleanser Cho Da Khô', N'Công thức dạng sữa không bọt làm sạch nhẹ nhàng và bù ẩm tức thì cho làn da nứt nẻ nhạy cảm.', N'Mỹ', N'3 Ceramide thiết yếu, Hyaluronic Acid, Glycerin thực vật', N'Cân bằng độ ẩm sinh học, bảo tồn tối đa lớp màng lipid tự nhiên của da.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (37, 18, 7, N'Sữa Rửa Mặt Hoa Cúc Làm Dịu Da Kiehl''s Calendula Deep Cleansing Foaming', N'Chiết xuất hoa cúc La Mã Marigold tạo bọt bông mịn cuốn trôi bụi bẩn mà giữ da êm ái thư giãn.', N'Mỹ', N'Chiết xuất Hoa cúc Calendula, Glycerin nguồn gốc tự nhiên', N'Làm dịu làn da kích ứng, kháng viêm nhẹ và se mịn bề mặt da.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (38, 14, 7, N'Gel Rửa Mặt Làm Sạch Dịu Nhẹ Cho Da Nhạy Cảm Bioderma Sensibio Gel Moussant', N'Phức hợp DAF tự nhiên được cấp bằng sáng chế giúp tăng ngưỡng dung nạp của làn da nhạy cảm.', N'Pháp', N'Phức hợp D.A.F, Coco-Glucoside, Glyceryl Oleate', N'Làm sạch nhẹ nhàng, dưỡng ẩm và làm dịu ngay cảm giác ngứa căng rát.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (39, 12, 7, N'Gel Rửa Mặt Trị Mụn Kiềm Dầu Sâu Vichy Normaderm Phytosolution', N'Kết hợp Kẽm, Đồng khoáng và BHA tự nhiên từ cây lộc đề giúp làm sạch bã nhờn và ngừa mụn.', N'Pháp', N'Zinc Oxide, Copper, Salicylic Acid thực vật, Nước khoáng Vichy', N'Giảm bóng dầu 80%, ngăn ngừa bùng phát mụn viêm và thu nhỏ chân lông.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (40, 17, 7, N'Gel Rửa Mặt Giảm Mụn Cho Da Nhờn Eucerin Pro Acne Solution Cleansing Gel', N'Công thức không xà phòng, không hương liệu chứa 6% Ampho-Tensides làm sạch bã nhờn dịu nhẹ.', N'Đức', N'6% Ampho-Tensides, Salicylic Acid, Citric Acid', N'Tẩy sạch bã nhờn dư thừa, ngăn chặn vi khuẩn gây mụn P.acnes phát triển.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (41, 15, 7, N'Sữa Rửa Mặt Tạo Bọt Dịu Nhẹ Klairs Rich Moist Foaming Cleanser', N'Công thức tạo bọt tự nhiên với amino acid thay thế chất hoạt động bề mặt SLS mạnh.', N'Hàn Quốc', N'Axit Amin thực vật, Ceramide 3, Dầu tràm trà hữu cơ', N'Làm sạch sâu nhẹ nhàng, cấp nước cho làn da khô nhạy cảm dễ kích ứng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (42, 4, 7, N'Sữa Rửa Mặt Phục Hồi Hàng Rào Ẩm Paula''s Choice Resist Optimal Results', N'Kết cấu sữa mịn như kem giúp bảo vệ lớp lipid bảo vệ da trong lúc loại bỏ lớp trang điểm nhẹ.', N'Mỹ', N'Chiết xuất Trà xanh, Cúc La Mã, Glycerin thực vật', N'Chống lão hóa, làm dịu da khô ngứa và duy trì độ ẩm mịn màng mượt mà.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (43, 1, 7, N'Gel Rửa Mặt Hoa Hồng Cocoon Cấp Ẩm Da Mềm Mượt 140ml', N'Nước cất hoa hồng hữu cơ Hậu Giang kết hợp Betaine và Allantoin làm sạch dịu ngọt da khô.', N'Việt Nam', N'Nước cất hoa hồng hữu cơ, Betaine, Allantoin', N'Cấp ẩm sâu, se nhỏ lỗ chân lông và lưu lại làn da mềm mại ngát hương hoa.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (44, 26, 7, N'Xà Phòng Rửa Mặt Dưỡng Ẩm Tinh Chất Oliu DHC Mild Soap 90g', N'Bánh xà phòng rửa mặt truyền thống Nhật Bản giàu dầu olive nguyên chất và mật ong tinh khiết.', N'Nhật Bản', N'Dầu Olive nguyên chất hữu cơ, Mật ong tự nhiên', N'Tạo lớp bọt xốp dày đặc cuốn trôi bã nhờn mà da vẫn mềm mịn mượt mà.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (45, 8, 7, N'Sữa Rửa Mặt Sáng Mịn & Căng Mướt L''Oréal Paris Glycolic-Bright Glowing Foam', N'Chứa Glycolic Acid (AHA) nồng độ nhẹ nhàng tẩy tế bào chết xỉn màu ngay trong bước rửa mặt.', N'Pháp', N'Glycolic Acid (AHA), Bọt khoáng mịn, Niacinamide', N'Làm sáng làn da sạm màu, làm mờ vết thâm nám và mang lại vẻ tươi sáng đều màu.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (46, 2, 8, N'Kem Chống Nắng Kiềm Dầu La Roche-Posay Anthelios UVMune 400 Oil Control', N'Đột phá màng lọc Mexoryl 400 chống tia UVA dài nhất (380-400nm), kết hợp công nghệ Airlicium kiềm dầu đến 12 giờ.', N'Pháp', N'Màng lọc Mexoryl 400, Hoạt chất Airlicium, Nước khoáng LRP', N'Bảo vệ toàn diện trước tia UV nguy hại, kháng nước kháng mồ hôi, ráo mịn không vệt trắng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (47, 7, 8, N'Sữa Chống Nắng Dưỡng Da Kiềm Dầu Anessa Perfect UV Sunscreen Skincare Milk SPF50+', N'Công nghệ Auto Booster độc quyền phản ứng với nhiệt độ, độ ẩm và mồ hôi để màng bảo vệ càng thêm bền bỉ.', N'Nhật Bản', N'Công nghệ Auto Booster, Chiết xuất Trà xanh Kyoto, Collagen', N'Chống trôi cực hạn khi đi bơi hay hoạt động ngoài trời, dưỡng ẩm chống oxy hóa da.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (48, 25, 8, N'Sữa Chống Nắng Hiệu Chỉnh Sắc Da Skin Aqua Tone Up UV Milk Lavender', N'Sắc tím Lavender hòa quyện giữa sắc xanh và hồng giúp hiệu chỉnh tone da vàng vọt sáng bừng rạng rỡ.', N'Nhật Bản', N'Hyaluronic Acid, Vitamin C, Chiết xuất Chanh Dây, Mận châu Âu', N'Nâng tone da tự nhiên bắt sáng trong trẻo, kiểm soát bã nhờn dịu nhẹ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (49, 12, 8, N'Kem Chống Nắng Kiềm Dầu Khô Thoáng Vichy Capital Soleil Mattifying Dry Touch', N'Công nghệ màng lọc Mexoryl XL kết hợp đất sét xanh Pháp giúp hấp thu bã nhờn tức thì suốt cả ngày.', N'Pháp', N'Màng lọc Mexoryl XL, Đất sét xanh Kaolin, Nước khoáng núi lửa Vichy', N'Kiềm dầu khô thoáng, bảo vệ tế bào da trước tia UV và ô nhiễm khói bụi thành thị.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (50, 5, 8, N'Kem Chống Nắng Kiềm Dầu Nâng Tone Innisfree Tone Up No Sebum Sunscreen', N'Bộ lọc khoáng chất vô cơ vật lý lành tính kết hợp bột kiềm dầu No-Sebum kiềm dầu 24h.', N'Hàn Quốc', N'Zinc Oxide, Titanium Dioxide, Chiết xuất rau má, Trà xanh', N'Bảo vệ da dịu nhẹ, nâng tone hồng hào tự nhiên thay thế lớp kem lót trang điểm.', 0, N'ACTIVE', N'2026-01-01 00:00:00');
GO

INSERT INTO dbo.products (id, brand_id, category_id, name, description, origin, main_ingredients, benefits, is_featured, status, created_at) VALUES
    (51, 17, 8, N'Kem Chống Nắng Cho Da Dầu Mụn Eucerin Sun Gel-Cream Dry Touch Oil Control', N'Công nghệ quang phổ tiên tiến kết hợp L-Carnitine kiểm soát bã nhờn suốt 8 giờ không bóng dầu.', N'Đức', N'L-Carnitine, Licochalcone A, Glycyrrhetinic Acid', N'Bảo vệ DNA tế bào da, chống lại tác hại của tia HEVIS (ánh sáng xanh từ màn hình).', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (52, 8, 8, N'Serum Chống Nắng Mịn Mờ Vệt Bóng L''Oréal Paris UV Defender Matte & Fresh', N'Màng lọc quang phổ rộng Mexoryl SX/XL kết hợp khoáng chất Airlicium cho bề mặt ráo mịn như nhung.', N'Pháp', N'Màng lọc Mexoryl SX/XL, Khoáng chất Airlicium, Niacinamide', N'Cản tia UV tối đa, ngăn ngừa 5 năm lão hóa do ánh nắng mặt trời.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (53, 4, 8, N'Kem Chống Nắng Toàn Diện Paula''s Choice Extra Care Non-Greasy Sunscreen SPF 50', N'Chống nắng thể thao kháng nước lâu trôi cho mặt và toàn thân với kết cấu mỏng mịn thấm nhanh.', N'Mỹ', N'Hợp chất chống oxy hóa Vitamin A, C, E, Màng lọc chống nắng thế hệ mới', N'Chống nắng phổ rộng bảo vệ da khi đi bơi hay chơi thể thao ngoài trời.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (54, 1, 8, N'Sữa Chống Nắng Bí Đao Cocoon Kháng Nước & Kiềm Dầu 50ml', N'Chống nắng thuần chay quang phổ rộng chứa chiết xuất bí đao và Vitamin B3 bảo vệ da mụn.', N'Việt Nam', N'Chiết xuất bí đao, 4% Niacinamide, Tinosorb S, Uvinul A Plus', N'Bảo vệ da mụn nhạy cảm, giảm tiết dầu và không gây bít tắc sinh mụn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (55, 18, 8, N'Kem Chống Nắng Thoáng Nhẹ Kiehl''s Ultra Light Daily UV Defense SPF 50', N'Kết cấu gel nước mỏng nhẹ không dầu giúp bảo vệ da trước tia UV và bụi mịn ô nhiễm PM 2.5.', N'Mỹ', N'Chiết xuất rễ cây Hoàng Cầm Baicalin, Vitamin E, Màng lọc Mexoryl', N'Ngăn ngừa đốm nâu nám da và các dấu hiệu lão hóa sớm do môi trường đô thị.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (56, 14, 8, N'Kem Chống Nắng Cho Da Dầu Mụn Bioderma Photoderm AKN Mat SPF 30', N'Sáng chế độc quyền Fluidactiv sinh học giúp duy trì chất lượng bã nhờn tự nhiên không bị oxy hóa đặc quánh.', N'Pháp', N'Phức hợp Fluidactiv, Bột Silica kiềm dầu, Axit Glycolic', N'Ngăn chặn hiệu ứng bùng phát mụn trở lại sau khi ngưng đi nắng (Rebound Effect).', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (57, 7, 8, N'Gel Chống Nắng Dưỡng Ẩm Mát Lạnh Anessa Perfect UV Sunscreen Skincare Gel', N'Kết cấu gel ẩm mượt chứa 50% thành phần dưỡng da giúp làn da luôn ngậm nước căng mọng.', N'Nhật Bản', N'Collagen biển, Chiết xuất hoa cúc vàng, Hyaluronic Acid', N'Bảo vệ da khô mất nước, tạo lớp lót ẩm mịn cho lớp trang điểm bám đều tự nhiên.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (58, 26, 8, N'Kem Chống Nắng Trắng Da DHC Suncut Q10 EX SPF50+ PA++++', N'Bổ sung hoạt chất Coenzyme Q10 và dầu Oliu đậm đặc chống lại quá trình oxy hóa tế bào da.', N'Nhật Bản', N'Coenzyme Q10, Dầu Oliu nguyên chất, Tinh chất rong biển Laminaria', N'Dưỡng trắng da, ngăn ngừa tàn nhang và giữ ẩm mềm mại.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (59, 19, 8, N'Kem Chống Nắng Dưỡng Trắng Đông Y Sulwhasoo UV Wise Brightening Multi Protector', N'Phức hợp Wowsome Formula và tinh chất Bạch sâm bảo vệ da trước tia UV và nhiệt độ cao sinh nhiệt nám.', N'Hàn Quốc', N'Tinh chất Bạch Sâm, Rễ Dâu Tằm, Màng lọc Nano vô cơ', N'Hạ nhiệt làm dịu da, ngăn ngừa sạm nám tận gốc và đem lại làn da sáng mịn rạng ngời.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (60, 2, 8, N'Xịt Chống Nắng Kiềm Dầu Tiện Lợi La Roche-Posay Anthelios Anti-Shine Mist', N'Xịt chống nắng phun sương siêu mịn giúp dặm lại kem chống nắng tiện lợi bên trên lớp trang điểm.', N'Pháp', N'Màng lọc Mexoryl XL, Khoáng chất Airlicium, Nước khoáng LRP', N'Không cần chạm tay, kiềm dầu tức thì và bảo vệ da liên tục cả ngày dài.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (61, 15, 9, N'Nước Hoa Hồng Cấp Ẩm Không Mùi Klairs Supple Preparation Unscented Toner', N'Toner quốc dân thuần chay không cồn, không hương liệu, cân bằng độ pH tức thì và cấp ẩm sâu.', N'Hàn Quốc', N'Hyaluronic Acid, Chiết xuất rau má, Phyto-Oligo, Lipidure-PMB', N'Làm dịu da kích ứng, cân bằng độ ẩm và mở đường cho tinh chất hấp thụ tối đa.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (62, 12, 9, N'Xịt Khoáng Dưỡng Da Làm Dịu Tức Thì Vichy Mineralizing Thermal Spa Water', N'100% nước khoáng núi lửa Pháp nguyên chất chứa 15 khoáng chất quý hiếm củng cố hàng rào phòng vệ.', N'Pháp', N'100% Nước khoáng núi lửa Pháp tự nhiên', N'Làm dịu mát tức thì, hạ nhiệt độ da khi đi nắng và bảo vệ da trước ô nhiễm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (63, 18, 9, N'Nước Cân Bằng Hoa Cúc Cánh Tươi Kiehl''s Calendula Herbal-Extract Toner', N'Toner chứa những cánh hoa cúc vạn thọ tươi được hái thủ công bằng tay giúp làm dịu và kháng viêm da.', N'Mỹ', N'Cánh hoa cúc Calendula tươi, Allantoin, Chiết xuất rễ Ngưu Bàng', N'Cân bằng dầu thừa, làm dịu các nốt mụn đỏ sưng tấy và se khít lỗ chân lông.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (64, 1, 9, N'Nước Hoa Hồng Hậu Giang Cocoon Thu Nhỏ Lỗ Chân Lông & Cấp Ẩm', N'Nước cất hoa hồng hữu cơ kết hợp HA đa phân tử và men vi sinh giúp nuôi dưỡng hệ vi sinh vật có lợi cho da.', N'Việt Nam', N'Nước cất hoa hồng Hậu Giang, Hyaluronic Acid, Men vi sinh Betaine', N'Cấp ẩm sâu, se khít lỗ chân lông và làm mềm bề mặt da khô ráp.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (65, 16, 9, N'Nước Hoa Hồng Dịu Nhẹ Simple Kind To Skin Soothing Facial Toner', N'Công thức cân bằng lành tính chứa Pro-Vitamin B5, hoa cúc Chamomile và cây phỉ Witch Hazel.', N'Anh', N'Pro-Vitamin B5, Chiết xuất Hoa cúc Chamomile, Nước cây phỉ', N'Cân bằng độ pH, làm sạch sâu cặn bẩn còn sót lại mà da vẫn ẩm mượt không căng rát.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (66, 14, 9, N'Nước Hoa Hồng Dưỡng Ẩm Chuyên Sâu Cho Da Khô Bioderma Hydrabio Tonique', N'Sáng chế sinh học Aquagenium kích thích làn da tự sản sinh kênh dẫn nước Aquaporins tự nhiên.', N'Pháp', N'Phức hợp Aquagenium, Vitamin PP, Chiết xuất hạt táo Pyrus Malus', N'Tái tạo khả năng giữ ẩm tự nhiên của tế bào, mang lại làn da căng tràn rạng rỡ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (67, 5, 9, N'Nước Cân Bằng Trị Mụn & Se Lỗ Chân Lông Innisfree Bija Trouble Skin', N'Tinh dầu hạt Bija tự nhiên từ rừng Bija ngàn năm trên đảo Jeju với Salicylic Acid thực vật.', N'Hàn Quốc', N'Dầu hạt Bija Jeju, Salicylic Acid (BHA), Vỏ cây liễu trắng', N'Làm dịu nốt mụn viêm sưng, kháng khuẩn và thanh lọc lỗ chân lông bít tắc.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (68, 4, 9, N'Nước Hoa Hồng Kiềm Dầu & Thu Nhỏ Chân Lông Paula''s Choice Skin Balancing Toner', N'Công thức bổ sung Niacinamide và chất chống oxy hóa giúp điều tiết bã nhờn và phục hồi da dầu.', N'Mỹ', N'Niacinamide, Ceramide, Chiết xuất cúc La Mã Chamomile, Sodium Hyaluronate', N'Giảm bóng dầu, thu nhỏ lỗ chân lông giãn nở và cân bằng hàng rào bảo vệ ẩm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (69, 3, 9, N'Dung Dịch Tẩy Da Chết Làm Sáng Da The Ordinary Glycolic Acid 7% Toning Solution', N'Chứa 7% Glycolic Acid (AHA) kết hợp chiết xuất hạt tiêu Tasmania làm giảm kích ứng khi tẩy da chết.', N'Canada', N'7% Glycolic Acid, Chiết xuất tiêu núi Tasmania, Nha đam, Nhân sâm', N'Tẩy sạch lớp sừng già cỗi, làm mờ thâm mụn, cho làn da bóng bẩy mịn màng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (70, 2, 9, N'Nước Hoa Hồng Giảm Dầu & Se Khít Lỗ Chân Lông La Roche-Posay Effaclar Astringent', N'Chứa hoạt chất LHA tẩy tế bào chết vi mô dịu nhẹ kết hợp nước khoáng nhiệt trị liệu.', N'Pháp', N'Capryloyl Salicylic Acid (LHA), Nước khoáng La Roche-Posay, Citric Acid', N'Thu nhỏ lỗ chân lông, ngăn chặn tích tụ bã nhờn gây mụn đầu đen.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (71, 17, 9, N'Nước Cân Bằng Cấp Ẩm Cho Da Nhạy Cảm Eucerin DermatoClean Hyaluron Toner', N'Phức hợp APG nhẹ nhàng làm sạch kết hợp Gluco-Glycerol và Hyaluronic Acid cấp ẩm sâu.', N'Đức', N'APG Complex, Hyaluronic Acid, Gluco-Glycerol', N'Giúp da thông thoáng, hô hấp tốt hơn và chuẩn bị tối ưu cho bước dưỡng tiếp theo.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (72, 1, 9, N'Toner Nghệ Hưng Yên Cocoon Dưỡng Sáng & Mờ Thâm 140ml', N'Chiết xuất nghệ Hưng Yên giàu curcuminoid kết hợp 5% Niacinamide phục hồi da thâm sạm.', N'Việt Nam', N'Chiết xuất củ nghệ Hưng Yên, 5% Niacinamide, Betaine', N'Làm sáng da xỉn màu, ức chế sắc tố melanin sau mụn và chống oxy hóa mạnh mẽ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (73, 8, 9, N'Nước Hoa Hồng Tươi Mát & Căng Mịn L''Oréal Paris Revitalift Aqua Milky Toner', N'Kết cấu sữa lỏng giàu tinh chất dưỡng ẩm giúp làn da mềm mượt ngay tức khắc.', N'Pháp', N'Hoạt chất Dermalift, Pro-Retinol A, Glycerin', N'Tăng cường độ đàn hồi, ngăn ngừa xuất hiện nếp nhăn sớm và cấp ẩm mịn màng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (74, 19, 9, N'Nước Cân Bằng Thiết Yếu Dưỡng Ẩm Sulwhasoo Essential Comfort Balancing Water', N'Công thức thảo mộc JAUM Moisturizing Complex làm dịu làn da mệt mỏi căng thẳng.', N'Hàn Quốc', N'JAUM Moisturizing Complex, Rễ Thược Dược, Hạt Sen Tươi', N'Cấp ẩm sâu êm dịu, khôi phục trạng thái mềm mượt thư thái cho làn da.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (75, 2, 9, N'Xịt Khoáng Làm Dịu & Giảm Bóng Nhờn La Roche-Posay Serozinc Zinc Sulfate', N'Công thức độc đáo chứa khoáng Kẽm Sunfat kháng viêm, giảm tiết bã nhờn tức thì cho da mụn.', N'Pháp', N'Kẽm Sunfat (Zinc Sulfate 0.10%), Muối khoáng NaCl, Nước tinh khiết LRP', N'Kiềm dầu, làm dịu nốt mụn sưng đỏ và bảo vệ bề mặt da khỏi vi khuẩn.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (76, 14, 10, N'Nước Tẩy Trang Dịu Nhẹ Cho Da Nhạy Cảm Bioderma Sensibio H2O', N'Nước tẩy trang công nghệ Micellar số 1 thế giới với hạt micelle sinh học tương thích làn da.', N'Pháp', N'Hạt Micelle sinh học, Chiết xuất dưa leo tự nhiên, Nước siêu tinh khiết', N'Làm sạch 99% lớp trang điểm và bụi mịn PM 2.5 mà hoàn toàn không cần rửa lại với nước.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (77, 8, 10, N'Nước Tẩy Trang Tươi Mát Kiềm Dầu L''Oréal Paris Micellar Water 3-in-1 Deep Cleansing', N'Công nghệ Bi-phase 2 lớp dầu và nước làm sạch tức thì kể cả mascara và son lì kháng nước.', N'Pháp', N'Hạt Micelle hút dầu, Nước khoáng ngầm Vosges Pháp, Dầu dưỡng nhẹ', N'Hòa tan lớp trang điểm cứng đầu chỉ với một lần lau, không gây nhờn dính.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (78, 1, 10, N'Dầu Tẩy Trang Hoa Hồng Cocoon Làm Sạch Sâu & Ẩm Mượt 140ml', N'Dầu tẩy trang thuần chay từ dầu hoa hồng hữu cơ và vitamin E nhũ hóa cực nhanh không gây mụn ẩn.', N'Việt Nam', N'Dầu hoa hồng hữu cơ Hậu Giang, Vitamin E, Tinh dầu hoa cúc', N'Hòa tan bã nhờn và cặn trang điểm trong lỗ chân lông, mang lại làn da mềm ẩm mịn màng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (79, 26, 10, N'Dầu Tẩy Trang Ô Liu Làm Sạch Sâu DHC Deep Cleansing Oil', N'Tuyệt tác làm sạch huyền thoại Nhật Bản chứa dầu oliu hữu cơ Flor de Aceite tinh khiết nhất.', N'Nhật Bản', N'Dầu Olive nguyên chất hữu cơ Tây Ban Nha, Dầu hương thảo, Vitamin E', N'Cuốn trôi mụn đầu đen, bã nhờn cứng đầu và lớp makeup dày cộm mà vẫn bảo vệ độ ẩm.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (80, 16, 10, N'Nước Tẩy Trang Dịu Nhẹ Cho Da Nhạy Cảm Simple Kind To Skin Micellar Water', N'Công thức dịu nhẹ chứa vitamin B3, vitamin C và nước tinh khiết làm sạch mà không làm khô da.', N'Anh', N'Hạt Micellar thông minh, Vitamin B3 (Niacinamide), Vitamin C, Nước cất', N'Tẩy sạch tạp chất và bụi bẩn hằng ngày nhẹ nhàng, không gây cay mắt.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (81, 2, 10, N'Nước Tẩy Trang Cho Da Dầu Nhạy Cảm La Roche-Posay Effaclar Micellar Water Ultra', N'Công nghệ cải tiến Glyco-Micellar kết hợp muối kẽm PCA làm sạch bã nhờn và tạp chất siêu tốc.', N'Pháp', N'Công nghệ Glyco-Micellar, Kẽm PCA, Nước khoáng La Roche-Posay', N'Giảm bóng dầu, duy trì độ pH sinh lý và ngăn ngừa hình thành mụn mới.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (82, 11, 10, N'Nước Tẩy Trang Mắt & Môi Chuyên Dụng Maybelline Eye & Lip Makeup Remover', N'Công thức 2 lớp dầu nước chuyên biệt hòa tan mascara chống trôi và son lì lì bám chặt.', N'Mỹ', N'Dầu dưỡng mi, Nước khoáng thanh khiết, Panthenol nuôi dưỡng mi', N'Tẩy sạch êm ái vùng da mắt môi nhạy cảm mà không cần chà xát mạnh gây rụng mi.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (83, 14, 10, N'Nước Tẩy Trang Kiềm Dầu Cho Da Dầu Mụn Bioderma Sébium H2O Nắp Xanh', N'Công nghệ Micellar kết hợp kẽm Gluconate và đồng Sulphate thanh lọc bã nhờn sâu trong lỗ chân lông.', N'Pháp', N'Hạt Micelle Sébium, Đồng Sulphate, Kẽm Gluconate, Ginkgo Biloba', N'Giảm bóng nhờn, ngăn ngừa mụn và giữ cho da thông thoáng cả ngày.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (84, 5, 10, N'Dầu Tẩy Trang Táo Xanh Sạch Sâu Innisfree Apple Seed Cleansing Oil', N'Dầu hạt táo xanh đảo Jeju giàu acid béo chưa bão hòa giúp làm tan chảy lớp trang điểm dày.', N'Hàn Quốc', N'Dầu hạt táo ép lạnh Jeju, Chiết xuất vỏ táo xanh tươi mát', N'Làm sạch mịn màng, thông thoáng lỗ chân lông với hương thơm táo ngọt ngào.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (85, 17, 10, N'Nước Tẩy Trang Cho Da Nhờn Mụn Eucerin Pro Acne Solution Micellar Water', N'Hợp chất làm sạch APG dịu nhẹ kết hợp Arginine làm dịu vết mụn viêm và giảm kích ứng da.', N'Đức', N'APG Complex, Arginine, Sodium Hyaluronate', N'Tẩy sạch bã nhờn dư thừa, ngăn ngừa mụn ẩn mà không làm khô căng hay rát da.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (86, 1, 10, N'Nước Tẩy Trang Bí Đao Cocoon Làm Sạch Dầu Mụn 500ml', N'Nước tẩy trang thuần chay ứng dụng công nghệ NatraGem S140 kết hợp tràm trà kháng khuẩn.', N'Việt Nam', N'Chiết xuất bí đao, Tinh dầu tràm trà hữu cơ, 3% Niacinamide', N'Làm sạch dầu nhờn và lớp kem chống nắng, làm dịu vết đỏ của nốt mụn viêm.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (87, 15, 10, N'Dầu Tẩy Trang Làm Dịu Da Đen Tuyền Klairs Gentle Black Deep Cleansing Oil', N'Phức hợp 4 loại tinh dầu hạt thực vật đen (đậu đen, mè đen, hạt nho, dầu jojoba) giàu dưỡng chất.', N'Hàn Quốc', N'Dầu đậu đen lên men, Dầu mè đen hữu cơ, Dầu hạt nho, Dầu bơ hạt mỡ', N'Hút sạch bã nhờn và nhân mụn cám, cấp ẩm nuôi dưỡng bề mặt da mịn màng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (88, 4, 10, N'Sáp Tẩy Trang Làm Dịu Da Nhạy Cảm Paula''s Choice Omega+ Complex Cleansing Balm', N'Sáp tẩy trang tan mịn màng như dầu giàu phức hợp Omega 3-6-9 phục hồi màng lipid tế bào da.', N'Mỹ', N'Omega 3-6-9 Complex, Dầu hạt Meadowfoam, Dầu hạt Jojoba', N'Hòa tan lớp trang điểm dày cộm mà vẫn duy trì độ ẩm mịn mượt mà cho da nhạy cảm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (89, 18, 10, N'Dầu Tẩy Trang Thảo Mộc Ban Đêm Kiehl''s Midnight Recovery Botanical Cleansing Oil', N'Tinh dầu hoa dạ thảo, hoa oải hương và tinh dầu squalane giúp thanh lọc da và thư giãn tinh thần.', N'Mỹ', N'Tinh dầu Hoa dạ thảo Evening Primrose, Tinh dầu Lavender, Squalane', N'Làm sạch sâu tạp chất ban ngày, nuôi dưỡng da căng bóng ngậm nước vào ban đêm.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (90, 9, 10, N'Nước Tẩy Trang Cấp Nước Tươi Mới Laneige Water Bank Micellar Cleansing Water', N'Chứa Blue Hyaluronic Acid phân tử siêu nhỏ hút sạch bụi mịn bám sâu trong lỗ chân lông.', N'Hàn Quốc', N'Blue Hyaluronic Acid, Men nấm men Beta-Glucan, Dầu hạt Meadowfoam', N'Làm sạch dịu nhẹ không cần rửa lại, lưu lại độ ẩm mượt mát lành trên da.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (91, 4, 11, N'Dung Dịch Tẩy Tế Bào Chết Hóa Học Paula''s Choice BHA 2% Liquid', N'Sản phẩm kinh điển chứa 2% BHA tan trong dầu giúp loại bỏ mụn đầu đen, bã nhờn tích tụ, đem lại bề mặt da mịn màng.', N'Mỹ', N'2% Salicylic Acid (BHA), Chiết xuất Trà xanh Camellia Oleifera, Methylpropanediol', N'Loại bỏ tế bào chết sâu trong nang lông, cải thiện mụn ẩn và thu nhỏ chân lông.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (92, 3, 11, N'Tinh Chất Peel Da Tẩy Tế Bào Chết The Ordinary AHA 30% + BHA 2% Peeling Solution', N'Serum peel da màu đỏ đặc trưng kết hợp 30% AHA và 2% BHA giúp tái tạo bề mặt da láng mịn.', N'Canada', N'30% AHA (Glycolic, Lactic, Tartaric, Citric), 2% BHA (Salicylic Acid), Cà rốt đen', N'Làm mờ vết thâm mụn, đều màu da và kích thích tái sinh tế bào biểu bì mới.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (93, 1, 11, N'Cà Phê Đắk Lắk Làm Sạch Da Chết Mặt Cocoon Dak Lak Coffee Face Polish', N'Hạt cà phê Đắk Lắk xay siêu mịn kết hợp bơ ca cao Tiền Giang giúp lấy đi lớp sừng già cỗi mà không rát da.', N'Việt Nam', N'Hạt cà phê Đắk Lắk xay nhuyễn, Bơ ca cao Tiền Giang, Dầu hoa rum', N'Làm sạch tế bào chết, cho làn da mặt mềm mịn, sáng bóng và đều màu.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (94, 15, 11, N'Mặt Nạ & Tẩy Da Chết Đường Đen Klairs Youthful Glow Sugar Mask', N'Mặt nạ đa năng 3 trong 1 tẩy da chết với tinh thể đường đen hữu cơ và bơ hạt mỡ làm mềm da.', N'Hàn Quốc', N'Đường đen hữu cơ, Bơ hạt mỡ, Dầu hạt jojoba, Dầu hạt bưởi', N'Tẩy sạch da chết nhẹ dịu, dưỡng ẩm và làm mềm các vùng da bong tróc sần sùi.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (95, 17, 11, N'Tẩy Tế Bào Chết Ngừa Mụn Eucerin Pro Acne Solution Scrub', N'Các vi hạt siêu nhỏ tác động nhẹ nhàng lên da kết hợp Acid Lactic làm thông thoáng chân lông.', N'Đức', N'Lactic Acid (AHA tự nhiên), Vi hạt Polyethylene siêu mịn', N'Giảm mụn đầu đen và mụn cám, không làm khô căng hay tổn thương da.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (96, 5, 11, N'Mặt Nạ Tẩy Tế Bào Chết Tro Núi Lửa Innisfree Volcanic Calming Pore Clay Mask', N'Đá tro núi lửa Jeju kết hợp AHA/PHA làm sạch tế bào chết và bã nhờn dịu nhẹ mỗi ngày.', N'Hàn Quốc', N'Tro núi lửa Jeju Sphere, Phức hợp AHA/PHA, Nước khoáng có ga', N'Hút sạch bã nhờn dư thừa, làm dịu da nhạy cảm và se nhỏ chân lông.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (97, 8, 11, N'Mặt Nạ Đất Sét Tẩy Tế Bào Chết L''Oréal Paris Pure Clay Mask Illuminating', N'Kết hợp 3 loại đất sét khoáng tự nhiên từ núi lửa Pháp và chiết xuất sả làm sạch tế bào xỉn màu.', N'Pháp', N'Đất sét Kaolin, Đất sét Montmorillonite, Chiết xuất sả Yuzu', N'Thanh lọc tạp chất, làm sáng bừng làn da và tái tạo độ mềm mịn tức thì.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (98, 14, 11, N'Gel Tẩy Tế Bào Chết Cho Da Dầu Mụn Bioderma Sébium Gel Gommant', N'Hạt vi tẩy tế bào chết Polyethylene kết hợp Acid Glycolic và Salicylic tác động kép cơ học và hóa học.', N'Pháp', N'Fluidactiv, Axit Glycolic, Axit Salicylic, Vi hạt tròn', N'Làm sạch bã nhờn sâu trong chân lông, giảm bóng dầu và ngăn ngừa mụn cám.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (99, 16, 11, N'Tẩy Tế Bào Chết Dịu Nhẹ Simple Kind To Skin Smoothing Facial Scrub', N'Chiết xuất hạt gạo li ti tự nhiên kết hợp Pro-Vitamin B5 tẩy da chết dịu êm cho da nhạy cảm.', N'Anh', N'Hạt gạo xay mịn, Pro-Vitamin B5, Vitamin E', N'Loại bỏ nhẹ nhàng tế bào chết, trả lại làn da mịn mượt không hề châm chích.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (100, 18, 11, N'Kem Tẩy Tế Bào Chết Tái Tạo Bề Mặt Da Kiehl''s Epidermal Re-Texturizing Micro-Dermabrasion', N'Công nghệ siêu mài mòn vi mô với tinh thể vỏ sò tự nhiên kết hợp chiết xuất liễu trắng thảo mộc.', N'Mỹ', N'Bột vi tinh thể vỏ sò tự nhiên, Chiết xuất cây liễu trắng, Chiết xuất cúc La Mã', N'Làm phẳng bề mặt da thô ráp, làm mờ vết thâm và sẹo mụn nhẹ sau 1 lần dùng.', 1, N'ACTIVE', N'2026-01-01 00:00:00');
GO

INSERT INTO dbo.products (id, brand_id, category_id, name, description, origin, main_ingredients, benefits, is_featured, status, created_at) VALUES
    (101, 4, 11, N'Gel Tẩy Tế Bào Chết Chống Lão Hóa Paula''s Choice Skin Perfecting 8% AHA Gel', N'Chứa 8% Glycolic Acid (AHA) kết hợp chiết xuất hoa cúc và cúc vạn thọ làm sáng mịn da khô lão hóa.', N'Mỹ', N'8% Glycolic Acid, Chiết xuất Cúc La Mã Chamomile, Lô hội', N'Đánh bay các vùng da khô sần sùi, làm mờ nếp nhăn li ti và kích thích sản sinh collagen.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (102, 1, 11, N'Dung Dịch Tẩy Da Chết Bí Đao Cocoon 2% BHA 140ml', N'BHA 2% tan trong dung môi tự nhiên chiết xuất bí đao giúp thẩm thấu sâu làm sạch mụn ẩn mụn đầu đen.', N'Việt Nam', N'2% Salicylic Acid (BHA), Chiết xuất bí đao, Nước cất hoa hồng', N'Đẩy mụn ẩn, làm thông thoáng lỗ chân lông và hạn chế tiết bã nhờn.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (103, 12, 11, N'Mặt Nạ Tẩy Tế Bào Chết Đất Sét Khoáng Núi Lửa Vichy Purifying Clay Mask', N'Kết hợp 2 loại đất sét Kaolin và Bentonite cùng nước khoáng núi lửa hút sạch độc tố dưới da.', N'Pháp', N'Đất sét Kaolin, Bentonite, Nước khoáng Vichy, Tinh chất Lô Hội', N'Thu nhỏ lỗ chân lông, kiềm dầu thừa và đem lại làn da sáng mịn khỏe mạnh.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (104, 26, 11, N'Gel Tẩy Tế Bào Chết Sáng Mịn Da DHC Mineral Face Scrub', N'Hạt mơ thiên nhiên tự nhiên nghiền mịn kết hợp tro núi lửa làm sạch lớp sừng trên bề mặt da.', N'Nhật Bản', N'Hạt quả mơ nghiền mịn, Bùn khoáng biển sâu, Tinh dầu Oliu', N'Làm sáng da xỉn màu, kích thích tuần hoàn máu và đem lại vẻ bóng mượt.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (105, 19, 11, N'Gel Tẩy Tế Bào Chết Đông Y Dịu Nhẹ Sulwhasoo White Ginseng Radiance Mask', N'Chứa bột Bạch sâm tự nhiên quý hiếm giúp thanh lọc da chết trong khi tắm hoặc thư giãn.', N'Hàn Quốc', N'Bột Bạch sâm Hàn Quốc, Mật ong tự nhiên, Tinh chất thảo mộc Đông y', N'Thanh lọc độc tố, cấp ẩm sâu và nuôi dưỡng làn da sáng trong như ngọc bích.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (106, 9, 12, N'Mặt Nạ Ngủ Cấp Nước Chuyên Sâu Laneige Water Sleeping Mask EX', N'Công nghệ Sleeping Micro Biome kết hợp phức hợp Pro-biotics và Squalane thanh lọc da trong giấc ngủ.', N'Hàn Quốc', N'Sleeping Micro Biome, Pro-biotics Complex, Squalane thực vật', N'Phục hồi làn da rạng rỡ, căng mọng ngậm nước sau khi thức giấc vào sáng hôm sau.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (107, 18, 12, N'Mặt Nạ Đất Sét Hút Sạch Dầu Kiehl''s Rare Earth Deep Pore Cleansing Masque', N'Mặt nạ đất sét trắng Amazon huyền thoại của Kiehl''s hút sạch độc tố, bã nhờn và nhân mụn đầu đen.', N'Mỹ', N'Đất sét trắng vùng Amazon, Yến mạch nghiền, Chiết xuất Nha Đam', N'Thanh lọc lỗ chân lông, kiềm dầu đến 95% và se nhỏ chân lông tức thì.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (108, 5, 12, N'Mặt Nạ Đất Sét Tro Núi Lửa Innisfree Super Volcanic Pore Clay Mask 2X', N'Công nghệ hạt tro núi lửa Jeju 2X hút sạch gấp đôi lượng bã nhờn sâu trong lỗ chân lông.', N'Hàn Quốc', N'Tro núi lửa đảo Jeju 2X, Đất sét khoáng, Bột óc chó, AHA', N'Thu nhỏ lỗ chân lông, tẩy da chết nhẹ nhàng và làm mát dịu da tức thì.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (109, 1, 12, N'Mặt Nạ Bí Đao Cocoon Kháng Viêm & Giảm Mụn Mờ Thâm 100ml', N'Mặt nạ thuần chay kết hợp dịch chiết bí đao, rau má và tràm trà hút sạch dầu thừa trên da mụn.', N'Việt Nam', N'Chiết xuất bí đao, Tinh dầu tràm trà hữu cơ, Chiết xuất rau má, Đất sét Kaolin', N'Làm dịu vết mụn đỏ, kháng viêm và giảm tiết bã nhờn rõ rệt sau 20 phút.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (110, 15, 12, N'Mặt Nạ Giấy Làm Dịu Khẩn Cấp Klairs Midnight Blue Calming Sheet Mask', N'Mặt nạ giấy sợi tre chứa chiết xuất Guaiazulene chiết xuất từ hoa cúc La Mã màu xanh biển dịu mát.', N'Hàn Quốc', N'Guaiazulene hoa cúc, Chiết xuất Rau má Centella Asiatica, Bột than tre', N'Hạ nhiệt tức thì cho làn da bị cháy nắng hoặc kích ứng mẩn đỏ sau nặn mụn.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (111, 8, 12, N'Mặt Nạ Giấy Căng Mướt Da L''Oréal Paris Revitalift Crystal Micro-Essence Mask', N'Chứa lượng dưỡng chất tương đương 1 chai tinh chất Micro-Essence với công nghệ màng cotton siêu mỏng.', N'Pháp', N'Hoạt chất Salicylic Acid (BHA), Dưỡng chất Centella Asiatica', N'Thẩm thấu sâu gấp 10 lần, đem lại làn da sáng trong trẻo căng mọng tức thì.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (112, 14, 12, N'Mặt Nạ Cấp Ẩm Phục Hồi Khẩn Cấp Bioderma Sensibio Mask', N'Liệu pháp điều trị chuyên sâu cấp nước tức thì cho làn da nhạy cảm mất nước bị ngứa rát.', N'Pháp', N'Sáng chế Toléridine, Glycyrrhetinic Acid, Xylitol, Fructooligosaccharides', N'Hạ nhiệt cảm giác bốc hỏa, tăng cường ngưỡng chịu đựng sinh học của làn da.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (113, 12, 12, N'Mặt Nạ Ngủ Dưỡng Ẩm Chuyên Sâu Ban Đêm Vichy Aqualia Thermal Night Spa', N'Gel mặt nạ ngủ chứa nước khoáng Vichy và Hyaluronic Acid biến giấc ngủ thành liệu trình spa tại nhà.', N'Pháp', N'15 Khoáng chất núi lửa Vichy, Hyaluronic Acid đa phân tử, Caffeine', N'Tái sinh làn da mệt mỏi, cấp nước suốt 48 giờ cho vẻ rạng rỡ căng tràn vào sáng hôm sau.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (114, 17, 12, N'Mặt Nạ Giấy Chống Lão Hóa & Cấp Nước Eucerin Hyaluron-Filler Intensive Mask', N'Mặt nạ chất liệu sợi sinh học biocellulose chứa Hyaluronic Acid chuỗi phân tử cực ngắn.', N'Đức', N'Hyaluronic Acid phân tử siêu nhỏ, Sợi sinh học Biocellulose', N'Làm đầy nếp nhăn tức thì sau 5 phút, cấp nước dồi dào cho làn da mệt mỏi.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (115, 19, 12, N'Mặt Nạ Lột Đông Y Làm Sáng Da Sulwhasoo Clarifying Mask', N'Chiết xuất bột Ngọc Trúc kết hợp mật ong tự nhiên nhẹ nhàng lấy đi tạp chất tế bào chết bám lì.', N'Hàn Quốc', N'Bột Ngọc Trúc Solomon''s Seal, Mật ong rừng nguyên chất, Hoa Kim Ngân', N'Làm sạch bề mặt da xỉn màu, se khít chân lông và đem lại làn da sáng bóng trong suốt.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (116, 1, 12, N'Mặt Nạ Hoa Hồng Cocoon Dưỡng Ẩm & Mềm Mượt 100ml', N'Cánh hoa hồng tươi Hậu Giang xay nhuyễn kết hợp Hyaluronic Acid cấp ẩm sâu cho da khô thiếu nước.', N'Việt Nam', N'Cánh hoa hồng tươi Hậu Giang, Hyaluronic Acid, Niacinamide', N'Làm mềm các mảng da khô ráp bong tróc, lưu giữ làn da thơm ngọt ngào.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (117, 18, 12, N'Mặt Nạ Hoa Cúc & Lô Hội Làm Dịu Da Kiehl''s Calendula Petal-Infused Calming', N'Kết cấu gel mát lạnh chứa cánh hoa cúc vạn thọ và lô hội làm dịu làn da mệt mỏi căng thẳng tức thì.', N'Mỹ', N'Cánh hoa cúc Calendula tươi, Chiết xuất Lô Hội tự nhiên, Tinh dầu hoa cúc', N'Hồi sinh làn da mất nước, giảm mẩn đỏ và mang lại cảm giác mát rượi thư thái.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (118, 5, 12, N'Mặt Nạ Giấy Trà Xanh Dưỡng Ẩm Tươi Mới Innisfree My Real Squeeze Mask Green Tea', N'Chất liệu 100% cellulose từ cây bạch đàn mỏng nhẹ ôm sát khuôn mặt giàu dưỡng chất trà xanh ép tươi.', N'Hàn Quốc', N'Nước ép lá trà xanh Jeju nguyên chất, Glycerin, Betaine', N'Cấp ẩm nhanh chóng, làm dịu da sau khi đi nắng về.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (119, 13, 12, N'Mặt Nạ Giấy Ban Đêm Phục Hồi Thần Tốc Estée Lauder Advanced Night Repair PowerFoil Mask', N'Mặt nạ lá kim loại bạc 2 lớp công nghệ PowerFoil giữ lại dưỡng chất thẩm thấu nhanh gấp 25 lần.', N'Mỹ', N'ChronoluxCB đậm đặc, Men Bifida Ferment, Hyaluronic Acid nồng độ kép', N'Hồi sinh làn da cạn kiệt năng lượng chỉ sau 10 phút, da căng bóng như vừa đi spa.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (120, 9, 12, N'Mặt Nạ Ngủ Môi Hương Quả Mọng Laneige Lip Sleeping Mask Berry Mini', N'Mặt nạ ngủ môi huyền thoại với phức hợp Berry Fruit Complex giàu vitamin C nhẹ nhàng làm tan tế bào chết.', N'Hàn Quốc', N'Berry Fruit Complex, Dầu hạt dừa, Vitamin C, Bơ hạt mỡ', N'Phục hồi đôi môi khô nứt nẻ, mang lại bờ môi căng mọng hồng hào sau đêm dài.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (121, 10, 13, N'Son Thỏi Lì Huyền Thoại MAC Matte Lipstick', N'Biểu tượng làm đẹp toàn cầu của MAC với công thức son thỏi lì trứ danh, chất son lướt êm ái trên bờ môi.', N'Mỹ', N'Sáp vi tinh thể, Sắc tố khoáng siêu mịn, Dầu dưỡng thực vật', N'Lên màu chuẩn sắc ngay lần quẹt đầu tiên, lưu giữ màu sắc tươi tắn suốt cả ngày dài.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (122, 5, 13, N'Son Kem Lì Mịn Môi Cao Cấp Innisfree Matte Velvet Tint', N'Chất son nhung mờ mịn màng lâu trôi đến 8 giờ, cho đôi môi vẻ đẹp cuốn hút tự nhiên không khô vân môi.', N'Hàn Quốc', N'Dầu jojoba hữu cơ, sắc tố màu khoáng thiên nhiên', N'Màu son lên chuẩn sắc, kết cấu nhẹ tựa mây tạo hiệu ứng viền môi mờ ảo.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (123, 9, 13, N'Mặt Nạ Ngủ Dưỡng Ẩm & Phục Hồi Môi Laneige Lip Sleeping Mask Berry', N'Mặt nạ ngủ môi huyền thoại với hương dâu rừng ngọt ngào tan chảy, loại bỏ hoàn toàn tình trạng khô nẻ bong tróc.', N'Hàn Quốc', N'Phức hợp Berry Fruit Complex, Vitamin C, Dầu hạt dừa', N'Nhẹ nhàng loại bỏ tế bào chết trên môi qua đêm, mang lại đôi môi căng mịn hồng hào.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (124, 24, 13, N'Son Kem Lì Siêu Mịn 3CE Velvet Lip Tint', N'Dòng son kem lì làm nên tên tuổi của 3CE với chất son xốp mềm phủ đầy rãnh môi tạo hiệu ứng mịn như nhung.', N'Hàn Quốc', N'Chiết xuất dầu hạt hướng dương, Tinh dầu hoa cúc, Sắc tố khoáng', N'Bền màu suốt 6-8 tiếng, không gây cảm giác nặng môi hay bột dính.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (125, 23, 13, N'Son Tint Bóng Căng Mọng Môi Rom&nd Juicy Lasting Tint', N'Son tint bóng tạo hiệu ứng bờ môi căng mọng như phủ lớp siro đường hồ lô ngọt ngào.', N'Hàn Quốc', N'Chiết xuất trái cây tự nhiên (kiwi, xoài, đu đủ), Lớp màng khóa bóng', N'Lớp tint bám màu cực dai dẳng sau khi ăn uống, lớp bóng nhẹ tênh không bết dính.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (126, 20, 13, N'Son Dưỡng Có Màu Cao Cấp Dior Addict Lip Glow', N'Cây son dưỡng công chúa huyền thoại thế giới với công nghệ Color Reviver kích hoạt sắc hồng tự nhiên theo độ pH.', N'Pháp', N'Bơ xoài hữu cơ, Dầu hạt anh đào, Axit Hyaluronic dưỡng môi', N'Dưỡng ẩm suốt 24 giờ, phục hồi sắc môi hồng hào tươi tắn tự nhiên.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (127, 22, 13, N'Son Thỏi Lì Siêu Mỏng Vuông YSL Rouge Pur Couture The Slim Matte', N'Thiết kế đầu thỏi son vuông vắn góc cạnh sắc sảo giúp kẻ viền môi chuẩn xác chỉ trong một lần chạm.', N'Pháp', N'Dầu dưỡng thực vật Jojoba, Sắc tố hạt màu micronized siêu mịn', N'Chất son lì mềm mượt nhẹ như không khí, lên màu đậm đà quý phái.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (128, 21, 13, N'Son Thỏi Lì Cao Cấp Chanel Rouge Allure Velvet', N'Thỏi son bấm kinh điển nắp bật sang trọng mang lại lớp finish mịn lì như nhung lụa cao cấp.', N'Pháp', N'Dầu jojoba, Bơ hạt mỡ, Hạt ngọc trai siêu nhỏ khuếch tán ánh sáng', N'Màu son thời thượng lộng lẫy, giữ cho bờ môi mềm mại không khô căng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (129, 11, 13, N'Son Kem Lì Lâu Trôi 16H Maybelline Superstay Matte Ink', N'Công nghệ mực xăm siêu bền màu suốt 16 giờ, ăn uống thả ga không lem không trôi dính cốc.', N'Mỹ', N'Polyme đàn hồi định hình màu, Dầu dưỡng nhẹ', N'Lên màu sắc nét bền bỉ bất chấp thời gian, không dính ra khẩu trang.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (130, 23, 13, N'Son Kem Lì Mịn Lì Như Nhung Rom&nd Zero Velvet Tint', N'Chất son nhẹ tênh cảm giác như không trọng lượng Zero grams, phủ đều nếp nhăn môi hoàn hảo.', N'Hàn Quốc', N'Bột khoáng silica siêu mịn, Tinh dầu hoa hồng dại', N'Hiệu ứng mờ lì ảo diệu, bám màu tốt không gây cảm giác khô nứt.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (131, 24, 13, N'Son Thỏi Lì Cổ Điển 3CE Mood Recipe Matte Lip Color', N'Bảng màu tông đất thời thượng tạo nên cơn sốt toàn châu Á với thiết kế vỏ vuông đồng màu son.', N'Hàn Quốc', N'Sáp sáp vi tinh thể, Bột màu sắc tố nén cao độ', N'Màu son tiệp chặt vào môi, tôn tone da châu Á rạng rỡ thu hút ánh nhìn.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (132, 1, 13, N'Son Dưỡng Dầu Dừa Bến Tre Cocoon Mềm Môi Mờ Thâm 5g', N'Son dưỡng thuần chay từ dầu dừa Bến Tre ép lạnh nguyên chất kết hợp bơ hạt mỡ dưỡng ẩm chuyên sâu.', N'Việt Nam', N'Dầu dừa nguyên chất Bến Tre, Bơ hạt mỡ, Vitamin E thực vật', N'Loại bỏ tình trạng môi khô nứt nẻ, giảm thâm môi và giữ bờ môi căng mọng mềm mại.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (133, 26, 13, N'Son Dưỡng Trị Thâm Môi Không Màu DHC Lip Cream 1.5g', N'Cây son dưỡng trị thâm môi bán chạy số 1 Nhật Bản với hơn 100 triệu thỏi được bán ra trên toàn cầu.', N'Nhật Bản', N'Dầu Oliu nguyên chất, Chiết xuất Lô Hội, Cam Thảo, Vitamin E', N'Trị thâm môi, dưỡng ẩm chống nứt nẻ suốt cả ngày không gây bóng nhẫy.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (134, 10, 13, N'Son Kem Lì Siêu Nhẹ Môi MAC Powder Kiss Liquid Lipcolour', N'Công thức mousse xốp nhẹ như mây phủ lớp mờ ảo sương khói lên bờ môi gợi cảm.', N'Mỹ', N'Bột khoáng làm mờ rãnh môi, Vitamin E chống khô', N'Chất son xốp mềm dưỡng ẩm đến 10 giờ, màu sắc thời thượng quyến rũ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (135, 8, 13, N'Son Mịn Mờ Lâu Trôi L''Oréal Paris Rouge Signature Matte Liquid Lipstick', N'Dòng son kem lì mỏng nhẹ gấp 2 lần son thông thường nhờ công nghệ hạt màu siêu mịn hạt nano.', N'Pháp', N'Công nghệ hạt màu siêu vi nano, Polyme bám dính nhẹ tênh', N'Lên màu chuẩn xác tự nhiên, không gây cảm giác có son trên môi suốt ngày dài.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (136, 11, 14, N'Kem Nền Mịn Lì Kiềm Dầu Maybelline Fit Me Matte + Poreless Foundation', N'Dòng kem nền quốc dân kiềm dầu đỉnh cao với hạt phấn Micro-powders che phủ hoàn hảo lỗ chân lông.', N'Mỹ', N'Hạt phấn Micro-powders, Đất sét khoáng hút nhờn, Titanium Dioxide', N'Tạo hiệu ứng mịn lì tự nhiên, kiềm dầu đến 12 giờ, không bóng nhờn không xuống tone.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (137, 9, 14, N'Phấn Nước Kiềm Dầu Che Phủ Hoàn Hảo Laneige Neo Cushion Matte SPF42 PA++', N'Phấn nước thế hệ mới với công nghệ hạt phấn siêu mịn kiềm dầu 24h không dính vào khẩu trang.', N'Hàn Quốc', N'Bột hấp thụ bã nhờn, Panthenol dưỡng ẩm, Bộ lọc chống ánh sáng xanh', N'Che phủ khuyết điểm và lỗ chân lông hoàn hảo, kháng nước kháng mồ hôi suốt cả ngày.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (138, 13, 14, N'Kem Nền Giữ Màu Bền Bỉ 24H Estée Lauder Double Wear Stay-in-Place', N'Huyền thoại kem nền che phủ hoàn hảo bền màu số 1 thế giới không lem trôi dưới mọi thời tiết nóng ẩm.', N'Mỹ', N'Polyme giữ màu định hình, Khoáng chất hấp thụ dầu, SPF 10', N'Che phủ khuyết điểm hoàn hảo, chống mồ hôi và duy trì lớp nền tươi mới suốt 24 giờ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (139, 20, 14, N'Kem Nền Căng Bóng Tự Nhiên Dior Forever Skin Glow 24H', N'Kem nền cao cấp kết hợp 86% chiết xuất hoa tự nhiên giúp nuôi dưỡng làn da căng mọng rạng ngời.', N'Pháp', N'Chiết xuất Hoa Diên Vĩ, Hoa Păng-xê hoang dã, Hoa Hồng Trắng', N'Hiệu ứng căng bóng khỏe khoắn, cấp ẩm sâu và chống xỉn màu suốt 24 giờ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (140, 10, 14, N'Kem Nền Kiềm Dầu Đa Năng MAC Studio Fix Fluid SPF 15', N'Dòng kem nền chuẩn Studio chuyên nghiệp kiểm soát độ bóng dầu và thu nhỏ chân lông suốt 24 giờ.', N'Mỹ', N'Khoáng chất kiềm dầu micronized, Vitamin E, SPF 15', N'Che phủ từ trung bình đến cao, tạo lớp nền lì tự nhiên không bóng loáng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (141, 22, 14, N'Kem Nền Mịn Lì Chống Trôi YSL All Hours Foundation SPF39', N'Công nghệ cải tiến Skin-Fusing hòa quyện hoàn hảo vào da tạo cảm giác nhẹ tênh không tì vết.', N'Pháp', N'Axit Hyaluronic đa phân tử, Chiết xuất Cánh hoa Nhài, Bio-polymer', N'Lớp nền mờ lì không tì vết suốt cả ngày, chống nước chống mồ hôi đỉnh cao.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (142, 8, 14, N'Kem Nền Mịn Nhẹ Thoáng Da L''Oréal Paris Infallible 24H Fresh Wear', N'Công nghệ hạt màu siêu mịn với mạng lưới bọt khí thoáng khí Oxygen Technology cho da thở thoải mái.', N'Pháp', N'Công nghệ bọt khí Oxygen, SPF 25, Vitamin E', N'Bền màu 24 giờ, chống trôi chống nước và mồ hôi trong điều kiện thời tiết nóng ẩm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (143, 5, 14, N'Phấn Nước Kiềm Dầu Mịn Màng Innisfree No-Sebum Mineral Cushion SPF35', N'Phấn nước kiềm dầu chiết xuất từ bột khoáng đảo Jeju và bạc hà cho lớp nền khô thoáng 48h.', N'Hàn Quốc', N'Bột khoáng No-Sebum tự nhiên, Chiết xuất Bạc Hà, Trà xanh', N'Kiềm dầu vượt trội, làm mờ lỗ chân lông và không bị lem dính vào khẩu trang.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (144, 23, 14, N'Phấn Nước Căng Mọng Tự Nhiên Rom&nd Nu Zero Cushion', N'Hạt phấn nano siêu nhỏ che phủ các khuyết điểm lồi lõm trên da đem lại làn da mịn màng như da mộc.', N'Hàn Quốc', N'Bột phấn siêu phân tử, Hyaluronic Acid, Niacinamide', N'Mỏng nhẹ tự nhiên không nặng mặt, che phủ lỗ chân lông và vết đỏ mụn hiệu quả.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (145, 24, 14, N'Phấn Nước Mỏng Nhẹ Tự Nhiên 3CE Bare Cover Cushion SPF50+', N'Công thức cushion tạo hiệu ứng làn da phủ sương trong veo chuẩn Hàn Quốc với độ chống nắng cao.', N'Hàn Quốc', N'Chiết xuất hạt quả bơ, Dầu jojoba, Bột khoáng bắt sáng', N'Bảo vệ da dưới nắng, tạo hiệu ứng căng mọng glowy tự nhiên.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (146, 19, 14, N'Phấn Nước Dưỡng Da Hoàn Hảo Sulwhasoo Perfecting Cushion SPF50+', N'Tuyệt tác cushion kết hợp tinh chất dưỡng da Đông y quý phái nuôi dưỡng làn da ẩm mịn căng sáng.', N'Hàn Quốc', N'Chiết xuất nấm Schizophyllum, Tinh chất Nhân sâm, Jaum Moisturizing', N'Che phủ nếp nhăn, tăng độ săn chắc và bảo vệ làn da tươi trẻ rạng rỡ suốt ngày dài.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (147, 12, 14, N'Kem Nền Che Khuyết Điểm Chuyên Nghiệp Vichy Dermablend 3D Correction', N'Công thức chuyên khoa da liễu chứa Acid Salicylic và Eperuline che phủ tuyệt đối sẹo lõm và mụn gồ ghề.', N'Pháp', N'Salicylic Acid (BHA), Eperuline, Bột màu khoáng tinh khiết', N'San phẳng bề mặt da mụn gồ ghề, che phủ 100% vết thâm đỏ và kiềm dầu 16 giờ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (148, 11, 14, N'Kem Lót Kiềm Dầu & Thu Nhỏ Chân Lông Maybelline Fit Me Matte + Poreless Primer', N'Kem lót kiềm dầu giúp làm mờ lỗ chân lông tức thì và giữ lớp nền bền đẹp không xê dịch suốt 16h.', N'Mỹ', N'Đất sét khoáng kiềm nhờn, Polyme làm mờ lỗ chân lông, SPF 20', N'Tạo lớp đệm mềm mịn như nhung, ngăn ngừa mồ hôi và bã nhờn làm chảy nền.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (149, 9, 14, N'Kem Lót Hiệu Chỉnh Sắc Tố Da Laneige Skin Veil Base SPF25', N'Kem lót màu tím và xanh ngọc giúp hiệu chỉnh các vùng da xỉn màu và vết ửng đỏ mụn tức thì.', N'Hàn Quốc', N'Bột khoáng ngọc trai phát sáng, Vitamin C, Chiết xuất quả mọng', N'Tạo nền da sáng trong veo đều màu chuẩn phong cách trang điểm K-Beauty.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (150, 20, 14, N'Phấn Nước Cao Cấp Sang Trọng Dior Forever Cushion Vỏ Da Cannage', N'Tuyệt tác cushion biểu tượng thời trang Paris với vỏ bọc da cannage tinh xảo và chất phấn lì nhung cao cấp.', N'Pháp', N'Chiết xuất Hoa Diên Vĩ, Bột khoáng phản chiếu ánh sáng, SPF 35', N'Che phủ hoàn hảo 24 giờ, mang lại đẳng cấp quý phái sang trọng cho phái đẹp.', 1, N'ACTIVE', N'2026-01-01 00:00:00');
GO

INSERT INTO dbo.products (id, brand_id, category_id, name, description, origin, main_ingredients, benefits, is_featured, status, created_at) VALUES
    (151, 10, 15, N'Phấn Má Hồng Mịn Nhẹ Ánh Nhũ MAC Mineralize Blush', N'Phấn má nung chứa các khoáng chất tinh khiết giúp gò má sáng bừng sức sống với sắc màu tinh tế.', N'Mỹ', N'Khoáng chất nung tinh khiết, Hạt nhũ ngọc trai siêu mịn', N'Tạo đôi gò má ửng hồng tươi tắn, bắt sáng tự nhiên tôn đường nét khuôn mặt.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (152, 5, 15, N'Phấn Phủ Bột Khoáng Kiềm Dầu Innisfree No-Sebum Mineral Powder', N'Hũ phấn phủ bột khoáng quốc dân hút sạch dầu thừa tức thì, cho làn da ráo mịn suốt cả ngày dài.', N'Hàn Quốc', N'Khoáng chất đảo Jeju tự nhiên, Tinh chất Bạc Hà hữu cơ', N'Kiềm dầu, giữ lớp trang điểm lâu trôi và dùng được cho cả da đầu kiềm dầu tóc bết.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (153, 24, 15, N'Phấn Má Hồng Mịn Màng Tươi Tắn 3CE Face Blush Mood Recipe', N'Chất phấn má siêu mịn bám chặt vào da suốt cả ngày không bị phai màu với tông màu đất thời thượng.', N'Hàn Quốc', N'Hạt bột màu micronized nén chặt, Tinh dầu hoa cúc', N'Tạo đôi má ửng hồng trong trẻo tự nhiên, không gây cảm giác dày phấn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (154, 11, 15, N'Phấn Phủ Dạng Nén Kiềm Dầu 16H Maybelline Fit Me Matte + Poreless Powder', N'Hạt phấn Perlite khoáng chất tự nhiên hấp thu bã nhờn mạnh mẽ, giữ lớp nền mờ lì suốt 16 giờ.', N'Mỹ', N'Khoáng chất Perlite núi lửa, Bột silica hút dầu, SPF 28', N'Kiềm dầu, che lỗ chân lông và làm đều màu da tự nhiên không bóng loáng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (155, 23, 15, N'Phấn Má Hồng Trái Cây Rom&nd Better Than Cheek', N'Hiệu ứng làm mờ lỗ chân lông Blur effect như vừa dùng filter, mang lại đôi má phơn phớt tự nhiên.', N'Hàn Quốc', N'Bột khoáng làm mờ, Chiết xuất việt quất và dâu tây', N'Màu má ửng ngọt ngào, chất phấn tan mịn không lộ vân da hay lỗ chân lông.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (156, 20, 15, N'Phấn Má Hồng Bắt Sáng Cao Cấp Dior Rosy Glow Blush', N'Phấn má hồng công nghệ Color Reviver phản ứng theo độ ẩm tự nhiên của da để tạo màu hồng riêng biệt.', N'Pháp', N'Hạt nhũ ngọc trai phát quang, Bột màu sắc tố vi mô', N'Đem lại đôi gò má ửng hồng tươi tắn tự nhiên bừng sáng sức sống suốt cả ngày.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (157, 21, 15, N'Phấn Má Hồng Sang Trọng Chanel Joues Contraste', N'Hộp phấn má tròn kinh điển của Chanel với hương thơm hoa hồng tinh tế và chất phấn nướng siêu mịn.', N'Pháp', N'Khoáng chất nướng siêu nhỏ, Chiết xuất tinh dầu hoa hồng quý', N'Tạo đường nét gò má thanh tú, sắc màu quý phái trường tồn theo năm tháng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (158, 10, 15, N'Phấn Phủ Bột Khoáng Cố Định Lớp Nền MAC Studio Fix Pro Set + Blur Weightless', N'Hạt phấn bột siêu tàng hình kiềm dầu suốt 8 giờ và khóa chặt lớp nền không xê dịch suốt ngày dài.', N'Mỹ', N'Bột khoáng quang học siêu vi, Dầu dưỡng nhẹ', N'Làm mờ lỗ chân lông tức thì, không gây đọng phấn rãnh cười hay hằn nếp nhăn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (159, 8, 15, N'Phấn Phủ Bột Kiềm Dầu Suốt 24 Giờ L''Oréal Paris Infallible Loose Setting Powder', N'Hạt phấn siêu nhẹ hấp thu bã nhờn dư thừa và giữ lớp trang điểm tươi mới suốt ngày hoạt động.', N'Pháp', N'Hạt phấn khoáng siêu vi, Bột Talc tinh khiết', N'Kiềm dầu, chống bóng nhờn và tạo bề mặt da láng mịn không tì vết.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (160, 1, 15, N'Phấn Phủ Bột Bí Đao Cocoon Kiềm Dầu Kháng Khuẩn 10g', N'Phấn phủ bột khoáng thuần chay kết hợp chiết xuất bí đao và tràm trà bảo vệ da dầu mụn.', N'Việt Nam', N'Chiết xuất bí đao, Tinh dầu tràm trà, Bột bắp tự nhiên, Bột ngọc trai nhẹ', N'Hút sạch dầu bóng nhờn, kháng khuẩn cho các nốt mụn và giữ da thoáng nhẹ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (161, 24, 15, N'Bột Phấn Phủ Bắt Sáng Tạo Khối 3CE Strobing Skin Powder', N'Hạt nhũ ngọc trai siêu mịn bắt sáng lấp lánh ở các điểm cao trên khuôn mặt như sống mũi và gò má.', N'Hàn Quốc', N'Bột nhũ khoáng ngọc trai phát quang, Vitamin E', N'Tạo hiệu ứng gương mặt thanh thoát thon gọn và căng tràn sức sống dưới ánh đèn.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (162, 23, 15, N'Phấn Bắt Sáng Căng Bóng Ngọc Trai Rom&nd See-Through Veillighter', N'Hạt phấn nhũ mịn màng tạo độ óng ả trong veo tựa làn nước mùa thu trên đôi gò má.', N'Hàn Quốc', N'Bột ngọc trai siêu phân tử, Tinh dầu dừa dưỡng ẩm', N'Bắt sáng tự nhiên không lộ hạt nhũ thô, giữ vẻ tươi tắn suốt cả ngày.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (163, 11, 15, N'Má Hồng Kem Dạng Thỏi Maybelline Cheek Heat Gel-Cream Blush', N'Chất gel cream mỏng nhẹ gốc nước tan tiệp vào da cho đôi má ửng màu tự nhiên như không trang điểm.', N'Mỹ', N'Nước khoáng tự nhiên, Sắc tố màu dạng gel tan chảy', N'Dễ tán bằng tay, dưỡng ẩm nhẹ cho gò má căng mướt tươi tắn.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (164, 9, 15, N'Phấn Phủ Bột Cấp Nước Khóa Ẩm Laneige Neo Essential Blurring Finish Powder', N'Hạt phấn bột chứa tinh chất Hyaluronic Acid dạng nén khóa chặt độ ẩm trong khi kiểm soát bã nhờn bên ngoài.', N'Hàn Quốc', N'Micro Hyaluronic Acid, Men nấm men xanh, Bột silica kiềm dầu', N'Làm mịn màng lỗ chân lông, kiềm dầu mà không làm mốc khô da vùng mũi má.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (165, 10, 15, N'Phấn Bắt Sáng Thần Thánh MAC Mineralize Skinfinish Extra Dimension', N'Tuyệt tác phấn bắt sáng nung chứa khoáng chất quý hiếm cho làn da phát sáng rạng ngời dưới mọi góc chụp.', N'Mỹ', N'Bột khoáng nướng ngọc trai 77 phức hợp, Vitamin E', N'Tạo hiệu ứng gương mặt thanh tú nổi bật, bền màu suốt 10 giờ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (166, 11, 16, N'Chuốt Mi Dài & Dày Mi Chống Trôi Maybelline Hypercurl Waterproof Mascara', N'Mascara huyền thoại giữ nếp mi cong vút và dày gấp 3 lần suốt 36 giờ bất chấp thời tiết nóng ẩm.', N'Mỹ', N'Sáp tự nhiên, Hợp chất Polymer định hình mi, Gel dưỡng mi', N'Giúp hàng mi cong vút tự nhiên, chống lem chống trôi kháng nước tuyệt đối.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (167, 11, 16, N'Mascara Dài & Cong Mi Bất Tận Maybelline Lash Sensational Sky High Waterproof', N'Cây chuốt mi gây sốt toàn cầu với đầu cọ Flex Tower linh hoạt chạm tới từng sợi mi ngắn nhất.', N'Mỹ', N'Chiết xuất tre tự nhiên, Sợi tơ kéo dài mi Pro-Keratin', N'Kéo dài mi vô tận từ gốc tới ngọn, không gây vón cục hay nặng mi.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (168, 24, 16, N'Bảng Phấn Mắt 9 Ô Thời Thượng 3CE Multi Eye Color Palette', N'Bảng màu mắt tông nâu cam đất thời thượng với 9 ô màu từ lì mịn mượt đến nhũ lấp lánh.', N'Hàn Quốc', N'Bột khoáng màu cao cấp, Hạt nhũ ngọc trai bắt sáng', N'Dễ phối màu từ trang điểm tự nhiên đi học đến quyến rũ dự tiệc ban đêm.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (169, 23, 16, N'Bảng Phấn Mắt 4 Ô Ngọt Ngào Rom&nd Better Than Eyes', N'Bảng phấn mắt nhỏ gọn tiện lợi gồm 3 ô lì mịn màng và 1 ô nhũ kim tuyến lấp lánh như giọt sương.', N'Hàn Quốc', N'Hạt bột màu siêu mịn kiểm soát dầu mí mắt, Nhũ ngọc trai', N'Chất phấn không bay bụi, bám chặt vào mí mắt suốt cả ngày.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (170, 11, 16, N'Bút Kẻ Mắt Nước Nét Mảnh Siêu Lì Maybelline Hyper Sharp Liner Extreme', N'Đầu cọ vi sợi 0.01mm siêu sắc mảnh lướt êm ái trên mí mắt cho đường kẻ mắt mèo chuẩn xác từng milimet.', N'Mỹ', N'Polyme màng phim khóa màu, Mực carbon đen tuyền', N'Kháng nước, kháng mồ hôi, không lem trôi suốt 36 giờ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (171, 8, 16, N'Chuốt Mi Dày Mi Dài Mi Quyến Rũ L''Oréal Paris Voluminous Lash Paradise', N'Đầu cọ lượn sóng hơn 200 sợi lông mềm mại ôm sát sợi mi cho hàng mi dày gấp 20 lần mềm mại như lông vũ.', N'Pháp', N'Dầu thầu dầu castor oil nuôi mi, Sáp hoa hồng, Sợi vi dệt', N'Làm mi dày rậm quyến rũ mà không hề vón cục hay làm rụng mi.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (172, 20, 16, N'Mascara Cao Cấp Dài & Cong Mi Dior Diorshow Mascara 24H', N'Mascara sàn diễn thời trang danh giá với công nghệ sợi vi lụa tơ tằm đem lại ánh nhìn hút hồn sâu thẳm.', N'Pháp', N'Chiết xuất Hoa Ngô Dại làm dịu mắt, Sợi tơ tằm nguyên chất', N'Định hình hàng mi cong vút chuẩn sắc Haute Couture suốt 24 giờ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (173, 5, 16, N'Mascara Cong Mi Tự Nhiên Thuần Chay Innisfree Simple Label Long & Curl', N'Chuốt mi thuần chay đạt chứng nhận EVE VEGAN lành tính tuyệt đối cho mắt nhạy cảm và người đeo lens.', N'Hàn Quốc', N'Chiết xuất rễ cây hoa cẩm tú cầu Jeju, Sáp thực vật', N'Giữ nếp mi cong vút tự nhiên, nhẹ tênh không cay mắt.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (174, 10, 16, N'Nhũ Mắt Dạng Lỏng Lấp Lánh MAC Dazzleshadow Liquid', N'Nhũ mắt dạng lỏng tan nhẹ lướt êm với hàng triệu hạt tinh thể bắt sáng lấp lánh như bầu trời đêm đầy sao.', N'Mỹ', N'Hạt nhũ đa chiều siêu sáng, Gel gốc nước bám dính cao', N'Khô nhanh, không lem rơi rụng hạt nhũ xuống má suốt buổi tiệc.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (175, 11, 16, N'Chì Kẻ Mày Định Hình Tự Nhiên Maybelline Define & Blend Brow Pencil', N'Thiết kế đầu chì vát xéo tiện lợi kết hợp đầu chổi xoắn chải mày giúp tạo dáng chân mày tự nhiên.', N'Mỹ', N'Sáp tự nhiên mềm mại, Bột màu khoáng tiệp tone da', N'Lên màu chuẩn, không vón cục, chống nước không sợ trôi màu đuôi mày.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (176, 24, 16, N'Bút Kẻ Mắt Dạ Nét Mảnh Chống Nước 3CE Super Slim Pen Eyeliner', N'Đầu bút lông dạ mềm mại linh hoạt tạo đường kẻ mắt từ tự nhiên thanh thoát đến mắt mèo sắc lẹm.', N'Hàn Quốc', N'Polyme tạo màng chống thấm nước, Sắc tố mực đen', N'Không lem khi đổ mồ hôi hay gặp nước mưa, tẩy trang dễ dàng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (177, 23, 16, N'Mascara Định Hình Cong Mi Tự Nhiên Rom&nd Han All Fix Mascara', N'Đầu cọ thon nhỏ len lỏi chải tơi từng sợi mi không vón cục, tạo hiệu ứng mi dài thanh thoát.', N'Hàn Quốc', N'Sợi nối mi vi mô, Sáp định hình mi cong', N'Cố định hàng mi cong vút suốt ngày dài kể cả với người có mí mắt dầu sụp mí.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (178, 5, 16, N'Chì Kẻ Mày 2 Đầu Tiện Lợi Innisfree Auto Eyebrow Pencil', N'Cây chì kẻ mày đầu oval dẹp thần thánh giúp viền khung và phẩy sợi chân mày siêu dễ dàng.', N'Hàn Quốc', N'Bột khoáng thiên nhiên, Chiết xuất đậu nành nuôi dưỡng chân mày', N'Dáng mày vào nếp tự nhiên sắc nét, không bị bết dính sợi lông mày.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (179, 8, 16, N'Phấn Mắt Nhũ Mịn Đơn Sắc L''Oréal Paris Color Queen Oil Eyeshadow', N'Công thức phấn mắt dạng kem dầu ép mịn đậm đặc sắc tố lấp lánh lên màu chuẩn chỉ với 1 ngón tay.', N'Pháp', N'Dầu dưỡng thực vật, Bột màu sắc tố ngọc trai', N'Chất phấn mượt mà như bơ, không bay bụi phấn vào mắt.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (180, 21, 16, N'Bảng Phấn Mắt 4 Màu Đẳng Cấp Chanel Les 4 Ombres Multi-Effect', N'Bảng phấn mắt 4 ô kinh điển của Chanel với công thức nướng đem lại lớp màu mượt mà sâu thẳm.', N'Pháp', N'Khoáng chất micronized nung tinh khiết, Bột ngọc trai', N'Tạo chiều sâu huyền bí cho đôi mắt, lưu giữ sắc màu quý phái cả ngày.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (181, 1, 17, N'Tẩy Tế Bào Chết Cơ Thể Cà Phê Đắk Lắk Cocoon Dak Lak Coffee Body Polish', N'Tẩy tế bào chết cơ thể thuần chay thơm lừng hương cà phê Việt Nam, đánh bay viêm lỗ chân lông và thâm sần.', N'Việt Nam', N'Hạt cà phê Đắk Lắk xay nhuyễn, Bơ ca cao Tiền Giang, Dầu dừa Bến Tre', N'Loại bỏ lớp sừng già cỗi, làm đều màu da và nuôi dưỡng làn da body mềm mịn như nhung.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (182, 29, 17, N'Tẩy Tế Bào Chết Toàn Thân Bath & Body Works Aromatherapy Eucalyptus Spearmint', N'Muối biển tự nhiên kết hợp tinh dầu khuynh diệp và bạc hà giải tỏa căng thẳng stress mệt mỏi.', N'Mỹ', N'Muối biển khoáng, Tinh dầu khuynh diệp, Tinh dầu bạc hà, Dầu hạt hướng dương', N'Làm mịn các vùng da thô ráp cùi chỏ đầu gối, lưu lại hương thơm thư giãn như spa.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (183, 6, 17, N'Kem Dưỡng Tẩy Da Chết Cơ Thể CeraVe SA Smoothing Cream 10% Urea & Salicylic', N'Công thức chứa 10% Urea, Axit Salicylic và 3 Ceramide thiết yếu làm mịn làn da sần sùi viêm nang lông.', N'Mỹ', N'10% Urea, Salicylic Acid (BHA), 3 Ceramide thiết yếu, LHA', N'Tẩy sạch da chết sần sùi viêm lỗ chân lông, làm mềm vùng da khô ráp cánh tay bắp chân.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (184, 4, 17, N'Kem Dưỡng Thể Tẩy Tế Bào Chết Paula''s Choice Weightless Body Treatment 2% BHA', N'Sản phẩm điều trị viêm nang lông và mụn lưng nổi tiếng của Paula''s Choice với 2% BHA dạng kem dưỡng.', N'Mỹ', N'2% Salicylic Acid (BHA), Chiết xuất Trà xanh, Vitamin E, Bisabolol', N'Giải quyết dứt điểm mụn lưng, mụn ngực và viêm nang lông, trả lại làn da mịn màng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (185, 1, 17, N'Tẩy Da Chết Body Hoa Hồng Cocoon Làm Sáng Mịn Da 200ml', N'Cánh hoa hồng tươi Hậu Giang kết hợp hạt cà phê nghiền mịn và dầu jojoba hữu cơ.', N'Việt Nam', N'Cánh hoa hồng tươi, Hạt cà phê xay mịn, Dầu jojoba, Vitamin B5', N'Làm sáng da thâm sạm nách bẹn mông, lưu lại làn da ngát hương hoa hồng quyến rũ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (186, 28, 17, N'Muối Tẩy Tế Bào Chết & Làm Sáng Body Tsubaki Camellia Scrub', N'Muối biển Nhật Bản giàu khoáng chất kết hợp tinh dầu hoa trà đỏ Tsubaki nuôi dưỡng da mượt mà.', N'Nhật Bản', N'Muối khoáng biển Seto, Dầu hoa trà đỏ Tsubaki, Sữa ong chúa', N'Làm sạch sừng già cỗi, cấp ẩm bóng mượt và ngăn ngừa lão hóa da cơ thể.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (187, 29, 17, N'Tẩy Tế Bào Chết Hương Nước Hoa Bath & Body Works Into The Night Body Scrub', N'Đường hạt mịn tự nhiên hòa quyện tinh chất bơ hạt mỡ và hương thơm dạ tiệc sang trọng quyến rũ.', N'Mỹ', N'Hạt đường mía tự nhiên, Bơ hạt mỡ hữu cơ, Dầu dừa, Vitamin E', N'Làm mịn da tức thì, để lại hương thơm nồng nàn lưu luyến suốt 6 giờ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (188, 30, 17, N'Tẩy Tế Bào Chết Body Victoria''s Secret Pure Seduction Smoothing Scrub', N'Hạt đường tinh thể nhuyễn mịn kết hợp quả mận ép và hoa lan Nam Phi tạo nên mùi hương ngọt ngào.', N'Mỹ', N'Đường mía tinh luyện, Chiết xuất mận đỏ ép, Tinh dầu hoa lan', N'Tẩy da chết êm ái, mang lại làn da body mềm mượt như lụa satin.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (189, 5, 17, N'Tẩy Tế Bào Chết Body Trà Xanh Innisfree Green Tea Pure Body Gel Scrub', N'Hạt óc chó xay nhuyễn trong gel tạo bọt trà xanh hữu cơ vừa tắm vừa tẩy tế bào chết 2 trong 1.', N'Hàn Quốc', N'Bột vỏ quả óc chó, Chiết xuất trà xanh Jeju Beauty Green Tea', N'Làm sạch tế bào chết dịu nhẹ, đem lại cảm giác mát mẻ sảng khoái sau khi tắm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (190, 8, 17, N'Sữa Dưỡng Thể Tẩy Tế Bào Chết Sáng Da L''Oréal Paris Glycolic-Bright Body Lotion', N'Chứa 1% Glycolic Acid (AHA) và Niacinamide dưỡng sáng da toàn thân và mờ vết thâm sạm.', N'Pháp', N'1% Glycolic Acid, Niacinamide, Bơ hạt mỡ, Vitamin C', N'Làm sáng các vùng da xỉn màu khuỷu tay đầu gối, nuôi dưỡng da căng mịn ẩm mượt.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (191, 14, 17, N'Kem Tẩy Da Chết Toàn Thân Cho Da Khô Nhạy Cảm Bioderma Atoderm Gel Douche Gommante', N'Công thức vi hạt tròn không gây trầy xước kết hợp phức hợp dưỡng ẩm Skin Protect Complex.', N'Pháp', N'Skin Protect Complex, Vi hạt tròn Polyethylene, Niacinamide', N'Tẩy da chết êm dịu, bảo vệ độ ẩm tự nhiên cho làn da khô nhạy cảm dễ kích ứng.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (192, 18, 17, N'Tẩy Tế Bào Chết Body Bưởi Chùm Kiehl''s Gently Exfoliating Body Scrub Grapefruit', N'Hạt tẩy tế bào chết từ vỏ quả mơ và hoa cúc La Mã đánh thức làn da với hương bưởi chùm tươi mát.', N'Mỹ', N'Hạt mơ tự nhiên nghiền mịn, Chiết xuất Bưởi chùm, Nha đam', N'Tẩy da chết toàn thân, giúp da mềm mại và lưu hương cam chanh sảng khoái.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (193, 26, 17, N'Muối Tắm Tẩy Tế Bào Chết Khoáng Biển DHC Mineral Body Scrub', N'Muối biển sâu Brittany Pháp giàu nguyên tố vi lượng kết hợp dầu olive và tinh chất bạc hà.', N'Nhật Bản', N'Muối biển vùng Brittany Pháp, Tinh dầu Olive hữu cơ, Tinh dầu Bạc hà', N'Thanh lọc độc tố, giảm tình trạng tích nước và đem lại cảm giác nhẹ nhõm thư thái.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (194, 17, 17, N'Sữa Dưỡng Thể Cấp Ẩm Chuyên Sâu 10% Urea Eucerin Complete Repair Moisture Lotion', N'Chứa 10% Urea kết hợp Ceramide và Gluco-Glycerol phục hồi chuyên sâu làn da khô nứt vảy cá.', N'Đức', N'10% Urea, Ceramide-3, Gluco-Glycerol tự nhiên', N'Làm mềm các mảng sừng dày cộm ráp ngứa, duy trì độ ẩm suốt 48 giờ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (195, 27, 17, N'Tẩy Tế Bào Chết Cơ Thể Tinh Dầu Argan Moroccanoil Body Polishing Scrub', N'Bột vỏ hạt argan nghiền mịn kết hợp tinh dầu argan nguyên chất nuôi dưỡng làn da bóng mượt quý phái.', N'Israel', N'Bột vỏ hạt Argan, Tinh dầu Argan hữu cơ, Dầu quả bơ, Dầu hoa anh thảo', N'Đánh bay da chết sần sùi, để lại hương thơm hổ phách trứ danh của Moroccanoil.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (196, 1, 18, N'Nước Dưỡng Tóc Tinh Dầu Bưởi Cocoon Pomelo Hair Tonic', N'Giải pháp thuần chay giải cứu mái tóc yếu gãy rụng với tinh dầu vỏ bưởi nguyên chất và Xylishine.', N'Việt Nam', N'Tinh dầu bưởi nguyên chất, Xylishine, Vitamin B5, Baicapil', N'Giảm gãy rụng rõ rệt, kích thích mọc tóc con và nuôi dưỡng nang tóc chắc khỏe bồng bềnh.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (197, 1, 18, N'Dầu Gội Bưởi Không Sulfate Giảm Rụng Tóc Cocoon Pomelo Shampoo', N'Dầu gội thuần chay không chứa chất tẩy rửa sulfate mạnh, dịu nhẹ cho da đầu nhạy cảm và tóc hư tổn.', N'Việt Nam', N'Tinh dầu vỏ bưởi nguyên chất, Xylishine, Vitamin B5', N'Làm sạch dịu nhẹ tóc và da đầu, giảm rụng tóc và nuôi dưỡng tóc bồng bềnh chắc khỏe.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (198, 28, 18, N'Dầu Gội Dưỡng Tóc Chuẩn Salon Nhật Bản Tsubaki Premium Moist & Repair Shampoo', N'Công nghệ thẩm thấu CMC mở rộng đường dẫn dưỡng chất vào sâu lõi tóc phục hồi hư tổn tức thì.', N'Nhật Bản', N'Dầu hoa trà đỏ Tsubaki, Sữa ong chúa, Protein đậu nành', N'Cho mái tóc suôn mềm óng ả chuẩn salon Nhật Bản, giải cứu tóc khô xơ chẻ ngọn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (199, 27, 18, N'Tinh Dầu Dưỡng Tóc Thần Thánh Moroccanoil Treatment Original', N'Tinh dầu dưỡng tóc số 1 thế giới chiết xuất từ dầu argan giàu chất chống oxy hóa và vitamin E.', N'Israel', N'Dầu Argan nguyên chất hữu cơ, Chiết xuất hạt lanh Linseed, Vitamin E', N'Biến đổi mái tóc xơ rối thành suôn mượt bóng bẩy ngay giọt đầu tiên, rút ngắn thời gian sấy tóc.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (200, 8, 18, N'Dầu Gội Phục Hồi Tóc Hư Tổn Toàn Diện L''Oréal Paris Elseve Total Repair 5', N'Chứa hoạt chất Cement Ceramide mô phỏng chất xi măng tự nhiên hàn gắn 5 dấu hiệu tóc hư tổn.', N'Pháp', N'Cement Ceramide mô phỏng sinh học, Protein lúa mì thủy phân', N'Chấm dứt 5 dấu hiệu: gãy rụng, khô ráp, xơ yếu, chẻ ngọn và mất độ bóng.', 0, N'ACTIVE', N'2026-01-01 00:00:00');
GO

INSERT INTO dbo.products (id, brand_id, category_id, name, description, origin, main_ingredients, benefits, is_featured, status, created_at) VALUES
    (201, 8, 18, N'Dầu Dưỡng Tóc Cao Cấp L''Oréal Paris Elseve Extraordinary Oil Chiết Xuất 6 Loài Hoa', N'Chiết xuất từ 6 loài hoa quý hiếm nuôi dưỡng mái tóc mềm mại óng ả bồng bềnh không bết dính.', N'Pháp', N'Tinh dầu 6 loài hoa quý (Hoa sen, Cúc, Tiara, Hồng, Lanh, Thược dược)', N'Dưỡng ẩm sâu, bảo vệ tóc trước nhiệt độ máy sấy đến 230 độ C.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (202, 28, 18, N'Mặt Nạ Tóc Phục Hồi Cao Cấp Tsubaki Premium EX Repair Mask', N'Mặt nạ tóc xả ngay không cần ủ với công nghệ định vị hư tổn phục hồi sợi tóc bóng như tơ lụa.', N'Nhật Bản', N'Dầu hoa trà đỏ Tsubaki lên men, Axit Amin thẩm thấu, Squalane', N'Phục hồi tóc cháy tẩy nhuộm ngay lập tức, tiết kiệm thời gian ủ tóc.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (203, 5, 18, N'Dầu Dưỡng Tóc Hoa Trà Phục Hồi Innisfree Camellia Essential Hair Oil Serum', N'Tinh dầu hoa trà đảo Jeju thẩm thấu nhanh vào các lớp biểu bì tóc bị chẻ ngọn do uốn nhuộm.', N'Hàn Quốc', N'Dầu hoa trà đảo Jeju lên men, Ceramide bảo vệ tóc', N'Làm mượt tức thì các lọn tóc rối xơ xác, giữ nếp uốn sóng tự nhiên bồng bềnh.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (204, 1, 18, N'Dầu Xả Bưởi Phục Hồi & Giảm Gãy Rụng Cocoon Pomelo Conditioner', N'Dầu xả thuần chay không chứa silicon giúp làm mềm mượt sợi tóc mà không làm bẹp dính chân tóc.', N'Việt Nam', N'Tinh dầu vỏ bưởi, Vitamin B5, Chiết xuất rễ cây phỉ, Xylishine', N'Gỡ rối nhẹ nhàng, giảm rụng tóc khi chải và mang lại mái tóc suôn mượt ngát hương bưởi.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (205, 26, 18, N'Tinh Chất Kích Thích Mọc Tóc Chuyên Sâu DHC Hair Growth Tonic', N'Dược liệu cổ truyền Nhật Bản kết hợp tinh dầu bạc hà kích thích vi tuần hoàn máu nuôi nang tóc.', N'Nhật Bản', N'Chiết xuất rễ Hoàng Cầm, Nhân sâm Panax, Tinh dầu Bạc hà', N'Kích thích tóc con mọc dày dài, ngăn ngừa rụng tóc từng mảng và hói đầu sớm.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (206, 18, 18, N'Dầu Gội Dưỡng Ẩm Axit Amin Kiehl''s Amino Acid Shampoo', N'Công thức tạo bọt kem béo ngậy giàu axit amin và dầu dừa tinh khiết làm sạch dịu nhẹ da đầu.', N'Mỹ', N'Phức hợp Axit Amin, Dầu dừa nguyên chất, Protein hạt lúa mì', N'Tạo độ phồng tự nhiên cho tóc mỏng xẹp, duy trì độ ẩm mềm mại cả ngày.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (207, 27, 18, N'Dầu Gội Phục Hồi Độ Ẩm Moroccanoil Moisture Repair Shampoo', N'Dầu gội phục hồi cao cấp chứa dầu argan và keratin tăng cường liên kết tóc bị đứt gãy do hóa chất.', N'Israel', N'Dầu Argan hữu cơ, Keratin tái cấu trúc, Axit béo thiết yếu', N'Khôi phục độ đàn hồi cho mái tóc tẩy nhuộm, không chứa sulfat và paraben.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (208, 28, 18, N'Dầu Gội Phục Hồi Chống Gãy Rụng Tsubaki Premium Volume & Repair', N'Công nghệ nâng phồng chân tóc kết hợp dầu hoa trà lên men cho mái tóc bồng bềnh dồi dào sức sống.', N'Nhật Bản', N'Dầu hoa trà vàng Tsubaki, Tinh dầu hương thảo, Axit Citric', N'Tăng độ bồng bềnh cho mái tóc bết dính xẹp lép, ngăn ngừa rụng tóc.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (209, 1, 18, N'Serum Sa-chi Phục Hồi Tóc Hư Tổn Cocoon Inca Inchi Hair Serum 70ml', N'Dầu hạt sa-chi giàu Omega 3-6-9 đậm đặc gấp 40 lần dầu Argan bao bọc lớp biểu bì sợi tóc.', N'Việt Nam', N'Dầu hạt Sa-chi Đắk Lắk, Vitamin E, Tinh dầu hương thảo', N'Hàn gắn ngọn tóc chẻ ngọn, bảo vệ tóc trước tia UV và nhiệt độ cao.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (210, 8, 18, N'Kem Ủ Tóc Phục Hồi Toàn Diện L''Oréal Paris Elseve Total Repair 5 Deep Repairing Mask', N'Kem ủ đậm đặc chứa Micro-Ceramide thẩm thấu sâu nuôi dưỡng tóc bóng mượt chỉ sau 3 phút.', N'Pháp', N'Micro-Ceramide, Protein lúa mì, Axit Amin tái sinh', N'Giải cứu mái tóc khô xơ cháy nắng, trả lại vẻ suôn mượt óng ả.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (211, 13, 19, N'Nước Hoa Nữ Cao Cấp LUNEA Midnight Rose Eau De Parfum', N'Tuyệt tác hương thơm mang phong cách Paris hoa lệ với nốt hương hoa hồng huyền bí hòa quyện cùng xạ hương ấm áp.', N'Pháp', N'Hoa hồng Damask, Quả mọng đen, Gỗ tuyết tùng, Xạ hương trắng', N'Lưu hương quyến rũ nồng nàn từ 8 đến 12 giờ, tỏa hương tinh tế quý phái trong bán kính 2 mét.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (212, 21, 19, N'Nước Hoa Nữ Biểu Tượng Chanel Coco Mademoiselle Eau De Parfum', N'Biểu tượng nước hoa nữ vĩ đại nhất mọi thời đại mở đầu với hương cam tươi mát và bừng nở cùng hoa hồng Thổ Nhĩ Kỳ.', N'Pháp', N'Cam Bergamot, Hoa hồng Thổ Nhĩ Kỳ, Hoắc hương Patchouli, Vani Bourbon', N'Lưu hương đỉnh cao hơn 12 giờ, toát lên phong thái tiểu thư Paris độc lập kiêu kỳ.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (213, 20, 19, N'Nước Hoa Nam Lịch Lãm Phong Trần Dior Sauvage Eau De Parfum', N'Tuyệt tác nước hoa nam bán chạy số 1 hành tinh với hương cam Bergamot vùng Calabria và hổ phách Ambroxan ma mị.', N'Pháp', N'Cam Bergamot Calabria, Tiêu Tứ Xuyên, Hương hoa Oải hương, Ambroxan', N'Tỏa hương lôi cuốn mãnh liệt, khẳng định bản lĩnh phái mạnh phong trần nam tính.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (214, 22, 19, N'Nước Hoa Nữ Quyến Rũ Gây Nghiện YSL Black Opium Eau De Parfum', N'Sự hòa quyện bùng nổ giữa cà phê đen kích thích và hoa trắng ngọt ngào tạo nên nét quyến rũ mê hoặc.', N'Pháp', N'Cà phê đen nguyên chất, Hoa nhài trắng Sambac, Vani phương Đông, Gỗ tuyết tùng', N'Lưu hương 8-10 giờ, tôn vinh hình ảnh người phụ nữ cá tính hiện đại gợi cảm.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (215, 20, 19, N'Nước Hoa Nữ Tiểu Thư Ngọt Ngào Miss Dior Eau De Parfum', N'Bó hoa hồng Grasse rực rỡ sắc màu được thắt nơ Haute Couture tinh xảo mang lại nét nữ tính đài các.', N'Pháp', N'Hoa hồng xứ Grasse, Hoa chuông thung lũng, Hoa diên vĩ, Gỗ đàn hương', N'Mùi hương ngập tràn tình yêu và sự lãng mạn, lưu hương bền bỉ suốt cả ngày.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (216, 21, 19, N'Nước Hoa Nam Nam Tính Quyền Lực Bleu De Chanel Eau De Parfum', N'Hương thơm gỗ thơm nồng ấm kết hợp bưởi tươi và tuyết tùng tôn vinh sự tự do của quý ông thành đạt.', N'Pháp', N'Quả bưởi hồng, Tinh dầu Bạc hà, Hương gỗ Tuyết tùng, Hương Nhang trầm hương', N'Độ bám tỏa hương cực đỉnh, hoàn hảo cho môi trường công sở lịch lãm và dạ tiệc sang trọng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (217, 22, 19, N'Nước Hoa Nữ Nồng Nàn Kiêu Hãnh YSL Libre Eau De Parfum', N'Bản tuyên ngôn tự do hòa quyện giữa hoa oải hương nước Pháp và hoa cam Maroc rực rỡ gợi cảm.', N'Pháp', N'Hoa oải hương Provence Pháp, Hoa cam Maroc, Xạ hương quý, Vani Madagascar', N'Lưu hương 10-12 giờ, phong cách sang chảnh kiêu sa đầy khí chất.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (218, 13, 19, N'Nước Hoa Nữ Nắng Hè Nhiệt Đới Estée Lauder Bronze Goddess Eau De Parfum', N'Hương thơm quyến rũ ấm áp của dừa nướng, hoa tiare và vani gợi nhớ về kỳ nghỉ biển ngập tràn nắng vàng.', N'Mỹ', N'Quả dừa tươi, Hoa Tiare Tahiti, Hổ phách ấm, Vani ngọt ngào', N'Mùi hương gợi cảm thư thái, lý tưởng cho những ngày hè rực rỡ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (219, 21, 19, N'Nước Hoa Nữ Ngọt Ngào Thanh Khiết Chanel Chance Eau Tendre EDP', N'Hương thơm hoa quả dịu dàng lãng mạn với bưởi thanh khiết, mộc qua và hoa nhài trắng e ấp.', N'Pháp', N'Quả bưởi, Trái mộc qua, Hoa nhài Sambac, Xạ hương trắng tinh khôi', N'Lưu hương nhẹ nhàng thanh lịch, tôn lên nét trong sáng đáng yêu của phái đẹp.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (220, 22, 19, N'Nước Hoa Nam Tươi Mát Bản Lĩnh YSL Y Eau De Parfum', N'Nốt hương mở đầu từ táo xanh giòn tan và cam bergamot kết hợp gỗ tuyết tùng nam tính hiện đại.', N'Pháp', N'Táo xanh tươi mát, Cây xô thơm Sage, Gỗ tuyết tùng, Đậu Tonka', N'Mùi hương nam đa dụng số 1 có thể dùng quanh năm từ đi làm đến hẹn hò.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (221, 20, 19, N'Nước Hoa Nữ Nữ Tính Rạng Ngời Dior J''adore Eau De Parfum', N'Bó hoa vàng kim quý tộc hội tụ hoa ngọc lan tây Ylang-Ylang Comoros và hoa nhài Grasse tráng lệ.', N'Pháp', N'Hoa ngọc lan tây Ylang-Ylang, Hoa hồng Damask, Hoa nhài Sambac', N'Biểu tượng của sự quyền quý xa hoa, tỏa hương thanh cao ngút ngàn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (222, 13, 19, N'Nước Hoa Nữ Lãng Mạn Tinh Khôi Estée Lauder Beautiful Magnolia EDP', N'Hương hoa mộc lan tinh khôi hòa cùng hoa sen nước và gỗ tuyết tùng tạo nên câu chuyện tình say đắm.', N'Mỹ', N'Cánh hoa mộc lan Magnolia, Hoa sen nước, Xạ hương ấm áp', N'Hương thơm dịu dàng trong trẻo như làn gió mùa xuân mơn man.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (223, 21, 19, N'Nước Hoa Huyền Thoại Thế Giới Chanel No.5 Eau De Parfum', N'Chai nước hoa huyền thoại hơn 100 năm tuổi với phức hợp Aldehydes hoa cỏ sang trọng bất hủ.', N'Pháp', N'Phức hợp Aldehydes, Hoa hồng tháng Năm xứ Grasse, Hoa nhài, Gỗ đàn hương', N'Mùi hương kinh điển vĩnh cửu định hình chuẩn mực quý bà thượng lưu.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (224, 22, 19, N'Nước Hoa Nữ Quyến Rũ Paris YSL Mon Paris Eau De Parfum', N'Hương thơm tình yêu mãnh liệt thắp sáng tháp Eiffel với dâu tây mọng đỏ và hoa cà độc dược Datura.', N'Pháp', N'Dâu tây, Quả mâm xôi, Hoa Datura ma mị, Xạ hương trắng, Hoắc hương', N'Ngọt ngào lãng mạn say đắm, lưu hương 8-10 giờ quyến luyến.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (225, 20, 19, N'Nước Hoa Nam Lãng Tử Sâu Lắng Dior Homme Intense Eau De Parfum', N'Hợp chất hoa diên vĩ Tuscan Iris quý tộc kết hợp hạt vông vang và tuyết tùng tạo nên hương thơm mê hoặc.', N'Pháp', N'Hoa diên vĩ Tuscany Iris, Hạt vông vang Ambrette, Tuyết tùng Virginia', N'Mùi hương ấm áp lịch lãm, đẳng cấp số 1 cho những buổi hẹn hò lãng mạn.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (226, 1, 20, N'Xịt Thơm Toàn Thân & Tóc LUNEA Golden Amber Body Mist', N'Xịt thơm body mist dịu ngọt quý phái mang lại sự tự tin và hương thơm lôi cuốn mọi ánh nhìn suốt cả ngày.', N'Việt Nam', N'Tinh dầu hổ phách, Hoa vani Madagascar, Nước cất hoa cam Hậu Giang', N'Lưu hương nhẹ nhàng suốt 4-6 giờ, tạo cảm giác thư giãn sảng khoái và dưỡng ẩm nhẹ cho tóc.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (227, 29, 20, N'Xịt Thơm Toàn Thân Bath & Body Works Into The Night Fine Fragrance Mist', N'Hương thơm dạ tiệc bán chạy nhất của BBW với sự kết hợp ma mị của quả mọng đen, hoa nhài đêm và hổ phách.', N'Mỹ', N'Quả mâm xôi đen, Tinh dầu hoa nhài đêm, Hổ phách đen quý phái', N'Lưu hương 4-6 tiếng trên cơ thể và quần áo, phong cách quyến rũ sang trọng.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (228, 29, 20, N'Xịt Thơm Hoa Anh Đào Bath & Body Works Japanese Cherry Blossom Mist', N'Mùi hương quốc dân huyền thoại sống mãi cùng năm tháng của BBW với hoa anh đào Nhật Bản và quả lê giòn.', N'Mỹ', N'Hoa anh đào Nhật Bản, Quả lê châu Á, Hoa trinh nữ Mimosa, Gỗ đàn hương', N'Dịu dàng thanh lịch, thích hợp dùng hằng ngày đi học đi làm nhẹ nhàng tươi mát.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (229, 29, 20, N'Xịt Thơm Ngọt Ngào Bath & Body Works Warm Vanilla Sugar Fine Fragrance Mist', N'Mùi hương vani đường nâu ấm áp ngọt ngào như mẻ bánh quy bơ vừa mới ra lò.', N'Mỹ', N'Vani Bourbon nguyên chất, Đường nâu caramen, Hoa phong lan trắng', N'Ấm áp ngọt lịm, mang lại cảm giác dễ chịu gần gũi trong những ngày se lạnh.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (230, 30, 20, N'Xịt Thơm Quyến Rũ Victoria''s Secret Pure Seduction Fragrance Mist', N'Mùi hương quả mận chín mọng kết hợp hoa lan Nam Phi nồng nàn mê đắm được triệu cô gái yêu thích.', N'Mỹ', N'Quả mận ép tươi, Hoa lan Nam Phi nghiền, Tinh chất Lô Hội dưỡng da', N'Ngọt ngào trẻ trung năng động, tỏa hương thơm ngát tự tin suốt ngày dài.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (231, 30, 20, N'Xịt Thơm Ấm Áp Victoria''s Secret Bare Vanilla Fragrance Mist', N'Hương vani ấm mịn đánh bông hòa quyện cùng hương táo mềm mại gợi cảm khó cưỡng.', N'Mỹ', N'Vani roi kem béo ngậy, Táo chín ngọt, Tinh chất dưỡng ẩm da', N'Tạo cảm giác ấm áp quyến rũ như một cái ôm êm ái.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (232, 30, 20, N'Xịt Thơm Dịu Ngọt Victoria''s Secret Velvet Petals Fragrance Mist', N'Hương hoa cỏ bồng bềnh hòa quyện hạnh nhân ngào đường đem lại nét đáng yêu dịu dàng.', N'Mỹ', N'Cánh hoa nhung mềm, Hạnh nhân nướng, Chiết xuất hoa cúc La Mã', N'Hương phấn thơm ngọt ngào thư giãn, thích hợp sau khi tắm hoặc trước khi ngủ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (233, 30, 20, N'Xịt Thơm Tươi Mát Đại Dương Victoria''s Secret Aqua Kiss Fragrance Mist', N'Cơn mưa rào mùa hạ tươi mát với hoa lan Nam Phi ướt sương và hoa cúc biển sảng khoái.', N'Mỹ', N'Nước khoáng biển sâu, Hoa lan Nam Phi, Hoa cúc cúc dại', N'Thanh mát sảng khoái đánh bay cái nóng oi bức, tiếp thêm năng lượng ngày mới.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (234, 1, 20, N'Xịt Thơm Tóc & Body Tinh Dầu Hoa Bưởi Cocoon Tươi Mát 140ml', N'Hương hoa bưởi thanh khiết tháng Ba miền Bắc kết hợp nước cất vỏ bưởi giải tỏa căng thẳng.', N'Việt Nam', N'Nước cất hoa bưởi nguyên chất, Tinh dầu hoa cúc, Nước khoáng thiên nhiên', N'Làm thơm dịu mát cơ thể và mái tóc, khử mùi thức ăn bám vào tóc sau khi ăn uống.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (235, 29, 20, N'Xịt Thơm Bữa Tiệc Bãi Biển Bath & Body Works At The Beach Fine Fragrance Mist', N'Hương thơm ngập tràn hơi thở mùa hè với hoa dừa trắng, cam bergamot và làn gió biển ấm áp.', N'Mỹ', N'Nước dừa tươi, Hoa mộc lan dừa, Cam bergamot, Muối biển', N'Đem lại cảm giác thư giãn tựa như đang nằm dài dưới rặng dừa nhiệt đới.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (236, 29, 20, N'Xịt Thơm Ngàn Điều Ước Bath & Body Works A Thousand Wishes Mist', N'Ly rượu sâm panh hồng lấp lánh hòa cùng hoa mẫu đơn pha lê và kem hạnh nhân ngọt ngào.', N'Mỹ', N'Rượu Champagne hồng Procecco, Hoa mẫu đơn pha lê, Hổ phách mạ vàng', N'Không khí lễ hội ngập tràn niềm vui và sự ấm áp lộng lẫy.', 1, N'ACTIVE', N'2026-01-01 00:00:00'),
    (237, 30, 20, N'Xịt Thơm Tình Yêu Quyến Rũ Victoria''s Secret Love Spell Fragrance Mist', N'Sự hòa quyện kinh điển giữa hoa anh đào trắng mộng mơ và quả đào mọng nước ngọt ngào.', N'Mỹ', N'Hoa anh đào trắng, Đào tươi ép mật, Chiết xuất cúc La Mã', N'Mùi hương tươi vui ngọt lịm như mối tình đầu e ấp rạng ngời.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (238, 5, 20, N'Xịt Thơm Toàn Thân & Tóc Hương Trà Đen Innisfree Perfumed Body & Hair Mist', N'Hương trà đen thảo mộc thanh tao từ đảo Jeju giúp thanh lọc tâm trí và lưu lại cảm giác dễ chịu.', N'Hàn Quốc', N'Chiết xuất Trà đen đảo Jeju, Tinh dầu hoa cỏ tự nhiên', N'Xịt dưỡng thơm mát nhẹ nhàng cho cả cơ thể và mái tóc khô xơ.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (239, 29, 20, N'Xịt Thơm Thư Giãn Tinh Thần Bath & Body Works Eucalyptus Tea Body Mist', N'Hương thảo mộc tinh tế kết hợp lá khuynh diệp và trà thảo mộc giúp xua tan mệt mỏi sau ngày dài.', N'Mỹ', N'Tinh dầu khuynh diệp nguyên chất, Trà thảo mộc thanh khiết', N'Tạo bầu không khí thư thái bình yên hỗ trợ giấc ngủ ngon sâu.', 0, N'ACTIVE', N'2026-01-01 00:00:00'),
    (240, 30, 20, N'Xịt Thơm Bờ Biển Nhiệt Đới Victoria''s Secret Coconut Passion Fragrance Mist', N'Hương thơm kem dừa ấm hòa quyện vani ngọt ngào quyến rũ đưa bạn đến ốc đảo nhiệt đới ngập nắng.', N'Mỹ', N'Cơm dừa nướng, Vani ngọt ấm, Tinh chất hoa cúc Chamomile', N'Lưu hương béo ngọt êm ái, tạo nét thu hút khó cưỡng cho phái đẹp.', 0, N'ACTIVE', N'2026-01-01 00:00:00');
GO

SET IDENTITY_INSERT dbo.products OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: PRODUCT_ATTRIBUTE_VALUES (474 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.product_attribute_values ON;
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (1, 1, 1, N'Da Dầu'),
    (2, 1, 5, N'Da Mụn'),
    (3, 1, 6, N'Trị Mụn'),
    (4, 1, 8, N'Dưỡng Sáng Mờ Thâm'),
    (5, 2, 1, N'Da Dầu'),
    (6, 2, 4, N'Da Hỗn Hợp'),
    (7, 2, 6, N'Trị Mụn'),
    (8, 2, 8, N'Dưỡng Sáng Mờ Thâm'),
    (9, 3, 2, N'Da Khô'),
    (10, 3, 3, N'Da Nhạy Cảm'),
    (11, 3, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (12, 3, 10, N'Phục Hồi Da'),
    (13, 4, 2, N'Da Khô'),
    (14, 4, 4, N'Da Hỗn Hợp'),
    (15, 4, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (16, 5, 2, N'Da Khô'),
    (17, 5, 3, N'Da Nhạy Cảm'),
    (18, 5, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (19, 5, 8, N'Dưỡng Sáng Mờ Thâm'),
    (20, 6, 2, N'Da Khô'),
    (21, 6, 3, N'Da Nhạy Cảm'),
    (22, 6, 9, N'Chống Lão Hóa'),
    (23, 6, 10, N'Phục Hồi Da'),
    (24, 7, 3, N'Da Nhạy Cảm'),
    (25, 7, 4, N'Da Hỗn Hợp'),
    (26, 7, 8, N'Dưỡng Sáng Mờ Thâm'),
    (27, 8, 1, N'Da Dầu'),
    (28, 8, 2, N'Da Khô'),
    (29, 8, 4, N'Da Hỗn Hợp'),
    (30, 8, 9, N'Chống Lão Hóa'),
    (31, 9, 2, N'Da Khô'),
    (32, 9, 3, N'Da Nhạy Cảm'),
    (33, 9, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (34, 9, 10, N'Phục Hồi Da'),
    (35, 10, 1, N'Da Dầu'),
    (36, 10, 4, N'Da Hỗn Hợp'),
    (37, 10, 8, N'Dưỡng Sáng Mờ Thâm'),
    (38, 11, 2, N'Da Khô'),
    (39, 11, 3, N'Da Nhạy Cảm'),
    (40, 11, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (41, 11, 9, N'Chống Lão Hóa'),
    (42, 12, 2, N'Da Khô'),
    (43, 12, 3, N'Da Nhạy Cảm'),
    (44, 12, 9, N'Chống Lão Hóa'),
    (45, 12, 10, N'Phục Hồi Da'),
    (46, 13, 1, N'Da Dầu'),
    (47, 13, 5, N'Da Mụn'),
    (48, 13, 6, N'Trị Mụn'),
    (49, 13, 8, N'Dưỡng Sáng Mờ Thâm'),
    (50, 14, 2, N'Da Khô');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (51, 14, 3, N'Da Nhạy Cảm'),
    (52, 14, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (53, 15, 2, N'Da Khô'),
    (54, 15, 3, N'Da Nhạy Cảm'),
    (55, 15, 9, N'Chống Lão Hóa'),
    (56, 15, 10, N'Phục Hồi Da'),
    (57, 16, 2, N'Da Khô'),
    (58, 16, 3, N'Da Nhạy Cảm'),
    (59, 16, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (60, 16, 10, N'Phục Hồi Da'),
    (61, 17, 2, N'Da Khô'),
    (62, 17, 3, N'Da Nhạy Cảm'),
    (63, 17, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (64, 17, 10, N'Phục Hồi Da'),
    (65, 18, 2, N'Da Khô'),
    (66, 18, 3, N'Da Nhạy Cảm'),
    (67, 18, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (68, 18, 10, N'Phục Hồi Da'),
    (69, 19, 2, N'Da Khô'),
    (70, 19, 4, N'Da Hỗn Hợp'),
    (71, 19, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (72, 20, 2, N'Da Khô'),
    (73, 20, 3, N'Da Nhạy Cảm'),
    (74, 20, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (75, 21, 2, N'Da Khô'),
    (76, 21, 4, N'Da Hỗn Hợp'),
    (77, 21, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (78, 21, 10, N'Phục Hồi Da'),
    (79, 22, 1, N'Da Dầu'),
    (80, 22, 5, N'Da Mụn'),
    (81, 22, 6, N'Trị Mụn'),
    (82, 22, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (83, 23, 2, N'Da Khô'),
    (84, 23, 3, N'Da Nhạy Cảm'),
    (85, 23, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (86, 23, 10, N'Phục Hồi Da'),
    (87, 24, 2, N'Da Khô'),
    (88, 24, 3, N'Da Nhạy Cảm'),
    (89, 24, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (90, 25, 1, N'Da Dầu'),
    (91, 25, 4, N'Da Hỗn Hợp'),
    (92, 25, 6, N'Trị Mụn'),
    (93, 26, 2, N'Da Khô'),
    (94, 26, 9, N'Chống Lão Hóa'),
    (95, 26, 10, N'Phục Hồi Da'),
    (96, 27, 2, N'Da Khô'),
    (97, 27, 3, N'Da Nhạy Cảm'),
    (98, 27, 9, N'Chống Lão Hóa'),
    (99, 27, 10, N'Phục Hồi Da'),
    (100, 28, 1, N'Da Dầu');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (101, 28, 5, N'Da Mụn'),
    (102, 28, 6, N'Trị Mụn'),
    (103, 28, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (104, 29, 2, N'Da Khô'),
    (105, 29, 4, N'Da Hỗn Hợp'),
    (106, 29, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (107, 30, 1, N'Da Dầu'),
    (108, 30, 5, N'Da Mụn'),
    (109, 30, 6, N'Trị Mụn'),
    (110, 31, 1, N'Da Dầu'),
    (111, 31, 4, N'Da Hỗn Hợp'),
    (112, 31, 8, N'Dưỡng Sáng Mờ Thâm'),
    (113, 32, 1, N'Da Dầu'),
    (114, 32, 5, N'Da Mụn'),
    (115, 32, 6, N'Trị Mụn'),
    (116, 33, 1, N'Da Dầu'),
    (117, 33, 5, N'Da Mụn'),
    (118, 33, 6, N'Trị Mụn'),
    (119, 34, 2, N'Da Khô'),
    (120, 34, 4, N'Da Hỗn Hợp'),
    (121, 34, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (122, 35, 2, N'Da Khô'),
    (123, 35, 3, N'Da Nhạy Cảm'),
    (124, 35, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (125, 36, 2, N'Da Khô'),
    (126, 36, 3, N'Da Nhạy Cảm'),
    (127, 36, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (128, 36, 10, N'Phục Hồi Da'),
    (129, 37, 1, N'Da Dầu'),
    (130, 37, 3, N'Da Nhạy Cảm'),
    (131, 37, 4, N'Da Hỗn Hợp'),
    (132, 37, 10, N'Phục Hồi Da'),
    (133, 38, 2, N'Da Khô'),
    (134, 38, 3, N'Da Nhạy Cảm'),
    (135, 38, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (136, 39, 1, N'Da Dầu'),
    (137, 39, 5, N'Da Mụn'),
    (138, 39, 6, N'Trị Mụn'),
    (139, 40, 1, N'Da Dầu'),
    (140, 40, 5, N'Da Mụn'),
    (141, 40, 6, N'Trị Mụn'),
    (142, 41, 2, N'Da Khô'),
    (143, 41, 3, N'Da Nhạy Cảm'),
    (144, 41, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (145, 42, 2, N'Da Khô'),
    (146, 42, 3, N'Da Nhạy Cảm'),
    (147, 42, 9, N'Chống Lão Hóa'),
    (148, 42, 10, N'Phục Hồi Da'),
    (149, 43, 2, N'Da Khô'),
    (150, 43, 3, N'Da Nhạy Cảm');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (151, 43, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (152, 44, 2, N'Da Khô'),
    (153, 44, 4, N'Da Hỗn Hợp'),
    (154, 44, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (155, 45, 1, N'Da Dầu'),
    (156, 45, 4, N'Da Hỗn Hợp'),
    (157, 45, 8, N'Dưỡng Sáng Mờ Thâm'),
    (158, 46, 1, N'Da Dầu'),
    (159, 46, 4, N'Da Hỗn Hợp'),
    (160, 46, 9, N'Chống Lão Hóa'),
    (161, 47, 1, N'Da Dầu'),
    (162, 47, 4, N'Da Hỗn Hợp'),
    (163, 47, 9, N'Chống Lão Hóa'),
    (164, 48, 1, N'Da Dầu'),
    (165, 48, 4, N'Da Hỗn Hợp'),
    (166, 48, 8, N'Dưỡng Sáng Mờ Thâm'),
    (167, 49, 1, N'Da Dầu'),
    (168, 49, 4, N'Da Hỗn Hợp'),
    (169, 49, 9, N'Chống Lão Hóa'),
    (170, 50, 1, N'Da Dầu'),
    (171, 50, 4, N'Da Hỗn Hợp'),
    (172, 50, 8, N'Dưỡng Sáng Mờ Thâm'),
    (173, 51, 1, N'Da Dầu'),
    (174, 51, 5, N'Da Mụn'),
    (175, 51, 6, N'Trị Mụn'),
    (176, 51, 9, N'Chống Lão Hóa'),
    (177, 52, 1, N'Da Dầu'),
    (178, 52, 4, N'Da Hỗn Hợp'),
    (179, 52, 9, N'Chống Lão Hóa'),
    (180, 53, 1, N'Da Dầu'),
    (181, 53, 2, N'Da Khô'),
    (182, 53, 4, N'Da Hỗn Hợp'),
    (183, 53, 9, N'Chống Lão Hóa'),
    (184, 54, 1, N'Da Dầu'),
    (185, 54, 5, N'Da Mụn'),
    (186, 54, 6, N'Trị Mụn'),
    (187, 55, 2, N'Da Khô'),
    (188, 55, 3, N'Da Nhạy Cảm'),
    (189, 55, 9, N'Chống Lão Hóa'),
    (190, 56, 1, N'Da Dầu'),
    (191, 56, 5, N'Da Mụn'),
    (192, 56, 6, N'Trị Mụn'),
    (193, 57, 2, N'Da Khô'),
    (194, 57, 3, N'Da Nhạy Cảm'),
    (195, 57, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (196, 58, 2, N'Da Khô'),
    (197, 58, 4, N'Da Hỗn Hợp'),
    (198, 58, 8, N'Dưỡng Sáng Mờ Thâm'),
    (199, 58, 9, N'Chống Lão Hóa'),
    (200, 59, 2, N'Da Khô');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (201, 59, 3, N'Da Nhạy Cảm'),
    (202, 59, 8, N'Dưỡng Sáng Mờ Thâm'),
    (203, 59, 9, N'Chống Lão Hóa'),
    (204, 60, 1, N'Da Dầu'),
    (205, 60, 4, N'Da Hỗn Hợp'),
    (206, 60, 9, N'Chống Lão Hóa'),
    (207, 61, 2, N'Da Khô'),
    (208, 61, 3, N'Da Nhạy Cảm'),
    (209, 61, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (210, 61, 10, N'Phục Hồi Da'),
    (211, 62, 2, N'Da Khô'),
    (212, 62, 3, N'Da Nhạy Cảm'),
    (213, 62, 10, N'Phục Hồi Da'),
    (214, 63, 1, N'Da Dầu'),
    (215, 63, 3, N'Da Nhạy Cảm'),
    (216, 63, 4, N'Da Hỗn Hợp'),
    (217, 63, 6, N'Trị Mụn'),
    (218, 64, 2, N'Da Khô'),
    (219, 64, 3, N'Da Nhạy Cảm'),
    (220, 64, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (221, 65, 2, N'Da Khô'),
    (222, 65, 3, N'Da Nhạy Cảm'),
    (223, 65, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (224, 66, 2, N'Da Khô'),
    (225, 66, 3, N'Da Nhạy Cảm'),
    (226, 66, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (227, 67, 1, N'Da Dầu'),
    (228, 67, 5, N'Da Mụn'),
    (229, 67, 6, N'Trị Mụn'),
    (230, 68, 1, N'Da Dầu'),
    (231, 68, 4, N'Da Hỗn Hợp'),
    (232, 68, 6, N'Trị Mụn'),
    (233, 69, 1, N'Da Dầu'),
    (234, 69, 4, N'Da Hỗn Hợp'),
    (235, 69, 8, N'Dưỡng Sáng Mờ Thâm'),
    (236, 70, 1, N'Da Dầu'),
    (237, 70, 5, N'Da Mụn'),
    (238, 70, 6, N'Trị Mụn'),
    (239, 71, 2, N'Da Khô'),
    (240, 71, 3, N'Da Nhạy Cảm'),
    (241, 71, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (242, 72, 1, N'Da Dầu'),
    (243, 72, 4, N'Da Hỗn Hợp'),
    (244, 72, 8, N'Dưỡng Sáng Mờ Thâm'),
    (245, 73, 2, N'Da Khô'),
    (246, 73, 3, N'Da Nhạy Cảm'),
    (247, 73, 9, N'Chống Lão Hóa'),
    (248, 74, 2, N'Da Khô'),
    (249, 74, 3, N'Da Nhạy Cảm'),
    (250, 74, 7, N'Dưỡng Ẩm Chuyên Sâu');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (251, 74, 10, N'Phục Hồi Da'),
    (252, 75, 1, N'Da Dầu'),
    (253, 75, 5, N'Da Mụn'),
    (254, 75, 6, N'Trị Mụn'),
    (255, 76, 2, N'Da Khô'),
    (256, 76, 3, N'Da Nhạy Cảm'),
    (257, 76, 4, N'Da Hỗn Hợp'),
    (258, 77, 1, N'Da Dầu'),
    (259, 77, 4, N'Da Hỗn Hợp'),
    (260, 78, 2, N'Da Khô'),
    (261, 78, 3, N'Da Nhạy Cảm'),
    (262, 78, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (263, 79, 1, N'Da Dầu'),
    (264, 79, 2, N'Da Khô'),
    (265, 79, 4, N'Da Hỗn Hợp'),
    (266, 80, 2, N'Da Khô'),
    (267, 80, 3, N'Da Nhạy Cảm'),
    (268, 80, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (269, 81, 1, N'Da Dầu'),
    (270, 81, 5, N'Da Mụn'),
    (271, 81, 6, N'Trị Mụn'),
    (272, 82, 3, N'Da Nhạy Cảm'),
    (273, 82, 4, N'Da Hỗn Hợp'),
    (274, 83, 1, N'Da Dầu'),
    (275, 83, 5, N'Da Mụn'),
    (276, 83, 6, N'Trị Mụn'),
    (277, 84, 1, N'Da Dầu'),
    (278, 84, 4, N'Da Hỗn Hợp'),
    (279, 85, 1, N'Da Dầu'),
    (280, 85, 5, N'Da Mụn'),
    (281, 85, 6, N'Trị Mụn'),
    (282, 86, 1, N'Da Dầu'),
    (283, 86, 5, N'Da Mụn'),
    (284, 86, 6, N'Trị Mụn'),
    (285, 87, 2, N'Da Khô'),
    (286, 87, 3, N'Da Nhạy Cảm'),
    (287, 87, 4, N'Da Hỗn Hợp'),
    (288, 88, 2, N'Da Khô'),
    (289, 88, 3, N'Da Nhạy Cảm'),
    (290, 88, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (291, 88, 10, N'Phục Hồi Da'),
    (292, 89, 2, N'Da Khô'),
    (293, 89, 3, N'Da Nhạy Cảm'),
    (294, 89, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (295, 89, 10, N'Phục Hồi Da'),
    (296, 90, 2, N'Da Khô'),
    (297, 90, 3, N'Da Nhạy Cảm'),
    (298, 90, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (299, 91, 1, N'Da Dầu'),
    (300, 91, 4, N'Da Hỗn Hợp');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (301, 91, 6, N'Trị Mụn'),
    (302, 92, 1, N'Da Dầu'),
    (303, 92, 4, N'Da Hỗn Hợp'),
    (304, 92, 8, N'Dưỡng Sáng Mờ Thâm'),
    (305, 93, 1, N'Da Dầu'),
    (306, 93, 2, N'Da Khô'),
    (307, 93, 4, N'Da Hỗn Hợp'),
    (308, 93, 8, N'Dưỡng Sáng Mờ Thâm'),
    (309, 94, 2, N'Da Khô'),
    (310, 94, 3, N'Da Nhạy Cảm'),
    (311, 94, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (312, 95, 1, N'Da Dầu'),
    (313, 95, 5, N'Da Mụn'),
    (314, 95, 6, N'Trị Mụn'),
    (315, 96, 1, N'Da Dầu'),
    (316, 96, 4, N'Da Hỗn Hợp'),
    (317, 96, 6, N'Trị Mụn'),
    (318, 97, 1, N'Da Dầu'),
    (319, 97, 4, N'Da Hỗn Hợp'),
    (320, 97, 8, N'Dưỡng Sáng Mờ Thâm'),
    (321, 98, 1, N'Da Dầu'),
    (322, 98, 5, N'Da Mụn'),
    (323, 98, 6, N'Trị Mụn'),
    (324, 99, 2, N'Da Khô'),
    (325, 99, 3, N'Da Nhạy Cảm'),
    (326, 99, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (327, 100, 1, N'Da Dầu'),
    (328, 100, 2, N'Da Khô'),
    (329, 100, 4, N'Da Hỗn Hợp'),
    (330, 100, 8, N'Dưỡng Sáng Mờ Thâm'),
    (331, 101, 2, N'Da Khô'),
    (332, 101, 3, N'Da Nhạy Cảm'),
    (333, 101, 8, N'Dưỡng Sáng Mờ Thâm'),
    (334, 101, 9, N'Chống Lão Hóa'),
    (335, 102, 1, N'Da Dầu'),
    (336, 102, 5, N'Da Mụn'),
    (337, 102, 6, N'Trị Mụn'),
    (338, 103, 1, N'Da Dầu'),
    (339, 103, 4, N'Da Hỗn Hợp'),
    (340, 103, 6, N'Trị Mụn'),
    (341, 104, 1, N'Da Dầu'),
    (342, 104, 4, N'Da Hỗn Hợp'),
    (343, 104, 8, N'Dưỡng Sáng Mờ Thâm'),
    (344, 105, 2, N'Da Khô'),
    (345, 105, 3, N'Da Nhạy Cảm'),
    (346, 105, 8, N'Dưỡng Sáng Mờ Thâm'),
    (347, 105, 9, N'Chống Lão Hóa'),
    (348, 106, 2, N'Da Khô'),
    (349, 106, 3, N'Da Nhạy Cảm'),
    (350, 106, 7, N'Dưỡng Ẩm Chuyên Sâu');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (351, 106, 10, N'Phục Hồi Da'),
    (352, 107, 1, N'Da Dầu'),
    (353, 107, 4, N'Da Hỗn Hợp'),
    (354, 107, 6, N'Trị Mụn'),
    (355, 108, 1, N'Da Dầu'),
    (356, 108, 4, N'Da Hỗn Hợp'),
    (357, 108, 6, N'Trị Mụn'),
    (358, 109, 1, N'Da Dầu'),
    (359, 109, 5, N'Da Mụn'),
    (360, 109, 6, N'Trị Mụn'),
    (361, 110, 2, N'Da Khô'),
    (362, 110, 3, N'Da Nhạy Cảm'),
    (363, 110, 10, N'Phục Hồi Da'),
    (364, 111, 1, N'Da Dầu'),
    (365, 111, 4, N'Da Hỗn Hợp'),
    (366, 111, 8, N'Dưỡng Sáng Mờ Thâm'),
    (367, 112, 2, N'Da Khô'),
    (368, 112, 3, N'Da Nhạy Cảm'),
    (369, 112, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (370, 112, 10, N'Phục Hồi Da'),
    (371, 113, 2, N'Da Khô'),
    (372, 113, 3, N'Da Nhạy Cảm'),
    (373, 113, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (374, 114, 2, N'Da Khô'),
    (375, 114, 3, N'Da Nhạy Cảm'),
    (376, 114, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (377, 114, 9, N'Chống Lão Hóa'),
    (378, 115, 1, N'Da Dầu'),
    (379, 115, 4, N'Da Hỗn Hợp'),
    (380, 115, 8, N'Dưỡng Sáng Mờ Thâm'),
    (381, 116, 2, N'Da Khô'),
    (382, 116, 3, N'Da Nhạy Cảm'),
    (383, 116, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (384, 117, 2, N'Da Khô'),
    (385, 117, 3, N'Da Nhạy Cảm'),
    (386, 117, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (387, 117, 10, N'Phục Hồi Da'),
    (388, 118, 2, N'Da Khô'),
    (389, 118, 3, N'Da Nhạy Cảm'),
    (390, 118, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (391, 119, 2, N'Da Khô'),
    (392, 119, 3, N'Da Nhạy Cảm'),
    (393, 119, 9, N'Chống Lão Hóa'),
    (394, 119, 10, N'Phục Hồi Da'),
    (395, 120, 2, N'Da Khô'),
    (396, 120, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (397, 123, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (398, 126, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (399, 132, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (400, 133, 7, N'Dưỡng Ẩm Chuyên Sâu');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (401, 136, 1, N'Da Dầu'),
    (402, 136, 4, N'Da Hỗn Hợp'),
    (403, 137, 1, N'Da Dầu'),
    (404, 137, 4, N'Da Hỗn Hợp'),
    (405, 138, 1, N'Da Dầu'),
    (406, 138, 4, N'Da Hỗn Hợp'),
    (407, 139, 2, N'Da Khô'),
    (408, 139, 3, N'Da Nhạy Cảm'),
    (409, 139, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (410, 140, 1, N'Da Dầu'),
    (411, 140, 4, N'Da Hỗn Hợp'),
    (412, 141, 1, N'Da Dầu'),
    (413, 141, 4, N'Da Hỗn Hợp'),
    (414, 142, 1, N'Da Dầu'),
    (415, 142, 4, N'Da Hỗn Hợp'),
    (416, 143, 1, N'Da Dầu'),
    (417, 143, 4, N'Da Hỗn Hợp'),
    (418, 144, 4, N'Da Hỗn Hợp'),
    (419, 145, 2, N'Da Khô'),
    (420, 145, 4, N'Da Hỗn Hợp'),
    (421, 146, 2, N'Da Khô'),
    (422, 146, 3, N'Da Nhạy Cảm'),
    (423, 146, 9, N'Chống Lão Hóa'),
    (424, 147, 1, N'Da Dầu'),
    (425, 147, 5, N'Da Mụn'),
    (426, 147, 6, N'Trị Mụn'),
    (427, 148, 1, N'Da Dầu'),
    (428, 148, 4, N'Da Hỗn Hợp'),
    (429, 149, 3, N'Da Nhạy Cảm'),
    (430, 149, 4, N'Da Hỗn Hợp'),
    (431, 149, 8, N'Dưỡng Sáng Mờ Thâm'),
    (432, 150, 1, N'Da Dầu'),
    (433, 150, 4, N'Da Hỗn Hợp'),
    (434, 152, 1, N'Da Dầu'),
    (435, 152, 4, N'Da Hỗn Hợp'),
    (436, 154, 1, N'Da Dầu'),
    (437, 154, 4, N'Da Hỗn Hợp'),
    (438, 158, 1, N'Da Dầu'),
    (439, 158, 4, N'Da Hỗn Hợp'),
    (440, 159, 1, N'Da Dầu'),
    (441, 159, 4, N'Da Hỗn Hợp'),
    (442, 160, 1, N'Da Dầu'),
    (443, 160, 5, N'Da Mụn'),
    (444, 160, 6, N'Trị Mụn'),
    (445, 164, 2, N'Da Khô'),
    (446, 164, 4, N'Da Hỗn Hợp'),
    (447, 164, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (448, 181, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (449, 181, 8, N'Dưỡng Sáng Mờ Thâm'),
    (450, 182, 7, N'Dưỡng Ẩm Chuyên Sâu');
GO

INSERT INTO dbo.product_attribute_values (id, product_id, attribute_id, [value]) VALUES
    (451, 183, 6, N'Trị Mụn'),
    (452, 183, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (453, 183, 10, N'Phục Hồi Da'),
    (454, 184, 6, N'Trị Mụn'),
    (455, 184, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (456, 185, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (457, 185, 8, N'Dưỡng Sáng Mờ Thâm'),
    (458, 186, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (459, 186, 8, N'Dưỡng Sáng Mờ Thâm'),
    (460, 187, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (461, 188, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (462, 189, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (463, 190, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (464, 190, 8, N'Dưỡng Sáng Mờ Thâm'),
    (465, 191, 2, N'Da Khô'),
    (466, 191, 3, N'Da Nhạy Cảm'),
    (467, 191, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (468, 192, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (469, 193, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (470, 194, 2, N'Da Khô'),
    (471, 194, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (472, 194, 10, N'Phục Hồi Da'),
    (473, 195, 7, N'Dưỡng Ẩm Chuyên Sâu'),
    (474, 195, 10, N'Phục Hồi Da');
GO

SET IDENTITY_INSERT dbo.product_attribute_values OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: SKUS (318 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.skus ON;
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (1, 1, N'SKU-CCN-BD-70ML', N'Dung tích 70ml', 265000, 295000, N'893851500001', N'ACTIVE'),
    (2, 1, N'SKU-CCN-BD-140ML', N'Dung tích 140ml Tiết Kiệm', 465000, 520000, N'893851500002', N'ACTIVE'),
    (3, 2, N'SKU-TO-NIA-30ML', N'Dung tích 30ml', 220000, 250000, N'769915190011', N'ACTIVE'),
    (4, 2, N'SKU-TO-NIA-60ML', N'Dung tích 60ml', 380000, 430000, N'769915190012', N'ACTIVE'),
    (5, 3, N'SKU-LRP-B5-30ML', N'Dung tích 30ml', 980000, 1150000, N'3337875583626', N'ACTIVE'),
    (6, 3, N'SKU-LRP-B5-50ML', N'Dung tích 50ml', 1450000, 1650000, N'3337875583633', N'ACTIVE'),
    (7, 4, N'SKU-INN-TEA-80ML', N'Dung tích 80ml', 610000, 680000, N'8809612850011', N'ACTIVE'),
    (8, 4, N'SKU-INN-TEA-160ML', N'Dung tích 160ml Jumbo', 980000, 1100000, N'8809612850012', N'ACTIVE'),
    (9, 5, N'SKU-LOR-HA-30ML', N'Dung tích 30ml', 399000, 449000, N'8992304037512', N'ACTIVE'),
    (10, 5, N'SKU-LOR-HA-50ML', N'Dung tích 50ml', 580000, 640000, N'8992304037529', N'ACTIVE'),
    (11, 6, N'SKU-EL-ANR-30ML', N'Dung tích 30ml', 2100000, 2350000, N'027131267111', N'ACTIVE'),
    (12, 6, N'SKU-EL-ANR-50ML', N'Dung tích 50ml', 3150000, 3500000, N'027131267128', N'ACTIVE'),
    (13, 7, N'SKU-KLR-VIT-35ML', N'Chai 35ml', 365000, 420000, N'8809572890212', N'ACTIVE'),
    (14, 8, N'SKU-KHL-RET-30ML', N'Chai vòi nhấn 30ml', 1750000, 1950000, N'3605972511211', N'ACTIVE'),
    (15, 8, N'SKU-KHL-RET-50ML', N'Chai vòi nhấn 50ml', 2400000, 2650000, N'3605972511228', N'ACTIVE'),
    (16, 9, N'SKU-VIC-M89-50ML', N'Chai 50ml', 950000, 1080000, N'3337875543248', N'ACTIVE'),
    (17, 9, N'SKU-VIC-M89-75ML', N'Chai 75ml Tiết Kiệm', 1250000, 1390000, N'3337875543255', N'ACTIVE'),
    (18, 10, N'SKU-EUC-SPOT-30ML', N'Chai 30ml', 1290000, 1450000, N'4005800261111', N'ACTIVE'),
    (19, 11, N'SKU-PC-HA-15ML', N'Chai 15ml', 1100000, 1250000, N'065543901511', N'ACTIVE'),
    (20, 12, N'SKU-SUL-FC-60ML', N'Chai 60ml', 2200000, 2450000, N'8809685811110', N'ACTIVE'),
    (21, 12, N'SKU-SUL-FC-90ML', N'Chai 90ml', 2900000, 3200000, N'8809685811127', N'ACTIVE'),
    (22, 13, N'SKU-LRP-EFF-30ML', N'Chai 30ml', 895000, 990000, N'3337875722827', N'ACTIVE'),
    (23, 14, N'SKU-SIM-HA-30ML', N'Chai 30ml', 245000, 290000, N'8720181123456', N'ACTIVE'),
    (24, 15, N'SKU-DIO-CAP-30ML', N'Chai 30ml', 3650000, 3950000, N'3348901611111', N'ACTIVE'),
    (25, 15, N'SKU-DIO-CAP-50ML', N'Chai 50ml', 4950000, 5350000, N'3348901611128', N'ACTIVE'),
    (26, 16, N'SKU-LRP-CICA-40ML', N'Dung tích 40ml', 365000, 410000, N'3337875816809', N'ACTIVE'),
    (27, 16, N'SKU-LRP-CICA-100ML', N'Dung tích 100ml Tiết Kiệm', 620000, 690000, N'3337875816816', N'ACTIVE'),
    (28, 17, N'SKU-CRV-LOT-236ML', N'Chai vòi 236ml', 340000, 385000, N'3337875597210', N'ACTIVE'),
    (29, 17, N'SKU-CRV-LOT-473ML', N'Chai vòi 473ml', 470000, 520000, N'3337875597227', N'ACTIVE'),
    (30, 18, N'SKU-KLR-CRM-80ML', N'Tuýp 80ml', 375000, 420000, N'8809572890113', N'ACTIVE'),
    (31, 19, N'SKU-INN-GTC-50ML', N'Hũ 50ml', 560000, 620000, N'8809612850310', N'ACTIVE'),
    (32, 20, N'SKU-KHL-ULT-50ML', N'Hũ 50ml', 1100000, 1250000, N'3605970371112', N'ACTIVE'),
    (33, 20, N'SKU-KHL-ULT-125ML', N'Hũ 125ml Jumbo', 2150000, 2350000, N'3605970371129', N'ACTIVE'),
    (34, 21, N'SKU-LAN-WB-50ML', N'Hũ 50ml', 950000, 1050000, N'8809643061112', N'ACTIVE'),
    (35, 22, N'SKU-CCN-THACH-100ML', N'Hũ 100ml', 215000, 245000, N'893851500015', N'ACTIVE'),
    (36, 23, N'SKU-EUC-LIPO-50ML', N'Hũ 50ml', 520000, 590000, N'4005800161114', N'ACTIVE'),
    (37, 24, N'SKU-SIM-CRM-125ML', N'Chai 125ml', 185000, 215000, N'8710908123456', N'ACTIVE'),
    (38, 25, N'SKU-BIO-PORE-30ML', N'Tuýp 30ml', 445000, 495000, N'3401348834413', N'ACTIVE'),
    (39, 26, N'SKU-LOR-REV-50ML', N'Hũ 50ml', 499000, 560000, N'8992304021234', N'ACTIVE'),
    (40, 27, N'SKU-SUL-GIN-60ML', N'Hũ 60ml', 4800000, 5200000, N'8809685822222', N'ACTIVE'),
    (41, 28, N'SKU-PC-CLR-60ML', N'Tuýp 60ml', 950000, 1050000, N'065543906011', N'ACTIVE'),
    (42, 29, N'SKU-VIC-AQU-50ML', N'Hũ 50ml', 890000, 990000, N'3337875588111', N'ACTIVE'),
    (43, 30, N'SKU-LRP-KPLUS-40ML', N'Tuýp 40ml', 485000, 540000, N'3337872419638', N'ACTIVE'),
    (44, 31, N'SKU-CCN-SRM-140ML', N'Chai 140ml', 175000, 195000, N'893851500003', N'ACTIVE'),
    (45, 31, N'SKU-CCN-SRM-310ML', N'Chai 310ml Tiết Kiệm', 325000, 360000, N'893851500005', N'ACTIVE'),
    (46, 32, N'SKU-CRV-FOAM-236ML', N'Chai vòi 236ml', 320000, 360000, N'3337875597111', N'ACTIVE'),
    (47, 32, N'SKU-CRV-FOAM-473ML', N'Chai vòi 473ml', 450000, 495000, N'3337875597128', N'ACTIVE'),
    (48, 33, N'SKU-LRP-GEL-200ML', N'Tuýp 200ml', 395000, 445000, N'3337872411083', N'ACTIVE'),
    (49, 33, N'SKU-LRP-GEL-400ML', N'Chai vòi 400ml', 550000, 620000, N'3337872411090', N'ACTIVE'),
    (50, 34, N'SKU-INN-FOAM-150G', N'Tuýp 150g', 240000, 275000, N'8809612850419', N'ACTIVE');
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (51, 35, N'SKU-SIM-WASH-150ML', N'Tuýp 150ml', 135000, 155000, N'8710908123111', N'ACTIVE'),
    (52, 36, N'SKU-CRV-HYD-236ML', N'Chai 236ml', 320000, 360000, N'3337875597135', N'ACTIVE'),
    (53, 36, N'SKU-CRV-HYD-473ML', N'Chai 473ml', 450000, 495000, N'3337875597142', N'ACTIVE'),
    (54, 37, N'SKU-KHL-CAL-230ML', N'Chai 230ml', 950000, 1050000, N'3605970611119', N'ACTIVE'),
    (55, 38, N'SKU-BIO-SENS-200ML', N'Chai vòi 200ml', 365000, 410000, N'3401560987114', N'ACTIVE'),
    (56, 39, N'SKU-VIC-NORM-200ML', N'Chai 200ml', 395000, 445000, N'3337875661119', N'ACTIVE'),
    (57, 40, N'SKU-EUC-PROA-200ML', N'Chai 200ml', 380000, 430000, N'4005800181112', N'ACTIVE'),
    (58, 41, N'SKU-KLR-FOAM-100ML', N'Chai 100ml', 295000, 335000, N'8809572890311', N'ACTIVE'),
    (59, 42, N'SKU-PC-RES-190ML', N'Chai 190ml', 780000, 880000, N'065543907811', N'ACTIVE'),
    (60, 43, N'SKU-CCN-ROSE-140ML', N'Chai 140ml', 175000, 195000, N'893851500018', N'ACTIVE'),
    (61, 44, N'SKU-DHC-SOAP-90G', N'Bánh 90g', 310000, 350000, N'4511413301111', N'ACTIVE'),
    (62, 45, N'SKU-LOR-GLY-100ML', N'Tuýp 100ml', 129000, 149000, N'8992304031211', N'ACTIVE'),
    (63, 46, N'SKU-LRP-SUN-50ML', N'Tuýp 50ml', 475000, 535000, N'3337875797641', N'ACTIVE'),
    (64, 47, N'SKU-ANS-MILK-20ML', N'Chai 20ml Mini', 245000, 275000, N'4909978120610', N'ACTIVE'),
    (65, 47, N'SKU-ANS-MILK-60ML', N'Chai 60ml Fullsize', 615000, 685000, N'4909978120627', N'ACTIVE'),
    (66, 48, N'SKU-SA-TONE-50G', N'Chai 50g', 185000, 215000, N'4987241157754', N'ACTIVE'),
    (67, 49, N'SKU-VIC-SUN-50ML', N'Tuýp 50ml', 485000, 545000, N'3337871324544', N'ACTIVE'),
    (68, 50, N'SKU-INN-SUN-50ML', N'Tuýp 50ml', 360000, 410000, N'8809612850518', N'ACTIVE'),
    (69, 51, N'SKU-EUC-SUN-50ML', N'Chai vòi 50ml', 490000, 560000, N'4005800181150', N'ACTIVE'),
    (70, 52, N'SKU-LOR-SUN-50ML', N'Chai 50ml', 299000, 340000, N'8992304033116', N'ACTIVE'),
    (71, 53, N'SKU-PC-SUN-148ML', N'Tuýp 148ml', 680000, 770000, N'065543902311', N'ACTIVE'),
    (72, 54, N'SKU-CCN-SUN-50ML', N'Chai 50ml', 365000, 395000, N'893851500021', N'ACTIVE'),
    (73, 55, N'SKU-KHL-SUN-30ML', N'Tuýp 30ml', 1250000, 1400000, N'3605971161118', N'ACTIVE'),
    (74, 55, N'SKU-KHL-SUN-60ML', N'Tuýp 60ml', 1950000, 2150000, N'3605971161125', N'ACTIVE'),
    (75, 56, N'SKU-BIO-AKN-40ML', N'Tuýp 40ml', 450000, 495000, N'3401348834116', N'ACTIVE'),
    (76, 57, N'SKU-ANS-GEL-90G', N'Tuýp 90g', 550000, 610000, N'4909978120634', N'ACTIVE'),
    (77, 58, N'SKU-DHC-SUN-50G', N'Chai 50g', 420000, 470000, N'4511413302222', N'ACTIVE'),
    (78, 59, N'SKU-SUL-SUN-50ML', N'Tuýp 50ml', 1600000, 1800000, N'8809685833333', N'ACTIVE'),
    (79, 60, N'SKU-LRP-MIST-75ML', N'Chai xịt 75ml', 445000, 495000, N'3337875558525', N'ACTIVE'),
    (80, 61, N'SKU-KLR-TON-180ML', N'Chai 180ml', 295000, 345000, N'8809572890014', N'ACTIVE'),
    (81, 62, N'SKU-VIC-SPA-50ML', N'Chai 50ml', 145000, 165000, N'3337871308612', N'ACTIVE'),
    (82, 62, N'SKU-VIC-SPA-150ML', N'Chai 150ml Tiêu Chuẩn', 255000, 290000, N'3337871308629', N'ACTIVE'),
    (83, 62, N'SKU-VIC-SPA-300ML', N'Chai 300ml Tiết Kiệm', 395000, 450000, N'3337871308636', N'ACTIVE'),
    (84, 63, N'SKU-KHL-TON-250ML', N'Chai 250ml', 1200000, 1350000, N'3605970361113', N'ACTIVE'),
    (85, 63, N'SKU-KHL-TON-500ML', N'Chai 500ml Jumbo', 2050000, 2250000, N'3605970361120', N'ACTIVE'),
    (86, 64, N'SKU-CCN-TON-140ML', N'Chai 140ml', 175000, 195000, N'893851500025', N'ACTIVE'),
    (87, 64, N'SKU-CCN-TON-310ML', N'Chai 310ml Tiết Kiệm', 315000, 350000, N'893851500026', N'ACTIVE'),
    (88, 65, N'SKU-SIM-TON-200ML', N'Chai 200ml', 145000, 175000, N'8710908123222', N'ACTIVE'),
    (89, 66, N'SKU-BIO-TON-250ML', N'Chai 250ml', 395000, 445000, N'3401573670114', N'ACTIVE'),
    (90, 67, N'SKU-INN-BIJA-200ML', N'Chai 200ml', 360000, 410000, N'8809612850617', N'ACTIVE'),
    (91, 68, N'SKU-PC-BAL-190ML', N'Chai 190ml', 750000, 850000, N'065543901311', N'ACTIVE'),
    (92, 69, N'SKU-TO-GLY-240ML', N'Chai 240ml', 350000, 395000, N'769915190025', N'ACTIVE'),
    (93, 70, N'SKU-LRP-AST-200ML', N'Chai 200ml', 420000, 470000, N'3337872412158', N'ACTIVE'),
    (94, 71, N'SKU-EUC-DERM-200ML', N'Chai 200ml', 350000, 390000, N'4005800161138', N'ACTIVE'),
    (95, 72, N'SKU-CCN-NGHE-140ML', N'Chai 140ml', 175000, 195000, N'893851500027', N'ACTIVE'),
    (96, 73, N'SKU-LOR-TON-200ML', N'Chai 200ml', 239000, 275000, N'8992304031122', N'ACTIVE'),
    (97, 74, N'SKU-SUL-BAL-150ML', N'Chai 150ml', 1550000, 1750000, N'8809685844444', N'ACTIVE'),
    (98, 75, N'SKU-LRP-SERO-150ML', N'Chai xịt 150ml', 365000, 410000, N'3337871318048', N'ACTIVE'),
    (99, 76, N'SKU-BIO-H2O-100ML', N'Chai 100ml Mini', 185000, 210000, N'3401572288105', N'ACTIVE'),
    (100, 76, N'SKU-BIO-H2O-500ML', N'Chai 500ml Tiết Kiệm', 495000, 550000, N'3401572288501', N'ACTIVE');
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (101, 77, N'SKU-LOR-MIC-400ML', N'Chai 400ml', 199000, 239000, N'8992304035123', N'ACTIVE'),
    (102, 78, N'SKU-CCN-OIL-140ML', N'Chai vòi 140ml', 175000, 195000, N'893851500031', N'ACTIVE'),
    (103, 79, N'SKU-DHC-OIL-70ML', N'Chai 70ml Mini', 295000, 340000, N'4511413303311', N'ACTIVE'),
    (104, 79, N'SKU-DHC-OIL-200ML', N'Chai 200ml', 680000, 760000, N'4511413303328', N'ACTIVE'),
    (105, 80, N'SKU-SIM-MIC-200ML', N'Chai 200ml', 135000, 160000, N'8710908123333', N'ACTIVE'),
    (106, 80, N'SKU-SIM-MIC-400ML', N'Chai 400ml', 235000, 275000, N'8710908123340', N'ACTIVE'),
    (107, 81, N'SKU-LRP-MIC-200ML', N'Chai 200ml', 365000, 410000, N'3337872411991', N'ACTIVE'),
    (108, 81, N'SKU-LRP-MIC-400ML', N'Chai 400ml Tiết Kiệm', 515000, 580000, N'3337872412004', N'ACTIVE'),
    (109, 82, N'SKU-MAY-EYE-70ML', N'Chai 70ml Mini', 115000, 135000, N'041554539111', N'ACTIVE'),
    (110, 82, N'SKU-MAY-EYE-150ML', N'Chai 150ml', 195000, 225000, N'041554539128', N'ACTIVE'),
    (111, 83, N'SKU-BIO-SEB-500ML', N'Chai 500ml', 495000, 550000, N'3401575645851', N'ACTIVE'),
    (112, 84, N'SKU-INN-APP-150ML', N'Chai 150ml', 360000, 410000, N'8809612850716', N'ACTIVE'),
    (113, 85, N'SKU-EUC-MIC-200ML', N'Chai 200ml', 350000, 395000, N'4005800181174', N'ACTIVE'),
    (114, 85, N'SKU-EUC-MIC-400ML', N'Chai 400ml', 490000, 560000, N'4005800181181', N'ACTIVE'),
    (115, 86, N'SKU-CCN-MIC-500ML', N'Chai 500ml Tiết Kiệm', 275000, 310000, N'893851500035', N'ACTIVE'),
    (116, 87, N'SKU-KLR-OIL-150ML', N'Chai 150ml', 395000, 450000, N'8809572890410', N'ACTIVE'),
    (117, 88, N'SKU-PC-BALM-104G', N'Tuýp 104g', 850000, 950000, N'065543901411', N'ACTIVE'),
    (118, 89, N'SKU-KHL-OIL-175ML', N'Chai vòi 175ml', 1250000, 1400000, N'3605971162221', N'ACTIVE'),
    (119, 90, N'SKU-LAN-MIC-400ML', N'Chai 400ml', 520000, 580000, N'8809643063333', N'ACTIVE'),
    (120, 91, N'SKU-PC-BHA-30ML', N'Dung tích 30ml Trial', 399000, 450000, N'065543902011', N'ACTIVE'),
    (121, 91, N'SKU-PC-BHA-118ML', N'Dung tích 118ml Fullsize', 949000, 1050000, N'065543902012', N'ACTIVE'),
    (122, 92, N'SKU-TO-PEEL-30ML', N'Chai 30ml', 265000, 310000, N'769915190033', N'ACTIVE'),
    (123, 93, N'SKU-CCN-FACE-150ML', N'Tuýp 150ml', 145000, 165000, N'893851500041', N'ACTIVE'),
    (124, 94, N'SKU-KLR-SUGAR-110G', N'Hũ 110g', 375000, 420000, N'8809572890519', N'ACTIVE'),
    (125, 95, N'SKU-EUC-SCRUB-100ML', N'Tuýp 100ml', 345000, 390000, N'4005800181198', N'ACTIVE'),
    (126, 96, N'SKU-INN-VOL-100ML', N'Tuýp 100ml', 390000, 440000, N'8809612850815', N'ACTIVE'),
    (127, 97, N'SKU-LOR-CLAY-50G', N'Hũ 50g', 199000, 230000, N'8992304037111', N'ACTIVE'),
    (128, 98, N'SKU-BIO-GOM-100ML', N'Tuýp 100ml', 390000, 440000, N'3401348834611', N'ACTIVE'),
    (129, 99, N'SKU-SIM-SCR-75ML', N'Tuýp 75ml', 145000, 175000, N'8710908123555', N'ACTIVE'),
    (130, 100, N'SKU-KHL-DERM-75ML', N'Tuýp 75ml', 1650000, 1850000, N'3605970364442', N'ACTIVE'),
    (131, 101, N'SKU-PC-AHA-100ML', N'Chai 100ml', 950000, 1050000, N'065543900811', N'ACTIVE'),
    (132, 102, N'SKU-CCN-BHA-140ML', N'Chai 140ml', 245000, 275000, N'893851500045', N'ACTIVE'),
    (133, 103, N'SKU-VIC-CLAY-75ML', N'Hũ 75ml', 650000, 720000, N'3337875501111', N'ACTIVE'),
    (134, 104, N'SKU-DHC-SCRUB-100G', N'Tuýp 100g', 420000, 480000, N'4511413304411', N'ACTIVE'),
    (135, 105, N'SKU-SUL-SCRUB-120ML', N'Tuýp 120ml', 1450000, 1650000, N'8809685855555', N'ACTIVE'),
    (136, 106, N'SKU-LAN-MASK-25ML', N'Hũ 25ml Mini', 180000, 210000, N'8809643067114', N'ACTIVE'),
    (137, 106, N'SKU-LAN-MASK-70ML', N'Hũ 70ml Fullsize', 680000, 750000, N'8809643067121', N'ACTIVE'),
    (138, 107, N'SKU-KHL-MASK-28ML', N'Hũ 28ml Mini', 390000, 440000, N'3605970365511', N'ACTIVE'),
    (139, 107, N'SKU-KHL-MASK-125ML', N'Hũ 125ml Fullsize', 950000, 1050000, N'3605970365528', N'ACTIVE'),
    (140, 108, N'SKU-INN-MASK-100ML', N'Hũ 100ml', 340000, 390000, N'8809612850914', N'ACTIVE'),
    (141, 109, N'SKU-CCN-MASK-100ML', N'Hũ 100ml', 145000, 165000, N'893851500051', N'ACTIVE'),
    (142, 110, N'SKU-KLR-SHT-1PC', N'Gói 1 miếng (25ml)', 55000, 65000, N'8809572890618', N'ACTIVE'),
    (143, 110, N'SKU-KLR-SHT-BOX', N'Hộp 10 miếng Tiết Kiệm', 490000, 580000, N'8809572890625', N'ACTIVE'),
    (144, 111, N'SKU-LOR-SHT-1PC', N'Miếng lẻ 25ml', 35000, 45000, N'8992304038111', N'ACTIVE'),
    (145, 111, N'SKU-LOR-SHT-BOX', N'Hộp 5 miếng', 160000, 195000, N'8992304038128', N'ACTIVE'),
    (146, 112, N'SKU-BIO-MASK-75ML', N'Tuýp 75ml', 450000, 510000, N'3401573670222', N'ACTIVE'),
    (147, 113, N'SKU-VIC-SPA-75ML', N'Hũ 75ml', 890000, 990000, N'3337875588222', N'ACTIVE'),
    (148, 114, N'SKU-EUC-SHT-1PC', N'Gói 1 miếng', 95000, 115000, N'4005800261150', N'ACTIVE'),
    (149, 115, N'SKU-SUL-CLAR-150ML', N'Tuýp 150ml', 1200000, 1350000, N'8809685866666', N'ACTIVE'),
    (150, 116, N'SKU-CCN-ROSEM-100ML', N'Hũ 100ml', 145000, 165000, N'893851500055', N'ACTIVE');
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (151, 117, N'SKU-KHL-CALM-100ML', N'Hũ 100ml', 1350000, 1500000, N'3605970366611', N'ACTIVE'),
    (152, 118, N'SKU-INN-SHT-1PC', N'Miếng 20ml', 25000, 30000, N'8809612851010', N'ACTIVE'),
    (153, 118, N'SKU-INN-SHT-SET', N'Set 10 miếng', 220000, 270000, N'8809612851027', N'ACTIVE'),
    (154, 119, N'SKU-EL-FOIL-4PCS', N'Hộp 4 miếng', 2600000, 2900000, N'027131267555', N'ACTIVE'),
    (155, 120, N'SKU-LAN-LIP-3G', N'Hũ 3g Dùng thử', 65000, 80000, N'8809643068005', N'ACTIVE'),
    (156, 121, N'SKU-MAC-LIP-CHILI', N'Màu Chili - Đỏ Gạch Huyền Thoại', 580000, 650000, N'077360205211', N'ACTIVE'),
    (157, 121, N'SKU-MAC-LIP-MARRAK', N'Màu Marrakesh - Đỏ Nâu Trầm Ấm', 580000, 650000, N'077360205212', N'ACTIVE'),
    (158, 121, N'SKU-MAC-LIP-RUBY', N'Màu Ruby Woo - Đỏ Cổ Điển', 580000, 650000, N'077360205213', N'ACTIVE'),
    (159, 122, N'SKU-INN-LIP-01', N'Màu 01 - Đỏ Cherry Rực Rỡ', 280000, 320000, N'8809612850021', N'ACTIVE'),
    (160, 122, N'SKU-INN-LIP-02', N'Màu 02 - Cam Đất Thời Thượng', 280000, 320000, N'8809612850022', N'ACTIVE'),
    (161, 122, N'SKU-INN-LIP-03', N'Màu 03 - Hồng Đất MLBB', 280000, 320000, N'8809612850023', N'ACTIVE'),
    (162, 123, N'SKU-LAN-LIP-8G', N'Hũ 8g Mini', 120000, 140000, N'8809643068012', N'ACTIVE'),
    (163, 123, N'SKU-LAN-LIP-20G', N'Hũ 20g Fullsize', 410000, 460000, N'8809643068029', N'ACTIVE'),
    (164, 124, N'SKU-3CE-VLT-TAUPE', N'Màu Taupe - Đỏ Nâu Trầm', 350000, 390000, N'8809437391111', N'ACTIVE'),
    (165, 124, N'SKU-3CE-VLT-DAFF', N'Màu Daffodil - Đỏ Đất Tươi', 350000, 390000, N'8809437391128', N'ACTIVE'),
    (166, 125, N'SKU-ROM-JLT-06', N'Màu 06 Figfig - Hồng Sung Đất', 185000, 220000, N'8809625241111', N'ACTIVE'),
    (167, 125, N'SKU-ROM-JLT-23', N'Màu 23 Nucadamia - Nâu Đỏ Hạt Dẻ', 185000, 220000, N'8809625241128', N'ACTIVE'),
    (168, 126, N'SKU-DIO-GLOW-001', N'Màu 001 Pink - Hồng Tự Nhiên', 890000, 990000, N'3348901551111', N'ACTIVE'),
    (169, 126, N'SKU-DIO-GLOW-004', N'Màu 004 Coral - Cam San Hô', 890000, 990000, N'3348901551128', N'ACTIVE'),
    (170, 127, N'SKU-YSL-SLIM-21', N'Màu 21 Rouge Paradoxe - Đỏ Thuần Quyền Lực', 980000, 1100000, N'3614272131111', N'ACTIVE'),
    (171, 127, N'SKU-YSL-SLIM-28', N'Màu 28 True Chili - Đỏ Gạch Cuốn Hút', 980000, 1100000, N'3614272131128', N'ACTIVE'),
    (172, 128, N'SKU-CHA-VEL-58', N'Màu 58 Rouge Vie - Đỏ Rượu Nồng Nàn', 1150000, 1300000, N'3145891621111', N'ACTIVE'),
    (173, 129, N'SKU-MAY-INK-117', N'Màu 117 Groundbreaker - Đỏ Đất', 215000, 245000, N'041554551111', N'ACTIVE'),
    (174, 129, N'SKU-MAY-INK-135', N'Màu 135 Globetrotter - Cam Cháy', 215000, 245000, N'041554551128', N'ACTIVE'),
    (175, 130, N'SKU-ROM-ZVT-02', N'Màu 02 Joyful - Đỏ Cam Đất', 175000, 210000, N'8809625242222', N'ACTIVE'),
    (176, 130, N'SKU-ROM-ZVT-18', N'Màu 18 Petal Tassel - Hồng Đất Cổ Điển', 175000, 210000, N'8809625242239', N'ACTIVE'),
    (177, 131, N'SKU-3CE-MOOD-220', N'Màu 220 Hit Me Up - Cam Cháy', 340000, 380000, N'8809437392222', N'ACTIVE'),
    (178, 131, N'SKU-3CE-MOOD-222', N'Màu 222 Step And Go - Đỏ Đất', 340000, 380000, N'8809437392239', N'ACTIVE'),
    (179, 132, N'SKU-CCN-LIP-5G', N'Thỏi 5g', 35000, 45000, N'893851500061', N'ACTIVE'),
    (180, 133, N'SKU-DHC-LIP-15G', N'Thỏi 1.5g', 185000, 220000, N'4511413305511', N'ACTIVE'),
    (181, 134, N'SKU-MAC-PK-991', N'Màu 991 Devoted to Chili - Đỏ Gạch Sương Mờ', 690000, 780000, N'077360205311', N'ACTIVE'),
    (182, 134, N'SKU-MAC-PK-989', N'Màu 989 Mull It Over - Cam Hồng Đất', 690000, 780000, N'077360205328', N'ACTIVE'),
    (183, 135, N'SKU-LOR-SIG-129', N'Màu 129 I Lead - Đỏ Đất Thời Thượng', 245000, 280000, N'8992304039111', N'ACTIVE'),
    (184, 135, N'SKU-LOR-SIG-130', N'Màu 130 I Amaze - Cam Đất Cá Tính', 245000, 280000, N'8992304039128', N'ACTIVE'),
    (185, 136, N'SKU-MAY-FIT-112', N'Tone 112 Natural Ivory Sáng Tự Nhiên', 235000, 260000, N'041554538111', N'ACTIVE'),
    (186, 136, N'SKU-MAY-FIT-120', N'Tone 120 Classic Ivory Tự Nhiên', 235000, 260000, N'041554538128', N'ACTIVE'),
    (187, 136, N'SKU-MAY-FIT-128', N'Tone 128 Warm Nude Da Ngăm', 235000, 260000, N'041554538135', N'ACTIVE'),
    (188, 137, N'SKU-LAN-NEO-21N', N'Tone 21N Beige Da Sáng (Kèm Lõi)', 630000, 700000, N'8809643069019', N'ACTIVE'),
    (189, 137, N'SKU-LAN-NEO-23N', N'Tone 23N Sand Da Tự Nhiên (Kèm Lõi)', 630000, 700000, N'8809643069026', N'ACTIVE'),
    (190, 138, N'SKU-EL-DW-1W1', N'Tone 1W1 Bone Da Trắng Sáng', 1550000, 1750000, N'027131268111', N'ACTIVE'),
    (191, 138, N'SKU-EL-DW-1W2', N'Tone 1W2 Sand Da Tự Nhiên', 1550000, 1750000, N'027131268128', N'ACTIVE'),
    (192, 139, N'SKU-DIO-GLOW-1N', N'Tone 1N Neutral Da Sáng', 1750000, 1950000, N'3348901581111', N'ACTIVE'),
    (193, 139, N'SKU-DIO-GLOW-2N', N'Tone 2N Neutral Da Tự Nhiên', 1750000, 1950000, N'3348901581128', N'ACTIVE'),
    (194, 140, N'SKU-MAC-FIX-NC15', N'Tone NC15 Da Sáng', 990000, 1100000, N'077360206111', N'ACTIVE'),
    (195, 140, N'SKU-MAC-FIX-NC20', N'Tone NC20 Da Tự Nhiên', 990000, 1100000, N'077360206128', N'ACTIVE'),
    (196, 141, N'SKU-YSL-AH-LC1', N'Tone LC1 Da Trắng Hồng', 1850000, 2050000, N'3614272141111', N'ACTIVE'),
    (197, 141, N'SKU-YSL-AH-LN4', N'Tone LN4 Da Tự Nhiên', 1850000, 2050000, N'3614272141128', N'ACTIVE'),
    (198, 142, N'SKU-LOR-INF-120', N'Tone 120 Vanilla Da Sáng', 299000, 340000, N'8992304036111', N'ACTIVE'),
    (199, 142, N'SKU-LOR-INF-130', N'Tone 130 True Beige Da Tự Nhiên', 299000, 340000, N'8992304036128', N'ACTIVE'),
    (200, 143, N'SKU-INN-CUSH-21N', N'Tone 21N Vanilla Da Sáng', 420000, 480000, N'8809612851119', N'ACTIVE');
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (201, 143, N'SKU-INN-CUSH-23N', N'Tone 23N Ginger Da Tự Nhiên', 420000, 480000, N'8809612851126', N'ACTIVE'),
    (202, 144, N'SKU-ROM-CUSH-02', N'Tone 02 Pure 21 Da Sáng Hồng', 320000, 365000, N'8809625243333', N'ACTIVE'),
    (203, 144, N'SKU-ROM-CUSH-03', N'Tone 03 Natural 21 Da Tự Nhiên', 320000, 365000, N'8809625243340', N'ACTIVE'),
    (204, 145, N'SKU-3CE-CUSH-01', N'Tone 01 Fair Da Sáng', 480000, 540000, N'8809437393333', N'ACTIVE'),
    (205, 146, N'SKU-SUL-CUSH-21N', N'Tone 21N Da Sáng (Kèm Lõi)', 1450000, 1650000, N'8809685877777', N'ACTIVE'),
    (206, 147, N'SKU-VIC-DERM-15', N'Tone 15 Opal Da Sáng', 680000, 760000, N'3337871328888', N'ACTIVE'),
    (207, 147, N'SKU-VIC-DERM-25', N'Tone 25 Nude Da Tự Nhiên', 680000, 760000, N'3337871328895', N'ACTIVE'),
    (208, 148, N'SKU-MAY-PRIM-30ML', N'Tuýp 30ml', 195000, 220000, N'041554552222', N'ACTIVE'),
    (209, 149, N'SKU-LAN-BASE-40', N'Màu 40 Pure Violet Sáng Da Vàng', 620000, 690000, N'8809643064444', N'ACTIVE'),
    (210, 150, N'SKU-DIO-CUSH-1N', N'Tone 1N Neutral Da Sáng (Kèm Lõi)', 1950000, 2150000, N'3348901591111', N'ACTIVE'),
    (211, 151, N'SKU-MAC-BLUSH-WARM', N'Màu Warm Soul - Cam Đào Ánh Nhũ', 790000, 890000, N'077360215111', N'ACTIVE'),
    (212, 151, N'SKU-MAC-BLUSH-LIKE', N'Màu Like Me, Love Me - Cam San Hô', 790000, 890000, N'077360215112', N'ACTIVE'),
    (213, 152, N'SKU-INN-POW-5G', N'Hũ 5g Tiện Lợi', 135000, 160000, N'8809612851218', N'ACTIVE'),
    (214, 153, N'SKU-3CE-BLUSH-ROSE', N'Màu Rose Beige - Hồng Đào Đất', 360000, 410000, N'8809437394444', N'ACTIVE'),
    (215, 153, N'SKU-3CE-BLUSH-NUDE', N'Màu Nude Peach - Cam Đào Trong Trẻo', 360000, 410000, N'8809437394451', N'ACTIVE'),
    (216, 154, N'SKU-MAY-POW-110', N'Tone 110 Porcelain Da Trắng', 175000, 205000, N'041554553333', N'ACTIVE'),
    (217, 154, N'SKU-MAY-POW-120', N'Tone 120 Classic Ivory Da Tự Nhiên', 175000, 205000, N'041554553340', N'ACTIVE'),
    (218, 155, N'SKU-ROM-BLUSH-02', N'Màu 02 Peach Chip - Cam Đào Tươi Tắn', 185000, 220000, N'8809625244444', N'ACTIVE'),
    (219, 155, N'SKU-ROM-BLUSH-03', N'Màu 03 Blueberry Chip - Hồng Dâu Sữa', 185000, 220000, N'8809625244451', N'ACTIVE'),
    (220, 156, N'SKU-DIO-BLUSH-001', N'Màu 001 Pink - Hồng Baby Phát Sáng', 1150000, 1300000, N'3348901571111', N'ACTIVE'),
    (221, 156, N'SKU-DIO-BLUSH-004', N'Màu 004 Coral - Cam San Hô Tươi Sáng', 1150000, 1300000, N'3348901571128', N'ACTIVE'),
    (222, 157, N'SKU-CHA-BLUSH-72', N'Màu 72 Rose Initial - Hồng Quyến Rũ', 1450000, 1650000, N'3145891681111', N'ACTIVE'),
    (223, 158, N'SKU-MAC-POW-TRANS', N'Màu Translucent Trong Suốt', 950000, 1050000, N'077360215222', N'ACTIVE'),
    (224, 159, N'SKU-LOR-POW-TRANS', N'Tone Translucent Trong Suốt Tự Nhiên', 279000, 320000, N'8992304037222', N'ACTIVE'),
    (225, 160, N'SKU-CCN-POW-10G', N'Hũ 10g Kèm Bông Phấn', 165000, 195000, N'893851500071', N'ACTIVE'),
    (226, 161, N'SKU-3CE-STR-GLOW', N'Màu Glow Pearl - Ánh Ngọc Trai Tự Nhiên', 390000, 440000, N'8809437395555', N'ACTIVE'),
    (227, 162, N'SKU-ROM-VEIL-01', N'Màu 01 Sun Kissed Veil - Vàng Nắng', 230000, 270000, N'8809625245555', N'ACTIVE'),
    (228, 163, N'SKU-MAY-HEAT-20', N'Màu 20 Rose Flush - Hồng Đào', 145000, 175000, N'041554554444', N'ACTIVE'),
    (229, 164, N'SKU-LAN-POW-7G', N'Hũ 7g Kèm Lưới Thông Minh', 520000, 590000, N'8809643065555', N'ACTIVE'),
    (230, 165, N'SKU-MAC-BEACON', N'Màu Double-Gleam - Vàng Bạc Ánh Kim', 990000, 1150000, N'077360215333', N'ACTIVE'),
    (231, 166, N'SKU-MAY-MASC-BLK', N'Màu Đen Tự Nhiên - Chuốt mi 9.2ml', 155000, 180000, N'041554539019', N'ACTIVE'),
    (232, 167, N'SKU-MAY-SKY-BLK', N'Màu Đen Sâu - Cây 7.2ml', 235000, 270000, N'041554555555', N'ACTIVE'),
    (233, 168, N'SKU-3CE-EYE-OVER', N'Bảng Overtake - Tông Hồng Nâu Cam Đất', 750000, 850000, N'8809437396666', N'ACTIVE'),
    (234, 168, N'SKU-3CE-EYE-PLOT', N'Bảng Plot Twist - Tông Nâu Tây Cổ Điển', 750000, 850000, N'8809437396673', N'ACTIVE'),
    (235, 169, N'SKU-ROM-EYE-01', N'Bảng 01 Dry Mango Tulip - Hoa Tulip Cam Đào', 280000, 320000, N'8809625246666', N'ACTIVE'),
    (236, 170, N'SKU-MAY-SHARP-BLK', N'Màu Ultra Black Đen Đậm', 185000, 215000, N'041554556666', N'ACTIVE'),
    (237, 171, N'SKU-LOR-LASH-BLK', N'Màu Đen Sâu - Cây 8.5ml', 259000, 295000, N'8992304038333', N'ACTIVE'),
    (238, 172, N'SKU-DIO-MASC-090', N'Màu 090 Black Đen Huyền Bí', 980000, 1100000, N'3348901561111', N'ACTIVE'),
    (239, 173, N'SKU-INN-MASC-7G', N'Cây 7g', 280000, 320000, N'8809612851317', N'ACTIVE'),
    (240, 174, N'SKU-MAC-DAZZ-GOLD', N'Màu Flash and Dash - Vàng Champagne Lấp Lánh', 650000, 750000, N'077360215444', N'ACTIVE'),
    (241, 175, N'SKU-MAY-BROW-BRN', N'Màu Nâu Tự Nhiên Natural Brown', 125000, 150000, N'041554557777', N'ACTIVE'),
    (242, 175, N'SKU-MAY-BROW-GRY', N'Màu Nâu Xám Grey Brown', 125000, 150000, N'041554557784', N'ACTIVE'),
    (243, 176, N'SKU-3CE-PEN-BLK', N'Màu Black Đen Tuyền', 295000, 340000, N'8809437397777', N'ACTIVE'),
    (244, 177, N'SKU-ROM-MASC-L01', N'Màu L01 Long Black - Làm Dài Mi Đen', 245000, 280000, N'8809625247777', N'ACTIVE'),
    (245, 178, N'SKU-INN-BROW-04', N'Màu 04 Ash Brown Nâu Tro', 85000, 105000, N'8809612851416', N'ACTIVE'),
    (246, 179, N'SKU-LOR-EYE-17', N'Màu 17 Don''t Stop Me - Nhũ Đồng Ánh Kim', 165000, 195000, N'8992304038444', N'ACTIVE'),
    (247, 180, N'SKU-CHA-EYE-204', N'Bảng 204 Tissé Vendôme - Tông Đào Nâu Sang Trọng', 1850000, 2100000, N'3145891641111', N'ACTIVE'),
    (248, 181, N'SKU-CCN-SCRUB-200ML', N'Hũ 200ml Tiêu Chuẩn', 125000, 145000, N'893851500007', N'ACTIVE'),
    (249, 181, N'SKU-CCN-SCRUB-600ML', N'Hũ 600ml Siêu Tiết Kiệm', 265000, 310000, N'893851500008', N'ACTIVE'),
    (250, 182, N'SKU-BBW-SCRUB-480G', N'Hũ 480g', 420000, 480000, N'0667554111111', N'ACTIVE');
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (251, 183, N'SKU-CRV-SA-177G', N'Tuýp 177g', 365000, 420000, N'3337875684111', N'ACTIVE'),
    (252, 183, N'SKU-CRV-SA-340G', N'Hũ 340g', 495000, 560000, N'3337875684128', N'ACTIVE'),
    (253, 184, N'SKU-PC-BODY-210ML', N'Tuýp 210ml', 850000, 950000, N'065543905711', N'ACTIVE'),
    (254, 185, N'SKU-CCN-ROSEB-200ML', N'Hũ 200ml', 145000, 165000, N'893851500081', N'ACTIVE'),
    (255, 186, N'SKU-TSU-SCRUB-250G', N'Hũ 250g', 280000, 320000, N'4901872461111', N'ACTIVE'),
    (256, 187, N'SKU-BBW-INTO-226G', N'Tuýp 226g', 380000, 440000, N'0667554112222', N'ACTIVE'),
    (257, 188, N'SKU-VS-SCRUB-368G', N'Hũ 368g', 410000, 470000, N'0667553113333', N'ACTIVE'),
    (258, 189, N'SKU-INN-BSCRUB-150ML', N'Tuýp 150ml', 230000, 270000, N'8809612851515', N'ACTIVE'),
    (259, 190, N'SKU-LOR-BLOT-200ML', N'Chai 200ml', 199000, 235000, N'8992304039222', N'ACTIVE'),
    (260, 191, N'SKU-BIO-BSCRUB-200ML', N'Tuýp 200ml', 360000, 410000, N'3401573670333', N'ACTIVE'),
    (261, 192, N'SKU-KHL-BSCRUB-200ML', N'Tuýp 200ml', 950000, 1080000, N'3605970367777', N'ACTIVE'),
    (262, 193, N'SKU-DHC-BSCRUB-200G', N'Hũ 200g', 450000, 520000, N'4511413306611', N'ACTIVE'),
    (263, 194, N'SKU-EUC-BODY-250ML', N'Chai 250ml', 420000, 480000, N'4005800181228', N'ACTIVE'),
    (264, 195, N'SKU-MOR-BSCRUB-200ML', N'Hũ 200ml', 1150000, 1300000, N'7290011521111', N'ACTIVE'),
    (265, 196, N'SKU-CCN-TOC-140ML', N'Chai xịt 140ml', 145000, 165000, N'893851500004', N'ACTIVE'),
    (266, 196, N'SKU-CCN-TOC-310ML', N'Chai xịt 310ml Tiết Kiệm', 265000, 295000, N'893851500006', N'ACTIVE'),
    (267, 197, N'SKU-CCN-SHAMP-310ML', N'Chai 310ml', 225000, 255000, N'893851500009', N'ACTIVE'),
    (268, 197, N'SKU-CCN-SHAMP-500ML', N'Chai 500ml Có Vòi Pump', 345000, 395000, N'893851500010', N'ACTIVE'),
    (269, 198, N'SKU-TSU-SHAMP-490ML', N'Chai vòi 490ml', 185000, 220000, N'4901872462222', N'ACTIVE'),
    (270, 199, N'SKU-MOR-OIL-25ML', N'Chai 25ml Bỏ Túi', 350000, 400000, N'7290011522222', N'ACTIVE'),
    (271, 199, N'SKU-MOR-OIL-100ML', N'Chai 100ml Fullsize', 950000, 1050000, N'7290011522239', N'ACTIVE'),
    (272, 200, N'SKU-LOR-ELS-650ML', N'Chai 650ml Cỡ Lớn', 165000, 195000, N'8992304037888', N'ACTIVE'),
    (273, 201, N'SKU-LOR-EXOIL-100ML', N'Chai 100ml', 249000, 289000, N'8992304037995', N'ACTIVE'),
    (274, 202, N'SKU-TSU-MASK-180G', N'Hũ 180g', 215000, 250000, N'4901872463333', N'ACTIVE'),
    (275, 203, N'SKU-INN-HAIR-100ML', N'Chai 100ml', 280000, 320000, N'8809612851614', N'ACTIVE'),
    (276, 204, N'SKU-CCN-COND-310ML', N'Chai 310ml', 225000, 255000, N'893851500091', N'ACTIVE'),
    (277, 205, N'SKU-DHC-HAIR-150ML', N'Chai 150ml', 550000, 620000, N'4511413307711', N'ACTIVE'),
    (278, 206, N'SKU-KHL-AMINO-250ML', N'Chai 250ml', 750000, 850000, N'3605970368888', N'ACTIVE'),
    (279, 207, N'SKU-MOR-SHAMP-250ML', N'Chai 250ml', 580000, 650000, N'7290011523333', N'ACTIVE'),
    (280, 208, N'SKU-TSU-VOL-490ML', N'Chai 490ml', 185000, 220000, N'4901872464444', N'ACTIVE'),
    (281, 209, N'SKU-CCN-SACHI-70ML', N'Chai 70ml', 145000, 165000, N'893851500095', N'ACTIVE'),
    (282, 210, N'SKU-LOR-MASK-200ML', N'Hũ 200ml', 159000, 185000, N'8992304037777', N'ACTIVE'),
    (283, 211, N'SKU-LUN-ROSE-50ML', N'Chai 50ml Eau de Parfum', 1850000, 2100000, N'893888800029', N'ACTIVE'),
    (284, 211, N'SKU-LUN-ROSE-100ML', N'Chai 100ml Eau de Parfum', 2750000, 3100000, N'893888800030', N'ACTIVE'),
    (285, 212, N'SKU-CHA-COCO-50ML', N'Chai 50ml EDP', 3450000, 3750000, N'3145891161111', N'ACTIVE'),
    (286, 212, N'SKU-CHA-COCO-100ML', N'Chai 100ml EDP', 4650000, 4950000, N'3145891161128', N'ACTIVE'),
    (287, 213, N'SKU-DIO-SAUV-60ML', N'Chai 60ml EDP', 2750000, 2950000, N'3348901361111', N'ACTIVE'),
    (288, 213, N'SKU-DIO-SAUV-100ML', N'Chai 100ml EDP', 3750000, 4100000, N'3348901361128', N'ACTIVE'),
    (289, 214, N'SKU-YSL-OPI-50ML', N'Chai 50ml EDP', 2850000, 3100000, N'3614270111111', N'ACTIVE'),
    (290, 214, N'SKU-YSL-OPI-90ML', N'Chai 90ml EDP', 3650000, 3950000, N'3614270111128', N'ACTIVE'),
    (291, 215, N'SKU-DIO-MISS-50ML', N'Chai 50ml EDP', 2950000, 3200000, N'3348901511111', N'ACTIVE'),
    (292, 216, N'SKU-CHA-BLEU-50ML', N'Chai 50ml EDP', 2950000, 3200000, N'3145891071111', N'ACTIVE'),
    (293, 216, N'SKU-CHA-BLEU-100ML', N'Chai 100ml EDP', 3950000, 4250000, N'3145891071128', N'ACTIVE'),
    (294, 217, N'SKU-YSL-LIB-50ML', N'Chai 50ml EDP', 2950000, 3250000, N'3614272641111', N'ACTIVE'),
    (295, 218, N'SKU-EL-BRONZE-50ML', N'Chai 50ml EDP', 1950000, 2200000, N'027131269111', N'ACTIVE'),
    (296, 219, N'SKU-CHA-TEND-50ML', N'Chai 50ml EDP', 3350000, 3650000, N'3145891261111', N'ACTIVE'),
    (297, 220, N'SKU-YSL-Y-60ML', N'Chai 60ml EDP', 2550000, 2800000, N'3614272051111', N'ACTIVE'),
    (298, 220, N'SKU-YSL-Y-100ML', N'Chai 100ml EDP', 3450000, 3750000, N'3614272051128', N'ACTIVE'),
    (299, 221, N'SKU-DIO-JAD-50ML', N'Chai 50ml EDP', 3100000, 3400000, N'3348901411111', N'ACTIVE'),
    (300, 222, N'SKU-EL-MAG-50ML', N'Chai 50ml EDP', 2150000, 2400000, N'027131269222', N'ACTIVE');
GO

INSERT INTO dbo.skus (id, product_id, sku_code, variant_name, price, list_price, barcode, status) VALUES
    (301, 223, N'SKU-CHA-NO5-50ML', N'Chai 50ml EDP', 3650000, 3950000, N'3145891251111', N'ACTIVE'),
    (302, 224, N'SKU-YSL-MONP-50ML', N'Chai 50ml EDP', 2850000, 3150000, N'3614270561111', N'ACTIVE'),
    (303, 225, N'SKU-DIO-INT-100ML', N'Chai 100ml EDP', 3850000, 4200000, N'3348901371111', N'ACTIVE'),
    (304, 226, N'SKU-LUN-MIST-150ML', N'Chai xịt 150ml', 245000, 280000, N'893888800031', N'ACTIVE'),
    (305, 227, N'SKU-BBW-INTO-236ML', N'Chai xịt 236ml', 320000, 360000, N'0667554113333', N'ACTIVE'),
    (306, 228, N'SKU-BBW-CHERRY-236ML', N'Chai xịt 236ml', 320000, 360000, N'0667554114444', N'ACTIVE'),
    (307, 229, N'SKU-BBW-SUGAR-236ML', N'Chai xịt 236ml', 320000, 360000, N'0667554115555', N'ACTIVE'),
    (308, 230, N'SKU-VS-PURE-250ML', N'Chai xịt 250ml', 350000, 395000, N'0667553114444', N'ACTIVE'),
    (309, 231, N'SKU-VS-BARE-250ML', N'Chai xịt 250ml', 350000, 395000, N'0667553115555', N'ACTIVE'),
    (310, 232, N'SKU-VS-VELVET-250ML', N'Chai xịt 250ml', 350000, 395000, N'0667553116666', N'ACTIVE'),
    (311, 233, N'SKU-VS-AQUA-250ML', N'Chai xịt 250ml', 350000, 395000, N'0667553117777', N'ACTIVE'),
    (312, 234, N'SKU-CCN-MIST-140ML', N'Chai 140ml', 125000, 145000, N'893851500098', N'ACTIVE'),
    (313, 235, N'SKU-BBW-BEACH-236ML', N'Chai xịt 236ml', 320000, 360000, N'0667554116666', N'ACTIVE'),
    (314, 236, N'SKU-BBW-WISH-236ML', N'Chai xịt 236ml', 320000, 360000, N'0667554117777', N'ACTIVE'),
    (315, 237, N'SKU-VS-SPELL-250ML', N'Chai xịt 250ml', 350000, 395000, N'0667553118888', N'ACTIVE'),
    (316, 238, N'SKU-INN-MIST-100ML', N'Chai 100ml', 240000, 280000, N'8809612851713', N'ACTIVE'),
    (317, 239, N'SKU-BBW-TEA-236ML', N'Chai xịt 236ml', 340000, 390000, N'0667554118888', N'ACTIVE'),
    (318, 240, N'SKU-VS-COCO-250ML', N'Chai xịt 250ml', 350000, 395000, N'0667553119999', N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.skus OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: PRODUCT_IMAGES (240 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.product_images ON;
GO

INSERT INTO dbo.product_images (id, product_id, sku_id, url, public_id, is_primary, sort_order) VALUES
    (1, 1, 1, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567425/lunea/products/prod_p1_1.jpg', N'lunea_prod_1_1', 1, 0),
    (2, 2, 3, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567427/lunea/products/prod_p2_3.jpg', N'lunea_prod_2_1', 1, 0),
    (3, 3, 5, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567428/lunea/products/prod_p3_4.jpg', N'lunea_prod_3_1', 1, 0),
    (4, 4, 7, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567436/lunea/products/prod_p9_10.jpg', N'lunea_prod_4_1', 1, 0),
    (5, 5, 9, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567442/lunea/products/prod_p16_17.jpg', N'lunea_prod_5_1', 1, 0),
    (6, 6, 11, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567452/lunea/products/prod_p26_27.jpg', N'lunea_prod_6_1', 1, 0),
    (7, 7, 13, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_7_1', 1, 0),
    (8, 8, 14, N'https://images.unsplash.com/photo-1608248597359-25757917a26f?w=600&auto=format&fit=crop&q=80', N'lunea_prod_8_1', 1, 0),
    (9, 9, 16, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_9_1', 1, 0),
    (10, 10, 18, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_10_1', 1, 0),
    (11, 11, 19, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_11_1', 1, 0),
    (12, 12, 20, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_12_1', 1, 0),
    (13, 13, 22, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_13_1', 1, 0),
    (14, 14, 23, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_14_1', 1, 0),
    (15, 15, 24, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_15_1', 1, 0),
    (16, 16, 26, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567431/lunea/products/prod_p5_6.jpg', N'lunea_prod_16_1', 1, 0),
    (17, 17, 28, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567440/lunea/products/prod_p13_14.jpg', N'lunea_prod_17_1', 1, 0),
    (18, 18, 30, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567452/lunea/products/prod_p27_28.jpg', N'lunea_prod_18_1', 1, 0),
    (19, 19, 31, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_19_1', 1, 0),
    (20, 20, 32, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_20_1', 1, 0),
    (21, 21, 34, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_21_1', 1, 0),
    (22, 22, 35, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567431/lunea/products/prod_p5_6.jpg', N'lunea_prod_22_1', 1, 0),
    (23, 23, 36, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_23_1', 1, 0),
    (24, 24, 37, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_24_1', 1, 0),
    (25, 25, 38, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_25_1', 1, 0),
    (26, 26, 39, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_26_1', 1, 0),
    (27, 27, 40, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_27_1', 1, 0),
    (28, 28, 41, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_28_1', 1, 0),
    (29, 29, 42, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_29_1', 1, 0),
    (30, 30, 43, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_30_1', 1, 0),
    (31, 31, 44, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567433/lunea/products/prod_p7_8.jpg', N'lunea_prod_31_1', 1, 0),
    (32, 32, 46, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567440/lunea/products/prod_p14_15.jpg', N'lunea_prod_32_1', 1, 0),
    (33, 33, 48, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_33_1', 1, 0),
    (34, 34, 50, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_34_1', 1, 0),
    (35, 35, 51, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_35_1', 1, 0),
    (36, 36, 52, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_36_1', 1, 0),
    (37, 37, 54, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_37_1', 1, 0),
    (38, 38, 55, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_38_1', 1, 0),
    (39, 39, 56, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_39_1', 1, 0),
    (40, 40, 57, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_40_1', 1, 0),
    (41, 41, 58, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_41_1', 1, 0),
    (42, 42, 59, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_42_1', 1, 0),
    (43, 43, 60, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567433/lunea/products/prod_p7_8.jpg', N'lunea_prod_43_1', 1, 0),
    (44, 44, 61, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_44_1', 1, 0),
    (45, 45, 62, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_45_1', 1, 0),
    (46, 46, 63, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567441/lunea/products/prod_p15_16.jpg', N'lunea_prod_46_1', 1, 0),
    (47, 47, 64, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567441/lunea/products/prod_p15_16.jpg', N'lunea_prod_47_1', 1, 0),
    (48, 48, 66, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_48_1', 1, 0),
    (49, 49, 67, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_49_1', 1, 0),
    (50, 50, 68, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_50_1', 1, 0);
GO

INSERT INTO dbo.product_images (id, product_id, sku_id, url, public_id, is_primary, sort_order) VALUES
    (51, 51, 69, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_51_1', 1, 0),
    (52, 52, 70, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_52_1', 1, 0),
    (53, 53, 71, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_53_1', 1, 0),
    (54, 54, 72, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567441/lunea/products/prod_p15_16.jpg', N'lunea_prod_54_1', 1, 0),
    (55, 55, 73, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_55_1', 1, 0),
    (56, 56, 75, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_56_1', 1, 0),
    (57, 57, 76, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_57_1', 1, 0),
    (58, 58, 77, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_58_1', 1, 0),
    (59, 59, 78, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_59_1', 1, 0),
    (60, 60, 79, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_60_1', 1, 0),
    (61, 61, 80, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567439/lunea/products/prod_p12_13.jpg', N'lunea_prod_61_1', 1, 0),
    (62, 62, 81, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567453/lunea/products/prod_p28_29.jpg', N'lunea_prod_62_1', 1, 0),
    (63, 63, 84, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_63_1', 1, 0),
    (64, 64, 86, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567439/lunea/products/prod_p12_13.jpg', N'lunea_prod_64_1', 1, 0),
    (65, 65, 88, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_65_1', 1, 0),
    (66, 66, 89, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_66_1', 1, 0),
    (67, 67, 90, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_67_1', 1, 0),
    (68, 68, 91, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_68_1', 1, 0),
    (69, 69, 92, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_69_1', 1, 0),
    (70, 70, 93, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_70_1', 1, 0),
    (71, 71, 94, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_71_1', 1, 0),
    (72, 72, 95, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567439/lunea/products/prod_p12_13.jpg', N'lunea_prod_72_1', 1, 0),
    (73, 73, 96, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_73_1', 1, 0),
    (74, 74, 97, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_74_1', 1, 0),
    (75, 75, 98, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_75_1', 1, 0),
    (76, 76, 99, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567438/lunea/products/prod_p11_12.jpg', N'lunea_prod_76_1', 1, 0),
    (77, 77, 101, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_77_1', 1, 0),
    (78, 78, 102, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567438/lunea/products/prod_p11_12.jpg', N'lunea_prod_78_1', 1, 0),
    (79, 79, 103, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_79_1', 1, 0),
    (80, 80, 105, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_80_1', 1, 0),
    (81, 81, 107, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_81_1', 1, 0),
    (82, 82, 109, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_82_1', 1, 0),
    (83, 83, 111, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_83_1', 1, 0),
    (84, 84, 112, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_84_1', 1, 0),
    (85, 85, 113, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_85_1', 1, 0),
    (86, 86, 115, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567438/lunea/products/prod_p11_12.jpg', N'lunea_prod_86_1', 1, 0),
    (87, 87, 116, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_87_1', 1, 0),
    (88, 88, 117, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_88_1', 1, 0),
    (89, 89, 118, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_89_1', 1, 0),
    (90, 90, 119, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_90_1', 1, 0),
    (91, 91, 120, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567429/lunea/products/prod_p4_5.jpg', N'lunea_prod_91_1', 1, 0),
    (92, 92, 122, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_92_1', 1, 0),
    (93, 93, 123, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567450/lunea/products/prod_p24_25.jpg', N'lunea_prod_93_1', 1, 0),
    (94, 94, 124, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_94_1', 1, 0),
    (95, 95, 125, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_95_1', 1, 0),
    (96, 96, 126, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_96_1', 1, 0),
    (97, 97, 127, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_97_1', 1, 0),
    (98, 98, 128, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_98_1', 1, 0),
    (99, 99, 129, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_99_1', 1, 0),
    (100, 100, 130, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_100_1', 1, 0);
GO

INSERT INTO dbo.product_images (id, product_id, sku_id, url, public_id, is_primary, sort_order) VALUES
    (101, 101, 131, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_101_1', 1, 0),
    (102, 102, 132, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567429/lunea/products/prod_p4_5.jpg', N'lunea_prod_102_1', 1, 0),
    (103, 103, 133, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_103_1', 1, 0),
    (104, 104, 134, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_104_1', 1, 0),
    (105, 105, 135, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_105_1', 1, 0),
    (106, 106, 136, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567443/lunea/products/prod_p17_18.jpg', N'lunea_prod_106_1', 1, 0),
    (107, 107, 138, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_107_1', 1, 0),
    (108, 108, 140, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_108_1', 1, 0),
    (109, 109, 141, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567443/lunea/products/prod_p17_18.jpg', N'lunea_prod_109_1', 1, 0),
    (110, 110, 142, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_110_1', 1, 0),
    (111, 111, 144, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_111_1', 1, 0),
    (112, 112, 146, N'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80', N'lunea_prod_112_1', 1, 0),
    (113, 113, 147, N'https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80', N'lunea_prod_113_1', 1, 0),
    (114, 114, 148, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_114_1', 1, 0),
    (115, 115, 149, N'https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80', N'lunea_prod_115_1', 1, 0),
    (116, 116, 150, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567443/lunea/products/prod_p17_18.jpg', N'lunea_prod_116_1', 1, 0),
    (117, 117, 151, N'https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80', N'lunea_prod_117_1', 1, 0),
    (118, 118, 152, N'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80', N'lunea_prod_118_1', 1, 0),
    (119, 119, 154, N'https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80', N'lunea_prod_119_1', 1, 0),
    (120, 120, 155, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567444/lunea/products/prod_p18_19.jpg', N'lunea_prod_120_1', 1, 0),
    (121, 121, 156, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567446/lunea/products/prod_p20_21.jpg', N'lunea_prod_121_1', 1, 0),
    (122, 122, 159, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567437/lunea/products/prod_p10_11.jpg', N'lunea_prod_122_1', 1, 0),
    (123, 123, 162, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567444/lunea/products/prod_p18_19.jpg', N'lunea_prod_123_1', 1, 0),
    (124, 124, 164, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_124_1', 1, 0),
    (125, 125, 166, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_125_1', 1, 0),
    (126, 126, 168, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_126_1', 1, 0),
    (127, 127, 170, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_127_1', 1, 0),
    (128, 128, 172, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_128_1', 1, 0),
    (129, 129, 173, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_129_1', 1, 0),
    (130, 130, 175, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_130_1', 1, 0),
    (131, 131, 177, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_131_1', 1, 0),
    (132, 132, 179, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567437/lunea/products/prod_p10_11.jpg', N'lunea_prod_132_1', 1, 0),
    (133, 133, 180, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_133_1', 1, 0),
    (134, 134, 181, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567446/lunea/products/prod_p20_21.jpg', N'lunea_prod_134_1', 1, 0),
    (135, 135, 183, N'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80', N'lunea_prod_135_1', 1, 0),
    (136, 136, 185, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567447/lunea/products/prod_p21_22.jpg', N'lunea_prod_136_1', 1, 0),
    (137, 137, 188, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567445/lunea/products/prod_p19_20.jpg', N'lunea_prod_137_1', 1, 0),
    (138, 138, 190, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_138_1', 1, 0),
    (139, 139, 192, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_139_1', 1, 0),
    (140, 140, 194, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_140_1', 1, 0),
    (141, 141, 196, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_141_1', 1, 0),
    (142, 142, 198, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_142_1', 1, 0),
    (143, 143, 200, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_143_1', 1, 0),
    (144, 144, 202, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_144_1', 1, 0),
    (145, 145, 204, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_145_1', 1, 0),
    (146, 146, 205, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_146_1', 1, 0),
    (147, 147, 206, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_147_1', 1, 0),
    (148, 148, 208, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_148_1', 1, 0),
    (149, 149, 209, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_149_1', 1, 0),
    (150, 150, 210, N'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80', N'lunea_prod_150_1', 1, 0);
GO

INSERT INTO dbo.product_images (id, product_id, sku_id, url, public_id, is_primary, sort_order) VALUES
    (151, 151, 211, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567449/lunea/products/prod_p23_24.jpg', N'lunea_prod_151_1', 1, 0),
    (152, 152, 213, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_152_1', 1, 0),
    (153, 153, 214, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_153_1', 1, 0),
    (154, 154, 216, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_154_1', 1, 0),
    (155, 155, 218, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_155_1', 1, 0),
    (156, 156, 220, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_156_1', 1, 0),
    (157, 157, 222, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_157_1', 1, 0),
    (158, 158, 223, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_158_1', 1, 0),
    (159, 159, 224, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_159_1', 1, 0),
    (160, 160, 225, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_160_1', 1, 0),
    (161, 161, 226, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_161_1', 1, 0),
    (162, 162, 227, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_162_1', 1, 0),
    (163, 163, 228, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_163_1', 1, 0),
    (164, 164, 229, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_164_1', 1, 0),
    (165, 165, 230, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_165_1', 1, 0),
    (166, 166, 231, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567448/lunea/products/prod_p22_23.jpg', N'lunea_prod_166_1', 1, 0),
    (167, 167, 232, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567448/lunea/products/prod_p22_23.jpg', N'lunea_prod_167_1', 1, 0),
    (168, 168, 233, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_168_1', 1, 0),
    (169, 169, 235, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_169_1', 1, 0),
    (170, 170, 236, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_170_1', 1, 0),
    (171, 171, 237, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_171_1', 1, 0),
    (172, 172, 238, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_172_1', 1, 0),
    (173, 173, 239, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_173_1', 1, 0),
    (174, 174, 240, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_174_1', 1, 0),
    (175, 175, 241, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_175_1', 1, 0),
    (176, 176, 243, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_176_1', 1, 0),
    (177, 177, 244, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_177_1', 1, 0),
    (178, 178, 245, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_178_1', 1, 0),
    (179, 179, 246, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_179_1', 1, 0),
    (180, 180, 247, N'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80', N'lunea_prod_180_1', 1, 0),
    (181, 181, 248, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567450/lunea/products/prod_p24_25.jpg', N'lunea_prod_181_1', 1, 0),
    (182, 182, 250, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_182_1', 1, 0),
    (183, 183, 251, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_183_1', 1, 0),
    (184, 184, 253, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_184_1', 1, 0),
    (185, 185, 254, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567450/lunea/products/prod_p24_25.jpg', N'lunea_prod_185_1', 1, 0),
    (186, 186, 255, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_186_1', 1, 0),
    (187, 187, 256, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_187_1', 1, 0),
    (188, 188, 257, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_188_1', 1, 0),
    (189, 189, 258, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_189_1', 1, 0),
    (190, 190, 259, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_190_1', 1, 0),
    (191, 191, 260, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_191_1', 1, 0),
    (192, 192, 261, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_192_1', 1, 0),
    (193, 193, 262, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_193_1', 1, 0),
    (194, 194, 263, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_194_1', 1, 0),
    (195, 195, 264, N'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=600&auto=format&fit=crop&q=80', N'lunea_prod_195_1', 1, 0),
    (196, 196, 265, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567434/lunea/products/prod_p8_9.jpg', N'lunea_prod_196_1', 1, 0),
    (197, 197, 267, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567451/lunea/products/prod_p25_26.jpg', N'lunea_prod_197_1', 1, 0),
    (198, 198, 269, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_198_1', 1, 0),
    (199, 199, 270, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_199_1', 1, 0),
    (200, 200, 272, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_200_1', 1, 0);
GO

INSERT INTO dbo.product_images (id, product_id, sku_id, url, public_id, is_primary, sort_order) VALUES
    (201, 201, 273, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_201_1', 1, 0),
    (202, 202, 274, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_202_1', 1, 0),
    (203, 203, 275, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_203_1', 1, 0),
    (204, 204, 276, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567434/lunea/products/prod_p8_9.jpg', N'lunea_prod_204_1', 1, 0),
    (205, 205, 277, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_205_1', 1, 0),
    (206, 206, 278, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_206_1', 1, 0),
    (207, 207, 279, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_207_1', 1, 0),
    (208, 208, 280, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_208_1', 1, 0),
    (209, 209, 281, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567434/lunea/products/prod_p8_9.jpg', N'lunea_prod_209_1', 1, 0),
    (210, 210, 282, N'https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80', N'lunea_prod_210_1', 1, 0),
    (211, 211, 283, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567454/lunea/products/prod_p29_30.jpg', N'lunea_prod_211_1', 1, 0),
    (212, 212, 285, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_212_1', 1, 0),
    (213, 213, 287, N'https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=600&auto=format&fit=crop&q=80', N'lunea_prod_213_1', 1, 0),
    (214, 214, 289, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_214_1', 1, 0),
    (215, 215, 291, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_215_1', 1, 0),
    (216, 216, 292, N'https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=600&auto=format&fit=crop&q=80', N'lunea_prod_216_1', 1, 0),
    (217, 217, 294, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_217_1', 1, 0),
    (218, 218, 295, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_218_1', 1, 0),
    (219, 219, 296, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_219_1', 1, 0),
    (220, 220, 297, N'https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=600&auto=format&fit=crop&q=80', N'lunea_prod_220_1', 1, 0),
    (221, 221, 299, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_221_1', 1, 0),
    (222, 222, 300, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_222_1', 1, 0),
    (223, 223, 301, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_223_1', 1, 0),
    (224, 224, 302, N'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600&auto=format&fit=crop&q=80', N'lunea_prod_224_1', 1, 0),
    (225, 225, 303, N'https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=600&auto=format&fit=crop&q=80', N'lunea_prod_225_1', 1, 0),
    (226, 226, 304, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567455/lunea/products/prod_p30_31.jpg', N'lunea_prod_226_1', 1, 0),
    (227, 227, 305, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_227_1', 1, 0),
    (228, 228, 306, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_228_1', 1, 0),
    (229, 229, 307, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_229_1', 1, 0),
    (230, 230, 308, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_230_1', 1, 0),
    (231, 231, 309, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_231_1', 1, 0),
    (232, 232, 310, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_232_1', 1, 0),
    (233, 233, 311, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_233_1', 1, 0),
    (234, 234, 312, N'https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567455/lunea/products/prod_p30_31.jpg', N'lunea_prod_234_1', 1, 0),
    (235, 235, 313, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_235_1', 1, 0),
    (236, 236, 314, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_236_1', 1, 0),
    (237, 237, 315, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_237_1', 1, 0),
    (238, 238, 316, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_238_1', 1, 0),
    (239, 239, 317, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_239_1', 1, 0),
    (240, 240, 318, N'https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80', N'lunea_prod_240_1', 1, 0);
GO

SET IDENTITY_INSERT dbo.product_images OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: INVENTORIES (954 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.inventories ON;
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (1, 1, 1, 35, 0, 5, N'2026-03-15 10:00:00'),
    (2, 2, 1, 25, 0, 5, N'2026-03-15 10:00:00'),
    (3, 3, 1, 20, 0, 5, N'2026-03-15 10:00:00'),
    (4, 1, 2, 35, 0, 5, N'2026-03-15 10:00:00'),
    (5, 2, 2, 25, 0, 5, N'2026-03-15 10:00:00'),
    (6, 3, 2, 20, 0, 5, N'2026-03-15 10:00:00'),
    (7, 1, 3, 35, 0, 5, N'2026-03-15 10:00:00'),
    (8, 2, 3, 25, 0, 5, N'2026-03-15 10:00:00'),
    (9, 3, 3, 20, 0, 5, N'2026-03-15 10:00:00'),
    (10, 1, 4, 35, 0, 5, N'2026-03-15 10:00:00'),
    (11, 2, 4, 25, 0, 5, N'2026-03-15 10:00:00'),
    (12, 3, 4, 20, 0, 5, N'2026-03-15 10:00:00'),
    (13, 1, 5, 35, 0, 5, N'2026-03-15 10:00:00'),
    (14, 2, 5, 25, 0, 5, N'2026-03-15 10:00:00'),
    (15, 3, 5, 20, 0, 5, N'2026-03-15 10:00:00'),
    (16, 1, 6, 35, 0, 5, N'2026-03-15 10:00:00'),
    (17, 2, 6, 25, 0, 5, N'2026-03-15 10:00:00'),
    (18, 3, 6, 20, 0, 5, N'2026-03-15 10:00:00'),
    (19, 1, 7, 35, 0, 5, N'2026-03-15 10:00:00'),
    (20, 2, 7, 25, 0, 5, N'2026-03-15 10:00:00'),
    (21, 3, 7, 20, 0, 5, N'2026-03-15 10:00:00'),
    (22, 1, 8, 35, 0, 5, N'2026-03-15 10:00:00'),
    (23, 2, 8, 25, 0, 5, N'2026-03-15 10:00:00'),
    (24, 3, 8, 20, 0, 5, N'2026-03-15 10:00:00'),
    (25, 1, 9, 35, 0, 5, N'2026-03-15 10:00:00'),
    (26, 2, 9, 25, 0, 5, N'2026-03-15 10:00:00'),
    (27, 3, 9, 20, 0, 5, N'2026-03-15 10:00:00'),
    (28, 1, 10, 35, 0, 5, N'2026-03-15 10:00:00'),
    (29, 2, 10, 25, 0, 5, N'2026-03-15 10:00:00'),
    (30, 3, 10, 20, 0, 5, N'2026-03-15 10:00:00'),
    (31, 1, 11, 35, 0, 5, N'2026-03-15 10:00:00'),
    (32, 2, 11, 25, 0, 5, N'2026-03-15 10:00:00'),
    (33, 3, 11, 20, 0, 5, N'2026-03-15 10:00:00'),
    (34, 1, 12, 35, 0, 5, N'2026-03-15 10:00:00'),
    (35, 2, 12, 25, 0, 5, N'2026-03-15 10:00:00'),
    (36, 3, 12, 20, 0, 5, N'2026-03-15 10:00:00'),
    (37, 1, 13, 35, 0, 5, N'2026-03-15 10:00:00'),
    (38, 2, 13, 25, 0, 5, N'2026-03-15 10:00:00'),
    (39, 3, 13, 20, 0, 5, N'2026-03-15 10:00:00'),
    (40, 1, 14, 35, 0, 5, N'2026-03-15 10:00:00'),
    (41, 2, 14, 25, 0, 5, N'2026-03-15 10:00:00'),
    (42, 3, 14, 20, 0, 5, N'2026-03-15 10:00:00'),
    (43, 1, 15, 35, 0, 5, N'2026-03-15 10:00:00'),
    (44, 2, 15, 25, 0, 5, N'2026-03-15 10:00:00'),
    (45, 3, 15, 20, 0, 5, N'2026-03-15 10:00:00'),
    (46, 1, 16, 35, 0, 5, N'2026-03-15 10:00:00'),
    (47, 2, 16, 25, 0, 5, N'2026-03-15 10:00:00'),
    (48, 3, 16, 20, 0, 5, N'2026-03-15 10:00:00'),
    (49, 1, 17, 35, 0, 5, N'2026-03-15 10:00:00'),
    (50, 2, 17, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (51, 3, 17, 20, 0, 5, N'2026-03-15 10:00:00'),
    (52, 1, 18, 35, 0, 5, N'2026-03-15 10:00:00'),
    (53, 2, 18, 25, 0, 5, N'2026-03-15 10:00:00'),
    (54, 3, 18, 20, 0, 5, N'2026-03-15 10:00:00'),
    (55, 1, 19, 35, 0, 5, N'2026-03-15 10:00:00'),
    (56, 2, 19, 25, 0, 5, N'2026-03-15 10:00:00'),
    (57, 3, 19, 20, 0, 5, N'2026-03-15 10:00:00'),
    (58, 1, 20, 35, 0, 5, N'2026-03-15 10:00:00'),
    (59, 2, 20, 25, 0, 5, N'2026-03-15 10:00:00'),
    (60, 3, 20, 20, 0, 5, N'2026-03-15 10:00:00'),
    (61, 1, 21, 35, 0, 5, N'2026-03-15 10:00:00'),
    (62, 2, 21, 25, 0, 5, N'2026-03-15 10:00:00'),
    (63, 3, 21, 20, 0, 5, N'2026-03-15 10:00:00'),
    (64, 1, 22, 35, 0, 5, N'2026-03-15 10:00:00'),
    (65, 2, 22, 25, 0, 5, N'2026-03-15 10:00:00'),
    (66, 3, 22, 20, 0, 5, N'2026-03-15 10:00:00'),
    (67, 1, 23, 35, 0, 5, N'2026-03-15 10:00:00'),
    (68, 2, 23, 25, 0, 5, N'2026-03-15 10:00:00'),
    (69, 3, 23, 20, 0, 5, N'2026-03-15 10:00:00'),
    (70, 1, 24, 35, 0, 5, N'2026-03-15 10:00:00'),
    (71, 2, 24, 25, 0, 5, N'2026-03-15 10:00:00'),
    (72, 3, 24, 20, 0, 5, N'2026-03-15 10:00:00'),
    (73, 1, 25, 35, 0, 5, N'2026-03-15 10:00:00'),
    (74, 2, 25, 25, 0, 5, N'2026-03-15 10:00:00'),
    (75, 3, 25, 20, 0, 5, N'2026-03-15 10:00:00'),
    (76, 1, 26, 35, 0, 5, N'2026-03-15 10:00:00'),
    (77, 2, 26, 25, 0, 5, N'2026-03-15 10:00:00'),
    (78, 3, 26, 20, 0, 5, N'2026-03-15 10:00:00'),
    (79, 1, 27, 35, 0, 5, N'2026-03-15 10:00:00'),
    (80, 2, 27, 25, 0, 5, N'2026-03-15 10:00:00'),
    (81, 3, 27, 20, 0, 5, N'2026-03-15 10:00:00'),
    (82, 1, 28, 35, 0, 5, N'2026-03-15 10:00:00'),
    (83, 2, 28, 25, 0, 5, N'2026-03-15 10:00:00'),
    (84, 3, 28, 20, 0, 5, N'2026-03-15 10:00:00'),
    (85, 1, 29, 35, 0, 5, N'2026-03-15 10:00:00'),
    (86, 2, 29, 25, 0, 5, N'2026-03-15 10:00:00'),
    (87, 3, 29, 20, 0, 5, N'2026-03-15 10:00:00'),
    (88, 1, 30, 35, 0, 5, N'2026-03-15 10:00:00'),
    (89, 2, 30, 25, 0, 5, N'2026-03-15 10:00:00'),
    (90, 3, 30, 20, 0, 5, N'2026-03-15 10:00:00'),
    (91, 1, 31, 35, 0, 5, N'2026-03-15 10:00:00'),
    (92, 2, 31, 25, 0, 5, N'2026-03-15 10:00:00'),
    (93, 3, 31, 20, 0, 5, N'2026-03-15 10:00:00'),
    (94, 1, 32, 35, 0, 5, N'2026-03-15 10:00:00'),
    (95, 2, 32, 25, 0, 5, N'2026-03-15 10:00:00'),
    (96, 3, 32, 20, 0, 5, N'2026-03-15 10:00:00'),
    (97, 1, 33, 35, 0, 5, N'2026-03-15 10:00:00'),
    (98, 2, 33, 25, 0, 5, N'2026-03-15 10:00:00'),
    (99, 3, 33, 20, 0, 5, N'2026-03-15 10:00:00'),
    (100, 1, 34, 35, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (101, 2, 34, 25, 0, 5, N'2026-03-15 10:00:00'),
    (102, 3, 34, 20, 0, 5, N'2026-03-15 10:00:00'),
    (103, 1, 35, 35, 0, 5, N'2026-03-15 10:00:00'),
    (104, 2, 35, 25, 0, 5, N'2026-03-15 10:00:00'),
    (105, 3, 35, 20, 0, 5, N'2026-03-15 10:00:00'),
    (106, 1, 36, 35, 0, 5, N'2026-03-15 10:00:00'),
    (107, 2, 36, 25, 0, 5, N'2026-03-15 10:00:00'),
    (108, 3, 36, 20, 0, 5, N'2026-03-15 10:00:00'),
    (109, 1, 37, 35, 0, 5, N'2026-03-15 10:00:00'),
    (110, 2, 37, 25, 0, 5, N'2026-03-15 10:00:00'),
    (111, 3, 37, 20, 0, 5, N'2026-03-15 10:00:00'),
    (112, 1, 38, 35, 0, 5, N'2026-03-15 10:00:00'),
    (113, 2, 38, 25, 0, 5, N'2026-03-15 10:00:00'),
    (114, 3, 38, 20, 0, 5, N'2026-03-15 10:00:00'),
    (115, 1, 39, 35, 0, 5, N'2026-03-15 10:00:00'),
    (116, 2, 39, 25, 0, 5, N'2026-03-15 10:00:00'),
    (117, 3, 39, 20, 0, 5, N'2026-03-15 10:00:00'),
    (118, 1, 40, 35, 0, 5, N'2026-03-15 10:00:00'),
    (119, 2, 40, 25, 0, 5, N'2026-03-15 10:00:00'),
    (120, 3, 40, 20, 0, 5, N'2026-03-15 10:00:00'),
    (121, 1, 41, 35, 0, 5, N'2026-03-15 10:00:00'),
    (122, 2, 41, 25, 0, 5, N'2026-03-15 10:00:00'),
    (123, 3, 41, 20, 0, 5, N'2026-03-15 10:00:00'),
    (124, 1, 42, 35, 0, 5, N'2026-03-15 10:00:00'),
    (125, 2, 42, 25, 0, 5, N'2026-03-15 10:00:00'),
    (126, 3, 42, 20, 0, 5, N'2026-03-15 10:00:00'),
    (127, 1, 43, 35, 0, 5, N'2026-03-15 10:00:00'),
    (128, 2, 43, 25, 0, 5, N'2026-03-15 10:00:00'),
    (129, 3, 43, 20, 0, 5, N'2026-03-15 10:00:00'),
    (130, 1, 44, 35, 0, 5, N'2026-03-15 10:00:00'),
    (131, 2, 44, 25, 0, 5, N'2026-03-15 10:00:00'),
    (132, 3, 44, 20, 0, 5, N'2026-03-15 10:00:00'),
    (133, 1, 45, 35, 0, 5, N'2026-03-15 10:00:00'),
    (134, 2, 45, 25, 0, 5, N'2026-03-15 10:00:00'),
    (135, 3, 45, 20, 0, 5, N'2026-03-15 10:00:00'),
    (136, 1, 46, 35, 0, 5, N'2026-03-15 10:00:00'),
    (137, 2, 46, 25, 0, 5, N'2026-03-15 10:00:00'),
    (138, 3, 46, 20, 0, 5, N'2026-03-15 10:00:00'),
    (139, 1, 47, 35, 0, 5, N'2026-03-15 10:00:00'),
    (140, 2, 47, 25, 0, 5, N'2026-03-15 10:00:00'),
    (141, 3, 47, 20, 0, 5, N'2026-03-15 10:00:00'),
    (142, 1, 48, 35, 0, 5, N'2026-03-15 10:00:00'),
    (143, 2, 48, 25, 0, 5, N'2026-03-15 10:00:00'),
    (144, 3, 48, 20, 0, 5, N'2026-03-15 10:00:00'),
    (145, 1, 49, 35, 0, 5, N'2026-03-15 10:00:00'),
    (146, 2, 49, 25, 0, 5, N'2026-03-15 10:00:00'),
    (147, 3, 49, 20, 0, 5, N'2026-03-15 10:00:00'),
    (148, 1, 50, 35, 0, 5, N'2026-03-15 10:00:00'),
    (149, 2, 50, 25, 0, 5, N'2026-03-15 10:00:00'),
    (150, 3, 50, 20, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (151, 1, 51, 35, 0, 5, N'2026-03-15 10:00:00'),
    (152, 2, 51, 25, 0, 5, N'2026-03-15 10:00:00'),
    (153, 3, 51, 20, 0, 5, N'2026-03-15 10:00:00'),
    (154, 1, 52, 35, 0, 5, N'2026-03-15 10:00:00'),
    (155, 2, 52, 25, 0, 5, N'2026-03-15 10:00:00'),
    (156, 3, 52, 20, 0, 5, N'2026-03-15 10:00:00'),
    (157, 1, 53, 35, 0, 5, N'2026-03-15 10:00:00'),
    (158, 2, 53, 25, 0, 5, N'2026-03-15 10:00:00'),
    (159, 3, 53, 20, 0, 5, N'2026-03-15 10:00:00'),
    (160, 1, 54, 35, 0, 5, N'2026-03-15 10:00:00'),
    (161, 2, 54, 25, 0, 5, N'2026-03-15 10:00:00'),
    (162, 3, 54, 20, 0, 5, N'2026-03-15 10:00:00'),
    (163, 1, 55, 35, 0, 5, N'2026-03-15 10:00:00'),
    (164, 2, 55, 25, 0, 5, N'2026-03-15 10:00:00'),
    (165, 3, 55, 20, 0, 5, N'2026-03-15 10:00:00'),
    (166, 1, 56, 35, 0, 5, N'2026-03-15 10:00:00'),
    (167, 2, 56, 25, 0, 5, N'2026-03-15 10:00:00'),
    (168, 3, 56, 20, 0, 5, N'2026-03-15 10:00:00'),
    (169, 1, 57, 35, 0, 5, N'2026-03-15 10:00:00'),
    (170, 2, 57, 25, 0, 5, N'2026-03-15 10:00:00'),
    (171, 3, 57, 20, 0, 5, N'2026-03-15 10:00:00'),
    (172, 1, 58, 35, 0, 5, N'2026-03-15 10:00:00'),
    (173, 2, 58, 25, 0, 5, N'2026-03-15 10:00:00'),
    (174, 3, 58, 20, 0, 5, N'2026-03-15 10:00:00'),
    (175, 1, 59, 35, 0, 5, N'2026-03-15 10:00:00'),
    (176, 2, 59, 25, 0, 5, N'2026-03-15 10:00:00'),
    (177, 3, 59, 20, 0, 5, N'2026-03-15 10:00:00'),
    (178, 1, 60, 35, 0, 5, N'2026-03-15 10:00:00'),
    (179, 2, 60, 25, 0, 5, N'2026-03-15 10:00:00'),
    (180, 3, 60, 20, 0, 5, N'2026-03-15 10:00:00'),
    (181, 1, 61, 35, 0, 5, N'2026-03-15 10:00:00'),
    (182, 2, 61, 25, 0, 5, N'2026-03-15 10:00:00'),
    (183, 3, 61, 20, 0, 5, N'2026-03-15 10:00:00'),
    (184, 1, 62, 35, 0, 5, N'2026-03-15 10:00:00'),
    (185, 2, 62, 25, 0, 5, N'2026-03-15 10:00:00'),
    (186, 3, 62, 20, 0, 5, N'2026-03-15 10:00:00'),
    (187, 1, 63, 35, 0, 5, N'2026-03-15 10:00:00'),
    (188, 2, 63, 25, 0, 5, N'2026-03-15 10:00:00'),
    (189, 3, 63, 20, 0, 5, N'2026-03-15 10:00:00'),
    (190, 1, 64, 35, 0, 5, N'2026-03-15 10:00:00'),
    (191, 2, 64, 25, 0, 5, N'2026-03-15 10:00:00'),
    (192, 3, 64, 20, 0, 5, N'2026-03-15 10:00:00'),
    (193, 1, 65, 35, 0, 5, N'2026-03-15 10:00:00'),
    (194, 2, 65, 25, 0, 5, N'2026-03-15 10:00:00'),
    (195, 3, 65, 20, 0, 5, N'2026-03-15 10:00:00'),
    (196, 1, 66, 35, 0, 5, N'2026-03-15 10:00:00'),
    (197, 2, 66, 25, 0, 5, N'2026-03-15 10:00:00'),
    (198, 3, 66, 20, 0, 5, N'2026-03-15 10:00:00'),
    (199, 1, 67, 35, 0, 5, N'2026-03-15 10:00:00'),
    (200, 2, 67, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (201, 3, 67, 20, 0, 5, N'2026-03-15 10:00:00'),
    (202, 1, 68, 35, 0, 5, N'2026-03-15 10:00:00'),
    (203, 2, 68, 25, 0, 5, N'2026-03-15 10:00:00'),
    (204, 3, 68, 20, 0, 5, N'2026-03-15 10:00:00'),
    (205, 1, 69, 35, 0, 5, N'2026-03-15 10:00:00'),
    (206, 2, 69, 25, 0, 5, N'2026-03-15 10:00:00'),
    (207, 3, 69, 20, 0, 5, N'2026-03-15 10:00:00'),
    (208, 1, 70, 35, 0, 5, N'2026-03-15 10:00:00'),
    (209, 2, 70, 25, 0, 5, N'2026-03-15 10:00:00'),
    (210, 3, 70, 20, 0, 5, N'2026-03-15 10:00:00'),
    (211, 1, 71, 35, 0, 5, N'2026-03-15 10:00:00'),
    (212, 2, 71, 25, 0, 5, N'2026-03-15 10:00:00'),
    (213, 3, 71, 20, 0, 5, N'2026-03-15 10:00:00'),
    (214, 1, 72, 35, 0, 5, N'2026-03-15 10:00:00'),
    (215, 2, 72, 25, 0, 5, N'2026-03-15 10:00:00'),
    (216, 3, 72, 20, 0, 5, N'2026-03-15 10:00:00'),
    (217, 1, 73, 35, 0, 5, N'2026-03-15 10:00:00'),
    (218, 2, 73, 25, 0, 5, N'2026-03-15 10:00:00'),
    (219, 3, 73, 20, 0, 5, N'2026-03-15 10:00:00'),
    (220, 1, 74, 35, 0, 5, N'2026-03-15 10:00:00'),
    (221, 2, 74, 25, 0, 5, N'2026-03-15 10:00:00'),
    (222, 3, 74, 20, 0, 5, N'2026-03-15 10:00:00'),
    (223, 1, 75, 35, 0, 5, N'2026-03-15 10:00:00'),
    (224, 2, 75, 25, 0, 5, N'2026-03-15 10:00:00'),
    (225, 3, 75, 20, 0, 5, N'2026-03-15 10:00:00'),
    (226, 1, 76, 35, 0, 5, N'2026-03-15 10:00:00'),
    (227, 2, 76, 25, 0, 5, N'2026-03-15 10:00:00'),
    (228, 3, 76, 20, 0, 5, N'2026-03-15 10:00:00'),
    (229, 1, 77, 35, 0, 5, N'2026-03-15 10:00:00'),
    (230, 2, 77, 25, 0, 5, N'2026-03-15 10:00:00'),
    (231, 3, 77, 20, 0, 5, N'2026-03-15 10:00:00'),
    (232, 1, 78, 35, 0, 5, N'2026-03-15 10:00:00'),
    (233, 2, 78, 25, 0, 5, N'2026-03-15 10:00:00'),
    (234, 3, 78, 20, 0, 5, N'2026-03-15 10:00:00'),
    (235, 1, 79, 35, 0, 5, N'2026-03-15 10:00:00'),
    (236, 2, 79, 25, 0, 5, N'2026-03-15 10:00:00'),
    (237, 3, 79, 20, 0, 5, N'2026-03-15 10:00:00'),
    (238, 1, 80, 35, 0, 5, N'2026-03-15 10:00:00'),
    (239, 2, 80, 25, 0, 5, N'2026-03-15 10:00:00'),
    (240, 3, 80, 20, 0, 5, N'2026-03-15 10:00:00'),
    (241, 1, 81, 35, 0, 5, N'2026-03-15 10:00:00'),
    (242, 2, 81, 25, 0, 5, N'2026-03-15 10:00:00'),
    (243, 3, 81, 20, 0, 5, N'2026-03-15 10:00:00'),
    (244, 1, 82, 35, 0, 5, N'2026-03-15 10:00:00'),
    (245, 2, 82, 25, 0, 5, N'2026-03-15 10:00:00'),
    (246, 3, 82, 20, 0, 5, N'2026-03-15 10:00:00'),
    (247, 1, 83, 35, 0, 5, N'2026-03-15 10:00:00'),
    (248, 2, 83, 25, 0, 5, N'2026-03-15 10:00:00'),
    (249, 3, 83, 20, 0, 5, N'2026-03-15 10:00:00'),
    (250, 1, 84, 35, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (251, 2, 84, 25, 0, 5, N'2026-03-15 10:00:00'),
    (252, 3, 84, 20, 0, 5, N'2026-03-15 10:00:00'),
    (253, 1, 85, 35, 0, 5, N'2026-03-15 10:00:00'),
    (254, 2, 85, 25, 0, 5, N'2026-03-15 10:00:00'),
    (255, 3, 85, 20, 0, 5, N'2026-03-15 10:00:00'),
    (256, 1, 86, 35, 0, 5, N'2026-03-15 10:00:00'),
    (257, 2, 86, 25, 0, 5, N'2026-03-15 10:00:00'),
    (258, 3, 86, 20, 0, 5, N'2026-03-15 10:00:00'),
    (259, 1, 87, 35, 0, 5, N'2026-03-15 10:00:00'),
    (260, 2, 87, 25, 0, 5, N'2026-03-15 10:00:00'),
    (261, 3, 87, 20, 0, 5, N'2026-03-15 10:00:00'),
    (262, 1, 88, 35, 0, 5, N'2026-03-15 10:00:00'),
    (263, 2, 88, 25, 0, 5, N'2026-03-15 10:00:00'),
    (264, 3, 88, 20, 0, 5, N'2026-03-15 10:00:00'),
    (265, 1, 89, 35, 0, 5, N'2026-03-15 10:00:00'),
    (266, 2, 89, 25, 0, 5, N'2026-03-15 10:00:00'),
    (267, 3, 89, 20, 0, 5, N'2026-03-15 10:00:00'),
    (268, 1, 90, 35, 0, 5, N'2026-03-15 10:00:00'),
    (269, 2, 90, 25, 0, 5, N'2026-03-15 10:00:00'),
    (270, 3, 90, 20, 0, 5, N'2026-03-15 10:00:00'),
    (271, 1, 91, 35, 0, 5, N'2026-03-15 10:00:00'),
    (272, 2, 91, 25, 0, 5, N'2026-03-15 10:00:00'),
    (273, 3, 91, 20, 0, 5, N'2026-03-15 10:00:00'),
    (274, 1, 92, 35, 0, 5, N'2026-03-15 10:00:00'),
    (275, 2, 92, 25, 0, 5, N'2026-03-15 10:00:00'),
    (276, 3, 92, 20, 0, 5, N'2026-03-15 10:00:00'),
    (277, 1, 93, 35, 0, 5, N'2026-03-15 10:00:00'),
    (278, 2, 93, 25, 0, 5, N'2026-03-15 10:00:00'),
    (279, 3, 93, 20, 0, 5, N'2026-03-15 10:00:00'),
    (280, 1, 94, 35, 0, 5, N'2026-03-15 10:00:00'),
    (281, 2, 94, 25, 0, 5, N'2026-03-15 10:00:00'),
    (282, 3, 94, 20, 0, 5, N'2026-03-15 10:00:00'),
    (283, 1, 95, 35, 0, 5, N'2026-03-15 10:00:00'),
    (284, 2, 95, 25, 0, 5, N'2026-03-15 10:00:00'),
    (285, 3, 95, 20, 0, 5, N'2026-03-15 10:00:00'),
    (286, 1, 96, 35, 0, 5, N'2026-03-15 10:00:00'),
    (287, 2, 96, 25, 0, 5, N'2026-03-15 10:00:00'),
    (288, 3, 96, 20, 0, 5, N'2026-03-15 10:00:00'),
    (289, 1, 97, 35, 0, 5, N'2026-03-15 10:00:00'),
    (290, 2, 97, 25, 0, 5, N'2026-03-15 10:00:00'),
    (291, 3, 97, 20, 0, 5, N'2026-03-15 10:00:00'),
    (292, 1, 98, 35, 0, 5, N'2026-03-15 10:00:00'),
    (293, 2, 98, 25, 0, 5, N'2026-03-15 10:00:00'),
    (294, 3, 98, 20, 0, 5, N'2026-03-15 10:00:00'),
    (295, 1, 99, 35, 0, 5, N'2026-03-15 10:00:00'),
    (296, 2, 99, 25, 0, 5, N'2026-03-15 10:00:00'),
    (297, 3, 99, 20, 0, 5, N'2026-03-15 10:00:00'),
    (298, 1, 100, 35, 0, 5, N'2026-03-15 10:00:00'),
    (299, 2, 100, 25, 0, 5, N'2026-03-15 10:00:00'),
    (300, 3, 100, 20, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (301, 1, 101, 35, 0, 5, N'2026-03-15 10:00:00'),
    (302, 2, 101, 25, 0, 5, N'2026-03-15 10:00:00'),
    (303, 3, 101, 20, 0, 5, N'2026-03-15 10:00:00'),
    (304, 1, 102, 35, 0, 5, N'2026-03-15 10:00:00'),
    (305, 2, 102, 25, 0, 5, N'2026-03-15 10:00:00'),
    (306, 3, 102, 20, 0, 5, N'2026-03-15 10:00:00'),
    (307, 1, 103, 35, 0, 5, N'2026-03-15 10:00:00'),
    (308, 2, 103, 25, 0, 5, N'2026-03-15 10:00:00'),
    (309, 3, 103, 20, 0, 5, N'2026-03-15 10:00:00'),
    (310, 1, 104, 35, 0, 5, N'2026-03-15 10:00:00'),
    (311, 2, 104, 25, 0, 5, N'2026-03-15 10:00:00'),
    (312, 3, 104, 20, 0, 5, N'2026-03-15 10:00:00'),
    (313, 1, 105, 35, 0, 5, N'2026-03-15 10:00:00'),
    (314, 2, 105, 25, 0, 5, N'2026-03-15 10:00:00'),
    (315, 3, 105, 20, 0, 5, N'2026-03-15 10:00:00'),
    (316, 1, 106, 35, 0, 5, N'2026-03-15 10:00:00'),
    (317, 2, 106, 25, 0, 5, N'2026-03-15 10:00:00'),
    (318, 3, 106, 20, 0, 5, N'2026-03-15 10:00:00'),
    (319, 1, 107, 35, 0, 5, N'2026-03-15 10:00:00'),
    (320, 2, 107, 25, 0, 5, N'2026-03-15 10:00:00'),
    (321, 3, 107, 20, 0, 5, N'2026-03-15 10:00:00'),
    (322, 1, 108, 35, 0, 5, N'2026-03-15 10:00:00'),
    (323, 2, 108, 25, 0, 5, N'2026-03-15 10:00:00'),
    (324, 3, 108, 20, 0, 5, N'2026-03-15 10:00:00'),
    (325, 1, 109, 35, 0, 5, N'2026-03-15 10:00:00'),
    (326, 2, 109, 25, 0, 5, N'2026-03-15 10:00:00'),
    (327, 3, 109, 20, 0, 5, N'2026-03-15 10:00:00'),
    (328, 1, 110, 35, 0, 5, N'2026-03-15 10:00:00'),
    (329, 2, 110, 25, 0, 5, N'2026-03-15 10:00:00'),
    (330, 3, 110, 20, 0, 5, N'2026-03-15 10:00:00'),
    (331, 1, 111, 35, 0, 5, N'2026-03-15 10:00:00'),
    (332, 2, 111, 25, 0, 5, N'2026-03-15 10:00:00'),
    (333, 3, 111, 20, 0, 5, N'2026-03-15 10:00:00'),
    (334, 1, 112, 35, 0, 5, N'2026-03-15 10:00:00'),
    (335, 2, 112, 25, 0, 5, N'2026-03-15 10:00:00'),
    (336, 3, 112, 20, 0, 5, N'2026-03-15 10:00:00'),
    (337, 1, 113, 35, 0, 5, N'2026-03-15 10:00:00'),
    (338, 2, 113, 25, 0, 5, N'2026-03-15 10:00:00'),
    (339, 3, 113, 20, 0, 5, N'2026-03-15 10:00:00'),
    (340, 1, 114, 35, 0, 5, N'2026-03-15 10:00:00'),
    (341, 2, 114, 25, 0, 5, N'2026-03-15 10:00:00'),
    (342, 3, 114, 20, 0, 5, N'2026-03-15 10:00:00'),
    (343, 1, 115, 35, 0, 5, N'2026-03-15 10:00:00'),
    (344, 2, 115, 25, 0, 5, N'2026-03-15 10:00:00'),
    (345, 3, 115, 20, 0, 5, N'2026-03-15 10:00:00'),
    (346, 1, 116, 35, 0, 5, N'2026-03-15 10:00:00'),
    (347, 2, 116, 25, 0, 5, N'2026-03-15 10:00:00'),
    (348, 3, 116, 20, 0, 5, N'2026-03-15 10:00:00'),
    (349, 1, 117, 35, 0, 5, N'2026-03-15 10:00:00'),
    (350, 2, 117, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (351, 3, 117, 20, 0, 5, N'2026-03-15 10:00:00'),
    (352, 1, 118, 35, 0, 5, N'2026-03-15 10:00:00'),
    (353, 2, 118, 25, 0, 5, N'2026-03-15 10:00:00'),
    (354, 3, 118, 20, 0, 5, N'2026-03-15 10:00:00'),
    (355, 1, 119, 35, 0, 5, N'2026-03-15 10:00:00'),
    (356, 2, 119, 25, 0, 5, N'2026-03-15 10:00:00'),
    (357, 3, 119, 20, 0, 5, N'2026-03-15 10:00:00'),
    (358, 1, 120, 35, 0, 5, N'2026-03-15 10:00:00'),
    (359, 2, 120, 25, 0, 5, N'2026-03-15 10:00:00'),
    (360, 3, 120, 20, 0, 5, N'2026-03-15 10:00:00'),
    (361, 1, 121, 35, 0, 5, N'2026-03-15 10:00:00'),
    (362, 2, 121, 25, 0, 5, N'2026-03-15 10:00:00'),
    (363, 3, 121, 20, 0, 5, N'2026-03-15 10:00:00'),
    (364, 1, 122, 35, 0, 5, N'2026-03-15 10:00:00'),
    (365, 2, 122, 25, 0, 5, N'2026-03-15 10:00:00'),
    (366, 3, 122, 20, 0, 5, N'2026-03-15 10:00:00'),
    (367, 1, 123, 35, 0, 5, N'2026-03-15 10:00:00'),
    (368, 2, 123, 25, 0, 5, N'2026-03-15 10:00:00'),
    (369, 3, 123, 20, 0, 5, N'2026-03-15 10:00:00'),
    (370, 1, 124, 35, 0, 5, N'2026-03-15 10:00:00'),
    (371, 2, 124, 25, 0, 5, N'2026-03-15 10:00:00'),
    (372, 3, 124, 20, 0, 5, N'2026-03-15 10:00:00'),
    (373, 1, 125, 35, 0, 5, N'2026-03-15 10:00:00'),
    (374, 2, 125, 25, 0, 5, N'2026-03-15 10:00:00'),
    (375, 3, 125, 20, 0, 5, N'2026-03-15 10:00:00'),
    (376, 1, 126, 35, 0, 5, N'2026-03-15 10:00:00'),
    (377, 2, 126, 25, 0, 5, N'2026-03-15 10:00:00'),
    (378, 3, 126, 20, 0, 5, N'2026-03-15 10:00:00'),
    (379, 1, 127, 35, 0, 5, N'2026-03-15 10:00:00'),
    (380, 2, 127, 25, 0, 5, N'2026-03-15 10:00:00'),
    (381, 3, 127, 20, 0, 5, N'2026-03-15 10:00:00'),
    (382, 1, 128, 35, 0, 5, N'2026-03-15 10:00:00'),
    (383, 2, 128, 25, 0, 5, N'2026-03-15 10:00:00'),
    (384, 3, 128, 20, 0, 5, N'2026-03-15 10:00:00'),
    (385, 1, 129, 35, 0, 5, N'2026-03-15 10:00:00'),
    (386, 2, 129, 25, 0, 5, N'2026-03-15 10:00:00'),
    (387, 3, 129, 20, 0, 5, N'2026-03-15 10:00:00'),
    (388, 1, 130, 35, 0, 5, N'2026-03-15 10:00:00'),
    (389, 2, 130, 25, 0, 5, N'2026-03-15 10:00:00'),
    (390, 3, 130, 20, 0, 5, N'2026-03-15 10:00:00'),
    (391, 1, 131, 35, 0, 5, N'2026-03-15 10:00:00'),
    (392, 2, 131, 25, 0, 5, N'2026-03-15 10:00:00'),
    (393, 3, 131, 20, 0, 5, N'2026-03-15 10:00:00'),
    (394, 1, 132, 35, 0, 5, N'2026-03-15 10:00:00'),
    (395, 2, 132, 25, 0, 5, N'2026-03-15 10:00:00'),
    (396, 3, 132, 20, 0, 5, N'2026-03-15 10:00:00'),
    (397, 1, 133, 35, 0, 5, N'2026-03-15 10:00:00'),
    (398, 2, 133, 25, 0, 5, N'2026-03-15 10:00:00'),
    (399, 3, 133, 20, 0, 5, N'2026-03-15 10:00:00'),
    (400, 1, 134, 35, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (401, 2, 134, 25, 0, 5, N'2026-03-15 10:00:00'),
    (402, 3, 134, 20, 0, 5, N'2026-03-15 10:00:00'),
    (403, 1, 135, 35, 0, 5, N'2026-03-15 10:00:00'),
    (404, 2, 135, 25, 0, 5, N'2026-03-15 10:00:00'),
    (405, 3, 135, 20, 0, 5, N'2026-03-15 10:00:00'),
    (406, 1, 136, 35, 0, 5, N'2026-03-15 10:00:00'),
    (407, 2, 136, 25, 0, 5, N'2026-03-15 10:00:00'),
    (408, 3, 136, 20, 0, 5, N'2026-03-15 10:00:00'),
    (409, 1, 137, 35, 0, 5, N'2026-03-15 10:00:00'),
    (410, 2, 137, 25, 0, 5, N'2026-03-15 10:00:00'),
    (411, 3, 137, 20, 0, 5, N'2026-03-15 10:00:00'),
    (412, 1, 138, 35, 0, 5, N'2026-03-15 10:00:00'),
    (413, 2, 138, 25, 0, 5, N'2026-03-15 10:00:00'),
    (414, 3, 138, 20, 0, 5, N'2026-03-15 10:00:00'),
    (415, 1, 139, 35, 0, 5, N'2026-03-15 10:00:00'),
    (416, 2, 139, 25, 0, 5, N'2026-03-15 10:00:00'),
    (417, 3, 139, 20, 0, 5, N'2026-03-15 10:00:00'),
    (418, 1, 140, 35, 0, 5, N'2026-03-15 10:00:00'),
    (419, 2, 140, 25, 0, 5, N'2026-03-15 10:00:00'),
    (420, 3, 140, 20, 0, 5, N'2026-03-15 10:00:00'),
    (421, 1, 141, 35, 0, 5, N'2026-03-15 10:00:00'),
    (422, 2, 141, 25, 0, 5, N'2026-03-15 10:00:00'),
    (423, 3, 141, 20, 0, 5, N'2026-03-15 10:00:00'),
    (424, 1, 142, 35, 0, 5, N'2026-03-15 10:00:00'),
    (425, 2, 142, 25, 0, 5, N'2026-03-15 10:00:00'),
    (426, 3, 142, 20, 0, 5, N'2026-03-15 10:00:00'),
    (427, 1, 143, 35, 0, 5, N'2026-03-15 10:00:00'),
    (428, 2, 143, 25, 0, 5, N'2026-03-15 10:00:00'),
    (429, 3, 143, 20, 0, 5, N'2026-03-15 10:00:00'),
    (430, 1, 144, 35, 0, 5, N'2026-03-15 10:00:00'),
    (431, 2, 144, 25, 0, 5, N'2026-03-15 10:00:00'),
    (432, 3, 144, 20, 0, 5, N'2026-03-15 10:00:00'),
    (433, 1, 145, 35, 0, 5, N'2026-03-15 10:00:00'),
    (434, 2, 145, 25, 0, 5, N'2026-03-15 10:00:00'),
    (435, 3, 145, 20, 0, 5, N'2026-03-15 10:00:00'),
    (436, 1, 146, 35, 0, 5, N'2026-03-15 10:00:00'),
    (437, 2, 146, 25, 0, 5, N'2026-03-15 10:00:00'),
    (438, 3, 146, 20, 0, 5, N'2026-03-15 10:00:00'),
    (439, 1, 147, 35, 0, 5, N'2026-03-15 10:00:00'),
    (440, 2, 147, 25, 0, 5, N'2026-03-15 10:00:00'),
    (441, 3, 147, 20, 0, 5, N'2026-03-15 10:00:00'),
    (442, 1, 148, 35, 0, 5, N'2026-03-15 10:00:00'),
    (443, 2, 148, 25, 0, 5, N'2026-03-15 10:00:00'),
    (444, 3, 148, 20, 0, 5, N'2026-03-15 10:00:00'),
    (445, 1, 149, 35, 0, 5, N'2026-03-15 10:00:00'),
    (446, 2, 149, 25, 0, 5, N'2026-03-15 10:00:00'),
    (447, 3, 149, 20, 0, 5, N'2026-03-15 10:00:00'),
    (448, 1, 150, 35, 0, 5, N'2026-03-15 10:00:00'),
    (449, 2, 150, 25, 0, 5, N'2026-03-15 10:00:00'),
    (450, 3, 150, 20, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (451, 1, 151, 35, 0, 5, N'2026-03-15 10:00:00'),
    (452, 2, 151, 25, 0, 5, N'2026-03-15 10:00:00'),
    (453, 3, 151, 20, 0, 5, N'2026-03-15 10:00:00'),
    (454, 1, 152, 35, 0, 5, N'2026-03-15 10:00:00'),
    (455, 2, 152, 25, 0, 5, N'2026-03-15 10:00:00'),
    (456, 3, 152, 20, 0, 5, N'2026-03-15 10:00:00'),
    (457, 1, 153, 35, 0, 5, N'2026-03-15 10:00:00'),
    (458, 2, 153, 25, 0, 5, N'2026-03-15 10:00:00'),
    (459, 3, 153, 20, 0, 5, N'2026-03-15 10:00:00'),
    (460, 1, 154, 35, 0, 5, N'2026-03-15 10:00:00'),
    (461, 2, 154, 25, 0, 5, N'2026-03-15 10:00:00'),
    (462, 3, 154, 20, 0, 5, N'2026-03-15 10:00:00'),
    (463, 1, 155, 35, 0, 5, N'2026-03-15 10:00:00'),
    (464, 2, 155, 25, 0, 5, N'2026-03-15 10:00:00'),
    (465, 3, 155, 20, 0, 5, N'2026-03-15 10:00:00'),
    (466, 1, 156, 35, 0, 5, N'2026-03-15 10:00:00'),
    (467, 2, 156, 25, 0, 5, N'2026-03-15 10:00:00'),
    (468, 3, 156, 20, 0, 5, N'2026-03-15 10:00:00'),
    (469, 1, 157, 35, 0, 5, N'2026-03-15 10:00:00'),
    (470, 2, 157, 25, 0, 5, N'2026-03-15 10:00:00'),
    (471, 3, 157, 20, 0, 5, N'2026-03-15 10:00:00'),
    (472, 1, 158, 35, 0, 5, N'2026-03-15 10:00:00'),
    (473, 2, 158, 25, 0, 5, N'2026-03-15 10:00:00'),
    (474, 3, 158, 20, 0, 5, N'2026-03-15 10:00:00'),
    (475, 1, 159, 35, 0, 5, N'2026-03-15 10:00:00'),
    (476, 2, 159, 25, 0, 5, N'2026-03-15 10:00:00'),
    (477, 3, 159, 20, 0, 5, N'2026-03-15 10:00:00'),
    (478, 1, 160, 35, 0, 5, N'2026-03-15 10:00:00'),
    (479, 2, 160, 25, 0, 5, N'2026-03-15 10:00:00'),
    (480, 3, 160, 20, 0, 5, N'2026-03-15 10:00:00'),
    (481, 1, 161, 35, 0, 5, N'2026-03-15 10:00:00'),
    (482, 2, 161, 25, 0, 5, N'2026-03-15 10:00:00'),
    (483, 3, 161, 20, 0, 5, N'2026-03-15 10:00:00'),
    (484, 1, 162, 35, 0, 5, N'2026-03-15 10:00:00'),
    (485, 2, 162, 25, 0, 5, N'2026-03-15 10:00:00'),
    (486, 3, 162, 20, 0, 5, N'2026-03-15 10:00:00'),
    (487, 1, 163, 35, 0, 5, N'2026-03-15 10:00:00'),
    (488, 2, 163, 25, 0, 5, N'2026-03-15 10:00:00'),
    (489, 3, 163, 20, 0, 5, N'2026-03-15 10:00:00'),
    (490, 1, 164, 35, 0, 5, N'2026-03-15 10:00:00'),
    (491, 2, 164, 25, 0, 5, N'2026-03-15 10:00:00'),
    (492, 3, 164, 20, 0, 5, N'2026-03-15 10:00:00'),
    (493, 1, 165, 35, 0, 5, N'2026-03-15 10:00:00'),
    (494, 2, 165, 25, 0, 5, N'2026-03-15 10:00:00'),
    (495, 3, 165, 20, 0, 5, N'2026-03-15 10:00:00'),
    (496, 1, 166, 35, 0, 5, N'2026-03-15 10:00:00'),
    (497, 2, 166, 25, 0, 5, N'2026-03-15 10:00:00'),
    (498, 3, 166, 20, 0, 5, N'2026-03-15 10:00:00'),
    (499, 1, 167, 35, 0, 5, N'2026-03-15 10:00:00'),
    (500, 2, 167, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (501, 3, 167, 20, 0, 5, N'2026-03-15 10:00:00'),
    (502, 1, 168, 35, 0, 5, N'2026-03-15 10:00:00'),
    (503, 2, 168, 25, 0, 5, N'2026-03-15 10:00:00'),
    (504, 3, 168, 20, 0, 5, N'2026-03-15 10:00:00'),
    (505, 1, 169, 35, 0, 5, N'2026-03-15 10:00:00'),
    (506, 2, 169, 25, 0, 5, N'2026-03-15 10:00:00'),
    (507, 3, 169, 20, 0, 5, N'2026-03-15 10:00:00'),
    (508, 1, 170, 35, 0, 5, N'2026-03-15 10:00:00'),
    (509, 2, 170, 25, 0, 5, N'2026-03-15 10:00:00'),
    (510, 3, 170, 20, 0, 5, N'2026-03-15 10:00:00'),
    (511, 1, 171, 35, 0, 5, N'2026-03-15 10:00:00'),
    (512, 2, 171, 25, 0, 5, N'2026-03-15 10:00:00'),
    (513, 3, 171, 20, 0, 5, N'2026-03-15 10:00:00'),
    (514, 1, 172, 35, 0, 5, N'2026-03-15 10:00:00'),
    (515, 2, 172, 25, 0, 5, N'2026-03-15 10:00:00'),
    (516, 3, 172, 20, 0, 5, N'2026-03-15 10:00:00'),
    (517, 1, 173, 35, 0, 5, N'2026-03-15 10:00:00'),
    (518, 2, 173, 25, 0, 5, N'2026-03-15 10:00:00'),
    (519, 3, 173, 20, 0, 5, N'2026-03-15 10:00:00'),
    (520, 1, 174, 35, 0, 5, N'2026-03-15 10:00:00'),
    (521, 2, 174, 25, 0, 5, N'2026-03-15 10:00:00'),
    (522, 3, 174, 20, 0, 5, N'2026-03-15 10:00:00'),
    (523, 1, 175, 35, 0, 5, N'2026-03-15 10:00:00'),
    (524, 2, 175, 25, 0, 5, N'2026-03-15 10:00:00'),
    (525, 3, 175, 20, 0, 5, N'2026-03-15 10:00:00'),
    (526, 1, 176, 35, 0, 5, N'2026-03-15 10:00:00'),
    (527, 2, 176, 25, 0, 5, N'2026-03-15 10:00:00'),
    (528, 3, 176, 20, 0, 5, N'2026-03-15 10:00:00'),
    (529, 1, 177, 35, 0, 5, N'2026-03-15 10:00:00'),
    (530, 2, 177, 25, 0, 5, N'2026-03-15 10:00:00'),
    (531, 3, 177, 20, 0, 5, N'2026-03-15 10:00:00'),
    (532, 1, 178, 35, 0, 5, N'2026-03-15 10:00:00'),
    (533, 2, 178, 25, 0, 5, N'2026-03-15 10:00:00'),
    (534, 3, 178, 20, 0, 5, N'2026-03-15 10:00:00'),
    (535, 1, 179, 35, 0, 5, N'2026-03-15 10:00:00'),
    (536, 2, 179, 25, 0, 5, N'2026-03-15 10:00:00'),
    (537, 3, 179, 20, 0, 5, N'2026-03-15 10:00:00'),
    (538, 1, 180, 35, 0, 5, N'2026-03-15 10:00:00'),
    (539, 2, 180, 25, 0, 5, N'2026-03-15 10:00:00'),
    (540, 3, 180, 20, 0, 5, N'2026-03-15 10:00:00'),
    (541, 1, 181, 35, 0, 5, N'2026-03-15 10:00:00'),
    (542, 2, 181, 25, 0, 5, N'2026-03-15 10:00:00'),
    (543, 3, 181, 20, 0, 5, N'2026-03-15 10:00:00'),
    (544, 1, 182, 35, 0, 5, N'2026-03-15 10:00:00'),
    (545, 2, 182, 25, 0, 5, N'2026-03-15 10:00:00'),
    (546, 3, 182, 20, 0, 5, N'2026-03-15 10:00:00'),
    (547, 1, 183, 35, 0, 5, N'2026-03-15 10:00:00'),
    (548, 2, 183, 25, 0, 5, N'2026-03-15 10:00:00'),
    (549, 3, 183, 20, 0, 5, N'2026-03-15 10:00:00'),
    (550, 1, 184, 35, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (551, 2, 184, 25, 0, 5, N'2026-03-15 10:00:00'),
    (552, 3, 184, 20, 0, 5, N'2026-03-15 10:00:00'),
    (553, 1, 185, 35, 0, 5, N'2026-03-15 10:00:00'),
    (554, 2, 185, 25, 0, 5, N'2026-03-15 10:00:00'),
    (555, 3, 185, 20, 0, 5, N'2026-03-15 10:00:00'),
    (556, 1, 186, 35, 0, 5, N'2026-03-15 10:00:00'),
    (557, 2, 186, 25, 0, 5, N'2026-03-15 10:00:00'),
    (558, 3, 186, 20, 0, 5, N'2026-03-15 10:00:00'),
    (559, 1, 187, 35, 0, 5, N'2026-03-15 10:00:00'),
    (560, 2, 187, 25, 0, 5, N'2026-03-15 10:00:00'),
    (561, 3, 187, 20, 0, 5, N'2026-03-15 10:00:00'),
    (562, 1, 188, 35, 0, 5, N'2026-03-15 10:00:00'),
    (563, 2, 188, 25, 0, 5, N'2026-03-15 10:00:00'),
    (564, 3, 188, 20, 0, 5, N'2026-03-15 10:00:00'),
    (565, 1, 189, 35, 0, 5, N'2026-03-15 10:00:00'),
    (566, 2, 189, 25, 0, 5, N'2026-03-15 10:00:00'),
    (567, 3, 189, 20, 0, 5, N'2026-03-15 10:00:00'),
    (568, 1, 190, 35, 0, 5, N'2026-03-15 10:00:00'),
    (569, 2, 190, 25, 0, 5, N'2026-03-15 10:00:00'),
    (570, 3, 190, 20, 0, 5, N'2026-03-15 10:00:00'),
    (571, 1, 191, 35, 0, 5, N'2026-03-15 10:00:00'),
    (572, 2, 191, 25, 0, 5, N'2026-03-15 10:00:00'),
    (573, 3, 191, 20, 0, 5, N'2026-03-15 10:00:00'),
    (574, 1, 192, 35, 0, 5, N'2026-03-15 10:00:00'),
    (575, 2, 192, 25, 0, 5, N'2026-03-15 10:00:00'),
    (576, 3, 192, 20, 0, 5, N'2026-03-15 10:00:00'),
    (577, 1, 193, 35, 0, 5, N'2026-03-15 10:00:00'),
    (578, 2, 193, 25, 0, 5, N'2026-03-15 10:00:00'),
    (579, 3, 193, 20, 0, 5, N'2026-03-15 10:00:00'),
    (580, 1, 194, 35, 0, 5, N'2026-03-15 10:00:00'),
    (581, 2, 194, 25, 0, 5, N'2026-03-15 10:00:00'),
    (582, 3, 194, 20, 0, 5, N'2026-03-15 10:00:00'),
    (583, 1, 195, 35, 0, 5, N'2026-03-15 10:00:00'),
    (584, 2, 195, 25, 0, 5, N'2026-03-15 10:00:00'),
    (585, 3, 195, 20, 0, 5, N'2026-03-15 10:00:00'),
    (586, 1, 196, 35, 0, 5, N'2026-03-15 10:00:00'),
    (587, 2, 196, 25, 0, 5, N'2026-03-15 10:00:00'),
    (588, 3, 196, 20, 0, 5, N'2026-03-15 10:00:00'),
    (589, 1, 197, 35, 0, 5, N'2026-03-15 10:00:00'),
    (590, 2, 197, 25, 0, 5, N'2026-03-15 10:00:00'),
    (591, 3, 197, 20, 0, 5, N'2026-03-15 10:00:00'),
    (592, 1, 198, 35, 0, 5, N'2026-03-15 10:00:00'),
    (593, 2, 198, 25, 0, 5, N'2026-03-15 10:00:00'),
    (594, 3, 198, 20, 0, 5, N'2026-03-15 10:00:00'),
    (595, 1, 199, 35, 0, 5, N'2026-03-15 10:00:00'),
    (596, 2, 199, 25, 0, 5, N'2026-03-15 10:00:00'),
    (597, 3, 199, 20, 0, 5, N'2026-03-15 10:00:00'),
    (598, 1, 200, 35, 0, 5, N'2026-03-15 10:00:00'),
    (599, 2, 200, 25, 0, 5, N'2026-03-15 10:00:00'),
    (600, 3, 200, 20, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (601, 1, 201, 35, 0, 5, N'2026-03-15 10:00:00'),
    (602, 2, 201, 25, 0, 5, N'2026-03-15 10:00:00'),
    (603, 3, 201, 20, 0, 5, N'2026-03-15 10:00:00'),
    (604, 1, 202, 35, 0, 5, N'2026-03-15 10:00:00'),
    (605, 2, 202, 25, 0, 5, N'2026-03-15 10:00:00'),
    (606, 3, 202, 20, 0, 5, N'2026-03-15 10:00:00'),
    (607, 1, 203, 35, 0, 5, N'2026-03-15 10:00:00'),
    (608, 2, 203, 25, 0, 5, N'2026-03-15 10:00:00'),
    (609, 3, 203, 20, 0, 5, N'2026-03-15 10:00:00'),
    (610, 1, 204, 35, 0, 5, N'2026-03-15 10:00:00'),
    (611, 2, 204, 25, 0, 5, N'2026-03-15 10:00:00'),
    (612, 3, 204, 20, 0, 5, N'2026-03-15 10:00:00'),
    (613, 1, 205, 35, 0, 5, N'2026-03-15 10:00:00'),
    (614, 2, 205, 25, 0, 5, N'2026-03-15 10:00:00'),
    (615, 3, 205, 20, 0, 5, N'2026-03-15 10:00:00'),
    (616, 1, 206, 35, 0, 5, N'2026-03-15 10:00:00'),
    (617, 2, 206, 25, 0, 5, N'2026-03-15 10:00:00'),
    (618, 3, 206, 20, 0, 5, N'2026-03-15 10:00:00'),
    (619, 1, 207, 35, 0, 5, N'2026-03-15 10:00:00'),
    (620, 2, 207, 25, 0, 5, N'2026-03-15 10:00:00'),
    (621, 3, 207, 20, 0, 5, N'2026-03-15 10:00:00'),
    (622, 1, 208, 35, 0, 5, N'2026-03-15 10:00:00'),
    (623, 2, 208, 25, 0, 5, N'2026-03-15 10:00:00'),
    (624, 3, 208, 20, 0, 5, N'2026-03-15 10:00:00'),
    (625, 1, 209, 35, 0, 5, N'2026-03-15 10:00:00'),
    (626, 2, 209, 25, 0, 5, N'2026-03-15 10:00:00'),
    (627, 3, 209, 20, 0, 5, N'2026-03-15 10:00:00'),
    (628, 1, 210, 35, 0, 5, N'2026-03-15 10:00:00'),
    (629, 2, 210, 25, 0, 5, N'2026-03-15 10:00:00'),
    (630, 3, 210, 20, 0, 5, N'2026-03-15 10:00:00'),
    (631, 1, 211, 35, 0, 5, N'2026-03-15 10:00:00'),
    (632, 2, 211, 25, 0, 5, N'2026-03-15 10:00:00'),
    (633, 3, 211, 20, 0, 5, N'2026-03-15 10:00:00'),
    (634, 1, 212, 35, 0, 5, N'2026-03-15 10:00:00'),
    (635, 2, 212, 25, 0, 5, N'2026-03-15 10:00:00'),
    (636, 3, 212, 20, 0, 5, N'2026-03-15 10:00:00'),
    (637, 1, 213, 35, 0, 5, N'2026-03-15 10:00:00'),
    (638, 2, 213, 25, 0, 5, N'2026-03-15 10:00:00'),
    (639, 3, 213, 20, 0, 5, N'2026-03-15 10:00:00'),
    (640, 1, 214, 35, 0, 5, N'2026-03-15 10:00:00'),
    (641, 2, 214, 25, 0, 5, N'2026-03-15 10:00:00'),
    (642, 3, 214, 20, 0, 5, N'2026-03-15 10:00:00'),
    (643, 1, 215, 35, 0, 5, N'2026-03-15 10:00:00'),
    (644, 2, 215, 25, 0, 5, N'2026-03-15 10:00:00'),
    (645, 3, 215, 20, 0, 5, N'2026-03-15 10:00:00'),
    (646, 1, 216, 35, 0, 5, N'2026-03-15 10:00:00'),
    (647, 2, 216, 25, 0, 5, N'2026-03-15 10:00:00'),
    (648, 3, 216, 20, 0, 5, N'2026-03-15 10:00:00'),
    (649, 1, 217, 35, 0, 5, N'2026-03-15 10:00:00'),
    (650, 2, 217, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (651, 3, 217, 20, 0, 5, N'2026-03-15 10:00:00'),
    (652, 1, 218, 35, 0, 5, N'2026-03-15 10:00:00'),
    (653, 2, 218, 25, 0, 5, N'2026-03-15 10:00:00'),
    (654, 3, 218, 20, 0, 5, N'2026-03-15 10:00:00'),
    (655, 1, 219, 35, 0, 5, N'2026-03-15 10:00:00'),
    (656, 2, 219, 25, 0, 5, N'2026-03-15 10:00:00'),
    (657, 3, 219, 20, 0, 5, N'2026-03-15 10:00:00'),
    (658, 1, 220, 35, 0, 5, N'2026-03-15 10:00:00'),
    (659, 2, 220, 25, 0, 5, N'2026-03-15 10:00:00'),
    (660, 3, 220, 20, 0, 5, N'2026-03-15 10:00:00'),
    (661, 1, 221, 35, 0, 5, N'2026-03-15 10:00:00'),
    (662, 2, 221, 25, 0, 5, N'2026-03-15 10:00:00'),
    (663, 3, 221, 20, 0, 5, N'2026-03-15 10:00:00'),
    (664, 1, 222, 35, 0, 5, N'2026-03-15 10:00:00'),
    (665, 2, 222, 25, 0, 5, N'2026-03-15 10:00:00'),
    (666, 3, 222, 20, 0, 5, N'2026-03-15 10:00:00'),
    (667, 1, 223, 35, 0, 5, N'2026-03-15 10:00:00'),
    (668, 2, 223, 25, 0, 5, N'2026-03-15 10:00:00'),
    (669, 3, 223, 20, 0, 5, N'2026-03-15 10:00:00'),
    (670, 1, 224, 35, 0, 5, N'2026-03-15 10:00:00'),
    (671, 2, 224, 25, 0, 5, N'2026-03-15 10:00:00'),
    (672, 3, 224, 20, 0, 5, N'2026-03-15 10:00:00'),
    (673, 1, 225, 35, 0, 5, N'2026-03-15 10:00:00'),
    (674, 2, 225, 25, 0, 5, N'2026-03-15 10:00:00'),
    (675, 3, 225, 20, 0, 5, N'2026-03-15 10:00:00'),
    (676, 1, 226, 35, 0, 5, N'2026-03-15 10:00:00'),
    (677, 2, 226, 25, 0, 5, N'2026-03-15 10:00:00'),
    (678, 3, 226, 20, 0, 5, N'2026-03-15 10:00:00'),
    (679, 1, 227, 35, 0, 5, N'2026-03-15 10:00:00'),
    (680, 2, 227, 25, 0, 5, N'2026-03-15 10:00:00'),
    (681, 3, 227, 20, 0, 5, N'2026-03-15 10:00:00'),
    (682, 1, 228, 35, 0, 5, N'2026-03-15 10:00:00'),
    (683, 2, 228, 25, 0, 5, N'2026-03-15 10:00:00'),
    (684, 3, 228, 20, 0, 5, N'2026-03-15 10:00:00'),
    (685, 1, 229, 35, 0, 5, N'2026-03-15 10:00:00'),
    (686, 2, 229, 25, 0, 5, N'2026-03-15 10:00:00'),
    (687, 3, 229, 20, 0, 5, N'2026-03-15 10:00:00'),
    (688, 1, 230, 35, 0, 5, N'2026-03-15 10:00:00'),
    (689, 2, 230, 25, 0, 5, N'2026-03-15 10:00:00'),
    (690, 3, 230, 20, 0, 5, N'2026-03-15 10:00:00'),
    (691, 1, 231, 35, 0, 5, N'2026-03-15 10:00:00'),
    (692, 2, 231, 25, 0, 5, N'2026-03-15 10:00:00'),
    (693, 3, 231, 20, 0, 5, N'2026-03-15 10:00:00'),
    (694, 1, 232, 35, 0, 5, N'2026-03-15 10:00:00'),
    (695, 2, 232, 25, 0, 5, N'2026-03-15 10:00:00'),
    (696, 3, 232, 20, 0, 5, N'2026-03-15 10:00:00'),
    (697, 1, 233, 35, 0, 5, N'2026-03-15 10:00:00'),
    (698, 2, 233, 25, 0, 5, N'2026-03-15 10:00:00'),
    (699, 3, 233, 20, 0, 5, N'2026-03-15 10:00:00'),
    (700, 1, 234, 35, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (701, 2, 234, 25, 0, 5, N'2026-03-15 10:00:00'),
    (702, 3, 234, 20, 0, 5, N'2026-03-15 10:00:00'),
    (703, 1, 235, 35, 0, 5, N'2026-03-15 10:00:00'),
    (704, 2, 235, 25, 0, 5, N'2026-03-15 10:00:00'),
    (705, 3, 235, 20, 0, 5, N'2026-03-15 10:00:00'),
    (706, 1, 236, 35, 0, 5, N'2026-03-15 10:00:00'),
    (707, 2, 236, 25, 0, 5, N'2026-03-15 10:00:00'),
    (708, 3, 236, 20, 0, 5, N'2026-03-15 10:00:00'),
    (709, 1, 237, 35, 0, 5, N'2026-03-15 10:00:00'),
    (710, 2, 237, 25, 0, 5, N'2026-03-15 10:00:00'),
    (711, 3, 237, 20, 0, 5, N'2026-03-15 10:00:00'),
    (712, 1, 238, 35, 0, 5, N'2026-03-15 10:00:00'),
    (713, 2, 238, 25, 0, 5, N'2026-03-15 10:00:00'),
    (714, 3, 238, 20, 0, 5, N'2026-03-15 10:00:00'),
    (715, 1, 239, 35, 0, 5, N'2026-03-15 10:00:00'),
    (716, 2, 239, 25, 0, 5, N'2026-03-15 10:00:00'),
    (717, 3, 239, 20, 0, 5, N'2026-03-15 10:00:00'),
    (718, 1, 240, 35, 0, 5, N'2026-03-15 10:00:00'),
    (719, 2, 240, 25, 0, 5, N'2026-03-15 10:00:00'),
    (720, 3, 240, 20, 0, 5, N'2026-03-15 10:00:00'),
    (721, 1, 241, 35, 0, 5, N'2026-03-15 10:00:00'),
    (722, 2, 241, 25, 0, 5, N'2026-03-15 10:00:00'),
    (723, 3, 241, 20, 0, 5, N'2026-03-15 10:00:00'),
    (724, 1, 242, 35, 0, 5, N'2026-03-15 10:00:00'),
    (725, 2, 242, 25, 0, 5, N'2026-03-15 10:00:00'),
    (726, 3, 242, 20, 0, 5, N'2026-03-15 10:00:00'),
    (727, 1, 243, 35, 0, 5, N'2026-03-15 10:00:00'),
    (728, 2, 243, 25, 0, 5, N'2026-03-15 10:00:00'),
    (729, 3, 243, 20, 0, 5, N'2026-03-15 10:00:00'),
    (730, 1, 244, 35, 0, 5, N'2026-03-15 10:00:00'),
    (731, 2, 244, 25, 0, 5, N'2026-03-15 10:00:00'),
    (732, 3, 244, 20, 0, 5, N'2026-03-15 10:00:00'),
    (733, 1, 245, 35, 0, 5, N'2026-03-15 10:00:00'),
    (734, 2, 245, 25, 0, 5, N'2026-03-15 10:00:00'),
    (735, 3, 245, 20, 0, 5, N'2026-03-15 10:00:00'),
    (736, 1, 246, 35, 0, 5, N'2026-03-15 10:00:00'),
    (737, 2, 246, 25, 0, 5, N'2026-03-15 10:00:00'),
    (738, 3, 246, 20, 0, 5, N'2026-03-15 10:00:00'),
    (739, 1, 247, 35, 0, 5, N'2026-03-15 10:00:00'),
    (740, 2, 247, 25, 0, 5, N'2026-03-15 10:00:00'),
    (741, 3, 247, 20, 0, 5, N'2026-03-15 10:00:00'),
    (742, 1, 248, 35, 0, 5, N'2026-03-15 10:00:00'),
    (743, 2, 248, 25, 0, 5, N'2026-03-15 10:00:00'),
    (744, 3, 248, 20, 0, 5, N'2026-03-15 10:00:00'),
    (745, 1, 249, 35, 0, 5, N'2026-03-15 10:00:00'),
    (746, 2, 249, 25, 0, 5, N'2026-03-15 10:00:00'),
    (747, 3, 249, 20, 0, 5, N'2026-03-15 10:00:00'),
    (748, 1, 250, 35, 0, 5, N'2026-03-15 10:00:00'),
    (749, 2, 250, 25, 0, 5, N'2026-03-15 10:00:00'),
    (750, 3, 250, 20, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (751, 1, 251, 35, 0, 5, N'2026-03-15 10:00:00'),
    (752, 2, 251, 25, 0, 5, N'2026-03-15 10:00:00'),
    (753, 3, 251, 20, 0, 5, N'2026-03-15 10:00:00'),
    (754, 1, 252, 35, 0, 5, N'2026-03-15 10:00:00'),
    (755, 2, 252, 25, 0, 5, N'2026-03-15 10:00:00'),
    (756, 3, 252, 20, 0, 5, N'2026-03-15 10:00:00'),
    (757, 1, 253, 35, 0, 5, N'2026-03-15 10:00:00'),
    (758, 2, 253, 25, 0, 5, N'2026-03-15 10:00:00'),
    (759, 3, 253, 20, 0, 5, N'2026-03-15 10:00:00'),
    (760, 1, 254, 35, 0, 5, N'2026-03-15 10:00:00'),
    (761, 2, 254, 25, 0, 5, N'2026-03-15 10:00:00'),
    (762, 3, 254, 20, 0, 5, N'2026-03-15 10:00:00'),
    (763, 1, 255, 35, 0, 5, N'2026-03-15 10:00:00'),
    (764, 2, 255, 25, 0, 5, N'2026-03-15 10:00:00'),
    (765, 3, 255, 20, 0, 5, N'2026-03-15 10:00:00'),
    (766, 1, 256, 35, 0, 5, N'2026-03-15 10:00:00'),
    (767, 2, 256, 25, 0, 5, N'2026-03-15 10:00:00'),
    (768, 3, 256, 20, 0, 5, N'2026-03-15 10:00:00'),
    (769, 1, 257, 35, 0, 5, N'2026-03-15 10:00:00'),
    (770, 2, 257, 25, 0, 5, N'2026-03-15 10:00:00'),
    (771, 3, 257, 20, 0, 5, N'2026-03-15 10:00:00'),
    (772, 1, 258, 35, 0, 5, N'2026-03-15 10:00:00'),
    (773, 2, 258, 25, 0, 5, N'2026-03-15 10:00:00'),
    (774, 3, 258, 20, 0, 5, N'2026-03-15 10:00:00'),
    (775, 1, 259, 35, 0, 5, N'2026-03-15 10:00:00'),
    (776, 2, 259, 25, 0, 5, N'2026-03-15 10:00:00'),
    (777, 3, 259, 20, 0, 5, N'2026-03-15 10:00:00'),
    (778, 1, 260, 35, 0, 5, N'2026-03-15 10:00:00'),
    (779, 2, 260, 25, 0, 5, N'2026-03-15 10:00:00'),
    (780, 3, 260, 20, 0, 5, N'2026-03-15 10:00:00'),
    (781, 1, 261, 35, 0, 5, N'2026-03-15 10:00:00'),
    (782, 2, 261, 25, 0, 5, N'2026-03-15 10:00:00'),
    (783, 3, 261, 20, 0, 5, N'2026-03-15 10:00:00'),
    (784, 1, 262, 35, 0, 5, N'2026-03-15 10:00:00'),
    (785, 2, 262, 25, 0, 5, N'2026-03-15 10:00:00'),
    (786, 3, 262, 20, 0, 5, N'2026-03-15 10:00:00'),
    (787, 1, 263, 35, 0, 5, N'2026-03-15 10:00:00'),
    (788, 2, 263, 25, 0, 5, N'2026-03-15 10:00:00'),
    (789, 3, 263, 20, 0, 5, N'2026-03-15 10:00:00'),
    (790, 1, 264, 35, 0, 5, N'2026-03-15 10:00:00'),
    (791, 2, 264, 25, 0, 5, N'2026-03-15 10:00:00'),
    (792, 3, 264, 20, 0, 5, N'2026-03-15 10:00:00'),
    (793, 1, 265, 35, 0, 5, N'2026-03-15 10:00:00'),
    (794, 2, 265, 25, 0, 5, N'2026-03-15 10:00:00'),
    (795, 3, 265, 20, 0, 5, N'2026-03-15 10:00:00'),
    (796, 1, 266, 35, 0, 5, N'2026-03-15 10:00:00'),
    (797, 2, 266, 25, 0, 5, N'2026-03-15 10:00:00'),
    (798, 3, 266, 20, 0, 5, N'2026-03-15 10:00:00'),
    (799, 1, 267, 35, 0, 5, N'2026-03-15 10:00:00'),
    (800, 2, 267, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (801, 3, 267, 20, 0, 5, N'2026-03-15 10:00:00'),
    (802, 1, 268, 35, 0, 5, N'2026-03-15 10:00:00'),
    (803, 2, 268, 25, 0, 5, N'2026-03-15 10:00:00'),
    (804, 3, 268, 20, 0, 5, N'2026-03-15 10:00:00'),
    (805, 1, 269, 35, 0, 5, N'2026-03-15 10:00:00'),
    (806, 2, 269, 25, 0, 5, N'2026-03-15 10:00:00'),
    (807, 3, 269, 20, 0, 5, N'2026-03-15 10:00:00'),
    (808, 1, 270, 35, 0, 5, N'2026-03-15 10:00:00'),
    (809, 2, 270, 25, 0, 5, N'2026-03-15 10:00:00'),
    (810, 3, 270, 20, 0, 5, N'2026-03-15 10:00:00'),
    (811, 1, 271, 35, 0, 5, N'2026-03-15 10:00:00'),
    (812, 2, 271, 25, 0, 5, N'2026-03-15 10:00:00'),
    (813, 3, 271, 20, 0, 5, N'2026-03-15 10:00:00'),
    (814, 1, 272, 35, 0, 5, N'2026-03-15 10:00:00'),
    (815, 2, 272, 25, 0, 5, N'2026-03-15 10:00:00'),
    (816, 3, 272, 20, 0, 5, N'2026-03-15 10:00:00'),
    (817, 1, 273, 35, 0, 5, N'2026-03-15 10:00:00'),
    (818, 2, 273, 25, 0, 5, N'2026-03-15 10:00:00'),
    (819, 3, 273, 20, 0, 5, N'2026-03-15 10:00:00'),
    (820, 1, 274, 35, 0, 5, N'2026-03-15 10:00:00'),
    (821, 2, 274, 25, 0, 5, N'2026-03-15 10:00:00'),
    (822, 3, 274, 20, 0, 5, N'2026-03-15 10:00:00'),
    (823, 1, 275, 35, 0, 5, N'2026-03-15 10:00:00'),
    (824, 2, 275, 25, 0, 5, N'2026-03-15 10:00:00'),
    (825, 3, 275, 20, 0, 5, N'2026-03-15 10:00:00'),
    (826, 1, 276, 35, 0, 5, N'2026-03-15 10:00:00'),
    (827, 2, 276, 25, 0, 5, N'2026-03-15 10:00:00'),
    (828, 3, 276, 20, 0, 5, N'2026-03-15 10:00:00'),
    (829, 1, 277, 35, 0, 5, N'2026-03-15 10:00:00'),
    (830, 2, 277, 25, 0, 5, N'2026-03-15 10:00:00'),
    (831, 3, 277, 20, 0, 5, N'2026-03-15 10:00:00'),
    (832, 1, 278, 35, 0, 5, N'2026-03-15 10:00:00'),
    (833, 2, 278, 25, 0, 5, N'2026-03-15 10:00:00'),
    (834, 3, 278, 20, 0, 5, N'2026-03-15 10:00:00'),
    (835, 1, 279, 35, 0, 5, N'2026-03-15 10:00:00'),
    (836, 2, 279, 25, 0, 5, N'2026-03-15 10:00:00'),
    (837, 3, 279, 20, 0, 5, N'2026-03-15 10:00:00'),
    (838, 1, 280, 35, 0, 5, N'2026-03-15 10:00:00'),
    (839, 2, 280, 25, 0, 5, N'2026-03-15 10:00:00'),
    (840, 3, 280, 20, 0, 5, N'2026-03-15 10:00:00'),
    (841, 1, 281, 35, 0, 5, N'2026-03-15 10:00:00'),
    (842, 2, 281, 25, 0, 5, N'2026-03-15 10:00:00'),
    (843, 3, 281, 20, 0, 5, N'2026-03-15 10:00:00'),
    (844, 1, 282, 35, 0, 5, N'2026-03-15 10:00:00'),
    (845, 2, 282, 25, 0, 5, N'2026-03-15 10:00:00'),
    (846, 3, 282, 20, 0, 5, N'2026-03-15 10:00:00'),
    (847, 1, 283, 35, 0, 5, N'2026-03-15 10:00:00'),
    (848, 2, 283, 25, 0, 5, N'2026-03-15 10:00:00'),
    (849, 3, 283, 20, 0, 5, N'2026-03-15 10:00:00'),
    (850, 1, 284, 35, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (851, 2, 284, 25, 0, 5, N'2026-03-15 10:00:00'),
    (852, 3, 284, 20, 0, 5, N'2026-03-15 10:00:00'),
    (853, 1, 285, 35, 0, 5, N'2026-03-15 10:00:00'),
    (854, 2, 285, 25, 0, 5, N'2026-03-15 10:00:00'),
    (855, 3, 285, 20, 0, 5, N'2026-03-15 10:00:00'),
    (856, 1, 286, 35, 0, 5, N'2026-03-15 10:00:00'),
    (857, 2, 286, 25, 0, 5, N'2026-03-15 10:00:00'),
    (858, 3, 286, 20, 0, 5, N'2026-03-15 10:00:00'),
    (859, 1, 287, 35, 0, 5, N'2026-03-15 10:00:00'),
    (860, 2, 287, 25, 0, 5, N'2026-03-15 10:00:00'),
    (861, 3, 287, 20, 0, 5, N'2026-03-15 10:00:00'),
    (862, 1, 288, 35, 0, 5, N'2026-03-15 10:00:00'),
    (863, 2, 288, 25, 0, 5, N'2026-03-15 10:00:00'),
    (864, 3, 288, 20, 0, 5, N'2026-03-15 10:00:00'),
    (865, 1, 289, 35, 0, 5, N'2026-03-15 10:00:00'),
    (866, 2, 289, 25, 0, 5, N'2026-03-15 10:00:00'),
    (867, 3, 289, 20, 0, 5, N'2026-03-15 10:00:00'),
    (868, 1, 290, 35, 0, 5, N'2026-03-15 10:00:00'),
    (869, 2, 290, 25, 0, 5, N'2026-03-15 10:00:00'),
    (870, 3, 290, 20, 0, 5, N'2026-03-15 10:00:00'),
    (871, 1, 291, 35, 0, 5, N'2026-03-15 10:00:00'),
    (872, 2, 291, 25, 0, 5, N'2026-03-15 10:00:00'),
    (873, 3, 291, 20, 0, 5, N'2026-03-15 10:00:00'),
    (874, 1, 292, 35, 0, 5, N'2026-03-15 10:00:00'),
    (875, 2, 292, 25, 0, 5, N'2026-03-15 10:00:00'),
    (876, 3, 292, 20, 0, 5, N'2026-03-15 10:00:00'),
    (877, 1, 293, 35, 0, 5, N'2026-03-15 10:00:00'),
    (878, 2, 293, 25, 0, 5, N'2026-03-15 10:00:00'),
    (879, 3, 293, 20, 0, 5, N'2026-03-15 10:00:00'),
    (880, 1, 294, 35, 0, 5, N'2026-03-15 10:00:00'),
    (881, 2, 294, 25, 0, 5, N'2026-03-15 10:00:00'),
    (882, 3, 294, 20, 0, 5, N'2026-03-15 10:00:00'),
    (883, 1, 295, 35, 0, 5, N'2026-03-15 10:00:00'),
    (884, 2, 295, 25, 0, 5, N'2026-03-15 10:00:00'),
    (885, 3, 295, 20, 0, 5, N'2026-03-15 10:00:00'),
    (886, 1, 296, 35, 0, 5, N'2026-03-15 10:00:00'),
    (887, 2, 296, 25, 0, 5, N'2026-03-15 10:00:00'),
    (888, 3, 296, 20, 0, 5, N'2026-03-15 10:00:00'),
    (889, 1, 297, 35, 0, 5, N'2026-03-15 10:00:00'),
    (890, 2, 297, 25, 0, 5, N'2026-03-15 10:00:00'),
    (891, 3, 297, 20, 0, 5, N'2026-03-15 10:00:00'),
    (892, 1, 298, 35, 0, 5, N'2026-03-15 10:00:00'),
    (893, 2, 298, 25, 0, 5, N'2026-03-15 10:00:00'),
    (894, 3, 298, 20, 0, 5, N'2026-03-15 10:00:00'),
    (895, 1, 299, 35, 0, 5, N'2026-03-15 10:00:00'),
    (896, 2, 299, 25, 0, 5, N'2026-03-15 10:00:00'),
    (897, 3, 299, 20, 0, 5, N'2026-03-15 10:00:00'),
    (898, 1, 300, 35, 0, 5, N'2026-03-15 10:00:00'),
    (899, 2, 300, 25, 0, 5, N'2026-03-15 10:00:00'),
    (900, 3, 300, 20, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (901, 1, 301, 35, 0, 5, N'2026-03-15 10:00:00'),
    (902, 2, 301, 25, 0, 5, N'2026-03-15 10:00:00'),
    (903, 3, 301, 20, 0, 5, N'2026-03-15 10:00:00'),
    (904, 1, 302, 35, 0, 5, N'2026-03-15 10:00:00'),
    (905, 2, 302, 25, 0, 5, N'2026-03-15 10:00:00'),
    (906, 3, 302, 20, 0, 5, N'2026-03-15 10:00:00'),
    (907, 1, 303, 35, 0, 5, N'2026-03-15 10:00:00'),
    (908, 2, 303, 25, 0, 5, N'2026-03-15 10:00:00'),
    (909, 3, 303, 20, 0, 5, N'2026-03-15 10:00:00'),
    (910, 1, 304, 35, 0, 5, N'2026-03-15 10:00:00'),
    (911, 2, 304, 25, 0, 5, N'2026-03-15 10:00:00'),
    (912, 3, 304, 20, 0, 5, N'2026-03-15 10:00:00'),
    (913, 1, 305, 35, 0, 5, N'2026-03-15 10:00:00'),
    (914, 2, 305, 25, 0, 5, N'2026-03-15 10:00:00'),
    (915, 3, 305, 20, 0, 5, N'2026-03-15 10:00:00'),
    (916, 1, 306, 35, 0, 5, N'2026-03-15 10:00:00'),
    (917, 2, 306, 25, 0, 5, N'2026-03-15 10:00:00'),
    (918, 3, 306, 20, 0, 5, N'2026-03-15 10:00:00'),
    (919, 1, 307, 35, 0, 5, N'2026-03-15 10:00:00'),
    (920, 2, 307, 25, 0, 5, N'2026-03-15 10:00:00'),
    (921, 3, 307, 20, 0, 5, N'2026-03-15 10:00:00'),
    (922, 1, 308, 35, 0, 5, N'2026-03-15 10:00:00'),
    (923, 2, 308, 25, 0, 5, N'2026-03-15 10:00:00'),
    (924, 3, 308, 20, 0, 5, N'2026-03-15 10:00:00'),
    (925, 1, 309, 35, 0, 5, N'2026-03-15 10:00:00'),
    (926, 2, 309, 25, 0, 5, N'2026-03-15 10:00:00'),
    (927, 3, 309, 20, 0, 5, N'2026-03-15 10:00:00'),
    (928, 1, 310, 35, 0, 5, N'2026-03-15 10:00:00'),
    (929, 2, 310, 25, 0, 5, N'2026-03-15 10:00:00'),
    (930, 3, 310, 20, 0, 5, N'2026-03-15 10:00:00'),
    (931, 1, 311, 35, 0, 5, N'2026-03-15 10:00:00'),
    (932, 2, 311, 25, 0, 5, N'2026-03-15 10:00:00'),
    (933, 3, 311, 20, 0, 5, N'2026-03-15 10:00:00'),
    (934, 1, 312, 35, 0, 5, N'2026-03-15 10:00:00'),
    (935, 2, 312, 25, 0, 5, N'2026-03-15 10:00:00'),
    (936, 3, 312, 20, 0, 5, N'2026-03-15 10:00:00'),
    (937, 1, 313, 35, 0, 5, N'2026-03-15 10:00:00'),
    (938, 2, 313, 25, 0, 5, N'2026-03-15 10:00:00'),
    (939, 3, 313, 20, 0, 5, N'2026-03-15 10:00:00'),
    (940, 1, 314, 35, 0, 5, N'2026-03-15 10:00:00'),
    (941, 2, 314, 25, 0, 5, N'2026-03-15 10:00:00'),
    (942, 3, 314, 20, 0, 5, N'2026-03-15 10:00:00'),
    (943, 1, 315, 35, 0, 5, N'2026-03-15 10:00:00'),
    (944, 2, 315, 25, 0, 5, N'2026-03-15 10:00:00'),
    (945, 3, 315, 20, 0, 5, N'2026-03-15 10:00:00'),
    (946, 1, 316, 35, 0, 5, N'2026-03-15 10:00:00'),
    (947, 2, 316, 25, 0, 5, N'2026-03-15 10:00:00'),
    (948, 3, 316, 20, 0, 5, N'2026-03-15 10:00:00'),
    (949, 1, 317, 35, 0, 5, N'2026-03-15 10:00:00'),
    (950, 2, 317, 25, 0, 5, N'2026-03-15 10:00:00');
GO

INSERT INTO dbo.inventories (id, store_id, sku_id, on_hand_qty, reserved_qty, low_stock_threshold, updated_at) VALUES
    (951, 3, 317, 20, 0, 5, N'2026-03-15 10:00:00'),
    (952, 1, 318, 35, 0, 5, N'2026-03-15 10:00:00'),
    (953, 2, 318, 25, 0, 5, N'2026-03-15 10:00:00'),
    (954, 3, 318, 20, 0, 5, N'2026-03-15 10:00:00');
GO

SET IDENTITY_INSERT dbo.inventories OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: VOUCHERS (5 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.vouchers ON;
GO

INSERT INTO dbo.vouchers (id, code, name, description, discount_type, discount_value, max_discount_amount, min_order_amount, valid_from, valid_to, usage_limit, used_count, status) VALUES
    (1, N'LUNEA10', N'Giảm 10% Cho Đơn Hàng Đầu Tiên', N'Áp dụng giảm 10% tối đa 50.000đ cho đơn hàng giá trị từ 200.000đ', N'PERCENTAGE', 10.0, 50000.0, 200000.0, N'2025-01-01 00:00:00', N'2027-12-31 23:59:59', 5000, 128, N'ACTIVE'),
    (2, N'FREESHIP', N'Miễn Phí Vận Chuyển Toàn Quốc', N'Miễn phí vận chuyển tối đa 30.000đ cho đơn hàng giá trị từ 300.000đ', N'FREE_SHIPPING', 30000.0, 30000.0, 300000.0, N'2025-01-01 00:00:00', N'2027-12-31 23:59:59', 10000, 452, N'ACTIVE'),
    (3, N'CHAOBANMOI', N'Voucher Chào Bạn Mới 50K', N'Giảm ngay 50.000đ tiền mặt cho đơn hàng đầu tiên từ 250.000đ', N'FIXED_AMOUNT', 50000.0, 50000.0, 250000.0, N'2025-01-01 00:00:00', N'2027-12-31 23:59:59', 2000, 310, N'ACTIVE'),
    (4, N'VIPCARE', N'Đặc Quyền Thành Viên VIP LUNEA', N'Giảm 15% tối đa 150.000đ cho đơn hàng mỹ phẩm chăm sóc da từ 600.000đ', N'PERCENTAGE', 15.0, 150000.0, 600000.0, N'2025-01-01 00:00:00', N'2027-12-31 23:59:59', 1000, 85, N'ACTIVE'),
    (5, N'LUNEASUMMER', N'Ưu Đãi Lễ Hội Mùa Hè Rực Rỡ', N'Giảm 20% tối đa 100.000đ cho tất cả dòng kem chống nắng và dưỡng ẩm', N'PERCENTAGE', 20.0, 100000.0, 400000.0, N'2025-01-01 00:00:00', N'2027-12-31 23:59:59', 3000, 219, N'ACTIVE');
GO

SET IDENTITY_INSERT dbo.vouchers OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: CARTS (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.carts ON;
GO

INSERT INTO dbo.carts (id, customer_id, updated_at) VALUES
    (1, 1, N'2026-03-20 14:00:00'),
    (2, 2, N'2026-03-20 15:30:00');
GO

SET IDENTITY_INSERT dbo.carts OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: CART_ITEMS (3 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.cart_items ON;
GO

INSERT INTO dbo.cart_items (id, cart_id, sku_id, quantity) VALUES
    (1, 1, 1, 2),
    (2, 1, 3, 1),
    (3, 2, 7, 1);
GO

SET IDENTITY_INSERT dbo.cart_items OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: ORDERS (3 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.orders ON;
GO

INSERT INTO dbo.orders (id, order_code, customer_id, fulfillment_store_id, receiver_name, receiver_phone, shipping_address, ward, district, province, delivery_method, subtotal, voucher_id, discount_amount, shipping_fee, total_amount, status, payment_method, note, created_at, confirmed_at, completed_at, cancelled_at) VALUES
    (1, N'ORD-20260301-0001', 1, 1, N'Lê Hoàng Yến', N'0901234567', N'Số 45 Đường Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'Phường Bến Nghé', N'Quận 1', N'Hồ Chí Minh', N'HOME_DELIVERY', 750000.0, 1, 50000.0, 0.0, 700000.0, N'DELIVERED', N'COD', N'Giao hàng giờ hành chính giúp em', N'2026-03-01 09:15:00', N'2026-03-01 09:30:00', N'2026-03-03 14:20:00', NULL),
    (2, N'ORD-20260305-0002', 2, 2, N'Nguyễn Tuấn Anh', N'0909876543', N'Số 12 Ngõ 88 Phố Huế, Phường Hàng Bài, Quận Hoàn Kiếm, Hà Nội', N'Phường Hàng Bài', N'Quận Hoàn Kiếm', N'Hà Nội', N'HOME_DELIVERY', 615000.0, 2, 30000.0, 0.0, 585000.0, N'DELIVERED', N'BANK_TRANSFER', N'Giao hàng cẩn thận dễ vỡ', N'2026-03-05 11:20:00', N'2026-03-05 11:45:00', N'2026-03-07 16:10:00', NULL),
    (3, N'ORD-20260310-0003', 1, 1, N'Lê Hoàng Yến', N'0901234567', N'Số 45 Đường Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'Phường Bến Nghé', N'Quận 1', N'Hồ Chí Minh', N'HOME_DELIVERY', 845000.0, NULL, 0.0, 25000.0, 870000.0, N'CONFIRMED', N'COD', N'Vui lòng gọi trước khi giao', N'2026-03-10 14:10:00', N'2026-03-10 14:35:00', NULL, NULL);
GO

SET IDENTITY_INSERT dbo.orders OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: ORDER_ITEMS (5 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.order_items ON;
GO

INSERT INTO dbo.order_items (id, order_id, sku_id, product_name, variant_name, unit_price, quantity, line_total) VALUES
    (1, 1, 1, N'Serum Bí Đao Cocoon 7% Niacinamide Trị Mụn & Mờ Thâm', N'Dung tích 70ml', 265000.0, 2, 530000.0),
    (2, 1, 3, N'Tinh Chất The Ordinary Niacinamide 10% + Zinc 1%', N'Dung tích 30ml', 220000.0, 1, 220000.0),
    (3, 2, 85, N'Sữa Chống Nắng Dưỡng Da Kiềm Dầu Anessa Perfect UV Sunscreen Skincare Milk SPF50+', N'Chai 60ml Fullsize', 615000.0, 1, 615000.0),
    (4, 3, 23, N'Kem Dưỡng Ẩm Phục Hồi Da La Roche-Posay Cicaplast Baume B5+', N'Dung tích 40ml', 365000.0, 1, 365000.0),
    (5, 3, 46, N'Kem Chống Nắng Kiềm Dầu La Roche-Posay Anthelios UVMune 400 Oil Control', N'Tuýp 50ml', 480000.0, 1, 480000.0);
GO

SET IDENTITY_INSERT dbo.order_items OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: REVIEWS (3 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.reviews ON;
GO

INSERT INTO dbo.reviews (id, customer_id, product_id, order_id, order_item_id, rating, content, moderation_status, created_at) VALUES
    (1, 1, 1, 1, 1, 5, N'Serum dùng siêu thích luôn, mình dùng hơn 2 tuần thấy nốt mụn khô còi nhanh và bớt bóng dầu hẳn. Hàng chính hãng LUNEA đóng gói rất kỹ!', N'APPROVED', N'2026-03-04 10:00:00'),
    (2, 1, 2, 1, 2, 5, N'Niacinamide The Ordinary kiềm dầu tốt, lỗ chân lông 2 bên má nhìn gọn gàng hơn rõ rệt. Rất hài lòng với dịch vụ giao hàng của shop.', N'APPROVED', N'2026-03-04 10:15:00'),
    (3, 2, 47, 2, 3, 5, N'Kem chống nắng Anessa chuẩn số 1, thấm nhanh ráo mịn đi đường nắng gắt không bị rát da mặt. 10 điểm cho LUNEA!', N'APPROVED', N'2026-03-08 09:30:00');
GO

SET IDENTITY_INSERT dbo.reviews OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: PURCHASE_ORDERS (1 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.purchase_orders ON;
GO

INSERT INTO dbo.purchase_orders (id, supplier_id, receiving_store_id, status, created_by, total_amount, created_at, confirmed_at) VALUES
    (1, 2, 1, N'COMPLETED', 4, 25000000.0, N'2026-02-15 08:30:00', N'2026-02-16 09:00:00');
GO

SET IDENTITY_INSERT dbo.purchase_orders OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: PURCHASE_ORDER_ITEMS (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.purchase_order_items ON;
GO

INSERT INTO dbo.purchase_order_items (id, purchase_order_id, sku_id, quantity, unit_cost, line_total) VALUES
    (1, 1, 1, 100, 180000.0, 18000000.0),
    (2, 1, 2, 50, 140000.0, 7000000.0);
GO

SET IDENTITY_INSERT dbo.purchase_order_items OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: STOCK_TRANSFERS (1 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.stock_transfers ON;
GO

INSERT INTO dbo.stock_transfers (id, from_store_id, to_store_id, status, employee_id, created_at, shipped_at, received_at) VALUES
    (1, 1, 2, N'COMPLETED', 4, N'2026-02-20 09:00:00', N'2026-02-20 14:00:00', N'2026-02-22 10:30:00');
GO

SET IDENTITY_INSERT dbo.stock_transfers OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: STOCK_TRANSFER_ITEMS (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.stock_transfer_items ON;
GO

INSERT INTO dbo.stock_transfer_items (id, stock_transfer_id, sku_id, quantity) VALUES
    (1, 1, 1, 30),
    (2, 1, 3, 20);
GO

SET IDENTITY_INSERT dbo.stock_transfer_items OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: WISHLISTS (2 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.wishlists ON;
GO

INSERT INTO dbo.wishlists (id, customer_id, created_at) VALUES
    (1, 1, N'2026-02-01 10:00:00'),
    (2, 2, N'2026-02-05 11:30:00');
GO

SET IDENTITY_INSERT dbo.wishlists OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: WISHLIST_ITEMS (3 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.wishlist_items ON;
GO

INSERT INTO dbo.wishlist_items (id, wishlist_id, product_id, added_at) VALUES
    (1, 1, 3, N'2026-02-01 10:05:00'),
    (2, 1, 46, N'2026-02-02 14:20:00'),
    (3, 2, 211, N'2026-02-05 11:35:00');
GO

SET IDENTITY_INSERT dbo.wishlist_items OFF;
GO

-- ---------------------------------------------------------
-- NẠP DỮ LIỆU CHO BẢNG: AUDIT_LOGS (3 bản ghi)
-- ---------------------------------------------------------
SET IDENTITY_INSERT dbo.audit_logs ON;
GO

INSERT INTO dbo.audit_logs (id, employee_id, action, target_type, target_id, content, ip_address, created_at) VALUES
    (1, 1, N'LOGIN', N'ACCOUNT', N'1', N'Admin đăng nhập thành công vào trang quản trị hệ thống', N'127.0.0.1', N'2026-03-20 08:00:00'),
    (2, 2, N'CONFIRM_ORDER', N'ORDER', N'1', N'Xác nhận đơn hàng ORD-20260301-0001', N'127.0.0.1', N'2026-03-01 09:30:00'),
    (3, 4, N'STOCK_TRANSFER', N'TRANSFER', N'1', N'Xuất kho điều chuyển 50 sản phẩm từ HCM ra Hà Nội', N'127.0.0.1', N'2026-02-20 14:00:00');
GO

SET IDENTITY_INSERT dbo.audit_logs OFF;
GO

PRINT N'>>> [LUNEA DML] Đã nạp thành công toàn bộ dữ liệu mẫu vào Microsoft SQL Server!';
GO
