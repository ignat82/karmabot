package ru.samokat.atlassian.jira.jirabot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.samokat.atlassian.jira.jirabot.controller.ActionsController;
import ru.samokat.atlassian.jira.jirabot.controller.CommandController;
import ru.samokat.atlassian.jira.jirabot.controller.PollController;
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JiraBot extends TelegramLongPollingBot {
    private final CommandController commandController;
    private final PollController pollController;
    private final ActionsController actionsController;
    private final String BOT_TOKEN;
    private static final String BOT_USERNAME = "samokat_jira_bot";

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

        Optional<List<BotApiMethod>> responses = Optional.empty();
        boolean isTextMessage = update.getMessage() != null && update.getMessage().getText() != null;

         if ((isTextMessage && PointRecord.PointType.fromCreateCommand(update.getMessage().getText()).isPresent())
                || update.getCallbackQuery() != null) {
            responses = pollController.handleUpdate(update);
        } else if (update.getMessage() != null && update.getMessage().isCommand()) {
             responses = commandController.handleUpdate(update);
         }

        responses.ifPresent(responsesList -> responsesList.forEach(resp -> {
            try {
                execute(resp);
            } catch (TelegramApiException e) {
                log.warn("caught an exception {} with message {} while sending message",
                         e.getClass().getSimpleName(),
                         e.getMessage());

            }
        }));
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
