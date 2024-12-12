package fr.wynbox.trade.listeners;

import fr.wynbox.trade.WynboxTrade;
import fr.wynbox.trade.gui.TradeGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class TradeListener implements Listener {

    private final WynboxTrade plugin;

    public TradeListener(WynboxTrade plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Vérifier si le joueur est en trade
        if (!plugin.getTradeManager().isInTrade(player.getUniqueId())) return;

        TradeGUI gui = plugin.getGuiManager().getTradeGUI(player.getUniqueId());
        if (gui == null) return;

        int slot = event.getRawSlot();
        
        // Si le clic est dans l'inventaire du haut (GUI de trade)
        if (slot < event.getView().getTopInventory().getSize()) {
            // Annuler par défaut, on réactivera si nécessaire
            event.setCancelled(true);

            // Gérer les boutons spéciaux
            if (slot == plugin.getConfigManager().getConfig().getInt("gui.accept-slot") ||
                slot == plugin.getConfigManager().getConfig().getInt("gui.decline-slot") ||
                slot == plugin.getConfigManager().getConfig().getInt("gui.money-slot")) {
                gui.handleClick(player, slot);
                return;
            }

            // Vérifier si c'est un slot de trade valide pour le joueur
            if (isValidTradeSlot(slot, player)) {
                // Autoriser le placement/retrait d'items
                event.setCancelled(false);
                // Mettre à jour l'interface après le clic
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    gui.updateSlot(player, slot, event.getCurrentItem());
                });
            }
        }
        // Si le clic est dans l'inventaire du bas (inventaire du joueur)
        else if (event.isShiftClick() && event.getCurrentItem() != null) {
            // Empêcher le shift-click depuis l'inventaire du joueur
            event.setCancelled(true);
        }
    }

    private boolean isValidTradeSlot(int slot, Player player) {
        int startSlot = plugin.getConfigManager().getConfig().getInt(
            isFirstPlayer(player) ? "gui.player1-start-slot" : "gui.player2-start-slot"
        );
        int endSlot = plugin.getConfigManager().getConfig().getInt(
            isFirstPlayer(player) ? "gui.player1-end-slot" : "gui.player2-end-slot"
        );
        return slot >= startSlot && slot <= endSlot;
    }

    private boolean isFirstPlayer(Player player) {
        TradeGUI gui = plugin.getGuiManager().getTradeGUI(player.getUniqueId());
        return gui != null && gui.isFirstPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!plugin.getTradeManager().isInTrade(player.getUniqueId())) return;

        // Annuler le drag dans l'inventaire de trade
        for (int slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        if (plugin.getTradeManager().isInTrade(player.getUniqueId())) {
            plugin.getTradeManager().cancelTrade(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getTradeManager().isInTrade(event.getPlayer().getUniqueId())) {
            plugin.getTradeManager().cancelTrade(event.getPlayer().getUniqueId());
        }
    }
}
