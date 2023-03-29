package com.joeyexecutive.dodgeball;

import com.infernalsuite.aswm.api.SlimePlugin;
import com.joeyexecutive.dodgeball.config.DodgeballConfig;
import com.joeyexecutive.dodgeball.util.BukkitTasks;
import com.joeyexecutive.dodgeball.util.GsonHelper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;

@Getter
public class DodgeballPlugin extends JavaPlugin {

    private DodgeballConfig dodgeballConfig;

    private SlimePlugin slimePlugin;

    @Override
    public void onEnable() {
        slimePlugin = (SlimePlugin) getServer().getPluginManager().getPlugin("SlimeWorldManager");

        if (slimePlugin == null) {
            getLogger().severe("SlimeWorldManager is not installed on this server!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitTasks.init(this);

        reloadDodgeballConfig();
    }

    /**
     * Reloads our config.json file into memory
     */
    @SneakyThrows
    public void reloadDodgeballConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.json");

        if (!configFile.exists()) {
            configFile.createNewFile();
            dodgeballConfig = new DodgeballConfig();
        } else {
            dodgeballConfig = GsonHelper.PRETTY_GSON.fromJson(Files.readString(configFile.toPath()), DodgeballConfig.class);
        }

        // re-save config in case the structure has changed, this will auto re-format
        Files.writeString(configFile.toPath(), GsonHelper.PRETTY_GSON.toJson(dodgeballConfig));
    }

}
