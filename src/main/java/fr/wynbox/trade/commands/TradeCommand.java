package fr.wynbox.trade.commands;

import fr.wynbox.trade.WynboxTrade;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TradeCommand implements CommandExecutor, TabCompleter {

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

        if (!player.hasPermission("wynbox.trade")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        // Si aucun argument n'est fourni
        if (args.length == 0) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage"));
            return true;
        }

        // Gérer les sous-commandes
        if (args[0].equalsIgnoreCase("accept")) {
            // Commande /trade accept [joueur]
            Player target = args.length > 1 ? plugin.getServer().getPlayer(args[1]) : null;
            plugin.getTradeManager().acceptTradeRequest(player, target);
            return true;
        } else if (args[0].equalsIgnoreCase("deny")) {
            // Commande /trade deny [joueur]
            Player target = args.length > 1 ? plugin.getServer().getPlayer(args[1]) : null;
            plugin.getTradeManager().denyTradeRequest(player, target);
            return true;
        }

        // Si c'est une demande de trade normale (/trade <joueur>)
        Player target = plugin.getServer().getPlayer(args[0]);
        plugin.getTradeManager().sendTradeRequest(player, target);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }

        if (args.length == 1) {
            // Première argument : sous-commandes ou nom de joueur
            completions.add("accept");
            completions.add("deny");
            completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny"))) {
            // Deuxième argument pour accept/deny : nom de joueur
            completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
