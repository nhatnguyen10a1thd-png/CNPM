# Phase 12 — Beauty Profile và gợi ý theo luật

> Status: NOT_STARTED. Complexity: MEDIUM. Risk: MEDIUM.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Đây là kế hoạch phát triển phần còn thiếu; tài liệu này chưa thực hiện code, migration hoặc verification của Phase.

## 1. Objective

Hoàn thiện Beauty Profile tùy chọn và gợi ý sản phẩm bằng các luật đối chiếu thuộc tính đã được xác nhận.

## 2. Why This Phase Exists

Profile read/upsert đã tồn tại nhưng chưa có recommendation engine/API/UI. Thiết kế KH-QĐ6 yêu cầu matching thuộc tính, không sử dụng AI; cần nối đúng catalog và profile thay vì dựng lại customer module.

## 3. Current State

- CustomerServiceImpl đã đọc/cập nhật BeautyProfile qua customerId; CustomerRestController có GET/PUT `/{id}/beauty-profile`.
- BeautyProfileEntity có loại da/vấn đề da/nhu cầu/sở thích/mức giá; không có service tìm sản phẩm phù hợp.
- Attribute/ProductSku/SkuAttributeValue có trong entity nhưng baseline thiếu group/CRUD; Phase05–06 sẽ bổ sung theo quyết định.
- Wishlist thuộc11, public product list/detail thuộc10; Phase12 chỉ nối dữ liệu recommendation, không viết lại các màn đó.
- Không có recommendation dependency, AI model, ranking hay cold-start policy trong source; D16 phải chốt vocabulary/rules.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [ ] Phase05,06,11 COMPLETED: attribute/SKU contract và authenticated customer ownership dùng được.
- [ ] D16 có vocabulary, matching/filter rules và ví dụ kết quả được duyệt; D01 có layout/UI technology.
- [ ] Đọc report11 để giữ nguyên profile/address/wishlist changes, không sửa field ngoài scope.
- [ ] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [ ] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

## 5. Scope

### IN SCOPE

- Validation/read/upsert Beauty Profile còn thiếu và optional UI.
- Recommendation query/service/API theo approved matching rules, active product/SKU và giá/ảnh đã có.
- Tích hợp gợi ý vào account/home/list theo navigation được duyệt, giữ existing screens.
- Test deterministic matching, no-profile, empty results, ownership và price boundaries.

### OUT OF SCOPE

- AI/ML, external recommendation platform, tracking hành vi, scoring/cold-start logic AI tự đặt.
- Customer address/wishlist CRUD đã thuộc11; catalog attributes/SKU CRUD thuộc05–06.
- Home/list/detail xây dựng lại thuộc10; cart/checkout thuộc17–19.
- Không bắt buộc khách điền Beauty Profile mới được mua hàng.

## 6. Requirements Covered

- UC12/UC13; KH-QĐ5/6; SYS31; NFR19/23; BeautyProfile bảng112/Attribute117/Value118.
- UI navigation account/Beauty và public skin/needs filters; không có full Beauty/recommendation mockup riêng.
- D16 mapping vocabulary/rules/sort và D01 approved presentation; Phase05–06 sở hữu schema catalog.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [CustomerServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java)
- [CustomerService.java](../../src/main/java/com/thinh/cosmetic/service/account/CustomerService.java)
- [CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java)
- [BeautyProfileRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/BeautyProfileRequest.java)
- [BeautyProfileResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/BeautyProfileResponse.java)
- [BeautyProfileEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/BeautyProfileEntity.java)
- [BeautyProfileRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/BeautyProfileRepository.java)
- [AttributeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/AttributeEntity.java)
- [SkuAttributeValueEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/SkuAttributeValueEntity.java)
- [SkuAttributeValueRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/SkuAttributeValueRepository.java)
- [ProductSkuEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java)

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [CustomerServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java)
- [CustomerService.java](../../src/main/java/com/thinh/cosmetic/service/account/CustomerService.java)
- [CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java)
- [BeautyProfileRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/BeautyProfileRequest.java)
- [SkuAttributeValueRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/SkuAttributeValueRepository.java)
- [ProductRepository.java](../../src/main/java/com/thinh/cosmetic/repository/catalog/ProductRepository.java)

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- Recommendation service/query/response theo packages/conventions hiện tại; chỉ tạo khi existing catalog service chưa đáp ứng.
- Rule definitions/fixtures được xác nhận và tests deterministic matching.
- Beauty Profile/gợi ý view/client theo D01; paths phụ thuộc frontend lựa chọn, chưa tồn tại ở baseline.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

NONE mặc định — BeautyProfile và attribute/value tables đã có. Group/uniqueness/value schema thiếu do05–06 sở hữu. Nếu approved D16 cần biểu diễn thêm field profile, phải ghi requirement/migration/data mapping cụ thể trước thay; không thêm behavioral tracking hoặc model/ranking tables tự phát.

## 11. Backend Tasks

- [ ] Đọc implementation11/05/06 và D16 để chọn vocabulary/mapping duy nhất, không tạo schema song song.
- [ ] Giữ profile optional; validate thuộc tính/giá khi khách cung cấp, không reject chỉ vì profile chưa tồn tại.
- [ ] Lấy customer từ principal/ownership contract11; profile ID do browser gửi không cho phép đọc/sửa khách khác.
- [ ] Bổ sung matching query/service đối chiếu approved profile với attributes của sản phẩm/SKU đang active.
- [ ] Dùng giá và status theo06; no list-all rồi tự suy luận thuộc tính bằng tên/description nếu D16 không quy định.
- [ ] Giữ kết quả deterministic và sorting/filter policy đã chốt; no-profile/no-match có behavior được ghi trong D16.
- [ ] Response reuse product card DTO/ảnh/giá hiện có từ06–10; không trả wishlist price giả hoặc stock toàn chuỗi.
- [ ] Ghi rõ recommendation chỉ là gợi ý, không cản catalog/cart/checkout khi profile rỗng.
- [ ] Test exact matching fixtures, optional fields, giá biên, inactive products và owner checks.

