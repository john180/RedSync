package de.lenic.redsync.db;

import de.lenic.redsync.events.AsyncRedisMessageEvent;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

public class RSSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        Bukkit.getPluginManager().callEvent(new AsyncRedisMessageEvent(channel, message));
    }

}
