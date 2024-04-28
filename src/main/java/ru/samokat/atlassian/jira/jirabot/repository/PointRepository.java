package ru.samokat.atlassian.jira.jirabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<PointRecord, Long> {
    Optional<PointRecord> getByUserIdAndChatIdAndPointType(long userId, long chatId, PointRecord.PointType pointType);
    Optional<List<PointRecord>> getByChatIdAndPointType(long chatId, PointRecord.PointType pointType);
    List<PointRecord> findAll();
 }
