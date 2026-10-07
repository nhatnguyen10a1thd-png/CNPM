package com.thinh.cosmetic.security;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Explicit permissions used by the existing actor model; no implicit wildcard grants. */
public final class PermissionCatalog {
    private PermissionCatalog() { }
    public static final List<String> ALL = List.of(
            "CATALOG_READ", "CATALOG_MANAGE", "STORE_READ", "STORE_MANAGE",
            "SUPPLIER_READ", "SUPPLIER_MANAGE", "PURCHASE_READ", "PURCHASE_MANAGE",
            "INVENTORY_READ", "INVENTORY_MANAGE", "TRANSFER_READ", "TRANSFER_MANAGE",
            "CUSTOMER_READ", "CUSTOMER_MANAGE", "ORDER_READ", "ORDER_MANAGE",
            "PROMOTION_READ", "PROMOTION_MANAGE", "EMPLOYEE_READ", "EMPLOYEE_MANAGE",
            "ROLE_MANAGE", "AUDIT_READ", "REPORT_READ", "REVIEW_MANAGE", "RETURN_MANAGE");
    public static final Map<String, Set<String>> ROLES = Map.of(
            "ADMIN", Set.copyOf(ALL),
            "QLSP", Set.of("CATALOG_READ", "CATALOG_MANAGE", "REVIEW_MANAGE"),
            "QLCH", Set.of("STORE_READ", "STORE_MANAGE"),
            "QLNH", Set.of("SUPPLIER_READ", "SUPPLIER_MANAGE", "PURCHASE_READ", "PURCHASE_MANAGE"),
            "QLTK", Set.of("INVENTORY_READ", "INVENTORY_MANAGE", "TRANSFER_READ", "TRANSFER_MANAGE"),
            "QLKH", Set.of("CUSTOMER_READ", "CUSTOMER_MANAGE", "RETURN_MANAGE"),
            "QLDH", Set.of("ORDER_READ", "ORDER_MANAGE", "RETURN_MANAGE"),
            "QLKM", Set.of("PROMOTION_READ", "PROMOTION_MANAGE"),
            "QLNV", Set.of("EMPLOYEE_READ", "EMPLOYEE_MANAGE"),
            "BCTK", Set.of("REPORT_READ"));
}
