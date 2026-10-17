# Phase 03 — Khôi phục mật khẩu

> Status: IMPLEMENTED_AND_TESTED_LOCAL. Complexity: MEDIUM. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Baseline Current State bên dưới là lịch sử trước khi triển khai. Xem Completion Report và [báo cáo tích hợp](../PHASE_01_04_REPORT.md) cho trạng thái mới.
> Kiểm chứng lại 2026-10-08: giữ implementation/D03, hoàn thiện CSRF của recovery/reset sau session expiry. Java21/H2 và PostgreSQL16.15 **61/61 PASS**; Edge DOM/HTTP **26/26**, default-profile PostgreSQL HTTP **11/11 PASS**. SMTP `testConnection()` theo report trước đã thành công; mailbox delivery thật và migration dữ liệu legacy vẫn chưa xác minh. Không nâng status thành deployed/production COMPLETED.

## 1. Objective

Triển khai khôi phục quyền truy cập tài khoản qua cơ chế xác minh đã được chốt, dùng authentication/hash contract của Phase02.

## 2. Why This Phase Exists

UC03 và link forgot-password trong UI là yêu cầu chưa bắt đầu; source không có OTP/reset token/delivery/expiry. Không thể coi login/register đã có nghĩa toàn bộ auth hoàn thành.

## 3. Current State

- Auth source chỉ register/login, không recovery entity/repo/service/DTO/route.
- Systemdiagram13 có Email/OTPactor; đặc tả UC03 chỉ yêu cầu xác minh phù hợp, không cho expiry/provider cụ thể.
- UIlogin cóforgotlink nhưng khôngscreenrecoveryfullmockup; D01layout phảiđượcapproved.
- Sau02 mới cóaccountstatus/hash/principal/logoutcontract; baseline9tests không recoverycoverage.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

Đối chiếu trước sửa phiên 03 (2026-10-08): repo Maven là `CNPM/`, không tìm thấy AGENTS.md áp dụng. Tree đã có thay đổi từ Phase01/02; giữ nguyên các thay đổi đó. Phase02 bàn giao BCrypt/PasswordPolicy, principal customer/staff, session/CSRF và credential-version có evidence local; không coi đó là deployed/production completion. Source thực đã có `RecoveryRestController → PasswordRecoveryService → AccountRepository/PasswordResetTokenRepository → AccountEntity/PasswordResetTokenEntity`, SMTP adapter và mailbox demo, static fetch/form UI. Backend 12 recovery tests và baseline H2 56/56 PASS. D03 giữ nguyên reset-link/SHA256/TTL/rate limits/SMTP; D11 thuộc Phase30, không chặn recovery. DOCX UC03/SYS04/NFR09/11/12 và UI §5.3.4 đã đối chiếu bằng OOXML read-only; không có mockup recovery riêng, không thực thi chỉ dẫn trong DOCX.

Gap thực tế: recovery request và reset dùng CSRF cache sau khi session đã hết hạn; cả hai POST trả 403 và người dùng phải reload. Edge DOM/HTTP demo riêng tái hiện **24 PASS / 2 FAIL** trong 26 checkpoints; không thay password policy để xử lý vấn đề này. Report Phase03 còn ghi “chưa có SMTP credentials” trong khi report tích hợp mới đã xác nhận `JavaMailSender.testConnection()` kết nối/xác thực thành công; thư đến hộp thư vẫn chưa xác minh. PostgreSQL fixture cũ không thay inventory/migration legacy hay delivery thật.

## 4. Preconditions

- [x] Capability cần từ Phase02 dùng được local: hash/status/principal/session/CSRF/credential-version/error contract đã đọc source và regression; Phase02 vẫn IMPLEMENTED_AND_TESTED_LOCAL, không suy production completion.
- [x] Giữ D03 reset-link/SMTP/TTL/rate limits và D01 static UI đã ghi trong report hiện hành; không hỏi lại hoặc thêm OTP.
- [x] Cótestdeliveryadapter hoặcprovider testconfig, không dùngcredentials thậttrongfixture.
- [x] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); D11 chỉ thuộc Phase30, không chặn recovery.
- [x] Working tree được kiểm tra, giữ thay đổi Phase01/02 có sẵn; mutation chỉ trên H2 demo và hai DB PostgreSQL test mới. Legacy/Supabase không bị sửa hoặc reset.

