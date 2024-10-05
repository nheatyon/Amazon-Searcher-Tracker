package com.nheatyon.searchoffersbot.delegators.impl.commands;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.annotations.BotCommand;
import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.AdminOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.SearchingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.TrackingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.Delegator;
import com.nheatyon.searchoffersbot.delegators.impl.commands.GenericCommand.GenericCommandBuilder;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * @author nheatyon
 * @see com.nheatyon.searchoffersbot.delegators.Delegator
 */
public final class CommandsDelegator extends BotOperations implements Delegator<Update> {

    private static final Set<Class<?>> ALL_COMMANDS_CLASSES = new Reflections(
            CommandsDelegator.class.getPackageName(),
            new SubTypesScanner(false))
            .getSubTypesOf(Object.class);

    public CommandsDelegator(ConfigurationManager<?, String> config, ConfigurationManager<Object, String> serializer, OperatorFactory operators, KeyboardsDeclarator keyboards) {
        super(config, serializer, operators, keyboards);
    }

    private void checkIfAdmin(String userId, BotCommand command, Runnable v) {
        if (command.adminCommand()) {
            DatabaseOperator adminOperator = getOperators().get(AdminOperator.class);
            if (!(boolean) adminOperator.get(userId)) {
                return;
            }
        }
        v.run();
    }

    @Override
    public void delegate(BotOperations bot, Update update) {
        Message msg = update.getMessage();
        String userId = msg.getFrom().getId().toString();
        String chatId = msg.getChatId().toString();
        // Check if users can send commands
        boolean isSearching = getOperators().get(SearchingOperator.class).get(userId);
        boolean isTracking = getOperators().get(TrackingOperator.class).get(userId);
        if (isSearching || isTracking) {
            return;
        }
        // Iterate over all commands
        for (Class<?> c : ALL_COMMANDS_CLASSES) {
            BotCommand botAnnotation = c.getAnnotation(BotCommand.class);
            if (!c.isAnnotationPresent(BotCommand.class)) {
                continue;
            }
            BotCommand command = c.getAnnotation(BotCommand.class);
            String[] args = msg.getText().trim().replaceAll(" +", " ").split(" ");
            if (!args[0].equalsIgnoreCase("/" + command.value())) {
                continue;
            }
            // Runnable for creating the command object and run it
            Runnable v = () -> {
                try {
                    GenericCommandBuilder<?, ?> builder = (GenericCommandBuilder<?, ?>) c.getMethod("builder").invoke(null);
                    GenericCommand object = builder
                            .userId(userId)
                            .chatId(chatId)
                            .args(args)
                            .bot(bot)
                            .annotation(botAnnotation)
                            .update(update)
                            .build();
                    checkIfAdmin(userId, command, object::run);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    Logger.getRootLogger().error(e);
                }
            };
            if (command.variableArgs() || args.length == command.args()) {
                // Variable Arguments Commands
                if (command.async()) {
                    AsyncWrapper.runAsync(v);
                    return;
                }
                v.run();
                return;
            }
            String syntaxError = getConfig().read("syntax_error");
            if (command.args() != 1) {
                checkIfAdmin(userId, command, () -> sendMessageMarkdown(chatId,  syntaxError + command.syntax()));
                return;
            }
            checkIfAdmin(userId, command, () -> sendMessageMarkdown(chatId, syntaxError + "`/" + command.value() + "`"));
            return;
        }
    }
}
