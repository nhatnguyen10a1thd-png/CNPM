# 01 — TỔNG QUAN KIẾN TRÚC VÀ NGHIỆP VỤ DỰ ÁN LUNEA

> **Tên dự án:** LUNEA — Website bán mỹ phẩm và quản lý chuỗi showroom  
> **Khách hàng / Nghiệp vụ:** Thương mại điện tử B2C đa kênh kết hợp quản lý chuỗi cửa hàng vật lý tập trung  
> **Ngôn ngữ & Nền tảng:** Java 21 LTS, Spring Boot 4.1.1 (Spring Framework 6), Maven  
> **Cơ sở dữ liệu:** Microsoft SQL Server (Local development) / PostgreSQL (Cloud production)  
> **Kiến trúc phần mềm:** Layered Architecture (MVC + REST API), Stateless Security với JWT & HttpOnly Cookie, Multi-store Data Isolation  

---

## 1. MÔ HÌNH NGHIỆP VỤ CỐT LÕI CỦA LUNEA

LUNEA giải quyết bài toán vận hành thực tế của một chuỗi bán lẻ mỹ phẩm cao cấp:
1. **Quản lý danh mục & sản phẩm tập trung:** Danh mục (`Category`), Thương hiệu (`Brand`), Thông tin sản phẩm (`Product`) và Biến thể (`ProductSku`: 30ml, 50ml, màu son, mã vạch) được quản trị tập trung bởi nhân viên QLSP.
2. **Quản lý tồn kho phân tán theo chi nhánh (`Store`):** Tồn kho được quản lý theo từng cặp `(cua_hang_id, sku_id)` trong bảng `TonKho`. Mỗi showroom có lượng tồn thực tế riêng biệt.
3. **Bất biến Tồn Khả Dụng (Available Stock Formula):**
   $$\text{Tồn khả dụng (Available Stock)} = \text{actual\_stock (Tồn thực tế)} - \text{held\_quantity (Đang tạm giữ)}$$
   - Khi khách đặt hàng thành công: Hệ thống sinh bản ghi `GiuTonDonHang` (`OrderReservation`), tăng `held_quantity` tại chi nhánh tương ứng.
   - Khi nhân viên xuất hàng đóng gói / giao thành công: Giảm cả `actual_stock` và `held_quantity`.
   - Khi đơn bị hủy: Giảm `held_quantity` để nhả tồn kho cho khách khác mua.
4. **Thuật toán Tự động chọn Chi nhánh gần nhất có đủ hàng:** Khi khách đặt đơn giao tận nơi (DELIVERY), hệ thống tự động quét tọa độ địa lý (Haversine Formula) hoặc độ ưu tiên chi nhánh để tìm cửa hàng gần địa chỉ khách nhất có đủ toàn bộ số lượng các SKU trong đơn.
5. **Snapshot giá và tên sản phẩm trong Đơn hàng:** `ChiTietDonHang` (`OrderItem`) luôn lưu bản sao bất biến của giá bán, tên sản phẩm và tên biến thể tại thời điểm khách bấm đặt hàng. Nếu sau này sản phẩm tăng/giảm giá hoặc đổi tên, lịch sử hóa đơn trong quá khứ không bị sai lệch doanh thu.

---

## 2. KIẾN TRÚC HỆ THỐNG PHÂN TẦNG (LAYERED ARCHITECTURE)

Dự án tuân thủ nghiêm ngặt nguyên lý thiết kế Spring Boot nhiều tầng:

```
+-----------------------------------------------------------------------------------+
|                            CLIENT / FRONTEND                                      |
|  - Trình duyệt Web (Thymeleaf Server-Side Rendering HTML5 + tokens.css + site.css)|
|  - JavaScript Asynchronous API Fetch Client (static/js/api.js)                    |
+-----------------------------------------------------------------------------------+
                                         │  (HTTP / HTTPS Requests)
                                         ▼
+-----------------------------------------------------------------------------------+
|                        SPRING SECURITY FILTER CHAIN                               |
|  - CookieBearerTokenResolver: Trích xuất JWT từ Cookie 'LUNEA_AT' hoặc Header     |
|  - JwtAuthenticationFilter / JwtAuthConverter: Validate chữ ký HS256, nạp Claims  |
|  - StoreScopeInterceptor: Nạp thông tin Chi nhánh của Nhân viên vào ThreadLocal    |
|  - SecurityConfig: Phân quyền theo ROLE_ (ADMIN, SALES, STORE_MANAGER) & PERM_    |
+-----------------------------------------------------------------------------------+
                                         │
                    ┌────────────────────┴────────────────────┐
                    ▼                                         ▼
+---------------------------------------+ +-----------------------------------------+
|      CONTROLLER LAYER (WEB MVC)       | |       REST CONTROLLER LAYER (API)       |
|  - AccountPageController              | |  - AuthRestController, AccountRestCtrl  |
|  - ProductPageController              | |  - ProductRestController, OrderRestCtrl |
|  - CartPageController, CheckoutPage   | |  - InventoryRestController, StoreRestCtrl|
|  - AdminPageController                | |  - VoucherRestController, ReviewRestCtrl|
|  => Trả về Template HTML (Thymeleaf)   | |  => Trả về JSON (ResponseEntity<ApiResponse>)|
+---------------------------------------+ +-----------------------------------------+
                    │                                         │
                    └────────────────────┬────────────────────┘
                                         ▼
+-----------------------------------------------------------------------------------+
|                            SERVICE LAYER (BUSINESS LOGIC)                         |
|  - OrderServiceImpl: Validate giỏ, kiểm tra tồn, giữ tồn kho, áp voucher, chốt đơn|
|  - ProductServiceImpl: Tìm kiếm chuẩn hóa tiếng Việt không dấu, lọc theo loại da  |
|  - InventoryServiceImpl / StockTransferServiceImpl: Điều chuyển hàng giữa các kho |
|  - OtpServiceImpl: Sinh mã ngẫu nhiên SecureRandom, kiểm soát TTL 5p & chống brute|
|  - EmailServiceImpl: Tích hợp Brevo REST API / Spring Mail gửi OTP bất đồng bộ    |
+-----------------------------------------------------------------------------------+
                                         │
                    ┌────────────────────┴────────────────────┐
                    ▼                                         ▼
+---------------------------------------+ +-----------------------------------------+
|      DATA ACCESS LAYER (REPOSITORY)   | |         DATA TRANSFER & MAPPING         |
|  - Spring Data JPA Interfaces         | |  - MapStruct Mappers (compile-time)     |
|  - Derived Query Methods              | |  - Request DTOs (Jakarta Validation)    |
|  - Custom JPQL Queries (@Query)       | |  - Response DTOs (Safe payload to client)|
+---------------------------------------+ +-----------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                       PERSISTENCE LAYER (JPA / HIBERNATE)                         |
|  - 43 JPA Entities: Account, Product, Sku, Store, Inventory, Order, Voucher...    |
|  - Quan hệ: @OneToMany, @ManyToOne, @ManyToMany, @EmbeddedId                      |
|  - Cấu hình: HikariCP Connection Pool (Max 20, Min Idle 5)                        |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                            DATABASE MANAGEMENT SYSTEM                             |
|  - Microsoft SQL Server 2022 (Local) / PostgreSQL 16 (Render Cloud Production)    |
|  - 37 Bảng dữ liệu logic, Khóa chính, Khóa ngoại, Ràng buộc UNIQUE & Indexes       |
+-----------------------------------------------------------------------------------+
```

