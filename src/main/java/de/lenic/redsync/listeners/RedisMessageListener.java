package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import de.lenic.redsync.events.AsyncRedisMessageEvent;
import de.lenic.redsync.objects.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RedisMessageListener implements Listener {

    private RedSync plugin;

    // Constructor
    public RedisMessageListener(RedSync plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onRedisMessage(AsyncRedisMessageEvent e) {
        //plugin.getLogger().info("\n\nNew message: \n" + e.getMessage() + "\n\n");
        final PlayerData data = PlayerData.fromJson(e.getMessage());

        Bukkit.getScheduler().runTask(plugin, () -> {
            final Player player = Bukkit.getPlayer(data.getOwner());

            if(player == null) {
                plugin.getPlayerDataProvider().putData(data);
                return;
            }

            // Apply data to player if not empty (empty means player has joined the network)
            if(!data.isEmpty())
                data.apply(player);

            plugin.getPlayerDataProvider().removeData(data.getOwner());
            player.removeMetadata(RedSync.LOCK_KEY, plugin);
        });
    }

}
