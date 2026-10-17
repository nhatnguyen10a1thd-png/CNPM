# Báo cáo phase 01–04 — 2026-10-07

Status: **IMPLEMENTED_AND_TESTED_SUPABASE_LOCAL_APP**. Code và giao diện cơ bản phục vụ test đã hoàn thành trên Java 21, H2 và PostgreSQL thật. Ngày 08/10/2026, schema `lunea` mới đã được tạo trên Supabase và app chạy local đã kết nối thành công. Đây chưa phải triển khai app công khai hay kiểm chứng dữ liệu database cũ localhost:5432; database đó chưa được kiểm kê/migrate. Không reset DB/volume hiện hữu.

## Kết quả

| Phase | Phần hoàn thành | Evidence |
|---|---|---|
| 01 | Cấu hình tách demo/test/dev; Java21; error JSON 400/401/403/404/409/500; unchecked business exceptions; Clock UTC; schema/migration runbook; guard chống test vào DB dev | 9 test cũ giữ lại; 5 test foundation; PostgreSQL test startup/schema/count inventory; V001 fixtures/rollback |
| 02 | Đăng ký atomic; BCrypt; email/phone; khách hàng và nhân viên; session/CSRF; remember; logout; status/credential-version; deny-default; current principal; rate limits | 14 auth integration tests gồm duplicate race và late failure; browser đăng ký/login/logout |
| 03 | Recovery qua link dùng một lần; SHA256 token, TTL, khóa và rate limit; reset atomic; revoke phiên; SMTP adapter và mailbox demo | 12 recovery tests gồm expiry boundary/replay/parallel/provider failure/late rollback; browser forgot → mailbox → reset → login |
| 04 | CRUD/search/page/deactivate nhân viên; roles/permissions/primary stores; permission AND scope; chống tự nâng quyền/khóa admin cuối; audit mandatory; composite repository ID; service permission guards cho global operations | 21 staff tests trên H2/PG gồm REST/direct service RBAC/scope, grant/revoke, invalid IDs, deactivation, audit DB rollback và duplicate-create race; PG HTTP18, Edge28 |

## Quyết định cho phạm vi test

- D01: HTML/CSS/JavaScript static cùng Spring Boot, fetch JSON cùng origin, Spring Security session + CSRF. Giao diện tối giản theo yêu cầu người dùng; không cần Node/npm. Đây là lựa chọn triển khai của agent trong scope được giao, không ghi nhận người dùng đã duyệt từng chi tiết công nghệ/layout riêng.
- D02: email trim/lowercase; điện thoại optional, normalize dấu cách/dấu phân cách, 9–15 chữ số với optional `+`; min8 có chữ ASCII và số, tối đa72 UTF8 bytes; confirm và terms server-side. Nhân viên dùng `email` để đăng nhập; `internalEmail` mặc định email nhưng có thể khác. Không có customer bypass.
- Session thường idle30 phút; remember cookie và idle7 ngày. Session/CSRF đổi khi login; logout invalidate và expire cookie. HttpOnly/SameSite=Lax; Secure theo cấu hình hoặc HTTPS. Tests chứng minh cấu hình timeout và revoke/logout; không giả vờ đã chờ30 phút/7 ngày thật trong container.
- D03: reset link qua SMTP; secret ngẫu nhiên32 bytes, DB chỉ SHA256, TTL15 phút configurable; một token/account, một lần dùng. Giới hạn request3/identifier/15 phút và10/IP/15 phút, reset10/IP/15 phút. Token/credential/outbox DTO redacted trong toString. Generic202 giữ anonymity kể cả delivery lỗi; token giao dịch rollback nếu gửi thất bại. Thay phiên bằng credentialsVersion.
- Mailbox demo profile riêng, chỉ loopback và trong bộ nhớ; link hết hạn/đã dùng bị loại bỏ. Đây là tiện ích test được yêu cầu, không có diagnostic token endpoint ở cấu hình thường. SMTP thật đã kết nối và xác thực thành công với cấu hình hiện tại; chưa gửi thư đến hộp thư nhận để xác minh delivery.
- Role matrix, phạm vi và audit contract: [STAFF_ACCESS.md](STAFF_ACCESS.md). ADMIN có explicit permissions, role bất biến; ROLE_MANAGE dành riêng ADMIN. QLNV quản trị nhân viên thông thường. Branch-bound queries giới hạn cửa hàng được cấp; transfer cần cả hai cửa hàng. Audit lấy principal và remote address, không tin forwarded-IP tùy ý.
- Manual versioned SQL V001, không thêm framework migration, không auto thay dữ liệu cũ. UTC cho security timestamps; chính sách ngày nghiệp vụ phase sau giữ nguyên.

