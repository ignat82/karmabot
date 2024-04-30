package ru.samokat.atlassian.jira.jirabot.client.gigachat;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

public class GigaChatRequest {
    private List<Message> messages;
    private final String model = "GigaChat";
    private final int temperature = 1;
    private int maxTokens = 512;
    private double repetitionPenalty = 1;

    @AllArgsConstructor
    private static class Message {
        private static final String USER_ROLE = "user";
        private static final String SYSTEM_ROLE = "system";
        private String role;
        private String content;

    }

    public static GigaChatRequest constructSimpleRequest(String query) {
        GigaChatRequest request = new GigaChatRequest();
        Message message = new Message(Message.USER_ROLE, query);
        request.messages = Collections.singletonList(message);
        return request;
    }
}



