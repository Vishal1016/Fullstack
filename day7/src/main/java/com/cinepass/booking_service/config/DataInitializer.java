package com.cinepass.booking_service.config;

import com.cinepass.booking_service.model.*;
import com.cinepass.booking_service.repository.*;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            TheatreRepository theatreRepository,
            ShowRepository showRepository,
            BookingRepository bookingRepository,
            ShowSeatRepository showSeatRepository,
            MongoTemplate mongoTemplate) {
        
        return args -> {
            // Ensure unique compound index is created on show_seats
            System.out.println("[DataInitializer] Ensuring unique compound index on show_seats (movieId, showtime, seatId)...");
            mongoTemplate.indexOps(ShowSeat.class).ensureIndex(
                new CompoundIndexDefinition(
                    new Document("movieId", 1)
                        .append("showtime", 1)
                        .append("seatId", 1)
                ).unique().named("movie_showtime_seat_unique")
            );

            // 1. Check and initialize Theatres
            if (theatreRepository.count() == 0) {
                System.out.println("[DataInitializer] Seeding default Theatres...");
                
                Screen screen1 = new Screen("S1", "Audi 1", 48); // 48 is rows A-F, cols 1-8
                Screen screen2 = new Screen("S2", "Audi 2", 48);
                Theatre pvr = new Theatre("T1", "PVR Cinemas", "Delhi", List.of(screen1, screen2));

                Screen screen3 = new Screen("S3", "Audi 1 VIP", 24);
                Theatre cinepolis = new Theatre("T2", "Cinepolis", "Mumbai", List.of(screen3));

                theatreRepository.saveAll(List.of(pvr, cinepolis));
            }

            // 2. Check and initialize Shows
            if (showRepository.count() == 0) {
                System.out.println("[DataInitializer] Seeding default movie Shows...");
                
                // Movie 1 (Dune: Part Two)
                Show show1 = new Show("SH1", "1", "T1", "S1", "10:30 AM");
                Show show2 = new Show("SH2", "1", "T1", "S1", "6:30 PM");
                
                // Movie 2 (Oppenheimer)
                Show show3 = new Show("SH3", "2", "T1", "S1", "3:15 PM");
                
                // Movie 3 (Spider-Man: Across the Spider-Verse)
                Show show4 = new Show("SH4", "3", "T1", "S2", "12:00 PM");
                Show show5 = new Show("SH5", "3", "T1", "S2", "8:00 PM");

                // Movie 4 (Interstellar)
                Show show6 = new Show("SH6", "4", "T1", "S2", "4:30 PM");

                showRepository.saveAll(List.of(show1, show2, show3, show4, show5, show6));
            }

            System.out.println("[DataInitializer] Database initialized successfully. Current counts - Theatres: " 
                    + theatreRepository.count() + ", Shows: " + showRepository.count() 
                    + ", Bookings: " + bookingRepository.count() + ", ShowSeats: " + showSeatRepository.count());
        };
    }
}
