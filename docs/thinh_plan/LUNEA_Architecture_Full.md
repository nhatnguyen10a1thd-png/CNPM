# LUNEA – Kiến trúc Repository Spring Boot (Đầy đủ, theo tài liệu SRS Nhóm 01)

> Tài liệu này thay thế/mở rộng bản kiến trúc rút gọn trước đó. Nó được xây dựng trực tiếp từ nội dung file **"Nhom01_ThietKeGiaoDien.docx"**: khảo sát hiện trạng, danh sách yêu cầu, 10 actor/91 use-case, mô hình dữ liệu **37 bảng**, và 8 màn hình giao diện chính. Vì vậy quy mô entity/nghiệp vụ ở đây lớn hơn nhiều so với bản rút gọn ban đầu (chuỗi cửa hàng, tồn kho theo SKU×cửa hàng, phân quyền động vai trò/quyền, nhập hàng, điều chuyển kho, trả hàng, nhật ký thao tác).

**Tech stack:** Java, Spring Boot, Spring MVC, Spring Data JPA/Hibernate, Spring Validation, Spring Security, **Lombok**, PostgreSQL, Thymeleaf, HTML/CSS/JavaScript (Bootstrap), Docker Compose, Maven.

---

## 0. Nguyên tắc kiến trúc chung

- **Layered Architecture** không đổi: `Controller/RestController → Service → Repository → Entity → PostgreSQL`.
- Entity đặt tên tiếng Anh chuẩn Java, ánh xạ 1-1 với 37 bảng tiếng Việt trong SRS (bảng ánh xạ đầy đủ ở Mục 6) — vừa dễ code theo convention quốc tế, vừa truy vết được về tài liệu gốc khi bảo vệ đồ án.
- **Không dùng `@ManyToMany`**: mọi quan hệ N-N (SKU↔Thuộc tính, Nhân viên↔Vai trò, Vai trò↔Quyền, Nhân viên↔Cửa hàng, Giỏ hàng↔SKU, Yêu thích↔Sản phẩm, Đơn hàng↔SKU, Phiếu nhập↔SKU, Điều chuyển↔SKU, Trả hàng↔Dòng đơn) đều có **entity trung gian tường minh**.
- Bảng trung gian thuần túy (không cần cột `id` riêng, khóa là cặp FK) dùng **`@EmbeddedId`** với lớp khóa nhúng `XxxId implements Serializable`, thay vì sinh thêm surrogate key vô nghĩa.
- **Lombok**: dùng `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` trên Entity. **Không dùng `@Data`/`@EqualsAndHashCode` mặc định** trên Entity vì Lombok sinh `equals/hashCode` theo toàn bộ field sẽ phá vỡ hành vi Hibernate proxy và quan hệ hai chiều; thay vào đó dùng `@EqualsAndHashCode(of = "id")` (hoặc override tay theo khóa chính/khóa nhúng).
- `FetchType.LAZY` mặc định cho **mọi** quan hệ `@ManyToOne`/`@OneToOne`/`@OneToMany`.
- `CascadeType.ALL` + `orphanRemoval = true` **chỉ** cho quan hệ "sở hữu hoàn toàn" con không tồn tại độc lập: `Cart→CartItem`, `Wishlist→WishlistItem`, `Order→OrderItem`, `Order→OrderStockHold`, `PurchaseOrder→PurchaseOrderItem`, `StockTransfer→StockTransferItem`, `ReturnRequest→ReturnItem`, `Product→ProductImage`, `ProductSku→SkuAttributeValue`, `Customer→CustomerAddress`.
- **Soft-delete** (đổi trạng thái, không xoá vật lý) cho mọi entity có thể phát sinh giao dịch: Product, ProductSku, Category, Brand, Store, Supplier, Employee, Voucher — đúng theo QLSP-QĐ3, QLCH-QĐ3, QLNH-QĐ2, QLNV-QĐ2.
- **Snapshot dữ liệu tại thời điểm giao dịch**: `OrderItem` lưu tên sản phẩm/biến thể/đơn giá lúc đặt (KH-QĐ9, QLDH-QĐ1); `Order` lưu địa chỉ/SĐT snapshot — thay đổi giá/địa chỉ sau này không ảnh hưởng lịch sử.
- **Enum trạng thái dùng đúng miền giá trị đã khai báo trong SRS** (Mục 4.1.2 của tài liệu gốc) để đảm bảo đúng thiết kế dữ liệu đã duyệt: ví dụ `HELD/RELEASED/COMMITTED` cho giữ tồn, `PERCENT/FIXED/FREESHIP` cho voucher, `VISIBLE/HIDDEN/PENDING` cho đánh giá, `DRAFT/CONFIRMED` cho phiếu nhập.
- Xác thực: **session-based (form login)** dùng chung cho cả Thymeleaf và REST nội bộ (cùng origin/cùng cookie) — không cần JWT vì không có client tách biệt domain. AJAX/fetch gửi kèm CSRF token qua header `X-CSRF-TOKEN` (lấy từ `<meta>` do Thymeleaf render).

---

## 1. Sơ đồ kiến trúc tổng thể (ASCII)

```
┌───────────────────────────────┐
│ Thymeleaf View (Customer/Admin)│  ← Nguyên
└───────────────┬────────────────┘
                │ render / form submit
┌───────────────▼────────────────┐        ┌─────────────────────────┐
│ @Controller (page)             │        │ Spring Security          │
│ @RestController (json/AJAX)    │◄──────►│ Account → GrantedAuthority│  ← Nguyên
└───────────────┬────────────────┘        │ ROLE_*, PERM_* động       │
                │ DTO                      └─────────────────────────┘
┌───────────────▼────────────────┐
│ Service / ServiceImpl          │  ← Thịnh
│ @Transactional, business rule  │
│ (KH-QĐ*, QLSP-QĐ*, QLTK-QĐ*...)│
└───────────────┬────────────────┘
                │ Entity
┌───────────────▼────────────────┐
│ Repository (JpaRepository)     │  ← Thịnh
└───────────────┬────────────────┘
                │
┌───────────────▼────────────────┐
│ Entity (JPA/Hibernate, Lombok) │  ← Thịnh
└───────────────┬────────────────┘
                │
┌───────────────▼────────────────┐
│           PostgreSQL            │
└─────────────────────────────────┘

Khách hàng: Home → ProductList → ProductDetail → Cart → Checkout → OrderSuccess
Nhân viên/Admin: /admin/** (Thymeleaf CRUD) + /api/admin/** (bảng động, filter, dashboard)
```

---

## 2. Cây thư mục project đầy đủ

