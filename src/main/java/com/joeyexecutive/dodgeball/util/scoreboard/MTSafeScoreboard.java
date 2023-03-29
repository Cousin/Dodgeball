package com.joeyexecutive.dodgeball.util.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A MT-Safe scoreboard implementation. All methods are concurrent safe and do not use the Bukkit API
 * at all - everything is sent using raw packets.
 */
public class MTSafeScoreboard implements Closeable {

    /**
     * Map of objective id -> objective that every player should have sent to them
     */
    private final Map<String, MTSafeScoreboardObjective> globalObjectives = new ConcurrentHashMap<>();
    /**
     * Map of team id -> team that every player should have sent to them
     */
    private final Map<String, MTSafeScoreboardTeam> globalTeams = new ConcurrentHashMap<>();
    /**
     * Map of objective id -> objective that only the player should see
     */
    private final Map<Player, Map<String, MTSafeScoreboardObjective>> playerObjectives = new ConcurrentHashMap<>();
    /**
     * Map of team id -> team that only the player should see
     */
    private final Map<Player, Map<String, MTSafeScoreboardTeam>> playerTeams = new ConcurrentHashMap<>();

    /**
     * The plugin instance
     */
    private final JavaPlugin plugin;

    private Listeners listeners;

    /**
     * Initialises the listeners for the class. Note: this is the only part of the class that is NOT
     * MT-safe.
     * @param plugin The plugin instance
     */
    public MTSafeScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(listeners = new Listeners(), plugin);
    }

    /**
     * Get the plugin the scoreboard is registered under
     * @return The {@link JavaPlugin}
     */
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Creates a new team that every player on the server should have sent to them. This method
     * will automatically subscribe all online players to the created team and generates a unique id
     * from the system time.
     * @param title The title or "display name" of the team (NOT the prefix)
     * @return A new {@link MTSafeScoreboardTeam} instance, or the instance that already
     * exists with the provided id
     */
    public MTSafeScoreboardTeam newTeam(Component title) {
        return newTeam(Long.toHexString(System.nanoTime()), title);
    }

    /**
     * Creates a new team that every player on the server should have sent to them. This method
     * will automatically subscribe all online players to the created team.
     * @param id The id of the team (unique)
     * @param title The title or "display name" of the team (NOT the prefix)
     * @return A new {@link MTSafeScoreboardTeam} instance, or the instance that already
     * exists with the provided id
     */
    public MTSafeScoreboardTeam newTeam(String id, Component title) {
        return newTeam(globalTeams, id, title, (Collection<Player>) Bukkit.getOnlinePlayers());
    }

    /**
     * Gets a global team by its id
     * @param id The id of the team
     * @return The {@link MTSafeScoreboardTeam} instance, or {@code null} if the team does not exist
     */
    public MTSafeScoreboardTeam getTeam(String id) {
        return globalTeams.get(id);
    }

    /**
     * Removes a global team. In the process, the team is removed from all of the subscribed clients.
     * @param id The id of the team
     * @return Whether the team has been removed successfully
     */
    public boolean removeTeam(String id) {
        return remove(globalTeams, id);
    }

    /**
     * Creates a new team that only the provided player should see. This method will automatically
     * subscribe the player to the created team.
     * @param player The player the team is to be displayed to
     * @param id The id of the team (unique per player)
     * @param title The title or "display name" of the team (NOT the prefix)
     * @return
     */
    public MTSafeScoreboardTeam newPlayerTeam(Player player, String id, Component title) {
        return newTeam(getPlayerTeams(player), id, title, Collections.singleton(player));
    }

    /**
     * Gets a player team by its id
     * @param player The player the team is registered under
     * @param id The id of the team
     * @return The {@link MTSafeScoreboardTeam} instance, or {@code null} if the team does not exist
     */
    public MTSafeScoreboardTeam getPlayerTeam(Player player, String id) {
        return getPlayerTeams(player).get(id);
    }

    /**
     * Removes a player team. This method will automatically unsubscribe the player from the team.
     * @param player The player the team is registered under
     * @param id The id of the team
     * @return Whether the team was removed successfully
     */
    public boolean removePlayerTeam(Player player, String id) {
        return remove(getPlayerTeams(player), id);
    }

    /**
     * Creates a new objective that every player should see. This method automatically subscribes all
     * of the online players to the created objective and generates a unique id from the system time.
     * @param title The title of the objective
     * @param displaySlot The display slot that the objective should be in
     * @return The created {@link MTSafeScoreboardObjective} instance that was created, or the already existing
     * one under the id
     */
    public MTSafeScoreboardObjective newObjective(Component title, DisplaySlot displaySlot) {
        return newObjective(Long.toHexString(System.nanoTime()), title, displaySlot);
    }

    /**
     * Creates a new objective that every player should see. This method automatically subscribes all
     * of the online players to the created objective.
     * @param id The id of the objective (unique)
     * @param title The title of the objective
     * @param displaySlot The display slot that the objective should be in
     * @return The created {@link MTSafeScoreboardObjective} instance that was created, or the already existing
     * one under the id
     */
    public MTSafeScoreboardObjective newObjective(String id, Component title, DisplaySlot displaySlot) {
        return newObjective(globalObjectives, id, title, displaySlot, (Collection<Player>) Bukkit.getOnlinePlayers());
    }

    /**
     * Gets a global objective by its id
     * @param id The id of the objective
     * @return The {@link MTSafeScoreboardObjective} instance, or {@code null} if the objective does not exist
     */
    public MTSafeScoreboardObjective getObjective(String id) {
        return globalObjectives.get(id);
    }

    /**
     * Removes a global objective. This method will automatically unsubscribe all players from the objective.
     * @param id The id of the objective
     * @return Whether the objective was removed
     */
    public boolean removeObjective(String id) {
        return remove(globalObjectives, id);
    }

    /**
     * Creates a new objective that only the provided player should see. This method will automatically
     * subscribe the player to the objective that is created and generates a unique id from the system time.
     * @param player The player that the objective is to be displayed to
     * @param title The title of the objective
     * @param displaySlot The display slot that the objective should be in
     * @return The created {@link MTSafeScoreboardTeam} instance
     */
    public MTSafeScoreboardObjective newPlayerObjective(Player player, Component title, DisplaySlot displaySlot) {
        return newPlayerObjective(player, Long.toHexString(System.nanoTime()), title, displaySlot);
    }

    /**
     * Creates a new objective that only the provided player should see. This method will automatically
     * subscribe the player to the objective that is created.
     * @param player The player that the objective is to be displayed to
     * @param id The id of the team (unique per player)
     * @param title The title of the objective
     * @param displaySlot The display slot that the objective should be in
     * @return The created {@link MTSafeScoreboardTeam} instance
     */
    public MTSafeScoreboardObjective newPlayerObjective(Player player, String id, Component title, DisplaySlot displaySlot) {
        return newObjective(getPlayerObjectives(player), id, title, displaySlot, Collections.singleton(player));
    }

    /**
     * Gets a player objective by its id
     * @param player The player that the objective is registered under
     * @param id The id of the objective
     * @return The {@link MTSafeScoreboardObjective} instance, or {@code null} if the objective does not exist
     */
    public MTSafeScoreboardObjective getPlayerObjective(Player player, String id) {
        return getPlayerObjectives(player).get(id);
    }

    /**
     * Removes a player objective. This method will automatically unsubscribe the player from the objective.
     * @param player The player that the objective is registered under
     * @param id The id of the objective
     * @return Whether the objective was removed
     */
    public boolean removePlayerObjective(Player player, String id) {
        return remove(getPlayerObjectives(player), id);
    }

    /**
     * Removes the provided id from the map and calls {@link AutoCloseable#close()} if
     * it exists.
     * @param map The map to get by id
     * @param id The id to remove
     * @return Whether the provided id existed and was removed successfully
     */
    private <T extends AutoCloseable> boolean remove(Map<String, T> map, String id) {
        final AutoCloseable closeable = map.get(id);
        if (closeable == null) {
            return false;
        }

        try {
            closeable.close();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a new team in the provided map if it doesn't already exist
     * @param teams The map
     * @param id The id to pass to the constructor
     * @param title The title to pass to the constructor
     * @param viewers The viewers to pass to the constructor
     * @return The {@link MTSafeScoreboardTeam} instance
     */
    private MTSafeScoreboardTeam newTeam(Map<String, MTSafeScoreboardTeam> teams, String id, Component title, Collection<Player> viewers) {
        if (!teams.containsKey(id)) {
            teams.put(id, new MTSafeScoreboardTeam(id, title, viewers));
        }

        return teams.get(id);
    }

    /**
     * Gets all of the player's teams
     * @param player The player
     * @return The player's teams
     */
    public Map<String, MTSafeScoreboardTeam> getPlayerTeams(Player player) {
        if (!playerTeams.containsKey(player)) {
            playerTeams.put(player, new HashMap<>());
        }

        return playerTeams.get(player);
    }

    /**
     * Creates a new objective in the provided map if it doesn't already exist
     * @param objectives The map to insert into
     * @param id The id to pass to the constructor
     * @param title The title to pass to the constructor
     * @param displaySlot The display slot to pass to the constructor
     * @param viewers The viewers to pass to the constructor
     * @return The {@link MTSafeScoreboardObjective} instance
     */
    private MTSafeScoreboardObjective newObjective(
        Map<String, MTSafeScoreboardObjective> objectives,
        String id,
        Component title,
        DisplaySlot displaySlot,
        Collection<Player> viewers
    ) {
        if (!objectives.containsKey(id)) {
            objectives.put(id, new MTSafeScoreboardObjective(id, title, displaySlot, viewers));
        }

        return objectives.get(id);
    }

    /**
     * Get all of the player's objectives
     * @param player The player
     * @return The player's objectives
     */
    public Map<String, MTSafeScoreboardObjective> getPlayerObjectives(Player player) {
        if (!playerObjectives.containsKey(player)) {
            playerObjectives.put(player, new HashMap<>());
        }

        return playerObjectives.get(player);
    }

    /**
     * Remove all per-player and global objectives and teams
     */
    @Override
    public void close() {
        playerTeams.values().forEach(this::closeAll);
        playerObjectives.values().forEach(this::closeAll);
        playerTeams.clear();
        playerObjectives.clear();
        closeAll(globalObjectives);
        closeAll(globalTeams);
        globalObjectives.clear();
        globalTeams.clear();
        HandlerList.unregisterAll(listeners);
    }

    /**
     * Close all values of the map
     * @param map The map of members to close the values of
     */
    private <T extends AutoCloseable> void closeAll(Map<String, T> map) {
        if (map == null) {
            return;
        }

        for (AutoCloseable closeable : map.values()) {
            try {
                closeable.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Contains all of the listeners for the class
     */
    private class Listeners implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            final Player player = event.getPlayer();
            globalObjectives.values().forEach(objective -> objective.subscribe(player));
            globalTeams.values().forEach(team -> team.subscribe(player));
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            final Player player = event.getPlayer();
            globalObjectives.values().forEach(objective -> objective.unsubscribe(player));
            globalTeams
                .values()
                .forEach(team -> {
                    team.removePlayer(player);
                    team.unsubscribe(player);
                });
            closeAll(playerObjectives.remove(player));
            closeAll(playerTeams.remove(player));
        }
    }
}
