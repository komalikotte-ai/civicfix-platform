import organizationService from './organizationService.js';
import { haversineDistance } from '../features/maps/utils/geoUtils.js';

// Civic municipal landmarks and intersections for local spatial resolution
const MUNICIPAL_LANDMARKS = [
  { name: 'City Central Bus Station', landmark: 'Platform Gate 1', lat: 17.385044, lng: 78.486671, wardNumber: '1' },
  { name: 'Tech Park IT Corridor', landmark: 'Cyber Tower Circle', lat: 17.440081, lng: 78.348915, wardNumber: '2' },
  { name: 'Gandhi Memorial General Hospital', landmark: 'Emergency Wing Gate', lat: 17.412345, lng: 78.475621, wardNumber: '3' },
  { name: 'Municipal Public Market', landmark: 'Clock Tower Gate', lat: 17.391204, lng: 78.469103, wardNumber: '1' },
  { name: 'Lakeview Residential Enclave', landmark: 'Community Water Tank', lat: 17.425678, lng: 78.451234, wardNumber: '4' },
  { name: 'Industrial Estate Phase 2', landmark: 'Substation Road Junction', lat: 17.468901, lng: 78.390123, wardNumber: '5' },
];

export const geocodingService = {
  /**
   * Searches locations and landmarks by address query.
   */
  searchAddress: async (query) => {
    if (!query || query.trim().length < 2) return [];
    const q = query.toLowerCase().trim();

    try {
      const wards = await organizationService.getWards().catch(() => []);
      const results = [];

      // 1. Search matching wards
      (Array.isArray(wards) ? wards : []).forEach((w) => {
        const nameMatch = w.name?.toLowerCase().includes(q);
        const numMatch = String(w.wardNumber).includes(q);
        const zoneMatch = w.zoneName?.toLowerCase().includes(q);

        if (nameMatch || numMatch || zoneMatch) {
          results.push({
            id: `ward-${w.id}`,
            type: 'ward',
            title: `Ward #${w.wardNumber} - ${w.name}`,
            subtitle: w.zoneName ? `Zone: ${w.zoneName}` : 'Municipal Ward',
            latitude: w.centerLatitude || 17.385044,
            longitude: w.centerLongitude || 78.486671,
            wardId: w.id,
            wardNumber: w.wardNumber,
            wardName: w.name,
            addressLine: `${w.name}, Ward #${w.wardNumber}`,
          });
        }
      });

      // 2. Search matching municipal landmarks
      MUNICIPAL_LANDMARKS.forEach((lm, idx) => {
        if (lm.name.toLowerCase().includes(q) || lm.landmark.toLowerCase().includes(q)) {
          results.push({
            id: `landmark-${idx}`,
            type: 'landmark',
            title: lm.name,
            subtitle: `${lm.landmark} (Near Ward #${lm.wardNumber})`,
            latitude: lm.lat,
            longitude: lm.lng,
            landmark: lm.landmark,
            wardNumber: lm.wardNumber,
            addressLine: `${lm.name}, ${lm.landmark}`,
          });
        }
      });

      return results;
    } catch {
      return [];
    }
  },

  /**
   * Reverse geocodes a latitude/longitude pair into nearest street address, ward, and landmark.
   */
  reverseGeocode: async (lat, lng) => {
    if (lat == null || lng == null) return null;

    try {
      const wards = await organizationService.getWards().catch(() => []);
      const wardList = Array.isArray(wards) ? wards : [];

      // Find nearest ward by Euclidean/Haversine distance to center
      let nearestWard = null;
      let minDistance = Infinity;

      wardList.forEach((w) => {
        const cLat = w.centerLatitude || 17.385044;
        const cLng = w.centerLongitude || 78.486671;
        const dist = haversineDistance(lat, lng, cLat, cLng);
        if (dist < minDistance) {
          minDistance = dist;
          nearestWard = w;
        }
      });

      // Find nearest landmark
      let nearestLandmark = MUNICIPAL_LANDMARKS[0];
      let minLmDist = Infinity;
      MUNICIPAL_LANDMARKS.forEach((lm) => {
        const dist = haversineDistance(lat, lng, lm.lat, lm.lng);
        if (dist < minLmDist) {
          minLmDist = dist;
          nearestLandmark = lm;
        }
      });

      const wardName = nearestWard ? nearestWard.name : 'Central Ward';
      const wardNumber = nearestWard ? nearestWard.wardNumber : '1';
      const wardId = nearestWard ? nearestWard.id : 1;

      return {
        latitude: lat,
        longitude: lng,
        wardId,
        wardNumber,
        wardName: `Ward #${wardNumber} - ${wardName}`,
        addressLine: `Road near ${nearestLandmark.name}, Sector ${wardNumber}`,
        landmark: nearestLandmark.landmark,
        approxDistanceMeters: Math.round(minLmDist),
      };
    } catch {
      return {
        latitude: lat,
        longitude: lng,
        wardId: 1,
        wardNumber: '1',
        wardName: 'Ward #1 - Central',
        addressLine: 'Municipal Zone Center',
        landmark: 'Main Highway',
      };
    }
  },
};

export default geocodingService;
