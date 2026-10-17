# LUNEA – Thiết kế Kiến trúc Repository Spring Boot

---

## 1. Tổng quan kiến trúc

LUNEA dùng kiến trúc **Layered Architecture (Controller → Service → Repository → Entity)** kết hợp **Hybrid Controller** (vừa `@Controller` cho Thymeleaf, vừa `@RestController` cho API động). Đây là kiến trúc phù hợp nhất cho team 2 người vì:

- Ranh giới trách nhiệm rõ: Thịnh làm tầng dữ liệu/logic, Nguyên làm tầng trình bày/API.
- Không cần microservice, không cần CQRS, không cần nhiều layer trừu tượng — vẫn là **monolith 1 module Maven**.
- DTO là "hợp đồng" giữa 2 người — đây là điểm cả hai cần thống nhất sớm.

---

## 2. Sơ đồ kiến trúc (ASCII)

```
┌─────────────────────────────┐
│   Thymeleaf View (HTML)     │  ← Nguyên
└──────────────┬───────────────┘
               │ render / form submit
┌──────────────▼───────────────┐
│   @Controller (page)         │  ← Nguyên
│   @RestController (json)     │
└──────────────┬───────────────┘
               │ gọi qua DTO
┌──────────────▼───────────────┐
│   Service / ServiceImpl      │  ← Thịnh
│   (business logic, @Transactional) │
└──────────────┬───────────────┘
               │ gọi qua Entity
┌──────────────▼───────────────┐
│   Repository (JpaRepository) │  ← Thịnh
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│   Entity (JPA/Hibernate)     │  ← Thịnh
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│        PostgreSQL            │
└───────────────────────────────┘

Frontend JS (fetch/AJAX) ──► REST Controller ──► DTO ──► Service
```

---

## 3. Cây thư mục hoàn chỉnh

```
lunea/
├── docker-compose.yml
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
│   │   │   │   └── DataInitializer.java (optional, seed data)
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── BrandController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── CheckoutController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   └── admin/
│   │   │   │       ├── AdminProductController.java
│   │   │   │       ├── AdminOrderController.java
│   │   │   │       └── AdminDashboardController.java
│   │   │   ├── rest/
│   │   │   │   ├── ProductRestController.java
│   │   │   │   ├── CategoryRestController.java
│   │   │   │   ├── CartRestController.java
│   │   │   │   ├── OrderRestController.java
│   │   │   │   └── ReviewRestController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── ProductRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── OrderRequest.java
│   │   │   │   │   ├── AddressRequest.java
│   │   │   │   │   └── ReviewRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ProductResponse.java
│   │   │   │       ├── ProductDetailResponse.java
│   │   │   │       ├── CategoryResponse.java
│   │   │   │       ├── OrderResponse.java
│   │   │   │       ├── CartResponse.java
│   │   │   │       ├── ApiResponse.java
│   │   │   │       └── PageResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Product.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Brand.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Cart.java
│   │   │   │   ├── CartItem.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderDetail.java
│   │   │   │   ├── Review.java
│   │   │   │   ├── Address.java
│   │   │   │   ├── Voucher.java
│   │   │   │   └── base/BaseEntity.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── BrandRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── CartRepository.java
│   │   │   │   ├── CartItemRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── OrderDetailRepository.java
│   │   │   │   ├── ReviewRepository.java
│   │   │   │   ├── AddressRepository.java
│   │   │   │   └── VoucherRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── BrandService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── CartService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   ├── AddressService.java
│   │   │   │   ├── VoucherService.java
│   │   │   │   └── impl/
│   │   │   │       ├── ProductServiceImpl.java
│   │   │   │       ├── CategoryServiceImpl.java
│   │   │   │       ├── BrandServiceImpl.java
│   │   │   │       ├── UserServiceImpl.java
│   │   │   │       ├── CartServiceImpl.java
│   │   │   │       ├── OrderServiceImpl.java
│   │   │   │       ├── ReviewServiceImpl.java
│   │   │   │       ├── AddressServiceImpl.java
│   │   │   │       └── VoucherServiceImpl.java
│   │   │   ├── mapper/
│   │   │   │   ├── ProductMapper.java
│   │   │   │   ├── CategoryMapper.java
│   │   │   │   ├── OrderMapper.java
│   │   │   │   └── UserMapper.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── security/
│   │   │       ├── CustomUserDetailsService.java
│   │   │       ├── CustomUserPrincipal.java
│   │   │       └── SecurityUtils.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   ├── common.css
│   │       │   │   ├── product.css
│   │       │   │   ├── cart.css
│   │       │   │   └── admin.css
│   │       │   ├── js/
│   │       │   │   ├── common.js
│   │       │   │   ├── product.js
│   │       │   │   ├── cart.js
│   │       │   │   └── admin.js
│   │       │   └── images/
│   │       ├── templates/
│   │       │   ├── fragments/
│   │       │   │   ├── header.html
│   │       │   │   ├── navbar.html
│   │       │   │   ├── footer.html
│   │       │   │   ├── sidebar-admin.html
│   │       │   │   └── pagination.html
│   │       │   ├── layout/
│   │       │   │   ├── main-layout.html
│   │       │   │   └── admin-layout.html
│   │       │   ├── home/index.html
│   │       │   ├── product/list.html, detail.html, form.html
│   │       │   ├── category/list.html
│   │       │   ├── brand/list.html
│   │       │   ├── cart/view.html
│   │       │   ├── checkout/checkout.html
│   │       │   ├── order/history.html, detail.html
│   │       │   ├── customer/login.html, register.html, profile.html
│   │       │   └── admin/dashboard.html, product-list.html, order-list.html
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test/java/com/lunea/
│       ├── service/ProductServiceTest.java, OrderServiceTest.java
│       ├── repository/ProductRepositoryTest.java
│       └── controller/ProductRestControllerTest.java
```

