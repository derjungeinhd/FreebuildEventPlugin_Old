package de.redstone.freebuildevent.game;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.team.Team;
import de.redstone.freebuildevent.game.team.TeamManager;
import de.redstone.freebuildevent.gameconfig.GameConfig;
import de.redstone.freebuildevent.gameconfig.GameConfigLoader;
import de.redstone.freebuildevent.lib.PlayerMessenger;
import de.redstone.freebuildevent.lib.RedPerms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Game {
    public TeamManager teamManager;
    public World world;
    private final File configFile;
    private final YamlConfiguration config;
    private boolean isRunning = false;
    private final int[] gameArea = {0, 0, 0, 0};
    private boolean isIntermission = false;
    private GameConfig activeGameConfig;
    private BukkitTask gameSecondRunner;
    private GameState gameState = GameState.STOPPED;
    private int currentRound = 0;
    private int remainingRoundTime = 20;
    private final BossBar bossBar;
    private int lastJudgedTeam = -1;
    private final RedPerms redPerms;

    public Game(File configFile) {
        this.configFile = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.teamManager = new TeamManager(config, configFile, this);

        world = Bukkit.getWorld("world");
        loadGameData();
        teamManager.loadTeams();

        bossBar = Bukkit.createBossBar("Spiel startet...", BarColor.PURPLE, BarStyle.SEGMENTED_20);
        this.redPerms = Main.getInstance().getRedPerms();
        //PlayerMessenger.showStatusBossbar(bossBar);
    }

    public void setGameArea(int x, int z, int xTwo, int zTwo) {
        System.out.println(Arrays.toString(gameArea));

        gameArea[0] = x;
        gameArea[1] = z;
        gameArea[2] = xTwo;
        gameArea[3] = zTwo;

        int[][] borderCoordinates = getBorderCoordinates();

        // Iterate over the array and get one point at a time
        for (int[] borderCoordinate : borderCoordinates) {
            int borderX = borderCoordinate[0];
            int borderZ = borderCoordinate[1];

            int yCoordinate = world.getHighestBlockYAt(borderX, borderZ);
            System.out.println("Coordinate: (" + borderX + ", " + yCoordinate + ", " + borderZ + ")");
            world.getBlockAt(borderX, yCoordinate, borderZ).setType(Material.REDSTONE_BLOCK);
        }
    }

    public boolean isInGameArea(int x, int z) {
        int minX = Math.min(gameArea[0], gameArea[2]);
        int maxX = Math.max(gameArea[0], gameArea[2]);
        int minZ = Math.min(gameArea[1], gameArea[3]);
        int maxZ = Math.max(gameArea[1], gameArea[3]);

        return (x >= minX && x <= maxX && z >= minZ && z <= maxZ);
    }

    public int[][] getBorderCoordinates() {
        int minX = Math.min(gameArea[0], gameArea[2]);
        int maxX = Math.max(gameArea[0], gameArea[2]);
        int minZ = Math.min(gameArea[1], gameArea[3]);
        int maxZ = Math.max(gameArea[1], gameArea[3]);

        // Calculate the number of border coordinates (including corners)
        int numCoordinates = 2 * (Math.abs(maxX - minX) + Math.abs(maxZ - minZ));

        // Initialize the array to store border coordinates
        int[][] borderCoordinates = new int[numCoordinates][2];

        if (numCoordinates > 0) {
            int index = 0;

            // Top border (left to right)
            for (int i = minX; i <= maxX; i++) {
                borderCoordinates[index++] = new int[]{i, minZ};
            }

            // Right border (top to bottom)
            for (int j = minZ + 1; j <= maxZ; j++) {
                borderCoordinates[index++] = new int[]{maxX, j};
            }

            // Bottom border (right to left)
            for (int i = maxX - 1; i >= minX; i--) {
                borderCoordinates[index++] = new int[]{i, maxZ};
            }

            // Left border (bottom to top)
            for (int j = maxZ - 1; j > minZ; j--) {
                borderCoordinates[index++] = new int[]{minX, j};
            }
        }

        return borderCoordinates;
    }

    public void loadGameData() {
        if (config.contains("game")) {
            ConfigurationSection gameSection = config.getConfigurationSection("game");

            if (gameSection.contains("gameArea")) {
                List<Integer> gameAreaList = gameSection.getIntegerList("gameArea");

                // Ensure the list has exactly 4 elements
                if (gameAreaList.size() == 4) {
                    int[] loadedGameArea = gameAreaList.stream().mapToInt(Integer::intValue).toArray();
                    setGameArea(loadedGameArea[0], loadedGameArea[1], loadedGameArea[2], loadedGameArea[3]);

                    System.out.println("Loaded GameArea " + Arrays.toString(gameArea));
                } else {
                    System.out.println("Invalid GameArea format in the config.yml");
                }
            }
        }

        //Load gameConfig
        loadAnotherConfig("default.json", null);
    }

    /**
     * Replaces the current configuration in the game. It only works when the game is NOT running
     * @param name the name of the file which contains the configuration (e.g /GameConfig/vehicles.json -> vehicles)
     */
    public void loadAnotherConfig(String name, Player player) {
        if (this.isRunning()) return;

        File configFolder = new File("./plugins/FreebuildEvent/GameConfig");
        if (!configFolder.exists()) configFolder.mkdirs();

        File gameConfig = new File(configFolder, name);
        this.activeGameConfig = GameConfigLoader.loadBuildEventConfig(gameConfig, player);
    }

    public void saveGameData() {
        ConfigurationSection gameSection = config.createSection("game");
        gameSection.set("gameArea", gameArea);

        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
        if (running) {
            gameState = GameState.RUNNING;
            GameEvents.onGameStart(activeGameConfig.rounds()[currentRound]);
            remainingRoundTime = activeGameConfig.rounds()[currentRound].time();
            gameSecondRunner = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), getGameSecondRunnable(), 0,20);
            bossBar.setProgress(1);
        } else {
            gameState = GameState.STOPPED;
            bossBar.setTitle("Spiel gestoppt.");
            bossBar.setProgress(0);
            setIntermission(false);
            gameSecondRunner.cancel();
        }
    }

    public boolean isIntermission() {
        return isIntermission;
    }

    public void setIntermission(boolean intermission) {
        if (intermission) {
            gameState = GameState.INTERMISSION;
            lastJudgedTeam = -1;
        } else {
            gameState = GameState.RUNNING;
        }
        isIntermission = intermission;
    }

    public boolean haveGameArea() {
        return gameArea[0] != 0 && gameArea[1] != 0 && gameArea[2] != 0 && gameArea[3] != 0;
    }

    private Runnable getGameSecondRunnable() {
        return () -> {
            if (gameState == GameState.STOPPED) return;

            if (gameState == GameState.RUNNING) {
                PlayerMessenger.sendTaskInActionbar(activeGameConfig.rounds()[currentRound].task(), remainingRoundTime);
                bossBar.setTitle(activeGameConfig.rounds()[currentRound].task() + " ["+(currentRound+1)+"/"+activeGameConfig.rounds().length+"]");
                bossBar.setProgress((double) remainingRoundTime / activeGameConfig.rounds()[currentRound].time());

                if (remainingRoundTime <= 0) {
                    setIntermission(true);
                    GameEvents.onGameIntermission(bossBar);
                }
                remainingRoundTime--;
            }

            if (gameState == GameState.INTERMISSION) {
                PlayerMessenger.sendIntermissionInActionbar();
                bossBar.setTitle("Bewertungsphase");
                bossBar.setProgress(1);
            }

            if (gameState == GameState.PAUSED) {
                PlayerMessenger.sendPausedInActionbar();
                bossBar.setTitle("Das Spiel wurde pausiert!");
                bossBar.setProgress(1);
            }

            if (gameState == GameState.FINISHED) {
                PlayerMessenger.sendFinishedInActionBar();
                bossBar.setTitle("Spiel wurde beendet!");
                bossBar.setProgress(100);
            }

            //PlayerMessenger.showStatusBossbar(bossBar);
        };
    }

    public void startNextRound() {
        setRunning(true);
        if (currentRound == activeGameConfig.rounds().length -1) {
            gameState = GameState.FINISHED;
            GameEvents.onGameFinish(bossBar);
            return;
        }

        currentRound++;
        remainingRoundTime = activeGameConfig.rounds()[currentRound].time();
        setIntermission(false);
        GameEvents.onGameNextRound(activeGameConfig.rounds()[currentRound]);
    }

    public String getConfigName() {
        return activeGameConfig.title();
    }

    public void skipRound() {
        if (!isRunning() || isIntermission()) return;
        remainingRoundTime = 0;
    }

    public void pause() {
        gameState = GameState.PAUSED;
    }

    public void reset() {
        setRunning(false);
        setIntermission(false);
        currentRound = 0;
        gameState = GameState.STOPPED;
    }

    public BossBar getBossbar() {
        return bossBar;
    }

    public void kill() {
        bossBar.removeAll();
        if (gameSecondRunner != null) gameSecondRunner.cancel();

        currentRound = 0;
        gameState = GameState.STOPPED;
    }

    public void teleportNextTeam() {
        lastJudgedTeam++;
        //Get team
        Team teamToJudge = teamManager.getTeam(teamManager.getusedIds().get(lastJudgedTeam));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (redPerms.getPlayerAllowedSilent(onlinePlayer, "game.judge") || teamToJudge.getMembers().contains(onlinePlayer)) {
                onlinePlayer.teleport(teamToJudge.getLeader().getLocation());
            }
        }
    }
}
