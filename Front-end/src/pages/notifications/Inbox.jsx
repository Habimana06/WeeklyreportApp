import React, { useEffect, useState } from 'react';
import { notificationsAPI, adminAPI, userAPI } from '../../services/api';
import { useAuth } from '../../contexts/AuthContext';

const Inbox = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [showComposeModal, setShowComposeModal] = useState(false);
  const [showReplyModal, setShowReplyModal] = useState(false);
  const [showMessageView, setShowMessageView] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [composeForm, setComposeForm] = useState({
    recipientId: '',
    subject: '',
    body: '',
    reportId: ''
  });
  const [replyForm, setReplyForm] = useState({
    body: ''
  });
  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [composeLoading, setComposeLoading] = useState(false);
  const [replyLoading, setReplyLoading] = useState(false);
  const { user, isAdmin, isSupervisor, isEmployee } = useAuth();

  useEffect(() => {
    loadNotifications();
    loadUsers();
    
    // Debug: Log user info on component mount
    console.log('=== DEBUG: Inbox component mounted');
    console.log('=== DEBUG: User object:', user);
    console.log('=== DEBUG: User ID:', user?.id);
    console.log('=== DEBUG: User role:', user?.role);
    console.log('=== DEBUG: Is authenticated:', !!user);
  }, [user]);

  const loadNotifications = async () => {
    try {
      console.log('=== DEBUG: Loading notifications for user:', user?.id);
        const res = await notificationsAPI.listMine();
      console.log('=== DEBUG: Notifications response:', res);
        setItems(Array.isArray(res.data) ? res.data : []);
      console.log('=== DEBUG: Set notifications count:', res.data?.length || 0);
      } catch (e) {
      console.error('=== DEBUG: Failed to load notifications:', e);
        setError(e.response?.data?.error || 'Failed to load notifications');
      } finally {
        setLoading(false);
      }
    };

  const loadUsers = async () => {
    setUsersLoading(true);
    try {
      let userList = [];
      
      if (isAdmin()) {
        // Admin can see all users
        try {
          const res = await adminAPI.getUsers();
          userList = Array.isArray(res.data) ? res.data : [];
          console.log('Admin users loaded:', userList.length);
        } catch (e) {
          console.error('Failed to load admin users:', e);
        }
        // If empty or failed, fallback to role-based (admin endpoints)
        if (!Array.isArray(userList) || userList.length === 0) {
          try {
            const [adminsRes, supervisorsRes, employeesRes] = await Promise.all([
              adminAPI.getUsersByRole('ADMIN'),
              adminAPI.getUsersByRole('SUPERVISOR'),
              adminAPI.getUsersByRole('EMPLOYEE')
            ]);
            const admins = Array.isArray(adminsRes.data) ? adminsRes.data : [];
            const supervisors = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
            const employees = Array.isArray(employeesRes.data) ? employeesRes.data : [];
            userList = [...admins, ...supervisors, ...employees];
          } catch (e2) {
            console.error('Admin role-based load failed:', e2);
          }
        }
        // If still empty, fallback to public role-based (same as supervisor approach)
        if (!Array.isArray(userList) || userList.length === 0) {
          try {
            const [supervisorsRes, employeesRes] = await Promise.all([
              userAPI.getUsersByRole('SUPERVISOR'),
              userAPI.getUsersByRole('EMPLOYEE')
            ]);
            const supervisors = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
            const employees = Array.isArray(employeesRes.data) ? employeesRes.data : [];
            userList = [...supervisors, ...employees];
          } catch (e3) {
            console.error('Public role-based load failed for admin:', e3);
          }
        }
      } else if (isSupervisor()) {
        // Supervisor can see assigned employees and other supervisors
        try {
          if (user?.id) {
            const [assignedEmployeesRes, supervisorsRes] = await Promise.all([
              adminAPI.getAssignedEmployees(user.id), // Get only assigned employees
              userAPI.getUsersByRole('SUPERVISOR') // Use public endpoint
            ]);
            
            const assignedEmployees = Array.isArray(assignedEmployeesRes.data) ? assignedEmployeesRes.data : [];
            const supervisors = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
            
            userList = [...assignedEmployees, ...supervisors];
            console.log('Supervisor users loaded:', userList.length, 'assigned employees:', assignedEmployees.length);
          } else {
            console.error('User ID is undefined, cannot load assigned employees');
            // Fallback to public endpoints
            const [employeesRes, supervisorsRes] = await Promise.all([
              userAPI.getUsersByRole('EMPLOYEE'),
              userAPI.getUsersByRole('SUPERVISOR')
            ]);
            
            const employees = Array.isArray(employeesRes.data) ? employeesRes.data : [];
            const supervisors = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
            userList = [...employees, ...supervisors];
          }
        } catch (e) {
          console.error('Failed to load supervisor users:', e);
          // Fallback to public endpoints
          try {
            const [employeesRes, supervisorsRes] = await Promise.all([
              userAPI.getUsersByRole('EMPLOYEE'),
              userAPI.getUsersByRole('SUPERVISOR')
            ]);
            
            const employees = Array.isArray(employeesRes.data) ? employeesRes.data : [];
            const supervisors = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
            userList = [...employees, ...supervisors];
          } catch (fallbackError) {
            console.error('Fallback also failed:', fallbackError);
            userList = [];
          }
        }
      } else if (isEmployee()) {
        // Employee can see their supervisor and other employees in same department
        try {
          // For employees, we'll show supervisors and other employees
          // In a real system, you might want to show only employees in the same department
          const [supervisorsRes, employeesRes] = await Promise.all([
            userAPI.getUsersByRole('SUPERVISOR'), // Use public endpoint
            userAPI.getUsersByRole('EMPLOYEE')   // Use public endpoint
          ]);
          
          const supervisors = Array.isArray(supervisorsRes.data) ? supervisorsRes.data : [];
          const employees = Array.isArray(employeesRes.data) ? employeesRes.data : [];
          
          userList = [...supervisors, ...employees];
          console.log('Employee users loaded:', userList.length);
        } catch (e) {
          console.error('Failed to load employee users:', e);
          userList = [];
        }
      }
      
      console.log('Final user list:', userList.length, 'users');
      setUsers(userList);
    } catch (e) {
      console.error('Failed to load users for compose:', e);
      setUsers([]);
    } finally {
      setUsersLoading(false);
    }
  };

  const handleComposeSubmit = async (e) => {
    e.preventDefault();
    setComposeLoading(true);
    setError(''); // Clear any previous errors
    
    try {
      const response = await notificationsAPI.send({
        recipientId: composeForm.recipientId,
        subject: composeForm.subject,
        body: composeForm.body,
        reportId: composeForm.reportId || null
      });
      
      console.log('Message sent successfully:', response);
      
      // Close modal and reset form
      setShowComposeModal(false);
      setComposeForm({ recipientId: '', subject: '', body: '', reportId: '' });
      
      // Reload notifications to show the new message
      await loadNotifications();
      
      // Show success message
      setSuccessMessage('Message sent successfully!');
      setError(''); // Clear any errors
      
      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (e) {
      console.error('Failed to send message:', e);
      setError(e.response?.data?.error || 'Failed to send message. Please try again.');
    } finally {
      setComposeLoading(false);
    }
  };

  const handleReplySubmit = async (e) => {
    e.preventDefault();
    setReplyLoading(true);
    try {
      await notificationsAPI.send({
        recipientId: selectedNotification.senderId,
        subject: `Re: ${selectedNotification.subject}`,
        body: replyForm.body,
        reportId: selectedNotification.reportId,
        parentId: selectedNotification.id
      });
      setShowReplyModal(false);
      setReplyForm({ body: '' });
      setSelectedNotification(null);
      await loadNotifications();
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to send reply');
    } finally {
      setReplyLoading(false);
    }
  };

  const handleMarkRead = async (notificationId, read = true) => {
    try {
      await notificationsAPI.markRead(notificationId, read);
      await loadNotifications();
    } catch {
      // Silent fail for mark read
    }
  };

  const openReplyModal = (notification) => {
    setSelectedNotification(notification);
    setReplyForm({ body: '' });
    setShowReplyModal(true);
  };

  const openMessageView = (notification) => {
    setSelectedNotification(notification);
    setShowMessageView(true);
    // Mark as read when opening
    if (!notification.read) {
      handleMarkRead(notification.id, true);
    }
  };

  const openComposeModal = () => {
    if (!user?.id) {
      setError('User ID not available. Please refresh the page and try again.');
      return;
    }
    setComposeForm({ recipientId: '', subject: '', body: '', reportId: '' });
    setShowComposeModal(true);
  };

  const canComposeMessage = () => {
    return (isAdmin() || isSupervisor() || isEmployee()) && !!user?.id;
  };

  const getRecipientLabel = () => {
    if (isAdmin()) return "Select recipient";
    if (isSupervisor()) return "Select employee or supervisor";
    if (isEmployee()) return "Select supervisor or employee";
    return "Select recipient";
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex justify-between items-center">
      <h1 className="text-3xl font-bold text-gray-900">Notifications</h1>
        {canComposeMessage() && (
          <button
            onClick={openComposeModal}
            className="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors text-lg font-medium"
          >
            Compose Message
          </button>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md text-base">{error}</div>
      )}
      
      {successMessage && (
        <div className="bg-green-50 border border-green-200 text-green-600 px-4 py-3 rounded-md text-base">
          {successMessage}
        </div>
      )}

      {items.length === 0 ? (
        <p className="text-base text-gray-600">No notifications</p>
      ) : (
        <ul className="divide-y divide-gray-200 bg-white rounded-lg border border-gray-200">
          {items.map(n => (
            <li key={n.id} className="p-6 flex items-start justify-between hover:bg-gray-50 cursor-pointer" onClick={() => openMessageView(n)}>
              <div className="flex-1">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-lg text-gray-900 font-medium">{n.subject}</p>
                    <p className="text-base text-gray-600">From {n.senderUsername} • {new Date(n.createdAt).toLocaleString()}</p>
                    {n.body && <p className="text-base text-gray-700 mt-2 line-clamp-2">{n.body}</p>}
                    {n.reportId && (
                      <p className="text-base text-blue-600 mt-2">
                        📄 Related to Report #{n.reportId}
                      </p>
                    )}
                  </div>
                  <div className="flex items-center space-x-3">
                    {!n.read && <span className="text-base px-3 py-1 bg-blue-100 text-blue-700 rounded-full">New</span>}
                    <button
                      onClick={(e) => { e.stopPropagation(); openReplyModal(n); }}
                      className="text-base px-3 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition-colors"
                    >
                      Reply
                    </button>
                  </div>
                </div>
                {!n.read && (
                  <button
                    onClick={(e) => { e.stopPropagation(); handleMarkRead(n.id, true); }}
                    className="text-base text-blue-600 hover:text-blue-800 mt-3"
                  >
                    Mark as read
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      

      {/* Compose Modal - For all roles */}
      {showComposeModal && canComposeMessage() && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-semibold mb-4">Compose Message</h2>
            <form onSubmit={handleComposeSubmit} className="space-y-4">
              <div>
                <label className="block text-base font-medium text-gray-700 mb-1">To:</label>
                {usersLoading ? (
                  <div className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500 text-base">
                    Loading recipients...
                  </div>
                ) : (
                  <select
                    value={composeForm.recipientId}
                    onChange={(e) => setComposeForm({...composeForm, recipientId: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                  >
                    <option value="">{getRecipientLabel()}</option>
                    {users.filter(u => u.id !== user?.id).map(u => (
                      <option key={u.id} value={u.id}>
                        {u.username} ({u.role})
                      </option>
                    ))}
                  </select>
                )}
                {users.length === 0 && !usersLoading && (
                  <p className="text-base text-red-500 mt-1">No recipients available. Please try refreshing the page.</p>
                )}
              </div>
              <div>
                <label className="block text-base font-medium text-gray-700 mb-1">Subject:</label>
                <input
                  type="text"
                  value={composeForm.subject}
                  onChange={(e) => setComposeForm({...composeForm, subject: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-base font-medium text-gray-700 mb-1">Message:</label>
                <textarea
                  value={composeForm.body}
                  onChange={(e) => setComposeForm({...composeForm, body: e.target.value})}
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setShowComposeModal(false)}
                  className="px-4 py-2 text-gray-600 hover:text-gray-800 text-base"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={composeLoading}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 text-base"
                >
                  {composeLoading ? 'Sending...' : 'Send'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reply Modal */}
      {showReplyModal && selectedNotification && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-semibold mb-4">Reply to {selectedNotification.senderUsername}</h2>
            <div className="mb-4 p-3 bg-gray-50 rounded-md">
              <p className="text-base text-gray-600">{selectedNotification.subject}</p>
              <p className="text-sm text-gray-500 mt-1">{selectedNotification.body}</p>
            </div>
            <form onSubmit={handleReplySubmit} className="space-y-4">
              <div>
                <label className="block text-base font-medium text-gray-700 mb-1">Your Reply:</label>
                <textarea
                  value={replyForm.body}
                  onChange={(e) => setReplyForm({...replyForm, body: e.target.value})}
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div className="flex justify-end space-x-3">
                <button
                  type="button"
                  onClick={() => setShowReplyModal(false)}
                  className="px-4 py-2 text-gray-600 hover:text-gray-800 text-base"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={replyLoading}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 text-base"
                >
                  {replyLoading ? 'Sending...' : 'Send Reply'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Message View Modal */}
      {showMessageView && selectedNotification && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 w-full max-w-4xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start mb-6">
              <h2 className="text-2xl font-bold text-gray-900">{selectedNotification.subject}</h2>
              <button
                onClick={() => setShowMessageView(false)}
                className="text-gray-400 hover:text-gray-600 text-2xl"
              >
                ×
              </button>
            </div>
            
            <div className="space-y-4 mb-8">
              <div className="flex items-center space-x-4 text-base text-gray-600">
                <span><strong>From:</strong> {selectedNotification.senderUsername}</span>
                <span><strong>Date:</strong> {new Date(selectedNotification.createdAt).toLocaleString()}</span>
                {selectedNotification.reportId && (
                  <span className="text-blue-600">
                    📄 Related to Report #{selectedNotification.reportId}
                  </span>
                )}
              </div>
              
              {selectedNotification.body && (
                <div className="bg-gray-50 p-6 rounded-lg">
                  <p className="text-lg text-gray-800 leading-relaxed whitespace-pre-wrap">{selectedNotification.body}</p>
                </div>
              )}
            </div>
            
            <div className="flex justify-between items-center pt-6 border-t border-gray-200">
              <div className="flex space-x-3">
                <button
                  onClick={() => {
                    setShowMessageView(false);
                    openReplyModal(selectedNotification);
                  }}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-lg font-medium"
                >
                  Reply
                </button>
                {!selectedNotification.read && (
                  <button
                    onClick={() => {
                      handleMarkRead(selectedNotification.id, true);
                      setShowMessageView(false);
                    }}
                    className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-lg font-medium"
                  >
                    Mark as Read
                  </button>
                )}
              </div>
              <button
                onClick={() => setShowMessageView(false)}
                className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors text-lg font-medium"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Inbox;


