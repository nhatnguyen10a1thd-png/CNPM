# TÀI LIỆU ÔN TẬP NHANH TRƯỚC GIỜ VẤN ĐÁP (05_Quick_Revision.md)
## Cẩm Nang 3 Giờ Cuối Cùng: Kịch Bản Thuyết Trình, Ma Trận Khái Niệm & Phản Ứng Nhanh

> **Mục tiêu:** Dùng để đọc lướt trước khi vào phòng bảo vệ 1–2 tiếng, giúp định hình lại toàn bộ tư duy, ngôn từ kỹ thuật chuẩn xác và tâm lý tự tin.

---

## 1. BÀI THUYẾT TRÌNH MỞ ĐẦU 2–3 PHÚT (ELEVATOR PITCH SCRIPT)

> 💡 **Hướng dẫn trình bày:** Đứng thẳng, nói với tốc độ vừa phải, tự tin, không nhìn giấy đọc từng chữ mà nói theo các ý gạch đầu dòng sau:

*"Kính thưa Thầy/Cô trong Hội đồng chấm đồ án,  
Em xin phép đại diện nhóm trình bày ngắn gọn về đề tài: **LUNEA – Hệ thống thương mại điện tử kết hợp quản lý chuỗi showroom mỹ phẩm**.

Trong thực tế, các hệ thống bán mỹ phẩm dạng chuỗi gặp phải một thách thức rất lớn: **Làm sao để khách hàng mua sắm online trên một website duy nhất, nhưng hàng hóa lại được xuất từ kho của showroom vật lý gần khách nhất để giao nhanh và không bị bán vượt tồn?**

Hệ thống LUNEA của chúng em giải quyết bài toán này thông qua 3 điểm kiến trúc cốt lõi:
1. **Quản lý danh mục tập trung nhưng phân bổ kho độc lập:** Toàn bộ bảng giá, voucher và thông tin sản phẩm được quản lý chung, nhưng số lượng tồn kho được quản lý theo từng cặp `(Showroom, SKU)`. Hệ thống áp dụng cơ chế **Giữ tồn kho (Stock Reservation)** để đảm bảo hàng chỉ được trừ vào tồn thực tế khi đã bàn giao cho shipper.
2. **Cơ chế phân quyền StoreScope cô lập:** Nhân sự tại chi nhánh Hồ Chí Minh chỉ được thấy và xử lý đơn hàng của Hồ Chí Minh, hoàn toàn bị chặn khi cố can thiệp vào đơn hàng của chi nhánh Hà Nội hay Đà Nẵng.
3. **Nền tảng công nghệ tối ưu:** Backend được xây dựng bằng **Java 21**, **Spring Boot 4.1.1**, kiến trúc bảo mật **Stateless JWT lưu trong HttpOnly Cookie**, hỗ trợ song song **PostgreSQL 16** trên Cloud Render và **SQL Server** khi chạy Local. Toàn bộ **163 bài kiểm thử tự động** của dự án đã vượt qua 100% trên bộ nhớ H2.

Hôm nay, hệ thống đã sẵn sàng cả trên môi trường Cloud thực tế và bản chạy cục bộ để kính mời Thầy/Cô cùng trải nghiệm và đặt câu hỏi phản biện ạ. Em xin chân thành cảm ơn Thầy/Cô!"*

---

## 2. BẢNG MA TRẬN KHÁI NIỆM DỄ BỊ NHẦM LẪN NHẤT KHI VẤN ĐÁP

