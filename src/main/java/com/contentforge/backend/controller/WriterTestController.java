
package com.contentforge.backend.controller;

import com.contentforge.backend.model.PipelineResult;
import com.contentforge.backend.service.PipelineOrchestrator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * WriterTestController
 *
 * PURPOSE:
 * Receives HTTP requests from the user and passes
 * the content requirements to PipelineOrchestrator.
 *
 * The controller no longer directly executes
 * the four AI agents.
 *
 * PipelineOrchestrator is responsible for:
 *
 * 1. Research
 * 2. Writing
 * 3. Fact-checking
 * 4. Editing
 * 5. Automatic revisions
 */
@RestController
@RequestMapping("/api/write")
public class WriterTestController {

    /*
     * Reference to the PipelineOrchestrator object.
     *
     * Spring provides this object through
     * constructor dependency injection.
     */
    private final PipelineOrchestrator pipelineOrchestrator;

    /*
     * Constructor
     *
     * Spring injects the PipelineOrchestrator bean.
     */
    public WriterTestController(
            PipelineOrchestrator pipelineOrchestrator) {

        this.pipelineOrchestrator = pipelineOrchestrator;
    }

    /*
     * GET /api/write
     *
     * Receives content generation requirements
     * through URL query parameters.
     *
     * Example:
     *
     * /api/write?topic=Java&platform=LinkedIn
     * &format=Post&audience=Developers&tone=Professional
     */
    @GetMapping
    public PipelineResult write(

            @RequestParam String topic,

            @RequestParam String platform,

            @RequestParam String format,

            @RequestParam String audience,

            @RequestParam String tone,

            @RequestParam(defaultValue = "")
            String additionalInstructions) {

        /*
         * Pass all the user inputs to the orchestrator.
         *
         * The orchestrator executes the four agents
         * and handles any required revisions.
         *
         * It returns one PipelineResult object.
         */
        return pipelineOrchestrator.generate(
                topic,
                platform,
                format,
                audience,
                tone,
                additionalInstructions
        );
    }
}
