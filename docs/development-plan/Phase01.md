# Phase 01 — Baseline, cấu hình và an toàn kỹ thuật

> Status: IMPLEMENTED_AND_TESTED_LOCAL. Complexity: MEDIUM. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Baseline Current State bên dưới là lịch sử trước khi triển khai. Xem Completion Report và [báo cáo tích hợp](../PHASE_01_04_REPORT.md) cho trạng thái mới.

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

- [ ] Đọc Audit §1–4,§8,§11,§17; xác nhận commit và kiểm kê source hiện tại.
- [ ] Có Java21 đã cài và quyền đọc config/DB thử nghiệm; cần quyền riêng trước thao tác môi trường thật ngoài scope.
- [ ] Ghi các quyết định D01–D18 đang OPEN hoặc cập nhật quyết định đã có; không chọn UI/return/pricing/report/shipping thay người dùng.
- [ ] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [ ] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

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
- [ ] Kết nối PostgreSQL16 hiện hữu; kiểm kê schema/row counts/constraints/enum/duplicate/null/orphan bằng read-only queries trước mọi migration.
- [x] Tách test database/profile khỏi dev database; giữ H2 chỉ smoke hỗ trợ và thêm khả năng chạy tests Postgres riêng.
- [x] Ghi versioned migration strategy và baseline schema; không auto tạo/drop bảng đang chứa dữ liệu.
- [x] Thiết kế tối thiểu semantic error contract (message/field errors/code phù hợp), map invalidinput400/notfound404/conflict409;401/403 do securityphases nối sau.
- [x] Introduce/reuse business exceptions giúp consumer rollback đúng; ghi rõ checkedException default không rollback; không sweep sửa domain services.
- [x] Xác định rollback test approach trên persistence context/DB thực, để từng domain owner thử late failure/concurrency trong Phase tương ứng.
- [x] Ghi clock/timezone conventions cho tests, timestamps và validation; dùng thời gian server/fixture có thể kiểm soát. Các boundary nghiệp vụ/date basis cần xác nhận trong D06/D17, không suy ra timezone từ máy người dùng.
- [x] Kiểm tra config dev credentials/port/driver; externalization theo môi trường, không ghi secret thật vào report/log.
- [x] Cập nhật README về implementation hiện tại, commands và giới hạn PostgreSQL/H2; không quảng cáo chưa có UI/security.

## 12. Frontend Tasks

- [ ] Ghi error-response contract cho các Phase UI để hiển thị lỗi field/global; không tạo template hoặc screen ở Phase01.
- [ ] Ghi rõ hiện không có Bootstrap/Thymeleaf/SiteMesh/static/fetch; D01 phải được giải quyết trước UI.

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
- [ ] Application context/startup PostgreSQL không exception; migrations/baseline strategy hiểu schema thực.
- [x] Test invalid request/notfound/conflict/unknownexception mapping không leak sensitive data.
- [x] Chứng minh test config không đụng dev data; không assertion H2=PostgreSQLconcurrency.
- [ ] Git diff giới hạn foundation/config/errors/tests/docs theo scope, không domainfeaturework.
- [ ] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [ ] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [ ] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P01-T01 | Supported build | JDK25 +Maven wrapper | Compile/tests thành công, không TypeTag error |
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
- [ ] README/config phù hợp source; no domain migrations/UI/security features làm trước.
- [ ] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [ ] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [ ] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

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

DB dev/deployed5432 chưa kiểm kê/áp V001; credential legacy có thể là plaintext và chỉ được thay bằng recovery xác minh. SMTP thật, restore rehearsal và timeout phiên trôi thời gian thực chưa kiểm chứng. Không tuyên bố stock/checkout/ownership của Phase sau đã sửa.

## Remaining Tasks

Trước khi chạy trên DB thật: chạy read-only inventory, xử lý null/duplicate/orphan, backup/restore theo quy trình, áp V001 và kiểm tra `validate` trên schema thật. Các mục này chưa được đánh dấu hoàn tất trong checklist.

## Verification Result

JDK21.0.10: 52/52 tests PASS trên H2 và PostgreSQL16.15; 9 test cũ giữ lại. Package PASS. `docs/database/01_inventory.sql` trên DB test: 38 bảng; account18/customer1/employee17; duplicate normalized/missing identity/orphan/legacy hashes0 chỉ cho fixture. Migration test ba điều kiện PASS (giữ dữ liệu, repeat, rollback duplicate). Jar khởi động PostgreSQL fixture đã migrate bằng `ddl-auto=validate`; GET `/api/auth/csrf` HTTP200. Lệnh/log/giới hạn: [báo cáo tích hợp](../PHASE_01_04_REPORT.md).

## Notes For Next Phase

Phase02–04 đã dùng error/profile/migration contract này. Phase05+ tiếp tục guard quyền và migration đã kiểm kê, test PostgreSQL riêng; không dùng H2 success làm bằng chứng cho dữ liệu deployed.
