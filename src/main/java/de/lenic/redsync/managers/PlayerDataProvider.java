package de.lenic.redsync.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.lenic.redsync.RedSync;
import de.lenic.redsync.objects.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerDataProvider implements Closeable {

    private int saveTaskId;
    private Cache<UUID, PlayerData> playerDataCache;

    // Constructor
    public PlayerDataProvider() {
        playerDataCache = CacheBuilder.newBuilder()
                .expireAfterWrite(60L, TimeUnit.SECONDS)
                .build();
    }


    public Optional<PlayerData> getData(UUID owner) {
        return Optional.ofNullable(playerDataCache.getIfPresent(owner));
    }

    public void putData(PlayerData data) {
        playerDataCache.put(data.getOwner(), data);
    }

    public void removeData(UUID owner) {
        playerDataCache.invalidate(owner);
    }

    public void startSaveTask(RedSync plugin) {
        saveTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Map<String, String> data = new HashMap<>();

            PlayerData playerData;
            for(Player player : Bukkit.getOnlinePlayers()) {
                playerData = new PlayerData(player);
                data.put(playerData.getOwner().toString(), playerData.toJsonString());
            }
        }, 1200 * plugin.getConfig().getInt("Security.Save-Interval"), 1200 * plugin.getConfig().getInt("Security.Save-Interval")).getTaskId();
    }

    @Override
    public void close() throws IOException {
        Bukkit.getScheduler().cancelTask(saveTaskId);
    }

}
