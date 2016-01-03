package de.lenic.redsync;

import de.lenic.redsync.api.RedSyncAPI;
import de.lenic.redsync.db.RedisDB;
import de.lenic.redsync.listeners.JoinListener;
import de.lenic.redsync.listeners.LockListener;
import de.lenic.redsync.listeners.QuitListener;
import de.lenic.redsync.managers.LangManager;
import de.lenic.redsync.managers.MessagingManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedSync extends JavaPlugin {

    // Redis database
    private RedisDB db = null;

    // Thread pool
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // Metrics
    private Metrics metrics;

    // Language manager
    private LangManager langManager;

    // Plugin messaging manager
    private final MessagingManager messagingManager = new MessagingManager(this);

    // API
    private final RedSyncAPI api = new RedSyncAPI(this);


    // ---------------- [ DEFAULT METHODS ] ---------------- //

    @Override
    public void onEnable(){
        this.loadMetrics();
        this.loadConfig();
        this.registerListeners();

        // Register messaging channels
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this.messagingManager);
    }

    @Override
    public void onDisable(){
        this.getLogger().info(this.getLang().getMessage("savingPlayers", Bukkit.getOnlinePlayers().size()));
        db.saveAll(Bukkit.getOnlinePlayers());
        db.close();
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
        getConfig().addDefault("Redis.Host", "127.0.0.1");
        getConfig().addDefault("Redis.Port", 6379);
        getConfig().addDefault("Redis.DB-Index", 0);
        getConfig().addDefault("Redis.Timeout", 60000);
        getConfig().addDefault("Redis.Connections", 4);
        getConfig().addDefault("Redis.Auth.Enabled", true);
        getConfig().addDefault("Redis.Auth.Password", "");
        getConfig().addDefault("Security.Lock-Player", true);
        getConfig().addDefault("Security.Load-Delay", 15);
        getConfig().addDefault("Experimental.Plugin-Messaging", false);
        getConfig().addDefault("Language", "en");
        getConfig().addDefault("Update-Mode", true);
        getConfig().addDefault("Config-Version", 2);
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Initialize LangManager
        switch (getConfig().getString("Language").toUpperCase()){
            case "EN": langManager = new LangManager("en"); break;
            case "DE": langManager = new LangManager("de"); break;
            default: langManager = new LangManager("en");
        }

        // Load settings
        RedSyncConfig.setLockPlayer(this.getConfig().getBoolean("Security.Lock-Player"));
        RedSyncConfig.setLoadDelay(this.getConfig().getInt("Security.Load-Delay"));
        RedSyncConfig.setUpdateMode(this.getConfig().getBoolean("Update-Mode"));
        RedSyncConfig.setPluginMessaging(this.getConfig().getBoolean("Experimental.Plugin-Messaging"));

        // Experimental Feature Warning
        if(RedSyncConfig.isPluginMessaging())
            this.getLogger().warning(this.langManager.getMessage("experimentalWarning", "Plugin-Messaging"));

        // Disable Update-Mode
        this.getConfig().set("Update-Mode", false);
        this.saveConfig();

        // Database
        if(this.getConfig().getBoolean("Redis.Auth.Enabled")){
            db = new RedisDB(this, this.getConfig().getString("Redis.Host"), this.getConfig().getInt("Redis.Port"), this.getConfig().getInt("Redis.Timeout"), this.getConfig().getInt("Redis.DB-Index"), this.getConfig().getString("Redis.Auth.Password"));
            db.init(this.getConfig().getInt("Redis.Connections"));
        } else {
            db = new RedisDB(this, this.getConfig().getString("Redis.Host"), this.getConfig().getInt("Redis.Port"), this.getConfig().getInt("Redis.Timeout"), this.getConfig().getInt("Redis.DB-Index"), null);
            db.init(this.getConfig().getInt("Redis.Connections"));
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

    public MessagingManager getMessagingManager(){
        return this.messagingManager;
    }

    public RedSyncAPI getAPI(){
        return this.api;
    }

}
