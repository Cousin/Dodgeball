package com.joeyexecutive.dodgeball.game;

import com.infernalsuite.aswm.api.exceptions.CorruptedWorldException;
import com.infernalsuite.aswm.api.exceptions.NewerFormatException;
import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.exceptions.WorldLockedException;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import com.joeyexecutive.dodgeball.DodgeballPlugin;
import com.joeyexecutive.dodgeball.config.MapConfig;
import com.joeyexecutive.dodgeball.util.MainThreadExecutor;
import org.bukkit.World;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DodgeballGame {

    private final DodgeballPlugin plugin;

    private final MapConfig mapConfig;

    private final String gameId;

    public DodgeballGame(DodgeballPlugin plugin, MapConfig mapConfig) {
        this.plugin = plugin;
        this.mapConfig = mapConfig;

        this.gameId = UUID.randomUUID().toString();
    }

    public CompletableFuture<World> loadMap() {
        return CompletableFuture.supplyAsync(() -> {
            SlimeLoader slimeLoader = plugin.getSlimePlugin().getLoader(mapConfig.getSlimeLoader());
            if (slimeLoader == null) {
                throw new IllegalArgumentException("SlimeLoader " + mapConfig.getSlimeLoader() + " not found!");
            }

            try {
                return plugin.getSlimePlugin().loadWorld(
                        slimeLoader,
                        mapConfig.getSlimeName(),
                        true,
                        new SlimePropertyMap()
                );
            } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException | WorldLockedException e) {
                System.out.println(e.getMessage());
                return null;
            }
        }).thenApplyAsync(slimeWorld -> {
            if (slimeWorld == null) {
                throw new IllegalArgumentException("SlimeWorld " + mapConfig.getSlimeName() + " not found!");
            }

            final String worldName = "db" + gameId;

            slimeWorld.clone(worldName);

            return plugin.getServer().getWorld(worldName);
        }, MainThreadExecutor.MAIN_THREAD_EXECUTOR);
    }

}
