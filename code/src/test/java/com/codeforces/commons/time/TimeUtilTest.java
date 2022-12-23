package com.codeforces.commons.time;

import org.junit.Test;

import java.text.ParseException;

import static com.codeforces.commons.time.TimeUtil.*;
import static org.junit.Assert.assertEquals;

public class TimeUtilTest {
    @Test
    public void testConversions() throws ParseException {
        {
            String dateTimeString = "2022-01-02 03:34:59";
            assertEquals(dateTimeString, toSystemDateTimeString(fromSystemDateTimeString(dateTimeString)));
            assertEquals(fromSystemDateTimeString(dateTimeString),
                    fromSystemDateTimeString(toSystemDateTimeString(fromSystemDateTimeString(dateTimeString))));
        }

        {
            String dateString = "2047-11-29";
            assertEquals(dateString, toSystemDateString(fromSystemDateString(dateString)));
            assertEquals(fromSystemDateString(dateString),
                    fromSystemDateString(toSystemDateString(fromSystemDateString(dateString))));

        }

        {
            String shortDateTimeString = "2123-12-31 17:45";
            assertEquals(shortDateTimeString, toSystemShortDateTimeString(fromSystemShortDateTimeString(shortDateTimeString)));
            assertEquals(fromSystemShortDateTimeString(shortDateTimeString),
                    fromSystemShortDateTimeString(toSystemShortDateTimeString(fromSystemShortDateTimeString(shortDateTimeString))));
        }

        {
            String timeString = "20:42:21";
            assertEquals(timeString, toSystemTimeString(fromSystemTimeString(timeString)));
            assertEquals(fromSystemTimeString(timeString),
                    fromSystemTimeString(toSystemTimeString(fromSystemTimeString(timeString))));
        }
    }
}
