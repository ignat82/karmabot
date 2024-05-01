package ru.samokat.atlassian.jira.jirabot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;
import ru.samokat.atlassian.jira.jirabot.repository.PointRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandController extends AbstractUpdateListener {
    private final PointRepository pointRepository;

    @Override
    public Optional<List<BotApiMethod>> handleUpdate(Update update) {
        log.debug("handleUpdate()");
        Message message = update.getMessage();

        log.trace("assembling rating");
        return assembleRatingForChat(message.getChatId(),
                                     PointRecord.PointType.fromRatingCommand(message.getText()).get());

    }

    @Override
    public boolean isTarget(Update update) {
        boolean isCommand = update.getMessage() != null && update.getMessage().isCommand();
        return isCommand && PointRecord.PointType.fromRatingCommand(update.getMessage().getText()).isPresent();
    }

    private Optional<List<BotApiMethod>> assembleRatingForChat(long chatId, PointRecord.PointType pointType) {
        String messagePrefix = String.format("рейтинг %s на сегодня:\n\n", pointType.getRatingTerm());
        String rating = pointRepository.getByChatIdAndPointTypeName(chatId, pointType.name())
                                       .map(ratingList -> {
                                          ratingList.sort(Comparator.comparingInt(PointRecord::getDailyPoints).reversed());
                                          return ratingList.isEmpty()
                                                  ? "...еще пуст"
                                                  : ratingList.stream()
                                                              .map(it -> it.getUserName().concat(" - ").concat(String.valueOf(it.getDailyPoints())))
                                                              .collect(Collectors.joining("\n"));
                                       }).get();
        return Optional.of(Collections.singletonList(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(messagePrefix.concat(rating))
                                      .build()));
    }
}
