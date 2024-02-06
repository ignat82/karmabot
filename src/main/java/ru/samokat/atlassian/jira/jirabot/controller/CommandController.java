package ru.samokat.atlassian.jira.jirabot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.samokat.atlassian.jira.jirabot.entity.KarmaRecord;
import ru.samokat.atlassian.jira.jirabot.repository.KarmaRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandController {
    private static final List<String> RATING_COMMANDS = List.of("/rating", "/rating@samokat_jira_bot");
    private final KarmaRepository karmaRepository;

    public Optional<List<BotApiMethod>> handleUpdate(Update update) {
        log.debug("handleUpdate()");
        Message message = update.getMessage();

        if(message.isCommand() && RATING_COMMANDS.contains(message.getText())) {
            log.trace("assembling rating");
            return assembleRatingForChat(message.getChatId());
        }

        return Optional.empty();
    }

    public Optional<List<BotApiMethod>> assembleRatingForChat(long chatId) {
        String messagePrefix = "рейтинг душнил на сегодня:\n\n";
        String rating = karmaRepository.getByChatId(chatId)
                              .map(ratingList -> {

                                  ratingList.sort(Comparator.comparingInt(KarmaRecord::getDailyKarmaPoints).reversed());
                                  return ratingList.stream()
                                                   .map(it -> it.getUserName()
                                                                .concat(" - ")
                                                                .concat(String.valueOf(it.getDailyKarmaPoints())))
                                                   .collect(Collectors.joining("\n"));
                              })
                              .orElse("...еще пуст");
        return Optional.of(Collections.singletonList(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(messagePrefix.concat(rating))
                                      .build()));
    }
}
