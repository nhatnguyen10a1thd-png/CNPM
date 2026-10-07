# Phase 04 — Nhân viên, RBAC, phạm vi chi nhánh và audit writer

> Status: IMPLEMENTED_AND_TESTED_LOCAL. Complexity: HIGH. Risk: HIGH.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Implementation ngày 2026-10-07 đã được kiểm chứng local trên H2, PostgreSQL 16.15 và trình duyệt Edge. Kiểm kê/migration database hiện hữu chưa được xác minh; xem Completion Report và [báo cáo Phase 01–04](../PHASE_01_04_REPORT.md).

## 1. Objective

Hoàn thiện quản trị nhân viên và thực thi role AND branch scope, đồng thời cung cấp audit writer cho các workflow quản trị sau.

## 2. Why This Phase Exists

Employee CRUD/role/store links đã có nhưng ignoresvalidatedcredentials, invalidIDs silentlyskip và inactiveemployee khôngdisableaccount. Role/Permission tables chưa bảo vệ bất kỳroute; AuditLog repository unused và thiếumodeltheodesign.

## 3. Current State (baseline lịch sử trước implementation)

- EmployeeRestController `/api/employees` CRUD;createAccount usesinternalEmail vàrawconstantpassword, bỏquaEmployeeRequestemail/password.
- Updatefullname/phone+replace role/storelinks; invalidIDsignored, primarybranchflagkhôngset; accountemail/phone khôngsync.
- Employee.status có INACTIVE, Account.status chưa có ở baseline; sau Phase02 cần reuse account status/hash/principal.
- Role/Permission/EmployeeRole/RolePermission/EmployeeStoreentities/repos có; RolePermissionRepository<Long> sai@IdClassRolePermissionId.
- AuditLog performedByString/action/details/time, thiếuemployeeFK/objecttype/id/IP/longtext; AutditLogRepository unused.
- No method/route/scope enforcement hoặcadminUI; noadminmockup trongprimaryUI.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [x] Phase02 identity/hash/status và deny-default boundary đã implemented và tested local; trạng thái triển khai xem report Phase02.
- [x] D01/D02 được ghi theo yêu cầu hoàn thiện Phase01–04 và UI cơ bản: static HTML/JS cùng origin, session + CSRF, permission matrix, login email và internalEmail riêng.
- [ ] PostgreSQL test baseline và demo bootstrap đã verified; kiểm kê roles/store/account và áp dụng migration trên database hiện hữu còn chờ. Không reset dữ liệu hoặc seed mật khẩu mặc định production.
- [x] D01/D02 và phạm vi quyền được ghi trong [Decision Register](01_MASTER_ROADMAP.md#decision-register), [STAFF_ACCESS](../STAFF_ACCESS.md) và report tổng.
- [ ] Working tree đã kiểm tra; database hiện hữu chưa truy cập để kiểm kê. Chỉ tạo môi trường test riêng; không reset DB hiện hữu.

## 5. Scope

### IN SCOPE

- EmployeeCRUD/search/paging/deactivation/role/storeassignment verticalslice.
- Permissionmatrix từUCactors; grant/readrolespermissions và enforce route/service branchscope.
- FixRolePermissionrepoIDcontract; validateallassociationsbeforemutations.
- Auditwriter/model theo bảng145 cho importantadminactions vàconsumercontract; queryUI riêng27.

### OUT OF SCOPE

- Product/store/purchase/inventory/order/businessscreensđầyđủ thuộc05+.
- Auditsearch/filter/reportUI thuộc27; khôngbuildwriterlầnhai.
- Wholesale/SSO/MFA/newidentityprovider; noarchitecturereplacement.
- Khôngbắtcatalog/supplierglobaldata cóstoreFK hoặccustomerphảiboundbranch tựphát.

## 6. Requirements Covered

- UC82–86;SYS06/07/27/30;QLNV-QĐ1–5;schema139–145 và109account.
- CácactorQLSP/QLCH/QLNH/QLTK/QLKH/QLDH/QLKM/QLNV/BCTKđượcmaptoactionsUC29–91, không grantall mặcđịnh.
- AuditqueryUC87 owner27;writer04 làprerequisite; staffviewsderiveUC vìnoadminmockup.
- D01role/layout/auth +D02staffidentitymapping; data migrationconstraints owner04.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [EmployeeRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/EmployeeRestController.java)
- [EmployeeService.java](../../src/main/java/com/thinh/cosmetic/service/account/EmployeeService.java)
- [EmployeeServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/EmployeeServiceImpl.java)
- [EmployeeRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/EmployeeRequest.java)
- [EmployeeResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/account/EmployeeResponse.java)
- [EmployeeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeEntity.java)
- [RoleEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/RoleEntity.java)
- [PermissionEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/PermissionEntity.java)
- [RolePermissionEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/RolePermissionEntity.java)
- [RolePermissionId.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/RolePermissionId.java)
- [EmployeeStoreEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeStoreEntity.java)
- [RolePermissionRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/RolePermissionRepository.java)
- [AuditLogEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java)
- [AutditLogRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AutditLogRepository.java)

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [EmployeeServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/EmployeeServiceImpl.java)
- [EmployeeRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/EmployeeRestController.java)
- [EmployeeRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/EmployeeRequest.java)
- [EmployeeRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/EmployeeRepository.java)
- [RolePermissionRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/RolePermissionRepository.java)
- [EmployeeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeEntity.java)
- [AuditLogEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java)

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- Permission/scope/auditservice helpers theoexistingaccount/servicepackages, rolegrantAPI nếuexistingemployeePUT khôngđủ.
- Adminemployee/assignment/grantviews/client theoD01approvedlayout; no fakeprimarymockup claim.
- Migrationstaff/auditconstraints/longtext/objectfields vàtestsrole/scope/assignment/actorlogging.
- Trusteddev/testbootstrapfixtures/rolepermissionmatrix docs; khôngseedproductiondefaultsecret.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

Fix repository generic ID thành RolePermissionId không tựđổiPK/schema. Giữ composite EmployeeRole/RolePermission/EmployeeStore và accountemployeeone-to-one. Reconcile Employee.requiredname/internalemail/accountunique/status/primarybranchtheodesign; auditORDINALvalues trướcmigrate. AuditLog proposedemployeeFK/action/objecttype/objectID/details/text/IP theo bảng145; legacyperformedBy không tựmap giả, giữ/reconcile trướcconstraint. Khôngdeleteemployee/audit/history. Grants/seed chỉtheoapprovedroles, nodroporadminautoaccount.

## 11. Backend Tasks

- [x] PermissionCatalog map chín staff actors và ADMIN thành explicit permission bundles; ghi route-family/store scope policy trong STAFF_ACCESS. Customer identity thuộc Phase02; không coi bundles là evidence hoàn tất toàn bộ91 UC consumer workflows.
- [x] Sửa RolePermissionRepository IDLong→RolePermissionId; addrepositoryintegrationtest compositeIDlookup/save.
- [x] Reconcile EmployeeRequestemail/password/internalEmail; validateidentifier/uniquecredentials, encodepasswordqua02, bỏconstantpassword.
- [x] PrevalidateallroleIds/storeIds/duplicateIDs/active store/primarybranchpolicy trướccreate/update; invalidassociationfail rõ, không silentlyskip.
- [x] Create/updateaccount+employee+role/storelinksatomic; updateprofileidentityfieldssynctheoapprovedcontract, no orphan/partialgrants.
- [x] Enforce employee/accountinactive vàexistingidentity/sessioninvalidations; preservehistory/deactivationsemanticsDELETE.
- [x] Addemployeesearchkeyword/name/email/phone vàpaging; no load-all foradminlist.
- [x] Enforce permission AND store scope ở service/query cho store/inventory/purchase/transfer; global catalog/supplier/voucher dùng permission. Order/return/customer/review và các workflow chưa hoàn thiện giữ deny-all; owner Phase sau phải bổ sung ownership/scope trước mở route.
- [x] Implementgrants UI/APIauthorization:QLNV/adminonlytheoapprovedmatrix, no self-escalation/rawemployeeIdoverride.
- [x] Prepareauditwriteractorfromprincipal, object/action/time/details/IPtheodesign; no passwords/secrets in details; safelegacyhandling.
- [x] Emitimportantemployee/account/rolescopechangesaudit events; consumerworkflowphasesđăngkýevents khi code;04khôngimplementmọibusinessmutation.
- [x] Testinvalidlinklatefailure/permissionscope/inactive/sharedidentity/audit andcompositeID.

## 12. Frontend Tasks

- [x] Buildemployeesearch/list/pagination/create/edit/deactivate screens +role/storeassignmenttheoapprovedD01.
- [x] Fields reflectapprovedemail/internalEmail/password/status contract; no displayhash/defaultpassword.
- [x] Grantcontrols vàprimarybranchselection theoagreedpolicy, invalidassociationerror rõ.
- [x] Handleemptyroles/stores/list, inactiveaccount,409duplicate vàpermission403; hiddencontrolsnotsoleenforcement.
- [x] Auditwriteconfirmation/actor safe; no Auditsearchscreen27 here.
- [x] Loginredirect02 staffpermissionsappropriate, responsive/labels/tablestate theoNFR.

## 13. Validation & Business Rules

- QLNV1uniqueidentifier;2inactivepreservehistory;3assignmentbranch;4role ANDscope;5actor/time/object/action/details.
- No silentlyskipinvalidroles/store; no caller-suppliedactoridentity accepted.
- Globalcatalog/supplierdata usepermission, branchscope appliedonlytoentities thựcgắnbranch; not inventedcustomerStoreFK.
- Deactivatedstaff cannotaccessprivateroutesusingoldidentity; preserveorders/procurement/auditreferences.
- Auditwriterfail/rollback behavior foradmintransaction must be explicit andtested, nounauditedpartialgrant.

## 14. Security Requirements

QLNV/admin quản trị employee/grants/audit theoapprovedmatrix; otherstaff không tựcreate/deactivate/grant. Anonymous/customer deniedstaffcommands. Principledeny-default, leastcapabilities từdesignactors; rolepermission changes không cho escalation bằngrequestroleIds/employeeId. Boundquery/entityscope checks không chỉfrontend. Auditactor lấyprincipal; forwardedIP chỉtrustedproxyconfig31, khôngtrustarbitraryheader.

## 15. Error Handling

- Duplicateemployee/accountidentifier409; invalidrole/store400/404 theo01; no partialaccount/grants.
- Noauthentication401/forbidden403; crossbranchrequest khôngtrảsensitiveentitydetails.
- Inactiveaccount/expiredidentity denied; no silentreactivation.
- Audit/persistencefailure mustrollbackadminmutation khi auditmandatory; no detailsleaksecrets.

## 16. Integration Points

- 05–29 reusepermission/branchscope/auditwriter; mỗiPhaseownsitsdomainvalidation/auditevents.
- 27queryconsumesauditmodel/writer withoutrewriting04.
- 02accountresolver/status and03recoverysharestaffidentity;25customerlockusestatuspolicy.
- 31/32config/rolefixtures/deactivation/session/permissionsmatrix E2E.

## 17. Implementation Order

1. Read02contracts/data vàapprovepermissionmatrix/employeeidentity/adminlayout.
2. FixcompositeIDcontract; prepareconstraint/auditmodelmigrations vớilegacydataaudit.
3. CorrectatomicemployeeCRUD/assignment/status/hash/validation.
4. Implementrolegrant andbranchscope enforcement +auditwriter/events.
5. BuildstaffadminUI;runpermission/association/rollback/audit regression;handoff.

## 18. Verification

- [x] BuildJDK21tests +PostgrescompositeID/constraints/employeeintegration.
- [x] TestQLNV vsotherstaff/customer/anonymous routes vàcrossbranchentities;correct401/403.
- [x] Invalidassociation trướcmutation;latefailure leavesaccount/employee/grants/auditconsistent.
- [x] No rawpassword/defaultconstant, inactiveemployeeoldidentitydenied,historyintact.
- [x] Browser verified staff create/edit/deactivate/search/grant restore, assigned/unassigned warehouse và mobile 390px; API tests verified empty page, invalid fields/links, audit fields và permission failures. Chưa claim browser pagination/empty reference lists được exhaustively tested.
- [x] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [x] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [x] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P04-T01 | Happy create | Admin +validcredentials/role/storeids | Onehashedaccount/employee andcorrectlinks, auditactorrecorded |
| P04-T02 | Validation | Missingemail/password/internalEmailtheocontract | Controlledfieldvalidation, nocreatewithconstantpassword |
| P04-T03 | Invalid IDs | Secondrole/storeID nonexistent | Wholecommandfails; no partialaccount/linkdeletion |
| P04-T04 | Duplicate race | Concurrentidentifiercreate | Exactlyoneemployee, controlledconflict |
| P04-T05 | Permission | Customer/QLSP attemptsgrant/adminCRUD | 401/403 perauth; no privilegechange |
| P04-T06 | Branch scope | Warehouse/orderemployee asksunassignedstoreentity | Denied/filter theoapprovedscope; no leak |
| P04-T07 | Deactivation | Admindisableemployee thenoldsessionreuse | Identitydenied; allhistoricalFKs/auditremain |
| P04-T08 | Assignment boundary | DuplicateIDs/no primary/theoapprovedpolicy | Correctvalidation/primarysemantics, no duplicates |
| P04-T09 | Composite repository | RolePermissionId valid/invalid pair | Lookup/save/delete workwithcorrectIdClass |
| P04-T10 | Audit failure | Mandatoryauditpersistfail duringgrantupdate | No unauditedpartialprivilegecommit |
| P04-T11 | Empty list | Noemployee orkeywordnonmatch | Emptytypedpage/UIstate, no500 |
| P04-T12 | Actor tamper | BrowseremployeeId/forwardedIPfake | Actorprincipalunchanged; trustedIPpolicyonly |

## 20. Definition of Done

- [x] UC82–86 employee/adminverticalslice vàrole/scopechecks complete đúngmatrix.
- [x] Hash/identity/status02 reused; invalidIDs/atomicassociationdata/errorhandlingtested.
- [x] Auditwriter/schema/events staff actionsverified, no queryUI27 implementedearly.
- [x] CompositeIDrepo fixed; realPostgres/testdata androlefixturesdocumented.
- [x] Các checkbox phản ánh implementation/test local; deployment inventory/migration và các giới hạn evidence được ghi rõ bên dưới.
- [x] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [x] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Staff cóadminemployee/grant/branchassignment thực thi server-side vàauditwriter ổn định. Domainpermissions/auditevents được consumerPhases kết nối,khôngtuyênbốmọiworkflow đãhoànthành.

## 22. Handoff To Next Phase

- 05+ biếtcurrentstaffprincipal/permissionmatrix/branchscopehelpers/auditwritercontract.
- 27cóAuditLogschema/fields/provider/eventexamples vàpermissionschoquery.
- Recordexactroutes/roles/seedfixtures/primarybranchpolicy/schema/legacydecisions/lockoutbehavior trongreport.

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

## Work Completed

- Employee create/update/deactivate atomic giữa Account, Employee, role/store links và audit; dùng password được gửi, BCrypt encoder chung Phase02, không còn password constant.
- Email login/username và internalEmail chuẩn hóa, đồng bộ phone/account status; PasswordPolicy chung xử lý password và phone. Create cần password; update có thể bỏ password. Validator bảo vệ cả gọi service trực tiếp.
- Validate toàn bộ role/store IDs, distinct positive IDs, store active và primary store trước mutation. PUT dùng complete form; null arrays được coi là empty. Một primary bắt buộc nếu có assigned stores.
- Search full name/login email/internal email/phone và paging 1–100; keyword wildcard được escape.
- PermissionCatalog định nghĩa ADMIN và chín actor QLSP/QLCH/QLNH/QLTK/QLKH/QLDH/QLKM/QLNV/BCTK. PermissionPolicy lấy grants/scope mới từ DB mỗi request, kiểm tra employee/account ACTIVE và principal binding/version.
- Method guards và bounded queries bảo vệ store/inventory/purchase/transfer. Transfers cần scope của cả source và destination. Global catalog/supplier/voucher dùng permissions tương ứng.
- QLNV quản trị nhân viên thông thường; ADMIN quản trị role grants. Reserved ADMIN grants bất biến, ROLE_MANAGE không cấp sang role khác; chặn self role/scope/status edits, self-deactivation và QLNV sửa/cấp quyền cho privileged employee.
- Pessimistic ADMIN-role lock và last-active-admin safeguard đã có trong code. Không tuyên bố đã chạy concurrent employee-create hoặc concurrent last-admin mutation test.
- AuditWriter tham gia transaction MANDATORY; employee create/update/deactivate và role-permission changes ghi actor từ principal, action/object/time/details/IP. IP dùng remoteAddr, bỏ qua forwarding header giả. Audit failure rollback cả employee creation và role grant replacement.
- Deactivate khóa account, tăng credentialsVersion, invalidates old session ở request kế tiếp; giữ employee/grants/history. Role/store revoke/grant có hiệu lực trên session hiện tại.
- Loại bỏ actor default ID 1 khỏi REST controllers. Các customer/order/return/review/cart/wishlist chưa đủ ownership giữ deny-all theo security boundary.

## Files Created

- security/PermissionCatalog.java, PermissionPolicy.java, StaffAccessSnapshot.java.
- service/account/AuditWriter.java, RoleGrantService.java.
- domain/dto/request/account/RolePermissionsRequest.java.
- domain/dto/response/account/RoleResponse.java, PermissionResponse.java.
- rest/account/RoleRestController.java, PermissionRestController.java.
- src/test/java/com/thinh/cosmetic/security/StaffAuthorizationIntegrationTest.java.
- [STAFF_ACCESS.md](../STAFF_ACCESS.md) ghi matrix/API/scope/audit contract.
- Shared UI src/main/resources/static/index.html, app.js, app.css và demo bootstrap thuộc implementation phối hợp Phase01–04; xem [report tổng](../PHASE_01_04_REPORT.md).

## Files Modified

- EmployeeService/EmployeeServiceImpl, EmployeeRestController, EmployeeRequest/Response, EmployeeEntity/Repository.
- EmployeeRoleRepository, RoleRepository, PermissionRepository, RolePermissionRepository; generic ID sửa thành RolePermissionId, giữ composite PK.
- AuditLogEntity thêm employee/object/IP fields và details text.
- StoreServiceImpl, InventoryServiceImpl, PurchaseOrderServiceImpl, StockTransferServiceImpl và purchase/transfer repositories thêm store-bounded guards/queries.
- REST controllers catalog/supplier/voucher thêm permission checks; controller stock/procurement lấy actor từ CurrentAccountResolver.
- REST controllers customer/cart/wishlist/order/return/review bỏ default actor IDs, dùng principal nơi có actor parameter; không mở các workflow còn thiếu ownership.
- Shared Account/status/hash/security/error/config/migration files xem report Phase01/02 và [report tổng](../PHASE_01_04_REPORT.md).

## Database Changes

Employee account_id required/unique, full_name required/max150 và internal_email required/unique. Employee.status giữ ORDINAL 0/1 với explicit smallint, không chuyển enum lịch sử sang STRING. Composite EmployeeRole/EmployeeStore/RolePermission giữ nguyên.

Audit thêm nullable employee_id FK, object_type/object_id/ip_address; details text. Legacy performed_by được giữ, không tự suy diễn nhân viên cho lịch sử cũ. Account status/version/unique credentials và recovery schema chia sẻ Phase02/03.

Môi trường PostgreSQL test riêng đã kiểm chứng ORM schema, constraints và composite repositories. [V001 và runbook](../database/README.md) là migration thủ công có preflight; chưa kiểm kê hoặc áp dụng lên database developer/deployed hiện hữu. Không coi tạo schema test là bằng chứng migration database hiện hữu.

## APIs / Routes Added

| Method/route | Permission | Contract |
|---|---|---|
| GET /api/employees?keyword=&page=0&size=10 | EMPLOYEE_READ hoặc EMPLOYEE_MANAGE | Spring Page<EmployeeResponse> |
| GET /api/employees/{id} | EMPLOYEE_READ hoặc EMPLOYEE_MANAGE | EmployeeResponse |
| POST /api/employees | EMPLOYEE_MANAGE | 201; complete EmployeeRequest, password required |
| PUT /api/employees/{id} | EMPLOYEE_MANAGE | Complete EmployeeRequest, password optional |
| DELETE /api/employees/{id} | EMPLOYEE_MANAGE | 204; deactivate, giữ history |
| GET /api/roles | EMPLOYEE_MANAGE hoặc ROLE_MANAGE | RoleResponse[] |
| GET /api/permissions | EMPLOYEE_MANAGE hoặc ROLE_MANAGE | PermissionResponse[] |
| PUT /api/roles/{id}/permissions | ROLE_MANAGE AND ADMIN | {"permissionIds":[...]} |

EmployeeRequest: fullName, email, internalEmail(optional defaults email), phone(optional), password, status ACTIVE/INACTIVE, roleIds, storeIds, primaryStoreId. Response thêm roleIds/storeIds/primaryStoreId bên cạnh safe profile/role/store names; không có secrets.

GET /api/stores hỗ trợ assignment lookup cho EMPLOYEE_MANAGE; STORE_READ staff chỉ thấy assigned stores. Full matrix và scope các route families trong [STAFF_ACCESS](../STAFF_ACCESS.md). /api/auth/me trả roles/permissions/storeIds từ grants mới, thuộc Phase02.

## UI Added

UI cùng origin dùng API thực: employee search/list/paging/create/edit/deactivate, role/store assignment và primary branch; role grant controls cho ROLE_MANAGE; profile/login/logout chung. UI hiển thị server validation/conflict/permission errors, loading/empty states và safe success messages. Không thêm audit query screen Phase27 hoặc business screens Phase05+.

Actual Edge headless DOM/browser run gồm 15 checkpoints toàn Phase01–04 đã pass: staff create/edit/deactivate/search, grant revoke/restore, warehouse store1 allowed/store2 denied và responsive 390px. Audit actor/details/rollback xác minh bằng integration tests; không suy ra toàn bộ UI edge state đã được kiểm tra chỉ từ browser smoke.

## Decisions Made

- Theo yêu cầu user hoàn thiện Phase01–04 và giao diện cơ bản để test: static HTML/JavaScript cùng Spring Boot, session/CSRF, không tạo frontend project mới.
- Email là account login/username; internalEmail là identifier nhân viên riêng, optional defaults email. Phone/password policy chung Phase02.
- Permission bundles explicit; ADMIN global branch bypass chỉ sau permission check. QLNV không có ROLE_MANAGE. Grant changes lấy DB mới, không cache grants trong session.
- Full-form PUT thay toàn bộ role/store sets; nonempty scope cần đúng một primary store; inactive stores không được assign.
- Audit mandatory trong cùng transaction; forwarding headers không được coi trusted IP.
- New-store creation reserved ADMIN; other store commands cần permission AND assignment. Transfers require cả hai branch scopes.
- Deactivation thay deletion và giữ historical FKs. Last-admin guard có ADMIN-role lock; chưa claim stress/concurrency evidence cho guard này.

## Deviations From Plan

UI cơ bản được tạo từ yêu cầu user và UC, không claim khớp admin mockup không có. Chỉ enabled staff capabilities có service/query guards; order/return/customer/review và unfinished customer domains giữ deny-all cho tới owner Phase tương ứng. Chưa hoàn thiện các business workflow Phase05+ hoặc audit search Phase27.

Không dùng database developer đang có dữ liệu để vượt test gate: PostgreSQL 16.15 test riêng và H2 disposable; migration hiện hữu là task deployment còn pending. H2 PostgreSQL mode cần NON_KEYWORDS=VALUE cho legacy column; employee ordinal mapping explicit smallint giữ dữ liệu.

## Known Issues

- Developer/deployed database chưa được kiểm kê; trạng thái duplicate/null/legacy staff/account/status/grants trên database đó chưa biết. V001 phải qua preflight/backup/review trước áp dụng.
- Twelve staff integration tests kiểm chứng duplicate error, không có concurrent employee-create race test riêng. ADMIN lock/last-admin safeguards có code nhưng chưa có concurrent lockout stress test.
- Browser smoke chưa bao phủ exhaustive pagination navigation hoặc mọi empty role/store reference state.
- Later customer/order/return/review/cart services còn domain gaps và routes bị đóng; permission code không chứng minh consumer workflow đã complete.
- Audit queries/report UI và event registration cho domain mutations khác thuộc Phase sau.

## Remaining Tasks

- Kiểm kê/reconcile database hiện hữu, review và áp dụng V001 bằng runbook; kiểm chứng validate startup/session/grants sau migration trên environment đó.
- Consumer Phases dùng PermissionPolicy/AuditWriter và bổ sung ownership/branch scope/business rules trước enable routes.
- Dedicated concurrent employee-create/last-admin mutation tests và full UI edge-state suite cần bổ sung trước claim production/concurrency acceptance.

## Verification Result

Ngày 2026-10-07, JDK21:

- H2: `mvnw.cmd test` — 52 tests, 0 failures/errors/skipped; staff suite 12 tests pass. Log target/phase1-4-h2.log.
- Real PostgreSQL 16.15, isolated lunea_test trên loopback port15432: `mvnw.cmd test -Dspring.profiles.active=test-postgres` với TEST_DB_URL/USERNAME/PASSWORD của test DB — 52 tests, 0 failures/errors/skipped; staff suite 12 tests pass. Log target/phase1-4-postgres.log; test profile create/drop chỉ test database.
- Package build pass, log target/phase1-4-package.log; browser Edge headless DOM smoke 15 checkpoints pass gồm staff CRUD/search/grant restore/scope và 390px viewport. Exact reproduction/environment details và artifact paths trong [report tổng](../PHASE_01_04_REPORT.md).
- Staff suite có: supplied BCrypt credential + primary/audit principal/IP, invalid/duplicate link no-write, invalid update giữ links/identity, missing password/duplicate409, anonymous/customer/nonmanager denial, QLNV ADMIN/self-escalation guards, REST/direct-service crossbranch, live grant/revoke/composite IDs, scoped purchase/transfer lists/IDs, deactivate old-session/history, audit rollback employee/grants và typed empty page/reserved ADMIN.
- Không dùng PASS test-schema để claim migration/deployed legacy inventory hoặc concurrent employee mutation đã verified. Verification migration V001 local riêng đang được tổng hợp ở report Phase01; applied-to-existing-database vẫn pending.

## Notes For Next Phase

Reuse CurrentAccountResolver và PermissionPolicy; actor IDs từ request không phải identity. Scope chỉ cho entities gắn store, không thêm store FK cho catalog/supplier/customer. PermissionPolicy current validates account/employee binding/version; fresh grants/store IDs có hiệu lực cho session cũ.

AuditWriter.write(action, objectType, objectId, details) bắt buộc gọi trong transaction admin/domain hiện hữu; audit failure rollback khi mandatory. Không đưa passwords/recovery secrets vào details. Phase27 dùng existing employee/object/IP/legacy performedBy model; không tạo writer khác.

Đọc [STAFF_ACCESS](../STAFF_ACCESS.md), [report tổng](../PHASE_01_04_REPORT.md) và [database runbook](../database/README.md) trước mở thêm routes. Trạng thái local implementation đã có evidence; deployment/migration còn phải hoàn tất theo môi trường thực tế.
