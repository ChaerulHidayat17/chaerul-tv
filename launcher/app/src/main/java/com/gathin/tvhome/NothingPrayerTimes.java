package com.gathin.tvhome;

import java.util.Calendar;
import java.util.Locale;

/**
 * Offline Astronomical Prayer Times Calculator for West Java (GMT+7)
 */
public class NothingPrayerTimes {

    public static class PrayerInfo {
        public String nextName;
        public String nextTime;
        public String[] names = {"FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA"};
        public String[] times = new String[6];
    }

    public static PrayerInfo getTodayTimes() {
        PrayerInfo info = new PrayerInfo();
        Calendar cal = Calendar.getInstance();
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        // Approximate equation of time variation throughout the year (minutes)
        double b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0;
        double eot = 9.87 * Math.sin(2 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b);

        // Base times for West Java (Longitude 107.6 E, Latitude -6.9 S, GMT+7)
        // Standard Dhuhr ~ 11:45 - eot
        int dhuhrMin = (int) Math.round(11 * 60 + 47 - eot);
        int fajrMin = dhuhrMin - 450;    // ~04:17
        int sunriseMin = dhuhrMin - 373; // ~05:34
        int asrMin = dhuhrMin + 195;     // ~15:02
        int maghribMin = dhuhrMin + 368; // ~17:55
        int ishaMin = dhuhrMin + 438;    // ~19:05

        int[] mins = {fajrMin, sunriseMin, dhuhrMin, asrMin, maghribMin, ishaMin};
        for (int i = 0; i < 6; i++) {
            int h = (mins[i] / 60) % 24;
            int m = mins[i] % 60;
            info.times[i] = String.format(Locale.US, "%02d:%02d", h, m);
        }

        // Determine next prayer
        int currentMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        int nextIdx = 0;
        for (int i = 0; i < 6; i++) {
            if (currentMin < mins[i]) {
                nextIdx = i;
                break;
            }
        }
        if (currentMin >= mins[5]) {
            nextIdx = 0; // Next is tomorrow's Fajr
        }

        info.nextName = info.names[nextIdx];
        info.nextTime = info.times[nextIdx];
        return info;
    }
}
