import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Film, LogOut, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../atomic/Button';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => {
    return location.pathname === path ? 'nav-link-active' : '';
  };

  return (
    <nav className="navbar glass-panel">
      <div className="nav-brand" onClick={() => navigate(user ? '/dashboard' : '/login')}>
        <div className="brand-logo-wrapper">
          <Film className="brand-icon" size={24} />
        </div>
        <span className="brand-name">CinePass</span>
        <span className="brand-badge">Gateway Secure</span>
      </div>

      <div className="nav-menu">
        {user && (
          <>
            <Link to="/dashboard" className={`nav-link ${isActive('/dashboard')}`}>
              Dashboard
            </Link>
            <Link to="/movies" className={`nav-link ${isActive('/movies')}`}>
              Movies Catalog
            </Link>
          </>
        )}
      </div>

      <div className="nav-actions">
        {user ? (
          <div className="user-profile-section">
            <div className="user-details">
              <span className="user-name">{user.name}</span>
              <span className={`badge badge-${user.role.replace('_', '')}`}>
                {user.role === 'theatre_owner' ? 'Owner' : user.role}
              </span>
            </div>
            <div className="user-avatar" title={user.name}>
              <User size={18} />
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleLogout}
              className="logout-btn"
              icon={LogOut}
            >
              Logout
            </Button>
          </div>
        ) : (
          location.pathname !== '/login' && (
            <Button variant="primary" size="sm" onClick={() => navigate('/login')}>
              Sign In
            </Button>
          )
        )}
      </div>
    </nav>
  );
};

export default Navbar;
