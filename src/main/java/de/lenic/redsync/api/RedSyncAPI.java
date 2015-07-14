package de.lenic.redsync.api;

import com.google.common.base.Optional;
import de.lenic.redsync.RedSync;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

public class RedSyncAPI {

    private RedSync plugin;


    // Constructor
    public RedSyncAPI(RedSync plugin){
        this.plugin = plugin;
    }


    // Add entry to player
    public void add(UUID uuid, Map.Entry<String, String> entry){
        try (Jedis j = plugin.getRedis().getResource()){
            j.hset("data_" + uuid.toString(), entry.getKey(), entry.getValue());
        }
    }

    // Get entry by UUID and key
    public Optional<String> get(UUID uuid, String key){
        try (Jedis j = plugin.getRedis().getResource()){
            String value =  j.hget("data_" + uuid.toString(), key);

            if(value == null)
                return Optional.absent();
            else
                return Optional.of(value);
        }
    }

    // Delete entries by UUID and keys
    public void delete(UUID uuid, String... deletions){
        try (Jedis j = plugin.getRedis().getResource()){
            j.hdel("data_" + uuid.toString(), deletions);
        }
    }

}
