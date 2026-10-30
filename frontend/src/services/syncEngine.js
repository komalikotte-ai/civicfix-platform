/**
 * Offline Synchronization and Storage Engine for Field Worker Application.
 *
 * Provides:
 * - Persistent caching of jobs and work orders in localStorage
 * - Offline action queue with sequential execution
 * - GPS check-in caching
 * - Conflict detection (version/status mismatch detection)
 * - Conflict resolution strategy (server-wins, client-wins, manual-reapply)
 * - Automatic background retry & sync status tracking
 */

const STORAGE_KEYS = {
  OFFLINE_JOBS: 'civicfix_offline_jobs',
  SYNC_QUEUE: 'civicfix_sync_queue',
  JOB_DRAFTS: 'civicfix_job_drafts',
  TIME_LOGS: 'civicfix_time_logs',
  GPS_CHECKINS: 'civicfix_gps_checkins',
  SYNC_STATUS: 'civicfix_sync_status',
};

export const syncEngine = {
  /**
   * Check if client is currently online
   */
  isOnline() {
    return typeof navigator !== 'undefined' && typeof navigator.onLine === 'boolean'
      ? navigator.onLine
      : true;
  },

  /**
   * Save or update jobs list in local cache
   */
  cacheJobs(jobs) {
    if (!Array.isArray(jobs)) return;
    try {
      const existing = this.getCachedJobsMap();
      jobs.forEach((job) => {
        existing[job.id] = {
          ...job,
          _cachedAt: new Date().toISOString(),
          _localVersion: job.updatedAt || new Date().toISOString(),
        };
      });
      localStorage.setItem(STORAGE_KEYS.OFFLINE_JOBS, JSON.stringify(existing));
    } catch (e) {
      console.warn('Failed to cache jobs in localStorage', e);
    }
  },

  /**
   * Get all cached jobs as an array
   */
  getCachedJobs() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.OFFLINE_JOBS);
      if (!data) return [];
      const map = JSON.parse(data);
      return Object.values(map);
    } catch (e) {
      return [];
    }
  },

  /**
   * Get cached jobs as a map by ID
   */
  getCachedJobsMap() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.OFFLINE_JOBS);
      return data ? JSON.parse(data) : {};
    } catch (e) {
      return {};
    }
  },

  /**
   * Get a single cached job by ID
   */
  getCachedJob(id) {
    const map = this.getCachedJobsMap();
    return map[id] || null;
  },

  /**
   * Update cached job locally (e.g., when status is updated offline)
   */
  updateLocalJob(id, updates) {
    const map = this.getCachedJobsMap();
    map[id] = {
      ...(map[id] || {}),
      ...updates,
      _isDirty: true,
      _lastModified: new Date().toISOString(),
    };
    localStorage.setItem(STORAGE_KEYS.OFFLINE_JOBS, JSON.stringify(map));
    return map[id];
  },

  /**
   * Enqueue an action to be executed when online
   */
  enqueueAction(action) {
    const queue = this.getSyncQueue();
    const item = {
      id: `sync_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      action: action.action, // 'ACCEPT', 'START', 'PAUSE', 'RESUME', 'COMPLETE', 'COMMENT', 'GPS_CHECKIN'
      workOrderId: action.workOrderId,
      payload: action.payload || {},
      enqueuedAt: new Date().toISOString(),
      retryCount: 0,
      status: 'PENDING', // 'PENDING', 'PROCESSING', 'FAILED', 'CONFLICT'
      expectedCurrentStatus: action.expectedCurrentStatus || null,
      error: null,
    };

    queue.push(item);
    this.saveSyncQueue(queue);

    // Apply optimistic local update to job cache
    this.applyOptimisticUpdate(action);

    return item;
  },

  /**
   * Get current sync queue items
   */
  getSyncQueue() {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.SYNC_QUEUE);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      return [];
    }
  },

  /**
   * Save sync queue items
   */
  saveSyncQueue(queue) {
    try {
      localStorage.setItem(STORAGE_KEYS.SYNC_QUEUE, JSON.stringify(queue));
    } catch (e) {
      console.warn('Failed to save sync queue', e);
    }
  },

  /**
   * Optimistically update the local job view
   */
  applyOptimisticUpdate(action) {
    const { action: type, workOrderId, payload } = action;
    const job = this.getCachedJob(workOrderId);
    if (!job) return;

    const updates = {};
    if (type === 'ACCEPT') {
      updates.status = 'ACCEPTED';
    } else if (type === 'START' || type === 'RESUME') {
      updates.status = 'IN_PROGRESS';
      if (!job.actualStartDate) updates.actualStartDate = new Date().toISOString();
    } else if (type === 'PAUSE') {
      updates.status = 'ON_HOLD';
    } else if (type === 'COMPLETE') {
      updates.status = 'COMPLETED';
      updates.actualCompletionDate = new Date().toISOString();
      if (payload.completionNotes) updates.completionNotes = payload.completionNotes;
      if (payload.actualCost) updates.actualCost = payload.actualCost;
      if (payload.evidenceMetadata) updates.evidenceMetadata = payload.evidenceMetadata;
    }

    this.updateLocalJob(workOrderId, updates);
  },

  /**
   * Record GPS Check-in for a work order
   */
  recordGpsCheckIn(workOrderId, coords) {
    const checkins = this.getGpsCheckins();
    const checkin = {
      id: `gps_${Date.now()}`,
      workOrderId,
      latitude: coords.latitude,
      longitude: coords.longitude,
      accuracy: coords.accuracy || null,
      timestamp: new Date().toISOString(),
    };
    checkins.push(checkin);
    try {
      localStorage.setItem(STORAGE_KEYS.GPS_CHECKINS, JSON.stringify(checkins));
    } catch (e) {}

    // Add note to sync queue
    this.enqueueAction({
      action: 'GPS_CHECKIN',
      workOrderId,
      payload: checkin,
    });

    return checkin;
  },

  getGpsCheckins(workOrderId = null) {
    try {
      const data = localStorage.getItem(STORAGE_KEYS.GPS_CHECKINS);
      const all = data ? JSON.parse(data) : [];
      return workOrderId ? all.filter((c) => c.workOrderId === workOrderId) : all;
    } catch (e) {
      return [];
    }
  },

  /**
   * Record Time Tracking Interval
   */
  recordTimeLog(workOrderId, interval) {
    try {
      const key = `${STORAGE_KEYS.TIME_LOGS}_${workOrderId}`;
      const data = localStorage.getItem(key);
      const logs = data ? JSON.parse(data) : [];
      logs.push({
        ...interval,
        timestamp: new Date().toISOString(),
      });
      localStorage.setItem(key, JSON.stringify(logs));
    } catch (e) {}
  },

  getTimeLogs(workOrderId) {
    try {
      const key = `${STORAGE_KEYS.TIME_LOGS}_${workOrderId}`;
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      return [];
    }
  },

  /**
   * Save safety checklist and digital signature draft for a job
   */
  saveDraft(workOrderId, draft) {
    try {
      const key = `${STORAGE_KEYS.JOB_DRAFTS}_${workOrderId}`;
      localStorage.setItem(key, JSON.stringify({ ...draft, updatedAt: new Date().toISOString() }));
    } catch (e) {}
  },

  getDraft(workOrderId) {
    try {
      const key = `${STORAGE_KEYS.JOB_DRAFTS}_${workOrderId}`;
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  },

  clearDraft(workOrderId) {
    try {
      localStorage.removeItem(`${STORAGE_KEYS.JOB_DRAFTS}_${workOrderId}`);
    } catch (e) {}
  },

  /**
   * Process all pending items in the sync queue sequentially with conflict detection
   *
   * @param {Object} service - workOrderService instance
   * @returns {Object} { syncedCount, failedCount, conflicts: [] }
   */
  async processSyncQueue(service) {
    if (!this.isOnline()) {
      return { syncedCount: 0, failedCount: 0, conflicts: [], message: 'Device is offline' };
    }

    const queue = this.getSyncQueue();
    if (queue.length === 0) {
      return { syncedCount: 0, failedCount: 0, conflicts: [] };
    }

    let syncedCount = 0;
    let failedCount = 0;
    const conflicts = [];
    const remainingQueue = [];

    for (const item of queue) {
      if (item.status === 'CONFLICT') {
        // Leave in queue until user resolves conflict
        remainingQueue.push(item);
        conflicts.push(item);
        continue;
      }

      try {
        // Step 1: Conflict Detection
        // Fetch fresh server state for the job before executing transition
        let serverJob = null;
        try {
          serverJob = await service.getWorkOrderById(item.workOrderId);
        } catch (fetchErr) {
          // If server job can't be fetched due to network, stop processing
          if (fetchErr.statusCode === 404) {
            item.status = 'FAILED';
            item.error = 'Work order no longer exists on server';
            remainingQueue.push(item);
            failedCount++;
            continue;
          }
          throw fetchErr;
        }

        // Conflict check: Has the server status moved beyond what the client expected?
        if (item.expectedCurrentStatus && serverJob.status !== item.expectedCurrentStatus) {
          // Detect if conflict is benign (e.g. already in the target state)
          const targetStatusMap = {
            ACCEPT: 'ACCEPTED',
            START: 'IN_PROGRESS',
            RESUME: 'IN_PROGRESS',
            PAUSE: 'ON_HOLD',
            COMPLETE: 'COMPLETED',
          };
          const desired = targetStatusMap[item.action];
          if (serverJob.status === desired) {
            // Already synced on server (idempotent), count as success
            syncedCount++;
            this.updateLocalJob(item.workOrderId, serverJob);
            continue;
          }

          // True conflict: Server status differs from what worker's offline action was based upon
          item.status = 'CONFLICT';
          item.conflictDetails = {
            serverStatus: serverJob.status,
            expectedStatus: item.expectedCurrentStatus,
            serverUpdatedAt: serverJob.updatedAt,
          };
          remainingQueue.push(item);
          conflicts.push(item);
          failedCount++;
          continue;
        }

        // Step 2: Execute action on server
        if (item.action === 'ACCEPT') {
          await service.acceptJob(item.workOrderId, item.payload?.remarks);
        } else if (item.action === 'START') {
          await service.startJob(item.workOrderId, item.payload?.remarks);
        } else if (item.action === 'PAUSE') {
          await service.pauseJob(item.workOrderId, item.payload?.reason);
        } else if (item.action === 'RESUME') {
          await service.resumeJob(item.workOrderId, item.payload?.remarks);
        } else if (item.action === 'COMPLETE') {
          await service.completeJob(item.workOrderId, item.payload);
        } else if (item.action === 'COMMENT') {
          await service.addComment(item.workOrderId, item.payload.comment, item.payload.internalOnly);
        } else if (item.action === 'GPS_CHECKIN') {
          const checkinText = `[GPS Check-in] Lat: ${item.payload.latitude}, Lng: ${item.payload.longitude} at ${item.payload.timestamp}`;
          await service.addComment(item.workOrderId, checkinText, true);
        }

        // Sync succeeded: update local cache with latest server representation
        const updatedServer = await service.getWorkOrderById(item.workOrderId);
        this.updateLocalJob(item.workOrderId, updatedServer);
        syncedCount++;
      } catch (err) {
        item.retryCount = (item.retryCount || 0) + 1;
        item.error = err.message || 'Execution error';

        if (item.retryCount >= 3) {
          item.status = 'FAILED';
        }
        remainingQueue.push(item);
        failedCount++;
      }
    }

    this.saveSyncQueue(remainingQueue);
    return { syncedCount, failedCount, conflicts };
  },

  /**
   * Resolve a conflicting sync queue item
   *
   * @param {string} queueItemId
   * @param {'SERVER_WINS' | 'CLIENT_FORCE' | 'DISCARD'} resolutionStrategy
   * @param {Object} service
   */
  async resolveConflict(queueItemId, resolutionStrategy, service) {
    const queue = this.getSyncQueue();
    const itemIndex = queue.findIndex((i) => i.id === queueItemId);
    if (itemIndex === -1) return false;

    const item = queue[itemIndex];

    if (resolutionStrategy === 'DISCARD' || resolutionStrategy === 'SERVER_WINS') {
      // Pull latest server copy and discard local queued change
      if (service) {
        try {
          const fresh = await service.getWorkOrderById(item.workOrderId);
          this.updateLocalJob(item.workOrderId, fresh);
        } catch (e) {}
      }
      queue.splice(itemIndex, 1);
      this.saveSyncQueue(queue);
      return true;
    }

    if (resolutionStrategy === 'CLIENT_FORCE') {
      // Clear conflict flag, reset expectedCurrentStatus so it forces application
      item.status = 'PENDING';
      item.expectedCurrentStatus = null;
      item.retryCount = 0;
      this.saveSyncQueue(queue);
      return this.processSyncQueue(service);
    }

    return false;
  },

  /**
   * Clear all synced and failed items from queue
   */
  clearQueue() {
    this.saveSyncQueue([]);
  },
};

export default syncEngine;