```
lunea/
├── docker-compose.yml
├── .env.example
├── pom.xml
├── README.md
├── API_DOCUMENTATION.md
├── DATABASE.md
├── GIT_GUIDE.md
├── src/
│   ├── main/
│   │   ├── java/com/lunea/
│   │   │   ├── LuneaApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   ├── JpaAuditingConfig.java
│   │   │   │   └── DataSeeder.java              # seed Role/Permission/Store mặc định
│   │   │   ├── controller/
│   │   │   │   ├── customer/
│   │   │   │   │   ├── HomeController.java
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── AccountController.java   # hồ sơ, địa chỉ, Beauty Profile
│   │   │   │   │   ├── WishlistController.java
│   │   │   │   │   ├── CartController.java
│   │   │   │   │   ├── CheckoutController.java
│   │   │   │   │   └── OrderController.java     # lịch sử/chi tiết/hủy đơn
│   │   │   │   └── admin/
│   │   │   │       ├── AdminDashboardController.java
│   │   │   │       ├── AdminProductController.java
│   │   │   │       ├── AdminCategoryBrandController.java
│   │   │   │       ├── AdminAttributeController.java
│   │   │   │       ├── AdminStoreController.java
│   │   │   │       ├── AdminSupplierController.java
│   │   │   │       ├── AdminPurchaseOrderController.java
│   │   │   │       ├── AdminInventoryController.java
│   │   │   │       ├── AdminStockTransferController.java
│   │   │   │       ├── AdminOrderController.java
│   │   │   │       ├── AdminReturnController.java
│   │   │   │       ├── AdminReviewController.java
│   │   │   │       ├── AdminVoucherController.java
│   │   │   │       ├── AdminEmployeeController.java   # nhân viên, vai trò, quyền
│   │   │   │       ├── AdminCustomerController.java
│   │   │   │       ├── AdminAuditLogController.java
│   │   │   │       └── AdminReportController.java
│   │   │   ├── rest/
│   │   │   │   ├── customer/
│   │   │   │   │   ├── ProductRestController.java     # search/filter/sort động
│   │   │   │   │   ├── CartRestController.java
│   │   │   │   │   ├── WishlistRestController.java
│   │   │   │   │   ├── OrderRestController.java
│   │   │   │   │   ├── ReviewRestController.java
│   │   │   │   │   ├── VoucherRestController.java      # kiểm tra/áp dụng voucher
│   │   │   │   │   └── StoreLocatorRestController.java # tồn theo cửa hàng, vị trí
│   │   │   │   └── admin/
│   │   │   │       ├── ProductAdminRestController.java
│   │   │   │       ├── InventoryAdminRestController.java
│   │   │   │       ├── PurchaseOrderAdminRestController.java
│   │   │   │       ├── StockTransferAdminRestController.java
│   │   │   │       ├── OrderAdminRestController.java
│   │   │   │       ├── ReturnAdminRestController.java
│   │   │   │       ├── VoucherAdminRestController.java
│   │   │   │       ├── EmployeeAdminRestController.java
│   │   │   │       └── ReportAdminRestController.java  # dashboard, charts
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── account/     # RegisterRequest, LoginRequest, ProfileUpdateRequest,
│   │   │   │   │   │                # AddressRequest, BeautyProfileRequest, ChangePasswordRequest
│   │   │   │   │   ├── catalog/     # ProductRequest, ProductSkuRequest, CategoryRequest,
│   │   │   │   │   │                # BrandRequest, AttributeRequest, ProductImageRequest
│   │   │   │   │   ├── store/       # StoreRequest, InventoryAdjustmentRequest, StockTransferRequest
│   │   │   │   │   ├── cart/        # CartItemRequest
│   │   │   │   │   ├── order/       # CheckoutRequest, OrderStatusUpdateRequest
│   │   │   │   │   ├── purchase/    # SupplierRequest, PurchaseOrderRequest
│   │   │   │   │   ├── promotion/   # VoucherRequest
│   │   │   │   │   ├── review/      # ReviewRequest, ReviewModerationRequest
│   │   │   │   │   ├── returns/     # ReturnRequestCreateRequest, ReturnDecisionRequest
│   │   │   │   │   └── employee/    # EmployeeRequest, RoleRequest, PermissionAssignRequest
│   │   │   │   └── response/        # ProductResponse, ProductDetailResponse, CategoryResponse,
│   │   │   │                        # StoreResponse, StoreStockResponse, CartResponse, OrderResponse,
│   │   │   │                        # OrderDetailResponse, InventoryResponse, PurchaseOrderResponse,
│   │   │   │                        # ReviewResponse, ReturnResponse, EmployeeResponse,
│   │   │   │                        # ReportRevenueResponse, ApiResponse, PageResponse
│   │   │   ├── entity/
│   │   │   │   ├── base/BaseEntity.java
│   │   │   │   ├── account/    # Account, Customer, CustomerAddress, BeautyProfile, Employee,
│   │   │   │   │               # Role, Permission, EmployeeRole(+Id), RolePermission(+Id),
│   │   │   │   │               # EmployeeStore(+Id), AuditLog
│   │   │   │   ├── catalog/    # Brand, Category, Product, ProductSku, Attribute,
│   │   │   │   │               # SkuAttributeValue(+Id), ProductImage
│   │   │   │   ├── store/      # Store, Inventory(+Id), InventoryAdjustment,
│   │   │   │   │               # StockTransfer, StockTransferItem(+Id)
│   │   │   │   ├── cart/       # Cart, CartItem(+Id), Wishlist, WishlistItem(+Id)
│   │   │   │   ├── order/      # Voucher, Order, OrderItem, OrderStockHold
│   │   │   │   ├── returns/    # ReturnRequest, ReturnItem(+Id)
│   │   │   │   ├── review/     # Review
│   │   │   │   └── purchase/   # Supplier, PurchaseOrder, PurchaseOrderItem(+Id)
│   │   │   ├── repository/     # (cùng cấu trúc domain như entity/, 1 repo / entity)
│   │   │   ├── service/ + impl/# (cùng cấu trúc domain)
│   │   │   ├── mapper/         # (cùng cấu trúc domain)
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── InvalidVoucherException.java
│   │   │   │   ├── InvalidStatusTransitionException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── security/
│   │   │       ├── CustomUserDetailsService.java
│   │   │       ├── CustomUserPrincipal.java
│   │   │       ├── SecurityUtils.java          # current employee, store scope
│   │   │       └── StoreScopeAccessDenied.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/   # common.css, home.css, product.css, cart.css, checkout.css,
│   │       │   │          # account.css, admin-common.css, admin-report.css
│   │       │   ├── js/    # common.js, product.js, cart.js, checkout.js, account.js,
│   │       │   │          # admin-common.js, admin-product.js, admin-inventory.js,
│   │       │   │          # admin-order.js, admin-report.js
│   │       │   └── images/
│   │       ├── templates/
│   │       │   ├── fragments/    # header.html, navbar.html, footer.html, pagination.html,
│   │       │   │                 # admin-sidebar.html, form-error.html
│   │       │   ├── layout/       # main-layout.html, admin-layout.html
│   │       │   ├── home/index.html
│   │       │   ├── product/      # list.html, detail.html
│   │       │   ├── auth/         # login.html, register.html
│   │       │   ├── account/      # profile.html, addresses.html, beauty-profile.html, wishlist.html
│   │       │   ├── cart/view.html
│   │       │   ├── checkout/checkout.html
│   │       │   ├── order/        # success.html, history.html, detail.html
│   │       │   └── admin/
│   │       │       ├── dashboard.html
│   │       │       ├── product/  # list.html, form.html, sku-form.html, images.html
│   │       │       ├── category-brand/list.html
│   │       │       ├── store/list.html, form.html
│   │       │       ├── supplier/list.html, form.html
│   │       │       ├── purchase-order/list.html, form.html, detail.html
│   │       │       ├── inventory/list.html, adjust.html
│   │       │       ├── stock-transfer/list.html, form.html
│   │       │       ├── order/list.html, detail.html
│   │       │       ├── return/list.html, detail.html
│   │       │       ├── review/list.html
│   │       │       ├── voucher/list.html, form.html
│   │       │       ├── employee/list.html, form.html, role-permission.html
│   │       │       ├── customer/list.html, detail.html
│   │       │       ├── audit-log/list.html
│   │       │       └── report/dashboard.html
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/java/com/lunea/
│       ├── service/       # OrderServiceTest, InventoryServiceTest, ProductServiceTest,
│       │                  # VoucherServiceTest, SecurityPermissionTest
│       ├── repository/    # ProductRepositoryTest, InventoryRepositoryTest, OrderRepositoryTest
│       └── controller/    # ProductRestControllerTest, OrderRestControllerTest
```

---

## 3. Vai trò từng package

| Package | Vai trò | Ai chỉnh chính |
|---|---|---|
| `config` | Security config, seed dữ liệu Role/Permission/Store mặc định | Nguyên (Security), chung phần còn lại |
| `controller/customer`, `controller/admin` | Trả view Thymeleaf, xử lý form | Nguyên |
| `rest/customer`, `rest/admin` | JSON cho AJAX/fetch: search, filter, dashboard, bảng động | Nguyên |
| `dto/request`, `dto/response` | Hợp đồng dữ liệu Controller ↔ Service | Thịnh định nghĩa, Nguyên dùng |
| `entity/*` | Mapping 37 bảng dữ liệu | Thịnh |
| `repository/*` | Truy vấn DB, `JpaRepository`/`Pageable` | Thịnh |
| `service/*` + `impl` | Business logic, transaction, truy vết quy định nghiệp vụ | Thịnh |
| `mapper/*` | Entity ↔ DTO | Thịnh |
| `exception/*` | Exception nghiệp vụ + xử lý lỗi tập trung | Thịnh (exception), Nguyên (hiển thị lỗi) |
| `security/*` | Authentication, phân quyền động theo Role/Permission, phạm vi cửa hàng | Nguyên chủ, Thịnh hỗ trợ (Employee/Role/Permission entity) |

---

## 4. Ánh xạ Actor (SRS) → Vai trò hệ thống & Nhóm quyền

