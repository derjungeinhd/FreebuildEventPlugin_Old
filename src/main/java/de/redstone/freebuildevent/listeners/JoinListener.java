package de.redstone.freebuildevent.listeners;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.team.TeamManager;
import de.redstone.freebuildevent.lib.Nametag;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BossBar bossBar = Main.getInstance().getGame().getBossbar();
        bossBar.addPlayer(event.getPlayer());

        Main.getInstance().getGame().teamManager.updateNametag(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        BossBar bossBar = Main.getInstance().getGame().getBossbar();
        bossBar.removePlayer(event.getPlayer());

        TeamManager teamManager = Main.getInstance().getGame().teamManager;
        if (teamManager.isLeader(event.getPlayer())) {
            teamManager.removeTeam(event.getPlayer());
        }

        new Nametag(event.getPlayer()).removeEverything();
    }
}
