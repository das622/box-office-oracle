package com.david.amcanalytics.repository;

import com.david.amcanalytics.model.ShowtimeMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowtimeMetricRepository extends JpaRepository<ShowtimeMetric, Long> {
    // THE UPGRADE: Spring instantly knows how to search the 'theaterName' column!
    List<ShowtimeMetric> findByTheaterName(String theaterName);

    // Spring instantly knows how to search the 'showtimeId' column!
    List<ShowtimeMetric> findByShowtimeId(String showtimeId);
}