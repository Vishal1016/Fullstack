import axios from 'axios';
import * as mockDb from './mockData';

// Configure Axios Client
export const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Token from localStorage
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cinema_jwt_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    console.log(`[Axios Request] -> ${config.method.toUpperCase()} ${config.url}`, {
      headers: config.headers,
      data: config.data
    });
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Log responses
apiClient.interceptors.response.use(
  (response) => {
    console.log(`[Axios Response] <- ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error(`[Axios Error] <- ${error.response?.status || 'Network Error'} ${error.config?.url}`);
    return Promise.reject(error);
  }
);

// API Service wrappers that mirror REST endpoints, simulate latency, and interface with our mockDb.
// Once backend services are active, these can be updated directly to use apiClient.

export const authService = {
  login: async (email, password) => {
    const response = await apiClient.post('/auth/login', { email, password });
    const user = response.data;
    
    // Set token in localStorage (matches JWT Validation flow)
    localStorage.setItem('cinema_jwt_token', user.token);
    
    return {
      token: user.token,
      email: user.email,
      name: user.name,
      role: user.role
    };
  },
  
  register: async (name, email, password) => {
    const response = await apiClient.post('/auth/register', { name, email, password });
    const newUser = response.data;
    return {
      token: newUser.token,
      email: newUser.email,
      name: newUser.name,
      role: newUser.role
    };
  },
  
  logout: () => {
    localStorage.removeItem('cinema_jwt_token');
  }
};

export const movieService = {
  getAll: async () => {
    const response = await apiClient.get('/movies');
    // Standard response format contains "data", which is a Spring Page object.
    const movies = response.data.data.content || response.data.data;
    return movies.map(m => ({
      ...m,
      genre: Array.isArray(m.genre) ? m.genre.join(' / ') : (m.genre || 'Drama'),
      duration: typeof m.duration === 'number' ? `${m.duration} min` : m.duration,
      poster: m.posterUrl || m.poster
    }));
  },
  
  getById: async (id) => {
    const response = await apiClient.get(`/movies/${id}`);
    const m = response.data.data;
    return {
      ...m,
      genre: Array.isArray(m.genre) ? m.genre.join(' / ') : (m.genre || 'Drama'),
      duration: typeof m.duration === 'number' ? `${m.duration} min` : m.duration,
      poster: m.posterUrl || m.poster
    };
  },
  
  create: async (movieData) => {
    // Parse duration (e.g. "166 min" or "166") to integer minutes for backend
    let durationMins = 120;
    if (movieData.duration) {
      const parsed = parseInt(movieData.duration.toString().replace(/[^0-9]/g, ''));
      if (!isNaN(parsed)) durationMins = parsed;
    }
    // Parse genre string (e.g. "Sci-Fi / Adventure") to array for backend
    const genreArray = movieData.genre ? movieData.genre.split('/').map(g => g.trim()) : [];
    
    const backendData = {
      title: movieData.title,
      genre: genreArray,
      language: movieData.language || 'English',
      duration: durationMins,
      releaseDate: movieData.releaseDate || new Date().toISOString().split('T')[0],
      posterUrl: movieData.poster || '',
      description: movieData.description || '',
      price: Number(movieData.price) || 12.0,
      showtimes: movieData.showtimes || ['12:00 PM', '4:00 PM', '8:00 PM']
    };
    
    const response = await apiClient.post('/movies', backendData);
    const m = response.data.data;
    return {
      ...m,
      genre: Array.isArray(m.genre) ? m.genre.join(' / ') : (m.genre || 'Drama'),
      duration: typeof m.duration === 'number' ? `${m.duration} min` : m.duration,
      poster: m.posterUrl || m.poster
    };
  },
  
  delete: async (id) => {
    const response = await apiClient.delete(`/movies/${id}`);
    return response.data.status === 'success';
  }
};

export const bookingService = {
  getAll: async () => {
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // Conceptually:
    // const response = await apiClient.get('/bookings');
    // return response.data;
    
    return mockDb.getBookings();
  },
  
  getByUserEmail: async (email) => {
    await new Promise(resolve => setTimeout(resolve, 400));
    
    // Conceptually:
    // const response = await apiClient.get(`/bookings/user/${email}`);
    // return response.data;
    
    return mockDb.getBookingsByEmail(email);
  },
  
  create: async (bookingData) => {
    await new Promise(resolve => setTimeout(resolve, 600));
    
    // Conceptually:
    // const response = await apiClient.post('/bookings', bookingData);
    // return response.data;
    
    return mockDb.addBooking(bookingData);
  },
  
  getOccupied: async (movieId, showtime) => {
    await new Promise(resolve => setTimeout(resolve, 200));
    
    // Conceptually:
    // const response = await apiClient.get(`/bookings/occupied?movieId=${movieId}&showtime=${showtime}`);
    // return response.data;
    
    return mockDb.getOccupiedSeats(movieId, showtime);
  }
};

export const adminService = {
  getUsers: async () => {
    await new Promise(resolve => setTimeout(resolve, 400));
    
    // Conceptually:
    // const response = await apiClient.get('/admin/users');
    // return response.data;
    
    return mockDb.getUsers();
  },
  
  updateUserRole: async (email, role) => {
    await new Promise(resolve => setTimeout(resolve, 400));
    
    // Conceptually:
    // const response = await apiClient.put(`/admin/users/${email}/role`, { role });
    // return response.data;
    
    return mockDb.updateUserRole(email, role);
  }
};
