package de.redstone.freebuildevent.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Nametag {

    private Team team;

    public Nametag(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "team_" + player.getName();
        this.team = scoreboard.getTeam(teamName);

        if (this.team == null) {
            this.team = scoreboard.registerNewTeam(teamName);
            this.team.addEntry(player.getName());
        }
    }

    public Nametag setPrefix(String prefix) {
        this.team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        return this;
    }

    public Nametag color(ChatColor color) {
        this.team.setColor(color);
        return this;
    }

    public Nametag setSuffix(String suffix) {
        this.team.setSuffix(ChatColor.translateAlternateColorCodes('&', suffix));
        return this;
    }

    public Nametag removeEverything() {
        this.team.unregister();
        return this;
    }

    private void build(Collection<? extends Player> players) {
        for (Player p : players) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void build() {
        this.build(Bukkit.getOnlinePlayers());
    }
}