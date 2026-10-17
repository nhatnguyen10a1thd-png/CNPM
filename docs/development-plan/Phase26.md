# Phase 26 — Chương trình khuyến mãi và hiệu quả

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **HIGH** · Risk: **HIGH** · Depends On: [Phase04](Phase04.md), [Phase18](Phase18.md), [Phase19](Phase19.md), [Phase22](Phase22.md)


## 1. Objective

Hoàn thiện UC76/80/81 bằng model chương trình khuyến mãi đã được duyệt và báo cáo hiệu quả từ giao dịch thực.

## 2. Why This Phase Exists

Source chỉ có Voucher; sơ đồ thiết kế có PromotionProgram–Voucher nhưng 37 bảng không định nghĩa chương trình. Voucher CRUD không thay thế được UC76, và không được tự phát minh schema để lấp chỗ trống.

## 3. Current State

- VoucherEntity có code/type/value/cap/min/date/quota/usedQuantity/status; VoucherService CRUD/calculation đã tồn tại và được Phase18 sửa phần thiếu.
- Chưa có PromotionProgram entity/API/UI hay aggregate effectiveness query.
- Order lưu voucher và discountAmount; Phase19/22 cung cấp giao dịch thực, phải đối chiếu semantics dùng/voucher/count trước report.

## 4. Preconditions

