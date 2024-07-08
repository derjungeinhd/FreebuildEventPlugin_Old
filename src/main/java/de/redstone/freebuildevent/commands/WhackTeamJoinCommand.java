package de.redstone.freebuildevent.commands;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class WhackTeamJoinCommand implements CommandExecutor {
    private Game game;

    public WhackTeamJoinCommand() {
        this.game = Main.getInstance().getGame();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.game = Main.getInstance().getGame();

        if (args.length != 1) return true;

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.DARK_RED + "Only players can use this command.");
            return true;
        }

        if (!game.teamManager.hasInvite(player)) {
            PlayerMessenger.sendNoPermissionText(player, "Du hast keine Einladung erhalten.", false);
            return true;
        }

        if (game.teamManager.findTeam(player) != null) {
            PlayerMessenger.sendNoPermissionText(player, "Du bist bereits in einem Team.", false);
            return true;
        }

        if (game.isRunning()) {
            PlayerMessenger.sendMessage(player, "Das Spiel hat bereits begonnen.");
            return true;
        }

        game.teamManager.getTeam(Bukkit.getPlayer(args[0])).addMember(player);

        PlayerMessenger.sendMessage(player, "Du bist dem Team von "+ChatColor.GOLD+args[0]+ChatColor.WHITE+" beigetreten.");
        PlayerMessenger.sendMessage(Objects.requireNonNull(Bukkit.getPlayer(args[0])), ChatColor.GOLD+player.getName()+ChatColor.WHITE+" ist dem Team beigetreten.");

        return true;
    }
}