---

## 3. CƠ CHẾ BẢO MẬT VÀ PHÂN QUYỀN ĐA CHI NHÁNH (STORE SCOPE)

1. **Stateless JWT trong HttpOnly Cookie:**
   - Khi đăng nhập thành công (`/api/v1/auth/login`), server sinh token JWT (ký bằng thuật toán HMAC-SHA256 với secret key 32 bytes).
   - Token được ghi vào Cookie với cờ `HttpOnly = true`, `SameSite = Lax`, `Path = /`.
   - **Lợi thế mấu chốt:** Miễn nhiễm hoàn toàn với tấn công XSS (JavaScript phía Client không thể đọc trộm cookie này).
2. **Cầu nối Linh hoạt (`CookieBearerTokenResolver`):**
   - Hỗ trợ cả 2 nguồn: Nếu request gửi qua trình duyệt thì đọc từ Cookie `LUNEA_AT`; nếu gọi từ Postman hoặc mobile app thì đọc từ Header `Authorization: Bearer <token>`.
3. **Phân quyền Chi nhánh (`StoreScope`):**
   - Quản trị viên (`ADMIN`) có quyền xem toàn bộ chuỗi showroom.
   - Nhân viên chi nhánh (`STORE_MANAGER`, `SALES_STAFF`, `WAREHOUSE`) chỉ được phép xem và thao tác dữ liệu (đơn hàng, tồn kho, phiếu nhập) của đúng chi nhánh được phân công trong bảng `NhanVienCuaHang`.
   - `StoreScope` sử dụng biến `ThreadLocal<Long>` để lưu giữ `storeId` của request hiện tại, giúp tầng Service tự động chèn điều kiện lọc mà không sợ lẫn dữ liệu giữa các luồng.

---

## 4. BẢNG TỔNG HỢP CÔNG NGHỆ VÀ PHIÊN BẢN

| Thành phần | Công nghệ lựa chọn | Phiên bản | Mục đích sử dụng |
|---|---|:---:|---|
| **Core Platform** | Java Development Kit (JDK) | 21 LTS | Sử dụng Virtual Threads, Pattern Matching, Record, Sequenced Collections |
| **Framework** | Spring Boot | 4.1.1 / 3.x | Bộ khung ứng dụng web, dependency injection, auto-configuration |
| **Security** | Spring Security 6 | 6.x | Lớp bảo vệ endpoint, kiểm soát quyền theo URL và method |
| **Data Access** | Spring Data JPA / Hibernate | 6.x | Quản trị thực thể ORM, tự động sinh truy vấn |
| **Database Pool** | HikariCP | 5.x | Quản lý kết nối CSDL hiệu năng cao |
| **Database** | SQL Server / PostgreSQL | 2022 / 16 | Hệ quản trị cơ sở dữ liệu quan hệ ACID |
| **Template Engine** | Thymeleaf + SpringSecurity Extras | 3.1.x | Server-Side Rendering HTML kết hợp phân quyền giao diện |
| **Object Mapping** | MapStruct | 1.6.3 | Ánh xạ DTO <-> Entity ở compile-time, zero reflection overhead |
| **Boilerplate Reduction** | Project Lombok | 1.18.48 | Tự động sinh getter/setter/builder/constructor |
| **Image CDN** | Cloudinary Java SDK | 2.0.0 | Lưu trữ và tối ưu hóa hình ảnh mỹ phẩm đám mây |
| **Email Service** | Brevo REST API / Spring Mail | v3 | Gửi mã OTP xác minh tài khoản qua Email |
| **Containerization** | Docker & Docker Compose | Multi-stage | Đóng gói image siêu nhẹ, triển khai lên Cloud Render |
