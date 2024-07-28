package de.redstone.freebuildevent.game.team;

import de.redstone.freebuildevent.game.Game;
import de.redstone.freebuildevent.lib.Nametag;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TeamManager {
    private final HashMap<Player, Team> teams = new HashMap<>();
    private int teamcount = 0;
    private final File configFile;
    private Game game;
    private final YamlConfiguration config;
    private final List<Player> hasInvite = new ArrayList<>();
    private final ArrayList<Integer> usedIds = new ArrayList<>();

    public TeamManager(YamlConfiguration config, File configFile, Game game) {
        this.game = game;
        this.config = config;
        this.configFile = configFile;
    }

    public void loadTeams() {
        if (config.contains("teams")) {
            ConfigurationSection teamsSection = config.getConfigurationSection("teams");
            for (String leaderName : (teamsSection.getKeys(false))) {
                Player leader = Bukkit.getPlayer(leaderName); // Make sure to import Bukkit
                if (leader != null) {
                    ConfigurationSection teamSection = teamsSection.getConfigurationSection(leaderName);

                    createTeam(leader);
                    Team team = getTeam(leader);

                    // Laden der Mitglieder
                    List<String> memberNames = teamSection.getStringList("members");
                    for (String memberName : memberNames) {
                        Player member = Bukkit.getPlayer(memberName);
                        if (member != null) {
                            team.addMember(member);
                        }
                    }
                }
            }
        }
    }

    public void updateNametag(Player player) {
        Team teamForPlayer = findTeam(player);
        if (teamForPlayer != null) {
            new Nametag(player).setPrefix("&6[T"+teamForPlayer.getId()+"] ").build();
        }
    }

    public void saveTeams() {
        System.out.println("Saving Teams...");
        ConfigurationSection teamsSection = config.createSection("teams");
        for (Player leader : teams.keySet()) {
            Team team = teams.get(leader);
            ConfigurationSection teamSection = teamsSection.createSection(leader.getName());
            teamSection.set("id", ++teamcount);

            // Speichern der Mitglieder
            List<String> memberNames = team.getMembers().stream().map(Player::getName).collect(Collectors.toList());
            teamSection.set("members", memberNames);
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTeam(Player leader) {
        if (game.isRunning()) {
            leader.sendMessage("Das Event wurde bereits gestartet!");
            return;
        }
        int teamId = 0;

        do teamId++;
        while (usedIds.contains(teamId));

        Team newTeam = new Team(teamId, leader);
        teams.put(leader, newTeam);
        usedIds.add(teamId);
    }

    public Team findTeam(int id) {
        final Team[] found = {null};

        teams.forEach((leader, team) -> {
            if (team.getId() == id) {
                found[0] = team;
            }
        });

        return found[0];
    }

    public Team findTeam(Player member) {
        final Team[] found = {null};

        teams.forEach((leader, team) -> {
            if (found[0] != null) return;

            for (Player teamMember : team.getMembers()) {
                if (teamMember.equals(member)) {
                    found[0] = team;
                    break;
                }
            }
        });

        return found[0];
    }

    /**
     * Removes the whole team with all it's members
     * @param leader The leader of the team
     */
    public void removeTeam(Player leader) {
        Team team = teams.get(leader);

        // Create a list to store members
        List<Player> membersToRemove = new ArrayList<>(team.getMembers());

        // Remove members outside the loop
        for (Player member : membersToRemove) {
            PlayerMessenger.sendTeamMessage(member, "Dein Team wurde aufgelöst.");

            team.removeMember(member);
        }

        System.out.println(usedIds + "   " + team.getId());
        usedIds.remove(Integer.valueOf(team.getId()));
        // Remove the leader
        teams.remove(leader);
    }

    public void removeTeam(int id) {
        teams.remove(findTeam(id).getLeader());
    }

    public boolean doTeamExist(Player player) {
        return teams.containsKey(player);
    }

    public Team getTeam(int id) {
        return teams.get(findTeam(id).getLeader());
    }

    public Team getTeam(Player leader) {
        return teams.get(leader);
    }

    public Player[] getAllLeaders() {
        HashSet<Player> leaders = new HashSet<>();

        teams.forEach((leader, team) -> {
            leaders.add(leader);
        });

        return leaders.toArray(new Player[0]);
    }

    /**
     * Returns all players who are in teams.
     * @return Playerarray with all players in teams.
     */
    public Player[] getAllTeamedPlayers() {
        HashSet<Player> teamedPlayers = new HashSet<>();
        teams.forEach((leader, team) -> {
            teamedPlayers.addAll(team.getMembers());
        });
        return teamedPlayers.toArray(new Player[0]);
    }

    public boolean isLeader(Player player) {
        return teams.containsKey(player);
    }
    public void haveNowInvite(Player player) {
        hasInvite.add(player);
    }

    public boolean hasInvite(Player player) {
        return hasInvite.remove(player);
    }

    public Team getFirstTeam() {
        AtomicReference<Team> returnable = new AtomicReference<>();
        teams.forEach((leader, team) -> {
            returnable.set(team);
        });
        return returnable.get();
    }

    public ArrayList<Integer> getusedIds() {
        return usedIds;
    }

    public ArrayList<Team> listTeams() {
        ArrayList<Team> teams = new ArrayList<>();
        this.teams.forEach((leader,team) -> {teams.add(team);});
        teams.sort(new TeamIdComparator());
        return teams;
    }
}
