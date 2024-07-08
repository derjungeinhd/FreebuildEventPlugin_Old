package de.redstone.freebuildevent.listeners;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.lib.RedPerms;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Arrays;

public class BuildListener implements Listener {
    private Game game;
    private final RedPerms redPerms;
    public BuildListener() {
        this.game = Main.getInstance().getGame();
        this.redPerms = Main.getInstance().getRedPerms();
    }

    @EventHandler
    public void onBlockBuild(BlockPlaceEvent event) {
        if (this.game == null) {
            this.game = Main.getInstance().getGame();
            return;
        }

        //Ignore players with permission
        if (redPerms.getPlayerAllowedSilent(event.getPlayer(), "game.admin.ignore")) return;

        //Cancel event if | block is in game area AND game is currently in intermission phase AND game is running
        if (game.isInGameArea(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockZ()) && (game.isIntermission() || !game.isRunning())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.game = Main.getInstance().getGame();

        //Ignore players with permission
        if (redPerms.getPlayerAllowedSilent(event.getPlayer(), "game.admin.ignore")) return;

        //Cancel event if | block is in game area AND game is currently in intermission phase AND game is running
        if (game.isInGameArea(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockZ()) && (game.isIntermission() || !game.isRunning())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockChange(BlockBurnEvent event) {
        this.game = Main.getInstance().getGame();

        //Cancel event if | block is in game area AND game is currently in intermission phase AND game is running
        if (game.isInGameArea(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockZ()) && (game.isIntermission() || !game.isRunning())) {
            event.setCancelled(true);
        }
    }
}
