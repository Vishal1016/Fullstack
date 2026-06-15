package com.cinepass.movie_service.controller;

import com.cinepass.movie_service.dto.ApiResponse;
import com.cinepass.movie_service.model.Movie;
import com.cinepass.movie_service.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping({"/api/movies", "/movies"})
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // ==========================================
    // CRUD Operations
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<Movie>> createMovie(@Valid @RequestBody Movie movie) {
        Movie createdMovie = movieService.createMovie(movie);
        return ResponseEntity.ok(ApiResponse.success("Movie created successfully", createdMovie));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Movie>>> getMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Movie> movies = movieService.searchMovies(title, genre, minRating, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Movies fetched successfully", movies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> getMovieById(@PathVariable String id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success("Movie fetched successfully", movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> updateMovie(@PathVariable String id, @RequestBody Movie movie) {
        Movie updatedMovie = movieService.updateMovie(id, movie);
        return ResponseEntity.ok(ApiResponse.success("Movie updated successfully", updatedMovie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Movie deleted successfully", null));
    }

    // ==========================================
    // Search API
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Movie>>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Double rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Movie> moviesPage = movieService.searchMovies(title, genre, rating, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Search results fetched successfully", moviesPage.getContent()));
    }

    // ==========================================
    // File Upload APIs
    // ==========================================

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadPoster(@RequestParam("file") MultipartFile file) {
        String posterUrl = movieService.storePoster(file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", posterUrl));
    }

    @PostMapping("/{id}/poster")
    public ResponseEntity<ApiResponse<Movie>> attachPoster(
            @PathVariable String id, 
            @RequestParam("file") MultipartFile file) {
        Movie updatedMovie = movieService.attachPosterToMovie(id, file);
        return ResponseEntity.ok(ApiResponse.success("Movie poster updated successfully", updatedMovie));
    }
}
