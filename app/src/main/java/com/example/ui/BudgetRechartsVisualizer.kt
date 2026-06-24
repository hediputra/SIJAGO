package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold
import com.example.ui.theme.GovNavy
import java.text.DecimalFormat
import kotlin.math.sqrt

data class BudgetSector(
    val id: String,
    val name: String,
    val allocated: Double,
    val realized: Double,
    val color: Color,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRechartsVisualizer(
    formatter: DecimalFormat,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val budgetSectors = remember {
        listOf(
            BudgetSector(
                id = "infra",
                name = "Infrastruktur & Irigasi",
                allocated = 652500000.0,
                realized = 410000000.0,
                color = Color(0xFF1D4ED8), // Royal Blue
                description = "Pembangunan jalan tani, jembatan penghubung dusun, dan perbaikan saluran irigasi pertanian."
            ),
            BudgetSector(
                id = "pemberdayaan",
                name = "Pemberdayaan Masyarakat",
                allocated = 362500000.0,
                realized = 220000000.0,
                color = Color(0xFF059669), // Emerald
                description = "Pelatihan keterampilan UMKM, pemberian bibit unggul tani, dan bantuan permodalan BUMDes."
            ),
            BudgetSector(
                id = "pembinaan",
                name = "Pembinaan Kemasyarakatan",
                allocated = 217500000.0,
                realized = 145000000.0,
                color = Color(0xFFD97706), // Amber
                description = "Fasilitasi kegiatan Posyandu, pembinaan karang taruna, dan pencegahan stunting balita."
            ),
            BudgetSector(
                id = "pemdes",
                name = "Operasional Kantor Pemdes",
                allocated = 217500000.0,
                realized = 210000000.0,
                color = Color(0xFFBE123C), // Crimson
                description = "Biaya administrasi kantor desa, tunjangan perangkat desa, serta koordinasi operasional."
            )
        )
    }

    val totalAllocated = remember(budgetSectors) { budgetSectors.sumOf { it.allocated } }
    val totalRealized = remember(budgetSectors) { budgetSectors.sumOf { it.realized } }
    val totalRealizationPercent = (totalRealized / totalAllocated) * 100

    // Tab Selection: "allocation" (Donut Chart) or "realization" (Double Bar Chart)
    var selectedVisualizationTab by remember { mutableStateOf("allocation") }
    var selectedSectorIndex by remember { mutableStateOf<Int?>(null) }

    // Trigger state for entrance animation
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_visualizer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header transparency & subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Visualisasi Transparansi Keuangan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "Plotting digital alokasi anggaran belanja & realisasi APBDes 2026",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF))
                        .border(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Real-Time",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab toggler: Alokasi Anggaran vs Realisasi Belanja
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = {
                        selectedVisualizationTab = "allocation"
                        selectedSectorIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedVisualizationTab == "allocation") {
                            if (isDark) Color(0xFF334155) else Color.White
                        } else Color.Transparent,
                        contentColor = if (selectedVisualizationTab == "allocation") {
                            if (isDark) Color.White else Color(0xFF0F172A)
                        } else Color.Gray
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (selectedVisualizationTab == "allocation") 2.dp else 0.dp
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_budget_allocation")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Alokasi (Donut)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        selectedVisualizationTab = "realization"
                        selectedSectorIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedVisualizationTab == "realization") {
                            if (isDark) Color(0xFF334155) else Color.White
                        } else Color.Transparent,
                        contentColor = if (selectedVisualizationTab == "realization") {
                            if (isDark) Color.White else Color(0xFF0F172A)
                        } else Color.Gray
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (selectedVisualizationTab == "realization") 2.dp else 0.dp
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_budget_realization")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rasio Realisasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Chart area
            Crossfade(
                targetState = selectedVisualizationTab,
                animationSpec = tween(durationMillis = 350)
            ) { tab ->
                when (tab) {
                    "allocation" -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChartCanvas(
                                    sectors = budgetSectors,
                                    totalAllocated = totalAllocated,
                                    selectedIndex = selectedSectorIndex,
                                    onIndexSelected = { selectedSectorIndex = it },
                                    isDark = isDark,
                                    animationPlayed = animationPlayed
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Color Legends list
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                budgetSectors.forEachIndexed { index, sector ->
                                    val isSelected = selectedSectorIndex == index
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) {
                                                    sector.color.copy(alpha = 0.15f)
                                                } else Color.Transparent
                                            )
                                            .clickable {
                                                selectedSectorIndex = if (isSelected) null else index
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(sector.color)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = sector.id.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) sector.color else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "realization" -> {
                        BudgetRealizationComparisonBars(
                            sectors = budgetSectors,
                            formatter = formatter,
                            isDark = isDark,
                            animationPlayed = animationPlayed,
                            selectedIdx = selectedSectorIndex,
                            onIdxSelected = { selectedSectorIndex = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Interactive Tooltip/Explanation Box (Mimicking Recharts dynamic details card)
            AnimatedContent(
                targetState = selectedSectorIndex,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                }
            ) { index ->
                if (index != null && index in budgetSectors.indices) {
                    val clicked = budgetSectors[index]
                    val percentAlloc = (clicked.allocated / totalAllocated) * 100
                    val percentUsed = (clicked.realized / clicked.allocated) * 100
                    val remaining = clicked.allocated - clicked.realized

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_tooltip_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(clicked.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = clicked.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedSectorIndex = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup rincian",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Alokasi Pagu:", fontSize = 9.sp, color = Color.Gray)
                                    Text(
                                        text = "Rp " + formatter.format(clicked.allocated),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", percentAlloc)}% dari total APBDes",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Realisasi Belanja:", fontSize = 9.sp, color = Color.Gray)
                                    Text(
                                        text = "Rp " + formatter.format(clicked.realized),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = clicked.color
                                    )
                                    Text(
                                        text = "${String.format("%.1f", percentUsed)}% terserap",
                                        fontSize = 9.sp,
                                        color = clicked.color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Sisa Anggaran:", fontSize = 9.sp, color = Color.Gray)
                                    Text(
                                        text = "Rp " + formatter.format(remaining),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remaining > 50000000) EmeraldGreen else GovGold
                                    )
                                    Text(
                                        text = "Tersedia",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Divider(color = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFEDF2F7))

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = clicked.color,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(top = 1.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = clicked.description,
                                    fontSize = 10.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Default overall transparency description summary card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Default tap will select infrastructure sector
                                selectedSectorIndex = 0
                            }
                            .testTag("budget_transparency_summary")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Sertifikat Transparansi",
                                    tint = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Komitmen Transparansi APBDesa",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total realisasi penggunaan anggaran mencapai Rp ${formatter.format(totalRealized)} dari Rp ${formatter.format(totalAllocated)} (${String.format("%.1f", totalRealizationPercent)}%). Sentuh grafik sektor belanja di atas untuk menilik rincian transparansi penuh.",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChartCanvas(
    sectors: List<BudgetSector>,
    totalAllocated: Double,
    selectedIndex: Int?,
    onIndexSelected: (Int?) -> Unit,
    isDark: Boolean,
    animationPlayed: Boolean
) {
    val totalAngle = 360f

    // Animated Sweep Progress
    val sweepProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 34.dp.toPx() }

    BoxWithConstraints(
        modifier = Modifier
            .size(180.dp)
            .pointerInput(sectors) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val distance = sqrt(dx * dx + dy * dy)

                    val outerRadius = centerX - strokeWidthPx / 4
                    val innerRadius = outerRadius - strokeWidthPx

                    if (distance in innerRadius..outerRadius) {
                        var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                        if (angle < 0) angle += 360.0

                        // Match against starting/ending angles of sectors
                        var cumulativeAngle = 0.0
                        var clickedIdx: Int? = null

                        for (i in sectors.indices) {
                            val percent = (sectors[i].allocated / totalAllocated)
                            val sectorSweep = percent * 360.0
                            if (angle >= cumulativeAngle && angle < (cumulativeAngle + sectorSweep)) {
                                clickedIdx = i
                                break
                            }
                            cumulativeAngle += sectorSweep
                        }

                        if (clickedIdx != null) {
                            onIndexSelected(if (selectedIndex == clickedIdx) null else clickedIdx)
                        }
                    } else {
                        onIndexSelected(null)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val center = Offset(canvasW / 2f, canvasH / 2f)
            val radius = (canvasW / 2f) - (strokeWidthPx / 2f)

            var startAngle = 0f

            sectors.forEachIndexed { index, sector ->
                val sweepAngle = ((sector.allocated / totalAllocated).toFloat() * totalAngle) * sweepProgress
                val isSelected = index == selectedIndex

                // Add nice selection glow/outset effect
                val adjustedStrokeWidth = if (isSelected) strokeWidthPx * 1.25f else strokeWidthPx
                val adjustedColor = if (isSelected) sector.color else sector.color.copy(alpha = 0.85f)

                drawArc(
                    color = adjustedColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = adjustedStrokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }

            // Draw center metric labels (Interactive Recharts center text)
            // Draw clean subtle circles around inner/outer boundaries
            drawCircle(
                color = if (isDark) Color(0xFF334155).copy(alpha = 0.3f) else Color(0xFFE2E8F0).copy(alpha = 0.3f),
                radius = radius - (strokeWidthPx / 2f),
                style = Stroke(width = 2f)
            )
        }

        // Overlay center texts
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selectedIndex != null && selectedIndex in sectors.indices) {
                val sec = sectors[selectedIndex]
                Text(
                    text = sec.id.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = sec.color
                )
                Text(
                    text = "${String.format("%.1f", (sec.allocated / totalAllocated) * 100)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "PAGU BELANJA",
                    fontSize = 7.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "TOTAL APBD",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "Rp 1.45M",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Pagu Belanja",
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BudgetRealizationComparisonBars(
    sectors: List<BudgetSector>,
    formatter: DecimalFormat,
    isDark: Boolean,
    animationPlayed: Boolean,
    selectedIdx: Int?,
    onIdxSelected: (Int?) -> Unit
) {
    val progressAnim by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        sectors.forEachIndexed { index, sector ->
            val isSelected = index == selectedIdx
            val remaining = sector.allocated - sector.realized
            val useRatio = (sector.realized / sector.allocated).toFloat()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                        } else Color.Transparent
                    )
                    .border(
                        1.dp,
                        if (isSelected) sector.color.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onIdxSelected(if (isSelected) null else index)
                    }
                    .padding(8.dp)
            ) {
                // Info header row for this sector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sector.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(sector.color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", useRatio * 100)}% Terealisasi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = sector.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dual progress meters representing allocation and realization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                ) {
                    // Pagu Alokasi (Base Background Indicator - 100%)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f * progressAnim)
                            .fillMaxHeight()
                            .background(sector.color.copy(alpha = 0.15f))
                    )

                    // Realisasi Belanja (Overlay Progress bar)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(useRatio * progressAnim)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        sector.color.copy(alpha = 0.75f),
                                        sector.color
                                    )
                                )
                            )
                    )

                    // Overlay percentage text
                    Text(
                        text = "Rp " + formatter.format(sector.realized) + " dari Rp " + formatter.format(sector.allocated),
                        color = if (useRatio > 0.65f) Color.White else (if (isDark) Color.White else Color.Black),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Belanja Terlaksana: Rp ${formatter.format(sector.realized)}",
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Sisa Anggaran: Rp ${formatter.format(remaining)}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining > 50000000) EmeraldGreen else GovGold
                    )
                }
            }
        }
    }
}
