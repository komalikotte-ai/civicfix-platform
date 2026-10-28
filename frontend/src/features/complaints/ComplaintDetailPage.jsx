import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Select from '../../components/common/Select';
import Input from '../../components/common/Input';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';

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
    { value: 'RESOLVED', label: 'Resolve Escaped Grievance' },
  ],
  RESOLVED: [
    { value: 'CLOSED', label: 'Confirm & Close' },
    { value: 'IN_PROGRESS', label: 'Re-open for Review' },
  ],
  REJECTED: [{ value: 'SUBMITTED', label: 'Re-open for Re-evaluation' }],
  CLOSED: [],
};

export const ComplaintDetailPage = () => {
  const { id } = useParams();
  const { user, hasRole } = useAuth();
  const [complaint, setComplaint] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [targetStatus, setTargetStatus] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const canManage = hasRole('SUPER_ADMIN') || hasRole('DEPT_ADMIN') || hasRole('OFFICER') || hasRole('OPERATOR');

  const loadData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [data, workflow] = await Promise.all([
        complaintService.getComplaintById(id),
        complaintService.getWorkflowHistory(id),
      ]);
      setComplaint(data);
      setHistory(workflow || []);
    } catch (err) {
      setError(err.message || 'Failed to load complaint details');
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
    setSubmitting(true);
    setError('');
    setSuccessMsg('');
    try {
      await complaintService.updateStatus(id, {
        targetStatus,
        remarks,
      });
      setSuccessMsg(`Status updated to ${targetStatus}`);
      setRemarks('');
      setTargetStatus('');
      await loadData();
    } catch (err) {
      setError(err.message || 'Failed to update status');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!complaint) {
    return <Alert type="error" message="Complaint record not found" />;
  }

  const availableTransitions = TRANSITION_OPTIONS[complaint.status] || [];

  return (
    <div className="flex flex-col gap-6">
      {/* Back button and title */}
      <div className="flex items-center justify-between">
        <div>
          <Link to="/complaints" className="text-xs text-blue-600 font-semibold hover:underline">
            ← Back to Complaints
          </Link>
          <div className="flex items-center gap-3 mt-1">
            <h2 className="text-xl font-bold text-gray-900">{complaint.title}</h2>
            <Badge variant={complaint.status} />
            <Badge variant={complaint.priority} />
          </div>
          <div className="text-xs text-gray-500 font-mono mt-0.5">
            Tracking ID: {complaint.trackingNumber}
          </div>
        </div>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {successMsg && <Alert type="success" message={successMsg} onClose={() => setSuccessMsg('')} />}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Details */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          <Card title="Grievance Details">
            <div className="space-y-4 text-xs">
              <div>
                <span className="font-semibold text-gray-500 block mb-1">Description</span>
                <p className="text-gray-800 bg-gray-50 p-3 rounded leading-relaxed whitespace-pre-wrap">
                  {complaint.description}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-4 pt-2">
                <div>
                  <span className="font-semibold text-gray-500">Category:</span>
                  <div className="text-gray-900 font-medium mt-0.5">{complaint.category}</div>
                </div>
                <div>
                  <span className="font-semibold text-gray-500">Address / Location:</span>
                  <div className="text-gray-900 font-medium mt-0.5">
                    {complaint.addressLine || 'Not specified'}
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 pt-2 border-t border-gray-100">
                <div>
                  <span className="font-semibold text-gray-500">Reported By:</span>
                  <div className="text-gray-900 font-medium mt-0.5">{complaint.citizenName}</div>
                </div>
                <div>
                  <span className="font-semibold text-gray-500">Assigned Officer:</span>
                  <div className="text-gray-900 font-medium mt-0.5">
                    {complaint.assignedOfficerName || 'Unassigned'}
                  </div>
                </div>
              </div>
            </div>
          </Card>

          {/* Workflow History */}
          <Card title="Redressal Workflow History">
            {!history.length ? (
              <div className="text-xs text-gray-500 py-4">No status transitions recorded yet.</div>
            ) : (
              <div className="space-y-4">
                {history.map((step, idx) => (
                  <div key={step.id || idx} className="flex items-start gap-3 text-xs">
                    <div className="h-2 w-2 rounded-full bg-blue-600 mt-1.5 flex-shrink-0" />
                    <div className="flex-1 bg-gray-50 p-2.5 rounded border border-gray-100">
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-gray-900">
                          {step.fromStatus} → {step.toStatus}
                        </span>
                        <span className="text-gray-400 text-[10px]">
                          {new Date(step.createdAt).toLocaleString()}
                        </span>
                      </div>
                      {step.remarks && (
                        <p className="text-gray-600 mt-1 italic">"{step.remarks}"</p>
                      )}
                      <div className="text-[10px] text-gray-500 mt-1">
                        By user: {step.actionByUsername}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        {/* Sidebar Status Management */}
        <div className="flex flex-col gap-6">
          <Card title="SLA & Governance">
            <div className="space-y-3 text-xs">
              <div>
                <span className="text-gray-500">Ward Number:</span>
                <div className="font-semibold text-gray-900">Ward #{complaint.wardNumber}</div>
              </div>
              <div>
                <span className="text-gray-500">Department:</span>
                <div className="font-semibold text-gray-900">{complaint.departmentName || 'General Triage'}</div>
              </div>
              <div>
                <span className="text-gray-500">SLA Due Target:</span>
                <div className="font-semibold text-red-600">
                  {complaint.slaDueAt ? new Date(complaint.slaDueAt).toLocaleString() : 'N/A'}
                </div>
              </div>
              {complaint.resolvedAt && (
                <div>
                  <span className="text-gray-500">Resolved At:</span>
                  <div className="font-semibold text-emerald-600">
                    {new Date(complaint.resolvedAt).toLocaleString()}
                  </div>
                </div>
              )}
            </div>
          </Card>

          {/* Workflow Transition Action Card */}
          {canManage && availableTransitions.length > 0 && (
            <Card title="Update Workflow State">
              <form onSubmit={handleStatusUpdate} className="flex flex-col gap-3">
                <Select
                  label="Target State"
                  options={availableTransitions}
                  value={targetStatus}
                  onChange={(e) => setTargetStatus(e.target.value)}
                  placeholder="Select Next State"
                  required
                />

                <Input
                  label="Operational Remarks"
                  placeholder="Reason or update notes..."
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                />

                <Button type="submit" loading={submitting} disabled={!targetStatus} className="mt-1">
                  Commit State Change
                </Button>
              </form>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default ComplaintDetailPage;
