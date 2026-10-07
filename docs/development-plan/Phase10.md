# Phase 10 — Home, danh sách và chi tiết sản phẩm customer

## 1. Objective

Xây ba màn hình customer đầu tiên: home, product list và product detail; kết nối dữ liệu active product/SKU/ảnh/available và search/filter/sort/pagination.

Complexity: HIGH. Risk: MEDIUM. Đây là consumer của catalog/kho đã hoàn thiện, không viết lại quản trị.

## 2. Why This Phase Exists

Source chưa có templates/static/MVC hay frontend riêng. REST product trả danh sách cơ bản, chưa search/filter/pagination, SKU/ảnh/stock. UI DOCX có ba mockup rõ ràng nhưng một số filter/sort/action thiếu business rule hoặc khác nhau giữa text và hình.

Phase này cần contract từ Phase07–09 để dựng storefront bằng công nghệ D01, giữ kiến trúc hiện tại.

## 3. Current State

- `GET /api/products` hiện findAll và ProductResponse không có SKU/image/price/stock.
- Product/brand/category có status nhưng chưa có public active filter.
- Phase06/07 dự kiến cung cấp SKU-price/gallery; Phase08/09 cung cấp available và active-store projection.
- UI §5.3.1 home, §5.3.2 list, §5.3.3 detail; tông tối/gold/cream, shared header/footer, card ảnh/giá/dung tích.
- List có brand, các mức giá, skin/needs; sort “featured” chưa có rule. D16 phải chốt vocabulary/featured.
- Detail có gallery, SKU/dung tích/qty/add cart, tabs thông tin/thành phần/hướng dẫn/review. Text nhắc Buy nhưng hình chỉ AddCart: ghi khác biệt, không tự thêm luồng mua.
- UI nói pagination/load-more nhưng hình không xác định control cụ thể; chọn một theo D01/D16 và ghi quyết định.

## 4. Preconditions

- [ ] Phase07, Phase08 và Phase09 hoàn thành data contract và verification liên quan.
- [ ] D01 chốt công nghệ frontend/shared layout; không tự thêm Thymeleaf/Bootstrap/SiteMesh.
- [ ] D16 chốt dữ liệu filter skin/needs, featured và quy tắc chọn giá/card nhiều SKU.
- [ ] D14 chốt cách xử lý Blog/link chưa có requirement; không tạo CMS.
- [ ] Có catalog fixture gồm active/inactive, nhiều SKU/ảnh, hết hàng và nhiều store.
- [ ] Public DTO không lộ staff inventory/audit fields.

## 5. Scope

### IN SCOPE

- UC04–08: list/search/filter-sort/detail/per-store available.
- Ba màn hình §5.3.1–3, header/footer/navigation và responsive cơ bản.
- Query/response công bố và UI state search/filter/pagination.
- Handoff hook wishlist/cart/review tới owner Phase sau.

### OUT OF SCOPE

- Cart mutation Phase17; wishlist/account Phase11; review UC27/28 Phase24.
- Beauty recommendation Phase12; promotion programs Phase26.
- Blog/CMS, wholesale hoặc Buy-now workflow chưa được chốt.

## 6. Requirements Covered

- Hệ thống §3.4.1 UC04, UC05, UC06, UC07, UC08.
- §4.2 KH-QĐ1–2: active product và available theo store×SKU.
- UI §5.3.1 home, §5.3.2 product list, §5.3.3 detail; ảnh nhúng tương ứng trong Audit.
- List: brand/price/skin/needs, reset, result count, sort và pagination.
- Detail: biến thể, giá, gallery, tồn, quantity và nội dung; review/cart chỉ tích hợp contract sau.
- NFR responsive/usability/browser/performance liên quan được kiểm bước đầu; benchmark đầy đủ Phase32.

## 7. Files To Inspect First

- [ProductRestController.java](../../src/main/java/com/thinh/cosmetic/rest/catalog/ProductRestController.java), [ProductServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java).
- [ProductRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductRepository.java), [ProductResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/catalog/ProductResponse.java).
- [ProductImageRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductImageRepository.java), [ProductSkuRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductSkuRepository.java).
- [InventoryService.java](../../src/main/java/com/thinh/cosmetic/service/store/InventoryService.java), [StoreService.java](../../src/main/java/com/thinh/cosmetic/service/store/StoreService.java).
- Phase06–09 completion reports và DTO/API mới thực tế; không suy đoán vì roadmap nói sẽ có.
- Hai DOCX: UI §5.3.1–3; hệ thống UC04–08 và KH-QĐ1/2.

