package de.redstone.freebuildevent.game;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.gameconfig.GameRound;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;

public class GameEvents {
    public static void onGameStart(GameRound currentRound) {
        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
            oPlayer.sendTitle("Das Event wurde gestartet!", "Aufgabe: "+ currentRound.task());
        }
    }

    public static void onGameNextRound(GameRound currentRound) {
        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
            oPlayer.sendTitle("Die nächste Runde wurde gestartet!", "Aufgabe: "+ currentRound.task());
        }
    }

    public static void onGameIntermission(BossBar bossBar) {
        bossBar.setTitle("Bewertungsphase");
        bossBar.setProgress(1);
        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
            oPlayer.sendTitle("Die Zeit ist abgelaufen!", "Die Bewertung findet nun statt");
        }
    }

    public static void onGameFinish(BossBar bossBar) {
        Main.getInstance().getGame().reset();
        bossBar.setTitle("Spiel wurde beendet!");
        bossBar.setProgress(1);

        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
            oPlayer.sendTitle("Das Event ist beendet!", "Gewinner: "+ Main.getInstance().getGame().teamManager.getFirstTeam().getLeader().getName());
        }
    }
}
