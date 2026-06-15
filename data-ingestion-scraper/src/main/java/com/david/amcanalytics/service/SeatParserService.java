package com.david.amcanalytics.service;

import com.david.amcanalytics.model.ShowtimeMetric;
import com.david.amcanalytics.repository.ShowtimeMetricRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class SeatParserService {

    private final ShowtimeMetricRepository repository;
    private final TmdbFinancialService tmdbService;

    // THE FIX: Ensure TmdbFinancialService is inside these parentheses!
    public SeatParserService(ShowtimeMetricRepository repository, TmdbFinancialService tmdbService) {
        this.repository = repository;
        this.tmdbService = tmdbService;
    }

    public void parseSeatsFromHtml(String rawHtml, String showtimeId, String theaterName, String startTime) {
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(rawHtml);

            // THE ULTIMATE NET: Grab every type of physical chair AMC manufactures
            org.jsoup.select.Elements seats = doc.select("[aria-label*='Seat'], [aria-label*='Recliner'], [aria-label*='Rocker']");

            if (seats.isEmpty()) {
                System.err.println("Parser Alert: Could not find any HTML elements with seat labels.");
                return;
            }

            int availableCount = 0;
            int occupiedCount = 0;

            for (org.jsoup.nodes.Element seat : seats) {
                String ariaLabelValue = seat.attr("aria-label");

                // The Binary Sort: If it says occupied, it's taken. Everything else is open.
                if (ariaLabelValue.contains("Occupied") || ariaLabelValue.contains("Unavailable")) {
                    occupiedCount++;
                } else {
                    availableCount++;
                }
            }

            int totalSeats = availableCount + occupiedCount;

            if (totalSeats == 0) {
                System.err.println("Parser Alert: Total seats calculated as 0. Skipping math.");
                return;
            }

            double fillRate = ((double) occupiedCount / totalSeats) * 100;
            // --- NEW ENRICHMENT LOGIC ---
            // --- NEW ENRICHMENT LOGIC ---
            String movieTitle = "Unknown Title";

            // The "Whack-a-Mole" Blacklist: Add any generic AMC interface phrases here
            java.util.List<String> blacklistedHeaders = java.util.Arrays.asList(
                    "Select Seats",
                    "Showtime Information",
                    "Seat Information",
                    "Order Summary",
                    "Checkout",
                    "Important Information"
            );

            org.jsoup.select.Elements headers = doc.select("h1, h2, h3");

            for (org.jsoup.nodes.Element header : headers) {
                String text = header.text().trim();

                if (text.isEmpty()) continue; // Skip blank tags entirely

                // Check if this text matches anything in our blacklist
                boolean isBlacklisted = false;
                for (String badHeader : blacklistedHeaders) {
                    if (text.equalsIgnoreCase(badHeader)) {
                        isBlacklisted = true;
                        break;
                    }
                }

                // If it survived the blacklist, it's our movie!
                if (!isBlacklisted) {
                    movieTitle = text;
                    break;
                }
            }

            // Ask TMDB for the money!
            // Ask TMDB for the data! (Using the new Record object)
            TmdbFinancialService.MovieFinancials financials = tmdbService.fetchFinancials(movieTitle);

            // FILLING OUT THE SUMMARY CARD
            com.david.amcanalytics.model.ShowtimeMetric metric = new com.david.amcanalytics.model.ShowtimeMetric();
            metric.setTheaterName(theaterName);
            metric.setShowtimeId(showtimeId);
            metric.setTotalSeats(totalSeats);
            metric.setOccupiedSeats(occupiedCount);
            metric.setAvailableSeats(availableCount);
            metric.setFillRate(fillRate);

            // Stamp the new data!
            metric.setMovieTitle(movieTitle);
            metric.setBudget(financials.budget());
            metric.setRevenue(financials.revenue());
            metric.setStartTime(startTime);
            metric.setReleaseDate(financials.releaseDate()); // The new missing ingredient!

            repository.save(metric);

            System.out.println("DATABASE: Successfully saved metrics for " + theaterName + " to PostgreSQL!");
            System.out.println("Total Auditorium Capacity : " + totalSeats);
            System.out.println("Tickets Sold So Far        : " + occupiedCount);
            System.out.println("Available Empty Seats      : " + availableCount);
            System.out.println("Current Capacity Fill Rate : " + String.format("%.2f", fillRate) + "%");

        } catch (Exception e) {
            System.err.println("Critical Error running the data parser: " + e.getMessage());
        }
    }
}