
package com.contentforge.backend.service;

import org.springframework.stereotype.Service;

import com.contentforge.backend.agent.ResearcherAgent;
import com.contentforge.backend.agent.WriterAgent;
import com.contentforge.backend.agent.FactCheckerAgent;
import com.contentforge.backend.agent.EditorAgent;

import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;
import com.contentforge.backend.model.FactCheckResult;
import com.contentforge.backend.model.EditorResult;
import com.contentforge.backend.model.PipelineResult;

@Service
public class PipelineOrchestrator {

        // Our four AI agents
        private final ResearcherAgent researcherAgent;
        private final WriterAgent writerAgent;
        private final FactCheckerAgent factCheckerAgent;
        private final EditorAgent editorAgent;

        /*
         * Constructor Dependency Injection
         *
         * Spring automatically provides the four agent objects.
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
         * Executes the complete multi-agent pipeline.
         *
         * Researcher -> Writer -> FactChecker -> Editor
         *
         * If the Editor requests revision:
         * Writer -> FactChecker -> Editor
         *
         * Maximum writing attempts: 3
         */
        public PipelineResult generate(
                        String topic,
                        String platform,
                        String format,
                        String audience,
                        String tone,
                        String additionalInstructions) {

                // Start measuring total pipeline execution time.
                long pipelineStart = System.nanoTime();

                /*
                 * ========================================
                 * STEP 1: RESEARCH
                 * ========================================
                 *
                 * Research happens only once.
                 * The same research is reused during revisions.
                 */

                long researchStart = System.nanoTime();

                ResearchResult research = researcherAgent.research(topic);

                long researchEnd = System.nanoTime();

                System.out.println(
                                "ResearcherAgent execution time: " +
                                                (researchEnd - researchStart) / 1_000_000 +
                                                " ms");

                // Store the latest output from each agent.
                WriterResult writer = null;
                FactCheckResult factCheck = null;
                EditorResult editor = null;

                // Maximum writing attempts, including the first draft.
                final int maxAttempts = 3;

                /*
                 * ========================================
                 * STEP 2: WRITING AND REVISION LOOP
                 * ========================================
                 *
                 * Attempt 1 -> Original draft
                 * Attempt 2 -> First revision
                 * Attempt 3 -> Second revision
                 */

                for (int attempt = 1; attempt <= maxAttempts; attempt++) {

                        System.out.println();
                        System.out.println("==================================");
                        System.out.println("Pipeline writing attempt: " + attempt);
                        System.out.println("==================================");

                        /*
                         * ========================================
                         * STEP 2A: WRITER AGENT
                         * ========================================
                         */

                        long writerStart = System.nanoTime();

                        if (attempt == 1) {

                                // Generate the original draft.
                                writer = writerAgent.write(
                                                topic,
                                                platform,
                                                format,
                                                audience,
                                                tone,
                                                additionalInstructions,
                                                research);

                        } else {

                                /*
                                 * Revise the previous draft using
                                 * Editor feedback and issues.
                                 */

                                String feedback = editor.feedback();

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
                                                feedback);
                        }

                        long writerEnd = System.nanoTime();

                        System.out.println(
                                        "Attempt " + attempt +
                                                        " - WriterAgent execution time: " +
                                                        (writerEnd - writerStart) / 1_000_000 +
                                                        " ms");

                        /*
                         * ========================================
                         * STEP 3: FACT CHECKER AGENT
                         * ========================================
                         *
                         * Checks the latest version of the content.
                         * Runs again after every revision.
                         */

                        long factCheckStart = System.nanoTime();

                        factCheck = factCheckerAgent.check(
                                        research,
                                        writer);

                        long factCheckEnd = System.nanoTime();

                        System.out.println(
                                        "Attempt " + attempt +
                                                        " - FactCheckerAgent execution time: " +
                                                        (factCheckEnd - factCheckStart) / 1_000_000 +
                                                        " ms");
                        System.out.println();
                        System.out.println("----- FACT CHECK REVIEW -----");

                        System.out.println(
                                        "Factually Correct: " + factCheck.factuallyCorrect());

                        System.out.println(
                                        "Fact Check Issues: " + factCheck.issues());

                        System.out.println(
                                        "Fact Check Recommendations: " + factCheck.recommendations());

                        System.out.println("-----------------------------");

                        /*
                         * ========================================
                         * STEP 4: EDITOR AGENT
                         * ========================================
                         *
                         * Reviews the latest draft and
                         * the FactChecker's findings.
                         */

                        long editorStart = System.nanoTime();

                        editor = editorAgent.review(
                                        platform,
                                        format,
                                        audience,
                                        tone,
                                        additionalInstructions,
                                        writer,
                                        factCheck);

                        long editorEnd = System.nanoTime();

                        System.out.println(
                                        "Attempt " + attempt +
                                                        " - EditorAgent execution time: " +
                                                        (editorEnd - editorStart) / 1_000_000 +
                                                        " ms");
                        /*
                         * ========================================
                         * EDITOR DECISION DEBUG LOGS
                         * ========================================
                         *
                         * Display why the Editor approved or
                         * rejected the current draft.
                         */

                        System.out.println();
                        System.out.println("----- EDITOR REVIEW -----");

                        System.out.println(
                                        "Attempt: " + attempt);

                        System.out.println(
                                        "Editor Status: " + editor.status());

                        System.out.println(
                                        "Editor Issues: " + editor.issues());

                        System.out.println(
                                        "Editor Feedback: " + editor.feedback());

                        System.out.println("-------------------------");

                        /*
                         * ========================================
                         * STEP 5: APPROVAL DECISION
                         * ========================================
                         */

                        if (editor != null
                                        && "APPROVED".equalsIgnoreCase(editor.status())) {

                                System.out.println(
                                                "Content approved on attempt: " + attempt);

                                break;
                        }

                        if (attempt == maxAttempts) {

                                System.out.println(
                                                "Maximum revision attempts reached.");

                        } else {

                                System.out.println(
                                                "Editor requested revision.");
                        }
                }

                /*
                 * ========================================
                 * STEP 6: TOTAL EXECUTION TIME
                 * ========================================
                 */

                long pipelineEnd = System.nanoTime();

                System.out.println();
                System.out.println("==================================");
                System.out.println(
                                "TOTAL PIPELINE TIME: " +
                                                (pipelineEnd - pipelineStart) / 1_000_000 +
                                                " ms");
                System.out.println("==================================");

                /*
                 * ========================================
                 * STEP 7: RETURN FINAL RESULT
                 * ========================================
                 *
                 * Return the latest results from all four agents.
                 *
                 * The Editor's status is preserved even if
                 * the maximum revision attempts are reached.
                 */

                return new PipelineResult(
                                research,
                                writer,
                                factCheck,
                                editor);
        }
}
