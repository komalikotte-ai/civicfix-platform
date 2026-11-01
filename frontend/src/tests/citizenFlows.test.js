import test from 'node:test';
import assert from 'node:assert';

// Polyfill localStorage for Node test environment if not present
if (typeof globalThis.localStorage === 'undefined') {
  const store = new Map();
  globalThis.localStorage = {
    getItem: (key) => store.get(key) || null,
    setItem: (key, value) => store.set(key, String(value)),
    removeItem: (key) => store.delete(key),
    clear: () => store.clear(),
  };
}

import { savedLocationService } from '../services/savedLocationService.js';
import { notificationPreferenceService } from '../services/notificationPreferenceService.js';
import { feedbackService } from '../services/feedbackService.js';
import { complaintService } from '../services/complaintService.js';

test('savedLocationService manages citizen location CRUD and default switching', () => {
  const userId = 'citizen-test-101';
  globalThis.localStorage.clear();

  // Initial fetch should return default seed locations
  const initial = savedLocationService.getSavedLocations(userId);
  assert.ok(Array.isArray(initial));
  assert.ok(initial.length > 0);
  assert.strictEqual(initial.some((l) => l.isDefault), true);

  // Add new location
  const added = savedLocationService.saveLocation(userId, {
    label: 'Community Garden',
    addressLine: 'Gate 4, City Botanical Gardens',
    landmark: 'Next to Flower Clock',
    wardId: 3,
    wardName: 'Ward #3 - Greenbelt',
    latitude: 17.412,
    longitude: 78.456,
    isDefault: false,
  });
  assert.ok(added.id);
  assert.strictEqual(added.label, 'Community Garden');

  // Verify list grew
  const updatedList = savedLocationService.getSavedLocations(userId);
  assert.strictEqual(updatedList.length, initial.length + 1);

  // Set as default
  const withNewDefault = savedLocationService.setDefaultLocation(userId, added.id);
  const target = withNewDefault.find((l) => l.id === added.id);
  assert.strictEqual(target.isDefault, true);

  // Delete location
  const afterDelete = savedLocationService.deleteLocation(userId, added.id);
  assert.strictEqual(afterDelete.some((l) => l.id === added.id), false);
});

test('notificationPreferenceService manages citizen alert triggers and channels', () => {
  const userId = 'citizen-test-202';
  globalThis.localStorage.clear();

  const prefs = notificationPreferenceService.getPreferences(userId);
  assert.strictEqual(prefs.emailEnabled, true);
  assert.strictEqual(prefs.smsEnabled, true);
  assert.strictEqual(prefs.statusUpdates, true);

  // Update preferences
  const updated = notificationPreferenceService.updatePreferences(userId, {
    pushEnabled: true,
    dailyDigest: true,
    smsEnabled: false,
  });

  assert.strictEqual(updated.pushEnabled, true);
  assert.strictEqual(updated.dailyDigest, true);
  assert.strictEqual(updated.smsEnabled, false);
  assert.strictEqual(updated.emailEnabled, true); // preserved
});

test('feedbackService formats rating and satisfaction into structured citizen dialogue', async () => {
  let capturedPayload = null;
  const originalAddComment = complaintService.addComment;

  // Mock addComment to inspect formatted payload
  complaintService.addComment = async (id, payload) => {
    capturedPayload = { id, ...payload };
    return { id: 999, ...payload };
  };

  try {
    await feedbackService.submitFeedback(42, {
      rating: 5,
      resolutionSatisfied: true,
      feedbackComments: 'Pothole was patched cleanly within 24 hours. Excellent work!',
    });

    assert.ok(capturedPayload);
    assert.strictEqual(capturedPayload.id, 42);
    assert.strictEqual(capturedPayload.internalOnly, false);
    assert.ok(capturedPayload.comment.includes('[CITIZEN_FEEDBACK]'));
    assert.ok(capturedPayload.comment.includes('Rating: 5/5'));
    assert.ok(capturedPayload.comment.includes('Satisfied: Yes'));
    assert.ok(capturedPayload.comment.includes('Pothole was patched cleanly'));
  } finally {
    complaintService.addComment = originalAddComment;
  }
});

test('complaintService exposes workflow, history, and category endpoints', () => {
  assert.strictEqual(typeof complaintService.getStatusHistory, 'function');
  assert.strictEqual(typeof complaintService.getWorkflowHistory, 'function');
  assert.strictEqual(typeof complaintService.getAssignmentHistory, 'function');
  assert.strictEqual(typeof complaintService.getCategories, 'function');
  assert.strictEqual(typeof complaintService.reopenComplaint, 'function');
});
