# Phase 16 — Điều chuyển kho an toàn giữa cửa hàng

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase08](Phase08.md), [Phase09](Phase09.md). Complexity: MEDIUM. Risk: HIGH.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Gia cố luồng điều chuyển PENDING → IN_TRANSIT → RECEIVED, đảm bảo xuất từ available stock và mỗi bước tác động kho đúng một lần, có màn hình nhân viên.

## 2. Why This Phase Exists

Happy path điều chuyển và state guards đã tồn tại. Ship hiện kiểm tra actualStock thay vì available nên có thể chuyển hàng đang giữ cho đơn; input, actor/scope, rollback và concurrent transition còn thiếu.

## 3. Current State

- StockTransferServiceImpl.create ghi header trước kiểm tra hết SKU, createdBy có thể null.
- confirmShipment chỉ chấp nhận PENDING; giảm actual ở source nhưng kiểm tra actual, không trừ held.
- confirmReceipt chỉ chấp nhận IN_TRANSIT; cộng actual destination, có thể tạo inventory với minimum=5.
- DTO items thiếu @Valid cascade/positive quantity; chưa kiểm tra source != destination.
- Route POST/GET /api/stock-transfers, GET /{id}, PUT /{id}/ship, PUT /{id}/receive đã có. Không viết lại toàn bộ state machine.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase08 có primitive kho/locks/rollback và convention inventory mới.
- [ ] Phase09 có cửa hàng, trạng thái và scope; Phase04 được hoàn thành qua dependency.
- [ ] D01 đã chốt staff layout; dữ liệu test có source/destination với held stock.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Validation phiếu, kiểm tra actor/source/destination, transaction create.
- Gia cố ship/receive dưới lock, available check và exactly-once mutation.
- Danh sách/chi tiết/tạo/xuất/nhận điều chuyển, có status/time và scoped search.

### OUT OF SCOPE

- Không thêm cancellation/partial delivery/damaged transfer workflow chưa có trong thiết kế.
- Không giữ kho từ cart hoặc lập đơn ở Phase này.
- Stocktake/nhập hàng/báo cáo kho là Phase15/14/28.

## 6. Requirements Covered

