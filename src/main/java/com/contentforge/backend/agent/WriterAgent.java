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

}
