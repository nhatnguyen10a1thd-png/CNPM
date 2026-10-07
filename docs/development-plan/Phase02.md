# Phase 02 — Authentication và màn hình đăng nhập/đăng ký

> Status: IMPLEMENTED_AND_TESTED_LOCAL. Complexity: HIGH. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Đã triển khai và kiểm thử local ngày 2026-10-07. Kiểm kê/migration dữ liệu đang triển khai và kiểm thử hết hạn theo thời gian thực còn chờ. Xem [báo cáo Phase 01–04](../PHASE_01_04_REPORT.md) và [hướng dẫn database](../database/README.md).

## 1. Objective

Hoàn thiện identity/authentication của tài khoản hiện có, bảo vệ mật khẩu và triển khai login/register/logout theo thiết kế được chốt.

## 2. Why This Phase Exists

Vấn đề tại baseline trước triển khai: Auth API chỉ so sánh plaintext/email rồi trảCustomerResponse; không session/principal/status/logout. Những route customer nhận ID1 hoặc IDs do browser gửi không thể dựa vào thao tác này để bảo vệ dữ liệu.

## 3. Current State

Các dòng bên dưới là **baseline audit trước triển khai**, được giữ để đối chiếu; trạng thái source hiện tại và bằng chứng mới nằm trong Completion Report cuối tài liệu.

- AuthRestController `/api/auth/register` và `/api/auth/login` đã có; AccountServiceImpl tạoAccount+Customer và checkexistsByEmail.
- AccountEntity thiếu status/uniqueusername,email,phone/hashrequired; createdAt vàAccountType có; login tìm Customer nên employee bị loại.
- RegisterRequest min6/password; phoneoptional; LoginRequest email-only; AccountResponse DTO có nhưng chưa consumer.
- UI§5.3.4/5 yêu cầu phone-or-email/remember/passwordvisibility/forgot;register min8lettersnumbers/confirm/terms; nofrontendimplemented.
- 9tests baseline không authentication/ownership tests; PostgreSQLschema/legacyaccounts chưa kiểm kê.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [x] Foundation Phase01 dùng được trong môi trường local: H2 và PostgreSQL test chạy thật, error contract và migration runbook có; chưa xác nhận database đang triển khai.
- [x] D01/D02 được ghi thành lựa chọn triển khai thường lệ trong phạm vi người dùng yêu cầu; static JS/session và các rules cụ thể xem Decisions Made, không suy thành phê duyệt riêng từng policy.
- [ ] Đọc dữ liệu account/customer/employee legacy an toàn; không dump plaintext hoặc đặt password chung.
- [x] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [x] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

## 5. Scope

### IN SCOPE

- Password storage/verification, account status và unified account principal cho customer/staff identity.
- Register/login/logout/sessionexpiry; root role enforcement chi tiết staff ở04.
- Customer identity resolver và loại bỏ khả năng dùng default1/rawID làm nguồn xác thực; domain ownership checks chi tiết ở11/17/19+.
- Hai screen login/register và state/error/navigation theo UI; preserve existingregistration creation flow.

### OUT OF SCOPE

- Recovery/OTP/email delivery thuộc03; staff grants/adminemployeeUI thuộc04.
- Customerprofile/address/cart/order hoàn thiện thuộc11/17–20.
- JWT/sociallogin/MFA framework chưa được chọn; no extra identity platform.
- Không auto đổi toàn bộ frontend technology hoặc rewrite REST architecture.

## 6. Requirements Covered

