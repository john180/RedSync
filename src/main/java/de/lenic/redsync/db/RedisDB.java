package de.lenic.redsync.db;

import de.lenic.redsync.RedSync;
import de.lenic.redsync.RedSyncConfig;
import de.lenic.redsync.objects.DataKey;
import de.lenic.redsync.objects.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RedisDB {

    // Plugin Instance
    private RedSync plugin;

    // Connection pool
    private JedisPool pool;
    private JedisPoolConfig cfg;

    // Connection settings
    private String host;
    private int port = 6379;
    private int timeout = 5000;
    private int db = 0;
    private String password;

    // Contructor
    public RedisDB(RedSync plugin, String host, int port, int timeout, int db, String password){
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.db = db;
        this.password = password;
    }


    // Connect to database
    public void connect(int threads){
        this.plugin.getLogger().info("Connecting to Redis database...");

        final long startTime = System.currentTimeMillis();

        // Configuring pool
        this.cfg = new JedisPoolConfig();
        this.cfg.setMaxTotal(threads);
        this.cfg.setMinIdle(2);
        this.cfg.setMaxIdle(4);
        this.cfg.setTestOnReturn(true);

        // Connect with auth
        if(this.password != null){
            this.pool = new JedisPool(this.cfg, this.host, this.port, this.timeout, this.password, this.db);

            // Test connection
            try (Jedis j = this.pool.getResource()){
                if(j.isConnected())
                    this.plugin.getLogger().info(plugin.getLang().getMessage("connectionSuccess", System.currentTimeMillis() - startTime));
                else {
                    this.plugin.getLogger().warning(plugin.getLang().getMessage("connectionFail"));
                    Bukkit.getPluginManager().disablePlugin(this.plugin);
                }
            }
        // Connect without auth
        } else {
            try {
                this.pool = new JedisPool(new URI("redis://" + this.host + ":" + this.port + '/' + this.db));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            // Test connection
            try (Jedis j = this.pool.getResource()) {
                if (j.isConnected()) {
                    this.plugin.getLogger().info(plugin.getLang().getMessage("connectionSuccess", System.currentTimeMillis() - startTime));
                    this.plugin.getLogger().warning(plugin.getLang().getMessage("authWarning"));
                } else {
                    this.plugin.getLogger().warning(plugin.getLang().getMessage("connectionFail"));
                    Bukkit.getPluginManager().disablePlugin(this.plugin);
                }
            }
        }

        // Update Mode
        if(RedSyncConfig.isUpdateMode()){
            try (Jedis j = this.pool.getResource()) {
                j.flushDB();
            }
        }
    }

    // Close pool
    public void disconnect(){
        if(this.pool != null)
            this.pool.close();
    }


    // Save player data
    public void savePlayer(Player p){
        try (Jedis j = this.pool.getResource()) {
            final Map<String, String> data = new HashMap<>();
            synchronized (this.plugin){
                data.put(DataKey.INV.value(), Serializer.itemStackArrayToBase64(p.getInventory().getContents()));
                data.put(DataKey.ARMOR.value(), Serializer.itemStackArrayToBase64(p.getInventory().getContents()));
                data.put(DataKey.ENDERCHEST.value(), Serializer.itemStackArrayToBase64(p.getEnderChest().getContents()));
                data.put(DataKey.POTION.value(), Serializer.potionEffectsToString(p.getActivePotionEffects()));
                data.put(DataKey.LEVEL.value(), String.valueOf(p.getLevel()));
                data.put(DataKey.EXP.value(), String.valueOf(p.getExp()));
                data.put(DataKey.HEALTH.value(), String.valueOf(p.getHealth()));
                data.put(DataKey.HUNGER.value(), String.valueOf(p.getFoodLevel()));
                data.put(DataKey.GAMEMODE.value(), String.valueOf(p.getGameMode().name()));
            }
            j.hmset("data_" + p.getUniqueId().toString(), data);
        }
    }

    // Load player data
    public void loadPlayer(Player p){
        try (Jedis j = this.pool.getResource()) {
            final Map<String, String> data = j.hgetAll("data_" + p.getUniqueId().toString());

            if(data.size() < 1)
                return;

            // Remove all potion effects
            for(PotionEffect effect : p.getActivePotionEffects()){
                p.removePotionEffect(effect.getType());
            }

            try {
                synchronized (this.plugin){
                    final ItemStack[] armor = Serializer.itemStackArrayFromBase64(data.get(DataKey.ARMOR.value()));

                    // Inventory
                    p.getInventory().setContents(Serializer.itemStackArrayFromBase64(data.get(DataKey.INV.value())));

                    // Armor
                    ItemStack[] is = new ItemStack[4];
                    for(int i = 0; i < 4; i++)
                        is[i] = armor[i];
                    p.getInventory().setArmorContents(is);

                    // Enderchest
                    p.getEnderChest().setContents(Serializer.itemStackArrayFromBase64(data.get(DataKey.ENDERCHEST.value())));

                    // Potion effects
                    for(PotionEffect effect : Serializer.potionEffectsFromString(data.get(DataKey.POTION.value())))
                        p.addPotionEffect(effect);

                    // Level
                    p.setLevel(Integer.parseInt(data.get(DataKey.LEVEL.value())));

                    // EXP
                    p.setExp(Float.parseFloat(data.get(DataKey.EXP.value())));

                    // Health
                    p.setHealth(Double.parseDouble(data.get(DataKey.HEALTH.value())));

                    // Food
                    p.setFoodLevel(Integer.parseInt(data.get(DataKey.HUNGER.value())));

                    // Gamemode
                    if(data.containsKey(DataKey.GAMEMODE.value()))
                        p.setGameMode(GameMode.valueOf(data.get(DataKey.GAMEMODE.value())));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Save all player data
    public void saveAll(Collection<? extends Player> players){
        for(Player p : players)
            savePlayer(p);
    }

}
