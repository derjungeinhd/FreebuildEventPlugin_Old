package de.redstone.freebuildevent.commands;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.gameconfig.GameConfig;
import de.redstone.freebuildevent.gameconfig.GameConfigLoader;
import de.redstone.freebuildevent.lib.RedPerms;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameCommandTabCompleter implements TabCompleter {
    private Main plugin;
    private RedPerms redPerms;
    public GameCommandTabCompleter() {
        this.plugin = Main.getInstance();
        this.redPerms = plugin.getRedPerms();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        Player player = (Player) sender;

        if (args.length == 1 &&
                (redPerms.getPlayerAllowedSilent(player, "game.admin.setArea") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.start") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.pause") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.resume") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.stop") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.skip") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.continue") ||
                 redPerms.getPlayerAllowedSilent(player, "game.admin.kill")))
        {
            // The first argument, which is "setArea"
            completions.add("setArea");
            completions.add("start");
            completions.add("kill");
            completions.add("pause");
            completions.add("resume");
            completions.add("stop");
            completions.add("config");
            completions.add("skip");
            completions.add("continue");


        } else if ((args.length == 2 || args.length == 4) && args[0].equalsIgnoreCase("setArea") && redPerms.getPlayerAllowedSilent(player, "game.admin.setArea")) {
            // The second argument, which is the current X coordinate of the player
            completions.add(Integer.valueOf((int) player.getLocation().getX()) + " "+ Integer.valueOf((int) player.getLocation().getZ()));

        } else if ((args.length == 3 || args.length == 5) && args[0].equalsIgnoreCase("setArea") && redPerms.getPlayerAllowedSilent(player, "game.admin.setArea")) {
            // The third argument, which is the current Z coordinate of the player
            completions.add(String.valueOf(Integer.valueOf((int) player.getLocation().getZ())));
        } else if ((args.length == 2) && args[0].equalsIgnoreCase("kill") && redPerms.getPlayerAllowedSilent(player, "game.admin.kill")) {
            completions.addAll(getTeamLeaderNameList());
        } else if ((args.length == 2) && args[0].equalsIgnoreCase("config") && redPerms.getPlayerAllowedSilent(player, "game.admin.config")) {
        completions.addAll(GameConfigLoader.getAllConfigs());
    }
        return completions;
    }

    private List<String> getTeamLeaderNameList() {
        List<String> leaderNames = new ArrayList<>();
        Player[] leaders = plugin.getGame().teamManager.getAllLeaders();
        this.plugin = Main.getInstance();

        for (Player leader : leaders) {
            leaderNames.add(leader.getName());
        }
        return leaderNames;
    }
}