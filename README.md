# LUNEA – Website Bán Mỹ Phẩm Theo Mô Hình Chuỗi Cửa Hàng

Muốn chuyển từ demo sang dữ liệu và dịch vụ thật: điền [biểu mẫu thông tin triển khai](docs/THONG_TIN_CAN_DIEN.md) và tệp cấu hình cục bộ `.local/production.env` được liên kết trong biểu mẫu. Phase 5–32 vẫn nằm trong roadmap, chưa có chức năng hoàn chỉnh.

## Chạy giao diện local với Supabase

Schema `lunea` đã được khởi tạo trên Supabase theo [runbook Supabase](docs/database/SUPABASE.md). Để dùng database thật thay vì H2 demo, tại thư mục `CNPM` chạy:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-supabase-local.ps1 -Port 8081
```

Mở **http://localhost:8081**. Script chỉ lắng nghe trên máy này, đọc `.local/production.env`, đặt URL recovery/cookie phù hợp HTTP localhost và không bật profile `demo`. Có thể thử đăng ký/đăng nhập khách hàng và dùng tài khoản ADMIN thật đã cấp để kiểm tra vai trò, nhân viên, chi nhánh; tài khoản demo không tồn tại trên Supabase. Nếu chưa có JAR hoặc vừa sửa Java, chạy `.\mvnw.cmd package` trước. Email recovery cần thử bằng hộp thư thật do bạn sở hữu; chưa có hosting/HTTPS công khai.

## Chạy giao diện test phase 1–4

Đã có Spring Boot 4.1.1/Java 21, giao diện HTML/CSS/JavaScript cùng ứng dụng, đăng nhập khách hàng/nhân viên bằng email hoặc điện thoại, đăng ký, đăng xuất, khôi phục mật khẩu, quản lý nhân viên/vai trò/quyền/chi nhánh và audit writer. Kiến trúc REST → service → repository được giữ.

Tại thư mục `CNPM`, chạy:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1
```

Mở **http://localhost:8080**. Script ưu tiên Java 21 đã cài ở máy này; máy khác đặt `JAVA_HOME` tới JDK 21. Có thể dùng `-Port 8081` nếu cổng 8080 đang bận. Không cần Node/npm hoặc PostgreSQL cho demo. H2 chỉ là dữ liệu demo trong bộ nhớ; khởi động lại sẽ phục hồi fixtures.

| Tài khoản demo | Mật khẩu | Mục đích |
|---|---|---|
| `admin@lunea.test` | `DemoAdmin123` | Quản lý nhân viên, vai trò, quyền và chi nhánh |
| `kho@lunea.test` | `KhoDemo123` | Quyền kho tại chi nhánh 1; chi nhánh 2 trả 403 |
| `customer@lunea.test` | `DemoUser123` | Đăng nhập khách hàng, xem phiên và thử reset mật khẩu |

Các mật khẩu này chỉ được seed ở profile **demo**, ràng buộc loopback; cấu hình mặc định không seed tài khoản. Giao diện có nút điền nhanh tài khoản khi nhận diện demo.

Thử theo thứ tự:

1. Đăng ký email mới với mật khẩu ít nhất 8 ký tự gồm chữ/số, xác nhận mật khẩu và chấp nhận điều khoản; đăng nhập rồi đăng xuất.
2. Chọn **Quên mật khẩu**, nhập email demo, mở link trong **Hộp thư demo** và đặt mật khẩu mới. Link dùng một lần, hết hạn sau 15 phút; các phiên cũ mất hiệu lực.
3. Đăng nhập quản trị, thêm/sửa/ngừng hoạt động nhân viên, chọn vai trò và một chi nhánh chính trong các chi nhánh được cấp. Không dùng mật khẩu chung khi tạo nhân viên.
4. Trong **Vai trò & quyền**, bỏ/cấp quyền cho vai trò QLTK; nhân viên nhận thay đổi ngay trong phiên hiện tại khi thực hiện yêu cầu mới. ADMIN là vai trò dành riêng và không thể gỡ quyền.
5. Đăng nhập nhân viên kho, dùng form kiểm tra chi nhánh: mã 1 được phép, mã 2 bị từ chối. Khách hàng không được truy cập API quản trị.

