# 02 — GIÁO TRÌNH HỌC TỪNG FILE SOURCE CODE CHI TIẾT (CODE LESSONS)

> **Mục tiêu:** Đọc code thực tế → Giải thích từng dòng → Đưa ví dụ nghiệp vụ → Kiểm tra vấn đáp → Sửa lỗi hiểu sai.  
> **Nguyên tắc:** Giữ nguyên tên class, method, biến, annotation, endpoint và bảng CSDL tiếng Anh để đối chiếu mã nguồn.  

---

# BÀI HỌC SỐ 01: CLASS KHỞI CHẠY HỆ THỐNG
## [`CosmeticStoreChainManagementApplication.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/CosmeticStoreChainManagementApplication.java)

---

### BƯỚC 1: ĐỊNH VỊ FILE
- **Đường dẫn trong repository:** `src/main/java/com/thinh/cosmetic/CosmeticStoreChainManagementApplication.java`
- **Package:** `com.thinh.cosmetic`
- **Tên class:** `CosmeticStoreChainManagementApplication`
- **Vị trí trong kiến trúc:** Entry Point (điểm nhập xuất đầu tiên) của toàn bộ ứng dụng Java Web.
- **Thành phần gọi file này:** Máy ảo Java (JVM) khi chạy lệnh `java -jar` hoặc Maven plugin `mvn spring-boot:run`.
- **Thành phần file này sử dụng:** Framework Spring Boot (`SpringApplication`), kích hoạt tính năng bất đồng bộ đa luồng (`@EnableAsync`).

---

### BƯỚC 2: GIẢI THÍCH TỔNG QUAN
- **File này dùng để làm gì?**  
  Đây là file gốc khởi động toàn bộ dự án LUNEA. Khi hàm `main` được kích hoạt, Spring Boot sẽ quét toàn bộ các class trong dự án, khởi tạo IoC Container (Inversion of Control Container), nạp các cấu hình kết nối database, khởi chạy server nhúng Apache Tomcat tại cổng 8080 và kích hoạt các service ngầm.
- **Giải quyết vấn đề gì?**  
  Thiếu file này, ứng dụng Java Web không thể khởi động, không có Web Server tiếp nhận request từ người dùng, và các Bean (`@Service`, `@Repository`, `@Controller`) sẽ không được nạp vào bộ nhớ.
- **Tại sao có `@EnableAsync`?**  
  Dự án LUNEA có tính năng gửi Email mã OTP xác thực qua Brevo API / SMTP khi khách hàng đăng ký hoặc quên mật khẩu. Quá trình gửi email qua mạng internet mất từ 1 - 2 giây. Nếu chạy tuần tự (synchronous/blocking), người dùng phải ngồi chờ màn hình quay vòng vòng. Annotation `@EnableAsync` mở khóa cho phép Spring đẩy tác vụ gửi mail vào một luồng riêng chạy ngầm (asynchronous/non-blocking), giúp giao diện web phản hồi ngay tức thì cho khách hàng chỉ trong 50ms.

---

### BƯỚC 3: NỘI DUNG CODE THỰC TẾ TRONG DỰ ÁN

```java
package com.thinh.cosmetic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CosmeticStoreChainManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CosmeticStoreChainManagementApplication.class, args);
    }

}
```

---

### BƯỚC 4: GIẢI THÍCH CHI TIẾT TỪNG DÒNG CODE

#### 1. Dòng 1: `package com.thinh.cosmetic;`
- **Ý nghĩa:** Định nghĩa gói gốc (root package) của dự án.
- **Quy tắc Spring Boot:** Tất cả các package con như `com.thinh.cosmetic.controller`, `com.thinh.cosmetic.service`, `com.thinh.cosmetic.repository`, `com.thinh.cosmetic.domain` đều nằm bên dưới package gốc này. Vị trí đặt file này quyết định phạm vi quét Bean tự động của Spring.

#### 2. Dòng 3 - 5: Các lệnh `import`
- `org.springframework.boot.SpringApplication`: Lớp tiện ích của Spring Boot cung cấp phương thức `run()` để khởi động ứng dụng từ phương thức tĩnh `main`.
- `org.springframework.boot.autoconfigure.SpringBootApplication`: Annotation chủ chốt của Spring Boot.
- `org.springframework.scheduling.annotation.EnableAsync`: Annotation kích hoạt cơ chế xử lý bất đồng bộ (`@Async`) của Spring.

