package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.database.ChatEntity
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeedScreen(
    viewModel: ChatViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.allChats.collectAsState()
    val activeFilterTab by viewModel.currentFilterTab.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val isDark = viewModel.isDarkTheme
    val bgColor = if (isDark) Color(0xFF0F1219) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF141822) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color.Gray else Color(0xFF475569)
    val labelTextColor = if (isDark) Color.LightGray else Color(0xFF334155)
    val inputBorderColor = if (isDark) Color(0xFF1F2430) else Color(0xFFD1D5DB)

    var showPinEntrySheet by remember { mutableStateOf(false) }
    var pinEntered by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var showCreateCallDialog by remember { mutableStateOf(false) }
    var newEntityName by remember { mutableStateOf("") }
    var newCallVideoSelected by remember { mutableStateOf(false) }

    val visibleChats = remember(chats) {
        chats.filter {
            val isPrivate = it.type.endsWith("_PRIVATE") || it.id.startsWith("hidden_")
            !isPrivate
        }
    }

    val secretChats = remember(chats, viewModel.isSecretSafeUnlocked) {
        if (viewModel.isSecretSafeUnlocked) {
            chats.filter {
                it.type.endsWith("_PRIVATE") || it.id.startsWith("hidden_")
            }
        } else {
            emptyList()
        }
    }

    val filteredChats = remember(visibleChats, secretChats, activeFilterTab) {
        when (activeFilterTab) {
            1 -> visibleChats.filter { it.type == "DIRECT" && it.id != "ton_devs" }
            2 -> visibleChats.filter { it.id == "ton_devs" }
            3 -> visibleChats.filter { it.type == "BOT" || it.type == "CHANNEL" }
            4 -> secretChats
            else -> visibleChats
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KARAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp,
                        style = LocalTextStyle.current.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
                            )
                        )
                    )
                },
                actions = {
                    val isSafeUnlocked = viewModel.isSecretSafeUnlocked
                    IconButton(
                        onClick = {
                            if (isSafeUnlocked) {
                                viewModel.lockSecretSafe()
                                viewModel.setFilterTab(0)
                            } else {
                                pinEntered = ""
                                pinError = ""
                                showPinEntrySheet = true
                            }
                        },
                        modifier = Modifier
                            .testTag("secret_vault_lock_btn")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isSafeUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Секретный сейф",
                            tint = if (isSafeUnlocked) Color(0xFF26D07C) else Color(0xFFEF5350),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .testTag("profile_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        AvatarView(viewModel.myAvatar, size = 36.dp, isOnline = true)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor
                )
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Sleek Search Input
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.triggerSearchGlobal(it) },
                placeholder = { Text("Поиск сообщений и контактов...", color = subTextColor, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск", tint = subTextColor) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.triggerSearchGlobal("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить", tint = subTextColor)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC93B6B),
                    unfocusedBorderColor = inputBorderColor,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("global_search_input")
            )

            // 2. Search Results Panel
            if (viewModel.searchQuery.isNotEmpty()) {
                Text(
                    text = "Результаты поиска сообщений (${searchResults.size})",
                    color = subTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Пусто",
                                tint = subTextColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ничего не найдено", color = subTextColor)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(searchResults) { msg ->
                            val linkedChat = chats.find { it.id == msg.chatId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectChat(msg.chatId)
                                        viewModel.triggerSearchGlobal("")
                                        onNavigateToChat(msg.chatId)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarView(msg.senderAvatar, size = 42.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = msg.senderName,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                            color = subTextColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.content,
                                        color = if (isDark) Color.LightGray else Color(0xFF334155),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (linkedChat != null) {
                                        Text(
                                            text = "в чате: ${linkedChat.title}",
                                            color = Color(0xFFC93B6B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = cardBg, thickness = 1.dp)
                        }
                    }
                }
            } else {
                // 3. Category Tabs
                val categories = remember(viewModel.isSecretSafeUnlocked) {
                    if (viewModel.isSecretSafeUnlocked) {
                        listOf("Все чаты", "Личные 💬", "Группы 👥", "Боты и каналы 🤖", "🔑 Сейф 🔒")
                    } else {
                        listOf("Все чаты", "Личные 💬", "Группы 👥", "Боты и каналы 🤖")
                    }
                }
                
                ScrollableTabRow(
                    selectedTabIndex = activeFilterTab.coerceAtMost(categories.size - 1),
                    edgePadding = 12.dp,
                    containerColor = bgColor,
                    contentColor = textColor,
                    indicator = { tabPositions ->
                        val targetIdx = activeFilterTab.coerceAtMost(categories.size - 1)
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[targetIdx]),
                            color = Color(0xFFE94057)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, title ->
                        Tab(
                            selected = activeFilterTab == index,
                            onClick = { viewModel.setFilterTab(index) },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            selectedContentColor = textColor,
                            unselectedContentColor = subTextColor,
                            modifier = Modifier.testTag("tab_filter_$index")
                        )
                    }
                }

                // Secret Vault Quick Action Banner (appears inside the safe tab)
                if (activeFilterTab == 4 && viewModel.isSecretSafeUnlocked) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141A25) else Color(0xFFE1F5FE)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "🔒 Скрытый Сейф Karal",
                                color = if (isDark) Color(0xFF26D07C) else Color(0xFF1B5E20),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Группы, каналы и звонки отсюда отсутствуют в стандартных списках и полностью зашифрованы.",
                                color = subTextColor,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { 
                                        newEntityName = ""
                                        showCreateGroupDialog = true 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26D07C)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = "Группа", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Группа", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = { 
                                        newEntityName = ""
                                        showCreateChannelDialog = true 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94057)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Канал", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Канал", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = { 
                                        newEntityName = ""
                                        showCreateCallDialog = true 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Звонок", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Звонок", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                // 4. Main Chat List
                if (filteredChats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Chat,
                                contentDescription = "Пусто",
                                tint = subTextColor,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Здесь пока нет чатов этой категории", color = subTextColor, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredChats, key = { it.id }) { chat ->
                            val isChatOnline = chat.statusText.contains("в сети", ignoreCase = true) || chat.id == "gemini_ai"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (chat.type == "CALL_PRIVATE") {
                                            viewModel.activeCallName = chat.title
                                            viewModel.activeCallAvatar = chat.avatarUrl
                                            viewModel.isCallVideo = chat.lastMessage?.contains("📹") ?: false
                                            viewModel.isCallActive = true
                                        } else {
                                            viewModel.selectChat(chat.id)
                                            onNavigateToChat(chat.id)
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                                    .testTag("chat_item_${chat.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar with Online Indicator ring
                                AvatarView(
                                    avatarType = chat.avatarUrl,
                                    size = 50.dp,
                                    isOnline = isChatOnline
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = chat.title,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (chat.type.endsWith("_PRIVATE") || chat.id.startsWith("hidden_")) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Скрыто",
                                                    tint = Color(0xFF26D07C),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        val simpleFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        val timeStr = simpleFormat.format(Date(chat.lastMessageTime))
                                        Text(
                                            text = timeStr,
                                            color = subTextColor,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Specific text or "typing..." indicator
                                        if (chat.statusText.contains("печатает", ignoreCase = true)) {
                                            Text(
                                                text = chat.statusText,
                                                color = Color(0xFF26D07C),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Text(
                                                text = chat.lastMessage ?: "Сообщений нет",
                                                color = subTextColor,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        if (chat.unreadCount > 0) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .background(Color(0xFFE94057), CircleShape)
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = chat.unreadCount.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = cardBg, thickness = 0.8.dp)
                        }
                    }
                }
            }
        }
    }

    if (showPinEntrySheet) {
        val sheetBg = if (isDark) Color(0xFF111418) else Color(0xFFF3F4F6)
        val keyBg = if (isDark) Color(0xFF1C2026) else Color(0xFFE2E8F0)
        val unselectedDotColor = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
        
        ModalBottomSheet(
            onDismissRequest = { showPinEntrySheet = false },
            containerColor = sheetBg,
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Замок",
                    tint = Color(0xFFE94057),
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Защищенный Сейф Karal",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                
                Text(
                    text = "Введите 4-значный системный PIN-код для входа в зашифрованное хранилище:",
                    color = subTextColor,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    for (i in 0 until 4) {
                        val isEntered = pinEntered.length > i
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isEntered) Color(0xFF26D07C) else unselectedDotColor
                                )
                        )
                    }
                }

                if (pinError.isNotEmpty()) {
                    Text(
                        text = pinError,
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "OK")
                    )
                    
                    keys.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            rowKeys.forEach { key ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(keyBg)
                                        .clickable {
                                            when (key) {
                                                "C" -> {
                                                    if (pinEntered.isNotEmpty()) {
                                                        pinEntered = pinEntered.dropLast(1)
                                                    }
                                                    pinError = ""
                                                }
                                                "OK" -> {
                                                    if (viewModel.verifySecretPin(pinEntered)) {
                                                        viewModel.setFilterTab(4)
                                                        showPinEntrySheet = false
                                                    } else {
                                                        pinError = "❌ Неверный PIN-код, попробуйте снова!"
                                                        pinEntered = ""
                                                    }
                                                }
                                                else -> {
                                                    if (pinEntered.length < 4) {
                                                        pinEntered += key
                                                        pinError = ""
                                                    }
                                                    if (pinEntered.length == 4) {
                                                        if (viewModel.verifySecretPin(pinEntered)) {
                                                            viewModel.setFilterTab(4)
                                                            showPinEntrySheet = false
                                                        } else {
                                                            pinError = "❌ Неверный PIN-код!"
                                                            pinEntered = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    Text(
                                        text = key,
                                        color = if (key == "C" || key == "OK") Color(0xFFE94057) else textColor,
                                        fontSize = 18.sp,
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

    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Создать Скрытую Группу 👥", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Группа будет создана в защищенном Сейфе и зашифрована.", color = subTextColor, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newEntityName,
                        onValueChange = { newEntityName = it },
                        label = { Text("Название группы") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Color(0xFF26D07C),
                            unfocusedBorderColor = inputBorderColor,
                            focusedLabelColor = Color(0xFF26D07C),
                            unfocusedLabelColor = subTextColor
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = cardBg,
            confirmButton = {
                Button(
                    onClick = {
                        val title = newEntityName.trim()
                        if (title.isNotEmpty()) {
                            viewModel.createPrivateChat(title, "GROUP_PRIVATE", "group")
                            showCreateGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26D07C))
                ) {
                    Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Отмена", color = subTextColor)
                }
            }
        )
    }

    if (showCreateChannelDialog) {
        AlertDialog(
            onDismissRequest = { showCreateChannelDialog = false },
            title = { Text("Создать Скрытый Канал 📣", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Канал будет создан в Сейфе. Сообщения публикуются анонимно.", color = subTextColor, fontSize = 12.sp)
                    OutlinedTextField(
                        value = newEntityName,
                        onValueChange = { newEntityName = it },
                        label = { Text("Название канала") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Color(0xFFE94057),
                            unfocusedBorderColor = inputBorderColor,
                            focusedLabelColor = Color(0xFFE94057),
                            unfocusedLabelColor = subTextColor
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = cardBg,
            confirmButton = {
                Button(
                    onClick = {
                        val title = newEntityName.trim()
                        if (title.isNotEmpty()) {
                            viewModel.createPrivateChat(title, "CHANNEL_PRIVATE", "channel")
                            showCreateChannelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94057))
                ) {
                    Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChannelDialog = false }) {
                    Text("Отмена", color = subTextColor)
                }
            }
        )
    }

    if (showCreateCallDialog) {
        var selectedContact by remember { mutableStateOf("Павел Дуров") }
        var selectedAvatar by remember { mutableStateOf("durov") }
        val contacts = listOf(
            Triple("Павел Дуров", "durov", "Telegram Creator"),
            Triple("Алиса Сейф", "alice", "Разработчик"),
            Triple("Виктор Секурити", "viktor", "Безопасность")
        )
        AlertDialog(
            onDismissRequest = { showCreateCallDialog = false },
            title = { Text("Запустить защищенный звонок 🔒", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Выберите контакт из адресной книги для защищенного звонка:", color = subTextColor, fontSize = 12.sp)
                    
                    contacts.forEach { contact ->
                        val isSel = selectedContact == contact.first
                        val unselectedContactBg = if (isDark) Color(0xFF1F2430) else Color(0xFFF1F5F9)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0x3326D07C) else unselectedContactBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedContact = contact.first
                                    selectedAvatar = contact.second
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarView(contact.second, size = 32.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(contact.first, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(contact.third, color = subTextColor, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Тип связи:", color = subTextColor, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !newCallVideoSelected,
                                onClick = { newCallVideoSelected = false },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF26D07C))
                            )
                            Text("Аудио", color = textColor, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newCallVideoSelected,
                                onClick = { newCallVideoSelected = true },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF26D07C))
                            )
                            Text("Видео", color = textColor, fontSize = 12.sp)
                        }
                    }
                }
            },
            containerColor = cardBg,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.activeCallName = selectedContact
                        viewModel.activeCallAvatar = selectedAvatar
                        viewModel.isCallVideo = newCallVideoSelected
                        viewModel.isCallActive = true
                        showCreateCallDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Text("Позвонить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCallDialog = false }) {
                    Text("Отмена", color = subTextColor)
                }
            }
        )
    }

    if (viewModel.isCallActive) {
        var callSec by remember { mutableStateOf(0) }
        var isMuted by remember { mutableStateOf(false) }
        var isSpeakerOn by remember { mutableStateOf(true) }
        var isCameraOff by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (viewModel.isCallActive) {
                delay(1000)
                callSec++
            }
        }

        val formattedCallTime = remember(callSec) {
            val mins = callSec / 60
            val secs = callSec % 60
            String.format("%02d:%02d", mins, secs)
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFA0A0D15))
                .clickable(enabled = true, onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 54.dp, horizontal = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ЗАШИФРОВАННЫЙ ЗВОНОК • KARAL SAFE 🔒",
                        color = Color(0xFF26D07C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.activeCallName,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (viewModel.isCallVideo && !isCameraOff) "Видеоподключение установлено" else "Соединение защищено сквозным шифрованием TON",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    if (viewModel.isCallVideo && !isCameraOff) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.radialGradient(listOf(Color(0xFF3355EE), Color(0xFF0F1522))))
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarView(viewModel.activeCallAvatar, size = 70.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    color = Color(0xFF26D07C),
                                    trackColor = Color(0xFF141822),
                                    modifier = Modifier.width(100.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Шифрование...", color = Color.LightGray, fontSize = 9.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(140.dp * pulseScale)
                                .clip(CircleShape)
                                .background(Color(0x1A26D07C))
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0x2226D07C))
                        )
                        AvatarView(viewModel.activeCallAvatar, size = 94.dp)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedCallTime,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        style = LocalTextStyle.current.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isMuted) "Микрофон отключен" else "В эфире",
                        color = if (isMuted) Color(0xFFEF5350) else Color(0xFF26D07C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color(0xFFEF5350) else Color(0xFF1F2430))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Мут",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { isCameraOff = !isCameraOff },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isCameraOff) Color(0xFF1F2430) else Color(0xFF0D9488))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Камера",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) Color(0xFF8A2387) else Color(0xFF1F2430))
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Динамик",
                                tint = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val mins = callSec / 60
                            val secs = callSec % 60
                            val durationText = if (mins > 0) "$mins мин $secs сек" else "$secs сек"
                            
                            viewModel.addCallRecord(
                                callerName = viewModel.activeCallName,
                                avatar = viewModel.activeCallAvatar,
                                durationStr = durationText,
                                isVideo = viewModel.isCallVideo,
                                isIncoming = false
                            )
                            viewModel.isCallActive = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Завершить звонок",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarView(avatarType: String, size: Dp = 48.dp, isOnline: Boolean = false) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        val backgroundBrush = remember(avatarType) {
            when (avatarType) {
                "bot" -> Brush.linearGradient(listOf(Color(0xFF5D2E8C), Color(0xFF2E7BCC)))
                "durov" -> Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                "channel" -> Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFDF2222)))
                "group" -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10b981)))
                "alice" -> Brush.linearGradient(listOf(Color(0xFFDB2777), Color(0xFFEC4899)))
                "viktor" -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1)))
                "avatar_geek" -> Brush.linearGradient(listOf(Color(0xFF0369A1), Color(0xFF0284C7)))
                "avatar_art" -> Brush.linearGradient(listOf(Color(0xFFBE185D), Color(0xFFDB2777)))
                "avatar_space" -> Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF5B21B6)))
                "avatar_neon" -> Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0D9488)))
                else -> Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
            }
        }

        val initialsAndEmoji = remember(avatarType) {
            when (avatarType) {
                "bot" -> "🌟"
                "durov" -> "📱"
                "channel" -> "📢"
                "group" -> "👥"
                "alice" -> "👩"
                "viktor" -> "👨"
                "avatar_geek" -> "🤓"
                "avatar_art" -> "🎨"
                "avatar_space" -> "🚀"
                "avatar_neon" -> "⚡"
                else -> "👤"
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(backgroundBrush)
        ) {
            Text(
                text = initialsAndEmoji,
                color = Color.White,
                fontSize = (size.value * 0.45).sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFF0F1219), CircleShape)
                    .padding(1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF26D07C), CircleShape)
                )
            }
        }
    }
}
