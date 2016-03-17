package de.lenic.redsync.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncRedisMessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    private String channel;
    private String message;

    // Constructor
    public AsyncRedisMessageEvent(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }


    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

}
