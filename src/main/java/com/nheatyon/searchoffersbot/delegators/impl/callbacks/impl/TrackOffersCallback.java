package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.TrackingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@SuperBuilder
@BotCallback(value = "track_offers")
public final class TrackOffersCallback extends GenericCallback {

    public TrackOffersCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        OperatorFactory operators = getBot().getOperators();
        String trackOfferMsg = getBot().getConfig().read("send_link_to_track");
        InlineKeyboardMarkup terminateButton = getBot().getKeyboards().get(KeyboardsType.TERMINATE_BUTTON);
        getBot().editMessage(getCallback(), trackOfferMsg, terminateButton);
        operators.get(TrackingOperator.class).set(getUserId(), true);
    }
}