## 5. Scope

### IN SCOPE

- Recovery request/verification/reset/confirmation lifecycle theoUC03.
- Hashreset qua02, one-time/invalidation/correctaccountcheck vàtestproviderfailure.
- Forgot-password UI tối thiểu theoapprovedlayout, reuseauthviews/errorcontract.
- Deliveryadapter cần cho recovery; không thông báoorder/system inbox.

### OUT OF SCOPE

- Notification subsystem/events choorders thuộc30; campaign/support/businessmodule khôngliênquan.
- Sociallogin/MFA/SSO và change-passwordfeature riêng ngoàiUC03 nếu chưaapproved.
- Email/SMS provider hoặcbroker AI tựchọn; recoveryauto cấpnewaccount/restoreinactiveaccount.
- Không tựthaypasswordpolicy02 hoặcchọnwindowTTL nhưbusinessrequirement.

## 6. Requirements Covered

- UC03;SYS04,NFR09/11/12; diagram system-image13 Email/OTPservice.
- UI§5.3.4forgot-password navigation; nospecificrecoverymockup, D01approvedlayout.
- D03verification, expiry/attempt/delivery settings prerequisite;D11tươngthíchsharedadapternếu30 reuse.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [AuthRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/AuthRestController.java)
- [AccountService.java](../../src/main/java/com/thinh/cosmetic/service/account/AccountService.java)
- [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java)
- [AccountRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java)
- [AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java)
- [LoginRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/LoginRequest.java)
- [application.properties](../../src/main/resources/application.properties)
- [pom.xml](../../pom.xml)

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [AuthRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/AuthRestController.java)
- [AccountService.java](../../src/main/java/com/thinh/cosmetic/service/account/AccountService.java)
- [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java)
- [application.properties](../../src/main/resources/application.properties)
- [pom.xml](../../pom.xml)

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- Approvedrecoveryrequest/verify/resetDTOs và service/controlleroperations; packageaccountconventions.
- Verificationstate repository/entity chỉnếumechanismD03 cầnpersistence; không tựtạoOTPschemahoặcunboundedhistory.
- Deliveryadapter/testdouble chochosenchannel; configexternalized.
- Recovery views/client +integrationtests theoD01.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

Conditional on D03. Nếu chọn persistent one-time verification state, phải ghi rõ account linkage, expiry/consumed/attempt fields tối thiểu và uniqueness/index theo lookup, rồi migration theo01. Nếu cơ chếapproved không cần schema: ghi NONE trongreport. Không lưu rawpassword/OTP/resetsecret trong logs/plainpersistentstate; khôngthay AccountPK hoặc xóa customerhistory.

## 11. Backend Tasks

- [x] Đối chiếu D03 exact states/I/O/failure behavior hiện có trước sửa; generic202 cho unknown/locked/throttled/delivery failure, rollback state khi gửi lỗi; không thêm OTP.
- [x] Reuseaccountlookup/hash/status từ02; khôngnewcredentialstorage hoặccreateCustomer khi phục hồi.
- [x] Implement requestverification phù hợpselectedchannel, safe response tránhaccountenumeration.
- [x] Implement expiry/attempt/one-timeconsumption theoapprovedpolicy; targetaccountchỉtừverifiedstate, khôngrawaccountIdbrowser.
- [x] Persist hoặcverifysecret bằngsafe mechanism phù hợpD03; replay/expired/invalidsecret không đượcreset.
- [x] Atomicconsumeverification+updatecredential; parallelreset/replay không dùngstatehai lần.
- [x] Handleproviderfailure/retry policy có xác nhận; khôngreport resetthànhcông khi chưaverified.
- [x] Invalidate hoặchandleprioridentitytheo02/D03policy saureset; khôi phục không tựmởaccountinactive.
- [x] Createintegrationtestsvalid/invalid/expired/replay/concurrent/providerfailure.

## 12. Frontend Tasks

