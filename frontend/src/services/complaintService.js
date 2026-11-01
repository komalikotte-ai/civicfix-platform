import apiClient from './apiClient.js';

export const complaintService = {
  getComplaints: async (params = {}) => {
    return apiClient.get('/complaints', { params });
  },

  getComplaintById: async (id) => {
    return apiClient.get(`/complaints/${id}`);
  },

  trackComplaint: async (trackingNumber) => {
    return apiClient.get(`/complaints/track/${encodeURIComponent(trackingNumber)}`);
  },

  createComplaint: async (payload) => {
    return apiClient.post('/complaints', payload);
  },

  updateStatus: async (id, payload) => {
    return apiClient.patch(`/complaints/${id}/status`, payload);
  },

  reopenComplaint: async (id, reason) => {
    return apiClient.post(`/complaints/${id}/reopen`, { reason });
  },

  assignComplaint: async (id, payload) => {
    return apiClient.post(`/complaints/${id}/assign`, payload);
  },

  getStatusHistory: async (id) => {
    return apiClient.get(`/complaints/${id}/history`);
  },

  getWorkflowHistory: async (id) => {
    return apiClient.get(`/complaints/${id}/history`);
  },

  getAssignmentHistory: async (id) => {
    return apiClient.get(`/complaints/${id}/assignments`);
  },

  getComments: async (id) => {
    return apiClient.get(`/complaints/${id}/comments`);
  },

  addComment: async (id, payload) => {
    return apiClient.post(`/complaints/${id}/comments`, payload);
  },

  getCategories: async () => {
    return apiClient.get('/complaints/categories');
  },

  linkDuplicate: async (id, payload) => {
    return apiClient.post(`/complaints/${id}/duplicate`, payload);
  },

  unlinkDuplicate: async (id) => {
    return apiClient.delete(`/complaints/${id}/duplicate`);
  },
};

export default complaintService;
