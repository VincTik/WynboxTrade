package fr.wynbox.trade.commands;

import fr.wynbox.trade.WynboxTrade;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {

    private final WynboxTrade plugin;

    public TradeCommand(WynboxTrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("wynboxtrade.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        plugin.getTradeManager().sendTradeRequest(player, target);

        return true;
    }
}
