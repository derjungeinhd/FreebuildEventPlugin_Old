package de.redstone.freebuildevent;

import de.redstone.freebuildevent.commands.*;
import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.lib.Nametag;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import de.redstone.freebuildevent.lib.RedPerms;
import de.redstone.freebuildevent.listeners.BuildListener;
import de.redstone.freebuildevent.listeners.CommandPreprocessEventListener;
import de.redstone.freebuildevent.listeners.JoinListener;
import de.redstone.freebuildevent.listeners.VisitorListener;
import net.luckperms.api.LuckPerms;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

public final class Main extends JavaPlugin {
    private final File configFile = new File("./plugins/FreebuildEvent/config.yml");
    private Game game;
    private static Main instance;
    private RedPerms redPerms;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        System.out.println("[Freebuild-Bauevent] Startvorgang");
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            redPerms = new RedPerms(provider.getProvider());
        }

        //Commands
        getCommand("teams").setExecutor(new TeamsCommand());
        getCommand("game").setExecutor(new GameCommand());
        getCommand("whackyteamjoin").setExecutor(new WhackTeamJoinCommand());

        getCommand("teams").setTabCompleter(new TeamsCommandTabCompleter());
        getCommand("game").setTabCompleter(new GameCommandTabCompleter());

        //Register listener
        getServer().getPluginManager().registerEvents(new CommandPreprocessEventListener(), this);
        getServer().getPluginManager().registerEvents(new VisitorListener(), this);
        getServer().getPluginManager().registerEvents(new BuildListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        //After world load
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::onWorldLoaded);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PlayerMessenger.hideStatusBossbar(game.getBossbar());
        //Save teams
        game.teamManager.saveTeams();
        game.saveGameData();
        game.kill();

        for (Player player : Bukkit.getOnlinePlayers()) {
            System.out.println("Removed tag from "+ player.getName());
            new Nametag(player).removeEverything();
        }
    }

    private void onWorldLoaded() {
        game = new Game(configFile);
        Bukkit.getLogger().warning("Game object created");

        BossBar bossBar = Main.getInstance().getGame().getBossbar();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(onlinePlayer);
            game.teamManager.updateNametag(onlinePlayer);
        }
    }

    public Game getGame() {
        return game;
    }

    public static Main getInstance() {
        return instance;
    }

    public RedPerms getRedPerms() {
        return redPerms;
    }
}