### Build và tests

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-21.0.10'
.\mvnw.cmd test
.\mvnw.cmd package
```

Tests mặc định dùng H2 riêng, không kết nối DB dev. PostgreSQL 16 test có profile `test-postgres`, yêu cầu DB riêng có tên kết thúc `_test`; xem [runbook DB](docs/database/README.md). Report/evidence ở [báo cáo phase 1–4](docs/PHASE_01_04_REPORT.md).

Java 21 là baseline được hỗ trợ; đặt `JAVA_HOME` trước cả build và package, không dùng JDK mặc định khác để suy ra lỗi source. URL PostgreSQL test phải ghi rõ host và tên DB `_test`; query parameter không được thay database/service. Supabase dùng database `postgres`, schema `lunea`, chỉ chạy ứng dụng với `validate` và inventory read-only; không chạy profile test tạo/drop schema trên đó. Inventory DB cũ mặc định dùng `public`; inventory Supabase dùng `-v inventory_schema=lunea` theo runbook.

### PostgreSQL và email thực

Cấu hình mặc định dùng PostgreSQL và `ddl-auto=validate`. Đặt `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; kiểm kê schema và áp migration thủ công theo [runbook](docs/database/README.md) trước khởi động. Không tự reset DB hay chạy Hibernate update trên dữ liệu cũ. Credential plaintext cũ không đăng nhập được; dùng recovery đã xác minh để đặt hash mới.

