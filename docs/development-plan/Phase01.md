# Phase 01 — Baseline, cấu hình và an toàn kỹ thuật

> Status: IMPLEMENTED_AND_TESTED_LOCAL. Complexity: MEDIUM. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Baseline Current State bên dưới là lịch sử trước khi triển khai. Xem Completion Report và [báo cáo tích hợp](../PHASE_01_04_REPORT.md) cho trạng thái mới.
> Re-verification 2026-10-08: các gap foundation được sửa/kiểm chứng; giữ trạng thái đã triển khai/kiểm thử local. Supabase mới đã inventory/validate read-only; DB cũ5432 vẫn chưa truy cập được. Xem phần tái kiểm chứng cuối Completion Report; không tuyên bố COMPLETED toàn bộ dữ liệu legacy.

## 1. Objective

Thiết lập baseline build/startup/PostgreSQL có thể lặp lại và các contract kỹ thuật dùng chung để tiếp tục code an toàn.

## 2. Why This Phase Exists

Java21 đã compile/test H2, nhưng default JDK25 lỗi TypeTag và PostgreSQL refused. Dữ liệu deployed chưa biết; mọi lỗi hiện trả400/string và late checked exceptions có nguy cơ commit một phần. Cần nền tảng kiểm chứng, không thực hiện các workflow domain ở Phase này.

## 3. Current State

- 190 main Java,37 entities/repos,17 REST, không frontend/security/migration tracked.
- pom target21; Lombok dependency1.18.46/processor1.18.36; JDK25.0.10 đã chạy9tests H2; JDK25.0.4 fail compile.
- application.properties PostgreSQL localhost5432/lunea, ddl-auto=update; docker-compose postgres16 +namedvolume; chưa kết nối được lúc audit.
- GlobalExceptionHandler catches Exception→400 rawmessage; @Transactional service lớp dùng checkedException nhiều nơi; domain owners sẽ sửa từng workflow.
- README và Architecture/phân công repo là tài liệu cũ/đề xuất, không source truth.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [x] Đọc toàn bộ Audit/Master/Phase01 và báo cáo/cập nhật mới; repo thực tế `CNPM`, commit `179bc49` khi bắt đầu phiên 2026-10-08; không có AGENTS.md áp dụng.
- [x] Java21.0.10 đã cài; có quyền đọc config và DB test riêng/Supabase. DB cũ localhost5432 vẫn connection refused.
- [x] Đối chiếu D01–D18: D01–D03 đã IMPLEMENTED_FOR_LOCAL_TEST; D04–D18 vẫn OPEN ở đúng owner. D14 chỉ ghi nhận phạm vi; D15/date/pricing/report không được tự chốt tại01.
- [x] Ghi nhận gate tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); các gate domain không chặn build/error/isolation/inventory độc lập.
- [x] Working tree được kiểm tra; giữ file prompt untracked và các thay đổi UI/browser của phiên đồng thời, không reset dữ liệu.

## 5. Scope

### IN SCOPE

- Build/runtime/test environment cho stack hiện tại; PostgreSQL startup +read-only schema/data inventory.
- Test profile/config isolation, migration strategy và schema baseline runbook; không áp migration domain.
- Semantic exception/error contract và nguyên tắc transaction regression mà các domain Phase dùng.
- Ghi evidence baseline, sửa hướng dẫn chạy/config cần thiết; giữ architecture.

### OUT OF SCOPE

- UI/securityimplementation và password/role business changes thuộc02–04.
- SKU/catalog/stock/checkout/return/report features thuộc owners sau.
- Upgrade Boot/Java target/DB/framework, blanket refactor hoặc reset database.
- Đổi tất cả throwsException/@Transactional toàn repo hoặc đánh dấu workflow stock đã sửa.

## 6. Requirements Covered

