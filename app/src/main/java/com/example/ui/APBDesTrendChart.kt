package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold
import com.example.ui.theme.GovNavy
import java.text.DecimalFormat

data class APBDesMonthData(
    val monthCode: String,
    val monthName: String,
    val income: Double,       // in IDR
    val expenditure: Double,  // in IDR
    val description: String = ""
)

enum class ChartDisplayType {
    COMBO_AREA, BAR, LINE
}

@Composable
fun APBDesTrendChartComponent(
    formatter: DecimalFormat,
    isDark: Boolean = isSystemInDarkTheme()
) {
    // 2026 Fiscal Year realization data up to June
    val fiscalYearData = remember {
        listOf(
            APBDesMonthData("Jan", "Januari", 220000000.0, 130000000.0, "Penerimaan triwulan pertama & Operasional rutin awal tahun"),
            APBDesMonthData("Feb", "Februari", 195000000.0, 150000000.0, "Penyaluran dana desa tambahan & belanja infrastruktur RT"),
            APBDesMonthData("Mar", "Maret", 280000000.0, 195000000.0, "Realisasi padat karya tunai pertanian & irigasi dusun"),
            APBDesMonthData("Apr", "April", 240000000.0, 260000000.0, "Pemberdayaan UMKM & belanja sarana kesehatan Posyandu"),
            APBDesMonthData("May", "Mei", 265000000.0, 180000000.0, "Penyaluran BLT Dana Desa & pelatihan keterampilan kerja"),
            APBDesMonthData("Jun", "Juni", 175000000.0, 110000000.0, "Penyusunan laporan tengah semester & belanja rutin")
        )
    }

    var chartType by remember { mutableStateOf(ChartDisplayType.COMBO_AREA) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var minAmountFilter by remember { mutableStateOf(0f) } // slider selector for min income

    // Filtered data
    val filteredData = remember(fiscalYearData, minAmountFilter) {
        fiscalYearData.filter { it.income >= minAmountFilter }
    }

    // Totals
    val totalIncome = remember(filteredData) { filteredData.sumOf { it.income } }
    val totalExpenditure = remember(filteredData) { filteredData.sumOf { it.expenditure } }
    val surplusDeficit = totalIncome - totalExpenditure

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apbdes_chart_card"),
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
            // Title & Configuration Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik Realisasi Bulanan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "Komparasi Pendapatan vs Belanja (FY 2026)",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }

                // Interactive Chart Type Toggle (Mimics Recharts types)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .padding(2.dp)
                ) {
                    val types = listOf(
                        Triple(ChartDisplayType.COMBO_AREA, Icons.Default.AreaChart, "Area"),
                        Triple(ChartDisplayType.BAR, Icons.Default.BarChart, "Bar"),
                        Triple(ChartDisplayType.LINE, Icons.Default.ShowChart, "Line")
                    )
                    types.forEach { (type, icon, label) ->
                        val isSelected = chartType == type
                        IconButton(
                            onClick = {
                                chartType = type
                                selectedMonthIndex = null
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) (if (isDark) Color(0xFF334155) else Color.White) else Color.Transparent)
                                .testTag("btn_chart_type_${label.lowercase()}")
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Pilih $label",
                                tint = if (isSelected) (if (isDark) Color(0xFF60A5FA) else EmeraldGreen) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics Info Ribbons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Income Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF))
                        .border(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isDark) Color(0xFF60A5FA) else EmeraldGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PENSUPPORT (PDPT)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rp " + formatter.format(totalIncome),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                        )
                    }
                }

                // Total Expenditure Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFF7ED))
                        .border(1.dp, if (isDark) Color(0xFF7C2D12) else Color(0xFFFFEDD5), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GovGold))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PENGELUARAN (STAF)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rp " + formatter.format(totalExpenditure),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GovGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Rendered Chart Area Box
            if (filteredData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Oops! Seluruh data tersembunyi filter minimum.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    // Render the Chart
                    InfiniteRechartsStyleCanvas(
                        data = filteredData,
                        chartDisplayType = chartType,
                        selectedIndex = selectedMonthIndex,
                        onSelectedIndexChanged = { selectedMonthIndex = it },
                        isDark = isDark
                    )
                }
            }

            // Legend indicators & Quick Interactive Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(if (isDark) Color(0xFF60A5FA) else EmeraldGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Realisasi Pendapatan", fontSize = 10.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GovGold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Realisasi Belanja", fontSize = 10.sp, color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic interactive details card (Tooltip equivalent when clicking elements, just like Recharts)
            AnimatedVisibility(
                visible = selectedMonthIndex != null && selectedMonthIndex!! in filteredData.indices,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val index = selectedMonthIndex
                if (index != null && index in filteredData.indices) {
                    val clicked = filteredData[index]
                    val monthSavings = clicked.income - clicked.expenditure
                    val efficiency = ((monthSavings / clicked.income) * 100).toInt()

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Detail ${clicked.monthName} 2026",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF1E293B)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedMonthIndex = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pendapatan:", fontSize = 10.sp, color = Color.Gray)
                                    Text("Rp " + formatter.format(clicked.income), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pembelanjaan:", fontSize = 10.sp, color = Color.Gray)
                                    Text("Rp " + formatter.format(clicked.expenditure), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GovGold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Sisa Kas (Surplus):", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = "Rp " + formatter.format(monthSavings),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (monthSavings >= 0) GovNavy else Color.Red
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "*Keterangan: ${clicked.description}",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls & Threshold selectors
            Text(
                text = "Saring Nilai Pendapatan Minimal: Rp ${formatter.format(minAmountFilter)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Slider(
                value = minAmountFilter,
                onValueChange = {
                    minAmountFilter = it
                    selectedMonthIndex = null
                },
                valueRange = 0f..300000000f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                    activeTrackColor = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                    inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.testTag("chart_min_amount_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Monthly breakdown datatable module (Under the trend graph)
            Text(
                text = "Tabel Rincian Bulanan (Real-Time)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BULAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("PENDAPATAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    Text("BELANJA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    Text("SURPLUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                }

                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))

                filteredData.forEachIndexed { i, item ->
                    val isSelectedRow = i == selectedMonthIndex
                    val surplus = item.income - item.expenditure
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelectedRow) {
                                    if (isDark) Color(0xFF334155).copy(alpha = 0.4f) else Color(0xFFEFF6FF)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                selectedMonthIndex = if (isSelectedRow) null else i
                            }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.monthName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Rp " + formatter.format(item.income),
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "Rp " + formatter.format(item.expenditure),
                            fontSize = 11.sp,
                            color = GovGold,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "Rp " + formatter.format(surplus),
                            fontSize = 11.sp,
                            color = if (surplus >= 0) GovNavy else Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.End
                        )
                    }
                    if (i < filteredData.lastIndex) {
                        Divider(color = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFEDF2F7))
                    }
                }
            }
        }
    }
}

