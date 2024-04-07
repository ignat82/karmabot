package ru.samokat.atlassian.jira.jirabot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.samokat.atlassian.jira.jirabot.entity.KarmaRecord;
import ru.samokat.atlassian.jira.jirabot.repository.KarmaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActionsController {
    private final KarmaRepository karmaRepository;
    private final String UPDATE_MESSAGE_TEXT = "Я чувствую себя обновленным";

    public Optional<List<BotApiMethod>> getUpdateMessages() {
        log.debug("getUpdateMessages()");
        List<KarmaRecord> karmaRecords = karmaRepository.findAll();
        List<BotApiMethod> messages = karmaRecords.stream()
                                                  .map(KarmaRecord::getChatId)
                                                  .distinct()
                                                  .map(chatId -> SendMessage.builder()
                                                                            .chatId(chatId)
                                                                            .text(UPDATE_MESSAGE_TEXT)
                                                                            .build())
                                                  .collect(Collectors.toList());
        return Optional.of(messages);
    }

}
