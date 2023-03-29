package com.joeyexecutive.dodgeball.util.scoreboard;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An MT-Safe scoreboard that does not flicker for updates, ever. It's per-player, fully supports RGB color
 * codes, custom font symbols, and extremely long line lengths (we're talking so long the lines will extend
 * off of the user's screen). Why you wouldn't use this sidebar scoreboard implementation I do not
 * know. It is literally epic.
 */
public class NoFlickerSideBar implements Closeable, Listener {

    private static final int MAX_SCOREBOARD_LINES = 15;

    private final List<ChatColor> colors = List.of(ChatColor.values());

    @Getter
    private final Map<Player, ScoreboardData> playerToScoreboardData = new ConcurrentHashMap<>();

    @Getter
    private final MTSafeScoreboard scoreboard;

    public NoFlickerSideBar(MTSafeScoreboard scoreboard) {
        this.scoreboard = scoreboard;
        Bukkit.getPluginManager().registerEvents(this, scoreboard.getPlugin());
    }

    /**
     * Set the title of the scoreboard for the specified player
     * @param player The player to set the scoreboard's title for
     * @param title The title of the scoreboard (automatically colorized!)
     */
    public void setTitle(Player player, Component title) {
        getScoreboardData(player).getObjective().setTitle(title);
    }

    /**
     * Sets the lines of the scoreboard for the specified player
     * @param player The player to set the scoreboard lines for
     * @param lines A list of lines that the scoreboard should now display to the user (automatically colorized!)
     */
    public void setLines(Player player, List<Component> lines) {
        final ScoreboardData data = getScoreboardData(player);
        final MTSafeScoreboardObjective objective = data.getObjective();
        final List<LineTeam> lineTeams = data.getLineTeams();
        for (int i = 0; i < Math.min(lines.size(), MAX_SCOREBOARD_LINES); i++) {
            final LineTeam team;
            if (i < lineTeams.size()) {
                team = lineTeams.get(i);
            } else {
                final ChatColor color = colors.get(i);
                if (color == null) {
                    throw new IllegalStateException("Out of chat colors.");
                }

                final MTSafeScoreboardTeam scoreboardTeam = scoreboard.newPlayerTeam(player, "BoardLine:" + i, Component.text(""));
                scoreboardTeam.addEntry(color.toString());
                lineTeams.add(team = new LineTeam(color, scoreboardTeam));
            }

            //objective.setScore(team.getColor().toString(), MAX_SCOREBOARD_LINES - i);
            objective.setScore(team.getColor().toString(), 0);
            team.setPrefix(lines.get(i));
        }

        for (int i = lines.size(); i < lineTeams.size(); i++) {
            objective.removeScore(lineTeams.get(i).getColor().toString());
        }
    }

    /**
     * Remove all scoreboard data
     */
    @Override
    public void close() {
        playerToScoreboardData
            .values()
            .forEach(data -> {
                data.getObjective().close();
                data.getLineTeams().forEach(team -> team.getTeam().close());
            });
        playerToScoreboardData.clear();
        scoreboard.close();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerToScoreboardData.remove(event.getPlayer());
    }

    private ScoreboardData getScoreboardData(Player player) {
        return playerToScoreboardData.computeIfAbsent(
            player,
            key -> new ScoreboardData(scoreboard.newPlayerObjective(player, Component.text("-"), DisplaySlot.SIDEBAR))
        );
    }

    public static class ScoreboardData {

        private final List<LineTeam> lineTeams = new ArrayList<>();

        private final MTSafeScoreboardObjective objective;

        public ScoreboardData(MTSafeScoreboardObjective objective) {
            this.objective = objective;
        }

        public List<LineTeam> getLineTeams() {
            return lineTeams;
        }

        public MTSafeScoreboardObjective getObjective() {
            return objective;
        }
    }

    private static class LineTeam {

        private final ChatColor color;
        private final MTSafeScoreboardTeam team;

        public LineTeam(ChatColor color, MTSafeScoreboardTeam team) {
            this.color = color;
            this.team = team;
        }

        public String getId() {
            return team.getId();
        }

        public void setPrefix(Component prefix) {
            team.setPrefix(prefix);
        }

        public void setSuffix(Component suffix) {
            team.setSuffix(suffix);
        }

        public ChatColor getColor() {
            return color;
        }

        public MTSafeScoreboardTeam getTeam() {
            return team;
        }
    }
}
