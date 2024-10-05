package com.nheatyon.searchoffersbot.closeable.amazon.tracker.impl;

import com.nheatyon.searchoffersbot.closeable.amazon.tracker.ObservableProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
public final class VariableProduct extends ObservableProduct implements Serializable {

    private final String asin;
    @Setter private String title;
    @Setter private String url;
    @Setter private String currency;
    private float price;

    public VariableProduct(String asin) {
        this.asin = asin;
        this.price = -1;
    }

    public void setPrice(float price) {
        this.price = price;
        notifyAllObservers();
    }

    @Override
    public void notifyAllObservers() {
        getObservers().forEach(o -> o.onPriceUpdate(this.title, this.url, this.currency, this.price));
    }
}
