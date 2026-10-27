# 📅 KẾ HOẠCH HỌC TẬP & ÔN TẬP VẤN ĐÁP CẤP TỐC 2 NGÀY (MASTER STUDY PLAN)
## DỰ ÁN LUNEA — WEBSITE BÁN MỸ PHẨM VÀ QUẢN LÝ CHUỖI CỬA HÀNG

> **Môn học:** Công nghệ Phần mềm  
> **Repository:** [https://github.com/nhatnguyen10a1thd-png/CNPM](https://github.com/nhatnguyen10a1thd-png/CNPM)  
> **Thời gian:** 2 Ngày (16 giờ học thực chiến — 8 Phiên học)  
> **Mục tiêu điểm số:** 8.5 – 10.0 (Hiểu bản chất toàn bộ 351 file, giải thích từng dòng code lõi, phản xạ trả lời trôi chảy trước hội đồng).  
> **Phương pháp cốt lõi:** Đọc code thực tế ➔ Giải thích cặn kẽ ➔ Minh họa ví dụ ➔ Kiểm tra vấn đáp 1 câu/lượt ➔ Sửa lỗi hiểu sai ➔ Chuyển bài.

---

## 🧭 BẢN ĐỒ TIẾN ĐỘ TỔNG QUAN (TIMELINE 2 NGÀY)

```
====================================================================================================
NGÀY 1: NỀN TẢNG KIẾN TRÚC, BẢO MẬT ĐA CHI NHÁNH, THỰC THỂ CSDL VÀ XỬ LÝ ĐƠN HÀNG LÕI
====================================================================================================
 [08:00 - 10:00] PHIÊN 1: Entry Point & Cấu hình môi trường (Application, pom.xml, app.properties)
 [10:15 - 12:15] PHIÊN 2: Bảo mật Stateless JWT, HttpOnly Cookie & Cô lập Chi nhánh StoreScope
 [13:30 - 15:30] PHIÊN 3: Xử lý ngoại lệ toàn cục & 43 JPA Entities (Bất biến tồn kho actual - held)
 [15:45 - 17:45] PHIÊN 4: Repositories, DTOs & Service Lõi #1: OrderServiceImpl (Đặt hàng, chống Deadlock)
====================================================================================================
NGÀY 2: TOÀN BỘ SERVICES CÒN LẠI, REST/MVC CONTROLLERS, FRONTEND VÀ TỔNG DUYỆT VẤN ĐÁP
====================================================================================================
 [08:00 - 10:00] PHIÊN 5: Service Lõi #2: ProductServiceImpl (Lọc tiếng Việt không dấu), OTP & Kho
 [10:15 - 12:15] PHIÊN 6: 17 REST API Controllers & 8 Web MVC Controllers (Giao tiếp Client/Server)
 [13:30 - 15:30] PHIÊN 7: Frontend Thymeleaf (37 HTML), api.js, Design Tokens, Tests & Docker
 [15:45 - 17:45] PHIÊN 8: TỔNG DUYỆT VẤN ĐÁP THỰC TẾ (MOCK VIVA INTERVIEW - 15 CÂU HỎI BẪY)
====================================================================================================
```

---

## 📋 CHI TIẾT NỘI DUNG TỪNG PHIÊN HỌC (SESSION BY SESSION)

---

### ☀️ NGÀY 1: LÀM CHỦ BẢN CHẤT KIẾN TRÚC, BẢO MẬT & DỮ LIỆU

#### 🔹 PHIÊN 1 (08:00 – 10:00): Entry Point & Cấu hình Nền tảng
- **Mục tiêu:** Nắm vững cơ chế khởi động ứng dụng, quản lý dependency và các thông số kết nối CSDL.
- **Danh sách file học:**
  1. [`CosmeticStoreChainManagementApplication.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/CosmeticStoreChainManagementApplication.java) (Đang học)
  2. [`pom.xml`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/pom.xml)
  3. [`src/main/resources/application.properties`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/resources/application.properties)
  4. [`src/main/java/com/thinh/cosmetic/config/AppProperties.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/config/AppProperties.java)
  5. [`src/main/java/com/thinh/cosmetic/config/CloudinaryConfig.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/config/CloudinaryConfig.java)
- **Kiến thức phải giải thích được:**
  - Tổ hợp 3 annotation trong `@SpringBootApplication` (`@SpringBootConfiguration`, `@EnableAutoConfiguration`, `@ComponentScan`).
  - Tác dụng của `@EnableAsync` trong việc gửi mail OTP bất đồng bộ qua Brevo API.
  - Cấu hình HikariCP Connection Pool: `maximum-pool-size=20`, `minimum-idle=5` để làm gì?
  - Cơ chế tự nhận diện cơ sở dữ liệu: Local dùng SQL Server, Cloud Render tự nhận PostgreSQL.
- **Câu hỏi kiểm tra trọng tâm:**
  - *Tại sao file Application phải đặt ở root package `com.thinh.cosmetic`?*
  - *Nếu cơ sở dữ liệu bị quá tải kết nối thì thông số nào trong `application.properties` cần điều chỉnh?*

---

#### 🔹 PHIÊN 2 (10:15 – 12:15): Bảo mật Stateless JWT & Phân quyền Đa chi nhánh
- **Mục tiêu:** Hiểu sâu kiến trúc bảo mật độc đáo của dự án: Lưu JWT trong HttpOnly Cookie và cơ chế cô lập dữ liệu showroom.
- **Danh sách file học:**
  1. [`src/main/java/com/thinh/cosmetic/config/SecurityConfig.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/config/SecurityConfig.java)
  2. [`src/main/java/com/thinh/cosmetic/security/StoreScope.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/StoreScope.java)
  3. [`src/main/java/com/thinh/cosmetic/security/CookieBearerTokenResolver.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/CookieBearerTokenResolver.java)
  4. [`src/main/java/com/thinh/cosmetic/security/JwtService.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/JwtService.java)
  5. [`src/main/java/com/thinh/cosmetic/security/JwtAuthConverter.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/JwtAuthConverter.java)
  6. [`src/main/java/com/thinh/cosmetic/security/AuthCookieFactory.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/AuthCookieFactory.java)
  7. Các file còn lại trong `com.thinh.cosmetic.security.*` (`AppUserDetails`, `JsonAuthEntryPoint`, `JsonAccessDeniedHandler`...)
- **Kiến thức phải giải thích được:**
  - Vì sao lưu JWT trong HttpOnly Cookie (`LUNEA_AT`) an toàn hơn lưu trong `LocalStorage` của trình duyệt? (Chống XSS).
  - Vai trò của `CookieBearerTokenResolver`: Đọc token thông minh từ Cookie khi dùng trình duyệt, đọc từ Header khi dùng Postman/Mobile.
  - Cơ chế hoạt động của `StoreScope`: Dùng `ThreadLocal<Long>` để lưu giữ `storeId` cho từng request của nhân viên, tại sao bắt buộc phải có lệnh `clear()` trong finally block?
- **Câu hỏi kiểm tra trọng tâm:**
  - *Làm thế nào để hệ thống ngăn chặn nhân viên chi nhánh Hà Nội xem trộm đơn hàng của chi nhánh TP.HCM?*
  - *Phân biệt sự khác nhau giữa Authentication (Xác thực) và Authorization (Phân quyền) trong `SecurityConfig.java`?*

---

#### 🔹 PHIÊN 3 (13:30 – 15:30): Xử lý Ngoại lệ, Enums & 43 Thực thể CSDL JPA
- **Mục tiêu:** Nắm vững cấu trúc 37 bảng CSDL, quan hệ giữa các thực thể và các bất biến dữ liệu kinh điển.
- **Danh sách file học:**
  1. [`src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java) & `ErrorCode.java`
  2. 16 Enums trong `com.thinh.cosmetic.domain.enums.*` (Trọng tâm: `OrderStatus`, `ReservationStatus`, `SkinType`, `SkincareNeed`)
  3. Cụm Entity Bán hàng: [`OrderEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/sales/OrderEntity.java), [`OrderItemEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/sales/OrderItemEntity.java), [`OrderReservationEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/sales/OrderReservationEntity.java)
  4. Cụm Entity Tồn kho: [`InventoryEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/inventory/InventoryEntity.java), [`StoreEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/inventory/StoreEntity.java)
  5. Cụm Entity Sản phẩm: [`ProductEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductEntity.java), [`ProductSkuEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java)
  6. Các Entity Tài khoản & Nhập hàng còn lại.
- **Kiến thức phải giải thích được:**
  - Bất biến tồn kho: $\text{Available Stock} = \text{actual\_stock} - \text{held\_quantity}$. Ràng buộc `UNIQUE(cua_hang_id, sku_id)`.
  - Tại sao `OrderItemEntity` phải lưu snapshot (`snapshot_product_name`, `snapshot_price`)?
  - Máy trạng thái `OrderStatus.canTransitionTo()` ngăn chặn nhảy cóc trạng thái đơn ra sao?
  - `@RestControllerAdvice` chuẩn hóa mã lỗi trả về theo chuẩn RFC-7807 như thế nào?
- **Câu hỏi kiểm tra trọng tâm:**
  - *Nếu giá sản phẩm bị nhân viên sửa từ 300k lên 500k thì đơn hàng của khách mua hôm qua có bị thay đổi tổng tiền không? Nhờ đâu?*
  - *Tại sao quan hệ giữa Product và ProductSku lại là `@OneToMany(fetch = FetchType.LAZY)`?*

---

#### 🔹 PHIÊN 4 (15:45 – 17:45): Repositories, DTOs & Service Lõi #1: Xử lý Đơn hàng
- **Mục tiêu:** Mổ xẻ từng dòng code của phân hệ quan trọng nhất hệ thống — Luồng đặt hàng và giữ tồn kho.
- **Danh sách file học:**
  1. [`src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java) (File quan trọng nhất dự án)
  2. [`src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java)
  3. [`src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java)
  4. Các Repository liên quan: `OrderRepository`, `InventoryRepository`, `OrderReservationRepository`, `VoucherRepository`
  5. Các DTOs: `CreateOrderRequest`, `OrderResponse`, `ApplyVoucherRequest`
- **Kiến thức phải giải thích được:**
  - Kỹ thuật **sắp xếp SKU ID theo thứ tự tăng dần** (`Collections.sort`) trước khi kiểm tra tồn kho để triệt tiêu nguy cơ **Deadlock CSDL** khi nhiều khách cùng mua một lúc.
  - Thuật toán tự động tìm chi nhánh gần nhất có đủ toàn bộ hàng cho đơn giao tận nơi.
  - Các bước trong `@Transactional`: Kiểm tra tồn ➔ Giữ tồn kho (`held_quantity++`) ➔ Lưu đơn ➔ Lưu snapshot món ➔ Xóa giỏ hàng ➔ Gửi mail.
  - Quy tắc hoàn trả tồn kho khi khách bấm Hủy đơn (`OrderStatus.CANCELLED`).
- **Câu hỏi kiểm tra trọng tâm:**
  - *Deadlock CSDL là gì? Tại sao sắp xếp SKU ID lại giải quyết được Deadlock trong quá trình checkout?*
  - *Nếu trong giỏ hàng có 1 sản phẩm bị hết hàng lúc bấm Đặt hàng thì hệ thống xử lý ra sao?*

---

### 🌙 NGÀY 2: HOÀN THIỆN TOÀN BỘ CODE, FRONTEND VÀ LUYỆN VẤN ĐÁP

#### 🔹 PHIÊN 5 (08:00 – 10:00): Service Lõi #2: Tìm kiếm Mỹ phẩm, OTP & Điều chuyển kho
- **Mục tiêu:** Làm chủ các thuật toán thông minh: Tìm kiếm tiếng Việt không dấu, bảo mật OTP và điều chuyển hàng giữa các showroom.
- **Danh sách file học:**
  1. [`src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java)
  2. [`src/main/java/com/thinh/cosmetic/service/account/impl/OtpServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/account/impl/OtpServiceImpl.java)
  3. [`src/main/java/com/thinh/cosmetic/service/store/impl/StockTransferServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/store/impl/StockTransferServiceImpl.java)
  4. [`src/main/java/com/thinh/cosmetic/service/notification/impl/EmailServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/notification/impl/EmailServiceImpl.java)
  5. [`src/main/java/com/thinh/cosmetic/service/storage/CloudinaryService.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/storage/CloudinaryService.java)
- **Kiến thức phải giải thích được:**
  - Thuật toán chuẩn hóa tiếng Việt loại bỏ dấu (`Normalizer.normalize(NFD)` và Regex) trong `ProductServiceImpl` giúp khách gõ "son duong" vẫn tìm thấy "Son dưỡng môi".
  - Bộ lọc mỹ phẩm thông minh theo loại da (`SkinType`) và nhu cầu chăm sóc (`SkincareNeed`).
  - Cơ chế OTP an toàn: Dùng `SecureRandom`, thời hạn sống 5 phút, cooldown chống spam 30 giây, khóa sau 5 lần nhập sai.
  - Quy trình điều chuyển hàng giữa 2 showroom: Kho xuất trừ hàng, Kho nhập cộng hàng khi xác nhận.
- **Câu hỏi kiểm tra trọng tâm:**
  - *Tại sao tạo OTP lại dùng `SecureRandom` mà không dùng `Math.random()` hay `java.util.Random`?*
  - *Nếu chi nhánh A điều chuyển 10 thỏi son sang chi nhánh B thì số lượng tồn thực tế và tồn khả dụng tại 2 kho thay đổi thế nào qua từng trạng thái?*

---

#### 🔹 PHIÊN 6 (10:15 – 12:15): 17 REST API Controllers & 8 Web MVC Controllers
- **Mục tiêu:** Hiểu rõ cách backend nhận request từ người dùng, kiểm tra tính hợp lệ và trả dữ liệu về giao diện.
- **Danh sách file học:**
  1. 17 REST Controllers trong `com.thinh.cosmetic.rest.*`: `AuthRestController`, `ProductRestController`, `OrderRestController`, `CartRestController`, `InventoryRestController`...
  2. 8 Web MVC Controllers trong `com.thinh.cosmetic.controller.*`: `HomeController`, `ProductPageController`, `CheckoutPageController`, `AdminPageController`, `CustomErrorController`...
- **Kiến thức phải giải thích được:**
  - Sự khác nhau bản chất giữa `@Controller` (trả về tên file HTML để Thymeleaf render) và `@RestController` (trả về JSON qua `@ResponseBody`).
  - Annotation `@Valid` kết hợp Jakarta Validation (`@NotBlank`, `@Min`, `@Email`) trong Request DTO.
  - Cách truyền dữ liệu từ Backend sang giao diện qua đối tượng `org.springframework.ui.Model` (`model.addAttribute`).
  - Phân quyền endpoint bằng `@PreAuthorize("hasRole('ADMIN')")` hoặc cấu hình tập trung trong `SecurityConfig`.
- **Câu hỏi kiểm tra trọng tâm:**
  - *Nếu người dùng gửi dữ liệu sai định dạng (ví dụ để trống số điện thoại) thì tầng Controller bắt lỗi ở đâu và trả về mã HTTP bao nhiêu?*
  - *Tại sao Controller không trực tiếp gọi Repository mà bắt buộc phải thông qua Service?*

---

#### 🔹 PHIÊN 7 (13:30 – 15:30): Giao diện Thymeleaf, JavaScript, Testing & Triển khai Docker
- **Mục tiêu:** Nắm cách giao diện hiển thị dữ liệu từ server, các hàm gọi API bằng JavaScript và quy trình đóng gói container.
- **Danh sách file học:**
  1. 37 file HTML trong `src/main/resources/templates/` (Trọng tâm: `home.html`, `product-detail.html`, `checkout.html`, `admin/orders.html`, fragments `header.html`, `sidebar.html`)
  2. [`src/main/resources/static/js/api.js`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/resources/static/js/api.js) & CSS Design Tokens (`tokens.css`, `site.css`)
  3. [`Dockerfile`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/Dockerfile) & [`docker-compose.yml`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/docker-compose.yml)
  4. 19 file kiểm thử tự động trong `src/test/java/` (Trọng tâm: `OrderServiceTest`, `OrderEndpointsTest`)
- **Kiến thức phải giải thích được:**
  - Cú pháp Thymeleaf cốt lõi: `th:text`, `th:each` (vòng lặp sản phẩm), `th:if` (ẩn hiện nút bấm theo role), `th:object` & `th:field` (binding form).
  - Thư viện `api.js`: Hàm bọc `fetch()` xử lý tự động đính kèm Cookie, parse JSON và hiển thị Toast thông báo.
  - Cấu hình Multi-stage build trong `Dockerfile` để tối ưu kích thước image và cấu hình JVM flags (`-Xmx350m`, `-XX:+UseSerialGC`) chạy mượt trên Cloud Render gói miễn phí.
- **Câu hỏi kiểm tra trọng tâm:**
  - *Làm thế nào để thanh menu hiển thị nút "Quản trị" cho Admin nhưng ẩn đi đối với Khách hàng trên cùng một file HTML?*
  - *Multi-stage build trong Dockerfile hoạt động ra sao và mang lại lợi ích gì?*

---

#### 🔹 PHIÊN 8 (15:45 – 17:45): TỔNG DUYỆT VẤN ĐÁP THỰC TẾ (MOCK VIVA INTERVIEW)
- **Mục tiêu:** Tập dượt 100% như thi thật, loại bỏ tâm lý sợ hãi, sẵn sàng đón nhận mọi câu hỏi xoáy từ giám khảo.
- **Hình thức thực hiện:**
  - Tôi đóng vai **Giảng viên chấm thi khó tính**, đặt liên tục **15 câu hỏi bẫy** trải dài từ mức 5.0 đến 10.0 (dựa trên ngân hàng câu hỏi tại [`06_VIVA_QUESTIONS.md`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/on_tap_van_dap/hoc_full_code_2_ngay/06_VIVA_QUESTIONS.md)).
  - Bạn trả lời trực tiếp từng câu.
  - Sau mỗi câu, tôi nhận xét ngay: Chỗ nào trả lời hay, chỗ nào nói hớ dễ bị trừ điểm, và cung cấp câu trả lời chuẩn chỉ nhất.

---

## 🎯 MA TRẬN PHÂN LOẠI MỨC ĐỘ ƯU TIÊN ÔN TẬP

```
+-----------------------------------------------------------------------------------+
| 🔴 TIER 1: BẮT BUỘC PHẢI THUỘC LÒNG (90% KHẢ NĂNG BỊ HỎI - ĐỂ ĐẠT 7.0 - 8.0)      |
+-----------------------------------------------------------------------------------+
| 1. Luồng Đặt hàng: Sắp xếp SKU chống Deadlock ➔ Giữ Tồn ➔ Snapshot giá.           |
| 2. Công thức Tồn khả dụng: available_stock = actual_stock - held_quantity.        |
| 3. Bảo mật: Tại sao lưu JWT trong HttpOnly Cookie? (Chống XSS).                  |
| 4. Phân quyền chi nhánh: Cơ chế ThreadLocal của StoreScope.                       |
| 5. Ba annotation trong @SpringBootApplication và vai trò của @EnableAsync.        |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
| 🟡 TIER 2: NẮM VỮNG ĐỂ LẤY ĐIỂM GIỎI VÀ XUẤT SẮC (8.5 - 10.0)                     |
+-----------------------------------------------------------------------------------+
| 1. Cách giải quyết triệt để N+1 Query trong JPA bằng JOIN FETCH & Batch Fetching. |
| 2. Thuật toán tìm kiếm mỹ phẩm chuẩn hóa tiếng Việt không dấu (Normalizer NFD).    |
| 3. Cơ chế bảo mật mã OTP: SecureRandom, Cooldown 30s, giới hạn 5 lần nhập sai.    |
| 4. Tối ưu bộ nhớ JVM trên Render Cloud: -Xmx350m và Serial Garbage Collector.    |
| 5. Máy trạng thái vòng đời đơn hàng: OrderStatus.canTransitionTo().               |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
| 🟢 TIER 3: ĐỌC HIỂU ĐỂ TRẢ LỜI CÁC CÂU HỎI PHỤ (BỔ TRỢ)                           |
+-----------------------------------------------------------------------------------+
| 1. Cấu hình upload ảnh lên Cloudinary CDN.                                        |
| 2. Cơ chế lưu hồ sơ làn da BeautyProfile của khách hàng.                          |
| 3. Kiểm duyệt đánh giá mỹ phẩm ReviewEntity.                                      |
+-----------------------------------------------------------------------------------+
```

---

## 📌 QUY ƯỚC CAM KẾT HỌC TẬP

1. **Học theo từng file/cụm file:** Không chuyển sang file mới khi chưa hiểu file cũ.
2. **Kiểm tra 1 câu/lượt:** Bạn trả lời ngắn gọn, đúng bản chất, không cần dài dòng văn vẻ.
3. **Cập nhật tiến độ sau mỗi câu:** File [`08_LEARNING_PROGRESS.md`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/on_tap_van_dap/hoc_full_code_2_ngay/08_LEARNING_PROGRESS.md) sẽ tự động nhảy số % hoàn thành sau mỗi lần bạn vượt qua câu hỏi kiểm tra!

---

Bây giờ bạn hãy mở file kế hoạch này ra xem lại bất cứ lúc nào tại:  
👉 [**`KE_HOACH_HOC_TAP_CHI_TIET_2_NGAY.md`**](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/on_tap_van_dap/hoc_full_code_2_ngay/KE_HOACH_HOC_TAP_CHI_TIET_2_NGAY.md)
