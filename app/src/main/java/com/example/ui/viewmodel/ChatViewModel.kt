package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ChatEntity
import com.example.data.database.MessageEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.chatDao(), database.messageDao())
    private val prefs = application.getSharedPreferences("karal_prefs", android.content.Context.MODE_PRIVATE)

    // Registration States
    var isUserRegistered by mutableStateOf(false)
    var myPinCode by mutableStateOf("")

    // Theme state
    var isDarkTheme by mutableStateOf(true)

    // Secret Vault States
    var isSecretSafeUnlocked by mutableStateOf(false)

    // Active Call States
    var isCallActive by mutableStateOf(false)
    var activeCallName by mutableStateOf("")
    var activeCallAvatar by mutableStateOf("")
    var isCallVideo by mutableStateOf(false)

    fun verifySecretPin(pin: String): Boolean {
        if (pin == myPinCode) {
            isSecretSafeUnlocked = true
            return true
        }
        return false
    }

    fun lockSecretSafe() {
        isSecretSafeUnlocked = false
    }

    fun createPrivateChat(title: String, type: String, avatar: String) {
        val chatId = "hidden_${type.lowercase()}_${java.util.UUID.randomUUID()}"
        val isCall = type == "CALL_PRIVATE"
        val statusText = when (type) {
            "GROUP_PRIVATE" -> "Вы, Александр, 2 участника"
            "CHANNEL_PRIVATE" -> "0 подписчиков"
            else -> "Входящий • Только что"
        }

        val chat = ChatEntity(
            id = chatId,
            title = title,
            avatarUrl = avatar,
            type = type,
            statusText = statusText,
            lastMessage = if (isCall) "📞 Разговор завершен" else "Чат зашифрован и создан в Сейфе",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0
        )

        viewModelScope.launch {
            database.chatDao().insertChat(chat)
            if (!isCall) {
                repository.sendMessage(
                    chatId = chatId,
                    content = if (type == "GROUP_PRIVATE") "👥 Добро пожаловать в скрытую группу '$title'. Это зашифрованное пространство." else "📣 Канал '$title' создан. Публикуйте свои мысли безопасно."
                )
            }
        }
    }

    fun addCallRecord(callerName: String, avatar: String, durationStr: String, isVideo: Boolean, isIncoming: Boolean) {
        val chatId = "hidden_call_${java.util.UUID.randomUUID()}"
        val typeIcon = if (isVideo) "📹" else "📞"
        val directionStr = if (isIncoming) "Входящий" else "Исходящий"
        val chat = ChatEntity(
            id = chatId,
            title = callerName,
            avatarUrl = avatar,
            type = "CALL_PRIVATE",
            statusText = "$directionStr • $durationStr",
            lastMessage = "$typeIcon Звонок завершен ($durationStr)",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0
        )
        viewModelScope.launch {
            database.chatDao().insertChat(chat)
        }
    }

    // Profile Settings
    var myName by mutableStateOf("Александр")
    var myBio by mutableStateOf("Пишу код, изучаю ИИ и создаю Karal Messenger.")
    var myAvatar by mutableStateOf("avatar_geek") // avatar_geek, avatar_art, avatar_space, avatar_neon

    // File sharing and simulation downloads
    val fileDownloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadedFileIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        isDarkTheme = prefs.getBoolean("is_dark_theme", true)
        isUserRegistered = prefs.getBoolean("is_registered", false)
        if (isUserRegistered) {
            myName = prefs.getString("user_name", "Александр") ?: "Александр"
            myBio = prefs.getString("user_bio", "Пишу код, изучаю ИИ и создаю Karal Messenger.") ?: "Пишу код, изучаю ИИ и создаю Karal Messenger."
            myAvatar = prefs.getString("user_avatar", "avatar_geek") ?: "avatar_geek"
            myPinCode = prefs.getString("user_pincode", "1234") ?: "1234"
        }
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        prefs.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
    }

    fun registerUser(name: String, bio: String, avatar: String, pinCode: String) {
        prefs.edit().apply {
            putBoolean("is_registered", true)
            putString("user_name", name)
            putString("user_bio", bio)
            putString("user_avatar", avatar)
            putString("user_pincode", pinCode)
            apply()
        }
        myName = name
        myBio = bio
        myAvatar = avatar
        myPinCode = pinCode
        isUserRegistered = true
    }

    fun logoutUser() {
        prefs.edit().clear().apply()
        isUserRegistered = false
        myName = "Александр"
        myBio = "Пишу код, изучаю ИИ и создаю Karal Messenger."
        myAvatar = "avatar_geek"
        myPinCode = ""
    }

    fun startFileDownload(messageId: String) {
        if (downloadedFileIds.value.contains(messageId) || fileDownloadProgress.value.containsKey(messageId)) return
        
        viewModelScope.launch {
            var progress = 0.0f
            while (progress < 1.0f) {
                delay(120)
                progress += 0.1f
                fileDownloadProgress.value = fileDownloadProgress.value + (messageId to progress.coerceAtMost(1.0f))
            }
            delay(100)
            downloadedFileIds.value = downloadedFileIds.value + messageId
            fileDownloadProgress.value = fileDownloadProgress.value - messageId
        }
    }

    fun sendFileMessage(chatId: String, fileName: String, fileExtension: String, fileSizeStr: String) {
        val attachmentValue = "file:$fileExtension:$fileName:$fileSizeStr"
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = "📎 Файл: $fileName ($fileSizeStr)",
                attachmentPath = attachmentValue
            )
        }
    }

    // Global Feed
    val allChats: StateFlow<List<ChatEntity>> = repository.allChats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    // Chats filtering tab (0 = "Все", 1 = "Личные", 2 = "Группы", 3 = "Каналы/Боты")
    private val _currentFilterTab = MutableStateFlow(0)
    val currentFilterTab: StateFlow<Int> = _currentFilterTab.asStateFlow()

    // Messages for the active chat
    val activeMessages: StateFlow<List<MessageEntity>> = _selectedChatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                repository.getMessagesForChat(chatId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Global & Chat Local Search query
    var searchQuery by mutableStateOf("")
    private val _searchResults = MutableStateFlow<List<MessageEntity>>(emptyList())
    val searchResults: StateFlow<List<MessageEntity>> = _searchResults.asStateFlow()

    // Message Input
    var currentMessageText by mutableStateOf("")

    // Self-destruct timers: 0 = Off, 5 = 5s, 10 = 10s, 30 = 30s
    var disappearingDurationSec by mutableStateOf(0)

    // Voice Recorder States
    var isRecordingVoice by mutableStateOf(false)
    var voiceRecordDurationSec by mutableStateOf(0)
    private var voiceTimerJob: Job? = null

    // Message context state (for bottom sheet controls)
    var selectedMessageForMenu by mutableStateOf<MessageEntity?>(null)

    // Wallpaper configuration options
    val wallpaperOptions = listOf(
        WallpaperConfig("slate_dark", "Глубокий сланец 🌌", "#111418", "#1C2026", "gradient"),
        WallpaperConfig("deep_indigo", "Индиго-Неон 🔮", "#0A0D1A", "#1C1430", "radial"),
        WallpaperConfig("sunset_aurora", "Закат Авроры 🌅", "#1A0F1F", "#301323", "gradient"),
        WallpaperConfig("matrix", "Терминал Киберпанка 📟", "#040B04", "#0C1F0C", "gradient"),
        WallpaperConfig("flat_sand", "Теплая Саванна 🏜️", "#1A1510", "#2E241B", "gradient")
    )

    // Voice speech simulation playback
    private val _playingVoiceId = MutableStateFlow<String?>(null)
    val playingVoiceId: StateFlow<String?> = _playingVoiceId.asStateFlow()
    private var voicePlayJob: Job? = null

    init {
        // Run database initializer
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }

        // Start a tick job to clean disappearing messages every second
        viewModelScope.launch {
            while (true) {
                delay(1000)
                repository.checkExpiredMessages()
            }
        }
    }

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
        // Clear message unread count on selection
        chatId?.let {
            viewModelScope.launch {
                val dbChat = repository.getChatById(it)
                if (dbChat != null && dbChat.unreadCount > 0) {
                    database.chatDao().updateUnreadCount(it, 0)
                }
            }
        }
    }

    fun setFilterTab(tabIndex: Int) {
        _currentFilterTab.value = tabIndex
    }

    fun triggerSearchGlobal(query: String) {
        searchQuery = query
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val results = database.messageDao().searchMessagesGlobal(query)
                val filtered = results.filter { msg ->
                    val isSecret = msg.chatId.startsWith("hidden_")
                    if (isSecret) isSecretSafeUnlocked else true
                }
                _searchResults.value = filtered
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun performSendMessage() {
        val text = currentMessageText.trim()
        val chatId = _selectedChatId.value ?: return

        if (text.isEmpty()) return

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = text,
                isDisappearing = disappearingDurationSec > 0,
                disappearDurationSec = if (disappearingDurationSec > 0) disappearingDurationSec else null
            )
        }
        currentMessageText = ""
    }

    fun insertSimulatedImageMessage() {
        val chatId = _selectedChatId.value ?: return
        val items = listOf(
            Pair("Красивая панорама космоса, снятая на новейший телескоп! 🌌", "image_nebula"),
            Pair("Дизайн-концепт нового интерфейса Karal Messenger! Плавные углы и неоновый акцент. 🚀", "image_design"),
            Pair("Запустили новый сервер для обработки ИИ-запросов. Скорость выросла на 40%! 🖥️", "image_hardware"),
            Pair("Утренний кофе вдохновляет на продуктивное программирование. Всем хорошего дня! ☕", "image_coffee")
        )
        val selected = items.random()

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = selected.first,
                attachmentPath = selected.second
            )
        }
    }

    fun triggerTranslate(messageId: String, langCode: String) {
        viewModelScope.launch {
            repository.translateMessage(messageId, langCode)
        }
    }

    fun triggerSentimentAnalysis(messageId: String) {
        viewModelScope.launch {
            repository.analyzeSentiment(messageId)
        }
    }

    fun toggleReaction(messageId: String, icon: String) {
        viewModelScope.launch {
            repository.addReaction(messageId, icon)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun updateWallpaper(wallpaperId: String) {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.updateWallpaper(chatId, wallpaperId)
        }
    }

    // Voice Message Simulation Features
    fun startVoiceRecording() {
        isRecordingVoice = true
        voiceRecordDurationSec = 0
        voiceTimerJob = viewModelScope.launch {
            while (isRecordingVoice) {
                delay(1000)
                voiceRecordDurationSec += 1
            }
        }
    }

    fun stopAndSendVoiceRecording() {
        isRecordingVoice = false
        voiceTimerJob?.cancel()
        val duration = voiceRecordDurationSec
        if (duration < 1) return

        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = "",
                isVoice = true,
                voiceDuration = duration
            )
        }
    }

    fun cancelVoiceRecording() {
        isRecordingVoice = false
        voiceTimerJob?.cancel()
    }

    fun toggleVoicePlayback(messageId: String, totalSec: Int) {
        if (_playingVoiceId.value == messageId) {
            _playingVoiceId.value = null
            voicePlayJob?.cancel()
        } else {
            voicePlayJob?.cancel()
            _playingVoiceId.value = messageId
            voicePlayJob = viewModelScope.launch {
                var current = 0
                while (current < totalSec && _playingVoiceId.value == messageId) {
                    delay(1000)
                    current++
                }
                if (_playingVoiceId.value == messageId) {
                    _playingVoiceId.value = null
                }
            }
        }
    }
}

data class WallpaperConfig(
    val id: String,
    val name: String,
    val hexStart: String,
    val hexEnd: String,
    val gradientType: String
)
