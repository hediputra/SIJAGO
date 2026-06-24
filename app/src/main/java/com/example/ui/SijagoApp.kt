package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SijagoApp(viewModel: SijagoViewModel) {
    val context = LocalContext.current
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val loggedInCitizen by viewModel.loggedInCitizen.collectAsStateWithLifecycle()
    val letters by viewModel.filteredLetters.collectAsStateWithLifecycle()
    val newsList by viewModel.allNews.collectAsStateWithLifecycle()
    val projectsList by viewModel.allProjects.collectAsStateWithLifecycle()
    val reportsList by viewModel.filteredReports.collectAsStateWithLifecycle()
    val attendanceList by viewModel.allAttendances.collectAsStateWithLifecycle()
    val umkmList by viewModel.allUmkms.collectAsStateWithLifecycle()
    val citizensList by viewModel.allCitizens.collectAsStateWithLifecycle()

    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.networkNotification.collect { message ->
            Toast.makeText(SpacerContextHolder.ctx, message, Toast.LENGTH_LONG).show()
        }
    }

    // Counts
    val citizenCount by viewModel.statCitizenCount.collectAsStateWithLifecycle()
    val kkCount by viewModel.statKkCount.collectAsStateWithLifecycle()
    val maleCount by viewModel.statMaleCount.collectAsStateWithLifecycle()
    val femaleCount by viewModel.statFemaleCount.collectAsStateWithLifecycle()
    val rtCount by viewModel.statRtCount.collectAsStateWithLifecycle()
    val rwCount by viewModel.statRwCount.collectAsStateWithLifecycle()
    val poorCount by viewModel.statPoorCount.collectAsStateWithLifecycle()
    val pendingLettersCount by viewModel.statPendingLettersCount.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("home") } // "home", "surat", "kependudukan", "berita", "aspirasi", "keuangan", "pembangunan", "wisata", "absensi", "gis", "qr" }
    var currentSubTitle by remember { mutableStateOf("Sistem Informasi Desa") }

    // Dialog state controllers
    var showRoleDialog by remember { mutableStateOf(false) }
    var showSubmitLetterDialog by remember { mutableStateOf(false) }
    var showApproveLetterDialog by remember { mutableStateOf<LetterRequest?>(null) }
    var showAddCitizenDialog by remember { mutableStateOf(false) }
    var showAddNewsDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddUmkmDialog by remember { mutableStateOf(false) }

    val formatter = remember { DecimalFormat("#,###") }

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SIJAGO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF2E7D32) else Color(0xFFD84315))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (if (selectedRole == "Masyarakat") "DESA SUMBER REJO - ${loggedInCitizen?.name ?: "Warga"}" else "DESA SUMBER REJO - $selectedRole") + if (isOnline) "" else " (OFFLINE)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFD84315)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { activeTab = "profile" },
                        modifier = Modifier.testTag("profile_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil Pengguna",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { activeTab = "qr" },
                        modifier = Modifier.testTag("verify_qr_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Verifikasi QR",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { showRoleDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("change_role_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ganti Role",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = selectedRole, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Keluar Secure",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Mulai", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = activeTab == "surat",
                    onClick = { activeTab = "surat" },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (selectedRole != "Masyarakat" && pendingLettersCount > 0) {
                                    Badge { Text(pendingLettersCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Description, contentDescription = "Pelayanan")
                        }
                    },
                    label = { Text("Surat", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_surat")
                )
                NavigationBarItem(
                    selected = activeTab == "berita",
                    onClick = { activeTab = "berita" },
                    icon = { Icon(Icons.Default.Newspaper, contentDescription = "Kabar") },
                    label = { Text("Berita", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_berita")
                )
                NavigationBarItem(
                    selected = activeTab == "aspirasi",
                    onClick = { activeTab = "aspirasi" },
                    icon = { Icon(Icons.Default.Feedback, contentDescription = "Suara") },
                    label = { Text("Pengaduan", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_aspirasi")
                )
                NavigationBarItem(
                    selected = activeTab == "kependudukan",
                    onClick = { activeTab = "kependudukan" },
                    icon = { Icon(Icons.Default.People, contentDescription = "Warga") },
                    label = { Text("Penduduk", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_kependudukan")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isOnline) {
                Surface(
                    color = Color(0xFFFFECE5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFFB399), RoundedCornerShape(8.dp))
                        .testTag("offline_status_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Mode",
                            tint = Color(0xFFD84315),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mode Offline Aktif",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD84315)
                            )
                            Text(
                                text = "Laporan baru akan disimpan lokal sebagai draf dan disinkronkan otomatis saat online.",
                                fontSize = 10.sp,
                                color = Color(0xFFE64A19)
                            )
                        }
                    }
                }
            }
            when (activeTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    citizenCount = citizenCount,
                    kkCount = kkCount,
                    maleCount = maleCount,
                    femaleCount = femaleCount,
                    poorCount = poorCount,
                    onNavigateToTab = { activeTab = it },
                    showAddUmkmDialog = { showAddUmkmDialog = true },
                    showAddProjectDialog = { showAddProjectDialog = true }
                )
                "surat" -> ServiceScreen(
                    letters = letters,
                    role = selectedRole,
                    onApplyLetter = { showSubmitLetterDialog = true },
                    onApproveLetter = { showApproveLetterDialog = it }
                )
                "kependudukan" -> PopulationScreen(
                    citizens = citizensList,
                    role = selectedRole,
                    onAddCitizen = { showAddCitizenDialog = true },
                    onDeleteCitizen = { viewModel.deleteCitizen(it) }
                )
                "berita" -> NewsScreen(
                    newsList = newsList,
                    role = selectedRole,
                    onAddNews = { showAddNewsDialog = true },
                    onDeleteNews = { viewModel.deleteNews(it) }
                )
                "aspirasi" -> {
                    val isSimulatedOffline by viewModel.isSimulatedOffline.collectAsStateWithLifecycle()
                    ReportScreen(
                        reports = reportsList,
                        onSubmitReport = { title, desc, cat, lat, lng, attachment ->
                            viewModel.submitReport(title, desc, cat, lat, lng, attachment)
                        },
                        role = selectedRole,
                        onUpdateStatus = { id, status ->
                            viewModel.updateReportStatus(id, status)
                        },
                        isOnline = isOnline,
                        isSimulatedOffline = isSimulatedOffline,
                        onToggleOffline = { viewModel.setSimulatedOffline(it) }
                    )
                }
                "keuangan" -> FinanceScreen(formatter = formatter)
                "pembangunan" -> ProjectScreen(
                    projects = projectsList,
                    role = selectedRole,
                    onAddProject = { showAddProjectDialog = true },
                    onDeleteProject = { viewModel.deleteProject(it) }
                )
                "wisata" -> WisataScreen(
                    umkms = umkmList,
                    role = selectedRole,
                    onAddUmkm = { showAddUmkmDialog = true },
                    onDeleteUmkm = { viewModel.deleteUmkm(it) }
                )
                "absensi" -> AttendanceScreen(
                    attendanceList = attendanceList,
                    onCheckIn = { staff, lat, lng, label ->
                        viewModel.recordCheckIn(staff, lat, lng, label)
                    },
                    onCheckOut = { recordId ->
                        viewModel.recordCheckOut(recordId)
                    }
                )
                "bansos" -> BansosScreen(
                    citizensList = citizensList,
                    role = selectedRole,
                    onUpdateBansos = { nik, isPoor, bansosType ->
                        viewModel.updateCitizenBansos(nik, isPoor, bansosType)
                    }
                )
                "gis" -> GisMapScreen(role = selectedRole)
                "qr" -> QrVerifyScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel)
            }
        }
    }

    // Interactive Dialogs definitions
    if (showRoleDialog) {
        RoleSelectionDialog(
            currentRole = selectedRole,
            onRoleSelected = {
                viewModel.changeRole(it)
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }

    if (showSubmitLetterDialog) {
        SubmitLetterFormDialog(
            viewModel = viewModel,
            onSubmit = { type, fieldMap ->
                viewModel.submitLetter(type, fieldMap)
                showSubmitLetterDialog = false
            },
            onDismiss = { showSubmitLetterDialog = false }
        )
    }

    if (showApproveLetterDialog != null) {
        val letter = showApproveLetterDialog!!
        ApproveLetterDialog(
            letter = letter,
            onApprove = { comment ->
                viewModel.updateLetterStatus(letter.id, "SELESAI", comment)
                showApproveLetterDialog = null
            },
            onReject = { comment ->
                viewModel.updateLetterStatus(letter.id, "DITOLAK", comment)
                showApproveLetterDialog = null
            },
            onProcess = {
                viewModel.updateLetterStatus(letter.id, "DIPROSES", "Sedang diproses oleh staf desa.")
                showApproveLetterDialog = null
            },
            onDismiss = { showApproveLetterDialog = null }
        )
    }

    if (showAddCitizenDialog) {
        AddCitizenDialog(
            onSave = { nik, kk, name, gender, age, job, isPoor ->
                viewModel.addNewCitizen(nik, kk, name, gender, age, job, isPoor)
                showAddCitizenDialog = false
            },
            onDismiss = { showAddCitizenDialog = false }
        )
    }

    if (showAddNewsDialog) {
        AddNewsDialog(
            onSave = { title, cat, content, imageUrl ->
                viewModel.addNews(title, cat, content, imageUrl)
                showAddNewsDialog = false
            },
            onDismiss = { showAddNewsDialog = false }
        )
    }

    if (showAddProjectDialog) {
        AddProjectDialog(
            onSave = { name, budget, fund, progress, loc ->
                viewModel.addProject(name, budget, fund, progress, loc)
                showAddProjectDialog = false
            },
            onDismiss = { showAddProjectDialog = false }
        )
    }

    if (showAddUmkmDialog) {
        AddUmkmDialog(
            onSave = { name, owner, cat, desc, price, phone, address ->
                viewModel.addUmkm(name, owner, cat, desc, price, phone, address)
                showAddUmkmDialog = false
            },
            onDismiss = { showAddUmkmDialog = false }
        )
    }
}
}

// ----------------------------------------------------
// SCREEN 1: HOME SCREEN COHESIVE WITH MODERN SHORTCUTS
// ----------------------------------------------------
@Composable
fun HomeScreen(
    viewModel: SijagoViewModel,
    citizenCount: Int,
    kkCount: Int,
    maleCount: Int,
    femaleCount: Int,
    poorCount: Int,
    onNavigateToTab: (String) -> Unit,
    showAddUmkmDialog: () -> Unit,
    showAddProjectDialog: () -> Unit
) {
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val loggedInCitizen by viewModel.loggedInCitizen.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Hero Sunrise Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1782213791936),
                contentDescription = "Pemandangan Desa Maju Jaya",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE28743))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "DESA SUMBER REJO",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selamat Datang di SIJAGO SID",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Layanan Terintegrasi, Mandiri, dan Transparan",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        // Unified Search Engine & Search Bar
        var searchQuery by remember { mutableStateOf("") }

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9)
            ),
            border = BorderStroke(
                1.dp,
                if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .testTag("village_portal_search_input"),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari surat, dokumen, berita, UMKM...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSystemInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus Pencarian",
                            tint = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.trim().lowercase()

            val lettersState by viewModel.filteredLetters.collectAsStateWithLifecycle()
            val newsState by viewModel.allNews.collectAsStateWithLifecycle()
            val umkmsState by viewModel.allUmkms.collectAsStateWithLifecycle()
            val projectsState by viewModel.allProjects.collectAsStateWithLifecycle()
            val reportsState by viewModel.filteredReports.collectAsStateWithLifecycle()

            val staticDocs = listOf(
                "Surat Keterangan Domisili" to "Keterangan domisili tempat tinggal warga.",
                "Surat Keterangan Usaha (SKU)" to "Pengantar izin usaha mikro & menengah.",
                "Surat Keterangan Tidak Mampu (SKTM)" to "Pengantar keringanan biaya pendidikan/kesehatan.",
                "Surat Pengantar KTP" to "Pengantar permohonan/perekaman KTP baru.",
                "Surat Keterangan Kelahiran" to "Pencatatan kelahiran warga baru.",
                "Surat Keterangan Kematian" to "Pencatatan kematian warga."
            ).filter { it.first.lowercase().contains(query) || it.second.lowercase().contains(query) }

            val matchedLetters = lettersState.filter {
                it.type.lowercase().contains(query) ||
                it.applicantName.lowercase().contains(query) ||
                it.trackingNo.lowercase().contains(query)
            }

            val matchedNews = newsState.filter {
                it.title.lowercase().contains(query) ||
                it.content.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }

            val matchedUmkms = umkmsState.filter {
                it.name.lowercase().contains(query) ||
                it.owner.lowercase().contains(query) ||
                it.description.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }

            val matchedProjects = projectsState.filter {
                it.name.lowercase().contains(query) ||
                it.location.lowercase().contains(query) ||
                it.fundingSource.lowercase().contains(query)
            }

            val matchedReports = reportsState.filter {
                it.title.lowercase().contains(query) ||
                it.description.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }

            val hasResults = staticDocs.isNotEmpty() || matchedLetters.isNotEmpty() || 
                             matchedNews.isNotEmpty() || matchedUmkms.isNotEmpty() || 
                             matchedProjects.isNotEmpty() || matchedReports.isNotEmpty()

            if (hasResults) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .testTag("search_results_container")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HASIL PENCARIAN PORTAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Ditemukan",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (staticDocs.isNotEmpty() || matchedLetters.isNotEmpty()) {
                        Text(
                            text = "LAYANAN DOKUMEN & SURAT DIGITAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        staticDocs.forEach { doc ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTab("surat") }
                                    .testTag("search_result_doc_template_${doc.first.replace(" ", "_")}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDBEAFE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color(0xFF1E40AF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = doc.first,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E3A8A)
                                        )
                                        Text(
                                            text = doc.second,
                                            fontSize = 11.sp,
                                            color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF1E40AF)
                                        )
                                    }
                                }
                            }
                        }

                        matchedLetters.forEach { letter ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTab("surat") }
                                    .testTag("search_result_letter_${letter.trackingNo}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = letter.type,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                        )
                                        val badgeColor = when (letter.status) {
                                            "MENUNGGU" -> Color(0xFFFF9F1C)
                                            "DIPROSES" -> Color(0xFF1D4ED8)
                                            "SELESAI" -> Color(0xFF047857)
                                            else -> Color(0xFFC53030)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeColor.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = letter.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Pelapor: ${letter.applicantName} | No: ${letter.trackingNo}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (matchedNews.isNotEmpty()) {
                        Text(
                            text = "BERITA & PENGUMUMAN PORTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        matchedNews.forEach { newsItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTab("berita") }
                                    .testTag("search_result_news_${newsItem.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = newsItem.category.uppercase(),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = newsItem.date,
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = newsItem.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = newsItem.content,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (matchedUmkms.isNotEmpty()) {
                        Text(
                            text = "UMKM & POTENSI EKONOMI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        matchedUmkms.forEach { umkm ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTab("wisata") }
                                    .testTag("search_result_umkm_${umkm.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = umkm.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Kategori: ${umkm.category} | Pemilik: ${umkm.owner}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = umkm.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (matchedProjects.isNotEmpty()) {
                        Text(
                            text = "PROGRAM PEMBANGUNAN DESA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        matchedProjects.forEach { proj ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTab("pembangunan") }
                                    .testTag("search_result_project_${proj.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = proj.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A),
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${proj.progress}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (proj.progress == 100) Color(0xFF047857) else Color(0xFF1D4ED8)
                                        )
                                    }
                                    Text(
                                        text = "Lokasi: ${proj.location} | Sumber: ${proj.fundingSource}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (matchedReports.isNotEmpty()) {
                        Text(
                            text = "SUARA & PENGADUAN WARGA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        matchedReports.forEach { report ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onNavigateToTab("aspirasi") }
                                    .testTag("search_result_report_${report.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = report.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (report.status) {
                                                        "MASUK", "DRAFT_LOKAL" -> Color(0xFFFF9F1C).copy(alpha = 0.2f)
                                                        "DIPROSES" -> Color(0xFF1D4ED8).copy(alpha = 0.2f)
                                                        else -> Color(0xFF047857).copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = report.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (report.status) {
                                                    "MASUK", "DRAFT_LOKAL" -> Color(0xFFFF9F1C)
                                                    "DIPROSES" -> Color(0xFF1D4ED8)
                                                    else -> Color(0xFF047857)
                                                }
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Kategori: ${report.category} | Dilaporkan: ${report.reporterName}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .testTag("search_no_results"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No Results Found",
                            tint = Color.Gray,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Pencarian Tidak Ditemukan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Silakan periksa kata kunci Anda atau cari dengan kategori lain seperti 'surat', 'berita', 'UMKM'.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        } else {
            // Statistics Dashboard Section as styled in Vibrant Palette
            Column(modifier = Modifier.padding(16.dp)) {
            if (selectedRole == "Admin" || selectedRole == "Kades" || selectedRole == "Sekdes") {
                val pendingLettersCount by viewModel.statPendingLettersCount.collectAsStateWithLifecycle()
                ExecutiveAdminDashboard(
                    viewModel = viewModel,
                    citizenCount = citizenCount,
                    kkCount = kkCount,
                    maleCount = maleCount,
                    femaleCount = femaleCount,
                    poorCount = poorCount,
                    pendingLettersCount = pendingLettersCount,
                    onNavigateToTab = onNavigateToTab
                )
            } else {
                Text(
                    text = "IKHTISAR KEPENDUDUKAN DESA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Penduduk Card
                    Card(
                        modifier = Modifier.weight(1f).height(90.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFE3F2FD)
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFBBDEFB)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL PENDUDUK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFF90CDF4) else Color(0xFF1E40AF),
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = citizenCount.toString(),
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Jiwa",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF1E40AF),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }
                    }

                    // Total KK Card
                    Card(
                        modifier = Modifier.weight(1f).height(90.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F8E9)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFDCEDC8)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL KK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFF93C5FD) else Color(0xFF2E7D32),
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = kkCount.toString(),
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "KK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF2E7D32),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }
                    }

                    // Total Prasejahtera Card (Welfare Stat)
                    Card(
                        modifier = Modifier.weight(1f).height(90.dp).clickable { onNavigateToTab("bansos") },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFFFF3E0)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFFFE0B2)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "KESEJAHTERAAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color(0xFFFBD38D) else Color(0xFFB45309),
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${((poorCount.toDouble() / (if (citizenCount == 0) 1 else citizenCount)) * 100).toInt()}%",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Prasejahtera",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFFB45309),
                                    modifier = Modifier.padding(bottom = 3.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Grid Menu Options of SID Modules
        Text(
            text = "MODUL INFRASTRUKTUR DESA DIGITAL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeShortcutCard(
                title = "Keuangan\nAPBDes",
                subtitle = "Transparansi Dana",
                icon = Icons.Default.AccountBalanceWallet,
                backgroundColor = Color(0xFF047857),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("keuangan") }
            )
            HomeShortcutCard(
                title = "Pembangunan\nProyek",
                subtitle = "Progress Lapangan",
                icon = Icons.Default.Engineering,
                backgroundColor = Color(0xFF1D4ED8),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("pembangunan") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeShortcutCard(
                title = "UMKM &\nWisata Desa",
                subtitle = "Potensi Ekonomi",
                icon = Icons.Default.Storefront,
                backgroundColor = Color(0xFFB45309),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("wisata") }
            )
            HomeShortcutCard(
                title = "Peta GIS &\nWilayah",
                subtitle = "Google & OSM SID",
                icon = Icons.Default.Map,
                backgroundColor = Color(0xFF6D28D9),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("gis") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeShortcutCard(
                title = "Absensi\nPerangkat",
                subtitle = "GPS & Kamera",
                icon = Icons.Default.CameraAlt,
                backgroundColor = Color(0xFF0F766E),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("absensi") }
            )
            HomeShortcutCard(
                title = "Verifikasi\nQr Surat",
                subtitle = "Keabsahan Dokumen",
                icon = Icons.Default.QrCode,
                backgroundColor = Color(0xFFBE123C),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("qr") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeShortcutCard(
                title = "Penerima\nBansos DTKS",
                subtitle = "Bantuan Terpadu",
                icon = Icons.Default.VolunteerActivism,
                backgroundColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f).testTag("shortcut_bansos"),
                onClick = { onNavigateToTab("bansos") }
            )
            HomeShortcutCard(
                title = "Analisis\nSejahtera",
                subtitle = "Verifikasi DTKS",
                icon = Icons.Default.Analytics,
                backgroundColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f).testTag("shortcut_sejahtera"),
                onClick = { onNavigateToTab("bansos") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Transparency Info Card (Vibrant Slate)
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clickable { onNavigateToTab("keuangan") },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REALISASI APBDES 2026",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rp 1.420.500.000",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "68%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Detail Keuangan",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Event Calendar & Agenda Section
        EventCalendarComponent(viewModel = viewModel)

        // Descriptive Profil Desa Section
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PROFIL DESA SUMBER REJO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mengakar Kuat, Menjulang Tinggi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Desa Sumber Rejo terletak di dataran lereng pegunungan makmur dengan kekayaan hortikultura, perkebunan cengkeh, dan peternakan sapi perah. Dipimpin oleh Kepala Desa Ir. Joko Susilo, M.Si, pemerintah desa berkomitmen mengedepankan transparansi anggaran publik desa, kemandirian layanan administrasi online melalui sistem rintisan 'SIJAGO', serta pembangunan infrastruktur modern.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Visi Desa:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "\"Mewujudkan Desa Sumber Rejo yang Mandiri, Sejahtera melalui Transformasi Ekonomi Digital Terintegrasi Berbasis Kearifan Lokal.\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}
}

@Composable
fun StatMetricItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HomeShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .height(104.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(backgroundColor.copy(alpha = if (isDark) 0.25f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDark) Color.White else backgroundColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.5f) else backgroundColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                Text(
                    text = subtitle,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 2: SERVICES - PELAYANAN SURAT DIGITAL
// ----------------------------------------------------
@Composable
fun ServiceScreen(
    letters: List<LetterRequest>,
    role: String,
    onApplyLetter: () -> Unit,
    onApproveLetter: (LetterRequest) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Layanan Surat Digital",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (role == "Masyarakat") "Ajukan & Pantau Surat Anda" else "Seluruh Permohonan Surat Warga",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            if (role == "Masyarakat") {
                Button(
                    onClick = onApplyLetter,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("apply_letter_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Surat", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (letters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AssignmentLate,
                        contentDescription = "No requests",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Pengajuan Surat",
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            letters.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("letter_item_${req.trackingNo}"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.type,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Status Badge
                            val badgeColor = when (req.status) {
                                "MENUNGGU" -> Color(0xFFFF9F1C)
                                "DIPROSES" -> Color(0xFF1D4ED8)
                                "SELESAI" -> Color(0xFF047857)
                                else -> Color(0xFFC53030) // DITOLAK
                            }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No. Register: ${req.trackingNo}",
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Pemohon: ${req.applicantName} (${req.applicantNik})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rincian: ${req.dataFields}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        if (req.adminComment.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Log Perangkat: ${req.adminComment}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Qr Signature presentation
                        if (req.status == "SELESAI") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified Signature",
                                        tint = Color(0xFF047857),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TTE Elektronik Terverifikasi (Verifikasi QR Aktif)",
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (role != "Masyarakat" && req.status != "SELESAI" && req.status != "DITOLAK") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { onApproveLetter(req) },
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp)
                                    .testTag("action_letter_${req.id}"),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Kelola Pengajuan", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 3: POPULATION - KEPENDUDUKAN DESA
// ----------------------------------------------------
@Composable
fun PopulationScreen(
    citizens: List<Citizen>,
    role: String,
    onAddCitizen: () -> Unit,
    onDeleteCitizen: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterPoorOnly by remember { mutableStateOf(false) }

    val filteredList = citizens.filter {
        (it.name.contains(searchQuery, ignoreCase = true) || it.nik.contains(searchQuery)) &&
                (!filterPoorOnly || it.isPoor)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Data Kependudukan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(
                    text = "Total Tercatat: ${citizens.size} Jiwa",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            if (role != "Masyarakat") {
                Button(
                    onClick = onAddCitizen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_citizen_button")
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari NIK atau Nama...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_citizen_input"),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filters
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filterPoorOnly,
                onClick = { filterPoorOnly = !filterPoorOnly },
                label = { Text("Prasejahtera Saja", fontSize = 11.sp) },
                leadingIcon = {
                    if (filterPoorOnly) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Penduduk Tidak Ditemukan", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            filteredList.forEach { citizen ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = citizen.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "NIK: ${citizen.nik}",
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Umur: ${citizen.age} Thn",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "Kerja: ${citizen.job}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }

                            if (citizen.isPoor) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFBE123C).copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Prasejahtera - Penerima ${citizen.bansosType}",
                                        color = Color(0xFFBE123C),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (role != "Masyarakat") {
                            IconButton(
                                onClick = { onDeleteCitizen(citizen.nik) },
                                modifier = Modifier.testTag("delete_citizen_${citizen.nik}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFBE123C))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 4: NEWS & ARCHIVE - KABAR DESA
// ----------------------------------------------------
// Cleaned up, now implemented elegantly in NewsScreen.kt

// ----------------------------------------------------
// SCREEN 5: REPORTS - SUARA WARGA, ADUAN & ASPIRASI
// ----------------------------------------------------
// Cleaned up, now implemented elegantly in PengaduanScreen.kt

// ----------------------------------------------------
// SCREEN 6: TRANSPARANSI KEUANGAN (APBDES)
// ----------------------------------------------------
@Composable
fun FinanceScreen(formatter: DecimalFormat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "APBDes Transparansi Keuangan",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Tahun Anggaran Realisasi Berlangsung 2026",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large summary cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "TOTAL ANGGARAN DESA", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "Rp 1.450.000.000", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Realisasi Pendapatan", color = Color(0xFF4ADE80), fontSize = 10.sp)
                        Text(text = "Rp 1.320.000.000", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Realisasi Belanja", color = Color(0xFFFB923C), fontSize = 10.sp)
                        Text(text = "Rp 985.000.000", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Recharts-like Monthly income vs expenditure chart
        APBDesTrendChartComponent(formatter = formatter)

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Recharts-like Budget & Expense Reports Visualizer Section
        BudgetRechartsVisualizer(formatter = formatter, isDark = isSystemInDarkTheme())

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Detail Rincian APBDesa:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                RowItemFinance(title = "Pendapatan Asli Desa (PADes)", amt = "Rp 85.000.000")
                RowItemFinance(title = "Dana Desa (DD) APBN Pusat", amt = "Rp 890.000.000")
                RowItemFinance(title = "Alokasi Dana Desa (ADD) APBD Kab", amt = "Rp 380.000.000")
                RowItemFinance(title = "Bantuan Keuangan Provinsi", amt = "Rp 95.000.000")
            }
        }
    }
}

data class SectorData(val name: String, val percent: Float, val color: Color)

@Composable
fun RowLegend(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f))
    }
}

@Composable
fun RowItemFinance(title: String, amt: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Text(text = amt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ----------------------------------------------------
// SCREEN 7: PROJECTS - MONITORING PEMBANGUNAN DESA
// ----------------------------------------------------
@Composable
fun ProjectScreen(
    projects: List<ProjectItem>,
    role: String,
    onAddProject: () -> Unit,
    onDeleteProject: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Program Pembangunan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(
                    text = "Monitoring pekerjaan & kemajuan fisik",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            if (role != "Masyarakat") {
                Button(
                    onClick = onAddProject,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_project_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buat Proyek", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        projects.forEach { proj ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = proj.fundingSource,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Kemajuan: ${proj.progress}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (proj.progress == 100) Color(0xFF047857) else Color(0xFFFF9F1C)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = proj.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Lokasi: ${proj.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    Text(text = "Anggaran Fisik: Rp ${DecimalFormat("#,###").format(proj.budget)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { proj.progress.toFloat() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (proj.progress == 100) Color(0xFF047857) else MaterialTheme.colorScheme.primary
                    )

                    if (role != "Masyarakat") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { onDeleteProject(proj.id) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_project_${proj.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFBE123C), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 8: UMKM & POTENSI DESA
// ----------------------------------------------------
@Composable
fun WisataScreen(
    umkms: List<UmkmItem>,
    role: String,
    onAddUmkm: () -> Unit,
    onDeleteUmkm: (Int) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UMKM & Potensi Desa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Sektor usaha mikro & ekonomi kreatif",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            if (role != "Masyarakat") {
                Button(
                    onClick = onAddUmkm,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_umkm_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah UMKM", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Potensi Desa section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NaturePeople,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Destinasi Edu-Wisata Sawah", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Kawasan konservasi sawah organik terasering lereng Sumber Rejo, ramai dikunjungi wisatawan akhir pekan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                }
            }
        }

        Text(text = "Daftar Direktori UMKM Warga:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        umkms.forEach { umkm ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = umkm.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Text(
                            text = umkm.priceRange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = umkm.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = umkm.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Pemilik: ${umkm.owner} | Hubungi: ${umkm.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${umkm.phone}"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hubungi Penjual", fontSize = 10.sp)
                        }

                        if (role != "Masyarakat") {
                            IconButton(
                                onClick = { onDeleteUmkm(umkm.id) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_umkm_${umkm.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFBE123C), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 9: ABSENSI PERANGKAT DESA
// ----------------------------------------------------
// (Now implemented elegantly in AttendanceScreen.kt)

// ----------------------------------------------------
// SCREEN 11: VERIFIER - SCANNER SURAT QR DESA
// ----------------------------------------------------
@Composable
fun QrVerifyScreen(viewModel: SijagoViewModel) {
    val qrResult by viewModel.qrResult.collectAsStateWithLifecycle()
    var manualCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Verifikasi Keaslian Dokumen Desa",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "Scan QR Code atau input kode registrasi surat digital",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Masukkan Kode Register Surat:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = manualCodeInput,
                    onValueChange = { manualCodeInput = it },
                    placeholder = { Text("Contoh: REG-DOM-20260623001") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_manual_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (manualCodeInput.isNotEmpty()) {
                                viewModel.verifyQrCode(manualCodeInput)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_verify_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verifikasi")
                    }

                    Button(
                        onClick = {
                            // Mock scan from a letter
                            viewModel.verifyQrCode("SIJAGO-VERIFIED-DOM-3301021008030005-20260623")
                            manualCodeInput = "SIJAGO-VERIFIED-DOM-3301021008030005-20260623"
                            Toast.makeText(SpacerContextHolder.ctx, "Simulasi pembacaan QR Code Berhasil!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_mock_qr_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulasi Scan")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (qrResult != null) {
            val letter = qrResult!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_result_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                border = BorderStroke(1.5.dp, Color(0xFF059669))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = "Valid", tint = Color(0xFF059669), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "DOKUMEN TERVERIFIKASI SAH", fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Jenis Surat: ${letter.type}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "No. Registrasi: ${letter.trackingNo}", fontSize = 12.sp)
                    Text(text = "Pemohon: ${letter.applicantName} (${letter.applicantNik})", fontSize = 12.sp)
                    Text(text = "Status Terakhir: ${letter.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Catatan: ${letter.adminComment}", fontSize = 11.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF059669).copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dokumen ini sah bertanda tangan elektronik secara tersertifikasi melalui sistem pelayanan administrasi terpadu SIJAGO Pemdes Sumber Rejo.",
                        fontSize = 10.sp,
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.clearQrResult(); manualCodeInput = "" },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Text("Bersihkan Hasil")
                    }
                }
            }
        } else {
            if (manualCodeInput.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Kode/Barcode tidak cocok. Silakan ulangi kembali.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ----------------------------------------------------
// UI SUPPORT DIALOGS
// ----------------------------------------------------
@Composable
fun RoleSelectionDialog(
    currentRole: String,
    onRoleSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val roles = listOf("Masyarakat", "Kepala Desa", "Sekretaris Desa", "Operator Desa", "RT / RW")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Pilih Hak Akses (Simulasi)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                roles.forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoleSelected(role) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentRole == role,
                            onClick = { onRoleSelected(role) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = role, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                }
            }
        }
    }
}

@Composable
fun ApproveLetterDialog(
    letter: LetterRequest,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onProcess: () -> Unit,
    onDismiss: () -> Unit
) {
    var adminComment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Kelola Pengajuan Surat", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "${letter.type}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Pemohon: ${letter.applicantName}", fontSize = 12.sp)
                Text(text = "NIK: ${letter.applicantNik}", fontSize = 12.sp)
                Text(text = "Rincian kolom: ${letter.dataFields}", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminComment,
                    onValueChange = { adminComment = it },
                    label = { Text("Tindak Lanjut / Catatan Perangkat") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_comment_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onProcess,
                        modifier = Modifier.weight(1f).testTag("dialog_process_button"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        Text("Proses", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onApprove(adminComment) },
                        modifier = Modifier.weight(1f).testTag("dialog_approve_button"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Text("Setujui", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onReject(adminComment) },
                        modifier = Modifier.weight(1f).testTag("dialog_reject_button"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C))
                    ) {
                        Text("Tolak", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Kembali") }
                }
            }
        }
    }
}

@Composable
fun AddCitizenDialog(
    onSave: (String, String, String, String, Int, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var nik by remember { mutableStateOf("") }
    var kk by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Laki-laki") }
    var age by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var isPoor by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Entri Data Penduduk Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(value = nik, onValueChange = { nik = it }, label = { Text("Nomor NIK (16 digit)") }, modifier = Modifier.fillMaxWidth().testTag("add_nik"))
                OutlinedTextField(value = kk, onValueChange = { kk = it }, label = { Text("Nomor KK") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth().testTag("add_name"))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Laki-laki", onClick = { gender = "Laki-laki" })
                    Text("Laki-laki")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = gender == "Perempuan", onClick = { gender = "Perempuan" })
                    Text("Perempuan")
                }

                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Umur (Tahun)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = job, onValueChange = { job = it }, label = { Text("Pekerjaan") }, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = isPoor, onCheckedChange = { isPoor = it })
                    Text("Keluarga Prasejahtera (Bansos Aktif)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Bantal") }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (nik.isNotEmpty() && name.isNotEmpty()) {
                                onSave(nik, kk, name, gender, age.toIntOrNull() ?: 30, job, isPoor)
                            }
                        },
                        modifier = Modifier.testTag("submit_add_citizen_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Cleaned up, now implemented elegantly in NewsScreen.kt

@Composable
fun AddProjectDialog(
    onSave: (String, Double, String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var fundingSource by remember { mutableStateOf("Dana Desa (DD)") }
    var progress by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Realisasi Program Pembangunan baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Proyek Infrastuktur") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Anggaran Biaya (Rupiah)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Row {
                    RadioButton(selected = fundingSource == "Dana Desa (DD)", onClick = { fundingSource = "Dana Desa (DD)" })
                    Text("DD", fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = fundingSource == "ADD", onClick = { fundingSource = "ADD" })
                    Text("ADD", fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterVertically))
                }

                OutlinedTextField(value = progress, onValueChange = { progress = it }, label = { Text("Progress Kemajuan Fisik (0-100)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi Pembangunan") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onSave(name, budget.toDoubleOrNull() ?: 10000000.0, fundingSource, progress.toIntOrNull() ?: 0, location)
                            }
                        }
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun AddUmkmDialog(
    onSave: (String, String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Kuliner") }
    var desc by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Promosi UMKM Warga baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk / Toko") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Nama Pengusaha / Pemilik") }, modifier = Modifier.fillMaxWidth())

                val categories = listOf("Kuliner", "Kerajinan", "Pertanian", "Jasa")
                Row {
                    categories.forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, fontSize = 10.sp) }, modifier = Modifier.padding(horizontal = 2.dp))
                    }
                }

                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Deskripsi Singkat Jualan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = priceRange, onValueChange = { priceRange = it }, label = { Text("Kisaran Harga (e.g. Rp 5.000)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No WhatsApp Pemilik") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat Usaha Lahan") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onSave(name, owner, category, desc, priceRange, phone, address)
                            }
                        }
                    ) {
                        Text("Publikasikan")
                    }
                }
            }
        }
    }
}

// Global Ambient Context Holder to feed Toast and local scopes
object SpacerContextHolder {
    lateinit var ctx: android.content.Context
}
