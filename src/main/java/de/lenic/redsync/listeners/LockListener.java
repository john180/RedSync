package de.lenic.redsync.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class LockListener implements Listener {

    public static final String lockedMeta = "rsLocked";

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getWhoClicked().hasMetadata(lockedMeta))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if(e.getPlayer().hasMetadata(lockedMeta))
            e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
        if(e.getPlayer().hasMetadata(lockedMeta))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getEntity().hasMetadata(lockedMeta))
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e){
        if(e.getDamager().hasMetadata(lockedMeta))
            e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e){
        if(e.getEntity().hasMetadata(lockedMeta))
            e.setCancelled(true);
    }

}
