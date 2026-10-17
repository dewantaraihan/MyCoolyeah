# MyCoolyeah - Perencanaan Implementasi Aplikasi Mahasiswa (Revisi 2)

MyCoolyeah adalah aplikasi Android native pengelola aktivitas akademik pribadi mahasiswa dengan antarmuka yang ramah, gaul, kekinian, dan menyenangkan. Rencana ini menetapkan arsitektur teknis, skema database Room lokal, rancangan UI 4 menu utama, dan tahapan implementasi tanpa backend/cloud.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Keputusan yang telah disepakati dan revisi yang telah diintegrasikan:

- **Confirmed Decision 1 (Tema & Estetika Visual)**: Menggunakan nuansa **Energetic Teal** (`#0D9488` / `#14B8A6`) berpadu dengan **Warm Amber** (`#F59E0B` / `#FBBF24`) cerah dan latar belakang bersih bernuansa dinamis. Gaya visual bernada gaul, santai, dan friendly khas mahasiswa.
- **Confirmed Decision 2 (Filter Semester vs. Semester Aktif)**:
  - Dropdown filter di Top Bar bersifat **view-only** (hanya mengubah tampilan layar yang sedang dilihat, **tidak** mengubah status semester aktif di database).
  - Pilihan awal dropdown otomatis mengarah ke **Semester Aktif**.
  - Untuk versi 1 (v1), opsi "Semua Semester" **ditiadakan**; dropdown hanya menampilkan daftar semester yang ada agar visual dan konteks akademik tetap terfokus.
- **Confirmed Decision 3 (Kondisi Awal, Onboarding, & Sapaan Adaptif)**:
  - Mulai dari kondisi **kosong (clean state)** dengan sambutan akrab dan panduan interaktif (onboarding card) yang memandu mahasiswa membuat profil dan semester pertama mereka.
  - **Logika Sapaan Home**: Bersifat adaptif terhadap ketersediaan profil.
    * *Jika profil belum diisi atau nama kosong*: Menggunakan sapaan ramah umum (misal: *"Semangat pagi, Teman Kuliah!"* atau *"Halo, Mahasiswa Keren! Kuliah santai tapi pasti"*).
    * *Jika nama sudah terisi*: Menggunakan nama panggilan pengguna (misal: *"Semangat pagi, [Nama]! Kuliah hari ini santai tapi pasti"*).
- **Confirmed Decision 4 (Single Source of Truth & Validasi Transaksi Semester Aktif)**:
  - Status aktif **hanya** disimpan pada kolom `isActive: Boolean` di tabel `Semester`.
  - Kolom `activeSemesterId` pada tabel `Profile` **dihapus** untuk menghindari duplikasi status atau risiko data bertentangan.
  - **Validasi Transaksi `setActiveSemester(semesterId)`**: Sebelum menonaktifkan semester lain, fungsi wajib memeriksa apakah semester tujuan ada di database (`getSemesterById(semesterId) != null`). Jika ID tidak valid atau tidak ditemukan, transaksi langsung dibatalkan (early return / no-op), sehingga status semester yang aktif sebelumnya tetap utuh dan tidak berubah menjadi tanpa semester aktif sama sekali.
- **Confirmed Decision 5 (Aturan Foreign Key CASCADE & Dialog Konfirmasi)**:
  - Relasi `Semester` -> `MataKuliah` dan `MataKuliah` -> (`JadwalKuliah`, `Tugas`, `Ujian`, `Materi`) menggunakan `ForeignKey.CASCADE`.
  - *Konsekuensi*: Menghapus semester akan menghapus seluruh mata kuliah dan aktivitas di dalamnya. Menghapus mata kuliah akan menghapus seluruh jadwal, tugas, ujian, dan materi terkait.
  - *Proteksi UI*: Setiap aksi hapus semester atau mata kuliah **wajib** memunculkan dialog konfirmasi bahaya dengan rincian dampak (misal: *"Menghapus Semester ini akan menghapus 6 mata kuliah, 12 jadwal, dan 8 tugas di dalamnya. Tetap hapus?"*).
