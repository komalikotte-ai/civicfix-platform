// Saved Locations Service
// Provides persistent storage for frequently used citizen reporting locations

const STORAGE_PREFIX = 'civicfix_saved_locations_';

const DEFAULT_INITIAL_LOCATIONS = [
  {
    id: 'loc-default-home',
    label: 'Home',
    addressLine: 'Plot 42, Green Avenue, Sector 3',
    landmark: 'Opposite Community Water Tank',
    wardId: 1,
    wardName: 'Ward #1 - Central',
    latitude: 17.385044,
    longitude: 78.486671,
    isDefault: true,
  },
  {
    id: 'loc-default-office',
    label: 'Office / Workplace',
    addressLine: 'Civic Tech Park, Building B, 4th Floor',
    landmark: 'Near Metro Gate 2',
    wardId: 2,
    wardName: 'Ward #2 - IT Corridor',
    latitude: 17.440081,
    longitude: 78.348915,
    isDefault: false,
  },
];

export const savedLocationService = {
  getSavedLocations: (userId = 'current') => {
    try {
      const key = `${STORAGE_PREFIX}${userId}`;
      const raw = localStorage.getItem(key);
      if (!raw) {
        // Initialize with standard defaults
        localStorage.setItem(key, JSON.stringify(DEFAULT_INITIAL_LOCATIONS));
        return DEFAULT_INITIAL_LOCATIONS;
      }
      return JSON.parse(raw);
    } catch {
      return DEFAULT_INITIAL_LOCATIONS;
    }
  },

  saveLocation: (userId = 'current', location) => {
    const list = savedLocationService.getSavedLocations(userId);
    const existingIndex = list.findIndex((l) => l.id === location.id);
    const isFirst = list.length === 0;

    const newLoc = {
      ...location,
      id: location.id || `loc-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
      isDefault: location.isDefault ?? isFirst,
    };

    let updated;
    if (newLoc.isDefault) {
      // Clear other defaults
      updated = list.map((l) => ({ ...l, isDefault: false }));
    } else {
      updated = [...list];
    }

    if (existingIndex >= 0) {
      updated[existingIndex] = newLoc;
    } else {
      updated.push(newLoc);
    }

    const key = `${STORAGE_PREFIX}${userId}`;
    localStorage.setItem(key, JSON.stringify(updated));
    return newLoc;
  },

  deleteLocation: (userId = 'current', locationId) => {
    const list = savedLocationService.getSavedLocations(userId);
    const updated = list.filter((l) => l.id !== locationId);
    if (updated.length > 0 && !updated.some((l) => l.isDefault)) {
      updated[0].isDefault = true;
    }
    const key = `${STORAGE_PREFIX}${userId}`;
    localStorage.setItem(key, JSON.stringify(updated));
    return updated;
  },

  setDefaultLocation: (userId = 'current', locationId) => {
    const list = savedLocationService.getSavedLocations(userId);
    const updated = list.map((l) => ({
      ...l,
      isDefault: l.id === locationId,
    }));
    const key = `${STORAGE_PREFIX}${userId}`;
    localStorage.setItem(key, JSON.stringify(updated));
    return updated;
  },
};

export default savedLocationService;
