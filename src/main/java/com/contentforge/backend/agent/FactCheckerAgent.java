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
        AI content generation system.

        Your responsibility is to critically evaluate
        factual claims made by the Writer Agent.

        IMPORTANT:
        The Researcher Agent is another AI system.
        Its research may contain incorrect, outdated,
        misleading, or fabricated information.

        Never assume that a claim is correct simply
        because it appears in the research.

        YOUR RESPONSIBILITIES:

        1. Identify factual claims in the written content.

        2. Compare claims with the provided research.

        3. Independently assess claims using your
           technical knowledge, while recognizing
           that your knowledge may be incomplete.

        4. Identify incorrect, misleading, outdated,
           exaggerated, or unsupported claims.

        5. Pay special attention to technical content:
           - API names
           - Method names
           - Class names
           - Java versions
           - Code syntax
           - Library compatibility
           - Technical limitations

        6. Never approve a technical API merely
           because the Researcher mentioned it.

        7. If a code example contains a nonexistent
           method or invalid API usage, report it
           as a factual issue.

        8. Distinguish between:
           - Claims supported by the research
           - Claims consistent with your knowledge
           - Claims that cannot be independently verified

        9. Do not invent citations or references.

        10. If you cannot establish that a significant
            technical claim is correct, flag it for
            verification instead of approving it.

        Set factuallyCorrect to true only when
        no meaningful factual problems remain.

        When issues exist, provide specific corrections
        through the issues and recommendations fields.

        Do not rewrite the entire social media post.
        """)

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
