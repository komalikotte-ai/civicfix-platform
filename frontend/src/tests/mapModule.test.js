import test from 'node:test';
import assert from 'node:assert';

import {
  haversineDistance,
  formatDistance,
  latLngToMercator,
  mercatorToLatLng,
  isPointInPolygon,
  computeBoundingBox,
  findPotentialDuplicates,
} from '../features/maps/utils/geoUtils.js';

import mapProviderRegistry from '../features/maps/providers/MapProviderRegistry.js';
import { MapProviderInterface } from '../features/maps/providers/MapProviderInterface.js';
import { VectorCanvasMapProvider } from '../features/maps/providers/VectorCanvasMapProvider.js';
import geocodingService from '../services/geocodingService.js';
import { GEOLOCATION_STATUS } from '../features/maps/hooks/useGeolocation.js';

test('haversineDistance computes spatial distance accurately', () => {
  // Distance between identical coordinates is 0
  const zeroDist = haversineDistance(17.3850, 78.4867, 17.3850, 78.4867);
  assert.strictEqual(zeroDist, 0);

  // Distance between Charminar (17.3616, 78.4747) and Secunderabad Station (17.4344, 78.5015) is ~8.5 km
  const dist = haversineDistance(17.3616, 78.4747, 17.4344, 78.5015);
  assert.ok(dist > 8000 && dist < 9500);

  // Human readable distance formatter
  assert.strictEqual(formatDistance(50), '50 m');
  assert.strictEqual(formatDistance(1500), '1.5 km');
});

test('latLngToMercator and mercatorToLatLng are reversible', () => {
  const lat = 17.385044;
  const lng = 78.486671;

  const merc = latLngToMercator(lat, lng);
  assert.ok(merc.x > 0 && merc.x < 1);
  assert.ok(merc.y > 0 && merc.y < 1);

  const back = mercatorToLatLng(merc.x, merc.y);
  assert.ok(Math.abs(back.lat - lat) < 1e-5);
  assert.ok(Math.abs(back.lng - lng) < 1e-5);
});

test('isPointInPolygon identifies interior and exterior points', () => {
  const square = [
    [10, 10],
    [10, 20],
    [20, 20],
    [20, 10],
  ];

  // Point inside
  assert.strictEqual(isPointInPolygon([15, 15], square), true);

  // Point outside
  assert.strictEqual(isPointInPolygon([5, 5], square), false);
  assert.strictEqual(isPointInPolygon([25, 25], square), false);
});

test('computeBoundingBox calculates bounds correctly', () => {
  const points = [
    { latitude: 17.35, longitude: 78.40 },
    { latitude: 17.45, longitude: 78.55 },
    { latitude: 17.40, longitude: 78.30 },
  ];

  const bounds = computeBoundingBox(points);
  assert.strictEqual(bounds.minLat, 17.35);
  assert.strictEqual(bounds.maxLat, 17.45);
  assert.strictEqual(bounds.minLng, 78.30);
  assert.strictEqual(bounds.maxLng, 78.55);
});

test('findPotentialDuplicates identifies close complaints of same category', () => {
  const target = { latitude: 17.3850, longitude: 78.4867, category: 'POTHOLE' };

  const existing = [
    {
      id: 1,
      latitude: 17.3852,
      longitude: 78.4869, // ~30m away
      category: 'POTHOLE',
      status: 'IN_PROGRESS',
    },
    {
      id: 2,
      latitude: 17.3851,
      longitude: 78.4868, // ~15m away
      category: 'STREETLIGHT', // different category, no duplicate group
      status: 'NEW',
    },
    {
      id: 3,
      latitude: 17.4500,
      longitude: 78.5500, // > 8km away
      category: 'POTHOLE',
      status: 'NEW',
    },
  ];

  const duplicates = findPotentialDuplicates(target, existing, 100);
  // Only id 1 is within 100m AND matching category
  assert.strictEqual(duplicates.length, 1);
  assert.strictEqual(duplicates[0].id, 1);
  assert.ok(duplicates[0].distance < 100);
});

test('mapProviderRegistry provides clean abstraction and swappability', () => {
  // Built in provider is vector-canvas
  const defaultProvider = mapProviderRegistry.get();
  assert.ok(defaultProvider instanceof MapProviderInterface);
  assert.ok(defaultProvider instanceof VectorCanvasMapProvider);

  // Custom provider registration
  class MockLeafletProvider extends MapProviderInterface {
    constructor() {
      super();
      this.type = 'leaflet-mock';
    }
  }

  mapProviderRegistry.register('mock-leaflet', MockLeafletProvider);
  assert.ok(mapProviderRegistry.getAvailableProviders().includes('mock-leaflet'));

  const instantiated = mapProviderRegistry.get('mock-leaflet');
  assert.strictEqual(instantiated.type, 'leaflet-mock');
});

test('geocodingService reverseGeocode resolves nearest address and ward', async () => {
  const result = await geocodingService.reverseGeocode(17.385044, 78.486671);
  assert.ok(result);
  assert.strictEqual(result.latitude, 17.385044);
  assert.strictEqual(result.longitude, 78.486671);
  assert.ok(result.wardNumber);
  assert.ok(result.addressLine);
});

test('GEOLOCATION_STATUS constants are defined', () => {
  assert.strictEqual(GEOLOCATION_STATUS.PERMISSION_DENIED, 'PERMISSION_DENIED');
  assert.strictEqual(GEOLOCATION_STATUS.POSITION_UNAVAILABLE, 'POSITION_UNAVAILABLE');
  assert.strictEqual(GEOLOCATION_STATUS.TIMEOUT, 'TIMEOUT');
  assert.strictEqual(GEOLOCATION_STATUS.SUCCESS, 'SUCCESS');
});
