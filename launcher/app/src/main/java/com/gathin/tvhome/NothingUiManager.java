package com.gathin.tvhome;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Controller for the complete NOTHING (TV) 4-screen design experience:
 * 01 / HOME, 02 / ALL APPS, 03 / INPUTS, 04 / QUICK SETTINGS.
 */
public class NothingUiManager {

    private final Activity activity;
    private final FrameLayout root;
    private final float density;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private View screenHome;
    private View screenAllApps;
    private View screenInputs;
    private View screenQuickSettings;
    private int currentScreenId = R.id.screenHome;

    private TextView calDayName, calDateNum, calMonthYear;
    private TextView prayerTimeDigital, prayerNextBadge;
    private TextView ptFajr, ptSunrise, ptDhuhr, ptAsr, ptMaghrib, ptIsha;
    private TextView wifiStatusText;
    private LinearLayout dockContainer;

    private TextView allAppsClock, appsCountText;
    private RecyclerView allAppsRecyclerView;
    private AllAppsAdapter allAppsAdapter;

    private TextView inputsClock;
    private TextView quickSettingsClock;
    private ProgressBar volumeProgressBar;
    private TextView volumeNumText;

    private final List<AppEntry> allAppsList = new ArrayList<>();
    private final List<AppEntry> filteredAppsList = new ArrayList<>();

    private int lastDayOfYear = -1;
    private int lastMinuteOfDay = -1;
    private boolean receiverRegistered = false;