@Composable
fun InfiniteRechartsStyleCanvas(
    data: List<APBDesMonthData>,
    chartDisplayType: ChartDisplayType,
    selectedIndex: Int?,
    onSelectedIndexChanged: (Int?) -> Unit,
    isDark: Boolean
) {
    val highestValue = remember(data) {
        val maxIncome = data.maxOfOrNull { it.income } ?: 1.0
        val maxExpenditure = data.maxOfOrNull { it.expenditure } ?: 1.0
        maxOf(maxIncome, maxExpenditure) * 1.15
    }

    // Grid details
    val horizontalGridLines = 4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures(
                    onTap = { offset ->
                        val widthAvailable = size.width
                        val itemWidth = widthAvailable / data.size
                        val clickedIndex = (offset.x / itemWidth).toInt()
                        if (clickedIndex in data.indices) {
                            onSelectedIndexChanged(if (selectedIndex == clickedIndex) null else clickedIndex)
                        } else {
                            onSelectedIndexChanged(null)
                        }
                    }
                )
            }
            .pointerInput(data) {
                detectDragGestures(
                    onDragEnd = { /* Keep last touched selection */ },
                    onDragCancel = { },
                    onDrag = { change, _ ->
                        val widthAvailable = size.width
                        val itemWidth = widthAvailable / data.size
                        val draggedIndex = (change.position.x / itemWidth).toInt()
                        if (draggedIndex in data.indices) {
                            onSelectedIndexChanged(draggedIndex)
                        }
                    }
                )
            }
    ) {
        // Compose Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp, start = 32.dp, top = 10.dp, end = 10.dp)
        ) {
            val h = size.height
            val w = size.width

            // 1. Draw horizontal grid lines (Recharts style)
            for (step in 0..horizontalGridLines) {
                val gridY = h * (step.toFloat() / horizontalGridLines)
                drawLine(
                    color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    start = Offset(0f, gridY),
                    end = Offset(w, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Calculations per item
            val stepX = w / data.size
            val pointsIncome = ArrayList<Offset>()
            val pointsExpenditure = ArrayList<Offset>()

            data.forEachIndexed { idx, item ->
                val xPos = (stepX * idx) + (stepX / 2f)
                val incomeY = h - (h * (item.income / highestValue).toFloat())
                val expenditureY = h - (h * (item.expenditure / highestValue).toFloat())

                pointsIncome.add(Offset(xPos, incomeY))
                pointsExpenditure.add(Offset(xPos, expenditureY))
            }

            // Highlight selected background column (like Recharts Tooltip hover)
            if (selectedIndex != null && selectedIndex in data.indices) {
                val left = stepX * selectedIndex
                drawRoundRect(
                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                    topLeft = Offset(left, 0f),
                    size = Size(stepX, h),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // 2. Draw based on display modes
            when (chartDisplayType) {
                ChartDisplayType.BAR -> {
                    val barWidth = (stepX * 0.35f)
                    val spacing = 4.dp.toPx()

                    data.forEachIndexed { idx, _ ->
                        val center = (stepX * idx) + (stepX / 2f)

                        // Income Bar
                        val incY = pointsIncome[idx].y
                        val incLeft = center - barWidth - spacing
                        drawRoundRect(
                            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            topLeft = Offset(incLeft, incY),
                            size = Size(barWidth, h - incY),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Expenditure Bar
                        val expY = pointsExpenditure[idx].y
                        val expLeft = center + spacing
                        drawRoundRect(
                            color = GovGold,
                            topLeft = Offset(expLeft, expY),
                            size = Size(barWidth, h - expY),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                ChartDisplayType.LINE -> {
                    // Draw Line Income
                    val incomePath = Path()
                    incomePath.moveTo(pointsIncome[0].x, pointsIncome[0].y)
                    for (i in 1 until pointsIncome.size) {
                        incomePath.lineTo(pointsIncome[i].x, pointsIncome[i].y)
                    }
                    drawPath(
                        path = incomePath,
                        color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Line Expenditure
                    val expPath = Path()
                    expPath.moveTo(pointsExpenditure[0].x, pointsExpenditure[0].y)
                    for (i in 1 until pointsExpenditure.size) {
                        expPath.lineTo(pointsExpenditure[i].x, pointsExpenditure[i].y)
                    }
                    drawPath(
                        path = expPath,
                        color = GovGold,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw dots on lines
                    data.forEachIndexed { i, _ ->
                        drawCircle(
                            color = if (isDark) Color(0xFF1E293B) else Color.White,
                            radius = 6.dp.toPx(),
                            center = pointsIncome[i]
                        )
                        drawCircle(
                            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            radius = 4.dp.toPx(),
                            center = pointsIncome[i]
                        )

                        drawCircle(
                            color = if (isDark) Color(0xFF1E293B) else Color.White,
                            radius = 6.dp.toPx(),
                            center = pointsExpenditure[i]
                        )
                        drawCircle(
                            color = GovGold,
                            radius = 4.dp.toPx(),
                            center = pointsExpenditure[i]
                        )
                    }
                }

                ChartDisplayType.COMBO_AREA -> {
                    // Area under income
                    val areaIncomePath = Path().apply {
                        moveTo(pointsIncome[0].x, h)
                        for (p in pointsIncome) {
                            lineTo(p.x, p.y)
                        }
                        lineTo(pointsIncome.last().x, h)
                        close()
                    }
                    drawPath(
                        path = areaIncomePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                (if (isDark) Color(0xFF60A5FA) else EmeraldGreen).copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw line income
                    val lineIncPath = Path().apply {
                        moveTo(pointsIncome[0].x, pointsIncome[0].y)
                        for (i in 1 until pointsIncome.size) {
                            lineTo(pointsIncome[i].x, pointsIncome[i].y)
                        }
                    }
                    drawPath(
                        path = lineIncPath,
                        color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw bars for expenditure
                    val expBarWidth = (stepX * 0.25f)
                    data.forEachIndexed { idx, _ ->
                        val center = (stepX * idx) + (stepX / 2f)
                        val expY = pointsExpenditure[idx].y
                        drawRoundRect(
                            color = GovGold.copy(alpha = 0.85f),
                            topLeft = Offset(center - (expBarWidth / 2f), expY),
                            size = Size(expBarWidth, h - expY),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }

                    // Circle marker for selected combo item
                    if (selectedIndex != null && selectedIndex in data.indices) {
                        drawCircle(
                            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            radius = 7.dp.toPx(),
                            center = pointsIncome[selectedIndex]
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = pointsIncome[selectedIndex]
                        )
                    }
                }
            }
        }

        // Side Y-Axis Static text Labels overlay list
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
                .padding(bottom = 24.dp, top = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val stepsList = listOf("300J", "225J", "150J", "75J", "0")
            stepsList.forEach { label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(28.dp)
                )
            }
        }

        // Bottom X-Axis labels Row Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 32.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIndex
                Text(
                    text = item.monthCode,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) (if (isDark) Color(0xFF60A5FA) else EmeraldGreen) else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
