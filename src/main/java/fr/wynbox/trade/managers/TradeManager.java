package fr.wynbox.trade.managers;

import fr.wynbox.trade.WynboxTrade;
import fr.wynbox.trade.utils.TradeRequest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.*;

public class TradeManager {

    private final WynboxTrade plugin;
    @Getter
    private final Map<UUID, TradeRequest> activeRequests;
    @Getter
    private final Map<UUID, UUID> activeTrades;
    private final Map<UUID, BukkitTask> countdownTasks;
    private final SimpleDateFormat dateFormat;

    public TradeManager(WynboxTrade plugin) {
        this.plugin = plugin;
        this.activeRequests = new HashMap<>();
        this.activeTrades = new HashMap<>();
        this.countdownTasks = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void sendTradeRequest(Player sender, Player target) {
        if (!canTrade(sender, target)) return;

        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        // Cancel any existing requests
        cancelRequest(senderUUID);

        // Create new request
        TradeRequest request = new TradeRequest(sender, target);
        activeRequests.put(senderUUID, request);

        // Send messages
        sender.sendMessage(plugin.getConfigManager().getMessage("trade-request-sent")
                .replace("%player%", target.getName()));
        target.sendMessage(plugin.getConfigManager().getMessage("trade-request-received")
                .replace("%player%", sender.getName()));

        // Expire request after 30 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, 
            () -> cancelRequest(senderUUID), 20L * 30);
    }

    public void acceptTradeRequest(Player player, Player requester) {
        if (requester == null) {
            // Si aucun joueur n'est spécifié, chercher une requête active
            TradeRequest pendingRequest = findPendingRequest(player.getUniqueId());
            if (pendingRequest == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-pending-request"));
                return;
            }
            requester = pendingRequest.getSender();
        }

        UUID requesterUUID = requester.getUniqueId();
        TradeRequest request = activeRequests.get(requesterUUID);

        if (request == null || !request.getTarget().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-request-from-player")
                    .replace("%player%", requester.getName()));
            return;
        }

        // Supprimer la requête
        activeRequests.remove(requesterUUID);

        // Ouvrir l'interface de trade pour les deux joueurs
        plugin.getGuiManager().openTradeGUI(requester, player);
        plugin.getGuiManager().openTradeGUI(player, requester);

        // Enregistrer le trade actif
        activeTrades.put(requesterUUID, player.getUniqueId());
        activeTrades.put(player.getUniqueId(), requesterUUID);

        // Envoyer les messages
        requester.sendMessage(plugin.getConfigManager().getMessage("trade-accepted")
                .replace("%player%", player.getName()));
        player.sendMessage(plugin.getConfigManager().getMessage("you-accepted-trade")
                .replace("%player%", requester.getName()));
    }

    public void denyTradeRequest(Player player, Player requester) {
        if (requester == null) {
            // Si aucun joueur n'est spécifié, chercher une requête active
            TradeRequest pendingRequest = findPendingRequest(player.getUniqueId());
            if (pendingRequest == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-pending-request"));
                return;
            }
            requester = pendingRequest.getSender();
        }

        UUID requesterUUID = requester.getUniqueId();
        TradeRequest request = activeRequests.get(requesterUUID);

        if (request == null || !request.getTarget().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-request-from-player")
                    .replace("%player%", requester.getName()));
            return;
        }

        // Supprimer la requête
        activeRequests.remove(requesterUUID);

        // Envoyer les messages
        requester.sendMessage(plugin.getConfigManager().getMessage("trade-denied")
                .replace("%player%", player.getName()));
        player.sendMessage(plugin.getConfigManager().getMessage("you-denied-trade")
                .replace("%player%", requester.getName()));
    }

    private TradeRequest findPendingRequest(UUID playerUUID) {
        return activeRequests.values().stream()
                .filter(request -> request.getTarget().getUniqueId().equals(playerUUID))
                .findFirst()
                .orElse(null);
    }

    public boolean canTrade(Player sender, Player target) {
        // Check permissions
        if (!sender.hasPermission("wynbox.trade")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return false;
        }

        // Check if target exists and is online
        if (target == null || !target.isOnline()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return false;
        }

        // Check if trying to trade with self
        if (sender.equals(target)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("cant-trade-yourself"));
            return false;
        }

        // Check distance
        double maxDistance = plugin.getConfigManager().getMaxDistance();
        if (sender.getLocation().distance(target.getLocation()) > maxDistance) {
            sender.sendMessage(plugin.getConfigManager().getMessage("too-far"));
            return false;
        }

        // Check if either player is already in a trade
        if (isInTrade(sender.getUniqueId()) || isInTrade(target.getUniqueId())) {
            sender.sendMessage(plugin.getConfigManager().getMessage("already-trading"));
            return false;
        }

        return true;
    }

    public void acceptRequest(Player acceptor, Player requester) {
        UUID requesterUUID = requester.getUniqueId();
        TradeRequest request = activeRequests.get(requesterUUID);

        if (request == null || request.isExpired()) {
            acceptor.sendMessage(plugin.getConfigManager().getMessage("no-request"));
            return;
        }

        if (!canTrade(acceptor, requester)) {
            return;
        }

        // Start trade
        startTrade(requester, acceptor);
        activeRequests.remove(requesterUUID);
    }

    public void cancelRequest(UUID uuid) {
        TradeRequest request = activeRequests.remove(uuid);
        if (request != null) {
            request.getSender().sendMessage(
                plugin.getConfigManager().getMessage("trade-cancelled"));
            request.getTarget().sendMessage(
                plugin.getConfigManager().getMessage("trade-cancelled"));
        }
    }

    public void startTrade(Player player1, Player player2) {
        UUID player1UUID = player1.getUniqueId();
        UUID player2UUID = player2.getUniqueId();

        activeTrades.put(player1UUID, player2UUID);
        activeTrades.put(player2UUID, player1UUID);

        // Log trade start
        logTrade(player1, player2, "Trade started");

        // Open trade GUI
        plugin.getGuiManager().openTradeGUI(player1, player2);
    }

    public void cancelTrade(UUID uuid) {
        UUID partnerUUID = activeTrades.remove(uuid);
        if (partnerUUID != null) {
            activeTrades.remove(partnerUUID);
            
            Player player1 = plugin.getServer().getPlayer(uuid);
            Player player2 = plugin.getServer().getPlayer(partnerUUID);
            
            if (player1 != null) {
                player1.closeInventory();
                player1.sendMessage(plugin.getConfigManager().getMessage("trade-cancelled"));
            }
            if (player2 != null) {
                player2.closeInventory();
                player2.sendMessage(plugin.getConfigManager().getMessage("trade-cancelled"));
            }

            // Log trade cancellation
            logTrade(player1, player2, "Trade cancelled");
        }
    }

    public void cancelAllTrades() {
        for (UUID uuid : new ArrayList<>(activeTrades.keySet())) {
            cancelTrade(uuid);
        }
    }

    public boolean isInTrade(UUID uuid) {
        return activeTrades.containsKey(uuid);
    }

    public UUID getTradePartner(UUID uuid) {
        return activeTrades.get(uuid);
    }

    private void logTrade(Player player1, Player player2, String action) {
        if (!plugin.getConfigManager().isLoggingEnabled()) return;

        String logMessage = plugin.getConfigManager().getLoggingFormat()
            .replace("%date%", dateFormat.format(new Date()))
            .replace("%player1%", player1 != null ? player1.getName() : "Unknown")
            .replace("%player2%", player2 != null ? player2.getName() : "Unknown")
            .replace("%action%", action);

        plugin.getTradeLogger().info(logMessage);
    }
}
