package de.redstone.freebuildevent.lib;

import de.redstone.freebuildevent.game.team.Team;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;

public class PlayerMessenger {
    private static final BaseComponent prefix = new ComponentBuilder()
            .append("[").color(ChatColor.GRAY)
            .append("FB").color(ChatColor.AQUA)
            .append("]").color(ChatColor.GRAY)
            .append(" ").color(ChatColor.WHITE)
            .build();

    private static final BaseComponent prefixTeam = new ComponentBuilder()
            .append("[").color(ChatColor.GRAY)
            .append("FB - Team").color(ChatColor.AQUA)
            .append("]").color(ChatColor.GRAY)
            .append(" ").color(ChatColor.WHITE)
            .build();

    public static void sendClickableText(Player player, String text, String clickableText, ClickEvent event) {
        BaseComponent[] message = new ComponentBuilder(prefix)
                .append(text)
                .append(" [").color(ChatColor.GRAY).event(event)
                .append(clickableText).color(ChatColor.DARK_GREEN).event(event)
                .append("]").color(ChatColor.GRAY).event(event)
                .create();

        player.spigot().sendMessage(message);
    }

    public static void sendClickableTeamText(Player player, String text, String clickableText, ClickEvent event) {
        BaseComponent[] message = new ComponentBuilder(prefixTeam)
                .append(text)
                .append(" [").color(ChatColor.GRAY).event(event)
                .append(clickableText).color(ChatColor.DARK_GREEN).event(event)
                .append("]").color(ChatColor.GRAY).event(event)
                .create();

        player.spigot().sendMessage(message);
    }

    public static void sendNoPermissionText(Player player, String permission, boolean leaderAlternative) {
        BaseComponent[] message = leaderAlternative ?
                new ComponentBuilder(prefix)
                        .append("FEHLER: DU HAST KEINE BERECHTIGUNG.").color(ChatColor.DARK_RED).append("\n")
                        .append("Du bist kein Leader oder dir fehlt Permission: ").color(ChatColor.RED)
                        .append(permission)
                        .create() :
                new ComponentBuilder(prefix)
                        .append("FEHLER: DU HAST KEINE BERECHTIGUNG.").color(ChatColor.DARK_RED).append("\n")
                        .append("Dir fehlt Permission: ").color(ChatColor.RED)
                        .append(permission)
                        .create();

        player.spigot().sendMessage(message);
    }

    /**
     *
     * @param player Who should get the message
     * @param description Short description e.g Spielfeld
     * @param requiredCommand Command to run to satisfy the requirement e.g. /game setArea
     * @param satisfied Is the requirement satisfied
     */
    public static void sendStartRequirement(Player player, String description,String requiredCommand, boolean satisfied) {
        String satisfactionIcon = satisfied ? "✔": "✖";
        ChatColor color = satisfied ? ChatColor.DARK_GRAY : ChatColor.WHITE;
        player.sendMessage(color +description + " | "+ satisfactionIcon + " | " + requiredCommand);
    }

    public static void sendStartRequirementTitle(Player player) {
        player.sendMessage("Spielstart - Übersicht");
    }

    public static void sendStartRequirementFooter(Player player, boolean checksPassed) {
        player.sendMessage("\n");
        if (checksPassed) {
            player.sendMessage("Ende der Überprüfung: "+ChatColor.DARK_GREEN+"Erfolgreich");
            player.sendMessage("\n");
            sendClickableText(player, "Spiel starten", "Ja", new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game start confirm"));
        } else {
            player.sendMessage("Ende der Überprüfung: "+ChatColor.RED+"Fehlerhaft");
            player.sendMessage("\n");
            player.sendMessage("Spiel starten nicht möglich!");
        }
    }

    public static void sendTaskInActionbar(String task, int remainingTime) {
        String remainingTimeAsString = convertToMMSS(remainingTime);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("Aktuelle Aufgabe: "+ task +" | Verbleibende Zeit: "+ remainingTimeAsString));
        }
    }

    public static void sendIntermissionInActionbar() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("Runde vorbei! Die Bewertung findet nun statt."));
        }
    }

    public static void sendMessage(Player player, String msg) {
        player.spigot().sendMessage(
                new ComponentBuilder(prefix)
                        .append(msg)
                .build()
        );
    }

    /**
     * Send a message as the Team-System
     * @param player Player the message should be sent to
     * @param msg The message as a String
     */
    public static void sendTeamMessage(Player player, String msg) {
        player.spigot().sendMessage(
                new ComponentBuilder(prefixTeam)
                        .append(msg)
                        .build()
        );
    }

    public static void sendMessageToAllPlayers(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(
                    new ComponentBuilder(prefix)
                            .append(msg)
                            .build()
            );
        }
    }

    public static void sendToTeam(Team team, String msg) {
        for (Player member : team.getMembers()) {
            PlayerMessenger.sendTeamMessage(member, msg);
        }
    }

    public static void sendFinishedInActionBar() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("Das Spiel ist beendet. Vielen Dank fürs Mitspielen!"));
        }
    }

    private static String convertToMMSS(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public static void showStatusBossbar(BossBar bossbar) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(onlinePlayer);
        }
    }

    public static void sendPausedInActionbar() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("Das Spiel wurde pausiert."));
        }
    }

    public static void hideStatusBossbar(BossBar bossbar) {
        bossbar.removeAll();
    }

    public static void sendTeamEliminated(Team team) {
        ComponentBuilder builder = new ComponentBuilder(prefix);
        builder.append("Das Team von "+ org.bukkit.ChatColor.GOLD + team.getLeader().getName() + org.bukkit.ChatColor.RESET +" ist ausgeschieden! | ");

        for (Player member : team.getMembers()) {
            builder.append(member.getName()+", ");
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.spigot().sendMessage(builder.build());
        }
    }

    public static String formatMemberList(HashSet<Player> list) {
        StringBuilder builder = new StringBuilder();
        for (Player player : list) {
            builder.append(player.getDisplayName());
            builder.append(", ");
        }

        return builder.toString();
    }
}
