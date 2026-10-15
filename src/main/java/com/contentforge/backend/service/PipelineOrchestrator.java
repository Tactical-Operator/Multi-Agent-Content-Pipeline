
package com.contentforge.backend.service;

// Spring annotation used to register this class as a service bean.
import org.springframework.stereotype.Service;

// Import the four AI agent classes.
import com.contentforge.backend.agent.ResearcherAgent;
import com.contentforge.backend.agent.WriterAgent;
import com.contentforge.backend.agent.FactCheckerAgent;
import com.contentforge.backend.agent.EditorAgent;

// Import the objects returned by the AI agents.
import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;
import com.contentforge.backend.model.FactCheckResult;
import com.contentforge.backend.model.EditorResult;
import com.contentforge.backend.model.PipelineResult;

/*
 * PipelineOrchestrator
 *
 * PURPOSE:
 * Coordinates the execution of our four AI agents.
 *
 * Execution order:
 *
 * 1. ResearcherAgent
 * 2. WriterAgent
 * 3. FactCheckerAgent
 * 4. EditorAgent
 *
 * If the Editor requests a revision, the Writer should
 * receive the feedback and generate an improved draft.
 *
 * The revised draft must be checked and reviewed again.
 */
@Service
public class PipelineOrchestrator {

    /*
     * These variables hold references to our four agents.
     *
     * We do not manually create their objects using 'new'.
     * Spring supplies the objects through dependency injection.
     *
     * 'final' means these references cannot be reassigned
     * after they have been initialized.
     */
    private final ResearcherAgent researcherAgent;
    private final WriterAgent writerAgent;
    private final FactCheckerAgent factCheckerAgent;
    private final EditorAgent editorAgent;

    /*
     * CONSTRUCTOR
     *
     * Spring calls this constructor when creating
     * the PipelineOrchestrator bean.
     *
     * It provides the four agent objects as arguments.
     *
     * 'this.researcherAgent' refers to the instance field.
     * 'researcherAgent' refers to the constructor parameter.
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
     * This method receives the user's content requirements.
     *
     * INPUT:
     * topic                  - Subject of the content
     * platform               - LinkedIn, Instagram, etc.
     * format                 - Post, thread, article, etc.
     * audience               - Target readers
     * tone                   - Professional, casual, etc.
     * additionalInstructions - Extra user requirements
     *
     * OUTPUT:
     * PipelineResult containing the results of all four agents.
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
         * Call the Researcher Agent and pass the topic.
         *
         * The agent communicates with the LLM and returns
         * a ResearchResult Java object.
         *
         * Research happens once, outside the revision loop.
         */
        ResearchResult research = researcherAgent.research(topic);

        /*
         * These variables store the latest results.
         *
         * We declare them outside the loop so they remain
         * accessible when constructing PipelineResult.
         */
        WriterResult writer = null;
        FactCheckResult factCheck = null;
        EditorResult editor = null;

        /*
         * Maximum number of writing attempts.
         *
         * Attempt 1: Original draft
         * Attempt 2: First revision
         * Attempt 3: Second revision
         */
        int maxAttempts = 3;

        /*
         * REVISION LOOP
         *
         * The Writer, Fact Checker and Editor execute
         * for each attempt.
         */
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            /*
             * STEP 2: CONTENT WRITING
             *
             * On the first attempt, generate the original post.
             */
            if (attempt == 1) {

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
                 * REVISION STEP - NOT IMPLEMENTED YET
                 *
                 * The Writer must receive:
                 *
                 * 1. Original research
                 * 2. Previous draft
                 * 3. Editor feedback
                 *
                 * We need the actual revision method from
                 * WriterAgent before implementing this section.
                 *
                 * Do not make another LLM call here until
                 * the revision method is connected.
                 */
                break;
            }

            /*
             * STEP 3: FACT CHECKING
             *
             * Pass the research and latest Writer output
             * to the Fact Checker.
             */
            factCheck = factCheckerAgent.check(research, writer);

            /*
             * STEP 4: EDITOR REVIEW
             *
             * The Editor receives the generated content,
             * fact-check results, and user requirements.
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
             * APPROVAL CHECK - NOT IMPLEMENTED YET
             *
             * Once we know the exact status field in
             * EditorResult, we will check whether the
             * Editor approved the content.
             *
             * If APPROVED:
             *     break out of the loop.
             *
             * If REVISION_REQUIRED:
             *     continue to the next writing attempt.
             *
             * For now, stop after one complete attempt.
             */
            break;
        }

        /*
         * STEP 5: RETURN PIPELINE RESULTS
         *
         * Combine the outputs of all four agents into
         * one PipelineResult object.
         *
         * The caller can access:
         *
         * result.research()
         * result.writer()
         * result.factCheck()
         * result.editor()
         */
        return new PipelineResult(
                research,
                writer,
                factCheck,
                editor
        );
    }
}
