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

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RedisDB implements Closeable {

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


    // Constructor
    public RedisDB(RedSync plugin, String host, int port, int timeout, int db, String password){
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.db = db;
        this.password = password;
    }


    // Connect to database
    public void init(int threads){
        this.plugin.getLogger().info("Connecting to Redis database...");

        long startTime = System.currentTimeMillis();

        // Configuring pool
        this.cfg = new JedisPoolConfig();
        this.cfg.setMaxTotal(threads);
        this.cfg.setMinIdle(2);
        this.cfg.setMaxIdle(4);
        this.cfg.setTestOnReturn(true);

        // Connect with authorization
        if (this.password != null) {
            this.pool = new JedisPool(this.cfg, this.host, this.port, this.timeout, this.password, this.db);
        // Connect without authorization
        } else {
            this.plugin.getLogger().warning(plugin.getLang().getMessage("authWarning"));
            try {
                this.pool = new JedisPool(new URI("redis://" + this.host + ":" + this.port + '/' + this.db));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        // Test connection
        try (Jedis j = this.pool.getResource()) {
            if (j.isConnected()) {
                this.plugin.getLogger().info(plugin.getLang().getMessage("connectionSuccess", System.currentTimeMillis() - startTime));
            } else {
                this.plugin.getLogger().warning(plugin.getLang().getMessage("connectionFail"));
                Bukkit.getPluginManager().disablePlugin(this.plugin);
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
    @Override
    public void close(){
        if(this.pool != null)
            this.pool.close();
    }


    // Save player data
    public void savePlayer(Player p){
        try (Jedis j = this.pool.getResource()) {
            final Map<String, String> data = new HashMap<>();
            synchronized (p){
                data.put(DataKey.INV.value(), Serializer.itemStackArrayToBase64(p.getInventory().getContents()));
                data.put(DataKey.ARMOR.value(), Serializer.itemStackArrayToBase64(p.getInventory().getArmorContents()));
                data.put(DataKey.ENDERCHEST.value(), Serializer.itemStackArrayToBase64(p.getEnderChest().getContents()));
                data.put(DataKey.POTION.value(), Serializer.potionEffectsToString(p.getActivePotionEffects()));
                data.put(DataKey.LEVEL.value(), String.valueOf(p.getLevel()));
                data.put(DataKey.EXP.value(), String.valueOf(p.getExp()));
                data.put(DataKey.MAX_HEALTH.value(), String.valueOf(p.getMaxHealth()));
                data.put(DataKey.HEALTH.value(), String.valueOf(p.getHealth()));
                data.put(DataKey.HUNGER.value(), String.valueOf(p.getFoodLevel()));
                data.put(DataKey.GAMEMODE.value(), p.getGameMode().name());
                data.put(DataKey.FIRETICKS.value(), String.valueOf(p.getFireTicks()));
                data.put(DataKey.SLOT.value(), String.valueOf(p.getInventory().getHeldItemSlot()));
            }
            j.hmset("data_" + p.getUniqueId().toString(), data);
        }
    }

    // Load player data
    public void loadPlayer(final Player p){
        try (Jedis j = this.pool.getResource()) {
            final Map<String, String> data = j.hgetAll("data_" + p.getUniqueId().toString());

            if(data.size() < 1)
                return;

            // Remove all potion effects
            for(PotionEffect effect : p.getActivePotionEffects())
                p.removePotionEffect(effect.getType());

            try {
                final ItemStack[] inv = Serializer.itemStackArrayFromBase64(data.get(DataKey.INV.value()));
                final ItemStack[] armor = Serializer.itemStackArrayFromBase64(data.get(DataKey.ARMOR.value()));
                final ItemStack[] enderchest = Serializer.itemStackArrayFromBase64(data.get(DataKey.ENDERCHEST.value()));
                final Collection<PotionEffect> effects = Serializer.potionEffectsFromString(data.get(DataKey.POTION.value()));
                final int level = Integer.parseInt(data.get(DataKey.LEVEL.value()));
                final float exp = Float.parseFloat(data.get(DataKey.EXP.value()));
                final double maxHealth = Double.parseDouble(data.get(DataKey.MAX_HEALTH.value()));
                final double health = Double.parseDouble(data.get(DataKey.HEALTH.value()));
                final int food = Integer.parseInt(data.get(DataKey.HUNGER.value()));
                final GameMode gamemode = GameMode.valueOf(data.get(DataKey.GAMEMODE.value()));
                final int fireticks = Integer.parseInt(data.get(DataKey.FIRETICKS.value()));
                final int slot = Integer.parseInt(data.get(DataKey.SLOT.value()));

                // Apply data
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // Inventory
                    p.getInventory().setContents(inv);

                    // Armor
                    p.getInventory().setArmorContents(armor);

                    // Enderchest
                    p.getEnderChest().setContents(enderchest);

                    // Potion effects
                    for(PotionEffect effect : effects)
                        p.addPotionEffect(effect);

                    // Level
                    p.setLevel(level);

                    // EXP
                    p.setExp(exp);

                    // Max Health
                    p.setMaxHealth(maxHealth);

                    // Health
                    p.setHealth(health);

                    // Food
                    p.setFoodLevel(food);

                    // Gamemode
                    p.setGameMode(gamemode);

                    // Fire ticks
                    p.setFireTicks(fireticks);

                    // Item slot
                    p.getInventory().setHeldItemSlot(slot);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Save all player data
    public void saveAll(Collection<? extends Player> players) {
        players.forEach(p -> savePlayer(p));
    }

    // Get resource from pool
    public Jedis getResource(){
        return this.pool.getResource();
    }

}
