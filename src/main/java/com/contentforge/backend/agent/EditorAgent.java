
package com.contentforge.backend.agent;

import com.contentforge.backend.model.EditorResult;
import com.contentforge.backend.model.FactCheckResult;
import com.contentforge.backend.model.WriterResult;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class EditorAgent {

    private final ChatClient chatClient;

    public EditorAgent(OpenAiChatModel groqChatModel) {

        this.chatClient = ChatClient.builder(groqChatModel)
                .defaultSystem("""
                        You are the Editor Agent in a multi-agent
                        social media content generation system.

                        You are the final reviewer before content
                        is approved.

                        Review the content based on:
                        - Requested social media platform
                        - Requested content format
                        - Target audience
                        - Requested tone
                        - Additional user instructions
                        - Factual issues reported by the Fact-Checker
                        - Overall clarity and quality

                        Your job is to decide whether the content
                        should be approved or revised.

                        Return an EditorResult containing:
                        - status
                        - issues
                        - feedback

                        The status field is mandatory.

                        Use exactly one of these status values:
                        APPROVED
                        REVISION_REQUIRED

                        Use APPROVED only when:
                        - The content satisfies user requirements.
                        - The Fact-Checker found no meaningful
                          factual problems.
                        - There are no meaningful quality issues.

                        Use REVISION_REQUIRED when:
                        - Factual corrections are necessary.
                        - Important user requirements are missing.
                        - Meaningful formatting or quality problems exist.

                        If revision is required, clearly explain
                        what the Writer Agent should change.

                        For minor punctuation or formatting issues,
                        identify only the specific corrections needed.

                        Do not rewrite the content yourself.

                        Never leave the status field null or empty.
                        """)
                .build();
    }

    public EditorResult review(
            String platform,
            String format,
            String audience,
            String tone,
            String additionalInstructions,
            WriterResult writtenContent,
            FactCheckResult factCheck) {

        String prompt = """
                Review the following social media content.

                USER REQUIREMENTS

                Platform:
                %s

                Format:
                %s

                Target Audience:
                %s

                Tone:
                %s

                Additional Instructions:
                %s

                WRITER CONTENT

                Title:
                %s

                Content:
                %s

                Caption:
                %s

                FACT-CHECK RESULT

                Factually Correct:
                %s

                Issues:
                %s

                Recommendations:
                %s

                REVIEW INSTRUCTIONS

                Return your decision using EditorResult.

                Set status to exactly APPROVED or
                REVISION_REQUIRED.

                If the Fact-Checker reports factual problems,
                do not approve the content.

                Provide specific feedback for any revision.
                """.formatted(
                platform,
                format,
                audience,
                tone,
                additionalInstructions,
                writtenContent.title(),
                writtenContent.contentParts(),
                writtenContent.caption(),
                factCheck.factuallyCorrect(),
                factCheck.issues(),
                factCheck.recommendations()
        );

        EditorResult result = chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(EditorResult.class);

        /*
         * Validate the AI response.
         *
         * A successful JSON conversion does not guarantee
         * that the status field contains a valid value.
         */

        if (result == null) {
            throw new IllegalStateException(
                    "EditorAgent returned a null result."
            );
        }

        String status = result.status();

        if (status == null || status.isBlank()) {
            throw new IllegalStateException(
                    "EditorAgent returned an empty status."
            );
        }

        if (!"APPROVED".equalsIgnoreCase(status)
                && !"REVISION_REQUIRED".equalsIgnoreCase(status)) {

            throw new IllegalStateException(
                    "EditorAgent returned an invalid status: " + status
            );
        }

        /*
         * Prevent approval when the Fact-Checker
         * has explicitly rejected the content.
         */

        if (!factCheck.factuallyCorrect()
                && "APPROVED".equalsIgnoreCase(status)) {

            return new EditorResult(
                    "REVISION_REQUIRED",
                    factCheck.issues() == null
                            ? java.util.List.of("Fact-check failed.")
                            : factCheck.issues(),
                    "The Fact-Checker identified factual issues. "
                            + "Correct them before approval."
            );
        }

        return result;
    }
}
