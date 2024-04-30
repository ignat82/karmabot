package ru.samokat.atlassian.jira.jirabot.service;

import jakarta.inject.Named;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Named
public class ChatService {
    public SendMessage assembleChatMessage(String text, long chatId, Integer replyToMessageId ) {
        SendMessage.SendMessageBuilder messageBuilder = SendMessage.builder();
        if (replyToMessageId != null) {
            messageBuilder.replyToMessageId(replyToMessageId);
        }
        return messageBuilder.chatId(chatId).text(text).build();
    }

    public SendMessage assembleChatMessage(String text, long chatId) {
        return assembleChatMessage(text, chatId, null);
    }
}
