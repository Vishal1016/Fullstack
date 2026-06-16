package com.cinepass.booking_service;

import com.cinepass.booking_service.model.*;
import com.cinepass.booking_service.repository.*;
import com.cinepass.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        showSeatRepository.deleteAll();
        
        // Ensure index exists for concurrent test run
        mongoTemplate.indexOps(ShowSeat.class).ensureIndex(
            new org.springframework.data.mongodb.core.index.CompoundIndexDefinition(
                new org.bson.Document("movieId", 1)
                    .append("showtime", 1)
                    .append("seatId", 1)
            ).unique().named("movie_showtime_seat_unique")
        );
    }

    @Test
    void testBasicBookingFlow() throws Exception {
        // 1. Fetch default seeded theatres
        mockMvc.perform(get("/api/bookings/theatres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        // 2. Create a Booking
        Booking bookingRequest = new Booking();
        bookingRequest.setMovieId("1");
        bookingRequest.setMovieTitle("Dune: Part Two");
        bookingRequest.setSeats(List.of("A3", "A4"));
        bookingRequest.setTotalPrice(30.0);
        bookingRequest.setShowtime("6:30 PM");
        bookingRequest.setUserEmail("user@cinema.com");

        mockMvc.perform(post("/api/bookings")
                .header("X-User-Email", "user@cinema.com")
                .header("X-User-Role", "user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.data.seats", containsInAnyOrder("A3", "A4")));

        // 3. Verify occupied seats
        mockMvc.perform(get("/api/bookings/occupied")
                .param("movieId", "1")
                .param("showtime", "6:30 PM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", containsInAnyOrder("A3", "A4")));
    }

    @Test
    void testConcurrencyDoubleBookingPrevention() throws InterruptedException {
        String movieId = "99";
        String showtime = "10:00 PM";
        String seatId = "C4";

        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String email = "user" + i + "@cinema.com";
            tasks.add(() -> {
                // Wait for the latch to fire to maximize race condition probability
                latch.await();
                
                Booking booking = new Booking();
                booking.setMovieId(movieId);
                booking.setMovieTitle("Concurrent Movie");
                booking.setSeats(Collections.singletonList(seatId));
                booking.setTotalPrice(15.0);
                booking.setShowtime(showtime);
                booking.setUserEmail(email);

                try {
                    bookingService.createBooking(booking);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("Booking failed for " + email + ": " + e.getMessage());
                }
                return null;
            });
        }

        // Submit tasks
        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }

        // Fire all threads simultaneously
        latch.countDown();

        // Wait for all threads to complete
        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(finished, "Executor did not finish in time");

        // Assert that exactly ONE booking succeeded, and the rest failed
        assertEquals(1, successCount.get(), "Exactly one booking should succeed under concurrency");
        assertEquals(threadCount - 1, failureCount.get(), "All other concurrent bookings should fail");

        // Verify that only one confirmed booking exists in DB
        List<Booking> bookings = bookingRepository.findByMovieIdAndShowtime(movieId, showtime);
        assertEquals(1, bookings.size(), "Only 1 booking document should exist in database");
        assertEquals(BookingStatus.CONFIRMED, bookings.get(0).getStatus());

        // Verify that exactly 1 occupied seat exists in show_seats collection
        List<ShowSeat> showSeats = showSeatRepository.findByMovieIdAndShowtime(movieId, showtime);
        assertEquals(1, showSeats.size(), "Only 1 seat should be marked in show_seats");
        assertEquals(SeatStatus.BOOKED, showSeats.get(0).getStatus());
    }
}
