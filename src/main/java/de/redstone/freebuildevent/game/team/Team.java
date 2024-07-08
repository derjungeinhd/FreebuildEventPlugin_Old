package de.redstone.freebuildevent.game.team;

import de.redstone.freebuildevent.lib.Nametag;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashSet;

public class Team {
    private int id;
    private Player leader;
    private HashSet<Player> members = new HashSet<>();
    public int getId() {
        return id;
    }
    public HashSet<Player> getMembers() {
        return members;
    }
    public Player getLeader() {
        return leader;
    }

    public Team (int id, Player leader) {
        this.id = id;
        this.leader = leader;

        addMember(leader);
    }

    public void removeMember(Player member) {
        members.remove(member);
        new Nametag(member).removeEverything().build();
    }

    public void addMember(Player member) {
        members.add(member);
        new Nametag(member).setPrefix("&6[T"+this.getId()+"] ").build();
    }

    public static String toString(Team team) {
        StringBuilder builder = new StringBuilder();
        builder.append("[T"+team.getId()+"] ");
        builder.append("Leader: " + team.getLeader().getName());
        builder.append(" | ");
        team.members.forEach((member) -> {
            builder.append(member.getName() + ", ");
        });
        return builder.toString();
    }
}
