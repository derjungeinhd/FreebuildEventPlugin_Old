package de.redstone.freebuildevent.game.team;

import java.util.Comparator;

public class TeamIdComparator implements Comparator<Team> {
    @Override
    public int compare(Team t1, Team t2) {
        int t1id = (t1).getId();
        int t2id = (t2).getId();

        return Integer.compare(t1id, t2id);
    }
}
