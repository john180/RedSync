package de.lenic.redsync;

import de.lenic.redsync.db.RedisPubSub;
import de.lenic.redsync.listeners.ConnectListener;
import de.lenic.redsync.listeners.ServerSwitchListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class RedSyncBungee extends Plugin {

    private Configuration config;
    private RedisPubSub redis;


    @Override
    public void onEnable() {
        loadConfig();
        initRedis();
        registerListeners();
    }

    @Override
    public void onDisable() {
        if(redis != null) {
            try {
                redis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void loadConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        final File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRedis() {
        // Database
        try {
            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(getConfig().getInt("Redis.Connections") + 1);
            poolConfig.setTestOnCreate(getConfig().getBoolean("Redis.Connection-Validation.Create"));
            poolConfig.setTestOnReturn(getConfig().getBoolean("Redis.Connection-Validation.Return"));

            redis = new RedisPubSub(
                    poolConfig,
                    getConfig().getString("Redis.Host"),
                    getConfig().getInt("Redis.Port"),
                    getConfig().getInt("Redis.Timeout"),
                    getConfig().getBoolean("Redis.Auth.Enabled") ? getConfig().getString("Redis.Auth.Password") : null,
                    getConfig().getInt("Redis.DB-Index"),
                    getConfig().getString("Redis.Channel")
            );

            getLogger().info("Successfully connected to Redis!");
        } catch (Exception e) {
            getLogger().warning("Failed to connect to Redis!");
            e.printStackTrace();
        }
    }

    private void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new ConnectListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerSwitchListener(this));
    }


    public Configuration getConfig() {
        return config;
    }

    public RedisPubSub getRedis() {
        return redis;
    }

}
