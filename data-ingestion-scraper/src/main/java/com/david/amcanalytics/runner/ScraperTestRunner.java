package com.david.amcanalytics.runner;

import com.david.amcanalytics.service.AmcScoutService;
import com.david.amcanalytics.service.AmcScraperService;
import com.david.amcanalytics.service.SeatParserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class ScraperTestRunner implements CommandLineRunner {

    private final AmcScoutService scoutService;
    private final AmcScraperService scraperService;
    private final SeatParserService parserService;

    // Injecting all three members of our team!
    public ScraperTestRunner(AmcScoutService scoutService, AmcScraperService scraperService, SeatParserService parserService) {
        this.scoutService = scoutService;
        this.scraperService = scraperService;
        this.parserService = parserService;
    }

    @Override
    public void run(String... args) {
        System.out.println("==================================================");
        System.out.println("DISPATCHER: Starting Production Pipeline...");

        String theaterName = "AMC Empire 25";
        String theaterUrl = "https://www.amctheatres.com/movie-theatres/new-york-city/amc-empire-25/showtimes";

        // 1. Send the upgraded Scout
        List<AmcScoutService.ShowtimeOffer> showtimes = scoutService.discoverShowtimeIds(theaterUrl);

        if (showtimes.isEmpty()) {
            System.out.println("DISPATCHER: Scout found no IDs. Aborting mission.");
            return;
        }

        // 2. The Assembly Line Loop (Using the new Record!)
        for (int i = 0; i < showtimes.size(); i++) {
            AmcScoutService.ShowtimeOffer currentShowtime = showtimes.get(i);
            String currentId = currentShowtime.id();
            String startTime = currentShowtime.startTime(); // Here is the missing time!

            System.out.println("\nDISPATCHER: Processing ID " + currentId + " for " + startTime + " (" + (i + 1) + " of " + showtimes.size() + ")");

            scraperService.fetchRawShowtimePayload(currentId)
                    .subscribe(
                            rawHtml -> {
                                if (rawHtml != null && !rawHtml.isEmpty()) {
                                    parserService.parseSeatsFromHtml(rawHtml, currentId, theaterName, startTime);
                                }
                            },
                            error -> System.err.println("DISPATCHER: Mission Failed for " + currentId)
                    );

            if (i < showtimes.size() - 1) {
                scoutService.humanSleep(3, 7);
            }
        }

        System.out.println("\nDISPATCHER: Entire Theater Successfully Scraped and Saved!");
    }
}