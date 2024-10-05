package com.nheatyon.searchoffersbot;

import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotOperations extends RSearchOffersBot {

    private final String welcomeMessage;

    public BotOperations(ConfigurationManager<?, String> config, ConfigurationManager<Object, String> serializer, OperatorFactory operators, KeyboardsDeclarator keyboards) {
        super(config, serializer, operators, keyboards);
        this.welcomeMessage = config.read("welcome_message");
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    final <T> T sendType(BotApiMethod<?> message) {
        return AsyncWrapper.supplyAsync(() -> {
            try {
                return (T) execute(message);
            } catch (TelegramApiException e) {
                Logger.getRootLogger().error(e);
            }
            return (T) new Message();
        }).get();
    }

    public final void sendWelcome(String userId) {
        sendMessage(userId, welcomeMessage, getKeyboards().getParsableMenu(this, userId));
    }

    public final void editWelcome(CallbackQuery callback) {
        String userId = callback.getFrom().getId().toString();
        editMessage(callback, welcomeMessage, getKeyboards().getParsableMenu(this, userId));
    }

    public final void sendMessage(String userId, String text, InlineKeyboardMarkup keyboards) {
        sendType(SendMessage.builder()
                .chatId(userId)
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboards)
                .disableWebPagePreview(true)
                .build());
    }

    public final void sendMessageMarkdown(String userId, String text) {
        sendType(SendMessage.builder()
                .chatId(userId)
                .text(text)
                .parseMode("MarkdownV2")
                .disableWebPagePreview(true)
                .build());
    }

    public final Message editMessage(CallbackQuery callback, String newText, InlineKeyboardMarkup keyboards) {
        String userId = callback.getFrom().getId().toString();
        int messageId = callback.getMessage().getMessageId();
        String inlineMessageId = callback.getInlineMessageId();
        return sendType(EditMessageText.builder()
                .chatId(userId)
                .messageId(messageId)
                .inlineMessageId(inlineMessageId)
                .text(newText)
                .parseMode("HTML")
                .replyMarkup(keyboards)
                .disableWebPagePreview(true)
                .build());
    }

    public final Message editMessage(String userId, int messageId, String newText, InlineKeyboardMarkup keyboards) {
        return sendType(EditMessageText.builder()
                .chatId(userId)
                .messageId(messageId)
                .text(newText)
                .parseMode("HTML")
                .replyMarkup(keyboards)
                .disableWebPagePreview(true)
                .build());
    }

    public final void answerCallback(CallbackQuery callback, String text) {
        sendType(AnswerCallbackQuery.builder()
                .callbackQueryId(callback.getId())
                .text(text)
                .showAlert(true)
                .build());
    }
}
