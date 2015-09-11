package de.lenic.redsync.managers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.lenic.redsync.RedSync;
import de.lenic.redsync.RedSyncConfig;
import de.lenic.redsync.listeners.LockListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MessagingManager implements PluginMessageListener {

    private RedSync plugin;

    // Messages
    private final String SAVING_COMPLETE = "savingComplete";

    // Constructor
    public MessagingManager(RedSync plugin){
        this.plugin = plugin;
    }


    // Send saving complete message
    public void sendSavingCompleted(Player p) {
        if(!RedSyncConfig.isPluginMessaging())
            return;

        try {
            final ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ONLINE");
            out.writeUTF(this.SAVING_COMPLETE);

            final ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            final DataOutputStream msgout = new DataOutputStream(msgbytes);
            msgout.writeUTF(p.getUniqueId().toString());

            out.writeShort(msgbytes.toByteArray().length);
            out.write(msgbytes.toByteArray());

            p.sendPluginMessage(this.plugin, "BungeeCord", out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Message listener
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(!RedSyncConfig.isPluginMessaging() || !channel.equals("BungeeCord"))
            return;

        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        final String subchannel = in.readUTF();
        if(subchannel.equals(this.SAVING_COMPLETE)) {
            final UUID uuid = UUID.fromString(in.readUTF());
            final Player online = this.plugin.getServer().getPlayer(uuid);

            // Check if player is online
            if(online == null)
                return;

            // Load and apply player data
            this.plugin.getRedis().loadPlayer(online);

            // Unlock player
            synchronized (online) {
                online.removeMetadata(LockListener.lockedMeta, this.plugin);
            }
        }
    }
}
