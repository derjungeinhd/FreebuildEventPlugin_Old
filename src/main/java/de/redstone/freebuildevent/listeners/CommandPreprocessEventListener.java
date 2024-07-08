package de.redstone.freebuildevent.listeners;

import de.redstone.freebuildevent.commands.TeamsCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandPreprocessEventListener implements Listener {
    @EventHandler
    public void deactivateDefaultTeams(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();

        if (message.equalsIgnoreCase("/team"))

        {
            e.setCancelled(true);
            TeamsCommand.showTeamsHelp(e.getPlayer(), 0);
        }
    }
}
