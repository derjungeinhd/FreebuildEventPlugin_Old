package de.redstone.freebuildevent.gameconfig;

import java.util.Arrays;

//TODO Hier weitere Konfigurationseinstellungen hinzufügen und unten testen
public record GameConfig(String title, GameRound[] rounds) {
    @Override
    public String toString() {
        return "GameConfig{" +
                "title='" + title + '\'' +
                ", rounds=" + Arrays.toString(rounds) +
                '}';
    }

    /*@Test
    public void testGSON() {
        GameRound[] rounds = {new GameRound("Baue einen bayrischen Bauer", 3600), new GameRound("Baue 2 bayrische Bauern", 3600)};
        GameConfig gameConfig = new GameConfig("test123", rounds);
        Gson gson = new Gson();
        System.out.println(gson.toJson(gameConfig));
    }*/
}

