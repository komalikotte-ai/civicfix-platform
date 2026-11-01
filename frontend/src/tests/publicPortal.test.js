import test from 'node:test';
import assert from 'node:assert';

import publicPortalService from '../services/publicPortalService.js';
import complaintService from '../services/complaintService.js';
import organizationService from '../services/organizationService.js';

test('publicPortalService.getPublicStatistics computes key civic aggregates', async () => {
  const stats = await publicPortalService.getPublicStatistics();
  assert.ok(stats);
  assert.strictEqual(typeof stats.totalComplaints, 'number');
  assert.strictEqual(typeof stats.resolvedComplaints, 'number');
  assert.strictEqual(typeof stats.resolutionRatePercent, 'number');
  assert.strictEqual(typeof stats.averageResolutionHours, 'number');
  assert.strictEqual(typeof stats.slaComplianceRate, 'number');
});

test('publicPortalService.getWardPerformance computes ward-level metrics without PII', async () => {
  const wards = await publicPortalService.getWardPerformance();
  assert.ok(Array.isArray(wards));

  wards.forEach((w) => {
    assert.ok(w.wardNumber != null);
    assert.ok(w.wardName != null);
    assert.strictEqual(typeof w.resolutionRate, 'number');
    assert.strictEqual(typeof w.totalComplaints, 'number');
    // Ensure zero citizen PII leaked
    assert.strictEqual(w.citizenName, undefined);
    assert.strictEqual(w.citizenEmail, undefined);
    assert.strictEqual(w.phoneNumber, undefined);
  });
});

test('publicPortalService.getDepartmentPerformance computes department workloads and SLA rates', async () => {
  const depts = await publicPortalService.getDepartmentPerformance();
  assert.ok(Array.isArray(depts));

  depts.forEach((d) => {
    assert.ok(d.name != null);
    assert.ok(d.code != null);
    assert.strictEqual(typeof d.resolutionRate, 'number');
    assert.strictEqual(typeof d.slaCompliancePercent, 'number');
  });
});

test('publicPortalService.getPublicProjects exposes civic infrastructure milestones', async () => {
  const projects = await publicPortalService.getPublicProjects();
  assert.ok(Array.isArray(projects));
  assert.ok(projects.length > 0);

  const first = projects[0];
  assert.ok(first.name);
  assert.ok(first.wardName);
  assert.strictEqual(typeof first.progressPercent, 'number');
  assert.ok(first.progressPercent >= 0 && first.progressPercent <= 100);
});

test('publicPortalService.getMonthlyReports returns historical accountability records', async () => {
  const reports = await publicPortalService.getMonthlyReports();
  assert.ok(Array.isArray(reports));
  assert.ok(reports.length > 0);

  const rep = reports[0];
  assert.ok(rep.month);
  assert.strictEqual(typeof rep.totalFiled, 'number');
  assert.strictEqual(typeof rep.totalResolved, 'number');
  assert.strictEqual(typeof rep.satisfactionScore, 'number');
});

test('publicPortalService.getSanitizedPublicPins strips all private citizen personal information', async () => {
  // Mock complaintService.getComplaints to return sensitive test data
  const originalGetComplaints = complaintService.getComplaints;
  complaintService.getComplaints = async () => ({
    content: [
      {
        id: 77,
        trackingNumber: 'CF-20261030-9999',
        title: 'Open sewer on 4th cross street',
        category: 'SEWAGE',
        status: 'IN_PROGRESS',
        priority: 'HIGH',
        severity: 'HIGH',
        wardId: 2,
        wardName: 'Ward #2 - IT Corridor',
        latitude: 17.441,
        longitude: 78.349,
        // Sensitive PII that must be scrubbed:
        citizenName: 'Jane Doe',
        citizenEmail: 'jane.doe@confidential.com',
        citizenPhoneNumber: '+91 9876543210',
        authorRole: 'CITIZEN',
      },
    ],
  });

  try {
    const pins = await publicPortalService.getSanitizedPublicPins();
    assert.ok(Array.isArray(pins));
    assert.strictEqual(pins.length, 1);

    const pin = pins[0];
    assert.strictEqual(pin.id, 77);
    assert.strictEqual(pin.trackingNumber, 'CF-20261030-9999');
    assert.strictEqual(pin.category, 'SEWAGE');

    // Strict PII privacy assertions:
    assert.strictEqual(pin.citizenName, undefined);
    assert.strictEqual(pin.citizenEmail, undefined);
    assert.strictEqual(pin.citizenPhoneNumber, undefined);
  } finally {
    complaintService.getComplaints = originalGetComplaints;
  }
});
