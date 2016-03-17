package de.lenic.redsync.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RedisPubSub implements Closeable {

    private String channel;
    private JedisPoolConfig poolConfig;
    private JedisPool pool;

    private Thread subsriberThread;
    private Jedis subscriberJedis;
    private RSSubscriber subscriber;
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

        subscriber = new RSSubscriber();
        subscriberJedis = pool.getResource();
        subsriberThread = new Thread(() -> subscriberJedis.subscribe(subscriber, channel) );
        subsriberThread.setName("RedSync Subscriber Thread");
        subsriberThread.start();
        publisherJedis = pool.getResource();
    }


    @Override
    public void close() throws IOException {
        if(publisherJedis != null)
            publisherJedis.close();

        if(subscriberJedis != null)
            subscriberJedis.close();

        if(subsriberThread != null)
            subsriberThread.interrupt();

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

}
