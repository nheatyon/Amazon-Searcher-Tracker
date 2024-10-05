package com.nheatyon.searchoffersbot;

import com.nheatyon.searchoffersbot.closeable.CloseableService;
import com.nheatyon.searchoffersbot.closeable.amazon.ProductAPI;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.closeable.database.DatabaseService;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.factory.OperatorFactory;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.config.impl.JsonManager;
import com.nheatyon.searchoffersbot.config.impl.SerializationManager;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsDeclarator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InitializerClass {

    private void setupLogs() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    @SneakyThrows
    private void setupBot(BotOperations bot) {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(new RSearchOffersBot(bot.getConfig(), bot.getSerializer(), bot.getOperators(), bot.getKeyboards()));
        Logger.getRootLogger().log(Level.INFO, "Bot started!");
    }

    private void setupBootHooks(BotOperations bot) {
        ProductTrackerService.loadCache(bot.getSerializer());
        ProductTrackerService.getTrackedProducts().forEach((userId, products) -> {
            products.forEach(p -> {
                String asin = p.getAsin();
                ProductAPI api = new ProductAPI(bot.getConfig());
                api.track(bot, userId, asin);
            });
        });
    }

    private void setupShutdownHooks(BotOperations bot) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CloseableService database = new DatabaseService();
            CloseableService tracker = new ProductTrackerService(bot, 0, null);
            database.close();
            tracker.close();
        }));
    }

    public static void main(String[] args) {
        InitializerClass instance = new InitializerClass();
        instance.setupLogs();
        ConfigurationManager<?, String> config = JsonManager.getInstance();
        ConfigurationManager<Object, String> serializer = SerializationManager.getInstance();
        OperatorFactory operators = OperatorFactory.getInstance(config);
        KeyboardsDeclarator keyboards = new KeyboardsDeclarator(config);
        BotOperations bot = new BotOperations(config, serializer, operators, keyboards);
        // Starting the bot
        instance.setupBot(bot);
        instance.setupBootHooks(bot);
        instance.setupShutdownHooks(bot);
    }
}
