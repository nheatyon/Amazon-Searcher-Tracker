package com.nheatyon.searchoffersbot.delegators.impl.callbacks;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.SearchingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.TrackingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.Delegator;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Set;

/**
 * @author nheatyon
 * @see com.nheatyon.searchoffersbot.delegators.Delegator
 */
public final class CallbackDelegator extends BotOperations implements Delegator<CallbackQuery> {

    private static final Set<Class<?>> ALL_CALLBACK_CLASSES = new Reflections(
            CallbackDelegator.class.getPackageName(),
            new SubTypesScanner(false))
            .getSubTypesOf(Object.class);

    public CallbackDelegator(ConfigurationManager<?, String> config, ConfigurationManager<Object, String> serializer, OperatorFactory operators, KeyboardsDeclarator keyboards) {
        super(config, serializer, operators, keyboards);
    }

    @Override
    @SneakyThrows
    public void delegate(BotOperations bot, CallbackQuery callback) {
        String callbackData = callback.getData();
        String userId = callback.getFrom().getId().toString();
        boolean isSearching = getOperators().get(SearchingOperator.class).get(userId);
        boolean isTracking = getOperators().get(TrackingOperator.class).get(userId);
        if ((isSearching || isTracking) && !callbackData.equalsIgnoreCase("terminate_operations")) {
            answerCallback(callback, getConfig().read("callback_not_valid"));
            return;
        }
        // Standard Callbacks
        for (Class<?> clazz : ALL_CALLBACK_CLASSES) {
            if (!clazz.isAnnotationPresent(BotCallback.class)) {
                continue;
            }
            BotCallback botCallback = clazz.getAnnotation(BotCallback.class);
            VariableCallback variableCallback = new VariableCallback(userId, bot, callback, botCallback);
            if (variableCallback.check(callbackData)) {
                return;
            }
            // Other callbacks
            if (!callbackData.equalsIgnoreCase(botCallback.value())) {
                continue;
            }
            variableCallback.run(clazz);
        }
    }
}
