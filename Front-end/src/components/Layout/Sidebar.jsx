import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const Sidebar = ({ collapsed = false }) => {
  const { isAdmin, isSupervisor, isEmployee } = useAuth();
  const location = useLocation();

  const isActive = (path) => {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  const getDashboardLink = () => {
    if (isAdmin()) return '/admin/dashboard';
    if (isSupervisor()) return '/supervisor/dashboard';
    if (isEmployee()) return '/employee/dashboard';
    return '/dashboard';
  };

  const itemBase = 'flex items-center space-x-3 px-4 py-3 rounded-xl text-base font-medium transition-all duration-200 hover-lift tilt-container';
  const activeCls = 'bg-blue-600 text-white shadow-md';
  const idleCls = 'text-gray-700 hover:bg-blue-50';

  return (
    <aside className={`sidebar-gradient border-r border-gray-200 min-h-screen transition-all duration-300 ease-in-out ${collapsed ? 'w-20' : 'w-72'}`}>
      <div className="p-4">
        <nav className="space-y-2">
          {/* Dashboard */}
          <Link
            to={getDashboardLink()}
            className={`${itemBase} ${isActive(getDashboardLink()) ? activeCls : idleCls}`}
          >
            <span className="tilt-card flex items-center space-x-3">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2-2z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 5a2 2 0 012-2h4a2 2 0 012 2v6H8V5z" />
              </svg>
              {!collapsed && <span>Dashboard</span>}
            </span>
          </Link>

          {/* Employee Menu */}
          {isEmployee() && (
            <>
              <Link
                to="/reports"
                className={`${itemBase} ${isActive('/reports') ? activeCls : idleCls}`}
              >
                <span className="tilt-card flex items-center space-x-3">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  {!collapsed && <span>My Reports</span>}
                </span>
              </Link>
              
              <Link
                to="/reports/create"
                className={`${itemBase} ${isActive('/reports/create') ? activeCls : idleCls}`}
              >
                <span className="tilt-card flex items-center space-x-3">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  {!collapsed && <span>Create Report</span>}
                </span>
              </Link>
            </>
          )}

          {/* Supervisor/Admin Menu */}
          {(isSupervisor() || isAdmin()) && (
            <>
              <Link
                to="/reports"
                className={`${itemBase} ${isActive('/reports') ? activeCls : idleCls}`}
              >
                <span className="tilt-card flex items-center space-x-3">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                  </svg>
                  {!collapsed && <span>Reports</span>}
                </span>
              </Link>
              
              <Link
                to="/reports/current-week"
                className={`${itemBase} ${isActive('/reports/current-week') ? activeCls : idleCls}`}
              >
                <span className="tilt-card flex items-center space-x-3">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  {!collapsed && <span>Current Week</span>}
                </span>
              </Link>
            </>
          )}

          {/* Admin Menu */}
          {isAdmin() && (
            <>
              {!collapsed && <div className="border-t border-gray-200 my-4"></div>}
              {!collapsed && (
                <p className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  Administration
                </p>
              )}
              
              <Link
                to="/admin/users"
                className={`${itemBase} ${isActive('/admin/users') ? activeCls : idleCls}`}
              >
                <span className="tilt-card flex items-center space-x-3">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
                  </svg>
                  {!collapsed && <span>Manage Users</span>}
                </span>
              </Link>
              
              <Link
                to="/admin/assign-supervisor"
                className={`${itemBase} ${isActive('/admin/assign-supervisor') ? activeCls : idleCls}`}
              >
                <span className="tilt-card flex items-center space-x-3">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                  {!collapsed && <span>Assign Supervisor</span>}
                </span>
              </Link>
            </>
          )}
        </nav>
      </div>
    </aside>
  );
};

export default Sidebar;
