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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Game class manages the state and behavior of the game.
 */
public class Game {
    public final TeamManager teamManager;
    private final World world;
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
    private boolean allJudged = false;
    private ArrayList<Integer> currentUsedIDs = null;

    /**
     * Constructor to initialize the Game instance.
     *
     * @param configFile The configuration file for the game.
     */
    public Game(File configFile) {
        this.configFile = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.teamManager = new TeamManager(config, configFile, this);

        world = Bukkit.getWorld("world");
        loadGameData();
        teamManager.loadTeams();

        bossBar = Bukkit.createBossBar("Spiel startet...", BarColor.PURPLE, BarStyle.SEGMENTED_20);
        this.redPerms = Main.getInstance().getRedPerms();
    }

    /**
     * Sets the game area with the specified coordinates.
     *
     * @param x    The x-coordinate of the first corner.
     * @param z    The z-coordinate of the first corner.
     * @param xTwo The x-coordinate of the opposite corner.
     * @param zTwo The z-coordinate of the opposite corner.
     */
    public void setGameArea(int x, int z, int xTwo, int zTwo) {
        System.out.println(Arrays.toString(gameArea));

        gameArea[0] = x;
        gameArea[1] = z;
        gameArea[2] = xTwo;
        gameArea[3] = zTwo;

        int[][] borderCoordinates = getBorderCoordinates();

        // Iterate over the array and set redstone blocks at the border coordinates.
        for (int[] borderCoordinate : borderCoordinates) {
            int borderX = borderCoordinate[0];
            int borderZ = borderCoordinate[1];

            int yCoordinate = world.getHighestBlockYAt(borderX, borderZ);
            System.out.println("Coordinate: (" + borderX + ", " + yCoordinate + ", " + borderZ + ")");
            world.getBlockAt(borderX, yCoordinate, borderZ).setType(Material.REDSTONE_BLOCK);
        }
    }

    /**
     * Checks if the given coordinates are within the game area.
     *
     * @param x The x-coordinate to check.
     * @param z The z-coordinate to check.
     * @return True if the coordinates are within the game area, false otherwise.
     */
    public boolean isInGameArea(int x, int z) {
        int minX = Math.min(gameArea[0], gameArea[2]);
        int maxX = Math.max(gameArea[0], gameArea[2]);
        int minZ = Math.min(gameArea[1], gameArea[3]);
        int maxZ = Math.max(gameArea[1], gameArea[3]);

        return (x >= minX && x <= maxX && z >= minZ && z <= maxZ);
    }

    /**
     * Gets the border coordinates of the game area.
     *
     * @return A 2D array of border coordinates.
     */
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

    /**
     * Loads game data from the configuration file.
     */
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

