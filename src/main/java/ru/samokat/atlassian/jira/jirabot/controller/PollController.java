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
import ru.samokat.atlassian.jira.jirabot.entity.PointRecord;
import ru.samokat.atlassian.jira.jirabot.entity.PollRecord;
import ru.samokat.atlassian.jira.jirabot.repository.PollRepository;
import ru.samokat.atlassian.jira.jirabot.service.ChatService;

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
public class PollController extends AbstractUpdateListener {
    private final PollRepository pollRepository;
    private final KarmaAdapter karmaAdapter;
    private final ChatService chatService;
    private static final String CONFIRMED = "confirmed";
    private static final String DECLINED = "declined";
    @Override
    public Optional<List<BotApiMethod>> handleUpdate(Update update) {

        if (update.getMessage() != null) {
            Optional<PointRecord.PointType> pointType =
                    PointRecord.PointType.fromCreateCommand(update.getMessage().getText());
            return pointType.isPresent() ? createPoll(update.getMessage(), pointType.get()) : Optional.empty();
        } else if (update.getCallbackQuery() != null) {
            return processCallback(update.getCallbackQuery());
        }

        return Optional.empty();
    }

    @Override
    public boolean isTarget(Update update) {
        if (update.getCallbackQuery() != null) {
            return true;
        }

        boolean isTextMessage = update.getMessage() != null && update.getMessage().getText() != null;
        if (!isTextMessage) {
            return false;
        }

        return PointRecord.PointType.fromCreateCommand(update.getMessage().getText()).isPresent();
    }

    private Optional<List<BotApiMethod>> processCallback(CallbackQuery callbackQuery) {
        List<BotApiMethod> responses = new ArrayList<>();
        responses.add(AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId()).build());

        PollRecord pollRecord = pollRepository.getByChatIdAndMessageId(callbackQuery.getMessage().getChatId(),
                                                                       callbackQuery.getMessage().getReplyToMessage().getMessageId())
                                              .orElseThrow();

        Optional<List<BotApiMethod>> selfPoll = isSelfPoll(callbackQuery, pollRecord);
        if (selfPoll.isPresent()) {
            responses.addAll(selfPoll.get());
            return Optional.of(responses);
        }


        Optional<BotApiMethod> revote = isRevote(callbackQuery, pollRecord);
        if (revote.isPresent()) {
            responses.add(revote.get());
            return Optional.of(responses);
        }

        InlineKeyboardMarkup keyboardMarkup = callbackQuery.getMessage().getReplyMarkup();
        int confirmed = getPolled(keyboardMarkup.getKeyboard().get(0).get(0));
        int declined = getPolled(keyboardMarkup.getKeyboard().get(0).get(1));
        if (callbackQuery.getData().equals(CONFIRMED)) {
            confirmed++;
            log.trace("user {} upvoted query", callbackQuery.getFrom().getUserName());
        } else {
            declined++;
            log.trace("user {} downvoted query", callbackQuery.getFrom().getUserName());
        }

