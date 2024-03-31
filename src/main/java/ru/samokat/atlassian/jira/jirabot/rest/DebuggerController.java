package ru.samokat.atlassian.jira.jirabot.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.samokat.atlassian.jira.jirabot.JiraBot;
import ru.samokat.atlassian.jira.jirabot.entity.KarmaRecord;
import ru.samokat.atlassian.jira.jirabot.repository.KarmaRepository;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DebuggerController {
    private final JiraBot jiraBot;
    private final KarmaRepository karmaRepository;



    @GetMapping("/pick_karma/{userId}/{chatId}")
    void pickKarma(@PathVariable String userId, @PathVariable String chatId) {
        log.debug("test({}, {})", userId, chatId);
        Optional<KarmaRecord> karmaRecord = karmaRepository.getByUserIdAndChatId(Long.parseLong(userId), Long.parseLong(chatId));

    }

}
