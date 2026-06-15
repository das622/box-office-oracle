package com.david.amcanalytics.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AmcScraperService {

    public Mono<String> fetchRawShowtimePayload(String showtimeId) {
        String url = "https://www.amctheatres.com/showtimes/" + showtimeId + "/seats";
        System.out.println("COURIER: Launching invisible Chrome browser and driving to: " + url);

        try {
            Playwright playwright = Playwright.create();
            // Turning the invisible engine back on!
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            page.navigate(url);

            try {
                // THE UPGRADE: Wait for ANY seat that is marked Available or Occupied
                page.waitForSelector("[aria-label*='Available'], [aria-label*='Occupied']", new Page.WaitForSelectorOptions().setTimeout(10000));
                System.out.println("COURIER: Seats have successfully rendered on the screen!");
            } catch (com.microsoft.playwright.TimeoutError e) {
                System.err.println("COURIER: Timeout! The seat map never loaded. (Cloudflare block or sold out)");
                browser.close();
                playwright.close();
                return Mono.empty();
            }

            String finalHtml = page.content();
            System.out.println("COURIER: Successfully ripped rendered HTML! Payload size: " + finalHtml.length());

            browser.close();
            playwright.close();

            return Mono.just(finalHtml);

        } catch (Exception e) {
            System.err.println("COURIER CRASH: " + e.getMessage());
            return Mono.empty();
        }
    }
}