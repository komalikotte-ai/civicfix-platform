import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Select from '../../components/common/Select';
import Input from '../../components/common/Input';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';
import ComplaintTimeline from './components/ComplaintTimeline';
import ComplaintComments from './components/ComplaintComments';
import ReopenComplaintModal from './components/ReopenComplaintModal';
import CitizenFeedbackModal from '../feedback/CitizenFeedbackModal';

const TRANSITION_OPTIONS = {
  SUBMITTED: [
    { value: 'ACKNOWLEDGED', label: 'Acknowledge' },
    { value: 'REJECTED', label: 'Reject' },
  ],
  ACKNOWLEDGED: [
    { value: 'ASSIGNED', label: 'Assign to Field Officer' },
    { value: 'REJECTED', label: 'Reject' },
  ],
  ASSIGNED: [
    { value: 'IN_PROGRESS', label: 'Begin Work (In Progress)' },
    { value: 'ESCALATED', label: 'Escalate to Department Head' },
  ],
  IN_PROGRESS: [
    { value: 'RESOLVED', label: 'Mark as Resolved' },
    { value: 'ESCALATED', label: 'Escalate' },
  ],
  ESCALATED: [
    { value: 'IN_PROGRESS', label: 'Resume Investigation' },
    { value: 'RESOLVED', label: 'Resolve Grievance' },
  ],
  RESOLVED: [
    { value: 'CLOSED', label: 'Confirm & Close' },
    { value: 'IN_PROGRESS', label: 'Re-open for Review' },
  ],
  REJECTED: [{ value: 'SUBMITTED', label: 'Re-open for Re-evaluation' }],
  CLOSED: [],
  REOPENED: [
    { value: 'ASSIGNED', label: 'Re-assign Field Worker' },
    { value: 'IN_PROGRESS', label: 'Resume Work' },
  ],
};

