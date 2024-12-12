package fr.wynbox.trade.managers;

import fr.wynbox.trade.WynboxTrade;
import fr.wynbox.trade.gui.TradeGUI;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    private final WynboxTrade plugin;
    private final Map<UUID, TradeGUI> activeGUIs;

    public GUIManager(WynboxTrade plugin) {
        this.plugin = plugin;
        this.activeGUIs = new HashMap<>();
    }

    public void openTradeGUI(Player player1, Player player2) {
        TradeGUI gui = new TradeGUI(plugin, player1, player2);
        activeGUIs.put(player1.getUniqueId(), gui);
        activeGUIs.put(player2.getUniqueId(), gui);
        gui.open();
    }

    public TradeGUI getTradeGUI(UUID playerUUID) {
        return activeGUIs.get(playerUUID);
    }

    public void removeTradeGUI(UUID playerUUID) {
        TradeGUI gui = activeGUIs.remove(playerUUID);
        if (gui != null) {
            gui.cancel();
        }
    }

    public boolean hasActiveGUI(UUID playerUUID) {
        return activeGUIs.containsKey(playerUUID);
    }
}
