package de.themoep.NeoBans.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Phoenix616 on 11.02.2015.
 */
public class Entry {

    protected EntryType type;

    protected long time;

    protected String reason;

    /**
     * An entry
     * @param type The type of this entry
     * @param reason The reason for this entry
     */
    public Entry(EntryType type, String reason) {
        this.type = type;
        this.reason = reason;
        this.time = (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * An entry
     * @param type The type of this entry
     * @param reason The reason for this entry
     * @param time The time that this entry occured
     */
    public Entry(EntryType type, String reason, long time) {
        this.type = type;
        this.reason = reason;
        this.time = time;
    }

    /**
     * Get the type of the entry
     *
     * @return The enum
     */
    public EntryType getType() {
        return type;
    }

    ;

    /**
     * Get the time the entry was issued.
     *
     * @return The timestamp of when the entry was issued as a long in <strong>seconds</strong>
     */
    public long getTime() {
        return time;
    }

    /**
     * Get a formatted string of when this entry was issued
     *
     * @param format The format to use to format the date with. (SimpleDateFormat)
     * @return The timestamp when this entry was issued as a formatted string
     */
    public String getTime(String format) {
        Date date = new Date(getTime() * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        return sdf.format(date);
    }

    /**
     * Get the reason for the entry
     * @return The entry reason as a string
     */
    public String getReason() {
        return reason;
    }

}