- **Confirmed Decision 6 (Konsistensi 3 Status Tugas)**:
  - Tugas memiliki 3 status logika:
    1. **Selesai**: `isSelesai == true`
    2. **Belum Selesai**: `isSelesai == false && deadlineMillis >= currentTimeMillis()`
    3. **Terlewat**: `isSelesai == false && deadlineMillis < currentTimeMillis()`
  - Status *Terlewat* dihitung secara dinamis (computed state) saat data di-query/dirender, sehingga tidak memerlukan worker latar belakang yang berat untuk mengubah state secara manual.
- **Confirmed Decision 7 (Format Entitas Materi)**:
  - Mendukung dua jenis: **Dokumen Lokal** (`PDF`, `DOCX`, `PPTX` via SAF / Uri lokal) dan **Tautan YouTube** (`URL` video materi).
- **Confirmed Decision 8 (Notifikasi Lokal & Waktu Hari Kalender)**:
  - **Ringkasan Jadwal Kuliah**: Dikirim pukul **00.00 WIB** dengan kueri hari kalender yang **baru saja dimulai** (misal: saat transisi hari Minggu malam ke Senin 00.00, sistem mengambil jadwal untuk hari Senin yang baru dimulai, bukan hari Minggu yang telah lewat). Notifikasi hanya dikirim jika ada jadwal kelas pada hari tersebut.
  - **Pengingat Tugas H-1**: Dikirim pukul **19.00 WIB pada hari sebelum tenggat**, hanya jika tugas belum selesai.
  - **Pengingat Ujian H-1**: Dikirim pukul **19.00 WIB pada hari sebelum tanggal ujian** untuk semua kategori (`KUIS`, `UTS`, `UAS`).
  - **Manajemen Notifikasi**: Pengingat yang sudah tidak relevan harus dibatalkan atau diperbarui ketika tugas diselesaikan, atau ketika tugas/ujian diubah maupun dihapus.
  - Waktu pengingat menggunakan zona waktu lokal perangkat; waktu acuan default pada dokumentasi dan pengujian adalah WIB.

> [!NOTE]
> **Catatan Implementasi**:
> - **Penanganan File Dokumen Materi**: Pengambilan file PDF/DOCX/PPTX menggunakan Storage Access Framework Android (`ActivityResultContracts.OpenDocument`). URI file disimpan dengan izin baca persisten (`takePersistableUriPermission`). Jika file asli di memori perangkat dipindahkan/dihapus pengguna di luar aplikasi, aplikasi akan menampilkan status ramah *"Berkas tidak ditemukan di perangkat"*.

---

## 1. Overview & Core Concept

- **Tujuan Aplikasi**: Membantu mahasiswa mengatur kehidupan perkuliahan dengan santai, rapi, dan tepat waktu tanpa beban aplikasi yang kaku.
- **Target Pengguna**: Mahasiswa aktif yang menginginkan catatan kuliah offline, cepat, tanpa login, dan privasi penuh di perangkat.
- **Nilai Utama**: Mandiri (100% offline via Room), terstruktur, bebas distraksi iklan/cloud sync, dengan desain UI bernuansa ramah anak muda.

---

## 2. User Experience & Visual Design

### 2.1 Struktur Navigasi & App Shell

```
┌────────────────────────────────────────────────────────┐
│                   Top App Bar                          │
│   [Logo MyCoolyeah]            [Filter: Semester 5 ▼]  │
├────────────────────────────────────────────────────────┤
│                                                        │
│                    Content Area                        │
│                                                        │
│  [ Home ]      [ Jadwal ]      [ Tugas ]    [ Profil ] │
│  Ringkasan     Agenda Hari     Daftar Todo  Data Mhs   │
│  Kuliah Hari   & Kalender      & Timeline   & Kelola   │
│  Ini & Tugas   Mingguan        Kuis/Ujian   Semester   │
│                                                        │
├────────────────────────────────────────────────────────┤
│             Bottom Navigation Bar (M3)                 │
│   [🏠 Home]    [📅 Jadwal]    [📝 Tugas]   [👤 Profil] │
└────────────────────────────────────────────────────────┘
```