- SYS-11 input validation foundation; NFR03 error clarity; NFR22–24 maintainability/reuse/integrity foundations.
- Database §4.1.2/§4.2: hiểu schema trước migrate; SYS09–10 inventory codes/timestamps không tự thêm.
- TECHNICAL RECOMMENDATION: test isolation/versioned SQL migration contract vì ddl-auto update và PostgreSQL chưa verified.
- D14 scope inventory và D15 documentcode decision chỉ ghi nhận; adoption thuộc từng owner.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [pom.xml](../../pom.xml)
- [maven-wrapper.properties](../../.mvn/wrapper/maven-wrapper.properties)
- [application.properties](../../src/main/resources/application.properties)
- [docker-compose.yml](../../docker-compose.yml)
- [README.md](../../README.md)
- [GlobalExceptionHandler.java](../../src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java)
- [MapperConfig.java](../../src/main/java/com/thinh/cosmetic/config/MapperConfig.java)
- [CosmeticStoreChainManagementApplicationTests.java](../../src/test/java/com/thinh/cosmetic/CosmeticStoreChainManagementApplicationTests.java)
- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java)

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [application.properties](../../src/main/resources/application.properties)
- [pom.xml](../../pom.xml)
- [GlobalExceptionHandler.java](../../src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java)
- [CosmeticStoreChainManagementApplicationTests.java](../../src/test/java/com/thinh/cosmetic/CosmeticStoreChainManagementApplicationTests.java)
- [README.md](../../README.md)

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- Dự kiến test-only profile/config trong `src/test/resources/` theo Spring Boot conventions.
- Dự kiến domain error types/response contract trong package `exception` hoặc `domain/dto/response` hiện tại; không thêm framework lỗi.
- Dự kiến versionedSQL baseline/runbook dưới resources/docs nếu cần; không migration bảng nghiệp vụ ở01.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

NONE — không thêm/đổi bảng nghiệp vụ trong Phase01. Đọc schema PostgreSQL hiện hữu và lưu baseline. TECHNICAL RECOMMENDATION: chọn versioned SQL/runbook phù hợp repo; nếu tool cần metadata table/dependency, ghi quyết định và review rõ trước tạo. Domain constraints/migrations thuộc các Phase02–29. Không dùng ddl-auto=create/drop trên DB có dữ liệu.

## 11. Backend Tasks

- [x] Xác nhận JAVA_HOME=JDK21; build trước sửa pom. Chỉ align Lombok processor/dependency nếu có lỗi trên JDK hỗ trợ, không nâng stack theo sở thích.
- [x] Kiểm kê PostgreSQL test riêng và schema Supabase mới bằng read-only queries; phân biệt rõ với legacy. Inventory hỗ trợ schema chỉ định, counts mọi bảng, constraints/indexes/enum/status/FK orphan và identity preflight.
- [ ] Kiểm kê DB cũ localhost5432 khi có quyền truy cập và listener; connection refused ngày 2026-10-08, chưa migrate legacy.
- [x] Tách test database/profile khỏi dev database; giữ H2 chỉ smoke hỗ trợ và thêm khả năng chạy tests Postgres riêng.
- [x] Ghi versioned migration strategy và baseline schema; không auto tạo/drop bảng đang chứa dữ liệu.
- [x] Thiết kế tối thiểu semantic error contract (message/field errors/code phù hợp), map invalidinput400/notfound404/conflict409;401/403 do securityphases nối sau.
- [x] Introduce/reuse business exceptions giúp consumer rollback đúng; ghi rõ checkedException default không rollback; không sweep sửa domain services.
- [x] Xác định rollback test approach trên persistence context/DB thực, để từng domain owner thử late failure/concurrency trong Phase tương ứng.
- [x] Ghi clock/timezone conventions cho tests, timestamps và validation; dùng thời gian server/fixture có thể kiểm soát. Các boundary nghiệp vụ/date basis cần xác nhận trong D06/D17, không suy ra timezone từ máy người dùng.
- [x] Kiểm tra config dev credentials/port/driver; externalization theo môi trường, không ghi secret thật vào report/log.
- [x] Cập nhật README về implementation hiện tại, commands và giới hạn PostgreSQL/H2; không quảng cáo chưa có UI/security.

## 12. Frontend Tasks

- [x] Contract hiện có: `timestamp,status,code,message,path,fieldErrors`; `app.js` dùng message/fieldErrors và xử lý status. Không tạo screen ở Phase01.
- [x] Đối chiếu trạng thái mới: đã có static HTML/CSS/JS cùng origin, fetch, session/CSRF từ02–04. Không có Bootstrap/Thymeleaf/SiteMesh; mô tả thiếu static/fetch ở baseline lịch sử không còn đúng.

