import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { notificationsAPI } from '../../services/api';

const Header = ({ onToggleSidebar }) => {
  const { user, logout, isAdmin, isSupervisor, isEmployee } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    loadUnreadCount();
    const interval = setInterval(loadUnreadCount, 30000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    // Initialize theme per user; migrate any legacy global key
    try {
      const key = user?.id ? `theme_${user.id}` : 'theme_default';
      // Migrate legacy 'theme' -> per-user key once
      if (user?.id) {
        const legacy = localStorage.getItem('theme');
        const existing = localStorage.getItem(key);
        if (!existing && legacy) {
          localStorage.setItem(key, legacy);
          localStorage.removeItem('theme');
        }
      }
      const stored = localStorage.getItem(key);
      const dark = stored === 'dark';
      setIsDark(dark);
      updateBodyThemeClass(dark);
    } catch {}
  }, [user?.id]);

  const updateBodyThemeClass = (dark) => {
    const body = document.body;
    if (!body) return;
    if (dark) body.classList.add('theme-dark');
    else body.classList.remove('theme-dark');
  };

  const toggleTheme = () => {
    const next = !isDark;
    setIsDark(next);
    updateBodyThemeClass(next);
    try {
      const key = user?.id ? `theme_${user.id}` : 'theme_default';
      localStorage.setItem(key, next ? 'dark' : 'light');
    } catch {}
  };

  const loadUnreadCount = async () => {
    try {
      const res = await notificationsAPI.listMine();
      const notifications = Array.isArray(res.data) ? res.data : [];
      const unread = notifications.filter(n => !n.read).length;
      setUnreadCount(unread);
    } catch {
      // Silent fail for unread count
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getDashboardLink = () => {
    if (isAdmin()) return '/admin/dashboard';
    if (isSupervisor()) return '/supervisor/dashboard';
    if (isEmployee()) return '/employee/dashboard';
    return '/dashboard';
  };

  return (
    <header className="header-gradient border-b border-gray-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-20">
          {/* Left: Sidebar Toggle + Logo */}
          <div className="flex items-center space-x-3">
            <button
              aria-label="Toggle sidebar"
              onClick={onToggleSidebar}
              className="glass rounded-xl p-2 hover-lift tilt-container"
            >
              <span className="tilt-card block">
                <svg className="w-7 h-7 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </span>
            </button>
            <Link to={getDashboardLink()} className="flex items-center space-x-3 tilt-container">
              <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center tilt-card depth-1">
                <span className="text-white font-bold text-base">WR</span>
              </div>
              <span className="text-2xl font-semibold text-gray-900">
                Weekly Report
              </span>
            </Link>
          </div>

          {/* Spacer to push nav far from logo */}
          <div className="flex-1" />

          {/* Right: Navigation + Theme Toggle + User */}
          <div className="flex items-center space-x-6">
            <nav className="hidden md:flex space-x-6 items-center">
              <Link
                to={getDashboardLink()}
                className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-lg text-base font-medium transition-colors tilt-container"
              >
                <span className="tilt-card">Dashboard</span>
              </Link>

              {isEmployee() && (
                <Link
                  to="/reports"
                  className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-lg text-base font-medium transition-colors tilt-container"
                >
                  <span className="tilt-card">My Reports</span>
                </Link>
              )}

              {(isSupervisor() || isAdmin()) && (
                <Link
                  to="/reports"
                  className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-lg text-base font-medium transition-colors tilt-container"
                >
                  <span className="tilt-card">Reports</span>
                </Link>
              )}

              {isAdmin() && (
                <Link
                  to="/admin/users"
                  className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-lg text-base font-medium transition-colors tilt-container"
                >
                  <span className="tilt-card">Manage Users</span>
                </Link>
              )}

              <Link
                to="/inbox"
                className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-lg text-base font-medium transition-colors relative tilt-container"
              >
                <span className="tilt-card">Inbox</span>
                {unreadCount > 0 && (
                  <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center shadow-md">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </Link>
            </nav>

            {/* Theme toggle */}
            <button
              onClick={toggleTheme}
              className="glass rounded-xl px-3 py-2 flex items-center space-x-2 hover-lift"
              aria-label="Toggle dark mode"
              title="Toggle dark mode"
            >
              {isDark ? (
                <>
                  <svg className="w-5 h-5 text-yellow-400" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 3a1 1 0 011 1v2a1 1 0 11-2 0V4a1 1 0 011-1zm0 14a5 5 0 100-10 5 5 0 000 10zm7-5a1 1 0 011-1h2a1 1 0 110 2h-2a1 1 0 01-1-1zM3 12a1 1 0 01-1 1H0a1 1 0 110-2h2a1 1 0 011 1zm15.657 6.657a1 1 0 010 1.414l-1.414 1.414a1 1 0 11-1.414-1.414l1.414-1.414a1 1 0 011.414 0zM6.757 6.757a1 1 0 010-1.414L8.171 3.93a1 1 0 111.414 1.414L8.17 6.757a1 1 0 01-1.414 0zm9.9-2.828l1.414-1.414A1 1 0 1119.485 3.93l-1.414 1.414a1 1 0 11-1.414-1.414zM6.757 17.243l-1.414 1.414A1 1 0 013.93 17.243l1.414-1.414a1 1 0 111.414 1.414z"/>
                  </svg>
                  <span className="text-base">Light</span>
                </>
              ) : (
                <>
                  <svg className="w-5 h-5 text-gray-700" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M21.64 13.02A9 9 0 1110.98 2.36a7 7 0 1010.66 10.66z"/>
                  </svg>
                  <span className="text-base">Dark</span>
                </>
              )}
            </button>

            {/* User Menu */}
            <div className="flex items-center space-x-3 glass rounded-xl px-3 py-2">
              <div className="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center">
                <span className="text-base font-medium text-gray-700">
                  {user?.username?.charAt(0).toUpperCase()}
                </span>
              </div>
              <div className="hidden md:block">
                <p className="text-base font-medium text-gray-900">{user?.username}</p>
                <p className="text-sm text-gray-500 capitalize">{user?.role?.toLowerCase()}</p>
              </div>
              <button
                onClick={handleLogout}
                className="text-gray-600 hover:text-gray-800 px-3 py-2 rounded-lg text-base font-medium transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
