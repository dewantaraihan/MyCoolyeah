package com.example.data.model

enum class KategoriUjian(val label: String) {
    KUIS("Kuis"),
    UTS("UTS"),
    UAS("UAS")
}

enum class TipeMateri(val label: String) {
    DOKUMEN_LOKAL("Dokumen"),
    YOUTUBE("YouTube")
}

enum class StatusTugas(val label: String) {
    BELUM_SELESAI("Belum Selesai"),
    SELESAI("Selesai"),
    TERLEWAT("Terlewat")
}

enum class PrioritasTugas(val label: String) {
    TINGGI("Tinggi"),
    SEDANG("Sedang"),
    RENDAH("Rendah")
}
