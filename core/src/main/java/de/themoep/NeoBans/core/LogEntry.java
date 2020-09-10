package de.themoep.NeoBans.core;

import java.util.UUID;

public class LogEntry extends Entry {
    private UUID playerId;
    private UUID issuerId;

    public LogEntry(EntryType type, UUID playerId, UUID issuerId, String reason, long time) {
        super(type, reason, time);
        this.playerId = playerId;
        this.issuerId = issuerId;
    }

    /**
     * Get the UUID of the affected player
     *
     * @return The UUID of the affected player
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Get the UUID of the player that issued the entry
     *
     * @return The UUID of the player issuing the entry
     */
    public UUID getIssuerId() {
        return issuerId;
    }
}
