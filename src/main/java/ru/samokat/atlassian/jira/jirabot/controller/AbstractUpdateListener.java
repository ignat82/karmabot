package ru.samokat.atlassian.jira.jirabot.controller;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

public abstract class AbstractUpdateListener {
    public final Optional<List<BotApiMethod>> notify(Update update) {
        boolean isTarget = isTarget(update);
        return isTarget ? handleUpdate(update) : Optional.empty();
    }

    public abstract Optional<List<BotApiMethod>> handleUpdate(Update update);
    public abstract boolean isTarget(Update update);
}
