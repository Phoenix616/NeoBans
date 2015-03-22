package de.themoep.NeoBans.core;

import java.util.UUID;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public class Entry {

    protected EntryType type;
    
    protected long time;

    protected String reason;

    Entry(EntryType type, String reason) {
        this.type = type;
        this.reason = reason;
        this.time = (int) (System.currentTimeMillis() / 1000);
    }

    public Entry(EntryType type, String reason, long time) {
        this.type = type;
        this.reason = reason;
        this.time = time;
    }

    /**
     * Get the type of the entry
     * @return The enum
     */
    public EntryType getType() {        
        return type;
    };

    /**
     * Get the time the entry was issued.
     * @return The timestamp of when the entry was issued as a long in <strong>seconds</strong>
     */
    public long getTime() {        
        return time;
    };

    /**
     * Get the reason for the ban 
     * @return The ban reason as a string
     */
    public String getReason() {
        return reason;
    }

}
