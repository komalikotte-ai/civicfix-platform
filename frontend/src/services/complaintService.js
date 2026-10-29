import apiClient from './apiClient';

export const complaintService = {
  getComplaints: async (params = {}) => {
    return apiClient.get('/complaints', { params });
  },

  getComplaintById: async (id) => {
    return apiClient.get(`/complaints/${id}`);
  },

  trackComplaint: async (trackingNumber) => {
    return apiClient.get(`/complaints/track/${trackingNumber}`);
  },

  createComplaint: async (payload) => {
    return apiClient.post('/complaints', payload);
  },

  updateStatus: async (id, payload) => {
    return apiClient.patch(`/complaints/${id}/status`, payload);
  },

  getWorkflowHistory: async (id) => {
    return apiClient.get(`/workflow/complaints/${id}/history`);
  },
};
