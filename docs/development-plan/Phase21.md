# Phase 21 — Quản lý và điều phối đơn cho nhân viên

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **HIGH** · Risk: **HIGH** · Depends On: [Phase04](Phase04.md), [Phase09](Phase09.md), [Phase19](Phase19.md)


## 1. Objective

Nhân viên đúng quyền và phạm vi có thể tra cứu, xác nhận, phân công chi nhánh và xử lý trạng thái/hủy đơn trên nền checkout đã an toàn.

## 2. Why This Phase Exists

OrderRestController đã có đọc/list/status/cancel nhưng mọi route đang công khai; updateStatus gán enum bất kỳ, oldStatus không dùng và chưa có branch reassignment. Không viết lại snapshot, phép tính checkout hay hold creation từ Phase19.

## 3. Current State

- Baseline có GET /api/orders, GET /api/orders/{id}, PUT /api/orders/{id}/status và PUT /api/orders/{id}/cancel; list dùng findAll và không phân trang.
- OrderStatus thực tế: PENDING_CONFIRMATION, CONFIRMED, PREPARING, SHIPPING, COMPLETED, CANCELLED. confirmedAt đã được gán; cancellation đã có release HELD nhưng thiếu workflow/locking/scope.
- OrderResponse thiếu assignedStore và các mốc trạng thái ngoài createdAt; chưa có màn hình staff. Trạng thái sau Phase19/20 phải kiểm tra lại trước sửa.

## 4. Preconditions

