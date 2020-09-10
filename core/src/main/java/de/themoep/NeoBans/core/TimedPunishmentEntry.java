package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.config.NeoLanguageConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Max on 28.04.2017.
 */
public class TimedPunishmentEntry extends TemporaryPunishmentEntry {

    /**
     * An entry for a timed punishment (like a jail)
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this punishment occurred for
     * @param durationString The duration this punishment will last for
     */
    public TimedPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, String durationString) {
        super(type, punished, issuer, reason, durationString, System.currentTimeMillis() / 1000, NeoUtils.getDurationFromString(durationString));
    }

    public TimedPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, long time, long duration) {
        super(type, punished, issuer, reason, NeoUtils.formatDuration(duration), time, duration);
    }

    public TimedPunishmentEntry(EntryType type, UUID punished, UUID issuer, String value, String comment, long time, long duration) {
        super(type, punished, issuer, value, comment, time, duration);
    }

    /**
     * Get if this punishment is still active
     * @return Whether or not the punishment duration is 0 or under
     */
    public boolean isExpired() {
        return getDuration() <= 0;
    }

    /**
     * Get the duration this temporary punishment will still last as a formatted string
     * @return The formatted duration string
     */
    public String getFormattedDuration() {
        return getFormattedDuration(null);
    }

    /**
     * Get the duration this temporary punishment will still last as a formatted string
     * @param lang The language config to use for formatting, if it is null it will use a short, english format
     * @return The formatted duration string
     */
    public String getFormattedDuration(NeoLanguageConfig lang) {
        return NeoUtils.formatDuration(getDuration(), lang);
    }

    public long getEndtime() {
        return System.currentTimeMillis() / 1000 + getDuration();
    }

    /**
     * Get the duration of this punishment
     * @return  The duration of this punishment in seconds
     */
    public long getDuration() {
        return endtime;
    }

    /**
     * Set the duration of this punishment
     * @param duration  The amount of seconds this punishment should last for
     */
    public void setDuration(long duration) {
        this.endtime = duration;
    }
}