## 13. Validation & Business Rules

- Java21 là target/baseline; PostgreSQL là primary, không thay bằng H2 vì smoke pass.
- Failure môi trường không được coi mọi module BROKEN; deployed schema UNKNOWN cho tới kiểm chứng.
- Không reset data, không sửa PK chỉ vì design composite/source surrogate equivalent.
- Lỗi500 phải không lộ thông tin nội bộ;400/404/409 có semantics; quyết định response được bàn giao cho02+.
- Transaction nguyên tắc commit toàn thao tác hoặc rollback; actual adoption từng workflow ghi ở owner.

## 14. Security Requirements

Chưa có authentication/authorization ở baseline. Phase01 không mở thêm admin route/DB console/public diagnostic endpoints. Secrets không xuất trong log/Markdown; config test chỉ trỏ DB test.401/403 và principal/scope được triển khai02/04, không tuyên bố đã bảo vệ routes.

## 15. Error Handling

- JDK mismatch: thông báo đúng supported runtime, không sửa entity để chữa compiler.
- PostgreSQL refused/authfailure: ghi environment/config và giữ dữ liệu, không recreate volume.
- Schema/duplicate/null audit: report vấn đề cho migration owner, không sửa tự phát.
- Validation/NotFound/Conflict/Unexpected: contract ổn định, không trả mọi lỗi như400.

## 16. Integration Points

- 02–04 dùng error/profile/migration baseline;08/14/16/19/23 dùng transaction test conventions.
- 31 dùng DBinventory/config và backup needs;32 dùng command/evidence baseline.
- Các test hiện có Inventory2/Order1/Voucher5/context1 được giữ.

## 17. Implementation Order

1. Đọc source/config/test và gitstatus; chốt supportedJava21.
2. Build và chạy tests trong DB thử; tái kiểm chứng PostgreSQLstartup/schema.
3. Thiết lập test isolation và schema/migration runbook, không applydomainchanges.
4. Chuẩn hóa lỗi/exception contract cần thiết và test mapping; mô tả rollback foundations.
5. Cập nhật README/baseline report và bàn giao exact commands cho02.

## 18. Verification

- [x] Run Maven tests với JDK21 và test DB riêng; giữ9existingcases; ghi profile/driver/DB type rõ.
- [x] Application context/startup PostgreSQL test có evidence; Supabase schema mới kiểm kê/validate riêng. DB cũ chưa truy cập được, không suy dữ liệu legacy từ fixture.
- [x] Test invalid request/notfound/conflict/unknownexception mapping không leak sensitive data.
- [x] Chứng minh test config không đụng dev data; không assertion H2=PostgreSQLconcurrency.
- [x] Diff do phiên01 giới hạn guard/config-runner/inventory/tests/docs; không đổi entity/domain/route/UI. UI/browser files được thay đổi đồng thời ngoài phiên01 được giữ riêng trong handoff.
- [x] Ghi command/environment/result và giới hạn trong Verification Result; legacy UNKNOWN không chuyển thành PASS.
- [x] Kiểm tra diff của phiên01, không refactor lớn hoặc làm nhiệm vụ Phase sau.
- [x] Giữ và chạy regression capability đã có; không dựng lại02–04.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P01-T01 | Supported build | JDK21 +Maven wrapper | Compile/tests thành công, không TypeTag error |
| P01-T02 | Wrong runtime | JDK25 so vớitarget21 | Supportedversion được báo rõ; không đổi target tự phát |
| P01-T03 | Database unavailable | DBtest port không listener | Startup error ghi đúng cause/config, no data reset |
| P01-T04 | Postgres startup | Postgres16test +existingmapping | Contextstartup pass, schema thực được kiểm kê |
| P01-T05 | Validation | Request vi phạm annotations | 400 với field/global error không lộ internals |
| P01-T06 | Not found | Nonexistent entity qua service contract | 404 theo contract chuẩn |
| P01-T07 | Conflict | Domain/state/unique conflict fixture | 409 theo contract, không partialcommit |
| P01-T08 | Empty dataset | DBtest không dữ liệu nghiệp vụ | Startup/list(empty) không crash, không auto seed tài khoảnprod |
| P01-T09 | Test isolation | TestDBname khác dev | Test ghi/drop chỉ ảnh hưởng testDB |
| P01-T10 | Unexpected exception | Synthetic internalfailure | 500 an toàn, server logs phù hợp, response không stacktrace |

