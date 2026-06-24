package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CitizenDao {
    @Query("SELECT * FROM citizens ORDER BY name ASC")
    fun getAllCitizens(): Flow<List<Citizen>>

    @Query("SELECT * FROM citizens WHERE nik = :nik LIMIT 1")
    suspend fun getCitizenByNik(nik: String): Citizen?

    @Query("SELECT COUNT(*) FROM citizens")
    fun getCitizenCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT kk) FROM citizens")
    fun getKkCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM citizens WHERE gender = 'Laki-laki'")
    fun getMaleCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM citizens WHERE gender = 'Perempuan'")
    fun getFemaleCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT rt) FROM citizens")
    fun getRtCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT rw) FROM citizens")
    fun getRwCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM citizens WHERE isPoor = 1")
    fun getPoorCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizen(citizen: Citizen)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizens(citizens: List<Citizen>)

    @Query("DELETE FROM citizens WHERE nik = :nik")
    suspend fun deleteCitizenByNik(nik: String)
}

@Dao
interface LetterRequestDao {
    @Query("SELECT * FROM letter_requests ORDER BY createdAt DESC")
    fun getAllLetterRequests(): Flow<List<LetterRequest>>

    @Query("SELECT * FROM letter_requests WHERE applicantNik = :nik ORDER BY createdAt DESC")
    fun getLetterRequestsByNik(nik: String): Flow<List<LetterRequest>>

    @Query("SELECT * FROM letter_requests WHERE trackingNo = :trackingNo LIMIT 1")
    suspend fun getLetterByTrackingNo(trackingNo: String): LetterRequest?

    @Query("SELECT COUNT(*) FROM letter_requests WHERE status = 'MENUNGGU'")
    fun getPendingRequestsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: LetterRequest)

    @Update
    suspend fun updateRequest(request: LetterRequest)

    @Query("DELETE FROM letter_requests WHERE id = :id")
    suspend fun deleteRequestById(id: Int)
}

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_items ORDER BY date DESC")
    fun getAllNews(): Flow<List<NewsItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNews(news: List<NewsItem>)

    @Query("DELETE FROM news_items WHERE id = :id")
    suspend fun deleteNewsById(id: Int)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY originalDate DESC")
    fun getAllProjects(): Flow<List<ProjectItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<ProjectItem>)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportItem>>

    @Query("SELECT * FROM reports WHERE reporterName = :reporterName ORDER BY timestamp DESC")
    fun getReportsByReporter(reporterName: String): Flow<List<ReportItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportItem)

    @Update
    suspend fun updateReport(report: ReportItem)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY date DESC, checkInTime DESC")
    fun getAllAttendances(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendances WHERE staffName = :staffName ORDER BY date DESC")
    fun getAttendancesByStaff(staffName: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord)
}

@Dao
interface UmkmDao {
    @Query("SELECT * FROM umkms ORDER BY name ASC")
    fun getAllUmkms(): Flow<List<UmkmItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUmkm(umkm: UmkmItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUmkms(umkms: List<UmkmItem>)

    @Query("DELETE FROM umkms WHERE id = :id")
    suspend fun deleteUmkmById(id: Int)
}

@Dao
interface VillageEventDao {
    @Query("SELECT * FROM village_events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<VillageEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: VillageEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<VillageEvent>)

    @Query("DELETE FROM village_events WHERE id = :id")
    suspend fun deleteEventById(id: Int)
}