| Mã | Actor (SRS) | Role đề xuất (`roles.name`) | Nhóm quyền chính (`permissions.code`) |
|---|---|---|---|
| KH | Khách hàng | `ROLE_CUSTOMER` (không qua Role/Permission, gắn ở Account) | — |
| QLSP | Nhân viên quản lý sản phẩm | `ROLE_PRODUCT_MANAGER` | `PRODUCT_MANAGE`, `CATEGORY_MANAGE`, `BRAND_MANAGE`, `ATTRIBUTE_MANAGE`, `PRODUCT_IMAGE_MANAGE`, `REVIEW_MODERATE` |
| QLCH | Quản trị viên / Quản lý chuỗi | `ROLE_CHAIN_ADMIN` | `STORE_MANAGE`, `STORE_VIEW_ALL` |
| QLNH | Nhân viên nhập hàng | `ROLE_PURCHASING_STAFF` | `SUPPLIER_MANAGE`, `PURCHASE_ORDER_CREATE`, `PURCHASE_ORDER_CONFIRM` |
| QLTK | Nhân viên kho | `ROLE_WAREHOUSE_STAFF` | `INVENTORY_VIEW`, `INVENTORY_ADJUST`, `STOCK_TRANSFER_MANAGE` |
| QLKH | Nhân viên chăm sóc khách hàng | `ROLE_CUSTOMER_SERVICE` | `CUSTOMER_VIEW`, `CUSTOMER_ACCOUNT_MANAGE`, `RETURN_RECEIVE`, `RETURN_PROCESS` |
| QLDH | Nhân viên xử lý đơn hàng | `ROLE_ORDER_STAFF` | `ORDER_VIEW`, `ORDER_CONFIRM`, `ORDER_ASSIGN_STORE`, `ORDER_UPDATE_STATUS`, `ORDER_CANCEL` |
| QLKM | Nhân viên Marketing | `ROLE_MARKETING_STAFF` | `PROMOTION_MANAGE`, `VOUCHER_MANAGE` |
| QLNV | Quản trị viên (nhân sự) | `ROLE_SYSTEM_ADMIN` | `EMPLOYEE_MANAGE`, `ROLE_PERMISSION_MANAGE`, `AUDIT_LOG_VIEW` |
| BCTK | Quản lý / Quản trị viên (báo cáo) | dùng chung `ROLE_CHAIN_ADMIN`/`ROLE_SYSTEM_ADMIN` | `REPORT_VIEW` |

Một `Employee` có thể được gán **nhiều Role** (qua `EmployeeRole`), mỗi `Role` có **nhiều Permission** (qua `RolePermission`) — đúng mô hình `VaiTro`/`Quyen` trong SRS, không hard-code role cố định 1-1 với actor.

---

## 5. Bảng ánh xạ ERD gốc (tiếng Việt, 37 bảng) → Entity Java

| STT | Bảng gốc (SRS) | Entity Java | Bảng PostgreSQL |
|---|---|---|---|
| 1 | TaiKhoan | `Account` | `accounts` |
| 2 | KhachHang | `Customer` | `customers` |
| 3 | DiaChiKhachHang | `CustomerAddress` | `customer_addresses` |
| 4 | BeautyProfile | `BeautyProfile` | `beauty_profiles` |
| 5 | ThuongHieu | `Brand` | `brands` |
| 6 | DanhMuc | `Category` | `categories` |
| 7 | SanPham | `Product` | `products` |
| 8 | SKU | `ProductSku` | `product_skus` |
| 9 | ThuocTinh | `Attribute` | `attributes` |
| 10 | GiaTriThuocTinhSKU | `SkuAttributeValue` | `sku_attribute_values` |
| 11 | HinhAnhSanPham | `ProductImage` | `product_images` |
| 12 | CuaHang | `Store` | `stores` |
| 13 | TonKho | `Inventory` | `inventories` |
| 14 | GioHang | `Cart` | `carts` |
| 15 | ChiTietGioHang | `CartItem` | `cart_items` |
| 16 | Voucher | `Voucher` | `vouchers` |
| 17 | DonHang | `Order` | `orders` |
| 18 | ChiTietDonHang | `OrderItem` | `order_items` |
| 19 | GiuTonDonHang | `OrderStockHold` | `order_stock_holds` |
| 20 | YeuThich | `Wishlist` | `wishlists` |
| 21 | ChiTietYeuThich | `WishlistItem` | `wishlist_items` |
| 22 | DanhGia | `Review` | `reviews` |
| 23 | YeuCauTraHang | `ReturnRequest` | `return_requests` |
| 24 | ChiTietTraHang | `ReturnItem` | `return_items` |
| 25 | NhaCungCap | `Supplier` | `suppliers` |
| 26 | PhieuNhap | `PurchaseOrder` | `purchase_orders` |
| 27 | ChiTietPhieuNhap | `PurchaseOrderItem` | `purchase_order_items` |
| 28 | DieuChinhTon | `InventoryAdjustment` | `inventory_adjustments` |
| 29 | DieuChuyenKho | `StockTransfer` | `stock_transfers` |
| 30 | ChiTietDieuChuyen | `StockTransferItem` | `stock_transfer_items` |
| 31 | NhanVien | `Employee` | `employees` |
| 32 | VaiTro | `Role` | `roles` |
| 33 | Quyen | `Permission` | `permissions` |
| 34 | NhanVienVaiTro | `EmployeeRole` | `employee_roles` |
| 35 | VaiTroQuyen | `RolePermission` | `role_permissions` |
| 36 | NhanVienCuaHang | `EmployeeStore` | `employee_stores` |
| 37 | NhatKyThaoTac | `AuditLog` | `audit_logs` |

---

## 6. Thiết kế Entity chi tiết (theo nhóm nghiệp vụ)

### 6.1. Account & Authorization

**Account** (`accounts`, gốc *TaiKhoan*)

| Field | Java | PostgreSQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| id | Long | BIGSERIAL | PK | |
| username | String | VARCHAR(100) | unique, NOT NULL | |
| passwordHash | String | VARCHAR(255) | NOT NULL | BCrypt |
| email | String | VARCHAR(150) | unique | |
| phone | String | VARCHAR(20) | unique | |
| accountType | enum `CUSTOMER, EMPLOYEE` | VARCHAR(20) | NOT NULL | |
| status | enum `ACTIVE, INACTIVE` | VARCHAR(20) | | INACTIVE = khóa (QLKH-QĐ2) |
| createdAt | LocalDateTime | TIMESTAMP | | |

**Customer** (`customers`, gốc *KhachHang*) — `@OneToOne` với Account
`id, account(FK unique), fullName, dob, gender, loyaltyPoints(>=0), joinDate`

**CustomerAddress** (`customer_addresses`, gốc *DiaChiKhachHang*) — N-1 Customer, cascade ALL+orphanRemoval
`id, customer(FK), recipientName, phone, addressDetail, ward, district, province, isDefault(boolean)`

**BeautyProfile** (`beauty_profiles`) — `@OneToOne` với Customer
`id, customer(FK unique), skinType, skinConcerns(TEXT), careNeeds(TEXT), preferences(TEXT), priceRangeInterest, updatedAt` — toàn bộ field tùy chọn (KH-QĐ5)

**Employee** (`employees`, gốc *NhanVien*) — `@OneToOne` với Account
`id, account(FK unique), fullName, internalEmail, phone, status(ACTIVE/INACTIVE)`

**Role** (`roles`, gốc *VaiTro*): `id, name(unique), description`
**Permission** (`permissions`, gốc *Quyen*): `id, code(unique, UPPER_SNAKE_CASE), name, description`
**EmployeeRole** (`employee_roles`, gốc *NhanVienVaiTro*): `@EmbeddedId(employeeId, roleId)`
**RolePermission** (`role_permissions`, gốc *VaiTroQuyen*): `@EmbeddedId(roleId, permissionId)`
**EmployeeStore** (`employee_stores`, gốc *NhanVienCuaHang*): `@EmbeddedId(employeeId, storeId)`, `isPrimaryBranch(boolean)`
**AuditLog** (`audit_logs`, gốc *NhatKyThaoTac*): `id, employee(FK, nullable), action, objectType, objectId, content(TEXT), timestamp, ipAddress`

### 6.2. Catalog (Danh mục sản phẩm)

**Brand** (`brands`, gốc *ThuongHieu*): `id, name(unique), description, status(ACTIVE/INACTIVE)`

**Category** (`categories`, gốc *DanhMuc*): `id, name, parent(self @ManyToOne nullable), description, status`

**Product** (`products`, gốc *SanPham*)

| Field | Java | PostgreSQL | Ghi chú |
|---|---|---|---|
| id | Long | BIGSERIAL | PK |
| brand | Brand | FK, LAZY, nullable | |
| category | Category | FK, LAZY, NOT NULL | index |
| name | String | VARCHAR(200) NOT NULL | index (có thể thêm `pg_trgm` để search gần đúng) |
| description | String | TEXT | |
| origin | String | VARCHAR(100) | xuất xứ |
| mainIngredients | String | TEXT | |
| uses | String | TEXT | công dụng |
| status | enum `ACTIVE, INACTIVE` | VARCHAR(20) | QLSP-QĐ3: soft-delete |
| createdAt | LocalDateTime | TIMESTAMP | |

