package com.nheatyon.searchoffersbot.analyzers.impl;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.analyzers.Analyzer;
import com.nheatyon.searchoffersbot.closeable.amazon.ProductAPI;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.SearchingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public final class SearchingAnalyzer implements Analyzer {

    private final BotOperations bot;
    private final Message msg;
    private final String userId;
    private final String keywords;
    private final String searchIndex;

    private String assemblyMessage(List<Map<String, Object>> items) {
        ConfigurationManager<?, String> config = bot.getConfig();
        ProductAPI api = new ProductAPI(config);
        StringBuilder builder = new StringBuilder();
        String stateMsg = config.read("search_success");
        if (items.size() == 0) {
            stateMsg = config.read("search_failed");
        }
        builder.append(stateMsg).append("\n\n");
        items.forEach(item -> {
            String displayedPrice = item.get("price").toString();
            String price = String.format("%s [-%s%s] [-%s]",
                    displayedPrice, item.get("savings"),
                    api.extractCurrency(displayedPrice),
                    item.get("discount_percentage") + "%");
            String linkedTitle = String.format("#%s - <a href=\"%s\">%s</a>",
                    items.indexOf(item) + 1,
                    item.get("url"),
                    item.get("title"));
            builder.append(linkedTitle).append("\n");
            builder.append(price).append("\n");
            builder.append("\n");
        });
        String clickToTrack = config.read("click_to_track");
        builder.append(clickToTrack);
        return builder.toString();
    }

    private InlineKeyboardMarkup generateKeyboards(List<Map<String, Object>> items) {
        InlineKeyboardMarkup backButton = bot.getKeyboards().get(KeyboardsType.BACK_BUTTON);
        List<List<InlineKeyboardButton>> buttonsToApply = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int trackableItemsPerRow = ((Long) bot.getConfig().read("trackable_per_row")).intValue();
        int buttonCount = 0;
        for (int i = 0; i < items.size(); i++) {
            String asin = items.get(i).get("id").toString();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("#%s", i + 1));
            button.setCallbackData("add_to_track_" + asin); // TODO risolvere nome callback (tracking)
            row.add(button);
            buttonCount++;
            if (buttonCount == trackableItemsPerRow || i == items.size() - 1) {
                buttonsToApply.add(row);
                row = new ArrayList<>();
                buttonCount = 0;
            }
        }
        assert backButton != null;
        buttonsToApply.add(List.of(backButton.getKeyboard().get(0).get(0)));
        return new InlineKeyboardMarkup(buttonsToApply);
    }

    @Override
    public void analyze() {
        ConfigurationManager<?, String> config = bot.getConfig();
        ProductAPI api = new ProductAPI(config);
        OperatorFactory operators = bot.getOperators();
        int limit = ((Long) config.read("search_limit")).intValue();
        List<Map<String, Object>> items = api.search(keywords, searchIndex, 20, limit);
        // Update message with results
        bot.editMessage(userId, msg.getMessageId(), assemblyMessage(items), generateKeyboards(items));
        operators.get(SearchingOperator.class).set(userId, false);
    }
}
