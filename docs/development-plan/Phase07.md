# Phase 07 — Quản lý ảnh và tích hợp Cloudinary

## 1. Objective

Tạo luồng quản lý ảnh sản phẩm theo UC36, dùng ProductImageEntity/repository hiện có, tích hợp upload và ảnh chính cho product/SKU.

Complexity: MEDIUM. Risk: MEDIUM. Hoàn thiện dữ liệu ảnh và UI quản trị; không dựng storefront.

## 2. Why This Phase Exists

ProductImageEntity đã lưu URL/product/isPrimary/sortOrder; cart và wishlist đã đọc ảnh chính. Chưa có upload service, API quản lý ảnh hoặc Cloudinary integration, nên không thể quản lý dữ liệu ảnh từ UI.

Thiết kế cho phép ảnh gắn SKU và URL dài500; source thiếu SKU FK và dùng độ dài mặc định.

## 3. Current State

- [ProductImageEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductImageEntity.java) có product FK, imageUrl, isPrimary, sortOrder.
- [ProductImageRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductImageRepository.java) có find primary và list theo sortOrder.
- CartServiceImpl/WishListServiceImpl đọc primary URL; dữ liệu không có ảnh trả null.
- Không có Cloudinary dependency/config/service, endpoint multipart hay thư mục static/template.
- Product không có publication gate kiểm tra ảnh chính.
- Ảnh UI DOCX §5.3.2–3 yêu cầu card và gallery/thumbnail; rendering thuộc Phase10.

## 4. Preconditions

- [ ] Phase06 hoàn thành contract product/SKU và permission catalog.
- [ ] D01 chốt công nghệ UI; chỉ tạo uploader/view theo lựa chọn đã ghi.
- [ ] D04 chốt SKU-image relation nếu cần giải quyết khác biệt schema.
- [ ] Phase01 có cách cung cấp secrets theo môi trường; có tài khoản/config Cloudinary cho integration test.
- [ ] Kiểm tra image rows legacy, URL dài và nhiều primary trước thêm constraint.

## 5. Scope

### IN SCOPE

- Upload, gắn ảnh vào product/SKU, sắp xếp, chọn primary và loại ảnh.
- Validation file, quyền và xử lý lỗi Cloudinary/database.
- API/UI quản trị ảnh và contract gallery cho consumer.
- Kiểm chứng điều kiện ảnh chính khi sản phẩm được công bố.

### OUT OF SCOPE

- Tạo ảnh bằng AI, thay bộ nhận diện hoặc thiết kế sản phẩm.
- Customer storefront/gallery rendering Phase10.
- Upload avatar, chứng từ, kho ảnh dùng chung ngoài phạm vi thiết kế.

## 6. Requirements Covered

- Hệ thống §3.4.2 UC36; §4.1.2 ProductImage; §4.2 QLSP-QĐ6.
- QLSP-QĐ6: product công bố phải có ít nhất ảnh chính; không ngừng/xóa ảnh làm product bán thiếu dữ liệu bắt buộc.
- UI §5.3.1–3: primary card image, gallery và thumbnail.
- Phase07 sở hữu UC36, tích hợp publication guard với output Phase05/06; không mở rộng UC31 thành làm lại CRUD.

## 7. Files To Inspect First

- [ProductImageEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductImageEntity.java), [ProductImageRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductImageRepository.java).
- [ProductSkuEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java), [ProductEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductEntity.java).
- [ProductServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java): điểm cập nhật status.
- [CartServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/CartServiceImpl.java), [WishListServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/WishListServiceImpl.java): contract primary URL.
- [pom.xml](../../pom.xml), [application.properties](../../src/main/resources/application.properties).
- Đọc DTO/API SKU và layout đã được Phase06/D01 bàn giao.

## 8. Files Expected To Modify

- ProductImageEntity/repository cho SKU link và URL length phù hợp.
- ProductServiceImpl hoặc publication policy để bảo vệ ảnh bắt buộc khi active/public.
- Config build/runtime chỉ bổ sung Cloudinary khi thực hiện Phase07, không phải khi tạo roadmap.
- Product response/gallery projection; consumer chỉ sửa khi contract cần tương thích.
- Migration theo Phase01, giữ product/SKU/image IDs.

