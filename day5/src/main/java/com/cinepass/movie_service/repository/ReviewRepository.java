package com.cinepass.movie_service.repository;

import com.cinepass.movie_service.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByMovieId(String movieId);
    Optional<Review> findByMovieIdAndUserEmail(String movieId, String userEmail);
}
