package com.cinepass.booking_service.repository;

import com.cinepass.booking_service.model.Theatre;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TheatreRepository extends MongoRepository<Theatre, String> {
    List<Theatre> findByLocation(String location);
}
