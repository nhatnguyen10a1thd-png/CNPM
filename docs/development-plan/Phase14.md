# Phase 14 — Xác nhận nhập kho và lịch sử phiếu nhập

Tài liệu nền: [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md).
Dependency: [Phase08](Phase08.md), [Phase13](Phase13.md). Complexity: MEDIUM. Risk: HIGH.
Trạng thái kế hoạch: `NOT_STARTED`. Nội dung dưới đây là việc phát triển tương lai, chưa phải capability đã bàn giao.

## 1. Objective

Xác nhận một phiếu DRAFT để cộng actual stock đúng một lần, giữ held stock và hiển thị lịch sử nhập có lọc/chi tiết.

## 2. Why This Phase Exists

confirm DRAFT → CONFIRMED và cộng stock đã tồn tại; thiếu bảo vệ gọi đồng thời, quyền, transaction cross-item và lịch sử có tìm kiếm. UC50 không được tạo lần cộng kho thứ hai cho cùng phiếu.

## 3. Current State

- PurchaseOrderServiceImpl.confirm kiểm tra DRAFT, cộng quantity mỗi dòng vào inventory, tạo inventory nếu thiếu, set confirmedAt/CONFIRMED.
- Guard hiện có chống lặp tuần tự; chưa đủ chứng minh hai request đồng thời không cùng thấy DRAFT.
- Inventory mới trong confirm hardcode minimumStock=5; phải dùng convention Phase08 thay vì coi số 5 là quy tắc thiết kế.
- GET /api/purchase-orders trả toàn bộ dữ liệu, chưa có lịch sử/filter/pagination; PUT /{id}/confirm tồn tại.
- Chưa có test nhập kho PostgreSQL; rollback/lock ngoài H2 chưa xác minh.
- Baseline audit đã đọc source; H2 có 9/9 test pass với Java 21. PostgreSQL runtime/schema và HTTP/UI chưa được xác minh. Agent phải kiểm tra lại sau các Phase phụ thuộc.

## 4. Preconditions

- [ ] Phase08 có primitive tăng actual và lock/invariant actual ≥ held ≥ 0.
- [ ] Phase13 có DRAFT valid, actor/scope và CRUD supplier giữ lịch sử.
- [ ] Contract confirmedAt/ID hiện có và migration policy Phase01 được xác nhận.
- [ ] Đã đọc report của các dependency; principal, quyền, contract lỗi và migration được dùng đúng phiên bản hiện hành.
- [ ] D01 đã chốt trước khi làm frontend; nếu gate liên quan chưa mở, ghi `BLOCKED`, không tự chọn công nghệ hay đánh dấu Phase hoàn thành.

## 5. Scope

### IN SCOPE

- Gia cố confirm nguyên tử và đúng một lần; ghi actor/audit xác nhận.
- Ghi nhận nhập kho theo UC50 gắn với phiếu xác nhận; dùng chung mutation, không cộng lại.
- Lịch sử phiếu nhập với status, khoảng thời gian, supplier/store và pagination; UI confirm/history.

### OUT OF SCOPE

- Lập/sửa DRAFT thuộc Phase13; không viết lại.
- Stock count/transfer/export kho thuộc Phase15/16; báo cáo nhập Phase29.

## 6. Requirements Covered

- §3.4.5 UC46, UC47 và §3.4.6 UC50; đây là Phase owner.
- QLNH-QĐ4–5: chỉ DRAFT được sửa/xác nhận, xác nhận làm tăng tồn thực tế.
- QLTK-QĐ2: ghi nhận nhập không cộng lặp sau phiếu đã xác nhận; §4.2 bất biến inventory.
- SYS10: confirmedAt/createdAt và audit phải phản ánh thao tác thực.
- Hai DOCX là nguồn yêu cầu; source là nguồn trạng thái implementation. UI quản trị chưa có mockup, dùng use case và layout được chốt tại D01.

## 7. Files To Inspect First

- [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java)
- [PurchaseOrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/PurchaseOrderRestController.java)
- [PurchaseOrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java)
- [PurchaseOrderItemRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderItemRepository.java)
- [InventoryRepository.java](../../src/main/java/com/thinh/cosmetic/repository/store/InventoryRepository.java)
- [PurchaseOrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/purchase/PurchaseOrderEntity.java)
- [PurchaseOrderResponse.java](../../src/main/java/com/thinh/cosmetic/domain/dto/response/purchase/PurchaseOrderResponse.java)
- [InventoryServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/store/impl/InventoryServiceImpl.java)
- Đọc thêm contract identity/RBAC/audit do Phase02/04 bàn giao và migration/transaction convention do Phase01 bàn giao.

## 8. Files Expected To Modify

