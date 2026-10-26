# HƯỚNG DẪN CÀI ĐẶT & SỬ DỤNG CƠ SỞ DỮ LIỆU HỆ THỐNG LUNEA
## Dự án: Nền Tảng Thương Mại Điện Tử & Quản Lý Chuỗi Cửa Hàng Mỹ Phẩm LUNEA

Thư mục này chứa toàn bộ mã nguồn SQL khởi tạo cơ sở dữ liệu hoàn chỉnh, chuẩn hóa 100% theo mô hình nghiệp vụ thực tế của chuỗi showroom mỹ phẩm LUNEA.

---

## 1. Cấu trúc các tập tin SQL trong thư mục `database/`

| Tên tập tin | Hệ quản trị CSDL | Mô tả nội dung |
|---|---|---|
| **`01_schema_mssql.sql`** | Microsoft SQL Server | Tập lệnh **DDL** tạo cấu trúc toàn bộ 40 bảng, các ràng buộc Khóa chính, Khóa ngoại `ON DELETE CASCADE`, `UNIQUE`, chỉ mục `INDEX` tối ưu truy vấn. |
| **`02_data_mssql.sql`** | Microsoft SQL Server | Tập lệnh **DML** nạp dữ liệu mẫu phong phú: 30 thương hiệu, 20 danh mục (mỗi danh mục >= 15 sản phẩm = tổng 240 sản phẩm), SKUs, tồn kho 3 chi nhánh, vouchers, tài khoản demo. |
| **`03_lunea_full_mssql.sql`** | Microsoft SQL Server | **Script 1-Click Toàn Diện**: Kết hợp tự động Tạo Database `lunea` + DDL + DML. Thầy chỉ cần mở file này trong SSMS và bấm **Execute (F5)** là xong ngay! |
| **`04_schema_postgresql.sql`** | PostgreSQL / Docker | Tập lệnh **DDL** chuẩn cú pháp PostgreSQL (dùng cho Docker Compose & Render Cloud). |
| **`05_data_postgresql.sql`** | PostgreSQL / Docker | Tập lệnh **DML** nạp dữ liệu chuẩn PostgreSQL và tự động cập nhật Sequences (`setval`). |
| **`06_lunea_full_postgresql.sql`** | PostgreSQL / Docker | **Script 1-Click Toàn Diện** cho PostgreSQL (dùng trong pgAdmin, DBeaver hoặc terminal `psql`). |

---

## 2. Thống kê dữ liệu mẫu (Auto Seeding Guarantee)

Hệ thống cam kết đáp ứng trọn vẹn yêu cầu: **Mỗi danh mục đều có ít nhất 15 sản phẩm** với thông tin mỹ phẩm chính xác, hình ảnh chất lượng cao và tồn kho đa chi nhánh:

1. **Tổng số danh mục:** 20 danh mục (4 danh mục cha & 16 danh mục lá chuyên sâu):
   - **Chăm sóc da (120 sản phẩm):**
     * Tinh chất & Serum: 15 sản phẩm
     * Kem dưỡng ẩm: 15 sản phẩm
     * Sữa rửa mặt: 15 sản phẩm
     * Kem chống nắng: 15 sản phẩm
     * Nước hoa hồng & Toner: 15 sản phẩm
     * Tẩy trang: 15 sản phẩm
     * Tẩy tế bào chết: 15 sản phẩm
     * Mặt nạ dưỡng da: 15 sản phẩm
   - **Trang điểm (60 sản phẩm):**
     * Son môi: 15 sản phẩm
     * Kem nền & Cushion: 15 sản phẩm
     * Phấn phủ & Má hồng: 15 sản phẩm
     * Trang điểm mắt & Mascara: 15 sản phẩm
   - **Chăm sóc tóc & Cơ thể (30 sản phẩm):**
     * Tẩy tế bào chết cơ thể: 15 sản phẩm
     * Chăm sóc tóc & Dầu gội: 15 sản phẩm
   - **Nước hoa (30 sản phẩm):**
     * Nước hoa cao cấp: 15 sản phẩm
     * Xịt thơm toàn thân Body Mist: 15 sản phẩm
