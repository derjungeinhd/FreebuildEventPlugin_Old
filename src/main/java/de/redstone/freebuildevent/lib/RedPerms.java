package de.redstone.freebuildevent.lib;

import de.redstone.freebuildevent.Main;
import de.redstone.freebuildevent.game.Game;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RedPerms {
    //THIS REQUIRES LUCKPERMS TO WORK!
    private LuckPerms luckPermsApi;
    private Game game;
    public RedPerms(LuckPerms luckPermsApi) {
        this.luckPermsApi = luckPermsApi;
        updateGameObject();
    }

    /**
     * @param player Player to check its permissions
     * @param permission Permission to check
     * @return Is true when the player has the permission and sends a message about the insufficient permissions into the console.
     */
    public boolean getPlayerAllowed(Player player, String permission) {
        updateGameObject();
        User luckPlayer = luckPermsApi.getPlayerAdapter(Player.class).getUser(player);
        boolean isGood = luckPlayer.getCachedData().getPermissionData().checkPermission(permission).asBoolean();

        if (!isGood) {
            Bukkit.getLogger().info(player.getDisplayName()+ " wurde blockiert. Keine Berechtigung | "+permission);
        }

        return isGood;
    }

    private void updateGameObject() {
        //Get current game variables (teams,...)
        this.game = Main.getInstance().getGame();
    }

    /**
     * @param player Player to check its permissions
     * @param permission Permission to check
     * @return Is true when the player has the permission without sending a message into the console.
     */
    public boolean getPlayerAllowedSilent(Player player, String permission) {
        updateGameObject();
        User luckPlayer = luckPermsApi.getPlayerAdapter(Player.class).getUser(player);

        return luckPlayer.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}
