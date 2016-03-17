package de.lenic.redsync;

import de.lenic.redsync.db.RedisPubSub;
import de.lenic.redsync.listeners.JoinListener;
import de.lenic.redsync.listeners.LockListener;
import de.lenic.redsync.listeners.QuitListener;
import de.lenic.redsync.listeners.RedisMessageListener;
import de.lenic.redsync.managers.LangManager;
import de.lenic.redsync.managers.PlayerDataProvider;
import de.lenic.redsync.objects.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedSync extends JavaPlugin {

    // Constants
    public static final String LOCK_KEY = "REDSYNC_LOCK";


    // PlayerData provider
    private PlayerDataProvider playerDataProvider;

    // Redis database
    private RedisPubSub redis = null;

    // Thread pool
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Metrics
    private Metrics metrics;

    // Language manager
    private LangManager langManager;


    // ---------------- [ DEFAULT METHODS ] ---------------- //

    @Override
    public void onEnable(){
        loadMetrics();
        loadConfig();
        initRedis();
        playerDataProvider = new PlayerDataProvider();
        registerListeners();
    }

    @Override
    public void onDisable(){
        getLogger().info(this.getLang().getMessage("savingPlayers", Bukkit.getOnlinePlayers().size()));
        try {
            if(redis != null) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    redis.saveData(player.getUniqueId().toString(), new PlayerData(player).toJsonString());
                }
                redis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ---------------- [ REGISTER / LOAD ] ---------------- //

    // Register listeners
    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new LockListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new RedisMessageListener(this), this);
    }

    // Load config
    private void loadConfig(){
        getConfig().addDefault("Redis.Host", "127.0.0.1");
        getConfig().addDefault("Redis.Port", 6379);
        getConfig().addDefault("Redis.DB-Index", 0);
        getConfig().addDefault("Redis.Timeout", 60000);
        getConfig().addDefault("Redis.Connections", 4);
        getConfig().addDefault("Redis.Channel", "REDSYNC");
        getConfig().addDefault("Redis.Auth.Enabled", true);
        getConfig().addDefault("Redis.Auth.Password", "");
        getConfig().addDefault("Redis.Connection-Validation.Create", true);
        getConfig().addDefault("Redis.Connection-Validation.Borrow", true);
        getConfig().addDefault("Redis.Connection-Validation.Return", true);
        getConfig().addDefault("Security.Lock-Player", true);
        getConfig().addDefault("Security.Load-Delay", 15);
        getConfig().addDefault("Language", "en");
        getConfig().addDefault("Update-Mode", true);
        getConfig().addDefault("Config-Version", 3);
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Initialize LangManager
        switch (getConfig().getString("Language").toUpperCase()){
            case "EN": langManager = new LangManager("en"); break;
            case "DE": langManager = new LangManager("de"); break;
            default: langManager = new LangManager("en");
        }

        // Disable Update-Mode
        getConfig().set("Update-Mode", false);
        saveConfig();
    }

    private void initRedis() {
        // Auth warning
        if(!getConfig().getBoolean("Redis.Auth.Enabled"))
            getLogger().warning(getLang().getMessage("authWarning"));

        // Database
        try {
            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(getConfig().getInt("Redis.Connections") + 2);
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
            getLogger().info(getLang().getMessage("connectionSuccess"));
        } catch (Exception e) {
            getLogger().warning(getLang().getMessage("connectionFail"));
            e.printStackTrace();
        }
    }

    // Load metrics
    private void loadMetrics(){
        try {
            metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ---------------- [ GETTERS ] ---------------- //

    public PlayerDataProvider getPlayerDataProvider() {
        return playerDataProvider;
    }

    public RedisPubSub getRedis() {
        return redis;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public LangManager getLang() {
        return langManager;
    }

}
