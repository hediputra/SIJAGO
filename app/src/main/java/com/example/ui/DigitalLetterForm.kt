package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold

/**
 * Reusable dynamic requirement document uploader following Material Design 3 guidelines.
 * Integrates real Android platform file picking via ActivityResultContracts.
 */
@Composable
fun RequirementUploaderItem(
    requirementName: String,
    description: String,
    attachedUri: Uri?,
    onFileSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    
    // File picker launcher using actual system APIs
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onFileSelected(uri)
            Toast.makeText(context, "Dokumen $requirementName berhasil dilampirkan!", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(
            1.dp,
            if (attachedUri != null) {
                if (isDark) Color(0xFF047857) else Color(0xFFD1FAE5)
            } else {
                if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (attachedUri != null) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = if (attachedUri != null) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = requirementName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (attachedUri != null) "File terlampir: ${attachedUri.lastPathSegment ?: "dokumen.pdf"}" else description,
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (attachedUri != null) {
                // Clear Attachment button
                IconButton(
                    onClick = { onFileSelected(null) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f), CircleShape)
                        .testTag("clear_requirement_${requirementName.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Lampiran",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Pick File Button with MD3 minimum interactive sizing
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF3B82F6).copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.12f),
                        contentColor = if (isDark) Color(0xFF60A5FA) else EmeraldGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .testTag("upload_requirement_${requirementName.lowercase().replace(" ", "_")}")
                        .height(36.dp)
                ) {
                    Text("Pilih File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Reusable dynamic form layout following Material Design 3 and Vibrant Palette.
 */
@Composable
fun DigitalLetterForm(
    viewModel: SijagoViewModel,
    onSubmit: (String, Map<String, String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loggedInCitizen by viewModel.loggedInCitizen.collectAsStateWithLifecycle()

    val letterTypes = listOf(
        "Surat Keterangan Domisili",
        "Surat Keterangan Usaha",
        "Surat Keterangan Tidak Mampu",
        "Surat Pengantar KTP"
    )

    var selectedType by remember { mutableStateOf(letterTypes.first()) }
    var detailKeperluan by remember { mutableStateOf("") }
    var extraDetailInput by remember { mutableStateOf("") }
    
    // Living live validation statuses
    var showErrors by remember { mutableStateOf(false) }
    
    // Document attachment attachments maps mapped dynamically
    val attachments = remember { mutableStateMapOf<String, Uri?>() }

    // Dynamic requirements based on Selected Letter Type
    val requirementsNeeded = remember(selectedType) {
        when (selectedType) {
            "Surat Keterangan Domisili" -> listOf(
                "Kartu Tanda Penduduk (KTP)" to "Scan / Foto KTP asli yang valid",
                "Kartu Keluarga (KK)" to "Scan / Foto KK asli terbaru anda"
            )
            "Surat Keterangan Usaha" -> listOf(
                "Kartu Tanda Penduduk (KTP)" to "Scan / Foto KTP asli pemilik usaha",
                "Foto Fisik Lokasi Usaha" to "Foto memuat papan toko / tempat produksi",
                "Surat Pengantar RT/RW" to "Pengantar sah dari RT atau RW setempat"
            )
            "Surat Keterangan Tidak Mampu" -> listOf(
                "Kartu Keluarga (KK)" to "Scan / Foto KK asli desa Sumber Rejo",
                "Surat Pengantar RT/RW" to "Pengantar sah prasejahtera dari pengurus RT"
            )
            "Surat Pengantar KTP" -> listOf(
                "Kartu Keluarga (KK)" to "Kartu keluarga berisikan data NIK pemohon",
                "Pas Foto Terbaru 3x4" to "Pas foto dengan latar belakang biru/merah"
            )
            else -> emptyList()
        }
    }

    // Clear attachments which belong to other types when selection changes
    LaunchedEffect(selectedType) {
        attachments.clear()
    }

    val isDark = isSystemInDarkTheme()
    val isReadyToSubmit = detailKeperluan.isNotBlank() && requirementsNeeded.all { attachments[it.first] != null }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Step 1: Identity Card Info Block (Auto-filled based on Authentication)
        val citizen = loggedInCitizen
        val userName = citizen?.name ?: "Budi Rahardjo"
        val userNik = citizen?.nik ?: "3301021008030005"
        val userKk = citizen?.kk ?: "3301021908050012"
        val userAddress = "${citizen?.address ?: "Jl. Makmur No. 12"}, RT ${citizen?.rt ?: 2} / RW ${citizen?.rw ?: 4}, Dusun ${citizen?.dusun ?: "Suka Makmur"}"
        val userPhone = citizen?.phoneNumber ?: "08123456789"

        Text(
            text = "1. IDENTITAS WARGA TERAUTENTIKASI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
            ),
            border = BorderStroke(
                1.dp,
                if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Terautentikasi",
                            tint = if (isDark) Color(0xFF34D399) else EmeraldGreen,
                            modifier = Modifier.size(20.dp).testTag("verified_identity_badge")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PROFIL TERVERIFIKASI SISTEM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = (if (isDark) Color(0xFF047857) else Color(0xFFD1FAE5)),
                        contentColor = (if (isDark) Color(0xFF34D399) else Color(0xFF065F46))
                    ) {
                        Text(
                            text = "AUTO-FILL AKTIF",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Data kependudukan Anda ditarik secara otomatis dari sistem pelayanan desa SIJAGO untuk kemudahan permohonan digital.",
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Read-only fields
                OutlinedTextField(
                    value = userName,
                    onValueChange = {},
                    label = { Text("Nama Pemohon") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        disabledLabelColor = Color.Gray,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("autofill_name_field")
                )
                
                OutlinedTextField(
                    value = userNik,
                    onValueChange = {},
                    label = { Text("NIK Pemohon") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        disabledLabelColor = Color.Gray,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("autofill_nik_field")
                )

                OutlinedTextField(
                    value = userKk,
                    onValueChange = {},
                    label = { Text("No. Kartu Keluarga (KK)") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        disabledLabelColor = Color.Gray,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("autofill_kk_field")
                )

                OutlinedTextField(
                    value = userAddress,
                    onValueChange = {},
                    label = { Text("Alamat Pemohon") },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        disabledLabelColor = Color.Gray,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("autofill_address_field")
                )

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = {},
                    label = { Text("No. Telepon / WhatsApp") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                        disabledBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        disabledLabelColor = Color.Gray,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("autofill_phone_field")
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "*Jika data kependudukan tidak sesuai, silakan hubungi operator desa atau perbarui profil Anda.",
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Step 2: Letter Type Selection Indicator & Chip Groups
        Text(
            text = "2. PILIH JENIS SURAT DIGITAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Vibrant MD3 Selectable Segment chips
        Column(
            modifier = Modifier
                .selectableGroup()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            letterTypes.forEach { type ->
                val isSelected = selectedType == type
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedType = type }
                        .testTag("chip_type_${type.lowercase().replace(" ", "_")}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            if (isDark) Color(0xFF1E3A8A) else Color(0xFFEFF6FF)
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color.White
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) {
                            if (isDark) Color(0xFF3B82F6) else Color(0xFF3B82F6)
                        } else {
                            if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = type,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                if (isDark) Color.White else Color(0xFF1D4ED8)
                            } else {
                                if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Form Details Input (Under MD3 input rules with beautiful guidance)
        Text(
            text = "3. ISI DETAIL PERMOHONAN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = detailKeperluan,
            onValueChange = { detailKeperluan = it },
            label = { Text("Keperluan / Dasar Pengajuan") },
            placeholder = { Text("Contoh: Mengurus beasiswa anak, melamar pekerjaan, etc.") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.SpeakerNotes,
                    contentDescription = null,
                    tint = if (showErrors && detailKeperluan.isBlank()) MaterialTheme.colorScheme.error else EmeraldGreen
                )
            },
            isError = showErrors && detailKeperluan.isBlank(),
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = EmeraldGreen,
                focusedLabelColor = EmeraldGreen,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showErrors && detailKeperluan.isBlank()) "Harus diisi secara lengkap!" else "Tuliskan alasan pengajuan peryaratan",
                        color = if (showErrors && detailKeperluan.isBlank()) MaterialTheme.colorScheme.error else Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${detailKeperluan.length} karakter",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_keperluan_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic supplemental input based on letter type
        val extraLabel = when (selectedType) {
            "Surat Keterangan Usaha" -> "Nama Usaha & Bidang Sektor Usaha"
            "Surat Keterangan Domisili" -> "Alamat Lengkap Domisili Saat Ini"
            "Surat Keterangan Tidak Mampu" -> "Jumlah Tanggungan Keluarga"
            "Surat Pengantar KTP" -> "Golongan Darah / Kehilangan KTP (Jika ganti)"
            else -> "Keterangan/Informasi Tambahan Surat"
        }
        val extraPlaceholder = when (selectedType) {
            "Surat Keterangan Usaha" -> "Contoh: Warung Sembako Berkah, Sektor Perdagangan"
            "Surat Keterangan Domisili" -> "Contoh: Jl. Merdeka No. 45, RT 02 / RW 04"
            else -> "Masukkan detail tambahan pendukung permohonan"
        }

        OutlinedTextField(
            value = extraDetailInput,
            onValueChange = { extraDetailInput = it },
            label = { Text(extraLabel) },
            placeholder = { Text(extraPlaceholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FactCheck,
                    contentDescription = null,
                    tint = EmeraldGreen
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = EmeraldGreen,
                focusedLabelColor = EmeraldGreen,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_extra_detail_input")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Step 4: Document Uploaders Dynamic checklist
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "4. UNGGAH DOKUMEN PERSYARATAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFF60A5FA) else EmeraldGreen,
                letterSpacing = 0.5.sp
            )
            
            // Checklist completeness count Indicator Badge
            val uploadedCount = requirementsNeeded.count { attachments[it.first] != null }
            val totalCount = requirementsNeeded.size
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (uploadedCount == totalCount) Color(0xFFD1FAE5) else Color(0xFFFFF3E0)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$uploadedCount dari $totalCount berkas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = if (uploadedCount == totalCount) Color(0xFF065F46) else Color(0xFFC2410C)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (requirementsNeeded.isEmpty()) {
            Text(
                text = "Tidak ada dokumen persyaratan wajib untuk kategori ini.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            requirementsNeeded.forEach { (reqName, reqDesc) ->
                RequirementUploaderItem(
                    requirementName = reqName,
                    description = reqDesc,
                    attachedUri = attachments[reqName],
                    onFileSelected = { uri ->
                        attachments[reqName] = uri
                    }
                )
            }
        }

        if (showErrors && !isReadyToSubmit) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Peringatan",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Harap lengkapi Keperluan permohonan dan lampirkan seluruh dokumen wajib di atas.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Trigger Row (Batal, Reset, Kirim)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cancel action
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            // Reset action
            OutlinedButton(
                onClick = {
                    detailKeperluan = ""
                    extraDetailInput = ""
                    attachments.clear()
                    showErrors = false
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("form_reset_button")
            ) {
                Text("Reset", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            // Submit Action with high contrast Vibrant Palette color
            Button(
                onClick = {
                    if (isReadyToSubmit) {
                        // Construct robust field-map for repository with auto-filled authenticated details
                        val fieldsMap = mutableMapOf(
                            "Nama Pemohon" to userName,
                            "NIK Pemohon" to userNik,
                            "No. KK Pemohon" to userKk,
                            "Alamat Pemohon" to userAddress,
                            "No. Telepon" to userPhone,
                            "Keperluan" to detailKeperluan,
                            "Detail" to extraDetailInput
                        )
                        // Inject attachment signifiers
                        attachments.forEach { (key, uri) ->
                            fieldsMap["Attachment_${key.replace(" ", "_")}"] = uri?.toString() ?: "Attached"
                        }
                        onSubmit(selectedType, fieldsMap)
                    } else {
                        showErrors = true
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("submit_form_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kirim Berkas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Reusable layout wrapping DigitalLetterForm inside a full screen responsive dialog container.
 */
@Composable
fun SubmitLetterFormDialog(
    viewModel: SijagoViewModel,
    onSubmit: (String, Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ajukan Surat Digital",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Lengkapi dokumen & informasi pendukung secara teliti",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Reusable Dynamic Form Component Embedded Elegantly!
                DigitalLetterForm(
                    viewModel = viewModel,
                    onSubmit = onSubmit,
                    onCancel = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
                )
            }
        }
    }
}
