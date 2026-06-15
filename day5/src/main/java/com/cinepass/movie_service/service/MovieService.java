package com.cinepass.movie_service.service;

import com.cinepass.movie_service.exception.ResourceNotFoundException;
import com.cinepass.movie_service.model.Movie;
import com.cinepass.movie_service.model.Review;
import com.cinepass.movie_service.repository.MovieRepository;
import com.cinepass.movie_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public MovieService(MovieRepository movieRepository, ReviewRepository reviewRepository, MongoTemplate mongoTemplate) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // ==========================================
    // Movie CRUD Operations
    // ==========================================

    public Movie createMovie(Movie movie) {
        if (movie.getRating() == null) {
            movie.setRating(0.0);
        }
        return movieRepository.save(movie);
    }

    public Movie getMovieById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    public Movie updateMovie(String id, Movie updatedMovie) {
        Movie existingMovie = getMovieById(id);
        
        if (updatedMovie.getTitle() != null) existingMovie.setTitle(updatedMovie.getTitle());
        if (updatedMovie.getGenre() != null) existingMovie.setGenre(updatedMovie.getGenre());
        if (updatedMovie.getLanguage() != null) existingMovie.setLanguage(updatedMovie.getLanguage());
        if (updatedMovie.getDuration() != null) existingMovie.setDuration(updatedMovie.getDuration());
        if (updatedMovie.getReleaseDate() != null) existingMovie.setReleaseDate(updatedMovie.getReleaseDate());
        if (updatedMovie.getPosterUrl() != null) existingMovie.setPosterUrl(updatedMovie.getPosterUrl());
        if (updatedMovie.getDescription() != null) existingMovie.setDescription(updatedMovie.getDescription());
        if (updatedMovie.getPrice() != null) existingMovie.setPrice(updatedMovie.getPrice());
        if (updatedMovie.getShowtimes() != null) existingMovie.setShowtimes(updatedMovie.getShowtimes());
        // Note: Rating is recalculated dynamically from reviews, but we preserve it if passed and no reviews exist
        if (updatedMovie.getRating() != null && reviewRepository.findByMovieId(id).isEmpty()) {
            existingMovie.setRating(updatedMovie.getRating());
        }

        return movieRepository.save(existingMovie);
    }

    public void deleteMovie(String id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
        
        // Cascade delete reviews for this movie
        List<Review> reviews = reviewRepository.findByMovieId(id);
        reviewRepository.deleteAll(reviews);
    }

    // ==========================================
    // Dynamic Query, Pagination, & Search
    // ==========================================

    public Page<Movie> searchMovies(String title, String genre, Double minRating, Pageable pageable) {
        Query query = new Query().with(pageable);

        if (title != null && !title.trim().isEmpty()) {
            query.addCriteria(Criteria.where("title").regex(title, "i"));
        }
        if (genre != null && !genre.trim().isEmpty()) {
            query.addCriteria(Criteria.where("genre").regex(genre, "i"));
        }
        if (minRating != null) {
            query.addCriteria(Criteria.where("rating").gte(minRating));
        }

        List<Movie> movies = mongoTemplate.find(query, Movie.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Movie.class);

        return PageableExecutionUtils.getPage(movies, pageable, () -> count);
    }

    // ==========================================
    // File Upload Storage Operations
    // ==========================================

    public String storePoster(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Clean filename to prevent path injection
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path destinationPath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(uniqueFilename);

            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // Return path endpoint served by our static handler
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public Movie attachPosterToMovie(String movieId, MultipartFile file) {
        Movie movie = getMovieById(movieId);
        String posterUrl = storePoster(file);
        movie.setPosterUrl(posterUrl);
        return movieRepository.save(movie);
    }

    // ==========================================
    // Reviews & Rating System
    // ==========================================

    public Review addOrUpdateReview(Review review) {
        // Ensure movie exists
        Movie movie = getMovieById(review.getMovieId());

        // Check if user has already reviewed this movie to prevent duplicates
        Optional<Review> existingReviewOpt = reviewRepository.findByMovieIdAndUserEmail(review.getMovieId(), review.getUserEmail());
        Review savedReview;

        if (existingReviewOpt.isPresent()) {
            Review existingReview = existingReviewOpt.get();
            existingReview.setRating(review.getRating());
            existingReview.setReview(review.getReview());
            existingReview.setCreatedAt(LocalDateTime.now());
            savedReview = reviewRepository.save(existingReview);
        } else {
            review.setCreatedAt(LocalDateTime.now());
            savedReview = reviewRepository.save(review);
        }

        // Trigger dynamic rating recalculation
        recalculateMovieRating(review.getMovieId());

        return savedReview;
    }

    public List<Review> getReviewsForMovie(String movieId) {
        // Ensure movie exists
        getMovieById(movieId);
        return reviewRepository.findByMovieId(movieId);
    }

    private void recalculateMovieRating(String movieId) {
        List<Review> reviews = reviewRepository.findByMovieId(movieId);
        Movie movie = getMovieById(movieId);

        if (reviews.isEmpty()) {
            movie.setRating(0.0);
        } else {
            double sum = 0.0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            double avg = sum / reviews.size();
            // Round to 1 decimal place
            avg = Math.round(avg * 10.0) / 10.0;
            movie.setRating(avg);
        }

        movieRepository.save(movie);
    }
}
