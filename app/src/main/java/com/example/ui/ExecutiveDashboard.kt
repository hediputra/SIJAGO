package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.DecimalFormat

@Composable
fun ExecutiveAdminDashboard(
    viewModel: SijagoViewModel,
    citizenCount: Int,
    kkCount: Int,
    maleCount: Int,
    femaleCount: Int,
    poorCount: Int,
    pendingLettersCount: Int,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val letters by viewModel.filteredLetters.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val formatter = remember { DecimalFormat("#,###") }

    // Aggregate additional stats dynamically
    val totalLetters = letters.size
    val completedLetters = letters.count { it.status == "SELESAI" }
    val inProgressLetters = letters.count { it.status == "DIPROSES" }
    val newLetters = letters.count { it.status == "MASUK" }

    // Colors
    val isDark = isSystemInDarkTheme()
    val emeraldGreen = Color(0xFF2E7D32)
    val royalBlue = Color(0xFF1D4ED8)
    val amberOrange = Color(0xFFD97706)
    val darkBg = Color(0xFF0F172A)
    
    val baseCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val borderCol = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("executive_dashboard_container")
    ) {
        // Dashboard Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Icon",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Dashboard Eksekutif SID",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ringkasan Kinerja & Data Real-Time Desa",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Quick live status badge
            Surface(
                color = if (isOnline) emeraldGreen.copy(alpha = 0.15f) else amberOrange.copy(alpha = 0.15f),
                contentColor = if (isOnline) emeraldGreen else amberOrange,
                shape = CircleShape,
                modifier = Modifier.testTag("dashboard_live_indicator")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) emeraldGreen else amberOrange)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOnline) "LIVE (ONLINE)" else "OFFLINE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Widget 1: Kependudukan & Demografis
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("resident_stats_card"),
            colors = CardDefaults.cardColors(containerColor = baseCardBg),
            border = BorderStroke(1.dp, borderCol),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Residents Overview",
                            tint = royalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IKHTISAR KEPENDUDUKAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = royalBlue,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Kelola Data",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = royalBlue,
                        modifier = Modifier
                            .clickable { onNavigateToTab("kependudukan") }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Penduduk",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatter.format(citizenCount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " Jiwa",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total KK",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatter.format(kkCount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " KK",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Prasejahtera",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            val poorPercent = if (citizenCount > 0) ((poorCount.toDouble() / citizenCount) * 100).toInt() else 0
                            Text(
                                text = "$poorPercent%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C)
                            )
                            Text(
                                text = " (DTKS)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gender Distribution Bar (Custom Mini Representation)
                val totalGender = (maleCount + femaleCount).toDouble()
                val maleRatio = if (totalGender > 0) maleCount.toFloat() / totalGender.toFloat() else 0.5f

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Laki-laki: $maleCount (${(maleRatio * 100).toInt()}%)",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Perempuan: $femaleCount (${((1f - maleRatio) * 100).toInt()}%)",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Dual-color Split Progress Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(maleRatio.coerceAtLeast(0.01f))
                                .background(Color(0xFF3B82F6))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight((1f - maleRatio).coerceAtLeast(0.01f))
                                .background(Color(0xFFEC4899))
                        )
                    }
                }
            }
        }

        // Widget 2: Service Requests (Aktivitas Surat Layanan)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("service_requests_stats_card"),
            colors = CardDefaults.cardColors(containerColor = baseCardBg),
            border = BorderStroke(1.dp, borderCol),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Requests Overview",
                            tint = amberOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AKTIVITAS LAYANAN SURAT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = amberOrange,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Kelola Layanan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = amberOrange,
                        modifier = Modifier
                            .clickable { onNavigateToTab("surat") }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pending Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (pendingLettersCount > 0) Color(0xFFFEE2E2) else Color(0xFFF1F8E9))
                            .border(
                                1.dp,
                                if (pendingLettersCount > 0) Color(0xFFFCA5A5) else Color(0xFFDCEDC8),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Menunggu Proses",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingLettersCount > 0) Color(0xFF991B1B) else Color(0xFF2E7D32)
                            )
                            Text(
                                text = "$pendingLettersCount Surat",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (pendingLettersCount > 0) Color(0xFFB91C1C) else Color(0xFF2E7D32)
                            )
                        }
                    }

                    // Approved / Total
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Tingkat Penyelesaian",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val solvedPercent = if (totalLetters > 0) ((completedLetters.toDouble() / totalLetters) * 100).toInt() else 100
                        Text(
                            text = "$solvedPercent%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = emeraldGreen
                        )
                        Text(
                            text = "$completedLetters dari $totalLetters Pengajuan Selesai",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom mini progression layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Masuk status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9F1C))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$newLetters Masuk",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Diproses status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(royalBlue)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$inProgressLetters Diproses",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Selesai status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(emeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$completedLetters Selesai",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Widget 3: Realisasi & Anggaran Keuangan APBDes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .testTag("financial_stats_card"),
            colors = CardDefaults.cardColors(containerColor = baseCardBg),
            border = BorderStroke(1.dp, borderCol),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Finance Overview",
                            tint = emeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REALISASI APBDES 2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = emeraldGreen,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Rincian Dana",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = emeraldGreen,
                        modifier = Modifier
                            .clickable { onNavigateToTab("keuangan") }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Finance Highlight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) darkBg else Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TOTAL ANGGARAN APBDesa",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rp 1.450.000.000",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4ADE80))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Realisasi Pendapatan",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                            Text(
                                text = "Rp 1.320.000.000",
                                color = Color(0xFF4ADE80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFB923C))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Realisasi Belanja",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                            Text(
                                text = "Rp 985.000.000",
                                color = Color(0xFFFB923C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remaining / Surplus representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SISA / SURPLUS ANGGARAN",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Rp 335.000.000",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF60A5FA)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Budget Absorption progression visual widget
                Column {
                    val budgetUsedPercent = (985000000.0 / 1450000000.0 * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Abshorbsi Belanja Desa",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$budgetUsedPercent% Belanja Terealisasi",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = emeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { 0.679f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = emeraldGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
