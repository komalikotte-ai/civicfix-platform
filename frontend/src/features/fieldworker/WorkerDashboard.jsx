import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import workOrderService from '../../services/workOrderService';
import syncEngine from '../../services/syncEngine';
import SyncStatusBanner from './SyncStatusBanner';

export const WorkerDashboard = () => {
  const { user } = useAuth();
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL'); // 'ALL', 'PENDING', 'ACTIVE', 'COMPLETED'
  const [isOnline, setIsOnline] = useState(syncEngine.isOnline());

  const loadJobs = async () => {
    setLoading(true);
    setIsOnline(syncEngine.isOnline());

    if (syncEngine.isOnline()) {
      try {
        const response = await workOrderService.getWorkOrders({ size: 50 });
        const remoteJobs = response?.content || response || [];
        setJobs(remoteJobs);
        // Update offline cache
        syncEngine.cacheJobs(remoteJobs);
      } catch (err) {
        console.warn('Network fetch failed, loading from local cache', err);
        const cached = syncEngine.getCachedJobs();
        setJobs(cached);
      }
    } else {
      const cached = syncEngine.getCachedJobs();
      setJobs(cached);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadJobs();
  }, []);

  // Quick stats
  const assignedCount = jobs.filter((j) => j.status === 'ASSIGNED').length;
  const inProgressCount = jobs.filter((j) => j.status === 'IN_PROGRESS' || j.status === 'ACCEPTED').length;
  const onHoldCount = jobs.filter((j) => j.status === 'ON_HOLD').length;
  const completedCount = jobs.filter((j) => ['COMPLETED', 'PENDING_VERIFICATION', 'VERIFIED'].includes(j.status)).length;

  const filteredJobs = jobs.filter((j) => {
    if (filter === 'PENDING') return j.status === 'ASSIGNED';
    if (filter === 'ACTIVE') return ['ACCEPTED', 'IN_PROGRESS', 'ON_HOLD'].includes(j.status);
    if (filter === 'COMPLETED') return ['COMPLETED', 'PENDING_VERIFICATION', 'VERIFIED'].includes(j.status);
    return true;
  });

  const getStatusBadge = (status) => {
    const styles = {
      CREATED: 'bg-gray-100 text-gray-700 border-gray-300',
      ASSIGNED: 'bg-blue-50 text-blue-700 border-blue-200',
      ACCEPTED: 'bg-cyan-50 text-cyan-700 border-cyan-200',
      IN_PROGRESS: 'bg-emerald-50 text-emerald-700 border-emerald-300 animate-pulse',
      ON_HOLD: 'bg-amber-50 text-amber-700 border-amber-300',
      COMPLETED: 'bg-purple-50 text-purple-700 border-purple-200',
      PENDING_VERIFICATION: 'bg-indigo-50 text-indigo-700 border-indigo-200',
      VERIFIED: 'bg-teal-50 text-teal-800 border-teal-300',
      REJECTED: 'bg-rose-50 text-rose-700 border-rose-300',
      CANCELLED: 'bg-slate-100 text-slate-500 border-slate-300',
    };
    return (
      <span
        className={`px-2 py-0.5 rounded text-[11px] font-semibold border ${
          styles[status] || 'bg-gray-100 text-gray-700 border-gray-300'
        }`}
      >
        {status}
      </span>
    );
  };

  const getPriorityBadge = (priority) => {
    const styles = {
      EMERGENCY: 'text-red-700 bg-red-100 border-red-300 font-bold',
      URGENT: 'text-rose-700 bg-rose-50 border-rose-200',
      HIGH: 'text-orange-700 bg-orange-50 border-orange-200',
      MEDIUM: 'text-amber-700 bg-amber-50 border-amber-200',
      LOW: 'text-slate-700 bg-slate-100 border-slate-200',
    };
    return (
      <span
        className={`px-2 py-0.5 rounded text-[10px] uppercase font-semibold border ${
          styles[priority] || 'text-gray-700 bg-gray-50 border-gray-200'
        }`}
      >
        {priority}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-2 border-b border-gray-200">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Field Operations Dashboard</h1>
          <p className="text-xs text-gray-500 mt-0.5">
            Logged in as {user?.fullName || user?.username} &bull; Station:{' '}
            <span className="font-medium text-gray-700">Central Municipal Zone</span>
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={loadJobs}
            className="px-3 py-1.5 bg-white border border-gray-300 text-gray-700 hover:bg-gray-50 text-xs font-medium rounded-lg shadow-2xs transition-colors flex items-center gap-1.5"
          >
            <span>⟳</span> Refresh Jobs
          </button>
        </div>
      </div>

      {/* Sync Status Banner */}
      <SyncStatusBanner onSyncComplete={loadJobs} />

      {/* Metric Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <div
          onClick={() => setFilter('PENDING')}
          className={`p-4 rounded-xl border cursor-pointer transition-all ${
            filter === 'PENDING'
              ? 'bg-blue-50 border-blue-400 ring-2 ring-blue-400/20 shadow-xs'
              : 'bg-white border-gray-200 hover:border-blue-300 shadow-2xs'
          }`}
        >
          <div className="text-[11px] font-medium text-gray-500 uppercase tracking-wide">Awaiting Accept</div>
          <div className="text-2xl font-bold text-blue-700 mt-1">{assignedCount}</div>
        </div>

        <div
          onClick={() => setFilter('ACTIVE')}
          className={`p-4 rounded-xl border cursor-pointer transition-all ${
            filter === 'ACTIVE'
              ? 'bg-emerald-50 border-emerald-400 ring-2 ring-emerald-400/20 shadow-xs'
              : 'bg-white border-gray-200 hover:border-emerald-300 shadow-2xs'
          }`}
        >
          <div className="text-[11px] font-medium text-gray-500 uppercase tracking-wide">In Progress</div>
          <div className="text-2xl font-bold text-emerald-700 mt-1">{inProgressCount}</div>
        </div>

        <div
          onClick={() => setFilter('ACTIVE')}
          className="p-4 rounded-xl border bg-white border-gray-200 shadow-2xs"
        >
          <div className="text-[11px] font-medium text-gray-500 uppercase tracking-wide">On Hold</div>
          <div className="text-2xl font-bold text-amber-600 mt-1">{onHoldCount}</div>
        </div>

        <div
          onClick={() => setFilter('COMPLETED')}
          className={`p-4 rounded-xl border cursor-pointer transition-all ${
            filter === 'COMPLETED'
              ? 'bg-purple-50 border-purple-400 ring-2 ring-purple-400/20 shadow-xs'
              : 'bg-white border-gray-200 hover:border-purple-300 shadow-2xs'
          }`}
        >
          <div className="text-[11px] font-medium text-gray-500 uppercase tracking-wide">Completed</div>
          <div className="text-2xl font-bold text-purple-700 mt-1">{completedCount}</div>
        </div>
      </div>

      {/* Jobs List Section */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-2xs overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <h2 className="text-sm font-bold text-gray-900">Assigned Field Jobs</h2>
            <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full font-medium">
              {filteredJobs.length}
            </span>
          </div>
          <div className="flex gap-1 bg-gray-100 p-0.5 rounded-lg text-xs font-medium">
            {['ALL', 'PENDING', 'ACTIVE', 'COMPLETED'].map((tab) => (
              <button
                key={tab}
                onClick={() => setFilter(tab)}
                className={`px-3 py-1 rounded-md transition-colors ${
                  filter === tab
                    ? 'bg-white text-gray-900 font-semibold shadow-2xs'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                {tab}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="p-10 text-center text-xs text-gray-500">
            <span className="inline-block animate-spin mr-2">⟳</span> Loading assigned operations...
          </div>
        ) : filteredJobs.length === 0 ? (
          <div className="p-12 text-center text-xs text-gray-500">
            <div className="text-3xl mb-2">📋</div>
            <p className="font-medium text-gray-700">No jobs found in this category.</p>
            <p className="text-gray-400 mt-1">New work assignments will appear here automatically.</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {filteredJobs.map((job) => (
              <Link
                key={job.id}
                to={`/operations/job/${job.id}`}
                className="block p-4 hover:bg-slate-50/80 transition-colors"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-xs font-bold text-blue-700">
                        {job.workOrderNumber}
                      </span>
                      {getPriorityBadge(job.priority)}
                      {getStatusBadge(job.status)}
                    </div>
                    <h3 className="text-sm font-semibold text-gray-900">{job.title}</h3>
                    <p className="text-xs text-gray-500 line-clamp-1">{job.description}</p>
                  </div>

                  <div className="flex sm:flex-col items-end justify-between sm:justify-center text-right text-xs text-gray-500">
                    <span className="font-medium text-gray-700">{job.departmentName}</span>
                    {job.wardName && <span>Ward: {job.wardName}</span>}
                    <span className="text-[11px] text-gray-400">
                      Scheduled: {job.scheduledStartDate ? new Date(job.scheduledStartDate).toLocaleDateString() : 'Immediate'}
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default WorkerDashboard;
