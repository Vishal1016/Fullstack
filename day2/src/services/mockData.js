// LocalStorage keys
const MOVIES_KEY = 'cinema_movies';
const BOOKINGS_KEY = 'cinema_bookings';
const USERS_KEY = 'cinema_users';

const DEFAULT_MOVIES = [
  {
    id: '1',
    title: 'Dune: Part Two',
    rating: 4.8,
    genre: 'Sci-Fi / Adventure',
    duration: '166 min',
    description: 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.',
    poster: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
    price: 15,
    showtimes: ['10:30 AM', '2:00 PM', '6:30 PM', '9:45 PM'],
  },
  {
    id: '2',
    title: 'Oppenheimer',
    rating: 4.9,
    genre: 'Biography / Drama',
    duration: '180 min',
    description: 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.',
    poster: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=600&q=80',
    price: 14,
    showtimes: ['11:00 AM', '3:15 PM', '7:00 PM'],
  },
  {
    id: '3',
    title: 'Spider-Man: Across the Spider-Verse',
    rating: 4.7,
    genre: 'Animation / Action',
    duration: '140 min',
    description: 'Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.',
    poster: 'https://images.unsplash.com/photo-1635805737707-575885ab0820?auto=format&fit=crop&w=600&q=80',
    price: 12,
    showtimes: ['12:00 PM', '4:30 PM', '8:00 PM'],
  },
  {
    id: '4',
    title: 'Interstellar',
    rating: 4.9,
    genre: 'Sci-Fi / Drama',
    duration: '169 min',
    description: 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity\'s survival.',
    poster: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=600&q=80',
    price: 13,
    showtimes: ['1:00 PM', '5:00 PM', '9:00 PM'],
  }
];

const DEFAULT_USERS = [
  {
    email: 'user@cinema.com',
    password: 'password',
    name: 'Jane Doe (Customer)',
    role: 'user',
    token: 'mock-jwt-token-user-123'
  },
  {
    email: 'owner@cinema.com',
    password: 'password',
    name: 'John Owner (Theatre Owner)',
    role: 'theatre_owner',
    token: 'mock-jwt-token-owner-456'
  },
  {
    email: 'admin@cinema.com',
    password: 'password',
    name: 'Alex Admin (Administrator)',
    role: 'admin',
    token: 'mock-jwt-token-admin-789'
  }
];

const DEFAULT_BOOKINGS = [
  {
    id: 'b1',
    movieId: '1',
    movieTitle: 'Dune: Part Two',
    seats: ['A3', 'A4'],
    totalPrice: 30,
    showtime: '6:30 PM',
    date: '2026-06-12',
    userEmail: 'user@cinema.com',
    status: 'Confirmed'
  },
  {
    id: 'b2',
    movieId: '2',
    movieTitle: 'Oppenheimer',
    seats: ['C5', 'C6', 'C7'],
    totalPrice: 42,
    showtime: '3:15 PM',
    date: '2026-06-11',
    userEmail: 'user@cinema.com',
    status: 'Confirmed'
  }
];

// Helper to initialize local storage
export const initMockStorage = () => {
  if (!localStorage.getItem(MOVIES_KEY)) {
    localStorage.setItem(MOVIES_KEY, JSON.stringify(DEFAULT_MOVIES));
  }
  if (!localStorage.getItem(USERS_KEY)) {
    localStorage.setItem(USERS_KEY, JSON.stringify(DEFAULT_USERS));
  }
  if (!localStorage.getItem(BOOKINGS_KEY)) {
    localStorage.setItem(BOOKINGS_KEY, JSON.stringify(DEFAULT_BOOKINGS));
  }
};

// Movie Data Operations
export const getMovies = () => {
  initMockStorage();
  return JSON.parse(localStorage.getItem(MOVIES_KEY));
};

export const getMovieById = (id) => {
  const movies = getMovies();
  return movies.find(m => m.id === id);
};

export const addMovie = (movieData) => {
  const movies = getMovies();
  const newMovie = {
    ...movieData,
    id: (movies.length + 1).toString(),
    rating: 5.0, // default new movie rating
    price: Number(movieData.price) || 12,
    showtimes: movieData.showtimes || ['12:00 PM', '4:00 PM', '8:00 PM'],
    poster: movieData.poster || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80'
  };
  movies.push(newMovie);
  localStorage.setItem(MOVIES_KEY, JSON.stringify(movies));
  return newMovie;
};

export const deleteMovie = (id) => {
  const movies = getMovies();
  const filtered = movies.filter(m => m.id !== id);
  localStorage.setItem(MOVIES_KEY, JSON.stringify(filtered));
  return true;
};

// Booking Operations
export const getBookings = () => {
  initMockStorage();
  return JSON.parse(localStorage.getItem(BOOKINGS_KEY));
};

export const getBookingsByEmail = (email) => {
  const bookings = getBookings();
  return bookings.filter(b => b.userEmail === email);
};

export const addBooking = (bookingData) => {
  const bookings = getBookings();
  const newBooking = {
    id: 'b' + (bookings.length + 1),
    date: new Date().toISOString().split('T')[0],
    status: 'Confirmed',
    ...bookingData
  };
  bookings.push(newBooking);
  localStorage.setItem(BOOKINGS_KEY, JSON.stringify(bookings));
  return newBooking;
};

// Seat Booked Status per Movie & Showtime
export const getOccupiedSeats = (movieId, showtime) => {
  const bookings = getBookings();
  const activeBookings = bookings.filter(
    b => b.movieId === movieId && b.showtime === showtime
  );
  const occupied = [];
  activeBookings.forEach(b => {
    occupied.push(...b.seats);
  });
  return occupied;
};

// User Operations
export const getUsers = () => {
  initMockStorage();
  return JSON.parse(localStorage.getItem(USERS_KEY));
};

export const registerUser = (userData) => {
  const users = getUsers();
  const exists = users.find(u => u.email === userData.email);
  if (exists) {
    throw new Error('User already exists');
  }
  const newUser = {
    ...userData,
    role: 'user', // standard registration is always user
    token: `mock-jwt-token-user-${Date.now()}`
  };
  users.push(newUser);
  localStorage.setItem(USERS_KEY, JSON.stringify(users));
  return newUser;
};

export const updateUserRole = (email, newRole) => {
  const users = getUsers();
  const index = users.findIndex(u => u.email === email);
  if (index !== -1) {
    users[index].role = newRole;
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
    return users[index];
  }
  throw new Error('User not found');
};
