package com.nheatyon.searchoffersbot.delegators.impl.callbacks;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback.GenericCallbackBuilder;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl.pages.CategoryOffersCallback;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl.tracks.AddToTrackCallback;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.lang.reflect.InvocationTargetException;

@RequiredArgsConstructor
public final class VariableCallback {

    private final String userId;
    private final BotOperations bot;
    private final CallbackQuery callback;
    private final BotCallback annotation;

    public boolean check(String callbackData) {
        callbackData = callbackData.toLowerCase();
        if (callbackData.startsWith("get_category_offers")) {
            return run(CategoryOffersCallback.class);
        }
        if (callbackData.startsWith("add_to_track_")) {
            return run(AddToTrackCallback.class);
        }
        return false;
    }

    public boolean run(Class<?> clazz) {
        GenericCallbackBuilder<?, ?> builder;
        try {
            builder = (GenericCallbackBuilder<?, ?>) clazz
                    .getMethod("builder")
                    .invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return false;
        }
        GenericCallback object = builder
                .userId(userId)
                .callback(callback)
                .bot(bot)
                .build();
        if (annotation.async()) {
            AsyncWrapper.runAsync(object::run);
            return true;
        }
        object.run();
        return true;
    }
}
