# Final Checklist

> Checklist acceptance sau khi thực hiện các Phase. Tạo35 Markdown không hoàn thành project. Baseline9H2tests và static evidence không thay real PostgreSQL/HTTP/UI verification. Tất cả checkbox hiện chưa đánh dấu.

Đọc [Project Audit](00_PROJECT_AUDIT.md), [Master Roadmap](01_MASTER_ROADMAP.md) và các Completion Reports. Khi đánh dấu một mục, ghi evidence (command, environment, result hoặc screenshot/report link) ở Phase owner. Gate OPEN không được ngầm chọn recommendation để PASS.

## Build

- [ ] Java21 và Maven Wrapper build thành công; không compiler/startup exception trên supported runtime.
- [ ] Existing9tests và các tests bổ sung pass; ghi rõ profile/driver/DB/environment.
- [ ] Startup PostgreSQL test/restored environment pass; H2 pass không thay evidence PostgreSQL.

## Configuration

- [ ] Dev/test/deployment config tách biệt; tests không ghi/drop dev hoặc primary DB.
- [ ] Datasource, port, schema mode và migrations phù hợp môi trường.
- [ ] Password/provider/backup secrets externalized, không nằm script/log/report.
- [ ] UI/auth/provider/deployment decisions được ghi; không mặc định công nghệ chưa được duyệt.

## Database

- [ ] 37 bảng được đối chiếu actualschema/migrations; mọi khác biệt/extra field có quyết định.
- [ ] PK/FK/unique/NOT NULL/positive/nonnegative/enum constraints kiểm chứng trên PostgreSQL.
- [ ] Inventory/cart/wishlist surrogate IDs có unique pair được giữ nếu tương đương design.
- [ ] Duplicates/nulls/orphans/negative stock/held>actual/legacy enums được xử lý không mất lịch sử.
- [ ] Migrations chạy với clean/existing/restored data; review legacy không map arbitrary OrderItem.
- [ ] Generated IDs/sequences và codes theo D15 unique khi concurrent/retry; timestamps đúng sự kiện.

## Authentication

- [ ] Register tạo Account+Customer atomic, identifier/password/optionalphone/confirmterms theo D02.
- [ ] Credentials hash/verify thật; không plaintext/hash/secret trong response/log.
- [ ] Customer/staff login đúng accounttype; inactive account bị từ chối.
- [ ] Logout/expiry/identity invalidation hoạt động; stale session/token không vào private API.
- [ ] Recovery verify/expiry/attempt/single-use/replay/providerfailure theo D03; không tự reactivate account.

## Authorization

- [ ] Permission matrix10actors/UCactions enforced trên route/service/query.
- [ ] Employee role AND branch scope đúng cho stock/purchase/transfer/order/report.
- [ ] Global catalog/supplier không bị gán storeFK giả; CSKH scope theo permission thiết kế.
- [ ] Anonymous/customer/otherstaff không vào admin commands hoặc tự grant quyền.
- [ ] CustomerA không đọc/sửa/xóa profile/address/cart/order/return/review của CustomerB khi tamper IDs.
- [ ] Actor lấy từ authenticated principal, không từ defaultcustomerId/employeeId1 hoặc browser parameter.

## Customer Functions

- [ ] Active catalog/SKU/images/prices/per-store available stock đúng.
- [ ] Profile/address/default-address policy D05 và wishlist price đúng.
- [ ] Beauty Profile optional; recommendation theo D16/noAI, không cản mua khi profile rỗng.
- [ ] Wishlist/cart nối actualroutes; không fake card/price/stock placeholder.
- [ ] Order history/detail/status/cancel-beforeSHIPPING và return/review eligibility hoạt động.

## Admin Functions

- [ ] Employee/grants/store assignment/inactive và audit writer/query đúng quyền.
- [ ] Product/category/brand/attribute/SKU/images đủ phần thiếu; không harddelete dữ liệu đã có giao dịch.
- [ ] Store/hours/map và supplier/DRAFTpurchase/edit/confirm/history hoạt động.
- [ ] Stock adjust/count/transfer/holds/lowstock/export bảo toàn invariants và scope.
- [ ] Stafforder confirm/assign/status/shipping/complete/cancel/print đúng workflow.
- [ ] CSKH lookup/history/lock/support D09; return reuse Phase23.
- [ ] Program D10/voucher/effectiveness/export và reports hoàn thành theo metrics được duyệt.

