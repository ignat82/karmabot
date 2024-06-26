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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "karma")
public class PointRecord {
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
    private int points;
    @Column(name = "daily_karma_points")
    private int dailyPoints;
    @Column(name = "point_type")
    private String pointTypeName;
    @Column(name = "chat_id")
    private long chatId;

    public PointRecord(long userId, String userName, long chatId, String pointTypeName) {
        this.userId = userId;
        this.userName = userName;
        this.chatId = chatId;
        this.pointTypeName = pointTypeName;
    }

    public void addPoint() {
        points++;
        dailyPoints++;
    }

    public void deductPoint() {
        points--;
        dailyPoints--;
    }

    @Getter
    public enum PointType {
        KARMA(   "+",  "душно",    "душнота",     "духотой",      "душноты",     "/karma",    "душнил"),
        TOXICITY("++", "токсично", "токсичность", "токсичностью", "токсичности", "/toxicity", "токсиков");

        private final String createCommand;
        private final String votePrompt;
        private final String voteSuccess;
        private final String voteFail;
        private final String decreaseVote;
        private final String ratingCommand;
        private final String ratingTerm;
        private static final Map<String, PointType> BY_CREATE_COMMAND = Arrays.stream(PointType.values())
                                                                              .collect(Collectors.toMap(value -> value.createCommand, value -> value));
        private static final Map<String, PointType> BY_RATING_COMMAND = Arrays.stream(PointType.values())
                .collect(Collectors.toMap(val -> val.ratingCommand, val -> val));
        public static Optional<PointType> fromCreateCommand(String command) {
            return Optional.ofNullable(BY_CREATE_COMMAND.get(command));
        }
        public static Optional<PointType> fromRatingCommand(String ratingCommand) {
            return BY_RATING_COMMAND.containsKey(ratingCommand)
                    ? Optional.ofNullable(BY_RATING_COMMAND.get(ratingCommand))
                    : Optional.empty();
        }

        PointType(String createCommand,
                  String votePrompt,
                  String voteSuccess,
                  String voteFail,
                  String decreaseVote,
                  String ratingCommand,
                  String ratingTerm) {
            this.createCommand = createCommand;
            this.votePrompt = votePrompt;
            this.voteSuccess = voteSuccess;
            this.voteFail = voteFail;
            this.decreaseVote = decreaseVote;
            this.ratingCommand = ratingCommand;
            this.ratingTerm = ratingTerm;
        }

    }
}
