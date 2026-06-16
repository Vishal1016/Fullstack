package com.cinepass.booking_service.repository;

import com.cinepass.booking_service.model.SeatStatus;
import com.cinepass.booking_service.model.ShowSeat;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowSeatRepository extends MongoRepository<ShowSeat, String> {
    List<ShowSeat> findByMovieIdAndShowtime(String movieId, String showtime);
    List<ShowSeat> findByMovieIdAndShowtimeAndSeatIdIn(String movieId, String showtime, List<String> seatIds);
    List<ShowSeat> findByLockExpirationTimeBeforeAndStatus(LocalDateTime time, SeatStatus status);
    void deleteByLockExpirationTimeBeforeAndStatus(LocalDateTime time, SeatStatus status);
    void deleteByBookingId(String bookingId);
}
