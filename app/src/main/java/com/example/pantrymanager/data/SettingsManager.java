package com.example.pantrymanager.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pantrymanager.logic.RecipeMatcher;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * User preferences, stored in SharedPreferences so they survive app restarts.
 */
public class SettingsManager {

    private static final String preference = "pantry_settings";

    public static final String KeyExpiryAlerts = "expiry_alerts";
    public static final String KeyExpiryDays = "expiry_days";
    public static final String KeyImperial = "imperial_units";
    public static final String KeyIgnoreExpired = "ignore_expired";
    public static final String KeyWaterStaple = "water_staple";
    public static final String KeyShowAlmost = "show_almost_there";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(preference, Context.MODE_PRIVATE);
    }

    public boolean isExpiryAlertsEnabled() { return prefs.getBoolean(KeyExpiryAlerts, true); }
    public void setExpiryAlertsEnabled(boolean v) { prefs.edit().putBoolean(KeyExpiryAlerts, v).apply(); }

    /** How many days ahead an item counts as "expiring soon". */
    public int getExpiryDays() { return prefs.getInt(KeyExpiryDays, 3); }
    public void setExpiryDays(int v) { prefs.edit().putInt(KeyExpiryDays, v).apply(); }

    public boolean isImperial() { return prefs.getBoolean(KeyImperial, false); }
    public void setImperial(boolean v) { prefs.edit().putBoolean(KeyImperial, v).apply(); }

    public boolean isIgnoreExpired() { return prefs.getBoolean(KeyIgnoreExpired, true); }
    public void setIgnoreExpired(boolean v) { prefs.edit().putBoolean(KeyIgnoreExpired, v).apply(); }

    public boolean isWaterStaple() { return prefs.getBoolean(KeyWaterStaple, true); }
    public void setWaterStaple(boolean v) { prefs.edit().putBoolean(KeyWaterStaple, v).apply(); }

    public boolean isShowAlmostThere() { return prefs.getBoolean(KeyShowAlmost, true); }
    public void setShowAlmostThere(boolean v) { prefs.edit().putBoolean(KeyShowAlmost, v).apply(); }

    /** Builds a matcher configured with the user's current settings. */
    public RecipeMatcher buildMatcher() {
        Set<String> staples = isWaterStaple()
                ? new HashSet<>(Collections.singletonList("water"))
                : Collections.<String>emptySet();
        return new RecipeMatcher(staples, isIgnoreExpired(), todayIso());
    }

    public static String todayIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}
