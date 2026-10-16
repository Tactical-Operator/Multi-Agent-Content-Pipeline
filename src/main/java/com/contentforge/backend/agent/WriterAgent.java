package com.contentforge.backend.agent;

import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class WriterAgent {

    private final ChatClient chatClient;

    public WriterAgent(OpenAiChatModel groqChatModel) {

        this.chatClient = ChatClient.builder(groqChatModel)
                .defaultSystem("""
                        You are the Writer Agent in a multi-agent
                                    social media content generation system.

                                    Your responsibility is to create high-quality
                                    social media content using research provided
                                    by the Researcher Agent.

                                    Follow the requested:
                                    - social media platform
                                    - content format
                                    - target audience
                                    - tone
                                    - additional instructions

                                    Use the provided research as your factual basis.

                                    Do not invent unsupported factual claims.

                                    Return content suitable for the requested
                                    social media platform and format.

                            """)
                .build();
    }

    public WriterResult write(
            String topic,
            String platform,
            String format,
            String audience,
            String tone,
            String additionalInstructions,
            ResearchResult research) {

        return chatClient
                .prompt()
                .user("""
                        Create social media content using the following information.

                        Topic:
                        %s

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

                        Research Summary:
                        %s

                        Key Facts:
                        %s

                        Important Concepts:
                        %s
                        """.formatted(
                        topic,
                        platform,
                        format,
                        audience,
                        tone,
                        additionalInstructions,
                        research.summary(),
                        research.keyFacts(),
                        research.importantConcepts()))
                .call()
                .entity(WriterResult.class);
    }

    
/*
 * revise()
 *
 * PURPOSE:
 * Revise previously generated content using feedback
 * from the Editor Agent and Fact Checker Agent.
 *
 * INPUT:
 * - Original user requirements
 * - ResearchResult: original research
 * - WriterResult: previous draft
 * - String: feedback explaining what must be fixed
 *
 * OUTPUT:
 * - A new WriterResult containing revised content
 *
 * This method makes a NEW LLM API request.
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

    return chatClient
            .prompt()
            .user("""
                    Revise the previously generated social media content.

                    ORIGINAL REQUIREMENTS:

                    Topic:
                    %s

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
                    - Do not invent unsupported facts.
                    - Follow the original platform, format and tone.
                    - Produce an improved version of the content.
                    - Return the revised content using the
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
                    feedback))
            .call()
            .entity(WriterResult.class);
}


}