- UC01,UC02,UC09; SYS01–03/05; NFR09/11/12; KH-QĐ3/4.
- UI§5.3.4 ui-image7,§5.3.5 ui-image8; ui-image3 eventbypass phải reconcileD02.
- Schema bảng109TaiKhoan/110KhachHang; giữ customer data/history; D01,D02 prerequisites.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [AuthRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/AuthRestController.java)
- [AccountService.java](../../src/main/java/com/thinh/cosmetic/service/account/AccountService.java)
- [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java)
- [AccountRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java)
- [CustomerRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java)
- [AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java)
- [CustomerEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java)
- [LoginRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/LoginRequest.java)
- [RegisterRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/RegisterRequest.java)
- [AccountResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/AccountResponse.java)
- [CustomerResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/CustomerResponse.java)
- [CartRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/CartRestController.java)
- [pom.xml](../../pom.xml)

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [AuthRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/AuthRestController.java)
- [AccountService.java](../../src/main/java/com/thinh/cosmetic/service/account/AccountService.java)
- [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java)
- [AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java)
- [AccountRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java)
- [LoginRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/LoginRequest.java)
- [RegisterRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/RegisterRequest.java)
- [pom.xml](../../pom.xml)

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- TheoD01: security/config/principal/current-account resolver trong existing package conventions; không giả địnhJWT.
- TheoD01/D02: login/register view/assets hoặc client files; exact paths ghi sau quyết định.
- Migration account constraints/status/hash compatibility theo baseline01; testauth/security integration.
- Reuse AccountResponse nếu phù hợp; authresultDTO mới chỉ khi contract cần, không chứa hash/password.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

Dự kiến account status theo bảng109, constraints username/email/phone uniqueness và hash required theo D02/design. Phone optional nếu bỏ trống trong UI; không ép unique emptystring làm lỗi mọi account không phone. Kiểm kê duplicate/null/legacy hash/accounttype trước migrate. Có phương án legacy credentials được xác nhận (hash đúng format hoặc recovery phù hợp); không tự gán mật khẩu chung/reset dữ liệu. Customer relations/joinDate/history giữ; không thay PK.

## 11. Backend Tasks

- [x] Reconcile D01/D02 thành auth/principal/API/view contract; login identity phải phân biệt customer/staff account mà không đòi mọiaccount cóCustomerprofile.
- [x] Hash và verify bằng mechanism phù hợp đã chọn; loại bỏ `.equals(rawPassword)`/rawstorage, không serialize secrets.
- [x] Validate/registerrequired/email/phone/unique/confirmterms theoapprovedD02; duplicate concurrent registration cóDBconstraint+controlledconflict.
- [x] CreateAccount+Customer atomic, giữ defaultloyalty/profilecreation; latevalidation không để orphanAccount.
- [x] Enforce active accountstatus; expose currentaccount/customerId/employeeId từprincipal, không trustbrowserIDs.
- [x] Implement login/sessionor token creation,expiry/logout invalidation đúngD01; signedout credential cũ không còn truy cậpprivateAPI.
- [x] Thiết lập restricteddefault route protection: publicauth/publiccatalogread theo policy, private/admin denied khi chưaapprovedRBAC04; không mở toàn bộAPI do thiếurolegrant.
- [x] Nối currentcustomerresolver/identity boundary cho consumerphases; rawdefaultID không được coi principal dù parameter vẫn tồn tại để compatibility.
- [x] Chuẩn hóa authfailure/duplicate/status errors với01; không leak accountpassword/hash/internal details.
- [x] Thêm 14 auth integration tests cho identity/password/duplicate/logout/status/version/legacy; kiểm thử giá trị timeout. Timed servlet expiry còn chờ trong Verification.

## 12. Frontend Tasks

- [x] Buildlogin/register screens theo UI dark/gold/moon/sharedconventions đã chọnD01, không tự thêm SiteMesh.
- [x] Login hỗ trợ approvedidentifier, show/hidepassword, remember behavior; linkforgot chỉ nối03 khi có, không tạo fake success.
- [x] Registeroptionalphone/confirm/terms/passwordhint/disabledsubmit theo D02; servervalidation vẫn authoritative.
- [x] Hiển thị invalidcredentials/accountinactive/duplicate/field errors; loading/submission prevention không thay atomicserverguards.
- [x] Login redirects theoaccounttype/rights contract; customer bypass diagram không được bypassprivateAPI.
- [x] Logout controls kết thúcidentitythật; browserback không cấp lạiprivateaccess; responsive/keyboard/formlabels.

## 13. Validation & Business Rules

