import React, { useState, useEffect } from 'react';
import { syncEngine } from '../../services/syncEngine';
import workOrderService from '../../services/workOrderService';

export const SyncStatusBanner = ({ onSyncComplete }) => {
  const [isOnline, setIsOnline] = useState(syncEngine.isOnline());
  const [queueCount, setQueueCount] = useState(0);
  const [conflicts, setConflicts] = useState([]);
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncMessage, setSyncMessage] = useState('');

  const refreshQueueStatus = () => {
    setIsOnline(syncEngine.isOnline());
    const queue = syncEngine.getSyncQueue();
    setQueueCount(queue.length);
    setConflicts(queue.filter((q) => q.status === 'CONFLICT'));
  };

  useEffect(() => {
    refreshQueueStatus();

    const handleOnline = () => {
      setIsOnline(true);
      triggerAutoSync();
    };
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    const interval = setInterval(refreshQueueStatus, 5000);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(interval);
    };
  }, []);

  const triggerAutoSync = async () => {
    if (!syncEngine.isOnline()) return;
    setIsSyncing(true);
    setSyncMessage('Synchronizing offline changes with server...');
    try {
      const res = await syncEngine.processSyncQueue(workOrderService);
      if (res.syncedCount > 0) {
        setSyncMessage(`Synced ${res.syncedCount} changes successfully.`);
        if (onSyncComplete) onSyncComplete();
      } else if (res.conflicts.length > 0) {
        setSyncMessage(`${res.conflicts.length} conflict(s) detected requiring review.`);
      } else {
        setSyncMessage('');
      }
    } catch (e) {
      setSyncMessage('Sync encountered an error.');
    } finally {
      setIsSyncing(false);
      refreshQueueStatus();
    }
  };

  const handleResolveConflict = async (itemId, strategy) => {
    await syncEngine.resolveConflict(itemId, strategy, workOrderService);
    refreshQueueStatus();
    if (onSyncComplete) onSyncComplete();
  };

  if (isOnline && queueCount === 0 && conflicts.length === 0 && !syncMessage) {
    return null;
  }

  return (
    <div className="mb-4 space-y-2">
      {/* Offline Alert */}
      {!isOnline && (
        <div className="bg-amber-500 text-white px-4 py-2.5 rounded-lg shadow-sm flex items-center justify-between text-xs font-medium">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 bg-amber-200 rounded-full animate-ping" />
            <span>
              <strong>Offline Mode Active:</strong> All job updates, GPS stamps, and notes are being queued locally.
            </span>
          </div>
          <span className="bg-amber-600/70 px-2 py-0.5 rounded text-[11px]">
            {queueCount} action(s) queued
          </span>
        </div>
      )}

      {/* Syncing or Pending Queue Bar */}
      {isOnline && (queueCount > 0 || isSyncing) && (
        <div className="bg-blue-600 text-white px-4 py-2.5 rounded-lg shadow-sm flex items-center justify-between text-xs font-medium">
          <div className="flex items-center gap-2">
            {isSyncing && <span className="animate-spin text-sm">⟳</span>}
            <span>
              {isSyncing ? syncMessage : `Online. You have ${queueCount} offline update(s) ready to sync.`}
            </span>
          </div>
          <button
            onClick={triggerAutoSync}
            disabled={isSyncing}
            className="bg-white text-blue-700 hover:bg-blue-50 px-3 py-1 rounded font-semibold text-xs shadow-sm transition-colors disabled:opacity-50"
          >
            {isSyncing ? 'Syncing...' : 'Sync Now'}
          </button>
        </div>
      )}

      {/* Conflict Resolution Modal / Card */}
      {conflicts.length > 0 && (
        <div className="bg-red-50 border border-red-200 text-red-900 p-4 rounded-lg text-xs space-y-2">
          <div className="font-bold text-red-800 flex items-center gap-1.5">
            <span>⚠</span>
            <span>Conflict Detected ({conflicts.length})</span>
          </div>
          <p className="text-red-700">
            A work order status on the central server changed while you were offline. Choose how to resolve:
          </p>
          <div className="space-y-2 pt-1">
            {conflicts.map((conf) => (
              <div
                key={conf.id}
                className="bg-white p-3 rounded border border-red-100 flex flex-col md:flex-row md:items-center justify-between gap-2 shadow-xs"
              >
                <div>
                  <div className="font-semibold text-gray-900">
                    Action: {conf.action} on Job #{conf.workOrderId}
                  </div>
                  <div className="text-[11px] text-gray-600">
                    Server Status: <span className="font-mono font-bold">{conf.conflictDetails?.serverStatus}</span> |
                    Your Action expected:{' '}
                    <span className="font-mono font-bold">{conf.conflictDetails?.expectedStatus}</span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleResolveConflict(conf.id, 'SERVER_WINS')}
                    className="px-2.5 py-1 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded font-medium text-[11px]"
                  >
                    Accept Server Status
                  </button>
                  <button
                    onClick={() => handleResolveConflict(conf.id, 'CLIENT_FORCE')}
                    className="px-2.5 py-1 bg-red-600 hover:bg-red-700 text-white rounded font-medium text-[11px]"
                  >
                    Overwrite With My Changes
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default SyncStatusBanner;
