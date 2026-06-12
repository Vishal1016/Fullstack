import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Film,
  PlusCircle,
  Users,
  BarChart3,
  Ticket,
  Settings
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './Sidebar.css';

const Sidebar = () => {
  const { user } = useAuth();

  if (!user) return null;

  const getLinksByRole = () => {
    const links = [
      {
        path: '/dashboard',
        label: 'Dashboard',
        icon: LayoutDashboard,
        roles: ['user', 'theatre_owner', 'admin']
      },
      {
        path: '/movies',
        label: 'Movie Catalog',
        icon: Film,
        roles: ['user', 'theatre_owner', 'admin']
      }
    ];

    // Role-based links
    if (user.role === 'user') {
      links.push({
        path: '/dashboard#my-bookings',
        label: 'My Bookings',
        icon: Ticket,
        roles: ['user']
      });
    }

    if (user.role === 'theatre_owner' || user.role === 'admin') {
      links.push(
        {
          path: '/dashboard#add-movie',
          label: 'Add Movie',
          icon: PlusCircle,
          roles: ['theatre_owner', 'admin']
        },
        {
          path: '/dashboard#reports',
          label: 'Revenue Reports',
          icon: BarChart3,
          roles: ['theatre_owner', 'admin']
        }
      );
    }

    if (user.role === 'admin') {
      links.push({
        path: '/dashboard#manage-users',
        label: 'Manage Users',
        icon: Users,
        roles: ['admin']
      });
    }

    return links;
  };

  const menuLinks = getLinksByRole();

  // Helper to handle smooth scroll to target hash within dashboard
  const handleLinkClick = (e, path) => {
    if (path.includes('#')) {
      const hash = path.split('#')[1];
      setTimeout(() => {
        const element = document.getElementById(hash);
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }, 100);
    }
  };

  return (
    <aside className="sidebar glass-panel">
      <div className="sidebar-section">
        <h4 className="sidebar-section-title">Navigation</h4>
        <ul className="sidebar-menu">
          {menuLinks.map((link) => {
            const IconComponent = link.icon;
            
            // Handle hash links differently
            if (link.path.includes('#')) {
              const baseRoute = link.path.split('#')[0];
              const hash = link.path.split('#')[1];
              
              return (
                <li key={link.label} className="sidebar-item">
                  <NavLink
                    to={link.path}
                    onClick={(e) => handleLinkClick(e, link.path)}
                    className={({ isActive }) => 
                      `sidebar-link ${isActive && window.location.hash === `#${hash}` ? 'sidebar-link-active' : ''}`
                    }
                  >
                    <IconComponent size={18} className="sidebar-link-icon" />
                    <span>{link.label}</span>
                  </NavLink>
                </li>
              );
            }

            return (
              <li key={link.label} className="sidebar-item">
                <NavLink
                  to={link.path}
                  className={({ isActive }) => 
                    `sidebar-link ${isActive && window.location.hash === '' ? 'sidebar-link-active' : ''}`
                  }
                >
                  <IconComponent size={18} className="sidebar-link-icon" />
                  <span>{link.label}</span>
                </NavLink>
              </li>
            );
          })}
        </ul>
      </div>

      <div className="sidebar-footer">
        <div className="system-status">
          <div className="status-dot"></div>
          <span>API Gateway: Active</span>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
