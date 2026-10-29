import test from 'node:test';
import assert from 'node:assert';
import { tokenService } from '../services/tokenService.js';

// Verify module exports and sanity contracts
test('tokenService contract has required methods', () => {
  assert.strictEqual(typeof tokenService.getAccessToken, 'function');
  assert.strictEqual(typeof tokenService.setTokens, 'function');
  assert.strictEqual(typeof tokenService.clearSession, 'function');
  assert.strictEqual(typeof tokenService.isAuthenticated, 'function');
  assert.strictEqual(typeof tokenService.hasRole, 'function');
});