**ProductSku** (`product_skus`, gốc *SKU*): `id, product(FK), skuCode(unique), variantName, sellingPrice(NUMERIC(12,2)>=0), listPrice(NUMERIC(12,2)>=0), barcode(unique, nullable), status(ACTIVE/INACTIVE)` — cascade `{PERSIST, MERGE}` từ Product (không ALL vì SKU tự quản lý vòng đời qua trạng thái, QLSP-QĐ5)

**Attribute** (`attributes`, gốc *ThuocTinh*): `id, name, attributeGroup` (nhóm: SKIN_TYPE, COLOR, VOLUME…)

**SkuAttributeValue** (`sku_attribute_values`, gốc *GiaTriThuocTinhSKU*): `@EmbeddedId(skuId, attributeId)`, `value` — cascade ALL+orphanRemoval từ ProductSku

**ProductImage** (`product_images`, gốc *HinhAnhSanPham*): `id, product(FK), sku(FK, nullable), imageUrl(Cloudinary URL), isPrimary(boolean), sortOrder(int>=0)` — cascade ALL+orphanRemoval từ Product; ràng buộc nghiệp vụ QLSP-QĐ6 (ít nhất 1 ảnh chính) kiểm tra ở Service

### 6.3. Store & Inventory (Cửa hàng & Tồn kho)

**Store** (`stores`, gốc *CuaHang*): `id, name, address, phone, openTime(LocalTime), closeTime(LocalTime), latitude(NUMERIC(10,7)), longitude(NUMERIC(10,7)), status(ACTIVE/INACTIVE)`

**Inventory** (`inventories`, gốc *TonKho*): `@EmbeddedId(storeId, skuId)`, `actualStock(int>=0), heldQuantity(int>=0), lowStockThreshold(int>=0), updatedAt`
- Tồn khả dụng = `actualStock - heldQuantity`, tính ở Service (QLTK-QĐ8); có thể thêm cột PostgreSQL *generated* `available_stock` (`STORED`) để filter/sort nhanh mà vẫn nhất quán.

**InventoryAdjustment** (`inventory_adjustments`, gốc *DieuChinhTon*): `id, store(FK), sku(FK), quantityBefore, quantityAfter, reason(NOT NULL), performedBy(Employee FK), adjustedAt` (QLTK-QĐ3)

**StockTransfer** (`stock_transfers`, gốc *DieuChuyenKho*): `id, fromStore(FK), toStore(FK), status(enum PENDING, IN_TRANSIT, RECEIVED), createdBy(Employee FK), createdDate, shippedDate, receivedDate`

**StockTransferItem** (`stock_transfer_items`, gốc *ChiTietDieuChuyen*): `@EmbeddedId(stockTransferId, skuId)`, `quantity(>0)` — cascade ALL+orphanRemoval

### 6.4. Cart & Wishlist

**Cart** (`carts`, gốc *GioHang*): `id, customer(FK unique), updatedAt`
**CartItem** (`cart_items`, gốc *ChiTietGioHang*): `@EmbeddedId(cartId, skuId)`, `quantity(>0)` — cascade ALL+orphanRemoval; thêm giỏ **không** giữ tồn (KH-QĐ7)
**Wishlist** (`wishlists`, gốc *YeuThich*): `id, customer(FK unique), createdAt`
**WishlistItem** (`wishlist_items`, gốc *ChiTietYeuThich*): `@EmbeddedId(wishlistId, productId)`, `addedAt` — cascade ALL+orphanRemoval

### 6.5. Order & Promotion

**Voucher** (`vouchers`): `id, code(unique), discountType(enum PERCENT, FIXED, FREESHIP), value, maxDiscount, minOrderValue, startDate, endDate, usageLimit(>=0), usedCount(>=0), status(ACTIVE/INACTIVE)` — miền giá trị đúng bảng 4.16 SRS

**Order** (`orders`, gốc *DonHang*)

| Field | Java | Ghi chú |
|---|---|---|
| id | Long | PK |
| customer | Customer | FK, LAZY |
| processingStore | Store | FK, LAZY, gán sau khi xác nhận (QLDH-QĐ3) |
| voucher | Voucher | FK, nullable |
| shippingAddressSnapshot | String (TEXT) | chốt tại thời điểm đặt |
| phoneSnapshot | String | |
| subtotal, discount, shippingFee, totalPayment | BigDecimal | KH-QĐ9, QLDH-QĐ6 |
| paymentMethod | enum `COD` | mở rộng sau |
| status | enum `PENDING_CONFIRMATION, CONFIRMED, PREPARING, SHIPPING, COMPLETED, CANCELLED` | QLDH-QĐ4 |
| orderDate | LocalDateTime | index cùng customer_id, processing_store_id, status |

**OrderItem** (`order_items`, gốc *ChiTietDonHang*): `id, order(FK), sku(FK), productNameSnapshot, variantNameSnapshot, unitPriceSnapshot, quantity(>0), lineTotal` — cascade ALL+orphanRemoval từ Order (QLDH-QĐ1)

**OrderStockHold** (`order_stock_holds`, gốc *GiuTonDonHang*): `id, order(FK), store(FK), sku(FK), quantity(>0), status(enum HELD, RELEASED, COMMITTED), heldAt, releasedAt` — cascade ALL+orphanRemoval từ Order

### 6.6. Return (Trả hàng)

**ReturnRequest** (`return_requests`, gốc *YeuCauTraHang*): `id, order(FK), customer(FK), reason, description(TEXT), status(enum REQUESTED, APPROVED, REJECTED, PROCESSING, COMPLETED — *giả định hợp lý, SRS chỉ ghi "theo quy trình trả hàng"*), requestDate, handledBy(Employee FK nullable)`

**ReturnItem** (`return_items`, gốc *ChiTietTraHang*): `@EmbeddedId(returnRequestId, orderItemId)`, `quantity(>0), detailReason` — cascade ALL+orphanRemoval

### 6.7. Review (Đánh giá)

**Review** (`reviews`, gốc *DanhGia*): `id, customer(FK), product(FK), orderItem(FK — minh chứng đã mua, KH-QĐ15), rating(TINYINT 1..5), content(TEXT), moderationStatus(enum VISIBLE, HIDDEN, PENDING), reviewDate`

### 6.8. Purchase (Nhập hàng)

**Supplier** (`suppliers`, gốc *NhaCungCap*): `id, name, address, phone, email, status(ACTIVE/INACTIVE)`

**PurchaseOrder** (`purchase_orders`, gốc *PhieuNhap*): `id, supplier(FK), receivingStore(FK), createdBy(Employee FK), status(enum DRAFT, CONFIRMED), createdDate, confirmedDate, totalAmount` — miền giá trị đúng bảng 4.26 SRS

**PurchaseOrderItem** (`purchase_order_items`, gốc *ChiTietPhieuNhap*): `@EmbeddedId(purchaseOrderId, skuId)`, `quantity(>0), unitCost, lineTotal` — cascade ALL+orphanRemoval; chỉ sửa được khi PurchaseOrder ở `DRAFT` (QLNH-QĐ4)

---

## 7. Entity Relationship tổng hợp

```
Account (1) ── (1) Customer            Account (1) ── (1) Employee
Customer (1) ──< CustomerAddress (N)    Customer (1) ── (1) BeautyProfile
Customer (1) ── (1) Cart                Customer (1) ── (1) Wishlist
Customer (1) ──< Order (N)              Customer (1) ──< Review (N)
Customer (1) ──< ReturnRequest (N)

Brand (1) ──< Product (N)               Category (1) ──< Product (N)
Category (1) ──< Category (N)  [tự tham chiếu, cha-con]
Product (1) ──< ProductSku (N)          Product (1) ──< ProductImage (N)
ProductSku (1) ──< ProductImage (N)     ProductSku (N) >──< Attribute (N)  qua SkuAttributeValue

Store (1) ──< Inventory (N) ── (N) >── ProductSku
Store (1) ──< InventoryAdjustment (N)   Store (2) ──< StockTransfer (from/to)
StockTransfer (1) ──< StockTransferItem (N) ── ProductSku

Cart (1) ──< CartItem (N) ── ProductSku          Wishlist (1) ──< WishlistItem (N) ── Product
Order (N) >── (1) Customer   Order (N) >── (1) Store   Order (N) >── (0..1) Voucher
Order (1) ──< OrderItem (N) ── ProductSku
Order (1) ──< OrderStockHold (N)        Order (1) ──< ReturnRequest (0..N)
ReturnRequest (1) ──< ReturnItem (N) ── OrderItem
Product (1) ──< Review (N)              Review (N) >── (1) Customer, (1) OrderItem

Supplier (1) ──< PurchaseOrder (N) >── (1) Store (receiving)
PurchaseOrder (1) ──< PurchaseOrderItem (N) ── ProductSku

Employee (N) >──< Role (N)  qua EmployeeRole
Role (N) >──< Permission (N)  qua RolePermission
Employee (N) >──< Store (N)  qua EmployeeStore
Employee (1) ──< AuditLog (N)
Employee (1) ──< {PurchaseOrder.createdBy, StockTransfer.createdBy, InventoryAdjustment.performedBy, ReturnRequest.handledBy}
```

