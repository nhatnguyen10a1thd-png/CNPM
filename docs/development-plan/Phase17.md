# Phase 17 — Giỏ hàng customer end-to-end

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase02](Phase02.md), [Phase06](Phase06.md), [Phase07](Phase07.md), [Phase10](Phase10.md). Complexity: MEDIUM. Risk: HIGH.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Nối màn hình giỏ hàng đúng thiết kế với cart hiện có, sửa ownership và validation cho mọi thao tác; giữ add/merge/tổng tiền đang đúng.

## 2. Why This Phase Exists

Cart không phải NOT_STARTED: service đã add/merge/update/remove/clear và response có ảnh/giá/subtotal. Lỗi nghiêm trọng là update/remove item không kiểm tra item thuộc cart/customer và API mặc định customerId=1.

## 3. Current State

- GET /api/cart, POST /items, PUT/DELETE /items/{cartItemId}, DELETE /api/cart đã có; tất cả nhận customerId mặc định1.
- getOrCreateCart có thể lưu cart với customer null nếu ID không hợp lệ; chỉ get cũng tạo cart khi chưa có.
- updateItemQuantity tìm item theo ID rồi sửa/xóa không kiểm tra cart; removeItem deleteById trực tiếp.
- CartItemRequest add đã @Min(1); update dùng @RequestParam Integer không constraint, quantity≤0 tự xóa.
- toCartResponse tính current price × quantity, tổng và primary image; price null bị coi 0; chưa có SKU/product active guard hay concurrency merge test.
- CartEntity customer unique và cart_items unique(cart_id, sku_id) đã có; giữ schema đúng này.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase02 có principal customer và account status; D01 đã chốt frontend/auth.
- [ ] Phase06/07 có SKU giá/trạng thái/ảnh và Phase10 có detail add-to-cart entry.
- [ ] Pricing cart/subtotal cơ bản không bị nhầm với shipping/voucher quote thuộc Phase18.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Sửa identity, ownership, positive quantity và SKU hợp lệ; bảo vệ concurrent add/merge.
- Màn hình §5.3.6 cart: từng dòng ảnh/tên/SKU/giá/qty/remove/line total và summary.
- Nối nút add cart từ detail, cart counter, clear/empty/loading/error; checkout CTA tới Phase19 khi có.

### OUT OF SCOPE

- Không giữ hàng khi add/update cart; hold chỉ successful checkout Phase19.
- Voucher/shipping quote Phase18; không tự dùng cart subtotal làm total checkout.
- Không tạo guest cart/merge guest hay số lượng giới hạn kinh doanh ngoài thiết kế.
- Wishlist Phase11; không viết lại module đó.

## 6. Requirements Covered

