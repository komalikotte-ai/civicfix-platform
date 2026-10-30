import { MapProviderInterface } from './MapProviderInterface.js';
import { latLngToMercator, mercatorToLatLng } from '../utils/geoUtils.js';

const SEVERITY_COLORS = {
  CRITICAL: '#ef4444',
  HIGH: '#f97316',
  MEDIUM: '#f59e0b',
  LOW: '#3b82f6',
};

const CATEGORY_ICONS = {
  POTHOLE: '🕳️',
  GARBAGE: '🗑️',
  STREETLIGHT: '💡',
  WATER_LEAKAGE: '💧',
  SEWAGE: '⚠️',
  ENCROACHMENT: '🚧',
  HEALTH: '🏥',
  OTHER: '📍',
};

export class VectorCanvasMapProvider extends MapProviderInterface {
  constructor() {
    super();
    this.container = null;
    this.canvas = null;
    this.ctx = null;
    this.center = [17.385044, 78.486671];
    this.zoom = 13;
    this.minZoom = 10;
    this.maxZoom = 18;

    this.markers = new Map();
    this.boundaries = [];
    this.heatmapPoints = [];
    this.showHeatmap = false;

    this.isDragging = false;
    this.dragStart = { x: 0, y: 0 };
    this.lastCenter = [...this.center];

    this.listeners = new Map();
    this.animationFrameId = null;
    this.pulsePhase = 0;
  }

  init(container, options = {}) {
    this.container = container;
    this.center = options.center || [17.385044, 78.486671];
    this.zoom = options.zoom || 13;
    this.minZoom = options.minZoom || 10;
    this.maxZoom = options.maxZoom || 18;

    // Create Canvas Element
    this.canvas = document.createElement('canvas');
    this.canvas.style.width = '100%';
    this.canvas.style.height = '100%';
    this.canvas.style.display = 'block';
    this.canvas.style.cursor = 'grab';
    this.canvas.setAttribute('tabindex', '0');
    this.canvas.setAttribute('aria-label', 'Interactive geographic map');

    this.container.innerHTML = '';
    this.container.appendChild(this.canvas);
    this.ctx = this.canvas.getContext('2d');

    this.resize();
    this.bindEvents();
    this.startRenderLoop();
  }

  resize() {
    if (!this.canvas || !this.container) return;
    const rect = this.container.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    this.canvas.width = (rect.width || 600) * dpr;
    this.canvas.height = (rect.height || 400) * dpr;
    this.width = rect.width || 600;
    this.height = rect.height || 400;
    if (this.ctx) {
      this.ctx.scale(dpr, dpr);
    }
  }

  destroy() {
    this.unbindEvents();
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
    this.markers.clear();
    this.boundaries = [];
    this.heatmapPoints = [];
    this.listeners.clear();
    if (this.container) {
      this.container.innerHTML = '';
    }
  }

  setCenter(lat, lng, zoom) {
    this.center = [lat, lng];
    if (zoom != null) {
      this.zoom = Math.max(this.minZoom, Math.min(this.maxZoom, zoom));
    }
    this.emit('moveEnd', { center: this.center, zoom: this.zoom });
  }

  getCenter() {
    return [...this.center];
  }

  getZoom() {
    return this.zoom;
  }

  fitBounds(bounds) {
    if (!bounds) return;
    const { minLat, maxLat, minLng, maxLng } = bounds;
    this.center = [(minLat + maxLat) / 2, (minLng + maxLng) / 2];

    const latSpan = Math.max(0.005, maxLat - minLat);
    const lngSpan = Math.max(0.005, maxLng - minLng);
    const maxSpan = Math.max(latSpan, lngSpan);

    if (maxSpan < 0.02) this.zoom = 15;
    else if (maxSpan < 0.05) this.zoom = 14;
    else if (maxSpan < 0.12) this.zoom = 13;
    else if (maxSpan < 0.25) this.zoom = 12;
    else this.zoom = 11;

    this.emit('moveEnd', { center: this.center, zoom: this.zoom });
  }

  addMarker(marker) {
    this.markers.set(marker.id, marker);
  }

  removeMarker(markerId) {
    this.markers.delete(markerId);
  }

  clearMarkers() {
    this.markers.clear();
  }

  renderHeatmap(points, options = {}) {
    this.heatmapPoints = points || [];
    this.showHeatmap = true;
  }

  clearHeatmap() {
    this.heatmapPoints = [];
    this.showHeatmap = false;
  }

