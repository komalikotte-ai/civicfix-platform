import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import ProtectedRoute from '../features/auth/ProtectedRoute';
import LoginPage from '../features/auth/LoginPage';
import RegisterPage from '../features/auth/RegisterPage';
import DashboardPage from '../features/dashboard/DashboardPage';
import ComplaintsListPage from '../features/complaints/ComplaintsListPage';
import ComplaintDetailPage from '../features/complaints/ComplaintDetailPage';
import CreateComplaintPage from '../features/complaints/CreateComplaintPage';
import CitizenDashboard from '../features/citizen/CitizenDashboard';
import CitizenGrievanceList from '../features/citizen/CitizenGrievanceList';
import WardMapView from '../features/maps/WardMapView';
import PublicTrackPage from '../features/public-portal/PublicTrackPage';
import PublicLandingPage from '../features/public-portal/PublicLandingPage';
import PublicTransparencyPage from '../features/public-portal/PublicTransparencyPage';

export const router = createBrowserRouter([
  // Public Citizen Portal & Tracking Routes (No login required)
  {
    path: '/welcome',
    element: <PublicLandingPage />,
  },
  {
    path: '/transparency',
    element: <PublicTransparencyPage />,
  },
  {
    path: '/track',
    element: <PublicTrackPage />,
  },
  {
    path: '/track/:trackingNumber',
    element: <PublicTrackPage />,
  },

  // Public Auth Routes
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },

  // Authenticated Application Shell
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <CitizenDashboard />,
      },
      {
        path: 'operations-dashboard',
        element: (
          <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'DEPT_ADMIN', 'SUPERVISOR', 'OFFICER']}>
            <DashboardPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'citizen',
        element: <CitizenDashboard />,
      },
      {
        path: 'citizen/grievances',
        element: <CitizenGrievanceList />,
      },
      {
        path: 'complaints',
        element: <ComplaintsListPage />,
      },
      {
        path: 'complaints/new',
        element: <CreateComplaintPage />,
      },
      {
        path: 'complaints/:id',
        element: <ComplaintDetailPage />,
      },
      {
        path: 'map',
        element: <WardMapView />,
      },
      {
        path: 'transparency',
        element: <PublicTransparencyPage />,
      },
      {
        path: 'wards',
        element: (
          <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'DEPT_ADMIN', 'WARD_OFFICER']}>
            <div className="p-6 bg-white rounded-xl border border-slate-200">
              <h2 className="text-lg font-bold text-slate-900">Wards Administration</h2>
              <p className="text-xs text-slate-500 mt-1">Geographical zones and municipal boundary allocations</p>
            </div>
          </ProtectedRoute>
        ),
      },
      {
        path: 'departments',
        element: (
          <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'DEPT_ADMIN']}>
            <div className="p-6 bg-white rounded-xl border border-slate-200">
              <h2 className="text-lg font-bold text-slate-900">Departments Administration</h2>
              <p className="text-xs text-slate-500 mt-1">Municipal departments and service categories configuration</p>
            </div>
          </ProtectedRoute>
        ),
      },
      {
        path: '*',
        element: <Navigate to="/" replace />,
      },
    ],
  },
]);

export default router;
