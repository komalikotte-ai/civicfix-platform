import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { citizenService } from '../../services/citizenService';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Table from '../../components/common/Table';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import CitizenFeedbackModal from '../feedback/CitizenFeedbackModal';
import SavedLocationsModal from './components/SavedLocationsModal';
import NotificationPreferencesModal from './components/NotificationPreferencesModal';

export const CitizenDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState({ total: 0, active: 0, resolved: 0, onHold: 0 });
  const [recentGrievances, setRecentGrievances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Quick Tracking Search in Hero
  const [quickTrackNumber, setQuickTrackNumber] = useState('');

  // Modals state
  const [feedbackComplaint, setFeedbackComplaint] = useState(null);
  const [feedbackSuccessMsg, setFeedbackSuccessMsg] = useState('');
  const [showSavedLocationsModal, setShowSavedLocationsModal] = useState(false);
  const [showNotificationModal, setShowNotificationModal] = useState(false);

  const loadDashboardData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsData, complaintsResponse] = await Promise.all([
        citizenService.getCitizenStats(),
        citizenService.getMyGrievances({ size: 5, sortBy: 'createdAt', sortDirection: 'desc' }),
      ]);

      setStats(statsData);
      const list = complaintsResponse?.content || (Array.isArray(complaintsResponse) ? complaintsResponse : []);
      setRecentGrievances(list);
    } catch (err) {
      setError(err.message || 'Failed to load citizen portal data.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  const handleQuickTrackSubmit = (e) => {
    e.preventDefault();
    if (!quickTrackNumber.trim()) return;
    navigate(`/track/${encodeURIComponent(quickTrackNumber.trim())}`);
  };

  const columns = [
    {
      key: 'trackingNumber',
      header: 'Tracking ID',
      render: (row) => (
        <span className="font-mono text-xs font-semibold text-blue-600 hover:underline">
          {row.trackingNumber || `CF-${row.id}`}
        </span>
      ),
    },
    {
      key: 'title',
      header: 'Title & Category',
      render: (row) => (
        <div>
          <div className="font-medium text-slate-900 line-clamp-1">{row.title}</div>
          <div className="text-[11px] text-slate-500">{row.category || 'General'}</div>
        </div>
      ),
    },
    {
      key: 'wardName',
      header: 'Ward / Location',
      render: (row) => (
        <span className="text-xs text-slate-600">
          {row.wardName || (row.wardId ? `Ward #${row.wardId}` : 'City Wide')}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge status={row.status}>{row.status}</Badge>,
    },
    {
      key: 'createdAt',
      header: 'Submitted',
      render: (row) => (
        <span className="text-xs text-slate-500">
          {row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (row) => (
        <div className="flex items-center justify-end gap-2" onClick={(e) => e.stopPropagation()}>
          <Button
            size="sm"
            variant="ghost"
            onClick={() => navigate(`/complaints/${row.id}`)}
          >
            View
          </Button>
          {(row.status === 'RESOLVED' || row.status === 'CLOSED') && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setFeedbackComplaint(row)}
            >
              Rate
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6 max-w-6xl mx-auto pb-12">
      {/* Welcome Citizen Hero Banner */}
      <div className="bg-gradient-to-r from-blue-700 via-blue-800 to-indigo-900 rounded-2xl p-6 sm:p-8 text-white shadow-lg flex flex-col gap-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-blue-500/30 text-blue-200 text-xs font-semibold mb-3 border border-blue-400/30">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              Citizen Grievance Redressal
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              Welcome, {user?.fullName || user?.username || 'Citizen'}
            </h1>
            <p className="text-sm text-blue-100/90 mt-1.5 max-w-xl">
              Report civic issues in your ward, track real-time resolution status, and help make our city cleaner, safer, and smarter.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <Button
              variant="success"
              size="lg"
              onClick={() => navigate('/complaints/new')}
              className="shadow-md font-bold text-sm"
            >
              <span aria-hidden="true">+</span> File Grievance
            </Button>
            <Button
              variant="outline"
              size="lg"
              onClick={() => setShowSavedLocationsModal(true)}
              className="bg-white/10 text-white border-white/20 hover:bg-white/20 text-xs"
            >
              📍 Saved Locations
            </Button>
            <Button
              variant="outline"
              size="lg"
              onClick={() => setShowNotificationModal(true)}
              className="bg-white/10 text-white border-white/20 hover:bg-white/20 text-xs"
            >
              🔔 Notifications
            </Button>
          </div>
        </div>

        {/* Quick Tracking Search Bar inside Hero */}
        <div className="bg-white/10 backdrop-blur-sm p-3.5 rounded-xl border border-white/20 flex flex-col sm:flex-row items-center justify-between gap-3">
          <span className="text-xs font-medium text-blue-100 flex items-center gap-1.5">
            🔍 Quick Grievance Tracking:
          </span>
          <form onSubmit={handleQuickTrackSubmit} className="flex items-center gap-2 w-full sm:w-auto">
            <input
              type="text"
              placeholder="e.g. CF-20261030-8901"
              value={quickTrackNumber}
              onChange={(e) => setQuickTrackNumber(e.target.value)}
              className="px-3 py-1.5 rounded-lg bg-white text-slate-900 text-xs font-mono placeholder:text-slate-400 border border-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-400 w-full sm:w-56"
            />
            <button
              type="submit"
              className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold transition-colors whitespace-nowrap cursor-pointer"
            >
              Track Now
            </button>
          </form>
        </div>
      </div>

      {feedbackSuccessMsg && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-sm flex items-center justify-between">
          <span>{feedbackSuccessMsg}</span>
          <button
            type="button"
            onClick={() => setFeedbackSuccessMsg('')}
            className="text-emerald-700 font-bold ml-4 cursor-pointer"
          >
            ✕
          </button>
        </div>
      )}

      {/* Grievance Metrics Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-5 flex items-center justify-between border-l-4 border-l-blue-500">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Total Grievances
            </div>
            <div className="text-2xl font-black text-slate-900 mt-1">{stats.total}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
            📋
          </div>
        </Card>

        <Card className="p-5 flex items-center justify-between border-l-4 border-l-amber-500">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Active / In Progress
            </div>
            <div className="text-2xl font-black text-amber-600 mt-1">{stats.active}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
            ⏳
          </div>
        </Card>

        <Card className="p-5 flex items-center justify-between border-l-4 border-l-emerald-500">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Resolved & Closed
            </div>
            <div className="text-2xl font-black text-emerald-600 mt-1">{stats.resolved}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
            ✓
          </div>
        </Card>

        <Card className="p-5 flex items-center justify-between border-l-4 border-l-purple-500">
          <div>
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              On Hold / Review
            </div>
            <div className="text-2xl font-black text-purple-600 mt-1">{stats.onHold}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center font-bold">
            ⏸
          </div>
        </Card>
      </div>

      {/* Recent Grievances Section */}
      <div className="flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-slate-900">Your Recent Grievances</h2>
            <p className="text-xs text-slate-500">Track and manage your submitted reports</p>
          </div>
          <Link
            to="/citizen/grievances"
            className="text-xs font-semibold text-blue-600 hover:text-blue-800 flex items-center gap-1"
          >
            View all history ({stats.total}) →
          </Link>
        </div>

        {loading ? (
          <LoadingState message="Loading your grievances..." />
        ) : error ? (
          <ErrorState message={error} onRetry={loadDashboardData} />
        ) : recentGrievances.length === 0 ? (
          <EmptyState
            title="No grievances submitted yet"
            description="You have not reported any civic issues yet. Click below to file your first grievance."
            actionLabel="+ Report an Issue"
            onAction={() => navigate('/complaints/new')}
          />
        ) : (
          <Table
            columns={columns}
            data={recentGrievances}
            onRowClick={(row) => navigate(`/complaints/${row.id}`)}
          />
        )}
      </div>

      {/* Citizen Feedback Modal */}
      {feedbackComplaint && (
        <CitizenFeedbackModal
          isOpen={Boolean(feedbackComplaint)}
          complaint={feedbackComplaint}
          onClose={() => setFeedbackComplaint(null)}
          onSuccess={() => {
            setFeedbackComplaint(null);
            setFeedbackSuccessMsg('Thank you! Your feedback has been recorded.');
            loadDashboardData();
          }}
        />
      )}

      {/* Saved Locations Management Modal */}
      {showSavedLocationsModal && (
        <SavedLocationsModal
          isOpen={showSavedLocationsModal}
          onClose={() => setShowSavedLocationsModal(false)}
          userId={user?.id}
        />
      )}

      {/* Notification Preferences Modal */}
      {showNotificationModal && (
        <NotificationPreferencesModal
          isOpen={showNotificationModal}
          onClose={() => setShowNotificationModal(false)}
          userId={user?.id}
        />
      )}
    </div>
  );
};

export default CitizenDashboard;
