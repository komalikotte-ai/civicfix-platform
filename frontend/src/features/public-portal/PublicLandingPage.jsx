import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';

export const PublicLandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between">
      {/* Top Navigation */}
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-30 shadow-sm">
        <Link to="/" className="flex items-center gap-2">
          <div className="h-9 w-9 rounded-xl bg-blue-600 text-white font-extrabold flex items-center justify-center text-base shadow-sm">
            CF
          </div>
          <div>
            <span className="text-base font-extrabold text-slate-900 leading-none">CivicFix</span>
            <span className="text-[11px] text-slate-500 block">Citizen Grievance Redressal</span>
          </div>
        </Link>
        <div className="flex items-center gap-3">
          <Link
            to="/track"
            className="text-xs font-semibold text-slate-700 hover:text-blue-600 px-3 py-1.5"
          >
            Track Status
          </Link>
          <Link
            to="/login"
            className="text-xs font-semibold text-slate-700 hover:text-blue-600 px-3 py-1.5"
          >
            Sign In
          </Link>
          <Link
            to="/register"
            className="text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 px-3.5 py-2 rounded-lg shadow-sm"
          >
            Register as Citizen
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 flex flex-col gap-12 py-10 px-4 max-w-5xl mx-auto w-full">
        <div className="text-center flex flex-col items-center gap-3 pt-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-semibold border border-blue-200">
            🏛️ Municipal Corporation of Hyderabad &bull; Urban Local Body
          </div>
          <h1 className="text-3xl sm:text-5xl font-black text-slate-900 tracking-tight max-w-2xl">
            Empowering Citizens for a Cleaner, Safer City
          </h1>
          <p className="text-sm sm:text-base text-slate-600 max-w-xl">
            Report potholes, water leakage, garbage overflow, or street light issues directly to municipal departments with transparent SLA tracking.
          </p>
          <div className="flex flex-wrap items-center justify-center gap-3 mt-4">
            <Button
              variant="primary"
              size="lg"
              onClick={() => navigate('/register')}
              className="shadow-md text-sm font-bold"
            >
              Report a Civic Issue →
            </Button>
            <Button
              variant="outline"
              size="lg"
              onClick={() => navigate('/track')}
              className="text-sm font-bold"
            >
              Track Existing Report
            </Button>
          </div>
        </div>

        {/* Civic Services Quick Action Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card className="p-5 hover:shadow-md transition-shadow border-t-4 border-t-amber-500">
            <div className="text-2xl mb-2">🛣️</div>
            <h3 className="text-sm font-bold text-slate-900">Roads & Footpaths</h3>
            <p className="text-xs text-slate-500 mt-1">
              Report potholes, damaged pavements, road cave-ins, and traffic obstruction.
            </p>
          </Card>

          <Card className="p-5 hover:shadow-md transition-shadow border-t-4 border-t-emerald-500">
            <div className="text-2xl mb-2">🗑️</div>
            <h3 className="text-sm font-bold text-slate-900">Sanitation & Waste</h3>
            <p className="text-xs text-slate-500 mt-1">
              Garbage dumps, open waste burning, delayed door-to-door collection.
            </p>
          </Card>

          <Card className="p-5 hover:shadow-md transition-shadow border-t-4 border-t-blue-500">
            <div className="text-2xl mb-2">💧</div>
            <h3 className="text-sm font-bold text-slate-900">Water & Drainage</h3>
            <p className="text-xs text-slate-500 mt-1">
              Pipeline burst, contaminated drinking water, sewer blockages, manhole repair.
            </p>
          </Card>

          <Card className="p-5 hover:shadow-md transition-shadow border-t-4 border-t-purple-500">
            <div className="text-2xl mb-2">💡</div>
            <h3 className="text-sm font-bold text-slate-900">Street Lighting</h3>
            <p className="text-xs text-slate-500 mt-1">
              Non-functioning streetlights, exposed wiring, dark municipal stretches.
            </p>
          </Card>
        </div>

        {/* Emergency & Municipal Helpline */}
        <div className="bg-slate-900 text-white rounded-2xl p-6 sm:p-8 flex flex-col md:flex-row items-center justify-between gap-6 shadow-xl">
          <div>
            <h3 className="text-lg font-bold">24x7 Emergency Municipal Helpline</h3>
            <p className="text-xs text-slate-300 mt-1 max-w-md">
              For immediate emergencies such as major water pipeline bursts, flood warnings, or road sinkholes, dial the municipal control room directly.
            </p>
          </div>
          <div className="flex items-center gap-4 text-center">
            <div className="bg-slate-800 px-4 py-2.5 rounded-xl border border-slate-700">
              <div className="text-[10px] text-slate-400 font-bold uppercase">Toll Free</div>
              <div className="text-lg font-mono font-bold text-emerald-400">1800-425-0011</div>
            </div>
            <div className="bg-slate-800 px-4 py-2.5 rounded-xl border border-slate-700">
              <div className="text-[10px] text-slate-400 font-bold uppercase">Control Room</div>
              <div className="text-lg font-mono font-bold text-blue-400">040-2111-1111</div>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 text-center text-xs text-slate-500">
        CivicFix Platform &bull; Ministry of Urban Development & Local Self Governance &copy; {new Date().getFullYear()}
      </footer>
    </div>
  );
};

export default PublicLandingPage;
