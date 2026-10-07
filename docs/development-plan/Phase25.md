# Phase 25 — CSKH tra cứu khách và lịch sử hỗ trợ

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **HIGH** · Risk: **HIGH** · Depends On: [Phase04](Phase04.md), [Phase11](Phase11.md), [Phase20](Phase20.md), [Phase23](Phase23.md)


## 1. Objective

CSKH có quyền tra cứu khách, xem hồ sơ/lịch sử mua, khóa tài khoản và ghi/xem lịch sử hỗ trợ theo đặc tả đã chốt.

## 2. Why This Phase Exists

CustomerService hiện có profile/list nhưng không search/pagination/lock/support. Return đã có Phase23 nên CSKH phải tái sử dụng để tránh hai workflow khác nhau.

## 3. Current State

- GET /api/customers và /{id}, PUT /{id}/profile đang có; list dùng findAll, thiếu scope và phone trong response cần kiểm tra lại Phase11.
- Account baseline chưa có trạng thái lock; Phase02/04 phải cung cấp account status/identity enforcement trước Phase này.
- Không có support history entity/repository/service; UC64 không được đánh dấu đã làm bằng customer profile.

## 4. Preconditions

- Các Phase phụ thuộc [Phase04](Phase04.md), [Phase11](Phase11.md), [Phase20](Phase20.md), [Phase23](Phase23.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D09: UC64 cần model/fields/workflow hỗ trợ được duyệt; không tự tạo ticket/chat hoặc SLA.
- D01: CSKH layout; admin/staff screens không có UI mockup. Quyền đọc theo customer/branch phải theo ma trận Phase04.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC58–61, UC64: lookup/profile/history/lock/support history.
- Search/filter/page, dữ liệu tối thiểu cần phục vụ hỗ trợ, account status changes có audit.
- CSKH return actions gọi module Phase23 như integration, không sở hữu lại UC62–63.

### OUT OF SCOPE

- Không rewrite self-service profile/addresses/beauty/wishlist Phase11–12.
- Không dựng chat, ticketing, email marketing, loyalty rules mới hoặc CRM ngoài thiết kế.
- Không xử lý tiền refund riêng; return policy/commands thuộc Phase23.

## 6. Requirements Covered

- System §3.4.7 UC58–61, UC64; §2.1.7 QLKH-QĐ1–3: permission, khóa không xóa history, return chi tiết.
- System §4.1.2 Account/Customer/Order và §2.1.9 role/scope; support model thiếu là D09.
- UI navigation account chỉ gợi luồng customer; không có CSKH mockup.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java)
- [src/main/java/com/thinh/cosmetic/service/account/CustomerService.java](../../src/main/java/com/thinh/cosmetic/service/account/CustomerService.java)
- [src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/account/CustomerResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/CustomerResponse.java)
- [src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java)
- [src/main/java/com/thinh/cosmetic/mapper/account/CustomerMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/account/CustomerMapper.java)
- [src/main/java/com/thinh/cosmetic/service/returns/ReturnService.java](../../src/main/java/com/thinh/cosmetic/service/returns/ReturnService.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java)
- [src/main/java/com/thinh/cosmetic/service/account/CustomerService.java](../../src/main/java/com/thinh/cosmetic/service/account/CustomerService.java)
- [src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/account/CustomerResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/CustomerResponse.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- CSKH-specific query/response/lock DTO và tests nếu self-service DTO không nên chứa thông tin staff.
- Support history entity/repository/service/migration chỉ sau D09; UI CSKH theo D01.

## 10. Database Changes

- NONE cho lookup/history/lock nếu Account status và audit schema đã có từ Phase02/04.
- Support history schema chỉ lập sau D09: xác định nguồn customer/employee FKs và retention được duyệt; không tự giả ticket table.
- Index search chỉ dựa query thật; migration phải giữ profile/orders/returns, không xóa tài khoản để khóa.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Đọc lại customer self-service Phase11, account status/principal Phase02/04 và return contracts Phase23.
- [ ] Tạo CSKH paginated lookup bằng identifier/name/email/phone phù hợp quyền; validation query/page/date; không dùng getAll rồi lọc toàn bộ.
- [ ] Detail trả profile/contact cần thiết và purchase history qua scoped order read service; snapshot tiền/status lịch sử giữ nguyên.
- [ ] Staff profile changes chỉ fields được quyền, reuse validation/uniqueness Phase11; không mở quyền sửa password/roles bằng DTO customer.
- [ ] Lock/unlock account theo permission được chốt, invalidate/chặn phiên/token hiện tại bằng cơ chế Phase02; audit lý do/actor/time.
- [ ] Không xóa customer/orders/reviews/returns khi lock; điều kiện đọc lịch sử giữ theo quyền staff.
- [ ] Thiết kế/lưu/tra cứu lịch sử hỗ trợ theo D09; nếu gate mở hoàn thành độc lập lookup nhưng report phase chưa COMPLETED.
- [ ] Wire return intake/process screen tới Phase23, không copy quantity/status/restock rules.

## 12. Frontend Tasks

- [ ] CSKH customer list search/filter/pagination, detail profile và purchase history/returns.
- [ ] Lock/unlock confirm modal có lý do, server errors và status refresh; không hiển thị button ngoài permission.
- [ ] Support history form/list theo D09; không thêm chat UI để thay yêu cầu thiếu model.
- [ ] Empty/history/no access/loading states; hạn chế PII trên bảng danh sách, giữ keyboard/focus/responsive.

## 13. Validation & Business Rules

- Khóa đăng nhập không làm mất lịch sử giao dịch; lock không ngầm cancel đơn/return.
- Permission quyết định staff được xem/sửa contact; Beauty Profile không tự lộ khi không cần.
- Search identity unique xử lý theo Account constraints Phase02/11; không trả credentials.
- Support history chỉ có semantics đã được duyệt; return workflow có một implementation Phase23.

## 14. Security Requirements

- CSKH permission lookup/profile/history/lock riêng theo Phase04, không đồng nghĩa mọi employee là admin.
- Customer chỉ self-service routes; endpoint staff cần explicit guard/scope.
- Không export dữ liệu khách hàng ngoài phạm vi UC nếu chưa yêu cầu; không log password/token.

## 15. Error Handling

- 404 customer; 403 scope/permission; 409 contact uniqueness/account status conflict; validation fields rõ.
- Support persistence/audit lỗi phải rollback write liên quan; không báo lock thành công khi session invalidation fail theo contract.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Phase11 customer DTO/validation; Phase20 order history; Phase23 returns; Phase04 audit and RBAC.
- Phase29 customer reports consume nguồn chuẩn; notification cho lock chỉ Phase30 theo D11.

## 17. Implementation Order

1. Chốt D09/CSKH permissions và đọc prerequisite reports.
2. Paginated lookup + scoped detail/history.
3. Lock/unlock enforcement và regression active sessions.
4. Support history model/migration đã duyệt.
5. CSKH UI và return reuse, handoff customer report.

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
| P25-T01 | Happy path | CSKH có quyền search/view/lock customer | Đúng profile/history; lock ngăn đăng nhập, history còn. |
| P25-T02 | Validation | Search/page/date sai; contact trùng | Field/409 error, không cập nhật sai. |
| P25-T03 | Permission | Customer/employee không có quyền CSKH | 403. |
| P25-T04 | Empty data | Khách chưa mua/chưa có hỗ trợ | List rỗng được trình bày rõ. |
| P25-T05 | Invalid input | Customer ID không có | 404. |
| P25-T06 | Boundary | Khóa tài khoản đang có phiên hoạt động | Không tiếp tục authenticated mutations sau lock theo contract Phase02. |
| P25-T07 | Atomicity/retry | Lock lặp hoặc support/audit write fail | Không tạo history giả/partial write; account state nhất quán. |
| P25-T08 | History | Khóa khách đã có orders/returns | Các history vẫn đọc được bởi staff đúng quyền. |

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

CSKH lookup/profile/history/lock hoạt động; support history chỉ DONE sau D09 và kiểm chứng đầy đủ.

## 22. Handoff To Next Phase

- Phase29 dùng customer/account status và history đúng nguồn; support không mặc định tham gia KPI khi chưa yêu cầu.
- Ghi route staff, allowed fields, session invalidation và D09 model thực tế; không đánh dấu COMPLETED khi support gate mở.
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
