import test from 'node:test';
import assert from 'node:assert';
import { validators } from '../hooks/useForm.js';

test('validators.required validates non-empty inputs', () => {
  const req = validators.required('Required field');
  assert.strictEqual(req(''), 'Required field');
  assert.strictEqual(req('   '), 'Required field');
  assert.strictEqual(req(null), 'Required field');
  assert.strictEqual(req(undefined), 'Required field');
  assert.strictEqual(req('Valid Content'), null);
  assert.strictEqual(req(123), null);
});

test('validators.email validates email address formats', () => {
  const emailVal = validators.email('Invalid email');
  assert.strictEqual(emailVal(''), null); // optional if empty
  assert.strictEqual(emailVal('invalid-email'), 'Invalid email');
  assert.strictEqual(emailVal('user@domain'), 'Invalid email');
  assert.strictEqual(emailVal('user@example.com'), null);
  assert.strictEqual(emailVal('citizen.hyderabad@telangana.gov.in'), null);
});

test('validators.phone validates 10-digit phone numbers', () => {
  const phoneVal = validators.phone('Invalid phone');
  assert.strictEqual(phoneVal(''), null); // optional if empty
  assert.strictEqual(phoneVal('12345'), 'Invalid phone');
  assert.strictEqual(phoneVal('9876543210'), null);
  assert.strictEqual(phoneVal('98765-43210'), null); // cleaned
});

test('validators.minLength enforces string length', () => {
  const minVal = validators.minLength(5, 'Minimum 5 characters');
  assert.strictEqual(minVal('abc'), 'Minimum 5 characters');
  assert.strictEqual(minVal('abcde'), null);
  assert.strictEqual(minVal('abcdef'), null);
});

test('validators.matches ensures target fields match', () => {
  const matchVal = validators.matches('password', 'Passwords must match');
  const values = { password: 'SecretPassword123' };
  assert.strictEqual(matchVal('DifferentPassword', values), 'Passwords must match');
  assert.strictEqual(matchVal('SecretPassword123', values), null);
});
