package com.nheatyon.searchoffersbot.closeable.amazon;

import com.amazon.paapi5.v1.PartnerType;
import com.amazon.paapi5.v1.SearchItemsRequest;
import com.amazon.paapi5.v1.SearchItemsResource;
import com.amazon.paapi5.v1.SearchItemsResponse;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProductSearcher extends ProductAPI implements Runnable {

    private final String keywords;
    private final String searchIndex;
    private final int maxItemPage;
    private final int itemCount;
    private final int limit;
    @Getter private List<Map<String, Object>> responseItems;
    private final List<SearchItemsResource> searchItemsResources;

    public ProductSearcher(ConfigurationManager<?, String> config, String keywords, String searchIndex, int itemCount, int limit) {
        super(config);
        this.keywords = keywords;
        this.searchIndex = searchIndex;
        this.maxItemPage = 10;
        this.itemCount = itemCount;
        this.limit = limit;
        this.responseItems = new ArrayList<>();
        this.searchItemsResources = List.of(
                SearchItemsResource.ITEMINFO_TITLE,
                SearchItemsResource.OFFERS_LISTINGS_PRICE,
                SearchItemsResource.IMAGES_PRIMARY_LARGE,
                SearchItemsResource.OFFERS_LISTINGS_SAVINGBASIS,
                SearchItemsResource.ITEMINFO_FEATURES,
                SearchItemsResource.OFFERS_LISTINGS_PROMOTIONS,
                SearchItemsResource.OFFERS_LISTINGS_CONDITION,
                SearchItemsResource.OFFERS_LISTINGS_ISBUYBOXWINNER
        );
    }

    private void sortByDiscountPercentage() {
        responseItems.sort((actualItem, nextItem) -> {
            int actualPercentage = (int) actualItem.get("discount_percentage");
            int nextPercentage = (int) nextItem.get("discount_percentage");
            return Integer.compare(nextPercentage, actualPercentage);
        });
    }

    @Override
    @SneakyThrows
    public void run() {
        int searchDelay = ((Long) getConfig().read("search_delay_ms")).intValue();
        for (int i = 1; i <= maxItemPage; i++) {
            SearchItemsRequest searchItemsRequest = new SearchItemsRequest()
                    .partnerTag(getPartnerTag())
                    .partnerType(PartnerType.ASSOCIATES)
                    .keywords(keywords)
                    .searchIndex(searchIndex)
                    .resources(searchItemsResources)
                    .itemPage(i)
                    .itemCount(itemCount);
            // Start searching for items
            SearchItemsResponse response = getApi().searchItems(searchItemsRequest);
            ProductAPI.parseSearch(response).forEach((item) -> {
                if (responseItems.size() < limit) {
                    if (item.get("savings") == null) {
                        return;
                    }
                    responseItems.add(item);
                }
            });
            Thread.sleep(searchDelay);
        }
        sortByDiscountPercentage();
    }
}
