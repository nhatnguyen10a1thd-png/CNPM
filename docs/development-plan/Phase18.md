# Phase 18 — Voucher và contract tính tiền thống nhất

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase04](Phase04.md), [Phase11](Phase11.md), [Phase17](Phase17.md). Complexity: HIGH. Risk: HIGH.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Hoàn thiện voucher management/eligibility và một nguồn tính subtotal, discount, shipping, total để cart/checkout/staff dùng chung, chưa tiêu quota khi chỉ xem trước.

## 2. Why This Phase Exists

PERCENT/FIXED/cap/min/date/quota đã có và 5 test pass; FREESHIP hiện rơi vào nhánh FIXED. Shipping source miễn phí từ500k trái ví dụ UI phí30k ngay cả subtotal4.292m, phải chốt D06 trước tính tiền mới.

## 3. Current State

- VoucherServiceImpl có CRUD/deactivate, existsByCode ở create, date/quota/active/min validation và calculateDiscount.
- Update không kiểm tra mã trùng; value/%/cap/date-order/quota chưa đủ constraint.
- DiscountType FREESHIP đã có enum nhưng service dùng else discountValue như FIXED.
- GET /api/vouchers/discount nhận orderTotal client; không phải nguồn tin cậy cho checkout cuối.
- OrderServiceImpl chứa shipping>=500000 riêng và increments usedQuantity trước order/stock; chuyển integration sang Phase19, không giữ preview mutation.
- Không có pricing quote contract lấy cart/address thực; chưa có voucher/pricing UI.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase04 có marketing permission và Phase11 address ownership; Phase17 có scoped cart.
- [ ] [D06](01_MASTER_ROADMAP.md#decision-register) đã chốt phí/miễn phí, FREESHIP, rounding và conditions; không hardcode30k từ mockup.
- [ ] Điều kiện đối tượng/phạm vi voucher chưa có model phải được chốt bằng D06/D10 ở mức contract; không implement campaign Phase26 tại đây.
- [ ] Có clock/timezone convention Phase01 và dataset đúng hạn/quota boundary.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Gia cố CRUD/deactivate voucher, eligibility và contract quote từ dữ liệu server.
- Dùng chung money calculations cho UC19/20/71/77–79; explicit FREESHIP sau gate.
- Màn hình marketing voucher và apply/remove voucher/summary trên cart/checkout theo contract.

### OUT OF SCOPE

- Không tạo PromotionProgram, báo cáo hiệu quả/chương trình Phase26.
- Không tạo order, giữ stock, clear cart hoặc consume quota trong preview.
- Không thêm stacking/multi-voucher, online payment hay loyalty discounts ngoài thiết kế.

## 6. Requirements Covered

- §3.4.1 UC19/20, §3.4.8 UC71, §3.4.9 UC77/78/79; đây là Phase owner.
- KH-QĐ8–9, QLDH-QĐ6 và QLKM-QĐ3–5: date/quota/min/eligibility, cap và tổng không âm.
- §4.1.2 Voucher; UI §5.3.6–7 voucher apply và summary; source/UI shipping conflict D06.
- QLKM-QĐ1–2 chương trình promotion thuộc Phase26, chỉ đọc contract sau khi được chốt.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [VoucherServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java)
- [VoucherService.java](../../src/main/java/com/thinh/cosmetic/service/order/VoucherService.java)
- [VoucherRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/VoucherRestController.java)
- [VoucherRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java)
- [VoucherRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/order/VoucherRequest.java)
- [VoucherEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/VoucherEntity.java)
- [DiscountType.java](../../src/main/java/com/thinh/cosmetic/domain/enums/DiscountType.java)
- [VoucherMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/order/VoucherMapper.java)
- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [VoucherServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/VoucherServiceTest.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [VoucherServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java)
- [VoucherService.java](../../src/main/java/com/thinh/cosmetic/service/order/VoucherService.java)
- [VoucherRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/VoucherRestController.java)
- [VoucherRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java)
- [VoucherRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/order/VoucherRequest.java)
- [VoucherServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/VoucherServiceTest.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- Dự kiến pricing service/quote request-response trong package order theo convention hiện có; tên/route chính xác ghi report trước khi Phase19 dùng.
- Tests pricing/eligibility/FREESHIP/date/quota/rounding; PostgreSQL quota consume contract integration test nếu primitive được thêm.
- UI quản trị voucher và apply/summary components theo D01, tái sử dụng ở checkout.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

Giữ Voucher và unique code hiện có; không thêm campaign tables. TECHNICAL RECOMMENDATION: CHECK value/cap/min/quantity nonnegative, usedQuantity ≤ totalQuantity khi quota hữu hạn, date-order nếu schema hỗ trợ, sau kiểm kê legacy data. Lock/atomic quota primitive dùng convention Phase01; thêm field eligibility chỉ khi D06/D10 có model được duyệt và migration có dữ liệu đối chiếu.

## 11. Backend Tasks

- [ ] Giữ PERCENT/FIXED arithmetic và 5 test đang đúng; thêm validation discountValue/cap/min/quota/date-range theo rule được chốt.
- [ ] Kiểm tra code uniqueness khi update loại trừ voucher hiện tại; unique DB xử lý concurrent duplicate.
- [ ] Tách FREESHIP khỏi discount tiền hàng; shipping reduction không vượt phí, total không âm theo D06.
- [ ] Quote lấy principal cart, server SKU prices và owned address/context; không tin orderTotal/subtotal client.
- [ ] Trả subtotal, item-discount, shipping trước/sau ưu đãi, total và trạng thái eligible/error; nếu chưa có địa chỉ dùng trạng thái chưa xác định phí theo D06.
- [ ] Preview/apply voucher không tăng usedQuantity; cung cấp primitive validate/consume nguyên tử cho Phase19 cùng transaction đặt đơn.
- [ ] Ghi rõ điều kiện start/end inclusive, timezone, quota boundary và rounding đã được chốt; không dùng thời gian client.
- [ ] Bảo vệ CRUD/list chi tiết voucher bằng marketing quyền; customer chỉ dùng quote/eligibility cần thiết, không lộ dữ liệu quản trị.
- [ ] Chuẩn hóa totals để staff order UC71 dùng lại snapshot, không tự tính theo giá catalog hiện tại sau đặt đơn.
- [ ] Ghi integration contract cho campaign eligibility khi D10 có quyết định; không kéo tạo campaign vào Phase18.

## 12. Frontend Tasks

- [ ] Màn hình marketing tạo/sửa/deactivate/list voucher có code/type/value/cap/min/date/quota/status và validation.
- [ ] Apply/remove voucher đọc quote server, hiển thị lý do hết hạn/hết lượt/chưa đủ điều kiện và reset summary đúng.
- [ ] Cart chưa có địa chỉ phải hiển thị shipping pending theo gate, không giả phí0 hoặc total cuối.
- [ ] Checkout summary tách subtotal/discount/shipping/total; không trừ FREESHIP vào giá hàng.
- [ ] Disable apply khi request chạy; stale response không ghi đè quote mới; xử lý empty cart/address thiếu.

## 13. Validation & Business Rules

- PERCENT/FIXED giữ cap/min/date/quota logic đúng; FIXED không vượt subtotal, % và tiền theo D06.
- FREESHIP tác động shipping; không tự mở nhiều voucher hoặc cộng dồn.
- Quote không tiêu quota, không hold/đặt đơn; checkout Phase19 revalidate dưới transaction.
- Tổng=sum price×qty−item discount+shipping sau ưu đãi, không âm và cùng convention rounding.
- Eligibility theo đối tượng/phạm vi chỉ theo contract được chốt, không suy từ tên voucher/campaign.

## 14. Security Requirements

- Marketing được CRUD/deactivate voucher; nhân viên xử lý đơn được dùng contract totals theo role/scope.
- Customer quote chỉ cart/address own và dữ liệu voucher tối thiểu; không dùng /discount client-total để đặt tiền thật.
- CSRF/auth theo Phase02/04; không cho client gửi usedQuantity hoặc đổi quota counter.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Code trùng/date sai/value âm/quota không hợp lệ → field validation/conflict.
- Không đủ điều kiện/hết quota/hết hạn trả reason ổn định cho UI; không biến mọi lỗi thành discount0.
- Address/cart ngoài owner bị từ chối; shipping chưa xác định là state có chủ đích, không exception lộ nội bộ.
- Nếu D06/D10 còn mở, ghi BLOCKED phần liên quan; không chọn chính sách để làm test pass.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Phase17 là nguồn cart, Phase11 nguồn owned address; Phase19 consumes quote/eligibility và quota atomically.
- UC71 staff totals Phase21/22 đọc snapshots/contract đã bàn giao, không viết money logic riêng.
- Phase26 dùng voucher/quota/discount data cho hiệu quả; không đổi arithmetic đã kiểm chứng.

## 17. Implementation Order

1. Chốt/ghi D06 và eligibility contract D10 liên quan, đọc tests arithmetic hiện có.
2. Gia cố voucher CRUD/validation/date/uniqueness mà giữ existing tests.
3. Thiết kế một pricing/quote service server-side và FREESHIP rõ ràng.
4. Thêm validate/consume contract không mutation trong preview; test boundary.
5. Nối marketing/apply/summary UI và bàn giao Phase19.

## 18. Verification

- [ ] 5 existing VoucherServiceTest vẫn pass; bổ sung FREESHIP và boundary tests.
- [ ] Tổng quote lấy cart prices server; giả subtotal request không ảnh hưởng.
- [ ] 100 lần preview không tăng usedQuantity/holds; address owner được enforce.
- [ ] Cùng dataset cho cart/checkout/staff có cùng arithmetic/snapshot semantics.
- [ ] D06 chính sách được ghi với ví dụ theo quyết định, không sử dụng ví dụ UI như policy mặc định.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P18-01 | Percent/fixed regression | Existing5 tests cap/min/expired | Giữ kết quả đang đúng |
| P18-02 | FREESHIP | Shipping fee theo D06 + voucher hợp lệ | Giảm shipping đúng; subtotal không đổi |
| P18-03 | Validation | %>100 theo rule, giá âm, start>end | Lỗi field; không lưu |
| P18-04 | Uniqueness update | Đổi code thành code voucher khác | Conflict; DB không duplicate |
| P18-05 | Date boundary | Exactly start/end, trước/sau | Theo inclusive rule đã chốt, server clock |
| P18-06 | Quota boundary | used=total và used=total−1 | Hết lượt vs eligible đúng; preview không consume |
| P18-07 | Tampered money | Client total1 nhưng cart100000 | Quote dùng cart server |
| P18-08 | Permission/ownership | Customer quản trị voucher hoặc address B | Từ chối; không lộ data |
| P18-09 | Empty/context missing | Cart rỗng hoặc chưa địa chỉ | State/error đúng contract, shipping không đoán |
| P18-10 | Nonnegative/rounding | Discount vượt subtotal, tiền chia phần trăm | Cap và scale đúng; total≥0 |

## 20. Definition of Done

- [ ] UC19/20/71/77–79 có owner contract thống nhất và UI trong scope.
- [ ] D06 cùng eligibility gate liên quan đã giải quyết; không hardcode mockup policy.
- [ ] Arithmetic hiện có giữ test, FREESHIP/validation/uniqueness/preview-no-consume kiểm chứng.
- [ ] Quota primitive có semantics để Phase19 dùng transaction; chưa tạo order trong Phase18.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Hệ thống có quote giá đáng tin cậy và voucher management/eligibility hoàn chỉnh theo các quyết định đã chốt. Cart/checkout không tự tính shipping/discount hoặc tiêu lượt khi xem trước.

## 22. Handoff To Next Phase

- Phase19 nhận exact quote DTO/service/route và quota validate-consume contract trong transaction.
- Phase21/22 biết tổng từ snapshot và không tính lại bằng giá mới; UC71 được kiểm chứng tích hợp.
- Phase26 nhận field voucher/usage/discount đã có; campaign model vẫn thuộc Phase26.
- Ghi D06 decisions, timezone/rounding, clock tests và version contract trong report.
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
