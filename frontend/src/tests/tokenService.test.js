import test from 'node:test';
import assert from 'node:assert';

// Mock localStorage for Node test environment
const mockStorage = new Map();
global.localStorage = {
  getItem: (key) => mockStorage.get(key) || null,
  setItem: (key, value) => mockStorage.set(key, String(value)),
  removeItem: (key) => mockStorage.delete(key),
  clear: () => mockStorage.clear(),
};

import { tokenService } from '../services/tokenService.js';

test('tokenService stores and retrieves access token correctly', () => {
  localStorage.clear();
  assert.strictEqual(tokenService.getAccessToken(), null);
  assert.strictEqual(tokenService.isAuthenticated(), false);

  tokenService.setTokens({ accessToken: 'sample_token_xyz', refreshToken: 'sample_refresh_xyz' });
  assert.strictEqual(tokenService.getAccessToken(), 'sample_token_xyz');
  assert.strictEqual(tokenService.getRefreshToken(), 'sample_refresh_xyz');
  assert.strictEqual(tokenService.isAuthenticated(), true);

  tokenService.clearSession();
  assert.strictEqual(tokenService.getAccessToken(), null);
  assert.strictEqual(tokenService.isAuthenticated(), false);
});

test('tokenService validates role checks accurately', () => {
  localStorage.clear();
  tokenService.setUser({
    id: 1,
    username: 'admin',
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_DEPT_ADMIN'],
  });

  assert.strictEqual(tokenService.hasRole('SUPER_ADMIN'), true);
  assert.strictEqual(tokenService.hasRole('ROLE_SUPER_ADMIN'), true);
  assert.strictEqual(tokenService.hasRole('CITIZEN'), false);
});
