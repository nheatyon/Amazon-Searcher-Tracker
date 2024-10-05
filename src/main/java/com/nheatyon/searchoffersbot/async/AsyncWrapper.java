package com.nheatyon.searchoffersbot.async;

import lombok.experimental.UtilityClass;
import org.apache.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@UtilityClass
public final class AsyncWrapper {

    private <T> T log(Throwable e, T executor) {
        Logger.getRootLogger().error(e);
        return executor;
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier).exceptionally((e) -> AsyncWrapper.log(e, supplier).get());
    }

    public void runAsync(Runnable v) {
        CompletableFuture.runAsync(v).exceptionally((e) -> AsyncWrapper.log(e, null));
    }
}
