import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Spinner from '../../components/common/Spinner';

export const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles.length > 0) {
    const hasRequiredRole = allowedRoles.some((role) => {
      const normalized = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
      return user.roles?.includes(normalized);
    });

    if (!hasRequiredRole) {
      return (
        <div className="p-8 text-center bg-white rounded border border-red-200 mt-6">
          <h2 className="text-lg font-bold text-red-600 mb-2">Access Denied</h2>
          <p className="text-sm text-gray-600">
            You do not have the required permissions ({allowedRoles.join(', ')}) to view this resource.
          </p>
        </div>
      );
    }
  }

  return children;
};

export default ProtectedRoute;
