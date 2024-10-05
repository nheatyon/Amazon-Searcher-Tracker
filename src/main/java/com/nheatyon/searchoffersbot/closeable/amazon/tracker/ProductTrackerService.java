package com.nheatyon.searchoffersbot.closeable.amazon.tracker;

import com.amazon.paapi5.v1.ApiException;
import com.amazon.paapi5.v1.GetItemsRequest;
import com.amazon.paapi5.v1.GetItemsResource;
import com.amazon.paapi5.v1.GetItemsResponse;
import com.amazon.paapi5.v1.Item;
import com.amazon.paapi5.v1.OfferListing;
import com.amazon.paapi5.v1.PartnerType;
import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.closeable.CloseableService;
import com.nheatyon.searchoffersbot.closeable.amazon.ProductAPI;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.impl.VariableProduct;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.config.FileCreator;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProductTrackerService extends ProductAPI implements CloseableService, ProductObserver, Runnable {

    private final BotOperations bot;
    private final long userId;
    private final String asin;
    private float previousPrice;
    private final GetItemsRequest getItemsRequest;

    private static final String SERIALIZATION_PATH = "tracked_products.ser";
    @Getter private static Map<Long, List<VariableProduct>> trackedProducts;

    public ProductTrackerService(BotOperations bot, long userId, String asin) {
        super(bot.getConfig());
        this.bot = bot;
        this.userId = userId;
        this.asin = asin;
        this.previousPrice = -1;
        this.getItemsRequest = new GetItemsRequest()
                .itemIds(Collections.singletonList(asin))
                .partnerTag(getPartnerTag())
                .resources(List.of(
                        GetItemsResource.ITEMINFO_TITLE,
                        GetItemsResource.OFFERS_LISTINGS_PRICE
                ))
                .partnerType(PartnerType.ASSOCIATES);
    }

    public static void loadCache(ConfigurationManager<Object, String> serializer) {
        if (trackedProducts == null) {
            FileCreator creator = new FileCreator();
            if (creator.isFileExists(SERIALIZATION_PATH)) {
                trackedProducts = serializer.read(SERIALIZATION_PATH);
                return;
            }
            trackedProducts = new HashMap<>();
        }
    }

    @Override
    public void run() {
        int trackDelay = ((Long) bot.getConfig().read("track_delay_ms")).intValue();
        VariableProduct product = new VariableProduct(asin);
        product.addObserver(this);
        while (!Thread.currentThread().isInterrupted()) {
            GetItemsResponse response = null;
            try {
                response = getApi().getItems(getItemsRequest);
            } catch (ApiException e) {
                Logger.getRootLogger().warn("API exception: ", e);
            }
            if (response == null
                    || response.getItemsResult() == null
                    || response.getItemsResult().getItems() == null
                    || response.getItemsResult().getItems().isEmpty()) {
                continue;
            }
            Item item = response.getItemsResult().getItems().get(0);
            OfferListing listing = item.getOffers().getListings().get(0);
            if (listing.getPrice() == null || listing.getPrice().getDisplayAmount() == null) {
                continue;
            }
            // Valid request
            String displayedTitle = item.getItemInfo().getTitle().getDisplayValue();
            String itemDetailPageURL = item.getDetailPageURL();
            String displayedAmount = listing.getPrice().getDisplayAmount();
            String currency = extractCurrency(displayedAmount);
            float price = extractPrice(displayedAmount);
            product.setTitle(displayedTitle);
            product.setUrl(itemDetailPageURL);
            product.setCurrency(currency);
            product.setPrice(price);
            previousPrice = price;
            List<VariableProduct> actualProducts = trackedProducts.getOrDefault(userId, new ArrayList<>());
            actualProducts.stream()
                    .filter(actualProduct -> actualProduct.getAsin().equals(product.getAsin()))
                    .findFirst()
                    .ifPresentOrElse(
                            actualProduct -> actualProducts.set(actualProducts.indexOf(actualProduct), product),
                            () -> actualProducts.add(product)
                    );
            trackedProducts.put(userId, actualProducts);
            try {
                Thread.sleep(trackDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onPriceUpdate(String title, String url, String currency, float price) {
        Logger.getRootLogger().info(String.format("[Tracker] Price observed for \"%s\" -> %s%s", title, price, currency));
        if (price < previousPrice && previousPrice != -1) {
            String notifyMsg = bot.getConfig().read("price_updated_notify")
                    .toString()
                    .replaceAll("%title%", String.format("<a href=\"%s\">%s</a>", url, title))
                    .replaceAll("%price%", price + currency)
                    .replaceAll("%old_price%", previousPrice + currency);
            bot.sendMessage(String.valueOf(userId), notifyMsg, null);
        }
    }

    @Override
    public void close() {
        if (trackedProducts == null) {
            return;
        }
        bot.getSerializer().write(trackedProducts, SERIALIZATION_PATH);
    }
}