## Routes và giao diện

| Method/route | Input/result | Access |
|---|---|---|
| GET `/` | Giao diện đăng nhập/đăng ký/recovery, tài khoản, nhân viên và quyền | Public shell; API bảo vệ riêng |
| GET `/api/auth/csrf` | token/headerName/parameterName | Public session initialization |
| POST `/api/auth/register` | fullName,email,phone?,password,confirmPassword,termsAccepted →201 CustomerResponse | Public + CSRF |
| POST `/api/auth/login` | identifier,password,rememberMe? →AuthResponse | Public + CSRF |
| GET `/api/auth/me` | accountId/type/status/customerId/employeeId/fullName/roles/permissions/storeIds | Authenticated |
| POST `/api/auth/logout` | →204 | Authenticated + CSRF |
| POST `/api/auth/recovery/request` | identifier →202 generic message | Public + CSRF/rate limit |
| POST `/api/auth/recovery/reset` | token,password,confirmPassword →200 or400 | Public + CSRF/verified token |
| GET `/api/demo/recovery/messages` | email/resetUrl/expiresAt | Demo + loopback only |
| GET/POST `/api/employees`, GET/PUT/DELETE `/api/employees/{id}` | Typed page/search; full-form create/update; deactivate | Employee read/manage, server validation |
| GET `/api/roles`, GET `/api/permissions`, PUT `/api/roles/{id}/permissions` | Lookup; permissionIds grant replacement | Employee manager lookup; ADMIN grants |

Enabled staff catalog/store/supplier/voucher/inventory/purchase/transfer routes reuse permissions; store-bound services enforce scope. Cart/wishlist/customer/order/return/review APIs remain denied until their owner phases complete ownership/workflow checks. Removing default actor1 is identity integration, not a claim those domains are finished.

## Schema và migration

Accounts: nonnull/unique username/email, optional unique phone, BCrypt storage size, status STRING và credentials_version. Employees: nonnull unique account/internal email và full-name constraints; ordinal status được giữ. Audit: giữ performed_by/content, thêm nullable employee FK/object/IP và details TEXT. Thêm password_reset_tokens với account FK, unique token_hash, timestamps và indexes.

[Runbook](database/README.md), [read-only inventory](database/01_inventory.sql), [V001](database/V001__identity_security.sql), [PostgreSQL16 test schema](database/POSTGRES16_TEST_SCHEMA.sql). V001 fixtures trên DB riêng chứng minh normalize identity, giữ credential legacy và audit, repeat không mất dữ liệu, reject duplicate trước mutation/rollback. Credential plaintext cũ không được tự hash/đổi mật khẩu chung và không authenticate; cần verified recovery sau migration. Không kết luận DB deployed sạch plaintext từ fixtures.

H2 PostgreSQL mode cần NON_KEYWORDS=VALUE cho mapping có sẵn. Brand/Employee ordinal status ghi rõ SMALLINT, giữ biểu diễn PostgreSQL. Bật halt_on_error để DDL lỗi không bị che bởi contextLoads. Không đổi Brand hierarchy hoặc nghiệp vụ catalog.

## Verification

