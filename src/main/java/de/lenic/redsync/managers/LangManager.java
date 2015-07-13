package de.lenic.redsync.managers;

import org.bukkit.ChatColor;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class LangManager {

    private ResourceBundle bundle;

    public LangManager(String locale){
        this.bundle = ResourceBundle.getBundle("lang." + locale);
    }

    public String getMessage(String key, Object... replace){
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(this.bundle.getString(key), replace));
    }

}
