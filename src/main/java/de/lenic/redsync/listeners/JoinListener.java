package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import de.lenic.redsync.objects.PlayerData;
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
