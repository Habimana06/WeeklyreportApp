import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import api from '../../services/api';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');

  const { login, isAdmin, isSupervisor, isEmployee } = useAuth();
  const [awaiting2FA, setAwaiting2FA] = useState(false);
  const [code, setCode] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const result = await login(formData);
    if (result.success) {
      if (result.awaiting2FA) {
        return navigate('/verify');
      }
      // Apply per-user theme immediately after successful login (no 2FA)
      try {
        const raw = localStorage.getItem('user');
        const parsed = raw ? JSON.parse(raw) : null;
        const key = parsed?.id ? `theme_${parsed.id}` : 'theme_default';
        const stored = localStorage.getItem(key);
        if (stored === 'dark') document.body.classList.add('theme-dark');
        else document.body.classList.remove('theme-dark');
      } catch { /* ignore theme read errors */ }
      setAwaiting2FA(false);
    } else {
      setError(result.error);
    }
    
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-16 px-6">
      <div className="max-w-md w-full space-y-8">
        <div className="bg-white/90 backdrop-blur rounded-2xl shadow-xl border border-gray-100 p-8">
          <div className="mx-auto h-14 w-14 bg-blue-600 rounded-xl flex items-center justify-center shadow-md">
            <span className="text-white font-bold text-2xl">WR</span>
          </div>
          <h2 className="mt-6 text-center text-4xl font-extrabold text-gray-900 tracking-tight">
            Sign in
          </h2>
          {/* Registration disabled by policy */}
        
        
        {!awaiting2FA ? (
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-base">
              {error}
            </div>
          )}
          
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-base font-medium text-gray-800">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                value={formData.username}
                onChange={handleChange}
                className="mt-1 appearance-none relative block w-full px-4 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base"
                placeholder="Enter your username"
              />
            </div>
            
            <div>
              <label htmlFor="password" className="block text-base font-medium text-gray-800">
                Password
              </label>
              <div className="mt-1 relative">
              <input
                id="password"
                name="password"
                  type={showPassword ? 'text' : 'password'}
                required
                value={formData.password}
                onChange={handleChange}
                  className="appearance-none relative block w-full px-4 py-3 pr-12 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base"
                placeholder="Enter your password"
              />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 px-4 flex items-center text-gray-500 hover:text-gray-700"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  <span className={`transition-transform duration-300 inline-block ${showPassword ? 'scale-110' : 'scale-100'}`}>
                    {/* Animated eye icon */}
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"></path>
                      <circle cx="12" cy="12" r={showPassword ? '4' : '2'} className="transition-all duration-300"></circle>
                    </svg>
                  </span>
                </button>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-3 px-6 border border-transparent text-base font-semibold rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed shadow"
            >
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
            <div className="text-center">
              <Link to="/forgot-password" className="text-base text-blue-700 hover:text-blue-600">
                Forgot your password?
              </Link>
            </div>
          </div>
        </form>
        ) : (
          <form className="mt-8 space-y-6" onSubmit={async (e) => {
            e.preventDefault();
            try {
              const user = JSON.parse(localStorage.getItem('user'));
              const res = await api.post('/auth/verify-2fa', null, { params: { userId: user.id, code } });
              const data = res.data;
              localStorage.setItem('token', data.token);
              localStorage.setItem('user', JSON.stringify({ id: data.id, username: data.username, role: data.role, email: data.email }));
              // Apply per-user theme immediately after 2FA completes
              try {
                const key = data?.id ? `theme_${data.id}` : 'theme_default';
                const stored = localStorage.getItem(key);
                if (stored === 'dark') document.body.classList.add('theme-dark');
                else document.body.classList.remove('theme-dark');
              } catch { /* ignore theme read errors */ }
              if (isAdmin()) navigate('/admin/dashboard');
              else if (isSupervisor()) navigate('/supervisor/dashboard');
              else if (isEmployee()) navigate('/employee/dashboard');
              else navigate('/dashboard');
            } catch (e) {
              setError(e.response?.data?.error || 'Invalid code');
            }
          }}>
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-base">{error}</div>
            )}
            <div>
              <label className="block text-base font-medium text-gray-800">Verification code</label>
              <input
                type="text"
                inputMode="numeric"
                pattern="[0-9]*"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                className="mt-1 block w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base"
                placeholder="Enter the 6-digit code"
                required
              />
            </div>
            <button type="submit" className="w-full py-3 px-6 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-base font-semibold shadow">Verify</button>
            <p className="text-sm text-center text-gray-600">We sent a code to your registered email.</p>
          </form>
        )}
        </div>
      </div>
    </div>
  );
};

export default Login;
