package ru.samokat.atlassian.jira.jirabot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.samokat.atlassian.jira.jirabot.entity.KarmaRecord;
import ru.samokat.atlassian.jira.jirabot.repository.KarmaRepository;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class KarmaAdapter {
    private final KarmaRepository karmaRepository;

    public Optional<BotApiMethod> increaseKarma(String userName, long userId, long chatId) {
        KarmaRecord karmaRecord = karmaRepository.getByUserIdAndChatId(userId, chatId)
                                                 .orElseGet(() -> new KarmaRecord(userId,
                                                                                  userName,
                                                                                  chatId));
        karmaRecord.increaseKarma();
        karmaRepository.save(karmaRecord);
        return Optional.of(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(String.format("добавил очко душноты %s(%s)",
                                                          userName,
                                                          karmaRecord.getKarmaPoints())).build());
    }

    public Optional<BotApiMethod> decreaseKarma(String userName, long userId, long chatId) {
        KarmaRecord karmaRecord = karmaRepository.getByUserIdAndChatId(userId, chatId)
                                                 .orElseGet(() -> new KarmaRecord(userId,
                                                                                  userName,
                                                                                  chatId));
        karmaRecord.decreaseKarma();
        karmaRepository.save(karmaRecord);
        return Optional.of(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(String.format("отнял очко душноты %s(%s)",
                                                          userName,
                                                          karmaRecord.getKarmaPoints())).build());
    }

    public Optional<BotApiMethod> increaseToxic(String userName, long userId, long chatId) {
        KarmaRecord karmaRecord = karmaRepository.getByUserIdAndChatId(userId, chatId)
                                                 .orElseGet(() -> new KarmaRecord(userId,
                                                                                  userName,
                                                                                  chatId));
        karmaRecord.increaseToxic();
        karmaRepository.save(karmaRecord);
        return Optional.of(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(String.format("добавил очко токсичности %s(%s)",
                                                          userName,
                                                          karmaRecord.getKarmaPoints())).build());
    }

    public Optional<BotApiMethod> decreaseToxic(String userName, long userId, long chatId) {
        KarmaRecord karmaRecord = karmaRepository.getByUserIdAndChatId(userId, chatId)
                                                 .orElseGet(() -> new KarmaRecord(userId,
                                                                                  userName,
                                                                                  chatId));
        karmaRecord.decreaseToxic();
        karmaRepository.save(karmaRecord);
        return Optional.of(SendMessage.builder()
                                      .chatId(chatId)
                                      .text(String.format("отнял очко токсичности %s(%s)",
                                                          userName,
                                                          karmaRecord.getKarmaPoints())).build());
    }

}
