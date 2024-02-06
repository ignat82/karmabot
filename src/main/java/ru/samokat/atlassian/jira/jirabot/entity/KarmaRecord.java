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

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "karma")
public class KarmaRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "karma_generator")
    @SequenceGenerator(name = "karma_generator", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "karma_points")
    private int karmaPoints;
    @Column(name = "daily_karma_points")
    private int dailyKarmaPoints;
    @Column(name = "chat_id")
    private long chatId;

    public KarmaRecord(long userId, String userName, long chatId) {
        this.userId = userId;
        this.userName = userName;
        this.chatId = chatId;
    }

    public void increaseKarma() {
        karmaPoints++;
        dailyKarmaPoints++;
    }

    public void decreaseKarma() {
        karmaPoints--;
        dailyKarmaPoints--;
    }
}
