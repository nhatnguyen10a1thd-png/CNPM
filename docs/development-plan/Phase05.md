# Phase 05 — Hoàn thiện quản trị catalog và taxonomy

## 1. Objective

Hoàn thiện phần quản trị danh mục sản phẩm hiện có: product cơ bản, category, brand và attribute, với validation, bảo toàn lịch sử và quyền truy cập.

Complexity: MEDIUM. Risk: MEDIUM. Không viết lại CRUD đang hoạt động.

## 2. Why This Phase Exists

UC29, UC31–34 đã có một phần backend nhưng chưa chạy end-to-end: API product/category/brand tồn tại; attribute mới có entity/repository; không có UI quản trị. Xóa vật lý có thể làm mất liên kết lịch sử; request thiếu validation.

Phase này tạo nền dữ liệu catalog để SKU ở Phase06 có thể sử dụng. Việc publish ra customer và điều kiện ảnh được tích hợp ở Phase07/10.

## 3. Current State

- Luồng hiện có: `/api/products` → ProductServiceImpl → ProductRepository → ProductEntity; tương tự `/api/categories` và `/api/brands`.
- POST/GET/PUT/DELETE đã tồn tại; getAll trả toàn bộ danh sách, chưa search/pagination.
- ProductServiceImpl.delete gọi deleteById. Brand/category cũng có CRUD; không cần dựng lại từ đầu.
- ProductRequest/BrandRequest/CategoryRequest chưa có constraint dù controller đặt @Valid.
- ProductMapper.updateEntity có thể ghi null cho field đơn giản; brand/category chỉ đổi khi ID khác null. Phải thống nhất contract cập nhật.
- Thiết kế Category tự tham chiếu parent; source Category không có parent, Brand lại có parent. Attribute thiếu nhóm thuộc tính. D04 là gate.
- Java21 + H2 đã pass 9 test ở baseline; chưa có test catalog hay UI/HTTP/PostgreSQL end-to-end.

## 4. Preconditions

- [ ] Phase04 hoàn thành, principal/permission và audit-write contract có thể sử dụng.
- [ ] Đọc [Audit](00_PROJECT_AUDIT.md) và [Master Roadmap](01_MASTER_ROADMAP.md).
- [ ] Chốt D04 về taxonomy và D16 về vocabulary thuộc tính trước thay đổi schema liên quan.
- [ ] Chốt D01 trước tạo UI; không tự chọn framework hoặc layout admin.
- [ ] Dữ liệu hiện hữu được kiểm tra trước constraint tên/FK/enum.

## 5. Scope

### IN SCOPE

- Hoàn thiện validation và cập nhật của CRUD product/category/brand đang có.
- Tìm kiếm/tra cứu danh mục dành cho staff UC29; quản lý attribute UC34.
- Deactivate/bảo toàn bản ghi đã được SKU/giao dịch tham chiếu.
- Giao diện quản trị cần thiết theo UC, sau D01.

### OUT OF SCOPE

- SKU, giá, barcode thuộc Phase06; ảnh/upload thuộc Phase07.
- Home/customer search/filter/detail thuộc Phase10.
- Promotion, AI recommendation, đổi kiến trúc hoặc database.

## 6. Requirements Covered

- File hệ thống: §3.4.2 UC29, UC31, UC32, UC33, UC34; §4.1.2 Product, Category, Brand, Attribute; §4.2 QLSP-QĐ1–4.
- QLSP-QĐ1: thông tin/tên, brand/category hợp lệ; QLSP-QĐ2–4: giữ lịch sử và liên kết khi thay đổi/ngừng kinh doanh.
- UI DOCX không có mockup admin; màn hình quản trị dựa trên UC, không được mô tả là khớp mockup.
- Chủ sở hữu requirement: Phase05. Phase06/07/10 chỉ mở rộng hoặc tích hợp output này.

## 7. Files To Inspect First

- [ProductRestController.java](../../src/main/java/com/thinh/cosmetic/rest/catalog/ProductRestController.java), [ProductServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java).
- [CategoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/CategoryServiceImpl.java), [BrandServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/BrandServiceImpl.java).
- [ProductRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/catalog/ProductRequest.java), [ProductMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/catalog/ProductMapper.java).
- [CategoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/CategoryEntity.java), [BrandEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/BrandEntity.java).
- [AttributeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/AttributeEntity.java), [AttributeRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/AttributeRepository.java).
- Đọc DTO/mapper/repository catalog liên quan và completion report Phase04 trước sửa.

## 8. Files Expected To Modify

