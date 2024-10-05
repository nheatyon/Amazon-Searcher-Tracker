package com.nheatyon.searchoffersbot.closeable.amazon.tracker;

public interface ProductObserver {

    void onPriceUpdate(String title, String url, String currency, float price);
}