Recovery ngoài demo dùng SMTP: đặt `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `MAIL_FROM`, `APP_BASE_URL`; TLS/auth mặc định bật. Khi dùng HTTPS đặt `COOKIE_SECURE=true`. SMTP không được cấu hình thì yêu cầu vẫn trả thông báo chung, không tạo link có hiệu lực. Thư demo chỉ tồn tại trong bộ nhớ và chỉ đọc được từ loopback.

Session/CSRF bảo vệ mọi mutation. Mật khẩu BCrypt, token reset chỉ lưu hash trong DB. Lỗi JSON có `status`, `code`, `message`, `path`, `fieldErrors`; lỗi nội bộ không trả stack trace/secret. Các luồng riêng tư cart/order/return/review/customer thuộc phase sau còn khóa; xem [ma trận quyền và route](docs/STAFF_ACCESS.md). Supabase đã có schema và ADMIN thật để kiểm tra Phase 1–4; còn cần thử email đến hộp thư, khai báo hosting/HTTPS và thay chi nhánh ảo trước khi công khai. Dữ liệu database cũ và các phase 5–32 là công việc riêng.

## Tài liệu phân tích thiết kế tham khảo

Nội dung bên dưới mô tả mục tiêu tổng thể của đồ án; trạng thái cài đặt hiện tại nằm ở phần chạy/test và các completion report phía trên.

Đồ án môn học **Công nghệ Phần mềm** – Tài liệu phân tích & thiết kế hệ thống cho website thương mại điện tử ngành mỹ phẩm, vận hành theo mô hình **chuỗi cửa hàng** (quản lý sản phẩm tập trung, tồn kho theo từng chi nhánh).

---

## 1. Thông tin đồ án

| | |
|---|---|
| **Trường** | Đại học Công nghệ Kỹ thuật TP. Hồ Chí Minh |
| **Khoa** | Công nghệ Thông tin |
| **Môn học** | Công nghệ Phần mềm |
| **Nhóm thực hiện** | Nhóm 01 |
| **Giảng viên hướng dẫn** | ThS. Nguyễn Trần Thi Văn |
| **Thời gian** | Tháng 9/2026 |

**Thành viên nhóm**

| Họ và tên | MSSV |
|---|---|
| Lý Đông Thịnh | 24110337 |
| Hà Nguyễn Nhật Nguyên | 24110288 |

**Tài liệu gốc:** `Nhom01_ThietKeGiaoDien.docx`

---

## 2. Giới thiệu

LUNEA là nền tảng thương mại điện tử kết nối nhiều cửa hàng mỹ phẩm vật lý. Sản phẩm và chính sách bán hàng được **quản lý tập trung**, trong khi **tồn kho, nhân viên xử lý và đơn hàng được theo dõi theo từng chi nhánh**. Hệ thống hỗ trợ song song bán lẻ trực tuyến (đa kênh) và nghiệp vụ bán sỉ cho đối tác.

Repo này tổng hợp toàn bộ tài liệu phân tích – thiết kế của đồ án, gồm 5 chương chính:

1. **Khảo sát hiện trạng** – tổng quan thị trường, tổ chức chuỗi LUNEA, nghiệp vụ hiện tại và khảo sát đối thủ (Sephora, Charlotte Tilbury, Fenty Beauty).
2. **Lập danh sách yêu cầu** – yêu cầu chức năng nghiệp vụ, chức năng hệ thống và yêu cầu phi chức năng.
3. **Xác định Actor và Use Case** – nhận diện tác nhân, sơ đồ use-case và đặc tả chi tiết từng chức năng.
4. **Thiết kế dữ liệu** – lược đồ logic và chi tiết 37 bảng dữ liệu.
5. **Thiết kế giao diện** – danh sách màn hình, sơ đồ luân chuyển và mô tả chi tiết từng màn hình.

> **Trạng thái:** Repo có mã nguồn backend và giao diện test phase 1–4. Danh sách chức năng trong phần thiết kế này còn gồm các phase chưa cài đặt; không coi toàn bộ danh sách là tính năng đã hoàn thành.

---

## 3. Tác nhân (Actors) trong hệ thống

| Mã số | Tác nhân | Vai trò |
|---|---|---|
| KH | Khách hàng | Tra cứu mỹ phẩm, quản lý hồ sơ, giỏ hàng, đặt hàng, theo dõi đơn, yêu cầu trả hàng, đánh giá sản phẩm |
| QLSP | Nhân viên quản lý sản phẩm | Quản lý danh mục, SKU/biến thể, thuộc tính mỹ phẩm, hình ảnh, kiểm duyệt đánh giá |
| QLCH | Quản trị viên / Quản lý chuỗi | Quản lý thông tin cửa hàng/chi nhánh: trạng thái, địa chỉ, giờ hoạt động |
| QLNH | Nhân viên nhập hàng | Quản lý nhà cung cấp, phiếu nhập, xác nhận nhập hàng |
| QLTK | Nhân viên kho | Quản lý tồn kho theo SKU – cửa hàng: kiểm kê, điều chỉnh, điều chuyển, giữ hàng |
| QLKH | Nhân viên chăm sóc khách hàng | Tra cứu hồ sơ/lịch sử mua, quản lý tài khoản, xử lý yêu cầu trả hàng |
| QLDH | Nhân viên xử lý đơn hàng | Xác nhận đơn, phân công chi nhánh, cập nhật vòng đời đơn, xử lý trả hàng |
| QLKM | Nhân viên Marketing | Quản lý khuyến mãi, voucher, theo dõi hiệu quả chương trình |
| QLNV | Quản trị viên | Quản lý nhân viên, phân chi nhánh, vai trò, quyền, nhật ký thao tác |
| BCTK | Quản lý / Quản trị viên | Khai thác dashboard và các báo cáo (doanh thu, đơn hàng, tồn kho, khuyến mãi...) |

Ngoài ra, hệ thống còn tương tác với các **actor bên ngoài**: cổng thanh toán, đơn vị vận chuyển, Google Maps API, Cloudinary (lưu trữ ảnh) và dịch vụ Email/OTP.

---

## 4. Các nhóm chức năng chính

- **Mua sắm & tài khoản khách hàng:** tìm kiếm/lọc sản phẩm, xem chi tiết & tồn kho theo cửa hàng, Beauty Profile & gợi ý sản phẩm, yêu thích, giỏ hàng, đặt hàng, theo dõi & hủy đơn, yêu cầu trả hàng, đánh giá sản phẩm.
- **Vận hành chuỗi:** quản lý sản phẩm/SKU, quản lý cửa hàng, nhập hàng từ nhà cung cấp, quản lý tồn kho & điều chuyển giữa các chi nhánh.
- **Xử lý đơn hàng:** vòng đời đơn *Chờ xác nhận → Đã xác nhận → Đang chuẩn bị → Đang giao/Chờ nhận tại cửa hàng → Hoàn thành* (kèm Đã hủy, Yêu cầu hoàn/trả).
- **Marketing:** khuyến mãi theo sản phẩm/danh mục/thương hiệu, voucher (%, số tiền cố định), ưu đãi vận chuyển, chương trình thành viên.
- **Quản trị hệ thống:** quản lý nhân viên, phân quyền theo vai trò **và** theo chi nhánh, nhật ký thao tác.
- **Báo cáo & thống kê:** doanh thu, đơn hàng, sản phẩm bán chạy, tồn kho/tồn thấp, nhập hàng, hiệu quả khuyến mãi, khách hàng.

---

## 5. Thiết kế dữ liệu

Lược đồ logic gồm **37 bảng**, chia thành 2 nhóm chính (thương mại điện tử – khách hàng; vận hành chuỗi – quản trị). Kiểu dữ liệu mang tính định hướng, có thể ánh xạ sang **MySQL / PostgreSQL / SQL Server** khi triển khai.

| Nhóm | Bảng dữ liệu |
|---|---|
| Tài khoản & người dùng | TaiKhoan, KhachHang, DiaChiKhachHang, BeautyProfile, NhanVien, VaiTro, Quyen, NhanVienVaiTro, VaiTroQuyen, NhanVienCuaHang, NhatKyThaoTac |
| Sản phẩm | ThuongHieu, DanhMuc, SanPham, SKU, ThuocTinh, GiaTriThuocTinhSKU, HinhAnhSanPham |
| Cửa hàng & tồn kho | CuaHang, TonKho, DieuChinhTon, DieuChuyenKho, ChiTietDieuChuyen |
| Giỏ hàng & yêu thích | GioHang, ChiTietGioHang, YeuThich, ChiTietYeuThich |
| Khuyến mãi | Voucher |
| Đơn hàng | DonHang, ChiTietDonHang, GiuTonDonHang, YeuCauTraHang, ChiTietTraHang, DanhGia |
| Nhập hàng | NhaCungCap, PhieuNhap, ChiTietPhieuNhap |

**Quy tắc quan trọng:** một sản phẩm có nhiều SKU · tồn kho quản lý theo cặp SKU–cửa hàng · hàng chỉ được giữ (giữ tồn) sau khi đơn tạo thành công · dữ liệu đã phát sinh giao dịch không xóa vật lý mà chuyển trạng thái · chi tiết đơn hàng lưu snapshot giá tại thời điểm đặt.

---

## 6. Thiết kế giao diện – Danh sách màn hình

| STT | Màn hình | Mục đích |
|---|---|---|
| 1 | Trang chủ | Giới thiệu LUNEA, danh mục và sản phẩm nổi bật |
| 2 | Danh sách sản phẩm | Hiển thị và lọc danh sách sản phẩm |
| 3 | Chi tiết sản phẩm | Hiển thị thông tin chi tiết một sản phẩm |
| 4 | Đăng nhập | Xác thực khách hàng |
| 5 | Đăng ký | Tạo tài khoản mới |
| 6 | Giỏ hàng | Hiển thị & quản lý sản phẩm đã chọn mua |
| 7 | Giao hàng & thanh toán | Nhập thông tin nhận hàng, chọn phương thức thanh toán |
| 8 | Đặt hàng thành công | Thông báo kết quả và thông tin đơn hàng |

**Luồng chính:** Trang chủ → Danh sách sản phẩm → Chi tiết sản phẩm → Giỏ hàng → Giao hàng & thanh toán → Đặt hàng thành công.

---

## 7. Yêu cầu phi chức năng nổi bật

- **Tương thích:** Responsive (desktop/tablet/mobile), hoạt động tốt trên Chrome, Edge, Firefox, Safari.
- **Hiệu quả:** thời gian phản hồi tìm kiếm/tải trang mục tiêu ≤ ~3 giây.
- **Bảo mật:** mật khẩu không lưu dạng văn bản rõ, kết nối HTTPS khi triển khai thực tế, kiểm soát quyền truy cập theo vai trò & chi nhánh.
- **Đúng đắn & toàn vẹn:** không để tồn khả dụng âm, ràng buộc dữ liệu nhất quán giữa khách hàng – đơn hàng – SKU – cửa hàng – tồn kho.
- **Tin cậy:** hỗ trợ sao lưu & phục hồi dữ liệu, không mất dữ liệu lịch sử.
- **Khả năng tiến hóa:** dễ dàng thêm chi nhánh, SKU mới, phương thức thanh toán, loại khuyến mãi mà không đổi kiến trúc lõi.

---

## 8. Tích hợp hệ thống ngoài

| Dịch vụ | Vai trò |
|---|---|
| Cổng thanh toán | Xử lý thanh toán (phiên bản hiện tại: COD) |
| Đơn vị vận chuyển | Giao hàng tận nơi |
| Google Maps API | Hiển thị vị trí cửa hàng |
| Cloudinary | Lưu trữ & xử lý hình ảnh sản phẩm |
| Email / OTP | Xác thực tài khoản, thông báo |

---

## 9. Định hướng tiếp theo

- [ ] Lựa chọn & thống nhất công nghệ triển khai (frontend, backend, hệ quản trị CSDL)
- [ ] Cài đặt cơ sở dữ liệu vật lý từ lược đồ logic (mục 5)
- [ ] Xây dựng các module theo đặc tả use case (mục 4)
- [ ] Hiện thực hóa 8 màn hình giao diện theo thiết kế (mục 6)
- [ ] Tích hợp thanh toán trực tuyến ngoài COD

---

## 10. License

Tài liệu phục vụ mục đích học thuật trong khuôn khổ môn học Công nghệ Phần mềm.
