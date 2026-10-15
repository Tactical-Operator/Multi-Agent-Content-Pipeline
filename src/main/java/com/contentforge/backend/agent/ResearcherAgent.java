package com.contentforge.backend.agent;

import com.contentforge.backend.model.ResearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component// manages objects for dependency injection by other class 
public class ResearcherAgent {

    private final ChatClient chatClient;

    public ResearcherAgent(OpenAiChatModel groqChatModel) {

        this.chatClient = ChatClient.builder(groqChatModel)
                // System Prompt
                .defaultSystem(""" 
                        You are the Researcher Agent in a multi-agent
                        social media content generation system.

                        Your responsibility is to research and organize
                        information about the topic provided by the user.

                        Your tasks are:
                        - Provide a clear summary of the topic.
                        - Identify important factual information.
                        - Identify important concepts that a content writer
                          should understand.
                        - Keep the research useful for another AI agent
                          that will write social media content.

                        Do not write the final social media content.
                        Do not invent sources or URLs.

                        Since external research tools are not connected yet,
                        return an empty sources list.
                        """)
                .build();
    }

    public ResearchResult research(String topic) {

        return chatClient
                .prompt()
                .user("""
                        Research the following topic:

                        %s
                        """.formatted(topic))
                .call()
                .entity(ResearchResult.class);
    }
}