- Passwords luôn protected storage; khônglog/returnpassword.
- KH-QĐ3 identifierunique/valid; normalization/phonepolicy phải ghiD02.
- Registeratomicaccount+customer; duplicateHTTPrequests không tạo profileshai lần.
- UC01 shared account actors; roles/scope chi tiết04, không tự grantADMIN cho employee.
- UC02 logout/expiry invalidate theoD01; no trust `customerId=1`.

## 14. Security Requirements

Public register/login cần validation và abuse controls phù hợp; currentaccount/private data yêu cầu authenticated principal; authresult không chứahash. NếuD01session: secure cookie/CSRF/expiry theo môi trường; nếu token: revocation/logout/storage/CORS theo quyết định. Không áp cả hai mặc định. Customer ownership checks phải nằm consumer services ở11/17/19+, không chỉẩn nút. Staff routes giữ denied cho tới04 quyền được định nghĩa.

## 15. Error Handling

- Invalidcredentials401 hoặcviewerror theoD01, không phân biệt leak lookup email tồn tại.
- Inactiveaccount từchối tạoidentity; duplicates409; invalidfields400; missingprofile theoaccounttype không tạoCustomer giả.
- Legacycredential chưa mapping: controlledblocked/recoverydecision, khôngfallbackplaintext indefinitely.
- Partialregistrationfailure phải rollbackAccount+Customer; unexpected500safe.

## 16. Integration Points

- 03recoveryreusehash/status/identitycontract.
- 04stafflogin dùngunifiedaccountidentity+status, thêmrolescope/grants.
- 11/17/19–25 deriveprincipalidentity vàownerguards; consumerkhông dựngsessionkhác.
- 30accountnotifications chỉ saueventpolicyD11, không thêmprovider ở02.

## 17. Implementation Order

1. ChốtD01/D02; đọclegacy data và existingroutes/DTO.
2. Prepare migration/hashstrategy và currentaccountprincipal contract.
3. Implement register/login/status/logout/expiry và routeboundary restricted.
4. Build2screens tích hợpactualcontract/errors.
5. Run auth/duplicate/legacy/logout/permission tests; report exact identity/API paths.

## 18. Verification

- [x] Maven build/tests JDK21 và authintegration trênPostgres test.
- [x] Account mới lưu BCrypt, response không lộ hash/password; unique constraints, duplicate race và rollback Account+Customer đã kiểm thử trên H2/PostgreSQL.
- [ ] Kiểm kê và xử lý plaintext/duplicate/null trong database đang triển khai: chưa truy cập được dữ liệu hiện hữu.
- [x] Anonymous private API bị deny; login/logout, staff identity, khóa tài khoản và credentialsVersion revocation đã chạy.
- [ ] Chờ session tự hết hạn theo thời gian thực trong servlet container: chưa thực hiện; giá trị timeout đã được kiểm thử.
- [x] Hai screen minrule/optionalphone/confirm/terms/remember theoapprovedD02; responsive/empty/errorstates.
- [x] Existing9tests pass; chưaclaim toàn bộdomainownershipđãfix.
- [x] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [x] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [x] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P02-T01 | Happy registration | Approved validfullname/email/optionalphone/password/confirmterms | OneAccount+Customer; hashedcredential; nosecretresponse |
| P02-T02 | Identifier login | Email hoặcphone theoD02 | Correctaccountprincipal; no rawIDsource |
| P02-T03 | Password mismatch | Sai password | Noidentity, controlledautherror |
| P02-T04 | Inactive account | Knowninactiveaccount | Login denied; not silentlyreactivated |
| P02-T05 | Duplicate race | 2registration cùngidentifier | Exactlyonecreated; otherconflict; no orphanprofiles |
| P02-T06 | Validation boundary | Độ dài/password/phone/email theo D02, gồm dữ liệu sát ngưỡng | Server và UI nhất quán với policy đã chốt; field errors rõ |
| P02-T07 | Logout/expiry | Oldsession/token reused | Privateaccess denied afterlogout/expiry |
| P02-T08 | Staff login | EMPLOYEE account không CustomerEntity | Identity tạo theoaccounttype; grantsdenieduntil04 |
| P02-T09 | Tampered customerId | AuthenticatedA sendscustomerIdB/1 | Doesnotchangeprincipal; privatefunctioncannotimpersonate |
| P02-T10 | Legacy data | Preexistingraw/mappedhash/doubleemailrecords | Approved migration/recovery only; noloss/history/no commonpassword |
| P02-T11 | Empty credentials | Blankidentifier/password | Validationerror; noNullPointerException |

