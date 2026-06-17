package com.cinepass.booking_service.controller;

import com.cinepass.booking_service.dto.ApiResponse;
import com.cinepass.booking_service.model.Booking;
import com.cinepass.booking_service.model.Show;
import com.cinepass.booking_service.model.Theatre;
import com.cinepass.booking_service.service.BookingService;
import com.cinepass.booking_service.service.ShowService;
import com.cinepass.booking_service.service.TheatreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/bookings", "/bookings"})
public class BookingController {

    private final BookingService bookingService;
    private final TheatreService theatreService;
    private final ShowService showService;

    public BookingController(BookingService bookingService, TheatreService theatreService, ShowService showService) {
        this.bookingService = bookingService;
        this.theatreService = theatreService;
        this.showService = showService;
    }

    // ==========================================
    // Core Booking endpoints
    // ==========================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookingService.getAllBookings()));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByUser(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", bookingService.getBookingsByEmail(email)));
    }

    @GetMapping("/occupied")
    public ResponseEntity<ApiResponse<List<String>>> getOccupiedSeats(
            @RequestParam String movieId,
            @RequestParam String showtime) {
        return ResponseEntity.ok(ApiResponse.success("Occupied seats retrieved successfully", bookingService.getOccupiedSeats(movieId, showtime)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(@RequestBody Booking bookingRequest) {
        try {
            Booking booking = bookingService.createBooking(bookingRequest);
            return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Secondary booking endpoint for academic mapping compatibility
    @PostMapping("/booking")
    public ResponseEntity<ApiResponse<Booking>> createBookingAlternative(@RequestBody Booking bookingRequest) {
        return createBooking(bookingRequest);
    }

    // ==========================================
    // Theatre & Show Scheduling endpoints
    // ==========================================

    @GetMapping("/theatres")
    public ResponseEntity<ApiResponse<List<Theatre>>> getAllTheatres() {
        return ResponseEntity.ok(ApiResponse.success("Theatres retrieved successfully", theatreService.getAllTheatres()));
    }

    @GetMapping("/shows")
    public ResponseEntity<ApiResponse<List<Show>>> getAllShows() {
        return ResponseEntity.ok(ApiResponse.success("Shows retrieved successfully", showService.getAllShows()));
    }

    @GetMapping("/seats/{showId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatLayout(@PathVariable String showId) {
        Show show = showService.getShowById(showId);
        if (show == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Show not found with ID: " + showId));
        }
        
        List<String> occupied = bookingService.getOccupiedSeats(show.getMovieId(), show.getStartTime());
        
        Map<String, Object> seatLayout = new HashMap<>();
        seatLayout.put("showId", showId);
        seatLayout.put("movieId", show.getMovieId());
        seatLayout.put("screenId", show.getScreenId());
        seatLayout.put("showtime", show.getStartTime());
        seatLayout.put("occupiedSeats", occupied);
        
        return ResponseEntity.ok(ApiResponse.success("Seat layout retrieved successfully", seatLayout));
    }

    // ==========================================
    // Locking & FSM Lifecycle State endpoints
    // ==========================================

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<Booking>> lockSeats(@RequestBody Booking bookingRequest) {
        try {
            Booking booking = bookingService.lockSeats(bookingRequest);
            return ResponseEntity.ok(ApiResponse.success("Seats locked temporarily for 5 minutes", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<Booking>> confirmBooking(@PathVariable String bookingId) {
        try {
            Booking booking = bookingService.confirmBooking(bookingId);
            return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(@PathVariable String bookingId) {
        try {
            Booking booking = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(ApiResponse.success("Booking cancelled and seats released", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
