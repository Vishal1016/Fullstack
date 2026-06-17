package com.cinepass.booking_service.repository;

import com.cinepass.booking_service.model.Booking;
import com.cinepass.booking_service.model.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserEmail(String userEmail);
    List<Booking> findByMovieIdAndShowtime(String movieId, String showtime);
    List<Booking> findByStatus(BookingStatus status);
}