## 20. Definition of Done

- [x] JDK21 build/tests pass và PostgreSQLtest startup có evidence.
- [x] 9test hiện có được giữ; H2 và Postgres kết quả ghi tách biệt.
- [x] Error contract/test isolation/migration strategy được ghi và dùng được.
- [x] README/config phù hợp source hiện tại; phiên01 không áp migration hoặc tạo domain/UI/security features.
- [x] Checkbox phản ánh evidence và giới hạn; legacy inventory chưa truy cập được vẫn mở, không chặn foundation độc lập.
- [x] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit/Master cập nhật contract/evidence mới, dependency giữ nguyên.
- [x] Regression capability hiện hữu; giữ Java21, stack, kiến trúc và conventions.

## 21. Expected Result After This Phase

Có baseline kiểm chứng được trên stack hiện tại, DBtest an toàn và contract kỹ thuật dùng chung. Các bug domain trong Audit chưa tự coi đã sửa.

## 22. Handoff To Next Phase

- 02 có thể dùng supportedJava/DBtest/error mapping và migration conventions.
- 04/08+ phải lấy schema thực trước constraints và dùng transaction regression approach.
- Report phải ghi DB/environment/classpath/commands, config paths, schema inventory và các blocker còn lại.

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

IMPLEMENTED_AND_TESTED_LOCAL (2026-10-07). Database dev/deployed ở localhost:5432 chưa kết nối để kiểm kê; các kết quả PostgreSQL bên dưới thuộc DB test riêng.

## Work Completed

Baseline Java21 và Maven wrapper; test isolation; guard từ chối demo dùng PostgreSQL và PostgreSQL test chạy create trên DB không có hậu tố `_test`; JSON error contract với mã 400/401/403/404/409/500; Clock UTC và unchecked BusinessException để rollback. Hibernate mặc định `validate`, lỗi tạo schema test dừng ngay. Giữ nguyên Java target và kiến trúc REST/service/repository.

## Files Created