Baseline commit `1e27c8e`; tree ban đầu chỉ có docs/development-plan untracked. Java21.0.10, Maven wrapper3.9.16, Spring Boot4.1.1 giữ nguyên. Processor Lombok không cần đổi trên JDK21. Không đổi stack target để chữa JDK25.

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-21.0.10'
.\mvnw.cmd -o test '-Dlogging.level.org.hibernate.SQL=OFF'
.\mvnw.cmd -o test '-Dspring.profiles.active=test-postgres' '-Dspring.jpa.hibernate.ddl-auto=create' '-Dlogging.level.org.springframework=ERROR' '-Dlogging.level.org.hibernate=ERROR' '-Dlogging.level.org.hibernate.SQL=OFF'
.\mvnw.cmd -o package -DskipTests
```

- H2: **52 tests,0 failures,0 errors,0 skipped**, BUILD SUCCESS. Log `target/phase1-4-h2.log`.
- PostgreSQL **16.15**, loopback15432, database **lunea_test**: **52 tests,0 failures,0 errors,0 skipped**, BUILD SUCCESS. Log `target/phase1-4-postgres.log`; real PostgreSQL contained test accounts/employees/grants after suite, confirming datasource use. Explicit create retained test schema for inventory/dump; normal documented test profile is create-drop and refuses non `_test` names.
- Package: BUILD SUCCESS, `target/cosmetic-0.0.1-SNAPSHOT.jar`.
- Inventory test snapshot: 38 tables; account18/customer1/employee17/role12/permission25/employee-role16/employee-store5; duplicate normalized identities/missing identity/orphan links/legacy hashes0 for that fixture. These counts are not production data.
- Migration:3 checks PASS on fresh separate PostgreSQL fixture database: preserves old credentials/history with normalized identifiers/status/version; repeat safe; normalized duplicate rejected atomically. Log `target/migration-verification.log`.
- Browser Edge headless/CDP: **15 checkpoints PASS**, real DOM clicks/forms, register confirmation validation; customer nav/login/logout; demo-mail reset and old/new password; admin create/edit/search/deactivate/grants; warehouse branch1 allowed/branch2 denied. Mobile390px scrollWidth390. Screenshot visual review passed during development; temporary browser artifacts were removed in the cleanup.
- Browser APIs could not initialize in this sandbox, so approved shell launched installed Edge headless in a fresh workspace profile; no existing user browser profile was read. The browser process was closed after smoke.
- Sau bộ kiểm thử trên, thay đổi giới hạn duy nhất một ADMIN được xác minh bằng **53 tests, 0 failures, 0 errors, 0 skipped**, BUILD SUCCESS (`.local/single-admin-build2.log`).
- Supabase PostgreSQL 17.11: 38 bảng trong schema riêng `lunea`, tất cả 38 bảng bật RLS; `anon`, `authenticated`, `service_role` không có quyền schema USAGE/SELECT bảng accounts. Hibernate `validate` thành công; đăng ký/đăng nhập/phiên khách hàng và đăng nhập ADMIN thực đều trả kết quả mong đợi. Tài khoản smoke tạm đã xóa; bootstrap một lần tạo một ADMIN, 10 vai trò, 25 quyền, một chi nhánh ảo. Xem [runbook Supabase](database/SUPABASE.md).
- SMTP: kết nối và xác thực qua cấu hình thực thành công bằng `JavaMailSender.testConnection()`; phép thử này không gửi email. Giao diện local với Supabase chạy qua `scripts/run-supabase-local.ps1` trên `http://localhost:8081`.

## Giới hạn và bàn giao