        // Load default game configuration
        loadAnotherConfig("default.json", null);
    }

    /**
     * Replaces the current game configuration with a new one.
     *
     * @param name   The name of the new configuration file.
     * @param player The player who requested the configuration change.
     */
    public void loadAnotherConfig(String name, Player player) {
        if (this.isRunning()) return;

        File configFolder = new File("./plugins/FreebuildEvent/GameConfig");
        if (!configFolder.exists()) configFolder.mkdirs();

        File gameConfig = new File(configFolder, name);
        this.activeGameConfig = GameConfigLoader.loadBuildEventConfig(gameConfig, player);
    }

    /**
     * Saves the current game data to the configuration file.
     */
    public void saveGameData() {
        ConfigurationSection gameSection = config.createSection("game");
        gameSection.set("gameArea", gameArea);

        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    /**
     * Checks if the game is currently running.
     *
     * @return True if the game is running, false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Sets the running state of the game.
     *
     * @param running True to start the game, false to stop it.
     */
    public void setRunning(boolean running) {
        isRunning = running;
        if (running) {
            gameState = GameState.RUNNING;
            GameEvents.onGameStart(activeGameConfig.rounds()[currentRound]);
            remainingRoundTime = activeGameConfig.rounds()[currentRound].time();
            gameSecondRunner = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), getGameSecondRunnable(), 0, 20);
            bossBar.setProgress(1);
        } else {
            gameState = GameState.STOPPED;
            bossBar.setTitle("Spiel gestoppt.");
            bossBar.setProgress(0);
            setIntermission(false);
            gameSecondRunner.cancel();
        }
    }

    /**
     * Checks if the game is in intermission state.
     *
     * @return True if the game is in intermission, false otherwise.
     */
    public boolean isIntermission() {
        return isIntermission;
    }

    /**
     * Sets the intermission state of the game.
     *
     * @param intermission True to set the game to intermission, false to resume the game.
     */
    public void setIntermission(boolean intermission) {
        if (intermission) {
            gameState = GameState.INTERMISSION;
            lastJudgedTeam = -1;
        } else {
            gameState = GameState.RUNNING;
        }
        isIntermission = intermission;
    }

    /**
     * Checks if the game area is defined.
     *
     * @return True if the game area is defined, false otherwise.
     */
    public boolean haveGameArea() {
        return gameArea[0] != 0 && gameArea[1] != 0 && gameArea[2] != 0 && gameArea[3] != 0;
    }

    /**
     * Creates a Runnable that updates the game state every second.
     *
     * @return The Runnable instance.
     */
    private Runnable getGameSecondRunnable() {
        return () -> {
            if (gameState == GameState.STOPPED) return;

            if (gameState == GameState.RUNNING) {
                PlayerMessenger.sendTaskInActionbar(activeGameConfig.rounds()[currentRound].task(), remainingRoundTime);
                bossBar.setTitle(activeGameConfig.rounds()[currentRound].task() + " [" + (currentRound + 1) + "/" + activeGameConfig.rounds().length + "]");
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
        };
    }

    /**
     * Starts the next round of the game.
     */
    public void startNextRound() {
        setRunning(true);
        this.allJudged = false;
        this.currentUsedIDs = null;

        System.out.println("New round started");
        System.out.println(this.allJudged);
        lastJudgedTeam = -1; // Reset lastJudgedTeam to start judging from the first team
        if (currentRound >= activeGameConfig.rounds().length - 1) {
            gameState = GameState.FINISHED;
            GameEvents.onGameFinish(bossBar);
            return;
        }

        currentRound++;
        remainingRoundTime = activeGameConfig.rounds()[currentRound].time();
        setIntermission(false);
        GameEvents.onGameNextRound(activeGameConfig.rounds()[currentRound]);
    }

    /**
     * Gets the name of the current configuration.
     *
     * @return The name of the configuration.
     */
    public String getConfigName() {
        return activeGameConfig.title();
    }

    /**
     * Skips the current round.
     */
    public void skipRound() {
        if (!isRunning() || isIntermission()) return;
        remainingRoundTime = 0;
    }

    /**
     * Pauses the game.
     */
    public void pause() {
        gameState = GameState.PAUSED;
    }

    /**
     * Resets the game to its initial state.
     */
    public void reset() {
        setRunning(false);
        setIntermission(false);
        currentRound = 0;
        gameState = GameState.STOPPED;
    }

    /**
     * Gets the boss bar for the game.
     *
     * @return The BossBar instance.
     */
    public BossBar getBossbar() {
        return bossBar;
    }

    /**
     * Stops the game and cleans up resources.
     */
    public void kill() {
        bossBar.removeAll();
        if (gameSecondRunner != null) gameSecondRunner.cancel();

        currentRound = 0;
        gameState = GameState.STOPPED;
    }

    /**
     * Teleports the player to the next team to be judged.
     *
     * @param player The player to be teleported.
     */
    public void teleportNextTeam(Player player) {
        lastJudgedTeam++;

        if (currentUsedIDs == null) {
            System.out.println("Update currentUsedIds");
            currentUsedIDs = new ArrayList<>(teamManager.getusedIds());
        }

        System.out.println("Teamwertung: " + lastJudgedTeam + "/" + currentUsedIDs.size());

        // Run startNextRound if all teams have been judged and the command ran again
        if (lastJudgedTeam >= currentUsedIDs.size() +1) {
            startNextRound();
            return;
        }

        // Check if all teams have been judged
        if (lastJudgedTeam >= currentUsedIDs.size()) {
            allJudged = true;
            PlayerMessenger.sendMessage(player, "Alle Teams wurden bewertet. Drücke \"Nächste Runde\", um die nächste Runde zu starten");
            return;
        }

        // Get team to judge
        Team teamToJudge = teamManager.getTeam(currentUsedIDs.get(lastJudgedTeam));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (redPerms.getPlayerAllowedSilent(onlinePlayer, "game.judge") || teamToJudge.getMembers().contains(onlinePlayer)) {
                onlinePlayer.teleport(teamToJudge.getLeader().getLocation());
            }
        }
    }
}