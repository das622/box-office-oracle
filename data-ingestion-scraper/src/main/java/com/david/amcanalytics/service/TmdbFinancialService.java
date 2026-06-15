package com.david.amcanalytics.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class TmdbFinancialService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // A clean container to hold all 3 pieces of data
    public record MovieFinancials(long budget, long revenue, String releaseDate) {}

    @SuppressWarnings("unchecked")
    public MovieFinancials fetchFinancials(String movieTitle) {
        try {
            String searchUrl = "https://api.themoviedb.org/3/search/movie?query=" + movieTitle + "&api_key=" + apiKey;
            Map<String, Object> searchResponse = restTemplate.getForObject(searchUrl, Map.class);

            if (searchResponse != null && searchResponse.get("results") != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) searchResponse.get("results");

                if (!results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);
                    Object tmdbId = firstResult.get("id");

                    String detailsUrl = "https://api.themoviedb.org/3/movie/" + tmdbId + "?api_key=" + apiKey;
                    Map<String, Object> details = restTemplate.getForObject(detailsUrl, Map.class);

                    if (details != null) {
                        long budget = ((Number) details.getOrDefault("budget", 0L)).longValue();
                        long revenue = ((Number) details.getOrDefault("revenue", 0L)).longValue();
                        String releaseDate = (String) details.getOrDefault("release_date", "Unknown");

                        return new MovieFinancials(budget, revenue, releaseDate);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("TMDB CRASH: " + e.getMessage());
        }
        // Return defaults if anything fails
        return new MovieFinancials(0L, 0L, "Unknown");
    }
}