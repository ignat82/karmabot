package ru.samokat.atlassian.jira.jirabot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "poll")
@Getter
@Setter
@NoArgsConstructor
public class PollRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "poll_generator")
    @SequenceGenerator(name = "poll_generator", sequenceName = "poll_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "message_id")
    private Long messageId;
    @Column(name = "giver_id")
    private long giverId;
    @Column(name = "giver_username")
    private String giverUsername;
    @Column(name = "recipient_id")
    private long recipientId;
    @Column(name = "polled")
    private String polled;
    @Column(name = "point_type")
    private String pointTypeName;

    public PollRecord(long chatId,
                      long messageId,
                      long giverId,
                      String giverUsername,
                      long recipientId,
                      PointRecord.PointType pointType) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.giverId = giverId;
        this.giverUsername = giverUsername;
        this.recipientId = recipientId;
        this.polled = String.valueOf(giverId);
        this.pointTypeName = pointType.name();
    }

}
