# Phase 20 — Lịch sử đơn customer và hủy trước SHIPPING

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase19](Phase19.md). Complexity: MEDIUM. Risk: HIGH.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Cho customer xem lịch sử, chi tiết, trạng thái đơn own và hủy đơn trước SHIPPING bằng release hold nguyên tử, có màn hình tài khoản/my orders.

## 2. Why This Phase Exists

GetByCustomer/getById và cancellation tồn tại nhưng read chưa có ownership và cancellation chỉ cho PENDING_CONFIRMATION. Thiết kế cho hủy trước SHIPPING; phải mở đúng CONFIRMED/PREPARING mà không cho hủy terminal/shipped.

## 3. Current State

- GET /api/orders/customer/{customerId}, GET /{id} và PUT /{id}/cancel đã có, cancel customerId mặc định1.
- getByCustomer trả list không pagination/filter; getById không owner check; OrderResponse có snapshot/total/status/items.
- cancelOrder kiểm owner rồi chỉ PENDING_CONFIRMATION; updateStatus(CANCELLED) đã release HELD, giảm heldQuantity bằng clamp0.
- Arbitrary updateStatus có thể hồi sinh terminal hoặc commit/release sai; full staff transition sửa ở Phase21, customer cancel phải có guard riêng an toàn.
- OrderStockHoldEntity có createdAt nhưng thiếu releasedAt so với thiết kế; OrderEntity có cancelledAt.
- Không có my-orders/list/detail/status UI; UI navigation mô tả tabs tất cả/đang xử lý/đã giao/đã hủy, không có admin mockup.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase19 có order snapshots/holds/owned summary và PostgreSQL transaction convention.
- [ ] Phase08 primitive release/invariant được bàn giao qua19; Phase02 principal và Phase04 quyền đã hoạt động.
- [ ] D01 có account/my-orders UI layout; mapping UI status groups sang enum được ghi rõ theo thiết kế.
- [ ] Không mở staff status routes cho customer; thống nhất shared order/hold lock order trước sửa service.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- History/detail/status customer own, filter/pagination và UI my orders.
- Hủy PENDING_CONFIRMATION/CONFIRMED/PREPARING theo quy tắc before SHIPPING, release HELD đúng một lần.
- Gia cố customer-cancel terminal/race guards, cancelledAt/releasedAt/audit nếu cần theo schema.

### OUT OF SCOPE

- Staff search/confirmation/branch reassign/status Phase21, shipping/complete/print Phase22.
- Không tạo return/refund logic Phase23 hoặc review Phase24.
- Không tính lại tiền snapshot, không tự hoàn quota voucher khi cancel nếu policy chưa chốt.
- Không thêm real-time tracking/shipping gateway hoặc trạng thái không có trong thiết kế.

## 6. Requirements Covered

