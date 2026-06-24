package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldGreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

data class GisMarker(
    val id: String,
    val title: String,
    val category: String, // "landmark", "infrastructure", "facility"
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val detailValue: String = "",
    val extraInfo: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GisMapScreen(role: String = "Masyarakat") {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Coordinates bounding box for Sumber Rejo village center
    val initialLat = -7.4532
    val initialLng = 110.3661

    // Initial Landmarks, Infrastructure, and Facilities datasets
    var markersList by remember {
        mutableStateOf(
            listOf(
                GisMarker(
                    id = "lm_balai",
                    title = "Balai Desa Sumber Rejo",
                    category = "landmark",
                    latitude = -7.4532,
                    longitude = 110.3661,
                    description = "Kantor pusat pelayanan administrasi desa Sumber Rejo, tempat koordinasi utama aparatur sipil desa.",
                    detailValue = "Layanan: Senin - Jumat (08.00 - 15.30 WIB)",
                    extraInfo = "Kepala Desa: Bp. H. Setyawan Prasetyo"
                ),
                GisMarker(
                    id = "fc_sd1",
                    title = "SD Negeri 1 Sumber Rejo",
                    category = "facility",
                    latitude = -7.4528,
                    longitude = 110.3642,
                    description = "Institusi pendidikan dasar tertua di desa yang dilengkapi fasilitas laboratorium komputer dan lapangan serbaguna.",
                    detailValue = "Akreditasi: A (Sangat Baik)",
                    extraInfo = "Kapasitas: 240 Siswa & 16 Guru"
                ),
                GisMarker(
                    id = "fc_posyandu",
                    title = "Posyandu Bougenville I",
                    category = "facility",
                    latitude = -7.4545,
                    longitude = 110.3650,
                    description = "Pos pelayanan kesehatan terpadu tingkat dukun, melayani pemeriksaan bulanan balita, ibu hamil, serta lansia.",
                    detailValue = "Buka: Pekan Ke-2 Hari Selasa",
                    extraInfo = "Kader Aktif: 6 Tenaga Kesehatan"
                ),
                GisMarker(
                    id = "fc_masjid",
                    title = "Masjid Jami' Al-Barokah",
                    category = "facility",
                    latitude = -7.4515,
                    longitude = 110.3685,
                    description = "Masjid jam'i terbesar yang menjadi pusat kegiatan spiritual, perayaan hari besar Islam, serta kajian remaja desa.",
                    detailValue = "Kapasitas: 500 Jamaah",
                    extraInfo = "Ketua Takmir: Ust. KH. Ahmad Fauzi"
                ),
                GisMarker(
                    id = "inf_irigasi",
                    title = "Irigasi Blok Sawah Kidul",
                    category = "infrastructure",
                    latitude = -7.4495,
                    longitude = 110.3612,
                    description = "Proyek saluran air sekunder pertanian beton untuk mengaliri sekitar 42 hektar area persawahan padi warga.",
                    detailValue = "Fisik: 85% Terealisasi",
                    extraInfo = "Dana: APBDesa Rp 120.000.000"
                ),
                GisMarker(
                    id = "inf_jalan",
                    title = "Pengaspalan Karangmulyo",
                    category = "infrastructure",
                    latitude = -7.4582,
                    longitude = 110.3698,
                    description = "Pekerjaan hotmix jalan penghubung utama antar dusun sepanjang 1.2 KM guna mempermudah logistik tani.",
                    detailValue = "Fisik: 100% Selesai (Sempurna)",
                    extraInfo = "Dana: Bantuan Provinsi Rp 245.000.000"
                ),
                GisMarker(
                    id = "inf_jembatan",
                    title = "Renovasi Jembatan Kali Elo",
                    category = "infrastructure",
                    latitude = -7.4552,
                    longitude = 110.3725,
                    description = "Pekerjaan perkuatan abutment penahan erosi air serta pengadaan pagar baja di sisi kanan-kiri jembatan perbatasan.",
                    detailValue = "Fisik: 40% Dalam Pengerjaan",
                    extraInfo = "Dana: Swadaya & APBD Rp 180.000.000"
                ),
                GisMarker(
                    id = "lm_batik",
                    title = "Sentra Batik Kencana-UMKM",
                    category = "landmark",
                    latitude = -7.4570,
                    longitude = 110.3675,
                    description = "Kawasan pameran industri kreatif lokal batik tulis khas motif Rejo Agung yang jadi maskot kerajinan warga.",
                    detailValue = "Buka: 09:00 - 17:00 WIB",
                    extraInfo = "Pengelola: Koperasi Batik Mekar Jaya"
                )
            )
        )
    }

    // Filter and Search States
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMarker by remember { mutableStateOf<GisMarker?>(null) }
    var mapModeGoogle by remember { mutableStateOf(false) } // Default to visual interactive vector map for best offline-ready emulator resilience

    // Dialog for adding custom marker
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("landmark") }
    var newLatStr by remember { mutableStateOf(initialLat.toString()) }
    var newLngStr by remember { mutableStateOf(initialLng.toString()) }
    var newDesc by remember { mutableStateOf("") }
    var newDetail by remember { mutableStateOf("") }
    var newExtra by remember { mutableStateOf("") }

    // Map view setup for Google Maps
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 15.5f)
    }

    // Filter logic
    val filteredMarkers = markersList.filter { marker ->
        val matchesCategory = (selectedCategory == "all" || marker.category == selectedCategory)
        val matchesSearch = marker.title.contains(searchQuery, ignoreCase = true) || 
                            marker.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Vector Map States - Transform Gestures (Pinch to Zoom, Pan)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("gis_screen_root")
    ) {
        // Upper Title Section with Modern Ribbon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Modul GIS Sijago",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Geographic Information System Desa Sumber Rejo",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Add Point Button for Admin/Roles at Top
                if (role != "Masyarakat") {
                    FilledTonalButton(
                        onClick = {
                            newLatStr = String.format("%.5f", -7.450 + (0.01 * Math.random() - 0.005))
                            newLngStr = String.format("%.5f", 110.366 + (0.01 * Math.random() - 0.005))
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("gis_add_marker_button")
                    ) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = "Plot", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plot Titik", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar Row & Map Engine Switch (M3 Accent Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari landmark, fasilitas, proyek...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("gis_search_field"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Map Engine Toggle Switch Button Group
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { mapModeGoogle = false },
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!mapModeGoogle) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Offline Vector Canvas",
                                tint = if (!mapModeGoogle) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        VerticalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        IconButton(
                            onClick = { mapModeGoogle = true },
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mapModeGoogle) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .testTag("toggle_google_maps_view")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Google Maps Native View",
                                tint = if (mapModeGoogle) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Category Quick Filter Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterCategoryBadge(
                label = "Semua Titik",
                isSelected = selectedCategory == "all",
                icon = Icons.Default.AllInclusive,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = { selectedCategory = "all" }
            )

            FilterCategoryBadge(
                label = "Landmark Desa",
                isSelected = selectedCategory == "landmark",
                icon = Icons.Default.Storefront,
                activeColor = Color(0xFFD97706), // Amber M3
                onClick = { selectedCategory = "landmark" }
            )

            FilterCategoryBadge(
                label = "Pembangunan",
                isSelected = selectedCategory == "infrastructure",
                icon = Icons.Default.Construction,
                activeColor = Color(0xFF1D4ED8), // Royal Blue
                onClick = { selectedCategory = "infrastructure" }
            )

            FilterCategoryBadge(
                label = "Layanan Publik",
                isSelected = selectedCategory == "facility",
                icon = Icons.Default.LocalHospital,
                activeColor = Color(0xFF059669), // Emerald Green
                onClick = { selectedCategory = "facility" }
            )
        }

        // Map Viewer Box (Active viewport)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ), RoundedCornerShape(20.dp)
                )
                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFE0F2FE))
                .testTag("gis_map_viewport")
        ) {
            if (mapModeGoogle) {
                // REAL GOOGLE MAPS IMPLEMENTATION
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapType = if (isDark) MapType.SATELLITE else MapType.NORMAL,
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = true
                    )
                ) {
                    filteredMarkers.forEach { gMarker ->
                        val position = LatLng(gMarker.latitude, gMarker.longitude)
                        Marker(
                            state = rememberMarkerState(key = gMarker.id, position = position),
                            title = gMarker.title,
                            snippet = gMarker.detailValue,
                            onClick = {
                                selectedMarker = gMarker
                                true // consume event
                            }
                        )
                    }
                }
            } else {
                // INTERACTIVE RICH OFFLINE VECTOR MAP CONTAINER (Built-In Digital Canvas with Drag/Zoom)
                // Center calculations inside local frame
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                                offset = offset + pan
                            }
                        }
                        .pointerInput(filteredMarkers) {
                            detectTapGestures { tapOffset ->
                                // Tap outside details clears selection if in empty space
                                selectedMarker = null
                            }
                        }
                ) {
                    val mapWidth = constraints.maxWidth.toFloat()
                    val mapHeight = constraints.maxHeight.toFloat()

                    val densityVal = LocalDensity.current.density
                    val markerWidthPx = 16f * densityVal
                    val markerHeightPx = 32f * densityVal

                    // Visual Graphic Layer responding to scale/offsets
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    ) {
                        // Drawing base boundaries and grids via Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Render simple administrative coordinate frame maps
                            val path = Path().apply {
                                moveTo(mapWidth * 0.15f, mapHeight * 0.25f)
                                quadraticTo(mapWidth * 0.5f, mapHeight * 0.1f, mapWidth * 0.85f, mapHeight * 0.35f)
                                lineTo(mapWidth * 0.8f, mapHeight * 0.75f)
                                quadraticTo(mapWidth * 0.45f, mapHeight * 0.9f, mapWidth * 0.2f, mapHeight * 0.7f)
                                close()
                            }
                            // Base administrative district solid color
                            drawPath(
                                path = path,
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFDCFCE7)
                            )
                            // Outer ring line
                            drawPath(
                                path = path,
                                color = if (isDark) Color(0xFF334155) else EmeraldGreen,
                                style = Stroke(width = 4f)
                            )

                            // Main Roads Grid Representation
                            // Horizontal Expressway
                            drawLine(
                                color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                                start = Offset(0f, mapHeight * 0.52f),
                                end = Offset(mapWidth, mapHeight * 0.55f),
                                strokeWidth = 14f
                            )
                            // Secondary Cross Path
                            drawLine(
                                color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                                start = Offset(mapWidth * 0.42f, 0f),
                                end = Offset(mapWidth * 0.5f, mapHeight),
                                strokeWidth = 8f
                            )
                        }

                        // Coordinates mapping helper to translate Lat/Lng bounds to local coordinate spaces
                        // Bounds: Lat [-7.460 to -7.448], Lng [110.360 to 110.375]
                        filteredMarkers.forEach { item ->
                            val mercX = ((item.longitude - 110.360) / (110.375 - 110.360)).toFloat()
                            val mercY = ((item.latitude - (-7.460)) / ((-7.448) - (-7.460))).toFloat()

                            val finalX = (mercX * mapWidth).coerceIn(10f, mapWidth - 10f)
                            val finalY = ((1.0f - mercY) * mapHeight).coerceIn(10f, mapHeight - 10f) // Inverse Y due to screen space orientation

                            val pinColor = when (item.category) {
                                "landmark" -> Color(0xFFD97706) // Amber
                                "infrastructure" -> Color(0xFF1D4ED8) // Royal Blue
                                else -> Color(0xFF059669) // Emerald Green
                            }

                            // Dynamic Marker Pins overlayed over canvas coordinates
                            Box(
                                modifier = Modifier
                                    .absoluteOffset {
                                        IntOffset(
                                            (finalX - markerWidthPx).roundToInt(),
                                            (finalY - markerHeightPx).roundToInt()
                                        )
                                    }
                                    .size(32.dp)
                                    .clickable { selectedMarker = item }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = item.title,
                                        tint = pinColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    // Custom visual ping shadow
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp, 3.dp)
                                            .clip(CircleShape)
                                            .background(pinColor.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Floating Search Instructions Help Bubble
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (mapModeGoogle) Icons.Default.Layers else Icons.Default.TouchApp,
                        contentDescription = "Control Tip",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (mapModeGoogle) "Google Maps Aktif" else "Geser & Cubit Peta",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Expanded Bottom Sheet Info Layout (Details of Tapped/Selected Pin)
        AnimatedVisibility(
            visible = selectedMarker != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val details = selectedMarker
            if (details != null) {
                val accentColor = when (details.category) {
                    "landmark" -> Color(0xFFD97706)
                    "infrastructure" -> Color(0xFF1D4ED8)
                    else -> Color(0xFF059669)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .testTag("gis_marker_details_sheet"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (details.category) {
                                            "landmark" -> Icons.Default.Storefront
                                            "infrastructure" -> Icons.Default.Construction
                                            else -> Icons.Default.LocalHospital
                                        },
                                        contentDescription = "Category",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = details.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (details.category) {
                                            "landmark" -> "Landmark Wisata & Budaya"
                                            "infrastructure" -> "Pembangunan Sarana Desa"
                                            else -> "Layanan Publik & Sosial"
                                        },
                                        fontSize = 10.sp,
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedMarker = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Landmark Specifications (Coordinates & Real details)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "KOORDINAT INTEGRASI",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lat: ${details.latitude}, Lng: ${details.longitude}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "STATUS LAYANAN / FISIK",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = details.detailValue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description Content
                        Text(
                            text = details.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (details.extraInfo.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Extra Info",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = details.extraInfo,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Plot Custom Points (M3 Styled Dialog)
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("gis_add_marker_dialog"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = "Add Location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Plot Baru Koordinat GIS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Plotting marker geospasial real-time baru pada sistem pemetaan administrasi desil wilayah.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Title Input Box
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Nama Landmark / Bangunan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .testTag("gis_input_title"),
                        singleLine = true
                    )

                    // Category Selector TabRow
                    Text(
                        text = "Kategori Titik Wilayah",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("landmark" to "Landmark", "infrastructure" to "Proyek", "facility" to "Fasilitas").forEach { (id, label) ->
                            val active = newCategory == id
                            val col = when(id) {
                                "landmark" -> Color(0xFFD97706)
                                "infrastructure" -> Color(0xFF1D4ED8)
                                else -> Color(0xFF059669)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) col else col.copy(alpha = 0.1f))
                                    .border(1.dp, col, RoundedCornerShape(8.dp))
                                    .clickable { newCategory = id }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else col
                                )
                            }
                        }
                    }

                    // Latitude & Longitude inputs side by side
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newLatStr,
                            onValueChange = { newLatStr = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newLngStr,
                            onValueChange = { newLngStr = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Description Input Box
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Deskripsi Informatif") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        maxLines = 3
                    )

                    // Detail Value Input Box
                    OutlinedTextField(
                        value = newDetail,
                        onValueChange = { newDetail = it },
                        label = { Text("Detail Status / Jam Operasional") },
                        placeholder = { Text("Contoh: Progressive: 90% Selesai") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        singleLine = true
                    )

                    // Extra Metadata Info
                    OutlinedTextField(
                        value = newExtra,
                        onValueChange = { newExtra = it },
                        label = { Text("Meta / Sumber Anggaran") },
                        placeholder = { Text("Contoh: APBDesa Dana Desa") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val dLat = newLatStr.toDoubleOrNull()
                                val dLng = newLngStr.toDoubleOrNull()
                                if (newTitle.isBlank()) {
                                    Toast.makeText(context, "Nama bangunan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                } else if (dLat == null || dLng == null) {
                                    Toast.makeText(context, "Input koordinat latitude/longitude tidak valid!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val compiledKey = "custom_" + System.currentTimeMillis()
                                    markersList = markersList + GisMarker(
                                        id = compiledKey,
                                        title = newTitle,
                                        category = newCategory,
                                        latitude = dLat,
                                        longitude = dLng,
                                        description = newDesc.ifBlank { "Titik plotting geospasial custom untuk peta administrasi desa." },
                                        detailValue = newDetail.ifBlank { "Status: Diregistrasi" },
                                        extraInfo = newExtra.ifBlank { "Registrasi: Plotting Digital" }
                                    )
                                    // Reset Dialog variables
                                    newTitle = ""
                                    newDesc = ""
                                    newDetail = ""
                                    newExtra = ""
                                    showAddDialog = false
                                    Toast.makeText(context, "Titik Geospasial Baru Berhasil Di-Plot!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("gis_submit_marker")
                        ) {
                            Text("Simpan Titik")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterCategoryBadge(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .testTag("filter_badge_$label"),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else activeColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
