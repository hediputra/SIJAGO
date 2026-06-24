package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SijagoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SijagoRepository
    private val sharedPrefs = application.getSharedPreferences("sijago_prefs", android.content.Context.MODE_PRIVATE)

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(sharedPrefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode.name).apply()
    }

    // Network status & simulation monitoring
    private val connectivityMonitor = com.example.utils.ConnectivityMonitor(application)

    private val _isSimulatedOffline = MutableStateFlow(false)
    val isSimulatedOffline: StateFlow<Boolean> = _isSimulatedOffline.asStateFlow()

    fun setSimulatedOffline(offline: Boolean) {
        _isSimulatedOffline.value = offline
    }

    val isOnline: StateFlow<Boolean> = combine(
        connectivityMonitor.isOnline,
        _isSimulatedOffline
    ) { realOnline, simulatedOffline ->
        realOnline && !simulatedOffline
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // UI Toast/Notification Flow for Network Status
    private val _networkNotification = MutableSharedFlow<String>()
    val networkNotification: SharedFlow<String> = _networkNotification.asSharedFlow()

    fun triggerManualNotification(msg: String) {
        viewModelScope.launch {
            _networkNotification.emit(msg)
        }
    }

    private fun syncOfflineReports() {
        viewModelScope.launch {
            val reportsList = repository.reports.first()
            val unsynced = reportsList.filter { !it.isSynced }
            if (unsynced.isNotEmpty()) {
                unsynced.forEach { report ->
                    val syncedReport = report.copy(isSynced = true, status = "MASUK")
                    repository.updateReport(syncedReport)
                }
                _networkNotification.emit("Sinkronisasi Selesai! ${unsynced.size} Draf Pengaduan berhasil diunggah ke server desa.")
            }
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SijagoRepository(db)
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // Monitor connection events to trigger notifications & sync
        viewModelScope.launch {
            var lastState: Boolean? = null
            isOnline.collect { online ->
                if (lastState != null && lastState != online) {
                    if (!online) {
                        _networkNotification.emit("Terputus! Anda masuk Mode Offline. Laporan baru akan disimpan lokal sebagai draf.")
                    } else {
                        _networkNotification.emit("Koneksi terhubung! Melakukan sinkronisasi draf pengaduan lokal...")
                        syncOfflineReports()
                    }
                }
                lastState = online
            }
        }
    }

    // Role state
    private val _selectedRole = MutableStateFlow("Masyarakat")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    // Login state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Logged in Citizen (for Masyarakat role simulation)
    private val _loggedInNik = MutableStateFlow("3301021008030005") // Budi Rahardjo
    val loggedInNik: StateFlow<String> = _loggedInNik.asStateFlow()

    val loggedInCitizen: StateFlow<Citizen?> = _loggedInNik
        .map { nik -> repository.getCitizenByNik(nik) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Data streams
    val allCitizens: StateFlow<List<Citizen>> = repository.citizens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNews: StateFlow<List<NewsItem>> = repository.news
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<ProjectItem>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUmkms: StateFlow<List<UmkmItem>> = repository.umkms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<VillageEvent>> = repository.events
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of letters adapts dynamically based on who is logged in!
    val filteredLetters: StateFlow<List<LetterRequest>> = combine(
        repository.letters,
        selectedRole,
        loggedInNik
    ) { letterList, role, nik ->
        if (role == "Masyarakat") {
            letterList.filter { it.applicantNik == nik }
        } else {
            letterList // Admins, Kades, Sekdes see all letters
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports adapt dynamically
    val filteredReports: StateFlow<List<ReportItem>> = combine(
        repository.reports,
        selectedRole,
        loggedInCitizen
    ) { reportList, role, citizen ->
        if (role == "Masyarakat") {
            reportList.filter { it.reporterName == (citizen?.name ?: "Budi Rahardjo") }
        } else {
            reportList
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendances
    val allAttendances: StateFlow<List<AttendanceRecord>> = repository.attendances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics Streams
    val statCitizenCount: StateFlow<Int> = repository.citizenCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statKkCount: StateFlow<Int> = repository.kkCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statMaleCount: StateFlow<Int> = repository.maleCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statFemaleCount: StateFlow<Int> = repository.femaleCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statRtCount: StateFlow<Int> = repository.rtCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statRwCount: StateFlow<Int> = repository.rwCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statPoorCount: StateFlow<Int> = repository.poorCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statPendingLettersCount: StateFlow<Int> = repository.pendingRequestsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // QR Code Verification State
    private val _qrResult = MutableStateFlow<LetterRequest?>(null)
    val qrResult: StateFlow<LetterRequest?> = _qrResult.asStateFlow()

    fun changeRole(role: String) {
        _selectedRole.value = role
    }

    fun changeLoggedInUser(nik: String) {
        _loggedInNik.value = nik
    }

    // Business Logic Actions
    fun submitLetter(type: String, fieldMap: Map<String, String>) {
        viewModelScope.launch {
            val citizen = repository.getCitizenByNik(_loggedInNik.value)
            val applicantName = citizen?.name ?: "Masyarakat Mandiri"
            val prefix = when (type) {
                "Surat Keterangan Domisili" -> "REG-DOM"
                "Surat Keterangan Usaha" -> "REG-SKU"
                "Surat Keterangan Tidak Mampu" -> "REG-SKTM"
                "Surat Pengantar KTP" -> "REG-KTP"
                "Surat Keterangan Kelahiran" -> "REG-LAHIR"
                "Surat Keterangan Kematian" -> "REG-MATI"
                else -> "REG-GEN"
            }
            val randomNum = (1000..9999).random()
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            val trackingNo = "$prefix-$dateStr-$randomNum"

            // Construct details representation from fields
            val detailsJsonBuilder = StringBuilder("{")
            fieldMap.forEach { (key, value) ->
                detailsJsonBuilder.append("\"$key\":\"$value\",")
            }
            if (fieldMap.isNotEmpty()) detailsJsonBuilder.deleteCharAt(detailsJsonBuilder.length - 1)
            detailsJsonBuilder.append("}")

            val request = LetterRequest(
                trackingNo = trackingNo,
                applicantNik = _loggedInNik.value,
                applicantName = applicantName,
                type = type,
                dataFields = detailsJsonBuilder.toString(),
                status = "MENUNGGU"
            )
            repository.submitLetterRequest(request)
        }
    }

    fun updateLetterStatus(id: Int, status: String, comment: String) {
        viewModelScope.launch {
            val lettersList = repository.letters.first()
            val letter = lettersList.find { it.id == id }
            if (letter != null) {
                val qrCode = if (status == "SELESAI") {
                    "SIJAGO-VERIFIED-${letter.type.take(4).uppercase()}-${letter.applicantNik}-${System.currentTimeMillis() / 1000}"
                } else {
                    ""
                }
                val updated = letter.copy(
                    status = status,
                    adminComment = comment,
                    qrCodeContent = qrCode
                )
                repository.updateLetterRequest(updated)
            }
        }
    }

    fun submitReport(title: String, description: String, category: String, lat: Double, lng: Double, photoLocalPath: String = "") {
        viewModelScope.launch {
            val citizen = repository.getCitizenByNik(_loggedInNik.value)
            val reporterName = citizen?.name ?: "Anonim"
            val online = isOnline.value
            val report = ReportItem(
                title = title,
                description = description,
                category = category,
                reporterName = reporterName,
                contact = citizen?.phoneNumber ?: "08123456789",
                latitude = lat,
                longitude = lng,
                photoLocalPath = photoLocalPath,
                status = if (online) "MASUK" else "DRAFT_LOKAL",
                isSynced = online
            )
            repository.submitReport(report)
            if (!online) {
                _networkNotification.emit("Tersimpan sebagai draf pengaduan lokal.")
            }
        }
    }

    fun updateReportStatus(id: Int, status: String) {
        viewModelScope.launch {
            val reportsList = repository.reports.first()
            val report = reportsList.find { it.id == id }
            if (report != null) {
                repository.updateReport(report.copy(status = status))
            }
        }
    }

    fun recordCheckIn(staffName: String, lat: Double, lng: Double, statusLabel: String = "Hadir") {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            val timeStr = timeFormat.format(Date())

            val record = AttendanceRecord(
                staffName = staffName,
                date = dateStr,
                checkInTime = timeStr,
                checkOutTime = "-",
                locationName = "Kantor Kepala Desa Sumber Rejo",
                latitude = lat,
                longitude = lng,
                label = statusLabel
            )
            repository.recordAttendance(record)
        }
    }

    fun recordCheckOut(recordId: Int) {
        viewModelScope.launch {
            val attendancesList = repository.attendances.first()
            val existing = attendancesList.find { it.id == recordId }
            if (existing != null) {
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val timeStr = timeFormat.format(Date())
                val updated = existing.copy(checkOutTime = timeStr)
                repository.recordAttendance(updated)
            }
        }
    }

    fun addUmkm(name: String, owner: String, category: String, desc: String, priceRange: String, phone: String, address: String) {
        viewModelScope.launch {
            val item = UmkmItem(
                name = name,
                owner = owner,
                category = category,
                description = desc,
                priceRange = priceRange,
                phone = phone,
                address = address
            )
            repository.insertUmkm(item)
        }
    }

    fun addVillageEvent(title: String, description: String, date: String, time: String, location: String, type: String, organizer: String) {
        viewModelScope.launch {
            val event = VillageEvent(
                title = title,
                description = description,
                date = date,
                time = time,
                location = location,
                type = type,
                organizer = organizer
            )
            repository.insertEvent(event)
        }
    }

    fun deleteVillageEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEvent(id)
        }
    }

    fun addProject(name: String, budget: Double, funding: String, progress: Int, location: String) {
        viewModelScope.launch {
            val project = ProjectItem(
                name = name,
                budget = budget,
                fundingSource = funding,
                progress = progress,
                location = location,
                originalDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.insertProject(project)
        }
    }

    fun addNewCitizen(nik: String, kk: String, name: String, gender: String, age: Int, job: String, isPoor: Boolean) {
        viewModelScope.launch {
            val citizen = Citizen(
                nik = nik,
                kk = kk,
                name = name,
                gender = gender,
                age = age,
                job = job,
                education = "SMA",
                religion = "Islam",
                address = "Dusun Suka Makmur",
                rt = 1,
                rw = 1,
                dusun = "Dusun Suka Makmur",
                isPoor = isPoor,
                bansosType = if (isPoor) "BLT" else "Tidak Ada"
            )
            repository.insertCitizen(citizen)
        }
    }

    fun deleteCitizen(nik: String) {
        viewModelScope.launch {
            repository.deleteCitizen(nik)
        }
    }

    fun updateCitizenBansos(nik: String, isPoor: Boolean, bansosType: String) {
        viewModelScope.launch {
            val citizen = repository.getCitizenByNik(nik)
            if (citizen != null) {
                val updated = citizen.copy(isPoor = isPoor, bansosType = bansosType)
                repository.insertCitizen(updated)
            }
        }
    }

    fun updateCitizenProfile(nik: String, phoneNumber: String, address: String, job: String, education: String) {
        viewModelScope.launch {
            val original = repository.getCitizenByNik(nik)
            if (original != null) {
                val updated = original.copy(
                    phoneNumber = phoneNumber,
                    address = address,
                    job = job,
                    education = education
                )
                repository.insertCitizen(updated)
            }
        }
    }

    fun addNews(title: String, category: String, content: String, imageUrl: String = "") {
        viewModelScope.launch {
            val news = NewsItem(
                title = title,
                category = category,
                content = content,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                imageUrl = imageUrl
            )
            repository.insertNews(news)
        }
    }

    fun deleteNews(id: Int) {
        viewModelScope.launch {
            repository.deleteNews(id)
        }
    }

    fun deleteProject(id: Int) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun deleteUmkm(id: Int) {
        viewModelScope.launch {
            repository.deleteUmkm(id)
        }
    }

    fun verifyQrCode(qrText: String) {
        viewModelScope.launch {
            val lettersList = repository.letters.first()
            // Find a letter whose verify qrCodeContent matches
            val target = lettersList.find { it.qrCodeContent == qrText || it.trackingNo == qrText }
            _qrResult.value = target
        }
    }

    fun clearQrResult() {
        _qrResult.value = null
    }

    fun login(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) return false
        
        // Custom check for Admin, Sekdes, or general Masyarakat login simulation
        if (email.contains("admin", ignoreCase = true)) {
            _selectedRole.value = "Admin"
        } else if (email.contains("sekdes", ignoreCase = true)) {
            _selectedRole.value = "Sekdes"
        } else if (email.contains("kades", ignoreCase = true)) {
            _selectedRole.value = "Kades"
        } else {
            _selectedRole.value = "Masyarakat"
            _loggedInNik.value = "3301021008030005" // Defaults to Budi Rahardjo
        }
        _isLoggedIn.value = true
        return true
    }

    fun loginWithGoogle(accountName: String, email: String) {
        // Simulates Google OAuth Authentication Flow successfully
        if (email.contains("admin", ignoreCase = true)) {
            _selectedRole.value = "Admin"
        } else {
            _selectedRole.value = "Masyarakat"
            _loggedInNik.value = "3301021008030005"
        }
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun forgotPassword(email: String): Boolean {
        if (email.isBlank() || !email.contains("@")) return false
        // Successfully simulate secure password reset code trigger
        return true
    }
}
