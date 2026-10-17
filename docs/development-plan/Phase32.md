# Phase 32 — Kiểm chứng tích hợp và đóng dự án

> Status: NOT_STARTED. Complexity: HIGH. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Đây là kế hoạch phát triển phần còn thiếu; tài liệu này chưa thực hiện code, migration hoặc verification của Phase.

## 1. Objective

Kiểm chứng tích hợp toàn bộ phạm vi được xác nhận và đóng final checklist bằng bằng chứng chạy thực.

## 2. Why This Phase Exists

Mỗi module có Phase riêng, nhưng frontend/API/security/stock/schema/reports/integration phải chạy cùng nhau. Baseline9H2tests không chứng minh realPostgres/HTTP/UI hoặc coverage toàn bộ design. Phase cuối không là nơi chứa tính năng bị bỏ sót.

## 3. Current State

- Baseline lúc lập docs có37entities/17REST,9H2pass,Postgresrefused,noUI/security; các Phase01–31 vẫnNOT_STARTED trước thực hiện.
- Phải đọc Completion Reports để lấy actualroutes/schema/UI/config/decisions; không dùng anticipatedAPI nhưexisting.
- 8screenscustomer vàstaffcapabilities phải được kiểm chứng bằng requirements/approvedlayouts, nofakeadminmockupclaim.
- Gate D13 benchmarknormalconditions khoảng3s chưa chốt; browser list Chrome/Edge/Firefox/Safari vàresponsive đã cóNFR.
- Reports/notifications/backup/media/recovery đã cóowners riêng, không thêmchúng nhưfeature mớiở32.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [ ] Phase01–31 COMPLETED vớiDoD/evidence hoặc có explicit scope-decision được xác nhận và cập nhậtAudit/Master trước32.
- [ ] Tất cả gate có ảnh hưởng acceptance đãresolved; thiếudecision phải BLOCKED, khônggiấuassumption.
- [ ] D13 dataset/load/config/browser/cache/scenarios measurement được xác nhận.
- [ ] RealPostgres test/restoreenvironment vàexternalintegrationtestadapters từ31/03/07/09/30 sẵn sàng.
- [ ] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [ ] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

## 5. Scope

### IN SCOPE

- Requirements traceability closure và regression/integration/security/browser/DB validation.
- Kiểm chứng91UC/32SYS/24NFR/37tables/8screens/63businessrules theoAudit cập nhật.
- Sửa integration/regression defects nhỏ thuộc accepted contracts; domainfeaturemissing chuyểnowner/report vàrerun liênquan.
- Update finalchecklist/Audit/roadmap/reports/README với evidence vàknownlimitations.

### OUT OF SCOPE

- New business feature/framework/DB migration redesign hoặc“backend/frontend toànhệthống”rebuild.
- Phase32 tựquyết gate, inventreportKPI/returnpolicy/fee/provider hoặcimplement omittedowners.
- Blanketrefactor/performanceoptimization khi chưa measuredproblem.
- Deploy production/publish/send externalmessages được tựđộng coi authorized.

## 6. Requirements Covered

- Toàn91UC +SYS01–32 +NFR01–24 +37tables +8screens +63QĐ trongAudit.
- NFR04 responsive,05 browsers,06–07 khoảng3s normalconditions,09–17security/history/consistency/backup đặc biệt cầnrealverification.
- No-AI/KH6, COD/KH10, beforeSHIPPINGcancel/KH13, onebranchALLSKU/QLDH3,available<=minimum/QLTK9.
- D13 measurementconditions; mọi remaininggate phải cóexplicitresolution/scope decision.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [pom.xml](../../pom.xml)
- [application.properties](../../src/main/resources/application.properties)
- [docker-compose.yml](../../docker-compose.yml)
- [README.md](../../README.md)
- [InventoryServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/InventoryServiceTest.java)
- [OrderServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/OrderServiceTest.java)
- [VoucherServiceTest.java](../../src/test/java/com/thinh/cosmetic/service/VoucherServiceTest.java)
- [00_PROJECT_AUDIT.md](00_PROJECT_AUDIT.md) và [01_MASTER_ROADMAP.md](01_MASTER_ROADMAP.md).
- ToànCompletionReports01–31 vàactualtests/routes/views/config/migrations họtạo; traceabilityràngbuộcphải đọcsource thực.

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- Existing tests/integration/browser fixtures chỉ theoacceptedfeaturecontracts vàdefects verified.
- Sourceownerfiles nếu nhỏ/regression fixcần thiết, ghi liênkếtPhaseowner/report; khôngnewfeaturebucket.
- [FINAL_CHECKLIST.md](FINAL_CHECKLIST.md), [00_PROJECT_AUDIT.md](00_PROJECT_AUDIT.md), [01_MASTER_ROADMAP.md](01_MASTER_ROADMAP.md), README vàCompletionReports liênquan.

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- Regression/integration/browser tests/fixtures vàverificationreportartifacts cần choacceptedrequirements.
- Không tạoapplicationmodule/UIfeature/bảngmới chỉ để vượtfinalchecklist.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

