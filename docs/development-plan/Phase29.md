# Phase 29 — Báo cáo nhập hàng, khuyến mãi và khách hàng

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **HIGH** · Risk: **MEDIUM** · Depends On: [Phase11](Phase11.md), [Phase14](Phase14.md), [Phase15](Phase15.md), [Phase16](Phase16.md), [Phase25](Phase25.md), [Phase26](Phase26.md)


## 1. Objective

Hoàn thiện UC90–91 bằng report nhập/khuyến mãi/khách hàng có định nghĩa metric rõ và tái sử dụng nguồn đã hoàn thành.

## 2. Why This Phase Exists

Không có reporting module trong baseline. Purchase history, promotion effectiveness và customer history phải hoàn thành trước, tránh report giả dựa số đếm đơn giản hoặc trùng export.

## 3. Current State

- PurchaseOrderEntity có supplier/receivingStore/status/createdAt/confirmedAt/totalAmount và items; chưa có aggregate query.
- Voucher usedQuantity có trong baseline nhưng không đủ thay report usage/discount thực tế Phase26.
- Customer profile/history/lock/support thuộc Phase11/25; transfer/count/export thuộc Phase15/16. Không có customer/purchase report UI.

## 4. Preconditions

- Các Phase phụ thuộc [Phase11](Phase11.md), [Phase14](Phase14.md), [Phase15](Phase15.md), [Phase16](Phase16.md), [Phase25](Phase25.md), [Phase26](Phase26.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D01: report UI/permissions; D10 phải đã đóng và Phase26 COMPLETED cho promotion report.
- D17 phải chốt metric/time/filter definitions; metric customer/purchase ngoài UC phải được duyệt trước thêm. Không tự tạo segmentation/CLV/net-profit; promotion metrics reuse quyết định D10/Phase26.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC90, UC91: báo cáo nhập hàng/khuyến mãi và khách hàng theo dữ liệu được thiết kế.
- Read-only aggregate query/DTO/UI với cùng period/scope conventions; reuse promotion effectiveness Phase26.
- Reuse inventory export Phase15 và promotion export Phase26 nếu UI cần điều hướng; không tạo exporters trùng.

### OUT OF SCOPE

- Revenue/bestseller/inventory report UC88–89 Phase28; voucher computation Phase18.
- Không thêm analytics customer profiling/marketing segment, campaign messaging hay support SLA KPIs.
- Không giả revenue net refunds/returncost hoặc purchasecost profitability khi thiếu quy định.

## 6. Requirements Covered

- System §3.4.10 UC90–91; BCTK-QĐ3 promotion usages/sum discount.
- System §3.4.5 purchase history UC47–48 và §3.4.7 customer history UC60 làm nguồn tái sử dụng.
- System §2.2 timestamp/permission, §2.3 performance/security; không có report mockup UI.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderItemRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/purchase/PurchaseOrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/purchase/PurchaseOrderEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/enums/PurchaseOrderStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/PurchaseOrderStatus.java)
- [src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/VoucherRepository.java)
- [src/main/java/com/thinh/cosmetic/service/purchase/PurchaseOrderService.java](../../src/main/java/com/thinh/cosmetic/service/purchase/PurchaseOrderService.java)
- [src/main/java/com/thinh/cosmetic/service/account/CustomerService.java](../../src/main/java/com/thinh/cosmetic/service/account/CustomerService.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderItemRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- Report projections/DTO/service/controller mở rộng module report thực tế nếu Phase28 đã tồn tại; không tạo module trùng khi Phase28 chạy song song.
- Tests report fixtures và UI purchase/promotion/customer tabs theo D01; export mới chỉ khi requirement được duyệt.

## 10. Database Changes

- NONE mặc định: aggregate từ transactions/customers/programs đã có.
- TECHNICAL RECOMMENDATION: index query period/status/supplier/store đã đo; không lưu duplicated reporting tables hoặc đổi DB.
- Timestamps legacy thiếu phải được thể hiện unresolved/excluded theo quyết định, không backfill thời gian giả.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Đọc Phase14 purchase confirmation, Phase26 program/report contract và Phase25 customer scope/model trước đặt query.
- [ ] Khóa report definitions: purchase confirmed transactions dùng confirmedAt/status theo nhu cầu UC; DRAFT không bị coi là đã nhập kho.
- [ ] Aggregate purchase quantity/value theo supplier/store/period có label rõ, dùng stored purchase totals/item cost; tránh double count header khi join items.
- [ ] Promotion report gọi/reuse projection/filter của Phase26, không dùng usedQuantity thay sum discounts từ order snapshots.
- [ ] Customer report dùng fields/count/history đã được UC91 chốt; giới hạn PII và branch/customer scope theo Phase04/25.
- [ ] Apply filter security ở DB query, validate period/page/sort; read-only transactions và BigDecimal money.
- [ ] Không tự suy transfer là purchase hoặc support event là sale; stock exports/reports tái dùng Phase15/28.
- [ ] Kiểm tra consistency report tables/totals với source history và exports hiện có; ghi mapping metric/source/date/status trong report handoff.

## 12. Frontend Tasks

- [ ] Tabs nhập hàng, khuyến mãi, khách hàng; period/store/supplier/program filters theo quyền và data model.
- [ ] Hiển thị labels confirmed/usage/discount/customer metric rõ; empty/loading/errors và responsive tables.
- [ ] Promotion/inventory export buttons điều hướng chức năng Phase26/15 nếu đủ quyền, không download dataset khác filter.
- [ ] Không thêm PII export/segmentation controls ngoài requirement.

## 13. Validation & Business Rules

- Đơn nhập DRAFT không là hàng đã nhận; confirmation time/status Phase14 là nguồn, không cộng duplicate receive.
- Promotion usage/discount cùng semantics Phase26 và BCTK-QĐ3.
- Customer locked/inactive vẫn có historical transactions; không xóa/ẩn lịch sử vô nghĩa.
- Transfer là nội bộ, không tự ghi doanh số nhập/chi mua mới; report dates nhất quán.

## 14. Security Requirements

- Role báo cáo hoặc quản lý tương ứng theo Phase04; customer không truy staff reports.
- Chi nhánh/supplier/customer data filter trong permitted scope, export routes guard riêng.
- Minimize/redact PII; không dùng report để đọc support/customer history ngoài quyền.

## 15. Error Handling

- Period/filter sai: validation; scope401/403; resource missing404; query/export fail lỗi sạch.
- Thiếu definition/gate chưa đóng không trả metric đoán; report section chưa hoàn thành phải thể hiện đúng.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Purchase Phase14; inventory count/transfer Phase15/16; customer Phase11/25; promotion Phase26.
- Dùng common report framework nếu Phase28 đã tạo, tránh concurrent edit shared DTO/controller qua phân công file.

## 17. Implementation Order

1. Đọc reports prerequisite và xác định dữ liệu/date/status metric.
2. Purchase projections và fixture sums.
3. Promotion reuse + customer projection permission tests.
4. Report UI/filter/export links cùng semantics.
5. PostgreSQL reconciliation với history và handoff final integration.

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
| P29-T01 | Happy path | CONFIRMED purchase, program usages, customer history | Đúng aggregate theo sources/filters. |
| P29-T02 | Validation | Date/page/supplier/program filters sai | Validation, không query tùy ý. |
| P29-T03 | Permission | Customer/branch staff xem ngoài scope | 403 hoặc dữ liệu giới hạn scope. |
| P29-T04 | Empty data | Period/customer chưa có giao dịch | Zeros/empty có label, không lỗi. |
| P29-T05 | Invalid input | Supplier/program/customer ID thiếu | 404/validation. |
| P29-T06 | Boundary | Confirmation đúng time boundary; locked customer | Period đúng quy ước, history locked vẫn giữ. |
| P29-T07 | Double count | DRAFT + confirmed receipt + transfer cùng SKU | Chỉ transactions nhập hợp lệ tính đúng một lần. |
| P29-T08 | Consistency | Promotion tab và Phase26 export cùng filter | Usage/sumdiscount/totals bằng nhau. |
| P29-T09 | PII | Report list cho role giới hạn | Không lộ customer/support detail vượt quyền. |

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

UC90–91 có report nguồn chuẩn, quyền và độ khớp history; không phát sinh mô hình analytics mới.

## 22. Handoff To Next Phase

- Phase31/32 nhận metric source/date/status conventions, query validation và reconciled fixture results.
- Ghi routes/UI/tables thực sự có, reused exporters và remaining gates; không mặc định report pass là toàn hệ thống E2E pass.
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
