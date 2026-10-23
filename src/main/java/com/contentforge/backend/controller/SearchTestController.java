
package com.contentforge.backend.controller;

import com.contentforge.backend.model.SearchResult;
import com.contentforge.backend.service.WebSearchService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchTestController {

    private final WebSearchService webSearchService;

    public SearchTestController(WebSearchService webSearchService) {
        this.webSearchService = webSearchService;
    }

    @GetMapping
    public List<SearchResult> search(@RequestParam String query) {

        return webSearchService.search(query);
    }
}