export const ComplaintDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, hasRole } = useAuth();

  const [complaint, setComplaint] = useState(null);
  const [history, setHistory] = useState([]);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Modals state
  const [showReopenModal, setShowReopenModal] = useState(false);
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);

  // Officer transition form
  const [targetStatus, setTargetStatus] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submittingStatus, setSubmittingStatus] = useState(false);

  const canManage =
    hasRole('SUPER_ADMIN') ||
    hasRole('DEPT_ADMIN') ||
    hasRole('SUPERVISOR') ||
    hasRole('OFFICER') ||
    hasRole('EMPLOYEE') ||
    hasRole('OPERATOR');

  const loadData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [data, historyData, commentsData] = await Promise.all([
        complaintService.getComplaintById(id),
        complaintService.getStatusHistory(id).catch(() => []),
        complaintService.getComments(id).catch(() => []),
      ]);
      setComplaint(data);
      setHistory(Array.isArray(historyData) ? historyData : []);
      setComments(Array.isArray(commentsData) ? commentsData : []);
    } catch (err) {
      setError(err.message || 'Failed to load complaint details.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleStatusUpdate = async (e) => {
    e.preventDefault();
    if (!targetStatus) return;
    setSubmittingStatus(true);
    setError('');
    setSuccessMsg('');
    try {
      await complaintService.updateStatus(id, {
        targetStatus,
        remarks: remarks.trim() || undefined,
      });
      setSuccessMsg(`Status updated to ${targetStatus}`);
      setRemarks('');
      setTargetStatus('');
      await loadData();
    } catch (err) {
      setError(err.message || 'Failed to update status.');
    } finally {
      setSubmittingStatus(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-16 gap-3">
        <Spinner size="lg" />
        <span className="text-xs text-slate-500">Loading grievance records...</span>
      </div>
    );
  }

  if (error && !complaint) {
    return (
      <div className="max-w-2xl mx-auto py-8">
        <Alert type="error" message={error} />
        <div className="mt-4 text-center">
          <Button variant="secondary" onClick={() => navigate('/citizen/grievances')}>
            ← Return to Grievances
          </Button>
        </div>
      </div>
    );
  }

  if (!complaint) return null;

  const isResolvedOrClosed = complaint.status === 'RESOLVED' || complaint.status === 'CLOSED';
  const availableTransitions = TRANSITION_OPTIONS[complaint.status] || [];

  return (
    <div className="flex flex-col gap-6 max-w-5xl mx-auto pb-12">
      {/* Top Breadcrumb & Hero */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link
            to="/citizen/grievances"
            className="text-xs text-blue-600 font-semibold hover:underline inline-flex items-center gap-1"
          >
            ← Back to My Grievances
          </Link>
          <div className="flex flex-wrap items-center gap-3 mt-1.5">
            <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
              {complaint.title}
            </h1>
            <Badge status={complaint.status}>{complaint.status}</Badge>
            <Badge status={complaint.priority}>{complaint.priority} Priority</Badge>
          </div>
          <div className="text-xs font-mono text-slate-500 mt-1">
            Tracking ID: <span className="font-bold text-slate-800">{complaint.trackingNumber}</span> • Reported on{' '}
            {complaint.createdAt ? new Date(complaint.createdAt).toLocaleDateString() : 'N/A'}
          </div>
        </div>

        {/* Citizen Action Buttons */}
        <div className="flex flex-wrap items-center gap-2">
          {isResolvedOrClosed && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowFeedbackModal(true)}
                className="text-xs"
              >
                ⭐ Rate Resolution
              </Button>
              <Button
                variant="danger"
                size="sm"
                onClick={() => setShowReopenModal(true)}
                className="text-xs"
              >
                🔄 Reopen Grievance
              </Button>
            </>
          )}
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/track/${encodeURIComponent(complaint.trackingNumber)}`)}
            className="text-xs"
          >
            🔍 Public Tracking
          </Button>
        </div>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {successMsg && <Alert type="success" message={successMsg} onClose={() => setSuccessMsg('')} />}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content Area */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          {/* Grievance Narrative Card */}
          <Card title="Grievance Details & Location">
            <div className="space-y-4 text-xs">
              <div>
                <span className="font-semibold text-slate-500 block mb-1">Description</span>
                <p className="text-slate-800 bg-slate-50 p-3.5 rounded-xl border border-slate-100 leading-relaxed whitespace-pre-wrap">
                  {complaint.description}
                </p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                <div>
                  <span className="font-semibold text-slate-500 block">Category:</span>
                  <div className="text-slate-900 font-bold mt-0.5">{complaint.category}</div>
                </div>
                <div>
                  <span className="font-semibold text-slate-500 block">Observed Severity:</span>
                  <div className="text-slate-900 font-bold mt-0.5">{complaint.severity || 'MEDIUM'}</div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-slate-100">
                <div>
                  <span className="font-semibold text-slate-500 block">Street Address:</span>
                  <div className="text-slate-900 font-medium mt-0.5">
                    {complaint.addressLine || 'Not specified'}
                  </div>
                </div>
                <div>
                  <span className="font-semibold text-slate-500 block">Landmark:</span>
                  <div className="text-slate-900 font-medium mt-0.5">
                    {complaint.landmark || 'None provided'}
                  </div>
                </div>
              </div>

              {(complaint.latitude || complaint.longitude) && (
                <div className="pt-2 border-t border-slate-100 flex items-center gap-3 text-[11px] font-mono text-slate-600">
                  <span>📍 GPS Coordinates:</span>
                  <span className="bg-slate-100 px-2 py-0.5 rounded font-semibold text-slate-900">
                    {complaint.latitude?.toFixed(6)}, {complaint.longitude?.toFixed(6)}
                  </span>
                </div>
              )}
            </div>
          </Card>

          {/* Attachments Gallery */}
          {complaint.attachments && complaint.attachments.length > 0 && (
            <Card title={`Photographic Evidence (${complaint.attachments.length})`}>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                {complaint.attachments.map((att, idx) => (
                  <div
                    key={att.id || idx}
                    className="border border-slate-200 rounded-xl overflow-hidden bg-slate-50 group hover:shadow-md transition-shadow"
                  >
                    {att.fileUrl && att.fileUrl.startsWith('data:image') ? (
                      <a href={att.fileUrl} target="_blank" rel="noopener noreferrer" className="block">
                        <img
                          src={att.fileUrl}
                          alt={att.fileName}
                          className="w-full h-28 object-cover group-hover:scale-105 transition-transform"
                        />
                      </a>
                    ) : (
                      <div className="w-full h-28 flex flex-col items-center justify-center text-slate-400 gap-1 p-2">
                        <span className="text-2xl">📄</span>
                        <span className="text-[10px] text-center truncate max-w-full font-mono">
                          {att.fileName}
                        </span>
                      </div>
                    )}
                    <div className="p-2 bg-white border-t border-slate-100 flex items-center justify-between text-[11px]">
                      <span className="font-medium text-slate-700 truncate" title={att.fileName}>
                        {att.fileName}
                      </span>
                      {att.fileUrl && (
                        <a
                          href={att.fileUrl}
                          download={att.fileName}
                          className="text-blue-600 hover:underline font-bold text-xs"
                          title="Download"
                        >
                          ⬇
                        </a>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}

          {/* Redressal Chronological Timeline */}
          <Card title="Grievance Redressal Timeline">
            <ComplaintTimeline history={history} currentStatus={complaint.status} />
          </Card>

          {/* Citizen & Officer Comments Thread */}
          <Card title="Citizen & Municipal Dialogue">
            <ComplaintComments
              complaintId={complaint.id}
              comments={comments}
              onCommentAdded={loadData}
            />
          </Card>
        </div>

        {/* Sidebar: Jurisdiction & SLA Governance */}
        <div className="flex flex-col gap-6">
          <Card title="Governance & SLA Status">
            <div className="space-y-3.5 text-xs">
              <div>
                <span className="text-slate-500 block">Municipal Ward:</span>
                <div className="font-bold text-slate-900 mt-0.5">
                  {complaint.wardName || (complaint.wardNumber ? `Ward #${complaint.wardNumber}` : 'General Ward')}
                </div>
              </div>

              <div>
                <span className="text-slate-500 block">Department:</span>
                <div className="font-bold text-slate-900 mt-0.5">
                  {complaint.departmentName || 'General Municipal Services'}
                </div>
              </div>

              <div>
                <span className="text-slate-500 block">Assigned Field Officer:</span>
                <div className="font-bold text-slate-900 mt-0.5">
                  {complaint.assignedOfficerName || complaint.assignedEmployeeName || 'Awaiting Triage Assignment'}
                </div>
              </div>

              <div className="pt-2 border-t border-slate-100">
                <span className="text-slate-500 block">Resolution Target (SLA):</span>
                <div className="font-bold text-amber-700 mt-0.5">
                  {complaint.slaDueAt
                    ? new Date(complaint.slaDueAt).toLocaleString()
                    : 'Within 48-72 Hours'}
                </div>
              </div>

              {complaint.resolvedAt && (
                <div className="pt-2 border-t border-slate-100">
                  <span className="text-slate-500 block">Actual Resolution Date:</span>
                  <div className="font-bold text-emerald-600 mt-0.5">
                    {new Date(complaint.resolvedAt).toLocaleString()}
                  </div>
                </div>
              )}

              {complaint.reopenedCount > 0 && (
                <div className="p-2.5 rounded-lg bg-purple-50 border border-purple-200 text-purple-900 text-xs">
                  <span className="font-bold">Reopened Count: {complaint.reopenedCount}</span>
                  {complaint.lastReopenedAt && (
                    <div className="text-[11px] text-purple-700 mt-0.5">
                      Last on: {new Date(complaint.lastReopenedAt).toLocaleString()}
                    </div>
                  )}
                </div>
              )}
            </div>
          </Card>

          {/* Operational Workflow Controls for Staff */}
          {canManage && availableTransitions.length > 0 && (
            <Card title="Staff Workflow Management">
              <form onSubmit={handleStatusUpdate} className="flex flex-col gap-3">
                <Select
                  label="Advance Workflow State"
                  options={availableTransitions}
                  value={targetStatus}
                  onChange={(e) => setTargetStatus(e.target.value)}
                  placeholder="Select Next State"
                  required
                />

                <Input
                  label="Operational Remarks"
                  placeholder="e.g. Field inspection completed, dispatching repair van..."
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                />

                <Button
                  type="submit"
                  variant="primary"
                  loading={submittingStatus}
                  disabled={!targetStatus}
                  className="mt-1"
                >
                  Commit State Change
                </Button>
              </form>
            </Card>
          )}
        </div>
      </div>

      {/* Reopen Complaint Modal */}
      {showReopenModal && (
        <ReopenComplaintModal
          isOpen={showReopenModal}
          complaint={complaint}
          onClose={() => setShowReopenModal(false)}
          onSuccess={() => {
            setShowReopenModal(false);
            setSuccessMsg('Grievance has been successfully reopened for further inspection.');
            loadData();
          }}
        />
      )}

      {/* Citizen Feedback Modal */}
      {showFeedbackModal && (
        <CitizenFeedbackModal
          isOpen={showFeedbackModal}
          complaint={complaint}
          onClose={() => setShowFeedbackModal(false)}
          onSuccess={() => {
            setShowFeedbackModal(false);
            setSuccessMsg('Thank you for rating municipal grievance redressal!');
            loadData();
          }}
        />
      )}
    </div>
  );
};

export default ComplaintDetailPage;
