package fr.wynbox.trade;

import fr.wynbox.trade.commands.TradeAcceptCommand;
import fr.wynbox.trade.commands.TradeDenyCommand;
import fr.wynbox.trade.commands.TradeCommand;
import fr.wynbox.trade.listeners.TradeListener;
import fr.wynbox.trade.managers.ConfigManager;
import fr.wynbox.trade.managers.GUIManager;
import fr.wynbox.trade.managers.TradeManager;
import fr.wynbox.trade.utils.EconomyManager;
import fr.wynbox.trade.utils.TradeLogger;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class WynboxTrade extends JavaPlugin {

    private ConfigManager configManager;
    private TradeManager tradeManager;
    private GUIManager guiManager;
    private EconomyManager economyManager;
    private TradeLogger tradeLogger;

    @Override
    public void onEnable() {
        // Initialisation des managers
        this.configManager = new ConfigManager(this);
        this.tradeManager = new TradeManager(this);
        this.guiManager = new GUIManager(this);
        this.economyManager = new EconomyManager(this);
        this.tradeLogger = new TradeLogger(this);

        // Enregistrement des commandes
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("tradeaccept").setExecutor(new TradeAcceptCommand(this));
        getCommand("tradedeny").setExecutor(new TradeDenyCommand(this));

        // Enregistrement des listeners
        getServer().getPluginManager().registerEvents(new TradeListener(this), this);

        // Message de démarrage
        getLogger().info("WynboxTrade a été activé avec succès !");
    }

    @Override
    public void onDisable() {
        // Annuler tous les trades en cours
        if (tradeManager != null) {
            tradeManager.cancelAllTrades();
        }

        // Message d'arrêt
        getLogger().info("WynboxTrade a été désactivé !");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public TradeLogger getTradeLogger() {
        return tradeLogger;
    }
}