- [x] Nối forgot link02 tới actualrecoveryrequest vàsteps theoD03.
- [x] Showfields/state/confirmation/expired/errors/loading bằngsharedlayout, khôngclaim cómockupđầyđủ.
- [x] Token không xuất trong API/log/UI thường; link demo chỉ mailbox in-memory/loopback theo ngoại lệ test hiện có. Password masked/labels, fragment được xóa, không lưu secret vào local/sessionStorage.
- [x] Finalresetvalidation giữpasswordpolicy02/confirm; successroute tới login theoapprovedflow.
- [x] Providerfailure/nonexistentaccount response phù hợpanonymitypolicy, không accountenumeration message.

## 13. Validation & Business Rules

- Verification trướccredentialupdate; not trustactoraccountIdpassedfrombrowser.
- Single-use, boundedexpiry/attemptpolicy phảiđượcchốt; no magicTTL.
- Credentialhash chung02; account/customer/historypreserved.
- UC03 áp dụng allaccountactors cótài khoản; không customer-onlybranch.
- Notificationdeliveryfoundation chỉcầnrecovery;SYS22fullsubsystemphase30.

## 14. Security Requirements

Recovery entrypoint có thểpublic nhưng cần verification/abusecontrols theoD03. Reset không grantrole/scope hoặcreactivateaccount. Secret không response/log; timeout/replay/paralleluse phảideny; credentialchange compatibility vớilogout/expiry02 đượctest.

## 15. Error Handling

- Invalid/expired/consumedstate: no passwordchange, controllederror theo01.
- Unknownidentifier: response approvedanti-enumeration; no datawrite.
- Providerfailure: controlledretry/configerror, nofakeverification/success.
- DBlatefailure: no consume state mà credential vẫncũ hoặcngượclại.

## 16. Integration Points

- 02hash/status/authcontract;04staffaccountcanrecover.
- 30reuseapproveddeliveryadapter nếusamechannel, no03dependencyon30.
- 32E2Eforgotflow/browserstate/secrets validation.

## 17. Implementation Order

1. ChốtD03flow/delivery/config vàreview02contracts.
2. Prepareconditionalstate migration/testfixtures.
3. Implementadapter+request/verify/resetatomiccommands.
4. BuildforgotUI nốiactualoperations.
5. Runexpiry/replay/race/providerfailure/regressionlogin;recordhandoff.

## 18. Verification

- [x] Build/testsJDK21+Postgres teststate nếuD03 persistence.
- [x] Không resetwithoutverification; expiry/one-time/racecases chạy.
- [x] Provider failuretestdouble khôngcallsproduction; password/state secrets khônglog.
- [x] Edge DOM/HTTP forgot→mailbox demo→reset→login, errors/empty/invalid/expired session UI: 26/26 PASS; không suy mailbox demo thành SMTP thật.
- [x] Auth02 login/logout/status behavior vẫn đúng.
- [x] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [x] Diff của phiên03 chỉ recovery UI/test/report; giữ nguyên dirty tree Phase01/02 có sẵn, không mở route domain hay refactor backend.
- [x] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P03-T01 | Happy recovery | Validaccount+approvedverification | Reset hashedcredential; oldpasswordfails, newpasswordlogin works |
| P03-T02 | Unknown account | Unregisteredidentifier | Approvedgenericresponse, nocreateaccount |
| P03-T03 | Invalidsecret | WrongOTP/token | No credential change, bounded attempt behavior |
| P03-T04 | Expiredstate | Secretoutsideapprovedexpiry | Rejected; no update/consume-as-success |
| P03-T05 | Replay | Consumedverification reused | Rejected; firstresetonly |
| P03-T06 | Concurrent consumption | Two resetrequests samevalidstate | Exactlyone succeeds; state/credentialconsistent |
| P03-T07 | Provider outage | Deliveryadapterthrows/timeouts | Controlledfailure/retry; nofakeverifiedstate |
| P03-T08 | Weak/mismatchpassword | Invalidnewpassword/confirm per02 | Fieldvalidation; verificationnotlostunexpectedly |
| P03-T09 | Inactiveaccount | Recoverinactiveaccount | No unauthorizedreactivation; approvedstatuspolicy |
| P03-T10 | LateDBfailure | Credentialwrite failsafterstateupdateattempt | Transactionrollback leavesconsistentstate |

## 20. Definition of Done

