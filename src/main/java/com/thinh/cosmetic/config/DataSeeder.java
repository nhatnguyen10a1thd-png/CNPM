package com.thinh.cosmetic.config;

import com.thinh.cosmetic.domain.entity.account.*;
import com.thinh.cosmetic.domain.entity.catalog.*;
import com.thinh.cosmetic.domain.entity.inventory.*;
import com.thinh.cosmetic.domain.entity.sales.CartEntity;
import com.thinh.cosmetic.domain.entity.sales.VoucherEntity;
import com.thinh.cosmetic.domain.enums.AccountType;
import com.thinh.cosmetic.domain.enums.ActiveStatus;
import com.thinh.cosmetic.domain.enums.Gender;
import com.thinh.cosmetic.domain.enums.PurchaseOrderStatus;
import com.thinh.cosmetic.domain.enums.TransferStatus;
import com.thinh.cosmetic.domain.enums.VoucherType;
import com.thinh.cosmetic.domain.entity.review.ReviewEntity;
import com.thinh.cosmetic.domain.enums.ReviewStatus;
import com.thinh.cosmetic.repository.account.*;
import com.thinh.cosmetic.repository.cart.CartRepository;
import com.thinh.cosmetic.repository.catalog.*;
import com.thinh.cosmetic.repository.order.VoucherRepository;
import com.thinh.cosmetic.repository.purchase.*;
import com.thinh.cosmetic.repository.review.ReviewRepository;
import com.thinh.cosmetic.repository.store.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeRoleRepository employeeRoleRepository;
    private final EmployeeStoreRepository employeeStoreRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CartRepository cartRepository;

    private final StoreRepository storeRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductImageRepository productImageRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final InventoryRepository inventoryRepository;
    private final VoucherRepository voucherRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final StockTransferRepository stockTransferRepository;
    private final StockTransferItemRepository stockTransferItemRepository;
    private final ReviewRepository reviewRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataSeeder(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            AccountRepository accountRepository,
            EmployeeRepository employeeRepository,
            EmployeeRoleRepository employeeRoleRepository,
            EmployeeStoreRepository employeeStoreRepository,
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository,
            CartRepository cartRepository,
            StoreRepository storeRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductSkuRepository productSkuRepository,
            ProductImageRepository productImageRepository,
            AttributeRepository attributeRepository,
            ProductAttributeValueRepository productAttributeValueRepository,
            InventoryRepository inventoryRepository,
            VoucherRepository voucherRepository,
            SupplierRepository supplierRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            StockTransferRepository stockTransferRepository,
            StockTransferItemRepository stockTransferItemRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.employeeRoleRepository = employeeRoleRepository;
        this.employeeStoreRepository = employeeStoreRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.cartRepository = cartRepository;
        this.storeRepository = storeRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productSkuRepository = productSkuRepository;
        this.productImageRepository = productImageRepository;
        this.attributeRepository = attributeRepository;
        this.productAttributeValueRepository = productAttributeValueRepository;
        this.inventoryRepository = inventoryRepository;
        this.voucherRepository = voucherRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.stockTransferRepository = stockTransferRepository;
        this.stockTransferItemRepository = stockTransferItemRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0 && accountRepository.count() > 0 && storeRepository.count() > 0) {
            ensureDemoAccountsExist();
            if (productRepository.count() >= 25) {
                log.info(">>> [DataSeeder] Cơ sở dữ liệu đã được khởi tạo đầy đủ. Bỏ qua bước nạp dữ liệu.");
                return;
            }
            log.info(">>> [DataSeeder] Dữ liệu tài khoản đã tồn tại trong SQL Server. Bổ sung danh mục, sản phẩm & tồn kho phong phú nếu cần...");
            seedVouchers();
            seedSuppliersAndWarehouseData();
            ensureCatalogEnriched();
            ensureCloudinaryImagesUpdated();
            fixDuplicatePrimaryImages();
            seedSampleReviews();
            return;
        }

        log.info(">>> [DataSeeder] Bắt đầu nạp dữ liệu mẫu hệ thống chuỗi cửa hàng mỹ phẩm LUNEA...");

        // 1. Phân quyền & Vai trò (Permissions & Roles)
        Map<String, PermissionEntity> perms = seedPermissions();
        Map<String, RoleEntity> roles = seedRoles(perms);

        // 2. Chi nhánh chuỗi cửa hàng (Stores)
        List<StoreEntity> stores = seedStores();

        // 3. Tài khoản demo (Accounts & Employees & Customer)
        seedDemoAccounts(roles, stores.get(0), stores.get(1));

        // 4. Thuộc tính làm đẹp (Attributes: Loại da & Nhu cầu)
        Map<String, AttributeEntity> attributes = seedAttributes();

        // 5. Thương hiệu & Danh mục (Brands & Categories)
        Map<String, BrandEntity> brands = seedBrands();
        Map<String, CategoryEntity> categories = seedCategories();

        // 6. Sản phẩm, Biến thể SKU, Thuộc tính, Ảnh & Tồn kho đa chi nhánh
        seedCatalogAndInventory(brands, categories, attributes, stores);

        // 7. Voucher khuyến mãi
        seedVouchers();

        // 8. Nhà cung cấp & Dữ liệu kho mẫu
        seedSuppliersAndWarehouseData();

        // 9. Đánh giá sản phẩm mẫu
        seedSampleReviews();

        ensureCloudinaryImagesUpdated();

        log.info(">>> [DataSeeder] Hoàn thành nạp dữ liệu mẫu thành công vào Microsoft SQL Server!");
    }

    private void ensureCatalogEnriched() {
        if (productRepository.count() >= 25) {
            return;
        }
        log.info(">>> [DataSeeder] Bổ sung bộ danh mục, thương hiệu và sản phẩm phong phú cho hệ thống...");
        List<StoreEntity> stores = storeRepository.findAll();
        Map<String, AttributeEntity> attributes = seedAttributes();
        Map<String, BrandEntity> brands = seedBrands();
        Map<String, CategoryEntity> categories = seedCategories();
        seedCatalogAndInventory(brands, categories, attributes, stores);
    }

        private static final Map<String, String> CLOUDINARY_MIGRATION_MAP = new HashMap<>();
    static {
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567425/lunea/products/prod_p1_1.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567426/lunea/products/prod_p1_2.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567427/lunea/products/prod_p2_3.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567428/lunea/products/prod_p3_4.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1556228720-195a672e8a03?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567429/lunea/products/prod_p4_5.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1571781926291-c477ebfd024b?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567431/lunea/products/prod_p5_6.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1519735777090-ec97162dc266?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567432/lunea/products/prod_p6_7.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1508746829417-e6f548d8d6ed?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567433/lunea/products/prod_p7_8.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567434/lunea/products/prod_p8_9.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567436/lunea/products/prod_p9_10.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567437/lunea/products/prod_p10_11.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567438/lunea/products/prod_p11_12.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567439/lunea/products/prod_p12_13.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1599305090598-fe179d501227?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567440/lunea/products/prod_p13_14.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1556228852-80b6e5eeff06?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567440/lunea/products/prod_p14_15.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1519735777090-ec97162dc266?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567441/lunea/products/prod_p15_16.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1563178406-4cdc2923acbc?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567442/lunea/products/prod_p16_17.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567443/lunea/products/prod_p17_18.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1612817288484-6f916006741a?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567444/lunea/products/prod_p18_19.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567445/lunea/products/prod_p19_20.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1625093742435-6fa192b6fb10?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567446/lunea/products/prod_p20_21.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1631730486784-5456119f69ae?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567447/lunea/products/prod_p21_22.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1516975080664-ed2fc6a32937?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567448/lunea/products/prod_p22_23.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1522337660859-02fbefca4702?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567449/lunea/products/prod_p23_24.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567450/lunea/products/prod_p24_25.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1585232351009-aa87416fca90?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567451/lunea/products/prod_p25_26.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567452/lunea/products/prod_p26_27.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1617897903246-719242758050?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567452/lunea/products/prod_p27_28.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1619451334792-150fd785ee74?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567453/lunea/products/prod_p28_29.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567454/lunea/products/prod_p29_30.jpg");
        CLOUDINARY_MIGRATION_MAP.put("https://images.unsplash.com/photo-1547887537-6158d64c35b3?w=600&auto=format&fit=crop&q=80", "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567455/lunea/products/prod_p30_31.jpg");
    }

    private void ensureCloudinaryImagesUpdated() {
        try {
            List<ProductImageEntity> images = productImageRepository.findAll();
            boolean hasOldImages = images.stream().anyMatch(img -> img.getImageUrl() != null && img.getImageUrl().contains("unsplash.com"));
            if (!hasOldImages) {
                return;
            }

            int count = 0;
            for (ProductImageEntity img : images) {
                if (img.getImageUrl() != null && CLOUDINARY_MIGRATION_MAP.containsKey(img.getImageUrl())) {
                    String newUrl = CLOUDINARY_MIGRATION_MAP.get(img.getImageUrl());
                    img.setImageUrl(newUrl);
                    productImageRepository.save(img);
                    count++;
                }
            }
            log.info(">>> [DataSeeder] Đã đồng bộ {} ảnh sản phẩm trong database sang tài khoản Cloudinary!", count);
        } catch (Exception e) {
            log.warn(">>> [DataSeeder] Không thể tự động đồng bộ ảnh sang Cloudinary: {}", e.getMessage());
        }
    }

    private void fixDuplicatePrimaryImages() {
        try {
            List<ProductImageEntity> all = productImageRepository.findAll();
            Map<Long, List<ProductImageEntity>> byProduct = all.stream()
                    .filter(i -> i.getProduct() != null && Boolean.TRUE.equals(i.getIsPrimary()))
                    .collect(Collectors.groupingBy(i -> i.getProduct().getId()));

            for (Map.Entry<Long, List<ProductImageEntity>> entry : byProduct.entrySet()) {
                List<ProductImageEntity> primaries = entry.getValue();
                if (primaries.size() > 1) {
                    for (int i = 1; i < primaries.size(); i++) {
                        ProductImageEntity dup = primaries.get(i);
                        dup.setIsPrimary(false);
                        productImageRepository.save(dup);
                    }
                    log.info(">>> [DataSeeder] Đã xử lý trùng lặp isPrimary cho sản phẩm #{}", entry.getKey());
                }
            }
        } catch (Exception e) {
            log.warn(">>> [DataSeeder] Không thể tự động làm sạch trùng lặp isPrimary: {}", e.getMessage());
        }
    }

    private void seedSampleReviews() {
        if (reviewRepository.count() > 0) return;
        List<CustomerEntity> customers = customerRepository.findAll();
        List<ProductEntity> products = productRepository.findAll();
        if (customers.isEmpty() || products.isEmpty()) return;

        CustomerEntity c1 = customers.get(0);
        ProductEntity p1 = products.get(0);
        ProductEntity p2 = products.size() > 1 ? products.get(1) : p1;
        ProductEntity p3 = products.size() > 2 ? products.get(2) : p1;
        ProductEntity p4 = products.size() > 3 ? products.get(3) : p1;

        reviewRepository.save(ReviewEntity.builder()
                .customer(c1)
                .product(p1)
                .rating(5)
                .content("Sản phẩm dùng rất thích, kiềm dầu tốt và giảm mụn viêm rõ rệt sau 2 tuần sử dụng!")
                .moderationStatus(ReviewStatus.VISIBLE)
                .build());

        reviewRepository.save(ReviewEntity.builder()
                .customer(c1)
                .product(p2)
                .rating(4)
                .content("Chất serum thấm nhanh, hơi châm chích nhẹ lúc đầu nhưng sau đó quen dần, da sáng mịn hơn.")
                .moderationStatus(ReviewStatus.PENDING)
                .build());

        reviewRepository.save(ReviewEntity.builder()
                .customer(c1)
                .product(p3)
                .rating(1)
                .content("Hàng giao chậm, quảng cáo lừa đảo, shop làm ăn không có tâm chút nào!!!")
                .moderationStatus(ReviewStatus.PENDING)
                .build());

        reviewRepository.save(ReviewEntity.builder()
                .customer(c1)
                .product(p4)
                .rating(2)
                .content("Spam quảng cáo link độc hại mua sắm ngoài hệ thống: http://scam-site.test")
                .moderationStatus(ReviewStatus.HIDDEN)
                .build());

        // Đánh giá thực tế thêm cho các sản phẩm danh mục khác
        for (ProductEntity p : products) {
            if (p.getName().contains("Bioderma")) {
                reviewRepository.save(ReviewEntity.builder()
                        .customer(c1)
                        .product(p)
                        .rating(5)
                        .content("Nước tẩy trang Bioderma nắp hồng chân ái, làm sạch sâu dịu nhẹ không hề căng rát hay cay mắt!")
                        .moderationStatus(ReviewStatus.VISIBLE)
                        .build());
            } else if (p.getName().contains("Anessa")) {
                reviewRepository.save(ReviewEntity.builder()
                        .customer(c1)
                        .product(p)
                        .rating(5)
                        .content("Kem chống nắng Anessa kiềm dầu cực đỉnh, đi nắng cả ngày da vẫn sáng mịn không xỉn màu.")
                        .moderationStatus(ReviewStatus.VISIBLE)
                        .build());
            } else if (p.getName().contains("MAC Matte Lipstick")) {
                reviewRepository.save(ReviewEntity.builder()
                        .customer(c1)
                        .product(p)
                        .rating(5)
                        .content("Màu son Chili cực kỳ tôn da và trắng răng, đánh lòng môi hay full môi đều siêu sang!")
                        .moderationStatus(ReviewStatus.VISIBLE)
                        .build());
            } else if (p.getName().contains("Lip Sleeping Mask")) {
                reviewRepository.save(ReviewEntity.builder()
                        .customer(c1)
                        .product(p)
                        .rating(5)
                        .content("Mặt nạ ngủ môi dùng ban đêm sáng dậy môi mềm mượt bóng mọng, hương dâu siêu thơm ngọt ngào.")
                        .moderationStatus(ReviewStatus.VISIBLE)
                        .build());
            }
        }

        log.info(">>> [DataSeeder] Đã nạp các đánh giá mẫu phục vụ kiểm duyệt sản phẩm.");
    }

    private Map<String, PermissionEntity> seedPermissions() {
        log.info(">>> [DataSeeder] Nạp danh sách Permissions...");
        List<PermissionEntity> list = List.of(
                PermissionEntity.builder().code("CATALOG_MANAGE").name("Quản lý danh mục & sản phẩm").description("Toàn quyền thêm/sửa/xóa sản phẩm và thương hiệu").build(),
                PermissionEntity.builder().code("ORDER_VIEW").name("Xem đơn hàng").description("Xem danh sách và chi tiết đơn hàng trong phạm vi chi nhánh").build(),
                PermissionEntity.builder().code("ORDER_PROCESS").name("Xử lý đơn hàng").description("Duyệt trạng thái đơn hàng, hủy đơn theo phạm vi chi nhánh").build(),
                PermissionEntity.builder().code("INVENTORY_VIEW").name("Tra cứu tồn kho").description("Xem tồn kho các sản phẩm theo chi nhánh").build(),
                PermissionEntity.builder().code("INVENTORY_ADJUST").name("Điều chỉnh tồn kho").description("Cập nhật số lượng tồn kho thực tế kèm lý do").build(),
                PermissionEntity.builder().code("STORE_MANAGE").name("Quản lý chi nhánh").description("Thêm, sửa thông tin các chi nhánh chuỗi cửa hàng").build(),
                PermissionEntity.builder().code("EMPLOYEE_MANAGE").name("Quản lý nhân viên").description("Quản lý tài khoản, phân vai trò và gán chi nhánh").build(),
                PermissionEntity.builder().code("VOUCHER_MANAGE").name("Quản lý voucher").description("Quản lý mã giảm giá khuyến mãi").build(),
                PermissionEntity.builder().code("PURCHASE_MANAGE").name("Quản lý nhập hàng").description("Quản lý nhà cung cấp và xác nhận phiếu nhập").build(),
                PermissionEntity.builder().code("CUSTOMER_MANAGE").name("Quản lý khách hàng").description("Xem và khóa/mở khóa tài khoản khách").build(),
                PermissionEntity.builder().code("REPORT_VIEW").name("Xem báo cáo thống kê").description("Xem dashboard doanh thu và biểu đồ đơn").build(),
                PermissionEntity.builder().code("AUDIT_VIEW").name("Xem nhật ký kiểm toán").description("Tra cứu lịch sử thao tác hệ thống").build()
        );

        Map<String, PermissionEntity> map = new HashMap<>();
        for (PermissionEntity p : list) {
            map.put(p.getCode(), permissionRepository.save(p));
        }
        return map;
    }

    private Map<String, RoleEntity> seedRoles(Map<String, PermissionEntity> perms) {
        log.info(">>> [DataSeeder] Nạp danh sách Roles...");
        RoleEntity adminRole = roleRepository.save(RoleEntity.builder().name("ADMIN").description("Quản trị viên toàn hệ thống").build());
        RoleEntity orderStaffRole = roleRepository.save(RoleEntity.builder().name("ORDER_STAFF").description("Nhân viên xử lý đơn hàng chi nhánh").build());
        RoleEntity warehouseRole = roleRepository.save(RoleEntity.builder().name("WAREHOUSE_STAFF").description("Nhân viên thủ kho chi nhánh").build());
        RoleEntity productManagerRole = roleRepository.save(RoleEntity.builder().name("PRODUCT_MANAGER").description("Quản lý danh mục sản phẩm").build());
        RoleEntity customerRole = roleRepository.save(RoleEntity.builder().name("CUSTOMER").description("Khách hàng mua sắm").build());

        // Admin có toàn bộ quyền
        for (PermissionEntity p : perms.values()) {
            rolePermissionRepository.save(RolePermissionEntity.builder().role(adminRole).permission(p).build());
        }

        // Order staff có quyền xem và xử lý đơn + xem tồn
        assignPerms(orderStaffRole, perms, "ORDER_VIEW", "ORDER_PROCESS", "INVENTORY_VIEW");

        // Warehouse staff có quyền xem tồn, điều chỉnh tồn và quản lý nhập hàng
        assignPerms(warehouseRole, perms, "INVENTORY_VIEW", "INVENTORY_ADJUST", "PURCHASE_MANAGE");

        // Product manager có quyền catalog và xem tồn
        assignPerms(productManagerRole, perms, "CATALOG_MANAGE", "INVENTORY_VIEW");

        Map<String, RoleEntity> map = new HashMap<>();
        map.put("ADMIN", adminRole);
        map.put("ORDER_STAFF", orderStaffRole);
        map.put("WAREHOUSE_STAFF", warehouseRole);
        map.put("PRODUCT_MANAGER", productManagerRole);
        map.put("CUSTOMER", customerRole);
        return map;
    }

    private void assignPerms(RoleEntity role, Map<String, PermissionEntity> perms, String... codes) {
        for (String c : codes) {
            PermissionEntity p = perms.get(c);
            if (p != null) {
                rolePermissionRepository.save(RolePermissionEntity.builder().role(role).permission(p).build());
            }
        }
    }

    private List<StoreEntity> seedStores() {
        log.info(">>> [DataSeeder] Nạp 3 Chi nhánh chuỗi cửa hàng...");
        StoreEntity s1 = StoreEntity.builder()
                .name("LUNEA Đồng Khởi - Quận 1 (TP.HCM)")
                .address("123 Đường Đồng Khởi, Phường Bến Nghé, Quận 1")
                .province("Hồ Chí Minh")
                .phone("028.3822.9999")
                .openTime(LocalTime.of(8, 30))
                .closeTime(LocalTime.of(22, 0))
                .latitude(new BigDecimal("10.776889"))
                .longitude(new BigDecimal("106.700806"))
                .status(ActiveStatus.ACTIVE)
                .build();

        StoreEntity s2 = StoreEntity.builder()
                .name("LUNEA Cầu Giấy - Hà Nội")
                .address("68 Đường Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy")
                .province("Hà Nội")
                .phone("024.3766.8888")
                .openTime(LocalTime.of(8, 30))
                .closeTime(LocalTime.of(21, 30))
                .latitude(new BigDecimal("21.033333"))
                .longitude(new BigDecimal("105.792778"))
                .status(ActiveStatus.ACTIVE)
                .build();

        StoreEntity s3 = StoreEntity.builder()
                .name("LUNEA Bạch Đằng - Hải Châu (Đà Nẵng)")
                .address("186 Đường Bạch Đằng, Phường Hải Châu 1, Quận Hải Châu")
                .province("Đà Nẵng")
                .phone("0236.3888.777")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(21, 30))
                .latitude(new BigDecimal("16.067778"))
                .longitude(new BigDecimal("108.223889"))
                .status(ActiveStatus.ACTIVE)
                .build();

        return List.of(
                storeRepository.save(s1),
                storeRepository.save(s2),
                storeRepository.save(s3)
        );
    }

    private void seedDemoAccounts(Map<String, RoleEntity> roles, StoreEntity storeHcm, StoreEntity storeHn) {
        log.info(">>> [DataSeeder] Nạp tài khoản demo...");
        String defaultPasswordHash = passwordEncoder.encode("123456");

        // 1. Admin
        AccountEntity accAdmin = accountRepository.save(AccountEntity.builder()
                .username("admin")
                .email("admin@lunea.test")
                .phone("0900000001")
                .passwordHash(defaultPasswordHash)
                .accountType(AccountType.EMPLOYEE)
                .status(ActiveStatus.ACTIVE)
                .build());
        EmployeeEntity empAdmin = employeeRepository.save(EmployeeEntity.builder()
                .account(accAdmin)
                .fullName("Nguyễn Quản Trị Hệ Thống")
                .internalEmail("admin@lunea.test")
                .phone("0900000001")
                .status(ActiveStatus.ACTIVE)
                .build());
        employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(empAdmin).role(roles.get("ADMIN")).build());
        employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(empAdmin).store(storeHcm).isPrimary(true).build());

        // 2. Order Staff HCM
        AccountEntity accOrderHcm = accountRepository.save(AccountEntity.builder()
                .username("order_hcm")
                .email("order.hcm@lunea.test")
                .phone("0900000002")
                .passwordHash(defaultPasswordHash)
                .accountType(AccountType.EMPLOYEE)
                .status(ActiveStatus.ACTIVE)
                .build());
        EmployeeEntity empOrderHcm = employeeRepository.save(EmployeeEntity.builder()
                .account(accOrderHcm)
                .fullName("Trần Minh Quân (Đơn hàng HCM)")
                .internalEmail("order.hcm@lunea.test")
                .phone("0900000002")
                .status(ActiveStatus.ACTIVE)
                .build());
        employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(empOrderHcm).role(roles.get("ORDER_STAFF")).build());
        employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(empOrderHcm).store(storeHcm).isPrimary(true).build());

        // 3. Order Staff HN
        AccountEntity accOrderHn = accountRepository.save(AccountEntity.builder()
                .username("order_hn")
                .email("order.hn@lunea.test")
                .phone("0900000003")
                .passwordHash(defaultPasswordHash)
                .accountType(AccountType.EMPLOYEE)
                .status(ActiveStatus.ACTIVE)
                .build());
        EmployeeEntity empOrderHn = employeeRepository.save(EmployeeEntity.builder()
                .account(accOrderHn)
                .fullName("Lê Thu Hà (Đơn hàng Hà Nội)")
                .internalEmail("order.hn@lunea.test")
                .phone("0900000003")
                .status(ActiveStatus.ACTIVE)
                .build());
        employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(empOrderHn).role(roles.get("ORDER_STAFF")).build());
        employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(empOrderHn).store(storeHn).isPrimary(true).build());

        // 4. Warehouse Staff HCM
        AccountEntity accWarehouse = accountRepository.save(AccountEntity.builder()
                .username("warehouse_hcm")
                .email("warehouse.hcm@lunea.test")
                .phone("0900000004")
                .passwordHash(defaultPasswordHash)
                .accountType(AccountType.EMPLOYEE)
                .status(ActiveStatus.ACTIVE)
                .build());
        EmployeeEntity empWarehouse = employeeRepository.save(EmployeeEntity.builder()
                .account(accWarehouse)
                .fullName("Phạm Văn Kho (Thủ kho HCM)")
                .internalEmail("warehouse.hcm@lunea.test")
                .phone("0900000004")
                .status(ActiveStatus.ACTIVE)
                .build());
        employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(empWarehouse).role(roles.get("WAREHOUSE_STAFF")).build());
        employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(empWarehouse).store(storeHcm).isPrimary(true).build());

        // 5. Customer Demo
        AccountEntity accCustomer = accountRepository.save(AccountEntity.builder()
                .username("khach01@lunea.test")
                .email("khach01@lunea.test")
                .phone("0901234567")
                .passwordHash(defaultPasswordHash)
                .accountType(AccountType.CUSTOMER)
                .status(ActiveStatus.ACTIVE)
                .build());
        CustomerEntity customer = customerRepository.save(CustomerEntity.builder()
                .account(accCustomer)
                .fullName("Lê Hoàng Yến")
                .dob(LocalDate.of(1998, 5, 15))
                .gender(Gender.FEMALE)
                .loyaltyPoints(120)
                .joinDate(LocalDate.now().minusMonths(3))
                .build());

        customerAddressRepository.save(CustomerAddressEntity.builder()
                .customerEntity(customer)
                .recipientName("Lê Hoàng Yến")
                .phone("0901234567")
                .addressDetail("Số 45 Đường Lê Duẩn")
                .ward("Phường Bến Nghé")
                .district("Quận 1")
                .province("Hồ Chí Minh")
                .isDefault(true)
                .build());

        cartRepository.save(CartEntity.builder()
                .customer(customer)
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private Map<String, AttributeEntity> seedAttributes() {
        log.info(">>> [DataSeeder] Nạp Attributes (Loại da & Nhu cầu)...");
        List<AttributeEntity> attrs = List.of(
                AttributeEntity.builder().name("Da Dầu").group("SKIN_TYPE").build(),
                AttributeEntity.builder().name("Da Khô").group("SKIN_TYPE").build(),
                AttributeEntity.builder().name("Da Nhạy Cảm").group("SKIN_TYPE").build(),
                AttributeEntity.builder().name("Da Hỗn Hợp").group("SKIN_TYPE").build(),
                AttributeEntity.builder().name("Da Mụn").group("SKIN_TYPE").build(),
                AttributeEntity.builder().name("Trị Mụn").group("NEED").build(),
                AttributeEntity.builder().name("Phục Hồi Da").group("NEED").build(),
                AttributeEntity.builder().name("Dưỡng Sáng Mờ Thâm").group("NEED").build(),
                AttributeEntity.builder().name("Chống Lão Hóa").group("NEED").build(),
                AttributeEntity.builder().name("Dưỡng Ẩm Chuyên Sâu").group("NEED").build()
        );

        Map<String, AttributeEntity> map = new HashMap<>();
        for (AttributeEntity a : attrs) {
            AttributeEntity existing = attributeRepository.findByName(a.getName()).orElse(null);
            if (existing == null) {
                existing = attributeRepository.save(a);
            }
            map.put(existing.getName(), existing);
        }
        return map;
    }

    private Map<String, BrandEntity> seedBrands() {
        log.info(">>> [DataSeeder] Nạp Thương hiệu mỹ phẩm...");
        List<BrandEntity> list = List.of(
                BrandEntity.builder().name("Cocoon Vietnam").description("Thương hiệu mỹ phẩm thuần chay 100% từ nguyên liệu thiên nhiên Việt Nam.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("The Ordinary").description("Thương hiệu dưỡng da khoa học tối giản, nồng độ hoạt chất cao từ Canada.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("La Roche-Posay").description("Dược mỹ phẩm hàng đầu từ Pháp, chuyên sâu chăm sóc làn da nhạy cảm.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Paula's Choice").description("Thương hiệu dược mỹ phẩm khoa học hàng đầu thế giới với BHA/Retinol.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Innisfree").description("Thương hiệu mỹ phẩm thiên nhiên từ đảo Jeju, Hàn Quốc.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("L'Oréal Paris").description("Tập đoàn mỹ phẩm hàng đầu thế giới từ Pháp với công nghệ làm đẹp tiên tiến.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("CeraVe").description("Dược mỹ phẩm số 1 được các bác sĩ da liễu tại Mỹ khuyên dùng.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Anessa").description("Thương hiệu kem chống nắng số 1 Nhật Bản thuộc tập đoàn Shiseido suốt 21 năm liền.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Klairs").description("Mỹ phẩm thuần chay lành tính cho da nhạy cảm Dear, Klairs từ Hàn Quốc.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Bioderma").description("Tiên phong giải pháp sinh học làm sạch và bảo vệ da nhạy cảm từ Pháp.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Laneige").description("Thương hiệu chăm sóc da và trang điểm nổi tiếng hàng đầu Hàn Quốc với công nghệ Water Science.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("MAC Cosmetics").description("Thương hiệu mỹ phẩm trang điểm chuyên nghiệp đẳng cấp quốc tế từ Mỹ.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Maybelline New York").description("Thương hiệu trang điểm số 1 thế giới với phong cách năng động New York.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Vichy").description("Dược mỹ phẩm nguồn gốc khoáng núi lửa Pháp giúp tăng cường sức sống cho da.").status(ActiveStatus.ACTIVE).build(),
                BrandEntity.builder().name("Estée Lauder").description("Thương hiệu mỹ phẩm cao cấp xa xỉ biểu tượng của Mỹ chuyên dưỡng da chống lão hóa.").status(ActiveStatus.ACTIVE).build()
        );

        Map<String, BrandEntity> map = new HashMap<>();
        for (BrandEntity b : list) {
            BrandEntity existing = brandRepository.findByName(b.getName()).orElse(null);
            if (existing == null) {
                existing = brandRepository.save(b);
            }
            map.put(existing.getName(), existing);
        }
        return map;
    }

    private CategoryEntity getOrCreateCategory(String name, String desc, CategoryEntity parent) {
        return categoryRepository.findFirstByName(name).orElseGet(() ->
                categoryRepository.save(CategoryEntity.builder()
                        .name(name)
                        .description(desc)
                        .parent(parent)
                        .status(ActiveStatus.ACTIVE)
                        .build())
        );
    }

    private Map<String, CategoryEntity> seedCategories() {
        log.info(">>> [DataSeeder] Nạp Danh mục mỹ phẩm...");
        CategoryEntity cSkincare = getOrCreateCategory("Chăm sóc da", "Sản phẩm dưỡng da mặt chuyên sâu", null);
        CategoryEntity cMakeup = getOrCreateCategory("Trang điểm", "Mỹ phẩm trang điểm cao cấp", null);
        CategoryEntity cBodyHair = getOrCreateCategory("Chăm sóc tóc & Cơ thể", "Sản phẩm chăm sóc toàn thân", null);
        CategoryEntity cFragrance = getOrCreateCategory("Nước hoa", "Nước hoa và hương thơm sang trọng", null);

        CategoryEntity cSerum = getOrCreateCategory("Tinh chất & Serum", "Serum đặc trị dưỡng da chuyên sâu", cSkincare);
        CategoryEntity cCream = getOrCreateCategory("Kem dưỡng ẩm", "Cấp ẩm khóa ẩm phục hồi da", cSkincare);
        CategoryEntity cCleanser = getOrCreateCategory("Sữa rửa mặt", "Làm sạch dịu nhẹ da mặt", cSkincare);
        CategoryEntity cSunscreen = getOrCreateCategory("Kem chống nắng", "Bảo vệ da trước tia UVA/UVB", cSkincare);
        CategoryEntity cToner = getOrCreateCategory("Nước hoa hồng & Toner", "Cân bằng độ ẩm và độ pH", cSkincare);
        CategoryEntity cRemover = getOrCreateCategory("Tẩy trang", "Làm sạch bụi mịn và lớp makeup", cSkincare);
        CategoryEntity cExfoliator = getOrCreateCategory("Tẩy tế bào chết", "Tẩy tế bào chết vật lý và hóa học", cSkincare);
        CategoryEntity cMask = getOrCreateCategory("Mặt nạ dưỡng da", "Mặt nạ giấy, đất sét và mặt nạ ngủ", cSkincare);

        CategoryEntity cLipstick = getOrCreateCategory("Son môi", "Son thỏi lì, son kem, son dưỡng bóng", cMakeup);
        CategoryEntity cFoundation = getOrCreateCategory("Kem nền & Cushion", "Tạo lớp nền mịn lì che phủ tự nhiên", cMakeup);
        CategoryEntity cBlush = getOrCreateCategory("Phấn phủ & Má hồng", "Tạo điểm nhấn tươi tắn cho khuôn mặt", cMakeup);
        CategoryEntity cEyeMakeup = getOrCreateCategory("Trang điểm mắt & Mascara", "Mascara, kẻ mắt và phấn mắt", cMakeup);

        CategoryEntity cBodyScrub = getOrCreateCategory("Tẩy tế bào chết cơ thể", "Tẩy da chết toàn thân mịn màng", cBodyHair);
        CategoryEntity cHairCare = getOrCreateCategory("Chăm sóc tóc & Dầu gội", "Dầu gội xả, serum và xịt dưỡng tóc", cBodyHair);

        CategoryEntity cPerfume = getOrCreateCategory("Nước hoa cao cấp", "Nước hoa Eau De Parfum lưu hương quyến rũ", cFragrance);
        CategoryEntity cBodyMist = getOrCreateCategory("Xịt thơm toàn thân Body Mist", "Mùi hương dịu nhẹ thư giãn cả ngày", cFragrance);

        Map<String, CategoryEntity> map = new HashMap<>();
        map.put("SKINCARE", cSkincare);
        map.put("SERUM", cSerum);
        map.put("CREAM", cCream);
        map.put("CLEANSER", cCleanser);
        map.put("SUNSCREEN", cSunscreen);
        map.put("TONER", cToner);
        map.put("MAKEUP_REMOVER", cRemover);
        map.put("EXFOLIATOR", cExfoliator);
        map.put("MASK", cMask);

        map.put("MAKEUP", cMakeup);
        map.put("LIPSTICK", cLipstick);
        map.put("FOUNDATION", cFoundation);
        map.put("BLUSH", cBlush);
        map.put("EYE_MAKEUP", cEyeMakeup);

        map.put("BODYHAIR", cBodyHair);
        map.put("BODY_SCRUB", cBodyScrub);
        map.put("HAIR_CARE", cHairCare);

        map.put("FRAGRANCE", cFragrance);
        map.put("PERFUME", cPerfume);
        map.put("BODY_MIST", cBodyMist);
        return map;
    }

    private void seedCatalogAndInventory(
            Map<String, BrandEntity> brands,
            Map<String, CategoryEntity> categories,
            Map<String, AttributeEntity> attrs,
            List<StoreEntity> stores
    ) {
        log.info(">>> [DataSeeder] Nạp Sản phẩm, Biến thể SKU, Ảnh & Tồn kho đa chi nhánh...");

        StoreEntity storeHcm = stores.get(0);
        StoreEntity storeHn = stores.size() > 1 ? stores.get(1) : storeHcm;
        StoreEntity storeDn = stores.size() > 2 ? stores.get(2) : storeHcm;

        // 1. Serum Bí Đao Cocoon (Đủ hàng ở 3 chi nhánh)
        ProductEntity p1 = createProduct(
                "Serum Bí Đao Cocoon 7% Niacinamide Trị Mụn & Mờ Thâm",
                brands.get("Cocoon Vietnam"),
                categories.get("SERUM"),
                "Việt Nam",
                "Bí đao, Rau má, Tràm trà, 7% Niacinamide",
                "Giúp kiểm soát dầu thừa, làm dịu vết mụn đỏ, làm mờ vết thâm mụn rõ rệt.",
                "Serum bí đao Cocoon với sự kết hợp của dịch chiết bí đao, rau má và tràm trà giúp kháng khuẩn, làm thông thoáng lỗ chân lông.",
                true
        );
        ProductSkuEntity p1s1 = createSku(p1, "SKU-CCN-BD-70ML", "Dung tích 70ml", new BigDecimal("265000"), new BigDecimal("295000"), "893851500001");
        ProductSkuEntity p1s2 = createSku(p1, "SKU-CCN-BD-140ML", "Dung tích 140ml", new BigDecimal("465000"), new BigDecimal("520000"), "893851500002");
        createImage(p1, p1s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567425/lunea/products/prod_p1_1.jpg", true);
        createImage(p1, null, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567426/lunea/products/prod_p1_2.jpg", false);
        linkAttributes(p1, attrs, "Da Dầu", "Da Mụn", "Trị Mụn", "Dưỡng Sáng Mờ Thâm");
        createInventoryAllStores(stores, p1s1, 30, 20, 15);
        createInventoryAllStores(stores, p1s2, 20, 10, 5);

        // 2. The Ordinary Niacinamide (HẾT HÀNG TOÀN CHUỖI để demo "Tạm hết hàng")
        ProductEntity p2 = createProduct(
                "Tinh Chất The Ordinary Niacinamide 10% + Zinc 1%",
                brands.get("The Ordinary"),
                categories.get("SERUM"),
                "Canada",
                "10% Niacinamide, 1% Zinc PCA",
                "Cân bằng bã nhờn, thu nhỏ lỗ chân lông và làm đều màu da.",
                "Công thức chứa vitamin B3 nồng độ cao kết hợp kẽm khoáng giúp thanh lọc da, giảm bít tắc tuyến bã nhờn.",
                true
        );
        ProductSkuEntity p2s1 = createSku(p2, "SKU-TO-NIA-30ML", "Dung tích 30ml", new BigDecimal("220000"), new BigDecimal("250000"), "769915190011");
        ProductSkuEntity p2s2 = createSku(p2, "SKU-TO-NIA-60ML", "Dung tích 60ml", new BigDecimal("380000"), new BigDecimal("430000"), "769915190012");
        createImage(p2, p2s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567427/lunea/products/prod_p2_3.jpg", true);
        linkAttributes(p2, attrs, "Da Dầu", "Da Hỗn Hợp", "Trị Mụn", "Dưỡng Sáng Mờ Thâm");
        createInventoryAllStores(stores, p2s1, 0, 0, 0);
        createInventoryAllStores(stores, p2s2, 0, 0, 0);

        // 3. Serum La Roche-Posay Hyalu B5 (CHỈ CÒN Ở HCM để demo hệ thống tự gán chi nhánh HCM)
        ProductEntity p3 = createProduct(
                "Serum Phục Hồi Tái Tạo Da La Roche-Posay Hyalu B5",
                brands.get("La Roche-Posay"),
                categories.get("SERUM"),
                "Pháp",
                "Vitamin B5 (Panthenol), Dual Hyaluronic Acid, Madecassoside",
                "Cấp ẩm chuyên sâu, phục hồi hàng rào bảo vệ da, làm dịu da kích ứng.",
                "Dưỡng chất chuyên sâu với phức hợp Hyaluronic Acid đa phân tử và Vitamin B5 đậm đặc giúp làm đầy nếp nhăn và tái tạo làn da mệt mỏi.",
                true
        );
        ProductSkuEntity p3s1 = createSku(p3, "SKU-LRP-B5-30ML", "Dung tích 30ml", new BigDecimal("980000"), new BigDecimal("1150000"), "3337875583626");
        ProductSkuEntity p3s2 = createSku(p3, "SKU-LRP-B5-50ML", "Dung tích 50ml", new BigDecimal("1450000"), new BigDecimal("1650000"), "3337875583633");
        createImage(p3, p3s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567428/lunea/products/prod_p3_4.jpg", true);
        linkAttributes(p3, attrs, "Da Nhạy Cảm", "Da Khô", "Phục Hồi Da", "Dưỡng Ẩm Chuyên Sâu");
        createInventory(storeHcm, p3s1, 25, 0);
        createInventory(storeHn, p3s1, 0, 0);
        createInventory(storeDn, p3s1, 0, 0);
        createInventory(storeHcm, p3s2, 15, 0);
        createInventory(storeHn, p3s2, 0, 0);
        createInventory(storeDn, p3s2, 0, 0);

        // 4. Dung Dịch Tẩy Tế Bào Chết Hóa Học Paula's Choice BHA 2% Liquid
        ProductEntity p4 = createProduct(
                "Dung Dịch Tẩy Tế Bào Chết Hóa Học Paula's Choice BHA 2% Liquid",
                brands.get("Paula's Choice"),
                categories.get("EXFOLIATOR"),
                "Mỹ",
                "2% Salicylic Acid (BHA), Chiết xuất Trà xanh",
                "Loại bỏ tế bào chết sâu trong lỗ chân lông, làm mịn da thô ráp.",
                "Sản phẩm kinh điển chứa 2% BHA tan trong dầu giúp loại bỏ mụn đầu đen, bã nhờn tích tụ, đem lại bề mặt da mịn màng.",
                true
        );
        ProductSkuEntity p4s1 = createSku(p4, "SKU-PC-BHA-30ML", "Dung tích 30ml (Trial)", new BigDecimal("399000"), new BigDecimal("450000"), "065543902011");
        ProductSkuEntity p4s2 = createSku(p4, "SKU-PC-BHA-118ML", "Dung tích 118ml (Fullsize)", new BigDecimal("949000"), new BigDecimal("1050000"), "065543902012");
        createImage(p4, p4s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567429/lunea/products/prod_p4_5.jpg", true);
        linkAttributes(p4, attrs, "Da Dầu", "Da Hỗn Hợp", "Trị Mụn");
        createInventoryAllStores(stores, p4s1, 40, 25, 20);
        createInventoryAllStores(stores, p4s2, 30, 15, 10);

        // 5. Kem Dưỡng Ẩm Phục Hồi Cicaplast Baume B5+
        ProductEntity p5 = createProduct(
                "Kem Dưỡng Ẩm Phục Hồi Da La Roche-Posay Cicaplast Baume B5+",
                brands.get("La Roche-Posay"),
                categories.get("CREAM"),
                "Pháp",
                "Tribioma, 5% Panthenol, Madecassoside, Zinc",
                "Làm dịu tức thì các vết đỏ, ngứa ngáy, phục hồi màng ẩm sau 1 giờ.",
                "Kem dưỡng ẩm làm dịu và phục hồi da đa năng dành cho cả gia đình, phù hợp cho da sau điều trị mụn, kích ứng hoặc laser.",
                true
        );
        ProductSkuEntity p5s1 = createSku(p5, "SKU-LRP-CICA-40ML", "Dung tích 40ml", new BigDecimal("365000"), new BigDecimal("410000"), "3337875816809");
        ProductSkuEntity p5s2 = createSku(p5, "SKU-LRP-CICA-100ML", "Dung tích 100ml", new BigDecimal("620000"), new BigDecimal("690000"), "3337875816816");
        createImage(p5, p5s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567431/lunea/products/prod_p5_6.jpg", true);
        linkAttributes(p5, attrs, "Da Nhạy Cảm", "Da Khô", "Phục Hồi Da", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p5s1, 50, 35, 25);
        createInventoryAllStores(stores, p5s2, 30, 20, 15);

        // 6. Kem Chống Nắng La Roche-Posay Anthelios UVMune 400
        ProductEntity p6 = createProduct(
                "Kem Chống Nắng Kiềm Dầu La Roche-Posay Anthelios UVMune 400 Oil Control",
                brands.get("La Roche-Posay"),
                categories.get("SUNSCREEN"),
                "Pháp",
                "Màng lọc Mexoryl 400, Công nghệ Airlicium",
                "Bảo vệ toàn diện trước tia UVA dài, kiềm dầu đến 12 giờ không bóng nhờn.",
                "Đột phá màng lọc Mexoryl 400 bảo vệ da trước tia UVA sâu nhất (380-400nm), kết cấu gel lỏng thấm nhanh ráo mịn.",
                true
        );
        ProductSkuEntity p6s1 = createSku(p6, "SKU-LRP-SUN-50ML", "Dung tích 50ml", new BigDecimal("475000"), new BigDecimal("535000"), "3337875797641");
        createImage(p6, p6s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567441/lunea/products/prod_p15_16.jpg", true);
        linkAttributes(p6, attrs, "Da Dầu", "Da Hỗn Hợp", "Chống Lão Hóa");
        createInventoryAllStores(stores, p6s1, 60, 40, 30);

        // 7. Sữa Rửa Mặt Cà Phê Đắk Lắk Cocoon
        ProductEntity p7 = createProduct(
                "Sữa Rửa Mặt Dịu Nhẹ Cocoon Cà Phê Đắk Lắk Sạch Sâu",
                brands.get("Cocoon Vietnam"),
                categories.get("CLEANSER"),
                "Việt Nam",
                "Chiết xuất cà phê Đắk Lắk, Dầu mù u, Axit Amin",
                "Làm sạch nhẹ dịu, không gây khô căng, đánh thức làn da tràn đầy năng lượng.",
                "Công thức pH 5.5 cân bằng với bọt mịn giúp cuốn trôi bụi bẩn và dầu thừa mà vẫn bảo tồn màng ẩm tự nhiên của da.",
                false
        );
        ProductSkuEntity p7s1 = createSku(p7, "SKU-CCN-SRM-140ML", "Chai 140ml", new BigDecimal("175000"), new BigDecimal("195000"), "893851500003");
        ProductSkuEntity p7s2 = createSku(p7, "SKU-CCN-SRM-310ML", "Chai 310ml Tiết Kiệm", new BigDecimal("325000"), new BigDecimal("360000"), "893851500005");
        createImage(p7, p7s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567433/lunea/products/prod_p7_8.jpg", true);
        linkAttributes(p7, attrs, "Da Dầu", "Da Hỗn Hợp", "Dưỡng Sáng Mờ Thâm");
        createInventoryAllStores(stores, p7s1, 45, 30, 20);
        createInventoryAllStores(stores, p7s2, 35, 25, 15);

        // 8. Nước Dưỡng Tóc Tinh Dầu Bưởi Cocoon
        ProductEntity p8 = createProduct(
                "Nước Dưỡng Tóc Tinh Dầu Bưởi Cocoon Pomelo Hair Tonic",
                brands.get("Cocoon Vietnam"),
                categories.get("HAIR_CARE"),
                "Việt Nam",
                "Tinh dầu bưởi nguyên chất, Xylishine, Vitamin B5",
                "Giảm gãy rụng rõ rệt, kích thích mọc tóc con và nuôi dưỡng nang tóc chắc khỏe.",
                "Giải pháp thuần chay giải cứu mái tóc yếu gãy rụng, mùi hương hoa bưởi thanh khiết sảng khoái.",
                false
        );
        ProductSkuEntity p8s1 = createSku(p8, "SKU-CCN-TOC-140ML", "Chai xịt 140ml", new BigDecimal("145000"), new BigDecimal("165000"), "893851500004");
        ProductSkuEntity p8s2 = createSku(p8, "SKU-CCN-TOC-310ML", "Chai xịt 310ml Tiết Kiệm", new BigDecimal("265000"), new BigDecimal("295000"), "893851500006");
        createImage(p8, p8s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567434/lunea/products/prod_p8_9.jpg", true);
        createInventoryAllStores(stores, p8s1, 50, 35, 25);
        createInventoryAllStores(stores, p8s2, 30, 20, 15);

        // 9. Innisfree Green Tea Seed Hyaluronic Serum
        ProductEntity p9 = createProduct(
                "Serum Cấp Ẩm Chuyên Sâu Innisfree Green Tea Seed Hyaluronic",
                brands.get("Innisfree"),
                categories.get("SERUM"),
                "Hàn Quốc",
                "Trà xanh hữu cơ Beauty Green Tea, 5 loại Hyaluronic Acid",
                "Cấp nước tức thì, cải thiện tình trạng da thiếu ẩm từ sâu bên trong.",
                "Công nghệ thẩm thấu nano giúp dưỡng chất từ mầm trà xanh đảo Jeju khóa ẩm suốt 24 giờ cho làn da căng mọng.",
                false
        );
        ProductSkuEntity p9s1 = createSku(p9, "SKU-INN-TEA-80ML", "Dung tích 80ml", new BigDecimal("610000"), new BigDecimal("680000"), "8809612850011");
        ProductSkuEntity p9s2 = createSku(p9, "SKU-INN-TEA-160ML", "Dung tích 160ml (Jumbo)", new BigDecimal("980000"), new BigDecimal("1100000"), "8809612850012");
        createImage(p9, p9s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567436/lunea/products/prod_p9_10.jpg", true);
        linkAttributes(p9, attrs, "Da Khô", "Da Hỗn Hợp", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p9s1, 30, 20, 15);
        createInventoryAllStores(stores, p9s2, 20, 15, 10);

        // 10. Innisfree Matte Velvet Tint
        ProductEntity p10 = createProduct(
                "Son Kem Lì Mịn Môi Cao Cấp Innisfree Matte Velvet Tint",
                brands.get("Innisfree"),
                categories.get("LIPSTICK"),
                "Hàn Quốc",
                "Dầu jojoba hữu cơ, sắc tố màu khoáng",
                "Màu son lên chuẩn sắc, chất son nhẹ tựa mây không gây khô vân môi.",
                "Chất son nhung mờ mịn màng lâu trôi đến 8 giờ, cho đôi môi vẻ đẹp cuốn hút tự nhiên.",
                false
        );
        ProductSkuEntity p10s1 = createSku(p10, "SKU-INN-LIP-01", "Màu 01 - Đỏ Cherry Rực Rỡ", new BigDecimal("280000"), new BigDecimal("320000"), "8809612850021");
        ProductSkuEntity p10s2 = createSku(p10, "SKU-INN-LIP-02", "Màu 02 - Cam Đất Thời Thượng", new BigDecimal("280000"), new BigDecimal("320000"), "8809612850022");
        ProductSkuEntity p10s3 = createSku(p10, "SKU-INN-LIP-03", "Màu 03 - Hồng Đất MLBB", new BigDecimal("280000"), new BigDecimal("320000"), "8809612850023");
        createImage(p10, p10s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567437/lunea/products/prod_p10_11.jpg", true);
        createInventoryAllStores(stores, p10s1, 25, 20, 15);
        createInventoryAllStores(stores, p10s2, 30, 25, 10);
        createInventoryAllStores(stores, p10s3, 35, 20, 15);

        // 11. Bioderma Sensibio H2O
        ProductEntity p11 = createProduct(
                "Nước Tẩy Trang Dịu Nhẹ Cho Da Nhạy Cảm Bioderma Sensibio H2O",
                brands.get("Bioderma"),
                categories.get("MAKEUP_REMOVER"),
                "Pháp",
                "Hạt micelle sinh học, Chiết xuất dưa leo, Nước tinh khiết",
                "Làm sạch 99% lớp trang điểm, bụi mịn PM 2.5 mà vẫn giữ ẩm mượt cho da.",
                "Nước tẩy trang công nghệ Micellar hàng đầu thế giới được các chuyên gia da liễu khuyên dùng cho làn da nhạy cảm.",
                true
        );
        ProductSkuEntity p11s1 = createSku(p11, "SKU-BIO-H2O-100ML", "Chai 100ml Mini", new BigDecimal("185000"), new BigDecimal("210000"), "3401572288105");
        ProductSkuEntity p11s2 = createSku(p11, "SKU-BIO-H2O-500ML", "Chai 500ml Tiết Kiệm", new BigDecimal("495000"), new BigDecimal("550000"), "3401572288501");
        createImage(p11, p11s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567438/lunea/products/prod_p11_12.jpg", true);
        linkAttributes(p11, attrs, "Da Nhạy Cảm", "Da Dầu", "Da Khô");
        createInventoryAllStores(stores, p11s1, 50, 35, 25);
        createInventoryAllStores(stores, p11s2, 40, 30, 20);

        // 12. Klairs Supple Preparation Toner
        ProductEntity p12 = createProduct(
                "Nước Hoa Hồng Cấp Ẩm Không Mùi Klairs Supple Preparation Unscented Toner",
                brands.get("Klairs"),
                categories.get("TONER"),
                "Hàn Quốc",
                "Hyaluronic Acid, Chiết xuất rau má, Phyto-Oligo",
                "Cân bằng độ pH tức thì, cấp ẩm sâu và làm dịu vùng da kích ứng đỏ rát.",
                "Toner dưỡng ẩm quốc dân với công thức thuần chay không cồn, không hương liệu, thẩm thấu nhanh không bết dính.",
                false
        );
        ProductSkuEntity p12s1 = createSku(p12, "SKU-KLR-TON-180ML", "Chai 180ml", new BigDecimal("295000"), new BigDecimal("345000"), "8809572890014");
        createImage(p12, p12s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567439/lunea/products/prod_p12_13.jpg", true);
        linkAttributes(p12, attrs, "Da Nhạy Cảm", "Da Khô", "Dưỡng Ẩm Chuyên Sâu", "Phục Hồi Da");
        createInventoryAllStores(stores, p12s1, 40, 25, 20);

        // 13. CeraVe Daily Moisturizing Lotion
        ProductEntity p13 = createProduct(
                "Sữa Dưỡng Ẩm Chuyên Sâu Dành Cho Da Khô Đến Rất Khô CeraVe Moisturizing Lotion",
                brands.get("CeraVe"),
                categories.get("CREAM"),
                "Mỹ",
                "3 Ceramide thiết yếu (1, 3, 6-II), Hyaluronic Acid, Công nghệ MVE",
                "Cung cấp độ ẩm suốt 24 giờ, phục hồi lớp màng bảo vệ tự nhiên của da.",
                "Sữa dưỡng ẩm dạng lỏng dịu nhẹ, thẩm thấu nhanh tức thì không bết rít, dùng được cho cả mặt và toàn thân.",
                true
        );
        ProductSkuEntity p13s1 = createSku(p13, "SKU-CRV-LOT-236ML", "Chai vòi nhấn 236ml", new BigDecimal("340000"), new BigDecimal("385000"), "3337875597210");
        ProductSkuEntity p13s2 = createSku(p13, "SKU-CRV-LOT-473ML", "Chai vòi nhấn 473ml", new BigDecimal("470000"), new BigDecimal("520000"), "3337875597227");
        createImage(p13, p13s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567440/lunea/products/prod_p13_14.jpg", true);
        linkAttributes(p13, attrs, "Da Khô", "Da Nhạy Cảm", "Phục Hồi Da", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p13s1, 45, 30, 20);
        createInventoryAllStores(stores, p13s2, 35, 25, 15);

        // 14. CeraVe Foaming Facial Cleanser
        ProductEntity p14 = createProduct(
                "Sữa Rửa Mặt Tạo Bọt CeraVe Foaming Facial Cleanser Cho Da Dầu Mụn",
                brands.get("CeraVe"),
                categories.get("CLEANSER"),
                "Mỹ",
                "3 Ceramide thiết yếu, Niacinamide, Hyaluronic Acid",
                "Làm sạch sâu bã nhờn mà không phá vỡ hàng rào ẩm tự nhiên.",
                "Sữa rửa mặt tạo bọt mịn nhẹ nhàng cuốn trôi dầu thừa và cặn trang điểm, duy trì độ pH lý tưởng 5.5 cho da.",
                false
        );
        ProductSkuEntity p14s1 = createSku(p14, "SKU-CRV-FOAM-236ML", "Chai 236ml", new BigDecimal("320000"), new BigDecimal("360000"), "3337875597111");
        ProductSkuEntity p14s2 = createSku(p14, "SKU-CRV-FOAM-473ML", "Chai 473ml", new BigDecimal("450000"), new BigDecimal("495000"), "3337875597128");
        createImage(p14, p14s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567440/lunea/products/prod_p14_15.jpg", true);
        linkAttributes(p14, attrs, "Da Dầu", "Da Hỗn Hợp", "Trị Mụn");
        createInventoryAllStores(stores, p14s1, 50, 35, 25);
        createInventoryAllStores(stores, p14s2, 40, 30, 20);

        // 15. Anessa Perfect UV Sunscreen Skincare Milk
        ProductEntity p15 = createProduct(
                "Sữa Chống Nắng Dưỡng Da Kiềm Dầu Anessa Perfect UV Sunscreen Skincare Milk SPF50+",
                brands.get("Anessa"),
                categories.get("SUNSCREEN"),
                "Nhật Bản",
                "Công nghệ Auto Booster, Chiết xuất Trà xanh Uji Kyoto, Collagen",
                "Chống nước, chống mồ hôi và ma sát cực mạnh, dưỡng da ẩm mịn suốt ngày dài.",
                "Kem chống nắng dạng sữa mỏng nhẹ số 1 Nhật Bản giúp bảo vệ da hoàn hảo trước tia UV khắc nghiệt nhất.",
                true
        );
        ProductSkuEntity p15s1 = createSku(p15, "SKU-ANS-MILK-20ML", "Chai 20ml Mini", new BigDecimal("245000"), new BigDecimal("275000"), "4909978120610");
        ProductSkuEntity p15s2 = createSku(p15, "SKU-ANS-MILK-60ML", "Chai 60ml Fullsize", new BigDecimal("615000"), new BigDecimal("685000"), "4909978120627");
        createImage(p15, p15s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567441/lunea/products/prod_p15_16.jpg", true);
        linkAttributes(p15, attrs, "Da Dầu", "Da Hỗn Hợp", "Chống Lão Hóa");
        createInventoryAllStores(stores, p15s1, 35, 25, 20);
        createInventoryAllStores(stores, p15s2, 45, 30, 25);

        // 16. L'Oréal Paris Pure Hyaluronic Acid 1.5%
        ProductEntity p16 = createProduct(
                "Serum Siêu Cấp Ẩm & Sáng Mịn Da L'Oréal Paris Pure Hyaluronic Acid 1.5%",
                brands.get("L'Oréal Paris"),
                categories.get("SERUM"),
                "Pháp",
                "1.5% Hyaluronic Acid đa phân tử, Glycerin",
                "Cấp ẩm tức thì, làm đầy nếp nhăn li ti và giúp da căng bóng rạng rỡ.",
                "Serum dưỡng ẩm bán chạy toàn cầu của L'Oréal với phân tử HA siêu nhỏ thẩm thấu sâu đến tận tầng hạ bì của da.",
                true
        );
        ProductSkuEntity p16s1 = createSku(p16, "SKU-LOR-HA-30ML", "Dung tích 30ml", new BigDecimal("399000"), new BigDecimal("449000"), "8992304037512");
        ProductSkuEntity p16s2 = createSku(p16, "SKU-LOR-HA-50ML", "Dung tích 50ml", new BigDecimal("580000"), new BigDecimal("640000"), "8992304037529");
        createImage(p16, p16s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567442/lunea/products/prod_p16_17.jpg", true);
        linkAttributes(p16, attrs, "Da Khô", "Da Nhạy Cảm", "Dưỡng Ẩm Chuyên Sâu", "Dưỡng Sáng Mờ Thâm");
        createInventoryAllStores(stores, p16s1, 40, 30, 20);
        createInventoryAllStores(stores, p16s2, 30, 20, 15);

        // 17. Laneige Water Sleeping Mask EX
        ProductEntity p17 = createProduct(
                "Mặt Nạ Ngủ Cấp Nước Chuyên Sâu Laneige Water Sleeping Mask EX",
                brands.get("Laneige"),
                categories.get("MASK"),
                "Hàn Quốc",
                "Sleeping Micro Biome, Pro-biotics Complex, Squalane",
                "Thanh lọc da trong lúc ngủ, phục hồi vẻ rạng ngời và căng mọng vào sáng hôm sau.",
                "Mặt nạ ngủ thế hệ mới giúp cân bằng hệ vi sinh da, tăng cường hàng rào bảo vệ cho làn da tươi tắn ngậm nước.",
                false
        );
        ProductSkuEntity p17s1 = createSku(p17, "SKU-LAN-MASK-25ML", "Hũ 25ml Mini", new BigDecimal("180000"), new BigDecimal("210000"), "8809643067114");
        ProductSkuEntity p17s2 = createSku(p17, "SKU-LAN-MASK-70ML", "Hũ 70ml Fullsize", new BigDecimal("680000"), new BigDecimal("750000"), "8809643067121");
        createImage(p17, p17s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567443/lunea/products/prod_p17_18.jpg", true);
        linkAttributes(p17, attrs, "Da Khô", "Phục Hồi Da", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p17s1, 35, 25, 20);
        createInventoryAllStores(stores, p17s2, 30, 20, 15);

        // 18. Laneige Lip Sleeping Mask Berry
        ProductEntity p18 = createProduct(
                "Mặt Nạ Ngủ Dưỡng Ẩm & Phục Hồi Môi Laneige Lip Sleeping Mask Berry",
                brands.get("Laneige"),
                categories.get("LIPSTICK"),
                "Hàn Quốc",
                "Phức hợp Berry Fruit Complex, Vitamin C, Dầu hạt dừa",
                "Nhẹ nhàng loại bỏ tế bào chết trên môi qua đêm, mang lại đôi môi căng mịn hồng hào.",
                "Mặt nạ ngủ môi huyền thoại với hương dâu rừng ngọt ngào tan chảy, loại bỏ hoàn toàn tình trạng khô nẻ bong tróc.",
                true
        );
        ProductSkuEntity p18s1 = createSku(p18, "SKU-LAN-LIP-8G", "Hũ 8g Mini", new BigDecimal("120000"), new BigDecimal("140000"), "8809643068012");
        ProductSkuEntity p18s2 = createSku(p18, "SKU-LAN-LIP-20G", "Hũ 20g Fullsize", new BigDecimal("410000"), new BigDecimal("460000"), "8809643068029");
        createImage(p18, p18s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567444/lunea/products/prod_p18_19.jpg", true);
        linkAttributes(p18, attrs, "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p18s1, 50, 35, 25);
        createInventoryAllStores(stores, p18s2, 40, 30, 20);

        // 19. Laneige Neo Cushion Matte SPF42 PA++
        ProductEntity p19 = createProduct(
                "Phấn Nước Kiềm Dầu Che Phủ Hoàn Hảo Laneige Neo Cushion Matte SPF42 PA++",
                brands.get("Laneige"),
                categories.get("FOUNDATION"),
                "Hàn Quốc",
                "Hạt phấn siêu mịn, Bột hấp thụ bã nhờn, Panthenol",
                "Che khuyết điểm và lỗ chân lông hoàn hảo, kháng nước kháng mồ hôi suốt 24 giờ.",
                "Phấn nước Neo Cushion Matte mang lại lớp nền siêu nhẹ tênh, mịn lì như nhung không dính vào khẩu trang.",
                true
        );
        ProductSkuEntity p19s1 = createSku(p19, "SKU-LAN-NEO-21N", "Tone 21N Beige Tự Nhiên (Kèm Lõi Thay Thế)", new BigDecimal("630000"), new BigDecimal("700000"), "8809643069019");
        ProductSkuEntity p19s2 = createSku(p19, "SKU-LAN-NEO-23N", "Tone 23N Sand Da Ngăm (Kèm Lõi Thay Thế)", new BigDecimal("630000"), new BigDecimal("700000"), "8809643069026");
        createImage(p19, p19s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567445/lunea/products/prod_p19_20.jpg", true);
        linkAttributes(p19, attrs, "Da Dầu", "Da Hỗn Hợp");
        createInventoryAllStores(stores, p19s1, 25, 20, 15);
        createInventoryAllStores(stores, p19s2, 25, 15, 10);

        // 20. Son Thỏi Lì Huyền Thoại MAC Matte Lipstick
        ProductEntity p20 = createProduct(
                "Son Thỏi Lì Huyền Thoại MAC Matte Lipstick",
                brands.get("MAC Cosmetics"),
                categories.get("LIPSTICK"),
                "Mỹ",
                "Sáp vi tinh thể, Sắc tố khoáng siêu mịn, Dầu dưỡng thực vật",
                "Lên màu chuẩn sắc ngay lần quẹt đầu tiên, lưu giữ màu sắc tươi tắn suốt cả ngày dài.",
                "Biểu tượng làm đẹp toàn cầu của MAC với công thức son thỏi lì trứ danh, chất son lướt êm ái trên bờ môi.",
                true
        );
        ProductSkuEntity p20s1 = createSku(p20, "SKU-MAC-LIP-CHILI", "Màu Chili - Đỏ Gạch Huyền Thoại", new BigDecimal("580000"), new BigDecimal("650000"), "077360205211");
        ProductSkuEntity p20s2 = createSku(p20, "SKU-MAC-LIP-MARRAK", "Màu Marrakesh - Đỏ Nâu Trầm Ấm", new BigDecimal("580000"), new BigDecimal("650000"), "077360205212");
        ProductSkuEntity p20s3 = createSku(p20, "SKU-MAC-LIP-RUBY", "Màu Ruby Woo - Đỏ Cổ Điển", new BigDecimal("580000"), new BigDecimal("650000"), "077360205213");
        createImage(p20, p20s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567446/lunea/products/prod_p20_21.jpg", true);
        createInventoryAllStores(stores, p20s1, 35, 25, 20);
        createInventoryAllStores(stores, p20s2, 30, 20, 15);
        createInventoryAllStores(stores, p20s3, 30, 25, 15);

        // 21. Kem Nền Maybelline Fit Me Matte + Poreless
        ProductEntity p21 = createProduct(
                "Kem Nền Mịn Lì Kiềm Dầu Maybelline Fit Me Matte + Poreless Foundation",
                brands.get("Maybelline New York"),
                categories.get("FOUNDATION"),
                "Mỹ",
                "Hạt phấn Micro-powders, Đất sét khoáng kiềm dầu",
                "Kiềm dầu và thu nhỏ lỗ chân lông, tạo hiệu ứng mịn lì tự nhiên không bóng nhờn.",
                "Dòng kem nền quốc dân với khả năng kiềm dầu đỉnh cao, tiệp chuẩn từng tone da châu Á.",
                false
        );
        ProductSkuEntity p21s1 = createSku(p21, "SKU-MAY-FIT-112", "Tone 112 Natural Ivory Sáng Tự Nhiên", new BigDecimal("235000"), new BigDecimal("260000"), "041554538111");
        ProductSkuEntity p21s2 = createSku(p21, "SKU-MAY-FIT-120", "Tone 120 Classic Ivory Da Trung Bình Sáng", new BigDecimal("235000"), new BigDecimal("260000"), "041554538128");
        ProductSkuEntity p21s3 = createSku(p21, "SKU-MAY-FIT-128", "Tone 128 Warm Nude Da Tự Nhiên / Hơi Ngăm", new BigDecimal("235000"), new BigDecimal("260000"), "041554538135");
        createImage(p21, p21s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567447/lunea/products/prod_p21_22.jpg", true);
        linkAttributes(p21, attrs, "Da Dầu", "Da Hỗn Hợp");
        createInventoryAllStores(stores, p21s1, 40, 25, 20);
        createInventoryAllStores(stores, p21s2, 40, 30, 20);
        createInventoryAllStores(stores, p21s3, 35, 25, 15);

        // 22. Maybelline Hypercurl Waterproof Mascara
        ProductEntity p22 = createProduct(
                "Chuốt Mi Dài & Dày Mi Chống Trôi Maybelline Hypercurl Waterproof Mascara",
                brands.get("Maybelline New York"),
                categories.get("EYE_MAKEUP"),
                "Mỹ",
                "Sáp tự nhiên, Hợp chất Polymer định hình mi",
                "Giúp hàng mi cong vút và dày gấp 3 lần suốt 36 giờ, chống lem chống trôi hiệu quả.",
                "Mascara huyền thoại giữ nếp mi cong vút trong điều kiện thời tiết nóng ẩm tại Việt Nam.",
                false
        );
        ProductSkuEntity p22s1 = createSku(p22, "SKU-MAY-MASC-BLK", "Màu Đen Tự Nhiên - Chuốt mi 9.2ml", new BigDecimal("155000"), new BigDecimal("180000"), "041554539019");
        createImage(p22, p22s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567448/lunea/products/prod_p22_23.jpg", true);
        createInventoryAllStores(stores, p22s1, 50, 35, 25);

        // 23. Phấn Má Hồng MAC Mineralize Blush
        ProductEntity p23 = createProduct(
                "Phấn Má Hồng Mịn Nhẹ Ánh Nhũ MAC Mineralize Blush",
                brands.get("MAC Cosmetics"),
                categories.get("BLUSH"),
                "Mỹ",
                "Khoáng chất nung, Hạt nhũ ngọc trai siêu mịn",
                "Tạo đôi gò má ửng hồng tươi tắn, bắt sáng tự nhiên tôn đường nét khuôn mặt.",
                "Phấn má nung chứa các khoáng chất tinh khiết giúp gò má sáng bừng sức sống với sắc màu tinh tế.",
                false
        );
        ProductSkuEntity p23s1 = createSku(p23, "SKU-MAC-BLUSH-WARM", "Màu Warm Soul - Cam Đào Ánh Nhũ", new BigDecimal("790000"), new BigDecimal("890000"), "077360215111");
        ProductSkuEntity p23s2 = createSku(p23, "SKU-MAC-BLUSH-LIKE", "Màu Like Me, Love Me - Cam San Hô", new BigDecimal("790000"), new BigDecimal("890000"), "077360215112");
        createImage(p23, p23s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567449/lunea/products/prod_p23_24.jpg", true);
        createInventoryAllStores(stores, p23s1, 25, 20, 15);
        createInventoryAllStores(stores, p23s2, 25, 15, 10);

        // 24. Tẩy Tế Bào Chết Cà Phê Đắk Lắk Cocoon
        ProductEntity p24 = createProduct(
                "Tẩy Tế Bào Chết Cơ Thể Cà Phê Đắk Lắk Cocoon Dak Lak Coffee Body Polish",
                brands.get("Cocoon Vietnam"),
                categories.get("BODY_SCRUB"),
                "Việt Nam",
                "Hạt cà phê Đắk Lắk xay nhuyễn, Bơ ca cao Tiền Giang, Dầu dừa",
                "Loại bỏ lớp sừng già cỗi, làm đều màu da và nuôi dưỡng làn da body mềm mịn như nhung.",
                "Tẩy tế bào chết cơ thể thuần chay thơm lừng hương cà phê Việt Nam, đánh bay viêm lỗ chân lông và thâm sần.",
                true
        );
        ProductSkuEntity p24s1 = createSku(p24, "SKU-CCN-SCRUB-200ML", "Hũ 200ml", new BigDecimal("125000"), new BigDecimal("145000"), "893851500007");
        ProductSkuEntity p24s2 = createSku(p24, "SKU-CCN-SCRUB-600ML", "Hũ 600ml Siêu Tiết Kiệm", new BigDecimal("265000"), new BigDecimal("310000"), "893851500008");
        createImage(p24, p24s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567450/lunea/products/prod_p24_25.jpg", true);
        linkAttributes(p24, attrs, "Dưỡng Sáng Mờ Thâm", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p24s1, 60, 40, 30);
        createInventoryAllStores(stores, p24s2, 45, 30, 20);

        // 25. Dầu Gội Bưởi Cocoon
        ProductEntity p25 = createProduct(
                "Dầu Gội Bưởi Không Sulfate Giảm Rụng Tóc Cocoon Pomelo Shampoo",
                brands.get("Cocoon Vietnam"),
                categories.get("HAIR_CARE"),
                "Việt Nam",
                "Tinh dầu vỏ bưởi nguyên chất, Xylishine, Vitamin B5",
                "Làm sạch dịu nhẹ tóc và da đầu, giảm rụng tóc và nuôi dưỡng tóc bồng bềnh chắc khỏe.",
                "Dầu gội thuần chay không chứa chất tẩy rửa sulfate mạnh, dịu nhẹ cho da đầu nhạy cảm và tóc hư tổn.",
                false
        );
        ProductSkuEntity p25s1 = createSku(p25, "SKU-CCN-SHAMP-310ML", "Chai 310ml", new BigDecimal("225000"), new BigDecimal("255000"), "893851500009");
        ProductSkuEntity p25s2 = createSku(p25, "SKU-CCN-SHAMP-500ML", "Chai 500ml Có Vòi Pump", new BigDecimal("345000"), new BigDecimal("395000"), "893851500010");
        createImage(p25, p25s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567451/lunea/products/prod_p25_26.jpg", true);
        linkAttributes(p25, attrs, "Phục Hồi Da");
        createInventoryAllStores(stores, p25s1, 40, 30, 20);
        createInventoryAllStores(stores, p25s2, 35, 25, 15);

        // 26. Estée Lauder Advanced Night Repair
        ProductEntity p26 = createProduct(
                "Serum Chống Lão Hóa Ban Đêm Estée Lauder Advanced Night Repair",
                brands.get("Estée Lauder"),
                categories.get("SERUM"),
                "Mỹ",
                "Công nghệ Chronolux Power Signal, Hyaluronic Acid, Men Bifida",
                "Phục hồi làn da ban đêm, giảm nếp nhăn rõ rệt và tăng độ săn chắc đàn hồi.",
                "Tinh chất chống lão hóa bán chạy số 1 tại Mỹ giúp đánh thức làn da tươi trẻ, rạng ngời và căng tràn sức sống.",
                true
        );
        ProductSkuEntity p26s1 = createSku(p26, "SKU-EL-ANR-30ML", "Dung tích 30ml", new BigDecimal("2100000"), new BigDecimal("2350000"), "027131267111");
        ProductSkuEntity p26s2 = createSku(p26, "SKU-EL-ANR-50ML", "Dung tích 50ml", new BigDecimal("3150000"), new BigDecimal("3500000"), "027131267128");
        createImage(p26, p26s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567452/lunea/products/prod_p26_27.jpg", true);
        linkAttributes(p26, attrs, "Chống Lão Hóa", "Phục Hồi Da", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p26s1, 20, 15, 10);
        createInventoryAllStores(stores, p26s2, 15, 10, 8);

        // 27. Klairs Rich Moist Soothing Cream
        ProductEntity p27 = createProduct(
                "Kem Dưỡng Ẩm Làm Dịu Da Khô & Nhạy Cảm Klairs Rich Moist Soothing Cream",
                brands.get("Klairs"),
                categories.get("CREAM"),
                "Hàn Quốc",
                "Chiết xuất nấm men Beta-Glucan, Dầu jojoba, Bơ hạt mỡ",
                "Cung cấp độ ẩm tức thì và duy trì độ ẩm bền vững cho làn da khô nhạy cảm.",
                "Kem dưỡng lành tính giúp hạ nhiệt và xoa dịu làn da kích ứng, củng cố màng lipid tự nhiên của biểu bì.",
                false
        );
        ProductSkuEntity p27s1 = createSku(p27, "SKU-KLR-CRM-80ML", "Tuýp 80ml", new BigDecimal("375000"), new BigDecimal("420000"), "8809572890113");
        createImage(p27, p27s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567452/lunea/products/prod_p27_28.jpg", true);
        linkAttributes(p27, attrs, "Da Khô", "Da Nhạy Cảm", "Phục Hồi Da", "Dưỡng Ẩm Chuyên Sâu");
        createInventoryAllStores(stores, p27s1, 35, 25, 15);

        // 28. Vichy Mineralizing Thermal Spa Water
        ProductEntity p28 = createProduct(
                "Xịt Khoáng Dưỡng Da Làm Dịu Tức Thì Vichy Mineralizing Thermal Spa Water",
                brands.get("Vichy"),
                categories.get("TONER"),
                "Pháp",
                "100% Nước khoáng núi lửa Pháp nguyên chất chứa 15 khoáng chất quý",
                "Làm dịu mát da tức thì, củng cố hàng rào phòng vệ và bảo vệ da trước tác hại môi trường.",
                "Nguồn khoáng quý hiếm từ vùng núi lửa Auvergne ngàn năm tuổi tại Pháp, tinh khiết và an toàn tuyệt đối cho mọi loại da.",
                false
        );
        ProductSkuEntity p28s1 = createSku(p28, "SKU-VIC-SPA-50ML", "Chai xịt 50ml Bỏ Túi", new BigDecimal("145000"), new BigDecimal("165000"), "3337871308612");
        ProductSkuEntity p28s2 = createSku(p28, "SKU-VIC-SPA-150ML", "Chai xịt 150ml Tiêu Chuẩn", new BigDecimal("255000"), new BigDecimal("290000"), "3337871308629");
        ProductSkuEntity p28s3 = createSku(p28, "SKU-VIC-SPA-300ML", "Chai xịt 300ml Tiết Kiệm", new BigDecimal("395000"), new BigDecimal("450000"), "3337871308636");
        createImage(p28, p28s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567453/lunea/products/prod_p28_29.jpg", true);
        linkAttributes(p28, attrs, "Da Nhạy Cảm", "Phục Hồi Da");
        createInventoryAllStores(stores, p28s1, 50, 35, 25);
        createInventoryAllStores(stores, p28s2, 40, 30, 20);
        createInventoryAllStores(stores, p28s3, 35, 25, 15);

        // 29. Nước Hoa LUNEA Midnight Rose Eau De Parfum
        ProductEntity p29 = createProduct(
                "Nước Hoa Nữ Cao Cấp LUNEA Midnight Rose Eau De Parfum",
                brands.get("Estée Lauder"),
                categories.get("PERFUME"),
                "Pháp",
                "Hoa hồng Damask, Quả mọng đen, Gỗ tuyết tùng, Xạ hương",
                "Lưu hương quyến rũ nồng nàn từ 8 đến 12 giờ, tỏa hương tinh tế quý phái.",
                "Tuyệt tác hương thơm mang phong cách Paris hoa lệ với nốt hương hoa hồng huyền bí hòa quyện cùng xạ hương ấm áp.",
                true
        );
        ProductSkuEntity p29s1 = createSku(p29, "SKU-LUN-ROSE-50ML", "Chai 50ml Eau de Parfum", new BigDecimal("1850000"), new BigDecimal("2100000"), "893888800029");
        ProductSkuEntity p29s2 = createSku(p29, "SKU-LUN-ROSE-100ML", "Chai 100ml Eau de Parfum", new BigDecimal("2750000"), new BigDecimal("3100000"), "893888800030");
        createImage(p29, p29s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567454/lunea/products/prod_p29_30.jpg", true);
        createInventoryAllStores(stores, p29s1, 25, 15, 10);
        createInventoryAllStores(stores, p29s2, 15, 10, 8);

        // 30. Xịt Thơm LUNEA Golden Amber Body Mist
        ProductEntity p30 = createProduct(
                "Xịt Thơm Toàn Thân & Tóc LUNEA Golden Amber Body Mist",
                brands.get("Cocoon Vietnam"),
                categories.get("BODY_MIST"),
                "Việt Nam",
                "Tinh dầu hổ phách, Hoa vani Madagascar, Nước cất hoa cam",
                "Lưu hương nhẹ nhàng suốt 4-6 giờ, tạo cảm giác thư giãn sảng khoái và dưỡng ẩm nhẹ cho tóc.",
                "Xịt thơm body mist dịu ngọt quý phái mang lại sự tự tin và hương thơm lôi cuốn mọi ánh nhìn.",
                false
        );
        ProductSkuEntity p30s1 = createSku(p30, "SKU-LUN-MIST-150ML", "Chai xịt 150ml", new BigDecimal("245000"), new BigDecimal("280000"), "893888800031");
        createImage(p30, p30s1, "https://res.cloudinary.com/p3kv8nx8/image/upload/v1792567455/lunea/products/prod_p30_31.jpg", true);
        createInventoryAllStores(stores, p30s1, 40, 30, 20);
    }

    private ProductEntity createProduct(
            String name, BrandEntity brand, CategoryEntity category,
            String origin, String ingredients, String uses, String description, boolean featured
    ) {
        ProductEntity existing = productRepository.findByName(name).orElse(null);
        if (existing != null) {
            return existing;
        }
        return productRepository.save(ProductEntity.builder()
                .name(name)
                .brand(brand)
                .category(category)
                .origin(origin)
                .mainIngredients(ingredients)
                .uses(uses)
                .description(description)
                .isFeatured(featured)
                .status(ActiveStatus.ACTIVE)
                .build());
    }

    private ProductSkuEntity createSku(
            ProductEntity product, String skuCode, String variantName,
            BigDecimal price, BigDecimal listPrice, String barcode
    ) {
        ProductSkuEntity existing = productSkuRepository.findBySkuCode(skuCode).orElse(null);
        if (existing != null) {
            return existing;
        }
        return productSkuRepository.save(ProductSkuEntity.builder()
                .product(product)
                .skuCode(skuCode)
                .variantName(variantName)
                .price(price)
                .listPrice(listPrice)
                .barcode(barcode)
                .status(ActiveStatus.ACTIVE)
                .build());
    }

    private void createImage(ProductEntity product, ProductSkuEntity sku, String url, boolean isPrimary) {
        if (!productImageRepository.existsByProductIdAndImageUrl(product.getId(), url)) {
            productImageRepository.save(ProductImageEntity.builder()
                    .product(product)
                    .productSku(sku)
                    .imageUrl(url)
                    .isPrimary(isPrimary)
                    .build());
        }
    }

    private void linkAttributes(ProductEntity product, Map<String, AttributeEntity> attrs, String... names) {
        for (String name : names) {
            AttributeEntity a = attrs.get(name);
            if (a != null && !productAttributeValueRepository.existsByProductIdAndAttributeId(product.getId(), a.getId())) {
                productAttributeValueRepository.save(ProductAttributeValueEntity.builder()
                        .product(product)
                        .attribute(a)
                        .value(a.getName())
                        .build());
            }
        }
    }

    private void createInventory(StoreEntity store, ProductSkuEntity sku, int actual, int held) {
        if (inventoryRepository.findByStoreIdAndSkuId(store.getId(), sku.getId()).isEmpty()) {
            inventoryRepository.save(InventoryEntity.builder()
                    .store(store)
                    .sku(sku)
                    .actualStock(actual)
                    .heldQuantity(held)
                    .minimumStock(5)
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
    }

    private void createInventoryAllStores(List<StoreEntity> stores, ProductSkuEntity sku, int hcmQty, int hnQty, int dnQty) {
        if (stores.size() > 0) createInventory(stores.get(0), sku, hcmQty, 0);
        if (stores.size() > 1) createInventory(stores.get(1), sku, hnQty, 0);
        if (stores.size() > 2) createInventory(stores.get(2), sku, dnQty, 0);
    }

    public void ensureDemoAccountsExist() {
        log.info(">>> [DataSeeder] Kiểm tra và đảm bảo các tài khoản mẫu nghiệp vụ tồn tại...");

        // 1. Đảm bảo permissions
        List<PermissionEntity> allExistingPerms = permissionRepository.findAll();
        Map<String, PermissionEntity> permMap = new HashMap<>();
        for (PermissionEntity p : allExistingPerms) {
            permMap.put(p.getCode(), p);
        }

        List<PermissionEntity> requiredPerms = List.of(
                PermissionEntity.builder().code("CATALOG_MANAGE").name("Quản lý danh mục & sản phẩm").description("Toàn quyền thêm/sửa/xóa sản phẩm và thương hiệu").build(),
                PermissionEntity.builder().code("ORDER_VIEW").name("Xem đơn hàng").description("Xem danh sách và chi tiết đơn hàng trong phạm vi chi nhánh").build(),
                PermissionEntity.builder().code("ORDER_PROCESS").name("Xử lý đơn hàng").description("Duyệt trạng thái đơn hàng, hủy đơn theo phạm vi chi nhánh").build(),
                PermissionEntity.builder().code("INVENTORY_VIEW").name("Tra cứu tồn kho").description("Xem tồn kho các sản phẩm theo chi nhánh").build(),
                PermissionEntity.builder().code("INVENTORY_ADJUST").name("Điều chỉnh tồn kho").description("Cập nhật số lượng tồn kho thực tế kèm lý do").build(),
                PermissionEntity.builder().code("STORE_MANAGE").name("Quản lý chi nhánh").description("Thêm, sửa thông tin các chi nhánh chuỗi cửa hàng").build(),
                PermissionEntity.builder().code("EMPLOYEE_MANAGE").name("Quản lý nhân viên").description("Quản lý tài khoản, phân vai trò và gán chi nhánh").build(),
                PermissionEntity.builder().code("VOUCHER_MANAGE").name("Quản lý voucher").description("Quản lý mã giảm giá khuyến mãi").build(),
                PermissionEntity.builder().code("PURCHASE_MANAGE").name("Quản lý nhập hàng").description("Quản lý nhà cung cấp và xác nhận phiếu nhập").build(),
                PermissionEntity.builder().code("CUSTOMER_MANAGE").name("Quản lý khách hàng").description("Xem và khóa/mở khóa tài khoản khách").build(),
                PermissionEntity.builder().code("REPORT_VIEW").name("Xem báo cáo thống kê").description("Xem dashboard doanh thu và biểu đồ đơn").build(),
                PermissionEntity.builder().code("AUDIT_VIEW").name("Xem nhật ký kiểm toán").description("Tra cứu lịch sử thao tác hệ thống").build()
        );

        for (PermissionEntity req : requiredPerms) {
            if (!permMap.containsKey(req.getCode())) {
                PermissionEntity saved = permissionRepository.save(req);
                permMap.put(saved.getCode(), saved);
            }
        }

        // 2. Đảm bảo roles
        RoleEntity adminRole = roleRepository.findByName("ADMIN").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("ADMIN").description("Quản trị viên toàn hệ thống").build()));
        RoleEntity orderRole = roleRepository.findByName("ORDER_STAFF").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("ORDER_STAFF").description("Nhân viên xử lý đơn hàng chi nhánh").build()));
        RoleEntity warehouseRole = roleRepository.findByName("WAREHOUSE_STAFF").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("WAREHOUSE_STAFF").description("Nhân viên thủ kho chi nhánh").build()));
        RoleEntity productRole = roleRepository.findByName("PRODUCT_MANAGER").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("PRODUCT_MANAGER").description("Quản lý danh mục sản phẩm").build()));

        // Gán permissions cho roles nếu chưa có
        for (PermissionEntity p : permMap.values()) {
            assignPermIfMissing(adminRole, p);
        }
        assignPermIfMissing(orderRole, permMap.get("ORDER_VIEW"));
        assignPermIfMissing(orderRole, permMap.get("ORDER_PROCESS"));
        assignPermIfMissing(orderRole, permMap.get("INVENTORY_VIEW"));

        assignPermIfMissing(warehouseRole, permMap.get("INVENTORY_VIEW"));
        assignPermIfMissing(warehouseRole, permMap.get("INVENTORY_ADJUST"));
        assignPermIfMissing(warehouseRole, permMap.get("PURCHASE_MANAGE"));

        assignPermIfMissing(productRole, permMap.get("CATALOG_MANAGE"));
        assignPermIfMissing(productRole, permMap.get("INVENTORY_VIEW"));

        // 3. Tìm chi nhánh HCM, HN, ĐN
        List<StoreEntity> stores = storeRepository.findAll();
        StoreEntity storeHcm = stores.stream()
                .filter(s -> s.getName() != null && (s.getName().contains("HCM") || s.getName().contains("Đồng Khởi") || s.getName().contains("Hồ Chí Minh")))
                .findFirst()
                .orElse(stores.isEmpty() ? null : stores.get(0));
        StoreEntity storeHn = stores.stream()
                .filter(s -> s.getName() != null && (s.getName().contains("Hà Nội") || s.getName().contains("Cầu Giấy")))
                .findFirst()
                .orElse(stores.size() > 1 ? stores.get(1) : storeHcm);
        StoreEntity storeDn = stores.stream()
                .filter(s -> s.getName() != null && (s.getName().contains("Đà Nẵng") || s.getName().contains("Bạch Đằng")))
                .findFirst()
                .orElse(stores.size() > 2 ? stores.get(2) : storeHn);

        // 4. Đồng bộ FULL các tài khoản mẫu nghiệp vụ cho tất cả các Role & Chi nhánh:
        // A. Nhóm Quản trị & Quản lý
        ensureEmployeeAccount("admin", "admin@lunea.test", "0900000001", "Nguyễn Quản Trị Hệ Thống", "123456", adminRole, storeHcm);
        ensureEmployeeAccount("product_manager", "product.manager@lunea.test", "0900000005", "Hoàng Minh Long (Quản lý sản phẩm)", "123456", productRole, storeHcm);

        // B. Nhóm Thủ kho chi nhánh (WAREHOUSE_STAFF)
        ensureEmployeeAccount("warehouse_hcm", "warehouse.hcm@lunea.test", "0900000004", "Phạm Văn Kho (Thủ kho HCM)", "123456", warehouseRole, storeHcm);
        ensureEmployeeAccount("warehouse_hn", "warehouse.hn@lunea.test", "0900000006", "Nguyễn Văn Tuấn (Thủ kho Hà Nội)", "123456", warehouseRole, storeHn);
        ensureEmployeeAccount("warehouse_dn", "warehouse.dn@lunea.test", "0900000007", "Đặng Thành Nam (Thủ kho Đà Nẵng)", "123456", warehouseRole, storeDn);

        // C. Nhóm Xử lý đơn hàng chi nhánh (ORDER_STAFF)
        ensureEmployeeAccount("order_hcm", "order.hcm@lunea.test", "0900000002", "Trần Minh Quân (Đơn hàng HCM)", "123456", orderRole, storeHcm);
        ensureEmployeeAccount("order_hn", "order.hn@lunea.test", "0900000003", "Lê Thu Hà (Đơn hàng Hà Nội)", "123456", orderRole, storeHn);
        ensureEmployeeAccount("order_dn", "order.dn@lunea.test", "0900000008", "Bùi Phương Thảo (Đơn hàng Đà Nẵng)", "123456", orderRole, storeDn);

        // D. Nhóm Khách hàng mua sắm (CUSTOMER)
        ensureCustomerAccount("khach01", "khach01@lunea.test", "0901234567", "Lê Hoàng Yến", "123456", 120,
                "Số 45 Đường Lê Duẩn", "Phường Bến Nghé", "Quận 1", "Hồ Chí Minh");
        ensureCustomerAccount("khach02", "khach02@lunea.test", "0902345678", "Nguyễn Minh Trang", "123456", 85,
                "Số 18 Đường Cầu Giấy", "Phường Quan Hoa", "Quận Cầu Giấy", "Hà Nội");
        ensureCustomerAccount("khach03", "khach03@lunea.test", "0903456789", "Vũ Thanh Thảo", "123456", 350,
                "Số 120 Đường Bạch Đằng", "Phường Hải Châu 1", "Quận Hải Châu", "Đà Nẵng");

        // E. Quét bổ sung tự động cho bất kỳ Account nào chưa có Customer/Employee entity tương ứng
        List<AccountEntity> allAccounts = accountRepository.findAll();
        for (AccountEntity acc : allAccounts) {
            if (acc.getAccountType() == AccountType.CUSTOMER) {
                if (customerRepository.findByAccountId(acc.getId()).isEmpty()) {
                    String rawName = acc.getUsername();
                    if (acc.getEmail() != null && acc.getEmail().contains("@")) {
                        rawName = acc.getEmail().split("@")[0];
                    }
                    String displayName = rawName.substring(0, 1).toUpperCase() + rawName.substring(1);
                    CustomerEntity newCust = customerRepository.save(CustomerEntity.builder()
                            .account(acc)
                            .fullName(displayName)
                            .dob(LocalDate.of(1998, 5, 15))
                            .gender(Gender.FEMALE)
                            .loyaltyPoints(100)
                            .joinDate(LocalDate.now().minusMonths(2))
                            .build());

                    if (customerAddressRepository != null) {
                        customerAddressRepository.save(CustomerAddressEntity.builder()
                                .customerEntity(newCust)
                                .recipientName(displayName)
                                .phone(acc.getPhone() != null ? acc.getPhone() : "0901234567")
                                .addressDetail("Số 88 Đường Nguyễn Huệ")
                                .ward("Phường Bến Nghé")
                                .district("Quận 1")
                                .province("Hồ Chí Minh")
                                .isDefault(true)
                                .build());
                    }

                    if (cartRepository != null) {
                        cartRepository.findByCustomerId(newCust.getId()).orElseGet(() ->
                                cartRepository.save(CartEntity.builder()
                                        .customer(newCust)
                                        .updatedAt(LocalDateTime.now())
                                        .build()));
                    }
                    log.info(">>> [DataSeeder] Đã tự động tạo hồ sơ khách hàng cho tài khoản: {}", acc.getUsername());
                }
            } else if (acc.getAccountType() == AccountType.EMPLOYEE) {
                if (employeeRepository.findByAccountId(acc.getId()).isEmpty()) {
                    EmployeeEntity newEmp = employeeRepository.save(EmployeeEntity.builder()
                            .account(acc)
                            .fullName(acc.getUsername())
                            .internalEmail(acc.getEmail())
                            .phone(acc.getPhone())
                            .status(ActiveStatus.ACTIVE)
                            .build());
                    employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(newEmp).role(orderRole).build());
                    if (storeHcm != null) {
                        employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(newEmp).store(storeHcm).isPrimary(true).build());
                    }
                    log.info(">>> [DataSeeder] Đã tự động tạo nhân sự cho tài khoản: {}", acc.getUsername());
                }
            }
        }
    }

    private void assignPermIfMissing(RoleEntity role, PermissionEntity perm) {
        if (role == null || perm == null) return;
        List<RolePermissionEntity> list = rolePermissionRepository.findByRoleIdIn(List.of(role.getId()));
        boolean exists = list.stream().anyMatch(rp -> rp.getPermission() != null && perm.getId().equals(rp.getPermission().getId()));
        if (!exists) {
            rolePermissionRepository.save(RolePermissionEntity.builder().role(role).permission(perm).build());
        }
    }

    private void ensureEmployeeAccount(String username, String email, String phone, String fullName, String rawPassword, RoleEntity role, StoreEntity store) {
        String defaultPasswordHash = passwordEncoder.encode(rawPassword);
        AccountEntity account = accountRepository.findByUsername(username)
                .or(() -> accountRepository.findByEmail(email))
                .orElse(null);

        if (account == null) {
            account = accountRepository.save(AccountEntity.builder()
                    .username(username)
                    .email(email)
                    .phone(phone)
                    .passwordHash(defaultPasswordHash)
                    .accountType(AccountType.EMPLOYEE)
                    .status(ActiveStatus.ACTIVE)
                    .build());
            log.info(">>> [DataSeeder] Đã tạo tài khoản demo: {} / {}", username, rawPassword);
        } else {
            account.setUsername(username);
            account.setEmail(email);
            account.setPasswordHash(defaultPasswordHash);
            account.setStatus(ActiveStatus.ACTIVE);
            account.setAccountType(AccountType.EMPLOYEE);
            account = accountRepository.save(account);
            log.info(">>> [DataSeeder] Đã đồng bộ tài khoản demo: {} / {}", username, rawPassword);
        }

        EmployeeEntity employee = employeeRepository.findByAccountId(account.getId()).orElse(null);
        if (employee == null) {
            employee = employeeRepository.save(EmployeeEntity.builder()
                    .account(account)
                    .fullName(fullName)
                    .internalEmail(email)
                    .phone(phone)
                    .status(ActiveStatus.ACTIVE)
                    .build());
        } else {
            employee.setStatus(ActiveStatus.ACTIVE);
            if (employee.getFullName() == null || employee.getFullName().isBlank()) {
                employee.setFullName(fullName);
            }
            employee = employeeRepository.save(employee);
        }

        if (role != null) {
            final EmployeeEntity finalEmp = employee;
            List<EmployeeRoleEntity> existingRoles = employeeRoleRepository.findByEmployeeId(employee.getId());
            boolean hasRole = existingRoles.stream().anyMatch(er -> er.getRole() != null && role.getId().equals(er.getRole().getId()));
            if (!hasRole) {
                employeeRoleRepository.save(EmployeeRoleEntity.builder().employee(finalEmp).role(role).build());
            }
        }

        if (store != null) {
            final EmployeeEntity finalEmp = employee;
            List<EmployeeStoreEntity> existingStores = employeeStoreRepository.findByEmployeeId(employee.getId());
            boolean hasStore = existingStores.stream().anyMatch(es -> es.getStore() != null && store.getId().equals(es.getStore().getId()));
            if (!hasStore) {
                employeeStoreRepository.save(EmployeeStoreEntity.builder().employee(finalEmp).store(store).isPrimary(true).build());
            }
        }
    }

    private void ensureCustomerAccount(String username, String email, String phone, String fullName, String rawPassword,
                                       int loyaltyPoints, String addressDetail, String ward, String district, String province) {
        String defaultPasswordHash = passwordEncoder.encode(rawPassword);
        AccountEntity account = accountRepository.findByUsername(username)
                .or(() -> accountRepository.findByEmail(email))
                .orElse(null);

        if (account == null) {
            account = accountRepository.save(AccountEntity.builder()
                    .username(username)
                    .email(email)
                    .phone(phone)
                    .passwordHash(defaultPasswordHash)
                    .accountType(AccountType.CUSTOMER)
                    .status(ActiveStatus.ACTIVE)
                    .build());
            log.info(">>> [DataSeeder] Đã tạo tài khoản khách hàng demo: {} / {}", username, rawPassword);
        } else {
            account.setUsername(username);
            account.setEmail(email);
            account.setPasswordHash(defaultPasswordHash);
            account.setStatus(ActiveStatus.ACTIVE);
            account.setAccountType(AccountType.CUSTOMER);
            account = accountRepository.save(account);
            log.info(">>> [DataSeeder] Đã đồng bộ tài khoản khách hàng demo: {} / {}", username, rawPassword);
        }

        CustomerEntity customer = customerRepository.findByAccountId(account.getId()).orElse(null);
        if (customer == null) {
            customer = customerRepository.save(CustomerEntity.builder()
                    .account(account)
                    .fullName(fullName)
                    .dob(LocalDate.of(1998, 5, 15))
                    .gender(Gender.FEMALE)
                    .loyaltyPoints(loyaltyPoints)
                    .joinDate(LocalDate.now().minusMonths(3))
                    .build());
        } else {
            customer.setFullName(fullName);
            if (customer.getLoyaltyPoints() == null || customer.getLoyaltyPoints() < loyaltyPoints) {
                customer.setLoyaltyPoints(loyaltyPoints);
            }
            customer = customerRepository.save(customer);
        }

        final CustomerEntity finalCust = customer;
        if (customerAddressRepository != null && addressDetail != null) {
            List<CustomerAddressEntity> addresses = customerAddressRepository.findByCustomerEntityId(customer.getId());
            if (addresses.isEmpty()) {
                customerAddressRepository.save(CustomerAddressEntity.builder()
                        .customerEntity(finalCust)
                        .recipientName(fullName)
                        .phone(phone)
                        .addressDetail(addressDetail)
                        .ward(ward)
                        .district(district)
                        .province(province)
                        .isDefault(true)
                        .build());
            }
        }

        if (cartRepository != null) {
            cartRepository.findByCustomerId(customer.getId()).orElseGet(() ->
                    cartRepository.save(CartEntity.builder()
                            .customer(finalCust)
                            .updatedAt(LocalDateTime.now())
                            .build()));
        }
    }

    private void seedVouchers() {
        if (voucherRepository.count() == 0) {
            log.info(">>> [DataSeeder] Khởi tạo danh sách Voucher & Khuyến mãi mẫu...");
            voucherRepository.saveAll(List.of(
                    VoucherEntity.builder()
                            .code("LUNEA10")
                            .name("Ưu đãi thành viên LUNEA 10%")
                            .description("Giảm 10% tối đa 100.000 ₫ cho mọi đơn hàng từ 300.000 ₫")
                            .discountType(VoucherType.PERCENT)
                            .discountValue(new BigDecimal("10.00"))
                            .maxDiscountAmount(new BigDecimal("100000.00"))
                            .minOrderAmount(new BigDecimal("300000.00"))
                            .validFrom(LocalDateTime.now().minusDays(30))
                            .validTo(LocalDateTime.now().plusYears(1))
                            .usageLimit(500)
                            .usedCount(0)
                            .status(ActiveStatus.ACTIVE)
                            .build(),
                    VoucherEntity.builder()
                            .code("GIAM50K")
                            .name("Giảm trực tiếp 50.000 ₫")
                            .description("Giảm ngay 50.000 ₫ tiền mặt cho đơn hàng từ 400.000 ₫")
                            .discountType(VoucherType.FIXED)
                            .discountValue(new BigDecimal("50000.00"))
                            .maxDiscountAmount(new BigDecimal("50000.00"))
                            .minOrderAmount(new BigDecimal("400000.00"))
                            .validFrom(LocalDateTime.now().minusDays(30))
                            .validTo(LocalDateTime.now().plusYears(1))
                            .usageLimit(500)
                            .usedCount(0)
                            .status(ActiveStatus.ACTIVE)
                            .build(),
                    VoucherEntity.builder()
                            .code("FREESHIP")
                            .name("Miễn phí vận chuyển toàn quốc")
                            .description("Miễn phí 100% phí giao hàng tiêu chuẩn cho đơn hàng LUNEA")
                            .discountType(VoucherType.FREESHIP)
                            .discountValue(new BigDecimal("30000.00"))
                            .maxDiscountAmount(new BigDecimal("30000.00"))
                            .minOrderAmount(BigDecimal.ZERO)
                            .validFrom(LocalDateTime.now().minusDays(30))
                            .validTo(LocalDateTime.now().plusYears(1))
                            .usageLimit(1000)
                            .usedCount(0)
                            .status(ActiveStatus.ACTIVE)
                            .build(),
                    VoucherEntity.builder()
                            .code("LUNEAVIP")
                            .name("Đặc quyền VIP LUNEA 20%")
                            .description("Giảm 20% tối đa 300.000 ₫ cho hóa đơn từ 1.000.000 ₫")
                            .discountType(VoucherType.PERCENT)
                            .discountValue(new BigDecimal("20.00"))
                            .maxDiscountAmount(new BigDecimal("300000.00"))
                            .minOrderAmount(new BigDecimal("1000000.00"))
                            .validFrom(LocalDateTime.now().minusDays(10))
                            .validTo(LocalDateTime.now().plusYears(1))
                            .usageLimit(100)
                            .usedCount(0)
                            .status(ActiveStatus.ACTIVE)
                            .build()
            ));
            log.info(">>> [DataSeeder] Đã nạp thành công 4 Voucher mẫu (LUNEA10, GIAM50K, FREESHIP, LUNEAVIP)");
        }
    }

    private void seedSuppliersAndWarehouseData() {
        if (supplierRepository.count() == 0) {
            log.info(">>> [DataSeeder] Khởi tạo danh sách Nhà Cung Cấp mẫu...");
            SupplierEntity sup1 = supplierRepository.save(SupplierEntity.builder()
                    .name("Công ty TNHH L'Oréal Việt Nam")
                    .contactPerson("Lê Thu Hương")
                    .phone("02839101234")
                    .email("contact@loreal.vn")
                    .address("Tầng 10, Vincom Center, 72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP.HCM")
                    .status(ActiveStatus.ACTIVE)
                    .build());

            SupplierEntity sup2 = supplierRepository.save(SupplierEntity.builder()
                    .name("Công ty TNHH Rohto-Mentholatum Việt Nam")
                    .contactPerson("Trần Đình Trọng")
                    .phone("02743743200")
                    .email("info@rohto.com.vn")
                    .address("Số 16 Đường số 5, KCN VSIP 1, Thuận An, Bình Dương")
                    .status(ActiveStatus.ACTIVE)
                    .build());

            SupplierEntity sup3 = supplierRepository.save(SupplierEntity.builder()
                    .name("Nhà phân phối độc quyền Cosrx Việt Nam")
                    .contactPerson("Kim Min Joon")
                    .phone("0909123456")
                    .email("supply@cosrx.vn")
                    .address("Số 45 Nguyễn Huệ, Bến Nghé, Quận 1, TP.HCM")
                    .status(ActiveStatus.ACTIVE)
                    .build());

            SupplierEntity sup4 = supplierRepository.save(SupplierEntity.builder()
                    .name("Công ty TNHH Shiseido Cosmetics Việt Nam")
                    .contactPerson("Nguyễn Hoàng Anh")
                    .phone("02839325555")
                    .email("shiseido-vn@shiseido.com")
                    .address("Tòa nhà Empress Tower, 138-142 Hai Bà Trưng, Quận 1, TP.HCM")
                    .status(ActiveStatus.ACTIVE)
                    .build());

            log.info(">>> [DataSeeder] Đã nạp 4 Nhà Cung Cấp mỹ phẩm chính hãng!");

            List<StoreEntity> stores = storeRepository.findAll();
            List<ProductSkuEntity> skus = productSkuRepository.findAll();
            EmployeeEntity adminEmp = employeeRepository.findAll().stream().findFirst().orElse(null);

            if (!stores.isEmpty() && !skus.isEmpty() && adminEmp != null && purchaseOrderRepository.count() == 0) {
                StoreEntity store1 = stores.get(0);
                ProductSkuEntity sku1 = skus.get(0);
                ProductSkuEntity sku2 = skus.size() > 1 ? skus.get(1) : sku1;

                PurchaseOrderEntity po1 = purchaseOrderRepository.save(PurchaseOrderEntity.builder()
                        .supplier(sup1)
                        .receivingStore(store1)
                        .status(PurchaseOrderStatus.CONFIRMED)
                        .createdBy(adminEmp)
                        .totalAmount(new BigDecimal("15000000.00"))
                        .confirmedAt(LocalDateTime.now().minusDays(2))
                        .build());

                purchaseOrderItemRepository.save(PurchaseOrderItemEntity.builder()
                        .purchaseOrder(po1)
                        .sku(sku1)
                        .quantity(50)
                        .unitPrice(new BigDecimal("180000.00"))
                        .subtotal(new BigDecimal("9000000.00"))
                        .build());

                purchaseOrderItemRepository.save(PurchaseOrderItemEntity.builder()
                        .purchaseOrder(po1)
                        .sku(sku2)
                        .quantity(30)
                        .unitPrice(new BigDecimal("200000.00"))
                        .subtotal(new BigDecimal("6000000.00"))
                        .build());

                PurchaseOrderEntity po2 = purchaseOrderRepository.save(PurchaseOrderEntity.builder()
                        .supplier(sup2)
                        .receivingStore(store1)
                        .status(PurchaseOrderStatus.DRAFT)
                        .createdBy(adminEmp)
                        .totalAmount(new BigDecimal("8500000.00"))
                        .build());

                purchaseOrderItemRepository.save(PurchaseOrderItemEntity.builder()
                        .purchaseOrder(po2)
                        .sku(sku1)
                        .quantity(40)
                        .unitPrice(new BigDecimal("212500.00"))
                        .subtotal(new BigDecimal("8500000.00"))
                        .build());

                log.info(">>> [DataSeeder] Đã nạp 2 Phiếu Nhập Hàng mẫu (1 CONFIRMED, 1 DRAFT)!");
            }

            if (stores.size() >= 2 && !skus.isEmpty() && adminEmp != null && stockTransferRepository.count() == 0) {
                StoreEntity storeFrom = stores.get(0);
                StoreEntity storeTo = stores.get(1);
                ProductSkuEntity skuTransfer = skus.get(0);

                StockTransferEntity tf1 = stockTransferRepository.save(StockTransferEntity.builder()
                        .sourceStore(storeFrom)
                        .destinationStore(storeTo)
                        .status(TransferStatus.IN_TRANSIT)
                        .createdBy(adminEmp)
                        .shippedAt(LocalDateTime.now().minusHours(5))
                        .build());

                stockTransferItemRepository.save(StockTransferItemEntity.builder()
                        .stockTransfer(tf1)
                        .sku(skuTransfer)
                        .quantity(15)
                        .build());

                StockTransferEntity tf2 = stockTransferRepository.save(StockTransferEntity.builder()
                        .sourceStore(storeFrom)
                        .destinationStore(storeTo)
                        .status(TransferStatus.PENDING)
                        .createdBy(adminEmp)
                        .build());

                stockTransferItemRepository.save(StockTransferItemEntity.builder()
                        .stockTransfer(tf2)
                        .sku(skuTransfer)
                        .quantity(20)
                        .build());

                log.info(">>> [DataSeeder] Đã nạp 2 Phiếu Điều Chuyển Kho mẫu (1 IN_TRANSIT, 1 PENDING)!");
            }
        }
    }
}
