package fr.wynbox.trade.utils;

import fr.wynbox.trade.WynboxTrade;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final WynboxTrade plugin;
    private Economy economy;

    public EconomyManager(WynboxTrade plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public boolean hasEnoughMoney(Player player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public boolean transferMoney(Player from, Player to, double amount) {
        if (economy == null || !hasEnoughMoney(from, amount)) {
            return false;
        }

        economy.withdrawPlayer(from, amount);
        economy.depositPlayer(to, amount);
        return true;
    }

    public boolean isEnabled() {
        return economy != null && plugin.getConfigManager().getConfig().getBoolean("gui.money-enabled", true);
    }

    public double getBalance(Player player) {
        return economy != null ? economy.getBalance(player) : 0;
    }
}
