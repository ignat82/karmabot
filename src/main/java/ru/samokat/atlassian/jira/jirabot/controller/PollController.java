package ru.samokat.atlassian.jira.jirabot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.samokat.atlassian.jira.jirabot.KarmaAdapter;
import ru.samokat.atlassian.jira.jirabot.entity.PollRecord;
import ru.samokat.atlassian.jira.jirabot.repository.PollRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PollController {
    private final PollRepository pollRepository;
    private final KarmaAdapter karmaAdapter;
    private static final String CONFIRMED = "confirmed";
    private static final String DECLINED = "declined";
    public Optional<List<BotApiMethod>> handleUpdate(Update update) {
        if (isPollRequest(update)) {
            return createPoll(update.getMessage());
        } else if (update.getCallbackQuery() != null) {
            return processCallback(update.getCallbackQuery());
        }

        return Optional.empty();
    }

    private Optional<List<BotApiMethod>> processCallback(CallbackQuery callbackQuery) {
        List<BotApiMethod> responses = new ArrayList<>();
        responses.add(AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId()).build());
        Optional<List<BotApiMethod>> selfPoll = isSelfPoll(callbackQuery);
        if (selfPoll.isPresent()) {
            responses.addAll(selfPoll.get());
            return Optional.of(responses);
        }

        InlineKeyboardMarkup keyboardMarkup = callbackQuery.getMessage().getReplyMarkup();
        int confirmed = getPolled(keyboardMarkup.getKeyboard().get(0).get(0));
        int declined = getPolled(keyboardMarkup.getKeyboard().get(0).get(1));
        if (callbackQuery.getData().equals(CONFIRMED)) {
            confirmed++;
        } else {
            declined++;
        }

        if (confirmed == 3 || declined == 3) {
            responses.addAll(closePoll(callbackQuery, confirmed, declined));
        } else {
            responses.add(updatePoll(callbackQuery, confirmed, declined));
        }
        log.trace("responses assembled");
        return Optional.of(responses);
    }

    private Optional<List<BotApiMethod>> isSelfPoll(CallbackQuery callbackQuery) {
        User voter = callbackQuery.getFrom();
        if (!callbackQuery.getMessage().getReplyToMessage().getFrom().equals(voter)) {
            return Optional.empty();
        }
        List<BotApiMethod> responses = new ArrayList<>();
        String message = String.format("%s, Вы же в курсе, что за себя голосовать грешновато? ОЧКО НА СТОЛ!!!",
                                       voter.getUserName());
        responses.add(assembleChatMessage(message, callbackQuery.getMessage().getChatId()));
        responses.add(karmaAdapter.decreaseKarma(voter.getUserName(), voter.getId(), callbackChatId(callbackQuery)).get());
        return Optional.of(responses);
    }

    private List<BotApiMethod> closePoll(CallbackQuery callbackQuery, int confirmed, int declined) {
        List<BotApiMethod> responses = new ArrayList<>();
        Message initialMessage = callbackQuery.getMessage().getReplyToMessage();
        responses.add(EditMessageReplyMarkup.builder()
                                            .chatId(callbackQuery.getMessage().getChatId())
                                            .messageId(callbackQuery.getMessage().getMessageId())
                                            .replyMarkup(null)
                                            .build());
        String messageText;
        long chatId = callbackQuery.getMessage().getChatId();
        PollRecord pollRecord = pollRepository.getByChatIdAndMessageId(chatId, initialMessage.getMessageId()).get();
        if (confirmed == 3) {
            messageText = String.format("душнота подтверждена %s голосами 'за' при %s 'против'", confirmed, declined);
            responses.add(assembleChatMessage(messageText, chatId, callbackQuery.getMessage().getReplyToMessage().getMessageId()));
            responses.add(karmaAdapter.increaseKarma(initialMessage.getFrom().getUserName(), initialMessage.getFrom().getId(), chatId).get());
        } else {
            messageText = String.format("духотой и не пахнет, %s! Придется попрощаться с очком", pollRecord.getGiverUsername());
            responses.add(assembleChatMessage(messageText, chatId, callbackQuery.getMessage().getReplyToMessage().getMessageId()));
            responses.add(karmaAdapter.decreaseKarma(pollRecord.getGiverUsername(), pollRecord.getGiverId(), chatId).get());
        }
        return responses;
    }

    private BotApiMethod updatePoll(CallbackQuery callbackQuery, int confirmed, int declined) {
        PollRecord pollRecord = pollRepository.getByChatIdAndMessageId(callbackQuery.getMessage().getChatId(),
                                                                       callbackQuery.getMessage().getReplyToMessage().getMessageId())
                                              .get();
        Set<Long> pollerIds = Arrays.stream(pollRecord.getPolled().split(", "))
                                    .map(Long::parseLong)
                                    .collect(Collectors.toSet());
        if (pollerIds.contains(callbackQuery.getFrom().getId())) {
            log.trace("user voted already");
            String messageText = String.format("%s, а ты ведь не от большого ума несколько раз голосуешь?",
                                               callbackQuery.getFrom().getUserName());
            return assembleChatMessage(messageText, callbackChatId(callbackQuery), polledMessageId(callbackQuery));
        }
        pollRecord.setPolled(pollRecord.getPolled().concat(", ").concat(callbackQuery.getFrom().getId().toString()));
        pollRepository.save(pollRecord);
        return EditMessageReplyMarkup.builder()
                                     .chatId(callbackQuery.getMessage().getChatId())
                                     .messageId(callbackQuery.getMessage().getMessageId())
                                     .replyMarkup(createPollMarkUp(confirmed, declined))
                                     .build();
    }

    private int getPolled(InlineKeyboardButton keyboardButton) {
        String buttonText = keyboardButton.getText();
        Pattern pattern = Pattern.compile("\\b(\\d)/");
        Matcher matcher = pattern.matcher(buttonText);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    private Optional<List<BotApiMethod>> createPoll(Message message) {
        log.debug("createPoll()");
        SendMessage sm;

        if (pollRepository.getByChatIdAndMessageId(message.getChatId(), message.getReplyToMessage().getMessageId()).isPresent()) {
            sm = assembleChatMessage("зациклило? по два раза сообщения не оцениваем", message.getChatId(), message.getMessageId());
        } else if (message.getFrom().equals(message.getReplyToMessage().getFrom())) {
            sm = assembleChatMessage("дома себе подрочи, а не в общем чате", message.getChatId(), message.getMessageId());
        } else if (message.getReplyToMessage().getFrom().getIsBot()) {
            sm = assembleChatMessage("робота пылесоса тоже приласкать пытаешься?", message.getChatId(), message.getMessageId());

        } else {
            PollRecord pollRecord = new PollRecord(message.getChatId(),
                                                   message.getReplyToMessage().getMessageId(),
                                                   message.getFrom().getId(),
                                                   message.getFrom().getUserName(),
                                                   message.getReplyToMessage().getFrom().getId());
            pollRepository.save(pollRecord);
            sm = SendMessage.builder()
                            .chatId(message.getChatId())
                            .replyToMessageId(message.getReplyToMessage().getMessageId())
                            .parseMode("HTML").text("a это точно душно?")
                            .replyMarkup(createPollMarkUp(1, 0))
                            .build();
        }

        return Optional.of(Collections.singletonList(sm));
    }

    private InlineKeyboardMarkup createPollMarkUp(int confNumb, int declNumb) {
        InlineKeyboardButton confirmed = InlineKeyboardButton.builder()
                                                             .text("душно (" + confNumb + "/3)")
                                                             .callbackData(CONFIRMED)
                                                             .build();
        InlineKeyboardButton declined = InlineKeyboardButton.builder()
                                                            .text("не душно (" + declNumb + "/3)")
                                                            .callbackData(DECLINED)
                                                            .build();
        InlineKeyboardMarkup confirmationKeyboard = InlineKeyboardMarkup.builder()
                                                                        .keyboardRow(List.of(confirmed, declined))
                                                                        .build();
        return confirmationKeyboard;
    }

    private boolean isPollRequest(Update update) {
        return update.getMessage() != null && update.getMessage().getReplyToMessage() != null && update.getMessage().getText().equals("+");
    }

    private long callbackChatId(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getChatId();
    }

    private int polledMessageId(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getReplyToMessage().getMessageId();
    }

    private SendMessage assembleChatMessage(String text, long chatId, Integer replyToMessageId ) {
        SendMessage.SendMessageBuilder messageBuilder = SendMessage.builder();
        if (replyToMessageId != null) {
            messageBuilder.replyToMessageId(replyToMessageId);
        }
        return messageBuilder.chatId(chatId).text(text).build();
    }

    private SendMessage assembleChatMessage(String text, long chatId) {
        return assembleChatMessage(text, chatId, null);
    }
}