## 20. Definition of Done

- [x] Identity/hash/status/register/login/logout verified local theo D01/D02 được chọn trong phạm vi yêu cầu người dùng.
- [ ] Kiểm thử session tự hết hạn theo đồng hồ thực còn chờ.
- [x] Hai screens nốibackend thực, không fakeauthed/customerbypass.
- [ ] Unique/rollback/no-orphan đã có evidence trên database test; kế hoạch legacy/migration có runbook, nhưng chưa áp dụng/kiểm kê database đang triển khai.
- [x] Identity resolver/routeboundary bàn giao04/11/17; domainbugs chưafix ghi rõ.
- [x] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [x] Completion Report có files/routes/schema/decisions/deviations/test evidence; cập nhật tổng hợp Audit/Master thuộc báo cáo Phase01–04 của phiên này.
- [x] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Có authentication duy trì danh tính, protectedcredentials, accountstatus và login/register/logout screens. Staffauthorization/grantUI và recovery chưa tự coi hoàn thành.

## 22. Handoff To Next Phase

- 03dùnghash/status/accountresolver vàapprovedforgotlink;04dùngstaffprincipal, khôngloginCustomer giả.
- Recordexactauthroutes/request/response/security/session/token/logout/expiry config trongreport.
- Consumerphases phải derivetheiractor fromprincipal; compatibility rawIDs khôngthayownershipcheck.

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

IMPLEMENTED_AND_TESTED_LOCAL

Ngày ghi nhận: 2026-10-07. Trạng thái này xác nhận implementation và kiểm thử local; không xác nhận dữ liệu/migration/HTTPS production. Bằng chứng tổng hợp tại [PHASE_01_04_REPORT.md](../PHASE_01_04_REPORT.md); quy trình triển khai schema tại [database/README.md](../database/README.md).

## Work Completed

- Đăng ký Account+Customer trong một transaction, BCrypt thay raw password, giữ loyaltyPoints=0 và joinDate. Unique email/username/phone; duplicate race trả 409; lỗi insert Customer rollback Account.
- Login email hoặc phone cho cả CUSTOMER/EMPLOYEE, không yêu cầu nhân viên có Customer profile. Password sai, account khóa, employee inactive hoặc legacy plaintext đều không tạo identity; trả lỗi 401 thống nhất.
- Spring Security session principal, current-account/customer/employee resolver, GET me, CSRF, logout; login xoay session và CSRF. Session bị vô hiệu hóa khi status hoặc credentialsVersion thay đổi.
- Route protection mặc định hạn chế; quyền nhân viên dùng policy Phase04. Các API customer/order/returns/reviews chưa có đầy đủ ownership vẫn đóng, không dùng customerId mặc định làm danh tính.
- Abuse guard bounded trong memory; UI login/register/remember/show-password/terms/error/loading/logout nối API thật và recovery Phase03.

## Files Created

- `src/main/java/com/thinh/cosmetic/config/SecurityConfig.java`
- `src/main/java/com/thinh/cosmetic/security/{AccountPrincipal,CurrentAccountResolver,PasswordPolicy,ActiveAccountFilter,AuthAttemptLimiter,SecurityErrorWriter}.java`
- `src/main/java/com/thinh/cosmetic/domain/enums/AccountStatus.java`
- `src/main/java/com/thinh/cosmetic/domain/dto/response/account/AuthResponse.java`
- `src/test/java/com/thinh/cosmetic/security/AuthenticationIntegrationTest.java` — 14 tests.
- UI dùng chung Phase02–04: `src/main/resources/static/{index.html,app.css,app.js}`.