---

## 4. Giải thích vai trò từng package

| Package | Vai trò | Ai chỉnh |
|---|---|---|
| `config` | Cấu hình Spring (Security, MVC, seed data) | Nguyên (Security), chung cho phần còn lại |
| `controller` | Trả về view Thymeleaf, xử lý form | Nguyên |
| `rest` | Trả JSON, phục vụ AJAX/fetch | Nguyên |
| `dto/request` | Dữ liệu client gửi lên, có validation | Thịnh định nghĩa field, Nguyên dùng |
| `dto/response` | Dữ liệu trả về client, ẩn field nhạy cảm | Thịnh |
| `entity` | Mapping bảng DB | Thịnh |
| `repository` | Truy vấn DB, kế thừa `JpaRepository` | Thịnh |
| `service` + `impl` | Business logic, transaction | Thịnh |
| `mapper` | Chuyển đổi Entity ↔ DTO | Thịnh |
| `exception` | Exception nghiệp vụ + xử lý lỗi tập trung | Thịnh (exception), Nguyên (format lỗi hiển thị) |
| `security` | Authentication/Authorization | Nguyên (chủ), Thịnh hỗ trợ (UserDetails cần User entity) |

---

## 5. Danh sách module (Entity cần thiết)

**Giữ lại (cần thiết):**
`Product, Category, Brand, User, Role, Cart, CartItem, Order, OrderDetail, Review, Address, Voucher`

**Gộp / không tạo riêng:**
- **Payment**: Không cần Entity riêng ở giai đoạn đầu — gộp thành field `paymentMethod`, `paymentStatus` trong `Order`. Chỉ tách `Payment` entity riêng nếu sau này tích hợp cổng thanh toán thật (VNPay, Momo) cần lưu transaction_id, response code…
- **Inventory/Stock**: Không tách bảng riêng — dùng field `stock` (số lượng tồn) ngay trong `Product`. Tách riêng chỉ cần khi có nhiều kho/chi nhánh thật sự quản lý tồn kho độc lập (chuỗi cửa hàng có thể cần, nêu ở phần mở rộng).
- **Role**: Có thể đơn giản hoá thành enum `RoleName` trong `Role` entity (bảng nhỏ, ít thay đổi) — không cần bảng phức tạp.

**Bảng trung gian (không dùng `@ManyToMany` trực tiếp):**
- `CartItem` — trung gian giữa `Cart` và `Product`.
- `OrderDetail` — trung gian giữa `Order` và `Product`.

---

## 6. Entity Relationship

