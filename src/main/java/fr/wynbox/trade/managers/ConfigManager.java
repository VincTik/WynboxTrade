package fr.wynbox.trade.managers;

import fr.wynbox.trade.WynboxTrade;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final WynboxTrade plugin;
    @Getter
    private final FileConfiguration config;

    public ConfigManager(WynboxTrade plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public String getMessage(String path) {
        String message = config.getString("messages." + path);
        if (message == null) return "Message not found: " + path;
        return ChatColor.translateAlternateColorCodes('&', 
               config.getString("messages.prefix", "") + message);
    }

    public int getMaxDistance() {
        return config.getInt("trade.max-distance", 10);
    }

    public int getCountdownTime() {
        return config.getInt("trade.countdown-time", 3);
    }

    public int getInventorySize() {
        return config.getInt("trade.inventory-size", 54);
    }

    public boolean isMoneySlotEnabled() {
        return config.getBoolean("trade.enable-money-slot", true);
    }

    public int getMoneySlot() {
        return config.getInt("trade.money-slot", 49);
    }

    public String getGuiTitle() {
        return ChatColor.translateAlternateColorCodes('&', 
               config.getString("gui.title", "&6Trade Menu"));
    }

    public boolean isLoggingEnabled() {
        return config.getBoolean("logging.enabled", true);
    }

    public String getLoggingFormat() {
        return config.getString("logging.format", 
               "[%date%] %player1% traded with %player2% - Items: %items%");
    }
}
