package fr.wynbox.trade.utils;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.time.Instant;

@Getter
public class TradeRequest {
    private final Player sender;
    private final Player target;
    private final Instant timestamp;

    public TradeRequest(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
        this.timestamp = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(timestamp.plusSeconds(30));
    }
}
