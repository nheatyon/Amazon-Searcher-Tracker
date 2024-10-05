package com.nheatyon.searchoffersbot.delegators;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.async.AsyncWrapper;

/**
 * Execute a delegator via Strategy Pattern.
 * @author nheatyon
 */
public final class DelegatorProcessor {

    public <E> void execute(BotOperations bot, Delegator<E> delegator, E update) {
        AsyncWrapper.runAsync(() -> delegator.delegate(bot, update));
    }
}
