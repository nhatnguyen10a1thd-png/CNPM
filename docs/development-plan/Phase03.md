# Phase 03 — Khôi phục mật khẩu

> Status: IMPLEMENTED_AND_TESTED_LOCAL. Complexity: MEDIUM. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Baseline Current State bên dưới là lịch sử trước khi triển khai. Xem Completion Report và [báo cáo tích hợp](../PHASE_01_04_REPORT.md) cho trạng thái mới.

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

## 4. Preconditions

- [ ] Phase02 COMPLETED: hash/status/identity/errorcontract dùng được.
- [ ] D03chốt verificationmode/channel/provider/config/expiry/rate-limit/attemptpolicy; UIlayoutD01 đượcghi.
- [x] Cótestdeliveryadapter hoặcprovider testconfig, không dùngcredentials thậttrongfixture.
- [ ] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [ ] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

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

- [ ] GhiD03exactstates/I/O/approvedfailure behavior trướccode, không giảđịnhOTP/resetlinkđồngthời.
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
- [ ] Không hiển thị OTP/token trong diagnosticUI hoặc log; inputmasked/labels/accessible.
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
- [ ] Browserforgot→verify→reset→login thực, errors/empty/invalid UI.
- [x] Auth02 login/logout/status behavior vẫn đúng.
- [x] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [ ] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
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
- [ ] Deliverytest/configsafe và conditionalDBmigrationverified.
- [x] Existingauth02 +9baselinetests pass.
- [ ] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [ ] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [ ] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

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

IMPLEMENTED_AND_TESTED_LOCAL (2026-10-07). SMTP ngoài demo và dữ liệu DB deployed chưa kiểm chứng.

## Work Completed

Recovery request/reset dùng token ngẫu nhiên32 byte, DB chỉ lưu SHA-256 hash; TTL15 phút, một lần dùng, khóa account rồi khóa token để tránh replay/concurrent reset. Một token/account, giới hạn3 request/identifier và10 request/IP mỗi15 phút, reset10/IP; phản hồi request chung tránh xác nhận tài khoản tồn tại. Reset dùng cùng password policy Phase02, thay hash BCrypt và tăng `credentialsVersion` để phiên cũ mất hiệu lực. Delivery SMTP thật ở profile thường và outbox in-memory chỉ profile demo/loopback. UI forgot → thư/link → reset → login.

## Files Created

`PasswordResetTokenEntity`, repository, `PasswordRecoveryService`, `RecoveryRateLimiter`, delivery adapter SMTP/demo, recovery DTO/controller; static `index.html`, `app.css`, `app.js` dùng chung Phase02–04; 12 test recovery integration. Xem [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

## Files Modified

`pom.xml` thêm mail dependency; `application.properties` cho SMTP/recovery config; demo/test profile tách biệt; `SecurityConfig` mở đúng request/reset public có CSRF.

## Database Changes

V001 tạo `password_reset_tokens` (account FK, unique token_hash, created/expires/consumed time, indexes) trên PostgreSQL fixture. Chưa áp migration vào DB dev/deployed. [Runbook](../database/README.md) yêu cầu inventory trước áp SQL.

## APIs / Routes Added

POST `/api/auth/recovery/request` `{identifier}` →202 thông báo chung; POST `/api/auth/recovery/reset` `{token,password,confirmPassword}` →200 hoặc400 cho link không hợp lệ/hết hạn/đã dùng. GET `/api/demo/recovery/messages` chỉ ở profile demo, loopback để test thư; không tồn tại trong profile thường.

## UI Added

Form quên mật khẩu, form nhập mật khẩu mới, link reset trong fragment URL và hộp thư demo nội bộ để test không cần SMTP. Hiển thị lỗi field/global, loading và điều hướng login.

## Decisions Made

D03 triển khai reset link qua SMTP, TTL và giới hạn configurable như trên. Token trong fragment không gửi lên server qua request trang; endpoint reset nhận token bằng POST sau khi người dùng mở link. Demo outbox chỉ dành cho yêu cầu tạo giao diện test, không là kênh delivery thường. Logout phiên cũ thông qua phiên bản credential.

## Deviations From Plan

Người dùng yêu cầu UI test tối giản, nên có thêm outbox local ở profile demo. Không triển khai OTP đồng thời. Token/reset/credentials được redacted khi DTO toString; mock provider dùng để thử failure/rollback.

## Known Issues

SMTP credentials/provider thật chưa có nên chưa gửi thử email ngoài demo. Limiter bounded trong một instance, chưa chia sẻ trạng thái giữa các instance. Database deployed chưa migrate. Không suy test clock là đã chờ15 phút thật.

## Remaining Tasks

Cấu hình SMTP và APP_BASE_URL/HTTPS thật rồi test delivery theo môi trường triển khai; áp migration sau inventory DB thật. Các kiểm chứng này chưa được đánh dấu hoàn tất.

## Verification Result

12 recovery tests PASS trên H2 và PostgreSQL16.15: hash-only, happy/reset session, expiry boundary, replay, concurrent one-use, provider outage/late rollback, rate limit, phone/locked account. Toàn bộ suite 52/52 PASS ở cả hai DB. Browser Edge thực chạy forgot → demo-mail → reset → old password bị từ chối/new password đăng nhập trong 15 checkpoint PASS; mobile390px không tràn. V001 fixture và startup validate PASS. Lệnh/log và giới hạn tại [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

## Notes For Next Phase

Phase30 có thể reuse SMTP adapter/config nhưng phải xác định event/delivery riêng. Auth Phase02 nhận `credentialsVersion` đổi sau reset; consumer Phase sau không được dựa vào raw account ID từ browser.
