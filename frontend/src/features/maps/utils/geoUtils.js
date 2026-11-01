// Geographic & Spatial Utilities for CivicFix Map Engine

/**
 * Calculates great-circle distance between two GPS coordinates using Haversine formula.
 * @returns Distance in meters
 */
export function haversineDistance(lat1, lon1, lat2, lon2) {
  if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Infinity;
  const R = 6371e3; // Earth radius in meters
  const toRad = (x) => (x * Math.PI) / 180;
  const phi1 = toRad(lat1);
  const phi2 = toRad(lat2);
  const deltaPhi = toRad(lat2 - lat1);
  const deltaLambda = toRad(lon2 - lon1);

  const a =
    Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return Math.round(R * c);
}

/**
 * Formats distance in meters into human-readable string (e.g. "85 m" or "1.4 km").
 */
export function formatDistance(meters) {
  if (meters == null || !isFinite(meters)) return 'Unknown';
  if (meters < 1000) {
    return `${meters} m`;
  }
  return `${(meters / 1000).toFixed(1)} km`;
}

/**
 * Converts Latitude/Longitude to Web Mercator projection coordinates.
 */
export function latLngToMercator(lat, lng) {
  const x = (lng + 180) / 360;
  const sinLat = Math.sin((lat * Math.PI) / 180);
  const y = 0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI);
  return { x, y };
}

/**
 * Converts Web Mercator projection coordinates back to Latitude/Longitude.
 */
export function mercatorToLatLng(x, y) {
  const lng = x * 360 - 180;
  const n = Math.PI - 2 * Math.PI * y;
  const lat = (180 / Math.PI) * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
  return { lat, lng };
}

/**
 * Checks whether a 2D point [lat, lng] is inside a polygon [[lat, lng], ...].
 * Ray-casting algorithm.
 */
export function isPointInPolygon(point, polygon) {
  const [lat, lng] = point;
  let inside = false;
  for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
    const xi = polygon[i][1], yi = polygon[i][0];
    const xj = polygon[j][1], yj = polygon[j][0];

    const intersect =
      yi > lat !== yj > lat && lng < ((xj - xi) * (lat - yi)) / (yj - yi) + xi;
    if (intersect) inside = !inside;
  }
  return inside;
}

/**
 * Computes bounding box from an array of coordinates or objects with latitude/longitude.
 */
export function computeBoundingBox(items) {
  if (!items || items.length === 0) {
    return { minLat: 17.3, maxLat: 17.5, minLng: 78.3, maxLng: 78.6 };
  }
  let minLat = Infinity, maxLat = -Infinity;
  let minLng = Infinity, maxLng = -Infinity;

  items.forEach((item) => {
    const lat = item.latitude ?? item.lat ?? (Array.isArray(item) ? item[0] : null);
    const lng = item.longitude ?? item.lng ?? (Array.isArray(item) ? item[1] : null);
    if (lat != null && lng != null) {
      if (lat < minLat) minLat = lat;
      if (lat > maxLat) maxLat = lat;
      if (lng < minLng) minLng = lng;
      if (lng > maxLng) maxLng = lng;
    }
  });

  return {
    minLat: minLat === Infinity ? 17.38 : minLat,
    maxLat: maxLat === -Infinity ? 17.48 : maxLat,
    minLng: minLng === Infinity ? 78.38 : minLng,
    maxLng: maxLng === -Infinity ? 78.48 : maxLng,
  };
}

/**
 * Checks if a target location and category matches existing nearby complaints for duplicate warning.
 * @param {Object} target - { latitude, longitude, category }
 * @param {Array} existingComplaints - Array of complaints
 * @param {number} thresholdMeters - Proximity threshold (default 150m)
 */
export function findPotentialDuplicates(target, existingComplaints = [], thresholdMeters = 150) {
  if (!target || target.latitude == null || target.longitude == null) return [];

  return existingComplaints
    .map((c) => {
      const distance = haversineDistance(target.latitude, target.longitude, c.latitude, c.longitude);
      const isSameCategory =
        !target.category ||
        !c.category ||
        target.category.toLowerCase().trim() === c.category.toLowerCase().trim();

      // Check if backend already identified duplicate link or similarity
      const hasBackendDuplicateGroup = Boolean(c.duplicateGroupId || c.duplicate);
      const score = c.duplicateSimilarityScore || (isSameCategory ? 0.85 : 0.4);

      return {
        ...c,
        distance,
        isSameCategory,
        hasBackendDuplicateGroup,
        duplicateSimilarityScore: score,
      };
    })
    .filter((c) => c.distance <= thresholdMeters && (c.isSameCategory || c.hasBackendDuplicateGroup))
    .sort((a, b) => a.distance - b.distance);
}
