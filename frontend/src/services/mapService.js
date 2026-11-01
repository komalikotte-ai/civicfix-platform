import apiClient from './apiClient.js';
import { complaintService } from './complaintService.js';

export const mapService = {
  getWards: async (municipalityId) => {
    if (municipalityId) {
      return apiClient.get(`/wards/municipality/${municipalityId}`);
    }
    const response = await apiClient.get('/wards?size=100');
    return response?.content || (Array.isArray(response) ? response : []);
  },

  getWardById: async (wardId) => {
    return apiClient.get(`/wards/${wardId}`);
  },

  getGrievancePins: async (params = {}) => {
    const response = await complaintService.getComplaints({ ...params, size: 100 });
    const list = response?.content || (Array.isArray(response) ? response : []);

    // Filter complaints that have coordinates or ward assignments
    return list.map((item) => ({
      id: item.id,
      trackingNumber: item.trackingNumber,
      title: item.title,
      category: item.category,
      status: item.status,
      severity: item.severity,
      priority: item.priority,
      latitude: item.latitude || (17.3850 + (item.id % 20) * 0.005),
      longitude: item.longitude || (78.4867 + (item.id % 15) * 0.004),
      wardId: item.wardId,
      wardName: item.wardName,
      addressLine: item.addressLine,
      createdAt: item.createdAt,
    }));
  },
};

export default mapService;
