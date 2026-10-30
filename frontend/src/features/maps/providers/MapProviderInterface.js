/**
 * MapProviderInterface (Contract)
 *
 * All map providers (e.g. VectorCanvas, Leaflet, OpenLayers, Google Maps, Mapbox)
 * must implement these standard methods to ensure zero vendor lock-in.
 */
export class MapProviderInterface {
  /**
   * Initializes the map engine inside the HTML container.
   * @param {HTMLElement} container
   * @param {Object} options - { center: [lat, lng], zoom: number, minZoom, maxZoom }
   */
  init(container, options = {}) {
    throw new Error('init() must be implemented by map provider');
  }

  /**
   * Destroys and cleans up resources, timers, and listeners.
   */
  destroy() {
    throw new Error('destroy() must be implemented by map provider');
  }

  /**
   * Sets map center and zoom.
   * @param {number} lat
   * @param {number} lng
   * @param {number} [zoom]
   */
  setCenter(lat, lng, zoom) {
    throw new Error('setCenter() must be implemented by map provider');
  }

  /**
   * Returns current map center as [lat, lng].
   * @returns {[number, number]}
   */
  getCenter() {
    throw new Error('getCenter() must be implemented by map provider');
  }

  /**
   * Fits view to bounding box.
   * @param {Object} bounds - { minLat, maxLat, minLng, maxLng }
   */
  fitBounds(bounds) {
    throw new Error('fitBounds() must be implemented by map provider');
  }

  /**
   * Adds or updates a marker on the map.
   * @param {Object} marker - { id, lat, lng, title, category, priority, status, icon, data, onClick }
   */
  addMarker(marker) {
    throw new Error('addMarker() must be implemented by map provider');
  }

  /**
   * Removes a marker by ID.
   * @param {string|number} markerId
   */
  removeMarker(markerId) {
    throw new Error('removeMarker() must be implemented by map provider');
  }

  /**
   * Clears all markers.
   */
  clearMarkers() {
    throw new Error('clearMarkers() must be implemented by map provider');
  }

  /**
   * Renders geographic heatmap layer.
   * @param {Array<Object>} points - Array of { lat, lng, weight }
   * @param {Object} [options]
   */
  renderHeatmap(points, options = {}) {
    throw new Error('renderHeatmap() must be implemented by map provider');
  }

  /**
   * Clears heatmap layer.
   */
  clearHeatmap() {
    throw new Error('clearHeatmap() must be implemented by map provider');
  }

  /**
   * Renders municipal ward boundaries from GeoJSON or polygon arrays.
   * @param {Array<Object>} wards - Array of { id, name, wardNumber, polygon, geoJson, color }
   * @param {Object} [options]
   */
  renderBoundaries(wards, options = {}) {
    throw new Error('renderBoundaries() must be implemented by map provider');
  }

  /**
   * Clears boundary layer.
   */
  clearBoundaries() {
    throw new Error('clearBoundaries() must be implemented by map provider');
  }

  /**
   * Adds event listener ('click', 'markerClick', 'moveEnd', 'zoomEnd').
   */
  on(event, handler) {
    throw new Error('on() must be implemented by map provider');
  }

  /**
   * Removes event listener.
   */
  off(event, handler) {
    throw new Error('off() must be implemented by map provider');
  }

  /**
   * Triggers resize/redraw recalculation when container dimension changes.
   */
  resize() {
    throw new Error('resize() must be implemented by map provider');
  }
}

export default MapProviderInterface;
