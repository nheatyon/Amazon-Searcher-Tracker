package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@BotCallback(value = "empty")
public final class EmptyCallback extends GenericCallback {

    public EmptyCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        getBot().editWelcome(getCallback());
    }
}