## 9. Files Expected To Create

Các file dự kiến chưa tồn tại:

- `src/main/java/com/thinh/cosmetic/service/catalog/ProductImageService.java` và implementation.
- `src/main/java/com/thinh/cosmetic/rest/catalog/ProductImageRestController.java`.
- Config/adapter Cloudinary, DTO image/gallery và test upload/service dưới convention hiện tại.
- Uploader/gallery editor staff theo D01; không mặc định Thymeleaf hoặc SPA.

## 10. Database Changes

- Bổ sung optional SKU FK theo bảng ProductImage sau D04; SKU phải thuộc chính product của ảnh.
- Mở rộng imageUrl tới500 theo design, giữ URL legacy.
- TECHNICAL RECOMMENDATION: nếu cần đảm bảo duy nhất primary bằng PostgreSQL, dùng unique partial index theo product khi is_primary=true; không unique(product_id,is_primary) vì sẽ cấm nhiều ảnh phụ.
- Nếu provider identifier cần cho xóa asset, ghi TECHNICAL RECOMMENDATION và lý do; chỉ thêm metadata tối thiểu sau kiểm tra Cloudinary contract.
- Kiểm tra/fix nhiều primary bằng quyết định giữ ảnh nào; không tùy ý xóa row/asset.
- Migration có backup và kiểm chứng query primary/list; không drop/recreate.

## 11. Backend Tasks

- [ ] Thêm upload adapter Cloudinary/config theo secrets pattern Phase01; không hardcode credential.
- [ ] Thêm API quản lý ảnh theo product: upload/list/change-primary/reorder/delete, ghi route thực tế trong report.
- [ ] Kiểm file rỗng/không phải ảnh, MIME và nội dung; giới hạn upload cấu hình. Đây là TECHNICAL RECOMMENDATION để bảo vệ integration.
- [ ] Kiểm product tồn tại, SKU thuộc product và quyền catalog trước upload hoặc mutation.
- [ ] Lưu URL/order/primary từ response provider, không tin client gán ảnh của product khác.
- [ ] Thay primary trong giao dịch, không tạo hai primary khi thao tác đồng thời.
- [ ] Khi xóa ảnh chính của product đang public, yêu cầu ảnh thay thế hoặc ngừng công bố theo rule đã ghi.
- [ ] Xử lý upload thành công nhưng DB thất bại bằng compensation/cleanup; không coi giao dịch DB rollback được Cloudinary.
- [ ] Xử lý delete asset thất bại có trạng thái/lịch retry kỹ thuật ghi rõ; không báo thành công mất ảnh khi DB chưa cập nhật.
- [ ] Tích hợp publication guard và gallery projection, giữ lookup primary hiện hữu cho cart/wishlist.
- [ ] Ghi audit thao tác ảnh, không ghi secrets/base64/file bytes vào log.

## 12. Frontend Tasks

- [ ] Tạo uploader/editor trong màn hình catalog đã chọn ở D01.
- [ ] Hiển thị preview, primary badge, sort order và SKU association có kiểm tra.
- [ ] Nối upload/remove/change-primary vào API; có loading/progress, disable action khi đang gửi và retry lỗi rõ ràng.
- [ ] Render field/provider errors; không chỉ toast thành công khi DB/provider chưa hoàn tất.
- [ ] Có empty gallery và confirm xóa, bảo vệ ảnh chính bắt buộc.
- [ ] Kiểm desktop/mobile editor theo layout được duyệt; customer gallery thuộc Phase10.

## 13. Validation & Business Rules

- Ảnh phải gắn product hợp lệ, optional SKU cùng product.
- URL và sort order tuân thủ độ dài/kiểu dữ liệu design.
- Product public cần ảnh chính; primary switch không làm mất ảnh hoặc đổi product.
- File size/type dùng cấu hình đã ghi, không tự coi extension là xác minh an toàn.
- Ảnh order snapshot không bị sửa tùy tiện khi catalog thay đổi; chỉ current catalog gallery thay đổi.
- Provider ID/URL không cho phép dùng thao tác xóa asset ngoài phạm vi product.

## 14. Security Requirements

