package de.redstone.freebuildevent.game;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.gameconfig.GameRound;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

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

        World w = Bukkit.getWorld("world");

        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
            oPlayer.playSound(oPlayer.getLocation(), Sound.ENTITY_DOLPHIN_DEATH, 20, 1);
            int diameter = 10; //Diameter of the circle centered on loc

            for (int i = 0; i < 3; i++)
            {
                Location loc = oPlayer.getLocation();
                Location newLocation = loc.add(new Vector(Math.random()-0.5, 0, Math.random()-0.5).multiply(diameter));
                Firework firework = (Firework) w.spawnEntity(newLocation, EntityType.FIREWORK_ROCKET);
                FireworkMeta fwm = firework.getFireworkMeta();

                fwm.setPower(2);
                fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

                firework.setFireworkMeta(fwm);
                firework.setLife(60);
            }
            oPlayer.sendTitle("Das Team von " + ChatColor.GOLD + Main.getInstance().getGame().teamManager.getFirstTeam().getLeader().getDisplayName() + ChatColor.WHITE + " hat gewonnen!",
                    "Teammitglieder: "+ PlayerMessenger.formatMemberList(Main.getInstance().getGame().teamManager.getFirstTeam().getMembers()));
        }
    }
}
