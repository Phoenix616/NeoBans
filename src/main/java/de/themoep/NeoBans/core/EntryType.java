package de.themoep.NeoBans.core;

/**
 * The type of an entry in the NeoBans database<br />
 * <br />
 * BAN - A permanent ban of a player<br />
 * TEMPBAN - A temporary ban of a player<br />
 * KICK - A kick of a player<br />
 * UNBAN - A punished player got allowed back on<br />
 * COMMAND - A comment on a player<br />
 */
public enum EntryType {
    REMOVED,
    UNBAN,
    BAN(UNBAN),
    TEMPBAN(UNBAN),
    UNJAIL,
    JAIL(UNJAIL),
    KICK,
    EDITBAN,
    COMMENT,
    FAILURE;

    private EntryType removeType;

    EntryType(EntryType removeType) {
        this.removeType = removeType;
    }

    EntryType() {
        removeType = null;
    }

    /**
     * Get the type that the entry should be when an entry of this type is removed
     * @return  The type
     */
    public EntryType getRemoveType() {
        return removeType != null ? removeType : REMOVED;
    }
}
