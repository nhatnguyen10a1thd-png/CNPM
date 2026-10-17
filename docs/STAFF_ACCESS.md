# Staff access and employee API

The minimal test interface uses the existing Spring Boot REST application, same-origin static HTML/JavaScript and server sessions with CSRF. Staff identity comes from `CurrentAccountResolver`; request parameters never select the acting employee. `PermissionPolicy` reads current employee/account status, role grants and active store assignments from the database for each request. Permission changes apply to sessions that are already signed in.

## Role matrix

`PermissionCatalog` is the source used by the demo bootstrap. Roles are explicit bundles, with no wildcard permission fallback.

| Role | Permissions |
|---|---|
| ADMIN | All explicit codes in `PermissionCatalog.ALL`; global store access after the requested permission check |
| QLSP | CATALOG_READ, CATALOG_MANAGE, REVIEW_MANAGE |
| QLCH | STORE_READ, STORE_MANAGE |
| QLNH | SUPPLIER_READ, SUPPLIER_MANAGE, PURCHASE_READ, PURCHASE_MANAGE |
| QLTK | INVENTORY_READ, INVENTORY_MANAGE, TRANSFER_READ, TRANSFER_MANAGE |
| QLKH | CUSTOMER_READ, CUSTOMER_MANAGE, RETURN_MANAGE |
| QLDH | ORDER_READ, ORDER_MANAGE, RETURN_MANAGE |
| QLKM | PROMOTION_READ, PROMOTION_MANAGE |
| QLNV | EMPLOYEE_READ, EMPLOYEE_MANAGE |
| BCTK | REPORT_READ |

ADMIN additionally has ROLE_MANAGE and AUDIT_READ. Reserved ADMIN grants cannot be replaced, and ROLE_MANAGE cannot be assigned to another role through the grant API. The first ADMIN was created by a one-time bootstrap on Supabase. No employee, including that ADMIN, can create or assign a second ADMIN through the staff API. A QLNV manager can administer ordinary employees but cannot create, edit, deactivate or assign a privileged ADMIN/ROLE_MANAGE employee. Employees cannot change their own roles, scope or status, or deactivate themselves. Last-active-admin protection runs under a pessimistic lock on the ADMIN role so concurrent changes cannot remove all administrators.

Catalog reads remain public. Catalog commands require CATALOG_MANAGE. Suppliers and vouchers use their global read/manage permissions. Store updates/deactivation require STORE_MANAGE and the target store assignment; creation is reserved for ADMIN. Store listing for EMPLOYEE_MANAGE supports assignment selection across branches. Other STORE_READ staff see assigned stores only. Inventory operations require INVENTORY_READ/MANAGE plus the requested store assignment. Purchases require PURCHASE_READ/MANAGE plus the receiving store assignment. Transfers require TRANSFER_READ/MANAGE plus both source and destination assignments; list queries use the same bounds. Missing/null store scope denies access.

Reverified 2026-10-08: catalog mutations, supplier operations and voucher operations now enforce these same permissions on the managed Spring service beans as well as their existing controllers. Calling a service directly cannot bypass the controller check. Global data still has no branch requirement. Catalog reads remain public. This closes a controller-only enforcement gap without changing the role matrix, routes or business rules.

## Existing routes for consumer phases

| Route family | Existing methods | Permission on controller and service |
|---|---|---|
| `/api/products`, `/api/categories`, `/api/brands` | GET collection and `/{id}`; POST collection; PUT/DELETE `/{id}` | GET public; mutations CATALOG_MANAGE |
| `/api/suppliers` | GET/POST collection; GET/PUT/DELETE `/{id}` | GET SUPPLIER_READ; mutations SUPPLIER_MANAGE |
| `/api/vouchers` | GET/POST collection; GET/PUT/DELETE `/{id}`; GET `/code/{code}`; GET `/discount?code=&orderTotal=` | GET PROMOTION_READ; mutations PROMOTION_MANAGE |

