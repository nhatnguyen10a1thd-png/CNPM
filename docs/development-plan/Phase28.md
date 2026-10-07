# Phase 28 — Báo cáo doanh thu, đơn hàng, sản phẩm và tồn

[Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md)
Complexity: **MEDIUM** · Risk: **MEDIUM** · Depends On: [Phase15](Phase15.md), [Phase20](Phase20.md), [Phase22](Phase22.md), [Phase23](Phase23.md)


## 1. Objective

Cung cấp báo cáo UC88–89 có số liệu doanh thu/đơn, sản phẩm bán và tồn nhất quán với giao dịch/tồn thực.

## 2. Why This Phase Exists

Source không có module aggregate/report. Order snapshot và stock primitives đã có hoặc hoàn thành các Phase trước; cần query tổng hợp chứ không đổi kiến trúc sang analytics system.

## 3. Current State

- OrderRepository baseline chỉ có customer list; chưa có revenue/group/count query.
- OrderEntity lưu subtotal/discount/shipping/total/status/completedAt và items snapshot; inventory có actual/held/minimum.
- Low stock baseline dùng actualStock nhưng Phase15 phải đã sửa available-based query; returns Phase23 có disposition được chốt, không tự suy net refunds.

## 4. Preconditions

- Các Phase phụ thuộc [Phase15](Phase15.md), [Phase20](Phase20.md), [Phase22](Phase22.md), [Phase23](Phase23.md) có Completion Report đủ bằng chứng; kiểm tra lại source và interface đã bàn giao.
- Có baseline Java 21 và PostgreSQL test/development độc lập; H2 9/9 pass trong audit chưa xác minh giao dịch PostgreSQL.
- D01: report UI/filter và role báo cáo được chốt Phase04.
- D17 phải chốt metric dictionary, công thức/nhãn revenue và date basis/timezone/range. Filter COMPLETED là bắt buộc; createdAt hay completedAt cho kỳ thống kê phải có quyết định, không tự thêm net-refund accounting.
- Các quyết định tham chiếu ở [Decision Register](01_MASTER_ROADMAP.md#decision-register). Có thể làm phần độc lập khi gate mở, nhưng không đánh dấu toàn Phase COMPLETED nếu gate còn chặn requirement.

## 5. Scope

### IN SCOPE

- UC88–89: doanh thu/đơn, sản phẩm bán chạy, tồn kho và cảnh báo tồn thấp.
- Period/store filters, aggregate DTO và UI consistent; chọn ngày/timezone thống nhất.
- Tái sử dụng inventory query/export Phase15; không duplicate stock report engine.

### OUT OF SCOPE

- Không làm UC90–91 của Phase29 hoặc promotion export Phase26.
- Không thêm profit/COGS/forecast/refund-adjusted accounting/KPI chưa định nghĩa.
- Không tạo BI warehouse, ETL, chart vendor hoặc database mới.

## 6. Requirements Covered

- System §3.4.10 UC88–89; BCTK-QĐ1–2: revenue và best-selling chỉ COMPLETED.
- System §2.1.6 QLTK-QĐ8–9 và §4.2: available=actual-held, thấp khi available ≤ minimum.
- NFR hiệu năng/security §2.3; staff reports không có UI mockup.

## 7. Files To Inspect First

- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderItemRepository.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/order/OrderItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderItemEntity.java)
- [src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [src/main/java/com/thinh/cosmetic/service/store/InventoryService.java](../../src/main/java/com/thinh/cosmetic/service/store/InventoryService.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java)
- [src/main/java/com/thinh/cosmetic/domain/entity/store/StoreEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StoreEntity.java)
- Đọc implementation/tests/interface mới từ prerequisite; tên/path có thể đã đổi, cập nhật report trước sửa.

## 8. Files Expected To Modify

- [src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/order/OrderItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/order/OrderItemRepository.java)
- [src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- Frontend đã được prerequisite tạo: sửa đúng screen/contract liên quan sau D01; baseline chưa có template/static để ghi một path đang tồn tại.

## 9. Files Expected To Create

- Report aggregate response/projection, service/controller trong package nghiệp vụ hiện có hoặc report package nhỏ nhất cần thiết.
- Tests dữ liệu nhiều statuses/store/time boundaries và report UI sau D01; không tạo pipeline ETL.

## 10. Database Changes

- NONE mặc định: số liệu từ orders/items/inventory hiện có.
- TECHNICAL RECOMMENDATION: index status/completedAt/store theo query đo được và dữ liệu thật; migration không backfill giả timestamps và không lưu duplicated totals nếu chưa cần.
- Mọi migration phải audit dữ liệu hiện hữu, bảo toàn lịch sử và có cách rollback/restore được kiểm chứng; không dùng reset database hoặc ddl-auto để che lỗi migration.

## 11. Backend Tasks

- [ ] Giải quyết D17 trước aggregate: chốt revenue dùng trường/công thức nào (totalAmount hay subtotal/discount/shipping breakdown), date basis/timezone và metric labels. Giữ filter COMPLETED bắt buộc; không tự trừ refund hoặc mặc định grand total là doanh thu khi chưa xác nhận.
- [ ] Order report có status counts chỉ khi UC cần, phân biệt counts với completed revenue; date basis từng metric phải ghi rõ.
- [ ] Bestseller aggregate quantity/sales từ items của COMPLETED orders, dùng snapshots cho historical names; tránh double count join nhiều quan hệ.
- [ ] Dùng DB projection/group aggregation, validate period/store filters và scope; không tải mọi order/items vào memory.
- [ ] Tồn hiện tại trả actual/held/available/minimum và cảnh báo query Phase15; không trình bày inventory hiện tại như historical stock cuối kỳ.
- [ ] Giữ zero/empty semantics, BigDecimal money và deterministic sort/tie rule; pagination bảng phù hợp.
- [ ] Report read-only, không mutate state để sửa sai số; phát hiện dữ liệu lỗi ghi Known Issues/owner phase.
- [ ] Query consistency cùng filters giữa tổng/bảng/chart; kỹ thuật snapshot/read consistency theo DB nếu cần có nhãn recommendation.

## 12. Frontend Tasks

- [ ] Report tabs revenue/orders, products, stock với period/store filters, labels time basis và số tiền VND.
- [ ] Charts chỉ hỗ trợ cùng số liệu bảng, không tự metric mới; empty/no access/loading/failure states.
- [ ] Tồn thấp hiển thị available và threshold, tái dùng module Phase15; không thêm export chưa UC yêu cầu.
- [ ] Responsive filter/table, pagination và keyboard theo D01.

## 13. Validation & Business Rules

- Revenue/bestseller chỉ COMPLETED; CANCELLED/PENDING/SHIPPING không vào completed metrics.
- Date period phải cùng timezone và quy ước biên; inactive products/stores vẫn giữ lịch sử.
- Available không clamp để che vi phạm; stock hiện tại không giả mốc lịch sử.
- Refund/accounting ngoài D07 chưa được định nghĩa không tự suy diễn vào revenue.

## 14. Security Requirements

- Role quản lý/báo cáo được permission Phase04 và scope; không mọi staff đều xem doanh thu toàn chuỗi.
- Branch filter không vượt scope, service apply security trước aggregation.
- Report DTO không expose customer PII/credentials không cần thiết.

## 15. Error Handling

- Invalid time range/filter: validation; store thiếu/ngoài scope404/403 theo contract; DB query fail lỗi sạch.
- Data anomaly không chuyển thành metric 0 giả; ghi chẩn đoán có quyền và owner phase.
- Dùng error contract đã hoàn thiện Phase01; không tạo GlobalExceptionHandler thứ hai hoặc tiếp tục trả raw Exception message cho mọi lỗi.

## 16. Integration Points

- Phase22 completedAt/totals/items; Phase20 orders; Phase15 inventory query; Phase23 return disposition.
- Phase29 theo cùng period/scope conventions; kỹ thuật performance benchmark thuộc Phase32.

## 17. Implementation Order

1. Khóa metric/time conventions và fixtures chuẩn.
2. Aggregate repository/service with permission/scope.
3. Stock report reuse và zero/history tests.
4. UI bảng/chart từ cùng DTO.
5. PostgreSQL query verification và handoff metric definitions.

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
| P28-T01 | Happy path | Fixtures COMPLETED ở hai store và items | Revenue/bestsellers đúng snapshots theo scope. |
| P28-T02 | Validation | Range ngược/quá giới hạn hợp đồng/page sai | Validation, không expensive query vô hạn. |
| P28-T03 | Permission | Branch manager query branch khác/all | Không vượt scope. |
| P28-T04 | Empty data | Period không có completed orders | Totals0, bảng/chart empty. |
| P28-T05 | Invalid input | Store/filter ID sai | 404/validation. |
| P28-T06 | Boundary | CompletedAt đúng mốc start/end; available=minimum | Date rule thống nhất, row được cảnh báo tại biên. |
| P28-T07 | Status | PENDING/CANCELLED/SHIPPING có totals cao | Không ảnh hưởng completed revenue/bestseller. |
| P28-T08 | History | Product inactive/giá đổi; return tồn không nhập lại | Historical sales đúng; currentstock đúng disposition. |
| P28-T09 | Join correctness | Một order nhiều items/returns | Revenue không bị nhân theo số dòng/joins. |

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

UC88–89 có aggregate/report kiểm chứng số liệu và quyền, không thay thế transaction modules.

## 22. Handoff To Next Phase

- Phase29 dùng chung time/scope/filter conventions và metric labels; không tự tái tính revenue khác.
- Ghi query/API thực tế, fixture expected sums, available semantics và accounting exclusions đã được quyết định.
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
