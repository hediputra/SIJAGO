package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold

@Composable
fun GoogleAccountItem(
    name: String,
    email: String,
    avatarColor: Color,
    initial: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = email,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoogleCustomAuthSheet(
    onDismiss: () -> Unit,
    onAccountSelected: (String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                // Header of Google Identity Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Minimal colored Google signifier dots
                        Row(modifier = Modifier.padding(end = 8.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEA4335)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFBBC05)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF34A853)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4285F4)))
                        }
                        Text(
                            text = "Masuk dengan Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
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

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pilih akun untuk melanjutkan ke SIJAGO",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                // Accounts List (simulating Google Identity flow)
                GoogleAccountItem(
                    name = "Putra Sukahujan",
                    email = "putrasukahujan@gmail.com",
                    avatarColor = Color(0xFF1D4ED8),
                    initial = "P"
                ) {
                    onAccountSelected("Putra Sukahujan", "putrasukahujan@gmail.com")
                }

                GoogleAccountItem(
                    name = "Admin Desa Sumber Rejo",
                    email = "admin.sumberrejo@gmail.com",
                    avatarColor = Color(0xFF047857),
                    initial = "A"
                ) {
                    onAccountSelected("Admin Desa Sumber Rejo", "admin.sumberrejo@gmail.com")
                }

                GoogleAccountItem(
                    name = "Gunakan akun lain",
                    email = "guest@gmail.com",
                    avatarColor = Color(0xFF64748B),
                    initial = "+"
                ) {
                    onAccountSelected("Warga Mandiri", "warga.mandiri@gmail.com")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Untuk melanjutkan, Google akan membagikan nama, alamat email, preferensi bahasa, dan gambar profil Anda dengan SIJAGO.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSendResetLink: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf(initialEmail) }
    var isSending by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSent) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Lupa Kata Sandi?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Masukkan alamat email Anda yang terdaftar. Kami akan mengirimkan tautan aman untuk menyetel ulang kata sandi Anda.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorText = ""
                        },
                        label = { Text("Alamat Email") },
                        placeholder = { Text("contoh@email.com") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        isError = errorText.isNotEmpty(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_email_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (errorText.isNotEmpty()) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (emailInput.isBlank() || !emailInput.contains("@")) {
                                    errorText = "Harap masukkan alamat email yang valid."
                                } else {
                                    isSending = true
                                    // Simulated network latency
                                    isSending = false
                                    isSent = true
                                    onSendResetLink(emailInput)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_reset_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Kirim Tautan")
                            }
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sukses",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tautan Atur Ulang Dikirim!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sistem keamanan SIJAGO telah mengirimkan instruksi penyetelan ulang kata sandi ke $emailInput. Silakan periksa kotak masuk atau folder spam Anda.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_success_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Selesai")
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: SijagoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var isLoggingIn by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    var showGoogleAuthSheet by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    // Top-to-bottom atmospheric dynamic gradient
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFEFF6FF), Color(0xFFF3F6F9))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Vibrant Badge Emblem
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(EmeraldGreen, Color(0xFF3B82F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
                // Small shining star dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Branding Names
            Text(
                text = "SIJAGO",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                letterSpacing = (-1).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DESA SUMBER REJO - PORTAL SECURE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Form login container Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Silakan Masuk",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1F2937)
                    )
                    Text(
                        text = "Gunakan akun warga atau admin desa yang valid",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = ""
                        },
                        label = { Text("Alamat Email / NIK") },
                        placeholder = { Text("contoh@email.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (emailError.isNotEmpty()) MaterialTheme.colorScheme.error else EmeraldGreen
                            )
                        },
                        isError = emailError.isNotEmpty(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = ""
                        },
                        label = { Text("Kata Sandi") },
                        placeholder = { Text("Masukkan kata sandi") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (passwordError.isNotEmpty()) MaterialTheme.colorScheme.error else EmeraldGreen
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Sembunyikan Kata Sandi" else "Tampilkan Kata Sandi"
                                )
                            }
                        },
                        isError = passwordError.isNotEmpty(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = EmeraldGreen,
                            focusedLabelColor = EmeraldGreen,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row with Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen),
                                modifier = Modifier.scale(0.85f)
                            )
                            Text(
                                text = "Ingat Saya",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563)
                            )
                        }

                        Text(
                            text = "Lupa Kata Sandi?",
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { showForgotPassword = true }
                                .padding(vertical = 4.dp)
                                .testTag("forgot_password_link")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Log in button
                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                emailError = "Email atau NIK tidak boleh kosong"
                            }
                            if (password.isBlank()) {
                                passwordError = "Kata sandi tidak boleh kosong"
                            }
                            if (email.isNotBlank() && password.isNotBlank()) {
                                isLoggingIn = true
                                val success = viewModel.login(email, password)
                                isLoggingIn = false
                                if (success) {
                                    Toast.makeText(context, "Selamat datang kembali!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Gagal masuk. Periksa kembali detail Anda.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Masuk Secure",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // A subtle separator row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "ATAU LOG IN GOOGLE OAUTH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 10.dp),
                            letterSpacing = 0.5.sp
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Login Button (Simulating Google OAuth verification flow)
                    OutlinedButton(
                        onClick = { showGoogleAuthSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_google_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Custom high-quality mini Google multicolored signifier icon
                            Row(modifier = Modifier.padding(end = 10.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEA4335)))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFBBC05)))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF34A853)))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4285F4)))
                            }
                            Text(
                                text = "Lanjutkan dengan Google",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer / Assistant helper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Butuh bantuan akses?",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hubungi RT/RW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Silakan hubungi admin di Kantor Kepala Desa Sumber Rejo.", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    // Google Identity overlay sheet simulator
    if (showGoogleAuthSheet) {
        GoogleCustomAuthSheet(
            onDismiss = { showGoogleAuthSheet = false },
            onAccountSelected = { name, emailSel ->
                showGoogleAuthSheet = false
                viewModel.loginWithGoogle(name, emailSel)
                Toast.makeText(context, "Google Sign-In Sukses: $emailSel", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Forgot Password Custom Dialog
    if (showForgotPassword) {
        ForgotPasswordDialog(
            initialEmail = email,
            onDismiss = { showForgotPassword = false },
            onSendResetLink = { emailSent ->
                Toast.makeText(context, "Tautan berhasil dikirim ke: $emailSent", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// Simple extension for checkbox scale support
private fun Modifier.scale(scale: Float) = this.then(
    Modifier.padding(all = 0.dp) // dummy modifier or we can import standard scale later if needed
)
