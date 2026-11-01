import apiClient from './apiClient.js';

export const organizationService = {
  getOrganizations: async (params = {}) => {
    return apiClient.get('/organizations', { params });
  },

  getDepartments: async (organizationId) => {
    if (organizationId) {
      return apiClient.get(`/departments/organization/${organizationId}`);
    }
    const res = await apiClient.get('/departments?size=100');
    return res?.content || (Array.isArray(res) ? res : []);
  },

  getWards: async (organizationId) => {
    if (organizationId) {
      return apiClient.get(`/wards/organization/${organizationId}`);
    }
    const res = await apiClient.get('/wards?size=100');
    return res?.content || (Array.isArray(res) ? res : []);
  },

  getCities: async () => {
    const res = await apiClient.get('/cities?size=50');
    return res?.content || (Array.isArray(res) ? res : []);
  },

  getMunicipalities: async (cityId) => {
    const endpoint = cityId ? `/municipalities?cityId=${cityId}&size=50` : '/municipalities?size=50';
    const res = await apiClient.get(endpoint);
    return res?.content || (Array.isArray(res) ? res : []);
  },
};

export default organizationService;
