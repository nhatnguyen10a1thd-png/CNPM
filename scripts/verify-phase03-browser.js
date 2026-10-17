(async () => {
  const results = [];
  const check = (name, passed) => results.push({ name, passed: !!passed });
  const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
  const settle = async form => {
    for (let i = 0; i < 100 && form.getAttribute('aria-busy') === 'true'; i++) await pause(50);
    if (form.getAttribute('aria-busy') === 'true') throw new Error('Submission did not settle');
  };
  const fill = (form, values) => {
    for (const [name, value] of Object.entries(values)) form.elements[name].value = value;
  };
  const submit = async form => { form.requestSubmit(); await settle(form); };
  async function signIn(password) {
    authView('login');
    fill($('login-form'), { identifier: 'customer@lunea.test', password });
    await submit($('login-form'));
  }
  const recover = $('recovery-form'), reset = $('reset-form');
  check('recovery POST requires CSRF', (await fetch('/api/auth/recovery/request', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identifier: 'customer@lunea.test' })
  })).status === 403);
  authView('recovery');
  fill(recover, { identifier: '' });
  check('empty identifier blocked by browser', !recover.checkValidity());
  await csrf();
  const generic = await api('/api/auth/recovery/request', { method: 'POST', body: { identifier: 'missing@lunea.test' } });
  check('unknown identifier has generic message and no secret', generic.message.includes('Nếu tài khoản hợp lệ') && Object.keys(generic).join() === 'message');
  await signIn('DemoUser123');
  check('existing password login works before recovery', state.user?.accountType === 'CUSTOMER');
  authView('recovery');
  const cached = state.csrf.token;
  await pause(5000);
  check('session really expired before recovery request', (await fetch('/api/auth/me', { cache: 'no-store' })).status === 401);
  fill(recover, { identifier: 'customer@lunea.test' });
  recover.requestSubmit();
  check('recovery submission loading and disabled', recover.getAttribute('aria-busy') === 'true' && recover.querySelector('[type="submit"]').disabled);
  await settle(recover);
  check('recovery request after idle expiry succeeds without reload', $('notice').className === 'success' && $('notice').textContent === generic.message && state.csrf.token !== cached);
  // Let the rest of the checks run independently when reproducing the pre-fix failure.
  if (!$('inbox-messages').querySelector('[data-reset-link]')) {
    await csrf(); await submit(recover);
  }
  check('actual local mailbox link received', !!$('inbox-messages').querySelector('[data-reset-link]'));
  $('inbox-messages').querySelector('[data-reset-link]').click();
  const rawToken = state.resetToken;
  check('mail link opens reset form and removes fragment', ! $('reset-view').hidden && !location.hash && /^[A-Za-z0-9_-]{43}$/.test(rawToken));
  check('reset secret absent from visible UI and web storage', !document.body.innerText.includes(rawToken) && !JSON.stringify(localStorage).includes(rawToken) && !JSON.stringify(sessionStorage).includes(rawToken));
  fill(reset, { password: 'lettersOnly', confirmPassword: 'lettersOnly' });
  check('password without number rejected in browser', !reset.checkValidity());
  let weak;
  try { await api('/api/auth/recovery/reset', { method: 'POST', body: { token: rawToken, password: 'short1', confirmPassword: 'short1' } }); }
  catch (error) { weak = error; }
  check('short password rejected server-side without consuming link', weak?.status === 400 && state.resetToken === rawToken);
  fill(reset, { password: 'Recovered456', confirmPassword: 'Different789' });
  await submit(reset);
  check('mismatch error keeps verification available', $('notice').className === 'error' && state.resetToken === rawToken);
  fill(reset, { password: 'é'.repeat(36) + 'A1', confirmPassword: 'é'.repeat(36) + 'A1' });
  await submit(reset);
  check('UTF8 password boundary rejected without consuming link', $('notice').className === 'error' && state.resetToken === rawToken);
  // Expire an authenticated session while the valid link remains in memory.
  await signIn('DemoUser123');
  takeResetLink('#reset=' + rawToken);
  const resetCsrf = state.csrf.token;
  await pause(5000);
  check('session really expired before reset submit', (await fetch('/api/auth/me', { cache: 'no-store' })).status === 401);
  fill(reset, { password: 'Recovered456', confirmPassword: 'Recovered456' });
  await submit(reset);
  check('reset after idle expiry succeeds without reload', !state.resetToken && !$('login-view').hidden && $('notice').className === 'success' && state.csrf.token !== resetCsrf);
  if (state.resetToken) { await csrf(); await submit(reset); }
  check('reset success clears passwords and identity', !state.user && !reset.elements.password.value && !reset.elements.confirmPassword.value);
  check('consumed mailbox entry removed', !(await api('/api/demo/recovery/messages')).length);
  await signIn('DemoUser123');
  check('old password denied over HTTP and displayed in UI', !state.user && $('notice').className === 'error');
  await signIn('Recovered456');
  check('new password login works over browser HTTP', state.user?.accountType === 'CUSTOMER');
  // Reset an authenticated session and check the next private request is revoked.
  await csrf();
  const second = await api('/api/auth/recovery/request', { method: 'POST', body: { identifier: 'customer@lunea.test' } });
  check('known and unknown recovery responses identical', JSON.stringify(second) === JSON.stringify(generic));
  await refreshInbox();
  $('inbox-messages').querySelector('[data-reset-link]').click();
  const secondToken = state.resetToken;
  fill(reset, { password: 'FinalPassword789', confirmPassword: 'FinalPassword789' });
  await submit(reset);
  check('reset revokes previously authenticated session', (await fetch('/api/auth/me')).status === 401);
  let replay;
  try { await api('/api/auth/recovery/reset', { method: 'POST', body: { token: secondToken, password: 'ReplayPassword123', confirmPassword: 'ReplayPassword123' } }); }
  catch (error) { replay = error; }
  check('replay rejected with safe error contract', replay?.status === 400 && replay.details?.code === 'INVALID_RECOVERY_TOKEN' && !JSON.stringify(replay.details).includes(secondToken));
  takeResetLink('#reset=' + 'x'.repeat(43));
  fill(reset, { password: 'FinalPassword789', confirmPassword: 'FinalPassword789' });
  await submit(reset);
  check('invalid link displays recoverable UI error', $('notice').className === 'error' && !$('reset-view').hidden && $('notice').textContent.includes('Liên kết'));
  await signIn('FinalPassword789');
  check('replay does not replace committed password', !!state.user);
  return { passed: results.filter(r => r.passed).length, failed: results.filter(r => !r.passed).length, results };
})()