- §3.4.1 UC22, UC23, UC24, UC25; đây là Phase owner.
- KH-QĐ11–13: chỉ đơn own, trạng thái rõ, hủy trước SHIPPING; QLDH-QĐ5/QLTK-QĐ7 release held khi cancel.
- §4.1.2 Order/OrderItem/OrderStockHold: cancelledAt/releasedAt; §4.2 inventory invariant.
- UI navigation account/My orders và tabs trạng thái; success CTA §5.3.8 nối đến lịch sử này.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [OrderService.java](../../src/main/java/com/thinh/cosmetic/service/order/OrderService.java)
- [OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [OrderStockHoldRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java)
- [OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- [OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [OrderStockHoldEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java)
- [OrderStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/OrderStatus.java)
- [OrderServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/OrderServiceTest.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [OrderService.java](../../src/main/java/com/thinh/cosmetic/service/order/OrderService.java)
- [OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [OrderStockHoldRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java)
- [OrderStockHoldEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java)
- [OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- Customer-order ownership/cancel tests và PostgreSQL concurrent cancel/status/release regression.
- My-orders/detail/status UI theo D01; filter DTO nếu list contract cần, không tạo order table mới.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

Order.cancelledAt đã có. Bổ sung OrderStockHold.releasedAt theo thiết kế nếu chưa có; backfill lịch sử chỉ khi có bằng chứng timestamp release, không dùng createdAt để đoán. Giữ status enums/order IDs/snapshots; lock/constraint dùng Phase08/19, không thêm bảng payment/refund. Voucher quota sau cancel chưa rõ phải ghi D06, không thêm mutation tự động.

## 11. Backend Tasks

- [ ] Scope history/detail bằng principal; bỏ customerId mặc định/client impersonation. Staff delegated lookup vẫn đi route/permission riêng Phase21/25.
- [ ] Thêm pagination và status filter cho history own theo createdAt desc cùng tie-break ID; tabs UI map rõ enum.
- [ ] Detail trả snapshot items/address/subtotal/discount/shipping/total, status và timestamps cần thiết; không join current price để sửa giá lịch sử.
- [ ] Customer-cancel allowlist chỉ PENDING_CONFIRMATION, CONFIRMED, PREPARING; SHIPPING/COMPLETED/CANCELLED bị chặn.
- [ ] Khóa order/holds/inventory đúng convention; release từng HELD một lần, actual giữ nguyên, không clamp0 để che inconsistency.
- [ ] Cancel status/cancelledAt/releasedAt/audit/held decrement cùng transaction; late failure rollback tất cả.
- [ ] Gọi lại cancelled order không giảm held thêm; trả current/conflict nhất quán theo contract hiện có và ghi report.
- [ ] Tách customer guarded cancellation khỏi generic arbitrary updateStatus; không implement full staff transition trong Phase20.
- [ ] Không tự decrement usedQuantity/refund khi hủy; nếu D06 xác định quota restore thì chỉ làm theo contract được chốt, phối hợp voucher primitive18.
- [ ] Thêm security/method tests chống IDOR cả read/cancel và code/timestamp legacy display theo D15.

## 12. Frontend Tasks

- [ ] Tạo account/my orders page có tabs tất cả/đang xử lý/đã giao/đã hủy, pagination và empty state.
- [ ] Tạo detail snapshot: recipient/address, item qty/price, totals, status, created/confirmed/completed/cancelled time nếu có.
- [ ] Nút hủy chỉ status trước SHIPPING và own; confirm và loading, response server quyết định status.
- [ ] Stale order đã SHIPPING khi nhấn hủy phải refresh/error, không optimistic cancelled.
- [ ] Nối success My orders CTA và account navigation; giữ filter/page khi mở detail/quay lại.
- [ ] Không hiển thị nút return/review như đang hoạt động trước Phase23/24; nối sau handoff COMPLETED.

## 13. Validation & Business Rules

- Customer chỉ xem/hủy đơn own; history status group không thay đổi enum stored.
- Before SHIPPING nghĩa PENDING_CONFIRMATION/CONFIRMED/PREPARING; terminal không hủy hoặc hồi sinh.
- Cancel release HELD đúng một lần, actual không đổi; không release COMMITTED/RELEASED lần nữa.
- Tổng và thông tin order là snapshots; cập nhật customer/address/catalog sau đặt đơn không sửa lịch sử.
- Không hoàn tiền COD/quota hoặc nhập kho return tự phát khi hủy; các policy chưa rõ phải gate.

## 14. Security Requirements

- Customer GET history/detail/status/cancel có principal ownership; không cho query customerId của người khác.
- Staff permissions cho order management không được mượn customer route impersonation; thuộc Phase21.
- Auth/CSRF Phase02; /api/orders/{id}/status không dành cho customer dù UI chỉ cần status read.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Order thiếu/outside-owner trả theo contract tránh lộ dữ liệu.
- Shipped/terminal/stale status hủy → conflict, UI cập nhật dữ liệu mới.
- Invariant held thiếu/đã commit hoặc missing inventory → rollback, không Math.max0 và đánh dấu hủy thành công.
- History filter enum/page invalid → validation; danh sách rỗng là state hợp lệ.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Phase19 cung cấp initial snapshots/holds; Phase08 primitive release được dùng nguyên trạng.
- Phase21/22 dùng cùng lock/status contract, cần regression cancel vs shipping/complete khi staff workflow được triển khai.
- Phase23/24 sử dụng detail/order ownership/completed status, không lấy current catalog làm bằng chứng mua.
- Phase30 nhận cancellation event sau commit theo integration contract, không gửi thông báo tại Phase20.

## 17. Implementation Order

1. Kiểm tra report19, existing owner/read và order/hold locks.
2. Gia cố scoped history/detail/filter/pagination và timestamp response.
3. Implement guarded before-shipping cancel với atomic release và regression.
4. Nối my-orders/detail/cancel UI và success CTA.
5. Bàn giao shared cancel contract cho Phase21/22, ghi tests phối hợp.

## 18. Verification

- [ ] A không đọc/hủy orderB qua mọi route; history chỉ trả own.
- [ ] PENDING/CONFIRMED/PREPARING được hủy; SHIPPING/COMPLETED/CANCELLED không giảm kho.
- [ ] Cancel actual giữ nguyên, held giảm đúng lần, timestamp/audit/status khớp.
- [ ] Late failure rollback toàn cancel; concurrent cancels không double release.
- [ ] History/detail UI tabs/paging/empty/error snapshots đúng; staff race kiểm lại khi21/22 hoàn thành.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P20-01 | Own history/detail | A có2orders, B có1 | A chỉ thấy2; detail/totals snapshot |
| P20-02 | Before shipping statuses | Hủy PENDING_CONFIRMATION, CONFIRMED, PREPARING | CANCELLED, releaseHELD, actual không đổi |
| P20-03 | Forbidden status | Hủy SHIPPING/COMPLETED/CANCELLED | Conflict; không release/commit thêm |
| P20-04 | Permission IDOR | A GET/cancel orderB | Từ chối; B không thay đổi |
| P20-05 | Concurrent duplicate cancel | 2request cancel cùng order | Một release; held không âm |
| P20-06 | Late release failure | Order nhiềuhold, lỗi dòng2 | Status/time/held/audit rollback toàn bộ |
| P20-07 | Snapshot boundary | Catalog/address thay đổi sau order | Detail giữ giá/address gốc |
| P20-08 | Empty/filter/page | Không có order khớp status; page invalid | Empty hợp lệ hoặc validation theo contract |
| P20-09 | Shipping race regression | Cancel vs staff SHIPPING (khi21 có) | Chỉ một transition hợp lệ dưới lock |
| P20-10 | Quota policy | Cancel đơn có voucher | Quota giữ/restore chỉ theo D06 đã chốt |

## 20. Definition of Done

- [ ] UC22–25 có customer UI/HTTP/data end-to-end và không IDOR.
- [ ] Hủy trước SHIPPING đúng thiết kế, release nguyên tử/đúng một lần có PostgreSQL tests.
- [ ] Read/detail giữ snapshots; history filter/pagination/tabs đúng.
- [ ] Không triển khai staff workflow/return/review hoặc quota/refund chưa chốt.
- [ ] Bàn giao shared cancel/status/lock contract và ghi deferred regression staff race cho21/22.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Customer quản lý được lịch sử và xem trạng thái đơn own, hủy được trước SHIPPING mà không làm sai kho. Luồng success → my orders hoạt động, dữ liệu lịch sử được giữ.

## 22. Handoff To Next Phase

- Phase21/22 nhận guarded customer cancellation và release helper để dùng chung, không copy logic giữ/nhả kho.
- Phase23/24 nhận owned detail/order/items/snapshot/status, chỉ mở return/review sau rule của phase đó.
- Ghi status groups UI, exact query/page/cancel responses, releasedAt migration và D06 quota decision.
- Ghi rõ race shipping/complete cần Phase21/22 chạy lại, không tuyên bố test staff workflow chưa có đã pass.
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
