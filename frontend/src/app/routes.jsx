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
import WorkerDashboard from '../features/fieldworker/WorkerDashboard';
import JobExecutionPage from '../features/fieldworker/JobExecutionPage';

export const router = createBrowserRouter([
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
        element: <DashboardPage />,
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
        path: 'operations',
        element: (
          <ProtectedRoute allowedRoles={['FIELD_WORKER', 'SUPERVISOR', 'DEPT_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'EMPLOYEE']}>
            <WorkerDashboard />
          </ProtectedRoute>
        ),
      },
      {
        path: 'operations/job/:id',
        element: (
          <ProtectedRoute allowedRoles={['FIELD_WORKER', 'SUPERVISOR', 'DEPT_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'EMPLOYEE']}>
            <JobExecutionPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'wards',
        element: (
          <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'DEPT_ADMIN']}>
            <div className="p-6 bg-white rounded border">
              <h2 className="text-lg font-bold">Wards Administration</h2>
              <p className="text-xs text-gray-500 mt-1">Geographical zones management</p>
            </div>
          </ProtectedRoute>
        ),
      },
      {
        path: 'departments',
        element: (
          <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
            <div className="p-6 bg-white rounded border">
              <h2 className="text-lg font-bold">Departments Administration</h2>
              <p className="text-xs text-gray-500 mt-1">Municipal departments configuration</p>
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
