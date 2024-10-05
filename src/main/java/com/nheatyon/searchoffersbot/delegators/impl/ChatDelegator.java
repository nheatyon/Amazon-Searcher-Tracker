package com.nheatyon.searchoffersbot.delegators.impl;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.analyzers.Analyzer;
import com.nheatyon.searchoffersbot.analyzers.impl.SearchingAnalyzer;
import com.nheatyon.searchoffersbot.analyzers.impl.TrackingAnalyzer;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.EditIdOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.SearchingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.TrackingOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.Delegator;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author nheatyon
 * @see com.nheatyon.searchoffersbot.delegators.Delegator
 */
public final class ChatDelegator extends BotOperations implements Delegator<Update> {

    public ChatDelegator(ConfigurationManager<?, String> config, ConfigurationManager<Object, String> serializer, OperatorFactory operators, KeyboardsDeclarator keyboards) {
        super(config, serializer, operators, keyboards);
    }

    @Override
    public void delegate(BotOperations bot, Update update) {
        Message msg = update.getMessage();
        String userId = msg.getFrom().getId().toString();
        // If user is searching
        if (getOperators().get(SearchingOperator.class).get(userId)) {
            String searchMsg = getConfig().read("search_msg");
            int loadingMessageId = getOperators().get(EditIdOperator.class).get(userId);
            // Update msg
            Message loadingMsg = bot.editMessage(userId, loadingMessageId, searchMsg, null);
            Analyzer searchingAnalyzer = new SearchingAnalyzer(bot, loadingMsg, userId, msg.getText(), "All");
            searchingAnalyzer.analyze();
            return;
        }
        // If user is tracking
        if (getOperators().get(TrackingOperator.class).get(userId)) {
            Analyzer trackingAnalyzer = new TrackingAnalyzer(bot, msg, userId);
            trackingAnalyzer.analyze();
        }
    }
}
