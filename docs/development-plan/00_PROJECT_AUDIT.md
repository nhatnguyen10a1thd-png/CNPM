# Project Audit

> Implementation update 2026-10-07: Phase01–04 code/UI **IMPLEMENTED_AND_TESTED_LOCAL**, Java21 +H2/PostgreSQL16.15 đều52/52tests pass, browser15checkpoints pass. Có BCrypt/session/CSRF/recovery/RBAC/scope/audit và basicstaticUI. Xem [báo cáo thay đổi](../PHASE_01_04_REPORT.md), [staff contract](../STAFF_ACCESS.md), [DB runbook](../database/README.md). PostgreSQL deployed/dev5432 và SMTP thật vẫn chưa kiểm chứng. Audit bên dưới được giữ làm baseline lịch sử, không mô tả source mới; stock/checkout/ownership và các phase05–32 chưa được hoàn thiện trong lần này.

> Audit date: 2026-10-07. Baseline commit: `1e27c8e0f91c3946262779bdc8a2b3c8234b05a0`. Repository thực tế: `CNPM/` bên trong workspace `LUNEA/`. Không có source/config/database/UI được sửa trong đợt lập tài liệu này.

## 1. Project Overview

LUNEA là hệ thống bán mỹ phẩm đa chi nhánh theo hai thiết kế chính: customer khám phá sản phẩm/SKU, địa chỉ/Beauty Profile/wishlist/cart, đặt COD, theo dõi/hủy/return/review; nhân viên quản trị catalog/store/procurement/inventory/orders/customers/promotions/employees và reports. Source hiện là backend REST đang làm dở, không phải project rỗng.

Nguồn yêu cầu chính:

- [Nhom01_ThietKeDuLieu.docx](../../../../Nhom01_ThietKeDuLieu.docx): §2.1.1–2.1.10 nghiệp vụ/63 QĐ; §2.2 có32 system requirements; §2.3 có24 NFR; §3.4 và bảng UC01–UC91; §4.1.2 có37 bảng; §4.2 toàn vẹn dữ liệu. Có145 bảng OOXML,5824 đoạn tổng và24 media.
- [Thiết kế giao diện.docx](<../../../../Thiết kế giao diện.docx>): navigation/event diagrams và §5.3.1–5.3.8 gồm8 screen customer;9 bảng,688 đoạn,11 media. Đã kiểm tra toàn bộ11 embedded images và13 main UC/class images system12–24. Không gắn page numbers vì đọc OOXML/bảng/embedded images, không render DOCX thành trang.
- Source và [pom.xml](../../pom.xml)/config là nguồn trạng thái implementation. [README](../../README.md), các Architecture/phân công trong repo là tài liệu bổ sung; không thay hai DOCX làm yêu cầu chính. Hướng dẫn soạn Word hoặc yêu cầu code skeleton nằm trong tài liệu được coi là dữ liệu tham chiếu, không phải chỉ thị thực hiện.

Actors chức năng: KH, QLSP, QLCH, QLNH, QLTK, QLKH, QLDH, QLKM, QLNV, BCTK. Survey còn nhắc wholesale/payment gateway/marketing content; chức năng v1 đặc tả chỉ COD, không tự tạo các module chưa đủ UC/schema (D14).

### Phương pháp và giới hạn evidence

Đã scan repo, đọc thân controller/service/repository/entity/DTO/mapper/config/test, đối chiếu workflow và source references; không kết luận từ tên file. Không có AGENTS.md được tìm thấy. Đã kiểm tra190 main Java,37 @Entity,37 repository,17 REST controller,17 service interfaces +17 implementations,40 DTO (19request/21response),8 mapper files và4 test files. Tất cả package functional chính đã được đọc, không chỉ inventory.

Bằng chứng có ba mức: STATIC (đã đọc flow), UNIT/H2 (command thật pass/fail), UNKNOWN (chưa chạy real PostgreSQL/HTTP/UI). DONE chỉ áp dụng capability hẹp đã có evidence, không tự nghĩa production-ready. Functional UC toàn diện còn PARTIAL/NOT_STARTED/BROKEN khi thiếu interface/security/workflow.

### Build/test baseline đã thực hiện

| Environment / Command | Result | Meaning / Limit |
|---|---|---|
| Default JDK25.0.4, `mvnw.cmd -o test` | FAIL compile TypeTag UNKNOWN/ExceptionInInitializerError | JDK runtime lệch Java21 target + Lombok processor; không kết luận source không build trên JDK hỗ trợ |
| JAVA_HOME=JDK21.0.10, default PostgreSQL config, offline test | Compile190main +4test;8unit PASS, contextLoads ERROR connection refused localhost5432 | PostgreSQL môi trường chưa sẵn sàng; runtime/schema PostgreSQL UNKNOWN |
| JDK21.0.10 + H2 in-memory PostgreSQL mode override | 9/9 PASS: Inventory2, Order1, Voucher5, context1 | JPA/repository/context mapping tạo được H2; không chứng minh PostgreSQL SQL/DDL/concurrency/HTTP/UI |

Command H2 đã chạy (override CLI, không sửa application.properties):

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-21.0.10'
.\mvnw.cmd -o test '-Dspring.datasource.url=jdbc:h2:mem:lunea_audit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1' '-Dspring.datasource.driver-class-name=org.h2.Driver' '-Dspring.datasource.username=sa' '-Dspring.datasource.password=' '-Dspring.jpa.hibernate.ddl-auto=create-drop' '-Dlogging.level.root=ERROR'
```

Các `target/surefire-reports/TEST-*.xml` hiện phản ánh lần H2 cuối; lần PostgreSQL trước đó đã bị overwrite. Kết quả failure trên là quan sát audit2026-10-07, có thể tái kiểm chứng bằng JDK21/default config khi môi trường đã thay đổi. Không khởi động PostgreSQL/reset data hoặc thực hiện request mutation để lập audit.

## 2. Current Technology Stack

| Technology | Version / Evidence | Current use |
|---|---|---|
| Java | target21 tại pom; verified JDK21.0.10; default machine25.0.4 | Java21 là baseline hỗ trợ đã chạy |
| Maven | distribution3.9.16, wrapper3.3.4 tại [wrapper properties](../../.mvn/wrapper/maven-wrapper.properties) | Maven Wrapper |
| Spring Boot | 4.1.1 tại pom parent | Application/autoconfiguration |
| Spring MVC | resolved7.0.9 từ H2 Surefire classpath | REST webmvc, không MVC page views |
| Spring Data JPA | resolved4.1.1 | 37 repositories |
| Hibernate ORM | resolved7.4.5.Final | Entity/JPA mappings |
| PostgreSQL | driver42.7.13 resolved; Docker `postgres:16` | Configured primaryDB; connection chưa chạy được |
| H2 | resolved2.4.240, runtime dependency | Test override đã dùng; không thay primaryDB |
| MapStruct | 1.6.3 + processor tại pom | 8 mapper files, generated compile artifacts |
| Lombok | dependencyresolved1.18.46; processor pinned1.18.36 | DTO/entity/builders; version skew debt |
| ModelMapper | 3.0.0 | Bean LOOSE/skipNull ở MapperConfig, chưa consumer ngoài config |
| Jakarta Validation | Spring Boot validation starter | @Valid và annotations có một phần, cascade/business rules còn thiếu |
| JUnit Jupiter | resolved API6.0.3 | 4 files/9cases |
| Thymeleaf/Bootstrap/SiteMesh | Không dependency/assets/config | NOT_STARTED; chưa chốt UI D01 |
| Spring Security/JWT/session | Không dependency/classes/config | NOT_STARTED; raw credentials check không là auth session |
| Cloudinary/email/Maps/reporting | Không integration implementation | NOT_STARTED theo các use case tương ứng |

Resolved versions lấy từ classpath báo cáo test đã chạy, không là đề xuất upgrade. Webmvc + web starters trùng concern, Lombok processor lệch dependency, ModelMapper unused là debt; không có lý do đổi stack/DB. Config datasource primary ở [application.properties](../../src/main/resources/application.properties), DB/dev volume ở [docker-compose.yml](../../docker-compose.yml).

## 3. Current Architecture

```text
HTTP JSON request
  → REST Controller (@Valid một phần)
  → Service interface → ServiceImpl (@Transactional)
  → Spring Data JPA Repository
  → JPA Entity
  → configured PostgreSQL (runtime chưa verified)
DTO ↔ MapStruct/manual mapping
Exception → GlobalExceptionHandler → HTTP400 plain string
```

17 controllers đều REST; services có business mutation/DTO creation; repository chủ yếu derived queries; MapStruct dùng cho account/catalog/customer/supplier/store/voucher. Order/cart/purchase/transfer/return/review dùng thêm manual mapping.

Chưa có luồng Browser→MVC Controller→Model→Thymeleaf hoặc frontend SPA trong repo. Không có layout/fragments/static CSS/JS/AJAX/fetch. REST không trả view là hợp lệ với kiến trúc hiện có, không phải orphan controller bug. UI architecture/auth mechanism cần D01 trước Phase02.

## 4. Current Project Structure

```text
CNPM/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/maven-wrapper.properties
├── docker-compose.yml
├── README.md
├── docs/
│   ├── LUNEA_Architecture*.md
│   ├── PHAN_CONG_CODE_LUNEA_2_TUAN*.md
│   ├── GIT_GUIDE*.md / Git_Tutorial.md
│   ├── Nhom01_ThietKeGiaoDien.pdf
│   └── development-plan/ (bộ tài liệu này)
├── src/main/java/com/thinh/cosmetic/
│   ├── CosmeticStoreChainManagementApplication.java
│   ├── config/MapperConfig.java
│   ├── domain/
│   │   ├── dto/request/ (19) / response/ (21)
│   │   ├── entity/account,cart,catalog,order,purchase,returns,review,store
│   │   └── enums/
│   ├── exception/GlobalExceptionHandler.java
│   ├── mapper/
│   ├── repository/ (37)
│   ├── rest/ (17)
│   └── service/ interfaces + impl/
└── src/
    ├── main/resources/application.properties
    └── test/java/com/thinh/cosmetic/ (4 files)