## Forms & Validation

- [ ] Required/format/length/unique rules enforced server-side.
- [ ] Nesteditems có validation cascade; quantity>0, money/range/dates đúng miền.
- [ ] Invalidrole/store/SKU/orderItem IDs fail rõ, không silentlyskip hoặc partial mutation.
- [ ] UI field/global errors, loading/disabled submit/input preservation hợp lý.

## Search

- [ ] Keyword search theo tên/code/brand/category/supplier/customer/order/audit đúng UC.
- [ ] Search không lộ inactive/private/crossbranch data ngoài quyền.
- [ ] Empty/nonomatch/invalid input an toàn, không500.
- [ ] Timing theo D13 khoảng3s normalconditions có measurement, không invented SLA.

## Filter

- [ ] Price/brand/category/skin/needs/status/store/time filters đúng contract.
- [ ] Combinedfilters/reset/count và query state nhất quán.
- [ ] Range/date/vocabulary boundary và invalid input được xử lý.
- [ ] Report filters/metrics/date basis theo D17 đúng thời gian/store/status được xác nhận.

## Pagination

- [ ] Large lists dùng pagination/sort query contract, không load-all rồi cắt ở UI.
- [ ] Empty/invalid page/lastpage/count/boundary đúng.
- [ ] Filter/search/sort/paging giữ state và quyền dữ liệu.
- [ ] Pagination/loadmore reconcile DOCX text/mockup theo D01/D16.

## Cart

- [ ] Add merge đúng SKU; update/remove/clear chỉ owner; positive quantity rule rõ.
- [ ] Invalid customer/SKU/inactive không tạo nullowner hoặc zero-price giả.
- [ ] Line/subtotal/discount/shipping từ pricing contract; wishlist price không ZERO placeholder.
- [ ] Cart không giữ stock; hold chỉ khi order thành công và branch đã xác định.
- [ ] Concurrent merge/repeated request/cartcounter/error/empty UI được test.

## Order

- [ ] COD-only; owned address và recipient info hợp lệ.
- [ ] Một active branch đủ ALL SKU available; không firststore/null/split shipment fallback.
- [ ] Header/items/snapshots/voucher/holds/cartclear commit cùng hoặc rollback cùng.
- [ ] Product/SKU/price/discount/address snapshots giữ khi product data/price đổi.
- [ ] PENDING_CONFIRMATION→CONFIRMED→PREPARING→SHIPPING→COMPLETED/CANCELLED theo approved guards.
- [ ] Customer cancel trước SHIPPING và staff scope đúng; cancel/shipping/complete races nhất quán.
- [ ] Repeated confirm/complete/cancel/reassign không double commit/release; actual≥held≥0.
- [ ] Shipping edit/print/export đúng saved order, permissions và D18 manual/provider contract đã xác nhận.
- [ ] Return D07/orderline/qty/cumulative/disposition/restock đúng một lần.

## Upload/Image

- [ ] Approved Cloudinary/storage integration, type/size/permission validation.
- [ ] Product/SKU images/primary/order đúng; publish có primary theo design.
- [ ] Provider/DB failure compensation không orphan asset hoặc xóa ảnh đang dùng.
- [ ] URLlength/render/responsive size phù hợp, không broken/mixedcontent images.
- [ ] Catalog/cart/checkout dùng ảnh thực; empty/error state rõ.

## Error Handling

- [ ] Structured errors/status400/401/403/404/409/500 đúng contract.
- [ ] Không lộ stacktrace/SQL/credentials/providersecret hoặc entity khác owner.
- [ ] Late failure rollback purchase/transfer/checkout/return/audit transactions pass.
- [ ] Provider failure không fake success hoặc làm hỏng committed order.

## UI

