# Smart Home App 

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?&logo=kotlin)

**Smart Home App** adalah aplikasi Android modern berbasis *Clean Architecture* yang dirancang untuk mengontrol dan memantau perangkat IoT rumah pintar secara *real-time*. Aplikasi ini terintegrasi langsung dengan broker fisik via protokol MQTT, dilengkapi enkripsi keamanan biometrik, otomatisasi berbasis sensor, penyimpanan lokal, hingga sistem monitoring performa hardware internal handphone.

---

## Kode Infrastruktur

Aplikasi ini dibangun dengan memenuhi standar fungsionalitas produksi yang komprehensif:

* **Kontrol & Monitoring IoT Real-Time :** Eksekusi saklar utama lampu, kendali tingkat kecerahan (*Dimmer Slider*), kustomisasi warna lampu RGB, serta live monitoring sensor suhu & kelembaban DHT melalui `MqttClientManager.kt`.
* **Otomasi Terjadwal & Timer :** Sistem pengaturan jadwal operasional perangkat elektronik otomatis yang terintegrasi langsung dengan database lokal Room (`ScheduleScreen.kt`).
* **Widget Home Screen Interaktif :** Akses cepat pantauan suhu dan tombol *shutdown* darurat langsung dari beranda HP menggunakan framework `Jetpack Glance` (`HomeWidgetProvider.kt`).
* **Adaptasi Tema Dinamis :** Manajemen *Dark Mode* dan *Light Mode* reaktif yang menyimpan preferensi visual pengguna secara persisten melalui `PreferencesManager.kt`.
* **Keamanan Biometrik & Hak Akses :** Proteksi gerbang masuk aplikasi menggunakan sensor sidik jari (*Fingerprint*) atau wajah (*Face Unlock*) asli bawaan Android OS (`BiometricAuthUseCase.kt`) lengkap dengan pembatasan hak kontrol per ruangan (*User Roles*).
* **Ekspor Data Riwayat / Logging :**  konversi data log sensor lingkungan dari Room DB menjadi dokumen fisik `.csv` di folder penyimpanan publik HP (`ExportDataUseCase.kt`).
* **Pemantau Kesehatan Perangkat :** Dashboard internal untuk melacak performa real-time resource HP, membaca persentase penggunaan RAM, sisa kapasitas memori, hingga estimasi beban load inti CPU melalui `SystemHealthMonitor.kt`.
* **Mode Bioskop / Cinema Mode :** Skenario satu ketukan untuk meredupkan lampu dan mengubah warna RGB ke mode redup secara halus (*Fade-In/Out Effect*  43) untuk pengalaman menonton film.

---

## Teknologi & Library yang Digunakan

Aplikasi ini memanfaatkan ekosistem library Android modern untuk memastikan performa yang optimal dan arsitektur yang kokoh:

| Komponen / Library | Fungsi & Peran dalam Proyek |
| :--- | :--- |
| **Kotlin Coroutines & Flow** | Pondasi pemrograman asinkron untuk menangani aliran data *stream* IoT secara *real-time* tanpa membebani *thread* utama. |
| **Jetpack Compose & Material 3** | Desain UI deklaratif modern yang interaktif, responsif, serta didukung animasi perpindahan status yang halus. |
| **Eclipse Paho MQTT Client** | Library penanganan protokol komunikasi ringan *publish/subscribe* standar skala industri IoT. |
| **Android Room Database** | Penyimpanan lokal terstruktur yang aman untuk menjaga data riwayat sensor lingkungan dan alarm. |
| **Jetpack DataStore** | Pengganti *SharedPreferences* asinkron berbasis data transaksional untuk menyimpan preferensi enkripsi & tema *user*. |
| **Android Biometric API** | *Framework* otentikasi biometrik standar Google untuk mengakses *hardware* pemindai sidik jari/wajah bawaan perangkat. |
| **Jetpack Glance** | *Framework* berbasis Compose khusus untuk membangun *App Widget* di beranda OS Android yang hemat daya. |

---

##  Langkah Instalasi & Build
### 1. Buka di Android Studio
* Jalankan Android Studio Anda
* Pilih menu **Open an Existing Project**
* Arahkan ke folder `Smart-Home-app` hasil proses *clone*

### 2. Sinkronisasi Gradle
* Tunggu beberapa saat hingga Android Studio selesai mengunduh dan menyinkronkan seluruh *dependencies* yang tertera di file `app/build.gradle`

### 3. Konfigurasi MQTT Broker
* Buka file `MqttClientManager.kt`
* Sesuaikan nilai variabel `MQTT_BROKER_URL` dengan alamat server atau IP broker fisik yang Anda gunakan

### 4. Jalankan Proyek
* Hubungkan perangkat HP Android asli (Minimal Android 8.0 / API 26) melalui *USB Debugging*
* Klik tombol **Run** atau gunakan pintasan **Shift + F10**

#### Copyright 2026 Mr.Rm19 - ramdan19id@gmail.com - github.com/Rm19x
