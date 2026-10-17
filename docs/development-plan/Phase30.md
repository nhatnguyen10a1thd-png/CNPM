# Phase 30 — Thông báo tài khoản và đơn

> Status: NOT_STARTED. Complexity: MEDIUM. Risk: MEDIUM.
> [Project Audit](00_PROJECT_AUDIT.md) · [Master Roadmap](01_MASTER_ROADMAP.md) · [Final Checklist](FINAL_CHECKLIST.md).
> Đây là kế hoạch phát triển phần còn thiếu; tài liệu này chưa thực hiện code, migration hoặc verification của Phase.

## 1. Objective

Bổ sung thông báo tài khoản và trạng thái đơn theo event/recipient/channel policy đã được xác nhận.

## 2. Why This Phase Exists

SYS-22 yêu cầu notifications nhưng source chưa có adapter/events/provider. Recovery03 có thể tạo delivery adapter cho xác minh; cần tái sử dụng khi phù hợp, không tự chọn email/SMS/inbox/broker hoặc để provider failure làm hỏng order transaction.

## 3. Current State

- Baseline không có email/OTP/notification library/config/service/template/event model.
- Sau03 có thể có recovery delivery adapter theo D03; phải đọc report để biết thực tế, không giả định SMTP đã có.
- Sau19/21/22 có checkout/order transitions; notification event sources phải dựa successful committed states.
- Không có notification mockup/admin inbox hoặc schema trong37 bảng; D11 quyết định channels/events/recipient/minimum retry.
- Account/order secrets, raw credentials và customer PII phải được giới hạn payload.

Trạng thái mô tả là baseline audit2026-10-07; phải đọc source và report các Phase trước để xác minh lại. Không coi file/class hiện có là bằng chứng hoàn tất toàn luồng.

## 4. Preconditions

