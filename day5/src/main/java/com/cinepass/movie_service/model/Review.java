package com.cinepass.movie_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "reviews")
@CompoundIndexes({
    @CompoundIndex(name = "movie_user_idx", def = "{'movieId': 1, 'userEmail': 1}", unique = true)
})
public class Review {
    @Id
    private String id;

    @NotBlank(message = "Movie ID is required")
    private String movieId;

    private String userId;
    
    private String userEmail;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    private String review;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {
    }

    public Review(String id, String movieId, String userId, String userEmail, Integer rating, String review, LocalDateTime createdAt) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.rating = rating;
        this.review = review;
        if (createdAt != null) {
            this.createdAt = createdAt;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
