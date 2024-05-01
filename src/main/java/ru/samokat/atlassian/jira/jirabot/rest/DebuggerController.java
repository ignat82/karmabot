package ru.samokat.atlassian.jira.jirabot.rest;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.samokat.atlassian.jira.jirabot.client.gigachat.GigaChatClient;
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;
import ru.samokat.atlassian.jira.jirabot.repository.PointRepository;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DebuggerController {
    private final GigaChatClient gigaChatClient;
    private final PointRepository pointRepository;
    private final Gson gson;

    @GetMapping("/pick_karma/{userId}/{chatId}/{pointType}")
    void pickKarma(@PathVariable String userId,
                   @PathVariable String chatId,
                   @PathVariable PointRecord.PointType pointType) {
        log.debug("test({}, {})", userId, chatId);
        Optional<PointRecord> pointRecord =
                pointRepository.getByUserIdAndChatIdAndPointTypeName(Long.parseLong(userId),
                                                                     Long.parseLong(chatId),
                                                                     pointType.name());
        log.trace("got pointRecord {}", gson.toJson(pointRecord));
    }

    @PostMapping("/send_question")
    void sendQuestion(@RequestBody String body) {
        log.debug("sendQuestion({})", body);
        Optional<String> response = gigaChatClient.sendQuestion(body);
        response.ifPresent(s -> log.trace("got response {}", s));

    }

}
