# Phase 11 — Hồ sơ, địa chỉ và wishlist customer

## 1. Objective

Hoàn thiện self-service customer profile, địa chỉ và wishlist trên các service hiện có, sửa ownership/default address và giá wishlist.

Complexity: MEDIUM. Risk: HIGH. Mục tiêu là dữ liệu cá nhân an toàn và UI account/wishlist chạy end-to-end.

## 2. Why This Phase Exists

CustomerServiceImpl đã update profile, CRUD address, beauty profile; WishListServiceImpl đã add/remove idempotent. Lỗi address update/delete không kiểm chủ sở hữu, wishlist nhận customerId mặc định1 và giá luôn0.

Không cần tạo lại CRUD; bổ sung principal scope, validation/default-address consistency và rendering.

## 3. Current State

- Routes `/api/customers/{id}/profile`, `/{id}/addresses`, `/{id}/addresses/{addressId}` tồn tại.
- addAddress default=true đã clear default cũ; updateAddress không làm vậy, deleteAddress bỏ qua customerId.
- CustomerResponse chưa có phone, dù updateProfile đổi Account.phone không kiểm uniqueness.
- CustomerAddressRequest yêu cầu province/district/ward; checkout mockup chỉ province/ward. Cấu trúc field là gate D05.
- `/api/wishlist` và `/products/{productId}` dùng customerId mặc định1.
- Wishlist add tránh duplicate, remove đúng wishList/product; getOrCreate có thể tạo customer=null; response price=BigDecimal.ZERO.
- UI navigation có account/personal/addresses và heart; không có mockup đầy đủ account/wishlist.

## 4. Preconditions

- [ ] Phase02 có customer principal/authenticated session/token theo quyết định D01.
- [ ] Phase06/07 bàn giao SKU/card-price/primary-image contract.
- [ ] D01 chốt account UI; D05 chốt address fields khác nhau giữa source/design.
- [ ] D16 đã chốt nguồn giá wishlist qua contract SKU, không chọn SKU đầu tùy ý.
- [ ] Quyết định D05 về xóa default được ghi trong D05: khi xóa địa chỉ mặc định, cho phép không có default hay yêu cầu chọn/thay thế; không tự chuyển một địa chỉ bất kỳ.
- [ ] Kiểm địa chỉ legacy trùng default/null customer và phone duplicates.

## 5. Scope

### IN SCOPE

- UC10 profile, UC11 address, UC14 wishlist.
- Ownership/principal, validation phone/profile/address, single default khi được chọn.
- Sửa wishlist giá và customer lookup; UI self-service.

### OUT OF SCOPE

- Beauty/recommendation Phase12; CSKH đọc/sửa thay khách Phase25.
- Cart Phase17, checkout/address snapshot Phase19.
- Thay registration/password flow hoặc tự tạo cơ chế loyalty points.

## 6. Requirements Covered

- Hệ thống §3.4.1 UC10, UC11, UC14; §4.1.2 Customer, Account, CustomerAddress, WishList/WishListItem.
- §4.2 KH-QĐ3–4: thông tin/phone hợp lệ và không trùng theo rule; dữ liệu customer ownership.
- UI navigation account/personal/addresses/heart; checkout §5.3.7 dùng địa chỉ, tích hợp sau Phase19.
- Chủ sở hữu UC10/11/14 là Phase11; account lookup CSKH không được ghép vào self-service.

## 7. Files To Inspect First

- [CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java), [CustomerServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java).
- [CustomerAddressRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerAddressRepository.java), [CustomerAddressEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerAddressEntity.java).
- [CustomerAddressRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/CustomerAddressRequest.java), [CustomerAddressMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/account/CustomerAddressMapper.java).
- [CustomerProfileRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/CustomerProfileRequest.java), [CustomerResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/CustomerResponse.java).
- [CustomerMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/account/CustomerMapper.java), [AccountRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java).
- [WishListRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/WishListRestController.java), [WishListServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/WishListServiceImpl.java).
- WishListEntity/Item repositories và Phase06/07 price/image DTO.

## 8. Files Expected To Modify

- Customer controller/service/DTO/mappers/address repository cho ownership/default/validation/phone response.
- Wishlist controller/service/response để principal, current price/image và active-product behavior.
- Entity/migration chỉ khi constraint/timestamp design còn thiếu.
- Shared CustomerServiceImpl có beauty methods; không sửa behavior beauty Phase12 cùng lúc.

