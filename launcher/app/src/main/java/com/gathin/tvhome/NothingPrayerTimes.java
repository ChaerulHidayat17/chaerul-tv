package com.gathin.tvhome;

import java.util.Calendar;
import java.util.Locale;

/**
 * Standard Kemenag RI Astronomical Prayer Times Calculator.
 * Accurate to the minute with Kemenag / Bimas Islam standards (Ihtiyat +2 mins,
 * Subuh -20 deg, Isya -18 deg, Maghrib -1 deg, Ashar Shafi'i shadow ratio = 1).
 */
public class NothingPrayerTimes {

    // Default coordinates: Jabodetabek / West Java (-6.23 S, 106.97 E, GMT+7)
    private static final double LATITUDE = -6.23;
    private static final double LONGITUDE = 106.97;
    private static final int TIMEZONE = 7;
    private static final int IHTIYAT = 2; // +2 minutes safety margin (standard Kemenag)

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

        // Fractional year (gamma) in radians
        double gamma = 2.0 * Math.PI / 365.25 * (dayOfYear - 1);

        // Equation of time (minutes)
        double eqtime = 229.18 * (0.000075 + 0.001868 * Math.cos(gamma) - 0.032077 * Math.sin(gamma)
                - 0.014615 * Math.cos(2 * gamma) - 0.040849 * Math.sin(2 * gamma));

        // Solar declination (radians)
        double decl = 0.006918 - 0.399912 * Math.cos(gamma) + 0.070257 * Math.sin(gamma)
                - 0.006758 * Math.cos(2 * gamma) + 0.000907 * Math.sin(2 * gamma)
                - 0.002697 * Math.cos(3 * gamma) + 0.00148 * Math.sin(3 * gamma);

        double latRad = Math.toRadians(LATITUDE);

        // Solar noon (Dzuhur) in minutes from midnight
        double timeOffset = (TIMEZONE * 15.0 - LONGITUDE) * 4.0;
        double noonMin = 720.0 - eqtime + timeOffset;

        // 1. Dzuhur
        double dzuhur = noonMin + IHTIYAT;

        // 2. Subuh (Sun altitude = -20.0 degrees)
        double haSubuh = calcHourAngle(-20.0, latRad, decl);
        double fajr = noonMin - haSubuh + IHTIYAT;

        // 3. Terbit / Sunrise (Sun altitude = -1.0 degrees)
        double haSunrise = calcHourAngle(-1.0, latRad, decl);
        double sunrise = noonMin - haSunrise - IHTIYAT;

        // 4. Ashar (Shadow ratio = 1)
        double phiDelta = Math.abs(latRad - decl);
        double altAsharRad = Math.atan(1.0 / (1.0 + Math.tan(phiDelta)));
        double cosHaAshar = (Math.sin(altAsharRad) - Math.sin(latRad) * Math.sin(decl)) / (Math.cos(latRad) * Math.cos(decl));
        double haAshar = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, cosHaAshar)))) * 4.0;
        double asr = noonMin + haAshar + IHTIYAT;

        // 5. Maghrib (Sun altitude = -1.0 degrees)
        double haMaghrib = calcHourAngle(-1.0, latRad, decl);
        double maghrib = noonMin + haMaghrib + IHTIYAT;

        // 6. Isya (Sun altitude = -18.0 degrees)
        double haIsya = calcHourAngle(-18.0, latRad, decl);
        double isha = noonMin + haIsya + IHTIYAT;

        double[] rawMins = {fajr, sunrise, dzuhur, asr, maghrib, isha};
        int[] mins = new int[6];
        for (int i = 0; i < 6; i++) {
            mins[i] = (int) Math.round(rawMins[i]);
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

    private static double calcHourAngle(double altDeg, double latRad, double declRad) {
        double altRad = Math.toRadians(altDeg);
        double cosHa = (Math.sin(altRad) - Math.sin(latRad) * Math.sin(declRad)) / (Math.cos(latRad) * Math.cos(declRad));
        cosHa = Math.max(-1.0, Math.min(1.0, cosHa));
        return Math.toDegrees(Math.acos(cosHa)) * 4.0;
    }
}