# 07 — CHECKLIST ÔN TẬP CẤP TỐC TRƯỚC GIỜ VẤN ĐÁP

> **Dành cho học viên:** Đánh dấu kiểm tra từng mục trước khi bước vào phòng thi.  

---

## BẢNG TỰ ĐÁNH GIÁ MỨC ĐỘ SẴN SÀNG

| STT | Nội dung kiến thức | Mức độ tự tin (1-5) | Đã thuộc | Ghi chú cá nhân |
|:---:|---|:---:|:---:|---|
| 1 | Trình bày luồng khởi động từ `CosmeticStoreChainManagementApplication.java` | [ ] | [ ] | Hiểu 3 annotation trong `@SpringBootApplication` |
| 2 | Phân tích cấu hình bảo mật `SecurityConfig.java` và cơ chế Stateless Cookie JWT | [ ] | [ ] | Thuộc các cờ HttpOnly, SameSite, Secure |
| 3 | Giải thích kiến trúc phân quyền chi nhánh bằng `StoreScope.java` | [ ] | [ ] | Bản chất ThreadLocal và lý do clear() |
| 4 | Trình bày công thức Tồn khả dụng: `available = actual_stock - held_quantity` | [ ] | [ ] | Bảng `TonKho` và `GiuTonDonHang` |
| 5 | Giải thích luồng Đặt hàng trong `OrderServiceImpl.java` (Sắp xếp SKU chống Deadlock) | [ ] | [ ] | Thứ tự các bước trong @Transactional |
| 6 | Trình bày lý do phải lưu Snapshot giá và tên trong `ChiTietDonHang` | [ ] | [ ] | Tính toàn vẹn lịch sử hóa đơn kế toán |
| 7 | Giải thích thuật toán tìm kiếm mỹ phẩm tiếng Việt không dấu trong `ProductServiceImpl.java` | [ ] | [ ] | Hàm stripAccents và Normalizer NFD |
| 8 | Trình bày cơ chế mã OTP bảo mật trong `OtpServiceImpl.java` | [ ] | [ ] | SecureRandom, TTL 5 phút, cooldown 30s |
| 9 | Phân biệt cơ chế xử lý lỗi tập trung `GlobalExceptionHandler.java` | [ ] | [ ] | Bắt @Valid, BusinessException, chuẩn RFC-7807 |
| 10 | Trình bày các annotation JPA quan trọng (`@Entity`, `@ManyToOne`, `@Enumerated`) | [ ] | [ ] | Sự khác nhau giữa Lazy và Eager |
