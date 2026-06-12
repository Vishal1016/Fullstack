import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Film, SlidersHorizontal, Plus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { movieService } from '../services/api';
import MovieCard from '../components/atomic/MovieCard';
import Button from '../components/atomic/Button';
import './MovieCatalog.css';

const MovieCatalog = () => {
  const { user, isOwner, isAdmin } = useAuth();
  const navigate = useNavigate();

  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('All');

  const fetchMovies = async () => {
    setLoading(true);
    try {
      const data = await movieService.getAll();
      setMovies(data);
    } catch (err) {
      console.error('Failed to load movies:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies();
  }, []);

  const handleBook = (movieId) => {
    navigate(`/booking/${movieId}`);
  };

  const handleDelete = async (movieId) => {
    if (window.confirm('Are you sure you want to remove this movie?')) {
      try {
        await movieService.delete(movieId);
        fetchMovies(); // reload
      } catch (err) {
        alert('Failed to delete movie');
      }
    }
  };

  // Extract unique genres for filtering
  const genres = ['All', ...new Set(movies.map(m => m.genre.split('/')[0].trim()))];

  // Filter logic
  const filteredMovies = movies.filter((movie) => {
    const matchesSearch = movie.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                          movie.description.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesGenre = selectedGenre === 'All' || 
                         movie.genre.includes(selectedGenre);

    return matchesSearch && matchesGenre;
  });

  const isAdminOrOwner = isAdmin || isOwner;

  return (
    <div className="movie-catalog-page animate-fade-in">
      <div className="catalog-header">
        <div className="header-info">
          <h2>Now Showing</h2>
          <p>Browse active listings, view schedules, and secure your reservations.</p>
        </div>
        {isAdminOrOwner && (
          <Button
            variant="primary"
            icon={Plus}
            onClick={() => navigate('/dashboard#add-movie')}
          >
            Add New Movie
          </Button>
        )}
      </div>

      {/* Filter and search bar */}
      <div className="filters-panel glass-panel">
        <div className="search-box-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            placeholder="Search movie title, synopsis, keywords..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="catalog-search-input"
          />
        </div>

        <div className="genre-filters">
          <SlidersHorizontal size={16} className="filter-icon" />
          <div className="genre-pills">
            {genres.map((genre) => (
              <button
                key={genre}
                onClick={() => setSelectedGenre(genre)}
                className={`genre-pill-btn ${selectedGenre === genre ? 'genre-pill-active' : ''}`}
              >
                {genre}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Movies Grid */}
      {loading ? (
        <div className="catalog-loading">
          <div className="spinner"></div>
          <p>Refreshing Theater Catalog...</p>
        </div>
      ) : filteredMovies.length === 0 ? (
        <div className="no-movies-panel glass-panel">
          <Film size={48} className="no-movies-icon" />
          <h3>No Movies Found</h3>
          <p>We couldn't find any listings matching your filter criteria. Try expanding your search terms.</p>
          <Button variant="secondary" onClick={() => { setSearchQuery(''); setSelectedGenre('All'); }}>
            Reset Filters
          </Button>
        </div>
      ) : (
        <div className="movies-grid grid-container grid-cols-4">
          {filteredMovies.map((movie) => (
            <MovieCard
              key={movie.id}
              movie={movie}
              onBook={handleBook}
              onDelete={handleDelete}
              role={user?.role}
              isAdminOrOwner={isAdminOrOwner}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default MovieCatalog;