1. **Home Screen**:
   - *Greeting Card Adaptif*:
     - Profil kosong / Nama belum diisi: *"Semangat pagi, Teman Kuliah! Yuk mulai atur jadwal santaimu hari ini."*
     - Profil terisi: *"Semangat pagi, [Nama]! Kuliah hari ini santai tapi pasti."*
   - *Ringkasan Hari Ini*: Kartu mata kuliah hari ini dengan jam, ruangan, dan dosen.
   - *Tugas Mendesak*: Menampilkan tugas prioritas tinggi atau yang mendekati tenggat.
   - *Quick Action Floating Action Button (FAB)*: Tombol tambah cepat untuk Mata Kuliah, Jadwal, Tugas, atau Ujian.
2. **Jadwal Screen**:
   - *Day Selector Bar*: Tab hari Senin–Minggu dengan sorotan hari ini.
   - *Class Timeline List*: Kartu kelas berurutan dari jam paling pagi ke sore.
   - *Empty State*: *"Hari ini free, waktunya recharge energi!"*
3. **Tugas & Ujian Screen**:
   - *Dua Sub-Tab*:
     - **Tab Tugas**: Filter 3 status (Belum Selesai, Selesai, Terlewat). Terdapat checklist interaktif untuk menandai selesai.
     - **Tab Ujian**: Daftar evaluasi terbagi dalam 3 badge warna berbeda: `KUIS` (Amber), `UTS` (Teal), dan `UAS` (Indigo/Purple).
4. **Profil & Akademik Screen**:
   - *Header Profil*: Avatar, Nama, NIM, Universitas, Jurusan, dan Angkatan.
   - *Kelola Semester*: Daftar seluruh semester, tombol tambah semester, tombol aktifkan semester (dengan dialog konfirmasi).
   - *Kelola Mata Kuliah & Materi*: Manajemen matkul per semester beserta lampiran berkas lokal (PDF/DOCX/PPTX) dan tautan video YouTube.

### 2.2 Desain Visual, Warna & Tipografi

- **Palet Warna**:
  - `Primary`: Energetic Teal (`#0D9488` / `#14B8A6`)
  - `Secondary / Accent`: Warm Amber (`#F59E0B` / `#FBBF24`)
  - `Error / Overdue Tag`: Soft Crimson (`#EF4444`)
  - `Surface / Background`: Light Canvas (`#F8FAFC`) dengan kartu putih bersih (`#FFFFFF`) dan outline halus (`#E2E8F0`).
- **Komponen M3**: Kartu dengan sudut membulat 16.dp–20.dp, tonal elevation halus, badge status warna-warni, serta animasi transisi yang mulus.

---

## 3. Rencana Database Room & Relasi Antartabel

### 3.1 Skema Tabel (Entities)

```
┌─────────────────────────────────┐       ┌─────────────────────────────────┐
│          ProfileEntity          │       │         SemesterEntity          │
├─────────────────────────────────┤       ├─────────────────────────────────┤
│ id: Int = 1 (PK)                │       │ id: Long (PK AutoGenerate)      │
│ nama: String                    │       │ namaSemester: String            │
│ nim: String                     │       │ tahunAjaran: String             │
│ universitas: String             │       │ isActive: Boolean               │ ◄── Single Source of Truth
│ jurusan: String                 │       └────────────────┬────────────────┘
│ angkatan: String                │                        │ 1
│ fotoUri: String?                │                        │
└─────────────────────────────────┘                        │ N (CASCADE)
                                                           ▼
                                          ┌─────────────────────────────────┐
                                          │        MataKuliahEntity         │
                                          ├─────────────────────────────────┤
                                          │ id: Long (PK AutoGenerate)      │
                                          │ semesterId: Long (FK -> Sem.id) │
                                          │ nama: String                    │
                                          │ kode: String                    │
                                          │ sks: Int                        │
                                          │ namaDosen: String               │
                                          │ ruang: String                   │
                                          │ warnaLabel: Long                │
                                          └────────────────┬────────────────┘
                                                           │
               ┌───────────────────────────────────────────┼───────────────────────────────────────────┐
               │ 1:N (CASCADE)                             │ 1:N (CASCADE)                             │ 1:N (CASCADE)
               ▼                                           ▼                                           ▼
┌─────────────────────────────────┐       ┌─────────────────────────────────┐       ┌─────────────────────────────────┐
│       JadwalKuliahEntity        │       │           TugasEntity           │       │           UjianEntity           │
├─────────────────────────────────┤       ├─────────────────────────────────┤       ├─────────────────────────────────┤
│ id: Long (PK AutoGenerate)      │       │ id: Long (PK AutoGenerate)      │       │ id: Long (PK AutoGenerate)      │
│ mataKuliahId: Long (FK)         │       │ mataKuliahId: Long (FK)         │       │ mataKuliahId: Long (FK)         │
│ hari: Int (1=Senin..7=Minggu)   │       │ judul: String                   │       │ kategori: KategoriUjian         │
│ jamMulai: String (e.g. "08:00") │       │ deskripsi: String               │       │   -> [KUIS, UTS, UAS]           │
│ jamSelesai: String              │       │ deadlineMillis: Long            │       │ tanggalMillis: Long             │
│ ruang: String                   │       │ prioritas: String               │       │ jamMulai: String                │
│ catatan: String                 │       │ isSelesai: Boolean              │       │ jamSelesai: String              │
└─────────────────────────────────┘       └─────────────────────────────────┘       │ ruang: String                   │
                                                           │ 1:N (CASCADE)          │ catatan: String                 │
                                                           ▼                        └─────────────────────────────────┘
                                          ┌─────────────────────────────────┐
                                          │          MateriEntity           │
                                          ├─────────────────────────────────┤
                                          │ id: Long (PK AutoGenerate)      │
                                          │ mataKuliahId: Long (FK)         │
                                          │ pertemuanKe: Int                │
                                          │ judulMateri: String             │
                                          │ tipeMateri: TipeMateri          │
                                          │   -> [DOKUMEN_LOKAL, YOUTUBE]   │
                                          │ uriOrUrl: String                │
                                          │ catatanTambahan: String         │
                                          └─────────────────────────────────┘
```

