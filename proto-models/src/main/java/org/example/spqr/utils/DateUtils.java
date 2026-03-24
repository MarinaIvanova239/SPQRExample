package org.example.spqr.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.isNull;

public class DateUtils {

    public static Date toDateOrNull(ZonedDateTime zonedDateTime) {
        return isNull(zonedDateTime) ? null : toDateOrNull(toDateTime(zonedDateTime));
    }

    public static DateTime toDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return new DateTime(dateTime.toInstant().toEpochMilli(), DateTimeZone.forID(dateTime.getZone().getId()));
    }

    public static Date toDateOrNull(DateTime dateTime) {
        return isNull(dateTime) ? null : dateTime.toDate();
    }

    public static ZonedDateTime toZonedDateTime(Date date) {
        DateTime dateTime = toDateTimeOrNull(date);
        return toZonedDateTime(dateTime);
    }

    public static DateTime toDateTimeOrNull(Date date) {
        return date == null ? null : new DateTime(date);
    }

    public static ZonedDateTime toZonedDateTime(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        ZoneId zoneId = ZoneId.of(dateTime.getZone().getID());
        Instant instant = Instant.ofEpochMilli(dateTime.getMillis());
        return ZonedDateTime.ofInstant(instant, zoneId);
    }
}
