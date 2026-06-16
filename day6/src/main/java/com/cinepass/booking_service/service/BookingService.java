package com.cinepass.booking_service.service;

import com.cinepass.booking_service.model.*;
import com.cinepass.booking_service.repository.BookingRepository;
import com.cinepass.booking_service.repository.ShowSeatRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;

    public BookingService(BookingRepository bookingRepository, ShowSeatRepository showSeatRepository) {
        this.bookingRepository = bookingRepository;
        this.showSeatRepository = showSeatRepository;
    }

    /**
     * Cleans up expired locks from the database
     */
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find bookings that have expired locks
        List<Booking> lockedBookings = bookingRepository.findByStatus(BookingStatus.LOCKED);
        for (Booking booking : lockedBookings) {
            if (booking.getLockExpirationTime() != null && booking.getLockExpirationTime().isBefore(now)) {
                booking.setStatus(BookingStatus.EXPIRED);
                bookingRepository.save(booking);
            }
        }

        // Delete expired locks in ShowSeat
        showSeatRepository.deleteByLockExpirationTimeBeforeAndStatus(now, SeatStatus.LOCKED);
    }

    /**
     * Gets occupied seats (both confirmed bookings and active locks)
     */
    public List<String> getOccupiedSeats(String movieId, String showtime) {
        cleanupExpiredLocks();
        List<ShowSeat> showSeats = showSeatRepository.findByMovieIdAndShowtime(movieId, showtime);
        return showSeats.stream()
                .map(ShowSeat::getSeatId)
                .collect(Collectors.toList());
    }

    /**
     * Full atomic booking flow. Attempt to lock seats, and if successful, confirm them.
     */
    public Booking createBooking(Booking bookingRequest) {
        cleanupExpiredLocks();

        String movieId = bookingRequest.getMovieId();
        String showtime = bookingRequest.getShowtime();
        List<String> seatIds = bookingRequest.getSeats();

        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("No seats selected");
        }

        // 1. Verify if any seat is already taken
        List<ShowSeat> existingSeats = showSeatRepository.findByMovieIdAndShowtimeAndSeatIdIn(movieId, showtime, seatIds);
        if (!existingSeats.isEmpty()) {
            List<String> takenSeats = existingSeats.stream().map(ShowSeat::getSeatId).collect(Collectors.toList());
            throw new IllegalStateException("Seats already booked or locked: " + takenSeats);
        }

        // 2. Initialize Booking
        bookingRequest.setStatus(BookingStatus.INITIATED);
        bookingRequest.setCreatedAt(LocalDateTime.now());
        if (bookingRequest.getDate() == null) {
            bookingRequest.setDate(LocalDateTime.now().toLocalDate().toString());
        }
        Booking savedBooking = bookingRepository.save(bookingRequest);

        // 3. Attempt to lock seats (Pessimistic-style write with unique index checking)
        List<ShowSeat> lockedSeats = new ArrayList<>();
        try {
            LocalDateTime lockExpiration = LocalDateTime.now().plusMinutes(5);
            for (String seatId : seatIds) {
                ShowSeat showSeat = new ShowSeat(
                        movieId,
                        showtime,
                        seatId,
                        SeatStatus.LOCKED,
                        savedBooking.getId(),
                        lockExpiration
                );
                ShowSeat savedSeat = showSeatRepository.save(showSeat);
                lockedSeats.add(savedSeat);
            }

            // Transition booking status to LOCKED
            savedBooking.setStatus(BookingStatus.LOCKED);
            savedBooking.setLockExpirationTime(lockExpiration);
            savedBooking = bookingRepository.save(savedBooking);

            // 4. Simulate Payment / Immediate Confirmation (since frontend completes payment automatically)
            confirmBooking(savedBooking.getId());

            // Fetch final confirmed booking
            return bookingRepository.findById(savedBooking.getId()).orElse(savedBooking);

        } catch (DuplicateKeyException e) {
            // Rollback: delete any locks we successfully created in this transaction
            for (ShowSeat seat : lockedSeats) {
                showSeatRepository.deleteById(seat.getId());
            }
            bookingRepository.deleteById(savedBooking.getId());
            throw new IllegalStateException("Double Booking Race Condition: One or more selected seats were locked by another user concurrently.");
        }
    }

    /**
     * Temporarily lock seats (without immediate confirmation).
     */
    public Booking lockSeats(Booking bookingRequest) {
        cleanupExpiredLocks();

        String movieId = bookingRequest.getMovieId();
        String showtime = bookingRequest.getShowtime();
        List<String> seatIds = bookingRequest.getSeats();

        List<ShowSeat> existingSeats = showSeatRepository.findByMovieIdAndShowtimeAndSeatIdIn(movieId, showtime, seatIds);
        if (!existingSeats.isEmpty()) {
            throw new IllegalStateException("Seats already booked or locked");
        }

        bookingRequest.setStatus(BookingStatus.INITIATED);
        bookingRequest.setCreatedAt(LocalDateTime.now());
        if (bookingRequest.getDate() == null) {
            bookingRequest.setDate(LocalDateTime.now().toLocalDate().toString());
        }
        Booking savedBooking = bookingRepository.save(bookingRequest);

        List<ShowSeat> lockedSeats = new ArrayList<>();
        try {
            LocalDateTime lockExpiration = LocalDateTime.now().plusMinutes(5);
            for (String seatId : seatIds) {
                ShowSeat showSeat = new ShowSeat(
                        movieId,
                        showtime,
                        seatId,
                        SeatStatus.LOCKED,
                        savedBooking.getId(),
                        lockExpiration
                );
                ShowSeat savedSeat = showSeatRepository.save(showSeat);
                lockedSeats.add(savedSeat);
            }

            savedBooking.setStatus(BookingStatus.LOCKED);
            savedBooking.setLockExpirationTime(lockExpiration);
            return bookingRepository.save(savedBooking);

        } catch (DuplicateKeyException e) {
            for (ShowSeat seat : lockedSeats) {
                showSeatRepository.deleteById(seat.getId());
            }
            bookingRepository.deleteById(savedBooking.getId());
            throw new IllegalStateException("Double Booking Race Condition");
        }
    }

    /**
     * Confirms a locked booking (post-payment)
     */
    public Booking confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return booking;
        }

        if (booking.getStatus() == BookingStatus.EXPIRED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm booking in status: " + booking.getStatus());
        }

        // Update show seats status to BOOKED and clear lock expiration time
        List<ShowSeat> seats = showSeatRepository.findByMovieIdAndShowtimeAndSeatIdIn(
                booking.getMovieId(), booking.getShowtime(), booking.getSeats()
        );

        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockExpirationTime(null);
            showSeatRepository.save(seat);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setLockExpirationTime(null);
        return bookingRepository.save(booking);
    }

    /**
     * Cancels a booking
     */
    public Booking cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        // Release seats from show_seats
        showSeatRepository.deleteByBookingId(bookingId);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setLockExpirationTime(null);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        cleanupExpiredLocks();
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByEmail(String email) {
        cleanupExpiredLocks();
        return bookingRepository.findByUserEmail(email);
    }
}
