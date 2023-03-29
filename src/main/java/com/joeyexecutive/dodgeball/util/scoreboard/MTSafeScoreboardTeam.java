package com.joeyexecutive.dodgeball.util.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.Sets;
import com.joeyexecutive.dodgeball.util.ChatUtils;
import com.joeyexecutive.dodgeball.util.Packets;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * An MT-Safe implementation of scoreboard teams
 */
public class MTSafeScoreboardTeam implements AutoCloseable, Subscribable {

    private static final int MODE_CREATE = 0;
    private static final int MODE_REMOVE = 1;
    private static final int MODE_UPDATE_INFO = 2;
    private static final int MODE_ADD_PLAYERS = 3;
    private static final int MODE_REMOVE_PLAYERS = 4;

    private final Set<Player> viewers = Sets.newConcurrentHashSet();
    private final Set<Player> playerMembers = Sets.newConcurrentHashSet();
    private final Set<String> entries = Sets.newConcurrentHashSet();

    private final String id;
    private Component title;
    private Component prefix = Component.text("");
    private Component suffix = Component.text("");
    private boolean friendlyFire = true;
    private boolean seeFriendlyInvisibles = true;
    private RuleValue nameTagVisibility = RuleValue.ALWAYS;
    private RuleValue collision = RuleValue.NEVER;
    private ChatColor color = ChatColor.RESET;

    MTSafeScoreboardTeam(String id, Component title, Collection<Player> initialViewers) {
        this.id = id;
        this.title = title;
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
        broadcast(MODE_REMOVE, Collections.emptySet(), players);
        viewers.removeAll(players);
    }

    /**
     * Get the id of the team
     * @return The id of the team
     */
    public String getId() {
        return id;
    }

    /**
     * Get the title of the team
     * @return The title of the team
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Set the title of the team. If the title is different, an update packet will be sent.
     * @param title The title to set to
     */
    public void setTitle(Component title) {
        if (!title.equals(this.title)) {
            this.title = title;
            broadcastInfoChange();
        }
    }

    /**
     * Get the prefix of the team
     * @return The prefix of the team
     */
    public Component getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix of the team. If the prefix is different, an update packet will be sent.
     * @param prefix The prefix to set to
     */
    public void setPrefix(Component prefix) {
        if (!prefix.equals(this.prefix)) {
            this.prefix = prefix;
            broadcastInfoChange();
        }
    }

    /**
     * Get the suffix of the team
     * @return The suffix of the team
     */
    public Component getSuffix() {
        return suffix;
    }

    /**
     * Set the suffix of the team. If the suffix is different, an update packet will be sent.
     * @param suffix The suffix to set to
     */
    public void setSuffix(Component suffix) {
        if (!suffix.equals(this.suffix)) {
            this.suffix = suffix;
            broadcastInfoChange();
        }
    }

    /**
     * Get whether friendly fire is enabled
     * @return Whether friendly fire is enabled
     */
    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    /**
     * Set whether friendly fire is enabled. If the state changes, an update packet will be sent.
     * @param friendlyFire Whether friendly fire is enabled
     */
    public void setFriendlyFire(boolean friendlyFire) {
        if (friendlyFire != this.friendlyFire) {
            this.friendlyFire = friendlyFire;
            broadcastInfoChange();
        }
    }

    /**
     * Get whether members of the team can see friendly invisibles
     * @return Whether members of the team can see friendly invisibles
     */
    public boolean isFriendlyInvisibles() {
        return seeFriendlyInvisibles;
    }

    /**
     * Set whether members of the team can see friendly invisibles. If the state changes, an update packet will be sent.
     * @param seeFriendlyInvisibles Whether members of the team can see friendly invisibles
     */
    public void setFriendlyInvisibles(boolean seeFriendlyInvisibles) {
        if (seeFriendlyInvisibles != this.seeFriendlyInvisibles) {
            this.seeFriendlyInvisibles = seeFriendlyInvisibles;
            broadcastInfoChange();
        }
    }

    /**
     * Get the name tag visibility
     * @return The name tag visibility
     */
    public RuleValue getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Set the name tag visibility. If the value changes, an update packet will be sent.
     * @param nameTagVisibility The name tag visibility
     */
    public void setNameTagVisibility(RuleValue nameTagVisibility) {
        if (nameTagVisibility != this.nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            broadcastInfoChange();
        }
    }

    /**
     * Get the collision rule
     * @return The collision rule value
     */
    public RuleValue getCollision() {
        return collision;
    }

    /**
     * Set the collision rule. If the value changes, an update packet will be sent.
     * @param collision The collision rule
     */
    public void setCollision(RuleValue collision) {
        if (collision != this.collision) {
            this.collision = collision;
            broadcastInfoChange();
        }
    }