`src/test/resources/application.properties`, `application-test-postgres.properties`; `src/main/resources/application-demo.properties`, `META-INF/spring.factories`; `config/DatabaseSafetyEnvironmentPostProcessor.java`, `TimeConfig.java`; các exception/ApiError và foundation tests; `docs/database/01_inventory.sql`, `V001__identity_security.sql`, runbook và PostgreSQL16 test schema. Xem [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

## Files Modified

`pom.xml`, `application.properties`, `GlobalExceptionHandler.java`, `README.md`, `.gitignore`; Brand/Employee ordinal status giữ mapping SMALLINT để H2 PostgreSQL mode tạo schema đầy đủ.

## Database Changes

Không áp SQL lên DB dev/deployed. V001 là migration thủ công cho identity/recovery/audit của Phase02–04 sau kiểm kê. Đã thử trên DB PostgreSQL legacy giả riêng; cùng script chạy lại an toàn và từ chối duplicate đã normalize trước mutation. H2 demo/test dùng schema tạm; DB primary giữ `ddl-auto=validate`.

## APIs / Routes Added

Không thêm route diagnostic Phase01. Các auth/staff routes được ghi tại báo cáo Phase02–04. Lỗi API có `timestamp,status,code,message,path,fieldErrors`.

## UI Added

UI cơ bản nằm trong Phase02–04 theo yêu cầu test của người dùng; Phase01 cung cấp contract lỗi cho UI.

## Decisions Made

Java21 là runtime hỗ trợ, PostgreSQL16 là primary. H2 chỉ cho demo/test. Chọn SQL migration có version và runbook thủ công để không tự sửa dữ liệu cũ. UTC dùng cho expiry bảo mật; ngày nghiệp vụ Phase sau chưa thay chính sách.

## Deviations From Plan

Đã triển khai Phase01–04 cùng lần làm việc thay vì dừng giữa các phase. Test PostgreSQL chạy bằng bản portable16.15 loopback15432/lunea_test vì DB dev5432 không có listener. Dữ liệu legacy/deployed chưa được suy luận từ fixture.

## Known Issues

DB cũ localhost5432 chưa kiểm kê/áp V001; credential legacy có thể là plaintext và chỉ được thay bằng recovery xác minh. Schema Supabase mới đã có mapping hiện tại, không chứa dữ liệu legacy migrate. Báo cáo tích hợp đã ghi SMTP connection/auth pass, delivery đến hộp thư vẫn chưa kiểm chứng; restore rehearsal và timeout thực dài thuộc owners sau. Không tuyên bố stock/checkout/ownership đã sửa.

## Remaining Tasks

Đối với DB cũ: cần listener/quyền kết nối để inventory read-only trước reconciliation/migration có review; áp V001 chỉ bởi owner02–04 sau preflight/bảo toàn legacy. Supabase là schema mới đã bootstrap, không chạy V001/bootstrap lần nữa và không chạy create/drop tests trên database `postgres`. Backup/restore/hosting/HTTPS thuộc31; không nhận vào phiên01.

## Verification Result

JDK21.0.10: 52/52 tests PASS trên H2 và PostgreSQL16.15; 9 test cũ giữ lại. Package PASS. `docs/database/01_inventory.sql` trên DB test: 38 bảng; account18/customer1/employee17; duplicate normalized/missing identity/orphan/legacy hashes0 chỉ cho fixture. Migration test ba điều kiện PASS (giữ dữ liệu, repeat, rollback duplicate). Jar khởi động PostgreSQL fixture đã migrate bằng `ddl-auto=validate`; GET `/api/auth/csrf` HTTP200. Lệnh/log/giới hạn: [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

## Notes For Next Phase

Phase02–04 đã dùng error/profile/migration contract này. Phase05+ tiếp tục guard quyền và migration đã kiểm kê, test PostgreSQL riêng; không dùng H2 success làm bằng chứng cho dữ liệu deployed.

## Tái kiểm chứng phiên01 ngày 2026-10-08

**Status phần hoàn thiện gap: COMPLETED.** Giữ Phase01 **IMPLEMENTED_AND_TESTED_LOCAL** với giới hạn legacy inventory còn mở. Không khởi chạy công việc của Phase khác.

### Khác biệt thực tế trước sửa

- Repo tại `LUNEA/CNPM`, `pom.xml` Java21/Boot4.1.1, commit bắt đầu `179bc49`; không có AGENTS.md áp dụng. Tree ban đầu chỉ có `SESSION_PROMPTS.md` untracked; các UI/browser/Phase02 report changes xuất hiện đồng thời được giữ nguyên và không nhận ownership ở01.
- Code đã có static HTML/CSS/JS, fetch cùng origin, session/CSRF, semantic JSON errors, unchecked exceptions, Clock UTC, H2 test/demo riêng, PostgreSQL test profile và mặc định `validate`. Không dựng lại từ baseline REST-only/190 Java/9 tests.
- H2 baseline hiện tại **53/53 PASS**. Báo cáo Supabase mới đúng là bootstrap schema mới, không phải đã migrate dữ liệu localhost5432.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-demo.ps1 -Port 18080` thực sự fail `NativeCommandError` khi Java21 ghi version lên stderr dưới `$ErrorActionPreference=Stop`.
- Guard cũ suy tên DB từ dấu `/` cuối toàn URL; URL dev có `?ApplicationName=/lunea_test` vượt guard. Các trường hợp query đổi database, `drop`, native Hibernate/JPA DDL và Hikari URL override cũng thiếu bảo vệ. Ba regression mới đều FAIL trên source cũ trong build riêng.
- Inventory cũ chỉ đọc metadata `public` và đếm bảy bảng identity, không kiểm kê schema Supabase `lunea` nhất quán.

### Work / files / database / routes / UI

Sửa `DatabaseSafetyEnvironmentPostProcessor.java`: xác định URL Hikari hiệu lực; kiểm tra create/drop settings; chỉ nhận PostgreSQL test URL có host/database rõ, decode tên DB trước `_test`, tách query khỏi path, từ chối database/service overrides và URL mơ hồ. Thêm ba case vào `DatabaseSafetyTest.java` (tổng năm); fixture tự đặt profile, không kế thừa `test-postgres` từ JVM CLI. Không đổi dependency, Java target hoặc transaction policy toàn repo.

Sửa `scripts/run-demo.ps1` để đọc version stderr đúng trên Windows PowerShell, vẫn kiểm tra Java21, thêm validation port. Sửa `docs/database/01_inventory.sql` và runbooks/README: schema parameter mặc định `public`, chỉ định `lunea` cho Supabase, explicit read-only transaction/search_path, counts mọi bảng, columns/constraints/indexes/native enums/status values, FK orphan counts và identity preflight. Schema thiếu trả lỗi, không fallback. `_test` vẫn cần operator xác nhận là DB disposable; suffix không chứng minh ownership.

Files documentation: `README.md`, `docs/database/{README.md,SUPABASE.md,01_inventory.sql}`, Phase01, Audit/Master và báo cáo tích hợp. Không thêm bảng, áp V001, sửa legacy, reset volume hoặc bootstrap Supabase. Không thêm/đổi API route, quyền, identity policy, entity hoặc screen. `app.js`/`index.html` tiếp tục consume `ApiError.message/fieldErrors`; source browser → fetch JSON → AuthRestController → AccountServiceImpl → repositories/entities được đối chiếu. UI changes của phiên đồng thời thuộc02.

### Decisions / deviations / known issues

D14 vẫn **OPEN**, chỉ ghi nhận scope; domain decisions giữ đúng owner và dependency DAG không đổi. Tái sử dụng manual SQL/runbook hiện có. **TECHNICAL RECOMMENDATION:** khi IDE/OneDrive khóa `target`, build snapshot/worktree riêng và so hash source thay vì dừng IDE hoặc reset cache/dữ liệu của người dùng. Phiên này đã làm vậy dưới `.local/phase01/verify`; Java sources, pom và runner khớp hash repo (0 mismatch). Log/evidence/tools đều ignored, không chứa credential/token thật được in ra handoff.

Một lần Maven dùng cache IDE có unresolved compiled classes; `clean` tại repo bị khóa thư mục `target/classes/.../config`. Build riêng giải quyết vấn đề môi trường. Một lần runner verification PowerShell Stop coi Mockito stderr warning là lỗi; đã chạy lại bằng exit code Maven thật. PostgreSQL rerun đầu 55/56 pass vì fixture unit mới kế thừa profile CLI; đã cô lập fixture và rerun toàn bộ. Các lỗi này không được tính PASS.

Browser tool không khởi tạo được (`kernel exited`, sandbox `setup refresh had errors`), nên phiên01 không có browser DOM/visual evidence mới. HTTP dưới đây là request thật, không được gọi là browser pass; UI evidence hiện có nằm ở report owner02. Không nhận hosting/HTTPS/restore rehearsal/SMTP delivery/long real-time session timeout vào01.

### Verification / exact commands

Java21.0.10, Maven wrapper3.9.16, Boot4.1.1 giữ nguyên. Source/pom/wrapper/scripts được copy vào build riêng sau khi repo `target` bị khóa:

```powershell
$verify = '.local/phase01/verify'
New-Item -ItemType Directory -Force -Path $verify
foreach ($item in @('pom.xml','mvnw.cmd','.mvn','src','scripts')) {
    Copy-Item -LiteralPath $item -Destination $verify -Recurse -Force
}
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-21.0.10'
Push-Location $verify
.\mvnw.cmd -o package '-Dlogging.level.org.hibernate.SQL=OFF'
.\mvnw.cmd -o test '-Dlogging.level.org.hibernate.SQL=OFF'
$env:TEST_DB_URL = 'jdbc:postgresql://127.0.0.1:15433/phase01_test'
$env:TEST_DB_USERNAME = 'phase01_test'
$env:TEST_DB_PASSWORD = '' # trust authentication only in the fresh loopback test cluster
.\mvnw.cmd -o test '-Dspring.profiles.active=test-postgres' '-Dspring.jpa.hibernate.ddl-auto=create' '-Dlogging.level.org.hibernate.SQL=OFF'
Pop-Location
```

| Check | Environment / result | Evidence / limits |
|---|---|---|
| Baseline regression | Java21, H2 **53/53 PASS** | `.local/phase01/phase01-baseline-h2.log`; includes nine original cases |
| New guard regression before fix | **3 failures**, five cases | `guard-before-isolated.log`; no DB connection/mutation |
| Final suite / package | H2 **56/56 PASS**, package BUILD SUCCESS | `h2-final.log`, `h2-package-final.log`; package JAR copied to normal `target` for runner smoke |
| PostgreSQL suite / rollback | PostgreSQL **16.15**, fresh loopback15433 **phase01_test**, **56/56 PASS** | `postgres-final.log`; auth late-account insert, recovery credential failure and mandatory audit rollback retained; no stock/checkout completion claim |
| Read-only test inventory | PostgreSQL16, `public`, **38 tables**, PASS | `postgres-inventory.log`; missing schema fails exit3 (`inventory-missing-schema.log`) |
| Real startup guard | Dev URL with query `/lunea_test` +create-drop refused, exit1 **before Hikari** | `guard-startup.log`; jar uses registered EnvironmentPostProcessor, not only a unit invocation |
| Unavailable DB startup | Test URL loopback15434 +validate: exit1, connection refused | `unavailable-startup.log`; no data reset |
| PostgreSQL16 validate / HTTP | JAR +read-only connections +default `validate`, port18082; **4/4 PASS** | `postgres-validate.log`, `postgres-http.log`: GET `/`, `/api/auth/csrf`→200, `/api/auth/me`→401, missing permitted asset→404 |
| Supabase inventory | PostgreSQL **17.11**, database `postgres`, schema **lunea**, **38 tables**, **55 FK checks=0 orphans** | `supabase-inventory.log`; accounts1/employees1/customers0/roles10/permissions25/stores1, business tables empty; identity duplicate/null/invalid/oversized/legacy-password counts0 only for this new schema |
| Supabase validate / HTTP | Existing runner, Hikari read-only, loopback18081; **4/4 PASS** | `supabase-startup.log`, `supabase-http.log`; same GET contract as test16; no register/login mutation on Supabase in this session |
| Demo runner / HTTP | Fixed PowerShell runner, isolated H2, loopback18080; **8/8 PASS** | `demo-startup.log`, `demo-http.log`: 200 shell/CSRF; 401 anonymous; 403 mutation without CSRF; 400 field validation +malformed JSON; 404 missing asset; 409 existing demo identity. No new account inserted |
| Error500 confidentiality | Existing standalone MockMvc suite PASS in both runs | Synthetic exception, no secret in response; no public diagnostic route added |
| Legacy local DB | localhost5432 connection refused, psql exit2 | `legacy-connection.log`; schema/data/migration remain UNVERIFIED |

Inventory commands actually used `psql -X -v ON_ERROR_STOP=1 -f docs/database/01_inventory.sql` on test16 and `-v inventory_schema=lunea` on Supabase. Supabase credentials loaded from ignored `.local/production.env` into process env, `PGSSLMODE=require`, `PGOPTIONS='-c default_transaction_read_only=on -c statement_timeout=30000'`; no secret in command/report. Existing runner smoke: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-supabase-local.ps1 -Port 18081` with `SPRING_DATASOURCE_HIKARI_READ_ONLY=true`. Demo smoke used the identical copied `scripts/run-demo.ps1 -Port 18080`; `$env:MAVEN_ARGS='-o'` for cached local dependencies. Test apps/cluster are stopped after verification; data/logs remain private in ignored `.local/phase01`.

Cleanup confirmed no listeners remain on15433/18080/18081/18082. Local evidence logs were sanitized to remove token-bearing response lines and any configured secret values; subsequent scan found0 configured secret values. `git diff --check` PASS. Changes made concurrently by owner02 remain untouched.

### Remaining / notes for next phase

Foundation gap work is finished. Legacy inventory still requires a reachable PostgreSQL localhost5432 and authorized credentials; no evidence from Supabase/test fixtures substitutes for it. Any future legacy migration needs read-only preflight, reconciliation and preservation by its owner. Supabase already has the new baseline; do not rerun bootstrap/V001 or destructive tests there.

02–04 capabilities already exist and are regression-tested, so the next requested owner phase should reuse error/Clock/session/CSRF/principal/permission/scope contracts after reading its report/current source. D14 does not block foundation. Additional owner decisions and migration gates apply only to dependent tasks; no new decision required to start independent work. This session stops at01.
