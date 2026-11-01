// Notification Preferences Service
// Manages citizen communication channels and event triggers

const STORAGE_PREFIX = 'civicfix_notification_prefs_';

const DEFAULT_PREFERENCES = {
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
};

export const notificationPreferenceService = {
  getPreferences: (userId = 'current') => {
    try {
      const key = `${STORAGE_PREFIX}${userId}`;
      const raw = localStorage.getItem(key);
      if (!raw) {
        localStorage.setItem(key, JSON.stringify(DEFAULT_PREFERENCES));
        return DEFAULT_PREFERENCES;
      }
      return { ...DEFAULT_PREFERENCES, ...JSON.parse(raw) };
    } catch {
      return DEFAULT_PREFERENCES;
    }
  },

  updatePreferences: (userId = 'current', newPrefs) => {
    const current = notificationPreferenceService.getPreferences(userId);
    const updated = { ...current, ...newPrefs };
    const key = `${STORAGE_PREFIX}${userId}`;
    localStorage.setItem(key, JSON.stringify(updated));
    return updated;
  },
};

export default notificationPreferenceService;
