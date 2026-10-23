package com.example.pantrymanager.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Helpers for the optional expiry date stored as yyyy-MM-dd.
 */
public final class ExpiryUtils {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());

    private ExpiryUtils() {
    }

    /** Days from today until the date (negative if past), or null if the date is missing/invalid. */
    public static Long daysUntil(String iso) {
        LocalDate date = parse(iso);
        return date == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public static LocalDate parse(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(iso.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String toIso(int year, int monthZeroBased, int day) {
        return LocalDate.of(year, monthZeroBased + 1, day).toString();
    }

    public static String formatForDisplay(String iso) {
        LocalDate date = parse(iso);
        return date == null ? "" : DISPLAY.format(date);
    }

    /** "Expired 2 days ago", "Expires today", "Expires in 4 days", or "Expires 3 Dec 2026". */
    public static String describe(String iso) {
        Long days = daysUntil(iso);
        if (days == null) {
            return "No expiry date";
        }
        if (days < -1) return "Expired " + (-days) + " days ago";
        if (days == -1) return "Expired yesterday";
        if (days == 0) return "Expires today";
        if (days == 1) return "Expires tomorrow";
        if (days <= 14) return "Expires in " + days + " days";
        return "Expires " + formatForDisplay(iso);
    }

    public static boolean isExpired(String iso) {
        Long d = daysUntil(iso);
        return d != null && d < 0;
    }

    public static boolean isExpiringSoon(String iso, int withinDays) {
        Long d = daysUntil(iso);
        return d != null && d >= 0 && d <= withinDays;
    }
}
