package de.lenic.redsync.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class RedisPubSub implements Closeable {

    private String channel;
    private JedisPoolConfig poolConfig;
    private JedisPool pool;
    private Jedis publisherJedis;


    // Constructor
    public RedisPubSub(JedisPoolConfig poolConfig, String host, int port, int timeout, String password, int database, String channel) {
        this.channel = channel;
        this.poolConfig = poolConfig;

        if (password == null) {
            // Connect with authorization
            try {
                this.pool = new JedisPool(poolConfig, new URI("redis://" + host + ":" + port + '/' + database), timeout);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            // Connect without authorization
            this.pool = new JedisPool(poolConfig, host, port, timeout, password, database);
        }

        publisherJedis = pool.getResource();
    }


    @Override
    public void close() throws IOException {
        if(publisherJedis != null)
            publisherJedis.close();

        if(pool != null)
            pool.close();
    }


    public void publish(String message) {
        publisherJedis.publish(channel, message);
    }

    public void saveData(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        }
    }

    public Optional<String> getData(String key) {
        try (Jedis jedis = pool.getResource()) {
            return Optional.ofNullable(jedis.get(key));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

}
