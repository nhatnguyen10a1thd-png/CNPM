# 06 — BỘ CÂU HỎI VẤN ĐÁP MÔ PHỎNG THỰC TẾ (VIVA QUESTIONS BANK)

> **Mục tiêu:** Chuẩn bị tâm lý và kiến thức để trả lời trôi chảy trước hội đồng giám khảo môn Công nghệ Phần mềm.  
> **Thang điểm:** Phân cấp rõ ràng từ 5.0 (Đạt) đến 10.0 (Xuất sắc).  

---

## PHẦN 1: MỨC ĐỘ ĐẠT YÊU CẦU (THANG ĐIỂM 5.0 - 6.5)

### Câu 1: Em hãy trình bày kiến trúc tổng thể của dự án LUNEA?
- **Gợi ý trả lời:** Dự án xây dựng theo kiến trúc phân tầng (Layered Architecture) trên nền Java 21 và Spring Boot. Gồm tầng Presentation (Thymeleaf MVC render giao diện web và REST Controllers cung cấp API JSON), tầng Business Service (xử lý nghiệp vụ đặt hàng, tính voucher, điều chuyển kho), tầng Data Access (Spring Data JPA Repositories) và tầng Persistence (Hibernate ánh xạ 37 bảng CSDL SQL Server/PostgreSQL). Bảo mật sử dụng Stateless JWT lưu trong HttpOnly Cookie.

### Câu 2: Dự án của em gồm bao nhiêu bảng dữ liệu, và quản lý tồn kho như thế nào?
- **Gợi ý trả lời:** Hệ thống có 37 bảng dữ liệu logic. Tồn kho được quản lý theo mô hình chuỗi phân tán: bảng `TonKho` có khóa duy nhất là cặp `(cua_hang_id, sku_id)`. Điểm đặc biệt là hệ thống tách biệt giữa `actual_stock` (tồn thực tế trong showroom) và `held_quantity` (số lượng đang tạm giữ cho các đơn chưa giao). Tồn khả dụng bán cho khách luôn bằng `actual_stock - held_quantity`.

### Câu 3: DTO là gì và tại sao em không trả thẳng Entity ra cho giao diện?
- **Gợi ý trả lời:** DTO (Data Transfer Object) là đối tượng chỉ dùng để vận chuyển dữ liệu giữa Client và Server. Ta không trả thẳng Entity ra vì:
  1. Bảo mật: Tránh lộ các trường nhạy cảm như `password_hash`, thông tin nội bộ.
  2. Hiệu năng: Tránh lỗi LazyInitializationException hoặc vòng lặp vô tận (Infinite Recursion) do quan hệ hai chiều của Hibernate khi Jackson parse JSON.
  3. Linh hoạt: DTO có thể chứa thêm các trường tính toán mà trong DB không có.

---

## PHẦN 2: MỨC ĐỘ KHÁ — GIỎI (THANG ĐIỂM 7.0 - 8.5)

### Câu 4: Em hãy giải thích chi tiết luồng Đặt hàng (Checkout) trong `OrderServiceImpl`?
- **Gợi ý trả lời:** Luồng gồm 6 bước chặt chẽ:
  1. Validate giỏ hàng và danh sách sản phẩm.
  2. Sắp xếp danh sách SKU ID theo thứ tự tăng dần trước khi thao tác DB để triệt tiêu nguy cơ Deadlock khi nhiều khách cùng mua đồng thời.
  3. Kiểm tra Tồn khả dụng tại chi nhánh (`actual_stock - held_quantity >= buyQuantity`).
  4. Nếu khách chọn giao hàng tận nơi, hệ thống tự động tìm chi nhánh gần nhất có đủ hàng.
  5. Tính toán giảm giá từ Voucher (kiểm tra hạn dùng, đơn tối thiểu, mức giảm tối đa).
  6. Mở `@Transactional`: Lưu `DonHang`, lưu `ChiTietDonHang` kèm **Snapshot giá và tên**, tạo bản ghi `GiuTonDonHang` trạng thái `HELD`, và tăng `held_quantity` tại kho tương ứng.

### Câu 5: Tại sao bảng `ChiTietDonHang` phải lưu Snapshot giá và tên sản phẩm?
- **Gợi ý trả lời:** Vì giá bán của mỹ phẩm và tên sản phẩm có thể thay đổi theo thời gian (tăng giá do lạm phát, đổi bao bì). Nếu ta chỉ lưu `sku_id` rồi JOIN ngược lại bảng `SKU` để lấy giá thì khi giá sản phẩm bị sửa, toàn bộ đơn hàng khách đã mua từ tháng trước sẽ bị tính lại tiền, gây sai lệch báo cáo tài chính và sổ sách kế toán. Snapshot đảm bảo tính bất biến của lịch sử giao dịch.

### Câu 6: Cơ chế phân quyền chi nhánh (`StoreScope`) hoạt động ra sao?
- **Gợi ý trả lời:** Khi nhân viên đăng nhập, token JWT chứa danh sách `storeIds` mà nhân viên đó được phân công. `StoreScopeInterceptor` sẽ trích xuất ID này và lưu vào một biến `ThreadLocal<Long>`. Khi nhân viên truy cập các chức năng xem đơn hàng hay tồn kho, tầng Service sẽ gọi `StoreScope.getStoreId()` để tự động lọc dữ liệu của đúng chi nhánh đó. Cơ chế `ThreadLocal` đảm bảo dữ liệu giữa các request của các nhân viên khác nhau không bao giờ bị lẫn lộn, và luôn được gọi `.clear()` sau khi request kết thúc để tránh rò rỉ bộ nhớ.

---

## PHẦN 3: MỨC ĐỘ XUẤT SẮC (THANG ĐIỂM 9.0 - 10.0)

### Câu 7: Vấn đề N+1 Query trong JPA là gì và em đã tối ưu nó ở đâu trong dự án?
- **Gợi ý trả lời:** Vấn đề N+1 xảy ra khi truy vấn 1 danh sách N đối tượng cha (ví dụ N Đơn hàng), sau đó lặp qua từng đơn để lấy danh sách con (các món trong đơn), dẫn đến Hibernate phát sinh thêm N câu lệnh SELECT con riêng biệt. Trong dự án, em đã giải quyết bằng:
  1. Sử dụng `JOIN FETCH` trong câu truy vấn `@Query` của Spring Data JPA để nạp toàn bộ cha và con trong 1 câu SQL duy nhất.
  2. Cấu hình `spring.jpa.properties.hibernate.default_batch_fetch_size=50` trong `application.properties` để Hibernate tự động gom các khóa ngoại và dùng mệnh đề `WHERE id IN (?, ?, ...)` thay vì query lẻ tẻ.

### Câu 8: Tại sao lại chọn lưu JWT trong HttpOnly Cookie thay vì LocalStorage của trình duyệt?
- **Gợi ý trả lời:** Nếu lưu JWT trong `LocalStorage`, mã độc XSS (Cross-Site Scripting) do kẻ tấn công chèn qua ô bình luận hoặc script bên ngoài có thể dùng lệnh `localStorage.getItem('token')` để đánh cắp token của người dùng. Ngược lại, Cookie được cấu hình `HttpOnly = true` thì trình duyệt hoàn toàn cấm JavaScript truy cập vào. Cookie sẽ được tự động đính kèm vào header của HTTP request gửi lên server, vừa an toàn trước XSS, vừa kết hợp thêm cờ `SameSite = Lax` để phòng chống tấn công CSRF.