### 3.2 Enum & Logika Status

1. **Enum Kategori Ujian**:
   ```kotlin
   enum class KategoriUjian { KUIS, UTS, UAS }
   ```
2. **Enum Tipe Materi**:
   ```kotlin
   enum class TipeMateri { DOKUMEN_LOKAL, YOUTUBE }
   ```
3. **Status Tugas**:
   ```kotlin
   enum class StatusTugas { BELUM_SELESAI, SELESAI, TERLEWAT }

   fun TugasEntity.getStatus(nowMillis: Long = System.currentTimeMillis()): StatusTugas {
       return when {
           isSelesai -> StatusTugas.SELESAI
           deadlineMillis < nowMillis -> StatusTugas.TERLEWAT
           else -> StatusTugas.BELUM_SELESAI
       }
   }
   ```

### 3.3 Validasi Transaksi Single Source of Truth Semester Aktif

Di dalam `SemesterDao`:
```kotlin
@Dao
interface SemesterDao {
    @Query("SELECT * FROM semester WHERE id = :semesterId LIMIT 1")
    suspend fun getSemesterById(semesterId: Long): SemesterEntity?

    @Query("UPDATE semester SET isActive = 0")
    suspend fun clearActiveSemester()

    @Query("UPDATE semester SET isActive = 1 WHERE id = :semesterId")
    suspend fun markActiveSemester(semesterId: Long)

    @Transaction
    suspend fun setActiveSemester(semesterId: Long): Boolean {
        // Validasi keberadaan semester: jika tidak ditemukan, jangan ubah status apa pun
        val target = getSemesterById(semesterId) ?: return false
        clearActiveSemester()
        markActiveSemester(target.id)
        return true
    }
}
```

---

## 4. Arsitektur & Struktur Pembagian File

Pola arsitektur menggunakan **Clean MVVM** (Model-View-ViewModel) dengan Repository Pattern.

