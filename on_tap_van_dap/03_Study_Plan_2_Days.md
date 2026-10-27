# LỘ TRÌNH ÔN VẤN ĐÁP CẤP TỐC TRONG 2 NGÀY (03_Study_Plan_2_Days.md)
## Kế Hoạch 16 Giờ Thực Chiến: Từ Hiểu Code Đến Làm Chủ Buổi Bảo Vệ Đồ Án LUNEA

> **Chiến lược:** Không học lý thuyết suông! Mọi giờ học đều mở mã nguồn thực tế, thực hành truy vết và tự nói to thành lời để não bộ hình thành phản xạ kỹ thuật.  
> **Thời gian:** 8 giờ học tập trung mỗi ngày (chia thành các phiên 90–120 phút).

---

## TỔNG QUAN TIẾN ĐỘ 2 NGÀY

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ NGÀY 1: HIỂU TOÀN DIỆN DỰ ÁN & LÀM CHỦ CÁC LUỒNG CODE THỰC TẾ               │
│ - Buổi Sáng (08:00 - 12:00): Tổng quan, Kiến trúc, Chạy Test & CSDL 40 bảng  │
│ - Buổi Chiều (13:30 - 17:30): Lần vết Luồng Đặt hàng, Tồn kho & Bảo mật JWT   │
│ - Buổi Tối (19:30 - 21:30): Tự kiểm tra & Vấn đáp chẩn đoán Ngày 1           │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ NGÀY 2: CỦNG CỐ JAVA/SPRING, PHẢN BIỆN TÌNH HUỐNG & DIỄN TẬP BẢO VỆ MÔ PHỎNG │
│ - Buổi Sáng (08:00 - 12:00): Nắm chắc Java Core, Spring Framework & CSDL     │
│ - Buổi Chiều (13:30 - 17:30): Luyện câu hỏi "Tại sao?", Thay đổi đề & Bug fix │
│ - Buổi Tối (19:30 - 21:30): Buổi Vấn Đáp Mô Phỏng Toàn Diện (Full Mock Viva)  │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## CHI TIẾT NGÀY 1 — HIỂU DỰ ÁN & NẮM ĐƯỢC LUỒNG CODE

### 🌅 PHIÊN 1 (08:00 – 10:00) | Tổng Quan Dự Án, Công Nghệ & Khởi Động Kiểm Thử
* **Mục tiêu phiên:** Giới thiệu trôi chảy dự án trong 3 phút, chứng minh dự án chạy được 100%.
* **Tài liệu & File cần mở:**
  1. File `01_Project_Map.md` (Mục 1, 2 và 3).
  2. File [`pom.xml`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/pom.xml) (Kiểm tra version Java 21, Spring Boot, dependencies).
  3. File [`src/main/resources/application.properties`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/resources/application.properties).
* **Kiến thức cần nắm vững:**
  - LUNEA giải quyết bài toán gì? (Mô hình chuỗi showroom, quản lý danh mục tập trung, theo dõi tồn kho và phân quyền theo từng chi nhánh).
  - Vì sao chọn kiến trúc phân tầng (Layered Architecture) kết hợp Dual Presentation (Thymeleaf SSR + REST JSON)?
  - Cấu hình đa database: Local dùng SQL Server, Cloud dùng PostgreSQL, Test dùng H2.
* **Thao tác thực hành:**
  Mở terminal và gõ lệnh chạy toàn bộ kiểm thử tự động:
  ```powershell
  .\mvnw.cmd test
  ```
  Quan sát và xác nhận dòng chữ: `Tests run: 163, Failures: 0, Errors: 0, Skipped: 0` ➔ `BUILD SUCCESS`.
* **Tiêu chí nghiệm thu:** Tự bấm đồng hồ nói to bản giới thiệu dự án trong 2 phút 30 giây mà không bị vấp.

---

### ☕ NGHỈ GIẢI LAO (10:00 – 10:15)

---

