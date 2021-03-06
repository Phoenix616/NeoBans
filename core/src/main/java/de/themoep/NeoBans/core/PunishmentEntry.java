package de.themoep.NeoBans.core;

import java.util.UUID;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public class PunishmentEntry extends Entry {

    protected UUID punished;

    protected UUID issuer;

    protected String comment;
    
    protected int dbId = -1;

    /**
     * An entry for a punishment
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this punishment occurred for
     */
    public PunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason) {
        super(type, reason);
        this.punished = punished;
        this.issuer = issuer;
        this.comment = "";
    }

    /**
     * An entry for a punishment
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this punishment occurred for
     */
    public PunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, String comment, long time) {
        super(type, reason, time);
        this.punished = punished;
        this.issuer = issuer;
        this.reason = reason;
        this.comment = comment;
    }

    /**
     * Get the UUID of the affected player
     * @return The UUID of the affected player
     */
    public UUID getPunished() {
        return punished;
    };

    /**
     * Get the UUID of the player that issued the entry
     * @return The UUID of the player issuing the entry
     */
    public UUID getIssuer() {
        return issuer;
    };


    /**
     * Get the comment to this ban
     * @return The ban comment as a string
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set the ban id. This should get automatically generated by the database
     * @param dbId The auto incremented database table entry id; null if no database or not yet assigned
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public int getDbId() {
        return dbId;
    }
}