- [x] UC03 request/verify/reset flow và screens thựcđápứngD03.
- [x] Hash/noenumstatus/rate/expiry/attempt/replay/parallelpolicy kiểmchứng.
- [ ] SMTP delivery đến hộp thư thật và migration/inventory dữ liệu legacy: chưa xác minh. Adapter failure/rollback, demo isolation và schema fixture/validate PostgreSQL đã PASS local.
- [x] Existingauth02 +9baselinetests pass.
- [x] Checkbox phân biệt evidence local với phần chưa kiểm chứng; không có business gate mở chặn sửa gap local, các giới hạn môi trường được ghi.
- [x] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit/Master/D03 nhận cập nhật evidence, giữ status/dependency hiện tại.
- [x] Auth14, staff13 và existing9 regression PASS trên H2/PostgreSQL; giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Có recoveryflow được xác minh, sử dụng đúng hash/accountstate. Không tồn tại recovery bypass hoặcorder-notificationfeaturepháttriểnsớm.

## 22. Handoff To Next Phase

- 04staffaccounts dùngsame recovery mechanism;30 cóadapterinterface/config/error contract nếuphùhợpchannel.
- Recordexactroutes/request/response/state storage/expiry/attemptlimits/providerfixture và identityinvalidationpolicy.
- 32 dùngrecoverytestfixtures và browsersteps từreport.

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

IMPLEMENTED_AND_TESTED_LOCAL, kiểm chứng lại 2026-10-08. Gap CSRF recovery/reset của phiên03: **COMPLETED**. Giữ status Phase hiện có; không đánh dấu toàn bộ Phase/deployed COMPLETED vì thư đến hộp thư SMTP thật và migration/inventory legacy chưa có evidence. `testConnection()` kết nối/xác thực SMTP thành công theo report tích hợp trước, không phải phép thử gửi thư.

## Work Completed

Recovery request/reset dùng token ngẫu nhiên32 byte, DB chỉ lưu SHA-256 hash; TTL15 phút, một lần dùng, khóa account rồi khóa token để tránh replay/concurrent reset. Một token/account, giới hạn3 request/identifier và10 request/IP mỗi15 phút, reset10/IP; phản hồi request chung tránh xác nhận tài khoản tồn tại. Reset dùng cùng password policy Phase02, thay hash BCrypt và tăng `credentialsVersion` để phiên cũ mất hiệu lực. Delivery SMTP thật ở profile thường và outbox in-memory chỉ profile demo/loopback. UI forgot → thư/link → reset → login.

Phiên03 chỉ sửa form request/reset lấy CSRF mới trước POST để xử lý session expiry; không tự retry mutation bị 403 hoặc tắt CSRF. Bổ sung 5 integration tests cho trước/sau expiry 1µs, replacement delivery failure giữ link cũ, token flush failure sau credential flush rollback/retry, IP limit kể cả unknown identifiers ở đúng window boundary, và nhân viên có profile thật giữ role/permission/store/profile và revoke phiên. Toàn bộ backend recovery hiện có được giữ nguyên.

## Files Created

