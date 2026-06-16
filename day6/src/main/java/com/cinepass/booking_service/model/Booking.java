package com.cinepass.booking_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    private String movieId;
    private String movieTitle;
    private List<String> seats;
    private double totalPrice;
    private String showtime;
    private String userEmail;
    private String date; // YYYY-MM-DD
    private BookingStatus status;
    private LocalDateTime lockExpirationTime;
    private LocalDateTime createdAt;

    public Booking() {
    }

    public Booking(String id, String movieId, String movieTitle, List<String> seats, double totalPrice,
                   String showtime, String userEmail, String date, BookingStatus status,
                   LocalDateTime lockExpirationTime, LocalDateTime createdAt) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.showtime = showtime;
        this.userEmail = userEmail;
        this.date = date;
        this.status = status;
        this.lockExpirationTime = lockExpirationTime;
        this.createdAt = createdAt;
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

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getLockExpirationTime() {
        return lockExpirationTime;
    }

    public void setLockExpirationTime(LocalDateTime lockExpirationTime) {
        this.lockExpirationTime = lockExpirationTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
