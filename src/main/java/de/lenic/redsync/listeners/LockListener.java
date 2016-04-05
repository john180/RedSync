package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class LockListener implements Listener {

    private RedSync plugin;

    // Constructor
    public LockListener(RedSync plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e) {
        if(e.getPlayer().hasMetadata(RedSync.LOCK_KEY)) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.getLang().getMessage("lockWarning"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity().hasMetadata(RedSync.LOCK_KEY))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if(e.getPlayer().hasMetadata(RedSync.LOCK_KEY))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent e) {
        if(e.getPlayer().hasMetadata(RedSync.LOCK_KEY))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent e) {
        if(e.getPlayer().hasMetadata(RedSync.LOCK_KEY))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getWhoClicked().hasMetadata(RedSync.LOCK_KEY))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if(e.getPlayer().hasMetadata(RedSync.LOCK_KEY))
            e.setCancelled(true);
    }

}
