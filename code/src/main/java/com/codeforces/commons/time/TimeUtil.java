package com.codeforces.commons.time;

import com.codeforces.commons.math.NumberUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.codeforces.commons.math.Math.round;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.06.11
 */
public class TimeUtil {
    private static final Locale DEFAULT_LOCALE = new Locale("en", "EN");

    public static final long DAYS_PER_WEEK = 7;

    public static final long HOURS_PER_DAY = 24;
    public static final long HOURS_PER_WEEK = HOURS_PER_DAY * DAYS_PER_WEEK;

    public static final long MINUTES_PER_HOUR = 60;
    public static final long MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    public static final long MINUTES_PER_WEEK = MINUTES_PER_DAY * DAYS_PER_WEEK;

    public static final long SECONDS_PER_MINUTE = 60;
    public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    public static final long SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;

    public static final long MILLIS_PER_SECOND = 1000;
    public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY;
    public static final long MILLIS_PER_WEEK = MILLIS_PER_DAY * DAYS_PER_WEEK;

    public static final long MICROSECONDS_PER_MILLISECOND = 1000;
    public static final long MICROSECONDS_PER_SECOND = MICROSECONDS_PER_MILLISECOND * MILLIS_PER_SECOND;
    public static final long MICROSECONDS_PER_MINUTE = MICROSECONDS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final long MICROSECONDS_PER_HOUR = MICROSECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long MICROSECONDS_PER_DAY = MICROSECONDS_PER_HOUR * HOURS_PER_DAY;
    public static final long MICROSECONDS_PER_WEEK = MICROSECONDS_PER_DAY * DAYS_PER_WEEK;

    public static final long NANOSECONDS_PER_MICROSECOND = 1000;
    public static final long NANOSECONDS_PER_MILLISECOND = NANOSECONDS_PER_MICROSECOND * MICROSECONDS_PER_MILLISECOND;
    public static final long NANOSECONDS_PER_SECOND = NANOSECONDS_PER_MILLISECOND * MILLIS_PER_SECOND;
    public static final long NANOSECONDS_PER_MINUTE = NANOSECONDS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final long NANOSECONDS_PER_HOUR = NANOSECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long NANOSECONDS_PER_DAY = NANOSECONDS_PER_HOUR * HOURS_PER_DAY;
    public static final long NANOSECONDS_PER_WEEK = NANOSECONDS_PER_DAY * DAYS_PER_WEEK;

    public static final String SYSTEM_DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final String SYSTEM_DATE_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public static final String SYSTEM_SHORT_DATE_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm";
    public static final String SYSTEM_TIME_FORMAT_STRING = "HH:mm:ss";

    private static final ThreadLocal<DateFormat> SYSTEM_DATE_FORMAT
            = newSimpleDateFormat(SYSTEM_DATE_FORMAT_STRING, DEFAULT_LOCALE);
    private static final ThreadLocal<DateFormat> SYSTEM_DATE_TIME_FORMAT
            = newSimpleDateFormat(SYSTEM_DATE_TIME_FORMAT_STRING, DEFAULT_LOCALE);
    private static final ThreadLocal<DateFormat> SYSTEM_SHORT_DATE_TIME_FORMAT
            = newSimpleDateFormat(SYSTEM_SHORT_DATE_TIME_FORMAT_STRING, DEFAULT_LOCALE);
    private static final ThreadLocal<DateFormat> SYSTEM_TIME_FORMAT
            = newSimpleDateFormat(SYSTEM_TIME_FORMAT_STRING, DEFAULT_LOCALE);