NONE — không thêm/đổi schema nghiệp vụ trongPhase32. Verify migrations lênclean vàrepresentative existing/restored testDB; new schema defect phải ghiowner/decision, cập nhậtphase/report rồimigrate kiểmchứng đúngprocess. No resetprimaryDB/nolegacydata guess. TestDB artifacts allowed theo31.

## 11. Backend Tasks

- [ ] ReadallCompletionReports vàactualcode; cậpnhậttraceabilitystatus/evidence, khôngcheckboxes bằngfileexistence.
- [ ] Build/test/startup Java21+Postgres test; verifyversionedmigrations từclean/existingdata/restoredDB.
- [ ] RunHTTPpublic/private/admin role/scope/owner matrix, authregister/logout/expiry/recovery/inactivecases.
- [ ] Runpositive/nestedvalidation/structurederrors/searchfilterpage/sort/empty/boundarycases acrossmodules.
- [ ] Runlatefailuretransactiontests checkout/purchase/transfer/return vàconcurrentlaststock/lastvoucher/repeatedcompletion/cancel/shippingraces.
- [ ] Verifystockactual>=held>=0/available formulas/holdsrelease/commit/returnrestock/lowstockavailablethreshold; no doublecounts.
- [ ] Verifyprices/snapshots/history afterproductpricechange/deactivation andreportsCOMPLETED/metricsapproved.
- [ ] Verifyintegrationproviderfailure/no-real-send/imagecompensation/notifications/backuprestoreconfig usingnamedtestenvironment.
- [ ] Measuresearch/mainpages thời gian theoD13; tối ưu chỉ bottleneck cóevidence vàscope bounded.
- [ ] Record defects byowner vàresolve/retest exactaffectedflows, khônghoànthànhnếu cócriticalgaps.

## 12. Frontend Tasks

- [ ] Walk8primarycustomer screens withactualroutes/backenddata vàcomparefield/layout/state toDOCX images.
- [ ] Testhome/list/detail/search/filter/sort/page→wishlist/cart→CODcheckout/success→orders/cancel/return/review.
- [ ] Teststaffscreens theoUC+approvedlayoutD01, no claimpixelmatchto nonexistentmockups.
- [ ] Responsive desktop/tablet/phone và Chrome/Edge/Firefox/Safari evidence; nếubrowserenvironmentthiếughiNOTVERIFIED/blockedacceptance tươngứng.
- [ ] Checklabels/keyboard/passwordtoggle/disabledform/loading/empty/error/permission/headercounter/navigationlinks.
- [ ] VerifyHTTPS/mixedcontent/images/Maps/authcookies với31testtopology; nofakeorder/price placeholders.

## 13. Validation & Business Rules

- Coverageowner/evidence/gate exhaustive; actualimplementationstatus chỉDONE/PARTIAL/NOT_STARTED/BROKEN/UNKNOWN.
- EveryPhaseCOMPLETE bằngtests/DoD/report; no remaininggate hiddeninassumptions.
- 9H2existingtests làregressionbaseline, notfinalacceptance.
- Performance~3s normalconditionapproved; noinventedp95/loadSLA.
- NoAI/noonlinegateway/CMS/wholesaleunlessscopeexplicitlyrevised trướcphase.

## 14. Security Requirements

Fullauth/permission/branchscope/customerownership tests áp dụngdirectHTTP tamperIDs, khôngchỉbrowsercontrols. Passwordstorage/nohashresponse/noerrorleak/CSRF/CORS/tokenorcookie theoapprovedD01 được kiểm chứng. Secrets vàbackup/providertestdata protected; khôngrealmessages hoặcproductionmutationtrongtest.

## 15. Error Handling

- Testfailure/requirementmissing: recordexactscenario/owner/severity, keepstatusaccurate.
- Missingbrowser/DB/providerconfig: notPASS; recordlimits vàblockacceptance ifrequired.
- Openpolicygate: BLOCKED/reconciledecision, khôngautoselect.
- Unexpectedregression: boundfixscope, updaterelatedreport vàrerun onlyjustifiedtests.

