package com.cinepass.movie_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "movies")
public class Movie {
    @Id
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    private List<String> genre;
    private Double rating;
    private String language;
    private Integer duration;
    private String releaseDate;
    private String posterUrl;
    private String description;
    private Double price;
    private List<String> showtimes;

    public Movie() {
    }

    public Movie(String id, String title, List<String> genre, Double rating, String language, Integer duration, String releaseDate, String posterUrl, String description, Double price, List<String> showtimes) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.language = language;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.description = description;
        this.price = price;
        this.showtimes = showtimes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<String> showtimes) {
        this.showtimes = showtimes;
    }
}
