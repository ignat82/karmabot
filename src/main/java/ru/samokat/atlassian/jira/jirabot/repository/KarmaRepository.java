package ru.samokat.atlassian.jira.jirabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.samokat.atlassian.jira.jirabot.entity.KarmaRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface KarmaRepository extends JpaRepository<KarmaRecord, Long> {
    Optional<KarmaRecord> getByUserIdAndChatId(long userId, long chatId);
    Optional<List<KarmaRecord>> getByChatId(long chatId);
    List<KarmaRecord> findAll();
 }
