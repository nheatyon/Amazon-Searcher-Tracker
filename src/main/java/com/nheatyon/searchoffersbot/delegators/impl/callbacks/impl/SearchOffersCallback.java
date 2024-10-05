package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.EditIdOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.SearchingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@SuperBuilder
@BotCallback(value = "search_offers")
public final class SearchOffersCallback extends GenericCallback {

    public SearchOffersCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        OperatorFactory operators = getBot().getOperators();
        String insertKeywordMsg = getBot().getConfig().read("insert_keyword");
        InlineKeyboardMarkup terminateButton = getBot().getKeyboards().get(KeyboardsType.TERMINATE_BUTTON);
        Message updatedMsg = getBot().editMessage(getCallback(), insertKeywordMsg, terminateButton);
        operators.get(SearchingOperator.class).set(getUserId(), true);
        operators.get(EditIdOperator.class).set(getUserId(), updatedMsg.getMessageId());
    }
}
