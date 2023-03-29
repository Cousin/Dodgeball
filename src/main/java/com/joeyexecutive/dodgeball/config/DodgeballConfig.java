package com.joeyexecutive.dodgeball.config;

import lombok.Getter;
import org.bukkit.Location;

import java.util.List;

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
                    new Location(null, 0, 0, 0),
                    List.of(new Location(null, 0, 0, 0)),
                    List.of(new Location(null, 0, 0, 0))
            )
    );

}
