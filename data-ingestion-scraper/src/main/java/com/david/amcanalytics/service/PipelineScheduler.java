package com.david.amcanalytics.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PipelineScheduler {

    private final AmcScoutService scoutService;
    private final AmcScraperService scraperService;
    private final SeatParserService parserService;

    // The Constructor
    public PipelineScheduler(AmcScoutService scoutService, AmcScraperService scraperService, SeatParserService parserService) {
        this.scoutService = scoutService;
        this.scraperService = scraperService;
        this.parserService = parserService;
    }

    @Scheduled(cron = "0 0 * * * *") // Keep this at hourly, or change to "0 * * * * *" for immediate testing
    public void runHourlyVelocityScrape() {
        System.out.println("\n==================================================");
        System.out.println("⏰ AUTOMATED PACER TRIGGERED AT " + LocalDateTime.now());
        System.out.println("==================================================");

        // THE UPGRADE: The Top 4 Highest-Grossing US Megaplexes
        Map<String, String> theaters = Map.of(
                "AMC Empire 25", "https://www.amctheatres.com/movie-theatres/new-york-city/amc-empire-25/showtimes",
                "AMC Lincoln Square 13", "https://www.amctheatres.com/movie-theatres/new-york-city/amc-lincoln-square-13/showtimes",
                "AMC Burbank 16", "https://www.amctheatres.com/movie-theatres/los-angeles/amc-burbank-16/showtimes",
                "AMC Disney Springs 24", "https://www.amctheatres.com/movie-theatres/orlando/amc-disney-springs-24/showtimes"
        );

        for (Map.Entry<String, String> theater : theaters.entrySet()) {
            String theaterName = theater.getKey();
            String theaterUrl = theater.getValue();

            System.out.println("\n🌎 TARGETING REGIONAL NODE: " + theaterName);

            // 1. Send the Scout
            List<AmcScoutService.ShowtimeOffer> showtimes = scoutService.discoverShowtimeIds(theaterUrl);

            int count = 1;
            for (AmcScoutService.ShowtimeOffer currentShowtime : showtimes) {
                String currentId = currentShowtime.id();
                String startTime = currentShowtime.startTime();

                System.out.println("DISPATCHER: Processing ID " + currentId + " for " + startTime + " (" + count + " of " + showtimes.size() + ")");

                scraperService.fetchRawShowtimePayload(currentId)
                        .subscribe(
                                rawHtml -> {
                                    if (rawHtml != null && !rawHtml.isEmpty()) {
                                        parserService.parseSeatsFromHtml(rawHtml, currentId, theaterName, startTime);
                                    }
                                },
                                error -> System.err.println("DISPATCHER: Mission Failed for " + currentId)
                        );

                scoutService.humanSleep(3, 7);
                count++;
            }
        }
        System.out.println("⏰ AUTOMATED REGIONAL SWEEP FINISHED.");
    }
}