- [ ] Đủ8 customer screens đối chiếu DOCX fields/layout/actions/states với actual evidence.
- [ ] Staff/account/orders/recovery screens theo UC +approved D01 layout; không claim mockup không tồn tại.
- [ ] Header/footer/menu/search/wishlist/account/cartcount/navigation hoạt động hoặc có D14 scope decision.
- [ ] Passwordtoggle/remember/confirmterms/qty/voucher/COD/success links nối backend thực.
- [ ] Loading/empty/noresult/validation/auth/forbidden/notfound/servererror states rõ, labels/keyboard dùng được.

## Responsive

- [ ] Desktop/tablet/phone layouts dùng được, không overflow che actions.
- [ ] Shopping/auth/orders flows có viewport evidence.
- [ ] Chrome/Edge/Firefox/Safari được kiểm chứng; missing environment không tự đánh PASS.

## Security

- [ ] Hash/status/auth/logout/recovery/ownership/role/scope test bằng directHTTP tamper.
- [ ] CSRF/CORS/cookie/tokenstorage/expiry theo chosen D01 mechanism, không áp cảJWT/session mặc định.
- [ ] Secrets/config/backup access protected; không public diagnostic/provider/backup endpoint tự phát.
- [ ] HTTPS/trustedproxy/IP/cert/cookies/redirect/mixedcontent theo D12.
- [ ] Importantadmin audit events ghi actor/time/object/details an toàn; không actor từ browser hoặc secret trong details.

## Testing

- [ ] Happy/validation/permission/invalid/empty/boundary cases của từng Phase có kết quả.
- [ ] Laststock/lastvoucher/concurrentstatus/retry/confirm/receive tests không oversell/doublemovement/quotaoverflow.
- [ ] RealPostgres migration/constraint/rollback/concurrency và HTTP tests, không chỉ H2/mock tests.
- [ ] BrowserE2E8screens/staffflows và external testdoubles dùng được.
- [ ] Tests không gửi realmessages/mutate providerproduction; testDB isolated và fixtures repeatable.

## Integration

- [ ] PostgreSQL startup/schema/data và restored test environment verified.
- [ ] Cloudinary/Maps/recovery/order notifications đúng config và failure behavior.
- [ ] Voucher quote→checkout consume→cancel policy D06 thống nhất; campaigns reuse Phase18.
- [ ] Revenue/bestsellers COMPLETED và metrics approved; inventory/promotion exports reuse.
- [ ] Notifications events/recipient/channel/retry D11 đúng committed state.
- [ ] Backup/restore/TLS evidence có; namedvolume không thay restore test.

## Documentation

- [ ] Audit91UC/32SYS/24NFR/37tables/8screens/63rules cập nhật actualstatus/evidence.
- [ ] Master dependency/owner/gates chính xác, không vòng hoặc việc bị giao trùng.
- [ ] Phase checkboxes/reports/status/files/routes/types/schema/config/testcommands/handoff chính xác.
- [ ] README/build/startup/test/role/config/backup docs phản ánh implementation.
- [ ] Không conflict tự chọn bên đúng, không localdecision gate vô chủ.

## Production Readiness

- [ ] D12 deployment/secrets/TLS/topology approved; không auto deploy/thay host/DB ngoài authorization.
- [ ] Backup schedule/retention/storage/restore goals xác nhận; restore DB riêng đã kiểm chứng.
- [ ] Schema migration/rollback/history runbook đủ, không uncontrolled destructive ddl-auto.
- [ ] D13 measurement khoảng3s normalconditions có env/dataset/result/limitations.
- [ ] Critical blocker/gate/knownissue resolved hoặc explicitapproved scope revision.
- [ ] Handoff có operator/maintenance instructions; không gọi projectDONE vì docscreated/9H2tests.

## Final Acceptance Record

Status: NOT_STARTED

- Verification environment: Chưa thực hiện.
- Build/PostgreSQL/HTTP/UI/browser/integration results: Chưa thực hiện.
- Decisions/gates closed or approved scope exceptions: Chưa thực hiện.
- Known issues/blockers: Xem Audit §17 và Master Decision Register.
- Evidence / Phase Completion Reports: Chưa thực hiện.
- Notes for maintenance / next agent: Chưa thực hiện.
