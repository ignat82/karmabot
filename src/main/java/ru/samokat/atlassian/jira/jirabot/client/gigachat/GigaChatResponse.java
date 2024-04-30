package ru.samokat.atlassian.jira.jirabot.client.gigachat;

import java.util.List;
import java.util.Optional;

public class GigaChatResponse {
    private List<Choice> choices;
    private long created;
    private String model;
    private String object;
    private Usage usage;

    public Optional<String> getFirstMessage() {
        if (choices.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(choices.get(0).message.content);
    }


    public static class Choice {
        private Message message;
        private int index;
        private String finish_reason;
    }

    public static class Message {
        private String content;
        private String role;
    }

    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
        private int system_tokens;
    }
}


