package ru.samokat.atlassian.jira.jirabot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.samokat.atlassian.jira.jirabot.controller.AbstractUpdateListener;
import ru.samokat.atlassian.jira.jirabot.controller.ActionsController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JiraBot extends TelegramLongPollingBot {
    @Autowired
    private Map<String, AbstractUpdateListener> listeners;
    private final ActionsController actionsController;
    private final String BOT_TOKEN;
    private final String BOT_USERNAME;

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        List<BotApiMethod> responses = new ArrayList<>();

        listeners.forEach((listenerName, listener) -> listener.notify(update).ifPresent(responses::addAll));

        responses.forEach(resp -> {
            try {
                execute(resp);
            } catch (TelegramApiException e) {
                log.warn("caught an exception {} with message {} while sending message",
                         e.getClass().getSimpleName(),
                         e.getMessage());

            }
        });
    }

    public void giveVoice() {
        log.debug("giveVoice()");
        actionsController.getUpdateMessages().ifPresent(messages -> messages.forEach(message -> {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.warn("caught an exception {} with message {} while sending message",
                         e.getClass().getSimpleName(),
                         e.getMessage());
            }
        }));
    }
}
