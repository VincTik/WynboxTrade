package fr.wynbox.trade.commands;

import fr.wynbox.trade.WynboxTrade;
import fr.wynbox.trade.utils.TradeRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TradeAcceptCommand implements CommandExecutor {

    private final WynboxTrade plugin;

    public TradeAcceptCommand(WynboxTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player acceptor = (Player) sender;

        if (!acceptor.hasPermission("wynboxtrade.use")) {
            acceptor.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Si un joueur est spécifié, on accepte sa demande spécifique
        if (args.length > 0) {
            Player requester = plugin.getServer().getPlayer(args[0]);
            if (requester == null) {
                acceptor.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }
            
            TradeRequest request = plugin.getTradeManager().getActiveRequests().get(requester.getUniqueId());
            if (request == null || !request.getTarget().equals(acceptor)) {
                acceptor.sendMessage(plugin.getConfigManager().getMessage("no-request-from-player")
                    .replace("%player%", args[0]));
                return true;
            }

            plugin.getTradeManager().acceptRequest(acceptor, requester);
            return true;
        }

        // Si aucun joueur n'est spécifié, on accepte la dernière demande reçue
        TradeRequest lastRequest = null;
        Player lastRequester = null;

        for (TradeRequest request : plugin.getTradeManager().getActiveRequests().values()) {
            if (request.getTarget().equals(acceptor) && !request.isExpired()) {
                lastRequest = request;
                lastRequester = request.getSender();
            }
        }

        if (lastRequest == null || lastRequester == null) {
            acceptor.sendMessage(plugin.getConfigManager().getMessage("no-request"));
            return true;
        }

        plugin.getTradeManager().acceptRequest(acceptor, lastRequester);
        return true;
    }
}
