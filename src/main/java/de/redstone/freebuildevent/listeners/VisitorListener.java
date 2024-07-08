package de.redstone.freebuildevent.listeners;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.lib.RedPerms;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;

public class VisitorListener implements Listener {
    private Game game;
    private final RedPerms redPerms;
    public VisitorListener() {
        this.game = Main.getInstance().getGame();
        this.redPerms = Main.getInstance().getRedPerms();
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event) {
        if (this.game == null) {
            this.game = Main.getInstance().getGame();
            return;
        }

        if (redPerms.getPlayerAllowedSilent(event.getPlayer(), "teams.admin.ignore")) return;

        if (game.isInGameArea(event.getFrom().getBlockX(), event.getFrom().getBlockZ()) && !(Arrays.stream(game.teamManager.getAllTeamedPlayers()).toList().contains(event.getPlayer()))) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        } else {
            event.getPlayer().setGameMode(GameMode.CREATIVE);
        }
    }
}
