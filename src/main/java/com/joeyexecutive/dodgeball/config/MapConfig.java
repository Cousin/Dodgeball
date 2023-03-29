package com.joeyexecutive.dodgeball.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MapConfig {

    /**
     * The name of the world in SWM
     */
    private final String slimeName;

    /**
     * Which slime loader to use (mongodb, mysql etc)
     */
    private final String slimeLoader;

    /**
     * The friendly display name to show players
     */
    private final String displayName;

    /**
     * Spawn point for the waiting lobby
     */
    private final Location lobbySpawn;

    /**
     * Spawn points for team 1
     */
    private final List<Location> team1Spawns;

    /**
     * Spawn points for team 2
     */
    private final List<Location> team2Spawns;

}
