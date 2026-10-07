# Phase 13 — Nhà cung cấp và phiếu nhập DRAFT

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase04](Phase04.md), [Phase06](Phase06.md), [Phase09](Phase09.md). Complexity: MEDIUM. Risk: MEDIUM.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Hoàn thiện tra cứu/quản trị nhà cung cấp và tạo, chỉnh sửa, xem chi tiết phiếu nhập DRAFT trong phạm vi cửa hàng được phép; không thay đổi tồn kho.

## 2. Why This Phase Exists

Supplier CRUD/deactivate và PurchaseOrder create/read đã tồn tại. Phần còn thiếu là tìm kiếm, màn hình nhân viên, chỉnh sửa DRAFT, validation các dòng và kiểm tra actor/phạm vi.

## 3. Current State

- SupplierServiceImpl đã create/get/update/deactivate; không cần viết lại CRUD đang đúng.
- PurchaseOrderServiceImpl.create lưu header DRAFT trước khi kiểm tra hết SKU, tính unitPrice × quantity và lưu total; getById/getAll tồn tại, chưa có update DRAFT.
- PurchaseOrderRequest chỉ @NotEmpty ở list; item thiếu @Valid cascade và positive quantity, unitPrice chỉ @NotNull; employee không hợp lệ có thể thành createdBy null.
- Route hiện có: /api/suppliers CRUD; POST/GET /api/purchase-orders và GET /{id}; PUT /{id}/confirm thuộc Phase14.
- Không có template, JS/CSS hoặc màn hình nhân viên.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase04/06/09 đã COMPLETED, tài khoản nhập hàng và scope cửa hàng hoạt động.
- [ ] Phase01 có rollback/error contract; supplier, SKU và cửa hàng test tồn tại.
- [ ] D15 đã ghi quyết định mã phiếu; không tự thêm mã prefix khi ID generated đã thỏa yêu cầu.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Tìm nhà cung cấp theo các trường UC43, trạng thái; cập nhật/khóa nhà cung cấp với lịch sử được giữ.
- Bổ sung chỉnh sửa header/items DRAFT, kiểm tra toàn bộ input trước mutation và lưu nguyên tử.
- Màn hình danh sách/chi tiết nhà cung cấp và form/chi tiết phiếu nhập, validation và scope.

### OUT OF SCOPE

- Xác nhận phiếu, tăng tồn và lịch sử xác nhận: Phase14.
- Điều chuyển, kiểm kê, báo cáo nhập hàng và nhà cung cấp ngoài thiết kế.

## 6. Requirements Covered

