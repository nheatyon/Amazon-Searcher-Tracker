package com.nheatyon.searchoffersbot;

import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.UserCheckerOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.DelegatorProcessor;
import com.nheatyon.searchoffersbot.delegators.impl.ChatDelegator;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.CallbackDelegator;
import com.nheatyon.searchoffersbot.delegators.impl.commands.CommandsDelegator;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Main bot class.
 * @author nheatyon
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public class RSearchOffersBot extends TelegramLongPollingBot {

    private final ConfigurationManager<?, String> config;
    private final ConfigurationManager<Object, String> serializer;
    private final OperatorFactory operators;
    private final KeyboardsDeclarator keyboards;

    @Override
    public final String getBotUsername() {
        return config.read("bot_name");
    }

    @Override
    public final String getBotToken() {
        return config.read("bot_token");
    }

    private void validateUserInDatabase(User user, DatabaseOperator userChecker) {
        String userId = user.getId().toString();
        String username = user.getUserName();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String fullName = firstName + " " + lastName;
        if (username == null) {
            username = "/";
        }
        if (lastName == null) {
            fullName = firstName;
        }
        userChecker.set(userId, fullName, username);
    }

    @Override
    @SneakyThrows
    public final void onUpdateReceived(Update update) {
        BotOperations bot = new BotOperations(config, serializer, operators, keyboards);
        DelegatorProcessor delegator = new DelegatorProcessor();
        DatabaseOperator userChecker = operators.get(UserCheckerOperator.class);
        Message msg;
        if (update.hasMessage()) {
            msg = update.getMessage();
            long userId = msg.getFrom().getId();
            String chatId = msg.getChatId().toString();
            boolean isTelegram = userId == 777000;
            // Leave if the bot is added to a group
            if (msg.getLeftChatMember() == null && msg.isGroupMessage() || msg.isSuperGroupMessage()) {
                LeaveChat leaveChat = LeaveChat.builder()
                        .chatId(chatId)
                        .build();
                execute(leaveChat);
                return;
            }
            // Private chat only
            if (msg.getFrom().getIsBot() || isTelegram) {
                return;
            }
            validateUserInDatabase(msg.getFrom(), userChecker);
            if (msg.hasText() && msg.getText().startsWith("/")) {
                // Check for commands (with and without args) after registering the user
                delegator.execute(bot, new CommandsDelegator(config, serializer, operators, keyboards), update);
                return;
            }
            delegator.execute(bot, new ChatDelegator(config, serializer, operators, keyboards), update);
            return;
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            validateUserInDatabase(query.getFrom(), userChecker);
            delegator.execute(bot, new CallbackDelegator(config, serializer, operators, keyboards), query);
        }
    }
}
