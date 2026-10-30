import test from 'node:test';
import assert from 'node:assert';

// Mock localStorage
const storage = new Map();
global.localStorage = {
  getItem: (key) => storage.get(key) || null,
  setItem: (key, value) => storage.set(key, String(value)),
  removeItem: (key) => storage.delete(key),
  clear: () => storage.clear(),
};

import { t, setLanguage, getLanguage } from '../i18n/index.js';

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

  // Switch back to English
  setLanguage('en');
  assert.strictEqual(getLanguage(), 'en');
});

test('i18n falls back gracefully when key is missing', () => {
  setLanguage('en');
  assert.strictEqual(t('nonexistent.key', 'Fallback Value'), 'Fallback Value');
});