Không có `@ManyToMany` ở đâu — mọi N-N đi qua entity trung gian có `@EmbeddedId` (`SkuAttributeValue`, `EmployeeRole`, `RolePermission`, `EmployeeStore`, `CartItem`, `WishlistItem`, `StockTransferItem`, `PurchaseOrderItem`) hoặc entity có nghiệp vụ riêng (`OrderItem`, `ReturnItem`) nên vẫn giữ khóa nhúng nhưng mang thêm dữ liệu snapshot.

---

## 8. Repository Design

Tất cả kế thừa `JpaRepository<Entity, ID>` (ID là `Long` hoặc lớp `XxxId` cho bảng trung gian).

| Repository | Khóa | Query tiêu biểu |
|---|---|---|
| `AccountRepository` | Long | `findByUsername`, `existsByEmail`, `existsByPhone` |
| `CustomerRepository` | Long | `findByAccountId` |
| `CustomerAddressRepository` | Long | `findByCustomerId`, `findByCustomerIdAndIsDefaultTrue` |
| `BeautyProfileRepository` | Long | `findByCustomerId` |
| `EmployeeRepository` | Long | `findByAccountId`, `findByStatus`, `searchByNameOrEmail(keyword, Pageable)` |
| `RoleRepository`, `PermissionRepository` | Long | `findByName`, `findByCode` |
| `EmployeeRoleRepository` | `EmployeeRoleId` | `findByEmployeeId` |
| `RolePermissionRepository` | `RolePermissionId` | `findByRoleId` |
| `EmployeeStoreRepository` | `EmployeeStoreId` | `findByEmployeeId` (→ danh sách store scope) |
| `AuditLogRepository` | Long | `findByEmployeeIdOrderByTimestampDesc(Pageable)` |
| `BrandRepository`, `CategoryRepository` | Long | `findByStatus`, `findByParentId` |
| `ProductRepository` | Long | `findByStatus(Pageable)`, `findByCategoryIdAndStatus`, `findByBrandIdAndStatus`, `findByNameContainingIgnoreCaseAndStatus(Pageable)`, `@Query` lọc kết hợp giá/loại da/nhu cầu qua join `ProductSku`/`SkuAttributeValue` |
| `ProductSkuRepository` | Long | `findBySkuCode`, `findByProductIdAndStatus` |
| `AttributeRepository` | Long | `findByAttributeGroup` |
| `SkuAttributeValueRepository` | `SkuAttributeValueId` | `findBySkuId` |
| `ProductImageRepository` | Long | `findByProductIdOrderBySortOrder`, `findBySkuId` |
| `StoreRepository` | Long | `findByStatus`, `@Query` tính khoảng cách theo lat/long (Store Locator) |
| `InventoryRepository` | `InventoryId` | `findBySkuId` (mọi cửa hàng), `findByStoreIdAndSkuIdIn`, `@Query` kiểm tra đủ tồn khả dụng cho danh sách SKU tại 1 store, `findByAvailableStockLessThanEqualLowStockThreshold` (tồn thấp – QLTK-QĐ9) |
| `InventoryAdjustmentRepository` | Long | `findByStoreIdAndSkuId(Pageable)` |
| `StockTransferRepository` | Long | `findByStatus`, `findByFromStoreIdOrToStoreId` |
| `StockTransferItemRepository` | `StockTransferItemId` | `findByStockTransferId` |
| `CartRepository` | Long | `findByCustomerId` |
| `CartItemRepository` | `CartItemId` | `findByCartId`, `deleteByCartId` |
| `WishlistRepository`, `WishlistItemRepository` | Long / `WishlistItemId` | `findByWishlistId` |
| `VoucherRepository` | Long | `findByCode`, `findByStatus` |
| `OrderRepository` | Long | `findByCustomerId(Pageable)`, `findByStatus(Pageable)`, `findByProcessingStoreIdAndStatus`, `@Query` doanh thu theo khoảng thời gian/cửa hàng (BCTK-QĐ1) |
| `OrderItemRepository` | Long | `findByOrderId`, `@Query` sản phẩm bán chạy theo khoảng thời gian (BCTK-QĐ2) |
| `OrderStockHoldRepository` | Long | `findByOrderIdAndStatus` |
| `ReviewRepository` | Long | `findByProductIdAndModerationStatus(Pageable)`, `existsByOrderItemIdAndCustomerId` |
| `ReturnRequestRepository` | Long | `findByCustomerId`, `findByStatus(Pageable)` |
| `ReturnItemRepository` | `ReturnItemId` | `findByReturnRequestId` |
| `SupplierRepository` | Long | `findByStatus`, `searchByName(keyword)` |
| `PurchaseOrderRepository` | Long | `findByStatus`, `findBySupplierIdAndReceivingStoreId(Pageable)` |
| `PurchaseOrderItemRepository` | `PurchaseOrderItemId` | `findByPurchaseOrderId` |

**Pageable** dùng cho mọi danh sách lớn: sản phẩm, đơn hàng, nhân viên, khách hàng, nhật ký thao tác, phiếu nhập. `@Query` chỉ dùng khi derived query không diễn tả được (lọc nhiều điều kiện, join tính tồn khả dụng, thống kê doanh thu/sản phẩm bán chạy).

---

## 9. Service Design & Truy vết quy định nghiệp vụ

Mỗi Service `Xxx` có interface + `XxxImpl`, method `create/update/delete` (soft-delete) có `@Transactional`; `get/search/filter` không cần transaction (đọc). Bảng dưới truy vết **toàn bộ mã quy định (QĐ)** trong SRS về đúng Service áp dụng.

