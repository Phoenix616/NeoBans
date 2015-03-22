package de.themoep.NeoBans.core;

/**
 * The type of an entry in the NeoBans database<br />
 * <br />
 * BAN - A permanent ban of a player<br />
 * TEMPBAN - A temporary ban of a player<br />
 * KICK - A kick of a player<br />
 * UNBAN - A banned player got allowed back on<br />
 * COMMAND - A comment on a player<br />
 */
public enum EntryType {
    BAN,
    TEMPBAN,
    KICK,
    UNBAN,
    COMMENT,
    FAILURE;
}
