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
    roles: ['ROLE_SUPER_ADMIN', 'ROLE_DEPARTMENT_HEAD'],
  });

  assert.strictEqual(tokenService.hasRole('SUPER_ADMIN'), true);
  assert.strictEqual(tokenService.hasRole('ROLE_SUPER_ADMIN'), true);
  assert.strictEqual(tokenService.hasRole('DEPARTMENT_HEAD'), true);
  assert.strictEqual(tokenService.hasRole('CITIZEN'), false);

  assert.strictEqual(tokenService.hasAnyRole(['CITIZEN', 'SUPER_ADMIN']), true);
  assert.strictEqual(tokenService.hasAnyRole(['FIELD_WORKER']), false);
});

test('tokenService detects token expiry accurately', () => {
  // Construct a valid base64url JWT payload with future exp
  const futureExp = Math.floor(Date.now() / 1000) + 3600; // +1 hour
  const pastExp = Math.floor(Date.now() / 1000) - 3600; // -1 hour

  const createJwt = (exp) => {
    const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url');
    const payload = Buffer.from(JSON.stringify({ sub: 'citizen1', exp })).toString('base64url');
    return `${header}.${payload}.signature`;
  };

  const validToken = createJwt(futureExp);
  const expiredToken = createJwt(pastExp);

  assert.strictEqual(tokenService.isTokenExpired(validToken), false);
  assert.strictEqual(tokenService.isTokenExpired(expiredToken), true);

  tokenService.setAccessToken(validToken);
  assert.strictEqual(tokenService.isAuthenticated(), true);

  tokenService.setAccessToken(expiredToken);
  assert.strictEqual(tokenService.isAuthenticated(), false);
});
