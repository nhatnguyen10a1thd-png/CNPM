(async () => {
  const results = [];
  const check = (name, passed) => results.push({ name, passed: !!passed });
  const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
  async function settle(form) {
    for (let i = 0; i < 100 && form.getAttribute('aria-busy') === 'true'; i++) await pause(50);
    if (form.getAttribute('aria-busy') === 'true') throw new Error('Submission did not settle');
  }
  function fill(form, values) {
    for (const [name, value] of Object.entries(values)) {
      const input = form.elements[name];
      if (input.type === 'checkbox') input.checked = value;
      else input.value = value;
      input.dispatchEvent(new Event('input', { bubbles: true }));
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }
  async function signIn(identifier, rememberMe = false, password = 'DemoUser123') {
    authView('login');
    const form = $('login-form');
    fill(form, { identifier, password, rememberMe });
    form.requestSubmit();
    await settle(form);
  }
  const register = $('register-form');
  check('anonymous private API denied over HTTP', (await fetch('/api/auth/me')).status === 401);
  check('CSRF required over HTTP', (await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ identifier: 'customer@lunea.test', password: 'DemoUser123' }) })).status === 403);
  authView('register');
  const submit = register.querySelector('[type="submit"]');
  check('empty registration disabled', submit.disabled);
  const email = 'phase02-' + crypto.randomUUID() + '@lunea.test';
  const password = 'Boundary123';
  fill(register, { fullName: 'Phase02 Test', email, phone: '', password, confirmPassword: password, termsAccepted: true });
  check('valid registration enabled and optional phone', !submit.disabled && register.checkValidity());
  fill(register, { fullName: '   ' });
  check('blank full name disabled', submit.disabled && !register.checkValidity());
  fill(register, { fullName: 'Phase02 Test', password: 'Short1', confirmPassword: 'Short1' });
  check('password minimum enforced', submit.disabled && !register.checkValidity());
  fill(register, { password, confirmPassword: password });
  fill(register, { confirmPassword: 'Different123' });
  check('mismatched confirmation disabled', submit.disabled && !register.checkValidity());
  fill(register, { confirmPassword: password, phone: '123' });
  check('invalid phone disabled', submit.disabled && !register.checkValidity());
  fill(register, { phone: '+84 (912) 345-678' });
  check('formatted phone accepted', !submit.disabled && register.checkValidity());
  fill(register, { password: 'é'.repeat(36) + 'A1', confirmPassword: 'é'.repeat(36) + 'A1' });
  check('74 UTF8 bytes disabled', submit.disabled && !register.checkValidity());
  fill(register, { password: 'é'.repeat(35) + 'A1', confirmPassword: 'é'.repeat(35) + 'A1' });
  check('72 UTF8 bytes accepted', !submit.disabled && register.checkValidity());
  fill(register, { password, confirmPassword: password, termsAccepted: false });
  check('terms required', !register.checkValidity() && submit.disabled);
  fill(register, { termsAccepted: true, phone: '' });
  register.requestSubmit();
  check('submit loading disabled', submit.disabled && register.getAttribute('aria-busy') === 'true');
  await settle(register);
  check('registration success and reset disabled', $('notice').textContent.includes('Đã tạo tài khoản') && submit.disabled);
  authView('register');
  fill(register, { fullName: 'Phase02 Test', email, phone: '', password, confirmPassword: password, termsAccepted: true });
  register.requestSubmit(); await settle(register);
  check('duplicate registration UI and retry enabled', $('notice').className === 'error' && !submit.disabled);
  await signIn(email, false, password);
  check('new customer browser login', state.user?.accountType === 'CUSTOMER' && !$('workspace').hidden);
  await api('/api/auth/logout', { method: 'POST' });
  state.user = null; state.csrf = null; renderSession(); await csrf();
  await signIn('customer@lunea.test', false, 'Wrong123');
  check('wrong credential UI and no identity', !state.user && $('notice').className === 'error');
  await signIn('customer@lunea.test');
  check('customer login navigation', state.user?.accountType === 'CUSTOMER' && $('staff-tab').hidden);
  const oldCsrf = state.csrf.token;
  await pause(5000);
  const expired = await fetch('/api/auth/me', { cache: 'no-store' });
  check('normal idle expiry in real servlet', expired.status === 401);
  await refreshSession(true);
  await signIn('customer@lunea.test');
  check('login after idle expiry without reload', !!state.user && state.csrf.token !== oldCsrf);
  // Proceed independently even if the preceding baseline bug prevented login.
  if (!state.user) { await csrf(); await signIn('customer@lunea.test'); }
  $('logout').click(); await settle(document.querySelector('header'));
  check('logout UI and private access denied', !state.user && (await fetch('/api/auth/me')).status === 401);
  await signIn('customer@lunea.test', true);
  await pause(4000);
  check('remember survives normal timeout', (await fetch('/api/auth/me')).status === 200);
  await pause(7500);
  check('remember idle expiry in real servlet', (await fetch('/api/auth/me')).status === 401);
  await refreshSession(true); await csrf();
  await signIn('admin@lunea.test', false, 'DemoAdmin123');
  check('staff identity and navigation', state.user?.accountType === 'EMPLOYEE' && !state.user.customerId && !$('staff-tab').hidden);
  check('unsafe customer domain stays denied', (await fetch('/api/cart?customerId=1')).status === 403);
  // Real DOM password visibility and desktop layout.
  $('logout').click(); await settle(document.querySelector('header'));
  $('login-form').elements.password.value = 'Visibility123';
  document.querySelector('[data-toggle-password="login-password"]').click();
  check('password visibility accessible', $('login-password').type === 'text' && document.querySelector('[data-toggle-password="login-password"]').getAttribute('aria-label') === 'Ẩn mật khẩu');
  return { passed: results.filter(r => r.passed).length, failed: results.filter(r => !r.passed).length, results };
})()
