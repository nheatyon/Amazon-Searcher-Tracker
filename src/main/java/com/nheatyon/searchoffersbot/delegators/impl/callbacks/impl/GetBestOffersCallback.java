package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.closeable.amazon.CategoriesMapper;
import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@SuperBuilder
@BotCallback(value = "get_best_offers")
public final class GetBestOffersCallback extends GenericCallback {

    public GetBestOffersCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    @SneakyThrows
    public void run() {
        KeyboardsDeclarator keyboards = getBot().getKeyboards();
        List<String> categories = CategoriesMapper.getInstance().getCategoriesTranslations(getBot().getConfig());
        InlineKeyboardMarkup offersMarkup = keyboards.getPaginationMenu(categories, 1, 2);
        String message = getBot().getConfig().read("best_offers_message");
        getBot().editMessage(getCallback(), message, offersMarkup);
    }
}
