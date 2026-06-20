package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkTheme
    val bgColor = if (isDark) Color(0xFF0F1219) else Color(0xFFF3F4F6)
    val cardBg = if (isDark) Color(0xFF141822) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color.Gray else Color(0xFF475569)
    val labelTextColor = if (isDark) Color.LightGray else Color(0xFF334155)
    val inputBorderColor = if (isDark) Color(0xFF1F2430) else Color(0xFFD1D5DB)
    val textInputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFE94057),
        unfocusedBorderColor = inputBorderColor,
        focusedLabelColor = Color(0xFFE94057),
        unfocusedLabelColor = subTextColor,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor
    )

    val isConfigured = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    val aiCardBg = if (isDark) { if (isConfigured) Color(0xFF03221E) else Color(0xFF261F12) } else { if (isConfigured) Color(0xFFDCFCE7) else Color(0xFFFEF3C7) }
    val aiTextColor = if (isDark) { if (isConfigured) Color(0xFF34D399) else Color(0xFFFBBF24) } else { if (isConfigured) Color(0xFF15803D) else Color(0xFFB45309) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой Профиль 👤", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor
                )
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Sleek Avatar Selector Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarView(viewModel.myAvatar, size = 80.dp, isOnline = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.myName,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "@" + viewModel.myName.lowercase() + "_karal",
                            color = subTextColor,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Avatar Pick Selector Pills
                        Text(
                            text = "Выберите аватар-символ:",
                            color = labelTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val avatars = listOf("avatar_geek", "avatar_art", "avatar_space", "avatar_neon")
                            avatars.forEach { av ->
                                val isSelected = viewModel.myAvatar == av
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) (if (isDark) Color(0xFF1E2433) else Color(0xFFE2E8F0)) else Color.Transparent)
                                        .clickable {
                                            viewModel.myAvatar = av
                                        }
                                ) {
                                    AvatarView(av, size = 36.dp)
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0x33E94057), CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Profile Details Inputs
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Личные данные",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        OutlinedTextField(
                            value = viewModel.myName,
                            onValueChange = { viewModel.myName = it },
                            label = { Text("Имя") },
                            singleLine = true,
                            colors = textInputFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input")
                        )

                        OutlinedTextField(
                            value = viewModel.myBio,
                            onValueChange = { viewModel.myBio = it },
                            label = { Text("Описание (О себе)") },
                            maxLines = 3,
                            colors = textInputFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Smart Key Integration Panel (Gemini API Configuration Details)
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = aiCardBg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConfigured) "Интеграция ИИ: Активна 🟢" else "Интеграция ИИ: Демо-режим 🟡",
                                color = aiTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = if (isConfigured) {
                                "Реальная модель gemini-3.5-flash подключена! ИИ-ассистент Karal AI готов к чату, трансляции мыслей и экспресс-анализу чувств."
                            } else {
                                "Вы используете умную оффлайн-симуляцию. Настройте ваш GEMINI_API_KEY в панели секретов (Secrets Panel) AI Studio для реальных запросов."
                            },
                            color = if (isDark) Color.LightGray else Color(0xFF334155),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 4. Stats dashboard row
            item {
                StatCard(
                    title = "Диалогов",
                    value = "4 активных",
                    icon = Icons.Default.ChatBubbleOutline,
                    color = Color(0xFF3B82F6),
                    containerColor = cardBg,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4b. Theme Preference settings Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                                contentDescription = "Тема",
                                tint = Color(0xFFE94057),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Тема оформления",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isDark) "Темная тема" else "Светлая тема",
                                    color = subTextColor,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFE94057),
                                checkedTrackColor = Color(0xFFE94057).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            ),
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }
                }
            }

            // 4a. Security Lock Details Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C1318) else Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Защита", tint = Color(0xFFE94057), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Безопасность сессии", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "Ваши сообщения шифруются локально. Текущий PIN-код авторизации: ${viewModel.myPinCode}.",
                            color = if (isDark) Color.LightGray else Color(0xFF7F1D1D),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.logoutUser()
                                onLogout()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Выйти")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Выйти из профиля", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. Back navigation bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94057)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить и вернуться", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = subTextColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
