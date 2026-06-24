package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "citizens")
data class Citizen(
    @PrimaryKey val nik: String,
    val kk: String,
    val name: String,
    val gender: String, // "Laki-laki" | "Perempuan"
    val age: Int,
    val job: String,
    val education: String,
    val religion: String,
    val address: String,
    val rt: Int,
    val rw: Int,
    val dusun: String,
    val isPoor: Boolean,
    val bansosType: String, // "PKH", "BPNT", "BLT", "Tidak Ada"
    val phoneNumber: String = "08123456789"
) : Serializable

@Entity(tableName = "letter_requests")
data class LetterRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackingNo: String,
    val applicantNik: String,
    val applicantName: String,
    val type: String, // "Domisili", "SKU", "SKTM", "Pengantar KTP", "Kelahiran", "Kematian"
    val dataFields: String, // JSON or formatted key-value text
    val status: String, // "MENUNGGU", "DIPROSES", "SELESAI", "DITOLAK"
    val createdAt: Long = System.currentTimeMillis(),
    val adminComment: String = "",
    val qrCodeContent: String = ""
) : Serializable

@Entity(tableName = "news_items")
data class NewsItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Berita", "Pengumuman", "Agenda", "Event"
    val content: String,
    val date: String,
    val imageUrl: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0
) : Serializable

@Entity(tableName = "projects")
data class ProjectItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val budget: Double,
    val fundingSource: String, // "Dana Desa (DD)", "ADD", "PBP"
    val progress: Int, // 0 - 100
    val location: String,
    val originalDate: String,
    val beforePhoto: String = "",
    val afterPhoto: String = ""
) : Serializable

@Entity(tableName = "reports")
data class ReportItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Infrastruktur", "Sosial", "Pelayanan", "Keamanan", "Darurat"
    val reporterName: String,
    val contact: String,
    val latitude: Double = -7.4523,
    val longitude: Double = 110.3654,
    val photoLocalPath: String = "",
    val status: String = "MASUK", // "MASUK", "DIPROSES", "SELESAI"
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) : Serializable

@Entity(tableName = "attendances")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val staffName: String,
    val date: String, // YYYY-MM-DD
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val locationName: String = "",
    val latitude: Double = -7.4523,
    val longitude: Double = 110.3654,
    val label: String = "Hadir" // "Hadir", "Terlambat", "Izin", "Sakit"
) : Serializable

@Entity(tableName = "umkms")
data class UmkmItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val owner: String,
    val category: String, // "Kuliner", "Kerajinan", "Pertanian", "Jasa", "Wisata"
    val description: String,
    val priceRange: String = "Rp 10.000 - Rp 50.000",
    val phone: String = "08123456789",
    val address: String = "RT 01 / RW 02"
) : Serializable

@Entity(tableName = "village_events")
data class VillageEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String, // "YYYY-MM-DD" e.g., "2026-06-25"
    val time: String, // "HH:MM" e.g., "09:00"
    val location: String,
    val type: String, // "MEETING", "ACTIVITY", "DEADLINE"
    val organizer: String = "Pemerintah Desa"
) : Serializable

