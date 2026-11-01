import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import publicPortalService from '../../services/publicPortalService.js';
import organizationService from '../../services/organizationService.js';
import complaintService from '../../services/complaintService.js';
import StatCard from './components/StatCard.jsx';
import SimpleBarChart from './components/SimpleBarChart.jsx';
import SimpleDonutChart from './components/SimpleDonutChart.jsx';
import PublicFilters from './components/PublicFilters.jsx';
import WardResolutionTable from './components/WardResolutionTable.jsx';
import DepartmentPerformanceTable from './components/DepartmentPerformanceTable.jsx';
import PublicProjectTracker from './components/PublicProjectTracker.jsx';
import MonthlyReportsView from './components/MonthlyReportsView.jsx';
import PublicMapComponent from './components/PublicMapComponent.jsx';
import LoadingState from '../../components/common/LoadingState.jsx';
import ErrorState from '../../components/common/ErrorState.jsx';
import Card from '../../components/common/Card.jsx';
import Button from '../../components/common/Button.jsx';

export const PublicTransparencyPage = () => {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'wards' | 'departments' | 'map' | 'projects' | 'reports'
  const [stats, setStats] = useState(null);
  const [wardData, setWardData] = useState([]);
  const [departmentData, setDepartmentData] = useState([]);
  const [projects, setProjects] = useState([]);
  const [reports, setReports] = useState([]);
  const [publicPins, setPublicPins] = useState([]);
  const [wards, setWards] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [categories, setCategories] = useState([]);

  const [filters, setFilters] = useState({
    wardId: '',
    departmentId: '',
    category: '',
    startDate: '',
    endDate: '',
    preset: 'all',
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [
        statsRes,
        wardsRes,
        deptsRes,
        projRes,
        repRes,
        pinsRes,
        wList,
        dList,
        cList,
      ] = await Promise.all([
        publicPortalService.getPublicStatistics(filters),
        publicPortalService.getWardPerformance(filters),
        publicPortalService.getDepartmentPerformance(filters),
        publicPortalService.getPublicProjects(),
        publicPortalService.getMonthlyReports(),
        publicPortalService.getSanitizedPublicPins(filters),
        organizationService.getWards().catch(() => []),
        organizationService.getDepartments().catch(() => []),
        complaintService.getCategories().catch(() => []),
      ]);

      setStats(statsRes);
      setWardData(wardsRes);
      setDepartmentData(deptsRes);
      setProjects(projRes);
      setReports(repRes);
      setPublicPins(pinsRes);
      setWards(Array.isArray(wList) ? wList : []);
      setDepartments(Array.isArray(dList) ? dList : []);
      setCategories(Array.isArray(cList) ? cList : []);
    } catch (err) {
      setError(err.message || 'Failed to load transparency portal data.');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleFilterChange = (newFilters) => {
    setFilters((prev) => ({ ...prev, ...newFilters }));
  };

  const handleResetFilters = () => {
    setFilters({
      wardId: '',
      departmentId: '',
      category: '',
      startDate: '',
      endDate: '',
      preset: 'all',
    });
  };

  const statusDonutData = [
    { label: 'Resolved / Closed', value: stats?.resolvedComplaints || 0, color: '#10b981' },
    { label: 'In Progress / Assigned', value: stats?.inProgressComplaints || 0, color: '#3b82f6' },
    { label: 'New / Triage', value: stats?.newComplaints || 0, color: '#f59e0b' },
  ];

  const departmentBarData = departmentData.slice(0, 6).map((d) => ({
    label: d.name,
    value: d.resolutionRate,
    color: d.resolutionRate >= 80 ? '#10b981' : d.resolutionRate >= 60 ? '#3b82f6' : '#f59e0b',
  }));

  const tabs = [
    { id: 'overview', label: '📊 City Overview', count: null },
    { id: 'wards', label: '🏛️ Ward Performance', count: wardData.length },
    { id: 'departments', label: '🏢 Departments', count: departmentData.length },
    { id: 'map', label: '🗺️ Public Issue Map', count: publicPins.length },
    { id: 'projects', label: '🏗️ City Projects', count: projects.length },
    { id: 'reports', label: '📜 Monthly Reports', count: reports.length },
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between">
      {/* Top Header */}
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-30 shadow-xs">
        <Link to="/" className="flex items-center gap-2.5">
          <div className="h-9 w-9 rounded-xl bg-blue-600 text-white font-black flex items-center justify-center text-base shadow-sm">
            CF
          </div>
          <div>
            <span className="text-base font-extrabold text-slate-900 leading-none">CivicFix</span>
            <span className="text-[11px] text-slate-500 block">Public Transparency & Open Governance</span>
          </div>
        </Link>

        <div className="flex items-center gap-2.5">
          <Link to="/track" className="text-xs font-semibold text-slate-700 hover:text-blue-600 px-3 py-1.5">
            Track Grievance
          </Link>
          <Link to="/login" className="text-xs font-semibold text-slate-700 hover:text-blue-600 px-3 py-1.5">
            Sign In
          </Link>
          <Button
            size="sm"
            variant="primary"
            onClick={() => navigate('/register')}
            className="text-xs font-bold"
          >
            Report an Issue
          </Button>
        </div>
      </header>

      {/* Main Body */}
      <main className="max-w-7xl w-full mx-auto px-4 py-8 flex-1 flex flex-col gap-6">
        {/* Hero Section */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-2 border-b border-slate-200">
          <div>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-semibold mb-2 border border-blue-100">
              🔓 Public Governance Disclosure
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
              City Grievance Redressal Transparency Portal
            </h1>
            <p className="text-xs sm:text-sm text-slate-600 mt-1 max-w-2xl">
              Open municipal performance data, ward resolution audits, and SLA compliance metrics published for citizen accountability.
            </p>
          </div>

          <Button variant="outline" size="sm" onClick={loadData} className="self-start md:self-auto text-xs">
            ↻ Sync Public Data
          </Button>
        </div>

        {/* Global Multi-dimensional Filter Bar */}
        <PublicFilters
          filters={filters}
          onChange={handleFilterChange}
          onReset={handleResetFilters}
          wards={wards}
          departments={departments}
          categories={categories}
        />

        {/* Navigation Tabs */}
        <div className="flex items-center gap-2 overflow-x-auto border-b border-slate-200 pb-2 text-xs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`px-3.5 py-2 rounded-xl font-bold whitespace-nowrap transition-all cursor-pointer flex items-center gap-1.5 ${
                activeTab === tab.id
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              <span>{tab.label}</span>
              {tab.count != null && (
                <span
                  className={`px-1.5 py-0.2 rounded-full text-[10px] ${
                    activeTab === tab.id ? 'bg-blue-700 text-blue-100' : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {tab.count}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        {loading ? (
          <LoadingState message="Aggregating municipal transparency disclosures..." />
        ) : error ? (
          <ErrorState message={error} onRetry={loadData} />
        ) : (
          <div>
            {/* Overview Tab */}
            {activeTab === 'overview' && (
              <div className="flex flex-col gap-6">
                {/* 4 Stat Cards */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  <StatCard
                    title="Total Complaints Filed"
                    value={stats?.totalComplaints || 0}
                    subtitle="Logged in municipal database"
                    icon="📋"
                    variant="blue"
                    trend="+12% this month"
                    trendPositive={true}
                  />

                  <StatCard
                    title="Resolution Rate"
                    value={`${stats?.resolutionRatePercent || 0}%`}
                    subtitle="Overall closed and verified"
                    icon="✅"
                    variant="emerald"
                    trend="+4.2% efficiency"
                    trendPositive={true}
                  />

                  <StatCard
                    title="Avg Turnaround Time"
                    value={`${stats?.averageResolutionHours || 0} hrs`}
                    subtitle="From filing to field closure"
                    icon="⏱️"
                    variant="amber"
                  />

                  <StatCard
                    title="SLA Compliance Rate"
                    value={`${stats?.slaComplianceRate || 0}%`}
                    subtitle="Resolved within statutory SLA"
                    icon="🎯"
                    variant="purple"
                    trend="Target: >90%"
                    trendPositive={true}
                  />
                </div>

                {/* Visual Charts Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  <Card className="p-5">
                    <SimpleDonutChart
                      data={statusDonutData}
                      title="Incident Status Distribution"
                    />
                  </Card>

                  <Card className="p-5">
                    <SimpleBarChart
                      data={departmentBarData}
                      title="Department Resolution Efficiency (%)"
                    />
                  </Card>
                </div>

                {/* Top Ward Highlight Table */}
                <Card title="Ward Redressal Scorecard (Top Wards)">
                  <WardResolutionTable wardPerformance={wardData.slice(0, 5)} />
                </Card>
              </div>
            )}

            {/* Wards Tab */}
            {activeTab === 'wards' && (
              <Card title="Ward-Level Resolution Statistics">
                <WardResolutionTable wardPerformance={wardData} />
              </Card>
            )}

            {/* Departments Tab */}
            {activeTab === 'departments' && (
              <Card title="Department Redressal Workload & SLA Compliance">
                <DepartmentPerformanceTable departmentPerformance={departmentData} />
              </Card>
            )}

            {/* Public Map Tab */}
            {activeTab === 'map' && (
              <Card title="Public Incident & Spatial Density Map">
                <PublicMapComponent pins={publicPins} wards={wards} />
              </Card>
            )}

            {/* Projects Tab */}
            {activeTab === 'projects' && (
              <PublicProjectTracker projects={projects} />
            )}

            {/* Reports Tab */}
            {activeTab === 'reports' && (
              <MonthlyReportsView reports={reports} />
            )}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <span>CivicFix Municipal Redressal Platform &copy; {new Date().getFullYear()}</span>
          <span>Open Data &bull; Citizen Empowerment &bull; Privacy Protected</span>
        </div>
      </footer>
    </div>
  );
};

export default PublicTransparencyPage;
