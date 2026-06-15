package com.cinepass.movie_service.controller;

import com.cinepass.movie_service.dto.ApiResponse;
import com.cinepass.movie_service.model.Review;
import com.cinepass.movie_service.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping
public class ReviewController {

    private final MovieService movieService;

    public ReviewController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping({"/api/reviews", "/reviews"})
    public ResponseEntity<ApiResponse<Review>> addReview(
            @Valid @RequestBody Review review, 
            HttpServletRequest request) {
        
        // Extract identity details propagated by API Gateway
        String userEmailHeader = request.getHeader("X-User-Email");
        String userRoleHeader = request.getHeader("X-User-Role");

        if (userEmailHeader != null) {
            review.setUserEmail(userEmailHeader);
            if (review.getUserId() == null) {
                review.setUserId(userEmailHeader); // fallback userId to email
            }
        } else {
            // Direct call fallback
            if (review.getUserEmail() == null) {
                review.setUserEmail("anonymous@cinepass.com");
            }
            if (review.getUserId() == null) {
                review.setUserId("anonymous");
            }
        }

        Review savedReview = movieService.addOrUpdateReview(review);
        return ResponseEntity.ok(ApiResponse.success("Review posted successfully", savedReview));
    }

    @GetMapping({"/api/reviews/{movieId}", "/reviews/{movieId}", "/api/movies/{movieId}/reviews", "/movies/{movieId}/reviews"})
    public ResponseEntity<ApiResponse<List<Review>>> getReviews(@PathVariable String movieId) {
        List<Review> reviews = movieService.getReviewsForMovie(movieId);
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched successfully", reviews));
    }
}
