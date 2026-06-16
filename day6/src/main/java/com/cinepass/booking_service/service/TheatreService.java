package com.cinepass.booking_service.service;

import com.cinepass.booking_service.model.Theatre;
import com.cinepass.booking_service.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheatreService {
    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    public List<Theatre> getAllTheatres() {
        return theatreRepository.findAll();
    }

    public Theatre getTheatreById(String id) {
        return theatreRepository.findById(id).orElse(null);
    }

    public Theatre createTheatre(Theatre theatre) {
        return theatreRepository.save(theatre);
    }

    public List<Theatre> getTheatresByLocation(String location) {
        return theatreRepository.findByLocation(location);
    }
}
