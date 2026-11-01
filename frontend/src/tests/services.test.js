import test from 'node:test';
import assert from 'node:assert';

import { authService } from '../services/authService.js';
import { complaintService } from '../services/complaintService.js';
import { citizenService } from '../services/citizenService.js';
import { mapService } from '../services/mapService.js';
import { feedbackService } from '../services/feedbackService.js';

test('authService exposes expected API methods', () => {
  assert.strictEqual(typeof authService.login, 'function');
  assert.strictEqual(typeof authService.register, 'function');
  assert.strictEqual(typeof authService.refreshToken, 'function');
  assert.strictEqual(typeof authService.logout, 'function');
  assert.strictEqual(typeof authService.getCurrentUser, 'function');
});

test('complaintService exposes core complaints and lifecycle methods', () => {
  assert.strictEqual(typeof complaintService.getComplaints, 'function');
  assert.strictEqual(typeof complaintService.getComplaintById, 'function');
  assert.strictEqual(typeof complaintService.trackComplaint, 'function');
  assert.strictEqual(typeof complaintService.createComplaint, 'function');
  assert.strictEqual(typeof complaintService.updateStatus, 'function');
  assert.strictEqual(typeof complaintService.reopenComplaint, 'function');
  assert.strictEqual(typeof complaintService.assignComplaint, 'function');
  assert.strictEqual(typeof complaintService.addComment, 'function');
  assert.strictEqual(typeof complaintService.getComments, 'function');
});

test('citizenService exposes citizen-centric workflows', () => {
  assert.strictEqual(typeof citizenService.getMyGrievances, 'function');
  assert.strictEqual(typeof citizenService.submitGrievance, 'function');
  assert.strictEqual(typeof citizenService.trackGrievance, 'function');
  assert.strictEqual(typeof citizenService.reopenGrievance, 'function');
  assert.strictEqual(typeof citizenService.getCitizenStats, 'function');
});

test('mapService and feedbackService expose spatial and rating methods', () => {
  assert.strictEqual(typeof mapService.getWards, 'function');
  assert.strictEqual(typeof mapService.getGrievancePins, 'function');
  assert.strictEqual(typeof feedbackService.submitFeedback, 'function');
  assert.strictEqual(typeof feedbackService.getFeedbackForComplaint, 'function');
});
