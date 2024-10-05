package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl.pages;

import com.nheatyon.searchoffersbot.analyzers.Analyzer;
import com.nheatyon.searchoffersbot.analyzers.impl.SearchingAnalyzer;
import com.nheatyon.searchoffersbot.closeable.amazon.CategoriesMapper;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map.Entry;

@SuperBuilder
public final class CategoryOffersCallback extends GenericCallback {

    public CategoryOffersCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        Message msg = getCallback().getMessage();
        String searchMsg = getBot().getConfig().read("search_msg");
        Message loadingMsg = getBot().editMessage(getUserId(), msg.getMessageId(), searchMsg, null);
        // Get category name
        String callbackData = getCallback().getData();
        String categoryTranslation = callbackData.replace("get_category_offers_", "");
        String categoryName = null;
        for (Entry<String, String> entry : CategoriesMapper.getInstance().getCategories().entrySet()) {
            if (entry.getValue().equals(categoryTranslation)) {
                categoryName = entry.getKey();
            }
        }
        Analyzer searchingAnalyzer = new SearchingAnalyzer(getBot(), loadingMsg, getUserId(), null, categoryName);
        searchingAnalyzer.analyze();
    }
}
