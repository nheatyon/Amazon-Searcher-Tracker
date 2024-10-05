package com.nheatyon.searchoffersbot.closeable.amazon;

import com.amazon.paapi5.v1.ApiClient;
import com.amazon.paapi5.v1.ApiException;
import com.amazon.paapi5.v1.GetItemsRequest;
import com.amazon.paapi5.v1.GetItemsResource;
import com.amazon.paapi5.v1.GetItemsResponse;
import com.amazon.paapi5.v1.ItemInfo;
import com.amazon.paapi5.v1.OfferListing;
import com.amazon.paapi5.v1.OfferPrice;
import com.amazon.paapi5.v1.PartnerType;
import com.amazon.paapi5.v1.SearchItemsResponse;
import com.amazon.paapi5.v1.api.DefaultApi;
import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ProductTrackerService;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.impl.commands.Validator;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAPI {

    @Getter private final DefaultApi api;
    @Getter private final String partnerTag;
    @Getter private final ConfigurationManager<?, String> config;
    @Getter private static Map<String, Thread> trackableThreads;

    public ProductAPI(ConfigurationManager<?, String> config) {
        this.config = config;
        ApiClient client = new ApiClient();
        client.setAccessKey(config.read("amazon_access_key"));
        client.setSecretKey(config.read("amazon_secret_key"));
        client.setHost(config.read("amazon_host"));
        client.setRegion(config.read("amazon_region"));
        this.api = new DefaultApi(client);
        this.partnerTag = config.read("amazon_partner_tag");
        if (ProductAPI.trackableThreads == null) {
            ProductAPI.trackableThreads = new HashMap<>();
        }
    }

    public final String extractCurrency(String text) {
        String pattern = "[$¢£¤¥֏؋৲৳৻૱௹฿៛\\u20a0-\\u20bd\\ua838\\ufdfc\\ufe69\\uff04\\uffe0\\uffe1\\uffe5\\uffe6]";
        Validator validator = new Validator();
        return validator.getMatch(pattern, text, 0);
    }

    public final float extractPrice(String text) {
        String pattern = "\\d+(\\.\\d+)?";
        Validator validator = new Validator();
        String price = validator.getMatch(pattern, text
                .replaceAll("\\.", "")
                .replaceAll(",", "."), 0
        );
        return Float.parseFloat(price);
    }

    public static List<Map<String, Object>> parseSearch(SearchItemsResponse response) {
        List<Map<String, Object>> resItems = new ArrayList<>();
        if (response.getSearchResult() == null || response.getSearchResult().getItems() == null) {
            return resItems;
        }
        response.getSearchResult().getItems().forEach((item) -> {
            Map<String, Object> itemParsed = new HashMap<>();
            if (item != null) {
                ItemInfo itemInfo = item.getItemInfo();
                List<OfferListing> listings = null;
                OfferPrice listingPrice = null;
                // if contains an offer
                if (item.getOffers() != null && item.getOffers().getListings() != null) {
                    listings = item.getOffers().getListings();
                    listingPrice = listings.get(0).getPrice();
                }
                if (item.getOffers() != null && listings != null && listingPrice != null && listingPrice.getSavings() != null) {
                    if (listings.get(0).isIsBuyBoxWinner()) {
                        itemParsed.put("off", true);
                    }
                    float savings = listingPrice.getSavings().getAmount().floatValue();
                    float price = savings + listingPrice.getAmount().floatValue();
                    itemParsed.put("original_price", String.format("%.2f", price));
                    itemParsed.put("savings", savings);
                }
                itemParsed.put("id", item.getASIN() != null ? item.getASIN() : null);
                itemParsed.put("url", item.getDetailPageURL() != null ? item.getDetailPageURL() : null);
                if (itemInfo.getTitle() != null && itemInfo.getTitle().getDisplayValue() != null) {
                    itemParsed.put("title", itemInfo.getTitle().getDisplayValue());
                }
                // Get item's price
                if (item.getOffers() != null && listings != null && listingPrice != null && listingPrice.getDisplayAmount() != null) {
                    itemParsed.put("price", listingPrice.getDisplayAmount());
                }
                // Get item's discount percentage
                if (item.getOffers() != null && listings != null && listingPrice != null && listingPrice.getSavings() != null) {
                    itemParsed.put("discount_percentage", listingPrice.getSavings().getPercentage());
                }
            }
            resItems.add(itemParsed);
        });
        return resItems;
    }

    @SneakyThrows
    public final List<Map<String, Object>> search(String keywords, String searchIndex, int itemCount, int limit) {
        ProductSearcher searcher = new ProductSearcher(config, keywords, searchIndex, itemCount, limit);
        Thread thread = new Thread(searcher);
        thread.start();
        thread.join();
        return searcher.getResponseItems();
    }

    @SneakyThrows
    public final void validateAsinAndExecute(BotOperations bot, String userId, String asin, Runnable v) {
        Thread thread = new Thread(() -> {
            GetItemsRequest getItemsRequest = new GetItemsRequest()
                    .itemIds(Collections.singletonList(asin))
                    .partnerTag(getPartnerTag())
                    .resources(List.of(
                            GetItemsResource.ITEMINFO_TITLE
                    ))
                    .partnerType(PartnerType.ASSOCIATES);
            GetItemsResponse response;
            try {
                response = getApi().getItems(getItemsRequest);
                Thread.sleep(1000);
            } catch (InterruptedException | ApiException e) {
                return;
            }
            if (response.getErrors() != null) {
                bot.sendMessage(userId, config.read("invalid_asin"), null);
                return;
            }
            v.run();
        });
        thread.start();
        thread.join();
    }

    @SneakyThrows
    public final void track(BotOperations bot, long userId, String asin) {
        ProductTrackerService tracker = new ProductTrackerService(bot, userId, asin);
        Thread thread = new Thread(tracker);
        thread.start();
        trackableThreads.put(asin, thread);
    }
}
