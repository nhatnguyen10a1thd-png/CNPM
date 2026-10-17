# Phase 06 — SKU, giá và thuộc tính biến thể

## 1. Objective

Hoàn thiện quản lý SKU và thuộc tính biến thể trên ProductSkuEntity đã có; cung cấp contract SKU/giá cho ảnh, stock, cart và customer catalog.

Complexity: HIGH. Risk: MEDIUM. Mục tiêu là slice UC30/UC35, không dựng lại product CRUD.

## 2. Why This Phase Exists

ProductSkuEntity và SkuAttributeValueEntity tồn tại, đã được cart/order/purchase/inventory tham chiếu; chưa có SKU service/controller quản trị. ProductResponse chưa trả SKU, giá hay ảnh.

Design yêu cầu listPrice/barcode và thuộc tính tổng quát; source thêm shade/volume nhưng thiếu các trường design. D04 phải được giải quyết trước migration.

## 3. Current State

- [ProductSkuEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java): skuCode unique/not-null, price DECIMAL(18,2), variantName, shade, volume, status và product FK.
- [ProductSkuRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductSkuRepository.java) hiện chỉ kế thừa JpaRepository.
- SkuAttributeValueEntity dùng surrogate ID, nhưng chưa unique pair sku_id/attribute_id.
- AttributeRepository/SkuAttributeValueRepository tồn tại nhưng chưa có luồng CRUD dùng chúng.
- ProductResponse chỉ gồm product/brand/category và nội dung; chưa đủ dữ liệu chọn biến thể trên detail.
- Chưa có frontend hay test SKU. PostgreSQL dữ liệu thật chưa kiểm chứng ở baseline.

## 4. Preconditions

- [ ] Phase05 hoàn thành taxonomy/attribute, validation và permission catalog.
- [ ] D04 được chốt về listPrice/barcode, shade/volume và biểu diễn attribute.
- [ ] D16 chốt vocabulary skin/need/variant dùng chung khi Phase này lưu các giá trị tương ứng.
- [ ] D01 được chốt trước UI; kiểm tra completion Phase05 và tránh sửa shared ProductResponse đồng thời.
- [ ] Có kế hoạch xử lý SKU/attribute pair trùng hoặc giá legacy không hợp lệ.

## 5. Scope

### IN SCOPE

- Tra cứu SKU UC30; tạo/sửa/deactivate biến thể UC35.
- Validation skuCode, giá, quan hệ product/attribute; contract response không serialize entity.
- Migration được phê duyệt cho trường design thiếu và uniqueness attribute pair.
- UI SKU staff và handoff dữ liệu cho các consumer.

### OUT OF SCOPE

- Upload/ảnh chính Phase07; tạo stock/hold Phase08.
- Customer list/detail Phase10; cart/checkout Phase17–19.
- Tự bỏ shade/volume, tạo hệ thống variant mới hoặc thay khóa chính toàn bộ.

## 6. Requirements Covered

- Hệ thống §3.4.2: UC30 tra cứu SKU, UC35 quản lý SKU.
- §4.1.2 ProductSku, SkuAttributeValue, Attribute; §4.2 QLSP-QĐ2, QLSP-QĐ3, QLSP-QĐ5.
- QLSP-QĐ5: một product có nhiều SKU/biến thể; lịch sử SKU được bảo toàn.
- UI §5.3.2–3 sử dụng giá/dung tích/biến thể; rendering thuộc Phase10.
- Phase06 sở hữu UC30/35; consumer không dựng lại SKU CRUD.

## 7. Files To Inspect First

- [ProductSkuEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java), [ProductSkuRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductSkuRepository.java).
- [SkuAttributeValueEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/SkuAttributeValueEntity.java), [SkuAttributeValueRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/SkuAttributeValueRepository.java).
- [AttributeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/AttributeEntity.java).
- [ProductResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/catalog/ProductResponse.java), [ProductMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/catalog/ProductMapper.java).
- [CartServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/CartServiceImpl.java), [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java): đọc cách dùng SKU/price, chưa sửa workflow.
- [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java) và InventoryEntity: kiểm tra FK/historical usage.

## 8. Files Expected To Modify