| Nhóm | Mã QĐ | Service.method() | Nội dung kiểm tra |
|---|---|---|---|
| Khách hàng | KH-QĐ1 | `ProductService` mọi query công khai | chỉ trả `status=ACTIVE` |
| | KH-QĐ2 | `InventoryService.getAvailableByStore()` | dựa `available_stock` |
| | KH-QĐ3, QĐ4 | `AccountService.register()`, `CustomerService.updateProfile()` | email/SĐT duy nhất, dữ liệu bắt buộc |
| | KH-QĐ5, QĐ6 | `BeautyProfileService`, `ProductService.recommend()` | field tuỳ chọn; đối chiếu thuộc tính, không dùng AI |
| | KH-QĐ7 | `CartService.addItem()/updateQuantity()` | số lượng > 0, **không giữ tồn** |
| | KH-QĐ8, QĐ9 | `VoucherService.validate()`, `OrderPricingService.calculateTotal()` | hạn dùng/điều kiện; công thức tổng tiền |
| | KH-QĐ10 | `OrderService.checkout()` | chỉ chấp nhận `paymentMethod = COD` |
| | KH-QĐ11 | `OrderService.getMyOrders()/getDetail()` | chỉ đơn của chính khách hàng |
| | KH-QĐ12 | `OrderService` | enum trạng thái đúng vòng đời |
| | KH-QĐ13 | `OrderService.cancel()` | chỉ hủy khi status trước `SHIPPING` |
| | KH-QĐ14 | `ReturnRequestService.create()` | theo chính sách trả hàng |
| | KH-QĐ15 | `ReviewService.create()` | bắt buộc có `OrderItem` (đã mua & đơn hoàn thành) |
| Quản lý sản phẩm | QLSP-QĐ1..QĐ3 | `ProductService.create/update/delete()` | mã duy nhất; không đổi dữ liệu đã snapshot trong đơn cũ; soft-delete |
| | QLSP-QĐ4 | `CategoryService`, `BrandService` | chặn xoá khi đang có Product tham chiếu |
| | QLSP-QĐ5 | `ProductSkuService.create/update/delete()` | 1 Product nhiều SKU, mã SKU riêng |
| | QLSP-QĐ6 | `ProductImageService` | bắt buộc ≥1 ảnh chính khi công khai |
| | QLSP-QĐ7 | `ReviewService.moderate()` | ẩn nhưng không xoá |
| Quản lý chuỗi | QLCH-QĐ1..QĐ3 | `StoreService.create/update/deactivate()` | mã duy nhất, dữ liệu tối thiểu, không xoá cửa hàng đã có giao dịch |
| | QLCH-QĐ4 | `InventoryService.getByStore()` | theo tồn khả dụng |
| Nhập hàng | QLNH-QĐ1, QĐ2 | `SupplierService.create/deactivate()` | mã duy nhất, không xoá khi đã có phiếu nhập |
| | QLNH-QĐ3, QĐ4 | `PurchaseOrderService.create/update()` | đủ NCC/kho nhận/SKU/SL; chỉ sửa khi `DRAFT` |
| | QLNH-QĐ5 | `PurchaseOrderService.confirm()` | tăng `actualStock` tại `receivingStore` khi chuyển `CONFIRMED` |
| Kho | QLTK-QĐ1 | `InventoryService` | tồn theo cặp (SKU, cửa hàng) |
| | QLTK-QĐ2 | `PurchaseOrderService.confirm()` | không cộng tồn thủ công trùng với phiếu nhập đã xác nhận |
| | QLTK-QĐ3 | `InventoryAdjustmentService.adjust()` | lưu trước/sau/lý do/người/thời gian |
| | QLTK-QĐ4 | `InventoryAdjustmentService.reconcileFromStockCount()` | kết quả kiểm kê làm căn cứ điều chỉnh |
| | QLTK-QĐ5 | `StockTransferService.create/ship/receive()` | đúng 3 trạng thái, cập nhật tồn đúng thời điểm |
| | QLTK-QĐ6, QĐ7 | `OrderService.checkout()/cancel()` | giữ tồn khi tạo đơn thành công; giải phóng khi huỷ/không hợp lệ |
| | QLTK-QĐ8 | `InventoryService.getAvailableStock()` | `available = actual − held`, không âm |
| | QLTK-QĐ9 | `InventoryService.findLowStock()` | `available <= lowStockThreshold` |
| CSKH | QLKH-QĐ1..QĐ3 | `CustomerAdminService`, `ReturnRequestService.receive/process()` | phạm vi quyền; khoá tài khoản không mất dữ liệu; trả hàng gắn đơn/sản phẩm cụ thể |
| Xử lý đơn | QLDH-QĐ1 | `OrderService.checkout()` | snapshot tên/giá tại thời điểm đặt |
| | QLDH-QĐ2 | `OrderService.confirm()` | đủ tồn khả dụng mới xác nhận |
| | QLDH-QĐ3 | `OrderService.assignProcessingStore()` | 1 đơn 1 chi nhánh, đủ tồn toàn bộ SKU |
| | QLDH-QĐ4 | `OrderService.updateStatus()` | đúng trình tự trạng thái |
| | QLDH-QĐ5 | `OrderService.cancel()` | giải phóng toàn bộ `OrderStockHold` |
| | QLDH-QĐ6 | `OrderPricingService.calculateTotal()` | công thức tổng thanh toán |
| | QLDH-QĐ7 | `ReturnRequestService.processApproved()` | cập nhật tồn theo quyết định xử lý thực tế |
| Marketing | QLKM-QĐ1, QĐ2 | `PromotionService` (nếu tách riêng khỏi Voucher) | thời gian/phạm vi/trạng thái; không xoá khi có lịch sử |
| | QLKM-QĐ3 | `VoucherService.create/update()` | đủ field bắt buộc |
| | QLKM-QĐ4 | `VoucherService.validate()` | thời gian + lượt dùng + đơn tối thiểu + đối tượng |
| | QLKM-QĐ5 | `VoucherService.calculateDiscount()` | không âm, áp trần giảm tối đa |
| Nhân sự | QLNV-QĐ1, QĐ2 | `EmployeeService.create/deactivate()` | định danh duy nhất; không xoá lịch sử |
| | QLNV-QĐ3 | `EmployeeStoreService.assign()` | gán phạm vi chi nhánh |
| | QLNV-QĐ4 | `EmployeeRoleService`, `RolePermissionService` | quyền theo vai trò + phạm vi |
| | QLNV-QĐ5 | `AuditLogService.record()` | ghi log mọi thao tác quản trị quan trọng |
| Báo cáo | BCTK-QĐ1..QĐ3 | `ReportService.revenueByPeriod/byStore()`, `.topSellingProducts()`, `.promotionEffectiveness()` | chỉ tính đơn `COMPLETED`; theo SKU bán trong khoảng thời gian; theo lượt dùng + giá trị ưu đãi |

---

## 10. DTO Design

Không expose Entity trực tiếp; mỗi module có `XxxRequest` (validate `@Valid`) và `XxxResponse`/`XxxDetailResponse` (ẩn field nội bộ, không kéo toàn bộ quan hệ lazy).

| Nhóm | DTO tiêu biểu | Ghi chú |
|---|---|---|
| Account | `RegisterRequest`, `LoginRequest`, `ChangePasswordRequest`, `ProfileUpdateRequest`, `AddressRequest`, `BeautyProfileRequest` | |
| Catalog | `ProductRequest`, `ProductSkuRequest`, `CategoryRequest`, `BrandRequest`, `AttributeRequest`, `ProductImageRequest` → `ProductResponse` (danh sách, nhẹ), `ProductDetailResponse` (SKU, ảnh, thuộc tính, đánh giá, sản phẩm gợi ý) | tách Response/DetailResponse như thiết kế cũ |
| Store/Inventory | `StoreRequest`, `InventoryAdjustmentRequest`, `StockTransferRequest` → `StoreResponse`, `StoreStockResponse` (tồn khả dụng theo cửa hàng cho trang chi tiết SP) | |
| Cart/Wishlist | `CartItemRequest` → `CartResponse` (kèm tạm tính) | |
| Order | `CheckoutRequest` (địa chỉ, voucher, ghi chú), `OrderStatusUpdateRequest` → `OrderResponse`, `OrderDetailResponse` | |
| Purchase | `SupplierRequest`, `PurchaseOrderRequest` → `PurchaseOrderResponse` | |
| Promotion | `VoucherRequest` → `VoucherResponse` | |
| Review | `ReviewRequest`, `ReviewModerationRequest` → `ReviewResponse` | |
| Return | `ReturnRequestCreateRequest`, `ReturnDecisionRequest` → `ReturnResponse` | |
| Employee | `EmployeeRequest`, `RoleRequest`, `PermissionAssignRequest` → `EmployeeResponse` | |
| Chung | `ApiResponse<T>`, `PageResponse<T>`, `ReportRevenueResponse`, `ReportTopProductResponse` | |

---

## 11. Mapper Design

Dùng **mapper thủ công** (không MapStruct — quy mô 37 entity vẫn quản lý được bằng tay, và tránh thêm annotation-processor phức tạp cho project sinh viên 2 người). Điểm cần chú ý so với thiết kế cũ:

- Entity có `@EmbeddedId` (VD `CartItem`) không có field `id` riêng — mapper nhận `Cart`/`ProductSku` đã load sẵn, không tự tạo khóa.
- `OrderMapper.toEntity()` **không** copy giá/tên hiện tại của `ProductSku` — nó nhận giá đã snapshot từ bước tính giá (`OrderPricingService`) để đảm bảo QLDH-QĐ1.
- `InventoryMapper.toResponse()` tự tính `availableStock = actualStock - heldQuantity` khi map sang `StoreStockResponse`.

```java
public class OrderMapper {
    public static OrderItem toOrderItem(ProductSku sku, int quantity, BigDecimal unitPriceSnapshot) {
        return OrderItem.builder()
            .sku(sku)
            .productNameSnapshot(sku.getProduct().getName())
            .variantNameSnapshot(sku.getVariantName())
            .unitPriceSnapshot(unitPriceSnapshot)
            .quantity(quantity)
            .lineTotal(unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity)))
            .build();
    }
}
```

---

## 12. Controller Design (Thymeleaf) — theo 8 màn hình SRS + khu vực Admin

| Màn hình SRS (Mục 5) | Controller / method | Template |
|---|---|---|
| Trang chủ | `HomeController.index()` | `home/index.html` |
| Danh sách sản phẩm | `ProductController.list(categoryId, brandId, priceMin, priceMax, skinType, sort, Pageable)` | `product/list.html` |
| Chi tiết sản phẩm | `ProductController.detail(id)` | `product/detail.html` |
| Đăng nhập | `AuthController.loginForm()/login()` | `auth/login.html` |
| Đăng ký | `AuthController.registerForm()/register()` | `auth/register.html` |
| Giỏ hàng | `CartController.view()` | `cart/view.html` |
| Giao hàng & thanh toán | `CheckoutController.checkoutForm()/placeOrder()` | `checkout/checkout.html` |
| Đặt hàng thành công | `OrderController.success(orderId)` | `order/success.html` |

Khu vực khách hàng bổ sung: `AccountController` (hồ sơ/địa chỉ/Beauty Profile), `WishlistController`, `OrderController.history()/detail()/cancel()`.

