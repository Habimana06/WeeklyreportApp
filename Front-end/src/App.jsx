import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Layout from './components/Layout/Layout';
import ErrorBoundary from './components/ErrorBoundary';

// Auth Pages
import Login from './pages/auth/Login';
// import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import VerifyCode from './pages/auth/VerifyCode';

// Dashboard Pages
import EmployeeDashboard from './pages/dashboard/EmployeeDashboard';
import SupervisorDashboard from './pages/dashboard/SupervisorDashboard';
import AdminDashboard from './pages/dashboard/AdminDashboard';

// Report Pages
import ReportList from './pages/reports/ReportList';
import CurrentWeek from './pages/reports/CurrentWeek';
import CreateReport from './pages/reports/CreateReport';
import ReportDetail from './pages/reports/ReportDetail';

// Admin Pages
import UserManagement from './pages/admin/UserManagement';
import AssignSupervisor from './pages/admin/AssignSupervisor';
import Inbox from './pages/notifications/Inbox';

// Protected Route Component
const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, hasRole, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Layout>{children}</Layout>;
};

// Role-based Dashboard Redirect
const DashboardRedirect = () => {
  const { isAdmin, isSupervisor, isEmployee } = useAuth();

  if (isAdmin()) {
    return <Navigate to="/admin/dashboard" replace />;
  } else if (isSupervisor()) {
    return <Navigate to="/supervisor/dashboard" replace />;
  } else if (isEmployee()) {
    return <Navigate to="/employee/dashboard" replace />;
  }

  return <Navigate to="/login" replace />;
};

// Unauthorized Page
const Unauthorized = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="text-center">
      <h1 className="text-4xl font-bold text-gray-900 mb-4">403</h1>
      <p className="text-gray-600 mb-8">You don't have permission to access this page.</p>
      <button
        onClick={() => window.history.back()}
        className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
      >
        Go Back
      </button>
    </div>
  </div>
);

function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <Router>
          <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/verify" element={<VerifyCode />} />
          {/* Registration disabled; admins create users */}
          {/* <Route path="/register" element={<Register />} /> */}
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Protected Routes */}
          <Route path="/" element={<DashboardRedirect />} />
          <Route path="/dashboard" element={<DashboardRedirect />} />

          {/* Employee Routes */}
          <Route
            path="/employee/dashboard"
            element={
              <ProtectedRoute requiredRole="EMPLOYEE">
                <EmployeeDashboard />
              </ProtectedRoute>
            }
          />

          {/* Supervisor Routes */}
          <Route
            path="/supervisor/dashboard"
            element={
              <ProtectedRoute requiredRole="SUPERVISOR">
                <SupervisorDashboard />
              </ProtectedRoute>
            }
          />

          {/* Admin Routes */}
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/inbox"
            element={
              <ProtectedRoute>
                <Inbox />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <UserManagement />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/assign-supervisor"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AssignSupervisor />
              </ProtectedRoute>
            }
          />

          {/* Report Routes */}
          <Route
            path="/reports"
            element={
              <ProtectedRoute>
                <ReportList />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports/current-week"
            element={
              <ProtectedRoute>
                <CurrentWeek />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports/create"
            element={
              <ProtectedRoute requiredRole="EMPLOYEE">
                <CreateReport />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reports/:id"
            element={
              <ProtectedRoute>
                <ReportDetail />
              </ProtectedRoute>
            }
          />

          {/* Catch all route */}
          <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
