package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.VillageEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCalendarComponent(
    viewModel: SijagoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()

    // Calendar state
    var selectedYear by remember { mutableStateOf(2026) }
    var selectedMonth by remember { mutableStateOf(5) } // 5 = June (0-indexed Calendar.JUNE is 5)
    var selectedDay by remember { mutableStateOf<Int?>(24) } // Default to June 24 (Today in context)
    var activeCategoryFilter by remember { mutableStateOf("ALL") } // ALL, MEETING, ACTIVITY, DEADLINE
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEventForDetail by remember { mutableStateOf<VillageEvent?>(null) }

    // Month names
    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    // Calculate days in selected month
    val daysInMonth = when (selectedMonth) {
        0, 2, 4, 6, 7, 9, 11 -> 31
        3, 5, 8, 10 -> 30
        1 -> if ((selectedYear % 4 == 0 && selectedYear % 100 != 0) || (selectedYear % 400 == 0)) 29 else 28
        else -> 30
    }

    // Days of week helper
    fun getDayOfWeekLabel(day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, day)
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Min"
            Calendar.MONDAY -> "Sen"
            Calendar.TUESDAY -> "Sel"
            Calendar.WEDNESDAY -> "Rab"
            Calendar.THURSDAY -> "Kam"
            Calendar.FRIDAY -> "Jum"
            Calendar.SATURDAY -> "Sab"
            else -> "Sen"
        }
    }

    // Date formatting helper for events
    val formattedSelectedDateString = remember(selectedYear, selectedMonth, selectedDay) {
        if (selectedDay == null) ""
        else {
            val monthStr = String.format("%02d", selectedMonth + 1)
            val dayStr = String.format("%02d", selectedDay)
            "$selectedYear-$monthStr-$dayStr"
        }
    }

    // Filter events based on selections
    val filteredEvents = remember(events, selectedDay, formattedSelectedDateString, activeCategoryFilter) {
        events.filter { event ->
            val matchesDate = if (selectedDay == null) true else event.date == formattedSelectedDateString
            val matchesCategory = if (activeCategoryFilter == "ALL") true else event.type == activeCategoryFilter
            matchesDate && matchesCategory
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Role-based Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AGENDA & KALENDER DESA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Kegiatan & Tenggat Penting",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                    )
                }

                if (selectedRole == "Admin" || selectedRole == "Kades" || selectedRole == "Sekdes") {
                    IconButton(
                        onClick = { showAddDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("village_portal_calendar_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Agenda",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedMonth == 0) {
                            selectedMonth = 11
                            selectedYear--
                        } else {
                            selectedMonth--
                        }
                        selectedDay = null // Reset day selection on month change
                    },
                    modifier = Modifier.testTag("village_portal_calendar_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Bulan Sebelumnya",
                        tint = if (isSystemInDarkTheme()) Color.White else Color(0xFF475569)
                    )
                }

                Text(
                    text = "${months[selectedMonth]} $selectedYear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
                )

                IconButton(
                    onClick = {
                        if (selectedMonth == 11) {
                            selectedMonth = 0
                            selectedYear++
                        } else {
                            selectedMonth++
                        }
                        selectedDay = null // Reset day selection on month change
                    },
                    modifier = Modifier.testTag("village_portal_calendar_next_month")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Bulan Berikutnya",
                        tint = if (isSystemInDarkTheme()) Color.White else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Day Row (Horizontal Calendar Grid)
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" / "Semua" option card
                Card(
                    modifier = Modifier
                        .width(55.dp)
                        .height(68.dp)
                        .clickable { selectedDay = null },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDay == null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isSystemInDarkTheme()) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFF8FAFC)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedDay == null) MaterialTheme.colorScheme.primary else {
                            if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Semua",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDay == null) Color.White else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Semua Agenda",
                            tint = if (selectedDay == null) Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Days of month
                for (day in 1..daysInMonth) {
                    val isSelected = selectedDay == day
                    val dayLabel = getDayOfWeekLabel(day)

                    // Find events on this day to draw indicator dots
                    val monthStr = String.format("%02d", selectedMonth + 1)
                    val dayStr = String.format("%02d", day)
                    val dateKey = "$selectedYear-$monthStr-$dayStr"
                    val dayEvents = events.filter { it.date == dateKey }

                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .height(68.dp)
                            .clickable { selectedDay = day }
                            .testTag("village_portal_calendar_day_card_$day"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                if (isSystemInDarkTheme()) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFF8FAFC)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else {
                                if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dayLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                            )
                            Text(
                                text = day.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else (if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A))
                            )

                            // Multi-colored dots for event categories
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.height(4.dp)
                            ) {
                                val types = dayEvents.map { it.type }.distinct()
                                for (type in types) {
                                    val dotColor = when (type) {
                                        "MEETING" -> Color(0xFF3B82F6) // Blue
                                        "ACTIVITY" -> Color(0xFF10B981) // Green
                                        "DEADLINE" -> Color(0xFFEF4444) // Red
                                        else -> Color.Gray
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else dotColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryChip(
                    label = "Semua",
                    selected = activeCategoryFilter == "ALL",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { activeCategoryFilter = "ALL" },
                    modifier = Modifier.testTag("village_portal_calendar_type_filter_all")
                )
                CategoryChip(
                    label = "Rapat",
                    selected = activeCategoryFilter == "MEETING",
                    color = Color(0xFF3B82F6),
                    onClick = { activeCategoryFilter = "MEETING" },
                    modifier = Modifier.testTag("village_portal_calendar_type_filter_meeting")
                )
                CategoryChip(
                    label = "Kegiatan",
                    selected = activeCategoryFilter == "ACTIVITY",
                    color = Color(0xFF10B981),
                    onClick = { activeCategoryFilter = "ACTIVITY" },
                    modifier = Modifier.testTag("village_portal_calendar_type_filter_activity")
                )
                CategoryChip(
                    label = "Tenggat",
                    selected = activeCategoryFilter == "DEADLINE",
                    color = Color(0xFFEF4444),
                    onClick = { activeCategoryFilter = "DEADLINE" },
                    modifier = Modifier.testTag("village_portal_calendar_type_filter_deadline")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Date / Period Indicator
            Text(
                text = if (selectedDay == null) "Menampilkan Semua Agenda" else "Agenda Tanggal $selectedDay ${months[selectedMonth]} $selectedYear",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF475569)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Events List Section
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No Events",
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak Ada Agenda",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tidak ada rapat, kegiatan, atau tenggat terjadwal.",
                            fontSize = 10.sp,
                            color = Color.Gray.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (event in filteredEvents) {
                        EventRowCard(
                            event = event,
                            isAdmin = selectedRole == "Admin" || selectedRole == "Kades" || selectedRole == "Sekdes",
                            onClick = { selectedEventForDetail = event },
                            onDelete = {
                                viewModel.deleteVillageEvent(event.id)
                                Toast.makeText(context, "Agenda dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Modal Dialog
    selectedEventForDetail?.let { event ->
        Dialog(onDismissRequest = { selectedEventForDetail = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        val badgeColor = when (event.type) {
                            "MEETING" -> Color(0xFF3B82F6)
                            "ACTIVITY" -> Color(0xFF10B981)
                            "DEADLINE" -> Color(0xFFEF4444)
                            else -> Color.Gray
                        }
                        val badgeLabel = when (event.type) {
                            "MEETING" -> "RAPAT DESA"
                            "ACTIVITY" -> "KEGIATAN WARGA"
                            "DEADLINE" -> "TENGGAT PENTING"
                            else -> "AGENDA"
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            contentColor = badgeColor
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = { selectedEventForDetail = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Divider(color = if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location / Time Metadata
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Tanggal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tanggal: ${event.date}",
                                fontSize = 12.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Waktu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pukul: ${event.time} WIB",
                                fontSize = 12.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Lokasi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tempat: ${event.location}",
                                fontSize = 12.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Penyelenggara",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Penyelenggara: ${event.organizer}",
                                fontSize = 12.sp,
                                color = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Deskripsi Agenda:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { selectedEventForDetail = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tutup Agenda", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showAddDialog) {
        var addTitle by remember { mutableStateOf("") }
        var addDesc by remember { mutableStateOf("") }
        var addDate by remember { mutableStateOf(formattedSelectedDateString.ifEmpty { "2026-06-24" }) }
        var addTime by remember { mutableStateOf("09:00") }
        var addLocation by remember { mutableStateOf("Balai Desa Sumber Rejo") }
        var addType by remember { mutableStateOf("MEETING") } // MEETING, ACTIVITY, DEADLINE
        var addOrganizer by remember { mutableStateOf("Pemerintah Desa") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "TAMBAH KEGIATAN DESA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = addTitle,
                        onValueChange = { addTitle = it },
                        label = { Text("Nama Kegiatan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_event_title"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addDesc,
                        onValueChange = { addDesc = it },
                        label = { Text("Deskripsi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_event_desc"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = addDate,
                            onValueChange = { addDate = it },
                            label = { Text("Tanggal (YYYY-MM-DD)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_event_date"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = addTime,
                            onValueChange = { addTime = it },
                            label = { Text("Waktu (HH:MM)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_event_time"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addLocation,
                        onValueChange = { addLocation = it },
                        label = { Text("Lokasi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_event_location"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addOrganizer,
                        onValueChange = { addOrganizer = it },
                        label = { Text("Penyelenggara") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_event_organizer"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tipe Agenda",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TypeSelectButton("Rapat", addType == "MEETING", Color(0xFF3B82F6)) { addType = "MEETING" }
                        TypeSelectButton("Warga", addType == "ACTIVITY", Color(0xFF10B981)) { addType = "ACTIVITY" }
                        TypeSelectButton("Tenggat", addType == "DEADLINE", Color(0xFFEF4444)) { addType = "DEADLINE" }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                if (addTitle.isNotBlank() && addDate.isNotBlank()) {
                                    viewModel.addVillageEvent(
                                        title = addTitle,
                                        description = addDesc,
                                        date = addDate,
                                        time = addTime,
                                        location = addLocation,
                                        type = addType,
                                        organizer = addOrganizer
                                    )
                                    Toast.makeText(context, "Agenda Berhasil Ditambahkan!", Toast.LENGTH_SHORT).show()
                                    showAddDialog = false
                                } else {
                                    Toast.makeText(context, "Judul dan Tanggal wajib diisi!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("village_portal_calendar_save_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) color else color.copy(alpha = 0.08f),
        contentColor = if (selected) Color.White else color,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TypeSelectButton(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(34.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) color else color.copy(alpha = 0.1f),
        contentColor = if (selected) Color.White else color,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else color.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EventRowCard(
    event: VillageEvent,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColor = when (event.type) {
        "MEETING" -> Color(0xFF3B82F6) // Blue
        "ACTIVITY" -> Color(0xFF10B981) // Green
        "DEADLINE" -> Color(0xFFEF4444) // Red
        else -> Color.Gray
    }

    val typeLabel = when (event.type) {
        "MEETING" -> "Rapat"
        "ACTIVITY" -> "Kegiatan"
        "DEADLINE" -> "Tenggat"
        else -> "Agenda"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("village_portal_calendar_event_card_${event.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF334155).copy(alpha = 0.25f) else Color(0xFFF8FAFC)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Indicator Block
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(themeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (event.type) {
                        "MEETING" -> Icons.Default.Groups
                        "ACTIVITY" -> Icons.Default.Celebration
                        "DEADLINE" -> Icons.Default.PriorityHigh
                        else -> Icons.Default.EventNote
                    },
                    contentDescription = typeLabel,
                    tint = themeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Metadata
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = themeColor.copy(alpha = 0.15f),
                        contentColor = themeColor
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = event.time,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1E293B)
                )

                Text(
                    text = event.location,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isAdmin) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("village_portal_calendar_delete_button_${event.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Agenda",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Lihat Detail",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
