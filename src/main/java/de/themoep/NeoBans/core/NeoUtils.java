package de.themoep.NeoBans.core;

import de.themoep.NeoBans.core.config.NeoLanguageConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Max on 28.04.2017.
 */
public class NeoUtils {

    /**
     * Convert a string to seconds
     * @param durationString    The inputted duration, accepts s, m, h, d, w, mo and y
     * @return                  The duration in seconds
     * @throws NumberFormatException    If the inputted values are not real numbers or you added an unsupported time unit
     */
    public static long getDurationFromString(String durationString) throws NumberFormatException {
        String[] values = durationString.split("[a-zA-Z]+");
        if(values.length == 0)
            throw new NumberFormatException("You didn't input a time!");
        String[] units = durationString.split("[0-9]+");

        long duration = 0L;

        for(int i = 0; i < values.length; i++) {
            String unit = units[i + 1].toLowerCase();
            switch (unit) {
                case "s":
                    duration = duration + Long.parseLong(values[i]);
                    break;
                case "m":
                    duration = duration + TimeUnit.MINUTES.toSeconds(Long.parseLong(values[i]));
                    break;
                case "h":
                    duration = duration + TimeUnit.HOURS.toSeconds(Long.parseLong(values[i]));
                    break;
                case "d":
                    duration = duration + TimeUnit.DAYS.toSeconds(Long.parseLong(values[i]));
                    break;
                case "w":
                    duration = duration + 7 * TimeUnit.DAYS.toSeconds(Long.parseLong(values[i]));
                    break;
                case "mo":
                    duration = (long) (duration + 29.53 * TimeUnit.DAYS.toSeconds(Long.parseLong(values[i])));
                    break;
                case "y":
                    duration = (long) (duration + 365.2425 * TimeUnit.DAYS.toSeconds(Long.parseLong(values[i])));
                    break;
                default:
                    throw new NumberFormatException("You inputted a time unit (" + unit + ") which is not available!");
            }
        }

        return duration;
    }

    /**
     * Format a duration with the same short format that {@link NeoUtils#getDurationFromString} accepts
     * @param duration  The duration in seconds
     * @return          A formatted string
     */
    public static String formatDuration(long duration) {
        return formatDuration(duration, null);
    }

    /**
     * Format a duration
     * @param duration  The duration in seconds
     * @param lang      The language config to use;
     *                  if null it will use the same short format that {@link NeoUtils#getDurationFromString} accepts
     * @return          A formatted string
     */
    public static String formatDuration(long duration, NeoLanguageConfig lang) {
        int seconds = (int) (duration % 60);
        int minutes = (int) (TimeUnit.SECONDS.toMinutes(duration) % 60);
        int hours = (int) (TimeUnit.SECONDS.toHours(duration) % 24);
        int days = (int) (TimeUnit.SECONDS.toDays(duration) % 30);
        int months = (int) ((TimeUnit.SECONDS.toDays(duration) / 30) % 365);
        int years = (int) (TimeUnit.SECONDS.toDays(duration) / 30 / 365);

        List<String> stringList = new ArrayList<String>();

        if(years > 0)
            if(years == 1)
                stringList.add(years + (lang == null ? "y" : " " + lang.getTranslation("time.year")));
            else
                stringList.add(years + (lang == null ? "y" : " " + lang.getTranslation("time.years")));

        if(months > 0)
            if(months == 1)
                stringList.add(months + (lang == null ? "mo" : " " + lang.getTranslation("time.month")));
            else
                stringList.add(months + (lang == null ? "mo" : " " + lang.getTranslation("time.months")));

        if(days > 0)
            if(days == 1)
                stringList.add(days + (lang == null ? "d" : " " + lang.getTranslation("time.day")));
            else
                stringList.add(days + (lang == null ? "d" : " " + lang.getTranslation("time.days")));

        if(hours > 0)
            if(hours == 1)
                stringList.add(hours + (lang == null ? "h" : " " + lang.getTranslation("time.hour")));
            else
                stringList.add(hours + (lang == null ? "h" : " " + lang.getTranslation("time.hours")));

        if(minutes > 0)
            if(minutes == 1)
                stringList.add(minutes + (lang == null ? "m" : " " + lang.getTranslation("time.minute")));
            else
                stringList.add(minutes + (lang == null ? "m" : " " + lang.getTranslation("time.minutes")));

        if(seconds > 0)
            if(seconds == 1)
                stringList.add(seconds + (lang == null ? "s" : " " + lang.getTranslation("time.second")));
            else
                stringList.add(seconds + (lang == null ? "s" : " " + lang.getTranslation("time.seconds")));

        String ft = "";
        for(int i = 0; i < stringList.size(); i++) {
            if(i != 0)
                ft += " ";
            ft += stringList.get(i);
        }
        return ft;
    }

    /**
     * Compare to version strings
     * @param versionA  Version string, should start with a normal major.minor.patch number version
     * @param versionB  The version string to compare the other one to
     * @return          A number below 0 if versionA is before versionB; above 0 if versionA is greater than versionB; 0 if they are equal
     */
    public static int compareVersions(String versionA, String versionB) {
        String[] partsA = versionA.split("[^.\\d]")[0].split("\\.");
        String[] partsB = versionB.split("[^.\\d]")[0].split("\\.");
        int len = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < len; i++) {
            if (i >= partsA.length) {
                if (!partsB[i].equals("0")) {
                    return 1;
                }
            } else if (i >= partsB.length) {
                if (!partsA[i].equals("0")) {
                    return -1;
                }
            } else {
                int compared = Integer.compare(partsB[i].length(), partsA[i].length());
                if (compared == 0) {
                    compared = partsB[i].compareTo(partsA[i]);
                }
                if (compared < 0 ) {
                    return -1;
                } else if (compared > 0) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
