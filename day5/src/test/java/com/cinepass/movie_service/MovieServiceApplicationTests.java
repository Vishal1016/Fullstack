package com.cinepass.movie_service;

import com.cinepass.movie_service.model.Movie;
import com.cinepass.movie_service.model.Review;
import com.cinepass.movie_service.repository.MovieRepository;
import com.cinepass.movie_service.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @Test
    void testMovieCrudAndReviewsFlow() throws Exception {
        // 1. Create a Movie (Needs X-User-Role: admin or owner headers to pass SecurityInterceptor)
        Movie movie = new Movie(
            null, 
            "Test Inception", 
            List.of("Sci-Fi", "Thriller"), 
            0.0, 
            "English", 
            148, 
            "2010-07-16", 
            "test-image.jpg", 
            "A mind-bending sci-fi film", 
            12.50, 
            List.of("12:00 PM", "3:00 PM")
        );

        String createMovieResponse = mockMvc.perform(post("/api/movies")
                .header("X-User-Email", "admin@cinepass.com")
                .header("X-User-Role", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("Test Inception")))
                .andExpect(jsonPath("$.data.rating", is(0.0)))
                .andReturn().getResponse().getContentAsString();

        String movieId = objectMapper.readTree(createMovieResponse).path("data").path("id").asText();

        // 2. Fetch the Movie
        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("Test Inception")));

        // 3. Post a Review (Needs X-User-Role: user)
        Review review1 = new Review(null, movieId, "user1", "user1@cinema.com", 5, "Amazing movie!", null);
        mockMvc.perform(post("/api/reviews")
                .header("X-User-Email", "user1@cinema.com")
                .header("X-User-Role", "user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.rating", is(5)));

        // 4. Verify Movie Rating has been aggregated to 5.0
        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating", is(5.0)));

        // 5. Post another Review from a different user
        Review review2 = new Review(null, movieId, "user2", "user2@cinema.com", 3, "Decent movie", null);
        mockMvc.perform(post("/api/reviews")
                .header("X-User-Email", "user2@cinema.com")
                .header("X-User-Role", "user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review2)))
                .andExpect(status().isOk());

        // 6. Verify Movie Rating has been recalculated to (5 + 3)/2 = 4.0
        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating", is(4.0)));

        // 7. Test Pagination and Sorting
        mockMvc.perform(get("/api/movies")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "title")
                .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }
}
