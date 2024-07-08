package de.redstone.freebuildevent.commands;

import de.redstone.freebuildevent.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TeamsCommandTabCompleter implements TabCompleter {
    private final Main plugin;
    public TeamsCommandTabCompleter() {
        this.plugin = Main.getInstance();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("menu");
            completions.add("disband");
            completions.add("add");
            completions.add("remove");
            completions.add("leave");
            completions.add("invite");
            completions.add("list");
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "add":
                case "remove":
                case "menu":
                case "disband":
                    for (String team : getTeamLeaderNameList()) {
                        completions.add(team);
                    }
                    break;
                case "invite":
                    completions.addAll(getTeamlessPlayers());
                    break;
            }
            return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "menu":
                    completions.addAll(getTeamLeaderNameList());
                    break;
                case "add":
                    if (sender.isOp()) {
                        completions.addAll(getTeamlessPlayers());
                    }
                    break;
                case "remove":
                    for (Player teamedPlayer : plugin.getGame().teamManager.getAllTeamedPlayers()) {
                        completions.add(teamedPlayer.getDisplayName());
                    }
                    break;
            }
            return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
        } else if (args.length > 3) {
            //MEHR ALS 3 ARGUMENTE bspw: /teams add <Team> <Spielername> <VIERTES ARGUMENT>
            completions.add("SYNTAX ERROR!");
            return completions;
        }

        return null;
    }

    // Replace this method with your own logic to get the list of teams
    private List<String> getTeamLeaderNameList() {
        List<String> leaderNames = new ArrayList<>();
        Player[] leaders = plugin.getGame().teamManager.getAllLeaders();
        for (Player leader : leaders) {
            leaderNames.add(leader.getName());
        }
        return leaderNames;
    }

    private List<String> getTeamlessPlayers() {
        List<String> teamlessPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            teamlessPlayers.add(player.getDisplayName());
        }
        Player[] teamedPlayers = plugin.getGame().teamManager.getAllTeamedPlayers();
        for (Player player : teamedPlayers) {
            teamlessPlayers.remove(player.getDisplayName());
        }
        return teamlessPlayers;
    }
}