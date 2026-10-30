import { useState, useCallback } from 'react';

export const GEOLOCATION_STATUS = {
  IDLE: 'IDLE',
  LOADING: 'LOADING',
  SUCCESS: 'SUCCESS',
  PERMISSION_DENIED: 'PERMISSION_DENIED',
  POSITION_UNAVAILABLE: 'POSITION_UNAVAILABLE',
  TIMEOUT: 'TIMEOUT',
  UNSUPPORTED: 'UNSUPPORTED',
};

export function useGeolocation(defaultCoords = { latitude: 17.385044, longitude: 78.486671 }) {
  const [coords, setCoords] = useState(defaultCoords);
  const [accuracy, setAccuracy] = useState(null);
  const [status, setStatus] = useState(GEOLOCATION_STATUS.IDLE);
  const [errorMessage, setErrorMessage] = useState('');

  const getCurrentLocation = useCallback((options = { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 }) => {
    if (!navigator.geolocation) {
      setStatus(GEOLOCATION_STATUS.UNSUPPORTED);
      setErrorMessage('Geolocation is not supported by your browser.');
      return;
    }

    setStatus(GEOLOCATION_STATUS.LOADING);
    setErrorMessage('');

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude, accuracy: acc } = position.coords;
        setCoords({ latitude, longitude });
        setAccuracy(acc);
        setStatus(GEOLOCATION_STATUS.SUCCESS);
      },
      (error) => {
        let mappedStatus = GEOLOCATION_STATUS.POSITION_UNAVAILABLE;
        let msg = 'Unable to retrieve your current location.';

        if (error.code === error.PERMISSION_DENIED) {
          mappedStatus = GEOLOCATION_STATUS.PERMISSION_DENIED;
          msg = 'Location permission denied. Please allow location access in your browser settings.';
        } else if (error.code === error.TIMEOUT) {
          mappedStatus = GEOLOCATION_STATUS.TIMEOUT;
          msg = 'GPS location detection timed out. Please try again or search by address.';
        } else if (error.code === error.POSITION_UNAVAILABLE) {
          mappedStatus = GEOLOCATION_STATUS.POSITION_UNAVAILABLE;
          msg = 'GPS location information is currently unavailable on this device.';
        }

        setStatus(mappedStatus);
        setErrorMessage(msg);
      },
      options
    );
  }, []);

  const resetStatus = useCallback(() => {
    setStatus(GEOLOCATION_STATUS.IDLE);
    setErrorMessage('');
  }, []);

  return {
    coords,
    accuracy,
    status,
    errorMessage,
    isLoading: status === GEOLOCATION_STATUS.LOADING,
    isSuccess: status === GEOLOCATION_STATUS.SUCCESS,
    getCurrentLocation,
    resetStatus,
  };
}

export default useGeolocation;
