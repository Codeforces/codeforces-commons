package com.codeforces.commons.time;

import com.codeforces.commons.math.NumberUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.lang.StrictMath.round;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 22.06.11
 */
public class TimeUtil {
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

    @Nullable
    public static Date toDate(@Nullable Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    /**
     * @param beginDate From date.
     * @param endDate   To date.
     * @return Returns endDate - beginDate in days. Hours, minutes and seconds are ignored, so
     *         beginDate and endDate are used as dates.
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

    private TimeUtil() {
        throw new UnsupportedOperationException();
    }
}
