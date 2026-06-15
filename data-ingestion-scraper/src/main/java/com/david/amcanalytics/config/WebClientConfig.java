package com.david.amcanalytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient amcWebClient() {
        final int maxMemoryBufferInBytes = 16 * 1024 * 1024;

        ExchangeStrategies customStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxMemoryBufferInBytes))
                .build();

        // SENIOR UPGRADE: Configuring the underlying Netty engine to automatically follow redirects!
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true); // <--- THE MAGIC KEY

        return WebClient.builder()
                .baseUrl("https://www.amctheatres.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Plugging the upgraded engine into our Van
                .exchangeStrategies(customStrategies)
                .defaultHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .defaultHeader("Accept-Language", "en-US,en;q=0.9") // Extra human disguise
                .build();
    }
}