- Các Phase phụ thuộc [Phase04](Phase04.md), [Phase18](Phase18.md), [Phase19](Phase19.md), [Phase22](Phase22.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D10: chốt model PromotionProgram, phạm vi áp dụng, liên kết voucher, trạng thái, precedence/stacking và nguồn dữ liệu hiệu quả.
- D01: marketing UI và export format cần thiết; nếu không có format cố định dùng khả năng xuất bảng đã duyệt, không ép XLSX/PDF.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC76, UC80, UC81: quản lý chương trình, thống kê hiệu quả và xuất dữ liệu.
- Giữ lịch sử khuyến mãi; dùng pricing Phase18 và order snapshots, không tính lại đơn cũ.
- Permission marketing và audit cho thay đổi chương trình; query thời gian/scope rõ ràng.

### OUT OF SCOPE

- UC77–79 voucher/eligibility/discount thuộc Phase18; không copy voucher engine.
- Không dựng campaign email/SMS, A/B testing, analytics platform hoặc loyalty tier.
- Không tự đặt discount stacking, target segments hoặc revenue attribution ngoài D10.

## 6. Requirements Covered

- System §3.4.9 UC76, UC80, UC81; §2.1.9 QLKM-QĐ1–5 và BCTK-QĐ3 usage/sum discount.
- Sơ đồ system image24 có PromotionProgram nhưng §4.1.2 không có bảng tương ứng: Design ↔ Design gate D10.
- UI header Promotions là navigation reference; chưa có marketing/admin mockup.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/rest/order/VoucherRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/VoucherRestController.java)
- [src/main/java/com/thinh/cosmetic/service/order/VoucherService.java](../../src/main/java/com/thinh/cosmetic/service/order/VoucherService.java)
- [src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/VoucherEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/VoucherEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/request/order/VoucherRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/order/VoucherRequest.java)
- [src/main/java/com/thinh/cosmetic/domain/dto/response/order/VoucherResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/order/VoucherResponse.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/VoucherEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/VoucherEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- PromotionProgram model/DTO/repository/service/controller/migration chỉ sau D10, trong kiến trúc REST → Service → Repository hiện tại.
- Query projection hiệu quả, export service nhỏ, tests và marketing UI theo D01; không tạo warehouse/report database.

## 10. Database Changes

- Chỉ sau D10: bổ sung bảng/quan hệ program và voucher nếu model duyệt yêu cầu; ghi FK/date/status/unique/nullable/backfill cụ thể trước migration.
- Voucher legacy không được gán chương trình tùy tiện. Lưu history nguồn áp dụng nếu D10 yêu cầu; không retroactively thay order discount.
- Không drop/đổi toàn bộ Voucher schema đang chạy; technical index theo query thật.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Chốt D10, ghi model/precedence/eligibility hợp đồng với Phase18 trước mọi migration hay pricing extension.
- [ ] Program create/update/deactivate với dates/scope/status theo model, permission marketing và audit; chương trình đã có giao dịch không physical delete.
- [ ] Tái sử dụng voucher validation/calculation/quotas Phase18; nếu program thay eligibility, bổ sung tại service chung và regression checkout, không engine song song.
- [ ] Giữ discount snapshot của Order; việc sửa program không làm thay đổi đơn trước đó.
- [ ] Effectiveness query lấy số lần dùng và tổng giảm thực tế theo BCTK-QĐ3; xác định status/time basis được chốt, không lấy usedQuantity làm mọi metric.
- [ ] Định nghĩa khoảng thời gian, timezone, filter phạm vi rõ ràng và tính aggregate trong DB; không load mọi entity về memory.
- [ ] Export từ cùng filtered aggregate dataset, consistent totals/labels/date range; enforce permission trước stream.
- [ ] Nhánh D10 còn mở ghi BLOCKED với quyết định cần có; các phần chuẩn bị không được coi hoàn thành UC76.

## 12. Frontend Tasks

- [ ] Marketing program list/detail/form/deactivate theo D10, dates/scope/status validation.
- [ ] Effectiveness bảng tổng usage/discount và filter, empty/loading/error; chỉ hiển thị metrics có định nghĩa được duyệt.
- [ ] Export rõ filter/range đang chọn; lịch sử và inactive programs vẫn tra cứu được theo quyền.
- [ ] Không tạo popup campaign/email flow hoặc UI rules chưa được duyệt.

## 13. Validation & Business Rules

- Không sửa giá trị discount lịch sử; no hard delete program/voucher đã có giao dịch.
- Date/scope/status/precedence phải thống nhất pricing Phase18; eligibility chỉ một nguồn truth.
- Hiệu quả theo usage và sum discount thực tế; không tự claim net profit, ROI hoặc refund-adjusted revenue.
- D10 open ngăn completion và mọi schema thực thi cho program.

## 14. Security Requirements

- Marketing permission quản lý chương trình/report/export theo Phase04; customer chỉ read public promotion nếu requirement và D01 đã chốt.
- Scope đối với số liệu/program theo ma trận quyền, không tin filter branch từ client.
- Export không chứa unnecessary customer PII, không expose voucher configuration bất hợp lệ.

## 15. Error Handling

- Dates/scope/duplicate invalid: field validation hoặc409; program missing404; inactive/historical delete conflict409.
- Export/query fail trả lỗi rõ và giữ chương trình; pricing extension fail không đặt đơn giảm một phần.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Phase18 pricing/voucher/quota; Phase19 voucher usage snapshots; Phase22 completed orders; Phase04 audit/identity.
- Phase29 consume cùng effectiveness query; không dựng export thứ hai với phép tính khác.

## 17. Implementation Order

1. Chốt D10 và model/migration plan.
2. Program CRUD/deactivate với validation/audit.
3. Integrate eligibility vào service Phase18 + checkout regression.
4. DB aggregate hiệu quả và export cùng filter.
5. Marketing UI + handoff query definitions.

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
| P26-T01 | Happy path | Program hợp lệ, đơn áp dụng voucher sau hoàn tất | Program/history lưu; hiệu quả theo dataset thật. |
| P26-T02 | Validation | End trước start, scope/code sai theo D10 | Không lưu/program không áp dụng. |
| P26-T03 | Permission | Customer hoặc staff thiếu marketing permission | 403 cho manage/report/export. |
| P26-T04 | Empty data | Program chưa có giao dịch | Usage/discount bằng 0, report rỗng đúng. |
| P26-T05 | Invalid input | Program/filter ID không tồn tại | 404/validation không giả số liệu. |
| P26-T06 | Boundary | Start/end/quota đúng biên theo Phase18 | Eligibility và report cùng rule. |
| P26-T07 | History | Đổi/deactivate program sau order | Order discount snapshot và lịch sử không đổi. |
| P26-T08 | Consistency | UI filters và export cùng range | Cùng rows/totals; không metric riêng. |
| P26-T09 | Integration | Hai chương trình trùng phạm vi theo D10 | Pricing quyết định đúng precedence đã duyệt, không double discount. |

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

PromotionProgram và hiệu quả/xuất có đặc tả và implementation kiểm chứng; Voucher được tái sử dụng, không đánh đồng hai module.

## 22. Handoff To Next Phase

- Phase29 dùng projection/filter/metric semantics cùng nguồn Phase26; ghi route/export format/model thực tế.
- Ghi migration legacy, decision D10 và limitations; gate mở giữ phase chưa COMPLETED.
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
