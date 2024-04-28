package ru.samokat.atlassian.jira.jirabot.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.samokat.atlassian.jira.jirabot.JiraBot;
import ru.samokat.atlassian.jira.jirabot.controller.ActionsController;
import ru.samokat.atlassian.jira.jirabot.controller.CommandController;
import ru.samokat.atlassian.jira.jirabot.controller.PollController;

@Configuration
@ComponentScan
@Slf4j
public class JiraBotConfiguration {

    @Bean
    public JiraBot registerBot(CommandController commandController,
                               PollController pollController,
                               ActionsController actionsController,
                               @Value("${ru.samokat.atlassian.jira.jirabot.token}") String botToken)
            throws TelegramApiException {
        log.debug("registerBot()");
        JiraBot jiraBot = new JiraBot(commandController, pollController, actionsController, botToken);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(jiraBot);
        jiraBot.giveVoice();
        return jiraBot;
    }

    @Bean Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();

    }
}