- §3.4.6 UC53; đây là Phase owner.
- QLTK-QĐ5: PENDING → IN_TRANSIT → RECEIVED, xuất giảm source, nhận tăng destination.
- QLTK-QĐ8: actual - held ≥ 0; QLNV-QĐ4: role và phạm vi cửa hàng.
- §4.1.2 StockTransfer/StockTransferItem/Inventory; SYS09/SYS10 kiểm tra mã và timestamps hiện có.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [StockTransferServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/StockTransferServiceImpl.java)
- [StockTransferRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/StockTransferRestController.java)
- [StockTransferRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/store/StockTransferRequest.java)
- [StockTransferEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StockTransferEntity.java)
- [StockTransferItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StockTransferItemEntity.java)
- [StockTransferRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/StockTransferRepository.java)
- [StockTransferItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/StockTransferItemRepository.java)
- [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [StockTransferStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/StockTransferStatus.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [StockTransferServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/StockTransferServiceImpl.java)
- [StockTransferRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/StockTransferRestController.java)
- [StockTransferRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/store/StockTransferRequest.java)
- [StockTransferRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/StockTransferRepository.java)
- [StockTransferService.java](../../src/main/java/com/thinh/cosmetic/service/store/StockTransferService.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- Unit/PostgreSQL integration tests cho available, ship/receive concurrency và multi-item rollback.
- UI điều chuyển theo D01 và DTO filter/list nếu contract hiện tại chưa đáp ứng.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

Giữ StockTransfer/Item và các timestamps hiện có. Nếu thiếu, thêm unique(stock_transfer_id, sku_id), CHECK quantity > 0 qua migration sau kiểm kê duplicate và quyết định normalize lines; không đổi PK. Lock dùng convention Phase08; không tự thêm bảng vận chuyển/tracking. D15 phải được chốt trước khi thêm human-readable mã.

## 11. Backend Tasks

- [ ] Bổ sung @Valid từng item, quantity > 0, list không rỗng, SKU tồn tại, source khác destination và cửa hàng hợp lệ cho phiếu mới.
- [ ] Loại default employeeId; lấy actor từ principal, kiểm tra quyền lập/xuất tại source và nhận tại destination.
- [ ] Kiểm tra tất cả dòng trước lưu; giữ create PENDING không tác động actual/held.
- [ ] Khóa transfer và các inventory theo thứ tự ổn định; ship chỉ dùng actual - held, không chuyển hàng đang giữ.
- [ ] Giữ existing PENDING/IN_TRANSIT guards; bảo vệ request đồng thời để ship/receive không cập nhật hai lần.
- [ ] Receive dùng primitive tăng actual và tạo inventory theo convention Phase08; held ở hai cửa hàng giữ nguyên.
- [ ] Lỗi SKU/dòng sau/constraint rollback toàn bước cùng status/timestamp; không để nửa phiếu đã xuất.
- [ ] Thêm search/filter/pagination scoped list, response đủ source/destination IDs, tên, status, actor và timestamps.
- [ ] Ghi audit create/ship/receive; không cho endpoint gọi state enum tùy ý ngoài hai transition thiết kế.

## 12. Frontend Tasks

- [ ] Form điều chuyển chọn source/destination được phép, SKU và quantity; source=destination bị báo lỗi.
- [ ] Danh sách/chi tiết hiển thị trạng thái, dòng, available nguồn, created/shipped/received time.
- [ ] Nút ship chỉ PENDING và đúng quyền source; receive chỉ IN_TRANSIT và đúng quyền destination.
- [ ] Confirm hành động, disable đang gửi; stale response refresh dữ liệu, không tự đánh dấu thành công.
- [ ] Có loading/empty/error/filter/page; xử lý available thay đổi từ lúc mở form.

## 13. Validation & Business Rules

- PENDING chưa đổi kho; ship giảm source actual; receive tăng destination actual; held không đổi.
- Available nguồn phải đủ từng SKU, invariant actual ≥ held sau ship.
- Ship/receive một lần, không skipping PENDING trực tiếp RECEIVED.
- Lịch sử vẫn truy vết cửa hàng/actor; không xóa transfer sau giao dịch.

## 14. Security Requirements

- Kho/manager được lập/xuất theo quyền source; nhận theo quyền destination. Ma trận cụ thể tái sử dụng Phase04.
- Một người có quyền kho tại A không tự có quyền receive tại B.
- Customer/anonymous bị từ chối mọi API điều chuyển; list/detail scope không lộ giao dịch cửa hàng ngoài quyền.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Source=destination, list rỗng, quantity không dương, duplicate SKU không được normalize → validation.
- Không đủ available hoặc status không hợp lệ → conflict; không giảm kho dòng khác.
- Missing inventory tại source là lỗi nghiệp vụ, không coi stock=0 rồi ship; destination creation xử lý duplicate race.
- Lock/persistence failure rollback và giữ trạng thái cũ, dùng error contract Phase01.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Dùng primitive kho Phase08, cửa hàng Phase09 và audit/RBAC Phase04.
- Order giữ hàng Phase19 cần cùng lock convention để transfer không vượt available.
- Phase28 chỉ đọc kết quả inventory; Phase16 không tạo báo cáo dashboard.

## 17. Implementation Order

1. Kiểm tra current guards, inventory primitive và scope source/destination.
2. Hoàn thiện create validation/rollback và contract list/detail.
3. Gia cố ship/receive locks, available và race creation inventory.
4. Viết transaction/concurrency tests rồi nối staff UI.
5. Kiểm chứng before/after hai cửa hàng và handoff.

## 18. Verification

- [ ] Ship actual10/held6 qty5 bị chặn dù actual đủ; qty4 hợp lệ và available về0.
- [ ] Receive tăng actual đúng qty, giữ held và timestamp/status đúng.
- [ ] Hai request cùng ship/receive không double mutation.
- [ ] Dòng2 lỗi rollback dòng1 và status; tạo header invalid SKU không để dữ liệu dở dang.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P16-01 | Happy transfer | A actual10/held2, ship3, receive B | A actual7/held2; B actual+3; RECEIVED |
| P16-02 | Held boundary | A actual10/held6, qty5 rồi qty4 | 5 bị chặn; 4 thành công available0 |
| P16-03 | Invalid input | source=dest, qty0/-1, items=[] | Không tạo phiếu; field lỗi |
| P16-04 | Late failure | Dòng2 thiếu available sau dòng1 | Toàn bước rollback; status không đổi |
| P16-05 | Transition | Receive PENDING hoặc ship RECEIVED | Conflict; không mutation |
| P16-06 | Concurrent duplicate | Hai ship hoặc receive cùng ID | Một mutation, không double stock |
| P16-07 | Permission | Kho A receive tại B không được cấp quyền | Từ chối; không lộ dữ liệu ngoài scope |
| P16-08 | Empty list | Filter không khớp | Page rỗng/UI empty state |
| P16-09 | Destination missing row | Receive SKU chưa có inventory | Tạo một row đúng convention, không duplicate |

## 20. Definition of Done

- [ ] UC53 chạy end-to-end, state guards cũ được giữ và race được kiểm chứng.
- [ ] Available check bảo vệ held stock; mỗi transition nguyên tử/đúng một lần.
- [ ] Scope source/destination, actor/audit/timestamps và UI đúng.
- [ ] Không thêm workflow ngoài thiết kế hoặc bảng tracking mới.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Điều chuyển an toàn giữa cửa hàng, không lấy lượng giữ cho đơn và không cập nhật một phần/hai lần. Nhân viên theo dõi được status/time/actor qua UI.

## 22. Handoff To Next Phase

- Phase19 phối hợp cùng inventory lock convention với điều chuyển.
- Bàn giao exact routes /api/stock-transfers/{id}/ship và /receive cùng error/status contract.
- Ghi testcase held boundary/concurrent mutation để Phase32 chạy regression.
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
