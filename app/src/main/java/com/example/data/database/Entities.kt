package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val avatarUrl: String,
    val type: String, // "DIRECT", "CHANNEL", "BOT"
    val statusText: String, // e.g. "в сети", "печатает...", "был в сети недавно"
    val lastMessage: String?,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val customWallpaper: String? = null // For customizing chat screen appearance
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val content: String,
    val timestamp: Long,
    val isSentByMe: Boolean,
    val attachmentPath: String? = null, // Path or type of image/sticker/etc.
    val reactions: String? = null, // e.g. "👍,❤️,🔥" comma-separated or similar
    val isDisappearing: Boolean = false,
    val disappearDurationSec: Int? = null,
    val disappearTime: Long? = null, // UTC timestamp when it disappears
    val isVoice: Boolean = false,
    val voiceDurationSec: Int? = null,
    val translatedText: String? = null,
    val sentiment: String? = null
)
