package de.redstone.freebuildevent.commands;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import de.redstone.freebuildevent.lib.RedPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GameCommand implements CommandExecutor {
    private Game game;
    private final RedPerms redPerms;

    public GameCommand() {
        this.game = Main.getInstance().getGame();
        this.redPerms = Main.getInstance().getRedPerms();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.game = Main.getInstance().getGame();
        Player player = (Player) sender;

        if (args.length == 5 && args[0].equalsIgnoreCase("setArea") && redPerms.getPlayerAllowed(player, "game.admin.setArea")) {

            System.out.println(redPerms.getPlayerAllowed(player, "teams.admin.setArea"));
            System.out.println(Arrays.toString(args));

            game.setGameArea(
                    Integer.parseInt(args[1]),
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4])
            );
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("start") && redPerms.getPlayerAllowed(player, "game.admin.start")) {
            //Test if all requirements to start are satisfied.
            testStartRequirements(player);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start") && args[1].equalsIgnoreCase("confirm") && redPerms.getPlayerAllowed(player, "game.admin.start")) {
            game.setRunning(true);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("pause") && redPerms.getPlayerAllowed(player, "game.admin.pause")) {
            game.setIntermission(true);
            game.pause();
            for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                oPlayer.sendTitle("Das Event wurde pausiert!", "Durch: "+ sender.getName());
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("next") && redPerms.getPlayerAllowed(player, "game.admin.master")) {
            if (!game.isIntermission()) {
                PlayerMessenger.sendMessage(player, "Dieser Befehl ist nur zwischen den Runden verfügbar.");
                return true;
            }

            game.teleportNextTeam(player);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("resume") && redPerms.getPlayerAllowed(player, "game.admin.resume")) {
            game.setIntermission(false);
            for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                oPlayer.sendTitle("Das Event wurde fortgesetzt!", "Durch: "+ sender.getName());
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stop") && redPerms.getPlayerAllowed(player, "game.admin.stop")) {
            game.setRunning(false);
            game.setIntermission(false);
            for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                oPlayer.sendTitle("Das Event wurde abgebrochen!", "Durch: "+ sender.getName());
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("skip") && redPerms.getPlayerAllowed(player, "game.admin.skip")) {
            if (!game.isRunning() || game.isIntermission()) {
                PlayerMessenger.sendMessage((Player) sender, "Die Runde ist bereits vorbei oder das Spiel ist nicht gestartet");
                return true;
            }
            game.skipRound();
            PlayerMessenger.sendMessageToAllPlayers("Die Runde wurde übersprungen!");
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("continue") && redPerms.getPlayerAllowed(player, "game.admin.continue")) {
            if (!game.isRunning() || !game.isIntermission()) {
                PlayerMessenger.sendMessage((Player) sender, "Die Runde kann nicht fortgesetzt werden: Die Runde läuft bereits oder das Spiel wurde nicht gestartet");
                return true;
            }
            game.startNextRound();
            PlayerMessenger.sendMessageToAllPlayers("Die Bewertung wurde vorzeitig beendet!");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("config") && redPerms.getPlayerAllowed(player, "game.admin.config")) {
            if (game.isRunning()) {
                sender.sendMessage("Das Spiel läuft bereits.");
                return true;
            }

            game.loadAnotherConfig(args[1], player);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("kill") && redPerms.getPlayerAllowed(player, "game.admin.kill")) {
            PlayerMessenger.sendTeamEliminated(game.teamManager.findTeam(Bukkit.getPlayer(args[1])));
            game.teamManager.removeTeam(Bukkit.getPlayer(args[1]));
        }

        return true;
    }

    private boolean testStartRequirements(Player player) {
        boolean isAreaSet = game.haveGameArea();
        boolean isConfigOk = !game.getConfigName().equalsIgnoreCase("default");
        System.out.println(game.getConfigName());
        boolean checksPassed = isAreaSet && isConfigOk;

        PlayerMessenger.sendStartRequirementTitle(player);
        PlayerMessenger.sendStartRequirement(player, "Spielfeld", "/game setArea", isAreaSet);
        PlayerMessenger.sendStartRequirement(player, "Spielkonfiguration", "/game config", isConfigOk);

        PlayerMessenger.sendStartRequirementFooter(player, checksPassed);

        return checksPassed;
    }
}
