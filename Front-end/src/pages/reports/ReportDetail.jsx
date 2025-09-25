import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { reportsAPI } from '../../services/api';
import { useAuth } from '../../contexts/AuthContext';

const ReportDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isEmployee, isSupervisor, isAdmin } = useAuth();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchReport();
    fetchAttachments();
  }, [id]);

  const fetchReport = async () => {
    try {
      const response = await reportsAPI.getReportById(id);
      setReport(response.data);
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to fetch report');
    } finally {
      setLoading(false);
    }
  };

  const fetchAttachments = async () => {
    try {
      const res = await reportsAPI.listAttachments(id);
      setAttachments(Array.isArray(res.data) ? res.data : []);
    } catch {
      // silent fail
    }
  };

  const handleSubmit = async () => {
    if (!report) return;
    
    setActionLoading(true);
    try {
      await reportsAPI.submitReport(id);
      setReport({ ...report, status: 'SUBMITTED', submittedAt: new Date().toISOString() });
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to submit report');
    } finally {
      setActionLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!report) return;
    
    setActionLoading(true);
    try {
      const response = await reportsAPI.approveReport(id, feedback);
      setReport(response.data);
      setFeedback('');
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to approve report');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    if (!report) return;
    
    setActionLoading(true);
    try {
      const response = await reportsAPI.rejectReport(id, feedback);
      setReport(response.data);
      setFeedback('');
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to reject report');
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await reportsAPI.uploadAttachment(id, file);
      await fetchAttachments();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to upload file');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleDownload = async (attachmentId, name) => {
    try {
      const res = await reportsAPI.downloadAttachment(id, attachmentId);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = name || 'attachment';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Failed to download attachment');
    }
  };

  const handleStatusOverride = async (newStatus) => {
    if (!report) return;
    setActionLoading(true);
    try {
      const response = await reportsAPI.overrideReportStatus(id, newStatus, feedback);
      setReport(response.data);
      setFeedback('');
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to override status');
    } finally {
      setActionLoading(false);
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
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
        {error}
      </div>
    );
  }

  if (!report) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-500">Report not found</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Weekly Report - Week of {formatDate(report.weekStartDate)}
          </h1>
          <div className="mt-2 space-y-1">
            <p className="text-gray-600">
              <span className="font-medium">Submitted by:</span> {report.employeeDisplayName || report.employeeUsername || 'Unknown'}
            </p>
            {report.supervisorDisplayName || report.supervisorUsername ? (
              <p className="text-gray-600">
                <span className="font-medium">Assigned Supervisor:</span> {report.supervisorDisplayName || report.supervisorUsername}
              </p>
            ) : (
              <p className="text-gray-500 italic">No supervisor assigned</p>
            )}
          </div>
        </div>
        <div className="flex items-center space-x-3">
          <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(report.status)}`}>
            {report.status}
          </span>
          <Link
            to="/reports"
            className="text-gray-600 hover:text-gray-900"
          >
            ← Back to Reports
          </Link>
        </div>
      </div>

      {/* Report Content */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="p-6 space-y-6">
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-3">Accomplishments</h2>
            <div className="prose max-w-none">
              <p className="text-gray-700 whitespace-pre-wrap">{report.accomplishments}</p>
            </div>
          </div>

          {report.challenges && (
            <div>
              <h2 className="text-lg font-semibold text-gray-900 mb-3">Challenges</h2>
              <div className="prose max-w-none">
                <p className="text-gray-700 whitespace-pre-wrap">{report.challenges}</p>
              </div>
            </div>
          )}

          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-3">Next Week Goals</h2>
            <div className="prose max-w-none">
              <p className="text-gray-700 whitespace-pre-wrap">{report.nextWeekGoals}</p>
            </div>
          </div>

          {report.additionalNotes && (
            <div>
              <h2 className="text-lg font-semibold text-gray-900 mb-3">Additional Notes</h2>
              <div className="prose max-w-none">
                <p className="text-gray-700 whitespace-pre-wrap">{report.additionalNotes}</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Report Metadata */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Report Details</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-600">Created</p>
              <p className="text-sm font-medium text-gray-900">{formatDate(report.createdAt)}</p>
            </div>
            {report.submittedAt && (
              <div>
                <p className="text-sm text-gray-600">Submitted</p>
                <p className="text-sm font-medium text-gray-900">{formatDate(report.submittedAt)}</p>
              </div>
            )}
            {report.approvedAt && (
              <div>
                <p className="text-sm text-gray-600">Approved</p>
                <p className="text-sm font-medium text-gray-900">{formatDate(report.approvedAt)}</p>
              </div>
            )}
            {report.supervisorFeedback && (
              <div className="md:col-span-2">
                <p className="text-sm text-gray-600">Supervisor Feedback</p>
                <p className="text-sm text-gray-700 whitespace-pre-wrap">{report.supervisorFeedback}</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Attachments */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Attachments</h2>
            {isEmployee() && (
              <label className="px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors cursor-pointer">
                {uploading ? 'Uploading...' : 'Upload File'}
                <input type="file" className="hidden" onChange={handleUpload} disabled={uploading} />
              </label>
            )}
          </div>
          {attachments.length === 0 ? (
            <p className="text-sm text-gray-500">No attachments</p>
          ) : (
            <ul className="divide-y divide-gray-200">
              {attachments.map(att => (
                <li key={att.id} className="py-2 flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-900">{att.originalName}</p>
                    <p className="text-xs text-gray-500">{att.mimeType} • {Math.round((att.sizeBytes || 0)/1024)} KB</p>
                  </div>
                  <div className="flex items-center space-x-3">
                    <button
                      onClick={() => handleDownload(att.id, att.originalName)}
                      className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-sm"
                    >
                      Download
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Actions */}
      {isEmployee() && report.status === 'DRAFT' && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Actions</h2>
          <div className="flex space-x-4">
            <Link
              to={`/reports/${id}/edit`}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              Edit Report
            </Link>
            <button
              onClick={handleSubmit}
              disabled={actionLoading}
              className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {actionLoading ? 'Submitting...' : 'Submit Report'}
            </button>
            <button
              onClick={async () => { try { await reportsAPI.deleteReport(id); navigate('/reports'); } catch (e) { setError(e.response?.data?.error || 'Failed to delete'); } }}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
            >
              Delete
            </button>
          </div>
        </div>
      )}

      {(isSupervisor() || isAdmin()) && report.status === 'SUBMITTED' && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Review Actions</h2>
          <div className="space-y-4">
            <div>
              <label htmlFor="feedback" className="block text-sm font-medium text-gray-700 mb-2">
                Feedback (Optional)
              </label>
              <textarea
                id="feedback"
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Add feedback for the employee..."
              />
            </div>
            <div className="flex space-x-4">
              <button
                onClick={handleApprove}
                disabled={actionLoading}
                className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {actionLoading ? 'Approving...' : 'Approve Report'}
              </button>
              <button
                onClick={handleReject}
                disabled={actionLoading}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {actionLoading ? 'Rejecting...' : 'Reject Report'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Admin/Supervisor delete button (Draft only for supervisor, any for admin) */}
      {(isAdmin() || (isSupervisor() && report.status === 'DRAFT')) && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex justify-end">
            <button
              onClick={async () => { try { await reportsAPI.deleteReport(id); navigate('/reports'); } catch (e) { setError(e.response?.data?.error || 'Failed to delete'); } }}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
            >
              Delete Report
            </button>
          </div>
        </div>
      )}

      {/* Admin Status Override */}
      {isAdmin() && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Admin Status Override</h2>
          <div className="space-y-4">
            <div>
              <label htmlFor="adminFeedback" className="block text-sm font-medium text-gray-700 mb-2">
                Admin Feedback (Optional)
              </label>
              <textarea
                id="adminFeedback"
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Add admin feedback..."
              />
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              <button
                onClick={() => handleStatusOverride('DRAFT')}
                disabled={actionLoading}
                className="px-3 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm"
              >
                Set to Draft
              </button>
              <button
                onClick={() => handleStatusOverride('SUBMITTED')}
                disabled={actionLoading}
                className="px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm"
              >
                Set to Submitted
              </button>
              <button
                onClick={() => handleStatusOverride('APPROVED')}
                disabled={actionLoading}
                className="px-3 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm"
              >
                Set to Approved
              </button>
              <button
                onClick={() => handleStatusOverride('REJECTED')}
                disabled={actionLoading}
                className="px-3 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm"
              >
                Set to Rejected
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportDetail;
