package fr.wynbox.trade.commands;

import fr.wynbox.trade.WynboxTrade;
import fr.wynbox.trade.utils.TradeRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeDenyCommand implements CommandExecutor {

    private final WynboxTrade plugin;

    public TradeDenyCommand(WynboxTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player denier = (Player) sender;

        if (!denier.hasPermission("wynbox.trade")) {
            denier.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Si un joueur est spécifié, on refuse sa demande spécifique
        if (args.length > 0) {
            Player requester = plugin.getServer().getPlayer(args[0]);
            if (requester == null) {
                denier.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }
            
            TradeRequest request = plugin.getTradeManager().getActiveRequests().get(requester.getUniqueId());
            if (request == null || !request.getTarget().equals(denier)) {
                denier.sendMessage(plugin.getConfigManager().getMessage("no-request-from-player")
                    .replace("%player%", args[0]));
                return true;
            }

            // Envoyer le message de refus
            requester.sendMessage(plugin.getConfigManager().getMessage("trade-declined-by")
                .replace("%player%", denier.getName()));
            denier.sendMessage(plugin.getConfigManager().getMessage("trade-declined-sender")
                .replace("%player%", requester.getName()));

            // Annuler la demande
            plugin.getTradeManager().cancelRequest(requester.getUniqueId());
            return true;
        }

        // Si aucun joueur n'est spécifié, on refuse la dernière demande reçue
        TradeRequest lastRequest = null;
        Player lastRequester = null;

        for (TradeRequest request : plugin.getTradeManager().getActiveRequests().values()) {
            if (request.getTarget().equals(denier) && !request.isExpired()) {
                lastRequest = request;
                lastRequester = request.getSender();
            }
        }

        if (lastRequest == null || lastRequester == null) {
            denier.sendMessage(plugin.getConfigManager().getMessage("no-request"));
            return true;
        }

        // Envoyer le message de refus
        lastRequester.sendMessage(plugin.getConfigManager().getMessage("trade-declined-by")
            .replace("%player%", denier.getName()));
        denier.sendMessage(plugin.getConfigManager().getMessage("trade-declined-sender")
            .replace("%player%", lastRequester.getName()));

        // Annuler la demande
        plugin.getTradeManager().cancelRequest(lastRequester.getUniqueId());
        return true;
    }
}
