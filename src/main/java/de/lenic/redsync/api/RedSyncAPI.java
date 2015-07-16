package de.lenic.redsync.api;

import com.google.common.base.Optional;
import de.lenic.redsync.RedSync;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

public class RedSyncAPI {

    // Plugin instance
    private RedSync plugin;


    // Constructor
    public RedSyncAPI(RedSync plugin){
        this.plugin = plugin;
    }


    /**
     * Sets an entry of a player to a specified value.
     * If entry does not exist it will be created.
     * Overwriting RedSync data can break playerdata!
     * @param uuid UUID of player
     * @param entry Key and value of your custom data
     */
    public void set(UUID uuid, Map.Entry<String, String> entry){
        try (Jedis j = plugin.getRedis().getResource()){
            j.hset("data_" + uuid.toString(), entry.getKey(), entry.getValue());
        }
    }

    /**
     * Deletes playerdata by key
     * Removing RedSync data can break playerdata!
     * @param uuid UUID of player
     * @param deletions Keys you want to delete
     */
    public void delete(UUID uuid, String... deletions){
        try (Jedis j = plugin.getRedis().getResource()){
            j.hdel("data_" + uuid.toString(), deletions);
        }
    }

    /**
     * Returns an Optional with value of the key
     * @param uuid UUID of player
     * @param key Key you want to get the value of
     * @return Optional with the value of the given key
     */
    public Optional<String> get(UUID uuid, String key){
        try (Jedis j = plugin.getRedis().getResource()){
            String value =  j.hget("data_" + uuid.toString(), key);
            return (value == null) ? Optional.<String>absent() : Optional.of(value);
        }
    }

    /**
     * Returns an Optional with all data of a player
     * @param uuid UUID of player
     * @return Optional with all playerdata
     */
    public Optional<Map<String, String>> getAll(UUID uuid){
        try (Jedis j = plugin.getRedis().getResource()){
            Map<String, String> data = j.hgetAll("data_" + uuid.toString());
            return (data == null) ? Optional.<Map<String,String>>absent() : Optional.of(data);
        }
    }

}