- Chỉ staff có permission quản lý ảnh catalog được upload/change/delete.
- Customer chỉ đọc URL/gallery công bố, không gọi mutation hoặc nhận credential.
- Upload tuân CSRF/token policy D01/Phase02; không tự tắt bảo vệ vì multipart.
- Audit principal từ server; adapter chỉ dùng credential môi trường, không trả trong error response.

## 15. Error Handling

- Product/SKU không tồn tại hoặc liên kết sai: not found/field error.
- File rỗng, MIME sai, quá giới hạn: reject trước lưu DB/provider.
- Cloudinary timeout/error: thông báo có thể retry, không tạo row URL giả.
- DB conflict/multiple primary: rollback và kiểm chứng compensation.
- UI không mất thứ tự/primary khi retry thất bại.

## 16. Integration Points

- Phase06: product/SKU ID và permission catalog.
- Phase05: status/publication mutation; phối hợp thay guard, không viết lại CRUD.
- Phase10: gallery/primary projection; Phase11/17: primary URL.
- Phase01: config/errors/migration; Phase04: audit.
- Shared ProductResponse/ProductServiceImpl/primary lookup phải được bàn giao trước consumer sửa.

## 17. Implementation Order

1. Kiểm tra legacy image data và Cloudinary config.
2. Migration URL/SKU link và primary invariant.
3. Adapter upload/delete + test lỗi ngoài hệ thống.
4. Image service/controller với ownership product, transaction/compensation.
5. Publication guard và projection.
6. UI quản trị ảnh sau D01; test integration và handoff.

## 18. Verification

- Build bằng Java21 và unit test adapter/service với mocked provider.
- Integration test Cloudinary chỉ dùng tài khoản/môi trường thử được cấu hình; ghi rõ nếu credential chưa có.
- PostgreSQL test SKU FK và primary concurrency.
- HTTP/UI upload, reorder, primary, delete và error states.
- Kiểm card/gallery projection và cart/wishlist primary lookup không bị phá.
- Xóa asset thử nghiệm sau test, không sử dụng production asset.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P07-01 | Happy path | File ảnh hợp lệ cho product | Lưu URL, hiển thị preview/gallery |
| P07-02 | File validation | Empty/non-image/quá giới hạn | Reject, không row/asset rác |
| P07-03 | Wrong SKU | SKU thuộc product khác | Reject liên kết |
| P07-04 | Primary boundary | Hai request chọn primary | Chỉ một primary, không mất ảnh |
| P07-05 | Published product | Xóa ảnh chính cuối cùng | Reject hoặc thay thế hợp lệ, không public thiếu ảnh |
| P07-06 | Provider failure | Cloudinary timeout | Error/retry rõ, DB không URL giả |
| P07-07 | DB failure | Upload xong nhưng save lỗi | Compensation được kiểm chứng |
| P07-08 | Permission/empty | Customer upload; gallery rỗng | 401/403; empty state rõ |

## 20. Definition of Done

- [ ] UC36 quản lý ảnh API/UI hoàn chỉnh theo quyền.
- [ ] Cloudinary test/config có kết quả, không hardcode secret.
- [ ] Primary, SKU ownership và publication guard được test.
- [ ] Migration giữ URL/ID và xử lý legacy có quyết định.
- [ ] Provider/DB failure và compensation được kiểm chứng.
- [ ] D01/D04 phần cần thiết đã đóng; thiếu credential phải ghi BLOCKED thay COMPLETED.
- [ ] Contract gallery/primary và routes thực tế được bàn giao.
- [ ] Không làm customer storefront hoặc các upload ngoài catalog.

## 21. Expected Result After This Phase

Product/SKU có dữ liệu ảnh quản lý được, ảnh chính nhất quán và lỗi provider được xử lý. Consumer tiếp theo có gallery/primary contract; storefront vẫn thuộc Phase10.

## 22. Handoff To Next Phase

- Phase10 có thể render gallery/thumbnail và card image từ contract sau khi Phase07 hoàn thành.
- Phase11/17 dùng primary URL hiện hữu hoặc mapping mới đã được ghi.
- Ghi route upload/ảnh, giới hạn file, secret names không kèm value, primary invariant và cleanup strategy.
- Không giả định ảnh/Cloudinary tồn tại khi Phase07 còn thiếu config hoặc chưa kiểm chứng.

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