  renderBoundaries(wards) {
    this.boundaries = (wards || []).map((w, i) => {
      let polygon = w.polygon;
      if (!polygon && w.boundaryGeoJson) {
        try {
          const parsed = JSON.parse(w.boundaryGeoJson);
          if (parsed.type === 'Polygon') {
            polygon = parsed.coordinates[0].map(([lng, lat]) => [lat, lng]);
          } else if (parsed.type === 'MultiPolygon') {
            polygon = parsed.coordinates[0][0].map(([lng, lat]) => [lat, lng]);
          }
        } catch {
          polygon = null;
        }
      }

      // Generate geometric ward boundary if no geojson is available
      if (!polygon && (w.centerLatitude || w.latitude)) {
        const cLat = w.centerLatitude || w.latitude;
        const cLng = w.centerLongitude || w.longitude;
        const rad = 0.015 + (i % 4) * 0.004;
        polygon = [
          [cLat - rad, cLng - rad],
          [cLat - rad, cLng + rad],
          [cLat + rad, cLng + rad],
          [cLat + rad, cLng - rad],
        ];
      }

      return {
        id: w.id,
        name: w.name,
        wardNumber: w.wardNumber || w.id,
        polygon: polygon || [],
        color: w.color || `hsl(${(i * 55) % 360}, 65%, 45%)`,
        centerLat: w.centerLatitude || w.latitude,
        centerLng: w.centerLongitude || w.longitude,
      };
    });
  }

  clearBoundaries() {
    this.boundaries = [];
  }

