package ru.samokat.atlassian.jira.jirabot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.samokat.atlassian.jira.jirabot.client.gigachat.GigaChatClient;
import ru.samokat.atlassian.jira.jirabot.service.ChatService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MlQuestionController extends AbstractUpdateListener {
    private final GigaChatClient client;
    private final ChatService chatService;
    @Value("${ru.samokat.atlassian.jira.jirabot.username}")
    private String BOT_USERNAME;

    @Override
    public Optional<List<BotApiMethod>> handleUpdate(Update update) {
        log.debug("handleUpdate()");

        Message message = update.getMessage();
        String query = message.getText().substring(message.getText().indexOf(' '));

        Optional<String> response = client.sendQuestion(query);

        if (response.isEmpty()) {
            log.trace("got empty response from ML client");
            return Optional.empty();
        }
        response.ifPresent(s -> log.trace("got response {}", s));
        BotApiMethod responseToChat = chatService.assembleChatMessage(response.get(),
                                                                      message.getChatId(),
                                                                      message.getMessageId());
        return Optional.of(Collections.singletonList(responseToChat));

    }

    @Override
    public boolean isTarget(Update update) {
        boolean isTextMessage = update.getMessage() != null && update.getMessage().getText() != null;
        if (!isTextMessage) {
            return false;
        }
        boolean isMessageToBot = update.getMessage().getEntities() != null &&
                update.getMessage().getEntities().get(0).getText().equals("@" + BOT_USERNAME);
        return isMessageToBot;
    }
}