- ProductSkuEntity/ProductSkuRepository và SkuAttributeValueEntity/repository cho query/constraint được duyệt.
- ProductResponse/ProductMapper hoặc projection catalog chuyên dụng nếu cần tách dữ liệu admin/public.
- Migration theo Phase01; không thay enum/ID khiến cart/order legacy mất tham chiếu.
- Service catalog nền chỉ sửa để tái sử dụng query/validation cần thiết.

## 9. Files Expected To Create

Dự kiến chưa tồn tại:

- `src/main/java/com/thinh/cosmetic/service/catalog/ProductSkuService.java` và implementation.
- `src/main/java/com/thinh/cosmetic/rest/catalog/ProductSkuRestController.java`.
- DTO request/response SKU và attribute-value dưới package catalog, mapper nếu cần.
- Test SKU service/repository và UI staff theo D01; route/view mới phải ghi trong completion report.

## 10. Database Changes

- D04 quyết định bổ sung listPrice/barcode và mapping shade/volume ↔ attribute; giữ dữ liệu cũ tới khi mapping được xác minh.
- Dự kiến unique constraint trên (sku_id, attribute_id), giữ surrogate ID hiện có. Báo cáo duplicate trước; không tự chọn một value để xóa.
- Kiểm tra giá hiện hữu rồi bổ sung constraint không âm và nullability theo bảng design.
- Nếu barcode có uniqueness hoặc listPrice quan hệ với price chưa rõ trong design, ghi quyết định thay vì tự đặt rule.
- Tạo migration bảo toàn SKU ID và FK cart/order/inventory/purchase; không drop/recreate.

## 11. Backend Tasks

- [ ] Thống kê SKU hiện hữu, duplicate skuCode/attribute pair và consumer field dùng price/shade/volume.
- [ ] Thực hiện phần D04 đã được duyệt; kiểm tra migration với fixture có giao dịch legacy.
- [ ] Thêm request SKU có required productId/skuCode/price và cascade validation cho danh sách attribute values.
- [ ] Thêm tra cứu theo skuCode/product/status và pagination cho staff UC30.
- [ ] Thêm create/update/deactivate, xác minh product/attribute tồn tại và active theo contract catalog.
- [ ] Kiểm tra attribute thuộc đúng SKU/product; từ chối pair lặp trong cùng request và conflict đồng thời.
- [ ] Ngừng kinh doanh thay vì xóa SKU đã có giao dịch; bảo toàn giá/name snapshot trong order cũ.
- [ ] Tạo response/projection chứa SKU ID/code/name/variant/current price/status/attribute; ghi nguồn giá card khi product có nhiều SKU.
- [ ] Áp permission/audit Phase04; không nhận principal/actor qua request.
- [ ] Ghi TECHNICAL RECOMMENDATION nếu tối ưu batch projection để tránh N+1; chỉ triển khai khi có query evidence.

## 12. Frontend Tasks

- [ ] Tạo danh sách/tra cứu SKU và form SKU theo D01, dùng product/attribute data Phase05.
- [ ] Hiển thị giá, barcode/listPrice nếu được duyệt; render field errors và duplicate code conflict.
- [ ] Editor attribute không tạo nhiều row cùng thuộc tính cho một SKU.
- [ ] Form sửa/deactivate giữ ID, có confirm khi ngừng kinh doanh.
- [ ] Có empty/loading/error state, pagination và ẩn action thiếu permission.
- [ ] Không xây product detail customer hoặc image uploader trong Phase này.

## 13. Validation & Business Rules

- skuCode unique, không rỗng, độ dài tối đa50 theo entity hiện tại nếu design không điều chỉnh.
- Price dùng BigDecimal, không âm theo schema; không chuyển sang double.
- Quantity/stock không phải field SKU; stock phân theo store ở Phase08.
- Mỗi SKU gắn đúng một product và mỗi attribute có tối đa một value/pair theo design.
- Shade/volume được giữ đến khi D04 mapping có quyết định; không tự thêm hàng loạt attribute chuẩn.
- Sửa giá hiện tại không sửa order-item snapshot cũ.

## 14. Security Requirements

