import React, { useState, useEffect } from 'react';
import Modal from '../../../components/common/Modal';
import Button from '../../../components/common/Button';
import notificationPreferenceService from '../../../services/notificationPreferenceService';

export const NotificationPreferencesModal = ({ isOpen, onClose, userId = 'current' }) => {
  const [prefs, setPrefs] = useState({
    emailEnabled: true,
    smsEnabled: true,
    pushEnabled: false,
    statusUpdates: true,
    officerAssigned: true,
    slaWarnings: true,
    resolutionFeedback: true,
    dailyDigest: false,
    quietHoursEnabled: true,
    quietHoursStart: '22:00',
    quietHoursEnd: '07:00',
  });
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    if (isOpen) {
      const data = notificationPreferenceService.getPreferences(userId);
      setPrefs(data);
      setSavedSuccess(false);
    }
  }, [isOpen, userId]);

  const handleChange = (key, val) => {
    setPrefs((prev) => ({ ...prev, [key]: val }));
    setSavedSuccess(false);
  };

  const handleSave = () => {
    notificationPreferenceService.updatePreferences(userId, prefs);
    setSavedSuccess(true);
    setTimeout(() => {
      onClose();
    }, 900);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Citizen Notification Preferences" size="md">
      <div className="flex flex-col gap-5 text-xs">
        <p className="text-slate-500">
          Configure how and when the municipal administration communicates grievance updates and SLA alerts to you.
        </p>

        {savedSuccess && (
          <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs font-semibold">
            ✓ Preferences saved successfully!
          </div>
        )}

        {/* Communication Channels */}
        <div className="border border-slate-200 rounded-xl p-4 bg-slate-50/50 space-y-3">
          <h4 className="font-bold text-slate-800 uppercase tracking-wider text-[11px]">
            Delivery Channels
          </h4>

          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <span className="font-semibold text-slate-900 block">Email Notifications</span>
              <span className="text-slate-500 text-[11px]">Detailed status logs and resolution reports</span>
            </div>
            <input
              type="checkbox"
              checked={prefs.emailEnabled}
              onChange={(e) => handleChange('emailEnabled', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>

          <label className="flex items-center justify-between cursor-pointer border-t border-slate-200/60 pt-2.5">
            <div>
              <span className="font-semibold text-slate-900 block">SMS Alerts</span>
              <span className="text-slate-500 text-[11px]">Instant text alerts for field dispatch and resolution</span>
            </div>
            <input
              type="checkbox"
              checked={prefs.smsEnabled}
              onChange={(e) => handleChange('smsEnabled', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>

          <label className="flex items-center justify-between cursor-pointer border-t border-slate-200/60 pt-2.5">
            <div>
              <span className="font-semibold text-slate-900 block">Browser Push Notifications</span>
              <span className="text-slate-500 text-[11px]">Real-time desktop and mobile push banners</span>
            </div>
            <input
              type="checkbox"
              checked={prefs.pushEnabled}
              onChange={(e) => handleChange('pushEnabled', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>
        </div>

        {/* Alert Triggers */}
        <div className="border border-slate-200 rounded-xl p-4 bg-slate-50/50 space-y-3">
          <h4 className="font-bold text-slate-800 uppercase tracking-wider text-[11px]">
            Notification Events
          </h4>

          <label className="flex items-center justify-between cursor-pointer">
            <span className="text-slate-800">Status change events (New, In Progress, Resolved)</span>
            <input
              type="checkbox"
              checked={prefs.statusUpdates}
              onChange={(e) => handleChange('statusUpdates', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>

          <label className="flex items-center justify-between cursor-pointer border-t border-slate-200/60 pt-2.5">
            <span className="text-slate-800">Field officer and team assignments</span>
            <input
              type="checkbox"
              checked={prefs.officerAssigned}
              onChange={(e) => handleChange('officerAssigned', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>

          <label className="flex items-center justify-between cursor-pointer border-t border-slate-200/60 pt-2.5">
            <span className="text-slate-800">SLA warning and escalation alerts</span>
            <input
              type="checkbox"
              checked={prefs.slaWarnings}
              onChange={(e) => handleChange('slaWarnings', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>

          <label className="flex items-center justify-between cursor-pointer border-t border-slate-200/60 pt-2.5">
            <span className="text-slate-800">Citizen satisfaction and feedback invitations</span>
            <input
              type="checkbox"
              checked={prefs.resolutionFeedback}
              onChange={(e) => handleChange('resolutionFeedback', e.target.checked)}
              className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500"
            />
          </label>
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="button" variant="primary" onClick={handleSave}>
            Save Preferences
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default NotificationPreferencesModal;
