package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReportItem
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold
import com.example.ui.theme.GovNavy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reports: List<ReportItem>,
    onSubmitReport: (String, String, String, Double, Double, String) -> Unit,
    role: String,
    onUpdateStatus: (Int, String) -> Unit,
    isOnline: Boolean,
    isSimulatedOffline: Boolean,
    onToggleOffline: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    // Form inputs state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Infrastruktur") }
    var attachedPhotoPath by remember { mutableStateOf("") }
    var attachedPhotoLabel by remember { mutableStateOf("") }

    // Navigation/Filter states for the dashboard
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var expandedReportId by remember { mutableStateOf<Int?>(null) }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    val categories = listOf("Infrastruktur", "Sosial", "Pelayanan", "Keamanan", "Darurat")

    // Filter reports list based on interactive dashboard selection
    val filteredReports = remember(reports, selectedStatusFilter) {
        if (selectedStatusFilter == null) {
            reports
        } else {
            reports.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    // Dashboard Statistics calculations
    val totalCount = reports.size
    val masukCount = reports.count { it.status == "MASUK" || it.status == "DRAFT_LOKAL" }
    val prosesCount = reports.count { it.status == "DIPROSES" }
    val selesaiCount = reports.count { it.status == "SELESAI" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .testTag("report_screen_container"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Simulated Offline Switch widget (High Polish Banner)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_simulation_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) {
                        if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                    } else {
                        if (isDark) Color(0xFF3B1E1E) else Color(0xFFFEF2F2)
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (isOnline) {
                        if (isDark) Color(0xFF334155) else Color(0xFFBFDBFE)
                    } else {
                        if (isDark) Color(0xFF5F2121) else Color(0xFFFCA5A5)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOnline) {
                                        if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
                                    } else {
                                        if (isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                contentDescription = "Simulasi Koneksi",
                                tint = if (isOnline) EmeraldGreen else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOnline) "Koneksi Sistem Aktif" else "Koneksi Offline (Lokal)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isOnline) "Laporan langsung sinkron ke server Pemdes" else "Laporan disimpan di draf lokal perangkat",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                    Switch(
                        checked = isSimulatedOffline,
                        onCheckedChange = { onToggleOffline(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFEF4444),
                            checkedTrackColor = Color(0xFFEF4444).copy(alpha = 0.3f),
                            uncheckedThumbColor = EmeraldGreen,
                            uncheckedTrackColor = EmeraldGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("toggle_offline_simulation")
                    )
                }
            }
        }

        // Section 2: Form Input for Masyarakat (Citizens)
        if (role == "Masyarakat") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_input_form_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kirim Pengaduan Warga",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "Sampaikan aspirasi atau aduan terkait infrastruktur dan fasilitas desa",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Judul Pengaduan
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Judul Pengaduan (cth: Jalan Rusak Dusun III)") },
                            label = { Text("Pokok Pengaduan", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = EmeraldGreen,
                                unfocusedIndicatorColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_title_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Deskripsi Pengaduan
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Tuliskan detail keluhan secara komprehensif agar dapat segera ditindaklanjuti...") },
                            label = { Text("Deskripsi Lengkap", fontSize = 12.sp) },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = EmeraldGreen,
                                unfocusedIndicatorColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_desc_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Selection
                        Text(
                            text = "Kategori Pengaduan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen,
                                        selectedLabelColor = Color.White,
                                        containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                        labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                        selectedBorderColor = EmeraldGreen
                                    ),
                                    modifier = Modifier.testTag("category_chip_$cat")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Attachment Upload Section
                        Text(
                            text = "Lampiran Foto Bukti",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (attachedPhotoPath.isEmpty()) {
                            // Empty Upload state button
                            OutlinedButton(
                                onClick = { showAttachmentDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("upload_attachment_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = EmeraldGreen
                                ),
                                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhotoCamera,
                                        contentDescription = "Unggah Foto Bukti",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ambil / Unggah Foto Bukti", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Selected attachment preview card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("attachment_preview_card"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF)
                                ),
                                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.InsertDriveFile,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = attachedPhotoLabel,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Color(0xFF1E293B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Terlampir (Simulasi File Kamera)",
                                                fontSize = 10.sp,
                                                color = EmeraldGreen
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            attachedPhotoPath = ""
                                            attachedPhotoLabel = ""
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("remove_attachment_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Hapus Lampiran",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (title.isEmpty()) {
                                    Toast.makeText(context, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                } else if (description.isEmpty()) {
                                    Toast.makeText(context, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSubmitReport(
                                        title,
                                        description,
                                        selectedCategory,
                                        -7.4532,
                                        110.3661,
                                        attachedPhotoPath
                                    )
                                    title = ""
                                    description = ""
                                    attachedPhotoPath = ""
                                    attachedPhotoLabel = ""
                                    Toast.makeText(
                                        context,
                                        if (isOnline) "Pengaduan terkirim ke server desa!" else "Disimpan sebagai draf pengaduan lokal.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kirim Laporan Pengaduan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Section 3: Status Tracking Dashboard Title & Stats Counters
        item {
            Column {
                Text(
                    text = "Dashboard Pengaduan Desa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Pantau status pengaduan warga secara transparan dan berkala",
                    fontSize = 11.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Box
                    StatsCard(
                        title = "TOTAL",
                        count = totalCount,
                        isSelected = selectedStatusFilter == null,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        onClick = { selectedStatusFilter = null },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stats_card_total")
                    )

                    // Masuk/Draft Box
                    StatsCard(
                        title = "MASUK",
                        count = masukCount,
                        isSelected = selectedStatusFilter == "MASUK",
                        color = GovGold,
                        onClick = { selectedStatusFilter = "MASUK" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stats_card_masuk")
                    )

                    // Diproses Box
                    StatsCard(
                        title = "PROSES",
                        count = prosesCount,
                        isSelected = selectedStatusFilter == "DIPROSES",
                        color = EmeraldGreen,
                        onClick = { selectedStatusFilter = "DIPROSES" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stats_card_proses")
                    )

                    // Selesai Box
                    StatsCard(
                        title = "SELESAI",
                        count = selesaiCount,
                        isSelected = selectedStatusFilter == "SELESAI",
                        color = GovNavy,
                        onClick = { selectedStatusFilter = "SELESAI" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stats_card_selesai")
                    )
                }

                if (selectedStatusFilter != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Menampilkan status: $selectedStatusFilter",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus filter",
                            tint = EmeraldGreen,
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { selectedStatusFilter = null }
                        )
                    }
                }
            }
        }

        // Section 4: Reports list
        if (filteredReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RateReview,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak Ada Pengaduan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = "Saat ini tidak ada laporan terdaftar untuk kriteria penyaringan.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredReports, key = { it.id }) { report ->
                val isExpanded = expandedReportId == report.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedReportId = if (isExpanded) null else report.id }
                        .testTag("report_card_item_${report.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isExpanded) 1.5.dp else 1.dp,
                        color = if (isExpanded) EmeraldGreen else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Category and Status header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (report.category) {
                                                "Infrastruktur" -> Color(0xFF1D4ED8).copy(alpha = 0.1f)
                                                "Social", "Sosial" -> GovGold.copy(alpha = 0.1f)
                                                "Pelayanan" -> EmeraldGreen.copy(alpha = 0.1f)
                                                "Darurat" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                                else -> Color.Gray.copy(alpha = 0.1f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = report.category.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = when (report.category) {
                                            "Infrastruktur" -> Color(0xFF1D4ED8)
                                            "Social", "Sosial" -> GovGold
                                            "Pelayanan" -> EmeraldGreen
                                            "Darurat" -> Color(0xFFEF4444)
                                            else -> Color.Gray
                                        }
                                    )
                                }

                                if (!report.isSynced) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFFECE5))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SyncProblem,
                                            contentDescription = "Pending Sync",
                                            tint = Color(0xFFD84315),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "LOKAL",
                                            color = Color(0xFFD84315),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Status Tag
                            val (statColor, statLabel) = when (report.status) {
                                "DRAFT_LOKAL" -> Color(0xFF64748B) to "DRAF LOKAL"
                                "MASUK" -> GovGold to "MASUK"
                                "DIPROSES" -> EmeraldGreen to "DIPROSES"
                                "SELESAI" -> GovNavy to "SELESAI"
                                else -> Color.Gray to report.status
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statColor.copy(alpha = 0.15f))
                                    .border(1.dp, statColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statLabel,
                                    color = statColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title
                        Text(
                            text = report.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Description
                        Text(
                            text = report.description,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Render Attachment thumbnail inside card if present
                        if (report.photoLocalPath.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Attachment,
                                    contentDescription = "Lampiran",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lampiran: ${report.photoLocalPath}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reporter details row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Oleh: ${report.reporterName}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )

                            val df = SimpleDateFormat("dd MMM 2026, HH:mm", Locale.getDefault())
                            val dateStr = df.format(Date(report.timestamp))
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        // Expanded section details: Timeline Tracker & Admin Status update buttons
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Status Tracking Timeline Dashboard (Material 3 tracker)
                                Text(
                                    text = "Status Alur Tindak Lanjut",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isDark) Color.White else Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Stage 1: Diajukan
                                TimelineStep(
                                    title = "Laporan Masuk",
                                    description = "Diterima oleh sistem administrasi Desa Sumber Rejo",
                                    timestamp = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(report.timestamp)),
                                    isCompleted = true,
                                    isActive = report.status == "MASUK" || report.status == "DRAFT_LOKAL"
                                )

                                // Stage 2: Diproses
                                TimelineStep(
                                    title = "Dalam Proses",
                                    description = "Laporan diteruskan ke Kepala Urusan Pembangunan & Kemasyarakatan",
                                    timestamp = if (report.status == "DIPROSES" || report.status == "SELESAI") {
                                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(report.timestamp + 3600000))
                                    } else "-",
                                    isCompleted = report.status == "DIPROSES" || report.status == "SELESAI",
                                    isActive = report.status == "DIPROSES"
                                )

                                // Stage 3: Selesai
                                TimelineStep(
                                    title = "Selesai Tindak Lanjut",
                                    description = "Kendala selesai diperbaiki / aspirasi disalurkan sepenuhnya",
                                    timestamp = if (report.status == "SELESAI") {
                                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(report.timestamp + 7200000))
                                    } else "-",
                                    isCompleted = report.status == "SELESAI",
                                    isActive = report.status == "SELESAI",
                                    isLast = true
                                )

                                // Official / Admin Controls (Role validation)
                                if (role != "Masyarakat" && report.status != "SELESAI") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tindakan Pejabat Desa / Admin:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (report.status != "DIPROSES") {
                                            Button(
                                                onClick = { onUpdateStatus(report.id, "DIPROSES") },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("admin_process_btn_${report.id}")
                                            ) {
                                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Proses Aduan", fontSize = 11.sp)
                                            }
                                        }

                                        Button(
                                            onClick = { onUpdateStatus(report.id, "SELESAI") },
                                            colors = ButtonDefaults.buttonColors(containerColor = GovNavy),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_complete_btn_${report.id}")
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Selesaikan Laporan", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Camera / Gallery Simulated Attachment Picker Dialog Sheet
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unggah Lampiran Aduan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Silakan pilih sumber lampiran foto bukti untuk pengaduan Anda:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // simulated CAMERA Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                attachedPhotoPath = "CAMERA_ADUAN_$timestamp.jpg"
                                attachedPhotoLabel = "Ambil Kamera: $attachedPhotoPath"
                                showAttachmentDialog = false
                            }
                            .testTag("simulate_camera_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Ambil dengan Kamera (Simulasi)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Gunakan kamera ponsel untuk mengambil bukti langsung", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    // simulated GALLERY Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                attachedPhotoPath = "IMG_GALERI_$timestamp.jpg"
                                attachedPhotoLabel = "Galeri: $attachedPhotoPath"
                                showAttachmentDialog = false
                            }
                            .testTag("simulate_gallery_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GovGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = GovGold, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Unggah dari Galeri (Simulasi)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Pilih file gambar bukti dari penyimpanan lokal perangkat", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("Batal", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                color.copy(alpha = 0.12f)
            } else {
                if (isDark) Color(0xFF1E293B) else Color.White
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) color else (if (isDark) Color.White else Color(0xFF0F172A))
            )
        }
    }
}

@Composable
fun TimelineStep(
    title: String,
    description: String,
    timestamp: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step bullet and connector line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) {
                            if (isActive) EmeraldGreen else GovNavy
                        } else Color.Gray.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && !isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(
                            if (isCompleted && !isActive) GovNavy else Color.Gray.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isActive) EmeraldGreen else (if (isDark) Color.White else Color(0xFF1E293B))
                )
                Text(
                    text = timestamp,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                fontSize = 10.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                lineHeight = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