```
com.example/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt
│   │   ├── dao/
│   │   │   ├── ProfileDao.kt
│   │   │   ├── SemesterDao.kt
│   │   │   ├── MataKuliahDao.kt
│   │   │   ├── JadwalKuliahDao.kt
│   │   │   ├── TugasDao.kt
│   │   │   ├── UjianDao.kt
│   │   │   └── MateriDao.kt
│   │   └── entity/
│   │       ├── ProfileEntity.kt
│   │       ├── SemesterEntity.kt
│   │       ├── MataKuliahEntity.kt
│   │       ├── JadwalKuliahEntity.kt
│   │       ├── TugasEntity.kt
│   │       ├── UjianEntity.kt
│   │       └── MateriEntity.kt
│   ├── model/
│   │   ├── KategoriUjian.kt
│   │   ├── TipeMateri.kt
│   │   ├── StatusTugas.kt
│   │   └── AcademicRelations.kt
│   └── repository/
│       ├── AcademicRepository.kt
│       └── AcademicRepositoryImpl.kt
├── notification/
│   ├── NotificationHelper.kt
│   ├── DailyScheduleWorker.kt
│   └── DeadlineReminderWorker.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── components/
│   │   ├── CoolyeahTopBar.kt
│   │   ├── CoolyeahBottomBar.kt
│   │   ├── SemesterFilterDropdown.kt
│   │   ├── EmptyStateCard.kt
│   │   └── ConfirmDeleteDialog.kt
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── jadwal/
│   │   │   ├── JadwalScreen.kt
│   │   │   └── JadwalViewModel.kt
│   │   ├── tugas/
│   │   │   ├── TugasScreen.kt
│   │   │   └── TugasViewModel.kt
│   │   └── profil/
│   │       ├── ProfilScreen.kt
│   │       └── ProfilViewModel.kt
│   └── navigation/
│       └── NavigationItem.kt
└── MainActivity.kt
```

---

## 5. Urutan Implementasi Bertahap yang Dapat Diverifikasi

Tahapan disusun secara berurutan dan terverifikasi di setiap tahap:

### Tahap 1: Setup Project & Kerangka UI
- Konfigurasi `metadata.json` ("MyCoolyeah") dan `res/values/strings.xml`.
- Implementasi tema Material 3 di `Color.kt` (Energetic Teal & Warm Amber) dan `Theme.kt`.
- Kerangka navigasi 4 menu di `MainActivity.kt` (`Scaffold`, `CoolyeahBottomBar`, `CoolyeahTopBar`).
- Penyediaan `EmptyStateCard` ramah anak muda untuk kondisi belum ada data.
- *Verifikasi*: Menjalankan build project untuk memastikan layout 4 tab dapat berpindah mulus dan bebas error.

### Tahap 2: Database Room & Repository
- Pembuatan 7 entitas Room (`ProfileEntity`, `SemesterEntity`, `MataKuliahEntity`, `JadwalKuliahEntity`, `TugasEntity`, `UjianEntity`, `MateriEntity`).
- Pembuatan `Converters.kt` untuk enum dan tanggal.
- Implementasi DAO dengan validasi `@Transaction` untuk `setActiveSemester(semesterId)` dan query reactive `Flow`.
- Implementasi `AcademicRepository` sebagai satu-satunya gerbang data.
- *Verifikasi*: Unit test lokal dengan Robolectric memastikan skema tabel, operasi CRUD, validasi ID transaksi semester aktif, dan cascade delete berhasil diverifikasi.

### Tahap 3: Fitur Akademik Inti - Profil & Manajemen Semester / Mata Kuliah
- Form profil mahasiswa (Nama, NIM, Kampus, Jurusan, Angkatan).
- Dialog Tambah & Edit Semester; aktivasi semester aktif via fungsi tervalidasi `@Transaction`.
- Dialog Tambah & Edit Mata Kuliah (SKS, dosen, ruang, warna tag).
- Dialog konfirmasi bahaya saat menghapus semester atau mata kuliah dengan rincian data terkait.
- *Verifikasi*: Pengguna dapat mengisi data profil, membuat semester baru, dan menambahkan mata kuliah dengan data tersimpan di SQLite lokal.

### Tahap 4: Fitur Akademik Inti - Jadwal Kuliah & Manajemen Materi
- Tampilan `JadwalScreen` dengan tab hari (Senin s/d Minggu).
- Form input jadwal kuliah (mata kuliah, hari, jam mulai, jam selesai, ruangan).
- Manajemen materi perkuliahan per mata kuliah:
  - Pemilihan dokumen lokal (PDF/DOCX/PPTX) menggunakan `ActivityResultContracts.OpenDocument()`.
  - Input link YouTube dan pembukaan link di aplikasi browser/YouTube.
- *Verifikasi*: Input jadwal tampil di tab hari yang sesuai; dokumen materi dan link YouTube dapat ditambahkan dan dibuka.

