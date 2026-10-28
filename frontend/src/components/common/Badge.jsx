import React from 'react';

const BADGE_VARIANTS = {
  SUBMITTED: 'bg-blue-50 text-blue-700 border-blue-200',
  ACKNOWLEDGED: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  ASSIGNED: 'bg-purple-50 text-purple-700 border-purple-200',
  IN_PROGRESS: 'bg-amber-50 text-amber-800 border-amber-200',
  RESOLVED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  CLOSED: 'bg-gray-100 text-gray-700 border-gray-300',
  REJECTED: 'bg-red-50 text-red-700 border-red-200',
  ESCALATED: 'bg-rose-100 text-rose-800 border-rose-300',

  LOW: 'bg-slate-100 text-slate-700 border-slate-300',
  MEDIUM: 'bg-blue-50 text-blue-700 border-blue-200',
  HIGH: 'bg-amber-100 text-amber-800 border-amber-300',
  CRITICAL: 'bg-red-100 text-red-800 border-red-300',
};

export const Badge = ({ label, variant, className = '' }) => {
  const resolvedVariant = BADGE_VARIANTS[variant] || 'bg-gray-100 text-gray-700 border-gray-300';

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${resolvedVariant} ${className}`}
    >
      {label || variant}
    </span>
  );
};

export default Badge;
