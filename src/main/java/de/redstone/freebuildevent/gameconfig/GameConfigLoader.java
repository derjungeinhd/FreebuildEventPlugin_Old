package de.redstone.freebuildevent.gameconfig;

import com.google.gson.Gson;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class GameConfigLoader {
    /**
     * @param configFile Input the name of the config file which should be loaded
     * @param player
     * @return Returns a GameConfig object
     */
    public static GameConfig loadBuildEventConfig(File configFile, Player player) {
        Gson gson = new Gson();

        //Insert default configuration if selected config does not exist
        if (configFile.exists()) {
            try {
                Bukkit.getLogger().info("Loaded new configuration: "+ gson.fromJson(new FileReader(configFile), GameConfig.class));
                GameConfig config = gson.fromJson(new FileReader(configFile), GameConfig.class);
                if (player != null) {
                    PlayerMessenger.sendMessage(player, "Die Konfiguration \""+configFile.getName()+"\" wurde geladen!");
                    PlayerMessenger.sendMessage(player, config.toString());
                }
                return config;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            Bukkit.getLogger().warning(configFile.getName() + " not found...Loading default configuration.");

            if (player != null) {
                PlayerMessenger.sendMessage(player, "Die Konfiguration \""+configFile.getName()+"\" wurde nicht gefunden!");
                PlayerMessenger.sendMessage(player, "Lade default Konfiguration, bitte ändern.");
            }

            try {
                FileWriter writer = new FileWriter(configFile);
                writer.write(DEFAULTCONFIG);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return gson.fromJson(DEFAULTCONFIG, GameConfig.class);
        }
    }

    //region DEFAULTCONFIG
    private static final String DEFAULTCONFIG =
        """
             {
             "title":"default",
             "rounds":[
               {"task":"Baue einen bayrischen Bauer","time":3600},
               {"task":"Baue 2 bayrische Bauern","time":3600}
             ]
           }
        """;

    public static Collection<String> getAllConfigs() {
        Collection<String> allFiles = new ArrayList<>();

        File configFolder = new File("./plugins/FreebuildEvent/GameConfig");
        for (File file : Objects.requireNonNull(configFolder.listFiles())) {
            allFiles.add(file.getName());
        }

        return allFiles;
    }
    //endregion
}