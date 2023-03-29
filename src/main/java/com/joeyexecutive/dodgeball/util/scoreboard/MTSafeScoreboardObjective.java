package com.joeyexecutive.dodgeball.util.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.Sets;
import com.joeyexecutive.dodgeball.util.Packets;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An MT-Safe implementation of a scoreboard objective.
 */
public class MTSafeScoreboardObjective implements Subscribable, AutoCloseable {

    private static final int MODE_CREATE = 0;
    private static final int MODE_REMOVE = 1;
    private static final int MODE_UPDATE_TITLE = 2;
    private static final List<DisplaySlot> DISPLAY_SLOT_POSITIONS = Arrays.asList(
        DisplaySlot.PLAYER_LIST,
        DisplaySlot.SIDEBAR,
        DisplaySlot.BELOW_NAME
    );

    private final Set<Player> viewers = Sets.newConcurrentHashSet();
    private final Map<String, Integer> scores = new ConcurrentHashMap<>();

    private final String id;
    private Component title;
    private DisplaySlot displaySlot;

    MTSafeScoreboardObjective(String id, Component title, DisplaySlot displaySlot, Collection<Player> initialViewers) {
        this.id = id;
        this.title = title;
        this.displaySlot = displaySlot;
        subscribeAll(initialViewers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeAll(Collection<Player> players) {
        viewers.addAll(players);
        broadcastCreate(players);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeAll(Collection<Player> players) {
        broadcastObjective(MODE_REMOVE, players);
        viewers.removeAll(players);
    }

    /**
     * Get the id of the objective
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the title of the objective
     * @return The title
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Set the title of the objective. If the title is actually different than the current title, an
     * update packet will be sent.
     * @param title The title to set to
     */
    public void setTitle(Component title) {
        final boolean newTitle = !title.equals(this.title);
        this.title = title;
        if (newTitle) {
            broadcastObjective(MODE_UPDATE_TITLE, viewers);
        }
    }

    /**
     * Get the display slot of the objective
     * @return The display slot
     */
    public DisplaySlot getDisplaySlot() {
        return displaySlot;
    }

    /**
     * Set the display slot of the objective. If the slot is different, an update packet will be sent.
     * @param displaySlot The display slot to set to
     */
    public void setDisplaySlot(DisplaySlot displaySlot) {
        final boolean newSlot = displaySlot != this.displaySlot;
        this.displaySlot = displaySlot;
        if (newSlot) {
            broadcastDisplay(viewers);
        }
    }

    /**
     * Get all of the scores of this objective
     * @return A map of score names to their values
     */
    public Map<String, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * Check whether the objective contains the current score
     * @param name The name of the score
     * @return Whether the score exists
     */
    public boolean hasScore(String name) {
        return scores.containsKey(cutName(name));
    }

    /**
     * Get the value of the provided score
     * @param name The name of the score
     * @return The value of the provided score
     */
    public int getScore(String name) {
        return scores.get(cutName(name));
    }

    /**
     * Set the value of a score. If the score is different than what currently exists, an update packet will be
     * sent.
     * @param name The name of the score
     * @param value The value of the score
     */
    public void setScore(String name, int value) {
        name = cutName(name);

        final Integer previous = scores.put(name, value);
        if (previous == null || previous != value) {
            broadcastScore(name, value, EnumWrappers.ScoreboardAction.CHANGE, viewers);
        }
    }

    /**
     * Removes a score from the objective. If the score did exist, an update packet will be sent.
     * @param name The name of the score
     * @return Whether the score existed
     */
    public boolean removeScore(String name) {
        name = cutName(name);
        if (!scores.containsKey(name)) {
            return false;
        }

        broadcastScore(name, scores.remove(name), EnumWrappers.ScoreboardAction.REMOVE, viewers);
        return true;
    }

    /**
     * Clears all of the scores in this objective
     */
    public void clearScores() {
        for (String name : scores.keySet()) {
            removeScore(name);
        }
    }

    /**
     * Get the maximum length of score names
     * @return The maximum length of score names
     */
    public int getMaxNameLength() {
        return 32;
    }

    /**
     * Unsubscribe all of the current viewers from the objective
     */
    @Override
    public void close() {
        unsubscribeAll(viewers);
    }

    private void broadcastCreate(Collection<Player> players) {
        broadcastObjective(MODE_CREATE, players);
        broadcastDisplay(players);
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            broadcastScore(entry.getKey(), entry.getValue(), EnumWrappers.ScoreboardAction.CHANGE, players);
        }
    }

    private void broadcastObjective(int mode, Collection<Player> players) {
        final PacketContainer packet = Packets.create(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        packet.getStrings().write(0, StringUtils.left(id, 16));
        packet.getChatComponents().write(0, Packets.convertComponent(title));
        packet.getEnumModifier(HealthDisplay.class, 2).write(0, HealthDisplay.INTEGER);
        packet.getIntegers().write(0, mode);
        Packets.send(packet, players);
    }

    private void broadcastDisplay(Collection<Player> players) {
        final PacketContainer packet = Packets.create(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        packet.getIntegers().write(0, DISPLAY_SLOT_POSITIONS.indexOf(displaySlot));
        packet.getStrings().write(0, StringUtils.left(id, 16));
        Packets.send(packet, players);
    }

    private void broadcastScore(String name, int value, EnumWrappers.ScoreboardAction action, Collection<Player> players) {
        final PacketContainer packet = Packets.create(PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, StringUtils.left(name, 40));
        packet.getStrings().write(1, StringUtils.left(id, 16));
        packet.getIntegers().write(0, value);
        packet.getScoreboardActions().write(0, action);
        Packets.send(packet, players);
    }

    private String cutName(String name) {
        if (name.length() > getMaxNameLength()) {
            return name.substring(0, getMaxNameLength());
        } else {
            return name;
        }
    }

    private enum HealthDisplay {
        INTEGER,
        HEARTS,
    }
}