## Files Modified

- `domain/entity/account/AccountEntity.java`, `repository/account/AccountRepository.java`.
- `domain/dto/request/account/{LoginRequest,RegisterRequest}.java`.
- `service/account/AccountService.java`, `service/account/impl/AccountServiceImpl.java`, `rest/account/AuthRestController.java`.
- Foundation phối hợp Phase01: `pom.xml`, cấu hình profiles, semantic error contract, Clock và schema runbook. Controller identity boundaries được Phase04 tích hợp, không tạo lại ownership workflow của Phase sau.

## Database Changes

Account mapping thêm `status` STRING (`ACTIVE`, `LOCKED`) và `credentials_version` BIGINT mặc định 0; email/username bắt buộc và unique tối đa 254 ký tự; phone optional/unique tối đa 16 ký tự; password_hash bắt buộc tối đa 100 ký tự. Password không serialize và không xuất trong toString. Giữ account/customer PK và lịch sử.

Schema mới đã chạy trên H2 và PostgreSQL16.15 disposable test. `docs/database/V001__identity_security.sql` là migration thủ công có transaction/preflight, normalization và unique indexes. Chưa áp dụng lên database hiện hữu; không reset dữ liệu cũ. Legacy plaintext không được so sánh để đăng nhập; recovery có xác minh thay credential. Không tự gán mật khẩu chung hay tuyên bố toàn bộ storage cũ đã hết plaintext.

## APIs / Routes Added

| Method/route | Request/response | Access |
|---|---|---|
| GET `/api/auth/csrf` | `{token,headerName,parameterName}` | Public; khởi tạo session CSRF |
| POST `/api/auth/register` | `{fullName,email,phone?,password,confirmPassword,termsAccepted}` → 201 CustomerResponse | Public, cần CSRF |
| POST `/api/auth/login` | `{identifier,password,rememberMe?}` → 200 AuthResponse + session cookie | Public, cần CSRF; alias `email` tương thích |
| GET `/api/auth/me` | AuthResponse | Authenticated |
| POST `/api/auth/logout` | 204; invalidate session/expire cookie | Authenticated, cần CSRF |

AuthResponse chỉ gồm `accountId,email,phone,accountType,status,customerId,employeeId,fullName,roles,permissions,storeIds`; không chứa password/hash. Grants nhân viên đọc mới từ database. Recovery routes thuộc Phase03.

## UI Added

Basic same-origin UI tại `/`: login email/phone, hiện/ẩn mật khẩu, remember, register fullName/email/optional phone/confirm/terms, password hint, validation/loading/errors; account view và logout thật. Navigation theo account type và permission; forgot/reset nối Phase03. Dùng dark/gold và moon motif đơn giản để test, không tuyên bố tái tạo đầy đủ mockup bán hàng hoặc các screen Phase10 trở đi.

## Decisions Made

- D01: HTML/CSS/JavaScript tĩnh + fetch cùng origin + Spring Security session/CSRF, giữ kiến trúc REST. Người dùng yêu cầu hoàn thiện Phase01–04 và UI cơ bản để test; đây là lựa chọn triển khai thường lệ trong phạm vi đó, không ghi là người dùng đã phê duyệt riêng từng policy.
- D02: email trim/lowercase; phone bỏ whitespace, dấu `().-`, còn optional `+` và 9–15 digits; blank phone thành null. Password tối thiểu 8 ký tự, có ASCII letter và digit, tối đa 72 UTF-8 bytes theo BCrypt; confirm và terms bắt buộc phía server.
- Normal session idle 30 phút (configurable `lunea.auth.session-timeout`, fallback `server.servlet.session.timeout`); remember persistent cookie/session idle 7 ngày (`lunea.auth.remember-timeout`). Session memory mất khi ứng dụng restart. HttpOnly, SameSite=Lax; Secure khi request HTTPS hoặc cookie-secure config bật, gồm TLS termination.
- Không bypass authentication theo diagram; customer/staff đều có principal. Login rotate session+CSRF; logout và password/status changes thu hồi session. Không thêm JWT/social/MFA.
- Login guard: 60/IP/15 phút và 10/canonical identifier/15 phút; thành công chỉ clear identifier counter. Register: 10/IP/giờ. Guard tối đa 10000 windows, single-node; cấu hình production/distributed guard ngoài scope local.