    private final BroadcastReceiver timeAndDateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCalendarAndPrayers(true);
            updateClocks();
            updateWifiStatus();
        }
    };

    public static class AppEntry {
        public String packageName;
        public String activityName;
        public String label;
        public Drawable icon;
        public String category; // "video", "music", "utilities", "all"

        public AppEntry(String pkg, String act, String lbl, Drawable ico, String cat) {
            this.packageName = pkg;
            this.activityName = act;
            this.label = lbl;
            this.icon = ico;
            this.category = cat;
        }
    }

    public NothingUiManager(Activity activity, FrameLayout root) {
        this.activity = activity;
        this.root = root;
        this.density = activity.getResources().getDisplayMetrics().density;
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View layout = inflater.inflate(R.layout.activity_nothing, root, true);

        screenHome = layout.findViewById(R.id.screenHome);
        screenAllApps = layout.findViewById(R.id.screenAllApps);
        screenInputs = layout.findViewById(R.id.screenInputs);
        screenQuickSettings = layout.findViewById(R.id.screenQuickSettings);

        // Header Navigation
        setupHeaderNav(layout);

        // Home Widgets
        setupHomeWidgets(layout);

        // Load Apps and App Dock
        loadInstalledApps();
        setupAppDock(layout);

        // Bottom Action Bar
        setupBottomActionBar(layout);

        // Screen 02: ALL APPS
        setupScreenAllApps(layout);

        // Screen 03: INPUTS
        setupScreenInputs(layout);

        // Screen 04: QUICK SETTINGS
        setupScreenQuickSettings(layout);

        // Start Clock & Time updater
        startRealtimeUpdater();

        // Register date & time broadcast receiver
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            activity.registerReceiver(timeAndDateReceiver, filter);
            receiverRegistered = true;
        } catch (Exception ignored) {}

        // Default: Show Home
        showScreen(R.id.screenHome);
    }

    public void onResume() {
        updateCalendarAndPrayers(true);
        updateClocks();
        updateWifiStatus();
    }

    public void onDestroy() {
        if (receiverRegistered) {
            try {
                activity.unregisterReceiver(timeAndDateReceiver);
            } catch (Exception ignored) {}
            receiverRegistered = false;
        }
        handler.removeCallbacksAndMessages(null);
    }

    public boolean handleBackPressed() {
        if (currentScreenId != R.id.screenHome) {
            showScreen(R.id.screenHome);
            return true;
        }
        return false;
    }

    public void showScreen(int screenId) {
        currentScreenId = screenId;
        screenHome.setVisibility(screenId == R.id.screenHome ? View.VISIBLE : View.GONE);
        screenAllApps.setVisibility(screenId == R.id.screenAllApps ? View.VISIBLE : View.GONE);
        screenInputs.setVisibility(screenId == R.id.screenInputs ? View.VISIBLE : View.GONE);
        screenQuickSettings.setVisibility(screenId == R.id.screenQuickSettings ? View.VISIBLE : View.GONE);

        updateClocks();

        if (screenId == R.id.screenHome) {
            updateCalendarAndPrayers(true);
            updateWifiStatus();
            if (dockContainer.getChildCount() > 0) {
                dockContainer.getChildAt(0).requestFocus();
            }
        } else if (screenId == R.id.screenAllApps) {
            allAppsRecyclerView.requestFocus();
        } else if (screenId == R.id.screenInputs) {
            View v = screenInputs.findViewById(R.id.inputCardHdmi1);
            if (v != null) v.requestFocus();
        } else if (screenId == R.id.screenQuickSettings) {
            View v = screenQuickSettings.findViewById(R.id.toggleWifi);
            if (v != null) v.requestFocus();
            updateVolumeSlider();
        }
    }

    private void setupHeaderNav(View layout) {
        View tabHome = layout.findViewById(R.id.tabHome);
        View tabApps = layout.findViewById(R.id.tabApps);
        View tabLiveTv = layout.findViewById(R.id.tabLiveTv);
        View tabMedia = layout.findViewById(R.id.tabMedia);

        tabHome.setOnClickListener(v -> showScreen(R.id.screenHome));
        tabApps.setOnClickListener(v -> showScreen(R.id.screenAllApps));
        tabLiveTv.setOnClickListener(v -> launchApp("com.mitv.livetv"));
        tabMedia.setOnClickListener(v -> launchApp("org.videolan.vlc"));

        View[] topTabs = {tabHome, tabApps, tabLiveTv, tabMedia};
        for (View t : topTabs) {
            if (t instanceof TextView) {
                TextView tv = (TextView) t;
                tv.setOnFocusChangeListener((v, hasFocus) -> {
                    if (tv == tabHome && currentScreenId == R.id.screenHome) {
                        tv.setBackgroundResource(R.drawable.nothing_pill_white);
                        tv.setTextColor(Color.BLACK);
                    } else {
                        tv.setBackgroundResource(hasFocus ? R.drawable.nothing_pill_white : R.drawable.nothing_pill_dark);
                        tv.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
                    }
                });
            }
        }

        View btnAllAppsTop = layout.findViewById(R.id.btnAllAppsTop);
        if (btnAllAppsTop != null) {
            btnAllAppsTop.setOnClickListener(v -> showScreen(R.id.screenAllApps));
        }

        View btnSettingsTop = layout.findViewById(R.id.btnSettingsTop);
        if (btnSettingsTop != null) {
            btnSettingsTop.setOnClickListener(v -> showScreen(R.id.screenQuickSettings));
        }

        View btnSearchTop = layout.findViewById(R.id.btnSearchTop);
        if (btnSearchTop != null) {
            btnSearchTop.setOnClickListener(v -> {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_ASSIST));
                } catch (Exception e) {
                    Toast.makeText(activity, "Search", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View[] topIcons = {btnSearchTop, btnAllAppsTop, btnSettingsTop};
        for (View icon : topIcons) {
            if (icon != null) {
                icon.setOnFocusChangeListener((v, hasFocus) -> {
                    icon.setBackgroundResource(hasFocus ? R.drawable.nothing_pill_white : R.drawable.nothing_pill_dark);
                    if (icon instanceof ImageView) {
                        ((ImageView) icon).setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
                    }
                });
            }
        }
    }

    private void setupHomeWidgets(View layout) {
        calDayName = layout.findViewById(R.id.calDayName);
        calDateNum = layout.findViewById(R.id.calDateNum);
        calMonthYear = layout.findViewById(R.id.calMonthYear);

        prayerTimeDigital = layout.findViewById(R.id.prayerTimeDigital);
        prayerNextBadge = layout.findViewById(R.id.prayerNextBadge);
        ptFajr = layout.findViewById(R.id.ptFajr);
        ptSunrise = layout.findViewById(R.id.ptSunrise);
        ptDhuhr = layout.findViewById(R.id.ptDhuhr);
        ptAsr = layout.findViewById(R.id.ptAsr);
        ptMaghrib = layout.findViewById(R.id.ptMaghrib);
        ptIsha = layout.findViewById(R.id.ptIsha);

        wifiStatusText = layout.findViewById(R.id.wifiLabelSquare);

        View cardHdmiWide = layout.findViewById(R.id.cardHdmiWide);
        if (cardHdmiWide != null) {
            cardHdmiWide.setOnClickListener(v -> showScreen(R.id.screenInputs));
            cardHdmiWide.setOnFocusChangeListener((v, hasFocus) -> {
                cardHdmiWide.setScaleX(hasFocus ? 1.03f : 1.0f);
                cardHdmiWide.setScaleY(hasFocus ? 1.03f : 1.0f);
            });
        }

        View cardWifiSquare = layout.findViewById(R.id.cardWifiSquare);
        if (cardWifiSquare != null) {
            cardWifiSquare.setOnClickListener(v -> {
                try {
                    activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } catch (Exception ignored) {}
            });
            cardWifiSquare.setOnFocusChangeListener((v, hasFocus) -> {
                cardWifiSquare.setBackgroundResource(hasFocus ? R.drawable.nothing_card_focused : R.drawable.nothing_card_bg);
                setChildTextColor(cardWifiSquare, hasFocus ? Color.BLACK : Color.WHITE);
            });
        }

        View cardSleepMode = layout.findViewById(R.id.cardSleepMode);
        TextView sleepModeStatusText = layout.findViewById(R.id.sleepModeStatusText);
        if (cardSleepMode != null) {
            cardSleepMode.setOnClickListener(v -> {
                if (sleepModeStatusText != null) {
                    String cur = sleepModeStatusText.getText().toString();
                    if ("OFF".equals(cur)) {
                        sleepModeStatusText.setText("30M");
                        Toast.makeText(activity, "Sleep timer: 30 minutes", Toast.LENGTH_SHORT).show();
                    } else if ("30M".equals(cur)) {
                        sleepModeStatusText.setText("60M");
                        Toast.makeText(activity, "Sleep timer: 60 minutes", Toast.LENGTH_SHORT).show();
                    } else {
                        sleepModeStatusText.setText("OFF");
                        Toast.makeText(activity, "Sleep timer: OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            cardSleepMode.setOnFocusChangeListener((v, hasFocus) -> {
                cardSleepMode.setScaleX(hasFocus ? 1.05f : 1.0f);
                cardSleepMode.setScaleY(hasFocus ? 1.05f : 1.0f);
            });
        }

        updateCalendarAndPrayers(true);
        updateWifiStatus();
    }

    public void updateCalendarAndPrayers() {
        updateCalendarAndPrayers(false);
    }

    public void updateCalendarAndPrayers(boolean force) {
        Calendar cal = Calendar.getInstance();
        int curDay = cal.get(Calendar.DAY_OF_YEAR);
        int curMinute = cal.get(Calendar.MINUTE);
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        int curMinuteOfDay = curHour * 60 + curMinute;

        if (!force && curDay == lastDayOfYear && curMinuteOfDay == lastMinuteOfDay) {
            return;
        }
        lastDayOfYear = curDay;
        lastMinuteOfDay = curMinuteOfDay;

        Locale idLoc = new Locale("in", "ID");
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", idLoc);
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMM / yyyy", idLoc);

        if (calDayName != null) calDayName.setText(dayFmt.format(cal.getTime()).toUpperCase(idLoc));
        if (calDateNum != null) calDateNum.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        if (calMonthYear != null) calMonthYear.setText(monthFmt.format(cal.getTime()).toUpperCase(idLoc));

        NothingPrayerTimes.PrayerInfo p = NothingPrayerTimes.getTodayTimes();
        if (prayerTimeDigital != null) prayerTimeDigital.setText(p.nextTime);
        if (prayerNextBadge != null) prayerNextBadge.setText("NEXT: " + p.nextName);

        if (ptFajr != null) ptFajr.setText(p.times[0]);
        if (ptSunrise != null) ptSunrise.setText(p.times[1]);
        if (ptDhuhr != null) ptDhuhr.setText(p.times[2]);
        if (ptAsr != null) ptAsr.setText(p.times[3]);
        if (ptMaghrib != null) ptMaghrib.setText(p.times[4]);
        if (ptIsha != null) ptIsha.setText(p.times[5]);
    }

    private void updateWifiStatus() {
        try {
            WifiManager wm = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            TextView wifiLabelSquare = activity.findViewById(R.id.wifiLabelSquare);
            if (wm != null && wm.isWifiEnabled()) {
                WifiInfo info = wm.getConnectionInfo();
                String ssid = info != null ? info.getSSID() : "";
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
                if (ssid == null || ssid.isEmpty() || "<unknown ssid>".equals(ssid)) {
                    ssid = "Connected";
                }
                if (wifiLabelSquare != null) wifiLabelSquare.setText(ssid);
            } else {
                if (wifiLabelSquare != null) wifiLabelSquare.setText("Off");
            }
        } catch (Exception e) {
            TextView wifiLabelSquare = activity.findViewById(R.id.wifiLabelSquare);
            if (wifiLabelSquare != null) wifiLabelSquare.setText("Connected");
        }
    }

    private void loadInstalledApps() {
        allAppsList.clear();
        PackageManager pm = activity.getPackageManager();

        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> leanbackApps = pm.queryIntentActivities(leanbackIntent, 0);

        Intent standardIntent = new Intent(Intent.ACTION_MAIN);
        standardIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> standardApps = pm.queryIntentActivities(standardIntent, 0);

        Set<String> seen = new HashSet<>();
        List<ResolveInfo> combined = new ArrayList<>(leanbackApps);
        for (ResolveInfo r : leanbackApps) {
            seen.add(r.activityInfo.packageName);
        }
        for (ResolveInfo r : standardApps) {
            if (!seen.contains(r.activityInfo.packageName) && !activity.getPackageName().equals(r.activityInfo.packageName)) {
                combined.add(r);
                seen.add(r.activityInfo.packageName);
            }
        }

        for (ResolveInfo r : combined) {
            if (activity.getPackageName().equals(r.activityInfo.packageName)) continue;
            String pkg = r.activityInfo.packageName;
            String act = r.activityInfo.name;
            String lbl = r.loadLabel(pm).toString();
            Drawable icon = r.loadIcon(pm);

            String cat = "utilities";
            String lower = (pkg + " " + lbl).toLowerCase(Locale.ROOT);
            if (lower.contains("netflix") || lower.contains("youtube") || lower.contains("vidio") ||
                lower.contains("disney") || lower.contains("prime") || lower.contains("vision") ||
                lower.contains("vlc") || lower.contains("tv") || lower.contains("cinema") || lower.contains("video")) {
                cat = "video";
            } else if (lower.contains("music") || lower.contains("spotify") || lower.contains("audio") || lower.contains("sound")) {
                cat = "music";
            }
            allAppsList.add(new AppEntry(pkg, act, lbl, icon, cat));
        }

        // Sort priority apps first (as in design)
        final List<String> priorityOrder = List.of(
            "com.netflix.ninja",
            "com.google.android.youtube.tv",
            "in.startv.hotstar.dplus.tv",
            "com.amazon.amazonvideo.livingroom",
            "com.vidio.android.tv",
            "id.visionplus.android.atv",
            "com.spotify.tv.android",
            "org.videolan.vlc",
            "com.megarohas.amtv",
            "com.apple.atve.androidtv.appletv",
            "com.tencent.qqlivei18n",
            "com.iqiyi.i18n.tv",
            "com.mitv.livetv",
            "com.android.vending"
        );

        Collections.sort(allAppsList, (a, b) -> {
            int idxA = priorityOrder.indexOf(a.packageName);
            int idxB = priorityOrder.indexOf(b.packageName);
            if (idxA != -1 && idxB != -1) return Integer.compare(idxA, idxB);
            if (idxA != -1) return -1;
            if (idxB != -1) return 1;
            return a.label.compareToIgnoreCase(b.label);
        });

        filteredAppsList.clear();
        filteredAppsList.addAll(allAppsList);
    }

    private void setupAppDock(View layout) {
        dockContainer = layout.findViewById(R.id.dockContainer);
        dockContainer.removeAllViews();

        int limit = Math.min(8, allAppsList.size());
        for (int i = 0; i < limit; i++) {
            AppEntry app = allAppsList.get(i);
            View card = createDockCard(app);
            dockContainer.addView(card);
        }

        // Add "All apps" card at the end of dock
        View allAppsCard = createAllAppsDockCard();
        dockContainer.addView(allAppsCard);
    }

    private Drawable getNothingVectorIcon(String packageName, Drawable fallback) {
        String lower = packageName.toLowerCase(Locale.ROOT);
        if (lower.contains("netflix")) {
            return activity.getDrawable(R.drawable.ic_nothing_netflix);
        } else if (lower.contains("youtube") && !lower.contains("music")) {
            return activity.getDrawable(R.drawable.ic_nothing_youtube);
        } else if (lower.contains("hotstar") || lower.contains("disney")) {
            return activity.getDrawable(R.drawable.ic_nothing_disney);
        } else if (lower.contains("amazon") || lower.contains("prime")) {
            return activity.getDrawable(R.drawable.ic_nothing_prime);
        } else if (lower.contains("vidio")) {
            return activity.getDrawable(R.drawable.ic_nothing_vidio);
        } else if (lower.contains("vision")) {
            return activity.getDrawable(R.drawable.ic_nothing_vision);
        } else if (lower.contains("spotify")) {
            return activity.getDrawable(R.drawable.ic_nothing_spotify);
        } else if (lower.contains("videolan") || lower.contains("vlc")) {
            return activity.getDrawable(R.drawable.ic_nothing_vlc);
        } else if (lower.contains("apple") || lower.contains("amtv")) {
            return activity.getDrawable(R.drawable.ic_nothing_applemusic);
        }
        return fallback;
    }

    private View createDockCard(AppEntry app) {
        LinearLayout item = new LinearLayout(activity);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setFocusable(true);
        item.setClickable(true);

        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLp.setMarginEnd((int) (20 * density));
        item.setLayoutParams(itemLp);

        LinearLayout pillContainer = new LinearLayout(activity);
        pillContainer.setOrientation(LinearLayout.HORIZONTAL);
        pillContainer.setGravity(Gravity.CENTER);
        pillContainer.setBackgroundResource(R.drawable.nothing_circle_dark);

        int pad = (int) (10 * density);
        pillContainer.setPadding(pad, pad, pad, pad);

        LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(
                (int) (58 * density), (int) (58 * density));
        pillContainer.setLayoutParams(pillLp);

        Drawable vectorIcon = getNothingVectorIcon(app.packageName, app.icon);
        ImageView iv = new ImageView(activity);
        iv.setImageDrawable(vectorIcon);
        iv.setLayoutParams(new LinearLayout.LayoutParams((int) (30 * density), (int) (30 * density)));
        pillContainer.addView(iv);

        TextView insideTv = new TextView(activity);
        insideTv.setText(app.label);
        insideTv.setTextColor(Color.WHITE);
        insideTv.setTextSize(13.5f);
        insideTv.setTypeface(activity.getResources().getFont(R.font.ntype), android.graphics.Typeface.BOLD);
        insideTv.setSingleLine(true);
        insideTv.setVisibility(View.GONE);
        LinearLayout.LayoutParams insideLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        insideLp.setMarginStart((int) (12 * density));
        insideLp.setMarginEnd((int) (8 * density));
        insideTv.setLayoutParams(insideLp);
        pillContainer.addView(insideTv);

        item.addView(pillContainer);

        TextView bottomTv = new TextView(activity);
        bottomTv.setText(app.label);
        bottomTv.setTextColor(Color.parseColor("#CCCCCC"));
        bottomTv.setTextSize(10.5f);
        bottomTv.setTypeface(activity.getResources().getFont(R.font.ntype));
        bottomTv.setSingleLine(true);
        bottomTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(
                (int) (70 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomLp.topMargin = (int) (5 * density);
        bottomTv.setLayoutParams(bottomLp);
        item.addView(bottomTv);

        item.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                pillContainer.setBackgroundResource(R.drawable.nothing_pill_focused_ring);
                pillLp.width = (int) (180 * density);
                pillContainer.setLayoutParams(pillLp);
                insideTv.setVisibility(View.VISIBLE);
                bottomTv.setVisibility(View.INVISIBLE);
            } else {
                pillContainer.setBackgroundResource(R.drawable.nothing_circle_dark);
                pillLp.width = (int) (58 * density);
                pillContainer.setLayoutParams(pillLp);
                insideTv.setVisibility(View.GONE);
                bottomTv.setVisibility(View.VISIBLE);
            }
        });

        item.setOnClickListener(v -> launchApp(app.packageName));
        return item;
    }

    private View createAllAppsDockCard() {
        LinearLayout item = new LinearLayout(activity);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setFocusable(true);
        item.setClickable(true);

        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        item.setLayoutParams(itemLp);

        LinearLayout pillContainer = new LinearLayout(activity);
        pillContainer.setOrientation(LinearLayout.HORIZONTAL);
        pillContainer.setGravity(Gravity.CENTER);
        pillContainer.setBackgroundResource(R.drawable.nothing_circle_white);

        int pad = (int) (10 * density);
        pillContainer.setPadding(pad, pad, pad, pad);

        LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(
                (int) (58 * density), (int) (58 * density));
        pillContainer.setLayoutParams(pillLp);

        ImageView iv = new ImageView(activity);
        iv.setImageResource(R.drawable.ic_apps);
        iv.setColorFilter(Color.BLACK);
        iv.setLayoutParams(new LinearLayout.LayoutParams((int) (26 * density), (int) (26 * density)));
        pillContainer.addView(iv);

        TextView insideTv = new TextView(activity);
        insideTv.setText("All apps");
        insideTv.setTextColor(Color.WHITE);
        insideTv.setTextSize(13.5f);
        insideTv.setTypeface(activity.getResources().getFont(R.font.ntype), android.graphics.Typeface.BOLD);
        insideTv.setSingleLine(true);
        insideTv.setVisibility(View.GONE);
        LinearLayout.LayoutParams insideLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        insideLp.setMarginStart((int) (12 * density));
        insideLp.setMarginEnd((int) (8 * density));
        insideTv.setLayoutParams(insideLp);
        pillContainer.addView(insideTv);

        item.addView(pillContainer);

        TextView bottomTv = new TextView(activity);
        bottomTv.setText("All apps");
        bottomTv.setTextColor(Color.parseColor("#CCCCCC"));
        bottomTv.setTextSize(10.5f);
        bottomTv.setTypeface(activity.getResources().getFont(R.font.ntype));
        bottomTv.setSingleLine(true);
        bottomTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(
                (int) (70 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomLp.topMargin = (int) (5 * density);
        bottomTv.setLayoutParams(bottomLp);
        item.addView(bottomTv);

        item.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                pillContainer.setBackgroundResource(R.drawable.nothing_pill_focused_ring);
                pillLp.width = (int) (180 * density);
                pillContainer.setLayoutParams(pillLp);
                iv.setColorFilter(Color.WHITE);
                insideTv.setVisibility(View.VISIBLE);
                bottomTv.setVisibility(View.INVISIBLE);
            } else {
                pillContainer.setBackgroundResource(R.drawable.nothing_circle_white);
                pillLp.width = (int) (58 * density);
                pillContainer.setLayoutParams(pillLp);
                iv.setColorFilter(Color.BLACK);
                insideTv.setVisibility(View.GONE);
                bottomTv.setVisibility(View.VISIBLE);
            }
        });

        item.setOnClickListener(v -> showScreen(R.id.screenAllApps));
        return item;
    }

    private void setupBottomActionBar(View layout) {
        View btnLiveTv = layout.findViewById(R.id.btnQuickLiveTv);
        View btnMedia = layout.findViewById(R.id.btnQuickMedia);
        View btnCustomize = layout.findViewById(R.id.btnQuickCustomize);
        View btnSettingsPill = layout.findViewById(R.id.btnQuickSettingsPill);

        btnLiveTv.setOnClickListener(v -> launchApp("com.mitv.livetv"));
        btnMedia.setOnClickListener(v -> launchApp("org.videolan.vlc"));
        btnCustomize.setOnClickListener(v -> showScreen(R.id.screenQuickSettings));
        btnSettingsPill.setOnClickListener(v -> showScreen(R.id.screenQuickSettings));

        View[] bottomPills = {btnLiveTv, btnMedia, btnCustomize, btnSettingsPill};
        for (View p : bottomPills) {
            if (p instanceof TextView) {
                TextView tv = (TextView) p;
                tv.setOnFocusChangeListener((v, hasFocus) -> {
                    tv.setBackgroundResource(hasFocus ? R.drawable.nothing_pill_white : R.drawable.nothing_pill_dark);
                    tv.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
                });
            }
        }
    }

    private void setupScreenAllApps(View layout) {
        allAppsClock = layout.findViewById(R.id.allAppsClock);
        appsCountText = layout.findViewById(R.id.appsCountText);
        if (appsCountText != null) {
            appsCountText.setText(allAppsList.size() + " apps / Hold OK for options");
        }

        allAppsRecyclerView = layout.findViewById(R.id.allAppsRecyclerView);
        allAppsRecyclerView.setLayoutManager(new GridLayoutManager(activity, 6));
        allAppsAdapter = new AllAppsAdapter(filteredAppsList, this::launchApp);
        allAppsRecyclerView.setAdapter(allAppsAdapter);

        // Category filter chips
        setupFilterChips(layout);
    }

    private void setupFilterChips(View layout) {
        TextView chipAll = layout.findViewById(R.id.filterAll);
        TextView chipVideo = layout.findViewById(R.id.filterVideo);
        TextView chipMusic = layout.findViewById(R.id.filterMusic);
        TextView chipUtils = layout.findViewById(R.id.filterUtilities);

        TextView[] chips = {chipAll, chipVideo, chipMusic, chipUtils};

        View.OnClickListener listener = v -> {
            for (TextView c : chips) {
                c.setBackgroundResource(R.drawable.nothing_pill_dark);
                c.setTextColor(Color.WHITE);
            }
            TextView sel = (TextView) v;
            sel.setBackgroundResource(R.drawable.nothing_pill_white);
            sel.setTextColor(Color.BLACK);

            String cat = sel.getText().toString().toLowerCase(Locale.ROOT);
            filterAppsByCategory(cat);
        };

        for (TextView c : chips) {
            if (c != null) c.setOnClickListener(listener);
        }
    }

    private void filterAppsByCategory(String cat) {
        filteredAppsList.clear();
        if ("all".equals(cat)) {
            filteredAppsList.addAll(allAppsList);
        } else {
            for (AppEntry a : allAppsList) {
                if (cat.equals(a.category)) {
                    filteredAppsList.add(a);
                }
            }
        }
        if (allAppsAdapter != null) allAppsAdapter.notifyDataSetChanged();
        if (appsCountText != null) {
            appsCountText.setText(filteredAppsList.size() + " apps / Hold OK for options");
        }
    }

    private void setupScreenInputs(View layout) {
        inputsClock = layout.findViewById(R.id.inputsClock);

        View liveTv = layout.findViewById(R.id.inputCardLiveTv);
        View hdmi1 = layout.findViewById(R.id.inputCardHdmi1);
        View hdmi2 = layout.findViewById(R.id.inputCardHdmi2);
        View hdmi3 = layout.findViewById(R.id.inputCardHdmi3);
        View usb = layout.findViewById(R.id.inputCardUsb);
        View cast = layout.findViewById(R.id.inputCardCast);

        if (liveTv != null) liveTv.setOnClickListener(v -> launchApp("com.mitv.livetv"));
        if (hdmi1 != null) hdmi1.setOnClickListener(v -> Toast.makeText(activity, "Switched to HDMI 1", Toast.LENGTH_SHORT).show());
        if (hdmi2 != null) hdmi2.setOnClickListener(v -> Toast.makeText(activity, "HDMI 2 (No Signal)", Toast.LENGTH_SHORT).show());
        if (hdmi3 != null) hdmi3.setOnClickListener(v -> Toast.makeText(activity, "HDMI 3 (No Signal)", Toast.LENGTH_SHORT).show());
        if (usb != null) usb.setOnClickListener(v -> launchApp("org.videolan.vlc"));
        if (cast != null) cast.setOnClickListener(v -> {
            try {
                activity.startActivity(new Intent("android.settings.CAST_SETTINGS"));
            } catch (Exception e) {
                Toast.makeText(activity, "Google Cast", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup focus highlight on each input card
        View[] cards = {liveTv, hdmi1, hdmi2, hdmi3, usb, cast};
        for (View c : cards) {
            if (c != null) {
                c.setOnFocusChangeListener((v, hasFocus) -> {
                    v.setBackgroundResource(hasFocus ? R.drawable.nothing_card_focused : R.drawable.nothing_card_bg);
                    setChildTextColor(v, hasFocus ? Color.BLACK : Color.WHITE);
                });
            }
        }
    }

    private void setupScreenQuickSettings(View layout) {
        quickSettingsClock = layout.findViewById(R.id.quickSettingsClock);
        volumeProgressBar = layout.findViewById(R.id.volumeProgressBar);
        volumeNumText = layout.findViewById(R.id.volumeNumText);

        View toggleWifi = layout.findViewById(R.id.toggleWifi);
        View toggleBt = layout.findViewById(R.id.toggleBt);
        View toggleInput = layout.findViewById(R.id.toggleInput);
        View toggleCast = layout.findViewById(R.id.toggleCast);
        View togglePic = layout.findViewById(R.id.togglePic);
        View toggleSound = layout.findViewById(R.id.toggleSound);
        View toggleTimer = layout.findViewById(R.id.toggleTimer);
        View togglePower = layout.findViewById(R.id.togglePower);

        if (toggleWifi != null) toggleWifi.setOnClickListener(v -> {
            try {
                activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) {
                try { activity.startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (Exception ignored) {}
            }
        });

        if (toggleBt != null) toggleBt.setOnClickListener(v -> {
            try {
                Intent i = new Intent("com.google.android.intent.action.CONNECT_INPUT");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
                return;
            } catch (Exception ignored) {}
            try {
                Intent i = new Intent();
                i.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.accessories.AddAccessoryActivity"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
                return;
            } catch (Exception ignored) {}
            try {
                Intent i = new Intent(Settings.ACTION_SETTINGS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
            } catch (Exception e) {
                Toast.makeText(activity, "Bluetooth", Toast.LENGTH_SHORT).show();
            }
        });

        if (toggleInput != null) toggleInput.setOnClickListener(v -> showScreen(R.id.screenInputs));
        if (toggleCast != null) toggleCast.setOnClickListener(v -> {
            try { activity.startActivity(new Intent("android.settings.CAST_SETTINGS")); } catch (Exception ignored) {}
        });
        if (togglePic != null) togglePic.setOnClickListener(v -> {
            try {
                activity.startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
            } catch (Exception e) {
                try { activity.startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (Exception ignored) {}
            }
        });
        if (toggleSound != null) toggleSound.setOnClickListener(v -> {
            try {
                activity.startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
            } catch (Exception e) {
                try { activity.startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (Exception ignored) {}
            }
        });
        if (togglePower != null) togglePower.setOnClickListener(v -> Toast.makeText(activity, "Power Standby", Toast.LENGTH_SHORT).show());

        View btnAllSettings = layout.findViewById(R.id.btnAllSettings);
        if (btnAllSettings != null) {
            btnAllSettings.setOnClickListener(v -> {
                try {
                    activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
                } catch (Exception ignored) {}
            });
        }

        View volumeBar = layout.findViewById(R.id.volumeBarContainer);
        if (volumeBar != null) {
            volumeBar.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                        updateVolumeSlider();
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                        updateVolumeSlider();
                        return true;
                    }
                }
                return false;
            });
        }

        View[] toggles = {toggleWifi, toggleBt, toggleInput, toggleCast, togglePic, toggleSound, toggleTimer, togglePower};
        for (View t : toggles) {
            if (t != null) {
                t.setOnFocusChangeListener((v, hasFocus) -> {
                    v.setBackgroundResource(hasFocus ? R.drawable.nothing_card_focused : R.drawable.nothing_card_bg);
                    setChildTextColor(v, hasFocus ? Color.BLACK : Color.WHITE);
                });
            }
        }
    }

    private void updateVolumeSlider() {
        try {
            AudioManager am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int cur = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            int pct = (int) ((cur / (float) max) * 100);
            if (volumeProgressBar != null) volumeProgressBar.setProgress(pct);
            if (volumeNumText != null) volumeNumText.setText(String.valueOf(pct));
        } catch (Exception ignored) {}
    }

    private void startRealtimeUpdater() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateClocks();
                updateCalendarAndPrayers(false);
                handler.postDelayed(this, 1000L); // Real-time 1-second update
            }
        });
    }

    private void updateClocks() {
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String curTime = timeFmt.format(new Date());

        if (allAppsClock != null) allAppsClock.setText(curTime);
        if (inputsClock != null) inputsClock.setText(curTime);
        if (quickSettingsClock != null) quickSettingsClock.setText(curTime);
    }

    private void setChildTextColor(View parent, int color) {
        if (parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            for (int i = 0; i < vg.getChildCount(); i++) {
                setChildTextColor(vg.getChildAt(i), color);
            }
        } else if (parent instanceof TextView) {
            ((TextView) parent).setTextColor(color);
        } else if (parent instanceof ImageView) {
            ((ImageView) parent).setColorFilter(color);
        }
    }

    private void launchApp(String packageName) {
        try {
            if ("com.mitv.livetv".equals(packageName)) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.mitv.livetv", "com.mitv.livetv.tv.MainActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return;
            }
            Intent launchIntent = activity.getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
            if (launchIntent == null) {
                launchIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
            }
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(launchIntent);
            } else {
                Toast.makeText(activity, "App not found: " + packageName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Failed to launch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------------------
    // RecyclerView Adapter for ALL APPS (Screen 02)
    // ------------------------------------------------------------------------
    private class AllAppsAdapter extends RecyclerView.Adapter<AllAppsAdapter.ViewHolder> {
        private final List<AppEntry> list;
        private final AppClickListener listener;

        AllAppsAdapter(List<AppEntry> list, AppClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout item = new LinearLayout(activity);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setFocusable(true);
            item.setClickable(true);
            item.setBackgroundResource(R.drawable.nothing_card_bg);

            int p = (int) (10 * density);
            item.setPadding(p, p, p, p);

            GridLayoutManager.LayoutParams lp = new GridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) (86 * density));
            lp.setMargins((int) (5 * density), (int) (5 * density), (int) (5 * density), (int) (5 * density));
            item.setLayoutParams(lp);

            ImageView iv = new ImageView(activity);
            iv.setLayoutParams(new LinearLayout.LayoutParams((int) (38 * density), (int) (38 * density)));
            item.addView(iv);

            TextView tv = new TextView(activity);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(11f);
            tv.setTypeface(activity.getResources().getFont(R.font.ntype));
            tv.setGravity(Gravity.CENTER);
            tv.setSingleLine(true);
            tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.topMargin = (int) (6 * density);
            tv.setLayoutParams(tlp);
            item.addView(tv);

            return new ViewHolder(item, iv, tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppEntry app = list.get(position);
            holder.title.setText(app.label);
            holder.icon.setImageDrawable(getNothingVectorIcon(app.packageName, app.icon));

            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                v.setBackgroundResource(hasFocus ? R.drawable.nothing_card_focused : R.drawable.nothing_card_bg);
                holder.title.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
                holder.icon.setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
                holder.itemView.setScaleX(hasFocus ? 1.05f : 1.0f);
                holder.itemView.setScaleY(hasFocus ? 1.05f : 1.0f);
            });

            holder.itemView.setOnClickListener(v -> listener.onClick(app.packageName));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;
            ViewHolder(View itemView, ImageView icon, TextView title) {
                super(itemView);
                this.icon = icon;
                this.title = title;
            }
        }
    }

    interface AppClickListener {
        void onClick(String packageName);
    }
}
