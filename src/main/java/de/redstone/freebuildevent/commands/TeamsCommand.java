package de.redstone.freebuildevent.commands;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.game.team.Team;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import de.redstone.freebuildevent.lib.RedPerms;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;

public class TeamsCommand implements CommandExecutor {
    private Game game;
    private RedPerms redPerms;
    public TeamsCommand() {
        this.game = Main.getInstance().getGame();
        this.redPerms = Main.getInstance().getRedPerms();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.game = Main.getInstance().getGame();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.DARK_RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            showTeamsHelp(player, 0);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                PlayerMessenger.sendMessage(player, "Teams: ");
                ArrayList<Team> teams = game.teamManager.listTeams();
                teams.forEach((team -> PlayerMessenger.sendMessage(player, Team.toString(team))));
                break;
            case "leave":
                if (game.isRunning()) return true;
                Team teamToLeave = game.teamManager.findTeam(player);
                if (teamToLeave != null) {
                    if (teamToLeave.getLeader().equals(player)) {
                        game.teamManager.removeTeam(player);
                        PlayerMessenger.sendMessage(player,"Du hast das Team aufgelöst.");
                    } else {
                        teamToLeave.removeMember(player);
                        PlayerMessenger.sendMessage(player,"Du hast das Team verlassen.");
                    }
                } else {
                    PlayerMessenger.sendNoPermissionText(player, "Du bist in keinem Team", false);
                }
                break;

            case "create":
                // Handle /teams create <Spielername> logic
                // Example: teamsCreate(player, args[1]);
                if (game.teamManager.doTeamExist(player)) {
                    PlayerMessenger.sendNoPermissionText(player, "Du hast bereits ein Team.", false);
                    return true;
                }

                game.teamManager.createTeam(player);
                PlayerMessenger.sendMessage(player, "Dein Team wurde erstellt! [T"+game.teamManager.getTeam(player).getId()+"]" );

                break;

            /*case "menu":
                if (args.length == 2) {
                    // Handle /teams menu <Teams> logic
                    // Example: teamsMenu(player, args[1]);
                    if (redPerms.getPlayerAllowed(player, "teams.admin.menu") || player.getDisplayName().equalsIgnoreCase(args[1])) {
                        player.sendMessage(ChatColor.DARK_RED + "Du hast keine Rechte!");
                        return true;
                    }

                    player.sendMessage("Opening menu for team: " + args[1]);
                    //TODO Interaktive Menüs machen
                } else {
                    showTeamsHelp(player, 3);
                }
                break;*/

            case "disband":
                if (args.length == 2 && (redPerms.getPlayerAllowed(player, "teams.admin.disband") || player.getDisplayName().equalsIgnoreCase(args[1]))) {
                    // Handle /teams disband <Spielername> logic
                    // Example: teamsCreate(player, args[1]);

                    try {
                        game.teamManager.removeTeam(player);
                        PlayerMessenger.sendMessage(player, "Dein Team wurde aufgelöst, " + args[1]);
                    } catch (Exception e) {
                        showTeamsHelp(player, 4);
                    }
                } else if (args.length != 2) {
                    showTeamsHelp(player, 4);
                } else {
                    PlayerMessenger.sendNoPermissionText(player, "teams.admin.disband", true);
                }
                /*} else if (args.length == 2 && !(player.isOp() || player.getDisplayName().equalsIgnoreCase(args[1]))) {
                    player.sendMessage(ChatColor.DARK_RED + "Du hast keine Rechte!");
                } else {
                    showTeamsHelp(player, 4);
                }*/
                break;

            case "remove":
                //Wenn (der ausführende Spieler der Leader ODER OP ist) UND es 3 ARGUMENTE gibt
                if (args.length == 3 && (redPerms.getPlayerAllowed(player,"teams.admin.remove") || player.getDisplayName().equalsIgnoreCase(args[1]))) {
                    // Handle /teams remove <Team> <Spielername> logic
                    // Example: teamsRemove(player, args[1]);

                    if (args[1].equalsIgnoreCase(args[2])) {
                        game.teamManager.removeTeam(Bukkit.getPlayer(args[1]));
                        return true;
                    }

                    try {
                        PlayerMessenger.sendMessage(player, "Removing member of team " + args[1] + ": " + args[2]);
                        game.teamManager.getTeam(Bukkit.getPlayer(args[1])).removeMember(Bukkit.getPlayer(args[2]));
                    } catch (Exception e) {
                        showTeamsHelp(player, 2);
                    }

                } else if (args.length != 3) {
                    showTeamsHelp(player, 2);
                } else {
                    PlayerMessenger.sendNoPermissionText(player, "teams.admin.remove", true);
                }
                break;

            case "add":
                //Wenn (der ausführende Spieler OP ist) UND es 3 ARGUMENTE gibt
                if (args.length == 3 && redPerms.getPlayerAllowed(player,"teams.admin.add")) {
                    // Handle /teams remove <Team> <Spielername> logic
                    // Example: teamsRemove(player, args[1]);

                    try {
                        game.teamManager.getTeam(Bukkit.getPlayer(args[1])).addMember(Bukkit.getPlayer(args[2]));
                        player.sendMessage("Adding member to team " + args[1] + ": " + args[2]);
                    } catch (Exception e) {
                        showTeamsHelp(player, 2);
                    }
                } else if (args.length == 3 && !(redPerms.getPlayerAllowed(player,"teams.admin.add"))) {
                    PlayerMessenger.sendNoPermissionText(player, "teams.admin.add", false);
                } else {
                    showTeamsHelp(player, 2);
                }
                break;

            case "invite":
                if (game.isRunning()) {
                    PlayerMessenger.sendMessage(player, "Das Spiel hat bereits begonnen.");
                    return true;
                }
                if (args.length == 2) {
                    if (game.teamManager.getTeam(player) == null || game.teamManager.findTeam(player).getLeader() != player) {
                        PlayerMessenger.sendNoPermissionText(player, "Du bist kein Team-Leader", false);
                        return true;
                    }
                    game.teamManager.haveNowInvite(Bukkit.getPlayer(args[1]));
                    PlayerMessenger.sendClickableText(Objects.requireNonNull(Bukkit.getPlayer(args[1])), "Du wurdest in das Team von "+ChatColor.GOLD+ player.getName()+ ChatColor.WHITE +" eingeladen!",
                            "Annehmen",
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/whackyteamjoin "+ player.getName()));
                } else {
                    showTeamsHelp(player, 5);
                }
                break;

            default:
                showTeamsHelp(player, 0);
                break;
        }

        return true;
    }

    public static void showTeamsHelp(Player player, int type) {
        String title = "-=-=-=- Teams -=-=-=-";
        String syntaxBasic = "Syntax: /teams <add/create/disband/invite/leave/menu/remove>";
        String syntaxRemove = "Syntax: /teams <add/remove> <Team> <Spielername>";
        String syntaxCreate = "Syntax: /teams create <Spielername(Admin-only)>";
        String syntaxDisband = "Syntax: /teams disband <Team(Admin-only)>";
        String syntaxLeave = "Syntax: /teams leave";
        String syntaxMenu = "Syntax: /teams menu <Team(Admin-only)>";
        String syntaxInvite = "Syntax: /teams invite";

        player.sendMessage(title);

        switch (type) {
            case 0:
                player.sendMessage(syntaxBasic);
                break;
            case 1:
                player.sendMessage(syntaxCreate);
                break;
            case 2:
                player.sendMessage(syntaxRemove);
                break;
            case 3:
                player.sendMessage(syntaxMenu);
                break;
            case 4:
                player.sendMessage(syntaxDisband);
                break;
            case 5:
                player.sendMessage(syntaxInvite);
                break;
        }
    }
}