- Controller/service/DTO/mapper catalog hiện có khi cần validation, permission, deactivate và search.
- Entity/repository Category/Brand/Attribute/Product chỉ sửa phần được D04 phê duyệt hoặc constraint đã rõ.
- Migration theo convention Phase01; audit integration theo Phase04.
- Không sửa SKU, ảnh hoặc order trừ test liên kết cần thiết.

## 9. Files Expected To Create

Các đường dẫn dự kiến chưa tồn tại; chỉ tạo khi cần:

- `service/catalog/AttributeService.java`, implementation và `rest/catalog/AttributeRestController.java`.
- DTO attribute request/response trong `domain/dto/.../catalog/`.
- Test catalog trong `src/test/java/com/thinh/cosmetic/service/`.
- View/controller/static của admin catalog chỉ xác định theo D01 đã được ghi trong Master.

## 10. Database Changes

Dự kiến, không thực thi trong lần tạo roadmap:

- Bổ sung ràng buộc tên bắt buộc/unique nơi thiết kế quy định; kiểm tra null, trùng và cách chuẩn hóa trước migration.
- Quan hệ parent Category và group Attribute phụ thuộc D04. Không tự chuyển Brand.parent thành Category.parent.
- Product/description/ingredients/uses cần độ dài phù hợp thiết kế, tránh mặc định VARCHAR255 làm mất nội dung.
- Khi đổi Brand.status từ ordinal phải mapping từng giá trị hiện hữu; không drop/recreate.
- Giữ PK/FK hiện có; không phát minh product business-code ngoài ID khi thiết kế chỉ định MaSP là PK.
- Migration có bước kiểm tra dữ liệu, backup và kiểm chứng giữ ID/liên kết; không reset database.

## 11. Backend Tasks

- [ ] Kiểm tra lại CRUD hiện hữu và bổ sung test trước sửa hành vi cập nhật/xóa.
- [ ] Thêm required/length/enum/FK validation vào request và service theo thiết kế; dùng error contract Phase01.
- [ ] Quy định rõ PUT đầy đủ hay cập nhật từng phần trong contract; test field null và brand/category ID null, không để mapper làm mất dữ liệu ngoài ý muốn.
- [ ] Hoàn thiện tra cứu staff theo tên/brand/category/status, pagination thay findAll khi cần danh sách lớn.
- [ ] Triển khai attribute service/controller dùng repository có sẵn, kiểm tra trùng tên và nhóm theo D04/D16.
- [ ] Thay thao tác xóa gây mất lịch sử bằng deactivate/deny delete theo QLSP-QĐ2–4; giữ linked SKU/order/purchase.
- [ ] Áp permission catalog và ghi audit cho mutation; giữ read public/staff phân biệt.
- [ ] Xử lý category cycle/self-parent nếu D04 thông qua hierarchy.
- [ ] Ghi giới hạn publish: Phase05 không tự chứng nhận sản phẩm sẵn bán khi chưa có SKU/ảnh.

## 12. Frontend Tasks

- [ ] Sau D01, tạo trang staff tra cứu product và form product/category/brand/attribute bằng công nghệ đã chọn.
- [ ] Hiển thị field errors, trạng thái active/inactive và dữ liệu quan hệ; không dùng entity trực tiếp.
- [ ] Nối form tạo/sửa/deactivate vào API/service hiện có; giữ ID sau cập nhật.
- [ ] Có confirm deactivate, empty/error/loading state và pagination cho danh sách staff.
- [ ] Kiểm tra nút/menu theo permission, đồng thời bảo vệ server.
- [ ] Chỉ dùng layout admin được duyệt; không thêm dashboard, SKU editor hoặc upload ở Phase này.

## 13. Validation & Business Rules

- Product cần tên và quan hệ brand/category hợp lệ theo QLSP-QĐ1.
- Tên unique chỉ áp nơi thiết kế yêu cầu; việc chuẩn hóa case/space phải được ghi rõ trước tạo unique index.
- Ngừng kinh doanh không phá lịch sử giao dịch; không cascade delete bản ghi đã tham chiếu.
- Parent không tự tham chiếu/cycle nếu hierarchy được chốt.
- Không đánh dấu publish đáp ứng QLSP-QĐ6 trước Phase07 có ảnh chính.

## 14. Security Requirements

- Nhân viên quản lý sản phẩm có permission tương ứng được gọi mutation catalog; chain manager chỉ theo quyền đã cấp.
- Customer không được POST/PUT/DELETE `/api/products`, `/api/categories`, `/api/brands` hoặc attribute API dự kiến.
- Public browsing đầy đủ thuộc Phase10; dữ liệu inactive cho staff không được tự lộ ra customer.
- Audit actor lấy từ principal; không nhận actor ID tùy ý từ form.

