# 08 — BẢNG THEO DÕI TIẾN ĐỘ HỌC TẬP CHÍNH XÁC (LEARNING PROGRESS)

> **Cập nhật lần cuối:** 27/10/2026 — Phiên học số 01  
> **Phương pháp đo lường:** Định lượng chính xác theo số lượng file thực tế trong repository.  

---

## 1. THỐNG KÊ TIẾN ĐỘ TỔNG QUAN

| Chỉ số đo lường | Công thức tính | Số lượng thực tế | Tỷ lệ hoàn thành |
|---|---|:---:|:---:|
| **Tổng số file trong phạm vi học** | Toàn bộ mã nguồn do dự án quản lý | **351 files** | **100%** |
| **Tổng số file đã khảo sát (B)** | Đã quét vai trò, cấu trúc & phụ thuộc | **351 files** | **100.0%** |
| **Tổng số file đã giảng dạy (C)** | Đã được giải thích chi tiết từng phần | **1 file** | **0.28%** |
| **Tổng số file đã kiểm tra đạt (D)** | Học viên tự trả lời vấn đáp đạt | **0 files** | **0.0%** *(Đang chờ trả lời Câu hỏi 1)* |
| **Số file còn lại cần học** | Phạm vi trừ đi các file đã hoàn thành | **350 files** | **99.72%** |

---

## 2. TIẾN ĐỘ CHI TIẾT THEO TỪNG GIAI ĐOẠN

```
[Giai đoạn 1: Bootstrap & Config] ──────► [1/5 file]   (Đang học file đầu tiên)
[Giai đoạn 2: Security & Multi-Store] ──► [0/15 files] (Chưa học)
[Giai đoạn 3: Exception & Enums] ──────► [0/21 files] (Chưa học)
[Giai đoạn 4: Database & Entities] ────► [0/43 files] (Chưa học)
[Giai đoạn 5: Repositories] ───────────► [0/35 files] (Chưa học)
[Giai đoạn 6: DTOs & Mappers] ─────────► [0/66 files] (Chưa học)
[Giai đoạn 7: Services (Logic lõi)] ───► [0/42 files] (Chưa học)
[Giai đoạn 8: REST Controllers] ───────► [0/17 files] (Chưa học)
[Giai đoạn 9: Web MVC & Frontend] ─────► [0/46 files] (Chưa học)
[Giai đoạn 10: Tests & Deployment] ────► [0/26 files] (Chưa học)
```

---

## 3. LỘ TRÌNH 2 NGÀY CẤP TỐC (8 PHIÊN HỌC CHUYÊN SÂU)

### NGÀY 1: LÀM CHỦ KIẾN TRÚC, BẢO MẬT, THỰC THỂ CSDL VÀ LOGIC NGHIỆP VỤ LÕI
- **Phiên 1 (2 giờ) — ĐANG THỰC HIỆN:**
  - Khảo sát toàn bộ repository (Hoàn tất 351 files).
  - Học Entry Point: `CosmeticStoreChainManagementApplication.java`.
  - Cấu hình Build & Môi trường: `pom.xml`, `application.properties`.
  - *Kiểm tra:* Cơ chế `@SpringBootApplication`, `@EnableAsync` và cấu hình HikariCP.
- **Phiên 2 (2 giờ):**
  - Cấu hình Bảo mật: `SecurityConfig.java`, `CookieBearerTokenResolver.java`, `JwtService.java`.
  - Cơ chế đa chi nhánh: `StoreScope.java`, `StoreScopeInterceptor.java`.
  - *Kiểm tra:* Cách cô lập dữ liệu chi nhánh và bảo mật Stateless Cookie JWT.
- **Phiên 3 (2 giờ):**
  - Hệ thống Xử lý ngoại lệ (`GlobalExceptionHandler.java`, `ErrorCode.java`).
  - 43 Thực thể CSDL JPA (`domain/entity/*`): Phân hệ Account, Catalog, Store, Inventory, Sales/Order.
  - *Kiểm tra:* Bất biến tồn kho và snapshot giá trong Entity.
- **Phiên 4 (2 giờ):**
  - Tầng Repository & Tầng DTO/Validation.
  - Tầng Service lõi #1: `OrderServiceImpl.java` (Luồng đặt hàng, giữ tồn, chọn kho).
  - *Kiểm tra:* Phân tích từng dòng code của hàm `createOrder()`.

### NGÀY 2: HOÀN THIỆN TOÀN BỘ SERVICES, CONTROLLERS, GIAO DIỆN VÀ VẤN ĐÁP
- **Phiên 5 (2 giờ):**
  - Tầng Service lõi #2: `ProductServiceImpl.java` (Lọc tiếng Việt không dấu), `OtpServiceImpl.java`, `InventoryServiceImpl.java`, `StockTransferServiceImpl.java`.
- **Phiên 6 (2 giờ):**
  - 17 REST API Controllers (`rest/*`) & Xử lý chuẩn JSON ApiResponse RFC-7807.
  - Giao tiếp Web MVC (`controller/*`): Kết nối Controller với Model và View.
- **Phiên 7 (2 giờ):**
  - Frontend Templates (Thymeleaf HTML, `api.js`, CSS tokens).
  - Khảo sát các test cases trong `src/test/*` và cấu hình `Dockerfile`.
- **Phiên 8 (2 giờ):**
  - Tổng duyệt vấn đáp mô phỏng (Mock Viva) với 15 câu hỏi bẫy từ hội đồng.
  - Đóng gói toàn bộ kiến thức, sẵn sàng bước vào phòng thi.

---

## 4. NHẬT KÝ KIỂM TRA VÀ GHI NHẬN LỖI HIỂU SAI

| Lượt | File kiểm tra | Câu hỏi đã đặt | Điểm (Thang 10) | Phân loại lỗi | Hướng dẫn sửa đổi |
|:---:|---|---|:---:|---|---|
| **#1** | `CosmeticStoreChainManagementApplication.java` | Phân tích 3 annotation trong `@SpringBootApplication` và hậu quả khi chuyển vị trí class? | *Đang chờ* | *Chưa có* | *Chờ học viên phản hồi trong lượt này* |