- DOCX hệ thống §3.4.5: UC43, UC44, UC45, UC48; đây là Phase owner.
- §2.1.5: QLNH-QĐ1–4 (mã duy nhất, giữ lịch sử, đủ supplier/store/SKU/quantity, chỉ DRAFT được sửa).
- §4.1.2: Supplier, PurchaseOrder, PurchaseOrderItem; §4.2: FK, quantity/price và tính toàn vẹn lịch sử.
- SYS09/SYS10: kiểm tra ID/mã và createdAt hiện có trước khi đề xuất thêm field.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [SupplierServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/SupplierServiceImpl.java)
- [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java)
- [SupplierRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/SupplierRestController.java)
- [PurchaseOrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/PurchaseOrderRestController.java)
- [PurchaseOrderRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/purchase/PurchaseOrderRequest.java)
- [PurchaseOrderItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/purchase/PurchaseOrderItemEntity.java)
- [PurchaseOrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java)
- [SupplierMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/purchase/SupplierMapper.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java)
- [PurchaseOrderService.java](../../src/main/java/com/thinh/cosmetic/service/purchase/PurchaseOrderService.java)
- [PurchaseOrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/PurchaseOrderRestController.java)
- [PurchaseOrderRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/purchase/PurchaseOrderRequest.java)
- [SupplierRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/SupplierRepository.java)
- [SupplierRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/purchase/SupplierRequest.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- Dự kiến `src/test/java/com/thinh/cosmetic/service/PurchaseOrderDraftServiceTest.java` và PostgreSQL integration test cho create/update rollback.
- Request/response chỉnh sửa DRAFT chỉ tạo riêng nếu contract hiện có không phù hợp; màn hình/route UI theo D01.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

Giữ bảng và FK hiện có. Kiểm tra dữ liệu trùng `(purchase_order_id, sku_id)` trước khi đề xuất unique pair theo schema thiết kế; không đổi surrogate PK. Bổ sung CHECK quantity > 0, unit_price/subtotal/total_amount ≥ 0 bằng migration Phase01 nếu chưa được đảm bảo. Việc nâng created_by lên NOT NULL chỉ sau khi kiểm kê/backfill dữ liệu lịch sử có bằng chứng; không gán nhân viên bất kỳ.

## 11. Backend Tasks

- [ ] Bổ sung search nhà cung cấp với parameter rõ ràng, pagination và trạng thái; giữ endpoint CRUD hiện tại khi tương thích.
- [ ] Kiểm tra email/phone khi được cung cấp, name bắt buộc; deactivate phải giữ liên kết PurchaseOrder.
- [ ] Loại employeeId mặc định; kiểm tra principal có quyền nhập hàng và receivingStore trong scope.
- [ ] Thêm update DRAFT trong service/controller; từ chối mọi status khác DRAFT, tính lại total từ dòng server-side.
- [ ] Validate list không rỗng, từng SKU tồn tại/được dùng, quantity > 0, unitPrice không âm; xử lý SKU trùng nhất quán với schema đã chốt.
- [ ] Kiểm tra supplier/store active cho phiếu mới; ghi audit create/update/deactivate với actor.
- [ ] Bao transaction toàn header/items; lỗi ở dòng sau không để header hoặc dòng trước commit.
- [ ] Bổ sung supplierId/storeId trong DTO chi tiết nếu form update cần ID; không dùng display name làm khóa.

## 12. Frontend Tasks

- [ ] Tạo danh sách nhà cung cấp có tìm kiếm, trạng thái, pagination, form tạo/sửa và xác nhận deactivate.
- [ ] Tạo form phiếu DRAFT: supplier/store từ dữ liệu được phép, chọn SKU, quantity, unitPrice và tổng tính để xem trước.
- [ ] Tạo chi tiết phiếu với mã/ID, actor, createdAt, status, từng dòng và total; DRAFT mới hiển thị sửa.
- [ ] Giữ dữ liệu form khi lỗi; đánh dấu dòng sai; không dùng tiền tổng do client gửi làm dữ liệu lưu.
- [ ] Chỉ liên kết hành động confirm sang capability Phase14 sau khi report Phase14 COMPLETED.

## 13. Validation & Business Rules

- Mỗi phiếu có supplier, receivingStore, actor hợp lệ và ít nhất một dòng; quantity dương, giá/tổng không âm.
- Chỉnh sửa chỉ DRAFT; DRAFT không tăng actual/held stock.
- Nhà cung cấp đã có giao dịch chỉ deactivate; giữ lịch sử và không cascade-delete phiếu.
- Tên nhà cung cấp không tự coi unique nếu thiết kế chỉ yêu cầu mã unique.

## 14. Security Requirements

- Nhân viên nhập hàng được create/update DRAFT và tra cứu phiếu theo receivingStore trong scope; Supplier là danh mục chung, CRUD/tra cứu theo permission Phase04, không tự thêm quan hệ Supplier–Store.
- Customer và nhân viên không có quyền nhập hàng bị từ chối trên /api/suppliers và /api/purchase-orders.
- Không cho đổi receivingStore sang cửa hàng ngoài scope qua update body.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- Supplier/store/SKU/phiếu không tồn tại trả lỗi không tìm thấy theo contract; inactive không dùng lập mới.
- Sai quantity/price/list rỗng trả validation; sửa phiếu đã confirmed trả conflict.
- Lỗi persistence/constraint phải rollback toàn phiếu và hiện thông báo có thể thử lại.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Tái sử dụng catalog SKU Phase06, store Phase09 và audit/RBAC Phase04.
- Phase14 dùng cùng header/items sau khi DRAFT valid; không sao chép logic tính tổng.
- Phase29 chỉ đọc lịch sử nhập; không bổ sung thống kê trong Phase này.

## 17. Implementation Order

1. Đọc source và report dependency, kiểm tra D15 cùng schema hiện hữu.
2. Xác lập DTO search/detail/update và kiểm tra quyền/validation.
3. Thêm update DRAFT và transaction regression tests.
4. Nối màn hình supplier/phiếu với endpoint đã kiểm thử.
5. Kiểm tra UI/HTTP/data/audit và hoàn thiện handoff.

## 18. Verification

- [ ] Create/update hợp lệ lưu đúng header/items/total và không đổi inventory.
- [ ] Invalid SKU ở dòng cuối rollback toàn phiếu; sửa CONFIRMED bị từ chối.
- [ ] Supplier deactivate vẫn xem được lịch sử phiếu; không cho lập mới bằng inactive supplier.
- [ ] Nhân viên khác cửa hàng/customer/anonymous không đọc hoặc sửa ngoài quyền.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P13-01 | Happy path DRAFT | 2 SKU, qty 2×100 và 3×200 | DRAFT total 800; inventory không đổi |
| P13-02 | Edit DRAFT | Đổi qty, supplier/store hợp lệ | Items và total đổi cùng transaction |
| P13-03 | Validation | qty 0/-1, giá âm hoặc items=[] | Lỗi field; không lưu header/items |
| P13-04 | Late invalid SKU | Dòng 1 hợp lệ, dòng 2 ID không tồn tại | Rollback; không có phiếu dở dang |
| P13-05 | Permission | Customer hoặc receivingStore ngoài scope | Bị từ chối; không mutation |
| P13-06 | Status boundary | Sửa phiếu CONFIRMED | Conflict; lịch sử và tồn không đổi |
| P13-07 | Empty search | Keyword không khớp | Page rỗng, UI empty state |
| P13-08 | Deactivation | Supplier đã có phiếu → deactivate | Supplier inactive; phiếu vẫn đọc được |

## 20. Definition of Done

- [ ] UC43/44/45/48 có luồng staff end-to-end và audit.
- [ ] CRUD đang đúng được tái sử dụng; update chỉ DRAFT và không làm tăng kho.
- [ ] Invalid input/late error/permission/empty search đã kiểm chứng.
- [ ] Không triển khai confirm/history/export thuộc Phase14/29.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Nhân viên nhập hàng quản lý được nhà cung cấp và phiếu DRAFT hợp lệ trong scope; chi tiết phiếu thể hiện dữ liệu đã lưu. Kho chưa thay đổi bởi các thao tác Phase này.

## 22. Handoff To Next Phase

- Phase14 nhận supplier/store/SKU valid, phiếu DRAFT và API create/read/update đã kiểm thử.
- Ghi route update thực tế và cấu trúc DTO; giữ /api/purchase-orders/{id}/confirm cho Phase14.
- Bàn giao cách xác định actor/scope và transaction/error/audit contract dùng chung.
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
