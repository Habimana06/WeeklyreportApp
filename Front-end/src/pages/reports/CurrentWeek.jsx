import React, { useEffect, useState } from 'react';
import { reportsAPI } from '../../services/api';
import { Link } from 'react-router-dom';

const CurrentWeek = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const run = async () => {
      try {
        const res = await reportsAPI.getCurrentWeekReports();
        setReports(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        setError(err.response?.data?.error || 'Failed to load current week reports');
      } finally {
        setLoading(false);
      }
    };
    run();
  }, []);

  if (loading) return <div className="p-6">Loading...</div>;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Current Week Reports</h1>
        <p className="text-gray-600">Visible to admins and supervisors</p>
      </div>
      {error && <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">{error}</div>}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Week</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Employee</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {reports.map(r => (
                <tr key={r.id}>
                  <td className="px-6 py-4 text-sm">{new Date(r.weekStartDate).toLocaleDateString()}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{r.employeeDisplayName || r.employeeUsername || 'Unknown'}</td>
                  <td className="px-6 py-4 text-sm">{r.status}</td>
                  <td className="px-6 py-4 text-sm">
                    <Link to={`/reports/${r.id}`} className="text-blue-600 hover:text-blue-800">View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {reports.length === 0 && (
          <div className="text-center py-8 text-gray-500">No reports for current week</div>
        )}
      </div>
    </div>
  );
};

export default CurrentWeek;


