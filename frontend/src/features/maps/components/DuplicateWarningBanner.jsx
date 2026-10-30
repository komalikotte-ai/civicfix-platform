import React from 'react';
import Badge from '../../../components/common/Badge.jsx';
import Button from '../../../components/common/Button.jsx';
import { formatDistance } from '../utils/geoUtils.js';

export const DuplicateWarningBanner = ({
  potentialDuplicates = [],
  onViewExisting,
  onProceedAnyway,
  onDismiss,
}) => {
  if (!potentialDuplicates || potentialDuplicates.length === 0) return null;

  const topMatch = potentialDuplicates[0];
  const distanceStr = formatDistance(topMatch.distance);
  const similarityPercent = topMatch.duplicateSimilarityScore
    ? Math.round(topMatch.duplicateSimilarityScore * 100)
    : 85;

  return (
    <div
      role="alert"
      className="p-4 rounded-xl border border-amber-300 bg-amber-50 text-amber-900 shadow-sm flex flex-col gap-3 animate-in fade-in duration-200"
    >
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="text-xl" aria-hidden="true">⚠️</span>
          <div>
            <h4 className="font-bold text-xs uppercase tracking-wider text-amber-800">
              Potential Duplicate Grievance Detected ({distanceStr} away)
            </h4>
            <p className="text-[11px] text-amber-700 mt-0.5">
              An active report in category <span className="font-bold">{topMatch.category || 'General'}</span> was already lodged nearby.
              {topMatch.duplicateGroupId && ` (Group: ${topMatch.duplicateGroupId})`}
            </p>
          </div>
        </div>

        {onDismiss && (
          <button
            type="button"
            onClick={onDismiss}
            className="text-amber-600 hover:text-amber-800 font-bold text-xs p-1 cursor-pointer"
            aria-label="Dismiss warning"
          >
            ✕
          </button>
        )}
      </div>

      {/* Duplicate Summary Card */}
      <div className="p-3 bg-white/80 rounded-lg border border-amber-200 text-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="space-y-0.5">
          <div className="flex items-center gap-2">
            <span className="font-mono font-bold text-blue-700">
              {topMatch.trackingNumber || `CF-${topMatch.id}`}
            </span>
            <Badge status={topMatch.status}>{topMatch.status}</Badge>
            <span className="text-[10px] bg-amber-100 text-amber-800 font-semibold px-1.5 py-0.5 rounded">
              {similarityPercent}% Spatial/AI Match
            </span>
          </div>
          <div className="font-medium text-slate-800 truncate max-w-md">{topMatch.title}</div>
          <div className="text-[11px] text-slate-500">
            {topMatch.addressLine || 'Same location'} • Reported on {new Date(topMatch.createdAt || Date.now()).toLocaleDateString()}
          </div>
        </div>

        <div className="flex items-center gap-2 self-end sm:self-center">
          {onViewExisting && (
            <Button
              type="button"
              size="sm"
              variant="outline"
              onClick={() => onViewExisting(topMatch)}
              className="text-xs bg-white"
            >
              View Existing Report
            </Button>
          )}
          {onProceedAnyway && (
            <Button
              type="button"
              size="sm"
              variant="ghost"
              onClick={onProceedAnyway}
              className="text-xs text-amber-800 hover:bg-amber-100/50"
            >
              Lodge Anyway
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default DuplicateWarningBanner;
