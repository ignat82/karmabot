package ru.samokat.atlassian.jira.jirabot.controller;

import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.samokat.atlassian.jira.jirabot.client.gigachat.GigaChatClient;
import ru.samokat.atlassian.jira.jirabot.service.ChatService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Named
@Slf4j
@RequiredArgsConstructor
public class MlQuestionController {
    private final GigaChatClient client;
    private final ChatService chatService;

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
}
