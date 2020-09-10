package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.config.NeoLanguageConfig;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Phoenix616 on 12.02.2015.
 */
public class TemporaryPunishmentEntry extends PunishmentEntry {

    protected long endtime;

    /**
     * An entry for a temporary punishment
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this ban occurred for
     * @param durationString The duration this ban will last for
     */
    public TemporaryPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, String durationString) throws NumberFormatException {
        super(type, punished, issuer, reason);
        endtime = System.currentTimeMillis()/1000 + NeoUtils.getDurationFromString(durationString);
    }

    /**
     * An entry for a temporary punishment
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this ban occurred for
     * @param time The time as a unix timestamp when this ban was executed at
     * @param durationString The duration this punishment will last for
     */
    public TemporaryPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, long time, String durationString) {
        this(type, punished, issuer, reason, "", time, time + NeoUtils.getDurationFromString(durationString));
    }

    /**
     * An entry for a temporary punishment
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this ban occurred for
     * @param comment A comment to the punishment
     * @param time The time as a unix timestamp when this punishment was executed at
     * @param endTime The time as a unix timestamp in seconds this punishment will end
     */
    public TemporaryPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, String comment, long time, long endTime) {
        super(type, punished, issuer, reason, comment, time);
        this.endtime = endTime;
    }

    /**
     * Get the timestamp when this temporary punishment will ends
     * @return The timestamp when this punishment ends as a long in <strong>seconds</strong>
     */
    public long getEndtime() {
        return endtime;
    }

    /**
     * Get a formatted string of when this temporary ban ends
     * @param format The format to use to format the date with. (SimpleDateFormat)
     * @return The timestamp when this ban ends as a formatted string
     */
    public String getEndtime(String format) {
        Date date = new Date(getEndtime() * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        return sdf.format(date);
    }

    /**
     * Get if this temporary ban is still active
     * @return If the ban is after (true) or before (false) its endtime
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > getEndtime() * 1000;
    }

    /**
     * Get the duration this temporary ban will still last as a formatted string
     * @return The formatted duration string
     */
    public String getFormattedDuration() {
        return getFormattedDuration(null);
    }

    /**
     * Get the duration this temporary ban will still last as a formatted string
     * @param lang The language config to use for formatting, if it is null it will use a short, english format
     * @return The formatted duration string
     */
    public String getFormattedDuration(NeoLanguageConfig lang) {
        long duration = (endtime - System.currentTimeMillis() / 1000);
        return NeoUtils.formatDuration(duration, lang);
    }

}