- Các Phase phụ thuộc [Phase04](Phase04.md), [Phase09](Phase09.md), [Phase19](Phase19.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D01: chốt công nghệ UI và bố cục staff; tài liệu UI không có mockup admin.
- Phase20 nếu đã hoàn thành: tái sử dụng chính sách customer cancellation và service chung; không tạo nhánh hủy độc lập.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC65–70: staff search/filter/page, detail, confirm, branch assignment, status và cancel có kiểm tra permission × branch.
- Hợp nhất guard chuyển trạng thái và thao tác hold cần cho thay chi nhánh/hủy; audit bằng writer Phase04.
- Bổ sung DTO staff thể hiện chi nhánh/mốc trạng thái cần thiết; UI staff có loading/empty/conflict states.

### OUT OF SCOPE

- Checkout/cart/voucher pricing của Phase18–19; customer screens Phase20.
- Shipping information edit, commit tồn khi COMPLETED, in/xuất đơn của Phase22.
- Split shipment, tích hợp hãng vận chuyển hoặc thanh toán online.

## 6. Requirements Covered

- System §3.4.8 UC65–70; §2.1.8 QLDH-QĐ2–5, §4.2: đủ hàng và một chi nhánh đủ toàn bộ dòng hàng.
- System §2.1.9 QLNV-QĐ4–5: permission, phạm vi cửa hàng và audit.
- UI: navigation tài khoản/đơn là tham chiếu luồng; không có mockup staff để khẳng định layout đã khớp.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [src/main/java/com/thinh/cosmetic/service/order/OrderService.java](../../src/main/java/com/thinh/cosmetic/service/order/OrderService.java)
- [src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderStockHoldRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- [src/main/java/com/thinh/cosmetic/domain/enums/OrderStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/OrderStatus.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java)
- [src/main/java/com/thinh/cosmetic/service/order/OrderService.java](../../src/main/java/com/thinh/cosmetic/service/order/OrderService.java)
- [src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/OrderResponse.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- DTO query/command staff cho assignment và status nếu DTO hiện tại chưa đáp ứng; giữ package domain/dto/order theo convention hiện có.
- Test workflow, scope, concurrency tại src/test/java/com/thinh/cosmetic/service/; UI staff chỉ tạo sau D01, trong cấu trúc frontend được chốt.

## 10. Database Changes

- NONE mặc định: assignedStore, status và các timestamps đã tồn tại.
- TECHNICAL RECOMMENDATION: chỉ thêm index phục vụ bộ lọc thực tế hoặc version nếu cơ chế khóa Phase08 cần; ghi migration sau kiểm tra PostgreSQL và dữ liệu hiện hữu, không đổi khóa chính.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Đọc lại authorization Phase04 và checkout/hold contract Phase19; loại customerId/employeeId do client tự khai khỏi quyền quyết định.
- [ ] Bổ sung query staff theo từ khóa/mã đơn, khoảng thời gian, status, chi nhánh với pagination; giới hạn chi nhánh trước khi query, không lọc sau khi tải toàn bộ.
- [ ] Detail phải kiểm tra scope trước khi trả customer/address/snapshot; DTO trả assignedStore và mốc cần hiển thị, không serialize entity.
- [ ] Xây guard state machine: PENDING_CONFIRMATION → CONFIRMED → PREPARING → SHIPPING → COMPLETED; CANCELLED/COMPLETED terminal; endpoint chung không vượt qua kiểm tra.
- [ ] Confirm kiểm tra trạng thái, địa chỉ snapshot đủ và holds đúng chi nhánh/đủ quantity; timestamp chỉ thiết lập một lần.
- [ ] Reassignment chỉ ở trạng thái cho phép trước SHIPPING; khóa order và inventory theo thứ tự Phase08, bảo đảm một chi nhánh mới đủ tất cả SKU, release old/create new holds trong một transaction.
- [ ] Staff cancel dùng cùng atomic release service của Phase20/19, không trừ held lần hai; không dùng Math.max để che invariant.
- [ ] Ghi audit actor, order, trạng thái/chi nhánh trước-sau và kết quả; từ chối giao dịch thiếu quyền trước mọi thay đổi.
- [ ] Giữ completion mutation thuộc Phase22; Phase21 không mở đường bỏ qua commit tồn an toàn nếu Phase22 chưa hoàn thành.

## 12. Frontend Tasks

- [ ] Tạo staff order list/detail theo D01: search, status/branch/time filter, pagination, loading/empty/error.
- [ ] Hiển thị snapshot sản phẩm/giá/địa chỉ và chi nhánh được gán; không dùng giá sản phẩm hiện tại để thay lịch sử.
- [ ] Confirm/reassign/status/cancel chỉ hiện theo permission và state; modal xác nhận cho thao tác thay chi nhánh/hủy, vẫn kiểm tra lại server.
- [ ] Gặp conflict dữ liệu thì reload order và thông báo rõ; disable submit khi đang xử lý không thay thế server locking.

## 13. Validation & Business Rules

- Không đổi giá/snapshot lịch sử khi xác nhận hoặc phân công cửa hàng.
- Chi nhánh phải ACTIVE và đủ available của tất cả dòng đơn; không chia đơn giữa nhiều cửa hàng.
- Staff branch scope áp dụng cả list, detail và mutation; quản lý chuỗi chỉ vượt scope nếu quyền Phase04 cho phép.
- Không quay lại terminal state; before SHIPPING cancellation đúng thiết kế. Completion side effects chuyển cho Phase22.

## 14. Security Requirements

- Customer không gọi staff list/assignment/status; nhân viên xử lý đơn có permission tương ứng và store scope.
- Anonymous nhận 401, thiếu permission/scope nhận 403 theo contract Phase01/04; không lấy role từ query/body.
- Session/CSRF hoặc token handling theo D01 và Phase02; bảo vệ cả REST và page route.

## 15. Error Handling

- Đơn không tồn tại: 404; chuyển trạng thái/stock cạnh tranh: 409; request sai enum/date/page: validation error theo Phase01.
- Nếu reassignment fail ở bất kỳ SKU nào, giữ nguyên toàn bộ old holds/assignedStore/timestamps; trả lỗi không lộ stack trace.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Identity/RBAC/audit Phase04; store Phase09; inventory atomic methods Phase08; checkout Phase19.
- Customer cancellation Phase20 dùng chung service; Phase22 nhận workflow/assignment để shipping/completion.

## 17. Implementation Order

1. Chốt D01 và đọc báo cáo completion các prerequisite.
2. Query và DTO staff + scope tests.
3. State guard và cancellation reuse.
4. Atomic branch reassignment + PostgreSQL failure/concurrency tests.
5. UI staff và audit verification, cập nhật handoff.

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
| P21-T01 | Happy path | Đơn PENDING_CONFIRMATION thuộc cửa hàng được phép | Confirm → CONFIRMED, giữ snapshot và gán timestamp một lần. |
| P21-T02 | Validation | status/date/page không hợp lệ | Lỗi theo contract; không sửa DB. |
| P21-T03 | Permission | Nhân viên cửa hàng B mở/sửa đơn A; customer gọi staff | 403, không rò detail/audit write. |
| P21-T04 | Empty data | Bộ lọc không có đơn | Page rỗng và empty UI, không 500. |
| P21-T05 | Invalid input | Order ID không tồn tại | 404; không tạo đơn mới. |
| P21-T06 | Boundary | Hủy PREPARING rồi thử SHIPPING | PREPARING được hủy đúng rule; SHIPPING bị chặn. |
| P21-T07 | Atomicity | Reassign sang cửa hàng thiếu SKU cuối | Old holds và assignedStore không đổi, không giữ một phần. |
| P21-T08 | Concurrency/retry | Hai staff confirm/reassign cùng đơn; gọi cancel lặp | Một kết quả hợp lệ hoặc conflict, không release/hold trùng. |

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

Đơn có luồng staff tra cứu/điều phối có scope và guard; completion stock/print còn thuộc Phase22.

## 22. Handoff To Next Phase

- Phase22 có thể dùng staff DTO, workflow guard và assignment đã kiểm chứng; status enum giữ nguyên.
- Ghi chính xác routes/query contract đã thêm, hold lock order và cách xử lý retry trong Completion Report; đây là kỳ vọng sau Phase, chưa phải API baseline.
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
