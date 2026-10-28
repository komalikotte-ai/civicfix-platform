import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { complaintService } from '../../services/complaintService';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';

export const DashboardPage = () => {
  const { user } = useAuth();
  const [complaintsData, setComplaintsData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const data = await complaintService.getComplaints({ page: 0, size: 5 });
        setComplaintsData(data);
      } catch (err) {
        console.error('Failed to load dashboard complaints', err);
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  return (
    <div className="flex flex-col gap-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-gray-900">
            Welcome back, {user?.fullName || user?.username}!
          </h2>
          <p className="text-xs text-gray-500 mt-1">
            Municipal operations and grievance status summary
          </p>
        </div>
        <div>
          <Link to="/complaints/new">
            <Button variant="primary" size="md">
              + Lodge New Grievance
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="p-4 border-l-4 border-l-blue-600">
          <div className="text-xs font-medium text-gray-500">Total Grievances</div>
          <div className="text-2xl font-bold text-gray-900 mt-1">
            {complaintsData?.totalElements || 0}
          </div>
        </Card>
        <Card className="p-4 border-l-4 border-l-amber-500">
          <div className="text-xs font-medium text-gray-500">In Progress</div>
          <div className="text-2xl font-bold text-gray-900 mt-1">
            {complaintsData?.content?.filter((c) => c.status === 'IN_PROGRESS').length || 0}
          </div>
        </Card>
        <Card className="p-4 border-l-4 border-l-emerald-500">
          <div className="text-xs font-medium text-gray-500">Resolved</div>
          <div className="text-2xl font-bold text-gray-900 mt-1">
            {complaintsData?.content?.filter((c) => c.status === 'RESOLVED').length || 0}
          </div>
        </Card>
        <Card className="p-4 border-l-4 border-l-rose-500">
          <div className="text-xs font-medium text-gray-500">Pending Triage</div>
          <div className="text-2xl font-bold text-gray-900 mt-1">
            {complaintsData?.content?.filter((c) => c.status === 'SUBMITTED').length || 0}
          </div>
        </Card>
      </div>

      {/* Recent Complaints */}
      <Card
        title="Recent Grievances"
        subtitle="Latest complaints filed across the municipality"
        headerActions={
          <Link to="/complaints" className="text-xs font-semibold text-blue-600 hover:text-blue-800">
            View All →
          </Link>
        }
      >
        {loading ? (
          <Spinner />
        ) : !complaintsData?.content?.length ? (
          <div className="text-center py-8 text-xs text-gray-500">
            No complaints found in this view.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 text-gray-600 font-semibold border-b">
                <tr>
                  <th className="py-2.5 px-3">Tracking ID</th>
                  <th className="py-2.5 px-3">Title</th>
                  <th className="py-2.5 px-3">Category</th>
                  <th className="py-2.5 px-3">Priority</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {complaintsData.content.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="py-2.5 px-3 font-mono font-medium text-blue-700">
                      {item.trackingNumber}
                    </td>
                    <td className="py-2.5 px-3 font-medium text-gray-900 max-w-xs truncate">
                      {item.title}
                    </td>
                    <td className="py-2.5 px-3 text-gray-600">{item.category}</td>
                    <td className="py-2.5 px-3">
                      <Badge variant={item.priority} />
                    </td>
                    <td className="py-2.5 px-3">
                      <Badge variant={item.status} />
                    </td>
                    <td className="py-2.5 px-3">
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
      </Card>
    </div>
  );
};

export default DashboardPage;
