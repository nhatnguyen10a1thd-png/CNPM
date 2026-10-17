# Phase 15 — Kiểm kê, cảnh báo tồn thấp và xuất kho

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase08](Phase08.md). Complexity: MEDIUM. Risk: MEDIUM.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Cho nhân viên kiểm kê SKU theo cửa hàng bằng nghiệp vụ adjustment đã bảo vệ, tra cứu tồn thấp theo available stock và xuất đúng dữ liệu trong scope.

## 2. Why This Phase Exists

Adjustment và getLowStockItems đã tồn tại, nhưng chưa có màn hình kiểm kê/export. Query tồn thấp đang so actualStock với minimumStock, trái QLTK-QĐ9 so available stock.

## 3. Current State

- InventoryRepository.findLowStock dùng i.actualStock <= i.minimumStock; chưa trừ heldQuantity.
- InventoryResponse và getAvailableStock đã tính actual - held, đang Math.max(0, …); Phase08 phải xử lý invariant gốc, không dùng clamp để che lỗi.
- InventoryAdjustmentEntity lưu quantityBefore/After/reason/performedBy; có thể tái sử dụng cho kết quả kiểm kê thay vì tự tạo hệ thống chứng từ kiểm kê mới.
- GET /api/inventory/store/{storeId}/low-stock và POST /api/inventory/adjust đã có; chưa có export/count UI.
- InventoryServiceTest có 2 happy tests available/adjustment; chưa kiểm thử low-stock boundary.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase08 COMPLETED với invariant, lock, actor, scope và adjustment reason.
- [ ] D01 có layout staff đã chốt; format xuất được ghi là quyết định kỹ thuật trong report.
- [ ] Có dataset actual/held/minimum gồm cả boundary bằng đúng threshold.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Sửa query low stock để so available ≤ minimum; hiển thị đủ actual/held/available/minimum.
- Nhập số đếm kiểm kê và lý do, lưu bằng adjustment Phase08, có xác nhận chênh lệch.
- Xuất kết quả inventory/low-stock theo filter/scope đang xem.

### OUT OF SCOPE

- Không tạo workflow kiểm kê nhiều cấp hoặc bảng StockCount không có trong schema thiết kế.
- Không triển khai order hold/release, purchase receipt hay transfer trong Phase này.
- Dashboard báo cáo Phase28 chỉ tái sử dụng query; không đưa dashboard vào đây.

## 6. Requirements Covered

