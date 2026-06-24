package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SijagoRepository(private val db: AppDatabase) {

    val citizens: Flow<List<Citizen>> = db.citizenDao().getAllCitizens()
    val letters: Flow<List<LetterRequest>> = db.letterRequestDao().getAllLetterRequests()
    val news: Flow<List<NewsItem>> = db.newsDao().getAllNews()
    val projects: Flow<List<ProjectItem>> = db.projectDao().getAllProjects()
    val reports: Flow<List<ReportItem>> = db.reportDao().getAllReports()
    val attendances: Flow<List<AttendanceRecord>> = db.attendanceDao().getAllAttendances()
    val umkms: Flow<List<UmkmItem>> = db.umkmDao().getAllUmkms()
    val events: Flow<List<VillageEvent>> = db.villageEventDao().getAllEvents()

    // Dashboard Statistics flows
    val citizenCount: Flow<Int> = db.citizenDao().getCitizenCountFlow()
    val kkCount: Flow<Int> = db.citizenDao().getKkCountFlow()
    val maleCount: Flow<Int> = db.citizenDao().getMaleCountFlow()
    val femaleCount: Flow<Int> = db.citizenDao().getFemaleCountFlow()
    val rtCount: Flow<Int> = db.citizenDao().getRtCountFlow()
    val rwCount: Flow<Int> = db.citizenDao().getRwCountFlow()
    val poorCount: Flow<Int> = db.citizenDao().getPoorCountFlow()
    val pendingRequestsCount: Flow<Int> = db.letterRequestDao().getPendingRequestsCountFlow()

    fun getLettersByNik(nik: String): Flow<List<LetterRequest>> =
        db.letterRequestDao().getLetterRequestsByNik(nik)

    fun getReportsByReporter(reporter: String): Flow<List<ReportItem>> =
        db.reportDao().getReportsByReporter(reporter)

    fun getAttendancesByStaff(staff: String): Flow<List<AttendanceRecord>> =
        db.attendanceDao().getAttendancesByStaff(staff)

    suspend fun getCitizenByNik(nik: String): Citizen? =
        db.citizenDao().getCitizenByNik(nik)

    suspend fun getLetterByTrackingNo(trackingNo: String): LetterRequest? =
        db.letterRequestDao().getLetterByTrackingNo(trackingNo)

    // Mutations
    suspend fun insertCitizen(citizen: Citizen) = db.citizenDao().insertCitizen(citizen)
    suspend fun deleteCitizen(nik: String) = db.citizenDao().deleteCitizenByNik(nik)

    suspend fun submitLetterRequest(request: LetterRequest) = db.letterRequestDao().insertRequest(request)
    suspend fun updateLetterRequest(request: LetterRequest) = db.letterRequestDao().updateRequest(request)
    suspend fun deleteLetterRequest(id: Int) = db.letterRequestDao().deleteRequestById(id)

    suspend fun insertNews(newsItem: NewsItem) = db.newsDao().insertNews(newsItem)
    suspend fun deleteNews(id: Int) = db.newsDao().deleteNewsById(id)

    suspend fun insertProject(projectItem: ProjectItem) = db.projectDao().insertProject(projectItem)
    suspend fun deleteProject(id: Int) = db.projectDao().deleteProjectById(id)

    suspend fun submitReport(reportItem: ReportItem) = db.reportDao().insertReport(reportItem)
    suspend fun updateReport(reportItem: ReportItem) = db.reportDao().updateReport(reportItem)
    suspend fun deleteReport(id: Int) = db.reportDao().deleteReportById(id)

    suspend fun recordAttendance(attendance: AttendanceRecord) = db.attendanceDao().insertAttendance(attendance)

    suspend fun insertUmkm(umkmItem: UmkmItem) = db.umkmDao().insertUmkm(umkmItem)
    suspend fun deleteUmkm(id: Int) = db.umkmDao().deleteUmkmById(id)

    suspend fun insertEvent(eventItem: VillageEvent) = db.villageEventDao().insertEvent(eventItem)
    suspend fun deleteEvent(id: Int) = db.villageEventDao().deleteEventById(id)

    // Predetermined Seeding function for fully immersive UX
    suspend fun prepopulateIfEmpty() {
        val count = db.citizenDao().getCitizenCountFlow().first()
        if (count == 0) {
            // Seed Citizens
            val sampleCitizens = listOf(
                Citizen("3301021405820001", "3301021405080001", "Joko Susilo", "Laki-laki", 42, "Petani", "SMP / Sederajat", "Islam", "Jl. Tani Mulya No. 4, RT 01 / RW 01", 1, 1, "Dusun Suka Makmur", false, "Tidak Ada"),
                Citizen("3301025508850002", "3301021405080001", "Siti Aminah", "Perempuan", 39, "Mengurus Rumah Tangga", "SMA", "Islam", "Jl. Tani Mulya No. 4, RT 01 / RW 01", 1, 1, "Dusun Suka Makmur", false, "Tidak Ada"),
                Citizen("3301021008030005", "3301022209110002", "Budi Rahardjo", "Laki-laki", 23, "Wiraswasta", "Diploma / Sarjana", "Kristen", "Dusun Suka Maju RT 03 / RW 02", 3, 2, "Dusun Suka Maju", false, "Tidak Ada"),
                Citizen("3301024412950001", "3301022209110002", "Pratiwi Indah", "Perempuan", 31, "Guru Honorer", "Diploma / Sarjana", "Islam", "Dusun Suka Maju RT 03 / RW 02", 3, 2, "Dusun Suka Maju", true, "PKH"),
                Citizen("3301022202610001", "3301020210880009", "Ahmad Sadudin", "Laki-laki", 65, "Buruh Harian Lepas", "SD / Sederajat", "Islam", "Dusun Krajan RT 02 / RW 03", 2, 3, "Dusun Krajan", true, "BLT"),
                Citizen("3301026305640003", "3301020210880009", "Dewi Lestari", "Perempuan", 62, "Buruh Tani", "Tidak Sekolah", "Islam", "Dusun Krajan RT 02 / RW 03", 2, 3, "Dusun Krajan", true, "BPNT"),
                Citizen("3301021708890001", "3301021111190004", "Kusuma Wardhana", "Laki-laki", 37, "PNS / Perangkat Desa", "Diploma / Sarjana", "Islam", "Dusun Suka Makmur RT 01 / RW 01", 1, 1, "Dusun Suka Makmur", false, "Tidak Ada"),
                Citizen("3301025203920005", "3301021111190004", "Sri Wahyuni", "Perempuan", 34, "Karyawan Swasta", "SMA", "Islam", "Dusun Suka Makmur RT 01 / RW 01", 1, 1, "Dusun Suka Makmur", false, "Tidak Ada"),
                Citizen("3301021101990003", "3301023112200007", "Rian Hidayat", "Laki-laki", 27, "Belum / Tidak Bekerja", "SMA", "Islam", "Dusun Suka Maju RT 04 / RW 02", 4, 2, "Dusun Suka Maju", false, "Tidak Ada"),
                Citizen("3301024205560002", "3301020202020001", "Mbah Sartini", "Perempuan", 70, "Tidak Bekerja / Lansia", "SD / Sederajat", "Islam", "Dusun Suka Maju RT 03 / RW 02", 3, 2, "Dusun Suka Maju", true, "PKH")
            )
            db.citizenDao().insertCitizens(sampleCitizens)

            // Seed News
            val sampleNews = listOf(
                NewsItem(
                    title = "Pembangunan Bak Penampung Air Bersih Dusun Krajan Rampung",
                    category = "Berita",
                    content = "Pemerintah Desa Sumber Rejo mengumumkan selesainya pembangunan bak penampung air bersih berkapasitas 10.000 liter di Dusun Krajan. Proyek ini bersumber dari anggaran Dana Desa (DD) Tahun 2026 guna mengantisipasi kekeringan pada musim kemarau mendatang. Sebanyak 150 KK kini dapat menikmati akses air bersih dengan mandiri dan lancar.",
                    date = "2026-06-20",
                    imageUrl = "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?auto=format&fit=crop&q=80&w=600"
                ),
                NewsItem(
                    title = "Pengumuman: Jadwal Pelayanan Posyandu Balita & Lansia Juni 2026",
                    category = "Pengumuman",
                    content = "Diberitahukan kepada seluruh warga Desa Sumber Rejo bahwa posyandu balita dan lansia terpadu akan dilaksanakan mulai tanggal 24 Juni hingga 28 Juni 2026 di masing-masing Pos Dusun. Harap membawa buku KIA dan kartu bansos (jika ada) untuk pendataan berkala serta pembagian makanan tambahan (PMT) bergizi.",
                    date = "2026-06-21",
                    imageUrl = "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&q=80&w=600"
                ),
                NewsItem(
                    title = "Festival UMKM & Pasar Rakyat Hari Kemerdekaan RI Ke-81 Desa Suka Makmur",
                    category = "Event",
                    content = "Dalam rangka memperingati HUT RI Ke-81, Karang Taruna Desa akan menggelar Festival UMKM & Pasar Rakyat pada bulan Agustus. Seluruh pelaku usaha lokal diperbolehkan memajang dan menjual produk unggulannya di stand secara gratis. Pendaftaran stand dibuka sampai tanggal 15 Juli 2026 melalui Operator Desa.",
                    date = "2026-06-18",
                    imageUrl = "https://images.unsplash.com/photo-1533900298318-6b8da08a523e?auto=format&fit=crop&q=80&w=600"
                ),
                NewsItem(
                    title = "Musyawarah Rencana Pembangunan Desa (Musrenbangdes) APBDes Perubahan",
                    category = "Agenda",
                    content = "BPD mengundang perwakilan RT, RW, Tokoh Masyarakat, serta Pengurus PKK untuk menghadiri Musyawarah Rencana Pembangunan Desa Terkait Anggaran Pendapatan dan Belanja Desa (APBDes) Perubahan tahun berjalan. Rapat diadakan pada Jumat pukul 13.30 WIB di Balai Desa.",
                    date = "2026-06-19",
                    imageUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&q=80&w=600"
                )
            )
            db.newsDao().insertAllNews(sampleNews)

            // Seed Projects
            val sampleProjects = listOf(
                ProjectItem(
                    name = "Pengaspalan Jalan Usaha Tani RT 02 Dusun Suka Makmur",
                    budget = 75000000.0,
                    fundingSource = "Dana Desa (DD)",
                    progress = 100,
                    location = "RT 02 RW 01, Dusun Suka Makmur",
                    originalDate = "2026-04-12"
                ),
                ProjectItem(
                    name = "Pembangunan Rumah Pompa Air Irigasi Sawah Dusun Krajan",
                    budget = 110000000.0,
                    fundingSource = "Alokasi Dana Desa (ADD)",
                    progress = 80,
                    location = "Sawah Blok Krajan Selatan",
                    originalDate = "2026-05-02"
                ),
                ProjectItem(
                    name = "Rehabilitasi Gedung PAUD Pelita Bangsa",
                    budget = 45000000.0,
                    fundingSource = "Dana Desa (DD)",
                    progress = 35,
                    location = "RT 03 RW 02, Suka Maju",
                    originalDate = "2026-06-10"
                )
            )
            db.projectDao().insertAllProjects(sampleProjects)

            // Seed UMKM
            val sampleUmkm = listOf(
                UmkmItem(
                    name = "Kripik Singkong Renyah Suka Makmur",
                    owner = "Siti Aminah",
                    category = "Kuliner",
                    description = "Memproduksi keripik singkong gurih aneka rasa (balado, original, keju) langsung dari hasil panen kebun sendiri tanpa pengawet tambahan.",
                    priceRange = "Rp 5.000 - Rp 15.000",
                    phone = "081234567812",
                    address = "RT 01 / RW 01, Dusun Suka Makmur"
                ),
                UmkmItem(
                    name = "Batik Tulis Corak Sawah Rejo",
                    owner = "Ibu Sri Wahyuni",
                    category = "Kerajinan",
                    description = "Batik tulis tradisional modern dengan corak khas kearifan lokal pertanian dan pegunungan daerah Sumber Rejo. Menerima pesanan seragam.",
                    priceRange = "Rp 150.000 - Rp 500.000",
                    phone = "081987654321",
                    address = "RT 01 / RW 01, Suka Makmur"
                ),
                UmkmItem(
                    name = "Madu Hutan Asli Kelompok Tani Krajan",
                    owner = "Ahmad Sadudin",
                    category = "Pertanian",
                    description = "Madu murni hasil budidaya lebah madu lokal di kaki bukit dusun Krajan. Berkhasiat tinggi menjaga imunitas.",
                    priceRange = "Rp 40.000 - Rp 120.000",
                    phone = "085223344556",
                    address = "RT 02 / RW 03, Dusun Krajan"
                )
            )
            db.umkmDao().insertAllUmkms(sampleUmkm)

            // Seed some sample submissions
            val sampleRequests = listOf(
                LetterRequest(
                    trackingNo = "REG-DOM-20260623001",
                    applicantNik = "3301021008030005",
                    applicantName = "Budi Rahardjo",
                    type = "Surat Keterangan Domisili",
                    dataFields = "{\"Keperluan\":\"Persyaratan Melamar Pekerjaan Swasta\",\"Alamat Tujuan\":\"Sleman, Yogyakarta\"}",
                    status = "SELESAI",
                    createdAt = System.currentTimeMillis() - 86400000 * 2,
                    adminComment = "Surat Domisili telah diverifikasi dan disetujui.",
                    qrCodeContent = "SIJAGO-VERIFIED-DOM-3301021008030005-20260623"
                ),
                LetterRequest(
                    trackingNo = "REG-SKU-20260623002",
                    applicantNik = "3301025508850002",
                    applicantName = "Siti Aminah",
                    type = "Surat Keterangan Usaha",
                    dataFields = "{\"Nama Usaha\":\"Kripik Singkong Renyah\",\"Sektor\":\"Kuliner/Dagang\",\"Keperluan\":\"Pengajuan Kredit Usaha Rakyat (KUR)\"}",
                    status = "DIPROSES",
                    createdAt = System.currentTimeMillis() - 86400000,
                    adminComment = "Sedang proses verifikasi lapangan oleh Kasi Pelayanan.",
                    qrCodeContent = ""
                )
            )
            // Save them
            for (req in sampleRequests) {
                db.letterRequestDao().insertRequest(req)
            }

            // Seed some reports
            val sampleReports = listOf(
                ReportItem(
                    title = "Saluran Irigasi Blok Tani Tersumbat Sampah Limbah Kayu",
                    description = "Saluran irigasi yang mengairi sawah di bagian barat terhambat oleh tumpukan sampah kayu dan ranting kering. Air meluap ke badan jalan tani, mohon gotong royong warga dikoordinasikan.",
                    category = "Infrastruktur",
                    reporterName = "Joko Susilo",
                    contact = "081234567800",
                    latitude = -7.4531,
                    longitude = 110.3662,
                    status = "DIPROSES",
                    timestamp = System.currentTimeMillis() - 3600000 * 12
                ),
                ReportItem(
                    title = "Tiang Lampu Penerangan Jalan Desa Roboh karena Angin Kencang",
                    description = "Satu unit tiang lampu PJUTS di tikungan jembatan dusun Krajan roboh setelah dihantam angin semalam. Bahaya untuk anak-anak dan membuat jalan gelap gulita.",
                    category = "Darurat",
                    reporterName = "Ahmad Sadudin",
                    contact = "085223344556",
                    latitude = -7.4510,
                    longitude = 110.3640,
                    status = "MASUK",
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                )
            )
            for (rep in sampleReports) {
                db.reportDao().insertReport(rep)
            }

            // Seed events
            val sampleEvents = listOf(
                VillageEvent(
                    title = "Posyandu Balita Suka Makmur",
                    description = "Pemeriksaan kesehatan rutin untuk balita dan pembagian PMT (Pemberian Makanan Tambahan) gizi seimbang oleh kader kesehatan desa.",
                    date = "2026-06-24",
                    time = "09:00",
                    location = "Posyandu Dusun Suka Makmur",
                    type = "ACTIVITY",
                    organizer = "Kader Posyandu & PKK"
                ),
                VillageEvent(
                    title = "Rapat Koordinasi Pembangunan Desa",
                    description = "Rapat membahas rincian anggaran serta target penyelesaian pembangunan infrastruktur jalan pertanian dan drainase tahun anggaran 2026.",
                    date = "2026-06-25",
                    time = "14:00",
                    location = "Balai Desa Sumber Rejo",
                    type = "MEETING",
                    organizer = "Sekretariat Desa"
                ),
                VillageEvent(
                    title = "Kerja Bakti Akbar Saluran Irigasi Barat",
                    description = "Gotong royong membersihkan penumpukan sampah kayu dan ranting di sepanjang saluran irigasi Blok Tani agar pengairan sawah lancar kembali.",
                    date = "2026-06-28",
                    time = "07:00",
                    location = "Saluran Irigasi Blok Tani Barat",
                    type = "ACTIVITY",
                    organizer = "Kelompok Tani Rejo Mulyo"
                ),
                VillageEvent(
                    title = "Batas Pelunasan PBB Tahap I",
                    description = "Batas akhir penyerahan dan pelunasan SPPT Pajak Bumi dan Bangunan (PBB) sektor perdesaan tahun 2026 Tahap I ke Koordinator Dusun.",
                    date = "2026-06-30",
                    time = "15:00",
                    location = "Kantor Koordinator Dusun masing-masing",
                    type = "DEADLINE",
                    organizer = "Pemerintah Desa Sumber Rejo"
                ),
                VillageEvent(
                    title = "Penyuluhan Pertanian Organik & Peternakan",
                    description = "Sosialisasi pembuatan pupuk organik padat dan cair mandiri berbasis kotoran sapi perah guna meningkatkan kualitas tanah tani.",
                    date = "2026-07-02",
                    time = "09:00",
                    location = "Aula Gapoktan Suka Maju",
                    type = "ACTIVITY",
                    organizer = "Penyuluh Pertanian Lapangan (PPL)"
                ),
                VillageEvent(
                    title = "Batas Akhir Pendaftaran Penerima BLT",
                    description = "Tenggat waktu pendaftaran dan penyerahan berkas verifikasi untuk calon penerima Bantuan Langsung Tunai (BLT) Dana Desa Triwulan III.",
                    date = "2026-07-05",
                    time = "16:00",
                    location = "Kantor Sekretaris Desa SIJAGO",
                    type = "DEADLINE",
                    organizer = "Seksi Kesejahteraan Rakyat"
                )
            )
            for (evt in sampleEvents) {
                db.villageEventDao().insertEvent(evt)
            }
        }
    }
}
