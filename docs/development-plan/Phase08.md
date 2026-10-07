# Phase 08 — Tồn kho, điều chỉnh và contract giữ hàng an toàn

## 1. Objective

Làm cho số liệu tồn kho và thao tác điều chỉnh/giữ/giải phóng hàng nhất quán theo store×SKU, có rollback và bảo vệ đồng thời.

Complexity: HIGH. Risk: HIGH. Phase này bàn giao contract kho cho nhập hàng, điều chuyển và checkout; chưa triển khai các workflow đó.

## 2. Why This Phase Exists

InventoryServiceImpl đã có read/available/adjust, OrderStockHoldEntity cũng tồn tại. Hiện adjust cho phép actual âm hoặc nhỏ hơn held; available bị clamp về0 nên che dữ liệu sai.

Chưa có lock/version; checkout/order đang cập nhật held trực tiếp. Cần một contract an toàn trước các luồng mutation tiếp tục phát triển.

## 3. Current State

- Routes hiện có: `/api/inventory/store/{storeId}`, `.../sku/{skuId}`, `.../available`, `POST /api/inventory/adjust`.
- adjustStock lưu history before/after rồi thay actual; actor có thể null và controller mặc định employeeId=1.
- available là Math.max(0, actual-held), không phát hiện held>actual.
- Inventory có surrogate ID với unique(store_id,sku_id): đúng ý nghĩa uniqueness của design; không cần đổi PK.
- OrderStockHold có order/SKU/store/quantity/status/createdAt, chưa releasedAt.
- InventoryRepository.findLowStock dùng actual<=minimum. Sửa query/cảnh báo thuộc Phase15; Phase08 ghi contract available làm nền.
- Hai InventoryServiceTest baseline kiểm availability/happy path adjust; phải giữ và mở rộng.

## 4. Preconditions

- [ ] Phase04 có employee principal, permission và branch scope.
- [ ] Phase06 có SKU hợp lệ và status/ID ổn định.
- [ ] Phase01 có PostgreSQL test/config và rollback/error convention.
- [ ] Có fixture StoreEntity hiện hữu; Phase08 không phụ thuộc Phase09 cải tiến store.
- [ ] Rà dữ liệu actual/held/minimum null/âm hoặc inconsistent trước thêm constraint.
- [ ] D01 chốt UI trước màn hình điều chỉnh staff.

## 5. Scope

### IN SCOPE

- UC49 tra cứu kho; UC51 điều chỉnh; UC54 hold/release contract; UC55 available.
- Bất biến stock, transaction, concurrency và audit.
- UI tra cứu/điều chỉnh theo branch scope.
- Contract internal cho mutation nhập hàng/transfer/checkout sau này.

### OUT OF SCOPE

- Kiểm kê/low-stock/export Phase15; receipt Phase14; transfer Phase16.
- Chọn branch và tạo order Phase19; fulfillment/completion Phase21–22.
- Làm lại Store CRUD hoặc tạo module kho mới bên ngoài thiết kế.

## 6. Requirements Covered

- Hệ thống §3.4.5 UC49, UC51, UC54, UC55.
- §4.1.2 Inventory, InventoryAdjustment, OrderStockHold.
- §4.2 QLTK-QĐ1, QĐ3, QĐ6–8: store×SKU, before/after/reason/actor/time, hold khi order hợp lệ, release khi hủy/không hợp lệ, available=actual-held≥0.
- QLTK-QĐ9 cần available cho Phase15; không nhận UC56 vào Phase08.
- UC54 được sở hữu ở đây; gọi contract theo lifecycle order được tích hợp ở Phase19–22.

## 7. Files To Inspect First

- [InventoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java), [InventoryService.java](../../src/main/java/com/thinh/cosmetic/service/store/InventoryService.java).
- [InventoryRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/InventoryRestController.java), [InventoryAdjustmentRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/store/InventoryAdjustmentRequest.java).
- [InventoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java), [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java).
- [InventoryAdjustmentEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryAdjustmentEntity.java).
- [OrderStockHoldEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java), [OrderStockHoldRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java).
- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java): đọc mutation kho hiện hữu để chuẩn bị adapter.
- [InventoryServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/InventoryServiceTest.java).

## 8. Files Expected To Modify

- Inventory service/controller/repository/DTO để validation, scoped identity và transaction.
- Inventory/Adjustment/Hold entities chỉ thêm constraint/timestamp cần theo design.
- Shared stock mutation trong OrderServiceImpl chỉ điều chỉnh để gọi contract nếu cần và có regression; không đổi order workflow.
- Migration/config theo Phase01, audit hook theo Phase04.

