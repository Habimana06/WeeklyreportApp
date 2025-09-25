import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { reportsAPI } from '../../services/api';
import WeekPicker from '../../components/WeekPicker';

const EditReport = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    weekStartDate: '',
    weekEndDate: '',
    accomplishments: '',
    challenges: '',
    nextWeekGoals: ''
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    fetchReport();
  }, [id]);

  const fetchReport = async () => {
    try {
      const response = await reportsAPI.getReportById(id);
      const report = response.data;
      
      if (report.status !== 'DRAFT') {
        setError('Only draft reports can be edited');
        return;
      }
      
      setFormData({
        weekStartDate: report.weekStartDate ? report.weekStartDate.split('T')[0] : '',
        weekEndDate: report.weekEndDate ? report.weekEndDate.split('T')[0] : '',
        accomplishments: report.accomplishedTasks || '',
        challenges: report.challengesFaced || '',
        nextWeekGoals: report.nextWeekPlans || ''
      });
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to fetch report');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const onWeekChange = ({ start, end }) => {
    setFormData(prev => ({ ...prev, weekStartDate: start, weekEndDate: end }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});

    try {
      const payload = {
        weekStartDate: formData.weekStartDate,
        weekEndDate: formData.weekEndDate,
        accomplishedTasks: formData.accomplishments,
        challengesFaced: formData.challenges || undefined,
        nextWeekPlans: formData.nextWeekGoals || undefined
      };

      await reportsAPI.updateReport(id, payload);
      navigate(`/reports/${id}`);
    } catch (error) {
      const data = error.response?.data;
      if (data?.validationErrors) {
        setFieldErrors(data.validationErrors);
        setError(data.message || 'Validation failed');
      } else {
        setError(data?.error || data?.message || 'Failed to update report');
      }
    } finally {
      setSaving(false);
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
    <div className="max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Edit Weekly Report</h1>
        <p className="text-gray-600">Update your weekly accomplishments and goals</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
            {error}
          </div>
        )}

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Select Week (Friday to Thursday)
            </label>
            <WeekPicker
              valueStart={formData.weekStartDate}
              valueEnd={formData.weekEndDate}
              onChange={onWeekChange}
              minDate={new Date()}
            />
            <div className="grid grid-cols-2 gap-4 mt-4">
              <div>
                <label className="block text-xs text-gray-600 mb-1">Week Start (Friday)</label>
                <input
                  type="date"
                  name="weekStartDate"
                  value={formData.weekStartDate}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 focus:outline-none"
                  readOnly
                />
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">Week End (Thursday)</label>
                <input
                  type="date"
                  name="weekEndDate"
                  value={formData.weekEndDate}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 focus:outline-none"
                  readOnly
                />
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Accomplishments</h2>
          <textarea
            name="accomplishments"
            value={formData.accomplishments}
            onChange={handleChange}
            required
            minLength={10}
            rows={6}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Describe what you accomplished this week (min 10 characters)..."
          />
          {fieldErrors.accomplishments && (
            <p className="mt-2 text-sm text-red-600">{fieldErrors.accomplishments}</p>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Challenges Faced</h2>
          <textarea
            name="challenges"
            value={formData.challenges}
            onChange={handleChange}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Describe any challenges you faced this week..."
          />
          {fieldErrors.challenges && (
            <p className="mt-2 text-sm text-red-600">{fieldErrors.challenges}</p>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Next Week Goals</h2>
          <textarea
            name="nextWeekGoals"
            value={formData.nextWeekGoals}
            onChange={handleChange}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="What are your goals for next week?"
          />
          {fieldErrors.nextWeekGoals && (
            <p className="mt-2 text-sm text-red-600">{fieldErrors.nextWeekGoals}</p>
          )}
        </div>

        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate(`/reports/${id}`)}
            className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default EditReport;