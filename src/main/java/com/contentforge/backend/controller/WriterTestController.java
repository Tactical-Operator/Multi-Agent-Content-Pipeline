package com.contentforge.backend.controller;

import com.contentforge.backend.agent.EditorAgent;
import com.contentforge.backend.agent.FactCheckerAgent;
import com.contentforge.backend.agent.ResearcherAgent;
import com.contentforge.backend.agent.WriterAgent;

import com.contentforge.backend.model.EditorResult;
import com.contentforge.backend.model.FactCheckResult;
import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;
import com.contentforge.backend.model.WriterTestResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/write")
public class WriterTestController {

    private final ResearcherAgent researcherAgent;
    private final WriterAgent writerAgent;
    private final FactCheckerAgent factCheckerAgent;
    private final EditorAgent editorAgent;

    public WriterTestController(
            ResearcherAgent researcherAgent,
            WriterAgent writerAgent,
            FactCheckerAgent factCheckerAgent,
            EditorAgent editorAgent) {

        this.researcherAgent = researcherAgent;
        this.writerAgent = writerAgent;
        this.factCheckerAgent = factCheckerAgent;
        this.editorAgent = editorAgent;
    }

    @GetMapping 
    public WriterTestResult write( 
            // @RequestParam will strip the data after ?
            @RequestParam String topic,
            @RequestParam String platform,
            @RequestParam String format,
            @RequestParam String audience,
            @RequestParam String tone,
            @RequestParam(defaultValue = "")
            String additionalInstructions) {

        // Agent 1: Research
        ResearchResult research =
                researcherAgent.research(topic);

        // Agent 2: Write
        WriterResult writtenContent =
                writerAgent.write(
                        topic,
                        platform,
                        format,
                        audience,
                        tone,
                        additionalInstructions,
                        research
                );

        // Agent 3: Fact-check
        FactCheckResult factCheck =
                factCheckerAgent.check(
                        research,
                        writtenContent
                );

        // Agent 4: Final review
        EditorResult editorReview =
                editorAgent.review(
                        platform,
                        format,
                        audience,
                        tone,
                        additionalInstructions,
                        writtenContent,
                        factCheck
                );

        return new WriterTestResult(
                research,
                writtenContent,
                factCheck,
                editorReview
        );
    }
}