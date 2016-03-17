package de.lenic.redsync.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.lenic.redsync.serialization.typeadapters.PlayerDataSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

public class PlayerData {

    private static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(PlayerData.class, new PlayerDataSerializer())
            .create();

    private UUID owner;
    private ItemStack[] inventory;
    private ItemStack offHand;
    private ItemStack[] armor;
    private ItemStack[] enderchest;
    private Collection<PotionEffect> potionEffects;
    private double maxHealth;
    private double health;
    private int food;
    private float exp;
    private int level;
    private int gameMode;
    private int fireTicks;
    private float fallDistance;
    private int selectedSlot;
    private float exhaustion;
    private float saturation;

    private boolean isEmpty = false;


    // Constructor
    public PlayerData() {}
    public PlayerData(Player player) {
        owner = player.getUniqueId();
        inventory = player.getInventory().getContents();
        offHand = player.getInventory().getItemInOffHand();
        armor = player.getInventory().getArmorContents();
        enderchest = player.getEnderChest().getContents();
        potionEffects = player.getActivePotionEffects();
        maxHealth = player.getMaxHealth();
        health = player.getHealth();
        food = player.getFoodLevel();
        exp = player.getExp();
        level = player.getLevel();
        gameMode = player.getGameMode().ordinal();
        fireTicks = player.getFireTicks();
        fallDistance = player.getFallDistance();
        selectedSlot = player.getInventory().getHeldItemSlot();
        exhaustion = player.getExhaustion();
        saturation = player.getSaturation();
    }


    // ---------------- [ OTHER METHODS ] ---------------- //

    public String toJsonString() {
        return gson.toJson(this);
    }

    public void apply(Player player) {
        player.getInventory().setContents(inventory);
        player.getInventory().setItemInOffHand(offHand);
        player.getInventory().setArmorContents(armor);
        player.getEnderChest().setContents(enderchest);

        // Potion effects
        player.getActivePotionEffects().clear();
        if(potionEffects != null)
            potionEffects.forEach(pe -> player.addPotionEffect(pe));

        player.setMaxHealth(maxHealth);
        player.setHealth(health);
        player.setFoodLevel(food);
        player.setExp(exp);
        player.setLevel(level);
        player.setGameMode(GameMode.values()[gameMode]);
        player.setFireTicks(fireTicks);
        player.setFallDistance(fallDistance);
        player.getInventory().setHeldItemSlot(selectedSlot);
        player.setExhaustion(exhaustion);
        player.setSaturation(saturation);
    }

    public static PlayerData fromJson(String json) {
        return gson.fromJson(json, PlayerData.class);
    }


    // ---------------- [ GETTERS / SETTERS ] ---------------- //

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public void setInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public ItemStack[] getEnderchest() {
        return enderchest;
    }

    public void setEnderchest(ItemStack[] enderchest) {
        this.enderchest = enderchest;
    }

    public Collection<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(Collection<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public float getExp() {
        return exp;
    }

    public void setExp(float exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }


    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

}
