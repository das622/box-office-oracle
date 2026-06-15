package com.david.amcanalytics.controller;

import com.david.amcanalytics.model.ShowtimeMetric;
import com.david.amcanalytics.repository.ShowtimeMetricRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 1. Tell Spring this class is a Drive-Thru Window that returns JSON
@RestController
// 2. Set the base URL address for this window
@RequestMapping("/api/v1/metrics")
public class ShowtimeController {

    private final ShowtimeMetricRepository repository;

    // Bring the File Clerk to the window
    public ShowtimeController(ShowtimeMetricRepository repository) {
        this.repository = repository;
    }

    // ENDPOINT 1: Get absolutely everything
    // URL: http://localhost:8080/api/v1/metrics
    @GetMapping
    public List<ShowtimeMetric> getAllMetrics() {
        return repository.findAll();
    }

    // ENDPOINT 2: Get metrics for a specific theater
    // URL: http://localhost:8080/api/v1/metrics/theater/AMC Empire 25
    @GetMapping("/theater/{theaterName}")
    public List<ShowtimeMetric> getMetricsByTheater(@PathVariable String theaterName) {
        return repository.findByTheaterName(theaterName);
    }

    // ENDPOINT 3: Get the historical timeline for ONE specific movie
    // URL: http://localhost:8080/api/v1/metrics/showtime/143971693
    @GetMapping("/showtime/{showtimeId}")
    public List<ShowtimeMetric> getMetricsByShowtimeId(@PathVariable String showtimeId) {
        return repository.findByShowtimeId(showtimeId);
    }
}