Khu vực Admin (mỗi controller gọi Service tương ứng, không truy cập Repository/không chứa business logic):
`AdminDashboardController`, `AdminProductController`, `AdminCategoryBrandController`, `AdminAttributeController`, `AdminStoreController`, `AdminSupplierController`, `AdminPurchaseOrderController`, `AdminInventoryController`, `AdminStockTransferController`, `AdminOrderController`, `AdminReturnController`, `AdminReviewController`, `AdminVoucherController`, `AdminEmployeeController`, `AdminCustomerController`, `AdminAuditLogController`, `AdminReportController`.

```java
@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) { this.productService = productService; }

    @GetMapping
    public String list(@RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Long brandId,
                        Pageable pageable, Model model) {
        model.addAttribute("products", productService.filter(categoryId, brandId, pageable));
        return "product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getDetail(id));
        return "product/detail";
    }
}
```

---

## 13. REST API Design

Convention giữ nguyên `/api/...`, response chuẩn `ApiResponse<T>` / `PageResponse<T>`.

| Module | Endpoint tiêu biểu |
|---|---|
| Product | `GET /api/products?categoryId=&brandId=&skinType=&priceMin=&priceMax=&keyword=&page=&size=` · `GET /api/products/{id}` |
| Cart | `GET /api/cart` · `POST /api/cart/items` · `PUT /api/cart/items/{skuId}` · `DELETE /api/cart/items/{skuId}` |
| Wishlist | `GET /api/wishlist` · `POST /api/wishlist/{productId}` · `DELETE /api/wishlist/{productId}` |
| Voucher | `POST /api/vouchers/validate` (body: code, cartTotal) |
| Order | `POST /api/orders` (checkout) · `GET /api/orders/{id}` · `PATCH /api/orders/{id}/cancel` |
| Review | `POST /api/products/{id}/reviews` · `GET /api/products/{id}/reviews` |
| Store locator | `GET /api/stores/nearby?lat=&lng=` · `GET /api/stores/{id}/stock?skuId=` |
| Admin – Product | `POST/PUT/DELETE /api/admin/products`, `/api/admin/product-skus`, `/api/admin/products/{id}/images` |
| Admin – Inventory | `GET /api/admin/inventories?storeId=&lowStock=true` · `POST /api/admin/inventories/adjust` |
| Admin – Purchase | `POST /api/admin/purchase-orders` · `PATCH /api/admin/purchase-orders/{id}/confirm` |
| Admin – Stock transfer | `POST /api/admin/stock-transfers` · `PATCH /api/admin/stock-transfers/{id}/ship` · `.../receive` |
| Admin – Order | `GET /api/admin/orders?status=` · `PATCH /api/admin/orders/{id}/confirm` · `.../assign-store` · `.../status` |
| Admin – Return | `PATCH /api/admin/returns/{id}/decision` |
| Admin – Voucher | `POST/PUT /api/admin/vouchers` |
| Admin – Employee | `POST/PUT /api/admin/employees` · `PUT /api/admin/employees/{id}/roles` |
| Admin – Report | `GET /api/admin/reports/revenue?from=&to=&storeId=` · `/top-products` · `/low-stock` · `/promotion-effectiveness` |

Pagination response, HTTP status, error response giữ nguyên format đã thống nhất trước đó (`200/201/204/400/404/409`).

---

## 14. Exception Design

```
exception/
├── ResourceNotFoundException          (404)
├── BusinessException                  (400/409)
├── InsufficientStockException         (409 – QLTK-QĐ8, QLDH-QĐ2/QĐ3)
├── InvalidVoucherException            (400 – KH-QĐ8, QLKM-QĐ4)
├── InvalidStatusTransitionException   (409 – QLDH-QĐ4, QLTK-QĐ5)
└── GlobalExceptionHandler             (@RestControllerAdvice)
```

`MethodArgumentNotValidException` (Spring Validation) bắt chung trong `GlobalExceptionHandler`, không cần `ValidationException` riêng.

---

## 15. Validation

| DTO | Input validation (`@Valid`) | Business validation (Service) |
|---|---|---|
| `RegisterRequest` | `@Email`, `@NotBlank`, `@Size(min=6)` password | email/SĐT chưa tồn tại (KH-QĐ3) |
| `ProductRequest` | `@NotBlank name`, `@NotNull categoryId` | danh mục/thương hiệu tồn tại (QLSP-QĐ1) |
| `ProductSkuRequest` | `@NotBlank skuCode`, `@Positive sellingPrice` | mã SKU duy nhất (QLSP-QĐ5) |
| `CartItemRequest` | `@Positive quantity` | SKU đang `ACTIVE` (KH-QĐ7) |
| `CheckoutRequest` | `@NotEmpty items`, `@NotNull addressId` | đủ tồn khả dụng 1 chi nhánh (QLDH-QĐ3), voucher hợp lệ (KH-QĐ8) |
| `InventoryAdjustmentRequest` | `@NotBlank reason` | không âm sau điều chỉnh (QLTK-QĐ3) |
| `VoucherRequest` | `@NotBlank code`, `@Positive value` | không trùng code, thời gian hợp lệ (QLKM-QĐ3) |
| `ReviewRequest` | `@Min(1) @Max(5) rating` | có `OrderItem` chứng minh đã mua & đơn hoàn thành (KH-QĐ15) |
| `ReturnRequestCreateRequest` | `@NotBlank reason` | đơn/sản phẩm đủ điều kiện theo chính sách trả hàng (KH-QĐ14) |

---

## 16. Spring Security Design (Role + Permission động)

- `Account.accountType = CUSTOMER` → authority `ROLE_CUSTOMER`.
- `Account.accountType = EMPLOYEE` → nạp `Employee → EmployeeRole → Role` thành `ROLE_<TÊN_VAI_TRÒ>` **và** `Role → RolePermission → Permission` thành `PERM_<MÃ_QUYỀN>`. `CustomUserDetailsService` gộp toàn bộ thành tập `GrantedAuthority`.
- Phân quyền URL (thô):
  ```java
  .requestMatchers("/admin/**", "/api/admin/**").hasAnyRole("PRODUCT_MANAGER","CHAIN_ADMIN","PURCHASING_STAFF",
        "WAREHOUSE_STAFF","CUSTOMER_SERVICE","ORDER_STAFF","MARKETING_STAFF","SYSTEM_ADMIN")
  .requestMatchers("/api/products/**", "/products/**").permitAll()
  .anyRequest().authenticated()
  ```
- Phân quyền hành động (tinh, method-level) dùng `@PreAuthorize("hasAuthority('PERM_PRODUCT_MANAGE')")` trên Service, **không** chỉ ẩn nút ở Thymeleaf (`sec:authorize` chỉ phục vụ UX).
- **Phạm vi theo cửa hàng**: nhân viên chỉ thao tác trong `EmployeeStore` của mình trừ khi có quyền `STORE_VIEW_ALL`/thuộc `ROLE_CHAIN_ADMIN`/`ROLE_SYSTEM_ADMIN`. Kiểm tra qua `SecurityUtils.getCurrentEmployeeStoreIds()` áp dụng ở Service khi truy vấn `Inventory`, `PurchaseOrder.receivingStore`, `Order.processingStore`, `StockTransfer` (QLNV-QĐ3, QLNV-QĐ4).
- Thymeleaf: thêm `thymeleaf-extras-springsecurity6`, dùng `sec:authorize="hasAuthority('PERM_INVENTORY_ADJUST')"` để ẩn/hiện thao tác trong template admin.
- CSRF: bật mặc định của Spring Security; AJAX/fetch gửi kèm header `X-CSRF-TOKEN` lấy từ `<meta name="_csrf" ...>` do Thymeleaf render trong `layout/admin-layout.html`.

**Seed dữ liệu mẫu** (`DataSeeder`): tạo 9 Role (bảng Mục 4) + danh sách Permission tương ứng + gán `RolePermission` mặc định khi khởi tạo hệ thống lần đầu (idempotent, chỉ chạy nếu bảng rỗng).

---

## 17. Thymeleaf Structure

- `layout/main-layout.html` (khách hàng), `layout/admin-layout.html` (khu quản trị, có sidebar theo quyền).
- `fragments/`: header, navbar, footer, pagination, admin-sidebar, form-error — tái sử dụng qua `th:insert`/`th:replace`.
- Mỗi module có thư mục riêng khớp Mục 12 (bảng controller↔template).

## 18. Static CSS/JS Structure

`common.css/js` dùng toàn site; `home.css/js`, `product.css/js`, `cart.css/js`, `checkout.css/js`, `account.css/js` cho khách hàng; `admin-common.css/js` + `admin-product.js`, `admin-inventory.js`, `admin-order.js`, `admin-report.js` (biểu đồ dashboard, có thể dùng Chart.js qua CDN) cho khu quản trị. Không gộp file lớn — mỗi trang chỉ load CSS/JS cần thiết.

