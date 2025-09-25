import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { useAuth } from '../../contexts/AuthContext';

const VerifyCode = () => {
  const navigate = useNavigate();
  const { isAdmin, isSupervisor, isEmployee, completeLogin } = useAuth();
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      const res = await api.post('/auth/verify-2fa', null, { params: { userId: user.id, code } });
      const data = res.data;
      completeLogin(data);
      if (isAdmin()) navigate('/admin/dashboard');
      else if (isSupervisor()) navigate('/supervisor/dashboard');
      else if (isEmployee()) navigate('/employee/dashboard');
      else navigate('/dashboard');
    } catch (e) {
      setError(e.response?.data?.error || 'Invalid code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-16 px-6">
      <div className="max-w-md w-full space-y-8">
        <div className="bg-white/90 backdrop-blur rounded-2xl shadow-xl border border-gray-100 p-8">
          <div className="mx-auto h-14 w-14 bg-blue-600 rounded-xl flex items-center justify-center shadow-md">
            <span className="text-white font-bold text-2xl">WR</span>
          </div>
          <h2 className="mt-6 text-center text-4xl font-extrabold text-gray-900 tracking-tight">Verify your login</h2>
          <p className="mt-2 text-center text-base text-gray-700">Enter the 6-digit code sent to your email.</p>
          <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
            {error && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-base">{error}</div>}
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
            <button type="submit" disabled={loading} className="w-full py-3 px-6 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 text-base font-semibold shadow">
              {loading ? 'Verifying...' : 'Verify'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default VerifyCode;


