package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import de.lenic.redsync.objects.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    // Plugin Instance
    private RedSync plugin;
    public QuitListener(RedSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        final Player player = e.getPlayer();

        // Do not save data if player is locked
        if(player.hasMetadata(RedSync.LOCK_KEY))
            return;

        // Bugfix: Clear inventory if dead
        if(player.isDead())
            player.getInventory().clear();

        final PlayerData data = new PlayerData(player);

        // Save player data
        plugin.getExecutor().execute(() -> {
            try {
                final String json = data.toJsonString();

                // Publish data
                plugin.getRedis().publish(json);
                plugin.getRedis().saveData(data.getOwner().toString(), json);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
