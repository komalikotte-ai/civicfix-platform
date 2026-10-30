import apiClient from './apiClient';

export const workOrderService = {
  /**
   * Fetch work orders with pagination and filtering
   */
  async getWorkOrders(params = {}) {
    return apiClient.get('/work-orders', { params });
  },

  /**
   * Fetch a single work order by ID
   */
  async getWorkOrderById(id) {
    return apiClient.get(`/work-orders/${id}`);
  },

  /**
   * Fetch a single work order by work order number
   */
  async getWorkOrderByNumber(number) {
    return apiClient.get(`/work-orders/number/${number}`);
  },

  /**
   * Worker accepts assigned job
   */
  async acceptJob(id, remarks = '') {
    return apiClient.post(`/work-orders/${id}/accept`, null, {
      params: remarks ? { remarks } : {},
    });
  },

  /**
   * Worker starts work on site
   */
  async startJob(id, remarks = '') {
    return apiClient.post(`/work-orders/${id}/start`, null, {
      params: remarks ? { remarks } : {},
    });
  },

  /**
   * Worker pauses work (puts on hold)
   */
  async pauseJob(id, reason = '') {
    return apiClient.post(`/work-orders/${id}/hold`, null, {
      params: reason ? { reason } : {},
    });
  },

  /**
   * Worker resumes work
   */
  async resumeJob(id, remarks = '') {
    return apiClient.patch(`/work-orders/${id}/status`, {
      targetStatus: 'IN_PROGRESS',
      remarks: remarks || 'Resumed work after pause',
    });
  },

  /**
   * Worker completes work order
   */
  async completeJob(id, payload = {}) {
    return apiClient.post(`/work-orders/${id}/complete`, {
      completionNotes: payload.completionNotes || '',
      actualCost: payload.actualCost || 0,
      evidenceMetadata: payload.evidenceMetadata || '',
    });
  },

  /**
   * Add comment / crew note
   */
  async addComment(id, comment, internalOnly = false) {
    return apiClient.post(`/work-orders/${id}/comments`, {
      comment,
      internalOnly,
    });
  },

  /**
   * Fetch comments
   */
  async getComments(id, includeInternal = true) {
    return apiClient.get(`/work-orders/${id}/comments`, {
      params: { includeInternal },
    });
  },

  /**
   * Fetch status history
   */
  async getStatusHistory(id) {
    return apiClient.get(`/work-orders/${id}/status-history`);
  },

  /**
   * Fetch assignment history
   */
  async getAssignmentHistory(id) {
    return apiClient.get(`/work-orders/${id}/assignment-history`);
  },

  /**
   * Fetch dependencies
   */
  async getDependencies(id) {
    return apiClient.get(`/work-orders/${id}/dependencies`);
  },
};

export default workOrderService;
