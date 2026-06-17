package com.cinepass.booking_service.repository;

import com.cinepass.booking_service.model.Show;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ShowRepository extends MongoRepository<Show, String> {
    List<Show> findByMovieId(String movieId);
    List<Show> findByTheatreId(String theatreId);
    List<Show> findByScreenIdAndStartTime(String screenId, String startTime);
}