## Deviations From Plan

- UI tối thiểu dùng static JS thay Thymeleaf/Bootstrap được roadmap nêu như recommendation chưa chốt; cùng origin và session nên không cần thêm frontend platform.
- Phase02/03/04 được tích hợp trong cùng yêu cầu người dùng, recovery và staff rights có report riêng.
- Phân biệt `IMPLEMENTED_AND_TESTED_LOCAL` với full deployed completion vì dữ liệu hiện hữu chưa kiểm kê/migrate; timed container expiry chưa thực nghiệm. Timeout configuration và status/version revocation đã verified.

## Known Issues

- PostgreSQL developer/deployed port5432 chưa truy cập được; duplicates/null/plaintext/profile link inventory và V001 trên dữ liệu đó chưa verified. Plaintext lịch sử vẫn nguyên storage đến khi recovery/migration xử lý; không có plaintext fallback.
- Chưa chờ session tự timeout thực tế trong servlet container. Remember là session kéo dài trong memory, không giữ identity sau app restart.
- Customer profile/cart/order/return/review business ownership thuộc Phase11/17/19–24; các route tương ứng đang bị deny, không coi resolver hay UI là đủ ownership.
- Chưa xác nhận distributed rate limiting, deployed HTTPS hoặc cross-browser đầy đủ. Demo H2 là disposable và loopback-only.

## Remaining Tasks

- Chạy read-only inventory, reconcile và migration có backup trên database hiện hữu theo runbook; thay credential legacy qua recovery có xác minh.
- Kiểm thử timed servlet expiry và environment HTTPS thật; follow-up consumer ownership theo Phase owners.

## Verification Result

JDK21 và Maven Wrapper: `mvnw.cmd -o test` — 52/52 pass trên H2; `mvnw.cmd -o test -Dspring.profiles.active=test-postgres` với PostgreSQL16.15 disposable `lunea_test` tại localhost15432 — 52/52 pass. `mvnw.cmd -o package -DskipTests` — BUILD SUCCESS. Trong đó AuthenticationIntegrationTest 14/14 pass trên cả hai databases, existing9 tests vẫn pass.

Auth tests verified BCrypt/no-secret response/default customer, phone identity/tampered customerId, session rotation/remember Secure cookie+timeout value, logout, ACTIVE/version revocation, staff login without Customer/no automatic grants, CSRF/anonymous deny, wrong/blank credentials, password/terms/UTF-8 boundary, throttle, concurrent registration, late Customer rollback và legacy plaintext rejection. P02-T07 timed expiry và P02-T10 deployed legacy inventory chưa verified; không dùng H2/new PG schema để suy ra deployed-data safety.

Root browser smoke dùng Edge headless, DOM thật với 15 checkpoints đều pass, gồm register/login/logout/reset/staff workflows và mobile width390. Demo tại `http://localhost:8080` chạy profile demo. Kết quả và log kiểm thử được tổng hợp trong [báo cáo Phase01–04](../PHASE_01_04_REPORT.md); hồ sơ browser tạm trong `target/browser-smoke/` đã được dọn sau kiểm thử.

## Notes For Next Phase

Dùng `CurrentAccountResolver.requireAccount()/requireCustomerId()/requireEmployeeId()` và AccountPrincipal; không tự dựng session mới hoặc lấy raw ID làm actor. Phase03 dùng PasswordEncoder/PasswordPolicy và tăng credentialsVersion khi reset. Phase04 dùng PermissionPolicy fresh grants, kiểm tra role AND store scope. Consumer chỉ mở private route sau khi service ownership được hoàn thiện và kiểm thử; đọc schema/runbook trước migration. Report này bàn giao local implementation có bằng chứng, không thay inventory của database thực.
