package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatEntity
import com.example.data.database.MessageEntity
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentChatId by viewModel.selectedChatId.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()
    val allChats by viewModel.allChats.collectAsState()

    val currentChat = remember(allChats, currentChatId) {
        allChats.find { it.id == currentChatId }
    }

    val playingVoiceId by viewModel.playingVoiceId.collectAsState()
    val downloadedFileIds by viewModel.downloadedFileIds.collectAsState()
    val fileDownloadProgress by viewModel.fileDownloadProgress.collectAsState()

    val isDark = viewModel.isDarkTheme
    val bgColor = if (isDark) Color(0xFF0F1219) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF141822) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color.Gray else Color(0xFF475569)

    // Retrieve wallpaper configurations
    val currentWallpaper = remember(currentChat) {
        val wallpaperId = currentChat?.customWallpaper ?: "slate_dark"
        viewModel.wallpaperOptions.find { it.id == wallpaperId } ?: viewModel.wallpaperOptions[0]
    }

    val backgroundBrush = remember(currentWallpaper, isDark) {
        val startHex = if (!isDark && currentWallpaper.id == "slate_dark") "#E2E8F0" else currentWallpaper.hexStart
        val endHex = if (!isDark && currentWallpaper.id == "slate_dark") "#F8FAFC" else currentWallpaper.hexEnd
        Brush.linearGradient(
            colors = listOf(
                Color(android.graphics.Color.parseColor(startHex)),
                Color(android.graphics.Color.parseColor(endHex))
            )
        )
    }

    // Scroll to latest message whenever size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var showWallpaperSelector by remember { mutableStateOf(false) }
    var showFileSharePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showWallpaperSelector = true }
                    ) {
                        currentChat?.let { chat ->
                            AvatarView(chat.avatarUrl, size = 38.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = chat.title,
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = chat.statusText,
                                    color = if (chat.statusText.contains("печатает", true)) Color(0xFF26D07C) else subTextColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = textColor)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showWallpaperSelector = true },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Outlined.Palette, contentDescription = "Обои чата", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF11141A) else Color(0xFFE2E8F0)
                )
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundBrush)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Messages List
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(Color(0xD90F1219), RoundedCornerShape(16.dp))
                                .padding(24.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = "Нет сообщений", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Здесь еще нет истории.\nНачни общение первым!", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                playingVoiceId = playingVoiceId,
                                downloadedFileIds = downloadedFileIds,
                                fileDownloadProgress = fileDownloadProgress,
                                isDark = isDark,
                                onClick = {
                                    viewModel.selectedMessageForMenu = msg
                                },
                                onVoicePlay = {
                                    viewModel.toggleVoicePlayback(msg.id, msg.voiceDurationSec ?: 0)
                                },
                                onDownloadFile = {
                                    viewModel.startFileDownload(msg.id)
                                }
                            )
                        }
                    }
                }

                // Voice Recording floating status bar
                AnimatedVisibility(
                    visible = viewModel.isRecordingVoice,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE94057))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Запись голосового: ${viewModel.voiceRecordDurationSec} сек",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { viewModel.cancelVoiceRecording() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                            ) {
                                Text("Отмена", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.stopAndSendVoiceRecording() },
                                modifier = Modifier.background(Color.White, CircleShape).size(36.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Отправить голос", tint = Color(0xFFE94057), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // 2. Draft message field with comprehensive options
                DraftBar(
                    viewModel = viewModel,
                    onSendText = {
                        viewModel.performSendMessage()
                    },
                    onAttachSimulation = {
                        showFileSharePicker = true
                    }
                )
            }

            // File Share / Exchange Picker Modal Sheet
            if (showFileSharePicker) {
                var customName by remember { mutableStateOf("") }
                var customSize by remember { mutableStateOf("1.5 MB") }
                var customExt by remember { mutableStateOf("pdf") }
                var fileGenError by remember { mutableStateOf("") }

                ModalBottomSheet(
                    onDismissRequest = { showFileSharePicker = false },
                    containerColor = Color(0xFF141822)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // safety for gesture pill and bottom bar
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Поделиться файлом", tint = Color(0xFFE94057), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Поделиться файлами 📎",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Отправьте быстрое вложение или спроектируйте свой собственный файл для моментального зашифрованного обмена:",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Quick Attach Actions Row
                        Text("Быстрые вложения:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Image Quick Action
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2430)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.insertSimulatedImageMessage()
                                        showFileSharePicker = false
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Изображение", tint = Color(0xFF26A69A), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Картинка", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 2. PDF Quick Action
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2430)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        currentChatId?.let { cid ->
                                            viewModel.sendFileMessage(cid, "Тенденции_рынка_TON.pdf", "pdf", "2.8 MB")
                                        }
                                        showFileSharePicker = false
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = "Документ", tint = Color(0xFFEF5350), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("План.pdf", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }

                            // 3. Zip Quick Action
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2430)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        currentChatId?.let { cid ->
                                            viewModel.sendFileMessage(cid, "karal_messenger_v1.zip", "zip", "14.2 MB")
                                        }
                                        showFileSharePicker = false
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Inventory, contentDescription = "Архив", tint = Color(0xFFFF7043), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Сборка.zip", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }

                            // 4. MP3 Quick Action
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2430)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        currentChatId?.let { cid ->
                                            viewModel.sendFileMessage(cid, "ambient_soundtrack.mp3", "mp3", "5.1 MB")
                                        }
                                        showFileSharePicker = false
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Audiotrack, contentDescription = "Аудио", tint = Color(0xFFAB47BC), modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Интро.mp3", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                        // Custom File Constructor Card block
                        Text("Конструктор своего файла:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1219)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Name input
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { 
                                        customName = it
                                        if (it.isNotBlank()) fileGenError = ""
                                    },
                                    label = { Text("Название файла (без расширения)") },
                                    singleLine = true,
                                    placeholder = { Text("например: Отчет_За_Июнь") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE94057),
                                        unfocusedBorderColor = Color(0xFF1F2430),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFE94057),
                                        unfocusedLabelColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (fileGenError.isNotBlank()) {
                                    Text(fileGenError, color = Color.Red, fontSize = 10.sp)
                                }

                                // Type choice chips
                                Text("Тип расширения:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val extensions = listOf("pdf", "docx", "zip", "mp3")
                                    extensions.forEach { ext ->
                                        val isSel = customExt == ext
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { customExt = ext },
                                            label = { Text(ext.uppercase()) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFE94057),
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFF1F2430),
                                                labelColor = Color.Gray
                                            )
                                        )
                                    }
                                }

                                // Size choice row
                                Text("Размер файла:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val sizes = listOf("1.2 MB", "4.5 MB", "18.4 MB", "75.0 MB")
                                    sizes.forEach { sz ->
                                        val isSel = customSize == sz
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { customSize = sz },
                                            label = { Text(sz) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF8A2387),
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFF1F2430),
                                                labelColor = Color.Gray
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Send custom generated file
                                Button(
                                    onClick = {
                                        val finalName = customName.trim()
                                        if (finalName.isBlank()) {
                                            fileGenError = "Пожалуйста, введите название файла"
                                        } else {
                                            currentChatId?.let { cid ->
                                                viewModel.sendFileMessage(cid, "$finalName.$customExt", customExt, customSize)
                                            }
                                            showFileSharePicker = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94057)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Отправить файл")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Прикрепить и отправить файл", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Wallpaper Selector Modal Sheet
            if (showWallpaperSelector) {
                ModalBottomSheet(
                    onDismissRequest = { showWallpaperSelector = false },
                    containerColor = Color(0xFF141822)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Оформление этого чата 🎨",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Выберите фоновый скин, чтобы персонализировать общение",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.wallpaperOptions) { wall ->
                                val wallBg = Brush.horizontalGradient(
                                    colors = listOf(Color(android.graphics.Color.parseColor(wall.hexStart)), Color(android.graphics.Color.parseColor(wall.hexEnd)))
                                )
                                val isSelected = currentWallpaper.id == wall.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF1F2636) else Color(0xFF10141D))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = Color(0xFFE94057),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.updateWallpaper(wall.id)
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(wallBg)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = wall.name,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showWallpaperSelector = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94057)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Применить обои")
                        }
                    }
                }
            }

            // Message context Bottom Sheet
            val clipboardManager = LocalClipboardManager.current
            viewModel.selectedMessageForMenu?.let { msg ->
                ModalBottomSheet(
                    onDismissRequest = { viewModel.selectedMessageForMenu = null },
                    containerColor = Color(0xFF141822)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Действия с сообщением",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 1. Fast Reactions Row Clicker
                        Text(
                            text = "Мгновенные реакции:",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        ) {
                            val reactions = listOf("👍", "❤️", "😂", "🔥", "😮", "😢")
                            reactions.forEach { react ->
                                val isSelected = msg.reactions?.contains(react) == true
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF1F2430) else Color(0xFF0F1219))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = Color(0xFFE94057),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.toggleReaction(msg.id, react)
                                            viewModel.selectedMessageForMenu = null
                                        }
                                ) {
                                    Text(react, fontSize = 20.sp)
                                }
                            }
                        }

                        // 2. Action commands list
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Color(0xFF1F2430))
                        Spacer(modifier = Modifier.height(12.dp))

                        BottomSheetActionItem(
                            label = "Перевести на русский 🇷🇺",
                            icon = Icons.Outlined.Translate,
                            onClick = {
                                viewModel.triggerTranslate(msg.id, "Russian")
                                viewModel.selectedMessageForMenu = null
                            }
                        )
                        BottomSheetActionItem(
                            label = "Translate to English 🇬🇧",
                            icon = Icons.Outlined.Translate,
                            onClick = {
                                viewModel.triggerTranslate(msg.id, "English")
                                viewModel.selectedMessageForMenu = null
                            }
                        )
                        BottomSheetActionItem(
                            label = "Анализ тона эмоции 🎭",
                            icon = Icons.Outlined.Face,
                            onClick = {
                                viewModel.triggerSentimentAnalysis(msg.id)
                                viewModel.selectedMessageForMenu = null
                            }
                        )
                        BottomSheetActionItem(
                            label = "Скопировать текст 📋",
                            icon = Icons.Outlined.ContentCopy,
                            onClick = {
                                clipboardManager.setText(AnnotatedString(msg.content))
                                viewModel.selectedMessageForMenu = null
                            }
                        )
                        BottomSheetActionItem(
                            label = "Удалить сообщение 🗑️",
                            icon = Icons.Outlined.Delete,
                            destructive = true,
                            onClick = {
                                viewModel.deleteMessage(msg.id)
                                viewModel.selectedMessageForMenu = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerWallpaperItem(name: String, tag: String) {

}

@Composable
fun BottomSheetActionItem(
    label: String,
    icon: ImageVector,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (destructive) Color(0xFFEF4444) else Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = if (destructive) Color(0xFFEF4444) else Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    playingVoiceId: String?,
    downloadedFileIds: Set<String>,
    fileDownloadProgress: Map<String, Float>,
    isDark: Boolean,
    onClick: () -> Unit,
    onVoicePlay: () -> Unit,
    onDownloadFile: () -> Unit
) {
    val isMe = message.isSentByMe
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    // Checking if disappearing message countdown should start
    var secondsRemaining by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(message.isDisappearing, message.disappearTime) {
        if (message.isDisappearing && message.disappearTime != null) {
            while (true) {
                val remaining = ((message.disappearTime) - System.currentTimeMillis()) / 1000
                secondsRemaining = if (remaining > 0) remaining else 0
                delay(1000)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            // Optional Sender label (grouped)
            if (!isMe) {
                Text(
                    text = message.senderName,
                    color = if (isDark) Color.LightGray else Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                )
            }

            // Message Bubble Card Base
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        if (isMe) {
                            Brush.linearGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057)))
                        } else {
                            if (isDark) {
                                Brush.linearGradient(listOf(Color(0xFF141A29), Color(0xFF1E2638)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                            }
                        }
                    )
                    .clickable { onClick() }
                    .padding(12.dp)
            ) {
                // Sentiment Analyst Tag badge
                message.sentiment?.let { mood ->
                    Row(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .background(Color(0x2EFFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Тон: ", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        Text(mood, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Disappearing countdown tag
                if (message.isDisappearing && secondsRemaining != null) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .background(Color(0x3DFE2A54), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Исчезнет через", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Исчезнет через: ${secondsRemaining}с",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // If voice message
                if (message.isVoice) {
                    val isPlaying = playingVoiceId == message.id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = onVoicePlay,
                            modifier = Modifier
                                .background(if (isMe || isDark) Color(0x33FFFFFF) else Color(0x1F000000), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Воспроизведение голоса",
                                tint = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Голосовое сообщение",
                                color = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Simulated audio progress track waveform
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(4.dp)
                                        .background(if (isMe || isDark) Color(0x33FFFFFF) else Color(0x1F000000), RoundedCornerShape(2.dp))
                                ) {
                                    if (isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(0.6f) // progress bar simulation
                                                .background(if (isMe || isDark) Color.White else Color(0xFF1E293B), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${message.voiceDurationSec ?: 0}с",
                                    color = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Image or File support (attachment simulation)
                message.attachmentPath?.let { asset ->
                    Spacer(modifier = Modifier.height(2.dp))
                    if (asset.startsWith("file:")) {
                        // File rendering
                        val parts = asset.split(":")
                        if (parts.size >= 4) {
                            val fileExt = parts[1]
                            val fileName = parts[2]
                            val fileSize = parts[3]
                            
                            val isDownloading = fileDownloadProgress.containsKey(message.id)
                            val progress = fileDownloadProgress[message.id] ?: 0f
                            val isDownloaded = downloadedFileIds.contains(message.id)
                            
                            val fileTypeColor = when (fileExt.lowercase()) {
                                "pdf" -> Color(0xFFEF5350)
                                "doc", "docx", "pptx" -> Color(0xFF42A5F5)
                                "zip", "rar", "tar", "gz" -> Color(0xFFFF7043)
                                "mp3", "wav", "flac" -> Color(0xFFAB47BC)
                                "png", "jpg", "jpeg" -> Color(0xFF26A69A)
                                else -> Color(0xFF78909C)
                            }
                            
                            val fileTypeIcon = when (fileExt.lowercase()) {
                                "pdf" -> Icons.Default.Description
                                "doc", "docx", "pptx" -> Icons.Default.Article
                                "zip", "rar", "tar", "gz" -> Icons.Default.Inventory
                                "mp3", "wav", "flac" -> Icons.Default.Audiotrack
                                "png", "jpg", "jpeg" -> Icons.Default.Image
                                else -> Icons.Default.AttachFile
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (!isDownloaded && !isDownloading) {
                                            onDownloadFile()
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(fileTypeColor)
                                        ) {
                                            Icon(
                                                fileTypeIcon, 
                                                contentDescription = fileExt, 
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fileName,
                                                color = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$fileExt • $fileSize",
                                                color = if (isMe || isDark) Color.LightGray else Color(0xFF475569),
                                                fontSize = 11.sp
                                            )
                                        }
                                        
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                                            if (isDownloaded) {
                                                Icon(
                                                    Icons.Default.Check, 
                                                    contentDescription = "Загружено", 
                                                    tint = Color(0xFF26D07C),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else if (isDownloading) {
                                                CircularProgressIndicator(
                                                    progress = { progress },
                                                    strokeWidth = 2.dp,
                                                    color = Color(0xFFE94057),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Download, 
                                                    contentDescription = "Скачать", 
                                                    tint = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (isDownloaded) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✓ Загружено. Нажмите, чтобы открыть",
                                            color = Color(0xFF26D07C),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else if (isDownloading) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Загрузка: ${(progress * 100).toInt()}%",
                                            color = Color(0xFFE94057),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(rememberAttachmentBrush(asset)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🖼️ Ресурс: $asset",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0x66000000), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!message.isVoice) {
                    Text(
                        text = message.content,
                        color = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                }

                // AI Translated Text Block
                message.translatedText?.let { translation ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0x3D000000), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                "Перевод 🌐",
                                color = Color(0xFFC93B6B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = translation,
                                color = if (isMe || isDark) Color.White else Color(0xFF1E293B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Bottom row: time & ticks
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val simpleFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val timeStr = simpleFormat.format(Date(message.timestamp))
                    Text(
                        text = timeStr,
                        color = if (isMe || isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF1E293B).copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Доставлено",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Reactions Display pills
            message.reactions?.let { reacts ->
                Row(
                    modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val list = reacts.split(",")
                    val counts = list.groupingBy { it }.eachCount()
                    counts.forEach { (emoji, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xE6141A29), RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color(0xFFE94057), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(emoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(count.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberAttachmentBrush(asset: String): Brush {
    return remember(asset) {
        when (asset) {
            "image_nebula" -> Brush.radialGradient(listOf(Color(0xFFF43F5E), Color(0xFF881337), Color(0xFF0F172A)))
            "image_design" -> Brush.sweepGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB), Color(0xFF0F172A)))
            "image_hardware" -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0F766E), Color(0xFF022C22)))
            else -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF451A03)))
        }
    }
}

@Composable
fun DraftBar(
    viewModel: ChatViewModel,
    onSendText: () -> Unit,
    onAttachSimulation: () -> Unit
) {
    Surface(
        color = Color(0xFF11141A),
        contentColor = Color.White,
        tonalElevation = 6.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach option pick button
                IconButton(
                    onClick = onAttachSimulation,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Прикрепить изображение", tint = Color(0xFFE94057))
                }

                // Disappearing Capsule stopwatch stopwatch picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            viewModel.disappearingDurationSec = when (viewModel.disappearingDurationSec) {
                                0 -> 5
                                5 -> 10
                                10 -> 30
                                else -> 0
                            }
                        }
                        .background(
                            if (viewModel.disappearingDurationSec > 0) Color(0x3DFE2A54) else Color(0x1AFFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.disappearingDurationSec > 0) Icons.Default.Timer else Icons.Default.TimerOff,
                        contentDescription = "Таймер",
                        tint = if (viewModel.disappearingDurationSec > 0) Color(0xFFFE2A54) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (viewModel.disappearingDurationSec > 0) "${viewModel.disappearingDurationSec}с" else "Выкл",
                        color = if (viewModel.disappearingDurationSec > 0) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Primary draft message element
                OutlinedTextField(
                    value = viewModel.currentMessageText,
                    onValueChange = { viewModel.currentMessageText = it },
                    placeholder = { Text("Сообщение...", color = Color.Gray, fontSize = 14.sp) },
                    singleLine = false,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF1A1F2B),
                        unfocusedContainerColor = Color(0xFF1A1F2B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .testTag("message_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Microphone Audio Record OR Send Button depending on text size
                if (viewModel.currentMessageText.isNotBlank()) {
                    IconButton(
                        onClick = onSendText,
                        modifier = Modifier
                            .testTag("send_button")
                            .background(Color(0xFFE94057), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Отправить", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.startVoiceRecording() },
                        modifier = Modifier
                            .background(Color(0xFF1F2430), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Запись голоса", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
