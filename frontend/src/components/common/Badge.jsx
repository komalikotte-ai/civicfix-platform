import React from 'react';

const STATUS_ICONS = {
  RESOLVED: '✓',
  CLOSED: '🔒',
  IN_PROGRESS: '⏳',
  ASSIGNED: '👤',
  ACKNOWLEDGED: '👀',
  NEW: '●',
  SUBMITTED: '●',
  ON_HOLD: '⏸',
  REJECTED: '✕',
  REOPENED: '🔄',
  ESCALATED: '⚠️',

  LOW: '↓',
  MEDIUM: '→',
  HIGH: '↑',
  URGENT: '⚡',
  CRITICAL: '⚠️',
};

const BADGE_VARIANTS = {
  NEW: 'bg-blue-50 text-blue-700 border-blue-200',
  SUBMITTED: 'bg-blue-50 text-blue-700 border-blue-200',
  ACKNOWLEDGED: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  ASSIGNED: 'bg-purple-50 text-purple-700 border-purple-200',
  IN_PROGRESS: 'bg-amber-50 text-amber-800 border-amber-200',
  ON_HOLD: 'bg-slate-100 text-slate-700 border-slate-300',
  RESOLVED: 'bg-emerald-50 text-emerald-800 border-emerald-300',
  CLOSED: 'bg-slate-100 text-slate-700 border-slate-300',
  REJECTED: 'bg-rose-50 text-rose-800 border-rose-300',
  REOPENED: 'bg-purple-50 text-purple-800 border-purple-300',
  ESCALATED: 'bg-red-50 text-red-800 border-red-300',

  LOW: 'bg-slate-100 text-slate-700 border-slate-300',
  MEDIUM: 'bg-blue-50 text-blue-700 border-blue-200',
  HIGH: 'bg-amber-100 text-amber-800 border-amber-300',
  URGENT: 'bg-orange-100 text-orange-900 border-orange-300',
  CRITICAL: 'bg-red-100 text-red-900 border-red-300',
};

export const Badge = ({
  children,
  label,
  variant,
  status,
  size = 'md',
  className = '',
}) => {
  const key = (status || variant || '').toUpperCase();
  const text = children || label || status || variant;
  const icon = STATUS_ICONS[key] || '●';
  const colorClass = BADGE_VARIANTS[key] || 'bg-slate-100 text-slate-700 border-slate-300';
  const sizeClass = size === 'lg' ? 'px-3 py-1 text-xs' : 'px-2 py-0.5 text-[11px]';

  return (
    <span
      role="status"
      aria-label={`Status: ${text}`}
      className={`inline-flex items-center gap-1.5 rounded-full font-semibold border ${colorClass} ${sizeClass} ${className}`}
    >
      <span aria-hidden="true" className="font-bold text-[10px] leading-none">
        {icon}
      </span>
      <span>{text}</span>
    </span>
  );
};

export default Badge;