2. **Tổng số sản phẩm:** **240 sản phẩm** đầy đủ thương hiệu chính hãng (Cocoon, La Roche-Posay, Innisfree, The Ordinary, Paula's Choice, MAC, Dior, Chanel, YSL, v.v.).
3. **Tổng số biến thể SKU:** **272 SKUs** với dung tích, màu sắc, giá niêm yết và mã vạch Barcode chuẩn.
4. **Tồn kho đa chi nhánh:** **816 bản ghi tồn kho** phân bổ tại 3 Showroom:
   - Chi nhánh 1: Flagship Quận 1 (TP. Hồ Chí Minh)
   - Chi nhánh 2: Showroom Tràng Tiền (Hà Nội)
   - Chi nhánh 3: Showroom Bạch Đằng (Đà Nẵng)
5. **Mã giảm giá (Vouchers):** 5 voucher phổ biến (`LUNEA10`, `FREESHIP`, `CHAOBANMOI`, `VIPCARE`, `LUNEASUMMER`).

---

## 3. Danh sách tài khoản thử nghiệm hệ thống

Tất cả tài khoản mẫu đều dùng mật khẩu mặc định: **`123456`**

| Tên đăng nhập (Username) | Mật khẩu | Vai trò (Role) | Phạm vi phân quyền |
|---|---|---|---|
| **`admin`** | `123456` | Quản trị viên tối cao (ADMIN) | Toàn quyền kiểm soát hệ thống, phân quyền nhân sự, báo cáo doanh thu, sản phẩm, chi nhánh |
| **`order_hcm`** | `123456` | Nhân viên đơn hàng (ORDER_STAFF) | Tiếp nhận, xác nhận, đóng gói và xử lý đơn hàng tại chi nhánh TP. Hồ Chí Minh |
| **`order_hn`** | `123456` | Nhân viên đơn hàng (ORDER_STAFF) | Tiếp nhận, xác nhận và xử lý đơn hàng tại chi nhánh Hà Nội |
| **`warehouse_hcm`** | `123456` | Thủ kho chi nhánh (WAREHOUSE_STAFF) | Quản lý nhập hàng, xuất kho, điều chuyển kho giữa các chi nhánh và kiểm kê tồn kho |
| **`khach01@lunea.test`** | `123456` | Khách hàng thân thiết (CUSTOMER) | Đăng nhập mua hàng, áp dụng voucher, theo dõi đơn hàng, quản lý hồ sơ da |
| **`khach02@lunea.test`** | `123456` | Khách hàng mới (CUSTOMER) | Trải nghiệm mua sắm và đánh giá sản phẩm |

---

## 4. Hướng dẫn chạy Script trên Microsoft SQL Server

### Cách 1: Sử dụng SQL Server Management Studio (SSMS) - Khuyên Dùng
1. Mở **SQL Server Management Studio (SSMS)** và kết nối tới SQL Server instance của bạn (ví dụ: `localhost` hoặc `.` hoặc `SQLEXPRESS`).
2. Vào menu **File** ➔ **Open** ➔ **File...** (hoặc bấm tổ hợp phím `Ctrl + O`).
3. Chọn tập tin: **`database/03_lunea_full_mssql.sql`**.
4. Nhấn phím **F5** (hoặc nút **Execute** trên thanh công cụ).
5. Thông báo hiển thị: `>>> [LUNEA DML] Đã nạp thành công toàn bộ dữ liệu mẫu vào Microsoft SQL Server!` là hoàn tất 100%!

### Cách 2: Sử dụng dòng lệnh `sqlcmd` (Terminal / PowerShell)
```powershell
sqlcmd -S localhost -U sa -P 123 -i "c:\Users\Dev\OneDrive\Desktop\lunea\CNPM\database\03_lunea_full_mssql.sql"
```

---

## 5. Hướng dẫn chạy Script trên PostgreSQL / Docker Compose

### Cách 1: Chạy tự động với Docker Compose (Có sẵn trong dự án)
Nếu máy bạn có cài đặt Docker:
```bash
docker compose up -d
docker exec -i lunea psql -U lunea -d lunea < database/06_lunea_full_postgresql.sql
```

### Cách 2: Sử dụng pgAdmin hoặc DBeaver
1. Mở **pgAdmin** hoặc **DBeaver** và kết nối tới database `lunea`.
2. Mở trình soạn thảo SQL (**SQL Editor**).
3. Mở file **`06_lunea_full_postgresql.sql`** và bấm **Execute Script**.

---

## 6. Cấu hình kết nối Spring Boot (`application.properties`)

Khi chạy mã nguồn Spring Boot, file `src/main/resources/application.properties` đã cấu hình sẵn sàng nhận diện CSDL:
```properties
# Microsoft SQL Server (Mặc định khi chạy Local)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=lunea;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true;
spring.datasource.username=sa
spring.datasource.password=123
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=none
```
*(Nếu thầy đã chạy script SQL trước đó, có thể đổi `ddl-auto=none` hoặc giữ nguyên `ddl-auto=update` để Spring Boot tự động kết nối và tái sử dụng toàn bộ dữ liệu).*
