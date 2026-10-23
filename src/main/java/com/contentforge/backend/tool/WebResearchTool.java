
package com.contentforge.backend.tool;

import com.contentforge.backend.model.SearchResult;
import com.contentforge.backend.service.WebSearchService;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebResearchTool {

    private final WebSearchService webSearchService;

    public WebResearchTool(WebSearchService webSearchService) {
        this.webSearchService = webSearchService;
    }

    @Tool(
        name = "searchWeb",
        description = """
            Search external reference material for information
            about a topic.

            Use this tool when factual verification, current
            information, or technical documentation is needed.

            Returns source titles, URLs, and article extracts.
            """
    )
    public List<SearchResult> searchWeb(String query) {

        System.out.println(
                "ResearcherAgent requested web search: " + query
        );

        return webSearchService.search(query);
    }
}
