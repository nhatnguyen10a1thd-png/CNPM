/**
 * LUNEA Client API & UI Helper Library
 */
const LUNEA = (function () {
    'use strict';

    // Format number to Vietnamese Dong currency
    function formatVND(amount) {
        if (amount === null || amount === undefined || isNaN(amount)) return '0 ₫';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            maximumFractionDigits: 0
        }).format(amount).replace('VND', '₫');
    }

    // Floating Luxury Toast Notification (Centered & Non-Obstructive)
    function showToast(message, type = 'info', duration = 3000) {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.setAttribute('aria-live', 'polite');
            container.setAttribute('aria-atomic', 'true');
            document.body.appendChild(container);
        }

        // Limit maximum simultaneous toasts to 3 to prevent screen obstruction
        const existingToasts = container.querySelectorAll('.lunea-toast');
        if (existingToasts.length >= 3) {
            const oldest = existingToasts[0];
            oldest.classList.remove('show');
            setTimeout(() => {
                if (oldest.parentElement) oldest.parentElement.removeChild(oldest);
            }, 200);
        }

        const toast = document.createElement('div');
        toast.className = `lunea-toast ${type}`;
        toast.setAttribute('role', 'alert');

        let iconSvg = '';
        if (type === 'success') {
            iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>';
        } else if (type === 'error') {
            iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>';
        } else if (type === 'warning') {
            iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>';
        } else {
            iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>';
        }

        toast.innerHTML = `
            <div class="lunea-toast-icon">${iconSvg}</div>
            <div class="lunea-toast-message">${message}</div>
            <button type="button" class="lunea-toast-close" aria-label="Đóng thông báo" title="Đóng">&times;</button>
        `;

        let isDismissed = false;
        let dismissTimer = null;

        function dismiss() {
            if (isDismissed) return;
            isDismissed = true;
            if (dismissTimer) clearTimeout(dismissTimer);
            toast.classList.remove('show');
            setTimeout(() => {
                if (toast.parentElement) toast.parentElement.removeChild(toast);
            }, 280);
        }

        const closeBtn = toast.querySelector('.lunea-toast-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                dismiss();
            });
        }

        // Clicking anywhere on toast dismisses it instantly
        toast.addEventListener('click', () => {
            dismiss();
        });

        // Hover pause functionality
        const actualDuration = type === 'error' ? Math.max(duration, 3800) : duration;
        let remainingTime = actualDuration;
        let timerStart = Date.now();

        function startTimer(time) {
            timerStart = Date.now();
            dismissTimer = setTimeout(dismiss, time);
        }

        toast.addEventListener('mouseenter', () => {
            if (dismissTimer) {
                clearTimeout(dismissTimer);
                remainingTime -= (Date.now() - timerStart);
            }
        });

        toast.addEventListener('mouseleave', () => {
            if (!isDismissed) {
                startTimer(Math.max(remainingTime, 1000));
            }
        });

        container.appendChild(toast);

        // Animate in
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        startTimer(actualDuration);
    }


    // Unified API Fetch Wrapper
    async function apiFetch(url, options = {}) {
        const skipAuthRedirect = options.skipAuthRedirect || false;
        const skipAuthToast = options.skipAuthToast || false;
        const isAuthPage = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register');

        const defaultHeaders = {
            'X-Requested-With': 'fetch',
            'Content-Type': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {})
            }
        };

        try {
            const response = await fetch(url, config);

            // 401 Unauthorized
            if (response.status === 401) {
                if (isAuthPage || url.includes('/api/auth/')) {
                    const contentType = response.headers.get('content-type');
                    const isJson = contentType && contentType.includes('application/json');
                    const data = isJson ? await response.json().catch(() => ({})) : await response.text().catch(() => '');
                    const errorMessage = (data && data.message) ? data.message : 'Email/SĐT hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa!';
                    if (!skipAuthToast && !options.skipToast) {
                        showToast(errorMessage, 'error', 4000);
                    }
                    const error = new Error(errorMessage);
                    error.data = data;
                    error.status = 401;
                    throw error;
                }

                if (!skipAuthToast) {
                    showToast('Vui lòng đăng nhập để tiếp tục thao tác.', 'error', 3000);
                }
                if (!skipAuthRedirect) {
                    setTimeout(() => {
                        const currentPath = window.location.pathname + window.location.search;
                        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
                    }, 1200);
                }
                throw new Error('UNAUTHENTICATED');
            }

            // 403 Forbidden
            if (response.status === 403) {
                if (!skipAuthToast && !isAuthPage) {
                    showToast('Bạn không có quyền thực hiện thao tác này.', 'error', 3500);
                }
                throw new Error('ACCESS_DENIED');
            }

            // 204 No Content
            if (response.status === 204) {
                return null;
            }

            // Parse response body
            const contentType = response.headers.get('content-type');
            const isJson = contentType && contentType.includes('application/json');
            const data = isJson ? await response.json() : await response.text();

            if (!response.ok) {
                const errorMessage = (data && data.message) ? data.message : 'Đã có lỗi xảy ra. Vui lòng thử lại!';
                if (!skipAuthToast && !options.skipToast) {
                    showToast(errorMessage, 'error', 4000);
                }
                const error = new Error(errorMessage);
                error.data = data;
                error.status = response.status;
                throw error;
            }

            return data;
        } catch (err) {
            if (err.message !== 'UNAUTHENTICATED' && err.message !== 'ACCESS_DENIED' && !err.status && !skipAuthToast && !options.skipToast) {
                showToast(err.message || 'Lỗi kết nối máy chủ', 'error', 3500);
            }
            throw err;
        }
    }

    // Cart Helper Methods
    async function updateCartBadge() {
        const isAuthPage = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register');
        const isAdminPage = window.location.pathname.startsWith('/admin');
        if (isAuthPage || isAdminPage) return null;

        try {
            const cart = await apiFetch('/api/cart', {
                method: 'GET',
                skipAuthRedirect: true,
                skipAuthToast: true,
                skipToast: true
            });
            const badges = document.querySelectorAll('.cart-badge-count');
            badges.forEach(b => {
                b.textContent = cart && cart.totalItems ? cart.totalItems : 0;
            });
            return cart;
        } catch (e) {
            // Unauthenticated guest or empty cart
            const badges = document.querySelectorAll('.cart-badge-count');
            badges.forEach(b => {
                b.textContent = 0;
            });
            return null;
        }
    }

    async function addToCart(skuId, quantity = 1) {
        try {
            const res = await apiFetch('/api/cart/items', {
                method: 'POST',
                body: JSON.stringify({ skuId, quantity })
            });
            showToast('Đã thêm sản phẩm vào giỏ hàng thành công!', 'success');
            await updateCartBadge();
            return res;
        } catch (e) {
            // Error handled by apiFetch
            throw e;
        }
    }

    // Wishlist Helper Methods & State Synchronization
    async function updateWishlistBadge(count) {
        if (count !== undefined && count !== null) {
            const badges = document.querySelectorAll('.wishlist-badge-count');
            badges.forEach(b => {
                b.textContent = count;
                b.style.display = count > 0 ? 'flex' : 'none';
            });
            return count;
        }

        const isAuthPage = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register');
        const isAdminPage = window.location.pathname.startsWith('/admin');
        if (isAuthPage || isAdminPage) return null;

        try {
            const wishlist = await apiFetch('/api/wishlist', {
                method: 'GET',
                skipAuthRedirect: true,
                skipAuthToast: true,
                skipToast: true
            });
            const itemCount = wishlist && wishlist.items ? wishlist.items.length : 0;
            const badges = document.querySelectorAll('.wishlist-badge-count');
            badges.forEach(b => {
                b.textContent = itemCount;
                b.style.display = itemCount > 0 ? 'flex' : 'none';
            });
            return itemCount;
        } catch (e) {
            const badges = document.querySelectorAll('.wishlist-badge-count');
            badges.forEach(b => {
                b.textContent = 0;
                b.style.display = 'none';
            });
            return 0;
        }
    }

    async function syncWishlistState() {
        const isAuthPage = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register');
        const isAdminPage = window.location.pathname.startsWith('/admin');
        if (isAuthPage || isAdminPage) return;

        try {
            const productIds = await apiFetch('/api/wishlist/product-ids', {
                method: 'GET',
                skipAuthRedirect: true,
                skipAuthToast: true,
                skipToast: true
            });

            if (Array.isArray(productIds)) {
                updateWishlistBadge(productIds.length);
                const idSet = new Set(productIds.map(Number));
                const buttons = document.querySelectorAll('.wishlist-heart-btn[data-product-id], #btnAddToWishlist[data-product-id]');
                buttons.forEach(btn => {
                    const pid = Number(btn.getAttribute('data-product-id'));
                    const isFav = idSet.has(pid);
                    btn.classList.toggle('active', isFav);
                    const icon = btn.querySelector('i');
                    if (icon) {
                        icon.className = isFav ? 'bi bi-heart-fill' : 'bi bi-heart';
                        icon.style.color = isFav ? '#f43f5e' : '';
                    }
                });
            }
        } catch (e) {
            // Not authenticated - keep outline hearts
        }
    }

    // Interactive Wishlist Toggle with REST API integration
    async function toggleWishlist(btn, productId) {
        if (!btn) return;
        const pId = productId || btn.getAttribute('data-product-id');
        if (!pId) return;

        const isCurrentlyActive = btn.classList.contains('active');
        const willBeActive = !isCurrentlyActive;
        const method = willBeActive ? 'POST' : 'DELETE';

        try {
            const res = await apiFetch('/api/wishlist/products/' + pId, {
                method: method,
                skipAuthToast: true,
                skipAuthRedirect: true
            });

            const relatedButtons = document.querySelectorAll(`.wishlist-heart-btn[data-product-id="${pId}"], #btnAddToWishlist[data-product-id="${pId}"]`);
            const targetButtons = relatedButtons.length > 0 ? relatedButtons : [btn];

            targetButtons.forEach(b => {
                b.classList.toggle('active', willBeActive);
                const icon = b.querySelector('i');
                if (icon) {
                    icon.className = willBeActive ? 'bi bi-heart-fill' : 'bi bi-heart';
                    icon.style.color = willBeActive ? '#f43f5e' : '';
                }
            });

            if (res && res.items) {
                updateWishlistBadge(res.items.length);
            } else {
                updateWishlistBadge();
            }

            showToast(willBeActive ? 'Đã thêm sản phẩm vào danh sách yêu thích.' : 'Đã bỏ sản phẩm khỏi danh sách yêu thích.', 'success');
        } catch (e) {
            if (e.message === 'UNAUTHENTICATED' || (e.data && e.data.status === 401)) {
                showToast('Vui lòng đăng nhập để lưu sản phẩm vào danh sách yêu thích.', 'warning');
                setTimeout(() => {
                    const currentPath = window.location.pathname + window.location.search;
                    window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
                }, 1400);
            } else {
                console.error(e);
                showToast('Không thể cập nhật danh sách yêu thích lúc này.', 'error');
            }
        }
    }

    async function clearWishlist() {
        try {
            await apiFetch('/api/wishlist', {
                method: 'DELETE'
            });
            updateWishlistBadge(0);
            showToast('Đã xóa tất cả sản phẩm khỏi danh sách yêu thích.', 'success');
            return true;
        } catch (e) {
            throw e;
        }
    }

    async function moveAllWishlistToCart() {
        try {
            const res = await apiFetch('/api/wishlist/move-all-to-cart', {
                method: 'POST'
            });
            await updateCartBadge();
            showToast('Đã thêm các sản phẩm còn hàng vào giỏ hàng thành công!', 'success');
            return res;
        } catch (e) {
            throw e;
        }
    }

    // Scroll Motion & Cinematic Reveal System (European Agency UX)
    function initScrollMotion() {
        // 1. Header scroll blur & compact mode
        const header = document.querySelector('.lunea-header');
        if (header) {
            const onScroll = () => {
                if (window.scrollY > 30) {
                    header.classList.add('header-scrolled');
                } else {
                    header.classList.remove('header-scrolled');
                }
            };
            window.addEventListener('scroll', onScroll, { passive: true });
            onScroll();
        }

        // 2. IntersectionObserver for smooth scroll reveals
        const revealElements = document.querySelectorAll('.reveal-on-scroll, .stagger-cards');
        if ('IntersectionObserver' in window && revealElements.length > 0) {
            const revealObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('is-revealed');
                        observer.unobserve(entry.target);
                    }
                });
            }, {
                root: null,
                threshold: 0.01,
                rootMargin: '0px 0px 60px 0px'
            });

            const winH = window.innerHeight || document.documentElement.clientHeight || 800;
            revealElements.forEach(el => {
                const rect = el.getBoundingClientRect();
                // If element is already in or near viewport upon page load, reveal immediately
                if (rect.top < winH + 60 && rect.bottom > 0) {
                    el.classList.add('is-revealed');
                } else {
                    revealObserver.observe(el);
                }
            });

            // Safety timeout: ensure all elements become visible even if scroll events or intersection are delayed
            setTimeout(() => {
                revealElements.forEach(el => {
                    if (!el.classList.contains('is-revealed')) {
                        el.classList.add('is-revealed');
                    }
                });
            }, 1200);
        } else {
            // Fallback for older browsers
            revealElements.forEach(el => el.classList.add('is-revealed'));
        }

        // 3. Subtle Hero & Auth Moon Cursor Parallax (Optimized with RAF & rect caching - zero layout thrashing)
        const parallaxPairs = [
            { container: document.querySelector('.hero-visual'), target: document.querySelector('.hero-visual .moon-image-wrapper') },
            { container: document.querySelector('.auth-story-col'), target: document.querySelector('.auth-moon-backdrop .moon-image-wrapper, .auth-moon-visual .moon-image-wrapper') }
        ];

        parallaxPairs.forEach(({ container, target }) => {
            if (container && target) {
                let rect = null;
                let rafId = null;

                const updateRect = () => {
                    rect = container.getBoundingClientRect();
                };

                container.addEventListener('mouseenter', updateRect, { passive: true });

                container.addEventListener('mousemove', (e) => {
                    if (!rect) updateRect();
                    if (rafId) cancelAnimationFrame(rafId);
                    rafId = requestAnimationFrame(() => {
                        const x = (e.clientX - rect.left) / rect.width - 0.5;
                        const y = (e.clientY - rect.top) / rect.height - 0.5;
                        target.style.transform = `translate3d(${x * 14}px, ${y * 14}px, 0) rotate(${x * 3}deg)`;
                    });
                }, { passive: true });

                container.addEventListener('mouseleave', () => {
                    if (rafId) cancelAnimationFrame(rafId);
                    rect = null;
                    target.style.transform = '';
                });
            }
        });
    }

    // Debounce Utility (WCAG & Front-End Checklist standard)
    function debounce(func, wait = 300) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    // Mobile Navigation Drawer Toggle
    function toggleMobileDrawer(open) {
        const drawer = document.getElementById('mobileDrawer');
        const overlay = document.getElementById('mobileDrawerOverlay');
        const hamburger = document.getElementById('mobileHamburgerBtn');
        if (!drawer) return;
        const isOpen = open !== undefined ? open : !drawer.classList.contains('open');
        if (isOpen) {
            drawer.classList.add('open');
            if (overlay) overlay.classList.add('open');
            if (hamburger) {
                hamburger.setAttribute('aria-expanded', 'true');
                hamburger.classList.add('active');
            }
            document.body.style.overflow = 'hidden';
            const firstFocusable = drawer.querySelector('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
            if (firstFocusable) firstFocusable.focus();
        } else {
            drawer.classList.remove('open');
            if (overlay) overlay.classList.remove('open');
            if (hamburger) {
                hamburger.setAttribute('aria-expanded', 'false');
                hamburger.classList.remove('active');
            }
            document.body.style.overflow = '';
        }
    }

    // -------------------------------------------------------------
    // Theme Management Engine (Dual Theme: Midnight Obsidian & Moonlight Pearl)
    // -------------------------------------------------------------
    function getTheme() {
        return document.documentElement.getAttribute('data-theme') || 'dark';
    }

    function updateThemeToggleUI(theme) {
        const btns = document.querySelectorAll('#themeToggleBtn, .theme-toggle-btn, .admin-theme-toggle-btn');
        btns.forEach(btn => {
            btn.setAttribute('aria-label', theme === 'dark' ? 'Chuyển sang giao diện Sáng' : 'Chuyển sang giao diện Tối');
            btn.setAttribute('title', theme === 'dark' ? 'Chuyển sang giao diện Sáng' : 'Chuyển sang giao diện Tối');
        });
    }

    function setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        try {
            localStorage.setItem('lunea-theme', theme);
        } catch (e) { }
        updateThemeToggleUI(theme);
    }

    function toggleTheme() {
        const current = getTheme();
        const next = current === 'dark' ? 'light' : 'dark';
        setTheme(next);
    }

    function initTheme() {
        const theme = getTheme();
        updateThemeToggleUI(theme);
    }

    // -------------------------------------------------------------
    // Advanced GSAP Motion Engine
    // -------------------------------------------------------------
    function initGSAP() {
        if (typeof window.gsap === 'undefined') return;
        const gsap = window.gsap;

        if (typeof window.ScrollTrigger !== 'undefined') {
            gsap.registerPlugin(window.ScrollTrigger);
        }

        // Hero Stagger Entrance
        const heroHeadline = document.querySelector('.hero-headline');
        if (heroHeadline) {
            const tl = gsap.timeline({ defaults: { ease: 'power3.out', duration: 0.9 } });
            tl.fromTo('.hero-brand-title', { opacity: 0, y: 20 }, { opacity: 1, y: 0, delay: 0.1 })
                .fromTo('.hero-headline', { opacity: 0, y: 30 }, { opacity: 1, y: 0 }, '-=0.65')
                .fromTo('.hero-description', { opacity: 0, y: 20 }, { opacity: 1, y: 0 }, '-=0.65')
                .fromTo('.hero-cta-group', { opacity: 0, y: 20 }, { opacity: 1, y: 0 }, '-=0.65')
                .fromTo('.moon-image-wrapper', { opacity: 0, scale: 0.88 }, { opacity: 1, scale: 1, duration: 1.1, ease: 'back.out(1.2)' }, '-=0.85');
        }

        // Auth Page Stagger Entrance
        const authStory = document.querySelector('.auth-story-col');
        if (authStory) {
            const tlAuth = gsap.timeline({ defaults: { ease: 'power3.out', duration: 0.9 } });
            tlAuth.fromTo('.auth-story-col .eyebrow-tag, .auth-story-col .hero-brand-title', { opacity: 0, y: 15 }, { opacity: 1, y: 0, delay: 0.1 })
                .fromTo('.auth-story-col h1', { opacity: 0, y: 20 }, { opacity: 1, y: 0 }, '-=0.65')
                .fromTo('.auth-story-col p', { opacity: 0, y: 15 }, { opacity: 1, y: 0 }, '-=0.65')
                .fromTo('.auth-moon-backdrop .moon-image-wrapper, .auth-moon-visual .moon-image-wrapper', { opacity: 0, scale: 0.88 }, { opacity: 1, scale: 1, duration: 1.1, ease: 'back.out(1.2)' }, '-=0.85');
        }

        // Bento Grid Scroll Scale
        if (window.ScrollTrigger && document.querySelector('.bento-moon-grid')) {
            gsap.fromTo('.bento-card',
                { opacity: 0, y: 35, scale: 0.96 },
                {
                    opacity: 1,
                    y: 0,
                    scale: 1,
                    stagger: 0.12,
                    duration: 0.85,
                    ease: 'power2.out',
                    scrollTrigger: {
                        trigger: '.bento-moon-grid',
                        start: 'top 82%'
                    }
                }
            );
        }

        // Editorial Manifesto Text Scrub / Reveal
        if (window.ScrollTrigger && document.querySelector('.editorial-manifesto-text')) {
            gsap.fromTo('.editorial-manifesto-text',
                { opacity: 0.25, y: 25 },
                {
                    opacity: 1,
                    y: 0,
                    duration: 1.1,
                    ease: 'power2.out',
                    scrollTrigger: {
                        trigger: '.editorial-manifesto-section',
                        start: 'top 78%',
                        end: 'bottom 45%',
                        scrub: 1
                    }
                }
            );
        }
    }

    // Global Modal Dialog & Accessibility Management (WCAG 2.4.3 & 2.1.2)
    function initModalAccessibility() {
        let lastFocusedElement = null;

        function isModalOpen(modal) {
            if (!modal) return false;
            if (modal.classList.contains('show') || modal.classList.contains('active') || modal.classList.contains('open')) {
                return true;
            }
            const display = modal.style.display || window.getComputedStyle(modal).display;
            return display !== 'none' && display !== '';
        }

        function closeModal(modal) {
            if (!modal) return;
            const closeBtn = modal.querySelector(
                '.modal-close, .admin-modal-close, button[aria-label*="Đóng"], button[aria-label*="Close"], [data-close]'
            );
            if (closeBtn && typeof closeBtn.onclick === 'function') {
                closeBtn.click();
            } else if (modal.id === 'forgotPasswordModal' && typeof window.closeForgotPasswordModal === 'function') {
                window.closeForgotPasswordModal();
            } else if (modal.id === 'storeStockModal' && typeof window.closeStoreStockModal === 'function') {
                window.closeStoreStockModal();
            } else if (modal.id === 'addressModal' && typeof window.closeAddressModal === 'function') {
                window.closeAddressModal();
            } else if (modal.id === 'returnModal' && typeof window.closeReturnModal === 'function') {
                window.closeReturnModal();
            } else if (modal.id === 'customerDetailModal' && typeof window.closeCustomerModal === 'function') {
                window.closeCustomerModal();
            } else if (modal.id === 'adjustModal' && typeof window.closeAdjustModal === 'function') {
                window.closeAdjustModal();
            } else {
                modal.classList.remove('show', 'active', 'open');
                modal.style.display = 'none';
            }
            document.body.style.overflow = '';
            if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
                lastFocusedElement.focus();
                lastFocusedElement = null;
            }
        }

        // Keyboard handler for Escape & Focus Trap (Tab / Shift+Tab)
        document.addEventListener('keydown', (e) => {
            const openModals = Array.from(document.querySelectorAll('[role="dialog"], .modal-overlay, .admin-modal-backdrop'))
                .filter(isModalOpen);

            if (openModals.length === 0) return;
            const topModal = openModals[openModals.length - 1];

            if (e.key === 'Escape') {
                e.preventDefault();
                closeModal(topModal);
                return;
            }

            if (e.key === 'Tab') {
                const focusable = Array.from(topModal.querySelectorAll(
                    'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'
                )).filter(el => {
                    const style = window.getComputedStyle(el);
                    return style.display !== 'none' && style.visibility !== 'hidden';
                });

                if (focusable.length === 0) {
                    e.preventDefault();
                    return;
                }

                const firstEl = focusable[0];
                const lastEl = focusable[focusable.length - 1];

                if (e.shiftKey) {
                    if (document.activeElement === firstEl || !topModal.contains(document.activeElement)) {
                        e.preventDefault();
                        lastEl.focus();
                    }
                } else {
                    if (document.activeElement === lastEl || !topModal.contains(document.activeElement)) {
                        e.preventDefault();
                        firstEl.focus();
                    }
                }
            }
        });

        // Click outside backdrop to dismiss
        document.addEventListener('click', (e) => {
            const modals = document.querySelectorAll('[role="dialog"], .modal-overlay, .admin-modal-backdrop');
            modals.forEach(modal => {
                if (isModalOpen(modal) && e.target === modal) {
                    closeModal(modal);
                }
            });
        });

        // Track opening trigger for focus restoration
        document.addEventListener('click', (e) => {
            const trigger = e.target.closest('button, a, [role="button"]');
            if (trigger) {
                const onclickStr = trigger.getAttribute('onclick') || '';
                const isOpener = onclickStr.includes('open') ||
                    trigger.dataset.modalTarget ||
                    trigger.classList.contains('btn-check-stores');
                if (isOpener) {
                    lastFocusedElement = trigger;
                }
            }
        }, true);
    }

    // Global ARIA Tabs Keyboard Navigation (WAI-ARIA Tabs: Arrow Left / Arrow Right)
    function initTabAccessibility() {
        document.addEventListener('keydown', (e) => {
            if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return;
            const activeTab = document.activeElement;
            if (!activeTab || activeTab.getAttribute('role') !== 'tab') return;

            const tablist = activeTab.closest('[role="tablist"]');
            if (!tablist) return;

            const tabs = Array.from(tablist.querySelectorAll('[role="tab"]'));
            if (tabs.length <= 1) return;

            const currentIndex = tabs.indexOf(activeTab);
            if (currentIndex === -1) return;

            e.preventDefault();
            let nextIndex = e.key === 'ArrowRight'
                ? (currentIndex + 1) % tabs.length
                : (currentIndex - 1 + tabs.length) % tabs.length;

            tabs[nextIndex].focus();
            tabs[nextIndex].click();
        });
    }

    // Initialize on page load
    document.addEventListener('DOMContentLoaded', () => {
        // Initialize Theme UI
        initTheme();

        // Refresh cart & wishlist state (skip for auth pages and back-office admin pages)
        const isAuthPage = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register');
        const isAdminPage = window.location.pathname.startsWith('/admin');
        if (!isAuthPage && !isAdminPage) {
            updateCartBadge();
            syncWishlistState();
        }

        // Initialize scroll motion & GSAP
        initScrollMotion();
        initGSAP();

        // Initialize modal & tab accessibility
        initModalAccessibility();
        initTabAccessibility();
    });

    async function logout(e) {
        if (e && e.preventDefault) {
            e.preventDefault();
        }
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'fetch'
                }
            });
        } catch (e) {
            console.error('Logout error:', e);
        }
        // Force full navigation to /logout to guarantee cookie eviction and redirect to /login
        window.location.href = '/logout';
    }

    async function showMyVouchers() {
        try {
            const list = await apiFetch('/api/vouchers/active');
            if (!list || list.length === 0) {
                showToast('Hiện tại chưa có mã ưu đãi nào đang mở.', 'info');
                return;
            }
            const codes = list.map(v => `${v.code} (${v.discountType === 'PERCENT' ? 'Giảm ' + v.discountValue + '%' : (v.discountType === 'FIXED' ? 'Giảm ' + formatVND(v.discountValue) : 'FreeShip')})`).join(' · ');
            showToast('Mã ưu đãi LUNEA có thể dùng: ' + codes, 'info', 6000);
        } catch (e) {
            showToast('Bạn đang có các mã ưu đãi: LUNEA10, GIAM50K, FREESHIP', 'info');
        }
    }

    return {
        formatVND,
        showToast,
        toastSuccess: (msg) => showToast(msg, 'success'),
        toastError: (msg) => showToast(msg, 'error'),
        apiFetch,
        addToCart,
        updateCartBadge,
        updateWishlistBadge,
        syncWishlistState,
        clearWishlist,
        moveAllWishlistToCart,
        logout,
        showMyVouchers,
        toggleWishlist,
        initScrollMotion,
        getTheme,
        setTheme,
        toggleTheme,
        initGSAP,
        initModalAccessibility,
        initTabAccessibility,
        debounce,
        toggleMobileDrawer,
        openMobileDrawer: () => toggleMobileDrawer(true),
        closeMobileDrawer: () => toggleMobileDrawer(false)
    };
})();
