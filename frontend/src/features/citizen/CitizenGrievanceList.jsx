import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { citizenService } from '../../services/citizenService';
import { complaintService } from '../../services/complaintService';
import { usePagination } from '../../hooks/usePagination';
import { useDebounce } from '../../hooks/useDebounce';
import Table from '../../components/common/Table';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import Pagination from '../../components/common/Pagination';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import ReopenComplaintModal from '../complaints/components/ReopenComplaintModal';
import CitizenFeedbackModal from '../feedback/CitizenFeedbackModal';

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'NEW', label: 'New' },
  { value: 'SUBMITTED', label: 'Submitted' },
  { value: 'ACKNOWLEDGED', label: 'Acknowledged' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'ON_HOLD', label: 'On Hold' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'REOPENED', label: 'Reopened' },
];

const SEVERITY_OPTIONS = [
  { value: '', label: 'All Severities' },
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'CRITICAL', label: 'Critical' },
];

export const CitizenGrievanceList = () => {
  const navigate = useNavigate();
  const { page, setPage, size, getQueryParams } = usePagination(0, 10);

  const [data, setData] = useState(null);
  const [categories, setCategories] = useState([]);
  const [status, setStatus] = useState('');
  const [category, setCategory] = useState('');
  const [severity, setSeverity] = useState('');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modals state
  const [reopenTarget, setReopenTarget] = useState(null);
  const [feedbackTarget, setFeedbackTarget] = useState(null);
  const [toastMessage, setToastMessage] = useState('');

  const debouncedSearch = useDebounce(search, 400);

  // Load Categories for Filter Dropdown
  useEffect(() => {
    complaintService
      .getCategories()
      .then((catList) => {
        const formatted = [
          { value: '', label: 'All Categories' },
          ...(Array.isArray(catList) ? catList : []).map((c) => ({
            value: c.name || c,
            label: c.displayName || c.name || c,
          })),
        ];
        setCategories(formatted);
      })
      .catch(() => {});
  }, []);

  const fetchGrievances = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        ...getQueryParams(),
        ...(status ? { status } : {}),
        ...(category ? { category } : {}),
        ...(severity ? { severity } : {}),
        ...(debouncedSearch ? { keyword: debouncedSearch } : {}),
      };
      const res = await citizenService.getMyGrievances(params);
      setData(res);
    } catch (err) {
      setError(err.message || 'Failed to load grievances');
    } finally {
      setLoading(false);
    }
  }, [getQueryParams, status, category, severity, debouncedSearch]);

  useEffect(() => {
    fetchGrievances();
  }, [fetchGrievances, page]);

  const complaintsList = data?.content || (Array.isArray(data) ? data : []);
  const totalElements = data?.totalElements ?? complaintsList.length;
  const totalPages = data?.totalPages ?? Math.ceil(totalElements / size);

  const columns = [
    {
      key: 'trackingNumber',
      header: 'Tracking ID',
      render: (row) => (
        <span className="font-mono text-xs font-bold text-blue-600 hover:underline">
          {row.trackingNumber || `CF-${row.id}`}
        </span>
      ),
    },
    {
      key: 'title',
      header: 'Title & Description',
      render: (row) => (
        <div className="max-w-md">
          <div className="font-semibold text-slate-900 line-clamp-1">{row.title}</div>
          <div className="text-xs text-slate-500 line-clamp-1 mt-0.5">{row.description}</div>
        </div>
      ),
    },
    {
      key: 'category',
      header: 'Category',
      render: (row) => (
        <span className="px-2 py-0.5 rounded text-[11px] font-medium bg-slate-100 text-slate-700">
          {row.category || 'General'}
        </span>
      ),
    },
    {
      key: 'severity',
      header: 'Severity / Priority',
      render: (row) => (
        <div className="flex flex-col gap-1 items-start">
          <Badge status={row.severity || 'MEDIUM'}>{row.severity || 'MEDIUM'}</Badge>
          <span className="text-[10px] text-slate-500 font-medium">Priority: {row.priority || 'MEDIUM'}</span>
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge status={row.status}>{row.status}</Badge>,
    },
    {
      key: 'createdAt',
      header: 'Reported Date',
      render: (row) => (
        <span className="text-xs text-slate-500 font-mono">
          {row.createdAt ? new Date(row.createdAt).toLocaleDateString() : '—'}
        </span>
      ),
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (row) => {
        const isResolvedOrClosed = row.status === 'RESOLVED' || row.status === 'CLOSED';
        return (
          <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
            <Button
              size="sm"
              variant="ghost"
              onClick={() => navigate(`/complaints/${row.id}`)}
            >
              View
            </Button>
            {isResolvedOrClosed && (
              <>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setFeedbackTarget(row)}
                  title="Rate Redressal"
                >
                  ⭐ Rate
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setReopenTarget(row)}
                  className="text-red-600 hover:bg-red-50"
                  title="Reopen Grievance"
                >
                  Reopen
                </Button>
              </>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6 max-w-6xl mx-auto pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            My Grievance History
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Browse, filter, and track all municipal grievances submitted from your citizen account.
          </p>
        </div>

        <Button
          variant="success"
          size="md"
          onClick={() => navigate('/complaints/new')}
          className="font-bold shadow-xs"
        >
          <span aria-hidden="true">+</span> Lodge New Grievance
        </Button>
      </div>

      {toastMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs flex items-center justify-between">
          <span>{toastMessage}</span>
          <button type="button" onClick={() => setToastMessage('')} className="font-bold">
            ✕
          </button>
        </div>
      )}

      {/* Filter and Search Bar */}
      <div className="p-4 bg-white rounded-xl border border-slate-200 shadow-xs flex flex-col gap-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          <Input
            placeholder="Search by ID, title, keyword..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />

          <Select
            options={STATUS_OPTIONS}
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
          />

          {categories.length > 0 && (
            <Select
              options={categories}
              value={category}
              onChange={(e) => {
                setCategory(e.target.value);
                setPage(0);
              }}
            />
          )}

          <Select
            options={SEVERITY_OPTIONS}
            value={severity}
            onChange={(e) => {
              setSeverity(e.target.value);
              setPage(0);
            }}
          />
        </div>

        {(search || status || category || severity) && (
          <div className="flex items-center justify-between text-xs text-slate-500 border-t border-slate-100 pt-2">
            <span>Filtering applied</span>
            <button
              type="button"
              onClick={() => {
                setSearch('');
                setStatus('');
                setCategory('');
                setSeverity('');
                setPage(0);
              }}
              className="text-blue-600 font-semibold hover:underline"
            >
              Reset all filters
            </button>
          </div>
        )}
      </div>

      {/* Table & Data Representation */}
      {loading ? (
        <LoadingState message="Loading grievance history..." />
      ) : error ? (
        <ErrorState message={error} onRetry={fetchGrievances} />
      ) : complaintsList.length === 0 ? (
        <EmptyState
          title="No grievances match your search"
          description={
            search || status || category || severity
              ? 'Try adjusting your search filters or status selection.'
              : 'You have not submitted any complaints yet. Report an issue to get started.'
          }
          actionLabel="+ Lodge a Grievance"
          onAction={() => navigate('/complaints/new')}
        />
      ) : (
        <div className="flex flex-col gap-4">
          <Table
            columns={columns}
            data={complaintsList}
            onRowClick={(row) => navigate(`/complaints/${row.id}`)}
          />

          {totalPages > 1 && (
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={(newPage) => setPage(newPage)}
            />
          )}
        </div>
      )}

      {/* Reopen Modal */}
      {reopenTarget && (
        <ReopenComplaintModal
          isOpen={Boolean(reopenTarget)}
          complaint={reopenTarget}
          onClose={() => setReopenTarget(null)}
          onSuccess={() => {
            setReopenTarget(null);
            setToastMessage('Grievance reopened successfully.');
            fetchGrievances();
          }}
        />
      )}

      {/* Citizen Feedback Modal */}
      {feedbackTarget && (
        <CitizenFeedbackModal
          isOpen={Boolean(feedbackTarget)}
          complaint={feedbackTarget}
          onClose={() => setFeedbackTarget(null)}
          onSuccess={() => {
            setFeedbackTarget(null);
            setToastMessage('Thank you! Your rating and feedback were submitted.');
            fetchGrievances();
          }}
        />
      )}
    </div>
  );
};

export default CitizenGrievanceList;
