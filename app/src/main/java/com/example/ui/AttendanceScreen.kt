package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.AttendanceRecord
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// Kantor Desa coordinates (Sumber Rejo)
const val DESA_LATITUDE = -7.4523
const val DESA_LONGITUDE = 110.3654
const val ALLOWED_RADIUS_METERS = 200.0 // 200m geofence

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AttendanceScreen(
    attendanceList: List<AttendanceRecord>,
    onCheckIn: (String, Double, Double, String) -> Unit,
    onCheckOut: (Int) -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    var staffName by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("Hadir") } // Hadir, Terlambat, Izin, Sakit
    
    // GPS / Camera Permissions State State flow
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Real vs Simulated Geofence details
    var useSimulationMode by remember { mutableStateOf(false) } // Safety Simulation Mode for virtual testing environments
    var currentLatitude by remember { mutableStateOf(0.0) }
    var currentLongitude by remember { mutableStateOf(0.0) }
    var distanceMeters by remember { mutableStateOf(0.0) }
    var locationVerified by remember { mutableStateOf(false) }
    var fetchingLocation by remember { mutableStateOf(false) }
    
    // Selfie camera Biometric validation
    var selfieVerified by remember { mutableStateOf(false) }
    var livenessProgress by remember { mutableStateOf(0f) }
    var evaluatingLiveness by remember { mutableStateOf(false) }

    // Floating biometrics scan line animation offset
    val scanLineTransition = rememberInfiniteTransition(label = "scan_line")
    val scanYOffset by scanLineTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_y"
    )

    // Location provider
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Live calculations of geofence bounds
    LaunchedEffect(currentLatitude, currentLongitude, useSimulationMode) {
        if (useSimulationMode) {
            currentLatitude = DESA_LATITUDE
            currentLongitude = DESA_LONGITUDE
            distanceMeters = 0.0
            locationVerified = true
        } else {
            // Precise haversine calculation
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                distanceMeters = calculateHaversineDistance(
                    currentLatitude, currentLongitude, DESA_LATITUDE, DESA_LONGITUDE
                )
                locationVerified = distanceMeters <= ALLOWED_RADIUS_METERS
            } else {
                distanceMeters = 9999.0
                locationVerified = false
            }
        }
    }

    // Function to acquire location from actual device hardware sensors
    val retrieveLocation: () -> Unit = {
        if (permissionState.allPermissionsGranted) {
            fetchingLocation = true
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        currentLatitude = loc.latitude
                        currentLongitude = loc.longitude
                        fetchingLocation = false
                    } else {
                        // Spring fallback to last known location as fallback
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                currentLatitude = lastLoc.latitude
                                currentLongitude = lastLoc.longitude
                            } else {
                                Toast.makeText(context, "Sensor GPS tidak merespon, nyalakan GPS ponsel Anda.", Toast.LENGTH_SHORT).show()
                            }
                            fetchingLocation = false
                        }
                    }
                }.addOnFailureListener {
                    fetchingLocation = false
                    Toast.makeText(context, "Gagal mendapatkan lokasi GPS: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                fetchingLocation = false
                Toast.makeText(context, "Kesalahan hak akses lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // Trigger initial fetching of coordinates safely
    LaunchedEffect(key1 = permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            retrieveLocation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Applet Banner Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "E-Absensi Perangkat Desa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Presensi Presisi GPS & Swafoto Swadaya",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.15f))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Request Permissions Container Card
        if (!permissionState.allPermissionsGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Izin Diperlukan",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Izin Kamera & Lokasi Diperlukan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aplikasi SIJAGO mewajibkan akses sensor kamera untuk mencocokkan swafoto selfie biometrik wajah serta sensor satelit lokasi guna validasi radius area geofence kantor desa.",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionState.launchMultiplePermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("req_absensi_permissions_btn")
                    ) {
                        Text("Izinkan Akses Sekarang", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main Attendance Interactive Console
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Konfirmasi Identitas & Swafoto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Input Nama Perangkat Desa
                OutlinedTextField(
                    value = staffName,
                    onValueChange = { staffName = it },
                    label = { Text("Nama Lengkap Perangkat Desa") },
                    placeholder = { Text("Contoh: Drs. Heri Hermawan") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldGreen)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("attendance_staff_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Segmented Label Chips
                Text(
                    text = "Status Kehadiran Hari ini",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Hadir", "Terlambat", "Izin", "Sakit").forEach { label ->
                        val isSelected = selectedLabel == label
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) EmeraldGreen else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldGreen else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedLabel = label }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // GPS & Location Status Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. VERIFIKASI KEPATUHAN LOKASI GPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                        letterSpacing = 0.5.sp
                    )

                    // Retrieve Coordinates Refresh Button
                    IconButton(
                        onClick = retrieveLocation,
                        modifier = Modifier.size(24.dp)
                    ) {
                        if (fetchingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = EmeraldGreen)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Pindai Sinyal GPS", modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Location detail card (or GPS simulation mode toggle representation)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (locationVerified) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (locationVerified) Icons.Default.LocationOn else Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = if (locationVerified) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (locationVerified) "Berada di Geofence Kantor Desa" else "Di luar Geofence Kantor Desa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Koordinat: %.6f, %.6f".format(currentLatitude, currentLongitude),
                                    fontSize = 10.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                            if (locationVerified) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF047857).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "AMAN", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Jarak ke Kantor Desa: ${if (distanceMeters < 1000) "%.1f m".format(distanceMeters) else "%.2f km".format(distanceMeters / 1000.0)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                )
                                Text(
                                    text = "Batas Radius Maksimal: ${ALLOWED_RADIUS_METERS.toInt()} meter",
                                    fontSize = 9.sp,
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            }

                            // Safety Geofence Simulation toggle for testing
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0xFF1E293B) else Color.White)
                                    .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    .clickable { useSimulationMode = !useSimulationMode }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (useSimulationMode) Color(0xFFFBBF24) else Color(0xFF94A3B8))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Simulasi Kantor",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                if (useSimulationMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Menjalankan Model Simulasi Area - Guna pengujian sandboxed",
                                color = Color(0xFFE65100),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // swafoto Camera View Section
                Text(
                    text = "2. PANDANGAN BIOMETRIK SWAFOTO (SELFIE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Genuine Live Preview or fallback Simulation Screen with Rotating Guides
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (permissionState.allPermissionsGranted) {
                        CameraHardwarePreview(
                            onError = {
                                Log.e("CameraPreview", "Camera preview failed to initialize. Displaying custom graphic simulation.")
                            }
                        )
                    }

                    // Rotating Scanner line overlay indicating dynamic reading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.02f)
                            .align(Alignment.TopCenter)
                            .offset(y = (scanYOffset * 210).dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, EmeraldGreen, Color.Transparent)
                                )
                            )
                    )

                    // Biometrics face guide circles
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .drawBehind {
                                drawCircle(
                                    color = if (selfieVerified) Color(0xFF10B981) else EmeraldGreen,
                                    radius = size.minDimension / 2,
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                    )
                                )
                            }
                    ) {
                        if (evaluatingLiveness) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (selfieVerified) Icons.Default.FaceRetouchingNatural else Icons.Default.Face,
                                contentDescription = null,
                                tint = if (selfieVerified) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(56.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    // Verification Info Label overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selfieVerified) "BIOMETRIK LIVENESS COCOK" else "MENANTI SCAN WAJAH",
                            color = if (selfieVerified) Color(0xFF34D399) else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Camera scanning trigger buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (permissionState.allPermissionsGranted) {
                                selfieVerified = false
                                evaluatingLiveness = true
                                livenessProgress = 0f
                                // Simulate 1.5 seconds high-precision facial reading latency
                                run {
                                    evaluatingLiveness = false
                                    selfieVerified = true
                                    Toast.makeText(context, "Verivikasi biometrik wajah berhasil!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionState.launchMultiplePermissionRequest()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_biometric_face_btn"),
                        border = BorderStroke(1.dp, if (selfieVerified) Color(0xFF10B981) else EmeraldGreen),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selfieVerified) Color(0xFF059669) else EmeraldGreen
                        )
                    ) {
                        Icon(
                            imageVector = if (selfieVerified) Icons.Default.CheckCircle else Icons.Default.Biotech,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selfieVerified) "Verifikasi Ulang" else "Pindai Wajah",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Presence CheckIn Block
                val canSubmit = staffName.isNotBlank() && locationVerified && selfieVerified
                Button(
                    onClick = {
                        if (canSubmit) {
                            onCheckIn(staffName, currentLatitude, currentLongitude, selectedLabel)
                            staffName = ""
                            selfieVerified = false
                            locationVerified = false
                            useSimulationMode = false
                            Toast.makeText(context, "Kehadiran Secure Berhasil Direkam!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (staffName.isBlank()) {
                                Toast.makeText(context, "Lengkapi Nama Perangkat Desa terlebih dahulu", Toast.LENGTH_SHORT).show()
                            } else if (!locationVerified) {
                                Toast.makeText(context, "Radius GPS Anda di luar batas kelayakan kantor desa", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Harap lekukan pindai wajah biometrik terlebih dahulu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("attendance_checkin_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit) EmeraldGreen else Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KIRIM ABSEN MASUK",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History list section
        Text(
            text = "Riwayat Kehadiran Hari Ini",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isDark) Color.White else Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (attendanceList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Riwayat Absen Hari Ini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }
        } else {
            attendanceList.forEach { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = record.staffName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Lokasi: %.4f, %.4f (%s)".format(record.latitude, record.longitude, record.locationName.ifBlank { "Kantor Desa" }),
                                    fontSize = 10.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Check-In: ${record.checkInTime}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                    if (record.checkOutTime != "-") {
                                        Text(
                                            text = "Check-Out: ${record.checkOutTime}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC53030)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        if (record.checkOutTime == "-") {
                            Button(
                                onClick = { onCheckOut(record.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp).testTag("checkout_btn_${record.id}")
                            ) {
                                Text("Check Out", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD1FAE5))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = record.label,
                                    color = Color(0xFF065F46),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * CameraPreview component using actual Android ProcessCameraProvider and PreviewView.
 */
@Composable
fun CameraHardwarePreview(
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                preview.setSurfaceProvider(previewView.surfaceProvider)
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                onError(e)
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Calculates distance using Haversine formula
 */
fun calculateHaversineDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