## 8. Files Expected To Modify

- Catalog repository/service/public projection để active search/filter/sort/pagination.
- Controller public/read API theo D01, giữ mutation staff đã được bảo vệ.
- Frontend structure D01 tạo từ Phase02, shared layout/header/footer và public route.
- Không thay snapshot/order, stock mutation hoặc SKU editor.

## 9. Files Expected To Create

Dự kiến, chưa tồn tại ở baseline:

- Public catalog query/filter/response DTO và repository query/specification tối thiểu.
- Page routes dự kiến `/`, `/products`, `/products/{id}`, brand/store navigation dưới D01; ghi route thực tế sau code.
- Views/assets hoặc frontend components home/list/detail theo công nghệ đã chốt.
- Service/query/UI tests cho public catalog; không mặc định đường dẫn templates.

## 10. Database Changes

NONE mặc định: tái sử dụng schema/fields Phase05–09.

Nếu D16 kết luận dữ liệu skin/needs/filter còn thiếu, không tự thêm bảng mới: ghi chính xác mapping vào attribute hiện có và chỉ migration sau quyết định được duyệt. Index query là TECHNICAL RECOMMENDATION dựa trên PostgreSQL query plan, không tạo index hàng loạt theo suy đoán.

## 11. Backend Tasks

- [ ] Thêm public projection active product và active SKU, gallery/primary, giá card/detail và trạng thái available.
- [ ] Search keyword theo requirement, filter brand/price/skin/needs theo D16, combine/reset đúng.
- [ ] Sort allowlist theo UI/rule đã chốt; thêm thứ tự ổn định bằng ID để pagination không lặp/mất item.
- [ ] Query pagination ở database/service, trả count và page metadata; không lấy findAll rồi cắt trong Java.
- [ ] Xác định price-filter cho nhiều SKU theo D16, bảo đảm giá card và filter dùng cùng nguồn.
- [ ] Detail theo ID chỉ public active; variant selection trả đúng SKU.price/image/available.
- [ ] Per-store availability dùng Phase08/09 projection; loại inactive store, không trả adjustment history.
- [ ] Batch query ảnh/SKU/stock khi có evidence N+1; ghi TECHNICAL RECOMMENDATION thay refactor kiến trúc.
- [ ] Không gọi raw staff list để public thấy inactive.
- [ ] Ghi API/page contract để Phase11/17/24 tích hợp action riêng; không nhận các UC đó vào Phase10.

## 12. Frontend Tasks

- [ ] Dựng shared header/footer và home theo UI §5.3.1: logo/moon hero/category/featured cards/CTA bằng dữ liệu thật.
- [ ] List theo §5.3.2: filters, reset, result count, sort, cards và pagination/control đã chốt.
- [ ] Detail theo §5.3.3: gallery/thumbnails, brand/name/price/variant, quantity, availability và nội dung tabs.
- [ ] Khi đổi SKU, cập nhật giá/ảnh/tồn của SKU đúng; disabled out-of-stock action theo dữ liệu, không hardcode badge.
- [ ] Đồng bộ search/filter/sort/page với URL để refresh/back giữ state; filter mới reset page theo contract.
- [ ] Có loading/error/no-result/empty-gallery/not-found states và retry.
- [ ] Header nav nối vào route có thật; Blog theo D14, không link giả hoặc tự xây CMS.
- [ ] Ghi hook wishlist/cart/review cho Phase11/17/24; không dùng toast thành công giả cho action chưa tích hợp.
- [ ] Responsive list/detail và keyboard/form labels; giữ visual tokens từ mockup, không thêm framework chưa chốt.

## 13. Validation & Business Rules

- Chỉ active product/SKU xuất hiện public theo KH-QĐ1.
- Available luôn theo SKU×store, không tổng actual hoặc cùng số cho mọi variant.
- Keyword/filter/page/sort được validate, không ghép client sort trực tiếp vào SQL.
- Count/filter/reset/pagination cùng một predicate.
- Giá card và price-range phải dùng rule D16 thống nhất; không lấy tùy SKU đầu tiên.
- Số lượng detail >0; checkout vẫn re-check stock/pricing ở Phase19.
- Nội dung mô tả/ingredients/uses hiển thị an toàn; không render HTML người nhập tùy ý.

## 14. Security Requirements