```
User (1) ────< (N) Order
User (1) ────< (N) Address
User (1) ────< (N) Review
User (N) >──── (1) Role         [Many-to-One]

Category (1) ──< (N) Product
Brand (1) ────< (N) Product

Product (1) ──< (N) CartItem
Product (1) ──< (N) OrderDetail
Product (1) ──< (N) Review

Cart (1) ─────< (N) CartItem
Cart (1) ──── (1) User           [One-to-One]

Order (1) ────< (N) OrderDetail
Order (N) >──── (1) User
Order (N) >──── (0..1) Voucher
Order (1) ──── (1) Address       [Many-to-One thực chất, snapshot địa chỉ giao hàng]
```

Không dùng `@ManyToMany` ở đâu cả — mọi quan hệ N-N (Product ↔ Order, Product ↔ Cart) đều đi qua bảng trung gian có nghĩa nghiệp vụ rõ ràng (`OrderDetail`, `CartItem`), vì các bảng này cần thêm field (`quantity`, `price`, `subtotal`).

---

## 7. Entity Design

**BaseEntity** (abstract, `@MappedSuperclass`): `id (Long, IDENTITY)`, `createdAt`, `updatedAt` (dùng `@CreationTimestamp`/`@UpdateTimestamp`).

### Product
| Field | Java | PostgreSQL | Null | Unique | Ghi chú |
|---|---|---|---|---|---|
| id | Long | BIGSERIAL | No | PK | |
| name | String | VARCHAR(255) | No | No | index |
| slug | String | VARCHAR(255) | No | Yes | dùng cho URL |
| description | String | TEXT | Yes | | |
| price | BigDecimal | NUMERIC(12,2) | No | | |
| stock | Integer | INTEGER | No | | default 0 |
| imageUrl | String | VARCHAR(500) | Yes | | |
| status | Enum(ProductStatus) | VARCHAR | No | | ACTIVE/INACTIVE |
| category | Category | FK | No | | `@ManyToOne(FetchType.LAZY)` |
| brand | Brand | FK | Yes | | `@ManyToOne(FetchType.LAZY)` |

Index: `name`, `category_id`, `brand_id`, `slug` (unique).

### Category / Brand
Đơn giản: `id, name, slug (unique), description`. Category có thể self-reference (`parentId`) nếu cần category cha-con — **chỉ thêm nếu thực sự có nhu cầu phân cấp**, ban đầu để phẳng (flat).

### User
`id, fullName, email (unique), password, phone, status, role (ManyToOne, LAZY)`.

### Role
`id, name (enum-like String, unique)` — CUSTOMER, STAFF, ADMIN, SUPER_ADMIN.

### Cart
`id, user (OneToOne, LAZY)`. Mỗi User có đúng 1 Cart (tạo tự động khi đăng ký).

### CartItem
`id, cart (ManyToOne), product (ManyToOne), quantity`. Unique constraint `(cart_id, product_id)`.

### Order
`id, user (ManyToOne, LAZY), address (ManyToOne hoặc snapshot fields), totalAmount, status (enum: PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED), paymentMethod, paymentStatus, voucher (ManyToOne, nullable), note`.

### OrderDetail
`id, order (ManyToOne), product (ManyToOne), quantity, price (giá tại thời điểm mua — snapshot, không lấy lại từ Product)`.

### Review
`id, product (ManyToOne), user (ManyToOne), rating (Integer 1-5), comment, createdAt`.

### Address
`id, user (ManyToOne), fullName, phone, province, district, ward, detail, isDefault (boolean)`.

### Voucher
`id, code (unique), discountType (PERCENT/FIXED), discountValue, minOrderValue, expiryDate, quantity`.

**Cascade**: chỉ dùng `CascadeType.ALL` cho quan hệ Cart→CartItem và Order→OrderDetail (vòng đời phụ thuộc hoàn toàn cha). Không cascade cho Product/Category vì chúng độc lập.

**FetchType**: mặc định `LAZY` cho mọi `@ManyToOne`/`@OneToMany` để tránh N+1 và load dư dữ liệu.

---

## 8. Repository Design

Tất cả kế thừa `JpaRepository<Entity, Long>`.

- **Derived query** dùng cho case đơn giản:
  - `findBySlug(String slug)`
  - `findByCategoryId(Long categoryId)`
  - `findByBrandId(Long brandId)`
  - `findByNameContainingIgnoreCase(String keyword, Pageable pageable)`
  - `findByEmail(String email)` (User)
  - `existsByEmail(String email)`