    /**
     * Add a player to this team. If the player is not already on the team, an adding packet will be sent.
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        if (addEntry(player.getName())) {
            playerMembers.add(player);
        }
    }

    /**
     * Removes a player from this team. If the player is on the team, a removing packet will be sent.
     * @param player The player to remove
     * @return Whether the player was removed from the team or not
     */
    public boolean removePlayer(Player player) {
        playerMembers.remove(player);
        return removeEntry(player.getName());
    }

    /**
     * Check whether the provided player is a member of the team
     * @param player The player to check
     * @return Whether the player is a member of the team
     */
    public boolean hasPlayer(Player player) {
        return playerMembers.contains(player);
    }

    /**
     * Get all of the players on the team
     * @return All of the players on the team
     */
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(playerMembers);
    }

    /**
     * Adds an entry to the team. If the entry is not already on the team, an update packet is broadcasted.
     * @param entryName The entry name
     * @return Whether the entry was new
     */
    public boolean addEntry(String entryName) {
        if (entries.add(entryName)) {
            broadcast(MODE_ADD_PLAYERS, Collections.singleton(entryName), viewers);
            return true;
        }
        return false;
    }

    /**
     * Removes an entry from the team. If the entry was on the team, an update packet is broadcasted
     * @param entryName The entry name
     * @return Whether the entry was on the team
     */
    public boolean removeEntry(String entryName) {
        if (entries.remove(entryName)) {
            broadcast(MODE_REMOVE_PLAYERS, Collections.singleton(entryName), viewers);
            return true;
        }
        return false;
    }

    /**
     * Check whether the team has an entry
     * @param entryName The entry name
     * @return Whether the entry is in this team
     */
    public boolean hasEntry(String entryName) {
        return entries.contains(entryName);
    }

    /**
     * Get all of the entries of this team, including player names from the player methods
     * @return A set of string entries of this team, including player names
     */
    public Set<String> getEntries() {
        return Collections.unmodifiableSet(entries);
    }

    /**
     * Set the color of the team
     * @param color The color of the team
     */
    public void setColor(ChatColor color) {
        if (color != this.color) {
            this.color = color;
            broadcastInfoChange();
        }
    }

    /**
     * Get the color of the team
     * @return The color of the team
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * Unsubscribe all of the current viewers from the team, clear the members, clear the entries
     */
    @Override
    public void close() {
        unsubscribeAll(viewers);
        playerMembers.clear();
        entries.clear();
    }

    private void broadcastCreate(Collection<Player> players) {
        broadcast(MODE_CREATE, entries, players);
    }

    private void broadcastInfoChange() {
        broadcast(MODE_UPDATE_INFO, Collections.emptySet(), viewers);
    }

    private void broadcast(int mode, Set<String> entriesModified, Collection<Player> sendTo) {
        final String teamName = ChatUtils.maxLengthStr(id, 16);

        ScoreboardTeam scoreboardTeam = new ScoreboardTeam(new Scoreboard(), teamName);

        scoreboardTeam.a(ChatUtils.adventureComponentToIChatBase(title));
        scoreboardTeam.b(ChatUtils.adventureComponentToIChatBase(prefix));
        scoreboardTeam.c(ChatUtils.adventureComponentToIChatBase(suffix));

        scoreboardTeam.a(ScoreboardTeamBase.EnumNameTagVisibility.a(nameTagVisibility.getName()));
        scoreboardTeam.a(ScoreboardTeamBase.EnumTeamPush.a(collision.getName()));
        scoreboardTeam.a(EnumChatFormat.a(color.getChar()));
        scoreboardTeam.g().clear();
        scoreboardTeam.g().addAll(entriesModified);
        scoreboardTeam.b(seeFriendlyInvisibles);
        scoreboardTeam.a(friendlyFire);

        final PacketContainer packet = Packets.create(PacketType.Play.Server.SCOREBOARD_TEAM);

        packet.getSpecificModifier(Optional.class).write(0, Optional.of(new PacketPlayOutScoreboardTeam.b(scoreboardTeam)));
        packet.getSpecificModifier(Collection.class).write(0, new ArrayList<>(entriesModified));

        packet.getIntegers().write(0, mode);
        packet.getStrings().write(0, teamName);

        Packets.send(packet, sendTo);
    }

    private int packOptionData() {
        int result = 0;
        if (friendlyFire) {
            result |= 1;
        }
        if (seeFriendlyInvisibles) {
            result |= 2;
        }
        return result;
    }

    public enum RuleValue {
        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private final String name;

        RuleValue(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
