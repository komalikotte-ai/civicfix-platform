import { useEffect, useRef, useState } from 'react';
import mapProviderRegistry from '../providers/MapProviderRegistry.js';

export function useMapInstance(containerRef, options = {}) {
  const {
    provider = 'vector-canvas',
    center = [17.385044, 78.486671],
    zoom = 13,
    onClick,
    onMarkerClick,
    onBoundaryClick,
    onMoveEnd,
  } = options;

  const mapInstanceRef = useRef(null);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    if (!containerRef.current) return;

    // Instantiate provider from registry
    const map = mapProviderRegistry.get(provider);
    map.init(containerRef.current, { center, zoom });
    mapInstanceRef.current = map;
    setIsReady(true);

    // Wire callbacks
    if (onClick) map.on('click', onClick);
    if (onMarkerClick) map.on('markerClick', onMarkerClick);
    if (onBoundaryClick) map.on('boundaryClick', onBoundaryClick);
    if (onMoveEnd) map.on('moveEnd', onMoveEnd);

    const handleResize = () => map.resize();
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      map.destroy();
      mapInstanceRef.current = null;
      setIsReady(false);
    };
  }, [containerRef, provider]);

  return {
    map: mapInstanceRef.current,
    isReady,
  };
}

export default useMapInstance;