## 9. Files Expected To Create

Dự kiến nếu cần, chưa tồn tại:

- Stock-hold/mutation service dưới `src/main/java/com/thinh/cosmetic/service/store/` hoặc package order theo convention hiện tại.
- DTO nội bộ cho danh sách SKU/quantity và result stock; không tạo public reserve endpoint cho customer.
- PostgreSQL concurrency/rollback integration test và UI tra cứu/điều chỉnh theo D01.
- Không tạo inventory migration bằng generated DDL không kiểm tra legacy.

## 10. Database Changes

- Giữ unique(store_id,sku_id) và surrogate ID.
- Sau data audit, bổ sung check actual>=0, held>=0, actual>=held và minimum>=0 nơi phù hợp design; không tự clamp/cắt held legacy.
- Bổ sung updatedAt cho Inventory và releasedAt cho OrderStockHold theo design.
- Adjustment actor cần có FK đúng employee và dữ liệu reason/before/after/time đầy đủ; kiểm tra legacy actor-null trước NOT NULL.
- TECHNICAL RECOMMENDATION: dùng khóa ghi khi thay stock/hold, lấy nhiều SKU theo thứ tự ổn định để hạn chế deadlock; không cần đổi database.
- Mọi backfill dữ liệu sai cần quyết định và chứng từ; không reset kho.

## 11. Backend Tasks

- [ ] Bổ sung repository query khóa bản ghi inventory theo store/SKU và xác định thứ tự khóa cho nhiều SKU.
- [ ] Tách calculateAvailable: trả actual-held khi invariant đúng; dữ liệu held>actual không được che bằng Math.max.
- [ ] Kiểm điều chỉnh quantity>=0 và >=held, reason bắt buộc; lấy employee từ principal, xác minh branch scope.
- [ ] Save adjustment history và stock trong một giao dịch; lỗi ở bất kỳ bước nào rollback cả hai.
- [ ] Bàn giao hold contract: input order hợp lệ, một store, danh sách SKU/qty>0; xác minh tất cả available rồi tăng held và lưu hold atomic.
- [ ] Bàn giao release contract theo order/hold: chỉ release HELD, cập nhật held/status/releasedAt atomic; retry không trừ held hai lần.
- [ ] Kiểm SKU/store tồn tại, active theo quy tắc; thiếu inventory không được silently bỏ qua khi hold.
- [ ] Giữ read-only lookup không tự tạo stock tùy tiện; tạo row mới trong receipt/transfer sẽ dùng contract sau.
- [ ] Áp error/rollback convention Phase01; test lỗi checked exception sau một item đã thay đổi.
- [ ] Ghi audit branch/SKU/before/after/reason/actor; giữ history adjustment hiện hữu.
- [ ] Ghi contract cho receipt/transfer/complete order, chưa triển khai lifecycle của chúng.

## 12. Frontend Tasks

- [ ] Sau D01, tạo tra cứu kho theo branch, SKU và trạng thái tồn với actual/held/available rõ ràng.
- [ ] Form điều chỉnh có số lượng mới, lý do và before/after preview từ server.
- [ ] Không đưa employeeId tùy ý vào form; branch selector chỉ gồm phạm vi được cấp.
- [ ] Hiển thị conflict tồn thay đổi đồng thời và cho reload trước retry.
- [ ] Có empty/error/loading states; không hiển thị dữ liệu nhạy cảm kho cho customer.
- [ ] Không làm stock-count, transfer, low-stock alert/export trong Phase08.

## 13. Validation & Business Rules

- actual>=0, held>=0 và available=actual-held>=0 theo QLTK-QĐ8.
- Điều chỉnh actual không được thấp hơn held đang bảo vệ đơn.
- Hold chỉ cho order đã hợp lệ với branch xác định; không giữ hàng chỉ vì thêm vào cart.
- Không một SKU thất bại nào được để các SKU trước đã hold/điều chỉnh.
- Release/completion retry không double-decrement; audit giữ timestamp/actor.
- Public availability là phép đọc, không làm reserve. Low-stock dùng available ở Phase15.

## 14. Security Requirements

- Nhân viên kho chỉ tra cứu/điều chỉnh branch được cấp; chain manager cần permission tương ứng.
- Customer không được gọi adjust, xem history actor hoặc public reserve/release mutation.
- Available public được Phase10 projection giới hạn; không mở mọi raw inventory route.
- Actor từ principal; không chấp nhận employeeId=1 hoặc ID do client chọn.

