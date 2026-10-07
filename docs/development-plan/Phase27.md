# Phase 27 — Tra cứu audit log

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **MEDIUM** · Risk: **MEDIUM** · Depends On: [Phase04](Phase04.md)


## 1. Objective

Người có quyền tra cứu nhật ký hệ thống theo actor/action/object/time với pagination, bảo vệ dữ liệu và giữ tính bất biến của log.

## 2. Why This Phase Exists

Baseline AuditLogEntity/AutditLogRepository chưa có consumer hay endpoint. Writer/schema cần hoàn thành Phase04; Phase này chỉ xây tra cứu, không viết lại audit foundation hoặc event của từng module.

## 3. Current State

- AuditLogEntity baseline có action, performedBy String, details mặc định 255 và createdAt; không có Employee FK/object/IP như thiết kế.
- AutditLogRepository tên typo đúng source hiện tại, extends JpaRepository; chưa có query/route/UI.
- Phase04 dự kiến đã hoàn thiện schema/writer/permission; phải đọc Completion Report để biết tên repository và fields thực tế.

## 4. Preconditions

- Các Phase phụ thuộc [Phase04](Phase04.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D01: audit admin UI; permission read và phạm vi nhật ký được chốt Phase04.
- Actor/system/legacy log mapping từ Phase04 phải được ghi rõ; không giả tất cả log đều có Employee.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC87: list/search/filter/page/detail nhật ký hệ thống có quyền.
- Test writer integration tối thiểu bằng vài sự kiện thật, không tạo lại writer.
- Giới hạn thông tin sensitive trong response/detail, giữ log append-only theo contract Phase04.

### OUT OF SCOPE

- Không thêm log retention cleanup/export/SIEM hoặc observability vendor ngoài yêu cầu.
- Không refactor rename repository chỉ vì typo; dùng trạng thái sau Phase04.
- Không implement audit events thay cho Phase21–26, không giả DONE cho module chưa có events.

## 6. Requirements Covered

- System §3.4.10 UC87; §2.1.10 QLNV-QĐ5 và §4.1.2 AuditLog: truy actor/object/action/time.
- SYS10 timestamps và quyền §2.2; log không expose credentials theo NFR security.
- Không có mockup audit trong UI DOCX.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/account/AutditLogRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AutditLogRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java)
- [src/main/java/com/thinh/cosmetic/rest/account/EmployeeRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/EmployeeRestController.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/repository/account/AutditLogRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AutditLogRepository.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- Audit read-only DTO/service/controller và query projection trong các package account hiện hữu nếu Phase04 chưa tạo.
- Tests pagination/filter/permission và audit UI theo D01; writer/schema của Phase04 chỉ sửa nếu phát hiện blocker và ghi deviation.

## 10. Database Changes

- NONE: dùng schema AuditLog Phase04.
- TECHNICAL RECOMMENDATION: nếu query đo được cần index(createdAt/actor/action/object), thêm migration riêng sau kiểm tra cardinality; không thay log content hay purge lịch sử.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Đọc schema/writer/read permission contract Phase04 trước đặt query names; kiểm tra typo filename thực tế.
- [ ] Implement paginated read/filter theo actor/action/object/time được schema hỗ trợ, validate date/page/sort và order deterministic.
- [ ] Enforce audit permission/scope trong service trước query/detail; không tin employeeId/request branch để mở rộng quyền.
- [ ] Return DTO có thông tin cần thiết, redact/exclude password/token/secret và details chứa PII theo policy đã chốt.
- [ ] Không cung cấp mutation/delete endpoint cho logs; writer gọi từ workflow đã có, không expose public write.
- [ ] Query projection/read-only transaction; tránh N+1 actor lookups khi list và không fetch mọi logs để lọc.
- [ ] Kiểm tra vài workflow đã hoàn thành thực sự có event; thiếu event ghi ở Phase sở hữu, không âm thầm implement module khác.
- [ ] Document filter semantics, timezone và legacy actor representation cho AI tiếp theo.

## 12. Frontend Tasks

- [ ] Audit list với actor/action/object/time filters, pagination và detail có controlled sensitive display.
- [ ] Empty/loading/invalid range/permission states; hiển thị timestamp timezone rõ.
- [ ] Không thêm edit/delete log controls; keyboard/focus và responsive theo D01.

## 13. Validation & Business Rules

- Nhật ký phản ánh actor thật từ principal và sự kiện module, không client supplied identity.
- Read không đổi log/order/stock; append-only history.
- Date range inclusive/exclusive theo convention đã chốt, UI và API thống nhất.
- Không báo toàn hệ thống audit DONE khi chỉ query chạy nhưng workflows chưa ghi.

## 14. Security Requirements

- Chỉ administrator hoặc role được cấp audit-read trong Phase04; nhân viên/customer thông thường không truy cập.
- Scope và thông tin object không cho vượt quyền qua direct ID.
- Không log thêm query secrets hay trả raw details chứa token; bảo vệ UI route/REST cùng lúc.

## 15. Error Handling

- Invalid query/date/page: validation; log ID missing404; thiếu quyền401/403.
- DB/read failure trả lỗi sạch; không fallback public findAll hoặc expose stacktrace.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Phase04 writer/schema/RBAC; completed workflows cung cấp event; không phụ thuộc report modules.
- Có thể chạy song song các module nghiệp vụ sau Phase04, chỉ dùng read interface đã ổn định.

## 17. Implementation Order

1. Đọc Phase04 report/schema và chốt read policy.
2. Repository projection/filter + API contract.
3. Authorization/redaction/pagination tests.
4. UI audit và event integration sample.
5. Handoff read contract và event gaps cho owner phases.

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
| P27-T01 | Happy path | Audit-reader lọc actor/action/time | Đúng rows/detail, deterministic page. |
| P27-T02 | Validation | End trước start/page âm/sort không cho phép | Field validation. |
| P27-T03 | Permission | Customer hoặc employee không có audit-read | 403 list/detail. |
| P27-T04 | Empty data | Không có log hợp filter | Page rỗng và empty UI. |
| P27-T05 | Invalid input | ID log không tồn tại | 404. |
| P27-T06 | Boundary | Rows đúng biên time/page cuối | Không mất/lặp do semantics mơ hồ. |
| P27-T07 | Sensitive data | Details chứa dữ liệu cần che theo policy | DTO/HTML không lộ credentials/secret. |
| P27-T08 | Integration | Một workflow đã hoàn thành ghi audit | Log có actor/object thật; query không mutation. |
| P27-T09 | Immutability | Gọi method write/delete qua route read | Không có public mutation endpoint. |

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

UC87 có tra cứu audit an toàn; writer/events vẫn do Phase04 và từng module chịu trách nhiệm.

## 22. Handoff To Next Phase

- Các Phase còn lại không cần dựng audit UI/writer lần nữa; chỉ thêm events qua contract đã có.
- Ghi chính xác endpoint/filter/permission/redaction và mọi event gaps, không tự đánh dấu các workflow chưa làm.
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
