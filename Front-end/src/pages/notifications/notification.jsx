import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    // Do not attempt token refresh on auth endpoints
    if (originalRequest?.url && (originalRequest.url.includes('/auth/login') || originalRequest.url.includes('/auth/register'))) {
      return Promise.reject(error);
    }
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshResponse = await api.post('/auth/refresh');
        const { token } = refreshResponse.data;
        localStorage.setItem('token', token);
        
        // Retry original request with new token
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  refresh: () => api.post('/auth/refresh'),
};

// Reports API
export const reportsAPI = {
  getReports: (params = {}) => api.get('/reports', { params }),
  getReportById: (id) => api.get(`/reports/${id}`),
  createReport: (reportData) => api.post('/reports', reportData),
  updateReport: (id, reportData) => api.put(`/reports/${id}`, reportData),
  deleteReport: (id) => api.delete(`/reports/${id}`),
  submitReport: (id) => api.post(`/reports/${id}/submit`),
  approveReport: (id, feedback) => api.post(`/reports/${id}/approve`, null, { 
    params: { feedback } 
  }),
  rejectReport: (id, feedback) => api.post(`/reports/${id}/reject`, null, { 
    params: { feedback } 
  }),
  getCurrentWeekReports: () => api.get('/reports/current-week'),
  // Admin status override
  setReportStatus: (id, status, feedback) => api.post(`/reports/${id}/set-status`, null, {
    params: { status, feedback }
  }),
  // Attachments
  listAttachments: (id) => api.get(`/reports/${id}/attachments`),
  uploadAttachment: (id, file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post(`/reports/${id}/attachments`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  downloadAttachment: (id, attachmentId) => api.get(`/reports/${id}/attachments/${attachmentId}`, { responseType: 'blob' }),
  deleteAttachment: (id, attachmentId) => api.delete(`/reports/${id}/attachments/${attachmentId}`),
};

// Admin API
export const adminAPI = {
  getUsers: () => api.get('/admin/users'),
  getUserById: (id) => api.get(`/admin/users/${id}`),
  createUser: (userData) => api.post('/admin/users', userData),
  updateUser: (id, userData) => api.put(`/admin/users/${id}`, userData),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
  enableUser: (id) => api.post(`/admin/users/${id}/enable`),
  disableUser: (id) => api.post(`/admin/users/${id}/disable`),
  getUsersByRole: (role) => api.get(`/admin/users/role/${role}`),
  getDashboard: () => api.get('/admin/dashboard'),
  assignSupervisor: (data) => api.post('/admin/assign-supervisor', data),
};

// Notifications API
export const notificationsAPI = {
  listMine: () => api.get('/notifications'),
  listThread: (id) => api.get(`/notifications/threads/${id}`),
  send: (recipientId, subject, body, reportId = null, parentId = null) => 
    api.post('/notifications', null, {
      params: { recipientId, subject, body, reportId, parentId }
    }),
  markRead: (id, read = true) => api.post(`/notifications/${id}/read`, null, { params: { read } }),
};

export default api;