        if (confirmed == 3 || declined == 3) {
            responses.addAll(closePoll(callbackQuery, confirmed, declined, pollRecord));
        } else {
            responses.add(updatePoll(callbackQuery, confirmed, declined, pollRecord));
        }
        log.trace("responses assembled");
        return Optional.of(responses);
    }

    private Optional<BotApiMethod> isRevote(CallbackQuery callbackQuery, PollRecord pollRecord) {
        log.debug("isRevote()");
        Set<Long> pollerIds = Arrays.stream(pollRecord.getPolled().split(", "))
                                    .map(Long::parseLong)
                                    .collect(Collectors.toSet());
        if (pollerIds.contains(callbackQuery.getFrom().getId())) {
            log.trace("{} has voted already", callbackQuery.getFrom().getUserName());
            String messageText = String.format("%s, а ты ведь не от большого ума несколько раз голосуешь?",
                                               callbackQuery.getFrom().getUserName());
            return Optional.of(chatService.assembleChatMessage(messageText, callbackChatId(callbackQuery), polledMessageId(callbackQuery)));
        } else {
            return Optional.empty();
        }
    }

    private Optional<List<BotApiMethod>> isSelfPoll(CallbackQuery callbackQuery, PollRecord pollRecord) {
        User voter = callbackQuery.getFrom();
        if (pollRecord.getRecipientId() != voter.getId()) {
            return Optional.empty();
        }
        List<BotApiMethod> responses = new ArrayList<>();
        String message = String.format("%s, Вы же в курсе, что за себя голосовать грешновато, и голосуете. Так сильно очко манит?",
                                       voter.getUserName());
        responses.add(chatService.assembleChatMessage(message, pollRecord.getChatId()));
        log.trace("user {} tried selfvote", voter.getUserName());
        return Optional.of(responses);
    }

    private List<BotApiMethod> closePoll(CallbackQuery callbackQuery, int confirmed, int declined, PollRecord pollRecord) {
        List<BotApiMethod> responses = new ArrayList<>();
        Message initialMessage = callbackQuery.getMessage().getReplyToMessage();
        responses.add(EditMessageReplyMarkup.builder()
                                            .chatId(callbackQuery.getMessage().getChatId())
                                            .messageId(callbackQuery.getMessage().getMessageId())
                                            .replyMarkup(null)
                                            .build());
        String messageText;
        long chatId = callbackQuery.getMessage().getChatId();

        PointRecord.PointType pointType = PointRecord.PointType.valueOf(pollRecord.getPointTypeName());

        if (confirmed == 3) {
            messageText = String.format("%s подтверждена %s голосами 'за' при %s 'против'", pointType.getVotePrompt(), confirmed, declined);
            responses.add(chatService.assembleChatMessage(messageText, chatId, callbackQuery.getMessage().getReplyToMessage().getMessageId()));
            responses.add(karmaAdapter.addPoint(initialMessage.getFrom().getUserName(),
                                                initialMessage.getFrom().getId(),
                                                chatId,
                                                PointRecord.PointType.valueOf(pollRecord.getPointTypeName())).get());
            log.trace("poll closed. recipient {} received karma point", callbackQuery.getFrom().getUserName());
        } else {
            messageText = String.format("%s и не пахнет, %s! Придется попрощаться с очком", pointType.getVoteFail(), pollRecord.getGiverUsername());
            responses.add(chatService.assembleChatMessage(messageText, chatId, callbackQuery.getMessage().getReplyToMessage().getMessageId()));
            responses.add(karmaAdapter.deductPoint(pollRecord.getGiverUsername(),
                                                   pollRecord.getGiverId(),
                                                   chatId,
                                                   PointRecord.PointType.valueOf(pollRecord.getPointTypeName())).get());
            log.trace("poll closed. giver {} lost karma point", callbackQuery.getFrom().getUserName());
        }
        return responses;
    }

    private BotApiMethod updatePoll(CallbackQuery callbackQuery, int confirmed, int declined, PollRecord pollRecord) {
        pollRecord.setPolled(pollRecord.getPolled().concat(", ").concat(callbackQuery.getFrom().getId().toString()));
        pollRepository.save(pollRecord);
        return EditMessageReplyMarkup.builder()
                                     .chatId(callbackQuery.getMessage().getChatId())
                                     .messageId(callbackQuery.getMessage().getMessageId())
                                     .replyMarkup(createPollMarkUp(confirmed, declined, PointRecord.PointType.valueOf(pollRecord.getPointTypeName())))
                                     .build();
    }

    private int getPolled(InlineKeyboardButton keyboardButton) {
        String buttonText = keyboardButton.getText();
        Pattern pattern = Pattern.compile("\\b(\\d)/");
        Matcher matcher = pattern.matcher(buttonText);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    private Optional<List<BotApiMethod>> createPoll(Message message, PointRecord.PointType pointType) {
        log.debug("createPoll()");

        if (message.getReplyToMessage() == null) {
            log.warn("no message to poll about");
            return Optional.empty();
        }

        SendMessage sm;
        if (pollRepository.getByChatIdAndMessageId(message.getChatId(), message.getReplyToMessage().getMessageId()).isPresent()) {
            sm = chatService.assembleChatMessage("зациклило? по два раза сообщения не оцениваем", message.getChatId(), message.getMessageId());
        } else if (message.getFrom().equals(message.getReplyToMessage().getFrom())) {
            sm = chatService.assembleChatMessage("дома себе подрочи, а не в общем чате", message.getChatId(), message.getMessageId());
        } else if (message.getReplyToMessage().getFrom().getIsBot()) {
            sm = chatService.assembleChatMessage("робота пылесоса тоже приласкать пытаешься?", message.getChatId(), message.getMessageId());

        } else {
            PollRecord pollRecord = new PollRecord(message.getChatId(),
                                                   message.getReplyToMessage().getMessageId(),
                                                   message.getFrom().getId(),
                                                   message.getFrom().getUserName(),
                                                   message.getReplyToMessage().getFrom().getId(),
                                                   pointType);
            pollRepository.save(pollRecord);
            sm = SendMessage.builder()
                            .chatId(message.getChatId())
                            .replyToMessageId(message.getReplyToMessage().getMessageId())
                            .parseMode("HTML").text(String.format("a это точно %s?", pointType.getVotePrompt()))
                            .replyMarkup(createPollMarkUp(1, 0, pointType))
                            .build();
        }

        return Optional.of(Collections.singletonList(sm));
    }

    private InlineKeyboardMarkup createPollMarkUp(int confNumb, int declNumb, PointRecord.PointType pointType) {
        InlineKeyboardButton confirmed = InlineKeyboardButton.builder()
                                                             .text(String.format("%s (" + confNumb + "/3)", pointType.getVotePrompt()))
                                                             .callbackData(CONFIRMED)
                                                             .build();
        InlineKeyboardButton declined = InlineKeyboardButton.builder()
                                                            .text(String.format("не %s (" + declNumb + "/3)", pointType.getVotePrompt()))
                                                            .callbackData(DECLINED)
                                                            .build();
        InlineKeyboardMarkup confirmationKeyboard = InlineKeyboardMarkup.builder()
                                                                        .keyboardRow(List.of(confirmed, declined))
                                                                        .build();
        return confirmationKeyboard;
    }

    private long callbackChatId(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getChatId();
    }

    private int polledMessageId(CallbackQuery callbackQuery) {
        return callbackQuery.getMessage().getReplyToMessage().getMessageId();
    }

}
