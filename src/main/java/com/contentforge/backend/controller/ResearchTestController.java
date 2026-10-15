package com.contentforge.backend.controller;

import com.contentforge.backend.agent.ResearcherAgent;
import com.contentforge.backend.model.ResearchResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/research")
public class ResearchTestController {

    private final ResearcherAgent researcherAgent;
    // The controller needs a Researcher Agent because the controller itself doesn't
    // know how to research.

    public ResearchTestController(ResearcherAgent researcherAgent) {
        this.researcherAgent = researcherAgent;
        // means:
        // Store the ResearcherAgent I received into my researcherAgent field.
    }

    @GetMapping
    public ResearchResult research(
            @RequestParam String topic) {

        return researcherAgent.research(topic);
    }
}