### 🏢 PHIÊN 2 (10:15 – 12:00) | Làm Chủ Cơ Sở Dữ Liệu & Khái Niệm Tồn Kho Đa Chi Nhánh
* **Mục tiêu phiên:** Giải thích được cấu trúc bảng, các quan hệ cốt lõi và bài toán tồn kho chuỗi.
* **File cần mở:**
  1. [`database/04_schema_postgresql.sql`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/database/04_schema_postgresql.sql) hoặc `01_schema_mssql.sql`.
  2. [`domain/entity/inventory/InventoryEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/inventory/InventoryEntity.java).
  3. [`domain/entity/sales/OrderItemEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/sales/OrderItemEntity.java).
  4. [`domain/entity/sales/OrderReservationEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/sales/OrderReservationEntity.java).
* **Kiến thức cốt lõi phải hiểu:**
  - Vì sao tách `SanPham` (Product) và `SKU` (ProductSku)? (Một sản phẩm có nhiều dung tích 30ml, 50ml, 100ml với giá và mã vạch khác nhau).
  - Bảng `TonKho`: Khóa duy nhất là cặp `(cua_hang_id, sku_id)`.
  - Công thức tính tồn khả dụng: $\text{Available} = \text{actual\_stock} - \text{held\_quantity}$.
  - Tại sao `ChiTietDonHang` phải lưu **Snapshot giá và tên**? (Để bảo vệ tính toàn vẹn lịch sử doanh thu khi sản phẩm đổi giá).
* **Câu hỏi tự kiểm tra:** *"Nếu một khách hàng thêm hàng vào giỏ nhưng không bấm thanh toán thì tồn kho có bị giảm không?"* (Đáp án: KHÔNG, chỉ khi bấm Đặt hàng thành công mới tăng `held_quantity`).
* **Tiêu chí nghiệm thu:** Vẽ lại được trên giấy nháp mối quan hệ giữa 6 bảng: `CuaHang`, `TonKho`, `SKU`, `DonHang`, `ChiTietDonHang`, `GiuTonDonHang`.

---

### 🍽️ NGHỈ TRƯA & NGHỈ NGƠI (12:00 – 13:30)

---

### 📦 PHIÊN 3 (13:30 – 15:30) | Mổ Xẻ Luồng Nghiệp Vụ Cốt Lõi: Đặt Hàng & Vòng Đời Đơn
* **Mục tiêu phiên:** Đọc và giải thích được từng khối lệnh trong file quan trọng nhất dự án: `OrderServiceImpl.java`.
* **File cần mở:**
  1. [`service/order/impl/OrderServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java).
  2. [`domain/enums/OrderStatus.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/enums/OrderStatus.java).
* **Các đoạn mã bắt buộc phải giải thích được:**
  - Thuật toán tự động tìm chi nhánh cùng tỉnh/thành phố có đủ hàng (Dòng 208–242).
  - Đoạn mã sắp xếp SKU ID chống Deadlock (Dòng 274–276).
  - Đoạn mã tăng `heldQuantity` và tạo bản ghi `OrderReservationEntity` (Dòng 307–320).
  - Vòng đời trạng thái: `PENDING -> CONFIRMED -> PREPARING -> SHIPPING -> COMPLETED`.
  - Hàm `commitHeldInventory()`: Trừ `actual_stock` khi đơn chuyển sang `SHIPPING` (Dòng 552–568).
  - Logic giải phóng hàng khi hủy đơn (`CANCELLED`): Trừ `heldQuantity` hoàn lại tồn khả dụng.
  - Hàm `toResponseBatch()`: Giải quyết triệt để vấn đề N+1 Query (Dòng 570–602).
* **Tiêu chí nghiệm thu:** Tự dùng tay che phần giải thích, nhìn vào code của `placeOrder()` và `updateStatus()` để đọc vanh vách luồng dữ liệu.

---

### ☕ NGHỈ GIẢI LAO (15:30 – 15:45)

---

### 🔐 PHIÊN 4 (15:45 – 17:30) | Giải Phẫu Tầng Bảo Mật: JWT Cookie & Phân Quyền StoreScope
* **Mục tiêu phiên:** Giải thích được cơ chế bảo mật của website và cách cô lập dữ liệu theo từng showroom.
* **File cần mở:**
  1. [`config/SecurityConfig.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/config/SecurityConfig.java).
  2. [`security/CookieBearerTokenResolver.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/CookieBearerTokenResolver.java).
  3. [`security/StoreScope.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/StoreScope.java).
  4. [`security/AuthCookieFactory.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/AuthCookieFactory.java).
* **Kiến thức trọng tâm:**
  - Tại sao dùng **Stateless JWT** thay vì Session HTTP truyền thống? (Không ngốn RAM server khi nhiều user, dễ scale container Docker trên Render).
  - Tại sao lưu JWT vào Cookie `HttpOnly` thay vì `localStorage`? (Chống mã độc JavaScript đánh cắp token qua lỗ hổng XSS).
  - Cách `CookieBearerTokenResolver` ưu tiên Header `Authorization` và fallback sang Cookie `LUNEA_AT`.
  - Cách `StoreScope` thẩm định: Nếu có quyền `ALL_STORES`/`ROLE_ADMIN` thì trả về `null` (xem hết), nếu là nhân viên thường thì truy vấn bảng `NhanVienCuaHang` để chỉ lấy danh sách ID showroom được phân công.
* **Tiêu chí nghiệm thu:** Trả lời xuất sắc câu hỏi: *"Nếu nhân viên Hà Nội dùng Postman gửi request xem đơn hàng của showroom Hồ Chí Minh thì code ở đâu sẽ phát hiện và chặn lại?"* (Chỉ ra `StoreScope.canAccessStore()` trong `OrderServiceImpl.java`).

---

### 🌙 PHIÊN 5 (19:30 – 21:30) | Tự Vấn Đáp & Đánh Giá Chẩn Đoán Cuối Ngày 1
* **Mục tiêu phiên:** Phát hiện ngay các lỗ hổng kiến thức còn mơ hồ để lên danh sách khắc phục vào sáng Ngày 2.
* **Nội dung thực hiện:** Tự trả lời 8 câu hỏi chẩn đoán Ngày 1 (không nhìn đáp án):
  1. *Trình bày kiến trúc phân tầng của dự án từ giao diện đến database?*
  2. *Tồn kho thực tế (actual_stock), Hàng đang giữ (held_quantity) và Tồn khả dụng khác nhau như thế nào?*
  3. *Hàng hóa được trừ vào tồn kho thực tế ở bước nào trong vòng đời đơn hàng?*
  4. *Khách hàng hủy đơn thì dữ liệu trong kho thay đổi ra sao?*
  5. *Cơ chế StoreScope hoạt động như thế nào trong dự án?*
  6. *Tại sao lại dùng Cookie HttpOnly để lưu chuỗi JWT?*
  7. *Dự án giải quyết vấn đề N+1 Query bằng cách nào?*
  8. *Làm thế nào để hệ thống ngăn chặn Deadlock khi nhiều người cùng đặt hàng?*
* **Đánh giá:** Ghi chú lại câu hỏi nào trả lời còn lúng túng hoặc chưa nhớ rõ tên file để đưa vào mục ưu tiên số 1 của Ngày 2.

---

## CHI TIẾT NGÀY 2 — CỦNG CỐ KIẾN THỨC & LUYỆN VẤN ĐÁP

### 🌅 PHIÊN 6 (08:00 – 10:00) | Ôn Tập Java Core & Spring Framework Gắn Liền Với Mã Nguồn
* **Mục tiêu phiên:** Nắm vững lý thuyết kỹ thuật nhưng luôn dẫn chứng được trực tiếp bằng code của dự án.
* **Nội dung & Dẫn chứng trong code:**
  - **OOP (Kế thừa, Đa hình, Đóng gói, Trừu tượng):**
    * *Đa hình/Trừu tượng:* Interface `OrderService` và class triển khai `OrderServiceImpl`.
    * *Đóng gói:* Các field trong Entity đặt `private`, truy cập qua getter/setter hoặc Lombok `@Getter/@Setter/@Builder`.
  - **Java Collection & Stream API:**
    * Sử dụng `List`, `Map`, `Set` để nhóm dữ liệu trong `OrderServiceImpl.toResponseBatch()` (`Collectors.groupingBy`).
    * Sử dụng `.filter()`, `.map()`, `.sorted()` trong tìm kiếm sản phẩm `ProductServiceImpl.searchAndFilter()`.
  - **Xử lý ngoại lệ (Exception Handling):**
    * Mở [`exception/BusinessException.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/exception/BusinessException.java) và [`exception/GlobalExceptionHandler.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java).
    * Bắt các lỗi `@Valid` (`MethodArgumentNotValidException`) và ném về mã JSON chuẩn RFC-7807.
  - **Spring Dependency Injection (DI) & Inversion of Control (IoC):**
    * Dùng `@RequiredArgsConstructor` của Lombok để tự động Inject qua Constructor (Constructor Injection), không dùng `@Autowired` trên field vì khó viết Unit Test.
  - **Ranh giới Transaction (`@Transactional`):**
    * Khi đặt hàng, nếu lưu `OrderEntity` thành công nhưng bước lưu `InventoryEntity` bị lỗi, `@Transactional` sẽ rollback toàn bộ, không để lại dữ liệu rác mồ côi.
* **Tiêu chí nghiệm thu:** Khi giảng viên hỏi về `@Transactional`, mở ngay `OrderServiceImpl` chỉ vào dòng `@Transactional` ở đầu class và giải thích ranh giới rollback.

---

### ☕ NGHỈ GIẢI LAO (10:00 – 10:15)

---

### 🔍 PHIÊN 7 (10:15 – 12:00) | Khảo Sát Các Module Phụ Trợ: Voucher, OTP & Tìm Kiếm Tiếng Việt
* **Mục tiêu phiên:** Tự tin trả lời các câu hỏi kỹ thuật về các tính năng thông minh của dự án.
* **File cần mở:**
  1. [`service/order/impl/VoucherServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java).
  2. [`service/catalog/impl/ProductServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java).
  3. [`service/account/impl/OtpServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/account/impl/OtpServiceImpl.java).
