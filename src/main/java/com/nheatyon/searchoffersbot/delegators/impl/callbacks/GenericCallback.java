package com.nheatyon.searchoffersbot.delegators.impl.callbacks;

import com.nheatyon.searchoffersbot.BotOperations;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Getter
@SuperBuilder
public abstract class GenericCallback {

    private final String userId;
    private final CallbackQuery callback;
    private final BotOperations bot;

    public GenericCallback(GenericCallback.GenericCallbackBuilder<?, ?> builder) {
        this.userId = builder.userId;
        this.callback = builder.callback;
        this.bot = builder.bot;
    }

    public abstract void run();
}