- **`@Query`** chỉ dùng khi derived query không diễn tả được (join nhiều điều kiện, tính toán):
  ```java
  @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.price BETWEEN :min AND :max")
  Page<Product> filterByCategoryAndPrice(...)
  ```

- **`Pageable`** dùng cho mọi API danh sách sản phẩm, đơn hàng, review (tránh load hết bảng).

Không viết native SQL trừ khi thực sự cần thống kê phức tạp (dashboard admin).

---

## 9. Service Design

Ví dụ `ProductService` (interface) / `ProductServiceImpl`:

- `getById(Long id)` → ném `ResourceNotFoundException` nếu không có.
- `getAll(Pageable)`
- `search(String keyword, Pageable)`
- `filter(Long categoryId, Long brandId, BigDecimal min, BigDecimal max, Pageable)`
- `create(ProductRequest)` — validate business (ví dụ: giá > 0, category tồn tại).
- `update(Long id, ProductRequest)`
- `delete(Long id)` — soft delete (đổi status INACTIVE) thay vì xoá cứng, vì đã có đơn hàng tham chiếu.

**Method không cần**: không cần `save()` generic lộ ra ngoài, không cần `findAll()` không phân trang (tránh lộ ra full-table scan).

**`@Transactional`**: đặt ở method `create/update/delete` và mọi flow nhiều bước (đặt hàng: trừ tồn kho + tạo Order + tạo OrderDetail + xoá CartItem phải cùng 1 transaction).

**Exception nghiệp vụ**: `BusinessException` khi vi phạm rule (VD: đặt hàng vượt tồn kho, voucher hết hạn).

---

## 10. DTO Design

- **Request DTO**: dùng khi client gửi dữ liệu lên (create/update). Có annotation validate (`@NotBlank`, `@Positive`...).
- **Response DTO**: dùng khi trả dữ liệu ra ngoài — ẩn field nhạy cảm (password), tránh lazy-loading exception khi serialize JSON.
- **Detail DTO** (`ProductDetailResponse`): dùng cho trang chi tiết cần nhiều thông tin hơn (review, related products) — tách riêng khỏi `ProductResponse` (dùng cho danh sách, ít field hơn để nhẹ).
- **Dùng chung DTO**: `ProductRequest` có thể dùng chung cho cả create và update (không cần `ProductCreateRequest`/`ProductUpdateRequest` riêng) trừ khi rule khác nhau rõ rệt.

---

## 11. Mapper Design

Dùng **mapper thủ công** (class `XxxMapper` với static method hoặc bean), **không dùng MapStruct** cho project này vì:
- Team 2 người, số lượng entity vừa phải (~12) → mapper tay dễ đọc, dễ debug hơn.
- MapStruct thêm annotation processor, build phức tạp hơn, không đáng cho quy mô này.

```java
public class ProductMapper {
    public static ProductResponse toResponse(Product p) { ... }
    public static Product toEntity(ProductRequest r, Category c, Brand b) { ... }
}
```

---

## 12. Controller Design (skeleton)

```java
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                        Pageable pageable, Model model) {
        model.addAttribute("products", productService.search(keyword, pageable));
        return "product/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        return "product/detail";
    }
}
```

---

## 13. REST API Design

Convention:
```
GET    /api/products?page=0&size=10&keyword=serum&categoryId=1
GET    /api/products/{id}
POST   /api/products          (ADMIN)
PUT    /api/products/{id}     (ADMIN)
DELETE /api/products/{id}     (ADMIN)
```

Response chuẩn hoá (dùng chung `ApiResponse<T>`):
```json
{
  "success": true,
  "message": "OK",
  "data": { ... }
}
```

Pagination response:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

HTTP status: `200` OK, `201` Created, `204` No Content (delete), `400` Validation, `404` Not Found, `409` Business conflict.

---

## 14. Exception Design