- §3.4.6 UC52, UC56, UC57; đây là Phase owner.
- QLTK-QĐ3–4: kiểm kê đưa chênh lệch qua điều chỉnh, lưu before/after/reason/actor/time.
- QLTK-QĐ8–9: actual - held ≥ 0, cảnh báo available ≤ minimum; §4.2 bất biến inventory.
- §4.1.2: Inventory và InventoryAdjustment; khóa store/SKU hiện có phải được giữ.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [InventoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java)
- [InventoryService.java](../../src/main/java/com/thinh/cosmetic/service/store/InventoryService.java)
- [InventoryRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/InventoryRestController.java)
- [InventoryAdjustmentEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryAdjustmentEntity.java)
- [InventoryAdjustmentRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/store/InventoryAdjustmentRequest.java)
- [InventoryResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/store/InventoryResponse.java)
- [InventoryServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/InventoryServiceTest.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [InventoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java)
- [InventoryRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/InventoryRestController.java)
- [InventoryServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/InventoryServiceTest.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- Test low-stock boundary/export/scope và kiểm kê rollback PostgreSQL.
- UI kiểm kê/tồn thấp/export theo D01; formatter xuất riêng nếu cần. TECHNICAL RECOMMENDATION: CSV UTF-8 là định dạng tối thiểu dễ kiểm chứng, không thêm thư viện spreadsheet.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

NONE. Dùng Inventory và InventoryAdjustment đã được Phase08 gia cố. Không đổi surrogate ID/unique(store_id, sku_id); không tạo bảng kiểm kê mới khi chức năng đếm và adjustment hiện có đáp ứng use case.

## 11. Backend Tasks

- [ ] Sửa findLowStock thành actualStock - heldQuantity <= minimumStock; sử dụng convention null/minimum đã chốt Phase08.
- [ ] Tái sử dụng query scoped inventory và filter/pagination; count tổng kết quả không phụ thuộc page hiện tại.
- [ ] Đưa số kiểm kê vào adjustment hiện có với reason rõ ràng; kiểm tra held hiện thời dưới lock trước khi ghi actual.
- [ ] Hiển thị chênh lệch before/after và actor/time từ lịch sử persisted; không thay held bằng số đếm.
- [ ] Nếu tồn đã đổi từ lúc mở form, trả conflict/giá trị mới để người dùng xác nhận lại theo lock convention Phase08.
- [ ] Thêm export chỉ đọc trên cùng filter và scope; xuất actual/held/available/minimum, store, SKU và timestamp xuất.
- [ ] Ghi audit mutation kiểm kê; export áp dụng permission riêng nếu ma trận Phase04 yêu cầu.
- [ ] Đảm bảo không truy vấn mọi cửa hàng rồi lọc client-side; không đưa dữ liệu ngoài scope vào file.

## 12. Frontend Tasks

- [ ] Màn hình kiểm kê chọn cửa hàng được phép/SKU, nhập count và reason, so trước/sau trước xác nhận.
- [ ] Hiển thị lỗi count thấp hơn held, dữ liệu đã thay đổi và validation; giữ dữ liệu người dùng nhập.
- [ ] Màn hình tồn thấp hiển thị SKU, actual/held/available/minimum, filter/pagination và trạng thái rỗng.
- [ ] Export áp dụng filter đang xem, báo đang tải/lỗi và tên file rõ phạm vi; không tự xuất dữ liệu ngoài scope.

## 13. Validation & Business Rules

- Low stock khi available bằng threshold cũng phải xuất hiện; không dùng actual để quyết định.
- Count ≥ held ≥ 0; reason/actor/time bắt buộc, giữ lịch sử adjustment.
- Count là tồn thực tế quan sát; held giữ nguyên và không nhận/chuyển hàng lần nữa.
- Export không thay đổi kho; dữ liệu hiển thị và file dùng cùng nghĩa filter.

## 14. Security Requirements

- Nhân viên kho kiểm kê/đọc/export trong assigned store; quản lý chuỗi chỉ theo quyền Phase04.
- Các route /api/inventory/store/{storeId}/low-stock, adjust và export kiểm tra permission/scope phía server.
- Customer không được truy cập chi tiết actual/held hoặc file kho; public availability thuộc Phase09/10.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Count âm/thấp hơn held, reason trống hoặc SKU không có inventory → validation/conflict rõ ràng.
- Stale value/lock failure không được overwrite âm thầm; mutation và audit nghiệp vụ rollback.
- Export lỗi phải có phản hồi tải lại; không trả file một phần như thành công.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Phase08 sở hữu mutation adjustment; Phase15 chỉ tái sử dụng và nối kiểm kê.
- Phase28 dùng query low stock đã sửa; export kho không phụ thuộc dashboard.
- Purchase/transfer/checkout cùng primitive kho nên cần regression invariant khi count xảy ra đồng thời.

## 17. Implementation Order

1. Kiểm tra primitive Phase08 và convention minimum/null.
2. Sửa low-stock query, viết test equality và held boundary.
3. Nối kiểm kê qua adjustment, kiểm thử actor/conflict/rollback.
4. Thêm scoped export và staff UI; đối chiếu file với filter.
5. Bàn giao query và mẫu xuất cho Phase28.

## 18. Verification

- [ ] Actual 10, held 4, min 6 có trong low-stock; held 3 không có.
- [ ] Count ghi before/after/reason/actor/time, held giữ nguyên.
- [ ] Count thấp hơn held bị từ chối dù UI nhập được; concurrent change không gây negative available.
- [ ] Export đúng store/filter và chứa cả kết quả ngoài page hiện tại nếu contract chọn toàn bộ filter.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P15-01 | Low stock equality | actual=10, held=4, min=6 | available=6; có cảnh báo |
| P15-02 | Above boundary | actual=10, held=3, min=6 | available=7; không cảnh báo |
| P15-03 | Happy stocktake | actual 10→12, held4, reason hợp lệ | Before10/after12; available8; giữ held4 |
| P15-04 | Validation | count=-1, count3 khi held4, reason trống | Không ghi history/kho; field lỗi |
| P15-05 | Concurrent count | Giữ hàng tăng trong lúc gửi count | Không phá invariant; conflict hoặc tính lại dưới lock |
| P15-06 | Permission | Nhân viên A đọc/export store B | Bị từ chối; không có dữ liệu file |
| P15-07 | Empty data | Store không có low-stock | UI và file header-only hợp lệ |
| P15-08 | Export filter | Filter SKU/store, nhiều page | Dữ liệu đúng filter/scope; không chỉ lấy UI page |

## 20. Definition of Done

- [ ] UC52/56/57 hoạt động với scoped UI và file xuất.
- [ ] Low-stock query đúng available và boundary có test.
- [ ] Count không thay held, có history/audit và không overwrite stale value.
- [ ] Không phát sinh schema/workflow kiểm kê ngoài thiết kế.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Nhân viên kiểm kê an toàn, cảnh báo tồn thấp đúng lượng có thể bán và xuất được inventory trong scope. Query chuẩn được sẵn sàng cho báo cáo.

## 22. Handoff To Next Phase

- Phase28 được tái sử dụng findLowStock/filter/export contract đã kiểm chứng.
- Ghi endpoint kiểm kê/export thực tế, format và ý nghĩa timestamp/filter.
- Bàn giao dataset equality, permission và concurrent-count để regression ở Phase32.
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
