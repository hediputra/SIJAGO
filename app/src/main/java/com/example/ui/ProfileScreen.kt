package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Citizen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold
import com.example.ui.theme.GovNavy
import com.example.ui.theme.LocalIsDarkTheme

@Composable
fun ProfileScreen(
    viewModel: SijagoViewModel,
    isDark: Boolean = LocalIsDarkTheme.current
) {
    val loggedInCitizen by viewModel.loggedInCitizen.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showNik by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_container")
    ) {
        // Cover Graphic Header Layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                        }
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified Profile",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Profil Akun Terverifikasi",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Avatar Overlay & Name Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (-40).dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Large Initial Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                        .border(4.dp, if (isDark) Color(0xFF0F172A) else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (selectedRole == "Masyarakat") {
                        loggedInCitizen?.name?.take(1) ?: "W"
                    } else {
                        selectedRole.take(1)
                    }
                    Text(
                        text = initial.uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val displayName = if (selectedRole == "Masyarakat") {
                        loggedInCitizen?.name ?: "Warga Desa"
                    } else {
                        "Aparatur: $selectedRole"
                    }
                    Text(
                        text = displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (selectedRole == "Masyarakat") GovNavy.copy(alpha = 0.15f) else GovGold.copy(alpha = 0.15f),
                            contentColor = if (selectedRole == "Masyarakat") GovNavy else GovGold
                        ) {
                            Text(
                                text = selectedRole.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Gray.copy(alpha = 0.1f),
                            contentColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        ) {
                            Text(
                                text = "SUMBER REJO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile content body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (selectedRole == "Masyarakat" && loggedInCitizen != null) {
                val citizen = loggedInCitizen!!

                // A. Main Profile Credentials Card
                ProfileSectionCard(title = "Kredensial Penduduk", isDark = isDark) {
                    ProfileRowItem(
                        icon = Icons.Default.Badge,
                        label = "Nomor Induk Kependudukan (NIK)",
                        value = if (showNik) citizen.nik else "• • • • • • • • • • • • " + citizen.nik.takeLast(4),
                        trailing = {
                            IconButton(
                                onClick = { showNik = !showNik },
                                modifier = Modifier.size(24.dp).testTag("toggle_nik_button")
                            ) {
                                Icon(
                                    imageVector = if (showNik) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Lihat NIK",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        isDark = isDark
                    )
                    ProfileRowItem(
                        icon = Icons.Default.CreditCard,
                        label = "Nomor Kartu Keluarga (KK)",
                        value = "• • • • • • • • • • • • " + citizen.kk.takeLast(4),
                        isDark = isDark
                    )
                    ProfileRowItem(
                        icon = Icons.Default.PersonOutline,
                        label = "Nama Lengkap Sesuai KTP",
                        value = citizen.name,
                        isDark = isDark
                    )
                    ProfileRowItem(
                        icon = Icons.Default.FamilyRestroom,
                        label = "Jenis Kelamin & Umur",
                        value = "${citizen.gender} - ${citizen.age} Tahun",
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // B. Contact & Address Details Card
                ProfileSectionCard(
                    title = "Detail Kontak & Domisili", 
                    isDark = isDark,
                    headerAction = {
                        TextButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("edit_profile_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubah Detail", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen)
                        }
                    }
                ) {
                    ProfileRowItem(
                        icon = Icons.Default.Phone,
                        label = "Nomor Handphone Terdaftar",
                        value = citizen.phoneNumber,
                        isDark = isDark
                    )
                    ProfileRowItem(
                        icon = Icons.Default.WorkOutline,
                        label = "Pekerjaan Utama",
                        value = citizen.job,
                        isDark = isDark
                    )
                    ProfileRowItem(
                        icon = Icons.Default.School,
                        label = "Pendidikan Terakhir",
                        value = citizen.education,
                        isDark = isDark
                    )
                    ProfileRowItem(
                        icon = Icons.Default.Home,
                        label = "Alamat Sesuai Domisili",
                        value = "${citizen.address}, RT 0${citizen.rt} / RW 0${citizen.rw}, Dusun ${citizen.dusun}",
                        isDark = isDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // C. Account Status & Social Assistances
                ProfileSectionCard(title = "Status Kelayakan DTKS & Akun", isDark = isDark) {
                    AccountStatusWidget(citizen = citizen, isDark = isDark)
                }

            } else {
                // Admin, Kades, Sekdes view
                AdminProfileComponent(selectedRole = selectedRole, isDark = isDark)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // D. Security Preferences
            ProfileSectionCard(title = "Pengaturan & Keamanan", isDark = isDark) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Notifikasi Real-time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1E293B))
                            Text("Terima info bansos & pengaduan desa", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            checkedTrackColor = (if (isDark) Color(0xFF60A5FA) else EmeraldGreen).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_notifications")
                    )
                }

                Divider(color = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Autentikasi Biometrik", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF1E293B))
                            Text("Sidik jari untuk verifikasi surat", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                            checkedTrackColor = (if (isDark) Color(0xFF60A5FA) else EmeraldGreen).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_biometric")
                    )
                }

                // Theme Selection Row
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tema Aplikasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        Triple(SijagoViewModel.ThemeMode.LIGHT, Icons.Default.LightMode, "Terang"),
                        Triple(SijagoViewModel.ThemeMode.DARK, Icons.Default.DarkMode, "Gelap"),
                        Triple(SijagoViewModel.ThemeMode.SYSTEM, Icons.Default.SettingsSuggest, "Sistem")
                    )

                    themes.forEach { (mode, icon, label) ->
                        val isSelected = themeMode == mode
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isDark) Color(0xFF334155) else EmeraldGreen.copy(alpha = 0.12f)
                                    } else {
                                        if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) {
                                        if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                                    } else {
                                        Color.Transparent
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("theme_btn_${mode.name.lowercase()}"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) {
                                    if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                                } else {
                                    Color.Gray
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDark) Color.White else EmeraldGreen
                                } else {
                                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Profile Modal Sheets / Dialog
    if (showEditDialog && loggedInCitizen != null) {
        val citizen = loggedInCitizen!!
        var phoneInput by remember { mutableStateOf(citizen.phoneNumber) }
        var addressInput by remember { mutableStateOf(citizen.address) }
        var jobInput by remember { mutableStateOf(citizen.job) }
        var eduInput by remember { mutableStateOf(citizen.education) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Perbarui Profil Kependudukan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Silakan perbarui nomor kontak dan alamat tempat tinggal aktif di desa saat ini.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("No. Handphone HP") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone_input")
                    )

                    OutlinedTextField(
                        value = jobInput,
                        onValueChange = { jobInput = it },
                        label = { Text("Pekerjaan") },
                        leadingIcon = { Icon(Icons.Default.WorkOutline, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_job_input")
                    )

                    OutlinedTextField(
                        value = eduInput,
                        onValueChange = { eduInput = it },
                        label = { Text("Pendidikan Sesuai Ijazah") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_edu_input")
                    )

                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Alamat Tinggal") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_address_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCitizenProfile(
                            nik = citizen.nik,
                            phoneNumber = phoneInput,
                            address = addressInput,
                            job = jobInput,
                            education = eduInput
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                    ),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    modifier = Modifier.testTag("cancel_profile_button")
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    isDark: Boolean,
    headerAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                )
                headerAction?.invoke()
            }

            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun ProfileRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    trailing: @Composable (() -> Unit)? = null,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
        }

        trailing?.invoke()
    }
}

@Composable
fun AccountStatusWidget(
    citizen: Citizen,
    isDark: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Status Row NIK Validation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFECFDF5))
                .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFA7F3D0), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(GovNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "VALIDASI KEPENDUDUKAN DUKCAPIL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF10B981) else Color(0xFF065F46)
                )
                Text(
                    text = "Sesuai Database Terpadu Kependudukan Kemendagri",
                    fontSize = 10.sp,
                    color = if (isDark) Color.Gray else Color(0xFF047857)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Welfare DTKS Status Section
        Text(
            text = "Kualifikasi Kesejahteraan & Jaminan Sosial (DTKS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (citizen.isPoor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFFFFBEB))
                    .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = GovGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEREGISTRASI DALAM DESIL KESEJAHTERAAN (DTKS)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GovGold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tercatat sebagai Keluarga Prasejahtera Desa Sumber Rejo. Berhak menerima tipe bantuan sosial berikut:",
                    fontSize = 10.sp,
                    color = if (isDark) Color.Gray else Color(0xFF78350F)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GovGold,
                    contentColor = Color.White
                ) {
                    Text(
                        text = "PENERIMA AKTIF: ${citizen.bansosType}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFEFF6FF))
                    .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFDBEAFE), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "KLASIFIKASI SECARA UMUM: WARGA MANDIRI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                    )
                    Text(
                        text = "Masyarakat Berdaya Mandiri (Non-Penerima DTKS)",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AdminProfileComponent(
    selectedRole: String,
    isDark: Boolean
) {
    ProfileSectionCard(title = "Kredensial Otoritas Staf Pemerintahan", isDark = isDark) {
        ProfileRowItem(
            icon = Icons.Default.AdminPanelSettings,
            label = "ID Staf Kepegawaian Desa",
            value = "STF-2026-SR-" + selectedRole.take(3).uppercase(),
            isDark = isDark
        )
        ProfileRowItem(
            icon = Icons.Default.WorkOutline,
            label = "Jabatan Instansi",
            value = selectedRole,
            isDark = isDark
        )
        ProfileRowItem(
            icon = Icons.Default.Security,
            label = "Tingkat Hak Akses Kontrol",
            value = if (selectedRole == "Admin" || selectedRole == "Kepala Desa" || selectedRole == "Sekretaris Desa") {
                "AKSES PENUH (Superuser / Verifikator Utama)"
            } else {
                "AKSES OPERATOR (Entri & Monitoring)"
            },
            isDark = isDark
        )
        ProfileRowItem(
            icon = Icons.Default.Terminal,
            label = "IP Adress Akses Terakhir",
            value = "192.168.12.105 (Secure Local Server)",
            isDark = isDark
        )
    }
}
