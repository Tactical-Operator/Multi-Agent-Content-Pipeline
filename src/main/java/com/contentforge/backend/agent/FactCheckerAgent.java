package com.contentforge.backend.agent;

import com.contentforge.backend.model.FactCheckResult;
import com.contentforge.backend.model.ResearchResult;
import com.contentforge.backend.model.WriterResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class FactCheckerAgent {

    private final ChatClient chatClient;

    public FactCheckerAgent(OpenAiChatModel groqChatModel){

        this.chatClient = ChatClient.builder(groqChatModel)
        .defaultSystem("""
                        You are the Fact-Checker Agent in a multi-agent
                        social media content generation system.

                        Your responsibility is to verify factual claims
                        made in content created by the Writer Agent.

                        Compare the written content with the research
                        provided by the Researcher Agent.

                        Your tasks are:
                        - Identify factual claims in the written content.
                        - Check whether those claims are supported by
                          the provided research.
                        - Identify incorrect, misleading, or unsupported claims.
                        - List claims that are supported by the research.
                        - Provide recommendations for correcting factual issues.

                        Do not rewrite the social media content.

                        Mark factuallyCorrect as true only when no meaningful
                        factual problems are found.
                        """
             )
             .build();
    }

    public FactCheckResult check(
        ResearchResult research,
        WriterResult writtenContent
    ){

        return chatClient
        .prompt()
        .user("""
                        Fact-check the following social media content.

                        RESEARCH:

                        Summary:
                        %s

                        Key Facts:
                        %s

                        Important Concepts:
                        %s

                        WRITTEN CONTENT:

                        Title:
                        %s

                        Content:
                        %s

                        Caption:
                        %s
                        """.formatted(
                                research.summary(),
                                research.keyFacts(),
                                research.importantConcepts(),
                                writtenContent.title(),
                                writtenContent.contentParts(),
                                writtenContent.caption()
                        ))
                        .call()
                        .entity(FactCheckResult.class);
    }

}
