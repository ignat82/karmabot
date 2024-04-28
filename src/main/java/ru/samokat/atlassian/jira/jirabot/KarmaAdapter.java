package ru.samokat.atlassian.jira.jirabot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;
import ru.samokat.atlassian.jira.jirabot.repository.PointRepository;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class KarmaAdapter {
    private final PointRepository pointRepository;

    public Optional<BotApiMethod> addPoint(String userName, long userId, long chatId, PointRecord.PointType pointType) {
        PointRecord pointRecord = pointRepository.getByUserIdAndChatIdAndPointTypeName(userId, chatId, pointType.name())
                                                 .orElseGet(() -> new PointRecord(userId,
                                                                                  userName,
                                                                                  chatId,
                                                                                  pointType.name()));
        pointRecord.addPoint();
        pointRepository.save(pointRecord);
        return Optional.of(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(String.format("добавил очко душноты %s(%s)",
                                                          userName,
                                                          pointRecord.getPoints())).build());
    }

    public Optional<BotApiMethod> deductPoint(String userName, long userId, long chatId, PointRecord.PointType pointType) {
        PointRecord pointRecord = pointRepository.getByUserIdAndChatIdAndPointTypeName(userId, chatId, pointType.name())
                                                 .orElseGet(() -> new PointRecord(userId,
                                                                                  userName,
                                                                                  chatId,
                                                                                  pointType.name()));
        pointRecord.deductPoint();
        pointRepository.save(pointRecord);
        return Optional.of(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(String.format("отнял очко душноты %s(%s)",
                                                          userName,
                                                          pointRecord.getPoints())).build());
    }
}
