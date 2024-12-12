package fr.wynbox.trade.utils;

import fr.wynbox.trade.WynboxTrade;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TradeLogger {
    private final WynboxTrade plugin;
    private final File logFile;
    private final SimpleDateFormat dateFormat;
    private final Logger logger;

    public TradeLogger(WynboxTrade plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), plugin.getConfigManager().getConfig().getString("logs.file", "trades.log"));
        this.dateFormat = new SimpleDateFormat(plugin.getConfigManager().getConfig().getString("logs.date-format", "dd/MM/yyyy HH:mm:ss"));
        this.logger = plugin.getLogger();
        
        // Créer le dossier si nécessaire
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Créer le fichier si nécessaire
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                logger.severe("Impossible de créer le fichier de logs: " + e.getMessage());
            }
        }
    }

    public void logTrade(Player player1, Player player2, Map<Integer, ItemStack> player1Items, Map<Integer, ItemStack> player2Items, double moneyAmount) {
        if (!plugin.getConfigManager().getConfig().getBoolean("logs.enabled", true)) {
            return;
        }

        String format = plugin.getConfigManager().getConfig().getString("logs.format",
                "[%date%] %player1% a échangé avec %player2% - Items: %items% - Argent: %money%");

        // Formater les items
        String player1ItemsStr = formatItems(player1Items);
        String player2ItemsStr = formatItems(player2Items);
        String itemsStr = String.format("(De %s: %s) (De %s: %s)",
                player1.getName(), player1ItemsStr,
                player2.getName(), player2ItemsStr);

        // Remplacer les placeholders
        String logMessage = format
                .replace("%date%", dateFormat.format(new Date()))
                .replace("%player1%", player1.getName())
                .replace("%player2%", player2.getName())
                .replace("%items%", itemsStr)
                .replace("%money%", String.format("%.2f", moneyAmount));

        // Écrire dans le fichier
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            logger.severe("Erreur lors de l'écriture dans le fichier de logs: " + e.getMessage());
        }

        // Aussi logger dans la console
        info(logMessage);
    }

    public void info(String message) {
        if (plugin.getConfigManager().getConfig().getBoolean("logs.enabled", true)) {
            logger.info(message);
        }
    }

    private String formatItems(Map<Integer, ItemStack> items) {
        return items.values().stream()
                .filter(item -> item != null)
                .map(item -> String.format("%dx %s", item.getAmount(), item.getType().name()))
                .collect(Collectors.joining(", "));
    }
}
