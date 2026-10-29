import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { complaintService } from '../../services/complaintService';
import { usePagination } from '../../hooks/usePagination';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Select from '../../components/common/Select';
import Pagination from '../../components/common/Pagination';
import Spinner from '../../components/common/Spinner';
import Alert from '../../components/common/Alert';

const STATUS_OPTIONS = [
  { value: 'SUBMITTED', label: 'Submitted' },
  { value: 'ACKNOWLEDGED', label: 'Acknowledged' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'RESOLVED', label: 'Resolved' },
  { value: 'CLOSED', label: 'Closed' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'ESCALATED', label: 'Escalated' },
];

export const ComplaintsListPage = () => {
  const { page, setPage, size, getQueryParams } = usePagination(0, 15);
  const [complaintsData, setComplaintsData] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');
  const [trackingSearch, setTrackingSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchComplaints = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = getQueryParams();
      if (statusFilter) params.status = statusFilter;
      const data = await complaintService.getComplaints(params);
      setComplaintsData(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch complaints');
    } finally {
      setLoading(false);
    }
  }, [getQueryParams, statusFilter]);

  useEffect(() => {
    fetchComplaints();
  }, [fetchComplaints, page]);

  const handleTrackSearch = async (e) => {
    e.preventDefault();
    if (!trackingSearch.trim()) {
      fetchComplaints();
      return;
    }
    setLoading(true);
    setError('');
    try {
      const single = await complaintService.trackComplaint(trackingSearch.trim());
      setComplaintsData({
        content: [single],
        page: 0,
        size: 1,
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
      });
    } catch (err) {
      setError(`No complaint found with tracking number '${trackingSearch}'`);
      setComplaintsData({ content: [], totalElements: 0, totalPages: 0 });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-gray-900">Municipal Grievances</h2>
          <p className="text-xs text-gray-500 mt-1">
            Browse, filter, and track public complaints
          </p>
        </div>
        <Link to="/complaints/new">
          <Button variant="primary" size="md">
            + Lodge Grievance
          </Button>
        </Link>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      {/* Filter & Search Bar */}
      <Card>
        <div className="flex flex-col sm:flex-row items-end gap-3">
          <form onSubmit={handleTrackSearch} className="flex-1 flex gap-2 w-full">
            <Input
              placeholder="Search by Tracking Number (e.g. CF-20261028-1234AB)"
              value={trackingSearch}
              onChange={(e) => setTrackingSearch(e.target.value)}
              className="flex-1"
            />
            <Button type="submit" variant="secondary">
              Search
            </Button>
          </form>

          <div className="w-full sm:w-48">
            <Select
              options={STATUS_OPTIONS}
              placeholder="All Statuses"
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
            />
          </div>
        </div>
      </Card>

      {/* Table */}
      <Card className="p-0">
        {loading ? (
          <Spinner />
        ) : !complaintsData?.content?.length ? (
          <div className="text-center py-12 text-xs text-gray-500">
            No complaints found matching current filters.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 text-gray-600 font-semibold border-b">
                <tr>
                  <th className="py-3 px-4">Tracking ID</th>
                  <th className="py-3 px-4">Title</th>
                  <th className="py-3 px-4">Ward</th>
                  <th className="py-3 px-4">Department</th>
                  <th className="py-3 px-4">Priority</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">SLA Due</th>
                  <th className="py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {complaintsData.content.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                    <td className="py-3 px-4 font-mono font-medium text-blue-700">
                      {item.trackingNumber}
                    </td>
                    <td className="py-3 px-4 font-medium text-gray-900 max-w-xs truncate">
                      {item.title}
                    </td>
                    <td className="py-3 px-4 text-gray-600">Ward #{item.wardNumber}</td>
                    <td className="py-3 px-4 text-gray-600">{item.departmentName || '—'}</td>
                    <td className="py-3 px-4">
                      <Badge variant={item.priority} />
                    </td>
                    <td className="py-3 px-4">
                      <Badge variant={item.status} />
                    </td>
                    <td className="py-3 px-4 text-gray-500 text-[11px]">
                      {item.slaDueAt ? new Date(item.slaDueAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="py-3 px-4">
                      <Link
                        to={`/complaints/${item.id}`}
                        className="text-blue-600 font-semibold hover:underline"
                      >
                        Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          page={complaintsData?.page || 0}
          totalPages={complaintsData?.totalPages || 1}
          totalElements={complaintsData?.totalElements || 0}
          size={size}
          onPageChange={(newPage) => setPage(newPage)}
        />
      </Card>
    </div>
  );
};

export default ComplaintsListPage;