    @Nonnull
    public static ThreadLocal<DateFormat> newSimpleDateFormat(@Nonnull String pattern, @Nullable Locale locale) {
        return ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern, (locale != null ? locale : Locale.ROOT)));
    }

    @Nonnull
    public static Date fromSystemDateString(@Nonnull String systemDateTimeString) throws ParseException {
        return SYSTEM_DATE_FORMAT.get().parse(systemDateTimeString);
    }

    @Nonnull
    public static Date fromSystemDateTimeString(@Nonnull String systemDateTimeString) throws ParseException {
        return SYSTEM_DATE_TIME_FORMAT.get().parse(systemDateTimeString);
    }

    @Nonnull
    public static Date fromSystemShortDateTimeString(@Nonnull String systemDateTimeString) throws ParseException {
        return SYSTEM_SHORT_DATE_TIME_FORMAT.get().parse(systemDateTimeString);
    }

    @Nonnull
    public static Date fromSystemTimeString(@Nonnull String systemDateTimeString) throws ParseException {
        return SYSTEM_TIME_FORMAT.get().parse(systemDateTimeString);
    }

    @Nonnull
    public static String toSystemDateString(@Nonnull Date date) {
        return SYSTEM_DATE_FORMAT.get().format(date);
    }

    @Nonnull
    public static String toSystemDateTimeString(@Nonnull Date date) {
        return SYSTEM_DATE_TIME_FORMAT.get().format(date);
    }

    @Nonnull
    public static String toSystemShortDateTimeString(@Nonnull Date date) {
        return SYSTEM_SHORT_DATE_TIME_FORMAT.get().format(date);
    }

    @Nonnull
    public static String toSystemTimeString(@Nonnull Date date) {
        return SYSTEM_TIME_FORMAT.get().format(date);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static Date toDate(@Nullable Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    /**
     * @param beginDate From date.
     * @param endDate   To date.
     * @return Returns endDate - beginDate in days. Hours, minutes and seconds are ignored, so
     * beginDate and endDate are used as dates.
     */
    public static int getDaysBetween(@Nonnull Date beginDate, @Nonnull Date endDate) {
        Calendar beginCalendar = new GregorianCalendar();
        beginCalendar.setTime(beginDate);
        beginCalendar.set(Calendar.MILLISECOND, 0);
        beginCalendar.set(Calendar.SECOND, 0);
        beginCalendar.set(Calendar.MINUTE, 0);
        beginCalendar.set(Calendar.HOUR_OF_DAY, 1);

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);
        endCalendar.set(Calendar.MILLISECOND, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.HOUR_OF_DAY, 0);

//        int result = 0;
//        while (beginCalendar.before(endCalendar)) {
//            beginCalendar.add(Calendar.DAY_OF_YEAR, 1);
//            result++;
//        }
//
//        return result;

        return NumberUtil.toInt(round(
                (endCalendar.getTimeInMillis() - beginCalendar.getTimeInMillis()) / (double) MILLIS_PER_DAY
        ));
    }

    @Nonnull
    public static String formatInterval(@Nonnegative long intervalMillis) {
        if (intervalMillis < 0) {
            throw new IllegalArgumentException("Argument 'intervalMillis' must be a positive integer or zero.");
        }

        if (intervalMillis >= MILLIS_PER_WEEK) {
            return formatInterval(intervalMillis, MILLIS_PER_WEEK, "week", "weeks");
        }

        if (intervalMillis >= MILLIS_PER_DAY) {
            return formatInterval(intervalMillis, MILLIS_PER_DAY, "day", "days");
        }

        if (intervalMillis >= MILLIS_PER_HOUR) {
            return formatInterval(intervalMillis, MILLIS_PER_HOUR, "hour", "hours");
        }

        if (intervalMillis >= MILLIS_PER_MINUTE) {
            return formatInterval(intervalMillis, MILLIS_PER_MINUTE, "minute", "minutes");
        }

        if (intervalMillis >= MILLIS_PER_SECOND) {
            return formatInterval(intervalMillis, MILLIS_PER_SECOND, "second", "seconds");
        }

        return intervalMillis + " ms";
    }

    @Nonnull
    private static String formatInterval(@Nonnegative long intervalMillis, @Nonnegative long unit,
                                         @Nonnull String unitName, @Nonnull String unitsName) {
        if (intervalMillis == unit) {
            return "1 " + unitName;
        }

        if (intervalMillis % unit == 0) {
            return intervalMillis / unit + " " + unitsName;
        }

        return String.format(Locale.US, "%.1f %s", (double) intervalMillis / (double) unit, unitsName);
    }

    private TimeUtil() {
        throw new UnsupportedOperationException();
    }
}