---

## 19. PostgreSQL + Docker

**docker-compose.yml**
```yaml
services:
  postgres:
    image: postgres:16
    container_name: lunea-db
    environment:
      POSTGRES_DB: ${DB_NAME:-lunea}
      POSTGRES_USER: ${DB_USER:-lunea_user}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - lunea_pgdata:/var/lib/postgresql/data

volumes:
  lunea_pgdata:
```

**application-dev.properties**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/${DB_NAME:lunea}
spring.datasource.username=${DB_USER:lunea_user}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
# tùy chọn: pg_trgm để tìm kiếm gần đúng theo tên sản phẩm
# CREATE EXTENSION IF NOT EXISTS pg_trgm;
cloudinary.url=${CLOUDINARY_URL:}
```

Mật khẩu/API key (DB, Cloudinary) lấy từ biến môi trường (`.env`, không commit).

## 20. Configuration

`application.properties` (`spring.profiles.active=dev`), `application-dev.properties` (DB local, `show-sql=true`), `application-prod.properties` (`ddl-auto=validate`, log WARN, secret qua env).

---

## 21. Git Workflow & Team Ownership

Không đổi so với thiết kế trước: `main`, `develop`, `feature/thinh-...`, `feature/nguyen-...`; commit convention `feat/fix/refactor/docs/test/style/chore`.

| Package | Thịnh | Nguyên |
|---|---|---|
| entity, repository, service, dto, mapper | ✅ | |
| controller, rest, templates, static, security | | ✅ |

Do quy mô 37 entity, **thống nhất trước theo nhóm domain** (account → catalog → store/inventory → cart/order → return/review → purchase) thay vì làm tất cả cùng lúc; Thịnh chốt Entity+DTO+interface Service của một domain rồi push, Nguyên mới bắt đầu Controller domain đó — giảm conflict khi cả hai cùng làm sản phẩm phức tạp gồm nhiều bảng liên quan (Product/Sku/Attribute/Image).

---

## 22. Testing Strategy

Ưu tiên (do độ phức tạp nghiệp vụ tăng mạnh so với bản trước):
- `OrderServiceTest`: checkout tạo hold đúng, huỷ giải phóng hold, tính tổng tiền/voucher đúng công thức.
- `InventoryServiceTest`: `available = actual - held` không âm, cảnh báo tồn thấp.
- `SecurityPermissionTest`: nhân viên không có quyền/không thuộc phạm vi cửa hàng bị từ chối (403).
- `ProductRepositoryTest`, `OrderRepositoryTest` (`@DataJpaTest`) cho các query filter/thống kê quan trọng.
- `ProductRestControllerTest`, `OrderRestControllerTest` (`@WebMvcTest`) cho API quan trọng.

Không bắt buộc test CRUD đơn giản (Brand, Category, Supplier, Attribute).

## 23. Documentation

`README.md`, `API_DOCUMENTATION.md`, `DATABASE.md` (kèm bảng ánh xạ Mục 5 + ERD), `GIT_GUIDE.md`.

---

## 24. Quy tắc naming

- Class PascalCase, method/field camelCase, table/column snake_case số nhiều.
- Lớp khóa nhúng: `<Entity>Id` (VD `CartItemId`, `InventoryId`).
- Enum hằng số: UPPER_SNAKE_CASE, **đúng miền giá trị đã khai báo trong SRS** khi có (VD `HELD/RELEASED/COMMITTED`).
- Permission code: `<DOMAIN>_<ACTION>` UPPER_SNAKE_CASE (VD `PRODUCT_MANAGE`, `INVENTORY_ADJUST`).
- Branch: `feature/<ten>-<domain>-<phan>` (VD `feature/thinh-order-service`).
- REST endpoint: danh từ số nhiều, không dùng verb.

## 25. Definition of Done

Không đổi + bổ sung: mọi action nhạy cảm (điều chỉnh tồn, xác nhận đơn, xác nhận nhập hàng, đổi trạng thái) phải **ghi `AuditLog`** và có kiểm tra quyền `@PreAuthorize` tương ứng trước khi merge.

---

## 26. Ví dụ luồng nghiệp vụ hoàn chỉnh: Đặt hàng (Checkout)

**Entity liên quan:** `Cart`, `CartItem`, `Inventory`, `Order`, `OrderItem`, `OrderStockHold`, `Voucher`.

1. `CartRestController.checkout()` nhận `CheckoutRequest` (địa chỉ, mã voucher, ghi chú).
2. `OrderService.checkout()` (`@Transactional`):
   a. Lấy `CartItem` hiện tại của khách hàng.
   b. **Xác định chi nhánh xử lý** (QLDH-QĐ3): `InventoryService.findStoreWithEnoughStock(cartItems)` — chỉ chọn 1 `Store` có `available_stock` đủ cho **toàn bộ** SKU trong giỏ; nếu không có → `InsufficientStockException`.
   c. Áp dụng voucher qua `VoucherService.validate()` (KH-QĐ8, QLKM-QĐ4).
   d. Tính tổng tiền qua `OrderPricingService.calculateTotal()` (KH-QĐ9, QLDH-QĐ6).
   e. Tạo `Order` + `OrderItem` snapshot tên/giá (QLDH-QĐ1) qua `OrderMapper`.
   f. Tạo `OrderStockHold` cho từng SKU tại chi nhánh đã chọn, đồng thời `Inventory.heldQuantity += quantity` (QLTK-QĐ6) — **cùng transaction** với bước (e) để đảm bảo nhất quán.
   g. Xoá `CartItem` của khách hàng.
3. Khi nhân viên xác nhận đơn (`OrderService.confirm()`, QLDH-QĐ2): chuyển `OrderStockHold.status = COMMITTED`, đồng thời `Inventory.actualStock -= quantity` và `Inventory.heldQuantity -= quantity` (tồn thực tế chỉ trừ khi đơn đã được xác nhận, không trừ ngay lúc đặt — **giả định thiết kế** vì SRS không quy định thời điểm trừ tồn thực tế chính xác, chỉ quy định thời điểm giữ/giải phóng).
4. Khi huỷ đơn trước `SHIPPING` (`OrderService.cancel()`, KH-QĐ13/QLDH-QĐ5): mọi `OrderStockHold` chưa `COMMITTED` chuyển `RELEASED`, `Inventory.heldQuantity -= quantity`.
5. Controller trả về `order/success.html` với `OrderResponse` (mã đơn, trạng thái, phương thức thanh toán, tổng thanh toán).

**Controller:** `CheckoutController.placeOrder()` (Thymeleaf) / `OrderRestController.checkout()` (JSON, dùng khi checkout qua AJAX).

---

## 27. Checklist trước khi bắt đầu code

- [ ] Thống nhất bảng ánh xạ Mục 5 (không đổi tên entity giữa chừng).
- [ ] Thống nhất field `Order`/`Inventory`/`OrderStockHold` trước (nhóm nghiệp vụ phức tạp nhất).
- [ ] Docker + PostgreSQL chạy local; seed Role/Permission/Store mặc định qua `DataSeeder`.
- [ ] `pom.xml` gồm: Spring Web, JPA, Validation, Security, Thymeleaf, `thymeleaf-extras-springsecurity6`, PostgreSQL driver, Lombok.
- [ ] File `.env.example` không chứa secret thật.

## 28. Checklist trước khi merge vào develop

- [ ] Build thành công (`mvn clean install`).
- [ ] Không có business logic trong Controller; Controller không gọi Repository.
- [ ] Validate input DTO + business validation đúng mã QĐ liên quan (Mục 9).
- [ ] Method nhạy cảm có `@PreAuthorize` + ghi `AuditLog`.
- [ ] Không âm tồn khả dụng trong mọi kịch bản test thủ công (đặt hàng/huỷ/điều chỉnh/điều chuyển).
- [ ] Không có secret/`.env` trong commit; đã được người còn lại review.

---

*Tài liệu này bám sát tài liệu SRS gốc (Chương 1-5) của Nhóm 01; các điểm không được SRS quy định chi tiết (VD: trạng thái `ReturnRequest`, thời điểm trừ tồn thực tế) được đánh dấu rõ là **giả định thiết kế** để nhóm có thể điều chỉnh khi bảo vệ đồ án nếu giảng viên yêu cầu khác.*

Bạn có muốn tôi tiếp tục tạo skeleton code (Entity, Repository, Service, DTO, Controller, Security config, Docker) theo kiến trúc này không? Nếu có, tôi đề xuất bắt đầu theo từng domain (account → catalog → store/inventory → cart/order → return/review/purchase) thay vì tạo toàn bộ cùng lúc.
