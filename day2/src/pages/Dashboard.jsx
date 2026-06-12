import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Ticket,
  DollarSign,
  Film,
  Users,
  TrendingUp,
  Plus,
  Trash2,
  TrendingDown,
  UserCheck
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { movieService, bookingService, adminService } from '../services/api';
import StatCard from '../components/composite/StatCard';
import Button from '../components/atomic/Button';
import Input from '../components/atomic/Input';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const location = useLocation();

  // Shared state
  const [movies, setMovies] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [usersList, setUsersList] = useState([]);
  
  // Dashboard Metrics
  const [metrics, setMetrics] = useState({
    userBookingsCount: 0,
    userTotalSpent: 0,
    ownerTotalRevenue: 0,
    ownerTicketsSold: 0,
    ownerActiveMoviesCount: 0,
  });

  // Forms and Modals
  const [newMovie, setNewMovie] = useState({
    title: '',
    genre: '',
    duration: '',
    price: '',
    description: '',
    showtimes: '1:00 PM, 4:30 PM, 8:00 PM',
    poster: ''
  });
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');

  // Fetch all necessary data on load
  const fetchData = async () => {
    try {
      const allMovies = await movieService.getAll();
      setMovies(allMovies);

      const allBookings = await bookingService.getAll();
      setBookings(allBookings);

      if (user.role === 'admin') {
        const list = await adminService.getUsers();
        setUsersList(list);
      }

      // Calculate Metrics
      if (user.role === 'user') {
        const userB = allBookings.filter(b => b.userEmail === user.email);
        const spent = userB.reduce((acc, curr) => acc + curr.totalPrice, 0);
        setMetrics({
          userBookingsCount: userB.length,
          userTotalSpent: spent,
        });
      } else {
        const ownerRevenue = allBookings.reduce((acc, curr) => acc + curr.totalPrice, 0);
        const tickets = allBookings.reduce((acc, curr) => acc + curr.seats.length, 0);
        setMetrics({
          ownerTotalRevenue: ownerRevenue,
          ownerTicketsSold: tickets,
          ownerActiveMoviesCount: allMovies.length
        });
      }
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  // Handle Hash Navigation Scrolling on render/hashchange
  useEffect(() => {
    if (location.hash) {
      const id = location.hash.replace('#', '');
      const element = document.getElementById(id);
      if (element) {
        setTimeout(() => {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 300);
      }
    }
  }, [location]);

  // Form submit to Add Movie
  const handleAddMovie = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    const { title, genre, duration, price, description, showtimes } = newMovie;
    if (!title || !genre || !duration || !price || !description) {
      setFormError('Please fill out all required fields.');
      return;
    }

    try {
      const showtimesArray = showtimes.split(',').map(s => s.trim());
      await movieService.create({
        title,
        genre,
        duration,
        price: Number(price),
        description,
        showtimes: showtimesArray,
        poster: newMovie.poster
      });

      setNewMovie({
        title: '',
        genre: '',
        duration: '',
        price: '',
        description: '',
        showtimes: '1:00 PM, 4:30 PM, 8:00 PM',
        poster: ''
      });
      setFormSuccess('Movie successfully added to the catalog!');
      fetchData(); // Reload stats
    } catch (err) {
      setFormError('Failed to add movie.');
    }
  };

  // Delete Movie (Admin/Owner action)
  const handleDeleteMovie = async (movieId) => {
    if (window.confirm('Are you sure you want to delete this movie?')) {
      await movieService.delete(movieId);
      fetchData();
    }
  };

  // Toggle User Role (Admin action)
  const handleToggleRole = async (email, currentRole) => {
    const rolesOrder = ['user', 'theatre_owner', 'admin'];
    const nextIndex = (rolesOrder.indexOf(currentRole) + 1) % rolesOrder.length;
    const newRole = rolesOrder[nextIndex];

    try {
      await adminService.updateUserRole(email, newRole);
      fetchData();
    } catch (err) {
      alert('Failed to update role');
    }
  };

  return (
    <div className="dashboard-page animate-fade-in">
      <div className="dashboard-welcome">
        <h2>Welcome back, {user.name}</h2>
        <p>Manage your account settings, credentials, and track role permissions.</p>
      </div>

      {/* RENDER USER INTERFACE */}
      {user.role === 'user' && (
        <div className="dashboard-content">
          <div className="metrics-grid grid-container grid-cols-3">
            <StatCard
              title="Active Bookings"
              value={metrics.userBookingsCount}
              icon={Ticket}
              subtext="Upcoming shows"
              trend="2 New"
              trendType="up"
            />
            <StatCard
              title="Total Expenses"
              value={`$${metrics.userTotalSpent}`}
              icon={DollarSign}
              subtext="Spent this month"
              trend="+$15 vs last week"
              trendType="up"
            />
            <StatCard
              title="Gateway Status"
              value="Secure"
              icon={UserCheck}
              subtext="JWT Session Active"
              trend="Valid"
              trendType="neutral"
            />
          </div>

          <div id="my-bookings" className="dashboard-section-panel glass-panel mt-8">
            <h3 className="section-title">My Reservation History</h3>
            <div className="table-responsive">
              {bookings.filter(b => b.userEmail === user.email).length === 0 ? (
                <p className="no-data-msg">You haven't made any bookings yet.</p>
              ) : (
                <table className="dashboard-table">
                  <thead>
                    <tr>
                      <th>Reservation ID</th>
                      <th>Movie Title</th>
                      <th>Seats Booked</th>
                      <th>Total Paid</th>
                      <th>Showtime</th>
                      <th>Date</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bookings
                      .filter(b => b.userEmail === user.email)
                      .map((booking) => (
                        <tr key={booking.id}>
                          <td><span className="code-id">{booking.id}</span></td>
                          <td className="bold-cell">{booking.movieTitle}</td>
                          <td>
                            <div className="seats-badges">
                              {booking.seats.map(s => <span key={s} className="seat-badge">{s}</span>)}
                            </div>
                          </td>
                          <td>${booking.totalPrice}</td>
                          <td>{booking.showtime}</td>
                          <td>{booking.date}</td>
                          <td>
                            <span className="status-badge status-confirmed">
                              {booking.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}

      {/* RENDER THEATRE OWNER / ADMIN INTERFACE */}
      {(user.role === 'theatre_owner' || user.role === 'admin') && (
        <div className="dashboard-content">
          <div className="metrics-grid grid-container grid-cols-4">
            <StatCard
              title="Gross Revenue"
              value={`$${metrics.ownerTotalRevenue}`}
              icon={DollarSign}
              subtext="All-time static sales"
              trend="14%"
              trendType="up"
            />
            <StatCard
              title="Tickets Sold"
              value={metrics.ownerTicketsSold}
              icon={Ticket}
              subtext="Total seats reserved"
              trend="8%"
              trendType="up"
            />
            <StatCard
              title="Active Movies"
              value={metrics.ownerActiveMoviesCount}
              icon={Film}
              subtext="Currently running"
              trend="Stable"
              trendType="neutral"
            />
            <StatCard
              title="Gateway Requests"
              value="240/s"
              icon={Users}
              subtext="Average API volume"
              trend="99.9% Up"
              trendType="up"
            />
          </div>

          <div className="dashboard-double-grid mt-8">
            {/* Visual Analytics */}
            <div id="reports" className="dashboard-section-panel glass-panel">
              <h3 className="section-title">Weekly Ticket Sales Reports</h3>
              <p className="section-description">Interactive representation of revenue across services via API Gateway.</p>
              
              <div className="chart-mockup">
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '40%' }}></div>
                  <span className="chart-label">Mon</span>
                </div>
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '65%' }}></div>
                  <span className="chart-label">Tue</span>
                </div>
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '50%' }}></div>
                  <span className="chart-label">Wed</span>
                </div>
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '85%' }}></div>
                  <span className="chart-label">Thu</span>
                </div>
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '100%' }}>
                    <div className="bar-glow"></div>
                  </div>
                  <span className="chart-label">Fri (Peak)</span>
                </div>
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '90%' }}></div>
                  <span className="chart-label">Sat</span>
                </div>
                <div className="chart-bar-container">
                  <div className="chart-bar" style={{ height: '70%' }}></div>
                  <span className="chart-label">Sun</span>
                </div>
              </div>

              <div className="chart-legend-metrics mt-4">
                <div className="legend-metric">
                  <span className="metric-dot primary-dot"></span>
                  <span>Order Microservice: 64%</span>
                </div>
                <div className="legend-metric">
                  <span className="metric-dot secondary-dot"></span>
                  <span>User Microservice: 36%</span>
                </div>
              </div>
            </div>

            {/* Add Movie Form */}
            <div id="add-movie" className="dashboard-section-panel glass-panel">
              <h3 className="section-title">Publish New Movie (Showtime Creator)</h3>
              <p className="section-description">Registers movie details to mock DB, accessible instantly in user views.</p>
              
              <form onSubmit={handleAddMovie} className="dashboard-form">
                {formError && <div className="form-alert error-alert">{formError}</div>}
                {formSuccess && <div className="form-alert success-alert">{formSuccess}</div>}

                <div className="form-row-2">
                  <Input
                    label="Movie Title"
                    id="title"
                    placeholder="e.g. Inception"
                    value={newMovie.title}
                    onChange={(e) => setNewMovie({ ...newMovie, title: e.target.value })}
                    required
                  />
                  <Input
                    label="Genre"
                    id="genre"
                    placeholder="e.g. Action / Thriller"
                    value={newMovie.genre}
                    onChange={(e) => setNewMovie({ ...newMovie, genre: e.target.value })}
                    required
                  />
                </div>

                <div className="form-row-2">
                  <Input
                    label="Duration"
                    id="duration"
                    placeholder="e.g. 148 min"
                    value={newMovie.duration}
                    onChange={(e) => setNewMovie({ ...newMovie, duration: e.target.value })}
                    required
                  />
                  <Input
                    label="Ticket Price ($)"
                    id="price"
                    type="number"
                    placeholder="e.g. 15"
                    value={newMovie.price}
                    onChange={(e) => setNewMovie({ ...newMovie, price: e.target.value })}
                    required
                  />
                </div>

                <Input
                  label="Showtimes (comma separated)"
                  id="showtimes"
                  placeholder="e.g. 1:00 PM, 4:30 PM, 8:00 PM"
                  value={newMovie.showtimes}
                  onChange={(e) => setNewMovie({ ...newMovie, showtimes: e.target.value })}
                />

                <Input
                  label="Poster Image URL (Optional)"
                  id="poster"
                  placeholder="Paste Unsplash or poster image URL"
                  value={newMovie.poster}
                  onChange={(e) => setNewMovie({ ...newMovie, poster: e.target.value })}
                />

                <div className="form-group">
                  <label className="form-label">Synopsis / Description</label>
                  <textarea
                    rows="2"
                    className="dashboard-textarea"
                    placeholder="Enter short movie plot summary..."
                    value={newMovie.description}
                    onChange={(e) => setNewMovie({ ...newMovie, description: e.target.value })}
                    required
                  ></textarea>
                </div>

                <Button type="submit" variant="primary" className="w-full">
                  Create Showtimes & Publish
                </Button>
              </form>
            </div>
          </div>

          {/* Manage Movies Grid */}
          <div className="dashboard-section-panel glass-panel mt-8">
            <h3 className="section-title">Current Movies Registry</h3>
            <div className="registry-table-wrapper">
              <table className="dashboard-table">
                <thead>
                  <tr>
                    <th>Poster</th>
                    <th>Movie Title</th>
                    <th>Genre</th>
                    <th>Ticket Cost</th>
                    <th>Showtimes Available</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {movies.map((movie) => (
                    <tr key={movie.id}>
                      <td>
                        <img src={movie.poster} alt={movie.title} className="table-poster-thumb" />
                      </td>
                      <td className="bold-cell">{movie.title}</td>
                      <td>{movie.genre}</td>
                      <td>${movie.price}</td>
                      <td>
                        <div className="showtime-pills">
                          {movie.showtimes.map(st => <span key={st} className="showtime-pill">{st}</span>)}
                        </div>
                      </td>
                      <td>
                        <Button
                          variant="ghost"
                          onClick={() => handleDeleteMovie(movie.id)}
                          className="delete-table-btn"
                          title="Delete Movie"
                        >
                          <Trash2 size={16} />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* RENDER SYSTEM ADMIN USER CONTROL PANEL */}
          {user.role === 'admin' && (
            <div id="manage-users" className="dashboard-section-panel glass-panel mt-8">
              <div className="section-header-flex">
                <div>
                  <h3 className="section-title">Role-Based Access Control (RBAC) Console</h3>
                  <p className="section-description">Modify credentials, switch user classes dynamically, and verify structural security gates.</p>
                </div>
                <div className="gateway-badge-glow">
                  <Users size={16} />
                  <span>Gateway Guard v1.0</span>
                </div>
              </div>
              
              <div className="table-responsive">
                <table className="dashboard-table">
                  <thead>
                    <tr>
                      <th>Evaluator Account</th>
                      <th>Email ID</th>
                      <th>Cryptographic JWT Signature (Gateway Intercepted)</th>
                      <th>Assigned Role</th>
                      <th>Security Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usersList.map((usr) => (
                      <tr key={usr.email}>
                        <td className="bold-cell">{usr.name}</td>
                        <td>{usr.email}</td>
                        <td>
                          <code className="jwt-key">{usr.token.substring(0, 15)}...{usr.token.substring(usr.token.length - 8)}</code>
                        </td>
                        <td>
                          <span className={`badge badge-${usr.role.replace('_', '')}`}>
                            {usr.role === 'theatre_owner' ? 'Owner' : usr.role}
                          </span>
                        </td>
                        <td>
                          <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => handleToggleRole(usr.email, usr.role)}
                            className="toggle-role-btn"
                          >
                            Cycle Role
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Dashboard;
