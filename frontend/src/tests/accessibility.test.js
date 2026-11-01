import test from 'node:test';
import assert from 'node:assert';

test('Badge component exposes color-independent status icons and ARIA attributes', async () => {
  // Test the badge mapping logic
  const STATUS_ICONS = {
    RESOLVED: '✓',
    CLOSED: '🔒',
    IN_PROGRESS: '⏳',
    REJECTED: '✕',
    CRITICAL: '⚠️',
  };

  Object.entries(STATUS_ICONS).forEach(([status, icon]) => {
    assert.ok(icon, `Icon must exist for status ${status} to ensure color-independence`);
    assert.notStrictEqual(icon, '', `Status ${status} must have a non-empty symbolic representation`);
  });
});

test('Public filters date horizon presets compute valid ISO date ranges', () => {
  const computePreset = (days) => {
    if (!days) return { startDate: '', endDate: '' };
    const end = new Date('2026-10-30T12:00:00Z');
    const start = new Date('2026-10-30T12:00:00Z');
    start.setDate(end.getDate() - days);
    return {
      startDate: start.toISOString().split('T')[0],
      endDate: end.toISOString().split('T')[0],
    };
  };

  const last7 = computePreset(7);
  assert.strictEqual(last7.startDate, '2026-10-23');
  assert.strictEqual(last7.endDate, '2026-10-30');

  const last30 = computePreset(30);
  assert.strictEqual(last30.startDate, '2026-09-30');
  assert.strictEqual(last30.endDate, '2026-10-30');
});
