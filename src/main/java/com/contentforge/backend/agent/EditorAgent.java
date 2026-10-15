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
                        - requested social media platform
                        - requested content format
                        - target audience
                        - requested tone
                        - additional user instructions
                        - factual issues reported by the Fact-Checker Agent
                        - overall clarity and quality

                        Your job is to decide whether the content
                        should be approved or revised.

                        Set status to exactly one of:

                        APPROVED

                        or

                        REVISION_REQUIRED

                        Use APPROVED only when the content satisfies
                        the user's requirements and there are no
                        meaningful factual or quality problems.

                        Use REVISION_REQUIRED when something should
                        be corrected or improved.

                        If revision is required, clearly explain what
                        the Writer Agent should change.

                        Do not rewrite the content yourself.
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

        return chatClient
                .prompt()
                .user("""
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
                        ))
                .call()
                .entity(EditorResult.class);
    }
}