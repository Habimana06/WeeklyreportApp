import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { reportsAPI } from '../../services/api';
import { useAuth } from '../../contexts/AuthContext';
import WeekPicker from '../../components/WeekPicker';

const ReportList = () => {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [dateFilter, setDateFilter] = useState('');
  const [downloadError, setDownloadError] = useState('');
  const { isEmployee } = useAuth();

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async (params = {}) => {
    try {
      const response = await reportsAPI.getReports(params);
      const data = Array.isArray(response.data) ? response.data : [];
      setReports(data);
    } catch (error) {
      console.error('Error fetching reports:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'DRAFT': return 'bg-yellow-100 text-yellow-800';
      case 'SUBMITTED': return 'bg-blue-100 text-blue-800';
      case 'APPROVED': return 'bg-green-100 text-green-800';
      case 'REJECTED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  const filteredReports = (Array.isArray(reports) ? reports : []).filter(report => {
    if (filter === 'all') return true;
    return report.status === filter;
  });

  const handleApplyDateFilter = async () => {
    setLoading(true);
    const params = dateFilter ? { date: dateFilter } : {};
    await fetchReports(params);
  };

  const onWeekChange = ({ start, end }) => {
    setDateFilter(start);
  };

  const onAnyDateChange = (date) => {
    setDateFilter(date);
  };

  const handleDownloadPdf = async (id) => {
    try {
      const res = await reportsAPI.downloadPdf(id);
      const blob = new Blob([res.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${id}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      console.error('Failed to download report PDF', e);
      let message = 'Failed to download PDF. Please try again later or contact support.';
      try {
        if (e?.response?.data instanceof Blob) {
          const text = await e.response.data.text();
          try {
            const json = JSON.parse(text);
            if (json?.error) message = json.error;
          } catch {
            if (text) message = text;
          }
        } else if (e?.response?.data?.error) {
          message = e.response.data.error;
        }
      } catch {}
      setDownloadError(message);
      setTimeout(() => setDownloadError(''), 6000);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEmployee() ? 'My Reports' : 'All Reports'}
          </h1>
          <p className="text-gray-600">Manage and review weekly reports</p>
        </div>
        {isEmployee() && (
          <Link
            to="/reports/create"
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            Create New Report
          </Link>
        )}
      </div>

      {downloadError && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md whitespace-pre-wrap">
          {downloadError}
        </div>
      )}

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div className="flex flex-col md:flex-row md:items-center md:space-x-4 space-y-3 md:space-y-0">
          <div className="flex items-center space-x-4">
            <div>
              <label className="text-sm text-gray-600">Filter by week or date:</label>
              <div className="mt-2 flex flex-col sm:flex-row gap-4">
                <div>
                  <div className="text-xs text-gray-500 mb-1">By Friday (week)</div>
                  <WeekPicker
                    valueStart={dateFilter}
                    valueEnd={dateFilter ? undefined : undefined}
                    onChange={onWeekChange}
                    minDate={new Date(0)}
                    compact
                  />
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">By any date</div>
                  <WeekPicker
                    valueStart={dateFilter}
                    selectMode="any"
                    onChangeDate={onAnyDateChange}
                    minDate={new Date(0)}
                    compact
                  />
                </div>
              </div>
            </div>
            <div className="flex items-center space-x-2 pt-6">
              <button
                onClick={handleApplyDateFilter}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Apply
              </button>
              {dateFilter && (
                <button
                  onClick={() => { setDateFilter(''); fetchReports({}); }}
                  className="px-3 py-2 text-gray-600 hover:text-gray-800"
                >
                  Clear
                </button>
              )}
            </div>
          </div>

          <div className="flex space-x-4">
            <button
              onClick={() => setFilter('all')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                filter === 'all'
                  ? 'bg-blue-100 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              All ({reports.length})
            </button>
            <button
              onClick={() => setFilter('DRAFT')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                filter === 'DRAFT'
                  ? 'bg-yellow-100 text-yellow-700'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              Draft ({reports.filter(r => r.status === 'DRAFT').length})
            </button>
            <button
              onClick={() => setFilter('SUBMITTED')}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                filter === 'SUBMITTED'
                  ? 'bg-blue-100 text-blue-700'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              Submitted ({reports.filter(r => r.status === 'SUBMITTED').length})
            </button>
          </div>
        </div>
      </div>

      {/* Reports Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Week
                </th>
                {!isEmployee() && (
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Employee
                  </th>
                )}
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Submitted
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredReports.map((report) => (
                <tr key={report.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    Week of {formatDate(report.weekStartDate)}
                  </td>
                  {!isEmployee() && (
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {report.employeeUsername || 'Unknown'}
                    </td>
                  )}
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(report.status)}`}>
                      {report.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(report.createdAt)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {report.submittedAt ? formatDate(report.submittedAt) : '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-3">
                    <Link
                      to={`/reports/${report.id}`}
                      className="inline-flex items-center px-3 py-1.5 rounded-md bg-blue-600 text-white hover:bg-blue-700"
                    >
                      Review
                    </Link>
                    <button
                      onClick={() => handleDownloadPdf(report.id)}
                      className="inline-flex items-center px-3 py-1.5 rounded-md bg-gray-100 hover:bg-gray-200 text-gray-800 border border-gray-300"
                      title="Download as PDF"
                    >
                      Download
                    </button>
                    {isEmployee() && report.status === 'DRAFT' && (
                      <Link
                        to={`/reports/${report.id}/edit`}
                        className="inline-flex items-center px-3 py-1.5 rounded-md bg-green-600 text-white hover:bg-green-700"
                      >
                        Edit
                      </Link>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {filteredReports.length === 0 && (
          <div className="text-center py-8">
            <p className="text-gray-500">
              {filter === 'all' ? 'No reports found' : `No ${filter.toLowerCase()} reports found`}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ReportList;
