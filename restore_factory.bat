@echo off
title Restore Xiaomi MiTV to Factory Defaults
cls
echo ====================================================================
echo        RESTORE XIAOMI SMART TV TO STOCK FACTORY SETTINGS            
echo ====================================================================
echo.

set /p TV_IP="Masukkan IP TV Xiaomi (default 192.168.1.12): "
if "%TV_IP%"=="" set TV_IP=192.168.1.12

echo.
echo [1/4] Menghubungkan ke TV %TV_IP%:5555...
adb connect %TV_IP%:5555

echo.
echo [2/4] Mengaktifkan kembali semua aplikasi bawaan Google TV & Xiaomi...
adb -s %TV_IP%:5555 shell "pm enable com.google.android.apps.tv.launcherx"
adb -s %TV_IP%:5555 shell "pm enable com.google.android.tungsten.setupwraith"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.tvhome.atv"
adb -s %TV_IP%:5555 shell "pm enable com.miui.tv.analytics"
adb -s %TV_IP%:5555 shell "pm enable com.xiaomi.statistic"
adb -s %TV_IP%:5555 shell "pm enable com.xiaomi.mitv.tvmanager"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.tvhome.mitvplus"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.tvhome.michannel"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.tvhome.oemtab"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.overseaservice"
adb -s %TV_IP%:5555 shell "pm enable com.xiaomi.floatingframe"
adb -s %TV_IP%:5555 shell "pm enable com.xiaomi.mi_connect_service"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.toolhouse"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.ABUpdateStatus"
adb -s %TV_IP%:5555 shell "pm enable com.xiaomi.android.tvsetup.partnercustomizer"
adb -s %TV_IP%:5555 shell "pm enable com.xiaomi.mitv.detection"
adb -s %TV_IP%:5555 shell "pm enable com.mitv.setup"

echo.
echo [3/4] Mengembalikan Launcher Bawaan Google TV / PatchWall...
adb -s %TV_IP%:5555 shell "cmd package set-home-activity com.google.android.apps.tv.launcherx/.MainActivity"
adb -s %TV_IP%:5555 shell "cmd role add-role-holder android.app.role.HOME com.google.android.apps.tv.launcherx"

echo.
echo [4/4] Membuka launcher bawaan di layar TV...
adb -s %TV_IP%:5555 shell "am start -n com.google.android.apps.tv.launcherx/.MainActivity"
adb -s %TV_IP%:5555 shell "input keyevent KEYCODE_HOME"

echo.
echo ====================================================================
echo SUKSES! TV Xiaomi kamu sudah kembali 100%% ke setelan bawaan pabrik!
echo ====================================================================
pause
