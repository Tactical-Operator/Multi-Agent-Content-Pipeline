
package com.contentforge.backend.service;

import org.springframework.stereotype.Service;

// Our four AI agents
import com.contentforge.backend.agent.ResearcherAgent;
import com.contentforge.backend.agent.WriterAgent;
import com.contentforge.backend.agent.FactCheckerAgent;
import com.contentforge.backend.agent.EditorAgent;

// Objects returned by our agents
import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;
import com.contentforge.backend.model.FactCheckResult;
import com.contentforge.backend.model.EditorResult;
import com.contentforge.backend.model.PipelineResult;

/*
 * PipelineOrchestrator
 *
 * PURPOSE:
 * Controls the execution of our four AI agents.
 *
 * Researcher -> Writer -> FactChecker -> Editor
 *
 * If the Editor requests a revision, the content
 * is sent back to the Writer.
 *
 * Maximum writing attempts: 3
 */
@Service
public class PipelineOrchestrator {

    // Spring injects these four agent objects.
    private final ResearcherAgent researcherAgent;
    private final WriterAgent writerAgent;
    private final FactCheckerAgent factCheckerAgent;
    private final EditorAgent editorAgent;

    /*
     * Constructor Dependency Injection
     *
     * Spring supplies the four agent objects when
     * creating PipelineOrchestrator.
     */
    public PipelineOrchestrator(
            ResearcherAgent researcherAgent,
            WriterAgent writerAgent,
            FactCheckerAgent factCheckerAgent,
            EditorAgent editorAgent) {

        this.researcherAgent = researcherAgent;
        this.writerAgent = writerAgent;
        this.factCheckerAgent = factCheckerAgent;
        this.editorAgent = editorAgent;
    }

    /*
     * generate()
     *
     * Executes the complete multi-agent pipeline.
     *
     * Returns a PipelineResult containing the
     * latest output of all four agents.
     */
    public PipelineResult generate(
            String topic,
            String platform,
            String format,
            String audience,
            String tone,
            String additionalInstructions) {

        /*
         * STEP 1: RESEARCH
         *
         * Research happens once before the loop.
         * Its result is reused for subsequent revisions.
         */
        ResearchResult research = researcherAgent.research(topic);

        // Store the latest output from each agent.
        WriterResult writer = null;
        FactCheckResult factCheck = null;
        EditorResult editor = null;

        // Maximum writing attempts, including the original draft.
        final int maxAttempts = 3;

        /*
         * STEP 2: WRITING AND REVISION LOOP
         *
         * Attempt 1 -> Original content
         * Attempt 2 -> First revision
         * Attempt 3 -> Second revision
         */
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            System.out.println(
                    "Pipeline writing attempt: " + attempt
            );

            if (attempt == 1) {

                /*
                 * FIRST ATTEMPT
                 *
                 * Generate original content using the
                 * user requirements and research.
                 */
                writer = writerAgent.write(
                        topic,
                        platform,
                        format,
                        audience,
                        tone,
                        additionalInstructions,
                        research
                );

            } else {

                /*
                 * REVISION ATTEMPT
                 *
                 * Pass the previous draft, original research,
                 * and Editor feedback to the Writer.
                 *
                 * The Writer makes another LLM API call.
                 */
                String feedback = editor.feedback();

                // Include the Editor's individual issues.
                if (editor.issues() != null
                        && !editor.issues().isEmpty()) {

                    feedback += "\n\nIssues to fix:\n"
                            + String.join("\n", editor.issues());
                }

                writer = writerAgent.revise(
                        topic,
                        platform,
                        format,
                        audience,
                        tone,
                        additionalInstructions,
                        research,
                        writer,
                        feedback
                );
            }

            /*
             * STEP 3: FACT CHECKING
             *
             * Check the latest version of the content.
             *
             * This executes again after every revision.
             */
            factCheck = factCheckerAgent.check(
                    research,
                    writer
            );

            /*
             * STEP 4: EDITOR REVIEW
             *
             * Review the latest draft and fact-check result.
             */
            editor = editorAgent.review(
                    platform,
                    format,
                    audience,
                    tone,
                    additionalInstructions,
                    writer,
                    factCheck
            );

            /*
             * STEP 5: APPROVAL DECISION
             *
             * If approved, stop the loop immediately.
             *
             * Otherwise, continue to the next attempt.
             */
            if (editor != null
                    && "APPROVED".equalsIgnoreCase(editor.status())) {

                System.out.println(
                        "Content approved on attempt: " + attempt
                );

                break;
            }

            /*
             * If this was the last attempt, no more
             * revisions are allowed.
             */
            if (attempt == maxAttempts) {

                System.out.println(
                        "Maximum revision attempts reached."
                );

            } else {

                System.out.println(
                        "Editor requested revision."
                );
            }
        }

        /*
         * STEP 6: FINAL RESULT
         *
         * Return the latest results from all four agents.
         *
         * IMPORTANT:
         * If the Editor did not approve the content,
         * its status remains REVISION_REQUIRED.
         *
         * We do not falsely mark it as APPROVED.
         */
        return new PipelineResult(
                research,
                writer,
                factCheck,
                editor
        );
    }
}