## 12. Frontend Tasks

- [ ] Nối GET/PUT Beauty Profile thật và recommendation endpoint; reuse account layout11 và product cards10.
- [ ] Cho phép bỏ qua hoặc xóa giá trị tùy chọn theo approved contract; không bắt khách nhập toàn bộ.
- [ ] Hiển thị loading/empty/no-profile/no-match/validation/permission states rõ, không fake recommended items.
- [ ] Link gợi ý tới product detail hiện có; wishlist/cart actions dùng services owners11/17 khi sẵn sàng.
- [ ] Labels/options theo vocabulary D16, responsive và accessible form.

## 13. Validation & Business Rules

- KH-QĐ5 profile tùy chọn; KH-QĐ6 rule-based matching attributes, no AI.
- Active products/SKUs và range giá phải theo catalog contract; không đổi giá vì profile.
- Không tự thêm rule default theo loại da/ranking popularity; phải có expected examples D16.
- Customer only own profile; recommendation dataset không chứa profile/PII khách khác.

## 14. Security Requirements

Private Beauty Profile read/update yêu cầu authenticated owner; staff đọc theo permission matrix04 chỉ nếu requirement cho phép. Public product data dùng active contract, recommendation response không lộ customer profile hoặc IDs khác. Method/route validation nằm server, không chỉ hidden UI.

## 15. Error Handling

- Invalid vocabulary/range: field errors theo01, không lưu một phần profile.
- Unknown/inactive product: loại khỏi recommendation theo contract, không crash response.
- No profile/no match: empty/approved state, không500 và không auto fill skin data.
- Unauthorized/crosscustomer:401/403, không trả thông tin beauty của người khác.

## 16. Integration Points

- 05–06 attributes/SKU/price;10 product cards/details;11 principal/profile/wishlist.
- 17 cart và19 checkout không phụ thuộc profile mandatory.
- 32 kiểm chứng no-AI/no-profile và matched examples trên UI.

## 17. Implementation Order

1. Read source/reports và chốt D16 vocabulary/rules/examples.
2. Hoàn thiện profile validation/ownership ở service hiện có.
3. Implement deterministic matching query/response và tests.
4. Build Beauty/gợi ý UI reuse existing cards/routes.
5. Run permission/empty/boundary/regression và record exact rule contract.

## 18. Verification

- [ ] Build/tests JDK21 và PostgreSQL query fixtures với expected matching IDs.
- [ ] GET/PUT own profile đúng; no-profile không lỗi hoặc cản mua hàng.
- [ ] Recommendations khớp D16 examples, inactive excluded, price boundary đúng.
- [ ] UI optional/empty/loading/errors/links hoạt động; không AI/provider dependency.
- [ ] Profile/address/wishlist và catalog Phase trước không bị đổi hành vi.
- [ ] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [ ] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [ ] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P12-T01 | Happy matching | Approved profile + known SKU attribute fixtures | Exactly expected active product IDs theo D16 |
| P12-T02 | Optional profile | Khách chưa tạo Beauty Profile | Approved no-profile state; catalog/checkout vẫn dùng được |
| P12-T03 | Empty results | Valid profile không có sản phẩm phù hợp | Empty state, không tự tạo ranking/fallback chưa duyệt |
| P12-T04 | Validation | Vocabulary không tồn tại hoặc invalid price range | Controlled field error; profile không partial save |
| P12-T05 | Permission | CustomerA đọc/sửa profileB | Denied, no profile data leak |
| P12-T06 | Inactive product | Perfect attribute match nhưng product/SKU inactive | Excluded theo active catalog contract |
| P12-T07 | Boundary price | Giá bằng/thấp/cao giới hạn approved | Matching theo boundary đã chốt |
| P12-T08 | Repeated upsert | Cùng owner cập nhật profile nhiều lần | One profile per customer, updatedAt đúng |
| P12-T09 | Regression | Không profile rồi addCart/checkout | Không bị yêu cầu profile bắt buộc |

## 20. Definition of Done

- [ ] UC12/13 profile optional và matching thực theo approved D16.
- [ ] Không thêm AI/model/tracking; expected rule fixtures và no-match behavior có evidence.
- [ ] Ownership/validation/query/UI states/test cases pass.
- [ ] Các catalog/profile/address/wishlist capability đúng được reuse.
- [ ] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [ ] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [ ] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Khách có thể quản lý Beauty Profile và nhận gợi ý theo luật minh bạch trên catalog hiện hữu; không thêm điều kiện mua hàng hoặc AI feature.

## 22. Handoff To Next Phase

- 10/11 có thể hiển thị recommendation cards theo exact query/rule/DTO contract trong report.
- 29 customer report chỉ dùng fields/metrics được yêu cầu; không tự biến matching thành analytics.
- 32 có deterministic fixtures và browser steps kiểm chứng profile optional/no-AI.

Các giả định bàn giao chỉ có hiệu lực sau COMPLETED + evidence. API/route mới phải được ghi đúng URL/method/request/response/permissions trong report; đây chưa phải API đã tồn tại.

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

Chưa thực hiện. Xem Current State và gate liên quan.

## Remaining Tasks

Toàn bộ checklist của Phase.

## Verification Result

Chưa thực hiện. Kết quả baseline trong Audit không thay thế kiểm chứng Phase.

## Notes For Next Phase

Chưa thực hiện; chỉ sử dụng Handoff sau khi report có evidence COMPLETED.
