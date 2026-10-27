# 04 — CẤU TRÚC DATABASE VÀ LUỒNG DỮ LIỆU TOÀN HỆ THỐNG

> **Hệ quản trị CSDL:** SQL Server (Local) / PostgreSQL (Cloud Production)  
> **Tổng số bảng:** 37 bảng dữ liệu quan hệ  
> **Cơ chế ORM:** Spring Data JPA + Hibernate ORM 6  

---

## 1. PHÂN BỐ 37 BẢNG DỮ LIỆU THEO 7 PHÂN HỆ

```
+--------------------------------------------------------------------------------------------------------+
| 1. TÀI KHOẢN & PHÂN QUYỀN (11 bảng):                                                                   |
|    TaiKhoan, KhachHang, DiaChiKhachHang, BeautyProfile, NhanVien, VaiTro, Quyen,                       |
|    NhanVienVaiTro, VaiTroQuyen, NhanVienCuaHang, NhatKyThaoTac                                         |
+--------------------------------------------------------------------------------------------------------+
| 2. DANH MỤC & SẢN PHẨM (7 bảng):                                                                       |
|    ThuongHieu, DanhMuc, SanPham, SKU, ThuocTinh, GiaTriThuocTinhSKU, HinhAnhSanPham                   |
+--------------------------------------------------------------------------------------------------------+
| 3. CỬA HÀNG & TỒN KHO CHUỖI (5 bảng):                                                                  |
|    CuaHang, TonKho, DieuChinhTon, DieuChuyenKho, ChiTietDieuChuyen                                      |
+--------------------------------------------------------------------------------------------------------+
| 4. GIỎ HÀNG & YÊU THÍCH (4 bảng):                                                                      |
|    GioHang, ChiTietGioHang, YeuThich, ChiTietYeuThich                                                  |
+--------------------------------------------------------------------------------------------------------+
| 5. KHUYẾN MÃI & VOUCHER (1 bảng):                                                                      |
|    Voucher                                                                                             |
+--------------------------------------------------------------------------------------------------------+
| 6. ĐƠN HÀNG & BÁN HÀNG (6 bảng):                                                                       |
|    DonHang, ChiTietDonHang, GiuTonDonHang, YeuCauTraHang, ChiTietTraHang, DanhGia                      |
+--------------------------------------------------------------------------------------------------------+
| 7. NHẬP HÀNG TỪ NHÀ CUNG CẤP (3 bảng):                                                                 |
|    NhaCungCap, PhieuNhap, ChiTietPhieuNhap                                                             |
+--------------------------------------------------------------------------------------------------------+
```

---

## 2. CÁC RÀNG BUỘC VÀ BẤT BIẾN DỮ LIỆU CỐT LÕI (DATA INVARIANTS)

1. **Bảng `TonKho` (`InventoryEntity`):**
   - **Khóa duy nhất (Composite Unique Constraint):** `UNIQUE(cua_hang_id, sku_id)`. Mỗi chi nhánh chỉ có duy nhất 1 dòng ghi tồn kho cho một SKU cụ thể.
   - **Cột:** `actual_stock` (tồn vật lý trong showroom), `held_quantity` (số lượng đang tạm giữ cho các đơn chưa giao).
   - **Ràng buộc kiểm tra:** `CHECK (actual_stock >= 0 AND held_quantity >= 0 AND held_quantity <= actual_stock)`.
   - **Tồn khả dụng:** Luôn được tính động bằng: `actual_stock - held_quantity`.

2. **Bảng `GiuTonDonHang` (`OrderReservationEntity`):**
   - Trạng thái: `HELD` (đang giữ hàng), `COMMITTED` (đã trừ kho thành công khi giao hàng), `RELEASED` (đã giải phóng trả lại tồn do hủy đơn).

3. **Bảng `ChiTietDonHang` (`OrderItemEntity`):**
   - **Các cột snapshot:** `snapshot_product_name`, `snapshot_variant_name`, `snapshot_price`.
   - **Mục đích:** Đảm bảo toàn vẹn dữ liệu kế toán và lịch sử giao dịch. Nếu sau này sản phẩm tăng giá từ 350.000đ lên 400.000đ hoặc đổi tên thì hóa đơn trong quá khứ không bao giờ bị biến động doanh thu.

---

## 3. SƠ ĐỒ LUỒNG DỮ LIỆU ĐẶT HÀNG (CHECKOUT DATA FLOW)

```
[Khách hàng bấm 'Đặt hàng']
            │
            ▼
1. Validate payload @Valid CreateOrderRequest (Họ tên, SĐT, Địa chỉ, Danh sách SKU)
            │
            ▼
2. OrderServiceImpl.createOrder():
   ├─► Sắp xếp danh sách SKU ID tăng dần: Collections.sort(skuIds) (Chống Deadlock CSDL)
   │
   ├─► Quét bảng TonKho: Kiểm tra Tồn khả dụng (actual_stock - held_quantity >= quantity)
   │     └── Nếu thiếu: Ném ra InsufficientStockException("Sản phẩm X tại kho tạm hết")
   │
   ├─► Chọn Chi nhánh tối ưu (Gần nhất có đủ hàng)
   │
   ├─► Kiểm tra Voucher: VoucherServiceImpl.applyVoucher() (Đơn tối thiểu, số lượt dùng, mức giảm tối đa)
   │
   ├─► INSERT DonHang: Tạo mã đơn (LUN-YYYYMMDD-XXXX), tổng tiền, phí ship, chi nhánh phụ trách
   │
   ├─► INSERT ChiTietDonHang: Lưu snapshot_product_name, snapshot_variant_name, snapshot_price
   │
   ├─► INSERT GiuTonDonHang (Status = 'HELD'): Ghi nhận giữ N sản phẩm tại CuaHang X
   │
   ├─► UPDATE TonKho: Tăng held_quantity = held_quantity + quantity (Giữ chân tồn kho)
   │
   └─► Xóa các sản phẩm đã mua khỏi ChiTietGioHang
            │
            ▼
3. Gửi thông báo đặt hàng thành công qua Email (Bất đồng bộ @Async)
            │
            ▼
4. Trả về OrderResponse DTO (Mã đơn hàng, trạng thái 'CHỜ XÁC NHẬN')
```
