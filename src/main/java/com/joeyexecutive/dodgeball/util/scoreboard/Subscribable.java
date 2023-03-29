package com.joeyexecutive.dodgeball.util.scoreboard;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents an object that is able to have players subscribed and unsubscribed from it
 */
public interface Subscribable {
    /**
     * Subscribes a single player
     * @param player The player to subscribe
     */
    default void subscribe(Player player) {
        subscribeAll(Collections.singleton(player));
    }

    /**
     * Subscribes a group of players to the object
     * @param players The players to subscribe
     */
    void subscribeAll(Collection<Player> players);

    /**
     * Unsubscribes a single player
     * @param player The player to unsubscribe
     */
    default void unsubscribe(Player player) {
        unsubscribeAll(Collections.singleton(player));
    }

    /**
     * Unsubscribes a group of players
     * @param players The players to unsubscribe
     */
    void unsubscribeAll(Collection<Player> players);
}