#### 3. Dòng 7: `@EnableAsync`
- **Thuộc về:** Spring Context / Scheduling.
- **Tác dụng:** Quét toàn bộ Spring Context tìm các phương thức được đánh dấu `@Async` (như trong `EmailServiceImpl.java`). Khi gặp `@Async`, Spring sẽ dùng kỹ thuật Dynamic Proxy để bọc phương thức đó, ủy quyền việc thực thi cho một Thread Pool ngầm (`TaskExecutor`) thay vì luồng chính đang phục vụ request HTTP.

#### 4. Dòng 8: `@SpringBootApplication`
- **Thuộc về:** Spring Boot Autoconfigure.
- **Bản chất:** Đây là một Meta-Annotation (annotation tổng hợp) tương đương với 3 annotation sau gộp lại:
  1. `@SpringBootConfiguration`: Đánh dấu class này là một Configuration Bean trong Spring Context.
  2. `@EnableAutoConfiguration`: Bật tính năng cấu hình tự động thông minh. Spring Boot sẽ tự phân tích các file JAR trong classpath (được khai báo ở `pom.xml`). Thấy có `spring-boot-starter-data-jpa` thì tự cấu hình DataSource và Hibernate; thấy có `spring-boot-starter-web` thì tự dựng Embedded Tomcat.
  3. `@ComponentScan`: Chỉ thị cho Spring tự động quét đệ quy từ package hiện tại (`com.thinh.cosmetic`) trở xuống để tìm và khởi tạo tất cả các Bean có annotation: `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, `@Configuration`.

#### 5. Dòng 9: `public class CosmeticStoreChainManagementApplication`
- **Từ khóa:** `public class` — lớp công khai để JVM bên ngoài có thể gọi tới. Tên class đặt theo tên dự án: *Cosmetic Store Chain Management Application*.

#### 6. Dòng 11: `public static void main(String[] args)`
- **`public`:** Cho phép JVM từ bất kỳ môi trường nào cũng truy cập được.
- **`static`:** Phương thức tĩnh, JVM có thể gọi trực tiếp mà không cần khởi tạo đối tượng `new CosmeticStoreChainManagementApplication()`.
- **`void`:** Không trả về giá trị khi kết thúc.
- **`String[] args`:** Mảng tham số dòng lệnh truyền vào khi khởi chạy (ví dụ `--server.port=9090` hoặc profile `--spring.profiles.active=prod`).

#### 7. Dòng 12: `SpringApplication.run(CosmeticStoreChainManagementApplication.class, args);`
- **Ý nghĩa:** Dòng lệnh trung tâm thực hiện quá trình Bootstrap của Spring Boot:
  1. Khởi tạo đối tượng `ApplicationContext` (kho chứa toàn bộ Bean của ứng dụng).
  2. Nạp cấu hình từ `application.properties`.
  3. Khởi chạy máy chủ Web nhúng (Embedded Tomcat) trên cổng 8080 (hoặc cổng cấu hình động qua biến môi trường `$PORT` trên Render).
  4. Quét và nạp tất cả các Repository, Service, Controller vào bộ nhớ.
  5. Kích hoạt lớp nạp dữ liệu mẫu ban đầu: `DataSeeder.java` (implement `CommandLineRunner`).

---

### BƯỚC 5: VÍ DỤ MINH HỌA NGHIỆP VỤ THỰC TẾ

**Tình huống:** Khách hàng Nguyễn Văn A nhập email trên trang `/auth/register` và bấm *"Nhận mã xác thực OTP"*.
- **Nếu KHÔNG có `@EnableAsync`:**
  - Request đi vào `AuthRestController` -> `OtpServiceImpl` -> `EmailServiceImpl`.
  - Luồng HTTP Tomcat Thread `http-nio-8080-exec-1` bị **khóa cứng (block)** trong 2 giây để chờ máy chủ Brevo API phản hồi thành công.
  - Trình duyệt khách hàng bị xoay vòng, nếu có 100 người cùng bấm gửi mail thì toàn bộ 200 thread của Tomcat sẽ bị nghẽn (Server Connection Timeout).
- **Nhờ CÓ `@EnableAsync` tại file này:**
  - Khi `EmailServiceImpl.sendVerificationEmail()` được gọi, luồng chính lập tức giao việc cho Worker Thread `task-1` trong Thread Pool và trả kết quả về cho Controller ngay lập tức.
  - Phản hồi JSON `{"status": 200, "message": "Mã OTP đã được gửi đến email"}` trả về cho trình duyệt chỉ mất **35 mili-giây**! Trải nghiệm người dùng mượt mà tuyệt đối.

---

### BƯỚC 6: MÔ TẢ LUỒNG THỰC THI (EXECUTION LIFECYCLE)

```
[1. Lệnh Run / Command Line]
       │
       ▼
