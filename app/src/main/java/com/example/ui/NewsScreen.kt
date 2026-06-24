package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.NewsItem
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GovGold

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    newsList: List<NewsItem>,
    role: String,
    onAddNews: () -> Unit,
    onDeleteNews: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Semua", "Berita", "Pengumuman", "Agenda", "Event")

    // Filtered lists based on Category, Search, and Publication Date queries
    val filteredNews = remember(newsList, searchQuery, selectedCategory, selectedDateFilter) {
        newsList.filter { news ->
            val matchesCategory = selectedCategory == "Semua" || news.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = news.title.contains(searchQuery, ignoreCase = true) ||
                    news.content.contains(searchQuery, ignoreCase = true)
            val matchesDate = selectedDateFilter == null || news.date == selectedDateFilter
            matchesCategory && matchesSearch && matchesDate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // News Page Title block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Berita & Pengumuman Resmi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = "Dapatkan kabar berkala dan agenda terbaru desa",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            if (role != "Masyarakat") {
                Button(
                    onClick = onAddNews,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("add_news_button"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tulis Kabar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tulis Berita", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search bar with keyword input and date picker integration
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari berita atau pengumuman...", fontSize = 13.sp) },
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
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = EmeraldGreen,
                    unfocusedIndicatorColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                    unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .weight(1f)
                    .testTag("news_search_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button for Date Filtering (opening DatePickerDialog)
            IconButton(
                onClick = {
                    val calendar = java.util.Calendar.getInstance()
                    // Default to current date or standard 2026 year matching seed dates
                    calendar.set(java.util.Calendar.YEAR, 2026)
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH)
                    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                    android.app.DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                            val formattedMonth = String.format("%02d", selectedMonth + 1)
                            val formattedDay = String.format("%02d", selectedDayOfMonth)
                            selectedDateFilter = "$selectedYear-$formattedMonth-$formattedDay"
                        },
                        year,
                        month,
                        day
                    ).show()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selectedDateFilter != null) EmeraldGreen.copy(alpha = 0.15f)
                        else (if (isDark) Color(0xFF1D2939) else Color(0xFFF1F5F9))
                    )
                    .border(
                        1.dp,
                        if (selectedDateFilter != null) EmeraldGreen else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                        RoundedCornerShape(14.dp)
                    )
                    .testTag("news_date_filter_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Saring Berdasarkan Tanggal",
                    tint = if (selectedDateFilter != null) EmeraldGreen else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                )
            }
        }

        // Active Date Filter Tag layout
        if (selectedDateFilter != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("news_active_date_filter_tag")
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tanggal: $selectedDateFilter",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus Saringan Tanggal",
                    tint = EmeraldGreen,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { selectedDateFilter = null }
                        .testTag("news_clear_date_filter_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Quick Filter Chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
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
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_chip_${cat.lowercase()}")
                ) {
                    Text(
                        text = cat,
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

        Spacer(modifier = Modifier.height(12.dp))

        // News articles scrolling columns
        if (filteredNews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Feed,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tidak Ada Kabar Ditemukan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Coba gunakan kata kunci atau kategori filter lain",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredNews, key = { it.id }) { news ->
                    NewsItemCard(
                        news = news,
                        role = role,
                        onDeleteNews = onDeleteNews
                    )
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(
    news: NewsItem,
    role: String,
    onDeleteNews: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Interactive likes count state engine
    var isLiked by remember { mutableStateOf(false) }
    val virtualLikesCount = remember(news.likesCount, isLiked) {
        if (isLiked) news.likesCount + 1 else news.likesCount
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("news_item_card_${news.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF334155) else Color(0xFFEDF2F7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // News Banner Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (news.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(news.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Gambar Berita",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Modern Gradient Placeholder with dynamic vector theme
                    val placeholderGradient = when (news.category) {
                        "Berita" -> Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF059669)))
                        "Pengumuman" -> Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)))
                        "Event" -> Brush.linearGradient(listOf(Color(0xFFBE123C), Color(0xFFE11D48)))
                        else -> Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(placeholderGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (news.category) {
                                    "Berita" -> Icons.Default.Announcement
                                    "Pengumuman" -> Icons.Default.Campaign
                                    "Event" -> Icons.Default.Celebration
                                    else -> Icons.Default.CalendarMonth
                                },
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SIJAGO DIGITAL NEWS",
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // Category Overlaid Badge Bubble
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (news.category) {
                                "Berita" -> Color(0xFF047857)
                                "Pengumuman" -> Color(0xFF1D4ED8)
                                "Event" -> Color(0xFFBE123C)
                                else -> Color(0xFFD97706)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = news.category.uppercase(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Admin Action Delete Overlay (Right top)
                if (role != "Masyarakat") {
                    IconButton(
                        onClick = { onDeleteNews(news.id) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(32.dp)
                            .testTag("delete_news_${news.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Berita",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Article Contents
            Column(modifier = Modifier.padding(16.dp)) {
                // Publish Dates Info & Read Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = news.date,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${maxOf(2, news.content.length / 100)} menit baca",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Article titles
                Text(
                    text = news.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Article details text
                Text(
                    text = news.content,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))

                // Footer interactive Actions (Likes, comments, shares layout)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Likes & Comments
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Likes click toggler
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isLiked = !isLiked }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Likes Button",
                                tint = if (isLiked) EmeraldGreen else Color.Gray,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$virtualLikesCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiked) EmeraldGreen else Color.Gray
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Discussion Comments",
                                tint = Color.Gray,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${news.commentsCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }

                    // Social Media Sharing Trigger Row!
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // WA short share
                        IconButton(
                            onClick = {
                                shareToSocialMedia(context, news, "com.whatsapp")
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366).copy(alpha = 0.1f))
                                .testTag("share_whatsapp_${news.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share WA",
                                tint = Color(0xFF128C7E),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Copy link helper
                        IconButton(
                            onClick = {
                                val linkPayload = "http://desa.sumberrejo.id/berita/read?id=${news.id}"
                                clipboardManager.setText(AnnotatedString(linkPayload))
                                Toast.makeText(context, "Link berita disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.1f))
                                .testTag("copy_link_${news.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Tautan",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Global Android platform Send ActionIntent Share Sheet
                        IconButton(
                            onClick = {
                                val genericShareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, news.title)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${news.title}\n\nKategori: ${news.category}\n\n${news.content}\n\nSelengkapnya dapatkan informasi publik Desa Sumber Rejo lewat SIJAGO App! http://desa.sumberrejo.id/berita/read?id=${news.id}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(genericShareIntent, "Bagikan Kabar Desa"))
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                                .testTag("share_generic_${news.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SendToMobile,
                                contentDescription = "Bagikan Berita",
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Initiates target social media sending or fallback generic sharing.
 */
fun shareToSocialMedia(context: Context, news: NewsItem, packageName: String) {
    val shareContent = "${news.title}\n\nKategori: ${news.category}\n\n${news.content}\n\nBagikan berita desa dari SIJAGO Desa Digital!"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareContent)
    }
    
    // Check if the target application (e.g. WhatsApp) is installed
    val pm = context.packageManager
    val resolvedInfo = pm.queryIntentActivities(intent, 0)
    var appFound = false
    
    for (info in resolvedInfo) {
        if (info.activityInfo.packageName.startsWith(packageName) || info.activityInfo.name.startsWith(packageName)) {
            intent.setPackage(info.activityInfo.packageName)
            appFound = true
            break
        }
    }

    try {
        if (appFound) {
            context.startActivity(intent)
        } else {
            // Fallback gracefully to Standard generic chooser
            context.startActivity(Intent.createChooser(intent, "Bagikan ke Media Sosial"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal memulai aplikasi berbagi: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Highly styled add news dialog allowing preset photo configurations.
 */
@Composable
fun AddNewsDialog(
    onSave: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Berita") }
    var content by remember { mutableStateOf("") }
    var selectedPresetUrl by remember { mutableStateOf("") }

    val presetBanners = listOf(
        "Pembangunan" to "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?auto=format&fit=crop&q=80&w=600",
        "Posyandu" to "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&q=80&w=600",
        "Festival" to "https://images.unsplash.com/photo-1533900298318-6b8da08a523e?auto=format&fit=crop&q=80&w=600",
        "Rapat" to "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&q=80&w=600"
    )

    val isDark = isSystemInDarkTheme()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tulis Pengumuman / Kabar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Publikasikan agenda, event, atau berita resmi warga secara transparan.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Berita Resmi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_news_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selection quick filter options
                Text(text = "Pilih Kategori:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                val categories = listOf("Berita", "Pengumuman", "Agenda", "Event")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelectedCat = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelectedCat) EmeraldGreen.copy(alpha = 0.15f) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                )
                                .border(
                                    1.dp,
                                    if (isSelectedCat) EmeraldGreen else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelectedCat) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelectedCat) EmeraldGreen else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Predefined Preset Banner Image Selectors
                Text(text = "Pilih Ilustrasi Gambar Pendukung:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetBanners.forEach { (label, url) ->
                        val isSelectedPreset = selectedPresetUrl == url
                        Card(
                            modifier = Modifier
                                .size(width = 90.dp, height = 54.dp)
                                .clickable { selectedPresetUrl = if (isSelectedPreset) "" else url }
                                .border(
                                    2.dp,
                                    if (isSelectedPreset) EmeraldGreen else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = label,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                            )
                                        )
                                )
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Description Narration
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Narasi lengkap kabar") },
                    placeholder = { Text("Tuliskan deskripsi lengkap atau rincian agenda desa di sini...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = EmeraldGreen,
                        focusedLabelColor = EmeraldGreen,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Actions Dismiss/Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && content.isNotEmpty()) {
                                onSave(title, category, content, selectedPresetUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_add_news_button")
                    ) {
                        Text("Tayangkan Kabar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
