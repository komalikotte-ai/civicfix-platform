import React from 'react';
import Badge from '../../../components/common/Badge';

const STATUS_ICONS = {
  NEW: '📝',
  SUBMITTED: '📝',
  ACKNOWLEDGED: '👀',
  ASSIGNED: '👷',
  IN_PROGRESS: '⚙️',
  ON_HOLD: '⏸️',
  RESOLVED: '✅',
  CLOSED: '🔒',
  REJECTED: '❌',
  REOPENED: '🔄',
  ESCALATED: '⚠️',
};

const STATUS_COLORS = {
  RESOLVED: 'bg-emerald-500 text-white',
  CLOSED: 'bg-slate-600 text-white',
  REOPENED: 'bg-purple-600 text-white',
  IN_PROGRESS: 'bg-blue-600 text-white',
  ASSIGNED: 'bg-indigo-500 text-white',
  ESCALATED: 'bg-red-500 text-white',
  REJECTED: 'bg-rose-500 text-white',
};

export const ComplaintTimeline = ({ history = [], currentStatus }) => {
  if (!history || history.length === 0) {
    return (
      <div className="py-6 text-center text-xs text-slate-500 bg-slate-50 rounded-xl border border-dashed border-slate-200">
        No state transitions recorded yet. The complaint is at its initial stage.
      </div>
    );
  }

  // Sort chronological ascending
  const sorted = [...history].sort(
    (a, b) => new Date(a.transitionTime || a.createdAt || 0) - new Date(b.transitionTime || b.createdAt || 0)
  );

  return (
    <div className="relative pl-6 sm:pl-8 space-y-6 before:absolute before:left-2.5 sm:before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
      {sorted.map((item, index) => {
        const isLast = index === sorted.length - 1;
        const toState = item.toStatus || item.targetStatus || 'UPDATED';
        const fromState = item.fromStatus;
        const icon = STATUS_ICONS[toState] || '📍';
        const colorClass = STATUS_COLORS[toState] || 'bg-blue-500 text-white';
        const timeFormatted = item.transitionTime || item.createdAt
          ? new Date(item.transitionTime || item.createdAt).toLocaleString(undefined, {
              dateStyle: 'medium',
              timeStyle: 'short',
            })
          : 'Timestamp recorded';

        return (
          <div key={item.id || index} className="relative group">
            {/* Timeline node icon */}
            <div
              className={`absolute -left-6 sm:-left-8 top-0.5 h-6 w-6 sm:h-7 sm:w-7 rounded-full flex items-center justify-center text-xs shadow-xs border-2 border-white ${colorClass}`}
              aria-hidden="true"
            >
              {icon}
            </div>

            {/* Event Content Card */}
            <div className={`p-4 rounded-xl border transition-shadow ${isLast ? 'bg-blue-50/40 border-blue-200 shadow-xs' : 'bg-white border-slate-200'}`}>
              <div className="flex flex-wrap items-center justify-between gap-1 mb-1">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-xs text-slate-900">
                    {fromState ? `${fromState} → ${toState}` : toState}
                  </span>
                  <Badge status={toState}>{toState}</Badge>
                </div>
                <time className="text-[11px] font-mono text-slate-500">
                  {timeFormatted}
                </time>
              </div>

              {item.remarks && (
                <p className="text-xs text-slate-700 bg-slate-50 p-2.5 rounded-lg border border-slate-100 my-2 leading-relaxed">
                  "{item.remarks}"
                </p>
              )}

              <div className="text-[11px] text-slate-500 flex items-center gap-2">
                <span>Action By:</span>
                <span className="font-semibold text-slate-800">
                  {item.actionByUsername || item.actionByUser?.username || 'System Automation'}
                </span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default ComplaintTimeline;
