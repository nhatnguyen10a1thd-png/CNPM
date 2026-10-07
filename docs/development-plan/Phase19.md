# Phase 19 — Checkout COD, giữ hàng và màn hình thành công

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase08](Phase08.md), [Phase09](Phase09.md), [Phase11](Phase11.md), [Phase17](Phase17.md), [Phase18](Phase18.md). Complexity: HIGH. Risk: HIGH.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Đặt đơn COD từ giỏ của principal bằng một transaction chọn một cửa hàng active đủ toàn bộ SKU, lưu snapshot/giữ kho/tiêu voucher/xóa giỏ cùng nhau và hiển thị checkout/success đúng thiết kế.

## 2. Why This Phase Exists

placeOrder có snapshot/tổng/holds/clear và một happy test, nhưng chưa kiểm tra address ownership, active/available cho mọi SKU; chọn store đầu tiên hoặc null; quota tăng trước khi xác minh kho. Phải gia cố workflow có sẵn thay vì viết mới.

## 3. Current State

- POST /api/orders nhận customerId mặc định1; OrderRequest có customerAddressId/voucherCode/paymentMethod/note.
- PlaceOrder tìm address theo ID không so owner, giá null coi0, chọn storeRepository.findAll().first hoặc null.
- Đã lưu snapshot tên/variant/price/address và trạng thái PENDING_CONFIRMATION; giữ logic này.
- Hold được tạo khi có store, inventory thiếu bị bỏ qua và held tăng không check available; cart clear sau vòng items.
- Voucher usedQuantity tăng trước lưu đơn/kho; checked Exception sau mutation có thể để commit từng phần nếu chưa có rollback convention.
- OrderServiceTest.testPlaceOrderSuccess kiểm totals/hold/clear; chỉ happy mock, không chứng minh PostgreSQL atomicity/concurrency.
- UI §5.3.7–8 chưa có implementation; success mock hiển thị mã dạng LN và COD chưa trả, không coi đó là schema/code format đã chốt.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase08/09/11/17/18 COMPLETED; có lock/invariant, store active, owned address/cart và pricing/quota contract.
- [ ] D01 UI/auth, D05 địa chỉ, D06 shipping/FREESHIP và D15 mã chứng từ/đơn đã ghi quyết định trước UI/schema liên quan.
- [ ] Dataset có một branch đủ all SKUs, branch chỉ đủ từng phần và trường hợp không branch nào đủ.
- [ ] Error/transaction convention Phase01 xử lý late business failure bằng rollback thật.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Sửa checkout identity/ownership/validation và lựa chọn một store đủ tất cả dòng.
- Atomic order/items/snapshots/holds/inventory/quota/cart; chống simultaneous duplicate submit bằng transaction/lock đã chốt.
- Màn hình checkout COD, địa chỉ/note/voucher/pricing và success/owned order summary tối thiểu.

### OUT OF SCOPE

- Lịch sử/detail/status customer đầy đủ/hủy Phase20; staff transitions/branch reassign Phase21/22.
- Không tạo online payment gateway, shipping carrier/tracking hoặc chia một order thành nhiều cửa hàng.
- Không tạo guest checkout, reservation khi cart, human code/payment state tự phát.

## 6. Requirements Covered

