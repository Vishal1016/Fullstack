import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/api';
import * as mockDb from '../services/mockData';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if token and user data are cached in localStorage
    mockDb.initMockStorage();
    const token = localStorage.getItem('cinema_jwt_token');
    const storedUser = localStorage.getItem('cinema_user_profile');
    
    if (token && storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const data = await authService.login(email, password);
      const userProfile = {
        email: data.email,
        name: data.name,
        role: data.role
      };
      
      setUser(userProfile);
      localStorage.setItem('cinema_user_profile', JSON.stringify(userProfile));
      return userProfile;
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const register = async (name, email, password) => {
    setLoading(true);
    try {
      const data = await authService.register(name, email, password);
      // Automatically log in on registration
      const userProfile = {
        email: data.email,
        name: data.name,
        role: data.role
      };
      localStorage.setItem('cinema_jwt_token', data.token);
      localStorage.setItem('cinema_user_profile', JSON.stringify(userProfile));
      setUser(userProfile);
      return userProfile;
    } catch (error) {
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    localStorage.removeItem('cinema_user_profile');
    setUser(null);
  };

  const hasRole = (allowedRoles) => {
    if (!user) return false;
    return allowedRoles.includes(user.role);
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    hasRole,
    isUser: user?.role === 'user',
    isOwner: user?.role === 'theatre_owner',
    isAdmin: user?.role === 'admin',
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
