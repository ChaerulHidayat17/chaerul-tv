# 📺 PANDUAN LENGKAP INSTALASI & DEPLOYMENT CHAERUL (TV)
### *Custom Industrial 10-Foot UI Launcher for Android TV & Google TV*

Dokumentasi ini disusun sebagai standar operasional prosedur (SOP) untuk memasang dan mengonfigurasi launcher **CHAERUL (TV)** di Smart TV atau Android TV Box milik orang lain/klien/keluarga dari nol sampai siap pakai.

---

## 📋 DAFTAR ISI
1. [Tentang CHAERUL (TV)](#1-tentang-chaerul-tv)
2. [Spesifikasi & Kompatibilitas](#2-spesifikasi--kompatibilitas)
3. [File Installer yang Dibutuhkan](#3-file-installer-yang-dibutuhkan)
4. [Persiapan di TV Target (Wajib)](#4-persiapan-di-tv-target-wajib)
5. [METODE A: Pasang via Flashdisk / File Manager (Paling Mudah)](#5-metode-a-pasang-via-flashdisk--file-manager-paling-mudah)
6. [METODE B: Pasang via Wi-Fi ADB Laptop (Profesional & Tanpa Sentuh TV)](#6-metode-b-pasang-via-wi-fi-adb-laptop-profesional--tanpa-sentuh-tv)
7. [Menjadikan CHAERUL (TV) sebagai Default Home Launcher](#7-menjadikan-chaerul-tv-sebagai-default-home-launcher)
8. [Trik Khusus: Nonaktifkan Iklan & Launcher Bawaan TV (Xiaomi / Google TV)](#8-trik-khusus-nonaktifkan-iklan--launcher-bawaan-tv)
9. [Cara Kembalikan ke Semula (Rollback / Uninstall)](#9-cara-kembalikan-ke-semula-rollback--uninstall)
10. [Troubleshooting & Solusi Kendala](#10-troubleshooting--solusi-kendala)

---

## 1. TENTANG CHAERUL (TV)

**CHAERUL (TV)** adalah antarmuka kustom (launcher) TV berbasis Android yang mengadopsi estetika industrial modern Nothing OS:
* **True Black OLED Background (`#000000`):** Menghemat konsumsi daya TV dan sangat nyaman di mata saat malam hari.
* **Tipografi Otentik Dot-Matrix (`Ndot 57` & `NType 82`):** Tampilan futuristik kelas atas.
* **Jadwal Sholat Astronomis 5 Waktu:** Bekerja 100% offline tanpa perlu koneksi internet.
* **Dock Aplikasi Responsif:** Ikon monokrom bersih dengan efek kapsul merah menyala (*Nothing Red Pill*) saat dipilih remote.
* **4 Layar Terintegrasi:** `01 / HOME`, `02 / ALL APPS`, `03 / INPUTS`, dan `04 / QUICK SETTINGS`.
* **Zero Bloatware & Tanpa Iklan:** Berukuran sangat ringan (~2.1 MB) dan responsif bahkan di TV dengan RAM 1 GB.

---

## 2. SPESIFIKASI & KOMPATIBILITAS

* **Sistem Operasi Target:** Android TV 9.0 (Pie), 10 (Q), 11 (R), 12 (S), 13 (T), hingga 14 (U) / Google TV.
* **Merek TV yang Didukung:**
  * Xiaomi Mi TV, TV A Pro, Redmi TV.
  * Google TV / Chromecast with Google TV (HD & 4K).
  * Sony BRAVIA Android TV.
  * TCL, Coocaa, Realme, Toshiba, Sharp, Polytron Android TV.
  * Set-Top Box: Mi Box S, Mecool, X96, TX3, IndiHome/ZTE B860H (yang sudah di-unlock).
* **Arsitektur CPU:** Universal (`armeabi-v7a` 32-bit & `arm64-v8a` 64-bit).
* **Kebutuhan RAM Minimum:** 1 GB (Sangat lancar di RAM 1.5 GB / 2 GB).

---

## 3. FILE INSTALLER YANG DIBUTUHKAN

Pastikan kamu sudah memiliki file installer master:
* **Nama File:** `CHAERUL_TV_v1.0.apk` (Ukuran: ~2.09 MB).
* **Lokasi di Laptop:** `C:\Users\Chaerul\Desktop\CHAERUL_TV_v1.0.apk`.

---

## 4. PERSIAPAN DI TV TARGET (WAJIB)

Sebelum memasang aplikasi pihak ketiga di TV orang lain, aktifkan mode pengembang:

### Langkah Mengaktifkan Developer Options & USB Debugging:
1. Nyalakan TV target.
2. Buka menu **Setelan (Settings)** di pojok kanan atas TV.
3. Pilih **Preferensi Perangkat (Device Preferences)** $\rightarrow$ **Tentang (About)**.  
   *(Pada Google TV: Pilih **Sistem** $\rightarrow$ **Tentang**)*.
4. Gulir ke bawah sampai menemukan baris **Build OS Android TV** (atau **Build**).
5. **Tekan tombol OK di remote TV sebanyak 7 kali berturut-turut.**
6. Akan muncul pesan popup di layar: *"Sekarang Anda adalah seorang developer!"*.
7. Tekan tombol **Back** satu kali, lalu pilih menu baru yang muncul: **Opsi Pengembang (Developer Options)**.
8. Cari menu **Debugging USB (USB Debugging)** $\rightarrow$ Geser ke posisi **AKTIF (ON)**.

---

## 5. METODE A: PASANG VIA FLASHDISK / FILE MANAGER (PALING MUDAH)

Gunakan metode ini jika kamu langsung berada di depan TV dan membawa USB Flashdisk.

1. **Salin File:** Colokkan Flashdisk ke laptop, salin file `CHAERUL_TV_v1.0.apk` ke dalam Flashdisk.
2. **Colok ke TV:** Lepaskan Flashdisk dari laptop dan tancapkan ke port USB Smart TV target.
3. **Buka File Manager:** 
   * Buka aplikasi File Manager bawaan TV (atau pasang *AnExplorer* / *FX File Explorer* gratis dari Google Play Store TV).
4. **Izinkan Sumber Tidak Dikenal:**
   * Klik file `CHAERUL_TV_v1.0.apk`.
   * Jika TV menampilkan peringatan keamanan, pilih **Setelan (Settings)** $\rightarrow$ Beri izin centang pada File Manager untuk **Menginstal Aplikasi dari Sumber Tidak Dikenal (Unknown Sources)**.
5. **Instal Aplikasi:**
   * Klik **Instal (Install)** dan tunggu 5 detik hingga status bertuliskan **Selesai (Done)**.

---

## 6. METODE B: PASANG VIA WI-FI ADB LAPTOP (PROFESIONAL & TANPA SENTUH TV)

Gunakan metode ini jika kamu ingin memasang APK dari laptop secara hening tanpa mengganggu orang yang sedang menonton TV.

### Syarat:
Laptop dan TV target **harus terhubung ke jaringan Wi-Fi yang sama**.

### Langkah-langkah:
1. **Cek Alamat IP TV Target:**
   * Di TV, buka **Setelan** $\rightarrow$ **Jaringan & Internet**.
   * Klik Wi-Fi yang sedang terhubung, lalu catat **Alamat IP** (Contoh: `192.168.1.50`).

2. **Buka Terminal / CMD di Laptop:**
   * Tekan tombol `Windows + R`, ketik `cmd`, lalu tekan **Enter**.

3. **Sambungkan ADB ke TV:**
   Ketik perintah berikut (ganti IP sesuai IP TV target):
   ```bash
   adb connect 192.168.1.50:5555
   ```

4. **Konfirmasi Popup di TV (Hanya Muncul Sekali):**
   * Di layar TV akan muncul popup: *"Izinkan USB Debugging dari komputer ini?"*.
   * Centang kotak **"Selalu izinkan dari komputer ini"**, lalu pilih **OK**.

5. **Kirim & Pasang APK Secara Otomatis:**
   Ketik perintah ini di terminal laptop:
   ```bash
   adb install -r -d "C:\Users\Chaerul\Desktop\CHAERUL_TV_v1.0.apk"
   ```
   *Tunggu sekitar 5–10 detik sampai muncul tulisan:*
   ```text
   Performing Streamed Install
   Success
   ```

---

## 7. MENJADIKAN CHAERUL (TV) SEBAGAI DEFAULT HOME LAUNCHER

Agar setiap kali TV dinyalakan atau tombol **HOME** di remote ditekan antarmuka CHAERUL (TV) langsung muncul:

1. Ambil remote TV target, lalu **tekan tombol HOME**.
2. Sistem Android TV akan menampilkan dialog pilihan:
   * **CleanYourTV / TvHome (CHAERUL TV)**
   * **Android TV Home / Google TV**
3. Sorot ke **TvHome (CHAERUL TV)**, lalu pilih tombol **"SELALU" (ALWAYS)**.
4. Selesai! TV sekarang secara permanen menggunakan launcher CHAERUL (TV).

---

## 8. TRIK KHUSUS: NONAKTIFKAN IKLAN & LAUNCHER BAWAAN TV

Beberapa TV seperti **Xiaomi (PatchWall)** atau **Google TV** terkadang suka memaksa membuka tampilan bawaan mereka yang penuh iklan dan video sponsor. 

Untuk mengunci TV agar **100% bersih, super kencang, dan hanya membuka CHAERUL (TV)**, jalankan trik ADB berikut di laptop:

### A. Untuk TV Xiaomi (Mi TV / Redmi TV):
Matikan launcher PatchWall dan launcherx bawaan Xiaomi yang berat:
```bash
adb shell pm disable-user --user 0 com.mitv.tvhome
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
```

### B. Untuk Google TV Merek Lain (TCL / Sony / Realme / Chromecast):
Matikan home screen Google TV yang penuh banner iklan:
```bash
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
```

*Begitu perintah di atas dijalankan, remote TV tidak akan pernah lagi membuka tampilan iklan bawaan, dan tombol HOME 100% responsif membuka CHAERUL (TV) secara instan!*

---

## 9. CARA KEMBALIKAN KE SEMULA (ROLLBACK / UNINSTALL)

Jika suatu saat pemilik TV ingin mengembalikan ke tampilan pabrikan aslinya, prosesnya sangat mudah, aman, dan tanpa merusak data:

1. **Aktifkan Kembali Launcher Bawaan:**
   Ketik perintah ini di laptop:
   ```bash
   adb shell pm enable com.google.android.apps.tv.launcherx
   adb shell pm enable com.mitv.tvhome
   ```
2. **Hapus Aplikasi CHAERUL (TV):**
   ```bash
   adb uninstall com.gathin.tvhome
   ```
3. Tekan tombol **HOME** di remote TV $\rightarrow$ TV akan kembali 100% seperti setelan pabrik semula.

---

## 10. TROUBLESHOOTING & SOLUSI KENDALA

### Q1: Muncul pesan `cannot connect to 192.168.1.XX:5555: Connection refused`
* **Solusi:** Pastikan opsi **USB Debugging** di TV sudah aktif. Jika masih ditolak, sambungkan TV sementara menggunakan kabel USB ke laptop satu kali, jalankan `adb tcpip 5555`, lalu cabut kabel dan ulangi koneksi via Wi-Fi.

### Q2: Tombol Home remote masih membuka launcher lama
* **Solusi:** Jalankan perintah penonaktifan launcher bawaan di **Bagian 8**. Jika tidak memakai laptop, buka **Setelan TV** $\rightarrow$ **Aplikasi** $\rightarrow$ **Akses Aplikasi Khusus** $\rightarrow$ **Aplikasi Beranda (Home App)** $\rightarrow$ Pilih **TvHome**.

### Q3: Siaran Live TV tidak terbuka saat tombol Live TV diklik
* **Solusi:** Di TV selain Xiaomi, aplikasi TV bawaan memiliki nama paket berbeda (misal `com.tcl.tv` atau `com.google.android.tv`). Pemilik TV cukup membuka aplikasi siaran TV mereka dari menu **02 / CHAERUL APPS**.

---

*Dokumentasi Resmi Sistem — CHAERUL (TV) v1.0*  
*Author & System Architect: Chaerul Hidayat*