- [PurchaseOrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/purchase/impl/PurchaseOrderServiceImpl.java)
- [PurchaseOrderRepository.java](../../src/main/java/com/thinh/cosmetic/repository/purchase/PurchaseOrderRepository.java)
- [PurchaseOrderRestController.java](../../src/main/java/com/thinh/cosmetic/rest/purchase/PurchaseOrderRestController.java)
- [PurchaseOrderService.java](../../src/main/java/com/thinh/cosmetic/service/purchase/PurchaseOrderService.java)
Đây là dự kiến. Chỉ sửa file còn thiếu hành vi sau khi kiểm tra trạng thái thật; giữ lại phần đang đúng.

## 9. Files Expected To Create

- PostgreSQL integration tests cho confirm rollback/concurrency; unit test bổ sung cho status guard và query lịch sử.
- UI xác nhận/lịch sử nhập theo D01; tạo DTO filter nếu cần, không tạo chứng từ nhập độc lập cộng stock.
Không tạo file UI theo framework giả định khi D01 chưa chốt; đường dẫn mới phải được ghi trong Completion Report.

## 10. Database Changes

NONE nếu primitive lock/constraint Phase08 và schema Phase13 đáp ứng. confirmedAt đã có; không thêm cột tương đương. Nếu cần định danh actor xác nhận riêng hoặc journal nhận hàng theo thiết kế chi tiết, ghi đề xuất migration/decision có bằng chứng trước khi làm; không đoán actor cho dữ liệu cũ.

## 11. Backend Tasks

- [ ] Giữ guard DRAFT và timestamp hiện có; khóa phiếu trước check và chuyển trạng thái trong cùng transaction với mọi dòng kho.
- [ ] Tái sử dụng tăng actual stock Phase08, held không đổi; khóa các dòng theo thứ tự ổn định và xử lý row chưa tồn tại không tạo duplicate.
- [ ] Kiểm tra actor quyền nhập tại receivingStore và supplier/store theo quy tắc giao dịch đã chốt.
- [ ] UC50 gọi lại nghiệp vụ confirm đã dùng cho UC46 hoặc chỉ đọc bằng chứng nhập; không thêm endpoint cộng quantity lần nữa.
- [ ] Kiểm tra toàn bộ dòng và invariant trước commit; lỗi dòng cuối rollback stock, status, timestamp, audit nghiệp vụ.
- [ ] Mở rộng getAll bằng filter/pagination server-side; giữ chi tiết phiếu confirmed và dữ liệu supplier lịch sử.
- [ ] Ghi audit thao tác confirm sau thành công trong transaction/convention Phase04; từ chối stale status bằng conflict.
- [ ] Không mặc định ngưỡng 5 cho inventory mới nếu Phase08 có config/mức tối thiểu được chốt.

## 12. Frontend Tasks

- [ ] Thêm confirm có xác nhận hành động, tóm tắt supplier/store/lines/total; disable trong lúc request.
- [ ] Hiển thị CONFIRMED cùng confirmedAt sau response thành công; lỗi không tự chuyển trạng thái UI.
- [ ] Tạo lịch sử theo thời gian/status/supplier/store, pagination và mở chi tiết tái dùng Phase13.
- [ ] Hiển thị đã nhận kho bằng dữ liệu persisted; không tạo nút nhập lại sau confirmed.

## 13. Validation & Business Rules

- DRAFT → CONFIRMED là một transaction; actual tăng bằng quantity của mỗi SKU, held không đổi.
- Confirmed không được sửa hoặc confirm thêm; concurrent confirm chỉ có một mutation thành công.
- Không cộng tồn cả UC46 và UC50 cho một phiếu; lịch sử phải truy vết phiếu gốc.
- Không đổi giá/tổng/người tạo khi xác nhận, không xóa lịch sử supplier inactive.

## 14. Security Requirements

- Nhân viên nhập hàng có permission confirm tại receivingStore; kho được ghi nhận nhận hàng theo ma trận Phase04.
- Lịch sử bị giới hạn scope; quyền báo cáo chuỗi không đồng nghĩa quyền confirm.
- Customer/anonymous không được dùng route /api/purchase-orders/{id}/confirm.
- Dùng principal và quyền/phạm vi từ Phase02/04; không tin ID người dùng hoặc nhân viên do client gửi, không duy trì default ID `1`.
- UI ẩn nút chưa đủ bảo vệ; controller/service phải kiểm tra quyền trước khi đọc hoặc ghi.

## 15. Error Handling

- ID thiếu → not found; không DRAFT → conflict; ngoài scope → permission error.
- Lỗi lock/concurrency phản hồi rõ trạng thái đã thay đổi; UI refresh, không tự retry mutation vô hạn.
- Bất biến kho/dòng không hợp lệ → rollback toàn thao tác, ghi nhận lỗi kỹ thuật theo Phase01.
- Dùng contract lỗi Phase01; không trả stack trace, SQL hoặc exception message nội bộ. Validation phải có field lỗi để UI giữ lại dữ liệu form.