Capability đã có từ lần triển khai trước: `PasswordResetTokenEntity`, repository, `PasswordRecoveryService`, `RecoveryRateLimiter`, delivery adapter SMTP/demo, recovery DTO/controller; static `index.html`, `app.css`, `app.js` dùng chung Phase02–04; 12 test recovery integration. Không tạo lại các file này. Xem [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

File mới phiên03: `scripts/verify-phase03-browser.{py,js}`, `scripts/verify-phase03-http.py`. Browser Python tái sử dụng transport CDP đã có theo pattern Phase02, fresh Edge profile và loopback cố định; không truy cập profile browser người dùng, không thêm dependency ứng dụng.

## Files Modified

`pom.xml` thêm mail dependency; `application.properties` cho SMTP/recovery config; demo/test profile tách biệt; `SecurityConfig` mở đúng request/reset public có CSRF.

Các thay đổi trên là capability trước phiên03. Phiên03 sửa `src/main/resources/static/app.js` tại hai handler recovery/reset và `src/test/java/com/thinh/cosmetic/security/PasswordRecoveryIntegrationTest.java` (12 →17 tests); cập nhật `Phase03.md`, Audit/Master/D03 và report tích hợp. Không sửa `pom.xml`, entity/repository/service/controller/security/config/migration. Tree ban đầu tại HEAD `179bc49` đã có thay đổi Phase01/02; snapshot đối chiếu tại `target/phase03-before/`, không nhận chúng là work của phiên03.

## Database Changes

V001 tạo `password_reset_tokens` (account FK, unique token_hash, created/expires/consumed time, indexes) trên PostgreSQL fixture. Chưa áp migration vào DB dev/deployed. [Runbook](../database/README.md) yêu cầu inventory trước áp SQL.

Phiên03 **NONE** schema/migration changes. PostgreSQL16.15 cluster mới `.local/phase03/pgdata` chỉ listen `127.0.0.1:15433`: suite mutation vào `lunea_phase03_test` (create-drop); HTTP vào `lunea_phase03_http_test` (schema-only `POSTGRES16_TEST_SCHEMA.sql` rồi default app `validate`). Hai DB riêng không có dữ liệu kinh doanh. Sau HTTP: 2 accounts/2 customers test, 0 reset_tokens, max credentials_version=0, xác nhận failure không consume/update. Không dùng cluster `.local/phase01/pgdata`, localhost5432 hay Supabase. Schema Supabase mới theo report trước không chứng minh legacy migration.

## APIs / Routes Added

POST `/api/auth/recovery/request` `{identifier}` →202 thông báo chung; POST `/api/auth/recovery/reset` `{token,password,confirmPassword}` →200 hoặc400 cho link không hợp lệ/hết hạn/đã dùng. GET `/api/demo/recovery/messages` chỉ ở profile demo, loopback để test thư; không tồn tại trong profile thường.

Route contract **không đổi** trong phiên03. Hai POST public vẫn cần session CSRF và limiter từ `request.getRemoteAddr()` (không tin forwarded IP). Reset target account chỉ từ token hash; malformed/expired/consumed/locked →400 `INVALID_RECOVERY_TOKEN`; quá10 reset/IP/window →400 `RECOVERY_RATE_LIMIT`; DTO validation →400 `VALIDATION_FAILED`, policy/confirm →400 `BAD_REQUEST`. Profile thường không có demo controller: boundary trả401 anonymous/403 authenticated khi gọi mailbox. Không mở domain API đang deny.

## UI Added

Form quên mật khẩu, form nhập mật khẩu mới, link reset trong fragment URL và hộp thư demo nội bộ để test không cần SMTP. Hiển thị lỗi field/global, loading và điều hướng login.

UI đã tồn tại; phiên03 giữ static HTML/CSS/layout và password policy02, chỉ cập nhật CSRF trước submit. 26 checkpoint Edge thật và ảnh mobile390 kiểm tra forms/error/loading, giữ token khi password invalid, fragment removal, không web storage, consumed inbox pruning, old/new password login và revoke phiên. Demo mailbox chỉ là ngoại lệ test được yêu cầu, không là diagnostic API ở cấu hình thường.

## Decisions Made

D03 triển khai reset link qua SMTP, TTL và giới hạn configurable như trên. Token trong fragment không gửi lên server qua request trang; endpoint reset nhận token bằng POST sau khi người dùng mở link. Demo outbox chỉ dành cho yêu cầu tạo giao diện test, không là kênh delivery thường. Logout phiên cũ thông qua phiên bản credential.

Giữ D03/D01/D02 theo chỉ dẫn người dùng phiên03, không tự đặt business rule. Config giữ `lunea.recovery.ttl-minutes`, `request-limit`, `ip-limit`, `reset-limit`, `request-window-minutes`, `public-base-url`, `sender` và biến SMTP đã externalize. Không hỏi lại các quyết định này. D11 vẫn OPEN thuộc Phase30. Khi cần gửi email thật phải xác định hộp thư test và phạm vi gửi được cho phép trước; phiên này không đọc `production.env` hay gửi thật.

## Deviations From Plan

Người dùng yêu cầu UI test tối giản, nên có thêm outbox local ở profile demo. Không triển khai OTP đồng thời. Token/reset/credentials được redacted khi DTO toString; mock provider dùng để thử failure/rollback.

Phiên03: terminal/Node trong sandbox không khởi động được; dùng PowerShell được phép ngoài sandbox và installed Edge headless với fresh temporary profile. DOCX chỉ đọc OOXML yêu cầu, không chỉnh/render/deliver DOCX hay claim page fidelity. Không có technical recommendation mới cần chốt; giữ bounded single-instance limiter. Test normal SMTP outage dùng cổng loopback đóng, không gọi provider thật.

## Known Issues

SMTP provider đã test kết nối/xác thực theo report trước; **chưa xác minh thư đến hộp thư**, không suy `testConnection()` thành delivery PASS. Limiter bounded trong một instance, chưa chia sẻ trạng thái giữa các instance. Database legacy chưa inventory/migrate; Supabase schema mới và app local không thay evidence dữ liệu cũ. Không suy test clock là đã chờ15 phút thật; session test chờ3s chỉ xác minh gap CSRF, không đổi token TTL15m hoặc chứng minh HTTPS deployed/cross-browser đầy đủ.

## Remaining Tasks

Xác định hộp thư test và phạm vi gửi được phép, kiểm tra MAIL_FROM/APP_BASE_URL/HTTPS rồi kiểm chứng email đến hộp thư thực qua request→link→reset→login. Đây là phần **UNVERIFIED**, không tự gửi tới account hiện hữu. Inventory/reconcile/backup và migration legacy theo runbook khi có môi trường/quyền phù hợp. Các phần này không chặn capability local đã tồn tại; không đánh dấu deployed readiness. Không có gap source recovery local còn phát hiện sau các kiểm chứng bên dưới.

## Verification Result

12 recovery tests PASS trên H2 và PostgreSQL16.15: hash-only, happy/reset session, expiry boundary, replay, concurrent one-use, provider outage/late rollback, rate limit, phone/locked account. Toàn bộ suite 52/52 PASS ở cả hai DB. Browser Edge thực chạy forgot → demo-mail → reset → old password bị từ chối/new password đăng nhập trong 15 checkpoint PASS; mobile390px không tràn. V001 fixture và startup validate PASS. Lệnh/log và giới hạn tại [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

### Phiên03 — verification 2026-10-08

- Java21.0.10/Maven Wrapper, PostgreSQL16.15 portable, Python3.13.7 và Edge154.0.4258.62. Không có AGENTS.md được tìm thấy trên các ancestor workspace/repo hoặc bên trong repo. Phiên chỉ dùng test DB riêng; production secrets không đọc, email không gửi ra ngoài.
- Baseline `.\mvnw.cmd -o test '-Dlogging.level.org.hibernate.SQL=OFF'`: **56/56 H2 PASS**, cùng command thêm `-Dspring.profiles.active=test-postgres` với `TEST_DB_URL=jdbc:postgresql://127.0.0.1:15433/lunea_phase03_test`, `TEST_DB_USERNAME=lunea_phase03`: **56/56 PostgreSQL PASS**; logs `target/phase03-{h2,postgres}-before.log`.
- Sau sửa: `$env:JAVA_HOME='C:/Program Files/Java/jdk-21.0.10'; .\mvnw.cmd -o test '-Dlogging.level.org.hibernate.SQL=OFF' '-Ddebug=false' '-Dlogging.level.org.springframework.boot.security.autoconfigure=OFF'`: **61/61 H2 PASS**. Với test DB settings trên và thêm `'-Dspring.profiles.active=test-postgres'`: **61/61 PostgreSQL PASS**, gồm recovery17, auth14, staff13 và existing9. Logs `target/phase03-{h2,postgres}-final.log`. Tắt DEBUG cho phép thử vì environment có DEBUG; không in tokens/credentials trong report. Lần đầu test mới lỗi `doCallRealMethod()` trên repository proxy (60 PASS/1 test error); sửa cách reset spy stub, chạy lại toàn suite PASS, không sửa backend để vượt test.
- `initdb.exe -D .local/phase03/pgdata -U lunea_phase03 --encoding=UTF8 --locale=C --auth=trust`; `pg_ctl.exe -D .local/phase03/pgdata -l .local/phase03/postgres.log -o '-h 127.0.0.1 -p 15433' -w start`; tạo hai DB `_test` mới bằng `createdb.exe`. Binaries lấy từ `.local/phase01/postgres16/pgsql/bin`; không dùng data directory cũ. Default HTTP fixture dùng `psql.exe -h 127.0.0.1 -p 15433 -U lunea_phase03 -d lunea_phase03_http_test -X -v ON_ERROR_STOP=1 -f docs/database/POSTGRES16_TEST_SCHEMA.sql`; startup default `validate` thành công, log `target/phase03-http-schema.log`.
- `.\mvnw.cmd -o package -DskipTests`: **BUILD SUCCESS**, `target/phase03-package.log`. Cả app verification chạy jar đã package sau sửa qua `Start-Process -WindowStyle Hidden`, không dùng baseline jar. Demo argv: `java -jar target/cosmetic-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo --server.port=18083 --lunea.auth.session-timeout=3s --lunea.recovery.public-base-url=http://127.0.0.1:18083 --debug=false --logging.level.root=WARN --logging.level.org.springframework.boot.security.autoconfigure=OFF --logging.level.org.hibernate.SQL=OFF`. H2 demo in-memory và server loopback theo profile.
- `py scripts/verify-phase03-browser.py`: trước sửa **24 PASS/2 FAIL**, sau sửa **26/26 PASS**, logs `target/phase03-browser-{before,final}.log`. DOM/forms + HTTP/fetch thật, không mock fetch/clock. Chờ5s rồi `/api/auth/me` trả401 xác nhận servlet session3s thật hết hạn trước request và reset; không chỉ đo timeout config. Ảnh `target/phase03-browser/{recovery,reset}-mobile.png` đã xem: mobile390 không clipping/tràn ngang. Fresh Edge profile đóng/xóa sau run.
- Default app argv: `java -jar target/cosmetic-0.0.1-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=18084 --spring.datasource.url=jdbc:postgresql://127.0.0.1:15433/lunea_phase03_http_test --spring.datasource.username=lunea_phase03 --spring.datasource.password= --spring.mail.host=127.0.0.1 --spring.mail.port=55999 --spring.mail.username= --spring.mail.password= --debug=false --logging.level.root=WARN --logging.level.org.springframework.boot.security.autoconfigure=OFF --logging.level.org.hibernate.SQL=OFF`; SMTP port55999 kiểm tra không có listener. `py scripts/verify-phase03-http.py`: **11/11 PASS**, `target/phase03-http-final.log`; actual SMTP connection failure → generic202 giống unknown, reset invalid400, CSRF403, normal mailbox401 anonymous/403 authenticated, old password vẫn login được, logout204. Lần đầu oracle anonymous mailbox kỳ vọng403 sai; chỉnh thành401 đúng auth boundary, không mở route. SQL snapshot `target/phase03-http-db.log`: 2 fixture accounts/customers, reset_tokens0, credential_version0. Search normal app stdout/stderr không chứa token/password thử: PASS; không phải kiểm chứng provider delivery.
- P03-T01–T10: integration17 PASS cả H2/PostgreSQL; T04 có trước/exact/sau expiry, T06 race đúng một commit, T07 giữ prior link khi replacement delivery lỗi, T10 rollback ở cả account và token flush/retry; staff profile/grants/session và identifier/phone/IP/reset window được giữ. HTTP/UI bổ sung độc lập như trên. H2/PG fixtures không chứng minh legacy/SMTP inbox/HTTPS production.
- `git diff --check` và diff phiên03 được kiểm tra riêng với snapshot tree ban đầu; source ứng dụng chỉ thay hai recovery handlers của `app.js`. Demo/default app của phiên03 và cluster PostgreSQL test được dừng sau kiểm chứng; không dừng app khác. Dừng tại Phase03.

## Notes For Next Phase

Phase30 có thể reuse SMTP adapter/config nhưng phải xác định event/delivery riêng. Auth Phase02 nhận `credentialsVersion` đổi sau reset; consumer Phase sau không được dựa vào raw account ID từ browser.

Capability recovery local **đã tồn tại**, nay có regression17 cả PostgreSQL/H2 và browser/HTTP hiện tại; Phase04 có thể kiểm chứng staff flow dựa trên principal/permission/scope đã có, không dựng lại auth/recovery hoặc chạy tự động trong phiên này. Nếu dùng DB legacy, prerequisite inventory/reconcile/backup/migration còn phải hoàn tất. Phase30 vẫn cần D11 event/recipient/channel/failure decision cùng dependencies19/22; không coi recovery adapter thay notification subsystem. SMTP delivery thật cần hộp thư test và phạm vi gửi được cho phép; không dùng chỉ testConnection làm handoff delivery đã triển khai.
