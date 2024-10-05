package com.nheatyon.searchoffersbot.closeable.amazon.tracker;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class ObservableProduct {

    private final List<ProductObserver> observers;

    public ObservableProduct() {
        this.observers = new ArrayList<>();
    }

    public final void addObserver(ProductObserver o) {
        observers.add(o);
    }

    public final void removeObserver(ProductObserver o) {
        observers.remove(o);
    }

    public abstract void notifyAllObservers();
}