| Khái niệm | Bản chất trong dự án LUNEA | Xuất hiện ở file nào | Dễ nhầm với | Câu hỏi bẫy của Thầy |
|---|---|---|---|---|
| **Authentication** | Xác thực *Bạn là ai?* (So khớp username, mật khẩu BCrypt, cấp mã JWT). | `AuthRestController`, `AppUserDetailsService` | **Authorization** | *"Hàm này đang làm Authen hay Author?"* |
| **Authorization** | Phân quyền *Bạn được làm gì?* (Kiểm tra Role `ROLE_ADMIN`, permission `ORDER_PROCESS`). | `SecurityConfig`, `StoreScope` | **Authentication** | *"Phân quyền StoreScope nằm ở tầng nào?"* |
| **Entity** | Đối tượng Java ánh xạ 1:1 với bảng cơ sở dữ liệu vật lý qua Hibernate. | `domain/entity/` | **DTO** | *"Tại sao không trả thẳng Entity ra cho View/JSON?"* |
| **DTO** | Đối tượng mang dữ liệu an toàn để giao tiếp với Client, che giấu mật khẩu. | `domain/dto/` | **Entity** | *"MapStruct làm nhiệm vụ gì trong project?"* |
| **Actual Stock** | Tồn kho vật lý thực tế nằm trên kệ showroom. | `InventoryEntity.actualStock` | **Available Stock** | *"Khi khách bấm đặt hàng, Actual Stock có bị giảm ngay không?"* (Đáp án: KHÔNG!) |
| **Held Quantity** | Số lượng hàng đang tạm giữ cho các đơn online chưa giao. | `InventoryEntity.heldQuantity` | **Actual Stock** | *"Hàng đang giữ được giải phóng khi nào?"* (Khi đơn bị HỦY) |
| **Available Stock** | Tồn khả dụng cho phép bán tiếp = `actual_stock - held_quantity`. | Tính toán trong `OrderServiceImpl` | **Actual Stock** | *"Khách trên web nhìn thấy con số tồn kho nào?"* (Tồn khả dụng) |
| **HttpOnly Cookie** | Cookie trình duyệt mà JavaScript không thể đọc được (`document.cookie` bị chặn). | `AuthCookieFactory.java` | **LocalStorage** | *"Lưu token ở Cookie có an toàn hơn LocalStorage không?"* (An toàn hơn nhiều trước mã độc XSS) |
| **N+1 Query** | Lỗi phát sinh 1 câu lệnh cha kéo theo N câu lệnh con bên trong vòng lặp. | Đã khắc phục ở `OrderServiceImpl` | **Query bình thường** | *"Em đã đo lường và triệt tiêu N+1 Query như thế nào?"* |

---

## 3. BẢNG PHẢN XẠ NHANH: "THẦY YÊU CẦU MỞ CODE X THÌ BẤM FILE NÀO?"

Khi ngồi trước Hội đồng, nếu Thầy yêu cầu: *"Em mở code cho Thầy xem chỗ..."*, hãy bình tĩnh dùng phím tắt `Ctrl + P` trong VS Code hoặc `Double Shift` trong IntelliJ để gõ tên file sau:

| Thầy hỏi xem phần nào? | File cần mở ngay lập tức | Dòng code đinh cần chỉ vào |
|---|---|---|
| **Luồng Đặt hàng & Phân bổ chi nhánh** | [`OrderServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java) | Dòng 78 (`placeOrder`), dòng 208 (chọn chi nhánh) |
| **Đoạn mã chống Deadlock CSDL** | [`OrderServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java) | Dòng 274–276 (`sortedCartItems.sort(...)`) |
| **Giải quyết lỗi N+1 Query** | [`OrderServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java) | Dòng 570–602 (`toResponseBatch`) |
| **Chuyển trạng thái & Trừ kho thực tế** | [`OrderServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java) | Dòng 480 (`updateStatus`), dòng 552 (`commitHeldInventory`) |
| **Cô lập dữ liệu nhân viên chi nhánh** | [`StoreScope.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/StoreScope.java) | Dòng 19 (`getAccessibleStoreIds`), dòng 45 (`canAccessStore`) |
| **Cấu hình Spring Security & Stateless JWT** | [`SecurityConfig.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/config/SecurityConfig.java) | Dòng 80–184 (`securityFilterChain`) |
| **Trích xuất JWT từ Cookie HttpOnly** | [`CookieBearerTokenResolver.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/CookieBearerTokenResolver.java) | Dòng 16–38 (`resolve`) |
| **Thuật toán tìm kiếm bỏ dấu tiếng Việt** | [`ProductServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java) | Dòng 226–230 (`stripAccents` dùng `Normalizer`) |
| **Bảo mật OTP 4 lớp & Gửi Email** | [`OtpServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/account/impl/OtpServiceImpl.java) | Dòng 64 (cooldown 30s), dòng 88 (`SecureRandom`) |
| **Tính toán chiết khấu Voucher** | [`VoucherServiceImpl.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java) | Dòng 22 (`validateAndApply`), dòng 65 (`calculateDiscount`) |
| **Bắt lỗi toàn cục & Validate** | [`GlobalExceptionHandler.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java) | Dòng 23 (`handleBusinessException`), dòng 39 (`handleMethodArgumentNotValidException`) |

---

## 4. BATTLECARD: ĐỐI PHÓ CÁC CÂU HỎI BẪY & CÁCH THỪA NHẬN THÔNG MINH

### Tình huống 1: "Tại sao không dùng Microservices mà lại làm Monolith?"
- **Cách trả lời yếu:** *"Dạ do tụi em không đủ thời gian làm Microservices ạ."* (Bị trừ điểm năng lực).
- **Cách trả lời chuẩn kỹ sư:** *"Dạ thưa Thầy, đối với quy mô chuỗi showroom mỹ phẩm vừa và nhỏ (3–10 chi nhánh) trong bài toán này, kiến trúc **Modular Monolith** là sự lựa chọn tối ưu nhất về mặt kỹ thuật và chi phí:
  1. Đảm bảo tính toàn vẹn giao dịch **ACID tuyệt đối** khi giữ tồn kho và trừ tiền, không gặp rủi ro bất đồng bộ như Distributed Transaction trong Microservices.
  2. Ứng dụng chỉ tiêu tốn chưa tới 350MB RAM, chạy vừa vặn trên container Docker gói Free của Render mà không phát sinh chi phí vận hành.
  3. Cấu trúc source code đã được nhóm module hóa rõ ràng (domain, catalog, sales, inventory), nếu sau này hệ thống mở rộng lên hàng triệu đơn/ngày thì việc bóc tách thành các microservice độc lập sẽ rất thuận lợi ạ."*

### Tình huống 2: "Hệ thống của em đã xử lý thanh toán trực tuyến qua VNPay thật chưa?"
- **Cách trả lời chuẩn:** *"Dạ thưa Thầy/Cô, hiện tại hệ thống tập trung hoàn thiện 100% nghiệp vụ thanh toán COD và máy trạng thái vòng đời đơn hàng gắn liền với kho thực tế. Về phần VNPay, cấu trúc database và enum `PaymentMethod.VNPAY` đã được nhóm thiết kế sẵn sàng, tuy nhiên phần Webhook IPN xử lý hoàn tiền tự động khi khách hủy đơn nhóm xin phép được hoàn thiện trong giai đoạn nâng cấp tiếp theo ạ."*

### Tình huống 3: "Nếu database bị mất kết nối khi đang xử lý đơn hàng thì sao?"
- **Cách trả lời chuẩn:** *"Dạ, nhờ cơ chế ranh giới giao dịch `@Transactional` của Spring kết hợp với Connection Pool của HikariCP: nếu database mất kết nối giữa chừng, toàn bộ các bước lưu tạm sẽ bị hủy bỏ (Rollback), hệ thống sẽ trả về lỗi cho người dùng và hoàn toàn không có dữ liệu rác mồ côi bị lưu lại trong CSDL ạ."*

---

## 5. DANH SÁCH TÀI KHOẢN VÀ THÔNG SỐ VÀNG CẦN NHỚ TRONG ĐẦU

- **Mật khẩu dùng chung cho tất cả tài khoản:** `123456`
- **Tài khoản Admin tối cao:** `admin`
- **Nhân viên chi nhánh TP.HCM:** `order_hcm` (Chỉ quản lý đơn showroom Q1 - TP.HCM)
- **Nhân viên chi nhánh Hà Nội:** `order_hn` (Chỉ quản lý đơn showroom Cầu Giấy - Hà Nội)
- **Thủ kho chi nhánh TP.HCM:** `warehouse_hcm` (Quản lý kiểm kê kho TP.HCM)
- **Khách hàng thân thiết:** `khach01` (Khách hàng Lê Hoàng Yến, địa chỉ Q1 TP.HCM)
- **Mã Voucher giảm 10%:** `LUNEA10`
- **Mã Voucher giảm 50.000đ:** `GIAM50K`
- **Mã Miễn phí vận chuyển:** `FREESHIP`
- **Ngưỡng tự động miễn phí vận chuyển:** Đơn hàng từ `500.000 đ` trở lên (Phí ship gốc 30.000 đ).
- **Bộ nhớ Heap JVM cấu hình trên Cloud:** `-Xmx350m -Xms128m -Xss512k -XX:+UseSerialGC`.
- **Tổng số bài test tự động:** `163/163 Tests PASS`.