Phase04 follow-up 2026-10-08 (dừng tại04): capability staff/principal/RBAC/scope/audit đã tồn tại, chỉ sửa một gap tái hiện được:21 catalog mutation/supplier/voucher operations có controller permission nhưng direct Spring service bypass. Thêm service `@PreAuthorize` dùng matrix hiện hành; public catalog reads và global data scope giữ nguyên. Giữ ADMIN ban đầu/single ADMIN API policy, reserved grants/ROLE_MANAGE/self/privileged guards; không đổi UI source/schema/migration/routes, không hoàn thiện business workflow05+ hoặc audit-query27. Java21 H2 và PostgreSQL16.15 cluster/DB `phase04_test` mới riêng loopback15434 **69/69 PASS**, staff13→21; package PASS. Default-profile PG jar/validate/HTTP **18/18**, Edge demo H2 DOM **28/28**, paging/empty search/grant controls và visual1280/390 PASS. Audit flush lỗi DB rollback update/password/version/roles/stores/deactivate; duplicate create đồng thời đúng201/409 và một identity/audit. Runner browser đầu checkpoint pass nhưng cleanup exit1; đã sửa process/profile cleanup và rerun exit0. Không đọc production.env/mutation Supabase/DB cũ; legacy inventory/migration, last-admin concurrency stress và mọi empty role/store reference states còn UNVERIFIED. Giữ status Phase04 **IMPLEMENTED_AND_TESTED_LOCAL**, phần gap **COMPLETED**; D01/D02/dependency không đổi. [Findings/files/commands/limits/handoff04](development-plan/Phase04.md#phiên04--tái-kiểm-chứng-và-hoàn-thiện-gap-ngày-2026-10-08).

Phase03 follow-up 2026-10-08 (dừng tại Phase03): giữ recovery reset-link/D03, static UI/session/CSRF và password policy02. Chỉ sửa CSRF cached trong hai handler request/reset sau session expiry; Edge tái hiện **24 PASS/2 FAIL →26/26 PASS**. Bổ sung5 tests (recovery12→17) cho trước/sau exact expiry, replacement delivery rollback giữ link cũ, late token flush rollback/retry, IP/window boundary và nhân viên giữ profile/grants/revoke. Java21/H2 và PostgreSQL16.15 DB test mới riêng **61/61 PASS**, package PASS; default-profile PostgreSQL HTTP **11/11 PASS**, actual SMTP loopback connection failure generic202/rollback, mailbox demo bị deny và không lộ token thử trong API/log thường. Không đổi routes/schema/migration/backend services, không đọc production.env/mutation Supabase hoặc gửi SMTP thật. [Phase03 report/commands/logs](development-plan/Phase03.md#phase-completion-report). Giữ **IMPLEMENTED_AND_TESTED_LOCAL**; testConnection SMTP thành công theo report trước không chứng minh email đã đến hộp thư. Legacy inventory/migration và inbox delivery vẫn UNVERIFIED.

Phase01 follow-up 2026-10-08 (dừng tại01): sửa runner demo trên Windows PowerShell, guard PostgreSQL database path/query/Hikari/native DDL và inventory chỉ định schema, không đổi domain/UI/route. Java21 H2 và PostgreSQL16.15 `phase01_test` riêng loopback15433 **56/56 PASS**, package PASS; guard jar từ chối trước Hikari, PostgreSQL16 +Supabase `validate`/HTTP200/401/404 PASS; demo HTTP400/403/409 cũng PASS. Supabase17.11 schema `lunea` inventory read-only38 bảng/55 FK checks0 orphan, identities sạch chỉ cho schema mới; DB cũ5432 vẫn connection refused, chưa migrate legacy. Browser tool phiên01 không khởi tạo được; không coi HTTP là browser pass. [Phase01 completion report/commands/limits](development-plan/Phase01.md#tái-kiểm-chứng-phiên01-ngày-2026-10-08); trạng thái giữ IMPLEMENTED_AND_TESTED_LOCAL, phần gap hoàn thiện COMPLETED, dependency/decisions giữ nguyên. Các thay đổi UI/browser và report owner02 xuất hiện đồng thời được giữ nguyên.

Phase02 follow-up 2026-10-08 (dừng tại Phase02): giữ BCrypt/customer-staff principal/session/CSRF/remember/status/credential-version, D01/D02 và route boundary. Sửa đúng hai gap UI đã tái hiện: register validity/disabled submit và login lại với cached CSRF sau session expiry. Java21/H2 hiện tại **56/56 PASS** (auth14 và existing9 giữ nguyên; foundation safety cập nhật đồng thời), package PASS; Edge headless/HTTP demo loopback riêng **27/27 PASS** và visual mobile390 PASS. Timed expiry đã chờ thật với normal3s/remember6s; chưa chờ thời lượng mặc định30m/7d hoặc HTTPS deployed. Không có PostgreSQL test listener để rerun, không mutation Supabase, không migration dữ liệu legacy. [Phase02 report/commands/logs](development-plan/Phase02.md#phase-completion-report) phân biệt evidence mới với PostgreSQL/Supabase verification của lần trước; Phase02 vẫn IMPLEMENTED_AND_TESTED_LOCAL.

Database cũ localhost:5432 chưa kiểm kê/migrate; Supabase hiện là schema mới, chưa có dữ liệu kinh doanh thật. SMTP đã xác thực nhưng chưa xác minh email đến hộp thư. Hosting công khai/HTTPS, thay thông tin chi nhánh ảo, restore rehearsal và distributed rate limits thuộc cấu hình triển khai; bộ giới hạn hiện tại bounded single-instance. Session timeout được kiểm tra giá trị, chưa chạy thử thời gian thực dài. Các phase 5–32 và ownership/stock/checkout defects của chúng vẫn phải thực hiện riêng.

Chạy demo: `powershell -ExecutionPolicy Bypass -File .\scripts\run-demo.ps1`, mở http://localhost:8080. Fixtures: admin@lunea.test/DemoAdmin123, kho@lunea.test/KhoDemo123, customer@lunea.test/DemoUser123. Chỉ profile demo seed; restart để xóa dữ liệu test trong bộ nhớ. Xem [README](../README.md) và completion report từng Phase trước khi tiếp tục.
