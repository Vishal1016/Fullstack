package com.cinepass.booking_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "show_seats")
@CompoundIndexes({
    @CompoundIndex(name = "movie_showtime_seat_unique", def = "{'movieId': 1, 'showtime': 1, 'seatId': 1}", unique = true)
})
public class ShowSeat {
    @Id
    private String id;
    private String movieId;
    private String showtime;
    private String seatId;
    private SeatStatus status;
    private String bookingId;
    private LocalDateTime lockExpirationTime;

    public ShowSeat() {
    }

    public ShowSeat(String movieId, String showtime, String seatId, SeatStatus status, String bookingId, LocalDateTime lockExpirationTime) {
        this.movieId = movieId;
        this.showtime = showtime;
        this.seatId = seatId;
        this.status = status;
        this.bookingId = bookingId;
        this.lockExpirationTime = lockExpirationTime;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getLockExpirationTime() {
        return lockExpirationTime;
    }

    public void setLockExpirationTime(LocalDateTime lockExpirationTime) {
        this.lockExpirationTime = lockExpirationTime;
    }
}