### Tahap 5: Fitur Akademik Inti - Tugas & Ujian
- Tampilan `TugasScreen` dengan sub-tab:
  - **Tugas**: Filter 3 status (Belum Selesai, Selesai, Terlewat), kartu tugas dengan checklist interaktif dan penanda deadline.
  - **Ujian**: Daftar kuis dan ujian dengan chip pembeda kategori `KUIS`, `UTS`, dan `UAS`.
- Form dialog tambah/edit tugas dan ujian.
- *Verifikasi*: Tugas dapat dicentang selesai, tugas yang lewat deadline otomatis berstatus Terlewat, dan kartu ujian tampil sesuai kategorinya.

### Tahap 6: Dashboard Home Terpadu & Filter Semester
- Integrasi `HomeScreen`:
  - Sambutan mahasiswa yang adaptif (sapaan umum ramah jika nama belum ada, sapaan personal jika sudah ada) dan banner semester aktif.
  - Kartu "Kuliah Hari Ini" yang otomatis menyesuaikan hari saat ini.
  - Kartu "Tugas Mendesak" untuk tugas yang mendekati tenggat.
  - FAB untuk penambahan cepat.
- Top Bar Dropdown Filter: Mengubah tampilan data di layar aktif tanpa mengubah semester aktif database.
- *Verifikasi*: Dashboard menampilkan informasi jadwal hari ini dan tugas terdekat secara akurat sesuai semester yang dipilih pada filter.

### Tahap 7: Sistem Notifikasi Lokal
- Setup `NotificationChannel` di Android (nama channel: "Jadwal Kuliah", "Pengingat Tugas & Ujian").
- Izin runtime `POST_NOTIFICATIONS` untuk Android 13+.
- Implementasi WorkManager / AlarmManager:
  - Ringkasan harian pukul 00.00 WIB untuk jadwal hari kalender yang baru dimulai (hanya jika ada kelas).
  - Pengingat tugas H-1 pukul 19.00 WIB, hanya jika tugas belum selesai.
  - Pengingat ujian H-1 pukul 19.00 WIB untuk semua kategori (`KUIS`, `UTS`, `UAS`).
  - Batalkan atau jadwalkan ulang pengingat ketika tugas diselesaikan atau data tugas/ujian diubah maupun dihapus.
- *Verifikasi*: Penjadwalan notifikasi terdaftar dengan benar di WorkManager/AlarmManager lokal.

### Tahap 8: Pengujian & Verifikasi Komprehensif
- Pengujian Robolectric untuk integritas database:
  - Validasi `@Transaction` `setActiveSemester`: memastikan ID yang valid berhasil diaktifkan dan semester lain dinonaktifkan; memastikan ID yang tidak valid gagal tanpa mengubah status yang ada.
  - Validasi Foreign Key `CASCADE` delete.
  - Validasi perhitungan status tugas (Belum Selesai, Selesai, Terlewat).
- Verifikasi kompilasi akhir menggunakan Gradle project build (`compile_applet` / `gradle assembleDebug` & unit test) dengan pelaporan hasil nyata dari build system.

---

## 6. Strategi Pengujian & Verifikasi

1. **Pengujian Unit & Robolectric (`ExampleRobolectricTest.kt`)**:
   - Memastikan `AppDatabase` in-memory dapat meng-insert profil, semester, matkul, jadwal, tugas, ujian, dan materi.
   - Menguji transaksi `setActiveSemester`: memastikan semester lain otomatis `isActive = false`, dan jika ID salah tidak ada perubahan.
   - Menguji cascade delete: memastikan penghapusan matkul otomatis menghapus tugas dan ujian terkait.
   - Menguji komputasi `StatusTugas` berdasarkan waktu sekarang.
2. **Pengujian Antarmuka Jetpack Compose**:
   - Verifikasi navigasi 4 tab bottom bar.
   - Verifikasi sapaan adaptif saat profil ada vs. saat profil kosong.
   - Verifikasi dialog konfirmasi hapus data.
3. **Verifikasi Kompilasi & Build**:
   - Menjalankan build project melalui build tool AI Studio (`compile_applet`) serta test suite Gradle (`gradle :app:testDebugUnitTest`), dan melaporkan log keluaran sebenarnya untuk menjamin tidak ada error sintaks atau runtime breaking issue.
