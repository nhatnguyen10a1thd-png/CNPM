'use strict';
const $ = (id) => document.getElementById(id);
const state = { user: null, csrf: null, resetToken: null, roles: [], stores: [], permissions: [], employees: [], page: 0, pages: 0, keyword: '', editing: null, demo: false };
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
const can = (permission) => (state.user?.permissions || []).includes(permission);
const names = { fullName: 'Họ và tên', email: 'Email', identifier: 'Email hoặc số điện thoại', phone: 'Số điện thoại', password: 'Mật khẩu', confirmPassword: 'Xác nhận mật khẩu', termsAccepted: 'Điều khoản sử dụng', roleIds: 'Vai trò', storeIds: 'Chi nhánh', primaryStoreId: 'Chi nhánh chính', internalEmail: 'Email nội bộ', token: 'Liên kết khôi phục' };

function notify(message, type = 'success') {
  $('notice').textContent = message;
  $('notice').className = type;
  $('notice').hidden = !message;
}
function describeError(error) {
  const fields = Object.entries(error.details?.fieldErrors || {}).map(([name, value]) => `${names[name] || name}: ${value}`).join(' · ');
  return fields || error.message || 'Không thể thực hiện. Vui lòng thử lại.';
}
async function csrf() {
  const response = await fetch('/api/auth/csrf', { credentials: 'same-origin', cache: 'no-store' });
  if (!response.ok) throw new Error('Không thể khởi tạo phiên. Vui lòng tải lại trang.');
  state.csrf = await response.json();
}
async function api(path, options = {}) {
  const method = options.method || 'GET';
  const headers = { Accept: 'application/json' };
  if (options.body !== undefined) headers['Content-Type'] = 'application/json';
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    if (!state.csrf) await csrf();
    headers[state.csrf.headerName] = state.csrf.token;
  }
  const response = await fetch(path, { method, headers, credentials: 'same-origin', cache: 'no-store', body: options.body === undefined ? undefined : JSON.stringify(options.body) });
  const raw = await response.text();
  let result;
  try { result = raw ? JSON.parse(raw) : null; } catch { result = null; }
  if (!response.ok) {
    if (response.status === 401 && state.user && !options.quiet) {
      state.user = null;
      state.csrf = null;
      renderSession();
    }
    const error = new Error(result?.message || ({ 401: 'Phiên đăng nhập đã hết hạn hoặc thông tin đăng nhập không hợp lệ.', 403: 'Bạn không có quyền thực hiện thao tác này.', 409: 'Thông tin đã tồn tại. Vui lòng kiểm tra lại.', 429: 'Có quá nhiều lần thử. Hãy thử lại sau.' })[response.status] || 'Không thể thực hiện. Vui lòng thử lại.');
    error.status = response.status;
    error.details = result;
    throw error;
  }
  return result;
}
async function busy(element, task, localError) {
  const buttons = [...element.querySelectorAll('button')];
  const before = buttons.map(button => button.disabled);
  buttons.forEach(button => button.disabled = true);
  element.setAttribute('aria-busy', 'true');
  if (localError) localError.hidden = true;
  try { await task(); } catch (error) {
    if (localError) { localError.textContent = describeError(error); localError.hidden = false; }
    else notify(describeError(error), 'error');
  } finally {
    buttons.forEach((button, index) => button.disabled = before[index]);
    element.removeAttribute('aria-busy');
    $('prev-page').disabled = state.page <= 0;
    $('next-page').disabled = state.page + 1 >= state.pages;
    const role = state.roles.find(item => String(item.id) === $('role-select').value);
    $('save-role').disabled = !role || role.name === 'ADMIN';
  }
}
function clearPasswords() {
  document.querySelectorAll('input[autocomplete="current-password"],input[autocomplete="new-password"],input[name="confirmPassword"]').forEach(input => { input.value = ''; input.type = 'password'; });
  document.querySelectorAll('[data-toggle-password]').forEach(button => { button.textContent = 'Hiện'; button.setAttribute('aria-label', 'Hiện mật khẩu'); });
}
function authView(view) {
  clearPasswords();
  $('guest').hidden = false;
  $('workspace').hidden = true;
  ['login', 'register', 'recovery', 'reset'].forEach(name => $(name + '-view').hidden = name !== view);
  document.querySelectorAll('[data-auth]').forEach(button => button.classList.toggle('active', button.dataset.auth === view));
  if (view === 'recovery') refreshInbox();
  $(view + '-view').querySelector('input')?.focus();
}
function badges(values) { return values?.length ? values.map(value => `<span class="badge">${escapeHtml(value)}</span>`).join('') : '<span class="muted">Chưa được cấp</span>'; }
function renderSession() {
  $('logout').hidden = !state.user;
  if (!state.user) { $('workspace').hidden = true; authView('login'); return; }
  $('guest').hidden = true;
  $('workspace').hidden = false;
  $('staff-tab').hidden = !can('EMPLOYEE_MANAGE');
  $('roles-tab').hidden = !can('ROLE_MANAGE');
  $('scope-panel').hidden = state.user.accountType !== 'EMPLOYEE';
  $('account-name').textContent = state.user.fullName || 'Tài khoản';
  $('account-info').innerHTML = `<dt>Loại tài khoản</dt><dd>${state.user.accountType === 'EMPLOYEE' ? 'Nhân viên' : 'Khách hàng'}</dd><dt>Mã tài khoản</dt><dd>${escapeHtml(state.user.accountId)}</dd><dt>Trạng thái</dt><dd>${state.user.status === 'ACTIVE' ? 'Đang hoạt động' : 'Đã khóa'}</dd><dt>Vai trò</dt><dd>${badges(state.user.roles)}</dd><dt>Quyền chức năng</dt><dd>${badges(state.user.permissions)}</dd><dt>Mã chi nhánh</dt><dd>${badges(state.user.storeIds)}</dd>`;
  workspaceView('account');
}
async function refreshSession(quiet = false) {
  try { state.user = await api('/api/auth/me', { quiet: true }); }
  catch (error) { if (error.status !== 401) throw error; state.user = null; }
  renderSession();
  if (!quiet && state.user) notify('Thông tin phiên đã được cập nhật.');
}
function workspaceView(view) {
  if (!state.user) return;
  if (view === 'staff' && !can('EMPLOYEE_MANAGE') || view === 'roles' && !can('ROLE_MANAGE')) return;
  ['account', 'staff', 'roles'].forEach(name => $(name + '-view').hidden = name !== view);
  document.querySelectorAll('[data-workspace]').forEach(button => button.classList.toggle('active', button.dataset.workspace === view));
  if (view === 'staff') busy($('staff-view'), async () => { await references(); await loadEmployees(); });
  if (view === 'roles') busy($('roles-view'), async () => { await loadRoleManagement(); });
}
async function references() {
  if (!state.roles.length || !state.stores.length) {
    const [roles, stores] = await Promise.all([api('/api/roles'), api('/api/stores')]);
    state.roles = roles;
    state.stores = stores;
  }
}
async function loadEmployees() {
  const page = await api(`/api/employees?keyword=${encodeURIComponent(state.keyword)}&page=${state.page}&size=10`);
  state.employees = page.content || [];
  state.pages = page.totalPages || 0;
  $('employee-rows').innerHTML = state.employees.length ? state.employees.map(employee => `<tr><td><strong>${escapeHtml(employee.fullName)}</strong><span class="employee-email">${escapeHtml(employee.email)}</span></td><td>${employee.status === 'ACTIVE' ? 'Đang hoạt động' : 'Ngừng hoạt động'}</td><td>${badges(employee.roles)}</td><td>${badges(employee.stores)}${employee.primaryStoreId ? `<span class="employee-email">CN chính: ${escapeHtml(employee.primaryStoreId)}</span>` : ''}</td><td>${employee.accountId === state.user.accountId ? '<span class="muted">Tài khoản của bạn</span>' : `<button class="table-action" data-edit="${employee.id}">Sửa</button>${employee.status === 'ACTIVE' ? `<button class="table-action danger" data-deactivate="${employee.id}">Ngừng hoạt động</button>` : ''}`}</td></tr>`).join('') : '<tr><td colspan="5" class="muted">Không tìm thấy nhân viên phù hợp.</td></tr>';
  $('page-info').textContent = `${page.totalElements || 0} nhân viên · Trang ${state.pages ? state.page + 1 : 0}/${state.pages}`;
  $('prev-page').disabled = state.page <= 0;
  $('next-page').disabled = state.page + 1 >= state.pages;
}
function options(container, items, name, selected = [], label = 'name') {
  const legend = container.querySelector('legend').outerHTML;
  container.innerHTML = legend + (items.length ? items.map(item => `<label class="check"><input type="checkbox" name="${name}" value="${item.id}" ${selected.includes(item.id) ? 'checked' : ''}>${escapeHtml(item[label])}</label>`).join('') : '<span class="muted">Chưa có dữ liệu.</span>');
}
function openEmployee(employee = null) {
  state.editing = employee;
  const form = $('employee-form');
  form.reset();
  $('employee-error').hidden = true;
  $('employee-title').textContent = employee ? 'Cập nhật nhân viên' : 'Thêm nhân viên';
  ['fullName', 'email', 'internalEmail', 'phone', 'status'].forEach(name => form.elements[name].value = employee?.[name] || (name === 'status' ? 'ACTIVE' : ''));
  form.elements.password.required = !employee;
  $('employee-password-hint').textContent = employee ? 'Để trống để giữ mật khẩu hiện tại.' : 'Ít nhất 8 ký tự, có chữ và số.';
  options($('employee-role-options'), state.roles, 'roleIds', employee?.roleIds || []);
  options($('employee-store-options'), state.stores.filter(store => store.status === 'ACTIVE' || employee?.storeIds?.includes(store.id)), 'storeIds', employee?.storeIds || []);
  primaryOptions(employee?.primaryStoreId);
  $('employee-dialog').showModal();
}
function primaryOptions(preferred) {
  const selected = [...$('employee-form').querySelectorAll('input[name="storeIds"]:checked')].map(input => Number(input.value));
  const previous = preferred || Number($('primary-store').value);
  $('primary-store').innerHTML = '<option value="">Không có</option>' + state.stores.filter(store => selected.includes(store.id)).map(store => `<option value="${store.id}">${escapeHtml(store.name)}</option>`).join('');
  $('primary-store').value = selected.includes(previous) ? String(previous) : (selected.length ? String(selected[0]) : '');
  $('primary-store').required = selected.length > 0;
}
async function loadRoleManagement() {
  [state.roles, state.permissions] = await Promise.all([api('/api/roles'), api('/api/permissions')]);
  const previous = $('role-select').value;
  $('role-select').innerHTML = state.roles.map(role => `<option value="${role.id}">${escapeHtml(role.name)}${role.description ? ' — ' + escapeHtml(role.description) : ''}</option>`).join('');
  if (state.roles.some(role => String(role.id) === previous)) $('role-select').value = previous;
  renderRole();
}
function renderRole() {
  const role = state.roles.find(role => String(role.id) === $('role-select').value);
  options($('permission-options'), state.permissions, 'permissionIds', role?.permissionIds || [], 'name');
  const locked = !role || role.name === 'ADMIN';
  $('permission-options').disabled = locked;
  $('save-role').disabled = locked;
}
async function refreshInbox() {
  try {
    const messages = await api('/api/demo/recovery/messages', { quiet: true });
    state.demo = true;
    $('demo-accounts').hidden = false;
    $('demo-inbox').hidden = false;
    $('inbox-messages').innerHTML = messages.length ? messages.map(message => {
      const url = new URL(message.resetUrl, location.origin);
      const token = /^#reset=([A-Za-z0-9_-]{43})$/.exec(url.hash)?.[1];
      return token ? `<div class="mail"><strong>${escapeHtml(message.email)}</strong><span class="employee-email">Hết hạn: ${escapeHtml(new Date(message.expiresAt).toLocaleString('vi-VN'))}</span><a href="/#reset=${token}" data-reset-link>Mở thư đặt lại mật khẩu</a></div>` : '';
    }).join('') : '<p class="muted small">Chưa có thư còn hiệu lực.</p>';
  } catch { $('demo-inbox').hidden = true; $('demo-accounts').hidden = true; }
}
function takeResetLink(fragment = location.hash) {
  const token = /^#reset=([A-Za-z0-9_-]{43})$/.exec(fragment)?.[1];
  if (!token) return false;
  state.resetToken = token;
  history.replaceState(null, '', location.pathname + location.search);
  authView('reset');
  return true;
}