* **Điểm nhấn ăn điểm:**
  - **Voucher Engine:** Giải thích cách kiểm tra: Hạn mức số lượng dùng (`usedCount < usageLimit`), đơn hàng tối thiểu (`minOrderAmount`), trần giảm giá tối đa (`maxDiscountAmount`).
  - **Tìm kiếm tiếng Việt không dấu:** Giải thích hàm `stripAccents()` sử dụng `Normalizer.Form.NFD` và Regex `\\p{M}` thay vì phải cài Elasticsearch phức tạp.
  - **Bảo mật OTP:** Nêu rõ 4 lớp bảo vệ: (1) `SecureRandom` 6 số; (2) Cooldown 30s chống spam; (3) Khóa sau 5 lần nhập sai chống brute-force; (4) Hết hạn sau 5 phút.
* **Tiêu chí nghiệm thu:** Giải thích trôi chảy thuật toán tách dấu tiếng Việt trong 1 phút.

---

### 🍽️ NGHỈ TRƯA & NGHỈ NGƠI (12:00 – 13:30)

---

### 🛡️ PHIÊN 8 (13:30 – 15:30) | Đối Phó Câu Hỏi Phản Biện & Xử Lý Điểm Chưa Hoàn Thiện
* **Mục tiêu phiên:** Chuẩn bị sẵn phương án trả lời các câu hỏi hóc búa, không bị lúng túng khi bị giảng viên "bắt bài".
* **Các tình huống phản biện kinh điển:**
  - **Tình huống 1: Bán vượt tồn kho (Overselling) khi có hàng nghìn người cùng mua 1 món hàng cuối cùng?**
    * *Thừa nhận thực tế:* Hiện tại dự án đang dựa trên `@Transactional` và kiểm tra lại tồn kho ở bước tạo phiếu giữ.
    * *Giải pháp nâng cấp:* Trình bày với giảng viên: Có thể áp dụng **Pessimistic Locking** (`SELECT ... FOR UPDATE` qua `@Lock(LockModeType.PESSIMISTIC_WRITE)`) trên bảng `TonKho` hoặc sử dụng **Redis Distributed Lock (Redisson)**.
  - **Tình huống 2: Nếu website bị sập mạng Internet giữa chừng khi thanh toán?**
    * Dữ liệu trong database an toàn vì Spring Transaction sẽ tự động Rollback toàn bộ các bước dở dang.
  - **Tình huống 3: Tại sao không dùng Microservices mà dùng Monolith?**
    * *Lập luận bảo vệ:* Với quy mô chuỗi showroom vừa và nhỏ, kiến trúc Monolith phân tầng giúp tối ưu chi phí hạ tầng (chạy vừa vặn trong gói Free 512MB RAM của Render), giảm độ trễ mạng và đảm bảo tính nhất quán dữ liệu ACID tuyệt đối mà không cần Distributed Transaction (Saga pattern) phức tạp.
