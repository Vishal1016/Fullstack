package com.cinepass.booking_service.service;

import com.cinepass.booking_service.model.Show;
import com.cinepass.booking_service.repository.ShowRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShowService {
    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    public Show getShowById(String id) {
        return showRepository.findById(id).orElse(null);
    }

    public List<Show> getShowsByMovieId(String movieId) {
        return showRepository.findByMovieId(movieId);
    }

    public List<Show> getShowsByTheatreId(String theatreId) {
        return showRepository.findByTheatreId(theatreId);
    }

    public Show scheduleShow(Show show) {
        // Validation check for overlapping shows on the same screen at the same time
        List<Show> existingShows = showRepository.findByScreenIdAndStartTime(show.getScreenId(), show.getStartTime());
        if (!existingShows.isEmpty()) {
            throw new IllegalArgumentException("Conflict: An overlapping show already exists on Screen " + show.getScreenId() + " at " + show.getStartTime());
        }
        return showRepository.save(show);
    }
}