document.addEventListener('click', (event) => {
  const button = event.target.closest('button');
  if (button?.dataset.auth) { notify(''); authView(button.dataset.auth); }
  if (button?.dataset.workspace) { notify(''); workspaceView(button.dataset.workspace); }
  if (button?.dataset.togglePassword) {
    const input = $(button.dataset.togglePassword);
    input.type = input.type === 'password' ? 'text' : 'password';
    button.textContent = input.type === 'password' ? 'Hiện' : 'Ẩn';
    button.setAttribute('aria-label', input.type === 'password' ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
  }
  if (button?.dataset.edit) openEmployee(state.employees.find(employee => employee.id === Number(button.dataset.edit)));
  if (button?.dataset.deactivate) {
    const employee = state.employees.find(item => item.id === Number(button.dataset.deactivate));
    if (employee && window.confirm(`Ngừng hoạt động tài khoản ${employee.fullName}? Các phiên đăng nhập sẽ bị vô hiệu hóa.`)) busy($('staff-view'), async () => { await api('/api/employees/' + employee.id, { method: 'DELETE' }); notify('Đã ngừng hoạt động nhân viên.'); await loadEmployees(); });
  }
  if (button?.dataset.demo && state.demo) {
    const accounts = { admin: ['admin@lunea.test', 'DemoAdmin123'], customer: ['customer@lunea.test', 'DemoUser123'], warehouse: ['kho@lunea.test', 'KhoDemo123'] };
    const account = accounts[button.dataset.demo];
    authView('login');
    $('login-form').elements.identifier.value = account[0];
    $('login-form').elements.password.value = account[1];
  }
  const resetLink = event.target.closest('[data-reset-link]');
  if (resetLink) { event.preventDefault(); notify(''); takeResetLink(new URL(resetLink.href).hash); }
});
$('login-form').addEventListener('submit', event => {
  event.preventDefault();
  busy(event.target, async () => {
    const form = event.target;
    state.user = await api('/api/auth/login', { method: 'POST', body: { identifier: form.elements.identifier.value, password: form.elements.password.value, rememberMe: form.elements.rememberMe.checked } });
    form.reset();
    clearPasswords();
    await csrf();
    renderSession();
    notify('Đăng nhập thành công.');
    if (can('EMPLOYEE_MANAGE')) workspaceView('staff');
  });
});
$('register-form').addEventListener('submit', event => {
  event.preventDefault();
  busy(event.target, async () => {
    const data = Object.fromEntries(new FormData(event.target));
    if (data.password !== data.confirmPassword) throw new Error('Mật khẩu xác nhận không khớp.');
    data.termsAccepted = event.target.elements.termsAccepted.checked;
    await api('/api/auth/register', { method: 'POST', body: data });
    event.target.reset();
    authView('login');
    $('login-form').elements.identifier.value = data.email;
    notify('Đã tạo tài khoản. Hãy đăng nhập để tiếp tục.');
  });
});
$('recovery-form').addEventListener('submit', event => {
  event.preventDefault();
  busy(event.target, async () => { const result = await api('/api/auth/recovery/request', { method: 'POST', body: Object.fromEntries(new FormData(event.target)) }); notify(result.message); await refreshInbox(); });
});
$('reset-form').addEventListener('submit', event => {
  event.preventDefault();
  busy(event.target, async () => {
    if (!state.resetToken) throw new Error('Hãy mở liên kết khôi phục trong email hoặc yêu cầu liên kết mới.');
    const data = Object.fromEntries(new FormData(event.target));
    if (data.password !== data.confirmPassword) throw new Error('Mật khẩu xác nhận không khớp.');
    const result = await api('/api/auth/recovery/reset', { method: 'POST', body: { ...data, token: state.resetToken } });
    state.resetToken = null; state.user = null; state.csrf = null;
    event.target.reset(); renderSession(); await csrf(); notify(result.message);
  });
});
$('logout').addEventListener('click', () => busy($('header') || document.querySelector('header'), async () => {
  await api('/api/auth/logout', { method: 'POST' }); state.user = null; state.csrf = null; state.roles = []; state.stores = []; state.permissions = []; clearPasswords(); renderSession(); await csrf(); notify('Đã đăng xuất.');
}));
$('refresh-session').addEventListener('click', () => busy($('account-view'), () => refreshSession()));
$('scope-form').addEventListener('submit', event => {
  event.preventDefault();
  $('scope-result').textContent = '';
  busy(event.target, async () => { const items = await api('/api/inventory/store/' + Number(event.target.elements.storeId.value)); $('scope-result').textContent = `Truy cập được phép · ${items.length} dòng tồn kho.`; notify('Quyền và phạm vi chi nhánh hợp lệ.'); });
});
$('staff-search').addEventListener('submit', event => { event.preventDefault(); state.keyword = event.target.elements.keyword.value; state.page = 0; busy($('staff-view'), loadEmployees); });
$('prev-page').addEventListener('click', () => { if (state.page > 0) { state.page--; busy($('staff-view'), loadEmployees); } });
$('next-page').addEventListener('click', () => { if (state.page + 1 < state.pages) { state.page++; busy($('staff-view'), loadEmployees); } });
$('add-employee').addEventListener('click', () => openEmployee());
$('close-employee').addEventListener('click', () => $('employee-dialog').close());
$('cancel-employee').addEventListener('click', () => $('employee-dialog').close());
$('employee-dialog').addEventListener('close', () => { $('employee-form').elements.password.value = ''; state.editing = null; });
$('employee-store-options').addEventListener('change', () => primaryOptions());
$('employee-form').addEventListener('submit', event => {
  event.preventDefault();
  busy(event.target, async () => {
    const form = event.target;
    const data = Object.fromEntries(new FormData(form));
    data.roleIds = [...form.querySelectorAll('input[name="roleIds"]:checked')].map(input => Number(input.value));
    data.storeIds = [...form.querySelectorAll('input[name="storeIds"]:checked')].map(input => Number(input.value));
    data.primaryStoreId = data.primaryStoreId ? Number(data.primaryStoreId) : null;
    const editing = state.editing;
    await api('/api/employees' + (editing ? '/' + editing.id : ''), { method: editing ? 'PUT' : 'POST', body: data });
    $('employee-dialog').close(); await loadEmployees(); notify(editing ? 'Đã cập nhật nhân viên và phân quyền.' : 'Đã tạo nhân viên.');
  }, $('employee-error'));
});
$('role-select').addEventListener('change', renderRole);
$('role-form').addEventListener('submit', event => {
  event.preventDefault();
  busy(event.target, async () => { const permissionIds = [...event.target.querySelectorAll('input[name="permissionIds"]:checked')].map(input => Number(input.value)); await api('/api/roles/' + $('role-select').value + '/permissions', { method: 'PUT', body: { permissionIds } }); await loadRoleManagement(); notify('Đã cập nhật quyền của vai trò.'); });
});
$('refresh-inbox').addEventListener('click', () => busy($('demo-inbox'), refreshInbox));
$('open-terms').addEventListener('click', () => $('terms-dialog').showModal());
$('close-terms').addEventListener('click', () => $('terms-dialog').close());
window.addEventListener('hashchange', () => takeResetLink());
window.addEventListener('pageshow', event => { if (event.persisted) refreshSession(true).catch(error => notify(describeError(error), 'error')); });
(async function init() {
  const fragment = location.hash;
  if (fragment.startsWith('#reset=')) history.replaceState(null, '', location.pathname + location.search);
  try { await csrf(); await refreshSession(true); takeResetLink(fragment); await refreshInbox(); }
  catch (error) { notify(describeError(error), 'error'); }
})();