* **Tiêu chí nghiệm thu:** Biết cách thừa nhận hạn chế một cách chuyên nghiệp và đưa ra phương án khắc phục nâng cao.

---

### ☕ NGHỈ GIẢI LAO (15:30 – 15:45)

---

### 🎬 PHIÊN 9 (15:45 – 17:30) | Diễn Tập Kịch Bản Demo Trực Tiếp Trên Web
* **Mục tiêu phiên:** Thành thạo kịch bản demo 16 bước từ Web khách hàng sang Web quản trị (theo Mục 6 trong `HUONG_DAN_CAI_DAT_VA_SU_DUNG.md`).
* **Các bước demo then chốt cần làm thuần thục:**
  1. Mở trang chủ, tìm kiếm từ khóa `Cocoon`.
  2. Mở chi tiết *Serum Bí Đao Cocoon*, đổi dung tích từ `70ml` sang `140ml` để thấy giá cập nhật tức thì. Bấm kiểm tra tồn kho tại 3 Showroom (HCM, HN, Đà Nẵng).
  3. Bấm đăng nhập nhanh tài khoản khách `khach01`, mở giỏ hàng, nhập voucher `LUNEA10`, bấm đặt hàng COD.
  4. Mở tab ẩn danh hoặc đăng xuất, đăng nhập tài khoản nhân viên `order_hcm`.
  5. Mở `/admin/orders`, thấy đơn hàng vừa đặt. Duyệt trạng thái từ `Chờ xác nhận` sang `Đang chuẩn bị` rồi sang `Đang giao`.
  6. Mở `/admin/inventory` để chứng minh tồn kho thực tế bị trừ.
  7. Đăng nhập nhân viên `order_hn` để chứng minh không thể nhìn thấy đơn hàng của HCM (Chứng minh `StoreScope`).
* **Tiêu chí nghiệm thu:** Hoàn thành toàn bộ kịch bản demo trong 7–10 phút mà không gặp lỗi phát sinh.

---

### 🌙 PHIÊN 10 (19:30 – 21:30) | Buổi Vấn Đáp Mô Phỏng Toàn Diện (Full Dress Viva)
* **Mục tiêu phiên:** Đóng vai bảo vệ thật với trọn vẹn bộ câu hỏi trong tài liệu `04_Viva_QA.md`.
* **Quy trình:**
  - Nhờ bạn học hoặc tự bật ghi âm điện thoại.
  - Bốc ngẫu nhiên 10 câu hỏi thuộc 10 chủ đề trong file `04_Viva_QA.md`.
  - Tự trả lời to, rõ ràng, căn thời gian 1–2 phút cho mỗi câu.
  - Nghe lại đoạn ghi âm, tự chấm điểm theo thang điểm 10 và xem lại các ý trả lời còn thiếu.
* **Tổng kết trước giờ đi ngủ:** Xem lại file `05_Quick_Revision.md` trong 15 phút, ngủ sớm để giữ tinh thần minh mẫn cho ngày bảo vệ.
