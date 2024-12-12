package fr.wynbox.trade.gui;

import fr.wynbox.trade.WynboxTrade;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class TradeGUI {
    private final WynboxTrade plugin;
    private final UUID player1;
    private final UUID player2;
    private final Inventory inventory;
    private final Map<Integer, ItemStack> player1Items;
    private final Map<Integer, ItemStack> player2Items;
    private boolean player1Accepted;
    private boolean player2Accepted;
    private double player1Money;
    private double player2Money;
    private int countdownTaskId;
    private final FileConfiguration config;
    private boolean player1Ready;
    private boolean player2Ready;

    public TradeGUI(WynboxTrade plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1 = player1.getUniqueId();
        this.player2 = player2.getUniqueId();
        this.config = plugin.getConfigManager().getConfig();
        this.inventory = Bukkit.createInventory(null, 
            config.getInt("gui.size", 54), 
            config.getString("gui.title").replace("%player%", player2.getName()));
        this.player1Items = new HashMap<>();
        this.player2Items = new HashMap<>();
        this.player1Accepted = false;
        this.player2Accepted = false;
        this.player1Money = 0;
        this.player2Money = 0;
        this.countdownTaskId = -1;
        this.player1Ready = false;
        this.player2Ready = false;

        initializeGUI();
    }

    private void initializeGUI() {
        // Séparateurs
        Material separatorMaterial = Material.valueOf(config.getString("gui.separator-material", "BLACK_STAINED_GLASS_PANE"));
        ItemStack separator = createItem(separatorMaterial, " ");
        for (int i = 18; i <= 26; i++) {
            inventory.setItem(i, separator);
        }

        // Bouton d'acceptation
        ItemStack acceptButton = createItem(
            Material.valueOf(config.getString("buttons.accept.material", "LIME_WOOL")),
            config.getString("buttons.accept.name", "§aAccepter"),
            config.getStringList("buttons.accept.lore")
        );
        inventory.setItem(config.getInt("gui.accept-slot", 45), acceptButton);

        // Bouton de refus
        ItemStack declineButton = createItem(
            Material.valueOf(config.getString("buttons.decline.material", "RED_WOOL")),
            config.getString("buttons.decline.name", "§cRefuser"),
            config.getStringList("buttons.decline.lore")
        );
        inventory.setItem(config.getInt("gui.decline-slot", 53), declineButton);

        // Slot d'argent si activé
        if (config.getBoolean("gui.money-enabled", true)) {
            ItemStack moneyItem = createItem(
                Material.valueOf(config.getString("buttons.money.material", "GOLD_INGOT")),
                config.getString("buttons.money.name", "§6Argent: §e0"),
                config.getStringList("buttons.money.lore")
            );
            inventory.setItem(config.getInt("gui.money-slot", 49), moneyItem);
        }

        // Boutons de statut
        ItemStack player1Status = new ItemStack(player1Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta player1Meta = player1Status.getItemMeta();
        player1Meta.setDisplayName(config.getString(player1Ready ? "gui.ready-status" : "gui.not-ready-status"));
        player1Status.setItemMeta(player1Meta);
        inventory.setItem(config.getInt("gui.player1-status-slot"), player1Status);

        ItemStack player2Status = new ItemStack(player2Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta player2Meta = player2Status.getItemMeta();
        player2Meta.setDisplayName(config.getString(player2Ready ? "gui.ready-status" : "gui.not-ready-status"));
        player2Status.setItemMeta(player2Meta);
        inventory.setItem(config.getInt("gui.player2-status-slot"), player2Status);
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, new ArrayList<>());
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (!checkDistance(p1, p2)) {
            p1.sendMessage(config.getString("messages.too-far"));
            p2.sendMessage(config.getString("messages.too-far"));
            return;
        }
        
        if (p1 != null) p1.openInventory(inventory);
        if (p2 != null) p2.openInventory(inventory);
    }

    private boolean checkDistance(Player p1, Player p2) {
        if (p1 == null || p2 == null) return false;
        if (p1.hasPermission("wynbox.trade.bypass.distance")) return true;
        
        double maxDistance = config.getDouble("trade.max-distance", 10.0);
        return p1.getLocation().distance(p2.getLocation()) <= maxDistance;
    }

    public void handleClick(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (slot == config.getInt("gui.accept-slot")) {
            handleAcceptClick(player);
        } else if (slot == config.getInt("gui.decline-slot")) {
            cancelTrade();
        } else if (slot == config.getInt("gui.money-slot") && config.getBoolean("gui.money-enabled")) {
            handleMoneyClick(player);
        }
    }

    private void handleAcceptClick(Player player) {
        if (player.getUniqueId().equals(player1)) {
            player1Accepted = !player1Accepted;
            updateAcceptButton(player1Accepted, config.getInt("gui.accept-slot"));
        } else if (player.getUniqueId().equals(player2)) {
            player2Accepted = !player2Accepted;
            updateAcceptButton(player2Accepted, config.getInt("gui.accept-slot"));
        }

        if (player1Accepted && player2Accepted) {
            startCountdown();
        }
    }

    private void handleMoneyClick(Player player) {
        // Cette méthode sera implémentée pour gérer l'interface de saisie du montant
        // Pour l'instant, on peut utiliser un montant fixe pour tester
        if (player.getUniqueId().equals(player1)) {
            player1Money = 100; // À remplacer par une interface de saisie
            updateMoneyDisplay();
        } else if (player.getUniqueId().equals(player2)) {
            player2Money = 100; // À remplacer par une interface de saisie
            updateMoneyDisplay();
        }
    }

    private void updateMoneyDisplay() {
        if (!config.getBoolean("gui.money-enabled")) return;

        int moneySlot = config.getInt("gui.money-slot", 49);
        ItemStack moneyItem = inventory.getItem(moneySlot);
        if (moneyItem != null) {
            ItemMeta meta = moneyItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(String.format("§6Argent: §e%.2f", player1Money + player2Money));
                moneyItem.setItemMeta(meta);
            }
        }
    }

    private void startCountdown() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
        }

        int duration = config.getInt("trade.countdown.duration", 5);
        Sound countdownSound = Sound.valueOf(config.getString("trade.countdown.sound", "BLOCK_NOTE_BLOCK_PLING"));
        float volume = (float) config.getDouble("trade.countdown.volume", 1.0);
        float pitch = (float) config.getDouble("trade.countdown.pitch", 1.0);

        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int timeLeft = duration;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    completeTrade();
                    Bukkit.getScheduler().cancelTask(countdownTaskId);
                    return;
                }

                Player p1 = Bukkit.getPlayer(player1);
                Player p2 = Bukkit.getPlayer(player2);

                if (p1 != null) {
                    p1.playSound(p1.getLocation(), countdownSound, volume, pitch);
                    p1.sendMessage(config.getString("messages.countdown")
                            .replace("%time%", String.valueOf(timeLeft)));
                }
                if (p2 != null) {
                    p2.playSound(p2.getLocation(), countdownSound, volume, pitch);
                    p2.sendMessage(config.getString("messages.countdown")
                            .replace("%time%", String.valueOf(timeLeft)));
                }

                timeLeft--;
            }
        }, 0L, 20L);
    }

    private void completeTrade() {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null && p2 != null) {
            // Échange des items
            for (Map.Entry<Integer, ItemStack> entry : player1Items.entrySet()) {
                if (entry.getValue() != null) {
                    p2.getInventory().addItem(entry.getValue().clone());
                    p1.getInventory().removeItem(entry.getValue());
                }
            }

            for (Map.Entry<Integer, ItemStack> entry : player2Items.entrySet()) {
                if (entry.getValue() != null) {
                    p1.getInventory().addItem(entry.getValue().clone());
                    p2.getInventory().removeItem(entry.getValue());
                }
            }

            // Échange d'argent si activé
            if (config.getBoolean("gui.money-enabled")) {
                plugin.getEconomyManager().transferMoney(p1, p2, player1Money);
                plugin.getEconomyManager().transferMoney(p2, p1, player2Money);
            }

            // Jouer le son de succès
            Sound completeSound = Sound.valueOf(config.getString("trade.complete-sound.sound", "ENTITY_PLAYER_LEVELUP"));
            float volume = (float) config.getDouble("trade.complete-sound.volume", 1.0);
            float pitch = (float) config.getDouble("trade.complete-sound.pitch", 1.0);

            p1.playSound(p1.getLocation(), completeSound, volume, pitch);
            p2.playSound(p2.getLocation(), completeSound, volume, pitch);

            // Logger le trade
            plugin.getTradeLogger().logTrade(p1, p2, player1Items, player2Items, player1Money + player2Money);

            // Envoyer les messages
            String message = config.getString("messages.trade-completed");
            p1.sendMessage(message);
            p2.sendMessage(message);
        }

        // Fermer les inventaires
        if (p1 != null) p1.closeInventory();
        if (p2 != null) p2.closeInventory();
    }

    private void updateAcceptButton(boolean accepted, int slot) {
        ItemStack button = inventory.getItem(slot);
        if (button != null) {
            Material material = accepted ? 
                Material.valueOf(config.getString("buttons.accept.accepted-material", "EMERALD_BLOCK")) :
                Material.valueOf(config.getString("buttons.accept.material", "LIME_WOOL"));
            String name = accepted ?
                config.getString("buttons.accept.accepted-name", "§aAccepté !") :
                config.getString("buttons.accept.name", "§aAccepter");

            button.setType(material);
            ItemMeta meta = button.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                button.setItemMeta(meta);
            }
        }
    }

    private void cancelTrade() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
        }

        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null) {
            p1.sendMessage(config.getString("messages.trade-cancelled"));
            p1.closeInventory();
        }
        if (p2 != null) {
            p2.sendMessage(config.getString("messages.trade-cancelled"));
            p2.closeInventory();
        }

        // Annuler le trade dans le TradeManager
        plugin.getTradeManager().cancelTrade(player1);
    }

    public void cancel() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
        }

        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 != null) {
            p1.sendMessage(config.getString("messages.trade-cancelled"));
            p1.closeInventory();
        }
        if (p2 != null) {
            p2.sendMessage(config.getString("messages.trade-cancelled"));
            p2.closeInventory();
        }
    }

    public void updateSlot(Player player, int slot, ItemStack item) {
        if (!isValidSlot(player, slot)) return;

        if (player.getUniqueId().equals(player1)) {
            if (item == null) {
                player1Items.remove(slot);
            } else {
                player1Items.put(slot, item.clone());
            }
        } else if (player.getUniqueId().equals(player2)) {
            if (item == null) {
                player2Items.remove(slot);
            } else {
                player2Items.put(slot, item.clone());
            }
        }

        // Reset les acceptations
        player1Accepted = false;
        player2Accepted = false;
        updateAcceptButton(false, config.getInt("gui.accept-slot"));

        // Mettre à jour l'inventaire
        inventory.setItem(slot, item);
    }

    private boolean isValidSlot(Player player, int slot) {
        String player1Slots = config.getString("gui.player1-slots", "0-17");
        String player2Slots = config.getString("gui.player2-slots", "27-44");

        String[] p1Range = player1Slots.split("-");
        String[] p2Range = player2Slots.split("-");

        int p1Start = Integer.parseInt(p1Range[0]);
        int p1End = Integer.parseInt(p1Range[1]);
        int p2Start = Integer.parseInt(p2Range[0]);
        int p2End = Integer.parseInt(p2Range[1]);

        if (player.getUniqueId().equals(player1)) {
            return slot >= p1Start && slot <= p1End;
        } else if (player.getUniqueId().equals(player2)) {
            return slot >= p2Start && slot <= p2End;
        }

        return false;
    }

    public boolean isOtherPlayerSlot(int slot, Player player) {
        if (player.getUniqueId().equals(player1)) {
            return slot >= config.getInt("gui.player2-start-slot") && slot < config.getInt("gui.player2-start-slot") + config.getInt("gui.player-slots");
        } else {
            return slot >= config.getInt("gui.player1-start-slot") && slot < config.getInt("gui.player1-start-slot") + config.getInt("gui.player-slots");
        }
    }

    public boolean isPlayerSlot(int slot, Player player) {
        if (player.getUniqueId().equals(player1)) {
            return slot >= config.getInt("gui.player1-start-slot") && slot < config.getInt("gui.player1-start-slot") + config.getInt("gui.player-slots");
        } else {
            return slot >= config.getInt("gui.player2-start-slot") && slot < config.getInt("gui.player2-start-slot") + config.getInt("gui.player-slots");
        }
    }

    public void removeItem(Player player, int slot) {
        if (isPlayerSlot(slot, player)) {
            inventory.setItem(slot, null);
            updateTradeStatus(player, false);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void updateTradeStatus(Player player, boolean status) {
        if (player.getUniqueId().equals(player1)) {
            player1Ready = status;
        } else {
            player2Ready = status;
        }

        // Mettre à jour les boutons de statut
        updateStatusButtons();

        // Si les deux joueurs sont prêts, démarrer le compte à rebours
        if (player1Ready && player2Ready) {
            startCountdown();
        }
    }

    private void updateStatusButtons() {
        // Mettre à jour le bouton de statut du joueur 1
        ItemStack player1Status = new ItemStack(player1Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta player1Meta = player1Status.getItemMeta();
        player1Meta.setDisplayName(config.getString(player1Ready ? "gui.ready-status" : "gui.not-ready-status"));
        player1Status.setItemMeta(player1Meta);
        inventory.setItem(config.getInt("gui.player1-status-slot"), player1Status);

        // Mettre à jour le bouton de statut du joueur 2
        ItemStack player2Status = new ItemStack(player2Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta player2Meta = player2Status.getItemMeta();
        player2Meta.setDisplayName(config.getString(player2Ready ? "gui.ready-status" : "gui.not-ready-status"));
        player2Status.setItemMeta(player2Meta);
        inventory.setItem(config.getInt("gui.player2-status-slot"), player2Status);
    }

    public boolean isFirstPlayer(UUID playerId) {
        return playerId.equals(player1);
    }
}