## 16. Integration Points

- Toàn01–31; FINAL_CHECKLIST làacceptanceindex, Auditstatus làtraceabilityrecord.
- Reportsreuseexistingexport/query; notificationsreusecommit events; backuprestore/TLS31environment.
- Nextagentmaintenancerunbook/routes/schema/testcommands phảiđủkhôngcầnđoán.

## 17. Implementation Order

1. Readreports/source vàfreezeacceptedrequirements/gates/testenv.
2. Runbuild/Postgresmigrations/HTTP/security/domainatomicintegration.
3. Run8screens/staffUC/browserresponsive/providerfailure/backupconfig.
4. MeasureperformanceD13 vàtriage defectsowners; fixsmallregressions/retest.
5. Updateallchecklists/status/reports/docs withreal evidence;finalhandoff.

## 18. Verification

- [ ] Build/testPostgres/startup/migrations clean/existing/restoredtestDB pass.
- [ ] 91UC/32SYS/24NFR/37tables/8screens/63QĐ matrix hasexplicitfinalevidence/status.
- [ ] HTTProle/owner/scopes/state/atomic/race cases verified; no knownHIGHdefecthidden.
- [ ] UIrequirements/browserresponsive/performance ~3s measurement hasenv+dataset+results.
- [ ] Externalintegrationsconfig/failure/backuprestoreHTTPS proof recorded; noproductionmutation.
- [ ] Finalchecklistaccurate; docslinks/routes/filesreportsmatchactualcode.
- [ ] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [ ] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [ ] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P32-T01 | Happy shopping | ActiveSKU+ownedaddress+validCODquote/branchstock | Cart→checkout→success→complete đúngtotals/snapshots/stock |
| P32-T02 | Validation | Emptycart/negativeqty/invalidnesteditem/date/price | Field/domainerror; no partialdata |
| P32-T03 | Owner tamper | CustomerA usesaddress/cart/order/return/review IDsB | DeniedwithnoPIIleak/datasideeffect |
| P32-T04 | Role/scope | Everyactor attemptsallowed/disallowed/crossbranchcommands | Permissionmatrix enforcedserver-side |
| P32-T05 | Last-stock race | 2checkout cùngSKU cuối ởbranch | Nooversell/negativeavailable, onlyvalidsuccessfulorder |
| P32-T06 | Last-voucher race | Concurrentclaims finalquota | Usage khôngvượtquota, failure toànrollback |
| P32-T07 | Latefailure/retry | Seconditeminvalid hoặcrepeatconfirm/receive/complete/cancel | Commit toàn hoặcrollback; no doublestock/holds/discount |
| P32-T08 | State race | Cancel vsSHIPPING/complete | Exactlylegaltransition;holds/stockconsistent |
| P32-T09 | Snapshot/history | Changeproductprice/deactivateafterorder | Savedhistory/totals unchanged; reportsapprovedmetrics |
| P32-T10 | Low-stock boundary | actual10 held4 min6; held3 | Firstlowstock, secondnotlowstock |
| P32-T11 | UI/browser/empty | 8screens+staffscreens,emptylists,mobile/browsermatrix | Approvedlayout/state/navigationresponsive; nofakeAPI |
| P32-T12 | Performance | D13normaldataset/scenarios | Measuredresults compared~3s, env/limitsrecorded |
| P32-T13 | Integration outage | Providerfailures andrestoretest | Domainsafe, safeerror/retry; restoreddata/configcorrect |

## 20. Definition of Done

- [ ] Allacceptedrequirementscó evidence vàno unresolvedblockinggates/HIGHbugs.
- [ ] Finalchecklistfulfilled vàreportsAudit/Masteraccurate khôngfalseDONE.
- [ ] RealPostgres/HTTP/security/atomic/UI/browser/performance/integrationproof đủ hoặcscopeexceptions explicitapproved.
- [ ] No newfeaturebucket/majorrefactor/productiondeploy inPhase32.
- [ ] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [ ] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [ ] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Có kết luận hoàn thành project dựa trên requirements và evidence thực tế, hoặc danh sách blocker chính xác nếu chưa đạt. Không coi32docs hoặc9testsH2 làprojectDONE.

## 22. Handoff To Next Phase

- Maintenanceagent córoutes/role/scopes/schema/config/testcommands/backuprestoreprocedure từactualreports.
- Knownissues/approveddeviations/performance/browserlimits vàscopeexceptions đượcghi khôngẩn.
- Dừng sauacceptedfinalverification; tasksnewscope cầnrequest vàroadmap riêng.

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