- §3.4.1 UC21; đây là Phase owner. UC19/20/71 do Phase18 cung cấp, chỉ kiểm thử integration.
- KH-QĐ7–10, QLDH-QĐ2–3/QĐ6: COD, đủ address/stock, một branch đủ ALLitems, snapshot totals.
- QLTK-QĐ6/QĐ8: hold chỉ sau đặt đơn thành công/assigned branch, actual-held không âm.
- §4.1.2 Order/OrderItem/OrderStockHold và §4.2 toàn vẹn; UI §5.3.7–8, ui-image10/11.
- SYS09/SYS10 mã và timestamps được kiểm tra trên field/ID hiện có; SYS22 notifications thuộc Phase30.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [OrderService.java](../../src/main/java/com/thinh/cosmetic/service/order/OrderService.java)
- [OrderRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/order/OrderRequest.java)
- [OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- [OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [OrderItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderItemEntity.java)
- [OrderStockHoldEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java)
- [OrderStockHoldRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java)
- [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [CartRepository.java](../../src/main/java/com/thinh/cosmetic/repository/cart/CartRepository.java)
- [OrderServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/OrderServiceTest.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [OrderRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/order/OrderRequest.java)
- [OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- [OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [OrderServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/OrderServiceTest.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- PostgreSQL checkout integration tests cho multi-SKU shortage, rollback, last-stock/last-quota race và duplicate submit.
- Checkout/success UI theo D01, sử dụng pricing và address components đã có; DTO bổ sung chỉ theo D05/D06/D15 đã chốt.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

Giữ Order/Item snapshots và Hold hiện có. Đối chiếu recipient fields bắt buộc, money ≥0, item/hold quantity>0 và assignedStore theo schema/rules; thêm constraint qua migration sau kiểm kê dữ liệu NULL/invalid, không xóa đơn lịch sử. Hold.releasedAt thiết kế thiếu ở source do Phase20 bổ sung khi release nếu cần, không làm trước. Không tự thêm bảng payment/idempotency hoặc mã prefix chưa chốt; use locks hiện có để chống duplicate từ cùng cart.

## 11. Backend Tasks

- [ ] Lấy customer principal; kiểm tra account active, cart own và address own; không chấp nhận customerId default1.
- [ ] Chỉ COD; validate recipient/address theo D05 và note; SKU/product active, quantity/price hợp lệ từ server.
- [ ] Dùng quote/pricing Phase18, revalidate voucher/quota và cart current prices ngay trước commit; không tin giá/tổng client.
- [ ] Chọn một store active có inventory available đủ tất cả SKU; không chọn first store tùy ý, không NULLstore, không chia branch.
- [ ] Khóa cart/selected inventory/voucher theo thứ tự ổn định của dependency; kiểm tra lại available sau lock trước tạo hold.
- [ ] Giữ snapshot tên/variant/giá/address hiện có; lưu order PENDING_CONFIRMATION, items, holds, held increments, quota consumption và clear cart cùng transaction.
- [ ] Stock thiếu ở dòng sau, lỗi quota/DB hay checked business failure phải rollback mọi mutation; giỏ và quota không mất.
- [ ] Chống hai checkout đồng thời cùng cart: tối đa một đơn từ cùng tập items; request sau đọc giỏ đã xử lý nhận state/conflict hợp lệ.
- [ ] Cung cấp owned read tối thiểu để success refresh không gọi lại POST; history/detail nâng cao thuộc Phase20.
- [ ] Bảo vệ generic GET/all/status route theo quyền Phase04; không mở arbitrary staff transition cho customer để success hoạt động.
- [ ] Ghi audit/event contract sau commit theo Phase04/30; không tích hợp provider thông báo tại Phase19.

## 12. Frontend Tasks

- [ ] Checkout theo ui-image10: recipient/phone/địa chỉ province/ward theo D05, chọn hoặc sửa owned address, shipping/COD/note/voucher và order summary.
- [ ] Chỉ hiển thị COD; phí/ngày dự kiến là dữ liệu theo policy đã chốt, không hardcode ngày hoặc30k từ mockup.
- [ ] Apply voucher tái dùng Phase18; summary server authoritative, trạng thái địa chỉ/phí còn thiếu rõ.
- [ ] Confirm order disable khi đang gửi; lỗi stock/price/quota giữ form/cart và cho xem quote mới.
- [ ] Success theo ui-image11 dùng order persisted: ID/mã theo D15, PENDING_CONFIRMATION, COD, total, recipient/ship và CTA.
- [ ] Refresh success dùng owned GET; nút đặt đơn không còn gọi lại POST. CTA my orders chỉ nối Phase20 khi COMPLETED.
- [ ] Có loading/error/empty cart/permission states, mobile layout theo D01.

## 13. Validation & Business Rules

- Một assignedStore active đủ all SKUs; available kiểm dưới lock, actual không giảm ở checkout chỉ held tăng.
- COD duy nhất; đơn thành công mới clear cart/consume quota; quote trước đó không đủ thay thế validation.
- OrderItem và recipient snapshot bất biến với thay đổi catalog/address sau này.
- Atomicity toàn checkout; lỗi một dòng không để order/hold/quota/inventory/cart cập nhật một phần.
- PENDING_CONFIRMATION là trạng thái đầu; không gọi generic updateStatus để skip workflow.
- Request lặp không tạo hai đơn từ cùng cart; success đọc order own, không tạo lại.

## 14. Security Requirements

- Customer đã xác thực chỉ checkout cart/address own; staff/customer khác không đọc success order ngoài scope.
- POST /api/orders bảo vệ auth/CSRF theo Phase02; không nhận actor ID như chứng cứ quyền.
- GET /api/orders/{id} dùng cho success có owner guard; GET all/status chỉ staff permission, chưa triển khai full transitions ở đây.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Empty cart/invalid address/COD khác/SKU inactive → validation hoặc conflict rõ.
- Không branch nào đủ allitems → out-of-stock result, không orderNULLstore/partialbranch.
- Last-stock/last-voucher race → một request thành công, request còn lại conflict có thể cập nhật quote.
- Lỗi late persistence hoặc invariant rollback toàn checkout; UI không hiển thị success từ optimistic client state.
- Không tiết lộ chi tiết địa chỉ/đơn của customer khác.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Inventory Phase08 và store Phase09 cung cấp chọn/lock stock; identity/address/cart/pricing từ02/11/17/18.
- Phase20 đọc order snapshots và dùng cancellation/release; Phase21/22 tiếp workflow sau PENDING.
- Phase30 nhận event integration contract sau commit; không gửi notification trước DB commit.

## 17. Implementation Order

1. Đọc report và chốt D05/D06/D15 liên quan; đối chiếu happy test snapshot đã có.
2. Gia cố owned principal/cart/address, COD validation và pricing integration.
3. Thêm candidate-all-SKUs và locking/atomic mutation; viết PostgreSQL failure/race tests.
4. Cung cấp owned success read, nối checkout/success UI.
5. Regression existing happy test, before/after DB và handoff snapshots/holds.

## 18. Verification

- [ ] Một branch thiếu một SKU bị bỏ qua; nếu các branch chỉ đủ rời rạc thì checkout thất bại nguyên vẹn.
- [ ] Successful order giữ actual, tăng held, consume quota1 và clear đúng cart; snapshots đúng quote persisted.
- [ ] Lỗi cuối vòng items rollback order/items/holds/held/quota/cart.
- [ ] Hai customers mua lượng cuối hoặc voucher lượt cuối chỉ một thành công; same-cart concurrent không tạo hai đơn.
- [ ] Success refresh/ID khác owner/validation UI được kiểm chứng HTTP/browser.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P19-01 | Happy COD | Cart2SKU, branch đủ all, address own | Một PENDING order, snapshots/holds đúng, cart clear |
| P19-02 | Owner | Address của B hoặc đọc success order B | Từ chối; không lộ/lưu dữ liệu |
| P19-03 | Single branch rule | A đủSKU1, B đủSKU2, không store đủboth | Không chia order; cart/quota/stock giữ nguyên |
| P19-04 | Available boundary | actual10 held8, qty2 rồi qty3 | 2 hợp lệ, 3 bị chặn; available≥0 |
| P19-05 | Concurrent last stock | Hai checkout qty1, available1 | Chỉ một thành công; không oversell |
| P19-06 | Concurrent last voucher | used=limit−1, hai checkout | Chỉ một consume; quota không vượt |
| P19-07 | Late failure | Lỗi item2/constraint sau mutation item1 | Rollback mọi bảng và giữ cart |
| P19-08 | Invalid/empty | Cart rỗng, qty0, SKU inactive, nonCOD | Lỗi rõ; không tạo đơn |
| P19-09 | Same-cart duplicate | Hai submit đồng thời hoặc refresh success | Tối đa một đơn; GET refresh không mutation |
| P19-10 | Snapshot stability | Sau order đổi product price/address | Order summary vẫn snapshot gốc |

## 20. Definition of Done

- [ ] UC21 COD end-to-end, all-SKUs-one-store và owned address được enforce.
- [ ] Atomicity/last-stock/last-quota/same-cart race có PostgreSQL evidence.
- [ ] Happy snapshot/total/hold/clear behavior và existing test giữ được.
- [ ] Checkout/success đúng mockup sau D01/D05/D06/D15, không policy ngày/phí/mã tự phát.
- [ ] Không triển khai staff workflow/history/hủy/online payment ngoài scope.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Customer đặt được một đơn COD có đầy đủ snapshots và holds hợp lệ; lỗi checkout không làm mất giỏ hoặc tiêu quota. Success phản ánh dữ liệu đã commit, sẵn sàng cho order workflow.

## 22. Handoff To Next Phase

- Phase20 nhận owned order summary, snapshots, initial status và hold data từ checkout valid.
- Phase21/22 nhận assigned branch đủ allitems, không cần viết lại checkout/pricing.
- Ghi exact quote/placeOrder/success routes, lock order, constraint changes và notification event contract đã chọn.
- Bàn giao dataset stock/quota races và known failures; không coi H2 pass là PostgreSQL lock proof.
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
