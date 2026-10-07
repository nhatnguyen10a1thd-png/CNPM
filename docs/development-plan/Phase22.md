# Phase 22 — Giao hàng, hoàn tất đơn và in/xuất đơn

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **HIGH** · Risk: **HIGH** · Depends On: [Phase08](Phase08.md), [Phase21](Phase21.md)


## 1. Objective

Hoàn tất phần shipping/completion của đơn, commit stock đúng một lần và xuất/in snapshot đơn có kiểm soát quyền.

## 2. Why This Phase Exists

Baseline COMPLETED đổi HELD→COMMITTED và trừ actual/held bằng Math.max(0,...), có thể che tồn sai; thiếu shipping edit và print/export. Cần nối workflow Phase21 với primitive kho Phase08.

## 3. Current State

- updateStatus đã có confirmedAt/completedAt/cancelledAt và nhánh commit holds; không kiểm tra oldStatus và bỏ qua inventory thiếu bằng ifPresent.
- OrderEntity có snapshot địa chỉ, tiền, items; OrderResponse nối address thành một string và không trả các mốc shipment.
- Chưa có UI vận hành giao hàng, route edit shipping, print/export. Không chứng minh workflow bằng một unit checkout test hiện có.

## 4. Preconditions

- Các Phase phụ thuộc [Phase08](Phase08.md), [Phase21](Phase21.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D01: frontend staff/print implementation được chốt.
- D15: cách hiển thị mã đơn thống nhất; Long generated ID đang có, không tự bắt buộc prefix #LN.
- D18: field/actor/allowed states của shipping updates và manual flow hay provider integration đã được xác nhận; không tự chọn carrier/tracking model.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC72, UC73, UC75: cập nhật thông tin giao, chuyển hoàn tất và in/xuất đơn.
- Commit inventory theo hold ledger Phase08, kiểm tra state/scope và audit; bảo toàn order snapshot.
- Print/export dùng snapshot và totals được lưu; format file tối thiểu theo nhu cầu đã chốt.

### OUT OF SCOPE

- Tính lại giá/voucher/shipping policy Phase18; tạo checkout Phase19.
- Tự động gọi payment/shipping provider hoặc chia kiện/chi nhánh.
- Return restock Phase23 và net refund accounting chưa có rule.

## 6. Requirements Covered

- System §3.4.8 UC72, UC73, UC75; QLDH-QĐ4,6 và §4.2: workflow, totals snapshot.
- System §2.1.6 QLTK-QĐ6–8: giữ hàng, release/commit và actual-held ≥ 0.
- UI checkout/success §5.3.7–8 tham chiếu snapshot tiền/địa chỉ; chưa có mockup staff hoặc bản in.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/enums/HoldStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/HoldStatus.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java)
- [src/main/java/com/thinh/cosmetic/service/store/InventoryService.java](../../src/main/java/com/thinh/cosmetic/service/store/InventoryService.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/service/order/OrderService.java](../../src/main/java/com/thinh/cosmetic/service/order/OrderService.java)
- [src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- Shipping update DTO và print/export renderer/service nhỏ trong module order nếu cần; không tạo hệ thống vận chuyển mới.
- Tests completion/print tại src/test/java/com/thinh/cosmetic/service/; UI shipping/detail/print view sau D01.

## 10. Database Changes

- NONE mặc định: địa chỉ và completedAt đã có.
- Nếu yêu cầu đã chốt cần field giao hàng mới mà snapshot chưa biểu diễn được, ghi rõ field/nullable/backfill trước migration; không tự thêm tracking provider hoặc payment status.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Đọc lại guard Phase21, inventory methods/lock order Phase08; không copy đoạn commit tồn thành service thứ hai.
- [ ] Bổ sung command cập nhật recipient/phone/address phù hợp validation Phase11; kiểm tra scope/state, audit giá trị trước-sau; không sửa customer saved address ngầm.
- [ ] Chuyển SHIPPING và COMPLETED qua guard; completion chỉ từ SHIPPING, kiểm tra all HELD tương ứng order items và assignedStore.
- [ ] Atomically đổi HELD→COMMITTED, giảm actual và held đúng quantity; từ chối thiếu inventory/held/actual thay vì clamp hoặc ifPresent bỏ qua.
- [ ] Set completedAt đúng một lần, không thay timestamp/stock khi replay đã COMPLETED; thống nhất trả trạng thái hiện tại hoặc conflict theo contract Phase21.
- [ ] Lỗi ở dòng cuối phải rollback tất cả dòng, holds, order state và audit transaction; dùng cơ chế rollback đã hoàn thành Phase01.
- [ ] Tạo read-only print/export lấy snapshot items/address/totals, branch và mã đã chốt; enforce scope trước render.
- [ ] Không recalculate discount/shipping từ voucher hoặc catalog đang thay đổi; tránh expose account credentials/beauty profile vào file.

## 12. Frontend Tasks

- [ ] Staff detail có form shipping rõ field errors, nút SHIPPING/COMPLETED đúng state và modal xác nhận.
- [ ] Hiển thị updated/completed state sau server response; conflict reload dữ liệu, không lạc quan trừ hàng trên browser.
- [ ] Print/export có bố cục dễ đọc, số tiền VND và mã D15; render theo snapshot, xử lý đơn nhiều dòng và trang dài.
- [ ] Trình bày lưu/giao/hoàn tất fail rõ ràng, keyboard/focus và responsive theo D01.

## 13. Validation & Business Rules

- Completed order chỉ một lần commit stock; actual ≥ held ≥ 0 sau mọi mutation.
- Không sửa trạng thái terminal hoặc sửa lịch sử để phục vụ print; customer không tự complete.
- Shipping edit không thay price snapshot/totals; thay phí chỉ theo pricing policy đã chốt, ngoài scope nếu chưa có yêu cầu.
- Bản in/export phản ánh đơn đã lưu, không phải cart hiện tại.

## 14. Security Requirements

- Nhân viên xử lý đơn đúng permission và store scope được shipping/complete/print; customer chỉ own read contracts Phase20.
- Scope kiểm tra ở service cả export; không dùng URL print để vượt authorization.
- Không cache file chứa PII ở vị trí public; xử lý CSRF/token theo Phase02.

## 15. Error Handling

- Thiếu/sai hold hoặc inventory: 409 + rollback; shipping field không hợp lệ: validation; order không có: 404.
- Export failure trả lỗi sạch, không file nửa chừng hoặc HTML stacktrace; retry export không mutation.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Phase21 state/branch/scoped detail; Phase08 inventory/holds; Phase19 order snapshots.
- Phase23 dựa completed items để return; Phase28 dựa status/completedAt để revenue; notification chỉ Phase30.

## 17. Implementation Order

1. Kiểm tra contract staff/inventory và quyết định mã/print UI.
2. Shipping command + validation/audit.
3. Atomic completion + repeat/concurrency/rollback tests trên PostgreSQL.
4. Read-only print/export và UI; render thử đơn dài.
5. Ghi trạng thái/mốc/ledger contract cho returns/reports.

## 18. Verification

- [ ] Java 21: build và chạy tests phù hợp thay đổi bằng Maven wrapper; giữ baseline tests đang pass.
- [ ] PostgreSQL test database riêng: startup/schema/migration và query/mutation thực tế. Không lấy H2 pass thay bằng chứng PostgreSQL.
- [ ] HTTP/page routes: kiểm tra success, error contract, principal/permission/scope/ownership với các actor liên quan.
- [ ] UI: dữ liệu thật qua server, loading/empty/error/validation, navigation, responsive và permission states; staff layout đối chiếu use case, không tuyên bố matched mockup không tồn tại.
- [ ] Mutation có nhiều dòng/side effects: kiểm tra rollback, replay và concurrent request khi liên quan; read-only report kiểm tra không mutation và số liệu fixture.
- [ ] Kiểm tra regression capability prerequisite; ghi command/config test, dữ liệu thử và kết quả thực trong report, không ghi PASS khi chưa chạy.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P22-T01 | Happy path | SHIPPING đơn đủ holds | COMPLETED, actual/held giảm đúng; completedAt có. |
| P22-T02 | Validation | Recipient phone/address trống/sai | Không lưu; field errors rõ. |
| P22-T03 | Permission | Customer hoặc staff khác branch complete/print | 401/403 và không rò file. |
| P22-T04 | Empty data | Danh sách detail/export không có items do dữ liệu lỗi | Từ chối có lý do, không tạo completion giả. |
| P22-T05 | Invalid input | Order ID không tồn tại hoặc PREPARING→COMPLETED | 404 hoặc 409. |
| P22-T06 | Boundary | actual=held=quantity | Sau complete actual=held=0, không âm. |
| P22-T07 | Atomicity | Inventory dòng cuối không khớp | Tất cả stock/hold/order/timestamp rollback. |
| P22-T08 | Concurrency/retry | Hai completion requests + retry | Không double deduct; timestamp không đổi lần hai. |
| P22-T09 | History | Đổi giá catalog sau đặt hàng rồi in | Bản in vẫn price/totals snapshot. |

## 20. Definition of Done

- [ ] Các gate/preconditions chặn Phase đã được giải quyết và ghi quyết định.
- [ ] Tasks trong scope hoàn thành; không làm phần out of scope.
- [ ] Build/startup và tests liên quan pass trên cấu hình được ghi; PostgreSQL integration có bằng chứng.
- [ ] Rules/validation/error handling/authorization đúng, không chỉ happy path.
- [ ] UI/function integration thực và use case đạt; layouts thiếu mockup được ghi đúng nguồn tham chiếu.
- [ ] Database và historical records đúng; migration/rollback được kiểm chứng nếu có.
- [ ] Không phá prerequisite, không duplicate service/rules/export/writer đang hoạt động.
- [ ] Audit/Master coverage, checkboxes và Completion Report cập nhật chính xác; còn blocker thì BLOCKED, không COMPLETED.

## 21. Expected Result After This Phase

Shipping/completion và print/export chạy có quyền; tồn đơn hoàn tất đúng ledger, dữ liệu đủ cho returns và reports.

## 22. Handoff To Next Phase

- Phase23 nhận order COMPLETED, item quantity/snapshot và stock commit đã kiểm chứng.
- Phase28 dùng completedAt/status/totals đã lưu; ghi route xuất và response timestamps thực tế, không giả định các field chưa thêm.
- Handoff liệt kê API/route/DTO/entity/screen thật đã tạo hoặc giữ, permission, DB migration và test results; chỉ các mục được kiểm chứng mới được Phase sau giả định tồn tại.

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

Chưa thực hiện. Các vấn đề baseline/gate được ghi ở Current State, Preconditions và Project Audit.

## Remaining Tasks

Toàn bộ checklist Phase này chưa được thực hiện.

## Verification Result

Chưa thực hiện Phase này. Kết quả baseline audit không thay thế verification của Phase.

## Notes For Next Phase

Chưa có handoff implementation; đọc prerequisite Completion Report trước bắt đầu.
