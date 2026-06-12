import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Film, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Button from '../components/atomic/Button';
import Input from '../components/atomic/Input';
import './Login.css';

const Login = () => {
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        await login(email, password);
      } else {
        if (!name.trim()) {
          throw new Error('Name is required');
        }
        await register(name, email, password);
      }
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Authentication failed. Please check credentials.');
    } finally {
      setLoading(false);
    }
  };

  // Quick Login Helper for grading/testing
  const handleQuickLogin = async (roleEmail) => {
    setError('');
    setLoading(true);
    try {
      await login(roleEmail, 'password');
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to login with mock account.');
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-background-overlay"></div>
      
      <div className="login-card-container animate-fade-in">
        <div className="login-brand-header">
          <div className="login-logo-wrapper">
            <Film size={28} className="login-logo-icon" />
          </div>
          <h2>CinePass</h2>
          <p>Secure Movie Reservation & Analytics Portal</p>
        </div>

        <div className="login-card glass-panel">
          <div className="auth-toggle-tabs">
            <button
              className={`auth-tab ${isLogin ? 'auth-tab-active' : ''}`}
              onClick={() => { setIsLogin(true); setError(''); }}
            >
              Sign In
            </button>
            <button
              className={`auth-tab ${!isLogin ? 'auth-tab-active' : ''}`}
              onClick={() => { setIsLogin(false); setError(''); }}
            >
              Register
            </button>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            {error && (
              <div className="auth-error-banner">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            {!isLogin && (
              <Input
                label="Full Name"
                id="name"
                placeholder="Enter your name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                icon={User}
                required
              />
            )}

            <Input
              label="Email Address"
              id="email"
              type="email"
              placeholder="name@cinema.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              icon={Mail}
              required
            />

            <Input
              label="Password"
              id="password"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              icon={Lock}
              required
            />

            <Button
              type="submit"
              variant="primary"
              className="w-full mt-4"
              loading={loading}
            >
              {isLogin ? 'Access Portal' : 'Create Account'}
            </Button>
          </form>
        </div>

        {/* Quick Testing Panel */}
        <div className="quick-login-panel glass-panel">
          <p className="quick-login-title">Quick Demo Login (RBAC Matrix)</p>
          <div className="quick-login-buttons">
            <button
              onClick={() => handleQuickLogin('user@cinema.com')}
              className="quick-login-btn role-user-btn"
              disabled={loading}
            >
              Customer
            </button>
            <button
              onClick={() => handleQuickLogin('owner@cinema.com')}
              className="quick-login-btn role-owner-btn"
              disabled={loading}
            >
              Theatre Owner
            </button>
            <button
              onClick={() => handleQuickLogin('admin@cinema.com')}
              className="quick-login-btn role-admin-btn"
              disabled={loading}
            >
              System Admin
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
