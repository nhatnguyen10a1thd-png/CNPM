# 05 — SỔ TAY KIẾN THỨC JAVA 21 & SPRING BOOT GẮN VỚI CODE THỰC TẾ

> **Mục tiêu:** Giải thích cặn kẽ các từ khóa, annotation và cơ chế framework xuất hiện trong source code LUNEA để tự tin trả lời vấn đáp.  

---

## 1. CÁC TÍNH NĂNG JAVA 21 ÁP DỤNG TRONG DỰ ÁN

1. **Java Record:**
   - Dùng cho các DTO ngắn gọn, bất biến (Immutable), ví dụ: `record ApiResponse<T>(int code, String message, T data)`.
   - Trình biên dịch tự động sinh: Constructor, `getter()` (không có chữ get, ví dụ `code()`), `equals()`, `hashCode()`, `toString()`.
2. **Pattern Matching for switch / instanceof:**
   - Dùng trong kiểm tra trạng thái đơn hàng và phân loại ngoại lệ trong `GlobalExceptionHandler`.
3. **Sequenced Collections & Stream API:**
   - Sắp xếp giỏ hàng, lọc sản phẩm theo mức giá (`PriceRange`), lọc danh sách SKU theo thứ tự ID để chống deadlock.

---

## 2. TOÀN TẬP ANNOTATION SPRING BOOT XUẤT HIỆN TRONG CODE

### 2.1. Nhóm Khởi tạo & Cấu hình
- `@SpringBootApplication`: Tổ hợp `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- `@EnableAsync`: Bật cơ chế đa luồng bất đồng bộ (`TaskExecutor`), áp dụng cho gửi email Brevo.
- `@Configuration`: Đánh dấu class định nghĩa các `@Bean`.
- `@Bean`: Phương thức trả về một đối tượng do Spring IoC Container quản lý vòng đời (Lifecycle).

### 2.2. Nhóm Tầng Web / Controller
- `@RestController`: Tổ hợp `@Controller` + `@ResponseBody`. Mọi phương thức mặc định chuyển đổi đối tượng trả về thành JSON qua Jackson `MappingJackson2HttpMessageConverter`.
- `@RequestMapping("/api/v1/orders")`: Định nghĩa tiền tố đường dẫn URL ở mức class.
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`: Ánh xạ các HTTP Methods tương ứng chuẩn RESTful.
- `@RequestBody`: Bảo Spring đọc chuỗi JSON từ HTTP Request Body và deserialize thành đối tượng DTO.
- `@PathVariable("id")`: Trích xuất biến động trên đường dẫn URL (ví dụ `/orders/{id}`).
- `@RequestParam`: Lấy query parameters từ URL (ví dụ `/products?keyword=kem&page=0`).
- `@Valid`: Kích hoạt cơ chế kiểm tra tính hợp lệ của dữ liệu (Bean Validation) trước khi code trong method chạy.

### 2.3. Nhóm Tầng Nghiệp vụ & Dữ liệu
- `@Service`: Đánh dấu class chứa Business Logic.
- `@Repository`: Đánh dấu Data Access Object, tự động dịch các exception SQL thành Spring `DataAccessException`.
- `@Transactional`: Quản lý giao dịch database.
  - `@Transactional(readOnly = true)`: Tối ưu hiệu năng khi chỉ đọc dữ liệu, Hibernate không cần bật Dirty Checking.
  - `@Transactional(rollbackFor = Exception.class)`: Tự động rollback dữ liệu nếu có bất kỳ ngoại lệ nào phát sinh.

### 2.4. Nhóm Tầng Thực thể JPA / Hibernate
- `@Entity`: Đánh dấu class ánh xạ với bảng CSDL.
- `@Table(name = "TonKho")`: Chỉ định tên bảng vật lý.
- `@Id` & `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Định nghĩa khóa chính tự tăng.
- `@Enumerated(EnumType.STRING)`: Lưu giá trị Enum dưới dạng chuỗi chữ trong CSDL (tránh lỗi khi thay đổi thứ tự enum ordinal).
- `@ManyToOne(fetch = FetchType.LAZY)`: Quan hệ nhiều-một, chỉ load dữ liệu bảng cha khi được gọi tới (tránh N+1 query).
- `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)`: Quan hệ một-nhiều, tự động đồng bộ thêm/xóa/sửa các bản ghi con.

---

## 3. CÁC MẪU THIẾT KẾ (DESIGN PATTERNS) TRONG SOURCE CODE

1. **Dependency Injection & Inversion of Control (IoC):**
   - Không dùng `new OrderServiceImpl()`. Thay vào đó, Spring tự động tiêm các Repository vào Service qua Constructor Injection (`@RequiredArgsConstructor` của Lombok).
2. **DTO Pattern (Data Transfer Object):**
   - Tách rời hoàn toàn Entity CSDL khỏi JSON trả về cho Client.
   - Ngăn chặn lộ thông tin nhạy cảm (như `password_hash`), tránh lỗi tuần hoàn vô tận (Infinite Recursion) của Jackson khi serialize quan hệ hai chiều.
3. **Repository Pattern:**
   - Ẩn giấu chi tiết câu lệnh SQL, cung cấp giao diện lập trình hướng đối tượng.
4. **State Machine Pattern (Máy trạng thái):**
   - Cài đặt trong `OrderStatus.java` với phương thức `canTransitionTo()` để đảm bảo đơn hàng không thể nhảy cóc trạng thái bất hợp lệ (ví dụ: Không thể chuyển từ `PENDING` sang thẳng `COMPLETED` mà chưa qua `SHIPPING`).
5. **ThreadLocal Scope Pattern:**
   - Cài đặt trong `StoreScope.java` để truyền tải ngữ cảnh chi nhánh xuyên suốt các tầng xử lý của một HTTP request mà không cần truyền tham số `storeId` qua từng method.
