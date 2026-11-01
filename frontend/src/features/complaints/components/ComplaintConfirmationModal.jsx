import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../../components/common/Button';
import Badge from '../../../components/common/Badge';

export const ComplaintConfirmationModal = ({ complaint, onReset }) => {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);

  if (!complaint) return null;

  const trackingNumber = complaint.trackingNumber || `CF-${complaint.id}`;

  const handleCopy = () => {
    navigator.clipboard?.writeText(trackingNumber);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const slaFormatted = complaint.slaDueAt
    ? new Date(complaint.slaDueAt).toLocaleString(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
    : 'Within 48-72 Hours';

  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-xl overflow-hidden max-w-2xl mx-auto my-4 animate-in fade-in duration-300">
      {/* Top Emerald Banner */}
      <div className="bg-gradient-to-r from-emerald-600 to-teal-700 px-6 py-8 text-center text-white">
        <div className="w-16 h-16 rounded-full bg-white/20 border-2 border-white/40 flex items-center justify-center text-3xl mx-auto mb-3 shadow-inner">
          ✓
        </div>
        <h2 className="text-2xl font-black tracking-tight">Grievance Lodged Successfully!</h2>
        <p className="text-xs text-emerald-100 mt-1 max-w-md mx-auto">
          Your complaint has been registered with municipal administration and routed for initial triage and field assignment.
        </p>
      </div>

      <div className="p-6 sm:p-8 flex flex-col gap-6">
        {/* Tracking ID Hero Box */}
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-5 text-center flex flex-col items-center gap-2">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
            Official Tracking Number
          </span>
          <div className="flex items-center justify-center gap-3">
            <span className="font-mono text-2xl sm:text-3xl font-extrabold text-blue-700 tracking-wider">
              {trackingNumber}
            </span>
            <Button
              type="button"
              size="sm"
              variant="outline"
              onClick={handleCopy}
              className="text-xs"
            >
              {copied ? '✓ Copied' : '📋 Copy'}
            </Button>
          </div>
          <p className="text-[11px] text-slate-500">
            Keep this tracking number handy to check resolution progress anytime without logging in.
          </p>
        </div>

        {/* Complaint Summary Details Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
            <span className="text-slate-500 block font-semibold">Title</span>
            <span className="text-slate-900 font-bold mt-0.5 block truncate">
              {complaint.title}
            </span>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
            <span className="text-slate-500 block font-semibold">Category</span>
            <span className="text-slate-900 font-bold mt-0.5 block">
              {complaint.category}
            </span>
          </div>

          <div className="p-3 bg-slate-50 rounded-lg border border-slate-100">
            <span className="text-slate-500 block font-semibold">Ward / Location</span>
            <span className="text-slate-900 font-bold mt-0.5 block">
              {complaint.wardName || (complaint.wardId ? `Ward #${complaint.wardId}` : 'Designated Ward')}
            </span>
          </div>

          <div className="p-3 bg-emerald-50 rounded-lg border border-emerald-100">
            <span className="text-emerald-700 block font-semibold">Target SLA Resolution</span>
            <span className="text-emerald-900 font-bold mt-0.5 block">
              {slaFormatted}
            </span>
          </div>
        </div>

        {/* Next Steps Card */}
        <div className="border-t border-slate-100 pt-4">
          <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider mb-2">
            What Happens Next?
          </h4>
          <ol className="text-xs text-slate-600 space-y-1.5 list-decimal list-inside">
            <li>Your ward supervisor verifies the report and assigns a field technician.</li>
            <li>You will receive SMS and email status updates as work progresses.</li>
            <li>Once resolved, you will be invited to inspect and rate the municipal redressal.</li>
          </ol>
        </div>

        {/* Navigation Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-2">
          <Button
            type="button"
            variant="ghost"
            onClick={onReset}
            className="w-full sm:w-auto text-xs"
          >
            + File Another Grievance
          </Button>

          <div className="flex items-center gap-2 w-full sm:w-auto">
            <Button
              type="button"
              variant="outline"
              onClick={() => navigate('/citizen/grievances')}
              className="w-full sm:w-auto text-xs"
            >
              My Grievance History
            </Button>
            <Button
              type="button"
              variant="primary"
              onClick={() => navigate(`/track/${encodeURIComponent(trackingNumber)}`)}
              className="w-full sm:w-auto text-xs font-bold shadow-md"
            >
              Track Grievance →
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ComplaintConfirmationModal;
