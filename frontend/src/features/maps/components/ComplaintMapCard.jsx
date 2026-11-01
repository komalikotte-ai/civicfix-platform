import React from 'react';
import { useNavigate } from 'react-router-dom';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';

export const ComplaintMapCard = ({ complaint, onClose }) => {
  const navigate = useNavigate();
  if (!complaint) return null;

  return (
    <div
      role="dialog"
      aria-labelledby="map-complaint-title"
      className="absolute bottom-4 left-4 right-4 sm:left-auto sm:right-4 sm:w-80 bg-white/95 backdrop-blur-md rounded-2xl shadow-2xl border border-slate-200 p-4 z-40 text-xs flex flex-col gap-3 animate-in slide-in-from-bottom duration-200"
    >
      <div className="flex items-start justify-between gap-2 border-b border-slate-100 pb-2.5">
        <div>
          <span className="font-mono font-bold text-blue-600 bg-blue-50 px-2 py-0.5 rounded text-[11px]">
            {complaint.trackingNumber || `CF-${complaint.id}`}
          </span>
          <h3 id="map-complaint-title" className="font-bold text-sm text-slate-900 mt-1 line-clamp-1">
            {complaint.title}
          </h3>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="text-slate-400 hover:text-slate-600 font-bold p-1 cursor-pointer"
          aria-label="Close details"
        >
          ✕
        </button>
      </div>

      <div className="flex items-center gap-1.5 flex-wrap">
        <Badge status={complaint.status}>{complaint.status}</Badge>
        <Badge status={complaint.priority}>{complaint.priority} Priority</Badge>
        {complaint.severity && <Badge status={complaint.severity}>{complaint.severity}</Badge>}
      </div>

      <div className="space-y-1 text-slate-600 text-[11px] bg-slate-50 p-2.5 rounded-lg border border-slate-100">
        <div>
          <span className="font-semibold text-slate-500">Category:</span>{' '}
          <span className="font-medium text-slate-900">{complaint.category || 'General'}</span>
        </div>
        <div>
          <span className="font-semibold text-slate-500">Location:</span>{' '}
          <span className="font-medium text-slate-900">{complaint.addressLine || 'Ward territory'}</span>
        </div>
        {complaint.landmark && (
          <div>
            <span className="font-semibold text-slate-500">Landmark:</span>{' '}
            <span className="font-medium text-slate-900">{complaint.landmark}</span>
          </div>
        )}
        <div>
          <span className="font-semibold text-slate-500">Ward:</span>{' '}
          <span className="font-medium text-slate-900">{complaint.wardName || `Ward #${complaint.wardId || '1'}`}</span>
        </div>
      </div>

      <div className="flex items-center gap-2 pt-1">
        <Button
          size="sm"
          variant="outline"
          onClick={() => navigate(`/track/${encodeURIComponent(complaint.trackingNumber || complaint.id)}`)}
          className="flex-1 text-xs"
        >
          Track Status
        </Button>
        <Button
          size="sm"
          variant="primary"
          onClick={() => navigate(`/complaints/${complaint.id}`)}
          className="flex-1 text-xs font-semibold"
        >
          View Details →
        </Button>
      </div>
    </div>
  );
};

export default ComplaintMapCard;