```
exception/
├── ResourceNotFoundException   (404)
├── BusinessException           (400/409)
└── GlobalExceptionHandler      (@RestControllerAdvice)
```

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    // ... BusinessException, MethodArgumentNotValidException
}
```

Không cần `ValidationException` riêng — dùng sẵn `MethodArgumentNotValidException` của Spring Validation, bắt trong `GlobalExceptionHandler`.

---

## 15. Validation

| Entity/Request | Validation |
|---|---|
| ProductRequest | `@NotBlank name`, `@Positive price`, `@Min(0) stock` |
| RegisterRequest | `@Email email`, `@NotBlank`, `@Size(min=6) password` |
| LoginRequest | `@NotBlank email`, `@NotBlank password` |
| OrderRequest | `@NotEmpty items`, `@NotNull addressId` |
| AddressRequest | `@NotBlank phone`, `@NotBlank province/district/ward` |

**Phân biệt**:
- **Input validation**: đặt annotation trên DTO, kiểm tra ở tầng Controller/`@Valid`.
- **Business validation**: đặt trong Service (VD: email đã tồn tại, tồn kho không đủ, voucher hết hạn) — không thể validate bằng annotation vì cần query DB.

---

## 16. Spring Security

```
security/
├── SecurityConfig
├── CustomUserDetailsService
└── SecurityUtils (lấy current user)
```

- **Authentication**: form login (Thymeleaf) + session, dùng `BCryptPasswordEncoder`.
- **Authorization**:
  ```java
  .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
  .requestMatchers("/staff/**").hasRole("STAFF")
  .requestMatchers("/customer/**").hasRole("CUSTOMER")
  .requestMatchers("/api/products/**").permitAll() // GET công khai
  ```
- **Method security**: dùng `@PreAuthorize` trên Service method quan trọng (VD: `deleteOrder`) — bảo mật ở tầng Service, **không chỉ ẩn nút ở Thymeleaf** (dùng `sec:authorize` chỉ để UX, không phải bảo mật thật).
- **Thymeleaf integration**: thêm dependency `thymeleaf-extras-springsecurity6`, dùng `sec:authorize="hasRole('ADMIN')"`.

---

## 17. Thymeleaf Structure

- `layout/main-layout.html`: layout chính (header, navbar, footer, content slot) dùng Thymeleaf Layout Dialect hoặc `th:replace`.
- `fragments/`: header, navbar, footer, pagination, form-error tách riêng, tái sử dụng qua `th:insert`.
- Mỗi module có thư mục riêng (`product/`, `cart/`...) chứa `list.html`, `detail.html`, `form.html`.

---

## 18. Static CSS/JS Structure

- `common.css/js`: style và hành vi dùng toàn site (navbar, button, layout).
- `product.css/js`: riêng cho trang sản phẩm (filter, gallery ảnh).
- `cart.css/js`: riêng giỏ hàng (update quantity qua AJAX).
- `admin.css/js`: riêng khu quản trị.

Không gộp tất cả vào 1 file lớn — mỗi trang chỉ load CSS/JS nó cần.

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
```

Mật khẩu lấy từ biến môi trường (`.env`, không commit), không hard-code.

---

## 20. Configuration

- `application.properties`: cấu hình chung, `spring.profiles.active=dev`.
- `application-dev.properties`: DB local, `show-sql=true`, log DEBUG.
- `application-prod.properties`: DB thật, `ddl-auto=validate`, log WARN, secret từ env variable.

---

## 21. Git Workflow

Branch: `main`, `develop`, `feature/thinh-*`, `feature/nguyen-*`.

Flow: `feature → develop → main` (merge qua Pull Request, không push thẳng).

Commit convention: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `style:`, `chore:`

Ví dụ:
```
feat: add Product entity and repository
fix: correct price validation in ProductRequest
refactor: extract mapper logic to ProductMapper
docs: update API_DOCUMENTATION for product endpoints
```

---

## 22. Team Ownership & Giảm Conflict

| Package | Chủ yếu Thịnh | Chủ yếu Nguyên |
|---|---|---|
| entity, repository, service, dto, mapper | ✅ | |
| controller, rest, templates, static, security | | ✅ |

**File dễ conflict & cách xử lý:**
- `pom.xml`: chỉ 1 người thêm dependency mỗi lần, thông báo trước trong nhóm chat.
- `application.properties`: tách riêng theo profile, tránh 2 người cùng sửa 1 file.
- `SecurityConfig`: Nguyên chủ trì, Thịnh chỉ review.
- **DTO/Entity**: đây là điểm giao nhau — quy ước: **Thịnh tạo Entity + DTO trước, push lên nhánh riêng, Nguyên pull về rồi mới code Controller dùng DTO đó**. Không để 2 người cùng sửa 1 DTO cùng lúc.

---

## 23. Dependency giữa task Thịnh & Nguyên

