import test from 'node:test';
import assert from 'node:assert';

// Mock localStorage if missing
const storage = new Map();
if (typeof globalThis.localStorage === 'undefined') {
  globalThis.localStorage = {
    getItem: (key) => storage.get(key) || null,
    setItem: (key, value) => storage.set(key, String(value)),
    removeItem: (key) => storage.delete(key),
    clear: () => storage.clear(),
  };
}

import {
  t,
  setLanguage,
  getLanguage,
  registerLanguage,
  getSupportedLanguages,
  onLanguageChange,
} from '../i18n/index.js';

test('i18n returns english keys correctly', () => {
  setLanguage('en');
  assert.strictEqual(getLanguage(), 'en');
  assert.strictEqual(t('app.title'), 'CivicFix');
  assert.strictEqual(t('nav.login'), 'Sign In');
  assert.strictEqual(t('nav.register'), 'Register');
});

test('i18n switches to Hindi translations seamlessly', () => {
  setLanguage('hi');
  assert.strictEqual(getLanguage(), 'hi');
  assert.strictEqual(t('app.title'), 'सिविकफिक्स');
  assert.strictEqual(t('nav.login'), 'साइन इन');
  assert.strictEqual(t('nav.register'), 'पंजीकरण');

  setLanguage('en');
});

test('i18n switches to Telugu translations seamlessly', () => {
  setLanguage('te');
  assert.strictEqual(getLanguage(), 'te');
  assert.strictEqual(t('app.title'), 'సివిక్‌ఫిక్స్');
  assert.strictEqual(t('nav.login'), 'సైన్ ఇన్');
  assert.strictEqual(t('nav.newComplaint'), 'ఫిర్యాదు నమోదు');
  assert.strictEqual(t('status.RESOLVED'), 'పరిష్కరించబడింది');

  setLanguage('en');
});

test('expandable translation architecture allows registering new languages at runtime', () => {
  registerLanguage('ta', {
    app: { title: 'சிவிக்ஃபிக்ஸ்' },
    nav: { login: 'உள்நுழைக' },
  }, { label: 'Tamil', nativeLabel: 'தமிழ்' });

  const supported = getSupportedLanguages();
  assert.ok(supported.some((s) => s.code === 'ta'));

  setLanguage('ta');
  assert.strictEqual(getLanguage(), 'ta');
  assert.strictEqual(t('app.title'), 'சிவிக்ஃபிக்ஸ்');
  assert.strictEqual(t('nav.login'), 'உள்நுழைக');

  setLanguage('en');
});

test('i18n supports parameter interpolation and fallback', () => {
  registerLanguage('custom', {
    welcome: 'Hello {name}, you have {count} updates',
  });
  setLanguage('custom');
  assert.strictEqual(t('welcome', { name: 'Komal', count: 3 }), 'Hello Komal, you have 3 updates');

  // Fallback when key missing
  assert.strictEqual(t('missing.key', 'Default Fallback'), 'Default Fallback');

  setLanguage('en');
});

test('i18n notifies change listeners reactively', () => {
  let observedLang = null;
  const unsubscribe = onLanguageChange((l) => {
    observedLang = l;
  });

  setLanguage('te');
  assert.strictEqual(observedLang, 'te');

  unsubscribe();
  setLanguage('en');
});