```

Không có templates/static/migration/schema/seed trong source tracked. Không liệt kê target/generated/IDE cache trong cây; source/media/test artifacts ở target chỉ là cache audit.

## 5. Functional Audit

Mỗi UC của bảng §3.4 có một dòng dưới đây, không suy luận DONE từ class existence. Evidence anchor trỏ §6 đã kiểm tra chain cụ thể. Missing gồm capability end-to-end; Existing Implementation là phần phải tái sử dụng. Những lỗi ở security/validation ảnh hưởng module không phủ nhận phần CRUD/happy arithmetic đã có.

| Module | Requirement | Status | Existing Implementation | Missing | Notes |
|---|---|---|---|---|---|
| Dùng chung / Auth | UC01 — Đăng nhập | BROKEN | Kiểm tra email/password rồi trả CustomerResponse; [evidence](#backend-auth) | Plaintext; không session/principal/status; không staff login | System §3.4 / UC01; [Phase02](Phase02.md) |
| Dùng chung / Auth | UC02 — Đăng xuất | NOT_STARTED | Không có endpoint hoặc cơ chế phiên; [evidence](#backend-auth) | Invalidate session/token và UI logout | System §3.4 / UC02; [Phase02](Phase02.md) |
| Dùng chung / Auth | UC03 — Khôi phục mật khẩu | NOT_STARTED | Không có service/DTO/endpoint/provider; [evidence](#backend-auth) | Xác minh và reset theo D03 | System §3.4 / UC03; [Phase03](Phase03.md) |
| Customer | UC04 — Xem danh sách sản phẩm | PARTIAL | getAll Product trả DTO; [evidence](#backend-catalog) | Active filter, SKU/giá/ảnh và screen | System §3.4 / UC04; [Phase10](Phase10.md) |
| Customer | UC05 — Tìm kiếm sản phẩm | NOT_STARTED | Không có keyword query/service; [evidence](#backend-catalog) | Tên/brand/category và empty state | System §3.4 / UC05; [Phase10](Phase10.md) |
| Customer | UC06 — Lọc và sắp xếp sản phẩm | NOT_STARTED | Không có filter/sort/Pageable; [evidence](#backend-catalog) | Price/brand/category/skin/needs và pagination | System §3.4 / UC06; [Phase10](Phase10.md) |
| Customer | UC07 — Xem chi tiết sản phẩm | PARTIAL | getById Product có mô tả cơ bản; [evidence](#backend-catalog) | SKU/giá/ảnh/availability/tabs/related screen | System §3.4 / UC07; [Phase10](Phase10.md) |
| Customer | UC08 — Xem tình trạng sản phẩm tại cửa hàng | PARTIAL | Inventory có available endpoint store/SKU; [evidence](#backend-inventory) | Product-to-SKU public stock view, active store | System §3.4 / UC08; [Phase10](Phase10.md) |
| Customer | UC09 — Đăng ký tài khoản | PARTIAL | Tạo Account+Customer, check email tồn tại; [evidence](#backend-auth) | Hash/phone uniqueness, D02 validation và screen | System §3.4 / UC09; [Phase02](Phase02.md) |
| Customer | UC10 — Cập nhật hồ sơ cá nhân | PARTIAL | Customer profile get/update; [evidence](#backend-customer) | Principal/phone/email uniqueness, phone response và UI | System §3.4 / UC10; [Phase11](Phase11.md) |
| Customer | UC11 — Quản lý địa chỉ nhận hàng | BROKEN | Address CRUD; create-default clear địa chỉ khác; [evidence](#backend-customer) | Update/delete không owner check; update-default không clear | System §3.4 / UC11; [Phase11](Phase11.md) |
| Customer | UC12 — Quản lý Beauty Profile | PARTIAL | Read/upsert theo customer; [evidence](#backend-customer) | Ownership, vocabulary/validation và optional UI | System §3.4 / UC12; [Phase12](Phase12.md) |
| Customer | UC13 — Xem sản phẩm gợi ý | NOT_STARTED | Beauty và attribute data tồn tại; [evidence](#backend-customer) | Rule-based matching theo D16, không AI | System §3.4 / UC13; [Phase12](Phase12.md) |
| Customer | UC14 — Quản lý danh sách yêu thích | PARTIAL | Get/add idempotent/remove theo product; [evidence](#backend-customer) | Price trả ZERO; identity/active data/UI | System §3.4 / UC14; [Phase11](Phase11.md) |
| Customer | UC15 — Xem giỏ hàng | PARTIAL | Get/create cart và totals/ảnh chính; [evidence](#backend-cart) | Identity, data validation và screen | System §3.4 / UC15; [Phase17](Phase17.md) |
| Customer | UC16 — Thêm sản phẩm vào giỏ hàng | PARTIAL | Merge SKU vào cart đã có; [evidence](#backend-cart) | Active SKU, positive quantity, invalid customer/concurrency | System §3.4 / UC16; [Phase17](Phase17.md) |
| Customer | UC17 — Cập nhật số lượng sản phẩm trong giỏ | BROKEN | Update theo cartItemId; ≤0 xóa; [evidence](#backend-cart) | Không kiểm tra item thuộc cart/customer; quantity policy | System §3.4 / UC17; [Phase17](Phase17.md) |
| Customer | UC18 — Xóa sản phẩm khỏi giỏ | BROKEN | Delete theo cartItemId; [evidence](#backend-cart) | Không kiểm tra ownership item | System §3.4 / UC18; [Phase17](Phase17.md) |
| Customer | UC19 — Áp dụng voucher | PARTIAL | Discount endpoint và checkout dùng voucher; [evidence](#backend-voucher) | Quote/UI, eligibility đầy đủ, FREESHIP đúng | System §3.4 / UC19; [Phase18](Phase18.md) |
| Customer | UC20 — Tính tổng giá trị đơn hàng | PARTIAL | Subtotal/discount/shipping/total đã tính; [evidence](#backend-voucher) | Một pricing contract, D06 fee policy | System §3.4 / UC20; [Phase18](Phase18.md) |
| Customer | UC21 — Đặt hàng | BROKEN | COD/order snapshots/holds/clear cart đã có; [evidence](#backend-order) | First-store/null, không đủ-stock/owner/atomic quota | System §3.4 / UC21; [Phase19](Phase19.md) |
| Customer | UC22 — Xem lịch sử đơn hàng | PARTIAL | findByCustomerIdOrderByCreatedAtDesc; [evidence](#backend-order) | Principal/scope/paging/customer screen | System §3.4 / UC22; [Phase20](Phase20.md) |
| Customer | UC23 — Xem chi tiết đơn hàng | BROKEN | getById trả items/snapshots; [evidence](#backend-order) | Không bảo vệ owner của route chi tiết | System §3.4 / UC23; [Phase20](Phase20.md) |
| Customer | UC24 — Theo dõi trạng thái đơn hàng | PARTIAL | Enum/status nằm trong response; [evidence](#backend-order) | Workflow guards và UI timeline/tabs | System §3.4 / UC24; [Phase20](Phase20.md) |
| Customer | UC25 — Hủy đơn hàng | BROKEN | Owner check và release holds hiện có; [evidence](#backend-order) | Chỉ PENDING_CONFIRMATION, lệch cancel trước SHIPPING | System §3.4 / UC25; [Phase20](Phase20.md) |
| Customer | UC26 — Gửi yêu cầu trả hàng | BROKEN | Create return header/items; [evidence](#backend-return) | Owner, eligible order, line membership/qty/policy | System §3.4 / UC26; [Phase23](Phase23.md) |
| Customer | UC27 — Đánh giá sản phẩm | BROKEN | Create Review liên kết product/customer/optional order; [evidence](#backend-review) | Không xác minh completed purchase; FK OrderItem thiếu | System §3.4 / UC27; [Phase24](Phase24.md) |
| Customer | UC28 — Xem đánh giá sản phẩm | PARTIAL | Public getByProduct lọc VISIBLE; [evidence](#backend-review) | Screen, paging và dữ liệu verified purchase | System §3.4 / UC28; [Phase24](Phase24.md) |
| Catalog | UC29 — Tra cứu sản phẩm | PARTIAL | Product list/getById; [evidence](#backend-catalog) | Keyword/filter/page/scope/admin UI | System §3.4 / UC29; [Phase05](Phase05.md) |
| Catalog | UC30 — Tra cứu SKU | NOT_STARTED | ProductSkuRepository được cart/order đọc; [evidence](#backend-catalog) | Dedicated SKU lookup/DTO/API/UI | System §3.4 / UC30; [Phase06](Phase06.md) |
| Catalog | UC31 — Quản lý sản phẩm | PARTIAL | Create/update/delete Product; [evidence](#backend-catalog) | Validation/deactivate thay hard delete/history/admin UI | System §3.4 / UC31; [Phase05](Phase05.md) |
| Catalog | UC32 — Quản lý danh mục sản phẩm | PARTIAL | Category CRUD; [evidence](#backend-catalog) | D04 hierarchy, linked history, validation/UI | System §3.4 / UC32; [Phase05](Phase05.md) |
| Catalog | UC33 — Quản lý thương hiệu | PARTIAL | Brand CRUD và parent extra; [evidence](#backend-catalog) | D04 mapping, linked history, validation/UI | System §3.4 / UC33; [Phase05](Phase05.md) |
| Catalog | UC34 — Quản lý thuộc tính mỹ phẩm | NOT_STARTED | Attribute entity/repository, chưa service sử dụng; [evidence](#backend-catalog) | CRUD/group và UI, vocabulary D16 | System §3.4 / UC34; [Phase05](Phase05.md) |
| Catalog | UC35 — Quản lý SKU/biến thể | NOT_STARTED | SKU entity/repo; chưa CRUD service/controller; [evidence](#backend-catalog) | Price/listPrice/barcode/attribute/status CRUD | System §3.4 / UC35; [Phase06](Phase06.md) |
| Catalog | UC36 — Quản lý hình ảnh sản phẩm | NOT_STARTED | Image entity/repository đọc bởi cart; [evidence](#backend-catalog) | Upload/Cloudinary, primary/SKU binding và UI | System §3.4 / UC36; [Phase07](Phase07.md) |
| Catalog | UC37 — Kiểm duyệt đánh giá sản phẩm | PARTIAL | Update moderation status và lọc VISIBLE; [evidence](#backend-review) | Role protection/admin list/UI; giữ nội dung | System §3.4 / UC37; [Phase24](Phase24.md) |
| Store | UC38 — Tra cứu cửa hàng | PARTIAL | Store list/getById; [evidence](#backend-store) | Search/page/scope/UI | System §3.4 / UC38; [Phase09](Phase09.md) |
| Store | UC39 — Quản lý cửa hàng | PARTIAL | Create/update/deactivate Store; [evidence](#backend-store) | Required info, inactive checkout exclusion/UI | System §3.4 / UC39; [Phase09](Phase09.md) |
| Store | UC40 — Quản lý giờ hoạt động | PARTIAL | operatingHours String được lưu; [evidence](#backend-store) | D05 schema/format/migration và form hours | System §3.4 / UC40; [Phase09](Phase09.md) |
| Store | UC41 — Tra cứu sản phẩm tại cửa hàng | PARTIAL | Inventory list/getByStoreAndSku; [evidence](#backend-store) | Store product/SKU search/page/available view | System §3.4 / UC41; [Phase09](Phase09.md) |
| Store | UC42 — Xem vị trí cửa hàng | NOT_STARTED | Store có coordinates; [evidence](#backend-store) | Map/locator theo Google Maps design/config | System §3.4 / UC42; [Phase09](Phase09.md) |
| Purchase | UC43 — Tra cứu nhà cung cấp | PARTIAL | Supplier list/getById; [evidence](#backend-purchase) | Keyword/page/scope/UI | System §3.4 / UC43; [Phase13](Phase13.md) |
| Purchase | UC44 — Quản lý thông tin nhà cung cấp | PARTIAL | Supplier create/update/deactivate; [evidence](#backend-purchase) | Validation/history/scope/UI | System §3.4 / UC44; [Phase13](Phase13.md) |
| Purchase | UC45 — Lập/Cập nhật phiếu nhập hàng | PARTIAL | Create DRAFT/header/items/total; [evidence](#backend-purchase) | Edit DRAFT, nested positive validation/atomic creation | System §3.4 / UC45; [Phase13](Phase13.md) |
| Purchase | UC46 — Xác nhận nhập hàng | PARTIAL | DRAFT guard, tăng stock, CONFIRMED; [evidence](#backend-purchase) | Rollback/concurrency/scope/UI; giữ sequential guard | System §3.4 / UC46; [Phase14](Phase14.md) |
| Purchase | UC47 — Tra cứu lịch sử nhập hàng | PARTIAL | Purchase getAll trả list; [evidence](#backend-purchase) | Supplier/SKU/time filters, page và UI | System §3.4 / UC47; [Phase14](Phase14.md) |
| Purchase | UC48 — Xem chi tiết phiếu nhập | PARTIAL | getById gồm items và totals; [evidence](#backend-purchase) | Scope/detail UI, immutable confirmed data | System §3.4 / UC48; [Phase13](Phase13.md) |
| Inventory | UC49 — Tra cứu tồn kho | PARTIAL | List store và detail store/SKU; [evidence](#backend-inventory) | Search/page/scope; actual/held/available hiển thị đúng | System §3.4 / UC49; [Phase08](Phase08.md) |
| Inventory | UC50 — Ghi nhận nhập kho | PARTIAL | Purchase confirm đã tăng actual; [evidence](#backend-inventory) | Tích hợp confirmed receipt, chứng minh không tăng hai lần | System §3.4 / UC50; [Phase14](Phase14.md) |
| Inventory | UC51 — Điều chỉnh tồn kho | BROKEN | Lưu log before/after rồi set stock; [evidence](#backend-inventory) | Cho âm/below-held, actor có thể null, rollback/lock | System §3.4 / UC51; [Phase08](Phase08.md) |
| Inventory | UC52 — Kiểm kê kho | NOT_STARTED | Adjustment có thể tái sử dụng; [evidence](#backend-inventory) | Count/difference/reason/actor flow và screen | System §3.4 / UC52; [Phase15](Phase15.md) |
| Inventory | UC53 — Điều chuyển hàng giữa cửa hàng | BROKEN | Create/ship/receive với sequential state guards; [evidence](#backend-inventory) | Ship dùng actual không available; same-store/qty/atomicity | System §3.4 / UC53; [Phase16](Phase16.md) |
| Inventory | UC54 — Quản lý hàng giữ | PARTIAL | Checkout creates HELD; cancel release; complete commit; [evidence](#backend-inventory) | Availability/locking/ownership và đúng một lần | System §3.4 / UC54; [Phase08](Phase08.md) |
| Inventory | UC55 — Tính tồn khả dụng | PARTIAL | max(0,actual-held); 2 inventory tests có case; [evidence](#backend-inventory) | Bảo vệ invariant, không che dữ liệu âm bằng clamp | System §3.4 / UC55; [Phase08](Phase08.md) |
| Inventory | UC56 — Tra cứu sản phẩm tồn thấp | BROKEN | Query actualStock≤minimumStock; [evidence](#backend-inventory) | Design yêu cầu available≤minimum | System §3.4 / UC56; [Phase15](Phase15.md) |
| Inventory | UC57 — Xuất báo cáo tồn kho | NOT_STARTED | Không có export/report handler; [evidence](#backend-inventory) | Scoped export theo store/SKU khớp inventory view | System §3.4 / UC57; [Phase15](Phase15.md) |
| CSKH | UC58 — Tra cứu khách hàng | PARTIAL | Customer getAll; [evidence](#backend-customer) | Name/email/phone search, page và CSKH scope/UI | System §3.4 / UC58; [Phase25](Phase25.md) |
| CSKH | UC59 — Xem hồ sơ khách hàng | PARTIAL | getProfile đã có; [evidence](#backend-customer) | CSKH authority/scope và screen | System §3.4 / UC59; [Phase25](Phase25.md) |
| CSKH | UC60 — Xem lịch sử mua hàng | PARTIAL | Order by customer list đã có; [evidence](#backend-customer) | CSKH role/scope/UI, reuse customer order contract | System §3.4 / UC60; [Phase25](Phase25.md) |
| CSKH | UC61 — Cập nhật trạng thái tài khoản khách hàng | NOT_STARTED | Account không có status; [evidence](#backend-customer) | Lock/unlock giữ history, login/session enforcement | System §3.4 / UC61; [Phase25](Phase25.md) |
| CSKH | UC62 — Tiếp nhận yêu cầu trả hàng | PARTIAL | Return create/list đã có; [evidence](#backend-return) | CSKH intake role + workflow từ Phase23 | System §3.4 / UC62; [Phase23](Phase23.md) |
| CSKH | UC63 — Xử lý yêu cầu trả hàng | BROKEN | Set return status/processedBy/time; [evidence](#backend-return) | Policy/state guard/disposition/restock/atomicity | System §3.4 / UC63; [Phase23](Phase23.md) |
| CSKH | UC64 — Tra cứu lịch sử yêu cầu hỗ trợ | NOT_STARTED | Không model/schema/service; [evidence](#backend-customer) | D09 phải chốt trước implementation | System §3.4 / UC64; [Phase25](Phase25.md) |
| Order staff | UC65 — Tra cứu đơn hàng | PARTIAL | Order getAll; [evidence](#backend-order) | get/filter/search/page/staff scope/UI | System §3.4 / UC65; [Phase21](Phase21.md) |
| Order staff | UC66 — Xem chi tiết đơn hàng | PARTIAL | Order DTO chứa header/items snapshot; [evidence](#backend-order) | Staff role/branch scope/detail UI | System §3.4 / UC66; [Phase21](Phase21.md) |
| Order staff | UC67 — Xác nhận đơn hàng | PARTIAL | Set CONFIRMED và confirmedAt; [evidence](#backend-order) | Valid receiving info/stock/state/scope checks | System §3.4 / UC67; [Phase21](Phase21.md) |
| Order staff | UC68 — Phân công chi nhánh xử lý | BROKEN | Checkout chọn first store hoặc null; [evidence](#backend-order) | Assign/reassign một branch đủ ALL SKUs và move holds | System §3.4 / UC68; [Phase21](Phase21.md) |
| Order staff | UC69 — Cập nhật trạng thái đơn hàng | BROKEN | updateStatus nhận enum bất kỳ; [evidence](#backend-order) | Transition guards, idempotency và branch scope | System §3.4 / UC69; [Phase21](Phase21.md) |
| Order staff | UC70 — Hủy đơn hàng bởi nhân viên | PARTIAL | Existing customer cancellation/release có thể reuse; [evidence](#backend-order) | Staff authority/allowed transitions/scope/API UI | System §3.4 / UC70; [Phase21](Phase21.md) |
| Order staff | UC71 — Tính tổng tiền đơn hàng bởi nhân viên | PARTIAL | Order totals/snapshot fields đã có; [evidence](#backend-order) | Reuse pricing/saved snapshot, không recalculation từ giá mới | System §3.4 / UC71; [Phase18](Phase18.md) |
| Order staff | UC72 — Cập nhật thông tin giao hàng | NOT_STARTED | Snapshot address khi checkout, chưa shipping edit; [evidence](#backend-order) | Shipping update constraints/time/status/UI | System §3.4 / UC72; [Phase22](Phase22.md) |
| Order staff | UC73 — Xác nhận hoàn thành đơn | BROKEN | COMPLETED trừ actual/held và clamp; [evidence](#backend-order) | Không previous-state/repeat guards, atomic commit đúng một lần | System §3.4 / UC73; [Phase22](Phase22.md) |
| Order staff | UC74 — Xử lý đơn trả hàng đã duyệt | NOT_STARTED | Return status có thể đổi, chưa stock disposition; [evidence](#backend-return) | D07 approved restock/reverse movement theo quyết định | System §3.4 / UC74; [Phase23](Phase23.md) |
| Order staff | UC75 — In/Xuất thông tin đơn hàng | NOT_STARTED | Không handler/template/export; [evidence](#backend-order) | Scoped snapshot print/export | System §3.4 / UC75; [Phase22](Phase22.md) |
| Marketing | UC76 — Quản lý chương trình khuyến mãi | NOT_STARTED | Chỉ Voucher tồn tại; [evidence](#backend-voucher) | D10 Program–Voucher model/CRUD scope | System §3.4 / UC76; [Phase26](Phase26.md) |
| Marketing | UC77 — Quản lý voucher | PARTIAL | Create/read/update/deactivate Voucher; [evidence](#backend-voucher) | Validation/search/page/unique update/security/UI | System §3.4 / UC77; [Phase18](Phase18.md) |
| Marketing | UC78 — Kiểm tra điều kiện voucher | PARTIAL | Active/date/quota/subtotal đã kiểm tra; [evidence](#backend-voucher) | Targets/D06+D10, atomic quota enforcement | System §3.4 / UC78; [Phase18](Phase18.md) |
| Marketing | UC79 — Tính giá trị giảm | PARTIAL | PERCENT/FIXED cap;5 tests; [evidence](#backend-voucher) | FREESHIP/negative bounds/preview consistency | System §3.4 / UC79; [Phase18](Phase18.md) |
| Marketing | UC80 — Xem hiệu quả chương trình khuyến mãi | NOT_STARTED | Không aggregates/query/report; [evidence](#backend-voucher) | D10 program model; usage/discount aggregates | System §3.4 / UC80; [Phase26](Phase26.md) |
| Marketing | UC81 — Xuất báo cáo khuyến mãi | NOT_STARTED | Không export; [evidence](#backend-voucher) | Reuse program effectiveness/scoped export | System §3.4 / UC81; [Phase26](Phase26.md) |
| Employee/RBAC | UC82 — Tra cứu nhân viên | PARTIAL | Employee list/getById; [evidence](#backend-employee) | Search/page/admin UI | System §3.4 / UC82; [Phase04](Phase04.md) |
| Employee/RBAC | UC83 — Quản lý nhân viên | BROKEN | Create/update liên kết roles/stores; [evidence](#backend-employee) | Ignores validated email/password; fixed raw password; invalid IDs skipped | System §3.4 / UC83; [Phase04](Phase04.md) |
| Employee/RBAC | UC84 — Quản lý trạng thái tài khoản nhân viên | PARTIAL | Employee.status→INACTIVE; [evidence](#backend-employee) | Account status/identity/session invalidation/history | System §3.4 / UC84; [Phase04](Phase04.md) |
| Employee/RBAC | UC85 — Phân nhân viên vào chi nhánh | PARTIAL | EmployeeStore links có CRUD qua employee; [evidence](#backend-employee) | Invalid IDs fail rõ, primary branch semantics, scope enforcement | System §3.4 / UC85; [Phase04](Phase04.md) |
| Employee/RBAC | UC86 — Phân quyền nhân viên | NOT_STARTED | Role/Permission/link entities có; [evidence](#backend-employee) | Enforcement + grant UI; RolePermission repo ID mismatch | System §3.4 / UC86; [Phase04](Phase04.md) |
| Employee/RBAC | UC87 — Tra cứu nhật ký thao tác | NOT_STARTED | AuditLog và AutditLogRepository chưa dùng; [evidence](#backend-employee) | Writer04 và scoped query/filter/page27 | System §3.4 / UC87; [Phase27](Phase27.md) |
| Reports | UC88 — Tổng quan doanh thu và đơn hàng | NOT_STARTED | Order snapshots có dữ liệu nguồn; [evidence](#backend-reports) | COMPLETED revenue/time/store/status aggregates/dashboard | System §3.4 / UC88; [Phase28](Phase28.md) |
| Reports | UC89 — Báo cáo sản phẩm và tồn kho | NOT_STARTED | Order items và inventory có dữ liệu nguồn; [evidence](#backend-reports) | Completed bestsellers, inventory/low-stock reuse15 | System §3.4 / UC89; [Phase28](Phase28.md) |
| Reports | UC90 — Báo cáo nhập hàng và khuyến mãi | NOT_STARTED | Purchase/Voucher/Order data có; [evidence](#backend-reports) | Scoped aggregate/export, reuse program26 | System §3.4 / UC90; [Phase29](Phase29.md) |
| Reports | UC91 — Báo cáo khách hàng | NOT_STARTED | Customer/beauty/orders có; [evidence](#backend-reports) | Approved customer report metrics/filters/dashboard | System §3.4 / UC91; [Phase29](Phase29.md) |

### 5.1. System requirements §2.2

SYS-xx là ID nội bộ theo STT gốc32 dòng, không thay UC numbering. Evidence chung: §6–10 và nguồn ghi tại từng module; ownership cross-cutting có thể nhiều Phase nhưng từng task domain phải có một owner.

| ID | Requirement | Status | Existing / Missing Evidence | Phase ownership |
|---|---|---|---|---|
| SYS-01 | Đăng ký tài khoản | PARTIAL | Register có nhưng hash/unique phone/UI thiếu | 02 |
| SYS-02 | Đăng nhập | BROKEN | Plaintext check không tạo principal/session | 02,04 |
| SYS-03 | Đăng xuất | NOT_STARTED | Không có cơ chế invalidate | 02 |
| SYS-04 | Khôi phục mật khẩu | NOT_STARTED | Không service/verification/delivery | 03 |
| SYS-05 | Quản lý phiên đăng nhập | NOT_STARTED | Không session/token; logout/expiry không tồn tại | 02,04 |
| SYS-06 | Phân quyền theo vai trò | NOT_STARTED | Role tables không enforcement | 04 |
| SYS-07 | Phân quyền theo chi nhánh | NOT_STARTED | EmployeeStore tables không scope checks | 04 và mỗi staff workflow |
| SYS-08 | Quản lý dữ liệu đa chi nhánh | PARTIAL | Inventory/store/order FKs đã có; checkout/transfer scope còn sai | 08,09,14,16,19,21 |
| SYS-09 | Tự động sinh mã duy nhất | PARTIAL | GeneratedValue IDs đã có; D15 chốt mã hiển thị nếu cần | 13,16,19,21 |
| SYS-10 | Tự động ghi thời gian | PARTIAL | Nhiều CreationTimestamp/status times có; Inventory/Cart/WishList/Hold timestamps thiếu | 04,08,11,13,14,16,17,19,22 |
| SYS-11 | Kiểm tra dữ liệu đầu vào | PARTIAL | Có @Valid/annotations một phần; nested/list/positive/domain validation thiếu | 01 và mỗi domain Phase |
| SYS-12 | Tự động tính tổng đơn hàng | PARTIAL | Order/cart arithmetic có, quote/fee policy chưa thống nhất | 18,19 |
| SYS-13 | Kiểm tra voucher | PARTIAL | Date/active/min/quota có; targets/FREESHIP/quota atomic còn thiếu | 18,19 |
| SYS-14 | Kiểm tra tồn khả dụng | BROKEN | Checkout không check và transfer dùng actual | 08,16,19,21 |
| SYS-15 | Giữ tồn cho đơn | PARTIAL | Order tạo holds; chưa đủ hàng/branch/locking | 08,19 |
| SYS-16 | Giải phóng hàng giữ | PARTIAL | Cancel release có; state/idempotent guards thiếu | 08,20,21 |
| SYS-17 | Đồng bộ biến động kho | PARTIAL | Purchase/transfer/complete/cancel có; consistency/returns thiếu | 08,14,16,19,20,22,23 |
| SYS-18 | Quản lý vòng đời đơn | BROKEN | updateStatus nhận mọi enum không previous-state guard | 19,20,21,22 |
| SYS-19 | Phân công cửa hàng đủ toàn đơn | BROKEN | Checkout first store hoặc null; chưa assignment workflow | 19,21 |
| SYS-20 | Thanh toán COD | PARTIAL | Order.paymentMethod đặt COD; checkout UI chưa có | 19 |
| SYS-21 | Quản lý thông tin giao hàng | PARTIAL | Address snapshot khi đặt; shipping update/status chưa có | 19,22 |
| SYS-22 | Gửi thông báo | NOT_STARTED | Không provider/adapter/events; D11 | 30;03 chỉ recovery delivery |
| SYS-23 | Tìm kiếm và lọc | NOT_STARTED | Không keyword/filter query nghiệp vụ | 05,09,10,13,15,21,25,27 |
| SYS-24 | Phân trang dữ liệu | NOT_STARTED | List trả toàn bộ; không Pageable/Page | Các Phase list;32 kiểm chứng |
| SYS-25 | Quản lý hình ảnh sản phẩm | NOT_STARTED | Chỉ entity/repo đọc ảnh; chưa CRUD/upload | 07,10 |
| SYS-26 | Hiển thị vị trí cửa hàng | NOT_STARTED | Coordinates có; không map/locator | 09,10 |
| SYS-27 | Nhật ký hệ thống | NOT_STARTED | AuditLog repo chưa dùng | 04 writer;27 read; từng workflow events |
| SYS-28 | Sao lưu dữ liệu định kỳ | NOT_STARTED | Không backup/runbook/schedule trong repo | 31 |
| SYS-29 | Phục hồi dữ liệu | NOT_STARTED | Không restore evidence/runbook | 31 |
| SYS-30 | Quản lý trạng thái thay hard delete | PARTIAL | Store/supplier/employee deactivate; product/category/brand hard delete | 04,05,06,09,13,25 |
| SYS-31 | Gợi ý theo Beauty Profile | NOT_STARTED | Profile data có nhưng matching không có | 12 |
| SYS-32 | Cảnh báo tồn thấp | BROKEN | Query actual≤minimum thay available≤minimum | 15 |

### 5.2. Quality requirements §2.3

NFR-xx giữ STT gốc24 dòng. Mục tiêu3s không được diễn giải thành SLA p95/tải production; chưa measurement là UNKNOWN.

| ID | Requirement | Status | Evidence / Verification Limit | Owner / Verification |
|---|---|---|---|---|
| NFR-01 | Giao diện rõ ràng, thống nhất | NOT_STARTED | Không templates/static hoặc MVC/UI app | 02,09–12,17,19–29;32 |
| NFR-02 | Quy trình mua hàng đơn giản | NOT_STARTED | Không screen Browser→API→UI end-to-end | 10,17–19;32 |
| NFR-03 | Thông báo lỗi rõ ràng | PARTIAL | GlobalExceptionHandler gom mọi Exception thành400/string message | 01 và các forms;32 |
| NFR-04 | Responsive | NOT_STARTED | Không frontend để kiểm chứng | Mỗi UI Phase;32 |
| NFR-05 | Chrome/Edge/Firefox/Safari | UNKNOWN | Chưa browser E2E; frontend chưa tồn tại | 32 |
| NFR-06 | Tìm kiếm khoảng3s điều kiện bình thường | UNKNOWN | Chưa search/benchmark; D13 dataset/load | 10 và32 |
| NFR-07 | Tải trang khoảng3s điều kiện bình thường | UNKNOWN | Không HTTP/page measurement; D13 | 32 |
| NFR-08 | Ảnh kích thước phù hợp | NOT_STARTED | Chưa provider/resize/delivery contract | 07,10;32 |
| NFR-09 | Mật khẩu không plaintext | BROKEN | register stores raw; employee hardcoded raw password | 02,04 |
| NFR-10 | HTTPS khi triển khai thực tế | UNKNOWN | Repo chỉ port8080; deployment/proxy chưa cung cấp | 31 |
| NFR-11 | Kiểm soát quyền truy cập | NOT_STARTED | Không Spring Security/config/principal/method protection | 02,04 và mọi workflow |
| NFR-12 | Bảo vệ dữ liệu giữa khách hàng | BROKEN | Raw IDs/default1; address/cart/order ownership gaps | 02,11,17,19,20,23,24 |
| NFR-13 | Không tồn khả dụng âm | BROKEN | Adjust nhận negative/below-held; transfer/checkout không đúng available | 08,14,16,19,22,23 |
| NFR-14 | Giá mới không đổi lịch sử đơn | PARTIAL | OrderItem/address/price snapshots đã có;1 unit happy test chưa price-change regression | 19,22,28;32 |
| NFR-15 | Giao dịch kho nhất quán | PARTIAL | Mutation paths có; checkedException/concurrency/retry/returns chưa bảo vệ | 08,14,16,19–23 |
| NFR-16 | Dữ liệu lịch sử không mất | PARTIAL | Một số deactivate có; catalog delete physical | 04–06,09,13,23 |
| NFR-17 | Sao lưu và phục hồi | NOT_STARTED | Chưa evidence backup/restored test DB | 31 |
| NFR-18 | Thêm cửa hàng không đổi kiến trúc lõi | PARTIAL | Store FK + Inventory(store,SKU) nền tảng hợp lý; workflow store selection thiếu | 09,19,21;32 |
| NFR-19 | Thêm SKU/attribute không sửa lõi | PARTIAL | ProductSku/Attribute/value model đã có; services/CRUD/group thiếu | 05,06,12 |
| NFR-20 | Có thể thêm phương thức thanh toán sau COD | UNKNOWN | Chưa checkout ổn định/architecture acceptance; không yêu cầu gateway v1 | 19;32 design review |
| NFR-21 | Mở rộng loại khuyến mãi | UNKNOWN | DiscountType có nhưng Program modelD10 chưa chốt | 18,26 |
| NFR-22 | Dễ bảo trì, phân lớp rõ | PARTIAL | REST/service/repository/DTO/mapper đã phân lớp; UI còn thiếu | Giữ architecture; mỗiPhase;32 |
| NFR-23 | Hạn chế lặp, tái sử dụng | PARTIAL | MapStruct/services có; ModelMapper bean unused, sharedvalidation/security/page chưa có | 01,04 và consumer Phases |
| NFR-24 | Ràng buộc toàn vẹn dữ liệu | PARTIAL | 37entities/mappings khởi tạo H2; nhiều constraints/FK/unique/atomic thiếu; PGUNKNOWN | Mỗi migration/workflow;32 |

### 5.3. Business rule traceability

63 QĐ dưới đây là tóm tắt; nguồn chính là nguyên văn §2.1.1–10 và §4.2. DONE ở snapshot/unique-ID/history là capability hẹp từ STATIC evidence; các consumer vẫn cần integration tests. Rule UNKNOWN chỉ vì policy chưa cụ thể, không tự tạo policy.

| Rule | Design requirement summary | Status | Owner / Gate / Regression |
|---|---|---|---|
| KH-QĐ1 | Active products only | PARTIAL | 10 |
| KH-QĐ2 | Stock theo SKU và branch available | PARTIAL | 08,09,10 |
| KH-QĐ3 | Email/phone hợp lệ và unique | PARTIAL | 02,11 |
| KH-QĐ4 | Required/format trước lưu | PARTIAL | 02,11 và forms |
| KH-QĐ5 | Beauty optional | PARTIAL | 12 |
| KH-QĐ6 | Match attributes, không AI | NOT_STARTED | 12 / D16 |
| KH-QĐ7 | Quantity>0; cart không hold | BROKEN | 17 |
| KH-QĐ8 | Voucher date/quota/conditions | PARTIAL | 18,19 |
| KH-QĐ9 | Tiền hàng−discount+shipping=total | PARTIAL | 18,19 / D06 |
| KH-QĐ10 | COD only v1 | PARTIAL | 19 |
| KH-QĐ11 | Chỉ own orders | BROKEN | 20 |
| KH-QĐ12 | Order lifecycle đúng trình tự | BROKEN | 21,22 |
| KH-QĐ13 | Customer cancel trước SHIPPING | BROKEN | 20 |
| KH-QĐ14 | Return theo LUNEA policy | UNKNOWN | 23 / D07 |
| KH-QĐ15 | Review khi completed order có sản phẩm | BROKEN | 24 |
| QLSP-QĐ1 | Unique ID, name/brand/category required | PARTIAL | 05,06 |
| QLSP-QĐ2 | Product edit không đổi order snapshots | DONE | 19 regression, giữ snapshots |
| QLSP-QĐ3 | Không hard delete transacted product/SKU | BROKEN | 05,06 |
| QLSP-QĐ4 | Giữ linked brand/category history | PARTIAL | 05; verify FK/deactivate behavior, không khẳng định đã mất history |
| QLSP-QĐ5 | Product nhiều SKU mã riêng | PARTIAL | 06 |
| QLSP-QĐ6 | Nhiều ảnh, có primary khi publish | NOT_STARTED | 07 |
| QLSP-QĐ7 | Hide review giữ content | PARTIAL | 24 |
| QLCH-QĐ1 | Store unique ID | DONE | 09 chỉ integration, D15 nếu cần displaycode |
| QLCH-QĐ2 | Name/address/phone/hours/status | PARTIAL | 09 / D05 |
| QLCH-QĐ3 | Inactive không nhận order, history giữ | PARTIAL | 09,19 |
| QLCH-QĐ4 | Store stock theo available SKU | PARTIAL | 09,10 |
| QLNH-QĐ1 | Supplier unique ID | DONE | 13 chỉ integration, không thêm code mặc định |
| QLNH-QĐ2 | Supplier history không bị xóa | DONE | 13 regression deactivate hiện có |
| QLNH-QĐ3 | Purchase supplier/store/SKU/qty required | PARTIAL | 13 |
| QLNH-QĐ4 | Chỉ DRAFT editable | PARTIAL | 13 |
| QLNH-QĐ5 | Confirm tăng actual đúng | PARTIAL | 14 |
| QLTK-QĐ1 | Inventory riêng store×SKU | DONE | 08 preserve unique pair |
| QLTK-QĐ2 | Receipt không tăng stock hai lần | PARTIAL | 14 |
| QLTK-QĐ3 | Adjust before/after/reason/actor/time | PARTIAL | 08 |
| QLTK-QĐ4 | Stocktake dẫn đến adjustment | NOT_STARTED | 15 |
| QLTK-QĐ5 | Transfer pending/transit/received movements | PARTIAL | 16 |
| QLTK-QĐ6 | Hold chỉ sau order thành công/branch xác định | BROKEN | 08,19 |
| QLTK-QĐ7 | Cancel/invalid order release holds | PARTIAL | 08,20,21 |
| QLTK-QĐ8 | available=actual−held; không âm | BROKEN | 08 và stock consumers |
| QLTK-QĐ9 | Low stock: available≤minimum | BROKEN | 15 |
| QLKH-QĐ1 | CSKH theo permission scope | NOT_STARTED | 04,25 |
| QLKH-QĐ2 | Lock account giữ customer/history | NOT_STARTED | 25 |
| QLKH-QĐ3 | Return gắn order/product/reason/state | PARTIAL | 23 / D07 |
| QLDH-QĐ1 | Lưu product/SKU/price/discount snapshots | DONE | 19 regression, không rewrite snapshot logic |
| QLDH-QĐ2 | Confirm valid receiving info/stock | BROKEN | 21 |
| QLDH-QĐ3 | Một branch đủ toàn bộ items | BROKEN | 19,21 |
| QLDH-QĐ4 | Legal order lifecycle | BROKEN | 21,22 |
| QLDH-QĐ5 | Cancel release toàn bộ holds | PARTIAL | 20,21 |
| QLDH-QĐ6 | Tổng từ snapshots−discount+shipping | PARTIAL | 18,19 / D06 |
| QLDH-QĐ7 | Return restock theo disposition thực tế | NOT_STARTED | 23 / D07 |
| QLKM-QĐ1 | Program dates/scope/status | NOT_STARTED | 26 / D10 |
| QLKM-QĐ2 | Không xóa program history | NOT_STARTED | 26 / D10 |
| QLKM-QĐ3 | Voucher fields tối thiểu | PARTIAL | 18 |
| QLKM-QĐ4 | Voucher date/quota/min/target eligibility | PARTIAL | 18 / D06,D10 |
| QLKM-QĐ5 | Discount cap, không âm hàng hóa | PARTIAL | 18 |
| QLNV-QĐ1 | Staff account identifier unique | PARTIAL | 04 |
| QLNV-QĐ2 | Deactivate giữ audit history | PARTIAL | 04 |
| QLNV-QĐ3 | Staff branch assignment | PARTIAL | 04 |
| QLNV-QĐ4 | Role AND branch scope | NOT_STARTED | 04 |
| QLNV-QĐ5 | Important admin actions actor/time/object/details | NOT_STARTED | 04 writer;27 read |
| BCTK-QĐ1 | Revenue chỉ COMPLETED | NOT_STARTED | 28 |
| BCTK-QĐ2 | Bestsellers từ completed quantities/time | NOT_STARTED | 28 |
| BCTK-QĐ3 | Promotion usage + sum discounts | NOT_STARTED | 26,29 |

## 6. Backend Audit

### Route inventory

Prefix dưới đây là literal annotation source; các suffix abbreviated phải đọc controller khi tiếp tục. Không có frontend trong repo gọi API này; không suy ra không có client ngoài repo. H2 context startup đã chạy, HTTP endpoint/permissions chưa được test.

| Controller evidence | Prefix | Existing operations | Contract / Gap |
|---|---|---|---|
| [AuthRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/AuthRestController.java) | `/api/auth` | POST /register; POST /login | CustomerResponse; @Valid, không session |
| [CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java) | `/api/customers` | GET list/{id}/addresses/beauty-profile; PUT profile/address/beauty; POST address; DELETE address | Arbitrary customer/address IDs |
| [EmployeeRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/EmployeeRestController.java) | `/api/employees` | POST; GET list/{id}; PUT {id}; DELETE {id} | Delete là deactivate; chưa RBAC |
| [ProductRestController.java](../../src/main/java/com/thinh/cosmetic/rest/catalog/ProductRestController.java) | `api/products` | POST; GET list/{id}; PUT {id}; DELETE {id} | Mapping source không leading /; basic CRUD |
| [CategoryRestController.java](../../src/main/java/com/thinh/cosmetic/rest/catalog/CategoryRestController.java) | `api/categories` | POST; GET list/{id}; PUT {id}; DELETE {id} | Basic CRUD, không hierarchy |
| [BrandRestController.java](../../src/main/java/com/thinh/cosmetic/rest/catalog/BrandRestController.java) | `api/brands` | POST; GET list/{id}; PUT {id}; DELETE {id} | Basic CRUD + parent field |
| [CartRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/CartRestController.java) | `/api/cart` | GET; POST /items; PUT/DELETE /items/{cartItemId}; DELETE cart | customerId default1 |
| [WishListRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/WishListRestController.java) | `/api/wishlist` | GET; POST/DELETE /products/{productId} | customerId default1 |
| [VoucherRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/VoucherRestController.java) | `/api/vouchers` | CRUD/deactivate; GET /code/{code}; GET /discount | BigDecimal discount; no atomic consume API |
| [OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java) | `/api/orders` | POST; GET list/{id}/customer/{customerId}; PUT /{id}/status; PUT /{id}/cancel | customerId default1 cho create/cancel |
| [StoreRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/StoreRestController.java) | `/api/stores` | POST; GET list/{id}; PUT {id}; DELETE {id} | Deactivate |
| [InventoryRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/InventoryRestController.java) | `/api/inventory` | GET /store/{storeId}; /sku/{skuId}; /low-stock; /available; POST /adjust | employeeId default1 |
| [StockTransferRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/StockTransferRestController.java) | `/api/stock-transfers` | POST; GET list/{id}; PUT /{id}/ship; PUT /{id}/receive | employeeId default1 |
| [SupplierRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/SupplierRestController.java) | `/api/suppliers` | POST; GET list/{id}; PUT {id}; DELETE {id} | Deactivate |
| [PurchaseOrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/PurchaseOrderRestController.java) | `/api/purchase-orders` | POST; GET list/{id}; PUT /{id}/confirm | employeeId default1 |
| [ReturnRestController.java](../../src/main/java/com/thinh/cosmetic/rest/returns/ReturnRestController.java) | `/api/returns` | POST; GET list/{id}/customer/{customerId}; PUT /{id}/status | employeeId default1 khi xử lý |
| [ReviewRestController.java](../../src/main/java/com/thinh/cosmetic/rest/review/ReviewRestController.java) | `/api/reviews` | POST; GET list/product/{productId}; PUT /{id}/status | customerId default1 khi tạo |

<a id="backend-auth"></a>

### Authentication/account

Chain: [AuthRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/AuthRestController.java) → [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java) → [AccountRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AccountRepository.java) / [CustomerRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/CustomerRepository.java) → [AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java) / [CustomerEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java).

Register check existsByEmail rồi tạo username=email/raw passwordHash/customer. Login findByEmail, equals raw password rồi find Customer profile; employee login không đi được qua customer-only branch. Account không status/unique email-phone/hash required theo design. [RegisterRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/RegisterRequest.java) min6/phone optional; [LoginRequest.java](../../src/main/java/com/thinh/cosmetic/domain/dto/request/account/LoginRequest.java) email-only. Không security context/session/token/logout/recovery. BROKEN password protection; authentication chưa end-to-end. Owner02–04, D01–03.

<a id="backend-employee"></a>

### Employee, role/permission, audit

Chain: [EmployeeRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/EmployeeRestController.java) → [EmployeeServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/EmployeeServiceImpl.java) → account/employee/role/employeeRole/employeeStore repositories → employee/account/store relations.

EmployeeRequest required email/password nhưng service dùng optional internalEmail, ignores password và đặt raw constant. Invalid role/store IDs bị skip; update delete links trước tạo lại; update phone không sync Account; deactivate chỉ Employee INACTIVE. Store link isPrimaryBranch chưa set trong builder. Role/permission tables chưa thành authorization.

[RolePermissionRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/RolePermissionRepository.java) dùng Long trong khi entity @IdClass(RolePermissionId) — latent defect, repo hiện unused nên không mô tả flow đã fail runtime. [AuditLogEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java) + [AutditLogRepository.java](../../src/main/java/com/thinh/cosmetic/repository/account/AutditLogRepository.java) unused; source performedBy/action/details/time thiếu design employee/object/IP. Owner04 writer,27 query;04 cũng employee CRUD correction và grant UI.

<a id="backend-catalog"></a>

### Product/category/brand/SKU/attribute/images

Chains: rest/catalog controllers → product/category/brand ServiceImpl → repositories → entities → mapper/request/response. [ProductServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/ProductServiceImpl.java), [CategoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/CategoryServiceImpl.java), [BrandServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/catalog/impl/BrandServiceImpl.java) có create/list/detail/update/delete, nhưng delete physical; no search/filter/Pageable/public active filter. Catalog DTO constraints gần như trống dù @Valid.

[ProductResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/catalog/ProductResponse.java) không SKU/images/prices/stock. [ProductMapper.java](../../src/main/java/com/thinh/cosmetic/mapper/catalog/ProductMapper.java) update SET_TO_NULL cho scalar trong khi service giữ brand/category nếu IDnull; Brand mapper IGNORE null và Category mapper default null behavior khác. Đây là PUT/null contract cần ghi quyết định, không auto gọi toàn bộ update là bug.

ProductSku/SkuAttributeValue/Attribute/ProductImage entities + repositories có; SKU/image đọc bởi cart/order. Chưa SKU/attribute/image CRUD service/API/admin UI. AttributeRepository và SkuAttributeValueRepository chưa consumer. Owner05–07; public catalog10; recommendation12; D04/D16.

<a id="backend-customer"></a>

### Customer/profile/address/beauty/wishlist và CSKH

[CustomerRestController.java](../../src/main/java/com/thinh/cosmetic/rest/account/CustomerRestController.java) → [CustomerServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/CustomerServiceImpl.java) → customer/address/beauty repositories + MapStruct. Profile read/update/list; address CRUD; optional beauty upsert/read có. Phone update không unique check, CustomerResponse omits phone. update/delete address findById mà không verify thuộc customerId. Create default clear others; update default không clear; deletion default chưa policy D05.

[WishListRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/WishListRestController.java) → [WishListServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/WishListServiceImpl.java) → wishlist/item/product/image/SKU repos. Add idempotent/remove đúng product association nhưng price luôn BigDecimal.ZERO; getOrCreate invalid customer có thể customer null. No rule-based recommendation, CSKH search/lock/support history. Owner11–12,25; return CSKH reuse23.

<a id="backend-store"></a>

### Store

[StoreRestController.java](../../src/main/java/com/thinh/cosmetic/rest/store/StoreRestController.java) → [StoreServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/StoreServiceImpl.java) → StoreRepository/StoreEntity + StoreMapper. CRUD/deactivate hiện có; requests chỉ validate name cơ bản, lists unfiltered. Hours chuỗi thay2TIME; Double coords thayDECIMAL; address/phone/status/hours required theo design chưa đủ. No maps provider/config/locator. Inactive exclusion phải kết nối checkout19, không chỉ đổi Store.status. Owner09, public availability10;D05.

<a id="backend-inventory"></a>

### Inventory và transfer

Inventory controller → [InventoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java) → [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java) → Inventory/InventoryAdjustment + manual response. Available=max(0,actual−held); adjust log before/after/reason/employee rồi set actual, không reject âm hoặc actual<held; employee lookup có thể null. Query low-stock dùng actual≤minimum, lệch QLTK-QĐ9 available≤minimum. Không @Lock/@Version. Inventory test2 cases không concurrency proof. Owner08; count/low/export15.

Transfer controller → [StockTransferServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/StockTransferServiceImpl.java) → transfer/items/store/SKU/inventory repos. Create PENDING, ship chỉ PENDING và trừ source actual, receive chỉ IN_TRANSIT và cộng destination actual (create inventory nếu chưa có). Sequential repeat guards tốt cần giữ. Ship kiểm tra actual thay available, no source≠destination/positive/active guards; later checked exception và concurrency có nguy cơ một phần dữ liệu. Owner16; no stocktake/export hiện tại.

<a id="backend-purchase"></a>

### Supplier/purchase

Supplier controller → [SupplierServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/SupplierServiceImpl.java) → SupplierRepository/mapper/entity: CRUD/deactivate; không keyword/page/full validation.

Purchase controller → [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java) → supplier/store/employee/SKU/purchase/items/inventory repositories. DRAFT creation + totals + snapshots cost/items có; confirmDRAFT addsstock→CONFIRMED có sequential guard. Chưa draft edit/search/time/supplier/SKU filters/role branch scope. Collection items thiếu validation cascade/positive; save header trước validate tất cả detail dẫn đến rollback risk. Owner13 DRAFT,14 confirm/history/receipt; không tạo second stock receipt path.

<a id="backend-cart"></a>

### Cart

[CartRestController.java](../../src/main/java/com/thinh/cosmetic/rest/cart/CartRestController.java) → [CartServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/cart/impl/CartServiceImpl.java) → cart/items/customer/SKU/image repositories. Get-or-create/add merge/update/remove/clear/line/cart totals và primary image lookup đã có. getOrCreate có write khi GET, invalid customer có thể tạo null relation. update/remove item chỉ dùng itemId không verify thuộc cart; sourceqty≤0 deletes; designqty>0 và riêngremove operation. No inactiveSKU/positive/concurrentmerge guards. Giữ arithmetic/merge đã có; sửa boundary/ownership và screen. Owner17.

<a id="backend-voucher"></a>

### Voucher/pricing/program

[VoucherRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/VoucherRestController.java) → [VoucherServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/VoucherServiceImpl.java) → VoucherRepository/entity/mapper. CRUD/deactivate + active/date/quota/minSubtotal/PERCENT/FIXED/max cap có;5unit cases pass. FREESHIP rơi vào else FIXED discount là lỗi ngữ nghĩa; no shipping-aware quote API, field bounds/dateorder/update code uniqueness chưa đủ, usedCount increment no lock. Programs/effectiveness/export không có. Owner18/26;D06/D10.

<a id="backend-order"></a>

### Order/checkout/fulfillment

[OrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/order/OrderRestController.java) → [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java) → customer/address/cart/voucher/store/order/items/hold/inventory repositories.

Flow hiện tại: loadcustomer/address/cart → subtotal current prices → apply voucher và tăngusedCount → first store hoặc null → save order COD/PENDING_CONFIRMATION +items/snapshots → tăngheld nếu tìm inventory → clearcart. Không address owner/activeSKU/actual available/branch đủ ALLitems. Nullprice→0 và missinginventory silent. Snapshots tên sản phẩm/variant/đơn giá/address/discount/totals là phần tốt giữ lại.

getById arbitrary ID, customer history rawcustomerId; statusupdate bất kỳ enum. CONFIRMED sets time; COMPLETED giảmactual/held clamp0; cancel releasesHELD. customer cancel có owner check nhưng chỉPENDING_CONFIRMATION, lệch trướcSHIPPING. No full transition/idempotent guards, branch assignment/print/shippingedit/search/page. Checked exceptions sau voucher/header/items/hold updates + no locking cần regression thực. Owners18–22; returnstock23.

<a id="backend-return"></a>

### Returns

[ReturnRestController.java](../../src/main/java/com/thinh/cosmetic/rest/returns/ReturnRestController.java) → [ReturnServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/returns/impl/ReturnServiceImpl.java) → return/order/orderItem/customer/employee repositories → ReturnRequest/ReturnItem. Header/items/list/get/status/processor time có; không owner/eligible status/item belongs order/positive qty/purchased qty/cumulative returns. Status tùyenum và invalidemployee có thể null; chưa refund/disposition/restock. D07 trước triển khai policy; owner23 (cả customer/CSKH/staff), reuse trong25.

<a id="backend-review"></a>

### Review

[ReviewRestController.java](../../src/main/java/com/thinh/cosmetic/rest/review/ReviewRestController.java) → [ReviewServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/review/impl/ReviewServiceImpl.java) → review/customer/product/order repositories → ReviewEntity. Create, getvisibleproduct/getall/moderate có. OrderId DTO @NotNull nhưng missingorder có thể vẫn lưu ordernull; no customer own/completed/item contains product. SourceFKOrder thay bảng130OrderItem. New reviewVISIBLE; publicVISIBLE filtering và moderation giữ lại. Owner24/D08.

<a id="backend-reports"></a>

### Reporting

Không reporting controller/service/DTO/query/export. Order, purchase, inventory và customer là dữ liệu nguồn hiện có, không phải reports đã triển khai. Revenue/bestsellers phải filterCOMPLETED theoBCTK1–2; promotion usage+discount theoBCTK3, scope/time/metric cần ghi rõ. Owners28–29, promotion26 và inventory15 được reuse.

### Validation, exceptions và transactions

[GlobalExceptionHandler.java](../../src/main/java/com/thinh/cosmetic/exception/GlobalExceptionHandler.java) bắt mọi Exception và trả400+raw message; no structured field errors/404/409/401/403 contract. Request nested lists chưa @Valid cascade ở purchase/transfer/returns; quantity chỉ @NotNull hoặc không positive, monetary/date bounds chưa đủ. Service class @Transactional không đồng nghĩa late checked Exception rollback. [Spring official rollback defaults](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html) xác nhận checked exceptions không mặc định rollback.

TECHNICAL RECOMMENDATION: introduce semantic error/validation contract và dùng đúng rollback/locking theo từng workflow; test late item failure và concurrent last-stock/last-voucher. Foundation01 cung cấp contract, domain phases sở hữu adoption/corrections; không sweep refactor toàn bộ.

## 7. Frontend Audit

Không có source frontend, nên tất cả8 screen NOT_STARTED, không screen nào DONE chỉ vì backend có endpoint. UI style là nền gần đen, điểm nhấn vàng/kem, headings serif/moon hero, shared header/footer. Responsive behavior cần triển khai/kiểm chứng theo NFR, không suy từ desktop mockup.

| Screen / Design evidence | Status | Fields / Layout / State in design | Source comparison / Owner |
|---|---|---|---|
| Home §5.3.1 / ui-image4 | NOT_STARTED | Header search/wishlist/account/cart count; categories, hero CTA, featured cards, out-of-stock badge; footer | No route/page/template/assets; Phase10/D01/D16 |
| Product list §5.3.2 / ui-image5 | NOT_STARTED | Brand/price/skin/needs left filters, reset/count/sort, product grid; text pagination/loadmore | No filter/query/page/UI; pagination not visible in image, reconcile text; Phase10 |
| Product detail §5.3.3 / ui-image6 | NOT_STARTED | Main image+thumbnails, SKU volume/price/stock/qty/AddCart; Info/Ingredients/Usage/Reviews; related | DTO lacks SKU/images/pricing/stock; text Buy button not shown in screenshot; Phase10/17/24 integration |
| Login §5.3.4 / ui-image7 + diagram3 | NOT_STARTED | Email or phone, password visibility, remember, forgot/register; moon side panel | Email-only/raw credentials/source noauth; diagram3 bypass conflict D02; Phase02/03 |
| Register §5.3.5 / ui-image8 | NOT_STARTED | Fullname/email/optionalphone/password8letters+numbers/confirm/terms; disabled submit invalid | DTO min6/no confirm/terms; D02; Phase02 |
| Cart §5.3.6 / ui-image9 | NOT_STARTED | Simplified checkout header/3step progress; qty±/remove/line totals; subtotal/discount/shipping later/CTA | Cart DTO/arithmetic exists, ownership broken; Phase17+pricing18 |
| Checkout §5.3.7 / ui-image10 | NOT_STARTED | Saved identity/address/change-account, recipient/phone/province/ward, shipping, COD, note, voucher, totals | Owner/stock/quote missing; no district UI but DTO requires district D05; fee30000 conflictD06; Phase19 |
| Success §5.3.8 / ui-image11 | NOT_STARTED | Order examplecode/status/COD unpaid/total/address/MyOrders/Continue | Source Longid/status/totals exists; displaycode D15 not new payment gateway; Phase19 |

Navigation ui-image1/2: products/brands/promotions/stores/blog, search results/account(profile/address/orders)/orders tabs all/processing/delivered/canceled. Full mockups thiếu cho account/orders/brands/search/admin; derive capability từ UC với approvedD01 layout. Không tự tạo CMS cho BlogD14. Header/footer links must be resolved/gated, không coi dead links là acceptable.

Templates/pages with missing controller: NONE vì templates chưa có. MVC controller without view: NONE vì không MVC pagecontroller. REST controller without view: expected, không defect. AJAX/fetch/API clients: NONE trong repo; future UI phải nối contract thật và handle empty/loading/error/permission states.

## 8. Database Audit

### Design vs Entity vs Deployed Database

37 @Entity tương ứng37 bảng thiết kế. Entity mapping đã tạo được H2 schema/context; không có SQL/migration/seed tracked. Primary config ddl-auto=update và PostgreSQL connection refused; deployed table/column/constraint/data/schema version đều UNKNOWN. Không khẳng định DB deployed giống entity.

SurrogateIDs + unique(store,SKU),unique(cart,SKU),unique(wishlist,product) là biểu diễn hợp lệ tương đương design composite, không phải lý do rewrite. Purchase/transfer/return/value pairs không có unique tương ứng cần xem dữ liệu thực trước thêm. ORM enumORDINAL trên một sốentity là migration risk, không auto đổi khi chưa map existing values.

| Design table (§4.1.2; OOXML table no.) | Entity evidence | Current mapping / Difference | Database implementation evidence | Owner / Decision |
|---|---|---|---|---|
| 109 — TaiKhoan | [AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java) | AccountType/createdAt và IDENTITY đã có; username/hash/email/phone chưa có constraints thiết kế; thiếu account status | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 02,04; D01,D02 |
| 110 — KhachHang | [CustomerEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerEntity.java) | Account one-to-one; loyaltyPoints/joinDate đã có; fullName NOT NULL và nonnegative points chưa bảo vệ; LocalDate joinDate khác DATETIME | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 02,11; D04 nếu đổi thời gian legacy |
| 111 — DiaChiKhachHang | [CustomerAddressEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/CustomerAddressEntity.java) | Recipient/phone/street/ward/district/city/default và customer FK đã có; ownership/default xử lý service chưa đúng | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 11; D05 |
| 112 — BeautyProfile | [BeautyProfileEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/BeautyProfileEntity.java) | Customer one-to-one và profile fields đã có; vocabulary/rule matching chưa có | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 12; D16 |
| 113 — ThuongHieu | [BrandEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/BrandEntity.java) | Name/description/status và extra parent có; hierarchy khác vị trí trong design; status ORDINAL cần audit trước migrate | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 05; D04 |
| 114 — DanhMuc | [CategoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/CategoryEntity.java) | Basic category có; design MaDMCha self-parent không có trong entity | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 05; D04 |
| 115 — SanPham | [ProductEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductEntity.java) | Brand/category FKs, text/status/createdAt có; required/length/text dài thiếu; design MaSP là PK không mặc định business-code mới | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 05; D04,D15 |
| 116 — SKU | [ProductSkuEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductSkuEntity.java) | SKU/product/sale price/status có; thiếu listPrice/barcode; extra shade/volume phải bảo toàn nếu đang dùng | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 06; D04 |
| 117 — ThuocTinh | [AttributeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/AttributeEntity.java) | Name/extra description có; thiếu group; name uniqueness source stricter cần đối chiếu | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 05; D04,D16 |
| 118 — GiaTriThuocTinhSKU | [SkuAttributeValueEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/SkuAttributeValueEntity.java) | Surrogate id và SKU/attribute/value có; thiếu unique(SKU,attribute) theo design pair | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 06; D04 |
| 119 — HinhAnhSanPham | [ProductImageEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/catalog/ProductImageEntity.java) | Product/url/primary/order có; thiếu optional SKU FK; URL mặc định255 khác design500 | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 07; D04 |
| 120 — CuaHang | [StoreEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StoreEntity.java) | ID/name/address/phone/status có; hours String khác hai TIME; coordinates Double khác DECIMAL; required info chưa đủ | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 09; D05 |
| 121 — TonKho | [InventoryEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryEntity.java) | Surrogate id + unique(store,SKU) tương đương design composite; actual/held/minimum có; thiếu updatedAt/invariant constraints | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 08; NONE cho giữ PK tương đương |
| 122 — GioHang | [CartEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/CartEntity.java) | Customer/cart relation có; thiếu updatedAt theo bảng122 | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 17; NONE |
| 123 — ChiTietGioHang | [CartItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/CartItemEntity.java) | Surrogate id + unique(cart,SKU) hợp lệ; quantity cần positive và owner tại service | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 17; NONE cho giữ PK tương đương |
| 124 — Voucher | [VoucherEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/VoucherEntity.java) | Code/type/value/cap/min/date/quota/status có; thiếu đủ nonnegative/date bounds và usage atomicity | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 18; D06,D10 cho eligibility mở rộng |
| 125 — DonHang | [OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java) | Customer/store/voucher, split address snapshots, totals/COD/status/timestamps có; required receiving info và nonnegative constraints thiếu | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 19,21,22; D05,D06,D15 |
| 126 — ChiTietDonHang | [OrderItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderItemEntity.java) | Order/SKU/name/variant/price/qty/line total snapshot có; quantity/amount bounds chưa đủ | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 19; NONE |
| 127 — GiuTonDonHang | [OrderStockHoldEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderStockHoldEntity.java) | Order/store/SKU/qty/status/createdAt (Ngày giữ) có; thiếu releasedAt và qty invariant; chưa có locking mechanism. Field releasedAt migration do20 sở hữu;08 contract,19/22 tích hợp. | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 08,19,20,22; NONE |
| 128 — YeuThich | [WishListEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/WishListEntity.java) | Customer/wishlist có; thiếu createdAt bảng128 | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 11; NONE |
| 129 — ChiTietYeuThich | [WishListItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/cart/WishListItemEntity.java) | Surrogate id + unique(wishlist,product) tương đương composite; add/remove hiện có | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 11; NONE cho giữ PK tương đương |
| 130 — DanhGia | [ReviewEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/review/ReviewEntity.java) | Product/customer/rating/content/moderation/time có; FK Order thay OrderItem; long content giới hạn mặc định | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 24; D08 |
| 131 — YeuCauTraHang | [ReturnRequestEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnRequestEntity.java) | Order/reason/status/date/processor có; customer chỉ suy qua order, không explicit FK; thiếu description | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 23; D07; explicit customer FK equivalence cần quyết định |
| 132 — ChiTietTraHang | [ReturnItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/returns/ReturnItemEntity.java) | Surrogate id/return/orderItem/qty/reason có; chưa unique pair/positive/member validation | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 23; D07 |
| 133 — NhaCungCap | [SupplierEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/purchase/SupplierEntity.java) | Basic info/status có; no versioned schema/required format constraints đủ | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 13; NONE |
| 134 — PhieuNhap | [PurchaseOrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/purchase/PurchaseOrderEntity.java) | Supplier/store/creator/status/createdAt/confirmedAt/total có; invalid creator có thể null; amounts/rules cần bảo vệ | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 13,14; D15 |
| 135 — ChiTietPhieuNhap | [PurchaseOrderItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/purchase/PurchaseOrderItemEntity.java) | Surrogate id/receipt/SKU/qty/price/total có; design pair uniqueness và positive bounds chưa có | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 13,14; NONE cho giữ id khi thêm unique pair |
| 136 — DieuChinhTon | [InventoryAdjustmentEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/InventoryAdjustmentEntity.java) | Store/SKU/before/after/reason/actor/time có; actor có thể null, invariant dữ liệu không bảo vệ | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 08,15; NONE |
| 137 — DieuChuyenKho | [StockTransferEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StockTransferEntity.java) | Source/destination/status/creator/created/shipped/received times có; same-store/active/creator validity chưa đủ | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 16; D15 |
| 138 — ChiTietDieuChuyen | [StockTransferItemEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/store/StockTransferItemEntity.java) | Surrogate id/transfer/SKU/qty có; thiếu unique pair và positive bounds | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 16; NONE cho giữ id khi thêm unique pair |
| 139 — NhanVien | [EmployeeEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeEntity.java) | Account one-to-one, name/internalEmail/phone/status có; fullName required/account status và credentials update còn thiếu; status ORDINAL cần audit | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04; D02 |
| 140 — VaiTro | [RoleEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/RoleEntity.java) | Role/name/description entity/repo có; role capability runtime chưa nối authorization | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04; D01 |
| 141 — Quyen | [PermissionEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/PermissionEntity.java) | Permission/code/name/description có; repo chưa consumer; chưa enforcement | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04; D01 |
| 142 — NhanVienVaiTro | [EmployeeRoleEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeRoleEntity.java) | Composite EmployeeRoleId và links có; role IDs invalid bị service bỏ qua | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04; NONE |
| 143 — VaiTroQuyen | [RolePermissionEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/RolePermissionEntity.java) | Composite RolePermissionId đúng; repository khai báo Long sai contract, hiện unused | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04; NONE |
| 144 — NhanVienCuaHang | [EmployeeStoreEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/EmployeeStoreEntity.java) | Composite EmployeeStoreId/store assignment có; scope chưa enforced; primary semantics cần verify service | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04; D01 |
| 145 — NhatKyThaoTac | [AuditLogEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AuditLogEntity.java) | performedBy String/action/details/time có; thiếu Employee FK/object type/id/IP; long content/required action thiếu; repo unused | H2 mapping startup PASS; PostgreSQL/schema deployed UNKNOWN | 04,27; D01 cho actor/scope layout |

PromotionProgram xuất hiện ở system-image24/UC76 nhưng không trong37table/schema/source. Support historyUC64 cũng thiếumodel; không thêm bảng do AI đoán. D09/D10 xử lý trước phases tương ứng.

### Migration safeguards

- TECHNICAL RECOMMENDATION: versioned migration phù hợp PostgreSQL hiện tại; tool/SQL strategy chọn trong01, không thêm dependency trong đợt tài liệu.
- Kiểm tra null/duplicate/orphan/invalid enums/negative qty/held>actual/oversubscribed voucher trước constraints.
- Bảo toàn order/product/price/address snapshots và historicalpurchase/transfer/return/audit.
- Review legacy nhiều order lines phải D08 mapping/reconcile; không arbitraryline, không delete để vượt FK.
- Constraint/index cần được chứng minh bởi access pattern/uniqueness/invariant; không premature tuning.
- Backup trước migration và test restore riêng theo31; no reset primary DB/no convert allPKs.

## 9. Security Audit

| Area | Status | Evidence / Gap | Owner |
|---|---|---|---|
| Authentication credentials | BROKEN | Rawpassword register/login; employee constant raw password; customer-only login branch |02,04|
| Session/token/logout/expiry | NOT_STARTED | No securitydependency/config/principal/token/session invalidation |02,04 D01|
| Account status | NOT_STARTED | Account.status absent; Employee.status inactive không bảo vệ login/session |02,04,25|
| Authorization role | NOT_STARTED | Role tables/repo không checks; no route/methodprotection |04|
| Branch scope | NOT_STARTED | EmployeeStore data không được enforce; raw employee IDs/default1 |04 và workflow|
| Customer ownership | BROKEN | Arbitraryids, address/cart item/order detail thiếuowner |11,17,19–20,23–24|
| Password protection | BROKEN | passwordHash tên field nhưng chứa plaintext; no encoder |02,04|
| Inputvalidation/error disclosure | PARTIAL | @Valid một phần; raw exceptionmessage, no typed errors/cascade/bounds |01 +domain|
| Cookie/CSRF/CORS/token storage | UNKNOWN | Auth/UI undecided và implementation absent; requirements phụ thuộcD01 |02,04,32|
| HTTPS/deployedsecrets | UNKNOWN | Only port8080/devcredentials tracked; deployment/proxy không cung cấp |31|

Do not infer JWT from sample checklist/user prompt. SauD01, route auth strategy phải tương thích chosenUI. Public read catalogs/reviews/store locator có thể public khi active/VISIBLE; mutation/admin/customer-private đều cần approvedrole/principal/owner/scope. Permission matrix from10actors + UC nằm trong04, consumer phases phải reuse; staff không bị giới hạn sai như customer và customer không dùngstaffID.

## 10. Integration Audit

| Integration | Status | Evidence / Remaining |
|---|---|---|
| PostgreSQL16 | UNKNOWN | Config+Docker service có; refused duringaudit; Phase01 verify realstartup/schema |
| H2 test integration | DONE | 9/9 override tests/context pass; giới hạn như§1, không primaryDB |
| Cloudinary/upload | NOT_STARTED | UI/catalogdesign và system-image15 cần image storage; source noadapter/key/upload;07 |
| Google Maps | NOT_STARTED | system-image16 và Storecoords; no locator/provider;09/D05 |
| Email/OTP recovery | NOT_STARTED | UC03/diagram13; noadapter/config;03/D03 |
| Account/order notifications | NOT_STARTED | SYS22; noevents/provider;30/D11 |
| Shipping provider | UNKNOWN | Actor trongdiagram20; UCshipping update nhưng no definedprovidercontract;22 phảichốt cần integration haymanualflow |
| Onlinepayment | UNKNOWN | Survey mentions gateway; functionalCODonly; không thêm v1gateway (D14) |
| AJAX/API frontend | NOT_STARTED | All17REST available source, nofrontendcalls; UIphases attach |
| Backup/restore | NOT_STARTED | DBnamedvolume không phải backup/restore verification;31/D12 |

TECHNICAL RECOMMENDATION: provider failures phải có error contract/compensation phù hợp imageupload hoặc notification delivery; không chọn broker, paymentplatform, externaldatabase hoặc service framework ngoài thiết kế.

## 11. Broken / Suspicious Code

| ID | Severity | Concrete evidence / Impact | Owner / Treatment |
|---|---|---|---|
| B01 | HIGH | AccountServiceImpl stores/equals raw credentials |02/04 hash+principal |
| B02 | HIGH | Employee create ignores validated email/password; default password; invalidroles/stores silentlyskip |04; reconcile DTO/identity |
| B03 | HIGH | Không có security; customerId/employeeId mặc định1; address/cartitem/order getById IDOR |02/04+domainownership |
| B04 | HIGH | Checkout firststore/null, noallSKUavailable, missinginventorysilent |19/21 |
| B05 | HIGH | Quantity/money/datebounds and @Validnesteditems absent; negative/belowheldadjust accepted |08/13–19/23 |
| B06 | HIGH | CheckedException late afterheader/earlieritems/voucher mutations with @Transactional default |01contract+14/16/19/23 regressions |
| B07 | HIGH | No lock/version onstock/quota/status paths, repeatedcompletion canmutateagain |08/14/16/18–23 |
| B08 | HIGH | Order arbitraryenum transitions and pending-onlycustomer cancel mismatch |20–22 |
| B09 | HIGH | Returnitem not checked againstorder/owner/remainingqty; no restockdisposition |23/D07 |
| B10 | HIGH | Reviewpurchase check absent, optionalOrderaccepted, wrongdesignFK |24/D08 |
| B11 | MEDIUM | Lowstockactual metric wrong; clampavailable masksbadstoreddata |08/15 |
| B12 | MEDIUM | FREESHIPtreatedFIXED; feesource500kfreethresholdvs30kmockup; noquotareversepolicy |18/D06 |
| B13 | MEDIUM | WishlistresponsepriceZERO; invalidcustomergetOrCreateNULL |11 |
| B14 | MEDIUM | updateDefault addressdoesnotclearothers; delete/updateignorecustomerId |11/D05 |
| B15 | MEDIUM | Catalogphysicaldeletes riskhistory/linkedFK; noactivepublicfilter |05/06/10 |
| B16 | MEDIUM | RolePermissionRepository IDLong vsRolePermissionId unused latentdefect |04 |
| B17 | MEDIUM | JDK25TypeTagfailure/Lombokprocessor1.18.36vsdependency1.18.46 |01;Java21baselinefirst |
| B18 | MEDIUM | Allerrors400/rawmessage; Product/Categoryupdate nullsemantics inconsistent |01/05contractdecision, no blanketrewrite |
| B19 | MEDIUM | Audit fields notdesign; repounused; typoAutditLogRepository |04/27;renameonlyifnecessarywithreferences |
| B20 | LOW | MapperConfig ModelMapperbeanLOOSE unused, MapStructactuallyused |Do not refactor unlessPhase01need |
| B21 | LOW | Duplicatewebstarters, READMEclaimsnoimplementation/staleworkallocation |01/32docs/configverification |
| B22 | MEDIUM | @Data onJPArelations canrecursiveequals/toString/lazyaccess; no failureproven |Suspicious only;fixwhereactualregressionnecessitates |
| B23 | MEDIUM | Default255 onlongdescription/review/audit/URLs; missingunique/NOTNULL/checks |Modulemigrationowners |

### Unused/unfinished references, TODO and comments

Reference search found unused consumers: AutditLogRepository, PermissionRepository, RolePermissionRepository, AttributeRepository, SkuAttributeValueRepository và AccountResponse DTO. Đây là preparedstructure, không completedfunction. Tất cả8mapperfiles có consumers; ModelMapper chỉconfig. No largeTODO/FIXME backlog hoặc commented-out implementation được tìm thấy; có inline comments như defaultpassword/firststore/simplifiedworkflows giúp nhận diện dởdang. Không xóa classes vì unused nếu futureusecase đã cần.

Noorphanpage/template vìfrontendabsent; noREST-to-view requirement. API chưa đượcfrontendgọi làintegrationgap, không có bằng chứng backendAPIunusedoutside repo.

## 12. Design vs Implementation Differences

| Area | Design | Current Implementation | Difference | Recommended Decision |
|---|---|---|---|---|
| UI technology |8customermockups, noframeworkspecified|RESTbackendonly; repoarchitecturementionsfutureThymeleaf/session|Sourcefrontendabsent; docsupplementnotcurrentstack|D01;TECHNICAL RECOMMENDATIONThymeleaf/session chưađượcchọn|
| Login |emailorphone UI, UC01 allaccountactors|email-only andCustomerResponseonly|Staffcan'tlogin; noactualsession; diagramcustombypasscontradiction|D02 reconcileUI/diagram,02unifiedidentity;no rawIDbypass|
| Password/register |UI8lettersnumbers/confirm/terms, NFRhash|DTOmin6/noextra; plaintextstore|Ruleconflict+securitydefect|D02policy;hashmandatoryNFR09|
| Taxonomy |Categoryselfparent;Brandnoparent|Categoryflat;Brandparent|Relationplacedelsewhere|D04;don'tdeleteBrandlegacyautomatically|
| SKU/attributes |listPrice/barcode/group/genericvalues|extra shadevolume; missingfields/group/service|Schema/capabilitydiff|D04+D16 retainusedextra,addonlyapprovedmapping|
| Images |ProductandoptionalSKUFk,URL500,primary|Product-onlyurl255; nocontroller/upload|Data/flowgap|07mapping+provider, noarbitraryuploadinfra|
| Store |TIMEhours/DECIMALcoords/requiredinfo|Stringhours/Doublecoords|Representations+validationdiff|D05 migrateonlyverifiedlegacy|
| Address |Schema9fieldsincludingdistrict; UIprovinceward|DTOdistrictrequired|Internaldesignscreenvsdata gap|D05fieldcontract/defaultdeletepolicy|
| Shipping |Mockup30000evenlargesubtotal|Sourcefree>=500000|Feescompetingdefinitions|D06approvepolicy/examples,don'thardcodemockupdates|
| Checkoutbranch |OnebranchhasALLSKUavailable|firststoreornull|ViolatesQLDH3|19/21alignruleafterrecordingrecommendation|
| Lowstock |available<=minimum|actual<=minimum|Explicitmetricmismatch|15fixmetric;keep<=boundary|
| Cancellation |beforeSHIPPING|onlyPENDING_CONFIRMATION|Legalstatesmissing|20alignKH13, racewithshippingtest|
| Review |OrderItem FK +verifiedcompletedpurchase|OrderFKoptional/noeligibility|Schema/businessgap+legacyambiguity|D08;24newverifiedline,legacyreconcile|
| Return |policy+orderline/reason/status+disposition|basicheaderitems/setstatus|Nopolicyconstraints/restock/refund|D07;23nofixedwindowinvented|
| PromotionProgram |UC76+image24Program–Voucher|Schema37tablelacksProgram;sourceonlyVoucher|DesignvsDesignandDesignvsSource|D10approveProgrammodel,don'tmarkVoucherfulfillsUC76|
| Supporthistory |UC64|Nobackend/schema|Requirementunderspecified|D09define before25|
| Audit |Employee/object/id/details/IP|performedByString/action/details|Modelandconsumerabsent|04writer/27query;noindiscriminateauditschemarewrite|
| Documentcode |Uniqueautocodes;UIexampleLNcode|GeneratedValueLongIDs|Couldalreadyfulfilluniqueness;displayformatunknown|D15perdocument,nonewprefixdefault|
| Report metrics | BCTK yêu cầu COMPLETED, chưa rõ gross/net/shipping/date basis | Không có aggregates trong source | Công thức/nhãn và kỳ báo cáo cần chốt, không tạo net-accounting tự phát | D17 trước28–29; filter COMPLETED giữ bắt buộc |
| ShippingProvider | Diagram20 có actor, UC72 shipping update | Không provider/tracking contract | Chưa đủ đặc tả integration vs manual flow | D18 trước22; không tự thêm carrier/schema |
| Layoutadmin |UCstaffactions|NoAdminmockuporUI|Noexactvisualreferencetomatch|D01approveUIlayout;derivefunctionsfromUC|
| Blog/wholesale |Nav/surveymentions|NoUC/schemaimplementation|Scopenotfullyspecified|D14scopegate;noCMS/wholesaleautomatic|

Gate register: [Master §5](01_MASTER_ROADMAP.md#decision-register). Recommendations do not assert either source or design automatically correct where credible definitions compete; recorddecision thenimplement. Designconcreteinvariants(COD/stock/cancel/noAI/history) haveexplicitrequirements, notAIpreference.

## 13. Completed Features

Những capability hẹp sau được giữ, không lập task làm lại:

- Phân lớp REST/service/repository,17 service interfaces +implementations và các MapStruct consumers: STATIC.
- 37 entity/repository registrations và JPA context startup trên H2: UNIT/H2; PostgreSQL còn UNKNOWN.
- Basic catalog read/create/update và DTO mapping: STATIC; các Phase chỉ bổ sung validation, deactivate, search và UI.
- Store/Supplier deactivate thay delete; supplier history preservation: STATIC.
- Inventory(store,SKU), cart-item(cart,SKU), wishlist-item(wishlist,product) unique pairs với surrogate IDs: mapping hiện có phù hợp, không rewrite PK.
- Cart merge/arithmetic/primary-image lookup/clear và wishlist add idempotent/remove: STATIC; các lỗi owner/giá được ghi riêng.
- Voucher PERCENT/FIXED, cap/min/date checks: service hiện có và5 unit cases; không suy coverage toàn bộ voucher.
- Purchase DRAFT confirm guard và transfer PENDING ship/IN_TRANSIT receive guards: STATIC cho lần gọi tuần tự; bổ sung atomicity/concurrency thay vì viết lại.
- Order product/variant/price/address snapshots, totals, hold creation và clearcart happy path: STATIC +1 unit case; stock/owner/transaction gaps chưa hoàn thành.
- Review VISIBLE filter và hide giữ content: STATIC; bổ sung verified purchase/auth/UI.
- Inventory valid availability/adjust happy path có2 tests; giữ expectation đúng, sửa negative/clamp cases.

Không có module customer/admin end-to-end được chứng minh DONE. DONE cho một capability hoặc9 tests không có nghĩa project hoàn thành.

## 14. Partially Completed Features

Authentication registration/credential checks; employee CRUD/role-store links; catalog CRUD; store/supplier CRUD; customer profile/address/beauty/wishlist; cart; voucher; purchase confirmation; stock read/adjust/transfer; checkout/snapshots/holds; order history/status/cancel; return records; review/moderation.

Từng phần đã có, phần thiếu và owner được ghi ở §5–6. Không gom chúng thành Phase “hoàn thiện backend” và không phủ nhận implementation hiện hữu vì chưa có UI.

## 15. Missing Features

- Principal/session/logout/recovery; role/branch scope enforcement.
- SKU/attribute/image CRUD/upload, search/filter/sort/pagination, eight customer screens và các staff views theo UC.
- Rule-based recommendation, store locator, DRAFT edit, stocktake/export.
- Legal order transitions/assignment/shipping/print; return policy/disposition; verified OrderItem reviews.
- CSKH account lock/support UC64; PromotionProgram UC76; audit writer/query; reports.
- Account/order notifications, test isolation/versioned schema/runbook, backup/restore và HTTPS verification.

D09/D10 và các business-policy gates phải được giải quyết trước tạo model; không dùng một entity tự suy đoán để đánh dấu requirement hoàn thành.

## 16. Technical Debt

### HIGH

- Credential, authorization và ownership defects; checkout không đủ stock/branch; quantity và stock invariants bị vi phạm.
- Atomic mutations, concurrency và repeated status updates chưa an toàn.
- Constraints account/stock/detail chưa đủ; deployed schema UNKNOWN; ddl-auto=update không có versioned migrations.
- Test coverage thực PostgreSQL/HTTP/security/domain races còn thiếu.

### MEDIUM

- Mọi lỗi trả400/raw message; nested validation/text lengths/null-update contracts chưa thống nhất.
- FREESHIP/pricing conflict; audit model/unused repository; RolePermission generic ID latent defect.
- Review legacy mapping và enum ORDINAL migration risks cần data audit.
- JDK25 lỗi trong khi Java21 là baseline; Lombok dependency/processor lệch version.
- Dev credentials/config profiles cần externalization; tests hiện có chưa bao phủ owner, permission, late failure và retries.

### LOW

- Duplicate web starters; ModelMapper bean unused cạnh MapStruct; typo AutditLogRepository; README/tài liệu phân công cũ.
- @Data trên JPA relations là suspicious risk; chưa có failure được chứng minh nên chỉ sửa khi có regression/nhu cầu cụ thể.

TECHNICAL RECOMMENDATION: xử lý debt trong owner Phase của workflow; thêm migration/index/lock từ bằng chứng thực, giữ stack/architecture và tránh refactor lớn hoặc premature optimization.

## 17. Blocking Issues

| Blocker | Impact | Action / Owner |
|---|---|---|
| PostgreSQL connection refused | Không verify real startup/schema/data | Phase01 kết nối PostgreSQL16 hiện hữu và read-only schema inventory; không reset data |
| JDK25 khác verified Java21 | Default command compile fails | Phase01 chọn/document Java21; align processor chỉ khi cần |
| D01/D02 UI/auth | Chưa approved frontend/identity/register contract | Phase02/04 và UI gates; không cần giải quyết để tạo bộ tài liệu này |
| Identity/schema/legacy data | Account uniqueness/hash/status và migrations chưa an toàn | Phase01 inventory;02/04 identity;D04/D05 module mapping |
| Stock/ownership/transaction core | Checkout không thể coi hoàn thành | Phase08/11/17/18 trước19; workflow guards21–23 |
| D06 pricing/quota lifecycle | Quote/checkout/cancel policy chưa thống nhất | Phase18–22 ghi policy fee/FREESHIP/consume/restore/date/money |
| D07/D08/D09/D10 | Return/review legacy/support/program chưa đủ đặc tả | Phase23–26 scoped gates; các nhánh độc lập tiếp tục |
| D11/D12/D13 | Notification/backup/benchmark conditions chưa có | Phase30–32 quyết định và verification |
| D17/D18 | Report metric/time basis và shipping/provider contract chưa rõ | Phase22/28–30 không tự đặt fields/formula/provider |

Không chạy migration, đổi database hoặc viết application code để vượt blocker trong đợt tài liệu này. Phase preconditions/DoD/report giúp phiên sau xác minh trạng thái mới.

Xem [Master Roadmap](01_MASTER_ROADMAP.md) và [Final Checklist](FINAL_CHECKLIST.md).
