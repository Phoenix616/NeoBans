package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.config.NeoLanguageConfig;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Phoenix616 on 12.02.2015.
 */
public class TimedPunishmentEntry extends PunishmentEntry {

    protected long endtime;

    /**
     * An entry for a temporary punishment
     * @param type The type of this entry
     * @param punished The uuid of the punished player
     * @param issuer The uuid of the issuer of the punishment
     * @param reason The reason this ban occurred for
     * @param durationString The duration this ban will last for
     */
    public TimedPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, String durationString) throws NumberFormatException {
        super(type, punished, issuer, reason);
        endtime = System.currentTimeMillis()/1000 + getDurationFromString(durationString);
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
    public TimedPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, long time, String durationString) {
        super(type, punished, issuer, reason, "", time);
        this.endtime = time + getDurationFromString(durationString);
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
    public TimedPunishmentEntry(EntryType type, UUID punished, UUID issuer, String reason, String comment, long time, long endTime) {
        super(type, punished, issuer, reason, comment, time);
        this.endtime = endTime;
    }

    private long getDurationFromString(String s) throws NumberFormatException {
        String[] values = s.split("[a-zA-Z]+");
        if(values.length == 0)
            throw new NumberFormatException("You didn't input a time!");
        String[] units = s.split("[0-9]+");

        long duration = 0L;        

        for(int i = 0; i < values.length; i++) {
            String unit = units[i + 1].toLowerCase();
            if(unit.equals("s"))
                duration = duration + Long.parseLong(values[i]);
            else if(unit.equals("m"))
                duration = duration + TimeUnit.MINUTES.toSeconds(Long.parseLong(values[i]));
            else if(unit.equals("h"))
                duration = duration + TimeUnit.HOURS.toSeconds(Long.parseLong(values[i]));
            else if(unit.equals("d"))
                duration = duration + TimeUnit.DAYS.toSeconds(Long.parseLong(values[i]));
            else if(unit.equals("w"))
                duration = duration + 7 * TimeUnit.DAYS.toSeconds(Long.parseLong(values[i]));
            else if(unit.equals("mo"))
                duration = (long) (duration + 29.53 * TimeUnit.DAYS.toSeconds(Long.parseLong(values[i])));
            else if(unit.equals("y"))
                duration = (long) (duration + 365.2425 * TimeUnit.DAYS.toSeconds(Long.parseLong(values[i])));
            else
                throw new NumberFormatException("You inputted a time unit which is not available!");
        }

        return duration;
    }

    /**
     * Get the timestamp when this temporary ban ends
     * @return The timestamp when this ban ends as a long in <strong>seconds</strong>
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
     * @param lang The language config to use for formatting
     * @return The formatted duration string
     */
    public String getFormattedDuration(NeoLanguageConfig lang, boolean shortformat) {

        long duration = (endtime - System.currentTimeMillis() / 1000);

        int seconds = (int) (duration % 60);
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(duration) % 60);
        int hours = (int) (TimeUnit.SECONDS.toHours(duration) % 24);
        int days = (int) (TimeUnit.SECONDS.toDays(duration) % 30);
        int months = (int) ((TimeUnit.SECONDS.toDays(duration) / 30) % 365);
        int years = (int) (TimeUnit.SECONDS.toDays(duration) / 30 / 365);

        List<String> stringList = new ArrayList<String>();

        if(years > 0)
            if(years == 1)
                stringList.add(years + ((shortformat) ? "y" : " " + lang.getTranslation("time.year")));
            else
                stringList.add(years + ((shortformat) ? "y" : " " + lang.getTranslation("time.years")));

        if(months > 0)
            if(months == 1)
                stringList.add(months + ((shortformat) ? "mo" : " " + lang.getTranslation("time.month")));
            else
                stringList.add(months + ((shortformat) ? "mo" : " " + lang.getTranslation("time.months")));

        if(days > 0)
            if(days == 1)
                stringList.add(days + ((shortformat) ? "d" : " " + lang.getTranslation("time.day")));
            else
                stringList.add(days + ((shortformat) ? "d" : " " + lang.getTranslation("time.days")));

        if(hours > 0)
            if(hours == 1)
                stringList.add(hours + ((shortformat) ? "h" : " " + lang.getTranslation("time.hour")));
            else
                stringList.add(hours + ((shortformat) ? "h" : " " + lang.getTranslation("time.hours")));

        if(minutes > 0)
            if(minutes == 1)
                stringList.add(minutes + ((shortformat) ? "m" : " " + lang.getTranslation("time.minute")));
            else
                stringList.add(minutes + ((shortformat) ? "m" : " " + lang.getTranslation("time.minutes")));

        if(seconds > 0)
            if(seconds == 1)
                stringList.add(seconds + ((shortformat) ? "s" : " " + lang.getTranslation("time.second")));
            else
                stringList.add(seconds + ((shortformat) ? "s" : " " + lang.getTranslation("time.seconds")));

        String ft = "";
        for(int i = 0; i < stringList.size(); i++) {
            if(i != 0)
                ft += " ";
            ft += stringList.get(i);
        }
        return ft;
    }

}
