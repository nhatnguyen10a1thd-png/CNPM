# Phase 31 — Backup, restore và cấu hình triển khai

> Status: NOT_STARTED. Complexity: MEDIUM. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Đây là kế hoạch phát triển phần còn thiếu; tài liệu này chưa thực hiện code, migration hoặc verification của Phase.

## 1. Objective

Kiểm chứng sao lưu/phục hồi và chuẩn bị cấu hình triển khai an toàn cho stack PostgreSQL hiện tại.

## 2. Why This Phase Exists

SYS-28/29 và NFR17 yêu cầu backup/restore; named Docker volume không chứng minh có bản sao lưu. Repo chỉ dev config/ddl-auto=update/plain development credentials và port8080, chưa có HTTPS deployment evidence.

## 3. Current State

- docker-compose.yml dùng postgres:16, port5432, namedvolume lunea_pgdata; không có backup script/schedule/restore runbook.
- application.properties cấu hình PostgreSQL dev, ddl-auto=update, port8080; không TLS/config profiles production trong baseline.
- Migrations/schema đã thay trong các Phase trước phải đọc báo cáo; không dùng schema audit H2 làm baseline deployed.
- Notification03/30 và Cloudinary/Maps07/09 có thể có config sau implementation; không giả định secrets đã externalized.
- D12 chưa chốt backup storage/schedule/retention/restore goals/host topology/TLS.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [ ] Phase01,24,28,29,30 COMPLETED; toàn bộ migrations của các Phase đã chốt được kiểm kê.
- [ ] D12 chốt backup schedule/retention/location/restore goals và deployment/HTTPS topology.
- [ ] Có PostgreSQL test database riêng và backup directory xác định; không dùng primary DB làm restore target.
- [ ] Có permission môi trường cần thiết trước thao tác bên ngoài workspace/production; Phase này không tự deploy.
- [ ] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [ ] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

## 5. Scope

### IN SCOPE

- Runbook/scripts/config tối thiểu cho native PostgreSQL backup/restore theo approved D12.
- Restore verification schema+data+constraints+critical workflow trên test database riêng.
- Externalized deployment config/secrets, startup/schema mode và HTTPS verification theo topology được xác nhận.
- Operational evidence và documentation; không thay DB/host provider theo sở thích.

### OUT OF SCOPE

- Deploy production/publish site/overwrite primary DB/reset Docker volume.
- Backup admin API/UI, database platform replacement, cloud service mới tự chọn.
- RPO/RTO/retention/schedule AI tự đặt như yêu cầu nghiệp vụ.
- New business features, schema redesign/PK rewrite, uncontrolled real emails/uploads during restore tests.

## 6. Requirements Covered

