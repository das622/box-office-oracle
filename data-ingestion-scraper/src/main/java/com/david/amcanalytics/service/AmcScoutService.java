package com.david.amcanalytics.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AmcScoutService {

    // 1. THE NEW CONTAINER: Holds both the ID and the physical time text
    public record ShowtimeOffer(String id, String startTime) {}

    // 2. Change the return type from List<String> to List<ShowtimeOffer>
    public List<ShowtimeOffer> discoverShowtimeIds(String theaterUrl) {
        System.out.println("==================================================");
        System.out.println("SCOUT: Deploying to " + theaterUrl);

        List<ShowtimeOffer> showtimes = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(theaterUrl);
            page.waitForTimeout(5000);

            String rawHtml = page.content();
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(rawHtml);
            org.jsoup.select.Elements allLinks = doc.select("a[href]");

            for (org.jsoup.nodes.Element link : allLinks) {
                String href = link.attr("href");

                if (href.matches(".*\\d{9}.*")) {
                    Matcher m = Pattern.compile("(\\d{9})").matcher(href);
                    if (m.find()) {
                        String id = m.group(1);

                        // THE UPGRADE: Grab the actual text written on the button!
                        String timeText = link.text().trim();
                        if (timeText.isEmpty()) {
                            timeText = "Unknown";
                        }

                        // Prevent duplicates
                        boolean alreadyExists = showtimes.stream().anyMatch(s -> s.id().equals(id));
                        if (!alreadyExists) {
                            showtimes.add(new ShowtimeOffer(id, timeText));
                            System.out.println("FOUND MATCH: ID=" + id + " | Time=" + timeText);
                        }
                    }
                }
            }
            System.out.println("SCOUT: Mission Complete. Found " + showtimes.size() + " unique showtimes.");

        } catch (Exception e) {
            System.err.println("SCOUT CRASH: " + e.getMessage());
        }

        return showtimes;
    }

    public void humanSleep(int minSeconds, int maxSeconds) {
        try {
            int sleepTime = (int) (Math.random() * (maxSeconds - minSeconds + 1) + minSeconds);
            System.out.println("SYSTEM: Sleeping for " + sleepTime + " seconds (Human Jitter)...");
            Thread.sleep(sleepTime * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}