## 16. Integration Points

- Dùng primitive Inventory Phase08; không triển khai mutation song song khác với checkout/transfer.
- Phase13 là nơi sửa DRAFT; Phase29 dùng lịch sử này cho báo cáo nhập.
- Journal/audit là bằng chứng UC50, không phải nguồn quantity độc lập.

## 17. Implementation Order

1. Kiểm tra DRAFT input/scope và primitive kho đã hoàn thành.
2. Thêm lock/order transaction và regression sequential/concurrent confirm.
3. Nối search/filter/pagination lịch sử và DTO đầy đủ.
4. Tạo confirm/history UI; kiểm thử trạng thái lỗi/stale.
5. Kiểm chứng PostgreSQL và ghi handoff exact-once semantics.

## 18. Verification

- [ ] Phiếu hai SKU confirmed tăng actual đúng quantity, held giữ nguyên.
- [ ] Confirm tuần tự và đồng thời không cộng lần hai; status/timestamp ổn định.
- [ ] Lỗi dòng sau rollback cả dòng trước, phiếu vẫn DRAFT.
- [ ] UC50 không thêm lần cộng tồn; history chỉ chứa scope/filter đúng.
- [ ] Chạy build/test với Java 21; chạy test giao dịch trên PostgreSQL test độc lập theo Phase01. H2/Mockito pass không thay thế kiểm chứng lock/constraint PostgreSQL.
- [ ] Đối chiếu HTTP request → controller → service → repository → dữ liệu và UI → route → response; ghi command, dataset, kết quả vào Completion Report.
- [ ] Chỉ chạy regression liên quan và baseline test; không chạy formatter/refactor ngoài scope.

## 19. Test Cases

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P14-01 | Happy confirm | DRAFT 2 SKU qty 3/4 | Actual tăng 3/4, held không đổi, CONFIRMED |
| P14-02 | Sequential duplicate | Confirm cùng ID hai lần | Lần hai conflict; kho tăng một lần |
| P14-03 | Concurrent duplicate | 2 transaction confirm cùng DRAFT | Một thành công; không double receipt |
| P14-04 | Rollback | Dòng 2 gặp lỗi inventory sau dòng 1 | Toàn kho/status/timestamp rollback |
| P14-05 | Permission | Confirm receivingStore ngoài scope | Bị từ chối, DRAFT không đổi |
| P14-06 | Invalid status/input | CONFIRMED hoặc legacy quantity âm | Từ chối; không cập nhật một phần |
| P14-07 | Empty history | Khoảng ngày không có phiếu | Page rỗng; filter được giữ |
| P14-08 | UC50 reuse | Ghi nhận nhận hàng đã confirmed | Không cộng quantity thêm |

## 20. Definition of Done

- [ ] UC46/47/50 có bằng chứng nhập liên kết đúng phiếu và không double-count.
- [ ] PostgreSQL kiểm chứng concurrency/rollback; guard tuần tự cũ được giữ.
- [ ] Filter/history/detail có scope, pagination và UI.
- [ ] ConfirmedAt/audit/actual/held khớp dataset kiểm chứng.
- [ ] Build/startup trên cấu hình test đã chọn thành công; không phá test/hành vi đã đúng của dependency.
- [ ] UI của scope chạy được với backend, có loading/empty/error, validation và quyền đúng.
- [ ] Migration nếu có đã kiểm thử trên dữ liệu mẫu hiện hữu; không xóa hoặc đoán dữ liệu lịch sử.
- [ ] Không còn gate mở ảnh hưởng DoD; checklist, report, API/route và deviation được cập nhật trung thực.

## 21. Expected Result After This Phase

Xác nhận nhập kho có transaction và bảo vệ gọi lặp; nhân viên xem lịch sử nhận hàng theo scope. Phiếu confirmed và tồn kho luôn khớp nhau.

## 22. Handoff To Next Phase

- Phase29 có nguồn nhập hàng confirmed theo store/supplier/thời gian.
- Phase15/16/19 dùng cùng primitive Inventory đã kiểm chứng; ghi thứ tự lock.
- Ghi route history/filter và semantics UC50, không để Agent sau thêm receipt cộng lặp.
- Đây là contract dự kiến sau khi Phase được kiểm chứng. Agent tiếp theo chỉ được giả định capability đã tồn tại khi report là `COMPLETED`.
- Ghi chính xác route/schema/DTO đã chọn, dữ liệu test và known issues; không để Phase sau suy đoán trạng thái.

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

Chưa thực hiện.

## Remaining Tasks

Chưa thực hiện.

## Verification Result

Chưa thực hiện.

## Notes For Next Phase

Chưa thực hiện.
