import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar.jsx';
import Sidebar from './Sidebar.jsx';

export const AppLayout = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Accessible Skip Link */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-3 focus:left-3 focus:z-50 focus:px-4 focus:py-2 focus:bg-blue-700 focus:text-white focus:rounded-xl focus:shadow-xl focus:font-bold focus:outline-none focus:ring-2 focus:ring-white"
      >
        Skip to main content
      </a>

      <Navbar />

      <div className="flex flex-1">
        <Sidebar />
        <main id="main-content" tabIndex={-1} className="flex-1 p-4 sm:p-6 max-w-7xl mx-auto w-full focus:outline-none">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
