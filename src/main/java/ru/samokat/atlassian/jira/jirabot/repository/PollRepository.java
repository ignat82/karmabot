package ru.samokat.atlassian.jira.jirabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.samokat.atlassian.jira.jirabot.entity.PollRecord;

import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<PollRecord, Long> {
    Optional<PollRecord> getByChatIdAndMessageId(long chatId, long messageId);
//    Optional<Long> getGiverIdByChatIdAndMessageId(long chatId, long messageId);
//    Optional<String> getGiverUsernameByChatIdAndMessageId(long chatId, long messageId);
}
