package com.nheatyon.searchoffersbot.analyzers.impl;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.analyzers.Analyzer;
import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.closeable.amazon.ProductAPI;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.impl.VariableProduct;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.TrackingOperator;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public final class TrackingAnalyzer implements Analyzer {

    private final BotOperations bot;
    private final Message msg;
    private final String userId;

    private String extractAsin(String url) {
        if (url.contains("amzn.to")) {
            String shortUrl = getShortRedirectUrl(url);
            if (shortUrl.equalsIgnoreCase(url)) {
                return "";
            }
            url = shortUrl;
        }
        int startIndex = url.indexOf("dp/") + 3;
        int endIndex = url.indexOf("/", startIndex);
        if (endIndex == -1) {
            endIndex = url.indexOf("?", startIndex);
        }
        if (endIndex != -1) {
            return url.substring(startIndex, endIndex);
        }
        return "";
    }

    @SneakyThrows
    private String getShortRedirectUrl(String url) {
        return AsyncWrapper.supplyAsync(() -> {
            String redirectUrl = url;
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setInstanceFollowRedirects(false);
                con.connect();
                con.getInputStream();
                if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                    redirectUrl = con.getHeaderField("Location");
                }
            } catch (IOException e) {
                Logger.getRootLogger().error(e);
            }
            return redirectUrl;
        }).get();
    }

    private void disableTracking() {
        bot.getOperators().get(TrackingOperator.class).set(userId, false);
    }

    @Override
    public void analyze() {
        ConfigurationManager<?, String> config = bot.getConfig();
        String asin = extractAsin(msg.getText());
        AtomicBoolean isAlreadyTracked = new AtomicBoolean(false);
        ProductTrackerService.getTrackedProducts().forEach((userId, products) -> {
            isAlreadyTracked.set(products.stream().anyMatch(p -> p.getAsin().equals(asin)));
        });
        if (isAlreadyTracked.get()) {
            bot.sendMessage(userId, config.read("already_tracked"), null);
            disableTracking();
            return;
        }
        long parsedUserId = Long.parseLong(userId);
        int maxTrackable = ((Long) config.read("max_trackable_limit")).intValue();
        List<VariableProduct> registeredProducts = ProductTrackerService.getTrackedProducts().get(parsedUserId);
        InlineKeyboardMarkup homepageButton = bot.getKeyboards().get(KeyboardsType.HOMEPAGE_BUTTON);
        if (registeredProducts != null && registeredProducts.size() >= maxTrackable) {
            bot.sendMessage(userId, config.read("max_trackable"), homepageButton);
            disableTracking();
            return;
        }
        if (asin.isEmpty()) {
            String errorMessage = config.read("invalid_track_url");
            bot.sendMessage(userId, errorMessage, null);
            return;
        }
        ProductAPI api = new ProductAPI(config);
        api.validateAsinAndExecute(bot, userId, asin, () -> {
            api.track(bot, parsedUserId, asin);
            disableTracking();
            bot.sendMessage(userId, config.read("tracked_successfully"), homepageButton);
        });
    }
}
