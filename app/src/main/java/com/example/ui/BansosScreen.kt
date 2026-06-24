package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Citizen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold

enum class BansosSortColumn {
    NAME, NIK, AGE, STATUS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BansosScreen(
    citizensList: List<Citizen>,
    role: String,
    onUpdateBansos: (String, Boolean, String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedBansosFilter by remember { mutableStateOf("Semua") } // Semua, PKH, BPNT, BLT, Non-Penerima
    var selectedDusunFilter by remember { mutableStateOf("Semua") }

    // Column sorting states
    var sortColumn by remember { mutableStateOf(BansosSortColumn.NAME) }
    var sortAscending by remember { mutableStateOf(true) }

    // Recipient selection details dialog state
    var selectedCitizenForDetail by remember { mutableStateOf<Citizen?>(null) }

    // Derive list of Dusun for dynamic dropdown filtering
    val dusunOptions = remember(citizensList) {
        listOf("Semua") + citizensList.map { it.dusun }.distinct().sorted()
    }

    // Live Metrics Calculations
    val stats = remember(citizensList) {
        val totalRecipients = citizensList.count { it.isPoor && it.bansosType != "Tidak Ada" }
        val pkhCount = citizensList.count { it.isPoor && it.bansosType == "PKH" }
        val bpntCount = citizensList.count { it.isPoor && it.bansosType == "BPNT" }
        val bltCount = citizensList.count { it.isPoor && it.bansosType == "BLT" }
        Triple(totalRecipients, pkhCount, Pair(bpntCount, bltCount))
    }

    val totalRecipientsCount = stats.first
    val pkhRecipientsCount = stats.second
    val bpntRecipientsCount = stats.third.first
    val bltRecipientsCount = stats.third.second

    // Applied search, filter, and sorting logic
    val processedList = remember(citizensList, searchQuery, selectedBansosFilter, selectedDusunFilter, sortColumn, sortAscending) {
        citizensList.filter { cit ->
            val matchesSearch = cit.name.contains(searchQuery, ignoreCase = true) || cit.nik.contains(searchQuery)
            val matchesBansos = when (selectedBansosFilter) {
                "Semua" -> true
                "Non-Penerima" -> !cit.isPoor || cit.bansosType == "Tidak Ada"
                else -> cit.isPoor && cit.bansosType.equals(selectedBansosFilter, ignoreCase = true)
            }
            val matchesDusun = selectedDusunFilter == "Semua" || cit.dusun.equals(selectedDusunFilter, ignoreCase = true)
            matchesSearch && matchesBansos && matchesDusun
        }.sortedWith { c1, c2 ->
            val result = when (sortColumn) {
                BansosSortColumn.NAME -> c1.name.compareTo(c2.name, ignoreCase = true)
                BansosSortColumn.NIK -> c1.nik.compareTo(c2.nik)
                BansosSortColumn.AGE -> c1.age.compareTo(c2.age)
                BansosSortColumn.STATUS -> c1.bansosType.compareTo(c2.bansosType, ignoreCase = true)
            }
            if (sortAscending) result else -result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // App Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bantuan Sosial (Bansos)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Data terpadu penerima bantuan sosial Desa Sumber Rejo",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
            
            // Indicator Badge for user privilege level
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (role != "Masyarakat") {
                            EmeraldGreen.copy(alpha = 0.12f)
                        } else {
                            Color.Gray.copy(alpha = 0.12f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (role != "Masyarakat") Icons.Default.VerifiedUser else Icons.Default.PersonOutline,
                        contentDescription = null,
                        tint = if (role != "Masyarakat") EmeraldGreen else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (role != "Masyarakat") "Petugas SID" else "Warga Publik",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (role != "Masyarakat") EmeraldGreen else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics Summary Dashboard Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricBadge(
                title = "Total Penerima",
                value = "$totalRecipientsCount KK",
                accentColor = EmeraldGreen,
                icon = Icons.Default.Groups,
                isDark = isDark
            )
            MetricBadge(
                title = "Program PKH",
                value = "$pkhRecipientsCount Jiwa",
                accentColor = Color(0xFFBE123C),
                icon = Icons.Default.FamilyRestroom,
                isDark = isDark
            )
            MetricBadge(
                title = "Bansos BPNT",
                value = "$bpntRecipientsCount Jiwa",
                accentColor = Color(0xFF1D4ED8),
                icon = Icons.Default.ShoppingBag,
                isDark = isDark
            )
            MetricBadge(
                title = "Bantuan BLT",
                value = "$bltRecipientsCount Jiwa",
                accentColor = Color(0xFFD97706),
                icon = Icons.Default.MonetizationOn,
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactivity Controllers: Search field & Dusun selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari NIK atau nama warga...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = EmeraldGreen,
                    unfocusedIndicatorColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                    unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White
                ),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("bansos_search_input")
            )

            // Dusun filter dropdown
            var showDusunMenu by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDusunMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedDusunFilter == "Semua") "Semua Dusun" else selectedDusunFilter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Dusun",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showDusunMenu,
                    onDismissRequest = { showDusunMenu = false },
                    modifier = Modifier
                        .background(if (isDark) Color(0xFF1E293B) else Color.White)
                ) {
                    dusunOptions.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d, fontSize = 13.sp) },
                            onClick = {
                                selectedDusunFilter = d
                                showDusunMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Category Quick Filter Chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf("Semua", "PKH", "BPNT", "BLT", "Non-Penerima")
            filters.forEach { filter ->
                val isSelected = selectedBansosFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                            } else {
                                if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) {
                                EmeraldGreen
                            } else {
                                if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedBansosFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("bansos_filter_chip_${filter.lowercase()}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (filter == "Non-Penerima") "Bukan Penerima" else filter,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                EmeraldGreen
                            } else {
                                if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabular Content: Custom Data Table in Compose. Make it Scrollable horizontally.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Headers (Row with sorting capability indicators)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Columns layout headers: Name (flex: 2), NIK (flex: 1.5), Dusun/Address (flex: 1.5), Status/Type (flex: 1.2), Action (flex: 0.8)
                    TableHeaderCell(
                        text = "NAMA PENERIMA",
                        column = BansosSortColumn.NAME,
                        activeSortColumn = sortColumn,
                        sortAscending = sortAscending,
                        modifier = Modifier.weight(2f),
                        onClick = {
                            if (sortColumn == BansosSortColumn.NAME) sortAscending = !sortAscending
                            else { sortColumn = BansosSortColumn.NAME; sortAscending = true }
                        }
                    )
                    TableHeaderCell(
                        text = "NIK",
                        column = BansosSortColumn.NIK,
                        activeSortColumn = sortColumn,
                        sortAscending = sortAscending,
                        modifier = Modifier.weight(1.5f),
                        onClick = {
                            if (sortColumn == BansosSortColumn.NIK) sortAscending = !sortAscending
                            else { sortColumn = BansosSortColumn.NIK; sortAscending = true }
                        }
                    )
                    TableHeaderCell(
                        text = "WILAYAH / DUSUN",
                        column = BansosSortColumn.AGE,
                        activeSortColumn = sortColumn,
                        sortAscending = sortAscending,
                        modifier = Modifier.weight(1.5f),
                        onClick = {
                            if (sortColumn == BansosSortColumn.AGE) sortAscending = !sortAscending
                            else { sortColumn = BansosSortColumn.AGE; sortAscending = true }
                        }
                    )
                    TableHeaderCell(
                        text = "JENIS BANSOS",
                        column = BansosSortColumn.STATUS,
                        activeSortColumn = sortColumn,
                        sortAscending = sortAscending,
                        modifier = Modifier.weight(1.3f),
                        onClick = {
                            if (sortColumn == BansosSortColumn.STATUS) sortAscending = !sortAscending
                            else { sortColumn = BansosSortColumn.STATUS; sortAscending = true }
                        }
                    )
                    Text(
                        text = "AKSI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))

                // Table Rows
                if (processedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada data warga penerima bansos", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(processedList) { citizen ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCitizenForDetail = citizen }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Name col
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(
                                        text = citizen.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E293B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Umur: ${citizen.age} Thn",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                // NIK col
                                Text(
                                    text = citizen.nik.take(4) + "..." + citizen.nik.takeLast(4),
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                    modifier = Modifier.weight(1.5f)
                                )

                                // Dusun / RT-RW col
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = citizen.dusun,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "RT ${citizen.rt} / RW ${citizen.rw}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                // Bansos Label Indicator block
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .padding(end = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    val isPoorBansos = citizen.isPoor && citizen.bansosType != "Tidak Ada"
                                    val pillBg = when {
                                        !isPoorBansos -> if (isDark) Color(0xFF334155).copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                                        citizen.bansosType == "PKH" -> Color(0xFFBE123C).copy(alpha = 0.12f)
                                        citizen.bansosType == "BPNT" -> Color(0xFF1D4ED8).copy(alpha = 0.12f)
                                        else -> Color(0xFFD97706).copy(alpha = 0.12f)
                                    }
                                    val pillColor = when {
                                        !isPoorBansos -> Color.Gray
                                        citizen.bansosType == "PKH" -> Color(0xFFE11D48)
                                        citizen.bansosType == "BPNT" -> Color(0xFF3B82F6)
                                        else -> Color(0xFFF59E0B)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(pillBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isPoorBansos) citizen.bansosType else "Bukan",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = pillColor
                                        )
                                    }
                                }

                                // Interactive View details arrow action button
                                Box(
                                    modifier = Modifier.weight(0.9f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = { selectedCitizenForDetail = citizen },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                                CircleShape
                                            )
                                            .testTag("bansos_row_action_${citizen.nik}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = "Details",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Divider(color = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFEDF2F7))
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to view recipient details Or Alter registration
    selectedCitizenForDetail?.let { citizen ->
        BansosDetailDialog(
            citizen = citizen,
            role = role,
            onDismiss = { selectedCitizenForDetail = null },
            onUpdateBansos = { isPoor, type ->
                onUpdateBansos(citizen.nik, isPoor, type)
                selectedCitizenForDetail = null
                Toast.makeText(context, "Bansos ${citizen.name} berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun TableHeaderCell(
    text: String,
    column: BansosSortColumn,
    activeSortColumn: BansosSortColumn,
    sortAscending: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isActive = column == activeSortColumn
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) EmeraldGreen else (if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isActive) {
            Icon(
                imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = "Sort Icon",
                tint = EmeraldGreen,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
fun MetricBadge(
    title: String,
    value: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean
) {
    Card(
        modifier = Modifier
            .size(width = 135.dp, height = 74.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title.uppercase(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BansosDetailDialog(
    citizen: Citizen,
    role: String,
    onDismiss: () -> Unit,
    onUpdateBansos: (Boolean, String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isAdmin = role != "Masyarakat"

    // Form states
    var isPoorState by remember { mutableStateOf(citizen.isPoor) }
    var bansosTypeState by remember { mutableStateOf(citizen.bansosType) }

    val presetBansosTypes = listOf("Tidak Ada", "PKH", "BPNT", "BLT")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Topic logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Profil Penerima Bansos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Kependudukan & Bantuan Terpadu Dinas Sosial",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Detail Information Block
                DetailRow(label = "Nama Lengkap", value = citizen.name, isDark = isDark)
                DetailRow(label = "Nomor NIK", value = citizen.nik, isDark = isDark, isMonospace = true)
                DetailRow(label = "Nomor KK", value = citizen.kk, isDark = isDark, isMonospace = true)
                DetailRow(label = "Wilayah / Dusun", value = "${citizen.dusun}, RT ${citizen.rt} / RW ${citizen.rw}", isDark = isDark)
                DetailRow(label = "Umur / Gender", value = "${citizen.age} Tahun, ${citizen.gender}", isDark = isDark)
                DetailRow(label = "Pekerjaan", value = citizen.job, isDark = isDark)
                DetailRow(label = "Pendidikan Terakhir", value = citizen.education, isDark = isDark)
                DetailRow(label = "Telepon", value = citizen.phoneNumber, isDark = isDark)

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Modify Settings Block (Only shown if user is admin / operator)
                if (isAdmin) {
                    Text(
                        text = "Kelola Status Bantuan Sosial",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldGreen
                    )
                    Text(
                        text = "Sesuaikan kualifikasi ekonomi untuk verifikasi dtks:",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Poor qualifications switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .clickable {
                                isPoorState = !isPoorState
                                if (!isPoorState) bansosTypeState = "Tidak Ada"
                                else if (bansosTypeState == "Tidak Ada") bansosTypeState = "PKH"
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isPoorState,
                            onCheckedChange = { v ->
                                isPoorState = v
                                if (!v) bansosTypeState = "Tidak Ada"
                                else if (bansosTypeState == "Tidak Ada") bansosTypeState = "PKH"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldGreen,
                                checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("switch_is_poor")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Keluarga Prasejahtera",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = "Tandai layak bansos DTKS",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bansos Type Dropdown Row (Only active if isPoorState is true)
                    Text(
                        text = "Metode / Jenis Alokasi Bansos:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetBansosTypes.forEach { type ->
                            val isSelected = bansosTypeState == type
                            val isEnabled = isPoorState || type == "Tidak Ada"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) EmeraldGreen.copy(alpha = 0.15f)
                                        else if (!isEnabled) Color.Gray.copy(alpha = 0.05f)
                                        else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) EmeraldGreen else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = isEnabled) { bansosTypeState = type }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("bansos_preset_option_$type")
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldGreen
                                    else if (!isEnabled) Color.Gray.copy(alpha = 0.3f)
                                    else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                                )
                            }
                        }
                    }
                } else {
                    // Non admin static view
                    Text(
                        text = "Status Verifikasi Sosial",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (citizen.isPoor) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (citizen.isPoor) EmeraldGreen else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (citizen.isPoor) "Terverifikasi Penerima Bansos" else "Non-Penerima Bansos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            if (citizen.isPoor) {
                                Text(
                                    text = "Aktif sebagai penerima bantuan ${citizen.bansosType}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action controls footer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tutup", fontSize = 13.sp)
                    }

                    if (isAdmin) {
                        Button(
                            onClick = {
                                onUpdateBansos(isPoorState, bansosTypeState)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("bansos_save_update_btn")
                        ) {
                            Text("Simpan Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isDark: Boolean,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White else Color(0xFF1E293B),
            fontFamily = if (isMonospace) androidx.compose.ui.text.font.FontFamily.Monospace else null,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
