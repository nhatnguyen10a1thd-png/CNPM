# Phase 09 — Cửa hàng, giờ hoạt động và bản đồ

## 1. Objective

Hoàn thiện quản lý cửa hàng và các phép đọc tồn theo cửa hàng, gồm thông tin, giờ hoạt động và bản đồ theo quyết định D05.

Complexity: MEDIUM. Risk: MEDIUM. Tái sử dụng Store CRUD/deactivate và kho đã được Phase08 bảo vệ.

## 2. Why This Phase Exists

StoreServiceImpl đã tạo/đọc/sửa/deactivate; chưa có search/pagination, UI, maps hoặc xử lý đầy đủ thông tin bắt buộc. Store đang inactive vẫn có thể xuất hiện trong getAll và bị checkout chọn.

Source operatingHours dạng String, tọa độ Double khác hai TIME/tọa độ decimal trong design; không được tự chuyển mất dữ liệu.

## 3. Current State

- Routes `/api/stores` POST/GET và `/{id}` GET/PUT/DELETE đã tồn tại.
- DELETE thực tế deactivate, là hành vi cần giữ; không làm lại thành xóa vật lý.
- StoreRequest chỉ name @NotBlank; address/phone/hours/coords/status chưa kiểm đầy đủ.
- StoreEntity name not-null; address nullable; hours String, latitude/longitude Double.
- StoreRepository hiện CRUD cơ bản; getAll chưa tách public active và staff all.
- InventoryService read tồn store/SKU đã có, Phase08 bổ sung branch scope/invariant.
- Chưa có map integration hoặc template. UI DOCX có Stores trên navigation nhưng không mockup quản trị.

## 4. Preconditions

- [ ] Phase04 hoàn thành permission/branch scope; Phase08 hoàn thành read-stock contract.
- [ ] D05 chốt giờ mở/đóng, timezone/overnight nếu cần, tọa độ và provider/maps config.
- [ ] D01 chốt UI/layout admin; không chọn mới framework.
- [ ] Kiểm chuỗi operatingHours và tọa độ legacy trước migration.
- [ ] Đọc completion report Phase08, không sửa stock mutation ở Phase09.

## 5. Scope

### IN SCOPE

- UC38–42: tra cứu, quản lý/ngừng hoạt động, giờ hoạt động, stock tại cửa hàng, vị trí bản đồ.
- Validation thông tin cửa hàng và projection public/staff riêng.
- Search/pagination, branch-scoped UI và map error fallback.

### OUT OF SCOPE

- Tạo lại Inventory hoặc thay công thức available.
- Checkout branch allocation Phase19, transfer Phase16, purchase Phase13–14.
- Tối ưu tuyến vận chuyển, tính khoảng cách/shipping fee hoặc dịch vụ location ngoài design.

## 6. Requirements Covered

- Hệ thống §3.4.3 UC38, UC39, UC40, UC41, UC42.
- §4.1.2 Store, Inventory; §4.2 QLCH-QĐ1–4.
- Store ID ổn định; thông tin name/address/phone/hours/status đầy đủ; ngừng hoạt động không xóa lịch sử.
- Inactive store không nhận order mới; Phase09 bàn giao eligibility contract, Phase19 kiểm lúc checkout.
- UI navigation Stores được nối ở Phase10; không tuyên bố có mockup staff trong DOCX.

## 7. Files To Inspect First

