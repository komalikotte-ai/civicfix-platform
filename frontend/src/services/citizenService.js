import apiClient from './apiClient.js';
import { complaintService } from './complaintService.js';

export const citizenService = {
  getMyGrievances: async (params = {}) => {
    return complaintService.getComplaints(params);
  },

  submitGrievance: async (payload) => {
    const formattedPayload = {
      ...payload,
      source: payload.source || 'CITIZEN_PORTAL',
    };
    return complaintService.createComplaint(formattedPayload);
  },

  trackGrievance: async (trackingNumber) => {
    return complaintService.trackComplaint(trackingNumber);
  },

  reopenGrievance: async (id, reason) => {
    return complaintService.reopenComplaint(id, reason);
  },

  addComment: async (id, comment) => {
    return complaintService.addComment(id, {
      comment,
      internalOnly: false,
    });
  },

  getCitizenStats: async () => {
    try {
      const response = await complaintService.getComplaints({ size: 100 });
      const complaints = response?.content || (Array.isArray(response) ? response : []);

      const total = complaints.length;
      const active = complaints.filter((c) =>
        ['NEW', 'SUBMITTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'REOPENED'].includes(c.status)
      ).length;
      const resolved = complaints.filter((c) => c.status === 'RESOLVED' || c.status === 'CLOSED').length;
      const onHold = complaints.filter((c) => c.status === 'ON_HOLD').length;

      return { total, active, resolved, onHold };
    } catch {
      return { total: 0, active: 0, resolved: 0, onHold: 0 };
    }
  },
};

export default citizenService;