- [ ] Phase03,19,22 COMPLETED, auth/recovery/order transition contracts có report.
- [ ] D11 chốt sự kiện/người nhận/kênh/message content/delivery failure/retry policy; D01 nếu có UI state. Payload shipping chỉ dùng fields đã được D18/Phase22 xác nhận.
- [ ] Có test adapter/provider fixture, config externalized; không gửi thông báo thật khi chạy tests.
- [ ] Ghi nhận gate áp dụng tại [Decision Register](01_MASTER_ROADMAP.md#decision-register); recommendation chưa phải quyết định được duyệt.
- [ ] Working tree và dữ liệu hiện hữu được kiểm tra; không reset DB để vượt migration.

## 5. Scope

### IN SCOPE

- Event matrix cho account và order notifications đúng SYS-22.
- Delivery adapter integration/reuse, payload/template tối thiểu, after-commit behavior.
- Failure/retry/dedup behavior theo approved D11; tests state transitions/delivery boundaries.
- UI feedback cần thiết nếu thiết kế chốt, không dựng một inbox mới mặc định.

### OUT OF SCOPE

- Order/return/recovery workflow xây lại; fields/phases owners đã hoàn thành phải reuse.
- Marketing campaign messaging, push/mobileapp/SMS/notificationcenter tự phát.
- Message broker/distributed events architecture, external platform mới không được chốt.
- Không biến mọi exception hoặc read request thành notification event.

## 6. Requirements Covered

- SYS-22 Gửi thông báo tài khoản/trạng thái đơn; NFR15/16/23 consistency/history/reuse.
- UC03 delivery reuse nếu cùng channel; UC21/24/69/73 là event sources integration, owners giữ domain behavior.
- D11 channel/events/recipients/content/delivery semantics; D03 tương thích adapter recovery.

Mỗi requirement trên có evidence tại Audit. Các integration points chỉ tái sử dụng capability owner khác, không nhận lại toàn bộ backlog của module đó.

## 7. Files To Inspect First

- [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java)
- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [AccountEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/account/AccountEntity.java)
- [OrderEntity.java](../../src/main/java/com/thinh/cosmetic/domain/entity/order/OrderEntity.java)
- [OrderStatus.java](../../src/main/java/com/thinh/cosmetic/domain/enums/OrderStatus.java)
- [pom.xml](../../pom.xml)
- [application.properties](../../src/main/resources/application.properties)
- Các delivery/principal/status components thực sự được Phase03/19/22 tạo (đọc Completion Reports).

Các file mới do Phase trước tạo phải đọc thêm theo Completion Report. Đường dẫn dự kiến chưa tồn tại không phải bằng chứng implementation.

## 8. Files Expected To Modify

- [AccountServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/account/impl/AccountServiceImpl.java)
- [OrderServiceImpl.java](../../src/main/java/com/thinh/cosmetic/service/order/impl/OrderServiceImpl.java)
- [application.properties](../../src/main/resources/application.properties)
- [pom.xml](../../pom.xml)
- Approved delivery adapter/template/config từ03 nếu phù hợp.

Danh sách là dự kiến; chỉ sửa file cần cho scope sau khi đọc source, không bắt buộc chạm mọi file.

## 9. Files Expected To Create

- Minimal notification payload/template/adapter orchestration theo D11 và packages hiện có.
- Provider testdouble và notification integration tests; không gửi thật.
- Persistent retry/dedup storage chỉ nếu D11 xác nhận cần; không mặc định bảng inbox/outbox/broker.

Tên/path mới là dự kiến, chưa tồn tại ở baseline; chỉ tạo sau gate cần thiết và theo conventions của repo.

## 10. Database Changes

NONE mặc định. Nếu D11 yêu cầu persistent delivery attempts/dedup, ghi schema tối thiểu, retention/PII/index/unique event identity và migration trước tạo. Đây là TECHNICAL RECOMMENDATION chỉ khi cần bảo đảm retry, không business requirement để tự thêm notification center. Không sửa order/account history hoặc tạo table do suy đoán channel.

## 11. Backend Tasks

- [ ] Ghi event→recipient→payload→channel→retry matrix D11; chỉ các events được yêu cầu.
- [ ] Đọc adapter03; reuse interface/config và safe verification semantics nếu cùng channel, không xây provider lần hai.
- [ ] Gắn event emission với committed successful account/order actions; failed/rejected transaction không báo thành công.
- [ ] Deliver sau domain commit bằng cơ chế tối thiểu phù hợp runtime; no network call được dùng để rollback successful checkout.
- [ ] Theo D11 deduplicate/retry theo event identity và approved policy; repeated status action không gửi duplicate unintended.
- [ ] Validate recipient/payload từ persisted owner/state, không browser-supplied account/employee IDs.
- [ ] Templates chỉ chứa cần thiết; không password/hash/OTP trong order notifications hoặc logs; verification03 giữ riêng protected flow.
- [ ] Handle provider unavailable/timeouts/config errors và ghi delivery result safe; không nuốt domain failure thành notification success.
- [ ] Record delivery observable result theo approved design, không expose public admin diagnostic endpoint.
- [ ] Test successful commit/rollback/provider failure/retry/repeated transition và wrong recipient.

## 12. Frontend Tasks

- [ ] Nếu D11 cần toast/message delivery state: reuse error/success states trong account/order UI.
- [ ] Không hiển thị 'đã gửi' trước provider result theo approved semantics; order created status độc lập delivery failure.
- [ ] Không tự tạo inbox/admin screen hoặc subscribe preferences ngoài scope.
- [ ] Nếu notification có links tới order/account: dùng real protected routes, không thêm raw-id access bypass.

## 13. Validation & Business Rules

- Only approved events; committed state authoritative.
- Recipient phải đúng account/customer/authorized staff theo D11, no cross-customer leak.
- Provider failure không làm stock/voucher/cart/order rollback sau domain commit.
- Retries/dedup/retention không magic values; chốt trong D11.
- No notification on failed transaction; no secrets in general payload/log.

## 14. Security Requirements

Provider keys externalized; tests dùng fake adapter. Notification links require existing authentication/ownership; không bearer link tự cấp quyền vào order khác. Diagnostic logs chỉ privileged operators theo04/31, không public endpoint. Channel-specific signature/verification chỉ sau quyết định provider.

## 15. Error Handling

- Provider unavailable/timeout: approved retry/result, domain transaction remains correct.
- Invalid/missing recipient: controlled delivery failure, không gửi tới fallback account1.
- Transaction rollback: không dispatch success notification.
- Duplicate event: approved dedup/result, không duplicate charge/stock/order updates.

## 16. Integration Points

- 03 recovery adapter;19/21/22 commit events;02/04 identity;31 config/secrets/deployment.
- 23 return notifications chỉ thêm nếu D11 event matrix thật yêu cầu, không tự mở scope.
- 32 E2E phải test fake delivery và domain consistency.

## 17. Implementation Order

1. Chốt D11 event matrix/channel/failure semantics.
2. Inspect03 adapter và domain commit boundaries.
3. Implement minimal after-commit orchestration/templates/dedup theo policy.
4. Wire UI feedback chỉ phần approved.
5. Run recipient/rollback/provider-failure/retry tests; document exact adapter/config/events.

## 18. Verification

- [ ] Build/tests JDK21, provider fixture không gửi external messages thật.
- [ ] Checkout/order state commit vẫn đúng khi delivery fail; rollback không emit success.
- [ ] Recipient/payload/event identities đúng, no secret logs/crosscustomer links.
- [ ] Repeated transitions/retries theo policy; config missing error không fake success.
- [ ] No new broker/inbox/platform ngoài approved scope.
- [ ] Ghi exact command/environment/result và giới hạn evidence trong Verification Result; không đổi UNKNOWN thành PASS bằng suy đoán.
- [ ] Kiểm tra git diff chỉ gồm scope Phase, không refactor hoặc nhiệm vụ Phase sau.
- [ ] Regression các capability đúng đã giữ; không tạo lại implementation đã hoàn thành.

## 19. Test Cases

Chạy các case phù hợp trên PostgreSQL/test environment có dữ liệu xác định; unit/H2 chỉ bổ trợ. Expected Result phụ thuộc gate phải được thay bằng quyết định đã ghi trước thực hiện.

| ID | Scenario | Input | Expected Result |
|---|---|---|---|
| P30-T01 | Happy delivery | Approved account/order event đã commit | Recipient và payload đúng; delivery result recorded |
| P30-T02 | Rollback | Late domain failure trướccommit | No success notification sent |
| P30-T03 | Provider failure | Adapter timeout/unavailable | Domain state unchanged correct, approved retry/result |
| P30-T04 | Recipient integrity | CustomerA order vs supplied recipientB | Recipient từ persisted owner, không gửi B |
| P30-T05 | Retry/repeated event | Same event delivered twice | Approved dedup/retry behavior; no duplicated domain writes |
| P30-T06 | Invalid input | Missing recipient/unsupported channel | Controlled failure, no fallback account1 |
| P30-T07 | Empty data | No pending approved events | No work/no error/no unsolicited messages |
| P30-T08 | Secret exposure | Inspect response/log/template data | No password/hash/provider secrets/verification secret leak |
| P30-T09 | Permission links | Recipient follows order link without auth/owner | Existing authorization enforced |

## 20. Definition of Done

- [ ] SYS22 approved event matrix/channel delivery thực với tests.
- [ ] No real messages in tests; provider/config/secrets externalized.
- [ ] Delivery failure/rollback/retry và recipient boundaries verified.
- [ ] 03 adapter reused when appropriate; không tạo unwanted inbox/broker/UI.
- [ ] Các checkbox phản ánh kết quả thật; không có gate mở chặn toàn scope hoặc known issue không được ghi.
- [ ] Completion Report có files/routes/schema/decisions/deviations/test evidence; Audit và Master được cập nhật nếu trạng thái/dependency đổi.
- [ ] Không phá chức năng của Phase trước; code giữ kiến trúc và conventions hiện tại.

## 21. Expected Result After This Phase

Có notification capability theo thiết kế được chốt, hoạt động độc lập đúng mức với successful domain transaction và không làm sai order/stock.

## 22. Handoff To Next Phase

- 31 có provider/channel config, credentials requirements và operational delivery behavior.
- 32 có fake adapter/event fixtures/recipient/failure/replay cases.
- Report ghi exact events/adapter/config/templates/schema nếu có, delivery retry/dedup policy được duyệt.

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
