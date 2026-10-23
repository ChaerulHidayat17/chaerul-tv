#!/usr/bin/env bash
# Restore Xiaomi MiTV to Factory Defaults
TV_IP="${1:-192.168.1.12}"

echo "Connecting to TV $TV_IP:5555..."
adb connect "$TV_IP:5555"

echo "Re-enabling all stock packages..."
PACKAGES=(
  "com.google.android.apps.tv.launcherx"
  "com.google.android.tungsten.setupwraith"
  "com.mitv.tvhome.atv"
  "com.miui.tv.analytics"
  "com.xiaomi.statistic"
  "com.xiaomi.mitv.tvmanager"
  "com.mitv.tvhome.mitvplus"
  "com.mitv.tvhome.michannel"
  "com.mitv.tvhome.oemtab"
  "com.mitv.overseaservice"
  "com.xiaomi.floatingframe"
  "com.xiaomi.mi_connect_service"
  "com.mitv.toolhouse"
  "com.mitv.ABUpdateStatus"
  "com.xiaomi.android.tvsetup.partnercustomizer"
  "com.xiaomi.mitv.detection"
  "com.mitv.setup"
)

for pkg in "${PACKAGES[@]}"; do
  adb -s "$TV_IP:5555" shell pm enable "$pkg"
done

echo "Restoring default Google TV launcher role..."
adb -s "$TV_IP:5555" shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.MainActivity
adb -s "$TV_IP:5555" shell cmd role add-role-holder android.app.role.HOME com.google.android.apps.tv.launcherx

echo "Starting stock home screen..."
adb -s "$TV_IP:5555" shell am start -n com.google.android.apps.tv.launcherx/.MainActivity
adb -s "$TV_IP:5555" shell input keyevent KEYCODE_HOME

echo "Done! TV restored to stock factory settings."
