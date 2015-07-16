package de.lenic.redsync;

import de.lenic.redsync.api.RedSyncAPI;
import de.lenic.redsync.db.RedisDB;
import de.lenic.redsync.listeners.JoinListener;
import de.lenic.redsync.listeners.LockListener;
import de.lenic.redsync.listeners.QuitListener;
import de.lenic.redsync.managers.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedSync extends JavaPlugin {

    // Redis database
    private RedisDB db = null;

    // Thread pool
    private ExecutorService executor = Executors.newCachedThreadPool();

    // Metrics
    private Metrics metrics;

    // Language manager
    private LangManager langManager = new LangManager("en");

    // API
    private RedSyncAPI api = new RedSyncAPI(this);


    // ---------------- [ METHODS ] ---------------- //

    public void onEnable(){
        this.loadMetrics();
        this.loadConfig();
        this.registerListeners();
    }

    public void onDisable(){
        this.getLogger().info(this.getLang().getMessage("savingPlayers", Bukkit.getOnlinePlayers().size()));
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        db.saveAll(players);
        db.disconnect();
    }


    // ---------------- [ REGISTER / LOAD ] ---------------- //

    // Register listeners
    private void registerListeners(){
        if(RedSyncConfig.getLockPlayer())
            this.getServer().getPluginManager().registerEvents(new LockListener(), this);

        this.getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        this.getServer().getPluginManager().registerEvents(new QuitListener(this), this);
    }

    // Load config
    private void loadConfig(){
        this.getConfig().addDefault("Redis.Host", "127.0.0.1");
        this.getConfig().addDefault("Redis.Port", 6379);
        this.getConfig().addDefault("Redis.DB-Index", 0);
        this.getConfig().addDefault("Redis.Timeout", 60000);
        this.getConfig().addDefault("Redis.Connections", 4);
        this.getConfig().addDefault("Redis.Auth.Enabled", true);
        this.getConfig().addDefault("Redis.Auth.Password", "");
        this.getConfig().addDefault("Security.Lock-Player", true);
        this.getConfig().addDefault("Security.Load-Delay", 15);
        this.getConfig().addDefault("Update-Mode", true);
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        // Load settings
        RedSyncConfig.setLockPlayer(this.getConfig().getBoolean("Security.Lock-Player"));
        RedSyncConfig.setLoadDelay(this.getConfig().getInt("Security.Load-Delay"));
        RedSyncConfig.setUpdateMode(this.getConfig().getBoolean("Update-Mode"));

        // Disable Update-Mode
        this.getConfig().set("Update-Mode", false);
        this.saveConfig();

        // Database
        if(this.getConfig().getBoolean("Redis.Auth.Enabled")){
            db = new RedisDB(this, this.getConfig().getString("Redis.Host"), this.getConfig().getInt("Redis.Port"), this.getConfig().getInt("Redis.Timeout"), this.getConfig().getInt("Redis.DB-Index"), this.getConfig().getString("Redis.Auth.Password"));
            db.connect(this.getConfig().getInt("Redis.Connections"));
        } else {
            db = new RedisDB(this, this.getConfig().getString("Redis.Host"), this.getConfig().getInt("Redis.Port"), this.getConfig().getInt("Redis.Timeout"), this.getConfig().getInt("Redis.DB-Index"), null);
            db.connect(this.getConfig().getInt("Redis.Connections"));
        }
    }

    // Load metrics
    private void loadMetrics(){
        try {
            this.metrics = new Metrics(this);
            this.metrics.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ---------------- [ GETTERS ] ---------------- //

    public RedisDB getRedis(){
        return this.db;
    }

    public ExecutorService getExecutor(){
        return this.executor;
    }

    public LangManager getLang(){
        return this.langManager;
    }

    public RedSyncAPI getAPI(){
        return this.api;
    }

}