## 9. Files Expected To Create

Dự kiến chưa tồn tại:

- Tests customer ownership/default-address/phone và wishlist projection/idempotency.
- Account/profile/address/wishlist UI/controller/components theo D01.
- Scoped repository lookup hoặc projection nếu không thể dùng query hiện hữu an toàn.
- Không tạo lại Customer/Address/Wishlist tables.

## 10. Database Changes

- Giữ surrogate ID+unique(wishlist_id,product_id); đây là tương đương hợp lệ với design, không đổi composite PK.
- WishList.createdAt theo design còn thiếu: bổ sung chỉ sau kiểm tra/backfill có nguồn; không dùng thời điểm audit làm ngày tạo giả.
- Account.phone uniqueness do Phase02/01 chuẩn hóa; Phase11 kiểm service và race constraint, không tạo index trùng.
- CustomerAddress.customer FK/nullability/required fields và default invariant cần data audit.
- TECHNICAL RECOMMENDATION: khóa customer/default-address mutation trong transaction hoặc constraint phù hợp PostgreSQL để tránh hai default; không xóa địa chỉ legacy để đạt constraint.
- Address field thay đổi theo D05, giữ giá trị district legacy nếu UI mới không nhập.

## 11. Backend Tasks

- [ ] Lấy customer từ principal cho profile/address/wishlist; không dùng arbitrary {id}/customerId=1 làm identity.
- [ ] Bảo vệ controller lẫn service: address lookup phải thuộc customer trước read/update/delete.
- [ ] Thêm profile validation tên/phone/dob/gender theo thiết kế; phone unique kiểm excluding current account, lỗi race không lộ DB.
- [ ] Bổ sung phone vào CustomerResponse/mapping để form đọc lại đúng sau update.
- [ ] Giữ addDefault logic đúng và áp cùng transaction khi update default=true; bảo vệ concurrent defaults.
- [ ] Xử lý delete default theo D05 về xóa default đã chốt, không tự chọn địa chỉ đầu tiên.
- [ ] Address validation/mapping theo D05; giữ mapping typo entity recientName nếu chưa cần migration rename.
- [ ] Wishlist getOrCreate phải tìm customer hợp lệ trước save, không tạo customer-null.
- [ ] Giữ add/remove idempotent, thêm concurrent duplicate protection; từ chối add product không được công bố.
- [ ] Thay price=ZERO bằng card-price projection đã chốt Phase06/D16, primary URL từ Phase07.
- [ ] Không hiển thị inactive product như đang bán; xử lý item legacy theo catalog lifecycle đã bàn giao.
- [ ] Tách staff customer list/read quyền riêng; không mở getAll cho customer.

## 12. Frontend Tasks

- [ ] Tạo profile form với dữ liệu hiện tại gồm phone, success và field errors.
- [ ] Address list/add/edit/delete/default selection theo D01 và D05, không gửi customerId tự chọn.
- [ ] Confirm delete/default change và xử lý conflict concurrent mà không mất form.
- [ ] Wishlist có card tên/giá/ảnh/link, remove action, heart state và no-data/loading/error.
- [ ] Hiển thị giá thực theo projection, không dùng0 làm giá placeholder.
- [ ] Sau logout/auth expired, private UI chuyển theo auth policy; không cache dữ liệu người trước.
- [ ] Nối nav self-service từ shared layout Phase02; detail/home hook tích hợp Phase10 theo contract khi có.
- [ ] Không xây CSKH customer search hoặc checkout ở Phase11.

## 13. Validation & Business Rules

- Customer chỉ sửa dữ liệu của mình; address ID phải thuộc current customer.
- Tối đa một default khi default được chọn; behavior khi không còn default theo quyết định D05.
- Phone/email uniqueness theo account rule, không cho update profile bỏ qua constraint registration.
- Profile/date field optional/required theo thiết kế; không tự ép Beauty Profile.
- Wishlist cùng product thêm lại không duplicate; current price không phải snapshot order.
- Xóa địa chỉ không thay shipping snapshot order cũ.
- Không đổi loyaltyPoints theo request self-service.

## 14. Security Requirements

