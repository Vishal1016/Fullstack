import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Calendar, Film, Star, Ticket, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { movieService, bookingService } from '../services/api';
import SeatLayout from '../components/composite/SeatLayout';
import Button from '../components/atomic/Button';
import './Booking.css';

const Booking = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedShowtime, setSelectedShowtime] = useState('');
  const [occupiedSeats, setOccupiedSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  
  const [bookingSuccess, setBookingSuccess] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);

  // Load movie details
  useEffect(() => {
    const loadMovie = async () => {
      try {
        const movieData = await movieService.getById(id);
        if (!movieData) {
          navigate('/movies');
          return;
        }
        setMovie(movieData);
        if (movieData.showtimes?.length > 0) {
          setSelectedShowtime(movieData.showtimes[0]);
        }
      } catch (err) {
        console.error('Failed to load movie for booking:', err);
      } finally {
        setLoading(false);
      }
    };
    loadMovie();
  }, [id, navigate]);

  // Load occupied seats when movie/showtime changes
  useEffect(() => {
    if (!id || !selectedShowtime) return;
    const loadOccupied = async () => {
      try {
        const seats = await bookingService.getOccupied(id, selectedShowtime);
        setOccupiedSeats(seats);
        setSelectedSeats([]); // reset selection
      } catch (err) {
        console.error('Failed to load seats:', err);
      }
    };
    loadOccupied();
  }, [id, selectedShowtime]);

  // Seat toggle handler
  const handleSeatClick = (seatId) => {
    setSelectedSeats(prev => {
      if (prev.includes(seatId)) {
        return prev.filter(s => s !== seatId);
      } else {
        return [...prev, seatId];
      }
    });
  };

  // Perform transaction
  const handleConfirmBooking = async () => {
    if (selectedSeats.length === 0) return;
    setBookingLoading(true);
    try {
      await bookingService.create({
        movieId: movie.id,
        movieTitle: movie.title,
        seats: selectedSeats,
        totalPrice: selectedSeats.length * movie.price,
        showtime: selectedShowtime,
        userEmail: user.email
      });
      setBookingSuccess(true);
      setTimeout(() => {
        navigate('/dashboard#my-bookings');
      }, 2000);
    } catch (err) {
      alert('Booking failed. Please try again.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-spinner-container">
        <div className="spinner"></div>
        <p>Gathering Showtime Information...</p>
      </div>
    );
  }

  const totalPrice = selectedSeats.length * movie.price;

  return (
    <div className="booking-page animate-fade-in">
      <div className="booking-back-nav">
        <Link to="/movies" className="back-link">
          <ArrowLeft size={16} />
          <span>Back to Movies</span>
        </Link>
      </div>

      {bookingSuccess ? (
        <div className="booking-success-screen glass-panel text-center animate-fade-in">
          <CheckCircle2 size={64} className="success-icon" />
          <h2>Booking Confirmed!</h2>
          <p>Your tickets have been registered. The microservice request completed successfully.</p>
          <div className="ticket-receipt-card">
            <h4>{movie.title}</h4>
            <p>Showtime: <strong>{selectedShowtime}</strong></p>
            <p>Seats: <strong>{selectedSeats.join(', ')}</strong></p>
            <p>Total Paid: <strong>${totalPrice}</strong></p>
          </div>
          <p className="redirect-notice">Redirecting to reservations page...</p>
        </div>
      ) : (
        <div className="booking-container-layout">
          {/* Left: Movie info & Showtime Select */}
          <div className="booking-details-column">
            <div className="movie-details-header glass-panel">
              <img src={movie.poster} alt={movie.title} className="booking-movie-poster" />
              <div className="movie-details-info">
                <span className="movie-badge-genre">{movie.genre}</span>
                <h2>{movie.title}</h2>
                <div className="rating-row">
                  <Star size={16} className="star-icon" fill="currentColor" />
                  <strong>{movie.rating.toFixed(1)}</strong>
                  <span className="duration-tag">{movie.duration}</span>
                </div>
                <p className="movie-synopsis">{movie.description}</p>
              </div>
            </div>

            {/* Showtime Selection */}
            <div className="showtime-selection-panel glass-panel mt-6">
              <h3 className="panel-title-label">
                <Calendar size={18} />
                <span>Select a Showtime</span>
              </h3>
              <div className="showtimes-list-btn">
                {movie.showtimes.map((st) => (
                  <button
                    key={st}
                    onClick={() => setSelectedShowtime(st)}
                    className={`showtime-btn-select ${selectedShowtime === st ? 'showtime-btn-active' : ''}`}
                  >
                    {st}
                  </button>
                ))}
              </div>
            </div>

            {/* Seat Grid Map */}
            <div className="seat-grid-map-panel mt-6">
              <SeatLayout
                occupiedSeats={occupiedSeats}
                selectedSeats={selectedSeats}
                onSeatClick={handleSeatClick}
              />
            </div>
          </div>

          {/* Right: Checkout summary panel */}
          <div className="booking-checkout-column">
            <div className="checkout-summary-card glass-panel sticky-checkout">
              <h3 className="checkout-title">
                <Ticket size={18} />
                <span>Ticket Checkout</span>
              </h3>

              <div className="checkout-row">
                <span className="checkout-label">Movie</span>
                <span className="checkout-val bold">{movie.title}</span>
              </div>
              <div className="checkout-row">
                <span className="checkout-label">Showtime</span>
                <span className="checkout-val bold text-primary">{selectedShowtime}</span>
              </div>
              <div className="checkout-row">
                <span className="checkout-label">Ticket Price</span>
                <span className="checkout-val">${movie.price} / seat</span>
              </div>

              <div className="checkout-divider"></div>

              <div className="checkout-row">
                <span className="checkout-label">Selected Seats</span>
                <span className="checkout-val">
                  {selectedSeats.length > 0 ? (
                    <div className="selected-seats-row">
                      {selectedSeats.map(s => <span key={s} className="seat-badge">{s}</span>)}
                    </div>
                  ) : (
                    <span className="text-muted">None selected</span>
                  )}
                </span>
              </div>

              <div className="checkout-divider"></div>

              <div className="checkout-row total-row">
                <span className="checkout-label">Total Payment</span>
                <span className="checkout-val total-price">${totalPrice}</span>
              </div>

              {user?.role === 'user' ? (
                <Button
                  variant="primary"
                  className="w-full mt-6"
                  disabled={selectedSeats.length === 0}
                  loading={bookingLoading}
                  onClick={handleConfirmBooking}
                >
                  {selectedSeats.length > 0
                    ? `Confirm Booking (${selectedSeats.length} Tickets)`
                    : 'Select Seats to Book'}
                </Button>
              ) : (
                <div className="checkout-rbac-warning mt-6">
                  <p>Only users with the <strong>Customer (User)</strong> role are permitted to reserve tickets.</p>
                  <p>Your current role is: <strong>{user?.role}</strong></p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Booking;
