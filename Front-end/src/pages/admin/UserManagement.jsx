import React, { useState, useEffect } from 'react';
import { adminAPI, userAPI } from '../../services/api';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    role: 'EMPLOYEE',
    firstName: '',
    lastName: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setError('');
      // 1) Try admin endpoint
      let collected = [];
      try {
        const response = await adminAPI.getUsers();
        if (Array.isArray(response.data)) collected = response.data;
      } catch (e) {
        console.warn('Admin getUsers failed:', e?.response?.status || e.message);
      }

      // 2) If empty, try admin role-based endpoints and merge
      if (collected.length === 0) {
        try {
          const [adminsRes, supervisorsRes, employeesRes] = await Promise.all([
            adminAPI.getUsersByRole('ADMIN'),
            adminAPI.getUsersByRole('SUPERVISOR'),
            adminAPI.getUsersByRole('EMPLOYEE'),
          ]);
          const admins = Array.isArray(adminsRes.data) ? adminsRes.data : [];
          const sups = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
          const emps = Array.isArray(employeesRes.data) ? employeesRes.data : [];
          collected = [...admins, ...sups, ...emps];
        } catch (e2) {
          console.warn('Admin role-based users failed:', e2?.response?.status || e2.message);
        }
      }

      // 3) If still empty (e.g., admin endpoints 403), use public role endpoints (read-only)
      if (collected.length === 0) {
        try {
          const [supsPub, empsPub] = await Promise.all([
            userAPI.getUsersByRole('SUPERVISOR'),
            userAPI.getUsersByRole('EMPLOYEE'),
          ]);
          const sups = Array.isArray(supsPub.data) ? supsPub.data : [];
          const emps = Array.isArray(empsPub.data) ? empsPub.data : [];
          collected = [...sups, ...emps];
        } catch (e3) {
          console.warn('Public role users failed:', e3?.response?.status || e3.message);
        }
      }

      setUsers(collected);
      if (collected.length === 0) {
        setError('No users found. Create a user to get started.');
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      setError('Failed to fetch users: ' + (error.response?.data?.error || error.message));
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setFieldErrors({});

    try {
      await adminAPI.createUser(formData);
      setSuccess('User created successfully');
      setShowCreateForm(false);
      setFormData({ username: '', email: '', password: '', role: 'EMPLOYEE', firstName: '', lastName: '' });
      fetchUsers();
    } catch (error) {
      const data = error.response?.data;
      if (data?.validationErrors) {
        setFieldErrors(data.validationErrors);
        setError(data.message || 'Validation failed');
      } else {
        setError(data?.error || data?.message || 'Failed to create user');
      }
    }
  };

  const handleUpdateUser = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setFieldErrors({});

    try {
      await adminAPI.updateUser(editingUser.id, formData);
      setSuccess('User updated successfully');
      setEditingUser(null);
      setFormData({ username: '', email: '', password: '', role: 'EMPLOYEE', firstName: '', lastName: '' });
      fetchUsers();
    } catch (error) {
      const data = error.response?.data;
      if (data?.validationErrors) {
        setFieldErrors(data.validationErrors);
        setError(data.message || 'Validation failed');
      } else {
        setError(data?.error || data?.message || 'Failed to update user');
      }
    }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;

    try {
      await adminAPI.deleteUser(userId);
      setSuccess('User deleted successfully');
      fetchUsers();
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to delete user');
    }
  };

  const handleEnableUser = async (userId) => {
    try {
      await adminAPI.enableUser(userId);
      setSuccess('User enabled successfully');
      fetchUsers();
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to enable user');
    }
  };

  const handleDisableUser = async (userId) => {
    try {
      await adminAPI.disableUser(userId);
      setSuccess('User disabled successfully');
      fetchUsers();
    } catch (error) {
      setError(error.response?.data?.error || 'Failed to disable user');
    }
  };

  const openEditForm = (user) => {
    setEditingUser(user);
    setFormData({
      username: user.username,
      email: user.email,
      password: '',
      role: user.role,
      firstName: user.profile?.firstName || '',
      lastName: user.profile?.lastName || '',
    });
  };

  const closeForms = () => {
    setShowCreateForm(false);
    setEditingUser(null);
    setFormData({ username: '', email: '', password: '', role: 'EMPLOYEE', firstName: '', lastName: '' });
    setError('');
    setSuccess('');
  };

  const getRoleColor = (role) => {
    switch (role) {
      case 'ADMIN': return 'bg-purple-100 text-purple-800';
      case 'SUPERVISOR': return 'bg-green-100 text-green-800';
      case 'EMPLOYEE': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
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
          <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-600">Manage system users and their roles</p>
        </div>
        <button
          onClick={() => setShowCreateForm(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          Create User
        </button>
      </div>

      {/* Messages */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
          {error}
        </div>
      )}
      {success && (
        <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-md">
          {success}
        </div>
      )}

      {/* Create/Edit Form */}
      {(showCreateForm || editingUser) && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            {editingUser ? 'Edit User' : 'Create New User'}
          </h2>
          <form onSubmit={editingUser ? handleUpdateUser : handleCreateUser} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
                  Username
                </label>
                <input
                  type="text"
                  id="username"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                {fieldErrors.username && (<p className="mt-1 text-sm text-red-600">{fieldErrors.username}</p>)}
              </div>
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </label>
                <input
                  type="email"
                  id="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                {fieldErrors.email && (<p className="mt-1 text-sm text-red-600">{fieldErrors.email}</p>)}
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
                  First Name
                </label>
                <input
                  type="text"
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  required={!editingUser}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                {fieldErrors.firstName && (<p className="mt-1 text-sm text-red-600">{fieldErrors.firstName}</p>)}
              </div>
              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
                  Last Name
                </label>
                <input
                  type="text"
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  required={!editingUser}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                {fieldErrors.lastName && (<p className="mt-1 text-sm text-red-600">{fieldErrors.lastName}</p>)}
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                  Password {editingUser && '(leave blank to keep current)'}
                </label>
                <input
                  type="password"
                  id="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  required={!editingUser}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                {fieldErrors.password && (<p className="mt-1 text-sm text-red-600">{fieldErrors.password}</p>)}
              </div>
              <div>
                <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-1">
                  Role
                </label>
                <select
                  id="role"
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="EMPLOYEE">Employee</option>
                  <option value="SUPERVISOR">Supervisor</option>
                  <option value="ADMIN">Admin</option>
                </select>
                {fieldErrors.role && (<p className="mt-1 text-sm text-red-600">{fieldErrors.role}</p>)}
              </div>
            </div>
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={closeForms}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
              >
                {editingUser ? 'Update User' : 'Create User'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Users Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Username
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Role
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {Array.isArray(users) && users.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {user.username}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {user.email}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getRoleColor(user.role)}`}>
                      {user.role}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {user.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                    <button
                      onClick={() => openEditForm(user)}
                      className="text-blue-600 hover:text-blue-900"
                    >
                      Edit
                    </button>
                    {user.enabled ? (
                      <button
                        onClick={() => handleDisableUser(user.id)}
                        className="text-yellow-600 hover:text-yellow-900"
                      >
                        Disable
                      </button>
                    ) : (
                      <button
                        onClick={() => handleEnableUser(user.id)}
                        className="text-green-600 hover:text-green-900"
                      >
                        Enable
                      </button>
                    )}
                    <button
                      onClick={() => handleDeleteUser(user.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {Array.isArray(users) && users.length === 0 && (
          <div className="text-center py-8">
            <p className="text-gray-500">No users found</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserManagement;
