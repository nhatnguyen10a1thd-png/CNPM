# BỘ CÂU HỎI VẤN ĐÁP & CÂU TRẢ LỜI MẪU THỰC CHIẾN (04_Viva_QA.md)
## 50 Câu Hỏi Phản Biện Phân Loại Theo 10 Nhóm Chủ Đề Dự Án LUNEA

> **Nguyên tắc trả lời:**  
> 1. Trả lời thẳng vào trọng tâm câu hỏi trong 1–2 câu đầu tiên (Pass answer).  
> 2. Đưa ngay bằng chứng bằng tên File, Class, Method hoặc Bảng CSDL thực tế.  
> 3. Giải thích lý do thiết kế ("Tại sao?") và hướng phát triển khi bị truy vấn sâu.

---

## NHÓM 1: TỔNG QUAN DỰ ÁN & NGHIỆP VỤ

### Câu 1: Em hãy tóm tắt dự án của em giải quyết vấn đề gì trong thực tế?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra sinh viên có hiểu bản chất nghiệp vụ của hệ thống hay chỉ đi học thuộc code.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô, dự án LUNEA giải quyết bài toán vận hành website thương mại điện tử cho chuỗi showroom mỹ phẩm. Điểm khác biệt lớn nhất so với các web bán hàng đơn lẻ là: LUNEA quản lý tập trung về danh mục sản phẩm, bảng giá và khuyến mãi, nhưng **tồn kho, nhân viên xử lý và điều phối đơn hàng được phân bổ độc lập theo từng chi nhánh vật lý**. Khách hàng có thể tra cứu tồn kho từng showroom gần nhất, và khi đặt hàng online thì hệ thống tự động gán đơn về chi nhánh cùng tỉnh/thành phố có đủ tồn kho để đóng gói và giao hàng."*
- **Bằng chứng trong dự án:** Bảng `CuaHang`, `TonKho`, `GiuTonDonHang`, logic điều phối trong [`OrderServiceImpl.java#L208-L242`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L208-L242).
- **Câu hỏi truy vấn tiếp:** *Nếu khách ở Cần Thơ đặt hàng mà LUNEA chưa có showroom ở Cần Thơ thì hệ thống xử lý thế nào?*
- **Cách trả lời:** *Hệ thống sẽ quét các showroom đang hoạt động có đủ hàng (ở HCM, Hà Nội hoặc Đà Nẵng) và chọn showroom có ID nhỏ nhất (mặc định là Flagship HCM) để thực hiện đóng gói và gửi chuyển phát nhanh liên tỉnh ạ.*

---

### Câu 2: Trong hệ thống, một sản phẩm có bao nhiêu biến thể và tại sao phải tách Product và SKU?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra tư duy thiết kế cơ sở dữ liệu thương mại điện tử thực tế.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, dự án tách làm 2 thực thể: `ProductEntity` và `ProductSkuEntity`. Trong ngành mỹ phẩm, cùng một dòng 'Serum Bí Đao Cocoon' nhưng có thể có nhiều dung tích như 70ml, 140ml với mã vạch Barcode, giá niêm yết và giá bán khác nhau. `Product` lưu thông tin chung (tên, thương hiệu, thành phần, công dụng), còn `SKU` (Stock Keeping Unit) là đơn vị định danh hàng hóa cụ thể được đưa vào quản lý tồn kho tại các showroom."*
- **Bằng chứng trong dự án:** [`ProductEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductEntity.java) quan hệ 1-Nhiều với [`ProductSkuEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java).

---

### Câu 3: Khái niệm "Tồn kho thực tế", "Hàng đang giữ" và "Tồn khả dụng" trong dự án hoạt động ra sao?
- **Mức độ:** Trung bình (Rất hay bị hỏi).
- **Ý đồ câu hỏi:** Kiểm tra hiểu biết về giải pháp chống bán vượt tồn (Overselling).
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô, trong bảng `TonKho`:
  - **Tồn thực tế (`actual_stock`):** Là số lượng hộp mỹ phẩm vật lý đang nằm trên kệ showroom.
  - **Hàng đang giữ (`held_quantity`):** Là số lượng sản phẩm đã được khách đặt online thành công (đơn ở trạng thái Chờ xác nhận hoặc Đang chuẩn bị) nhưng shipper chưa lấy đi.
  - **Tồn khả dụng (Available Stock):** Là số lượng thực sự còn lại cho phép khách mua tiếp, tính bằng công thức: $\text{Available} = \text{actual\_stock} - \text{held\_quantity}$."*
