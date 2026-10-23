
package com.contentforge.backend.agent;

import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class WriterAgent {

    private final ChatClient chatClient;

    // Constructor Injection
    public WriterAgent(OpenAiChatModel groqChatModel) {

        this.chatClient = ChatClient.builder(groqChatModel)
                .defaultSystem("""
                        You are the Writer Agent in a multi-agent
                        social media content generation system.

                        Your responsibility is to create high-quality
                        social media content using research provided
                        by the Researcher Agent.

                        Follow the requested platform, format, audience,
                        tone and additional instructions.

                        Use the provided research as the factual basis.

                        Do not invent unsupported factual claims.

                        Generate clear, engaging and well-structured
                        social media content.
                        """)
                .build();
    }

    /*
     * Generates the first draft of social media content.
     */
    public WriterResult write(
            String topic,
            String platform,
            String format,
            String audience,
            String tone,
            String additionalInstructions,
            ResearchResult research) {

        String prompt = """
                Create social media content using the following information.

                ORIGINAL REQUIREMENTS:

                Topic: %s
                Platform: %s
                Format: %s
                Target Audience: %s
                Tone: %s
                Additional Instructions: %s

                RESEARCH INFORMATION:

                Research Summary:
                %s

                Key Facts:
                %s

                Important Concepts:
                %s

                WRITING INSTRUCTIONS:

                - Follow the requested platform and format.
                - Write for the specified target audience.
                - Maintain the requested tone.
                - Follow all additional instructions.
                - Use the research as the factual foundation.
                - Do not invent unsupported factual claims.
                - Make the content clear and engaging.

                Return the generated content using the
                required WriterResult structure.
                """.formatted(
                topic,
                platform,
                format,
                audience,
                tone,
                additionalInstructions,
                research.summary(),
                research.keyFacts(),
                research.importantConcepts()
        );

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(WriterResult.class);
    }

    /*
     * Revises previously generated content using feedback
     * from the Editor Agent and Fact Checker Agent.
     */
    public WriterResult revise(
            String topic,
            String platform,
            String format,
            String audience,
            String tone,
            String additionalInstructions,
            ResearchResult research,
            WriterResult previousDraft,
            String feedback) {

        String prompt = """
                Revise the previously generated social media content.

                ORIGINAL REQUIREMENTS:

                Topic: %s
                Platform: %s
                Format: %s
                Target Audience: %s
                Tone: %s
                Additional Instructions: %s

                ORIGINAL RESEARCH:

                Research Summary:
                %s

                Key Facts:
                %s

                Important Concepts:
                %s

                PREVIOUS DRAFT:

                %s

                EDITOR AND FACT-CHECKER FEEDBACK:

                %s

                REVISION INSTRUCTIONS:

                - Fix all issues identified in the feedback.
                - Preserve accurate information from the previous draft.
                - Do not invent unsupported factual claims.
                - Follow the original platform, format and tone.
                - Follow the original additional instructions.
                - Improve clarity, accuracy and readability.
                - Produce an improved version of the content.

                Return the revised content using the
                required WriterResult structure.
                """.formatted(
                topic,
                platform,
                format,
                audience,
                tone,
                additionalInstructions,
                research.summary(),
                research.keyFacts(),
                research.importantConcepts(),
                previousDraft,
                feedback
        );

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .entity(WriterResult.class);
    }
}
