package de.lenic.redsync.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.lenic.redsync.objects.PlayerData;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerDataProvider {

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

}
