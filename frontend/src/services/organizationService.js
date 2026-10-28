import apiClient from './apiClient';

export const organizationService = {
  getOrganizations: async (params = {}) => {
    return apiClient.get('/organizations', { params });
  },

  getDepartments: async (organizationId) => {
    return apiClient.get(`/departments/organization/${organizationId}`);
  },

  getWards: async (organizationId) => {
    return apiClient.get(`/wards/organization/${organizationId}`);
  },
};