  on(event, handler) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event).add(handler);
  }

  off(event, handler) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).delete(handler);
    }
  }

  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach((fn) => fn(data));
    }
  }

  // Coordinate Projection: lat/lng -> canvas (px)
  latLngToPixel(lat, lng) {
    const scale = Math.pow(2, this.zoom) * 256;
    const centerMerc = latLngToMercator(this.center[0], this.center[1]);
    const pointMerc = latLngToMercator(lat, lng);

    const x = (pointMerc.x - centerMerc.x) * scale + this.width / 2;
    const y = (pointMerc.y - centerMerc.y) * scale + this.height / 2;
    return { x, y };
  }

  // Coordinate Unprojection: canvas (px) -> lat/lng
  pixelToLatLng(px, py) {
    const scale = Math.pow(2, this.zoom) * 256;
    const centerMerc = latLngToMercator(this.center[0], this.center[1]);

    const x = (px - this.width / 2) / scale + centerMerc.x;
    const y = (py - this.height / 2) / scale + centerMerc.y;
    return mercatorToLatLng(x, y);
  }

  bindEvents() {
    this.onMouseDown = (e) => {
      this.isDragging = true;
      this.dragStart = { x: e.clientX, y: e.clientY };
      this.lastCenter = [...this.center];
      this.canvas.style.cursor = 'grabbing';
    };

    this.onMouseMove = (e) => {
      if (!this.isDragging) return;
      const dx = e.clientX - this.dragStart.x;
      const dy = e.clientY - this.dragStart.y;

      const scale = Math.pow(2, this.zoom) * 256;
      const centerMerc = latLngToMercator(this.lastCenter[0], this.lastCenter[1]);

      const newMercX = centerMerc.x - dx / scale;
      const newMercY = centerMerc.y - dy / scale;
      const newCoord = mercatorToLatLng(newMercX, newMercY);

      this.center = [newCoord.lat, newCoord.lng];
    };

    this.onMouseUp = (e) => {
      if (this.isDragging) {
        this.isDragging = false;
        this.canvas.style.cursor = 'grab';
        this.emit('moveEnd', { center: this.center, zoom: this.zoom });
      }
    };

    this.onClick = (e) => {
      const rect = this.canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      // Check Marker Hit First
      for (const [id, marker] of this.markers) {
        const p = this.latLngToPixel(marker.lat || marker.latitude, marker.lng || marker.longitude);
        const dist = Math.hypot(x - p.x, y - p.y);
        if (dist <= 18) {
          this.emit('markerClick', marker);
          return;
        }
      }

      // Check Boundary Hit
      for (const b of this.boundaries) {
        if (b.centerLat && b.centerLng) {
          const bp = this.latLngToPixel(b.centerLat, b.centerLng);
          if (Math.hypot(x - bp.x, y - bp.y) < 25) {
            this.emit('boundaryClick', b);
            return;
          }
        }
      }

      // General map click
      const coord = this.pixelToLatLng(x, y);
      this.emit('click', { lat: coord.lat, lng: coord.lng });
    };

    this.onWheel = (e) => {
      e.preventDefault();
      const delta = e.deltaY < 0 ? 0.3 : -0.3;
      const newZoom = Math.max(this.minZoom, Math.min(this.maxZoom, this.zoom + delta));
      if (newZoom !== this.zoom) {
        this.zoom = newZoom;
        this.emit('zoomEnd', { zoom: this.zoom });
      }
    };

    this.canvas.addEventListener('mousedown', this.onMouseDown);
    window.addEventListener('mousemove', this.onMouseMove);
    window.addEventListener('mouseup', this.onMouseUp);
    this.canvas.addEventListener('click', this.onClick);
    this.canvas.addEventListener('wheel', this.onWheel, { passive: false });
  }

  unbindEvents() {
    if (this.canvas) {
      this.canvas.removeEventListener('mousedown', this.onMouseDown);
      this.canvas.removeEventListener('click', this.onClick);
      this.canvas.removeEventListener('wheel', this.onWheel);
    }
    window.removeEventListener('mousemove', this.onMouseMove);
    window.removeEventListener('mouseup', this.onMouseUp);
  }

  startRenderLoop() {
    const render = () => {
      this.pulsePhase = (this.pulsePhase + 0.04) % (Math.PI * 2);
      this.draw();
      this.animationFrameId = requestAnimationFrame(render);
    };
    this.animationFrameId = requestAnimationFrame(render);
  }

  draw() {
    if (!this.ctx || !this.width || !this.height) return;
    const ctx = this.ctx;

    // 1. Clear background
    ctx.fillStyle = '#f8fafc';
    ctx.fillRect(0, 0, this.width, this.height);

    // 2. Draw Geographic Grid Lines
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 1;
    const step = 60;
    for (let x = 0; x < this.width; x += step) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, this.height);
      ctx.stroke();
    }
    for (let y = 0; y < this.height; y += step) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(this.width, y);
      ctx.stroke();
    }

    // 3. Draw Municipal Ward Boundaries
    this.boundaries.forEach((b) => {
      if (!b.polygon || b.polygon.length < 3) return;

      ctx.beginPath();
      b.polygon.forEach(([lat, lng], idx) => {
        const p = this.latLngToPixel(lat, lng);
        if (idx === 0) ctx.moveTo(p.x, p.y);
        else ctx.lineTo(p.x, p.y);
      });
      ctx.closePath();

      ctx.fillStyle = `${b.color}15`; // 15% opacity fill
      ctx.fill();
      ctx.strokeStyle = b.color;
      ctx.lineWidth = 2;
      ctx.stroke();

      // Ward Center Label
      if (b.centerLat && b.centerLng) {
        const cp = this.latLngToPixel(b.centerLat, b.centerLng);
        ctx.fillStyle = '#1e293b';
        ctx.font = 'bold 11px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(`Ward #${b.wardNumber}`, cp.x, cp.y);
        ctx.font = '9px sans-serif';
        ctx.fillStyle = '#64748b';
        ctx.fillText(b.name || '', cp.x, cp.y + 12);
      }
    });

    // 4. Draw Heatmap Layer (if enabled)
    if (this.showHeatmap && this.heatmapPoints.length > 0) {
      this.heatmapPoints.forEach((pt) => {
        const p = this.latLngToPixel(pt.lat, pt.lng);
        const radius = (pt.radius || 35) * (this.zoom / 13);
        const intensity = Math.min(1, pt.weight || 0.6);

        const gradient = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, radius);
        gradient.addColorStop(0, `rgba(239, 68, 68, ${0.55 * intensity})`);
        gradient.addColorStop(0.4, `rgba(249, 115, 22, ${0.35 * intensity})`);
        gradient.addColorStop(0.8, `rgba(234, 179, 8, ${0.15 * intensity})`);
        gradient.addColorStop(1, 'rgba(234, 179, 8, 0)');

        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(p.x, p.y, radius, 0, Math.PI * 2);
        ctx.fill();
      });
    }

    // 5. Draw Complaint Markers
    for (const [id, m] of this.markers) {
      const lat = m.lat || m.latitude;
      const lng = m.lng || m.longitude;
      if (lat == null || lng == null) continue;

      const p = this.latLngToPixel(lat, lng);
      const color = SEVERITY_COLORS[m.severity] || '#3b82f6';
      const icon = CATEGORY_ICONS[m.category] || '📍';
      const isSelected = m.isSelected;

      // Pulse ring for critical or selected
      if (m.severity === 'CRITICAL' || isSelected) {
        const pulseSize = 16 + Math.sin(this.pulsePhase) * 6;
        ctx.beginPath();
        ctx.arc(p.x, p.y, pulseSize, 0, Math.PI * 2);
        ctx.strokeStyle = `${color}66`;
        ctx.lineWidth = 2.5;
        ctx.stroke();
      }

      // Marker Shadow
      ctx.beginPath();
      ctx.arc(p.x, p.y + 2, 10, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(0,0,0,0.18)';
      ctx.fill();

      // Marker Body
      ctx.beginPath();
      ctx.arc(p.x, p.y, 11, 0, Math.PI * 2);
      ctx.fillStyle = color;
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2;
      ctx.stroke();

      // Marker Icon / Text
      ctx.fillStyle = '#ffffff';
      ctx.font = '10px sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(icon, p.x, p.y);

      // Marker Title on Hover / Selection
      if (isSelected) {
        ctx.font = 'bold 11px sans-serif';
        const text = m.title || m.trackingNumber || `ID #${id}`;
        const metrics = ctx.measureText(text);
        const bgW = metrics.width + 12;

        ctx.fillStyle = '#0f172a';
        ctx.roundRect ? ctx.roundRect(p.x - bgW / 2, p.y - 32, bgW, 20, 4) : ctx.fillRect(p.x - bgW / 2, p.y - 32, bgW, 20);
        ctx.fill();

        ctx.fillStyle = '#ffffff';
        ctx.fillText(text, p.x, p.y - 22);
      }
    }
  }
}

export default VectorCanvasMapProvider;