## 15. Error Handling

- Negative/below-held quantity: validation/conflict, không save history giả.
- Thiếu store/SKU/inventory: not found hoặc stock unavailable rõ.
- Insufficient stock và concurrent modification: conflict; transaction rollback.
- Invalid invariant legacy: thông báo cần đối soát, ghi kỹ thuật có ID nhưng không sửa số âm bằng clamp.
- Deadlock/timeout: retry theo convention được ghi, không lặp mutation mù.

## 16. Integration Points

- Phase04 identity/branch scope/audit; Phase06 SKU.
- Phase09 dùng read projection; Phase14 receipt và Phase16 transfer dùng stock mutation contract.
- Phase15 dùng available và adjustment; Phase19 dùng multi-SKU hold; Phase20–22 dùng release/complete.
- Shared InventoryService/Repository và OrderStockHoldEntity: không chỉnh song song khi contract đang thay đổi.
- Không yêu cầu Phase09 trước Phase08; StoreEntity/CRUD đã có đủ để test nền kho.

## 17. Implementation Order

1. Data audit invariant và test hiện hữu.
2. Migration timestamp/check được duyệt.
3. Repository locking + available/adjust service.
4. Atomic hold/release internal contract và regression rollback/concurrency.
5. Scoped REST/audit và UI theo D01.
6. Ghi contract cho các Phase consumer, kiểm chứng PostgreSQL.

## 18. Verification

- Giữ hai test InventoryServiceTest hiện hữu, thêm boundary held/actual và actor/permission.
- PostgreSQL hai transaction tranh cùng SKU, multi-SKU failure và release retry.
- Test rollback sau history save hoặc sau SKU đầu; không dựa H2 để kết luận lock behavior PostgreSQL.
- HTTP/UI adjust theo branch scope, field errors và conflict.
- Reconcile actual/held/hold rows sau happy path và thất bại.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P08-01 | Happy adjust | actual10/held3 → new12 + reason | actual12, available9, history đúng |
| P08-02 | Boundary | newQuantity=held | available0, hợp lệ |
| P08-03 | Invalid quantity | Âm hoặc actual mới<held | Reject, không history/stock thay đổi |
| P08-04 | Multi-SKU hold | Một SKU đủ, một thiếu | Rollback tất cả hold/held |
| P08-05 | Concurrent holds | Hai order tranh stock cuối | Không oversell, available không âm |
| P08-06 | Release retry | Release cùng hold hai lần | Trừ held một lần, status/time đúng |
| P08-07 | Permission | Staff branchA adjust branchB | 403, không mutation |
| P08-08 | Empty/legacy | Không inventory; held>actual | Unavailable/đối soát rõ, không clamp che lỗi |
| P08-09 | Failure atomicity | Lỗi sau save adjustment | Stock và history rollback cùng nhau |

## 20. Definition of Done

- [ ] UC49/51/54/55 có contract và test kiểm chứng.
- [ ] Invariant được bảo vệ, không còn clamp che lỗi.
- [ ] Adjust/hold/release atomic và retry không double mutation.
- [ ] PostgreSQL concurrency/rollback test có kết quả.
- [ ] Employee principal/branch scope/audit hoạt động.
- [ ] UI theo D01 và test lỗi/empty/boundary hoàn tất.
- [ ] Query low-stock sai được ghi cho Phase15, không bị quên.
- [ ] Contract handoff cho receipt/transfer/order rõ, không triển khai workflow Phase sau.

## 21. Expected Result After This Phase

Kho có số liệu nhất quán và thao tác nền an toàn cho store×SKU; staff điều chỉnh trong scope và giữ history đúng. Các Phase nhập/transfer/order có thể gọi contract này sau khi tích hợp lifecycle riêng.

## 22. Handoff To Next Phase

- Phase09/15 có thể dựa vào available chính xác; Phase14/16 nhận stock mutation contract.
- Phase19 có multi-SKU hold atomic; Phase20–22 nhận release và completion integration notes.
- Ghi chữ ký/service thực tế, cách khóa, isolation/rollback test và dữ liệu legacy unresolved.
- Existing routes inventory đã được bảo vệ; không giả định checkout/order status hoàn thiện từ Phase08.
- Low-stock query cần đổi actual→available trong Phase15 theo owner đã chỉ định.

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