- §3.4.1 UC15, UC16, UC17, UC18; đây là Phase owner.
- KH-QĐ1/QĐ7: SKU/product được bán, quantity dương, thêm giỏ không giữ hàng; §4.2 toàn vẹn cart.
- UI §5.3.6, hình embedded ui-image9: giỏ dạng bảng, +/- qty, xóa, subtotal và bước tiếp checkout.
- §4.1.2 Cart/CartItem: customer duy nhất, một SKU trong một cart; updatedAt thiết kế hiện thiếu.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [CartServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/CartServiceImpl.java)
- [CartService.java](../../src/main/java/com/thinh/cosmetic/service/cart/CartService.java)
- [CartRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/CartRestController.java)
- [CartRepository.java](../../src/main/java/com/thinh/cosmetic/repository/cart/CartRepository.java)
- [CartItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/cart/CartItemRepository.java)
- [CartItemRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/cart/CartItemRequest.java)
- [CartResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/cart/CartResponse.java)
- [CartItemResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/cart/CartItemResponse.java)
- [CartEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/CartEntity.java)
- [CartItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/CartItemEntity.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [CartServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/CartServiceImpl.java)
- [CartRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/CartRestController.java)
- [CartItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/cart/CartItemRepository.java)
- [CartRepository.java](../../src/main/java/com/thinh/cosmetic/repository/cart/CartRepository.java)
- [CartEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/CartEntity.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- CartService unit/HTTP security tests và PostgreSQL concurrent add integration test.
- Cart UI/logic theo D01; request DTO update quantity chỉ tạo nếu @RequestParam validation không đáp ứng contract thống nhất.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

Giữ unique customer và unique(cart_id, sku_id); không chuyển về composite PK. Đối chiếu Cart.updatedAt theo §4.1.2/SYS10, bổ sung timestamp bằng migration nếu Phase01 chưa xử lý. TECHNICAL RECOMMENDATION: CHECK quantity > 0 sau kiểm kê dữ liệu âm/0 để chặn bypass validation; lock/version chỉ theo convention đã chốt, không thêm schema tự phát.

## 11. Backend Tasks

- [ ] Lấy customer từ principal, loại default customerId=1 ở toàn bộ route; invalid account không tạo cart null.
- [ ] Scope item lookup bằng cart/customer, áp dụng cả update/remove; không dùng deleteById không điều kiện.
- [ ] Update quantity phải dương theo thiết kế; xóa dùng DELETE rõ ràng, không âm thầm xóa khi gửi quantity0.
- [ ] Giữ add/merge và subtotal đang đúng; kiểm tra SKU/product active và giá hợp lệ, không đổi null price thành sản phẩm miễn phí.
- [ ] Validate lượng add và tổng quantity chống integer overflow; không tự đặt max quantity kinh doanh.
- [ ] Bảo vệ concurrent get/create cart và add cùng SKU để không duplicate row/lost update; kiểm thử trên PostgreSQL.
- [ ] Giữ clear scoped theo cartId đã xác minh owner; thao tác không đổi Inventory/OrderStockHold.
- [ ] Cập nhật updatedAt nếu được bổ sung; response vẫn dùng server price và ảnh theo contract Phase06/07.
- [ ] Ghi rõ semantics get-or-create đã chọn và mọi thay đổi response; không thêm anonymous cart thiếu requirement.

## 12. Frontend Tasks

- [ ] Tạo cart screen theo ui-image9: progress3bước, ảnh/tên/variant, đơn giá, +/- quantity, subtotal từng dòng và remove.
- [ ] Nối add-to-cart từ detail, phản hồi loading/success/error và cập nhật cart counter từ response server.
- [ ] Tổng tạm tính lấy server; shipping hiển thị tính sau địa chỉ, voucher discount chỉ hiển thị từ quote Phase18 khi có.
- [ ] Ở quantity1, nút giảm không gửi0; xóa có hành động riêng, xử lý request đến muộn tránh hiển thị tổng cũ.
- [ ] Empty cart có CTA tiếp tục mua; checkout button disabled khi rỗng; navigation theo layout Phase10.
- [ ] Ownership/validation error không hiển thị dữ liệu cart khác; giữ màn hình có thông báo và refresh đúng cart.

## 13. Validation & Business Rules

- Quantity>0; một row/cart/SKU, add cùng SKU cộng lượng, subtotal=sum current price×qty.
- Không hold/reserve hay giảm actual khi sửa giỏ; checkout sẽ revalidate giá/stock.
- Item phải thuộc cart của principal; cart thuộc customer active hợp lệ.
- SKU inactive/giá thiếu không thể thêm như hàng miễn phí; dữ liệu cũ cần UI chỉ rõ không còn mua được.
- Mọi total và cart counter authoritative từ server; client không quyết định tiền.

## 14. Security Requirements

- Chỉ customer đã xác thực dùng /api/cart và /items; không cho staff dùng customerId để tự impersonate.
- GET/PUT/DELETE phải ownership check phía server; cartItemId của customer khác trả lỗi theo contract Phase01/02.
- Nếu UI dùng cookie/session, áp dụng CSRF của Phase02; không tự bỏ CSRF để fetch chạy.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Customer/SKU/item thiếu → lỗi đúng contract; item ngoài owner → từ chối không tiết lộ thông tin.
- Quantity null/âm/0/overflow, inactive SKU hoặc thiếu giá → validation/conflict có field/message UI.
- Concurrent unique violation/lock xử lý theo convention, không trả giỏ trùng hoặc mất lần cộng.
- Empty cart là state hợp lệ; không startup/server error.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Reuses catalog/ảnh Phase06/07 và detail Phase10; profile principal Phase02.
- Phase18 đọc cart server để quote; Phase19 chỉ clear sau checkout thành công trong transaction.
- Inventory không bị thay đổi trong phase; không yêu cầu cart phụ thuộc Phase08 chỉ để giữ hàng.

## 17. Implementation Order

1. Kiểm chứng cart principal và schema unique hiện tại.
2. Sửa ownership, get/create invalid customer và update-positive semantics.
3. Bổ sung active/price validation cùng concurrent merge regression.
4. Nối detail/cart UI với authoritative response.
5. Kiểm tra no-stock-mutation và handoff quote/checkout.

## 18. Verification

- [ ] A gửi cartItemId của B trong update/remove bị chặn; B không đổi.
- [ ] Add cùng SKU hai lần merge đúng; concurrent add không duplicate/lost quantity.
- [ ] Không operation cart nào đổi actual/held hoặc tạo stock hold.
- [ ] UI hiển thị server totals và empty/error/qty boundary, counter đúng.
- [ ] Existing add DTO @Min(1) vẫn hoạt động, update cũng enforce; null price không bị coi0.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P17-01 | Happy cart | Add SKU qty2 rồi thêm3 | Một dòng qty5, subtotal/counter đúng |
| P17-02 | Update boundary | quantity1 rồi0/-1/null | 1 hợp lệ; invalid bị từ chối, không xóa ngầm |
| P17-03 | Ownership update/remove | A dùng cartItemId của B | Từ chối; cart B không đổi |
| P17-04 | Invalid SKU/customer | SKU inactive/thiếu giá; customer thiếu | Không cart null, không hàng miễn phí |
| P17-05 | Empty cart | GET cart mới; clear cart own | Response rỗng hợp lệ, checkout disabled |
| P17-06 | Concurrent add | Hai add cùng customer/SKU | Một row; quantity tổng đúng |
| P17-07 | No holds | Add/update/remove/clear | Inventory/holds không thay đổi |
| P17-08 | Overflow | Add gây qty vượt kiểu Integer | Validation; quantity không wrap âm |
| P17-09 | Permission | Anonymous/staff gọi customer cart | Bị từ chối đúng authentication contract |

## 20. Definition of Done

- [ ] UC15–18 có ownership/identity đúng và UI end-to-end.
- [ ] Happy add/merge/subtotal/ảnh tái sử dụng; chỉ sửa phần thiếu.
- [ ] Positive quantity, concurrent add và no-stock-mutation có test.
- [ ] Không triển khai voucher/checkout/guest cart ngoài scope.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Customer thao tác giỏ an toàn, nhìn đúng SKU/ảnh/giá/tổng và không can thiệp giỏ người khác. Giỏ sẵn sàng làm nguồn server-side cho pricing/checkout.

## 22. Handoff To Next Phase

- Phase18 nhận cart của principal với SKU/quantity/giá current hợp lệ, response totals rõ.
- Phase19 biết route cart và primitive clear scoped; chỉ gọi sau đặt đơn thành công.
- Ghi quantity semantics, updatedAt, concurrency convention và frontend integration để không lặp sửa cart.
- Đây là contract dự kiến sau khi Phase được kiểm chứng. Agent tiếp theo chỉ được giả định capability đã tồn tại khi report là `COMPLETED`.
- Ghi chính xác route/schema/DTO đã chọn, dữ liệu test và known issues; không để Phase sau suy đoán trạng thái.

# Instructions For The Next AI Agent

You are continuing an existing project.

Before modifying code:

1. Read this entire Phase document.
2. Read 00_PROJECT_AUDIT.md.
3. Read 01_MASTER_ROADMAP.md.
4. Inspect the files listed under 'Files To Inspect First'.
5. Verify the Current State because previous phases may have modified the code.
6. Do not implement tasks belonging to later phases unless required to unblock this phase.
7. Do not rewrite working code unnecessarily.
8. Preserve the current architecture and conventions.
9. After completing the phase, update all checkboxes accurately.
10. Record every important decision and deviation in the Phase Completion Report.

# Phase Completion Report

Status:

NOT_STARTED

## Work Completed

Chưa thực hiện.

## Files Created

Chưa thực hiện.

## Files Modified

Chưa thực hiện.

## Database Changes

Chưa thực hiện.

## APIs / Routes Added

Chưa thực hiện.

## UI Added

Chưa thực hiện.

## Decisions Made

Chưa thực hiện.

## Deviations From Plan

Chưa thực hiện.

## Known Issues

Chưa thực hiện.

## Remaining Tasks

Chưa thực hiện.

## Verification Result

Chưa thực hiện.

## Notes For Next Phase

Chưa thực hiện.
