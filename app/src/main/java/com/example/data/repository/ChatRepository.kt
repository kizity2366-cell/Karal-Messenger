package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.database.ChatDao
import com.example.data.database.ChatEntity
import com.example.data.database.MessageDao
import com.example.data.database.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun getChatById(chatId: String): ChatEntity? = withContext(Dispatchers.IO) {
        chatDao.getChatById(chatId)
    }

    /**
     * Initializes the DB with rich sample chats and messages if it is empty.
     */
    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingChats = allChats.firstOrNull() ?: emptyList()
        if (existingChats.isEmpty()) {
            Log.d("ChatRepository", "Initializing DB with default chats...")
            val now = System.currentTimeMillis()

            // 1. Create Default Chats
            val defaultChats = listOf(
                ChatEntity(
                    id = "gemini_ai",
                    title = "Karal AI 🌟",
                    avatarUrl = "bot",
                    type = "BOT",
                    statusText = "в сети",
                    lastMessage = "Я готов ответить на любой твой вопрос!",
                    lastMessageTime = now,
                    unreadCount = 0,
                    customWallpaper = "deep_indigo"
                ),
                ChatEntity(
                    id = "pavel_durov",
                    title = "Павел Дуров 📱",
                    avatarUrl = "durov",
                    type = "DIRECT",
                    statusText = "был(а) недавно",
                    lastMessage = "Попробуй ИИ-помощника, реакции и исчезающие сообщения!",
                    lastMessageTime = now - 60000 * 5,
                    unreadCount = 1,
                    customWallpaper = "slate_dark"
                ),
                ChatEntity(
                    id = "aura_channel",
                    title = "Karal News & Updates 📣",
                    avatarUrl = "channel",
                    type = "CHANNEL",
                    statusText = "14,800 подписчиков",
                    lastMessage = "🔥 Рады представить Karal Messenger на Jetpack Compose!",
                    lastMessageTime = now - 60000 * 60,
                    unreadCount = 0,
                    customWallpaper = "sunset_aurora"
                ),
                ChatEntity(
                    id = "ton_devs",
                    title = "Разработчики Karal 🧑‍💻",
                    avatarUrl = "group",
                    type = "DIRECT",
                    statusText = "Алиса, Виктор, Вы",
                    lastMessage = "Всё работает потрясающе плавно!",
                    lastMessageTime = now - 10000,
                    unreadCount = 0,
                    customWallpaper = "matrix"
                )
            )
            chatDao.insertChats(defaultChats)

            // 2. Insert Initial Messages
            val initialMessages = listOf(
                // Aura AI Chat
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "gemini_ai",
                    senderId = "gemini_ai",
                    senderName = "Karal AI",
                    senderAvatar = "bot",
                    content = "Привет! Я твой персональный ИИ-ассистент Karal AI 🤖. Напиши мне любой вопрос, и я отвечу тебе, используя модель gemini-3.5-flash!",
                    timestamp = now - 10000,
                    isSentByMe = false
                ),

                // Pavel Durov Chat
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "pavel_durov",
                    senderId = "pavel_durov",
                    senderName = "Павел Дуров",
                    senderAvatar = "durov",
                    content = "Привет! Добро пожаловать в Karal Messenger — новый стандарт скорости, конфиденциальности и изящного дизайна в мире общения 🌟.",
                    timestamp = now - 60000 * 20,
                    isSentByMe = false
                ),
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "pavel_durov",
                    senderId = "me",
                    senderName = "Вы",
                    senderAvatar = "me",
                    content = "Привет, Павел! Каковы основные возможности этого приложения?",
                    timestamp = now - 60000 * 15,
                    isSentByMe = true
                ),
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "pavel_durov",
                    senderId = "pavel_durov",
                    senderName = "Павел Дуров",
                    senderAvatar = "durov",
                    content = "Смотри: у тебя есть локальная база данных Room (работает оффлайн), ИИ-ассистент Karal AI, мгновенный умный перевод сообщений, автоанализ настроения, голосовые сообщения, реакции, исчезающие сообщения и кастомизация оформления для каждого чата. Пользуйся на здоровье!",
                    timestamp = now - 60000 * 5,
                    isSentByMe = false,
                    reactions = "👍"
                ),

                // Karal Updates Channel
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "aura_channel",
                    senderId = "aura_channel",
                    senderName = "Karal News",
                    senderAvatar = "channel",
                    content = "🚀 Добро пожаловать на официальный канал новостей Karal Messenger!\n\nЗдесь мы будем делиться обновлениями, планами разработки и крутыми фишками. Рады быть с вами!",
                    timestamp = now - 60000 * 120,
                    isSentByMe = false
                ),
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "aura_channel",
                    senderId = "aura_channel",
                    senderName = "Karal News",
                    senderAvatar = "channel",
                    content = "🔥 Сегодня состоялся релиз первой версии на Jetpack Compose! Приложение полностью адаптировано под Material Design 3, поддерживает тёмную тему, анимации и работает автономно на базе Android Room Database. Ставь лайк, если нравится!",
                    timestamp = now - 60000 * 60,
                    isSentByMe = false,
                    reactions = "❤️,🔥,👍"
                ),

                // ton_devs Chat
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "ton_devs",
                    senderId = "alice",
                    senderName = "Алиса",
                    senderAvatar = "alice",
                    content = "Привет ребята! Я добавила поддержку Room Database. Чат открывается мгновенно, и кэшируются все диалоги.",
                    timestamp = now - 60000 * 4,
                    isSentByMe = false
                ),
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "ton_devs",
                    senderId = "viktor",
                    senderName = "Виктор",
                    senderAvatar = "viktor",
                    content = "Класс! А обои в чатах тоже сохраняются локально?",
                    timestamp = now - 60000 * 3,
                    isSentByMe = false
                ),
                MessageEntity(
                    id = UUID.randomUUID().toString(),
                    chatId = "ton_devs",
                    senderId = "alice",
                    senderName = "Алиса",
                    senderAvatar = "alice",
                    content = "Да, wallpaperId сохраняется в ChatEntity, и фон меняется динамически на лету. Всё работает потрясающе плавно!",
                    timestamp = now - 10000,
                    isSentByMe = false,
                    reactions = "👍"
                )
            )

            for (msg in initialMessages) {
                messageDao.insertMessage(msg)
            }
        }
    }

    /**
     * Inserts a user message, updates chat and starts Gemini call if to BOT
     */
    suspend fun sendMessage(
        chatId: String,
        content: String,
        isVoice: Boolean = false,
        voiceDuration: Int? = null,
        attachmentPath: String? = null,
        isDisappearing: Boolean = false,
        disappearDurationSec: Int? = null
    ): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val messageId = UUID.randomUUID().toString()

        val disappearTime = if (isDisappearing && disappearDurationSec != null) {
            now + (disappearDurationSec * 1000L)
        } else null

        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = "me",
            senderName = "Вы",
            senderAvatar = "me",
            content = if (isVoice) "🎙 Голосовое сообщение (${voiceDuration ?: 0} сек)" else content,
            timestamp = now,
            isSentByMe = true,
            attachmentPath = attachmentPath,
            isDisappearing = isDisappearing,
            disappearDurationSec = disappearDurationSec,
            disappearTime = disappearTime,
            isVoice = isVoice,
            voiceDurationSec = voiceDuration
        )

        // Insert message
        messageDao.insertMessage(message)

        // Update chat's last message info
        chatDao.updateLastMessage(chatId, message.content, now)

        // If direct or Bot, handle potential response
        if (chatId == "gemini_ai" && !isVoice) {
            chatDao.updateChatStatus(chatId, "печатает...")
            val responseText = tryGetGeminiResponse(chatId, content)
            chatDao.updateChatStatus(chatId, "в сети")

            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = "gemini_ai",
                senderName = "Karal AI",
                senderAvatar = "bot",
                content = responseText,
                timestamp = System.currentTimeMillis(),
                isSentByMe = false
            )
            messageDao.insertMessage(replyMsg)
            chatDao.updateLastMessage(chatId, responseText, replyMsg.timestamp)
        } else if (chatId == "pavel_durov") {
            // Simulated Pavel Durov response for interactive feel
            chatDao.updateChatStatus(chatId, "печатает...")
            kotlinx.coroutines.delay(1500)
            chatDao.updateChatStatus(chatId, "в сети")
            val pavelMessages = listOf(
                "Это интересная мысль. В Telegram мы бы сделали это за 3 дня! 😉",
                "Конфиденциальность — это не то, чем можно жертвовать ради сиюминутного удобства.",
                "Хороший мессенджер должен быть быстрым, простым и абсолютно защищенным.",
                "Мне нравится кастомизация оформления в Karal. Качественный штрих!",
                "Продолжай развивать этот проект, у него отличный потенциал!"
            )
            val randomReply = pavelMessages.random()
            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = "pavel_durov",
                senderName = "Павел Дуров",
                senderAvatar = "durov",
                content = randomReply,
                timestamp = System.currentTimeMillis(),
                isSentByMe = false
            )
            messageDao.insertMessage(replyMsg)
            chatDao.updateLastMessage(chatId, randomReply, replyMsg.timestamp)
        } else if (chatId == "ton_devs") {
            // Simulated group chat responses from Alice or Viktor
            chatDao.updateChatStatus(chatId, "Алиса печатает...")
            kotlinx.coroutines.delay(2000)
            chatDao.updateChatStatus(chatId, "Алиса, Виктор, Вы")
            val devReplies = listOf(
                "Алиса: Крутая фича! Надо будет добавить развертывание в облаке.",
                "Виктор: Да, согласен. А еще ИИ-анализ тона сообщений работает отлично!",
                "Алиса: Давайте завтра устроим созвон по поводу релиза.",
                "Виктор: Завтра буду готов в любое время после обеда."
            )
            val randomReply = devReplies.random()
            val senderParts = randomReply.split(": ")
            val senderLabel = senderParts[0]
            val senderText = senderParts[1]
            val sId = if (senderLabel == "Алиса") "alice" else "viktor"

            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = sId,
                senderName = senderLabel,
                senderAvatar = sId,
                content = senderText,
                timestamp = System.currentTimeMillis(),
                isSentByMe = false
            )
            messageDao.insertMessage(replyMsg)
            chatDao.updateLastMessage(chatId, senderText, replyMsg.timestamp)
        }

        // Return message ID
        messageId
    }

    /**
     * Communicates with the real Gemini API if configured; falls back to offline model responses if not.
     */
    private suspend fun tryGetGeminiResponse(chatId: String, userPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Привет! Твой запрос получен: \"$userPrompt\".\n\n⚠️ Обрати внимание: Ключ API Gemini не настроен в секретах AI Studio. Чтобы связать меня с реальной моделью, укажи твой ключ в секретах (Secrets Panel) как GEMINI_API_KEY. Я буду рад помочь тебе со всеми умными ответами, переводами и анализом!"
        }

        return try {
            val messagesFlow = messageDao.getMessagesForChat(chatId)
            val previousMessages = messagesFlow.firstOrNull() ?: emptyList()

            // Build context of last 5 messages for conversation flow
            val conversationList = previousMessages.takeLast(7).map { msg ->
                val role = if (msg.isSentByMe) "user" else "model"
                GeminiContent(
                    parts = listOf(GeminiPart(text = msg.content)),
                    role = role
                )
            }

            val request = GeminiRequest(
                contents = conversationList,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = "You are Karal AI, an intelligent, helpful and very friendly assistant inside Karal Messenger. Speak in Russian. Keep your responses structured, clear, visually elegant using emojis. Suggest help."))
                )
            )

            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Я получил пустой ответ от ИИ."
        } catch (e: Exception) {
            "Ошибка при запросе к Karal AI: ${e.localizedMessage}. Проверь подключение или правильность GEMINI_API_KEY."
        }
    }

    /**
     * Translates a message content into English or Russian using Gemini API
     */
    suspend fun translateMessage(messageId: String, targetLanguage: String): String? = withContext(Dispatchers.IO) {
        val message = messageDao.getMessageById(messageId) ?: return@withContext null
        val apiKey = BuildConfig.GEMINI_API_KEY

        val translationPrompt = "Translate the following message into $targetLanguage. Provide strictly only the translated text, no introductions, no explanations, no original copy, no quotes. Just the clean translation: \n\"${message.content}\""

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Simulated offline dummy translator
            return@withContext if (targetLanguage.contains("Russian", ignoreCase = true) || targetLanguage.contains("RU", ignoreCase = true)) {
                "[Перевод (симуляция):] " + mockTranslateRu(message.content)
            } else {
                "[Translation (mock):] " + mockTranslateEn(message.content)
            }
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = translationPrompt))))
            )
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val translatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (translatedText != null) {
                val updatedMessage = message.copy(translatedText = translatedText)
                messageDao.updateMessage(updatedMessage)
                Log.d("ChatRepository", "Message translated: $translatedText")
                return@withContext translatedText
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Translation failed", e)
        }
        null
    }

    /**
     * Performs Sentiment analysis/mood check for a message using Gemini API
     */
    suspend fun analyzeSentiment(messageId: String): String? = withContext(Dispatchers.IO) {
        val message = messageDao.getMessageById(messageId) ?: return@withContext null
        val apiKey = BuildConfig.GEMINI_API_KEY

        val sentimentPrompt = "Evaluate the emotional content or sentiment of the following sentence. Answer with exactly one emoji and one capitalised word in Russian, strictly matching this pattern: '[Emojji] [Mood]'. Select the closest one from: 'Positive 😊', 'Angry 😡', 'Sad 😢', 'Excited 🤩', 'Anxious 😰', 'Love ❤️', 'Humorous 😂', 'Peaceful 🍃'. Do not write any other words or sentences! The sentence: \"${message.content}\""

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Simulated offline mood analyst
            val offlineSentiment = mockAnalyzeSentiment(message.content)
            val updatedMessage = message.copy(sentiment = offlineSentiment)
            messageDao.updateMessage(updatedMessage)
            return@withContext offlineSentiment
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = sentimentPrompt))))
            )
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val sentimentText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()?.filter { it != '"' && it != '\'' }
            if (sentimentText != null) {
                val updatedMessage = message.copy(sentiment = sentimentText)
                messageDao.updateMessage(updatedMessage)
                return@withContext sentimentText
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Sentiment analysis failed", e)
        }
        null
    }

    suspend fun addReaction(messageId: String, icon: String) = withContext(Dispatchers.IO) {
        val message = messageDao.getMessageById(messageId) ?: return@withContext
        val currentReactions = message.reactions ?: ""
        val newReactions = if (currentReactions.isEmpty()) {
            icon
        } else if (currentReactions.contains(icon)) {
            // Toggle it off
            val list = currentReactions.split(",").toMutableList()
            list.remove(icon)
            list.joinToString(",")
        } else {
            currentReactions + "," + icon
        }
        messageDao.updateMessage(message.copy(reactions = if(newReactions.isEmpty()) null else newReactions))
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        messageDao.deleteMessageById(messageId)
    }

    suspend fun updateWallpaper(chatId: String, wallpaper: String?) = withContext(Dispatchers.IO) {
        chatDao.updateWallpaper(chatId, wallpaper)
    }

    // Run interval checker to clean expiring disappearing messages
    suspend fun checkExpiredMessages() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val deletedCount = messageDao.deleteExpiredMessages(now)
        if (deletedCount > 0) {
            Log.d("ChatRepository", "Cleaned up $deletedCount expired messages")
        }
    }

    // Mock functions for full offline capability
    private fun mockTranslateRu(text: String): String {
        return when {
            text.contains("Привет", true) -> "Hello!"
            text.contains("Как дела", true) -> "How are you?"
            text.contains("Starship", true) -> "Испытание Starship успешно завершилось на космодроме Starbase в Техасе! Все 33 двигателя Raptor сработали в штатном режиме."
            text.contains("Room", true) -> "I added Room DB support. The chat loads instantly."
            else -> "Это автоматический тестовый перевод сообщения: \"$text\""
        }
    }

    private fun mockTranslateEn(text: String): String {
        return when {
            text.contains("Hello", true) -> "Привет!"
            text.contains("Starship", true) -> "Испытательный запуск Starship успешно состоялся в Техасе!"
            text.contains("Room", true) -> "Я добавил поддержку базы данных Room. Чат загружается моментально."
            else -> "This is an automatic simulated translation of message: \"$text\""
        }
    }

    private fun mockAnalyzeSentiment(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("привет") || lower.contains("добро пожаловать") || lower.contains("рады") -> "Дружелюбно 😊"
            lower.contains("класс") || lower.contains("отлично") || lower.contains("крутая") || lower.contains("успешно") -> "Радостно 🤩"
            lower.contains("вопрос") || lower.contains("каковы") -> "Любопытство 🤔"
            lower.contains("ошибка") || lower.contains("сбой") || lower.contains("задержка") -> "Обеспокоенность 😰"
            else -> "Спокойствие 🍃"
        }
    }
}
