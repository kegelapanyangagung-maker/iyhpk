# Market Digital Pay Bridge (Android source)

Aplikasi Android ini membaca notifikasi dari paket `com.gojek.gopaymerchant` melalui Android `NotificationListenerService`, lalu mengirim notifikasi bertajuk pembayaran diterima ke endpoint bot menggunakan HTTPS dan header `x-webhook-secret`. Implementasi NotificationListenerService mengikuti API Android resmi: https://developer.android.com/reference/android/service/notification/NotificationListenerService.html

## Build APK
1. Ekstrak ZIP dan buka folder ini di Android Studio (Hedgehog atau lebih baru).
2. Tunggu Gradle Sync selesai.
3. Pilih Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install APK di HP yang menerima notifikasi GoPay Merchant.
5. Buka aplikasi, isi URL `https://webhook.marketdigital.web.id/webhook/payment` dan secret yang sama dengan `paymentWebhook.secret` pada `config.js` bot.
6. Simpan, lalu tekan Aktifkan akses notifikasi dan izinkan Market Digital Pay Bridge.
7. Pastikan notifikasi GoPay Merchant aktif dan pengecualian optimasi baterai diatur agar service tetap berjalan.

## Penting
- APK belum dibangun di lingkungan ini; ZIP berisi source project Android Studio.
- Listener hanya meneruskan judul yang memuat "pembayaran" dan "diterima" dari paket GoPay Merchant.
- Nominal harus sama persis dengan nominal unik yang ditampilkan bot ketika order dibuat. Patch bot menambahkan kode unik Rp101-Rp999 pada setiap order. Pembeli harus membayar nominal penuh tersebut.
- Jangan gunakan untuk menganggap notifikasi promosi sebagai pembayaran.
- Notifikasi perangkat bukan pengganti rekonsiliasi resmi dari provider. Uji dengan transaksi kecil terlebih dahulu.
