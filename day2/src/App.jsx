import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import Navbar from './components/layout/Navbar';
import Sidebar from './components/layout/Sidebar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import MovieCatalog from './pages/MovieCatalog';
import Booking from './pages/Booking';
import './App.css';

function AppContent() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <div className="app-container">
              <Navbar />
              <Sidebar />
              <main className="main-layout">
                <div className="content-container">
                  <Routes>
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/movies" element={<MovieCatalog />} />
                    <Route path="/booking/:id" element={<Booking />} />
                    <Route path="*" element={<Navigate to="/dashboard" replace />} />
                  </Routes>
                </div>
              </main>
            </div>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

export default App;
