package com.joeyexecutive.dodgeball.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;

/**
 * Gson config object that is automatically serialized and deserialized
 */
@Getter
public class DodgeballConfig {

    /**
     * List of {@link MapConfig}s for the game to randomly select and use
     */
    private List<MapConfig> mapConfigs = List.of(
            new MapConfig(
                    "dodgeball",
                    "mongodb",
                    "Dodgeball",
                    new Location(Bukkit.getWorld("world"), 0, 0, 0),
                    List.of(new Location(Bukkit.getWorld("world"), 0, 0, 0)),
                    List.of(new Location(Bukkit.getWorld("world"), 0, 0, 0))
            )
    );

}