- **Bằng chứng trong dự án:** [`InventoryEntity.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/domain/entity/inventory/InventoryEntity.java), logic kiểm tra tại [`OrderServiceImpl.java#L194`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L194).
- **Câu hỏi truy vấn tiếp:** *Khi nào thì hàng bị trừ thẳng vào tồn thực tế?*
- **Cách trả lời:** *Khi nhân viên chi nhánh chuyển trạng thái đơn hàng sang `SHIPPING` (Đang giao), tức là shipper đã nhận hàng rời khỏi showroom thì hàm `commitHeldInventory()` mới trừ `actual_stock` và trừ `held_quantity` ạ.*

---

## NHÓM 2: KIẾN TRÚC & CẤU TRÚC MÃ NGUỒN

### Câu 4: Trình bày kiến trúc tổng thể của dự án LUNEA?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra khả năng nhìn nhận hệ thống mức kiến trúc.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, dự án được xây dựng theo kiến trúc phân tầng chuẩn công nghiệp (Layered Architecture) gồm 4 tầng chính:
  1. **Tầng Presentation:** Kết hợp Server-Side Rendering bằng Thymeleaf cho các trang Storefront thân thiện SEO và REST Controller trả về JSON cho các tác vụ bất đồng bộ AJAX/Admin.
  2. **Tầng Security:** Spring Security 6+ kết hợp OAuth2 Resource Server xử lý Stateless JWT giải mã từ HttpOnly Cookie.
  3. **Tầng Service:** Chứa toàn bộ nghiệp vụ logic, ranh giới giao dịch `@Transactional`, và bộ lọc chi nhánh `StoreScope`.
  4. **Tầng Persistence (Data Access):** Spring Data JPA kết hợp Hibernate ORM giao tiếp với CSDL PostgreSQL hoặc SQL Server."*
- **Bằng chứng trong dự án:** Xem sơ đồ kiến trúc tại `01_Project_Map.md`.

---

### Câu 5: Tại sao dự án của em dùng cả Controller thường và RestController?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra sinh viên có phân biệt được MVC truyền thống và kiến trúc REST API.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ:
  - Các `@Controller` (trong package `com.thinh.cosmetic.controller`) chịu trách nhiệm render ra các trang giao diện HTML thông qua Thymeleaf Template Engine (`home.html`, `product-detail.html`). Ưu điểm là tải trang nhanh lần đầu và hỗ trợ SEO tốt.
  - Các `@RestController` (trong package `com.thinh.cosmetic.rest`) chịu trách nhiệm cung cấp các API xử lý dữ liệu và trả về JSON chuẩn (`@ResponseBody`). Được sử dụng cho các thao tác mượt mà không cần load lại trang như: thêm giỏ hàng, áp dụng voucher, chuyển trạng thái đơn hàng, và phân quyền giao diện Admin."*
- **Bằng chứng trong dự án:** So sánh [`ProductPageController.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/controller/ProductPageController.java) (trả về String view name) và [`ProductRestController.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/rest/catalog/ProductRestController.java) (trả về `ResponseEntity<ProductResponse>`).

---

### Câu 6: DTO là gì và tại sao trong dự án lại không trả thẳng Entity ra cho Controller?
- **Mức độ:** Cơ bản đến Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra nguyên lý đóng gói và bảo mật tầng dữ liệu.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, DTO (Data Transfer Object) là các đối tượng dùng để đóng gói và chuyển dữ liệu giữa các tầng:
  1. **Bảo mật:** Entity `AccountEntity` chứa trường `passwordHash`. Nếu trả thẳng Entity ra JSON, mật khẩu đã mã hóa hoặc thông tin nhạy cảm có thể bị lộ ra ngoài trình duyệt.
  2. **Tránh lỗi đệ quy vô tận (Infinite Recursion):** Trong JPA, các quan hệ hai chiều `@OneToMany` và `@ManyToOne` khi serialize sang JSON sẽ gọi lẫn nhau dẫn đến lỗi `StackOverflowError`.
  3. **Tối ưu băng thông:** DTO chỉ chứa đúng những trường mà màn hình cần hiển thị. Dự án sử dụng thư viện **MapStruct** để tự động ánh xạ Entity sang DTO một cách an toàn và có hiệu năng cao nhất tại thời điểm biên dịch."*
- **Bằng chứng trong dự án:** Các DTO trong `domain/dto/` và mapper trong [`mapper/catalog/ProductMapper.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/mapper/catalog/ProductMapper.java).

---

## NHÓM 3: JAVA CORE ỨNG DỤNG TRONG DỰ ÁN

### Câu 7: Em sử dụng Java Stream API ở đâu trong dự án và mang lại lợi ích gì?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra kỹ năng viết mã hiện đại của Java 8/17/21.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, em sử dụng Stream API rất nhiều trong tầng Service, tiêu biểu là:
  1. Trong [`ProductServiceImpl.java#L182-L194`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java#L182-L194): Dùng `.filter()` để lọc sản phẩm theo từ khóa, thương hiệu, loại da, khoảng giá và `.sorted()` để sắp xếp.
  2. Trong [`OrderServiceImpl.java#L581`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L581): Dùng `.collect(Collectors.groupingBy(...))` để gom nhóm các món hàng theo Order ID trong bộ nhớ RAM, giúp giải quyết triệt để lỗi N+1 Query.
  Lợi ích là code mang phong cách khai báo (Declarative), ngắn gọn, dễ đọc và tối ưu hóa thao tác xử lý tập hợp."*

---

### Câu 8: Tại sao trước khi trừ tồn kho trong đơn hàng, em lại có đoạn code sắp xếp SKU ID?
- **Mức độ:** Khó (Điểm 9–10).
- **Ý đồ câu hỏi:** Kiểm tra kiến thức chuyên sâu về Database Concurrency và Deadlock.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô, tại dòng 274–276 file `OrderServiceImpl.java`, em thực hiện:
  ```java
  sortedCartItems.sort(Comparator.comparing(item -> item.getSku().getId()));
  ```
  Đây là kỹ thuật **ngăn chặn Deadlock (Deadlock Prevention)** ở tầng cơ sở dữ liệu. Giả sử có 2 khách hàng cùng đặt đơn có chứa 2 sản phẩm A và B cùng một thời điểm:
  - Nếu không sắp xếp, giao dịch 1 có thể khóa bản ghi A trước rồi chờ B, trong khi giao dịch 2 khóa B trước rồi chờ A ➔ Hai luồng chờ chéo nhau gây ra Deadlock (CSDL sẽ kill 1 trong 2 transaction).
  - Khi sắp xếp theo thứ tự ID tăng dần, cả hai giao dịch đều bắt buộc phải khóa A trước rồi mới khóa B ➔ Triệt tiêu hoàn toàn điều kiện chờ đợi vòng tròn (Circular Wait), hệ thống xử lý đồng thời cực kỳ ổn định."*
- **Bằng chứng trong dự án:** [`OrderServiceImpl.java#L274-L276`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L274-L276).

---

### Câu 9: Thuật toán tách dấu tiếng Việt trong dự án của em hoạt động như thế nào?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra hiểu biết về String manipulation và chuẩn Unicode.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, trong file `ProductServiceImpl.java`, em xây dựng hàm `stripAccents()` sử dụng lớp `java.text.Normalizer`:
  ```java
  String n = Normalizer.normalize(s, Normalizer.Form.NFD);
  return n.replaceAll("\\p{M}", "").replace("đ", "d").replace("Đ", "d");
  ```
  Cơ chế là dùng chuẩn **NFD (Canonical Decomposition)** để phân rã một ký tự có dấu (ví dụ `á`) thành 2 mã Unicode độc lập: ký tự gốc `a` và dấu sắc `'`. Sau đó, biểu thức chính quy `\\p{M}` sẽ khớp và xóa sạch toàn bộ các dấu thanh/mũ. Cuối cùng chuyển chữ 'đ' thành 'd'. Nhờ đó, khách hàng gõ không dấu hay có dấu đều tìm thấy sản phẩm chính xác."*
- **Bằng chứng trong dự án:** [`ProductServiceImpl.java#L226-L230`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java#L226-L230).

---

## NHÓM 4: SPRING FRAMEWORK & SPRING SECURITY

### Câu 10: Dependency Injection (DI) và Inversion of Control (IoC) xuất hiện ở đâu trong dự án?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra kiến thức nền tảng bắt buộc của Spring Framework.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, IoC (Đảo ngược điều khiển) là cơ chế mà Spring IoC Container quản lý toàn bộ vòng đời và khởi tạo các Beans (như Service, Repository, Controller).
  Dự án áp dụng DI (Tiêm phụ thuộc) thông qua kỹ thuật **Constructor Injection** kết hợp annotation `@RequiredArgsConstructor` của Lombok:
  Spring sẽ tự động tìm các Bean tương ứng (ví dụ `OrderRepository`, `VoucherService`) và tiêm vào `OrderServiceImpl` thông qua constructor. Dự án không dùng `@Autowired` trực tiếp trên field vì Constructor Injection giúp các dependency là `final` (bất biến) và dễ dàng viết Unit Test bằng Mockito."*
- **Bằng chứng trong dự án:** Tất cả các class Service và Controller đều khai báo `@Service @RequiredArgsConstructor` và các thuộc tính `private final ...`.

---

### Câu 11: Ranh giới giao dịch `@Transactional` hoạt động ra sao nếu xảy ra lỗi giữa chừng?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra hiểu biết về ACID và Transaction Management trong Spring.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, khi một method trong Service được đánh dấu `@Transactional` (ví dụ `placeOrder`), Spring sẽ bọc method đó trong một database transaction.
  Nếu toàn bộ các lệnh chạy thành công, transaction sẽ được `COMMIT`.
  Nếu có bất kỳ ngoại lệ `RuntimeException` (như `BusinessException` hết hàng hoặc sai voucher) bị ném ra, Spring sẽ lập tức kích hoạt `ROLLBACK`: toàn bộ dữ liệu vừa lưu như bản ghi Đơn hàng, Chi tiết đơn, Tồn kho đều được hủy bỏ quay về trạng thái ban đầu, đảm bảo tính toàn vẹn dữ liệu (Atomicity)."*
- **Bằng chứng trong dự án:** `@Transactional` đặt ở đầu class [`OrderServiceImpl.java#L59`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L59).

---

### Câu 12: Tại sao trong `SecurityConfig` em lại cấu hình `SessionCreationPolicy.STATELESS` và tắt CSRF?
- **Mức độ:** Khó.
- **Ý đồ câu hỏi:** Kiểm tra hiểu biết chuyên sâu về kiến trúc bảo mật hiện đại.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ:
  1. **Tắt Session (`STATELESS`):** Dự án sử dụng mã xác thực JWT (JSON Web Token). Máy chủ không lưu giữ phiên làm việc của user trong bộ nhớ RAM (Tomcat session). Nhờ đó máy chủ tiết kiệm tài nguyên RAM (giúp app chạy mượt trong giới hạn 512MB trên Render) và dễ dàng mở rộng nhiều instance (horizontal scaling).
  2. **Tắt CSRF (`csrf.disable()`):** CSRF là hình thức tấn công lợi dụng việc trình duyệt tự động gửi cookie session của phiên đăng nhập. Do hệ thống sử dụng Stateless Token, kết hợp với việc Cookie `LUNEA_AT` được cấu hình cờ bảo mật cao cấp **`SameSite=Lax`**, trình duyệt bên thứ ba không thể tự ý kích hoạt các request đột lốt, do đó việc tắt CSRF là an toàn và giúp các API nhận request từ AJAX fetch mượt mà."*
- **Bằng chứng trong dự án:** [`SecurityConfig.java#L82-L83`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/config/SecurityConfig.java#L82-L83).

---

## NHÓM 5: BẢO MẬT, XÁC THỰC & PHÂN QUYỀN (STORESCOPE)

### Câu 13: Trình bày cơ chế trích xuất token của `CookieBearerTokenResolver`?
- **Mức độ:** Trung bình đến Khó.
- **Ý đồ câu hỏi:** Đánh giá khả năng tùy biến Spring Security ngoài các cấu hình mặc định.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, mặc định Spring Security OAuth2 Resource Server chỉ đọc token trong header `Authorization: Bearer <token>`. Nhưng giao diện web của LUNEA là Server-Side Rendering kết hợp Fetch, nên token được lưu an toàn trong Cookie `LUNEA_AT`.
  Em đã viết class [`CookieBearerTokenResolver`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/CookieBearerTokenResolver.java) cài đặt interface `BearerTokenResolver`:
  - Bước 1: Ưu tiên kiểm tra xem request có gửi Header `Authorization` không (dành cho Postman / Mobile App).
  - Bước 2: Nếu không có, duyệt danh sách Cookie trong request tìm cookie tên `LUNEA_AT` và lấy giá trị ra.
  Nhờ đó, một cấu hình bảo mật duy nhất phục vụ được đồng thời cả trình duyệt web lẫn các client gọi API bên ngoài."*
- **Bằng chứng trong dự án:** [`CookieBearerTokenResolver.java#L16-L39`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/CookieBearerTokenResolver.java#L16-L39).

---

### Câu 14: StoreScope phân quyền chi nhánh như thế nào? Bằng chứng trong code?
- **Mức độ:** Khó (Điểm nhấn kiến trúc chuỗi).
- **Ý đồ câu hỏi:** Kiểm tra giải pháp kỹ thuật ngăn chặn nhân viên truy cập trái phép dữ liệu chi nhánh khác.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, StoreScope hoạt động như sau:
  1. Khi nhân viên đăng nhập, `StoreScope.getAccessibleStoreIds(principal)` kiểm tra: Nếu có quyền `ROLE_ADMIN` hoặc `ALL_STORES` thì trả về `null` (xem toàn chuỗi).
  2. Nếu là nhân viên thông thường, truy vấn bảng `NhanVienCuaHang` để lấy danh sách ID các chi nhánh mà nhân viên đó trực thuộc.
  3. Khi nhân viên xem danh sách đơn hàng, Service truyền danh sách ID này vào câu query SQL `WHERE assigned_store_id IN (:storeIds)`.
  4. Nếu nhân viên cố tình thao tác trên một đơn hàng cụ thể, hàm `storeScope.canAccessStore(principal, storeId)` sẽ kiểm tra. Nếu ID chi nhánh của đơn không nằm trong danh sách quyền thì ném lỗi `ACCESS_DENIED` (HTTP 403) ngay lập tức."*
- **Bằng chứng trong dự án:** [`StoreScope.java`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/security/StoreScope.java) và [`OrderServiceImpl.java#L484-L488`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L484-L488).

---

### Câu 15: Tại sao lại dùng `SecureRandom` cho OTP mà không dùng `Math.random()`?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra kiến thức an toàn thông tin (Cryptography).
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, `Math.random()` hoặc `java.util.Random` chỉ là bộ sinh số giả ngẫu nhiên tuyến tính (Pseudorandom), thuật toán bên dưới hoàn toàn có thể bị kẻ tấn công dịch ngược (reverse-engineer) để đoán trước mã OTP tiếp theo nếu biết được giá trị seed.
  Dự án sử dụng `java.security.SecureRandom` là bộ sinh số ngẫu nhiên an toàn mã hóa (CSPRNG), lấy nguồn entropy ngẫu nhiên từ hệ điều hành, đảm bảo mã OTP 6 chữ số sinh ra hoàn toàn không thể dự đoán trước được."*
- **Bằng chứng trong dự án:** [`OtpServiceImpl.java#L46`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/account/impl/OtpServiceImpl.java#L46).

---

## NHÓM 6: CƠ SỞ DỮ LIỆU, SQL & HIBERNATE/JPA

### Câu 16: Vấn đề N+1 Query là gì và em giải quyết nó trong dự án như thế nào?
- **Mức độ:** Khó (Câu hỏi kinh điển phỏng vấn Senior/Đồ án).
- **Ý đồ câu hỏi:** Đánh giá khả năng tối ưu hóa truy vấn CSDL trong JPA/Hibernate.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô, vấn đề N+1 xảy ra khi lấy 1 danh sách N đơn hàng, sau đó trong vòng lặp ta gọi `order.getItems()` làm Hibernate phát sinh thêm N câu lệnh SQL con để lấy chi tiết từng đơn hàng, gây quá tải database.
  Dự án giải quyết triệt để trong hàm `toResponseBatch()` của `OrderServiceImpl`:
  - **Query 1:** Lấy danh sách N đơn hàng (`findAll`).
  - **Query 2:** Lấy toàn bộ OrderItem của TẤT CẢ các đơn hàng đó trong 1 câu lệnh duy nhất bằng từ khóa `IN`:
    `findByOrderIdInWithSkuAndProduct(orderIds)` kết hợp `JOIN FETCH` để nạp sẵn SKU và Product.
  - **Query 3:** Lấy toàn bộ ảnh sản phẩm chính trong 1 câu lệnh bằng `findByProductIdInAndIsPrimaryTrue(productIds)`.
  Sau đó, toàn bộ dữ liệu được gom nhóm và ánh xạ hoàn toàn trong bộ nhớ RAM (`Collectors.groupingBy`). Nhờ đó, dù có 1.000 đơn hàng thì hệ thống cũng **chỉ tốn đúng 3 câu lệnh SQL**."*
- **Bằng chứng trong dự án:** [`OrderServiceImpl.java#L570-L602`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L570-L602).

---

### Câu 17: Ràng buộc Unique trên bảng `TonKho` có ý nghĩa gì?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra thiết kế Schema toàn vẹn dữ liệu.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, trên bảng `TonKho`, cặp `(cua_hang_id, sku_id)` được thiết lập `UNIQUE CONSTRAINT`. Ràng buộc này đảm bảo một biến thể SKU tại một showroom vật lý chỉ có duy nhất một dòng ghi nhận số lượng tồn kho, không bao giờ xảy ra tình trạng trùng lặp dữ liệu dẫn đến tính sai số lượng tồn khả dụng."*
- **Bằng chứng trong dự án:** [`04_schema_postgresql.sql#L182`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/database/04_schema_postgresql.sql#L182) (`CONSTRAINT uq_inventory_store_sku UNIQUE (store_id, sku_id)`).

---

### Câu 18: Sự khác nhau giữa `CascadeType.ALL` và việc không dùng Cascade trong Entity là gì?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra kiến thức Hibernate Cascade và vòng đời Entity.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, `CascadeType.ALL` có nghĩa là mọi thao tác (persist, merge, remove) trên Entity cha sẽ tự động lan truyền sang Entity con.
  Ví dụ trong `OrderEntity`, khi xóa đơn hàng thì các `OrderItemEntity` liên kết có thể tự xóa. Tuy nhiên, với `ProductEntity` và `BrandEntity`, ta **tuyệt đối không dùng Cascade Delete**, vì nếu xóa một Thương hiệu mà lan truyền xóa toàn bộ Sản phẩm thì sẽ gây mất mát dữ liệu nghiêm trọng trong hệ thống."*

---

## NHÓM 7: XỬ LÝ LỖI, GIAO DỊCH & VALIDATION

### Câu 19: Khi người dùng gửi dữ liệu sai (ví dụ thiếu số điện thoại hoặc mật khẩu quá ngắn), hệ thống xử lý ở đâu?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra luồng Validation trong Spring Boot.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ:
  1. Tại DTO Request, em sử dụng các annotation của Jakarta Validation như `@NotBlank`, `@Size(min=6)`, `@Email`.
  2. Tại Controller, em đặt annotation `@Valid` trước `@RequestBody`. Nếu payload không thỏa mãn, Spring sẽ ném ra ngoại lệ `MethodArgumentNotValidException`.
  3. Lớp [`GlobalExceptionHandler`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java) được đánh dấu `@RestControllerAdvice` sẽ chặn ngoại lệ này lại, trích xuất danh sách các trường bị lỗi (`fieldErrors`) và trả về mã lỗi HTTP 400 Bad Request kèm JSON chi tiết cho giao diện hiển thị thông báo."*
- **Bằng chứng trong dự án:** [`GlobalExceptionHandler.java#L38-L62`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java#L38-L62).

---

### Câu 20: Nếu hai khách hàng cùng đặt sản phẩm cuối cùng vào đúng 1 phần nghìn giây, điều gì sẽ xảy ra?
- **Mức độ:** Khó (Câu hỏi tình huống phản biện).
- **Ý đồ câu hỏi:** Kiểm tra tư duy Concurrency và Khóa (Locking).
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô:
  Trong luồng `placeOrder`, hệ thống thực hiện kiểm tra tồn kho lần hai ngay tại thời điểm tạo phiếu giữ:
  ```java
  int availableStock = inv.getActualStock() - inv.getHeldQuantity();
  if (availableStock < item.getQuantity()) {
      throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, ...);
  }
  ```
  Nhờ có `@Transactional`, giao dịch nào được CSDL cấp quyền ghi trước sẽ cập nhật tăng `held_quantity`. Khi giao dịch thứ hai đọc lại bản ghi thì `availableStock` đã bằng 0, hệ thống sẽ ném ra ngoại lệ `INSUFFICIENT_STOCK` và rollback đơn của khách thứ hai.
  Để tối ưu hóa hơn nữa trong các đợt Flash Sale triệu request, em đề xuất có thể áp dụng **Pessimistic Locking** (`SELECT FOR UPDATE`) hoặc **Redis Distributed Lock** để xếp hàng request ạ."*
- **Bằng chứng trong dự án:** [`OrderServiceImpl.java#L299-L305`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java#L299-L305).

---

## NHÓM 8: KIỂM THỬ, HIỆU NĂNG & TRIỂN KHAI CLOUD

### Câu 21: Dự án đã được kiểm thử như thế nào? Em hãy nêu phạm vi các bài test?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Kiểm tra tính xác thực và chất lượng phần mềm.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, dự án đã xây dựng bộ kiểm thử tự động toàn diện gồm **163 bài test** (đều PASS 100%), bao gồm:
  - **Unit Test tầng Service:** Kiểm tra logic tính toán voucher, quy trình đặt hàng, máy trạng thái đơn hàng, và chính sách bảo mật OTP (`OrderServiceTest`, `VoucherServiceTest`, `OtpServiceTest`).
  - **Integration Test tầng Web/Security:** Sử dụng `MockMvc` để kiểm thử phân quyền các endpoint, xác thực cookie JWT, và kiểm tra tính năng cô lập dữ liệu theo vai trò (`SecurityAuthorizationTest`, `CartRestControllerTest`).
  - **Môi trường test:** Toàn bộ test chạy độc lập trên **H2 Database trong bộ nhớ**, không làm ảnh hưởng đến dữ liệu thật của hệ thống."*
- **Bằng chứng trong dự án:** Thư mục `src/test/java/`, kết quả chạy lệnh `.\mvnw.cmd test` (163 tests pass).

---

### Câu 22: Em đã làm gì để ứng dụng Spring Boot chạy mượt mà trên gói RAM 512MB của Render Cloud mà không bị crash (OOM 137)?
- **Mức độ:** Khó (Ăn điểm kỹ sư DevOps/Cloud).
- **Ý đồ câu hỏi:** Kiểm tra kiến thức tối ưu hóa JVM và containerization thực tế.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô, các app Spring Boot khi đóng gói container rất dễ bị Render kill do vượt trần 512MB RAM (Exit code 137). Em đã áp dụng 2 kỹ thuật tối ưu hóa tại `Dockerfile`:
  1. **Multi-stage Build:** Stage 1 dùng Maven để build file JAR, sau đó Stage 2 chỉ chạy trên nền image JRE siêu nhẹ `eclipse-temurin:21-jre-alpine` (chỉ nặng ~180MB).
  2. **JVM Memory Tuning:**
     `ENV JAVA_OPTS="-Xmx350m -Xms128m -Xss512k -XX:+UseSerialGC"`
     - `-Xmx350m`: Khống chế bộ nhớ Heap tối đa 350MB, chừa lại 162MB cho JRE Metaspace và hệ điều hành Linux.
     - `-Xss512k`: Giảm stack size của mỗi thread từ 1MB xuống 512KB.
     - `-XX:+UseSerialGC`: Sử dụng bộ gom rác đơn luồng siêu nhẹ, không tiêu tốn CPU và RAM nền của container."*
- **Bằng chứng trong dự án:** File [`Dockerfile`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/Dockerfile) và mục 3.4 file `HUONG_DAN_CAI_DAT_VA_SU_DUNG.md`.

---

## NHÓM 9: THIẾT KẾ GIAO DIỆN & TRẢI NGHIỆM NGƯỜI DÙNG

### Câu 23: Giao diện website được thiết kế theo phong cách gì và tối ưu trải nghiệm ra sao?
- **Mức độ:** Cơ bản.
- **Ý đồ câu hỏi:** Kiểm tra khía cạnh UI/UX của đồ án Công nghệ phần mềm.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, website LUNEA được thiết kế theo phong cách **Moonlight Dark Luxury** kết hợp bảng màu vàng gold và nền tối sang trọng, phù hợp với định vị ngành mỹ phẩm cao cấp.
  Về trải nghiệm:
  - Khi xem chi tiết sản phẩm, khách bấm đổi dung tích SKU thì giá tiền và giá niêm yết tự động nhảy theo thời gian thực mà không cần reload trang.
  - Có tính năng **'Kiểm tra tồn kho tại showroom'** hiển thị rõ chi nhánh nào còn hàng trước khi mua.
  - Trang đăng nhập có sẵn danh sách các nút **'Chọn nhanh tài khoản Demo' (Fast-login)** giúp giảng viên và người dùng trải nghiệm ngay các vai trò Admin, Thủ kho, Nhân viên đơn hàng chỉ với 1 click."*
- **Bằng chứng trong dự án:** [`templates/product-detail.html`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/resources/templates/product-detail.html) và [`templates/auth/login.html`](file:///c:/Users/Dev/OneDrive/Desktop/lunea/CNPM/src/main/resources/templates/auth/login.html).

---

## NHÓM 10: ĐIỂM HẠN CHẾ & HƯỚNG PHÁT TRIỂN

### Câu 24: Điểm hạn chế lớn nhất của dự án hiện tại là gì?
- **Mức độ:** Trung bình đến Khó (Câu hỏi kiểm tra tính trung thực).
- **Ý đồ câu hỏi:** Xem sinh viên có nhìn nhận được điểm yếu của hệ thống hay chỉ ca ngợi một chiều.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ thưa Thầy/Cô, dự án có 2 điểm hạn chế em nhìn nhận được:
  1. **Thanh toán trực tuyến:** Hiện tại dự án tập trung hoàn thiện nghiệp vụ thanh toán COD (nhận hàng trả tiền) và logic giữ tồn kho. Cổng thanh toán VNPay/MoMo đã có cấu trúc dữ liệu nhưng chưa tích hợp Webhook IPN xử lý hoàn tiền tự động khi hủy đơn.
  2. **Điều phối đa chi nhánh cho một đơn (Split Order):** Hiện tại hệ thống yêu cầu một showroom phải có đủ 100% các món trong giỏ mới giao được. Nếu khách mua 2 món mà showroom A chỉ có món 1, showroom B chỉ có món 2 thì hệ thống sẽ báo chưa đáp ứng được, chưa hỗ trợ tách đơn thành 2 kiện hàng giao từ 2 nơi."*

---

### Câu 25: Nếu có thêm thời gian phát triển, em sẽ nâng cấp tính năng nào trước tiên?
- **Mức độ:** Trung bình.
- **Ý đồ câu hỏi:** Đánh giá tầm nhìn mở rộng phần mềm.
- **Câu trả lời đạt yêu cầu:**  
  *"Dạ, em sẽ ưu tiên 2 nâng cấp:
  1. Tích hợp tính năng **Tách đơn hàng tự động (Order Splitting)**: Cho phép giao hàng từ nhiều chi nhánh khác nhau nếu giỏ hàng của khách không có showroom nào chứa đủ toàn bộ.
  2. Ứng dụng **Redis Caching** cho danh mục sản phẩm và **Redis Distributed Lock** cho bảng tồn kho để phục vụ các chiến dịch Flash Sale với lưu lượng truy cập lớn."*
