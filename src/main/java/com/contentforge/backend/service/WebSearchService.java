
package com.contentforge.backend.service;

import com.contentforge.backend.model.SearchResult;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebSearchService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebSearchService(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<SearchResult> search(String query) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be empty."
            );
        }

        try {
            String encodedQuery = URLEncoder.encode(
                    query,
                    StandardCharsets.UTF_8
            );

            String url =
                    "https://en.wikipedia.org/w/api.php"
                    + "?action=query"
                    + "&generator=search"
                    + "&gsrsearch=" + encodedQuery
                    + "&gsrlimit=3"
                    + "&prop=extracts%7Cinfo"
                    + "&exintro=1"
                    + "&explaintext=1"
                    + "&inprop=url"
                    + "&format=json"
                    + "&formatversion=2";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(
                            "User-Agent",
                            "ContentForgeLearningProject/1.0 "
                            + "(educational Java research client)"
                    )
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Wikipedia API returned HTTP "
                        + response.statusCode()
                );
            }

            JsonNode root = objectMapper.readTree(response.body());

            JsonNode pages = root.path("query").path("pages");

            List<SearchResult> results = new ArrayList<>();

            if (pages.isArray()) {

                for (JsonNode page : pages) {

                    String title = page.path("title").asText();
                    String sourceUrl = page.path("fullurl").asText();
                    String extract = page.path("extract").asText();

                    results.add(new SearchResult(
                            title,
                            sourceUrl,
                            extract
                    ));
                }
            }

            return results;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Web search interrupted.",
                    e
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Web search failed: " + e.getMessage(),
                    e
            );
        }
    }
}
