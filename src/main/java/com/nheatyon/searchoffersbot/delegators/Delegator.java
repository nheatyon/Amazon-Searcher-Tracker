package com.nheatyon.searchoffersbot.delegators;

import com.nheatyon.searchoffersbot.BotOperations;

/**
 * Interface for delegators.
 * @author nheatyon
 * @param <E>
 */
public interface Delegator<E> {

    void delegate(BotOperations bot, E update);
}
