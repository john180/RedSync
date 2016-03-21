package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import de.lenic.redsync.objects.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public class JoinListener implements Listener {

    // Plugin Instance
    private RedSync plugin;
    public JoinListener(RedSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        e.getPlayer().setMetadata(RedSync.LOCK_KEY, new FixedMetadataValue(plugin, true));

        // Unlock player automatically after 30 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(e.getPlayer() == null || e.getPlayer().hasMetadata(RedSync.LOCK_KEY))
                return;

            final Optional<String> data = plugin.getRedis().getData(e.getPlayer().getUniqueId().toString());

            // Data is not present
            if(!data.isPresent())
                return;

            // Apply data to player and unlock player
            PlayerData.fromJson(data.get()).apply(e.getPlayer());
            e.getPlayer().removeMetadata(RedSync.LOCK_KEY, plugin);
        }, 600L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        final Player player = e.getPlayer();
        final Optional<PlayerData> data = plugin.getPlayerDataProvider().getData(player.getUniqueId());

        // Data not present
        if(!data.isPresent())
            return;

        // Apply data to player if not empty (empty means player has joined the network)
        if(!data.get().isEmpty())
            data.get().apply(player);

        plugin.getPlayerDataProvider().removeData(player.getUniqueId());
        player.removeMetadata(RedSync.LOCK_KEY, plugin);
    }

}
