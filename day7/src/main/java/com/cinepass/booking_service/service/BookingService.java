package com.cinepass.booking_service.service;

import com.cinepass.booking_service.model.*;
import com.cinepass.booking_service.repository.BookingRepository;
import com.cinepass.booking_service.repository.ShowSeatRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public BookingService(BookingRepository bookingRepository, ShowSeatRepository showSeatRepository, RedisTemplate<String, Object> redisTemplate) {
        this.bookingRepository = bookingRepository;
        this.showSeatRepository = showSeatRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Lock a seat in Redis with a TTL of 5 minutes.
     */
    public boolean lockSeat(String movieId, String showtime, String seatNumber) {
        String key = "seat:" + movieId + ":" + showtime + ":" + seatNumber;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "LOCKED", Duration.ofMinutes(5));
        return Boolean.TRUE.equals(success);
    }

    /**
     * Check if a seat is available (no lock in Redis).
     */
    public boolean isSeatAvailable(String movieId, String showtime, String seatNumber) {
        String key = "seat:" + movieId + ":" + showtime + ":" + seatNumber;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Release a seat lock in Redis.
     */
    public void releaseSeat(String movieId, String showtime, String seatNumber) {
        String key = "seat:" + movieId + ":" + showtime + ":" + seatNumber;
        redisTemplate.delete(key);
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

        // Delete expired locks in ShowSeat (if any legacy exists)
        showSeatRepository.deleteByLockExpirationTimeBeforeAndStatus(now, SeatStatus.LOCKED);
    }

    /**
     * Gets occupied seats (both confirmed bookings and active locks)
     */
    public List<String> getOccupiedSeats(String movieId, String showtime) {
        cleanupExpiredLocks();
        
        // 1. Fetch confirmed booked seats from MongoDB
        List<ShowSeat> showSeats = showSeatRepository.findByMovieIdAndShowtime(movieId, showtime);
        List<String> occupied = showSeats.stream()
                .map(ShowSeat::getSeatId)
                .collect(Collectors.toList());

        // 2. Fetch temporary locks from Redis
        String pattern = "seat:" + movieId + ":" + showtime + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null) {
            for (String key : keys) {
                String[] parts = key.split(":");
                if (parts.length >= 4) {
                    occupied.add(parts[3]);
                }
            }
        }

        return occupied.stream().distinct().collect(Collectors.toList());
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

        // 1. Verify if any seat is already booked in MongoDB (status BOOKED)
        List<ShowSeat> bookedSeats = showSeatRepository.findByMovieIdAndShowtimeAndSeatIdIn(movieId, showtime, seatIds);
        if (!bookedSeats.isEmpty()) {
            List<String> takenSeats = bookedSeats.stream().map(ShowSeat::getSeatId).collect(Collectors.toList());
            throw new IllegalStateException("Seats already booked: " + takenSeats);
        }

        // 2. Initialize Booking
        bookingRequest.setStatus(BookingStatus.INITIATED);
        bookingRequest.setCreatedAt(LocalDateTime.now());
        if (bookingRequest.getDate() == null) {
            bookingRequest.setDate(LocalDateTime.now().toLocalDate().toString());
        }
        Booking savedBooking = bookingRepository.save(bookingRequest);

        // 3. Attempt to lock seats in Redis
        List<String> lockedSeats = new ArrayList<>();
        try {
            LocalDateTime lockExpiration = LocalDateTime.now().plusMinutes(5);
            for (String seatId : seatIds) {
                boolean locked = lockSeat(movieId, showtime, seatId);
                if (!locked) {
                    throw new IllegalStateException("Double Booking Race Condition: One or more selected seats were locked by another user concurrently.");
                }
                lockedSeats.add(seatId);
            }

            // Transition booking status to LOCKED
            savedBooking.setStatus(BookingStatus.LOCKED);
            savedBooking.setLockExpirationTime(lockExpiration);
            savedBooking = bookingRepository.save(savedBooking);

            // 4. Simulate Payment / Immediate Confirmation (since frontend completes payment automatically)
            confirmBooking(savedBooking.getId());

            // Fetch final confirmed booking
            return bookingRepository.findById(savedBooking.getId()).orElse(savedBooking);

        } catch (Exception e) {
            // Rollback: delete any Redis locks we successfully created in this transaction
            for (String seatId : lockedSeats) {
                releaseSeat(movieId, showtime, seatId);
            }
            bookingRepository.deleteById(savedBooking.getId());
            throw new IllegalStateException("Double Booking Race Condition: One or more selected seats were locked by another user concurrently.", e);
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

        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("No seats selected");
        }

        // 1. Check MongoDB if already booked
        List<ShowSeat> bookedSeats = showSeatRepository.findByMovieIdAndShowtimeAndSeatIdIn(movieId, showtime, seatIds);
        if (!bookedSeats.isEmpty()) {
            throw new IllegalStateException("Seats already booked");
        }

        bookingRequest.setStatus(BookingStatus.INITIATED);
        bookingRequest.setCreatedAt(LocalDateTime.now());
        if (bookingRequest.getDate() == null) {
            bookingRequest.setDate(LocalDateTime.now().toLocalDate().toString());
        }
        Booking savedBooking = bookingRepository.save(bookingRequest);

        List<String> lockedSeats = new ArrayList<>();
        try {
            LocalDateTime lockExpiration = LocalDateTime.now().plusMinutes(5);
            for (String seatId : seatIds) {
                boolean locked = lockSeat(movieId, showtime, seatId);
                if (!locked) {
                    throw new IllegalStateException("Double Booking Race Condition");
                }
                lockedSeats.add(seatId);
            }

            savedBooking.setStatus(BookingStatus.LOCKED);
            savedBooking.setLockExpirationTime(lockExpiration);
            return bookingRepository.save(savedBooking);

        } catch (Exception e) {
            for (String seatId : lockedSeats) {
                releaseSeat(movieId, showtime, seatId);
            }
            bookingRepository.deleteById(savedBooking.getId());
            throw new IllegalStateException("Double Booking Race Condition", e);
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
        for (String seatId : booking.getSeats()) {
            ShowSeat showSeat = new ShowSeat(
                    booking.getMovieId(),
                    booking.getShowtime(),
                    seatId,
                    SeatStatus.BOOKED,
                    booking.getId(),
                    null
            );
            showSeatRepository.save(showSeat);
        }

        // Release the Redis locks since the seat is now permanently booked in the database
        for (String seatId : booking.getSeats()) {
            releaseSeat(booking.getMovieId(), booking.getShowtime(), seatId);
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

        // Also release Redis locks (just in case they are still locked)
        for (String seatId : booking.getSeats()) {
            releaseSeat(booking.getMovieId(), booking.getShowtime(), seatId);
        }

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
