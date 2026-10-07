# Phase 23 — Đổi trả và xử lý tồn từ hàng trả

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **HIGH** · Risk: **HIGH** · Depends On: [Phase04](Phase04.md), [Phase11](Phase11.md), [Phase20](Phase20.md), [Phase22](Phase22.md)


## 1. Objective

Customer và nhân viên có thể tạo/xử lý return đúng chính sách được chốt, có ownership, quantity ledger và stock disposition an toàn.

## 2. Why This Phase Exists

ReturnServiceImpl đã lưu header/items và status nhưng chưa kiểm tra owner/completed/item membership/quantity, processedBy có thể null và không có restock. UC26/62/63/74 không thể xem DONE chỉ vì entity tồn tại.

## 3. Current State

- POST/GET /api/returns và PUT /api/returns/{id}/status đang tồn tại; customerId/employeeId chưa lấy từ principal.
- create lưu header trước khi đọc toàn bộ OrderItem; checked Exception ở dòng sau có nguy cơ partial commit.
- ReturnStatus có REQUESTED, APPROVED, REJECTED, PROCESSING, COMPLETED; updateStatus gán bất kỳ và chỉ ghi processedAt/employee.

## 4. Preconditions

- Các Phase phụ thuộc [Phase04](Phase04.md), [Phase11](Phase11.md), [Phase20](Phase20.md), [Phase22](Phase22.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D07 phải chốt eligibility, thời hạn nếu có, refund/đổi hàng, transition permissions và disposition nhập lại/không nhập lại; không tự đặt 7/14/30 ngày.
- D01: customer/staff return UI; thiết kế UI không có return mockup chi tiết.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC26, UC62, UC63, UC74: tạo, xem, tiếp nhận, xử lý và nhập lại hàng theo quyết định D07.
- Owner/branch permission, positive quantity, order item membership, tổng quantity đã yêu cầu/xử lý và idempotent stock mutation.
- Giữ history và lý do/actor/time; migration tối thiểu để biểu diễn disposition được chốt.

### OUT OF SCOPE

- Không thêm payment gateway, tự chuyển khoản, kế toán refund hoặc tự tạo replacement order khi D07 chưa yêu cầu.
- Không rewrite order checkout/completion; không đặt chính sách bồi thường mới.
- CSKH general support history thuộc Phase25.

## 6. Requirements Covered

- System §3.4.1 UC26, §3.4.7 UC62–63, §3.4.8 UC74; KH-QĐ14, QLKH-QĐ3, QLDH-QĐ7.
- System §4.1.2 ReturnRequest/ReturnItem/OrderItem và §4.2: quantity/history/relationships.
- Màn hình customer và staff phát sinh từ use case; không có mockup return trong tám UI screen.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/rest/returns/ReturnRestController.java](../../src/main/java/com/thinh/cosmetic/rest/returns/ReturnRestController.java)
- [src/main/java/com/thinh/cosmetic/service/returns/ReturnService.java](../../src/main/java/com/thinh/cosmetic/service/returns/ReturnService.java)
- [src/main/java/com/thinh/cosmetic/service/returns/impl/ReturnServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/returns/impl/ReturnServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/request/returns/ReturnRequestDto.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/returns/ReturnRequestDto.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnRequestEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnRequestEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnItemEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/returns/ReturnRequestRepository.java](../../src/main/java/com/thinh/cosmetic/repository/returns/ReturnRequestRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/returns/ReturnItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/returns/ReturnItemRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/enums/ReturnStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/ReturnStatus.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderItemEntity.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/rest/returns/ReturnRestController.java](../../src/main/java/com/thinh/cosmetic/rest/returns/ReturnRestController.java)
- [src/main/java/com/thinh/cosmetic/service/returns/ReturnService.java](../../src/main/java/com/thinh/cosmetic/service/returns/ReturnService.java)
- [src/main/java/com/thinh/cosmetic/service/returns/impl/ReturnServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/returns/impl/ReturnServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/request/returns/ReturnRequestDto.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/returns/ReturnRequestDto.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnRequestEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnRequestEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnItemEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/returns/ReturnItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/returns/ReturnItemRepository.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- Tests ownership/quantity/workflow/restock và UI return sau D01.
- Migration và DTO xử lý disposition chỉ sau D07; model mới nếu đã phê duyệt và entity hiện tại không đủ.

## 10. Database Changes

- Sau D07: bổ sung tối thiểu thông tin disposition/đã restock cần chống lặp; mô tả FK/constraint/nullable/backfill trong Completion Report trước triển khai.
- reason bắt buộc và quantity >0 theo thiết kế; khảo sát dữ liệu lỗi trước constraint. Pair uniqueness nếu contract không cho duplicate dòng cùng request; không đổi surrogate PK chỉ để giống diagram.
- Customer FK trực tiếp so với suy ra qua Order là điểm đối chiếu; quyết định có bằng chứng, không tự duplication/backfill thiếu nguồn.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Dừng phần policy-specific khi D07 mở; khóa policy version/quy tắc được duyệt trong report, không hardcode thời hạn tự đoán.
- [ ] Validate toàn bộ nested items trước persistence: nonempty, positive, item thuộc đúng order, không duplicate trái contract, reason hợp lệ.
- [ ] Customer principal sở hữu order; order đủ điều kiện D07 và COMPLETED theo workflow; staff tiếp nhận phải đúng permission/branch.
- [ ] Tính remaining returnable quantity từ ordered quantity và các return liên quan theo trạng thái D07; khóa/cạnh tranh để hai request không vượt lượng mua.
- [ ] Create header+items atomically; lỗi bất kỳ item phải không lưu header/items dở; detail/list enforce own customer hoặc staff scope.
- [ ] Guard ReturnStatus theo transition D07, processedBy lấy từ principal; không cho client giả employee hoặc đổi enum bất kỳ.
- [ ] Chỉ restock hàng có disposition cho phép, tại chi nhánh đã chốt; quantity bounded và mutation đúng một lần qua InventoryService Phase08.
- [ ] Audit request/approve/reject/process/complete/restock gồm actor, lý do, trạng thái và quantity; giữ chứng từ lịch sử.
- [ ] Nếu refund/đổi hàng đã chốt nhưng chưa có nguồn dữ liệu đủ, ghi BLOCKED đúng gate; không giả completion hoàn tiền.

## 12. Frontend Tasks

- [ ] Customer own order detail cho chọn eligible items, remaining qty và reason; hiển thị rule D07 rõ ràng.
- [ ] Customer list/detail return và trạng thái; staff queue/detail với role actions, disposition/reason và confirm modal.
- [ ] Không cho quantity vượt remaining ở UI; server vẫn authoritative. Reload conflict/stock error, không báo thành công trước commit.
- [ ] Empty/loading/validation/permission states; không tự tạo UI chat hoặc payment controls.

## 13. Validation & Business Rules

- Không trả SKU/dòng của đơn khác; không tạo return cho order chưa đủ điều kiện.
- Tổng quantity theo policy không vượt quantity đã mua; restock không đồng nghĩa mọi return đều nhập lại.
- Các mốc/actor không bị overwrite vô nghĩa khi replay; rejected/terminal transition theo D07.
- Order snapshot và customer purchase history không bị xóa khi return.

## 14. Security Requirements

- Customer chỉ tạo/xem return của mình; CSKH tiếp nhận và staff xử lý/nhập kho theo permissions Phase04.
- Branch scope đối với order/return/restock; chain role vượt scope chỉ khi đã được cấp quyền.
- Bỏ employeeId default=1 và trust customerId path; log PII tối thiểu, không lộ dữ liệu khách khác.

## 15. Error Handling

- 404 resource không tồn tại; 403 ownership/scope; 409 eligibility/remaining/state/stock conflict; nested field validation theo Phase01.
- Restock hoặc audit transaction fail phải rollback toàn return state và inventory; message không lộ stacktrace.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Phase20 order ownership; Phase22 completed stock ledger; Phase11 customer/addresses; Phase04 identity/audit.
- Inventory primitives Phase08 được tái sử dụng; Phase25 chỉ gọi command return của Phase23, reports không tự suy ra refund.

## 17. Implementation Order

1. Chốt D07 và dữ liệu/model migration tối thiểu.
2. Eligibility/remaining calculation + owner/nested validation.
3. Atomic create và state machine.
4. Idempotent disposition/restock với PostgreSQL concurrency tests.
5. Customer/staff UI, audit và handoff cho CSKH/reports.

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
| P23-T01 | Happy path | Owner, COMPLETED, item eligible, qty hợp lệ | REQUESTED có đầy đủ items/reason; staff xử lý theo D07. |
| P23-T02 | Validation | Qty 0/âm, list rỗng, reason trống | Không tạo header/items. |
| P23-T03 | Permission | Customer khác hoặc staff ngoài branch | 403 cho read/create/process. |
| P23-T04 | Empty data | Không có eligible items/returns | UI trạng thái rỗng, không giả lỗi. |
| P23-T05 | Invalid input | OrderItem thuộc order khác/không tồn tại | Reject toàn request, không lưu một phần. |
| P23-T06 | Boundary | Requested qty bằng remaining rồi thêm 1 | Bằng được chấp nhận; vượt bị từ chối. |
| P23-T07 | Atomicity | Item cuối sai hoặc restock dòng cuối fail | Header/items/state/inventory không commit một phần. |
| P23-T08 | Concurrency/retry | Hai request cùng remaining; complete/restock replay | Không vượt ordered quantity, không cộng stock lần hai. |
| P23-T09 | Policy | Disposition không nhập lại | Return có thể xử lý theo D07 nhưng inventory không tăng. |

## 20. Definition of Done

- [ ] Các gate/preconditions chặn Phase đã được giải quyết và ghi quyết định.
- [ ] Build/startup và tests liên quan pass trên cấu hình được ghi; PostgreSQL integration có bằng chứng.
- [ ] Rules/validation/error handling/authorization đúng, không chỉ happy path.
- [ ] UI/function integration thực và use case đạt; layouts thiếu mockup được ghi đúng nguồn tham chiếu.
- [ ] Database và historical records đúng; migration/rollback được kiểm chứng nếu có.
- [ ] Không phá prerequisite, không duplicate service/rules/export/writer đang hoạt động.
- [ ] Audit/Master coverage, checkboxes và Completion Report cập nhật chính xác; còn blocker thì BLOCKED, không COMPLETED.

## 21. Expected Result After This Phase

Luồng return có policy được duyệt, quyền và ledger quantity/restock kiểm chứng; không tuyên bố refund nếu chưa thực hiện.

## 22. Handoff To Next Phase

- Phase25 tiếp nhận/process bằng API/service return này, không copy business rules.
- Phase28/29 ghi đúng ý nghĩa status/disposition; quyết định accounting ngoài thiết kế phải còn gate, không tự trừ refund khỏi doanh thu.
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