## 15. Error Handling

- Field/FK sai: validation error có field rõ ràng.
- ID không tồn tại: not found; trùng tên/ràng buộc/history-reference: conflict theo Phase01.
- Không trả nguyên exception database hoặc thông tin cấu hình.
- Failed taxonomy migration không được tự bỏ FK/unique hoặc xóa dữ liệu để vượt kiểm tra.

## 16. Integration Points

- Phase04: permission và audit-write.
- Phase06: danh sách product/attribute hợp lệ, ID và status ổn định.
- Phase07: bổ sung publication/image requirement sau này.
- Phase10: public projection chỉ active; Phase12 dùng vocabulary đã chốt.
- Shared files: ProductEntity/ProductResponse và mapper; không chỉnh đồng thời với Phase06 nếu chưa bàn giao contract.

## 17. Implementation Order

1. Đọc baseline/completion Phase04 và tái xác minh CRUD.
2. Đóng D04/D16 phần liên quan; kiểm tra dữ liệu trước migration.
3. Chuẩn hóa validation/update/deactivate và test service.
4. Thêm attribute slice và search staff.
5. Nối permission/audit, rồi dựng UI sau D01.
6. Kiểm chứng database/API/UI và ghi handoff catalog.

## 18. Verification

- Build/test bằng Java21 theo Phase01; không tự đổi version khi build bằng JDK khác lỗi.
- PostgreSQL integration cho FK/unique/enum, kiểm tra linked history còn nguyên.
- HTTP: create/read/update/deactivate, pagination, lỗi dữ liệu và access matrix.
- UI: form lỗi, danh sách rỗng, tìm kiếm không kết quả, thao tác bị từ chối.
- H2 chỉ là test hỗ trợ; chưa kiểm chứng PostgreSQL/UI thì ghi rõ trong report.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P05-01 | Happy path product | Tên và brand/category hợp lệ | Lưu đúng, đọc lại giữ quan hệ |
| P05-02 | Validation | Tên rỗng/FK không tồn tại | Reject, không tạo product |
| P05-03 | Update boundary | Field null theo contract đã chốt | Không xóa ngoài ý muốn; hành vi nhất quán |
| P05-04 | History preservation | Deactivate product có order/SKU | Lịch sử giữ ID, product ngừng bán |
| P05-05 | Permission | Customer gọi mutation | 401/403, không thay đổi dữ liệu |
| P05-06 | Empty data | Search không khớp | Danh sách rỗng có pagination hợp lệ |
| P05-07 | Duplicate | Brand/category trùng theo constraint | Conflict, không duplicate |
| P05-08 | Hierarchy | Parent=self hoặc tạo cycle | Reject nếu hierarchy được duyệt |

## 20. Definition of Done

- [ ] CRUD đang đúng được giữ; validation/update contract được test.
- [ ] Attribute và tra cứu staff chạy end-to-end theo UC.
- [ ] Linked history không bị xóa khi deactivate.
- [ ] Permission/audit hoạt động cho mutation.
- [ ] D04/D16 phần liên quan và D01 UI được ghi rõ; gate còn mở không được gọi COMPLETED.
- [ ] Build và kiểm chứng PostgreSQL/API/UI có kết quả ghi trong report.
- [ ] Không thêm task SKU/ảnh/customer browsing ngoài Phase.
- [ ] Cập nhật checkboxes và handoff bằng kết quả thực tế.

## 21. Expected Result After This Phase

Catalog quản trị có validation, tra cứu và lifecycle an toàn trên nền CRUD hiện hữu. Product/category/brand/attribute là dữ liệu nền để Phase06 sử dụng; chưa tuyên bố customer storefront hoặc product publish hoàn tất.

## 22. Handoff To Next Phase

- Phase06 có thể dùng product/attribute ID, status và taxonomy contract đã được ghi sau khi Phase05 hoàn thành.
- Routes có sẵn `/api/products`, `/api/categories`, `/api/brands` được giữ hoặc ghi thay đổi tương thích trong report; attribute route chỉ được coi tồn tại sau implementation.
- Handoff ghi quyết định D04/D16, migration đã chạy, quyền catalog và dữ liệu legacy cần lưu ý.
- QLSP-QĐ6 về ảnh chính tiếp tục tích hợp ở Phase07; không chuyển phần validation/deactivate còn thiếu sang Phase sau.

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