Thứ tự làm việc theo module (ví dụ Product):
1. Thịnh: Entity → Repository → Service → DTO (push trước).
2. Nguyên: pull, viết Controller/RestController dùng DTO có sẵn → Thymeleaf.

Nguyên **không cần chờ Thịnh hoàn thiện 100%** — chỉ cần Thịnh chốt **interface Service + DTO** trước (ký hiệu method), code bên trong có thể hoàn thiện sau, Nguyên vẫn compile được nhờ mock/interface.

---

## 24. Testing

```
test/java/com/lunea/
├── service/ProductServiceTest.java   (business logic, mock repository)
├── repository/ProductRepositoryTest.java (@DataJpaTest, query quan trọng)
└── controller/ProductRestControllerTest.java (@WebMvcTest, API quan trọng)
```

Ưu tiên test: Service (business logic), Repository (query filter/search phức tạp), API quan trọng (đặt hàng, thanh toán). Không bắt buộc test CRUD đơn giản (Category, Brand).

---

## 25. Documentation

- `README.md`: giới thiệu, tech stack, kiến trúc, setup, Docker, chạy project, git workflow, thành viên.
- `API_DOCUMENTATION.md`: liệt kê endpoint, request/response mẫu.
- `DATABASE.md`: ERD, mô tả bảng.
- `GIT_GUIDE.md`: quy tắc branch/commit.

---

## 26. Quy tắc naming

- Class: PascalCase (`ProductService`).
- Method/field: camelCase (`getById`).
- Table: snake_case, số nhiều (`products`, `order_details`).
- Column: snake_case (`created_at`).
- DTO: `XxxRequest`, `XxxResponse`, `XxxDetailResponse`.
- Branch: `feature/<ten>-<module>-<phan>` (VD: `feature/thinh-product-service`).
- REST endpoint: số nhiều, danh từ, không dùng verb (`/api/products` không phải `/api/getProducts`).

---

## 27. Definition of Done (mỗi task)

- Code compile, không warning nghiêm trọng.
- Không có business logic trong Controller.
- Có validation input (nếu là Request DTO).
- Có xử lý exception (không để lộ stack trace ra UI/API).
- Đã test thủ công (Postman/UI) ít nhất 1 lần.
- Đã code review bởi người còn lại trước khi merge vào `develop`.
- Không commit file `.env`, secret.

---

## 28. Ví dụ 1 module hoàn chỉnh: Product

**Entity** → `Product.java` (mapping bảng `products`).
**Repository** → `ProductRepository extends JpaRepository<Product, Long>` với `findByNameContainingIgnoreCase`, `findByCategoryId`.
**Service** → `ProductService.getById/search/create/update/delete`, `ProductServiceImpl` xử lý logic + `@Transactional` cho `create/update/delete`.
**DTO** → `ProductRequest` (input), `ProductResponse`/`ProductDetailResponse` (output).
**Mapper** → `ProductMapper.toResponse()`, `.toEntity()`.
**Controller** → `ProductController` (`/products`, Thymeleaf) trả `product/list.html`, `product/detail.html`.
**REST** → `ProductRestController` (`/api/products`) cho search/filter AJAX ở trang danh sách.
**Thymeleaf** → `product/list.html` dùng `th:each` render danh sách, gọi `product.js` để filter động qua `/api/products`.

---

## 29. Checklist trước khi bắt đầu code

- [ ] Đã thống nhất field của Entity/DTO chính (Product, Order trước tiên).
- [ ] Đã tạo branch `develop`, mỗi người tạo `feature/...` riêng.
- [ ] Đã cấu hình Docker + PostgreSQL chạy được local.
- [ ] Đã thống nhất `pom.xml` dependency ban đầu (Spring Web, JPA, Validation, Security, Thymeleaf, PostgreSQL driver, Lombok).
- [ ] Đã tạo file `.env.example` (không chứa secret thật).

## 30. Checklist trước khi merge vào develop

- [ ] Build thành công (`mvn clean install`).
- [ ] Không có business logic lọt vào Controller.
- [ ] Không truy cập Repository từ Controller.
- [ ] Đã validate input DTO.
- [ ] Đã xử lý exception (không throw raw exception ra ngoài).
- [ ] Đã test thủ công chức năng liên quan.
- [ ] Không có secret/`.env` trong commit.
- [ ] Đã được người còn lại review.

---

Bạn có muốn tôi tiếp tục tạo skeleton code cho project theo kiến trúc này không?
