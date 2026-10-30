import test from 'node:test';
import assert from 'node:assert/strict';

// Mock localStorage for Node test runner
const localStorageMock = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => {
      store[key] = value.toString();
    },
    removeItem: (key) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

global.localStorage = localStorageMock;

// Import syncEngine
import { syncEngine } from '../services/syncEngine.js';

test('syncEngine caches and retrieves offline jobs accurately', () => {
  localStorage.clear();

  const mockJobs = [
    { id: 101, workOrderNumber: 'WO-20261030-001', title: 'Fix Pothole', status: 'ASSIGNED' },
    { id: 102, workOrderNumber: 'WO-20261030-002', title: 'Street Light', status: 'IN_PROGRESS' },
  ];

  syncEngine.cacheJobs(mockJobs);

  const cached = syncEngine.getCachedJobs();
  assert.equal(cached.length, 2);
  assert.equal(cached[0].workOrderNumber, 'WO-20261030-001');

  const single = syncEngine.getCachedJob(102);
  assert.equal(single.title, 'Street Light');
});

test('syncEngine enqueues actions with optimistic local state updates', () => {
  localStorage.clear();

  const mockJob = { id: 201, workOrderNumber: 'WO-001', status: 'ASSIGNED' };
  syncEngine.cacheJobs([mockJob]);

  // Enqueue ACCEPT action
  const queueItem = syncEngine.enqueueAction({
    action: 'ACCEPT',
    workOrderId: 201,
    expectedCurrentStatus: 'ASSIGNED',
    payload: { remarks: 'Worker acknowledged' },
  });

  assert.equal(queueItem.action, 'ACCEPT');
  assert.equal(queueItem.status, 'PENDING');

  // Verify queue has 1 item
  const queue = syncEngine.getSyncQueue();
  assert.equal(queue.length, 1);

  // Verify optimistic local status is updated to ACCEPTED
  const updatedLocal = syncEngine.getCachedJob(201);
  assert.equal(updatedLocal.status, 'ACCEPTED');
});

test('syncEngine records GPS check-ins and enqueues sync actions', () => {
  localStorage.clear();

  const coords = { latitude: 12.9716, longitude: 77.5946, accuracy: 5 };
  const checkin = syncEngine.recordGpsCheckIn(301, coords);

  assert.equal(checkin.workOrderId, 301);
  assert.equal(checkin.latitude, 12.9716);

  const stored = syncEngine.getGpsCheckins(301);
  assert.equal(stored.length, 1);
  assert.equal(stored[0].longitude, 77.5946);

  const queue = syncEngine.getSyncQueue();
  assert.equal(queue[0].action, 'GPS_CHECKIN');
});

test('syncEngine detects and flags conflicts when server status changes unexpectedly', async () => {
  localStorage.clear();

  const mockJob = { id: 401, workOrderNumber: 'WO-401', status: 'IN_PROGRESS' };
  syncEngine.cacheJobs([mockJob]);

  // Enqueue action based on expecting IN_PROGRESS
  const item = syncEngine.enqueueAction({
    action: 'COMPLETE',
    workOrderId: 401,
    expectedCurrentStatus: 'IN_PROGRESS',
    payload: { completionNotes: 'Done' },
  });

  // Mock server returning CANCELLED (conflict!)
  const mockService = {
    getWorkOrderById: async (id) => ({
      id,
      workOrderNumber: 'WO-401',
      status: 'CANCELLED',
      updatedAt: '2026-10-30T10:00:00Z',
    }),
  };

  const result = await syncEngine.processSyncQueue(mockService);
  assert.equal(result.conflicts.length, 1);
  assert.equal(result.conflicts[0].status, 'CONFLICT');
  assert.equal(result.conflicts[0].conflictDetails.serverStatus, 'CANCELLED');
});

test('syncEngine resolves conflicts via SERVER_WINS strategy', async () => {
  localStorage.clear();

  const item = syncEngine.enqueueAction({
    action: 'START',
    workOrderId: 501,
    expectedCurrentStatus: 'ACCEPTED',
  });

  // Flag as conflict
  item.status = 'CONFLICT';
  syncEngine.saveSyncQueue([item]);

  const mockService = {
    getWorkOrderById: async (id) => ({
      id,
      status: 'REASSIGNED',
    }),
  };

  const resolved = await syncEngine.resolveConflict(item.id, 'SERVER_WINS', mockService);
  assert.equal(resolved, true);

  const queue = syncEngine.getSyncQueue();
  assert.equal(queue.length, 0);

  const cached = syncEngine.getCachedJob(501);
  assert.equal(cached.status, 'REASSIGNED');
});

test('syncEngine stores and retrieves drafts and checklists', () => {
  localStorage.clear();

  const draft = {
    completionNotes: 'Replaced 50m cabling',
    safetyChecklist: { ppeEquipped: true, siteBarricaded: true },
    materials: [{ name: 'Cable', quantity: 50, unit: 'm' }],
  };

  syncEngine.saveDraft(601, draft);

  const retrieved = syncEngine.getDraft(601);
  assert.equal(retrieved.completionNotes, 'Replaced 50m cabling');
  assert.equal(retrieved.safetyChecklist.ppeEquipped, true);
  assert.equal(retrieved.materials[0].name, 'Cable');

  syncEngine.clearDraft(601);
  assert.equal(syncEngine.getDraft(601), null);
});