- Các route profile/address/wishlist yêu cầu customer đăng nhập.
- Staff không dùng self-service route để giả customer; delegated CSKH thuộc Phase25 với permission/audit riêng.
- GET /api/customers danh sách chỉ staff có quyền; customer không được đọc người khác.
- CSRF/token handling theo D01/Phase02; server kiểm ownership, không chỉ ẩn button.
- Principal/account status từ Phase02/04; customer bị khóa không dùng mutation.

## 15. Error Handling

- Customer/address/product không tồn tại hoặc không thuộc quyền: not-found/forbidden theo Phase01, không lộ dữ liệu người khác.
- Duplicate phone/default conflict: lỗi rõ, không partial save.
- Invalid address fields: field errors giữ form.
- Empty wishlist/addresses là trạng thái bình thường.
- Auth expired: private data không được tiếp tục render/cached sang user mới.

## 16. Integration Points

- Phase02 identity; Phase06 price/SKU; Phase07 images.
- Phase12 tái sử dụng CustomerService và account layout, không viết lại profile/address.
- Phase19 dùng địa chỉ đã ownership/default validated, vẫn re-check trong checkout.
- Phase10/17 nối heart/cart hooks sau khi contract được bàn giao.
- CustomerServiceImpl/CustomerMapper/shared layout phải phối hợp để không xung đột Phase12/25.

## 17. Implementation Order

1. Đóng D05/D05 về xóa default và xác minh principal/account contract.
2. Scoped repository/service và ownership regression tests.
3. Validation/phone response/default atomicity.
4. Wishlist customer lookup/idempotency/price-image projection.
5. UI profile/address/wishlist theo D01.
6. Cross-customer/DB/UI verification và handoff checkout.

## 18. Verification

- Java21 build/test; giữ baseline inventory/order/voucher.
- PostgreSQL tests duplicate phone/default concurrency và FK legacy.
- HTTP hai customer A/B: không truy cập/chỉnh/xóa địa chỉ hoặc wishlist của nhau.
- UI create/edit/default/delete, empty data, price thật, logout/auth expiry.
- Kiểm order snapshot cũ giữ nguyên khi address thay đổi.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P11-01 | Profile happy path | Sửa name/phone hợp lệ | Đọc lại phone đúng, không đổi loyalty |
| P11-02 | Phone duplicate | Phone thuộc account khác | Reject, không partial update |
| P11-03 | Ownership | CustomerA sửa/xóa addressB | Không mutation, không lộ dữ liệu B |
| P11-04 | Default update | Đổi default từA sangB | ChỉB default, transaction atomic |
| P11-05 | Default delete | Xóa default cuối | Behavior đúng D05 về xóa default đã chốt |
| P11-06 | Wishlist idempotency | Thêm product hai lần/đồng thời | Một item, đúng giá/ảnh |
| P11-07 | Invalid/empty | Customer không có; wishlist rỗng | Không customer-null row; empty state |
| P11-08 | Boundary address | Field bắt buộc thiếu theo D05 | Field error, giữ form |
| P11-09 | Permission/history | Guest gọi profile; đổi address có order | Auth reject; order snapshot không đổi |

## 20. Definition of Done

- [ ] UC10/11/14 chạy API/UI, giữ CRUD/idempotency đang đúng.
- [ ] Ownership regression pass cho mọi address/wishlist route.
- [ ] Phone đọc/ghi/unique và default atomicity được kiểm.
- [ ] D05/D05 về xóa default/D16/D01 phần liên quan đã chốt.
- [ ] Wishlist không customer-null, không price giả0.
- [ ] Migration bảo toàn địa chỉ/phone/ID/historical order snapshots.
- [ ] Test auth expiry/empty/concurrent có evidence.
- [ ] Handoff cho Phase12/19 ghi contract thực tế và known issues.

## 21. Expected Result After This Phase

Customer có thể quản lý profile/địa chỉ/wishlist của mình an toàn, đọc đúng phone/giá/ảnh và default address nhất quán. Checkout và Beauty Profile vẫn là Phase riêng.

## 22. Handoff To Next Phase

- Phase12 sử dụng customer principal/profile và layout; Phase19 dùng address ownership/default contract.
- Ghi API private thực tế, response phone và address fields, default-deletion policy, price-card rule.
- Routes tồn tại ở baseline đã được sửa identity/permission; API dự kiến chỉ coi có sau implementation.
- Địa chỉ order snapshot không tự đồng bộ với profile; Phase19 vẫn phải validate lại khi tạo order.

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