- Quản lý sản phẩm có quyền SKU được tạo/sửa/deactivate; customer không được dùng mutation.
- Tra cứu staff kiểm permission catalog, không mặc định public toàn bộ inactive SKU.
- Dữ liệu public chỉ active qua Phase10 projection.
- Audit ghi actor từ principal và SKU/product liên quan.

## 15. Error Handling

- Product/attribute/SKU không tồn tại: not found hoặc field-validation theo Phase01.
- Duplicate code/pair: conflict rõ ràng; transaction không lưu nửa danh sách attribute.
- Giá sai, enum sai, payload attribute rỗng/không hợp lệ: field errors.
- SKU có giao dịch không được hard-delete để tránh xử lý FK bằng xóa lịch sử.

## 16. Integration Points

- Phase05 cung cấp taxonomy và attribute catalog.
- Phase07 dùng product/SKU link cho ảnh; Phase08 dùng SKU ID ổn định.
- Phase10 nhận product+SKU projection; Phase11 wishlist dùng nguồn giá card đã ghi.
- Cart/order hiện đang đọc SKU.price: giữ field hoặc ghi mapping tương thích, không refactor workflow ở đây.
- Shared ProductResponse/Mapper phải được bàn giao trước Phase07/10 chỉnh.

## 17. Implementation Order

1. Tái xác minh source/legacy data và đóng D04/D16 phần liên quan.
2. Migration nhỏ, không mất FK; repository uniqueness/query.
3. DTO/service SKU + attribute-value với transaction và test.
4. Controller, permission/audit và response contract.
5. UI SKU staff sau D01.
6. Regression consumer giá/ID và handoff.

## 18. Verification

- Java21 build và test SKU; giữ nguyên test inventory/order/voucher baseline.
- PostgreSQL test unique SKU code/pair, cascade rollback khi một attribute sai.
- HTTP create/edit/deactivate/search và permission.
- Regression cart/order đọc đúng giá/SKU hiện tại, historical snapshots không đổi.
- UI kiểm form, duplicate conflict, nhiều SKU cùng product và no-data state.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P06-01 | Happy path | Product có hai SKU hợp lệ | Hai ID/giá/biến thể đúng |
| P06-02 | Duplicate code | skuCode đã tồn tại | Conflict, không tạo SKU thứ hai |
| P06-03 | Invalid price | price âm/null | Validation reject |
| P06-04 | Duplicate attribute | Hai value cùng attributeId | Reject atomic, không pair trùng |
| P06-05 | Invalid FK | Product/attribute không có | Reject, không orphan |
| P06-06 | History | Deactivate SKU đã đặt hàng | Order snapshot giữ nguyên |
| P06-07 | Permission | Customer gọi SKU mutation | 401/403, không ghi DB |
| P06-08 | Empty/boundary | Search rỗng; code >50 ký tự | Empty list hoặc field error đúng |

## 20. Definition of Done

- [ ] UC30/35 chạy API/UI end-to-end theo quyền.
- [ ] SKU/attribute validation và rollback có test.
- [ ] D04/D16 cần thiết đã đóng; không gọi COMPLETED khi còn gate schema/UI.
- [ ] Migration giữ SKU ID và FK lịch sử, xử lý duplicate có quyết định.
- [ ] Contract giá/variant cho consumer được ghi trong handoff.
- [ ] Build, PostgreSQL integration, permission/UI có kết quả.
- [ ] Không làm upload/stock/cart/customer-detail ngoài Phase.
- [ ] Completion report ghi mọi route/field mới, không chỉ tên class.

## 21. Expected Result After This Phase

Staff có thể tra cứu và quản lý SKU/thuộc tính; dữ liệu giá/biến thể có validation và contract ổn định. Product tiếp tục dùng CRUD hiện có; ảnh, stock và storefront chưa được coi hoàn tất.

## 22. Handoff To Next Phase

- Phase07/08 có thể dựa vào SKU ID/code/product link và status đã kiểm chứng.
- Ghi chính xác API SKU mới, response DTO, giá card nhiều SKU, mapping legacy và migration.
- Existing ProductSkuRepository/Entity được bảo toàn; route dự kiến không được coi tồn tại trước khi report hoàn thành.
- Consumer phải dùng contract đã bàn giao, không suy đoán listPrice/barcode/attribute vocabulary.

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
