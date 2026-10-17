# Phase 24 — Review giao dịch xác thực và moderation

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **MEDIUM** · Risk: **HIGH** · Depends On: [Phase04](Phase04.md), [Phase20](Phase20.md), [Phase22](Phase22.md)


## 1. Objective

Chỉ khách đã mua sản phẩm trong đơn hoàn tất được review, public chỉ thấy nội dung VISIBLE và moderator giữ lịch sử.

## 2. Why This Phase Exists

Baseline review có create/list/moderate nhưng accepts optional Order hoặc Order không tồn tại, không kiểm tra owner/completed/product và mới tạo mặc định VISIBLE. Thiết kế FK OrderItem cần xử lý dữ liệu legacy có kiểm soát.

## 3. Current State

- ReviewRestController có POST /api/reviews, GET /api/reviews/product/{productId}, GET list staff và PUT status; customerId mặc định 1.
- ReviewServiceImpl gán product/customer/order riêng; order thiếu vẫn null, không verified purchase. Public filter VISIBLE đã đúng và phải giữ.
- ReviewEntity có order_id, thiết kế §4.1.2 Review liên kết OrderItem; comment dùng độ dài JPA mặc định, chưa có migration/UI.

## 4. Preconditions

- Các Phase phụ thuộc [Phase04](Phase04.md), [Phase20](Phase20.md), [Phase22](Phase22.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D08: quyết định migration order_id legacy → order_item_id; đơn nhiều dòng không được map tùy tiện.
- D01: review form trên product detail và moderation staff; chốt chính sách visible ban đầu nếu design không quy định.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC27, UC28, UC37: verified review create/read và hide/show/moderation.
- Owner/order COMPLETED/item belongs product/rating validation, migration được duyệt, history audit.
- Tích hợp tab Reviews trên product detail Phase10 và review entry từ own order Phase20.

### OUT OF SCOPE

- Không tạo review recommendation/AI/spam scoring, social login hoặc upload ảnh review nếu chưa có yêu cầu.
- Không sửa return eligibility hoặc thay order status để cho review.
- Không mặc định giới hạn một review/mỗi dòng nếu thiết kế chưa chốt uniqueness rule.

## 6. Requirements Covered

- System §3.4.1 UC27–28, §3.4.2 UC37; KH-QĐ15 và QLSP-QĐ7: completed purchase và ẩn vẫn giữ history.
- System §4.1.2 Review/OrderItem; FK thiết kế khác source phải giải quyết D08.
- UI §5.3.3 product detail, tab Reviews; staff moderation layout không có mockup.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/rest/review/ReviewRestController.java](../../src/main/java/com/thinh/cosmetic/rest/review/ReviewRestController.java)
- [src/main/java/com/thinh/cosmetic/service/review/ReviewService.java](../../src/main/java/com/thinh/cosmetic/service/review/ReviewService.java)
- [src/main/java/com/thinh/cosmetic/service/review/impl/ReviewServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/review/impl/ReviewServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/review/ReviewEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/review/ReviewEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/request/review/ReviewRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/review/ReviewRequest.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/review/ReviewResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/review/ReviewResponse.java)
- [src/main/java/com/thinh/cosmetic/repository/review/ReviewRepository.java](../../src/main/java/com/thinh/cosmetic/repository/review/ReviewRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/enums/ReviewModerationStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/ReviewModerationStatus.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderItemRepository.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/rest/review/ReviewRestController.java](../../src/main/java/com/thinh/cosmetic/rest/review/ReviewRestController.java)
- [src/main/java/com/thinh/cosmetic/service/review/impl/ReviewServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/review/impl/ReviewServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/review/ReviewEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/review/ReviewEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/request/review/ReviewRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/review/ReviewRequest.java)
- [src/main/java/com/thinh/cosmetic/repository/review/ReviewRepository.java](../../src/main/java/com/thinh/cosmetic/repository/review/ReviewRepository.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- Migration FK order_item theo D08 và tests eligible/public/moderation.
- Customer review form/tab và staff moderation screen theo frontend D01; không lập lại product detail toàn trang.

## 10. Database Changes

- Sau D08 mới thêm/migrate order_item_id và FK. Audit legacy: chỉ backfill khi xác định duy nhất đúng dòng; giữ unresolved records/history theo quyết định, không chọn dòng đầu.
- Comment dài theo thiết kế thay vì default 255 nếu được duyệt; khảo sát dữ liệu trước constraint.
- Không tự unique(customer,item) nếu chưa chốt review multiplicity; không xóa order_id trước kiểm chứng migration/rollback.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Đọc eligibility/order service Phase20/22 và nguồn identity; bỏ customerId mặc định từ client.
- [ ] Review command lấy orderItem và kiểm tra order.owner=principal, status=COMPLETED, product khớp SKU; không chấp nhận nonexistent Order thành null.
- [ ] Validate rating giới hạn hiện có 1–5, comment theo thiết kế; DTO/database contract thống nhất sau D08.
- [ ] Thực hiện migration test với đơn một dòng/nhiều dòng, order thiếu/product mismatch; report unresolved thay vì suy đoán.
- [ ] Giữ public VISIBLE filtering, chuyển lọc/pagination xuống repository nếu danh sách tăng; expose response DTO, không trả PII/Order entity.
- [ ] Staff moderation có permission, audit actor/status/reason khi cần theo rule đã chốt; hide/show không physical delete.
- [ ] Create moderation initial state theo quyết định, không tự đổi VISIBLE baseline thành pending nếu không có requirement.
- [ ] Giữ historical linkage khi catalog inactive/order history lâu năm; error semantics Phase01.

## 12. Frontend Tasks

- [ ] Order detail hiện review action chỉ eligible item; UI gửi orderItem theo contract mới sau D08.
- [ ] Product detail Reviews tab có rating/text, pagination nếu cần, empty/loading và lỗi validation.
- [ ] Staff moderation list/filter/detail và hide/show; retained history không xuất public hidden content.
- [ ] UI không chứng nhận verified chỉ bằng client flag; server response mới được hiển thị trạng thái xác thực.

## 13. Validation & Business Rules

- Review phải từ own COMPLETED order containing product; không xác nhận bằng productId tùy ý.
- Rating 1–5; comment giữ đúng độ dài được chốt, không trust HTML; output escape theo framework.
- Public VISIBLE only; moderation không làm mất lịch sử.
- Legacy mapping ambiguous là blocking issue, không đánh dấu phase COMPLETED khi còn policy chưa chốt.

## 14. Security Requirements

- Anonymous được đọc VISIBLE reviews; customer authenticated được create own verified review.
- Staff catalog/review moderator theo permission Phase04 được xem hidden/moderate; customer không gọi GET all/PUT status.
- CSRF/session hoặc token bảo vệ mutation theo Phase02; customer name display theo PII policy.

## 15. Error Handling

- Owner/order/item/product mismatch: 403/409 tùy contract; resource missing 404; rating/comment sai field validation.
- D08 chưa chốt thì không chạy migration phá dữ liệu; review legacy unresolved có báo cáo rõ thay vì startup failure.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Product detail Phase10; own order detail Phase20; completion Phase22; auth/audit Phase04.
- Không phụ thuộc các báo cáo; catalog ACTIVE/INACTIVE không xóa review lịch sử.

## 17. Implementation Order

1. Chốt D08 và moderation initial state; audit dữ liệu cũ.
2. DTO/FK migration với fixtures ambiguous, không backfill đoán.
3. Verified create rules + public repository filtering.
4. Moderation/audit + customer/staff UI.
5. Regression legacy/read/permission và handoff.

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
| P24-T01 | Happy path | Owner, COMPLETED, OrderItem sản phẩm đúng, rating 5 | Review lưu với linkage hợp lệ, state theo quyết định. |
| P24-T02 | Validation | Rating 0/6; comment vượt giới hạn | Không lưu; field error. |
| P24-T03 | Permission | Đơn khách khác; customer moderate | 403. |
| P24-T04 | Empty data | Sản phẩm chưa có VISIBLE review | Empty tab; hidden không xuất hiện. |
| P24-T05 | Invalid input | OrderItem không tồn tại hoặc sản phẩm khác | 404/409, không review null order. |
| P24-T06 | Boundary | Rating 1 và 5; comment tại giới hạn | Chấp nhận biên đúng. |
| P24-T07 | Migration | Legacy order nhiều dòng/mismatch | Unresolved report, không gán dòng tùy tiện. |
| P24-T08 | History | Ẩn rồi hiện review; product inactive | Record vẫn tồn tại, public visibility đúng. |

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

Review xác thực theo dòng giao dịch và moderation giữ history, legacy được xử lý theo D08.

## 22. Handoff To Next Phase

- Product detail và order history sử dụng linkage/rating contract thực tế đã bổ sung.
- Ghi số legacy migrated/unresolved, initial moderation state và routes được bảo vệ; phase không hoàn tất nếu D08 còn mở.
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