- SYS28 periodic backup;SYS29 restore;NFR17 backup/recovery;NFR10 HTTPS khi thực tế deploy.
- NFR16 historicaldata/NFR24integrity phải giữ sau restore; existing PostgreSQL16 architecture.
- D12 operational settings/topology;D11/03 integration configs không được lộ secrets.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [docker-compose.yml](../../docker-compose.yml)
- [application.properties](../../src/main/resources/application.properties)
- [pom.xml](../../pom.xml)
- [README.md](../../README.md)
- [OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [InventoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java)
- [AuditLogEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java)
- Migrations/config/SQL/schema baselines và reports do01–30 thật tạo; kiểm kê trước backup.

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [README.md](../../README.md)
- [application.properties](../../src/main/resources/application.properties)
- [docker-compose.yml](../../docker-compose.yml)
- Operational/config files thực tế được tạo trong01/07/09/30; chỉ khi cần externalization.

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- TECHNICAL RECOMMENDATION: backup/restore scripts sử dụng pg_dump/pg_restore của PostgreSQL hiện tại, path/target checks rõ và no embedded secrets.
- Runbook deployment/backup/restore/HTTPS, environment variable inventory và verification log template.
- Schedule/config chỉ theo D12; không tạo admin backup controller/table.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

NONE — không đổi business schema. Backup đọc source DB; restore chỉ vào database thử nghiệm mới/tách biệt và đã xác nhận target. Không drop/recreate original DB hoặc volumes. Schema+sequence+FK+unique/check/index/migration history từ các Phase trước phải được phục hồi đúng; kiểm tra version/extension compatibility PostgreSQL16.

## 11. Backend Tasks

- [ ] Chốt D12 operational topology/storage/schedule/retention/restore goals, ghi approvals/config; không tự suy diễn thời gian.
- [ ] Kiểm kê migration versions/entities/critical data và external dependencies trước backup.
- [ ] Viết/review pg_dump/pg_restore runbook/scripts native phù hợp environment; explicit resolved paths/DB names, không shell string commands từ input không trusted.
- [ ] Bảo vệ backup/credentials/access; không ghi passwords trong script/report; test data an toàn.
- [ ] Thực hiện backup test/schedule dry-run theo approved policy; kiểm tra artifact format/completeness trước restore.
- [ ] Restore vào DB thử riêng, verify schema/row counts/IDs/sequences/FK/unique/checks/text/enum/legacy mapping.
- [ ] Smoke critical catalog/account/order/stock/return/report reads trên restored DB với test integration adapters.
- [ ] Externalize runtime secrets và config primary/test; choose safe schema mode theo01 migration strategy, không ddl-auto create/drop production.
- [ ] Verify HTTPS bằng approved hosting/proxy/server topology; cookie/auth redirect/cert handling compatible02.
- [ ] Document failure recovery và repeatability; preserve original DB/volume untouched.

## 12. Frontend Tasks

- [ ] Kiểm tra HTTPS URL/redirects/mixed content cho ảnh/Maps/account/checkout trong approved deployment test.
- [ ] Auth cookies/session/token behavior theo02 trên TLS; không tự đổi mechanism.
- [ ] Không tạo backup admin screen; UI chỉ chạy smoke trên restored environment.
- [ ] Ghi browser verification và environment rõ; không tuyên bố deployed production khi chỉ localtest.

## 13. Validation & Business Rules

- PrimaryDB/volume không là restore target; backup artifact phải có schema và dữ liệu cần thiết.
- Generated IDs/sequences sau restore không collision; snapshots/audit/history intact.
- Schedule/retention/goals theo D12; no invented RPO/RTO.
- Dev/test/production secrets/config tách biệt; provider adapters test-safe.
- HTTPS requirement thực tế, không đánh dấu đạt chỉ vì configport8080.

## 14. Security Requirements

Backup chứa PII/credentials hashes nên access/storage/retention theo D12, no public download route. Credentials qua approved environment/secret handling, không verbose logs. TLS topology/signatures/trusted forwarded headers theo confirmed deployment; không request broad cloud permissions hoặc đổi provider tự phát.

## 15. Error Handling

- Invalid resolved path/DB target: abort trước write/restore.
- Missing/corrupt/incompatible backup: report failure, no drop original.
- Restore FK/sequence/migration discrepancy: keep target test isolated; do not repairprimary automatically.
- TLS/config/provider error: controlled blocked readiness with environment/evidence, no fake pass.

## 16. Integration Points

- 01 schema/testbaseline;24 reviewlegacy migration;28–29 reportdata integrity;30 integrationconfig.
- All migrations/data changes from02–30 phải trong inventory; report không bypass với H2 snapshot.
- 32 dùng restored test environment và TLS/config/runbook evidence.

## 17. Implementation Order

1. Chốt D12, inventory actual migrations/environment và explicit targets.
2. Prepare backup scripts/runbook/config review với path/DB safeguards.
3. Backup và restore separate test DB, verify schema/data/constraints/sequences.
4. Smoke restored app withfake externaladapters; verify HTTPS config/testtopology.
5. Record outputs/timing/issues/runbook/config và handoff32.

## 18. Verification

- [ ] Backup artifact đọc/validate được; schedule command/config theo policy.
- [ ] Restore schema+data trên DB test riêng; counts/constraints/sequences/legacy refs đúng.
- [ ] Confirm originalDB/volume untouched; no destructive commandagainstcomputeduncheckedtarget.
- [ ] Application startup restored DB và critical workflows/report reads pass; externaladaptersfake.
- [ ] HTTPS/cookies/redirects/mixedcontent verified in named testdeployment; production readiness ghi limitations.
- [ ] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [ ] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [ ] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P31-T01 | Happy restore | Known PostgreSQL16fixture backup→separateDB | Schema/data/history/critical reads correct |
| P31-T02 | Permission | No backup destination access | Controlledfailure, no partial dangerous restore |
| P31-T03 | Target safety | Restore target pointsprimaryDB/volume/outsideapproveddir | Abort before mutation |
| P31-T04 | Corrupt artifact | Invalid/incomplete backup | Rejected or failure isolated in testDB; primaryuntouched |
| P31-T05 | Constraint integrity | Duplicate/orphan/negative fixturecheck afterrestore | Constraints giữ nhưsource schema; discrepancyreported |
| P31-T06 | Sequence boundary | Create new test record after restored maxID | No duplicate PK/sequence collision |
| P31-T07 | Empty database | Backup empty butvalidschema | Restore validschema, no fakebusinessseed |
| P31-T08 | Config secret safety | Inspect scripts/logs/docs/env inventory | No plaintextsecrets in tracked scripts/logs |
| P31-T09 | TLS test | Approved HTTPS topology +account/checkout/images | Certificate/redirect/auth/mixedcontent correct |
| P31-T10 | Version mismatch | Unsupportedrestore PostgreSQL client/server | Controlledreported compatibilityfailure, no originalreset |

## 20. Definition of Done

- [ ] SYS28/29 backup/restore có realtest evidence vàapprovedschedule/retention.
- [ ] OriginalDB untouched; schema/data/constraints/sequences/history verified.
- [ ] Config/secrets/schema mode vàHTTPS proof/limitations rõ theoD12.
- [ ] Runbook córepeatablecommands/failure recovery, nobackup adminAPI/UI.
- [ ] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [ ] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [ ] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Có khả năng sao lưu/phục hồi có kiểm chứng và cấu hình/TLS readiness cho môi trường được xác nhận; không tự deploy hoặc thay đổi primarydata.

## 22. Handoff To Next Phase

- 32 biết exact restoredDB/testenvironment/TLS URL/runbook/backup format/schema versions vàlimitations.
- Operators cóapprovedschedule/retention/location/goals vàsafeguards; secrets không nằmMarkdown.
- Reportghi artifact location safe/commands/results/counts/migration inventory, khôngchỉ'namedvolumeexists'.

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
