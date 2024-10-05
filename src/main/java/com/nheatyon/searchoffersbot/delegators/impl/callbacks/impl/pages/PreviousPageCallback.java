package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl.pages;

import com.nheatyon.searchoffersbot.closeable.amazon.CategoriesMapper;
import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@SuperBuilder
@BotCallback(value = "previous_page")
public final class PreviousPageCallback extends GenericCallback {

    public PreviousPageCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        KeyboardsDeclarator keyboards = getBot().getKeyboards();
        int previousPage = keyboards.getPage(getCallback(), false);
        // Update page
        List<String> categories = CategoriesMapper.getInstance().getCategoriesTranslations(getBot().getConfig());
        InlineKeyboardMarkup offersMarkup = keyboards.getPaginationMenu(categories, previousPage, 2);
        String message = getBot().getConfig().read("best_offers_message");
        getBot().editMessage(getCallback(), message, offersMarkup);
    }
}
