import React from 'react';
import { Star, Clock, Trash2, Calendar } from 'lucide-react';
import Button from './Button';
import './MovieCard.css';

const MovieCard = ({
  movie,
  onBook,
  onDelete,
  role,
  isAdminOrOwner = false
}) => {
  const { id, title, rating, genre, duration, description, poster, price } = movie;

  return (
    <div className="movie-card glass-panel animate-fade-in">
      <div className="movie-poster-wrapper">
        <img src={poster} alt={title} className="movie-poster" loading="lazy" />
        <div className="movie-rating-badge">
          <Star size={14} className="star-icon" fill="currentColor" />
          <span>{rating.toFixed(1)}</span>
        </div>
        <div className="movie-price-tag">
          ${price}
        </div>
      </div>
      
      <div className="movie-info">
        <span className="movie-genre">{genre}</span>
        <h3 className="movie-title">{title}</h3>
        <p className="movie-description">{description}</p>
        
        <div className="movie-metadata">
          <div className="meta-item">
            <Clock size={14} />
            <span>{duration}</span>
          </div>
          <div className="meta-item">
            <Calendar size={14} />
            <span>{movie.showtimes?.length || 0} Shows</span>
          </div>
        </div>
        
        <div className="movie-actions">
          {!isAdminOrOwner ? (
            <Button
              variant="primary"
              className="w-full"
              onClick={() => onBook(id)}
            >
              Book Tickets
            </Button>
          ) : (
            <div className="admin-actions">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onBook(id)} // Can still view schedule
                className="flex-1"
              >
                View Shows
              </Button>
              {onDelete && (
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => onDelete(id)}
                  className="btn-icon-only"
                  title="Delete Movie"
                >
                  <Trash2 size={16} />
                </Button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MovieCard;
