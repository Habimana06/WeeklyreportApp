import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { reportsAPI } from '../../services/api';
import WeekPicker from '../../components/WeekPicker';

const CreateReport = () => {
  const [formData, setFormData] = useState({
    weekStartDate: '',
    weekEndDate: '',
    accomplishedTasks: '',
    challengesFaced: '',
    nextWeekPlans: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  const navigate = useNavigate();

  React.useEffect(() => {
    const today = new Date();
    const day = today.getDay();
    const diffToFriday = (5 - day + 7) % 7;
    const friday = new Date(today);
    friday.setDate(today.getDate() + diffToFriday);
    const thursday = new Date(friday);
    thursday.setDate(friday.getDate() + 6);
    const toYmdLocal = (d) => {
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const dd = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${dd}`;
    };
    setFormData(prev => ({ ...prev, weekStartDate: toYmdLocal(friday), weekEndDate: toYmdLocal(thursday) }));
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setFieldErrors({});

    try {
      const payload = {
        weekStartDate: formData.weekStartDate,
        weekEndDate: formData.weekEndDate,
        accomplishedTasks: formData.accomplishedTasks,
        challengesFaced: formData.challengesFaced || undefined,
        nextWeekPlans: formData.nextWeekPlans || undefined
      };

      await reportsAPI.createReport(payload);
      navigate('/reports');
    } catch (error) {
      const data = error.response?.data;
      if (data?.validationErrors) {
        setFieldErrors(data.validationErrors);
        setError(data.message || 'Validation failed');
      } else {
        setError(data?.error || data?.message || 'Failed to create report');
      }
    } finally {
      setLoading(false);
    }
  };

  const onWeekChange = ({ start, end }) => {
    setFormData(prev => ({ ...prev, weekStartDate: start, weekEndDate: end }));
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Create Weekly Report</h1>
        <p className="text-gray-600">Week runs Friday to Thursday. Choose Friday only.</p>
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
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Accomplished Tasks</h2>
          <textarea
            name="accomplishedTasks"
            value={formData.accomplishedTasks}
            onChange={handleChange}
            required
            minLength={10}
            rows={6}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Describe what you accomplished this week (min 10 characters)..."
          />
          {fieldErrors.accomplishedTasks && (
            <p className="mt-2 text-sm text-red-600">{fieldErrors.accomplishedTasks}</p>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Challenges Faced</h2>
          <textarea
            name="challengesFaced"
            value={formData.challengesFaced}
            onChange={handleChange}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Describe any challenges you faced this week..."
          />
          {fieldErrors.challengesFaced && (
            <p className="mt-2 text-sm text-red-600">{fieldErrors.challengesFaced}</p>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Next Week Plans</h2>
          <textarea
            name="nextWeekPlans"
            value={formData.nextWeekPlans}
            onChange={handleChange}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="What are your goals for next week?"
          />
          {fieldErrors.nextWeekPlans && (
            <p className="mt-2 text-sm text-red-600">{fieldErrors.nextWeekPlans}</p>
          )}
        </div>

        <div className="flex justify-end space-x-4">
          <button
            type="button"
            onClick={() => navigate('/reports')}
            className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Creating...' : 'Create Report'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateReport;