- [StoreRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/StoreRestController.java), [StoreServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/StoreServiceImpl.java).
- [StoreRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/store/StoreRequest.java), [StoreResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/store/StoreResponse.java).
- [StoreEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StoreEntity.java), [StoreRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/StoreRepository.java), [StoreMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/store/StoreMapper.java).
- [InventoryService.java](../../src/main/java/com/thinh/cosmetic/service/store/InventoryService.java), [InventoryRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/InventoryRestController.java).
- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java): đọc current store selection, không sửa checkout trong Phase09.
- Phase04/08 completion và D05 trong [Master](01_MASTER_ROADMAP.md#decision-register).

## 8. Files Expected To Modify

- Store controller/service/repository/request/response/mapper cho search/validation/projection.
- StoreEntity chỉ sửa biểu diễn đã D05 phê duyệt.
- Config maps và migration theo Phase01/D05.
- UI store read/create/edit/deactivate + stock read chỉ sau D01.
- Không sửa order branch allocation hoặc Inventory mutation logic.

## 9. Files Expected To Create

Dự kiến chưa tồn tại:

- Map adapter/config hoặc map view integration tối thiểu dưới công nghệ D01/D05.
- Query/projection DTO cho store availability nếu StoreResponse không đủ.
- Test StoreService và PostgreSQL migration/backfill hours.
- Template/controller/static store UI xác định sau D01; không tạo branch dashboard ngoài UC.

## 10. Database Changes

- D05 quyết định giữ chuỗi hours hay bổ sung openTime/closeTime theo design; không tự parse chuỗi tự do thành TIME.
- Nếu chuyển tọa độ Double→decimal, kiểm độ chính xác, null/out-of-range và mapping không mất vị trí.
- Bổ sung not-null/length cho address/phone khi yêu cầu đã rõ; legacy thiếu dữ liệu cần bổ sung có nguồn.
- Giữ store ID/status và FK inventory/order/purchase/transfer/employee-store.
- Dùng migration có kiểm chứng, không delete/recreate cửa hàng để đổi cấu trúc.

## 11. Backend Tasks

- [ ] Giữ create/read/update/deactivate có sẵn; thêm required/format validation cho thông tin design.
- [ ] Bổ sung search theo thông tin cửa hàng/status, pagination staff; public chỉ active.
- [ ] Thống nhất DTO giờ/coordinate với D05 và mapping dữ liệu legacy.
- [ ] Kiểm update/deactivate đúng permission; scope store cho phép đọc stock theo employee assignment.
- [ ] Bàn giao query/eligibility active store cho Phase19; test inactive không lọt public candidate.
- [ ] Tái sử dụng InventoryService Phase08 cho UC41, không tính lại held/available khác công thức.
- [ ] Tích hợp map display từ tọa độ theo D05; không gọi geocoding tự động nếu chưa được xác định.
- [ ] Ghi audit create/update/deactivate với actor principal và store ID.
- [ ] Xử lý không có tọa độ hoặc map provider lỗi mà vẫn cho đọc địa chỉ/hours.

## 12. Frontend Tasks

- [ ] Tạo list/detail/form cửa hàng theo D01, search/status filter/pagination.
- [ ] Render hours đúng contract, phone/address và trạng thái active/inactive.
- [ ] Store stock tab dùng response actual/held/available cho staff trong branch scope.
- [ ] Render map/location theo D05; thiếu tọa độ có trạng thái rõ, không marker giả tại0,0.
- [ ] Confirm deactivate, giữ thông tin lịch sử và thông báo ảnh hưởng tới order mới.
- [ ] Kiểm empty/error/loading và map failure fallback.
- [ ] Public store list/navigation chỉ được dùng sau projection active đã có; customer stock không lộ history/actor.

## 13. Validation & Business Rules

- Store ID unique bởi PK; không tự tạo business-code mới nếu design không có cột riêng.
- Name/address/phone/hours/status theo QLCH-QĐ1–4; inactive không làm mất lịch sử.
- Latitude nằm trong[-90,90], longitude[-180,180] khi có; thiếu tọa độ không được coi là0.
- Hours validation phụ thuộc D05; không tự cấm/cho overnight khi chưa chốt.
- Stock dùng available của Phase08, không actual thay available.
- Deactivation bảo vệ order mới, không tự hủy order hiện hữu ngoài lifecycle Phase21.

## 14. Security Requirements

- Chain/store management permission kiểm server cho mutation.
- Staff chỉ xem stock store được cấp; manager phạm vi rộng theo Phase04 permission.
- Customer/public chỉ xem active store và projection công bố.
- Maps credential/key scope theo deployment config; không lộ server secret vào response/log.

## 15. Error Handling

- ID không tồn tại: not found; field/coordinate/hour lỗi: field error.
- Store chưa đủ thông tin để active: validation/conflict theo quyết định design.
- Provider map timeout/error: địa chỉ/hours vẫn đọc được, UI có fallback.
- Unauthorized branch stock read: 403, không dùng client store selector như enforcement.

## 16. Integration Points

- Phase04 branch scope/audit; Phase08 stock read.
- Phase10 dùng active-store/location projection cho nav/detail availability.
- Phase13/16 dùng Store IDs ổn định; Phase19 chọn active branch đủ hàng.
- Shared StoreResponse/StoreRepository và inventory projection phải bàn giao trước consumer chỉnh.
- Không yêu cầu Phase10 để Phase09 hoàn thành staff slice.

## 17. Implementation Order

1. Chốt D05 và audit hours/coords legacy.
2. Migration nhỏ và validation DTO/mapper.
3. Store queries/projections và scoped stock read.
4. Controller/permission/audit, map adapter.
5. UI store/form/map sau D01.
6. PostgreSQL/API/UI verification và handoff eligibility.

## 18. Verification

- Java21 build/test StoreService và mapper.
- PostgreSQL migration hours/coordinates/required fields, kiểm FK giữ nguyên.
- HTTP list/search/create/update/deactivate và branch stock permission.
- UI map/empty/no-coordinates/provider-error/mobile form.
- Test public query loại inactive store; checkout integration thực tế thuộc Phase19.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P09-01 | Happy CRUD | Store đủ thông tin hợp lệ | Lưu/đọc/sửa đúng |
| P09-02 | Validation | Address rỗng/phone sai theo rule | Field error, không ghi DB |
| P09-03 | Boundary coords | Latitude90; latitude91 | Chấp nhận90, reject91 |
| P09-04 | Lifecycle | Deactivate store có order cũ | Giữ history/FK, không public candidate |
| P09-05 | Scope | Staff branchA đọc stock branchB | 403 |
| P09-06 | Empty/location | Search không khớp; thiếu tọa độ | Empty/fallback rõ, không marker0,0 |
| P09-07 | Provider | Maps unavailable | Địa chỉ/hours còn đọc được |
| P09-08 | Hours migration | Chuỗi legacy không parse được | Báo cần đối soát, không mất hours |

## 20. Definition of Done

- [ ] UC38–42 có API/UI và map behavior được kiểm chứng.
- [ ] CRUD/deactivate hiện có được giữ, thông tin/validation đủ.
- [ ] D05 và D01 đã đóng phần liên quan.
- [ ] Store legacy migration giữ ID/hours/coords/FK.
- [ ] Branch scope và public active projection có test.
- [ ] Provider lỗi không làm hỏng trang store.
- [ ] Bàn giao eligibility cho Phase19, chưa sửa checkout.
- [ ] Completion report ghi map config và verification giới hạn.

## 21. Expected Result After This Phase

Quản lý cửa hàng chạy end-to-end, giờ/vị trí có contract được duyệt, stock đọc đúng scope. Inactive store được loại khỏi public/eligibility query để order phase sử dụng sau.

## 22. Handoff To Next Phase

- Phase10 nhận active-store/location/availability projection; Phase13/16 dùng store IDs.
- Phase19 nhận eligibility query kiểm active; vẫn phải re-check status trong transaction checkout.
- Ghi routes hiện hữu thay đổi, giờ/tọa độ contract, D05 và dữ liệu thiếu chưa giải quyết.
- Không mô tả kiểm chứng checkout inactive nếu chưa thực hiện Phase19.

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
