import React from 'react';
import Badge from '../../../components/common/Badge.jsx';
import { formatDistance } from '../utils/geoUtils.js';

export const NearbyComplaintsDrawer = ({
  nearbyComplaints = [],
  selectedComplaintId,
  onSelectComplaint,
}) => {
  if (!nearbyComplaints || nearbyComplaints.length === 0) {
    return (
      <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl text-center text-xs text-slate-500">
        No complaints detected within this local vicinity.
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between px-1">
        <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
          Nearby Grievances ({nearbyComplaints.length})
        </h4>
        <span className="text-[11px] text-slate-500">Sorted by proximity</span>
      </div>

      <div className="space-y-2 max-h-72 overflow-y-auto pr-1 divide-y divide-slate-100">
        {nearbyComplaints.map((c) => {
          const isSelected = String(c.id) === String(selectedComplaintId);
          return (
            <button
              key={c.id}
              type="button"
              onClick={() => onSelectComplaint(c)}
              className={`w-full text-left p-2.5 rounded-xl border transition-all cursor-pointer flex flex-col gap-1 ${
                isSelected
                  ? 'bg-blue-50/70 border-blue-400 ring-1 ring-blue-400 shadow-xs'
                  : 'bg-white border-slate-200 hover:border-slate-300'
              }`}
            >
              <div className="flex items-center justify-between w-full">
                <span className="font-mono text-xs font-bold text-blue-600">
                  {c.trackingNumber || `CF-${c.id}`}
                </span>
                <span className="text-[11px] font-semibold text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded">
                  {formatDistance(c.distance)} away
                </span>
              </div>

              <div className="font-semibold text-xs text-slate-900 truncate w-full">
                {c.title}
              </div>

              <div className="flex items-center gap-1.5 text-[11px] mt-0.5">
                <Badge status={c.status}>{c.status}</Badge>
                <span className="text-slate-500">• {c.category || 'General'}</span>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default NearbyComplaintsDrawer;
