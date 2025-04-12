# IF3210-2025-MAD-TAZ - Purrytify

## Deskripsi Aplikasi

Purrytify adalah aplikasi yang memungkinkan pengguna untuk dengan mudah memutar musik dan menambahkan lagu-lagu baru ke dalam koleksi mereka. Aplikasi ini dibangun menggunakan bahasa pemrograman [Kotlin](https://kotlinlang.org/) dengan arsitektur [MVVM *(Model-View-ViewModel*)](https://medium.com/@MuhammadYaumilRamadhani/architecture-pattern-mvvm-android-c48c9d77fccf) untuk memisahkan logika bisnis dari tampilan. Selain itu, Purrytify dilengkapi dengan berbagai fitur seperti pemutaran lagu yang canggih, pengelolaan daftar lagu yang dimiliki, ~~antrian, pengulangan, acak~~, dan pencarian lagu. Pengguna juga dapat menyimpan dan mengelola lagu-lagu yang disukai, dengan fitur yang memungkinkan mereka menandai lagu favorit agar lebih mudah ditemukan kapan saja.

## Library yang Digunakan

Berikut adalah library yang digunakan dalam pengembangan aplikasi ini:

### UI & Komponennya

* **AndroidX AppCompat:** Untuk kompatibilitas UI di berbagai versi Android
* **AndroidX Material & Material3:** Untuk implementasi komponen Material Design
* **AndroidX Compose:** Framework UI modern berbasis deklaratif
* **AndroidX ConstraintLayout:** Untuk layout yang kompleks dan responsif
* **AndroidX RecyclerView & CardView:** Untuk menampilkan daftar lagu secara efisien
* **AndroidX Core Splashscreen:** Untuk implementasi splash screen modern
* **AndroidX Material Icons Extended:** Koleksi ikon tambahan untuk UI
* **AndroidX Navigation Compose:** Untuk navigasi antar layar dalam aplikasi Compose

### Network & Data

* **Retrofit & Converter GSON:** Untuk melakukan network requests ke backend API
* **OkHttp & Logging Interceptor:** HTTP client dan logging untuk Retrofit
* **Room Persistence Library:** Database SQLite untuk menyimpan metadata lagu secara lokal
* **AndroidX Security Crypto:** Untuk implementasi encrypted shared preferences

### Image Loading

* **Glide:** Library untuk memuat dan caching gambar secara efisien
* **Coil:** Image loader modern khusus untuk Jetpack Compose

### Concurrency & Asynchronous

* **Kotlin Coroutines Core & Android:** Untuk manajemen asynchronous tasks dan background service

### Dependency Injection

* **Hilt Android:** Framework untuk dependency injection di Android
* **AndroidX Hilt Navigation Compose:** Integrasi Hilt dengan Navigation Compose

### Komponen Tambahan

* **AndroidX Fragment KTX:** Extension Kotlin untuk Fragment
* **AndroidX Lifecycle Runtime KTX:** Untuk implementasi komponen yang aware terhadap lifecycle
* **AndroidX Core KTX:** Extension Kotlin untuk API Android

## Screenshot Aplikasi

| Keterangan | Gambar |
|:----------:|:------:|
| Login | ![Login](/screenshots/LoginScreen.png) |
| Home | ![Home](/screenshots/HomeScreen.jpg) |
| Library + Mini Player | ![Library](/screenshots/LibraryScreen+MiniPlayer.jpg) |
| Profile | ![Profile](/screenshots/ProfileScreen.jpg) |
| Music Player | ![Music Player](/screenshots/MusicPlayer.jpg) |
| Add New Music | ![Add New Music (empty)](/screenshots/EmptyAddSong.jpg) ![Add New Music](/screenshots/AddNewSong.jpg)|
| Music Option | ![Edit Music](/screenshots/MusicOption.jpg) |
| Edit Music Info | ![Edit Music](/screenshots/EditSong.jpg) |

## Pembagian Kerja Anggota Kelompok

|            Fitur           |    NIM   |
|:--------------------------:|:--------:|
|           Navbar           | 13522032 |
|       Login & Logout       | 13522032 |
|            Home            | 13522114 |
|           Library          | 13522114 |
| Music Player + Mini Player | 13522004 |
|        Add new Music       | 13522114 |
|           Profile          | 13522032 |
|         Liked Songs        | 13522114 |
|     Background Service     | 13522032 |
|       Network Sensing      | 13522032 |
|            Queue           | 13522004 |
|           Shuffle          | 13522004 |
|           Repeat           | 13522004 |
|            OWASP           |          |
|           Search           | 13522114 |

## Jumlah Jam Persiapan dan Pengerjaan

* **13522004 | Eduardus Alvito Kristiadi:**
  * Persiapan: 10 jam
  * Pengerjaan: 20 jam
* **13522032 | Tazkia Nizami:**
  * Persiapan: 12 jam
  * Pengerjaan: 40 jam
* **13522114 | Muhammad Dava Fathurrahman:**
  * Persiapan: 10 jam
  * Pengerjaan: 20 jam

---
