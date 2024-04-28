package ru.samokat.atlassian.jira.jirabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<PointRecord, Long> {
    Optional<PointRecord> getByUserIdAndChatIdAndPointTypeName(long userId, long chatId, String pointTypeName);
    Optional<List<PointRecord>> getByChatIdAndPointTypeName(long chatId, String pointTypeName);
    List<PointRecord> findAll();
 }