[2. JVM gọi hàm main()]
       │
       ▼
[3. SpringApplication.run()]
       │
       ├─► Đọc application.properties (Datasource URL, JPA dialect, JWT secret, Brevo API key)
       │
       ├─► Kích hoạt @EnableAsync (Chuẩn bị Thread Pool cho tác vụ gửi mail/thông báo)
       │
       ├─► Thực hiện @ComponentScan (Quét toàn bộ package com.thinh.cosmetic.*)
       │     ├── Nạp các @Entity (Account, Product, Store, Order...) vào JPA Metamodel
       │     ├── Nạp các @Repository (AccountRepository, OrderRepository...)
       │     ├── Nạp các @Service (OrderServiceImpl, ProductServiceImpl...)
       │     └── Nạp các @Controller & @RestController (xử lý routing URL)
       │
       ├─► Nạp SecurityConfig (Khởi tạo SecurityFilterChain, CookieBearerTokenResolver)
       │
       ├─► Khởi động Embedded Tomcat Web Server trên cổng 8080
       │
       ├─► Gọi CommandLineRunner (DataSeeder.run() kiểm tra CSDL, nạp 240 sản phẩm mẫu)
       │
       ▼
[4. Ứng dụng sẵn sàng lắng nghe Request tại http://localhost:8080]
```

---

### BƯỚC 7: BỘ CÂU HỎI VẤN ĐÁP CỐT LÕI VỀ FILE NÀY

1. **Câu hỏi 1 (Cơ bản):** Annotation `@SpringBootApplication` là sự kết hợp của 3 annotation nào? Nêu vai trò của từng cái?
2. **Câu hỏi 2 (Kiến trúc):** Tại sao file này phải nằm ở package `com.thinh.cosmetic`? Nếu di chuyển file này vào `com.thinh.cosmetic.controller` thì chuyện gì sẽ xảy ra?
3. **Câu hỏi 3 (Chuyên sâu):** Annotation `@EnableAsync` dùng để giải quyết bài toán nghiệp vụ gì trong dự án LUNEA? Nếu bỏ nó đi thì ứng dụng có báo lỗi compile không, và hành vi lúc chạy thay đổi ra sao?
4. **Câu hỏi 4 (Luồng chạy):** Dòng lệnh `SpringApplication.run()` thực hiện những việc gì sau hậu trường trước khi web server sẵn sàng phục vụ?
5. **Câu hỏi 5 (Vận hành):** Sau khi `SpringApplication.run()` kết thúc, class nào trong dự án sẽ được tự động kích hoạt để nạp dữ liệu ban đầu cho database?

---

### BƯỚC 8: CÂU HỎI KIỂM TRA HỌC VIÊN (VIVA TEST)

> [!IMPORTANT]
> **CÂU HỎI KIỂM TRA SỐ 1 DÀNH CHO BẠN:**
> *Trong file `CosmeticStoreChainManagementApplication.java`, annotation `@SpringBootApplication` là sự kết hợp của 3 annotation cốt lõi nào trong Spring Framework? Nếu chúng ta di chuyển file này vào trong package con `com.thinh.cosmetic.controller` thì hệ thống sẽ gặp phải lỗi gì khi khởi động và vì sao?*

---

### BƯỚC 9: KẾT LUẬN & GHI NHỚ MẤU CHỐT

- **3 ý quan trọng nhất:**
  1. Là Entry Point duy nhất khởi tạo IoC Container và Embedded Tomcat Server.
  2. Vị trí ở root package `com.thinh.cosmetic` là yếu tố sống còn để `@ComponentScan` quét được toàn bộ các tầng Service, Repository, Entity, Controller của dự án.
  3. `@EnableAsync` kích hoạt đa luồng bất đồng bộ, tối ưu hóa tốc độ gửi email OTP qua Brevo API mà không làm nghẽn luồng xử lý web.
- **Trạng thái file trong Inventory:** `C — Đã học` *(Sẽ chuyển thành `D — Đã kiểm tra` sau khi bạn trả lời chính xác câu hỏi ở Bước 8)*.
- **File tiếp theo cần học:** [`pom.xml`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/pom.xml) và [`src/main/resources/application.properties`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/resources/application.properties).