There is no attribute or dedicated SKU CRUD route in this handoff. Catalog search, taxonomy, validation, history-safe deactivation and the consumer screens remain with Phase05+. The current DELETE implementations must be reviewed by those owners before claiming their business acceptance. `VoucherService.calculateDiscount` is currently a staff operation guarded by PROMOTION_READ; Phase18 must provide its customer quote/eligibility contract before enabling any customer pricing flow. Permission changes alone do not enable the denied customer APIs.

Customer, cart, wishlist, order, return and review routes remain denied by the security boundary pending their later ownership/business phases. Their old default actor ID of 1 has been removed. Their existing business logic has not been completed by Phase 04. Audit search and reports are also later phases; creating a writer or permission code does not enable those workflows.

## Employee endpoints

| Method and route | Access | Response |
|---|---|---|
| GET /api/employees?keyword=&page=0&size=10 | EMPLOYEE_READ or EMPLOYEE_MANAGE | Spring Page with content, totalElements, totalPages, number and size |
| GET /api/employees/{id} | EMPLOYEE_READ or EMPLOYEE_MANAGE | EmployeeResponse |
| POST /api/employees | EMPLOYEE_MANAGE | 201 EmployeeResponse |
| PUT /api/employees/{id} | EMPLOYEE_MANAGE | EmployeeResponse |
| DELETE /api/employees/{id} | EMPLOYEE_MANAGE | 204; deactivates account and employee, preserving links/history |
| GET /api/roles | EMPLOYEE_MANAGE or ROLE_MANAGE | RoleResponse array |
| GET /api/permissions | EMPLOYEE_MANAGE or ROLE_MANAGE | PermissionResponse array |
| PUT /api/roles/{id}/permissions | ROLE_MANAGE and ADMIN | RoleResponse |

POST/PUT use a complete employee form:

```json
{
  "fullName": "Test Staff",
  "email": "staff@example.test",
  "internalEmail": "staff.internal@example.test",
  "phone": null,
  "password": "UniquePassword123",
  "status": "ACTIVE",
  "roleIds": [2],
  "storeIds": [1],
  "primaryStoreId": 1
}
```

`email` is the account login email and username. `internalEmail` is an employee identifier; omission/blank defaults to email. Both identifiers are normalized to lowercase, and duplicate identifiers produce 409. Optional phone is normalized with the authentication policy (strip spaces, parentheses, dots and hyphens; 9–15 digits with optional plus) and synchronized to the account. Password is required on create and optional on update; omission keeps the previous hash. New passwords contain letters and numbers and have at least eight characters, with at most 72 UTF-8 bytes. No response includes a password or hash.

Role/store IDs must be distinct positive values and must all exist. Store assignments must be active. A nonempty store list requires exactly one `primaryStoreId` from that list; an empty list requires a null primary ID. Missing role/store arrays are treated as empty for the full-form update. Association validation completes before any account, profile or link change.

EmployeeResponse contains id, accountId, email, fullName, internalEmail, phone, status, roles, stores, roleIds, storeIds and primaryStoreId. Page size is 1–100 and page is nonnegative. Search matches full name, login/internal email and phone; wildcard characters in keywords are escaped.

RoleResponse contains id, name, description, permissionIds and permissions (codes). PermissionResponse contains id, code, name and description. Grant replacement accepts `{"permissionIds":[1,2]}` and validates the entire set before deleting old links. The caller cannot mutate a role currently assigned to them.

## Audit and transaction contract

`AuditWriter.write(action, objectType, objectId, details)` participates in an existing transaction with `Propagation.MANDATORY`. Employee creation/update/deactivation and role grant replacement write an audit event in the same transaction; an audit persistence failure rolls back the administrative mutation. Actor is the authenticated employee, IP is `request.getRemoteAddr()`; arbitrary forwarding headers are ignored. Details contain role/store/status or permission IDs, never credentials. Legacy `performedBy` remains intact for existing history; new events add an employee FK, object type/ID and IP. Other business phases must invoke the writer within their own transactions.

Account locking and password replacement increment credentialsVersion. The active-account filter rejects stale sessions on their next request. Role/store assignment changes are enforced from fresh database reads and do not require a logout/login cycle.
