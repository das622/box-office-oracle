package com.david.amcanalytics.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtime_metrics")
@Data
public class ShowtimeMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // THE UPGRADE: Storing which flagship theater this metric belongs to
    private String theaterName;
    private String movieTitle;
    private long budget;
    private long revenue;
    private String releaseDate;
    private String startTime;
    private String showtimeId;
    private int totalSeats;
    private int occupiedSeats;
    private int availableSeats;
    private double fillRate;

    @CreationTimestamp
    private LocalDateTime scrapedAt;
}