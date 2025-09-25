import React, { useState, useEffect } from 'react';
import { adminAPI } from '../../services/api';

const AssignSupervisor = () => {
  const [employees, setEmployees] = useState([]);
  const [supervisors, setSupervisors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [assigning, setAssigning] = useState(false);
  const [formData, setFormData] = useState({
    employeeId: '',
    supervisorId: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [employeesResponse, supervisorsResponse] = await Promise.all([
        adminAPI.getUsersByRole('EMPLOYEE'),
        adminAPI.getUsersByRole('SUPERVISOR')
      ]);
      
      setEmployees(Array.isArray(employeesResponse.data) ? employeesResponse.data : []);
      setSupervisors(Array.isArray(supervisorsResponse.data) ? supervisorsResponse.data : []);
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Failed to fetch employees and supervisors');
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setAssigning(true);
    setError('');
    setSuccess('');

    if (!formData.employeeId || !formData.supervisorId) {
      setError('Please select both employee and supervisor');
      setAssigning(false);
      return;
    }

    try {
      await adminAPI.assignSupervisor({
        employeeId: parseInt(formData.employeeId),
        supervisorId: parseInt(formData.supervisorId)
      });
      
      setSuccess('Supervisor assigned successfully!');
      setFormData({ employeeId: '', supervisorId: '' });
    } catch (error) {
      console.error('Error assigning supervisor:', error);
      setError(error.response?.data?.error || 'Failed to assign supervisor');
    } finally {
      setAssigning(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <h1 className="text-2xl font-bold text-gray-900">Assign Supervisor</h1>
            <p className="mt-1 text-sm text-gray-600">
              Assign supervisors to employees to manage their weekly reports.
            </p>
          </div>

          <div className="p-6">
            {error && (
              <div className="mb-6 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
                {error}
              </div>
            )}
            
            {success && (
              <div className="mb-6 bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-md">
                {success}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="employeeId" className="block text-sm font-medium text-gray-700 mb-2">
                    Select Employee
                  </label>
                  <select
                    id="employeeId"
                    name="employeeId"
                    value={formData.employeeId}
                    onChange={handleChange}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    required
                  >
                    <option value="">Choose an employee...</option>
                    {employees.map((employee) => (
                      <option key={employee.id} value={employee.id}>
                        {employee.username} - {employee.email}
                      </option>
                    ))}
                  </select>
                  {employees.length === 0 && (
                    <p className="mt-1 text-sm text-gray-500">No employees found</p>
                  )}
                </div>

                <div>
                  <label htmlFor="supervisorId" className="block text-sm font-medium text-gray-700 mb-2">
                    Select Supervisor
                  </label>
                  <select
                    id="supervisorId"
                    name="supervisorId"
                    value={formData.supervisorId}
                    onChange={handleChange}
                    className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    required
                  >
                    <option value="">Choose a supervisor...</option>
                    {supervisors.map((supervisor) => (
                      <option key={supervisor.id} value={supervisor.id}>
                        {supervisor.username} - {supervisor.email}
                      </option>
                    ))}
                  </select>
                  {supervisors.length === 0 && (
                    <p className="mt-1 text-sm text-gray-500">No supervisors found</p>
                  )}
                </div>
              </div>

              <div className="flex justify-end">
                <button
                  type="submit"
                  disabled={assigning || employees.length === 0 || supervisors.length === 0}
                  className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {assigning ? 'Assigning...' : 'Assign Supervisor'}
                </button>
              </div>
            </form>

            {/* Current Assignments Display */}
            <div className="mt-8">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Current Assignments</h3>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600">
                  This feature will display current supervisor-employee assignments once the backend implementation is complete.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AssignSupervisor;
