import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';

export const PublicTrackPage = () => {
  const { trackingNumber: routeTrackingNumber } = useParams();
  const navigate = useNavigate();

  const [inputNumber, setInputNumber] = useState(routeTrackingNumber || '');
  const [complaint, setComplaint] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searched, setSearched] = useState(false);

  const performSearch = async (numToSearch) => {
    if (!numToSearch || !numToSearch.trim()) return;
    setLoading(true);
    setError(null);
    setSearched(true);
    try {
      const data = await complaintService.trackComplaint(numToSearch.trim());
      setComplaint(data);
    } catch (err) {
      setComplaint(null);
      setError(
        err.statusCode === 404
          ? `Grievance #${numToSearch} was not found. Please verify the tracking number and try again.`
          : err.message || 'Failed to search grievance record.'
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (routeTrackingNumber) {
      setInputNumber(routeTrackingNumber);
      performSearch(routeTrackingNumber);
    }
  }, [routeTrackingNumber]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (!inputNumber.trim()) return;
    navigate(`/track/${encodeURIComponent(inputNumber.trim())}`, { replace: true });
    performSearch(inputNumber);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between">
      {/* Top Navigation Bar */}
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-blue-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
            CF
          </div>
          <div>
            <span className="text-base font-bold text-slate-900 leading-none">CivicFix</span>
            <span className="text-xs text-slate-500 block">Public Redressal Portal</span>
          </div>
        </Link>
        <div className="flex items-center gap-3">
          <Link
            to="/login"
            className="text-xs font-semibold text-slate-700 hover:text-blue-600 px-3 py-1.5"
          >
            Staff Login
          </Link>
          <Link
            to="/register"
            className="text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 px-3.5 py-1.5 rounded-lg shadow-sm"
          >
            Citizen Sign Up
          </Link>
        </div>
      </header>

      {/* Main Track Section */}
      <main className="max-w-3xl w-full mx-auto px-4 py-8 flex-1 flex flex-col gap-6">
        <div className="text-center">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-semibold mb-2 border border-blue-100">
            🔍 Public Grievance Tracking
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            Track Grievance Status
          </h1>
          <p className="text-sm text-slate-600 mt-1 max-w-lg mx-auto">
            Check the real-time resolution timeline of your municipal complaint using your tracking reference number.
          </p>
        </div>

        {/* Tracking Search Input */}
        <Card className="p-4 sm:p-6 shadow-md">
          <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                placeholder="Enter Reference (e.g. CF-20261028-0001 or CMP-001)"
                value={inputNumber}
                onChange={(e) => setInputNumber(e.target.value)}
                required
                aria-label="Grievance tracking reference number"
              />
            </div>
            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={loading}
              className="sm:w-36"
            >
              Track Status
            </Button>
          </form>
        </Card>

        {/* Search Results */}
        {loading ? (
          <LoadingState message="Fetching grievance status from municipal registry..." />
        ) : error ? (
          <ErrorState
            title="Tracking Search Failed"
            message={error}
            onRetry={() => performSearch(inputNumber)}
          />
        ) : complaint ? (
          <Card className="p-6 flex flex-col gap-6 shadow-sm border-slate-200">
            {/* Header info */}
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 pb-5 border-b border-slate-100">
              <div>
                <span className="font-mono text-xs font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-full border border-blue-100">
                  {complaint.trackingNumber || `REF-${complaint.id}`}
                </span>
                <h2 className="text-xl font-bold text-slate-900 mt-2">{complaint.title}</h2>
                <p className="text-xs text-slate-500 mt-1">
                  Reported on: {new Date(complaint.createdAt).toLocaleString()}
                </p>
              </div>
              <div className="flex flex-col sm:items-end gap-1.5">
                <Badge status={complaint.status} size="lg">
                  {complaint.status}
                </Badge>
                {complaint.slaDueAt && (
                  <span className="text-[11px] text-slate-500">
                    SLA Target: {new Date(complaint.slaDueAt).toLocaleDateString()}
                  </span>
                )}
              </div>
            </div>

            {/* Metadata Grid */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 bg-slate-50/70 p-4 rounded-xl border border-slate-100 text-xs">
              <div>
                <div className="text-slate-500 font-medium">Department</div>
                <div className="font-semibold text-slate-900 mt-0.5">
                  {complaint.departmentName || complaint.department?.name || 'General Municipal'}
                </div>
              </div>
              <div>
                <div className="text-slate-500 font-medium">Category</div>
                <div className="font-semibold text-slate-900 mt-0.5">{complaint.category || 'General'}</div>
              </div>
              <div>
                <div className="text-slate-500 font-medium">Ward</div>
                <div className="font-semibold text-slate-900 mt-0.5">
                  {complaint.wardName || (complaint.wardId ? `Ward #${complaint.wardId}` : 'City Wide')}
                </div>
              </div>
              <div>
                <div className="text-slate-500 font-medium">Severity / Priority</div>
                <div className="font-semibold text-slate-900 mt-0.5">
                  {complaint.severity || 'MEDIUM'} / {complaint.priority || 'MEDIUM'}
                </div>
              </div>
            </div>

            {/* Description */}
            <div>
              <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Grievance Description
              </h3>
              <p className="text-sm text-slate-600 bg-white p-3.5 rounded-lg border border-slate-200 leading-relaxed">
                {complaint.description}
              </p>
            </div>

            {/* Progression Bar */}
            <div>
              <h3 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-3">
                Resolution Workflow Progress
              </h3>
              <div className="flex items-center justify-between relative text-xs">
                {['SUBMITTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED'].map((step, idx) => {
                  const stepsOrder = ['NEW', 'SUBMITTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
                  const currentIndex = stepsOrder.indexOf(complaint.status);
                  const stepIndex = stepsOrder.indexOf(step);
                  const isDone = currentIndex >= stepIndex;
                  const isCurrent = complaint.status === step;

                  return (
                    <div key={step} className="flex flex-col items-center flex-1 text-center relative z-10">
                      <div
                        className={`w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs transition-colors ${
                          isDone
                            ? 'bg-blue-600 text-white'
                            : isCurrent
                            ? 'bg-blue-100 text-blue-700 border-2 border-blue-600'
                            : 'bg-slate-200 text-slate-500'
                        }`}
                      >
                        {isDone ? '✓' : idx + 1}
                      </div>
                      <span className={`text-[11px] mt-1.5 font-medium ${isDone ? 'text-slate-900' : 'text-slate-400'}`}>
                        {step.replace('_', ' ')}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Public Action Footer */}
            <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
              <span>Have additional information or want to appeal this resolution?</span>
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="font-semibold text-blue-600 hover:text-blue-800 underline"
                >
                  Sign in to Reopen / Add Comments →
                </Link>
              </div>
            </div>
          </Card>
        ) : searched ? (
          <div className="text-center text-xs text-slate-500 py-6">
            Enter a valid reference number above to view resolution updates.
          </div>
        ) : null}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-4 text-center text-xs text-slate-500">
        CivicFix Municipal Platform &copy; {new Date().getFullYear()} &bull; Citizen Grievance Redressal
      </footer>
    </div>
  );
};

export default PublicTrackPage;