- Public browsing UC04–08 được phép anonymous; mutation catalog vẫn chỉ staff.
- Public DTO không lộ cost, audit actor, credential hoặc raw inventory history.
- Mọi query SQL theo parameter/allowlist; không tạo lỗ injection qua keyword/sort.
- Authenticated header dùng principal Phase02; không tin customerId từ URL để xác định người dùng.

## 15. Error Handling

- Unknown/inactive product: not found public thống nhất, không lộ dữ liệu quản trị.
- Filter/page/sort sai: field error hoặc canonical state theo contract đã ghi.
- Không kết quả là empty state, không exception.
- Thiếu ảnh/SKU hợp lệ không được bịa giá/tồn; ghi dữ liệu cần kiểm catalog.
- API/map/provider lỗi: UI retry/fallback rõ, không gọi dữ liệu mẫu là production.

## 16. Integration Points

- Phase07 gallery, Phase08 available, Phase09 active stores/maps.
- Phase02 header principal; Phase11 wishlist/profile, Phase17 cart, Phase24 review tab.
- Phase12 rule recommendations chưa tồn tại ở đây; home featured không được nhầm với personalized suggestion.
- Shared header/footer/DTO phải có owner tránh Phase11/17 chỉnh song song mất thay đổi.
- Navigation chỉ tích hợp route đã bàn giao; mỗi action write thuộc Phase sở hữu.

## 17. Implementation Order

1. Chốt D01/D16/D14 phần liên quan, đọc completion contract07–09.
2. Public query/projection và backend tests.
3. Shared layout/routes và home.
4. List/search/filter/sort/pagination.
5. Detail/variant/availability/gallery và hook integration.
6. Responsive/browser smoke và handoff UI contracts.

## 18. Verification

- Build frontend/backend theo công nghệ D01; không ép npm nếu project chọn server-rendered.
- PostgreSQL query tests active, combined filters, pagination stability và multi-SKU price rule.
- HTTP public route/detail/no-result/invalid params; kiểm mutation vẫn forbidden cho customer.
- UI đối chiếu mockup ba màn hình, navigation, refresh/back, keyboard/mobile.
- Record screenshot hoặc evidence theo tooling đã chọn; không claim toàn website hoàn tất.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P10-01 | Home/list happy path | Catalog active có ảnh/SKU | Card giá/ảnh và link đúng |
| P10-02 | Combined filters | Brand + mức giá + skin/needs | Count/items khớp predicate |
| P10-03 | Sort/page | Data bằng giá, chuyển trang | Thứ tự ổn định, không trùng/mất |
| P10-04 | Invalid input | Sort lạ/page âm/filter sai | Reject hoặc normalize đúng contract |
| P10-05 | Empty | Keyword không khớp | No-result state, reset dùng được |
| P10-06 | Detail variant | Đổi SKU A→B | Giá/ảnh/available đúng SKU B |
| P10-07 | Active permission | URL inactive product | Not found; không public dữ liệu staff |
| P10-08 | Boundary stock | Available0 ở một branch | Badge/action đúng, branch khác độc lập |
| P10-09 | Responsive/history | Mobile + refresh/back filters | Layout dùng được, state giữ nhất quán |

## 20. Definition of Done

- [ ] UC04–08 và ba màn hình chạy bằng data thật.
- [ ] Search/filter/sort/page/count dùng backend query đúng và có test.
- [ ] Public active/DTO scope được bảo vệ; permission mutation không bị mở.
- [ ] Variant/gallery/per-store availability đúng.
- [ ] D01/D16/D14 phần liên quan có quyết định; không tự thêm Buy-now/Blog.
- [ ] Responsive/error/empty/navigation được kiểm.
- [ ] Cart/wishlist/review hook được bàn giao, chưa claim các UC đó DONE.
- [ ] Completion report ghi routes/shared UI và những integration chưa thực hiện.

## 21. Expected Result After This Phase

Customer có thể vào home, duyệt/tìm/lọc sản phẩm và xem chi tiết/biến thể/tồn cửa hàng. Storefront read slice hoàn tất; các action account/wishlist/cart/review được nối trong Phase sở hữu sau.

## 22. Handoff To Next Phase

- Phase11/17/24 có shared layout, product ID/SKU ID và hook/action contract sau khi Phase10 hoàn thành.
- Ghi page/API routes thật, filter vocabulary, price/featured sorting rule và pagination semantics.
- Ghi component/view cần tránh sửa xung đột và mockup differences đã được quyết định.
- Không giả định cart mutation/review submission hoặc personalized recommendations đã có.

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
