package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: ChatViewModel,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("Пользователь Karal Messenger") }
    var avatar by remember { mutableStateOf("avatar_geek") }
    var pinCode by remember { mutableStateOf("") }
    var showPinError by remember { mutableStateOf(false) }
    var showNameError by remember { mutableStateOf(false) }

    val isDark = viewModel.isDarkTheme
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDark) Color.Gray else Color(0xFF475569)
    val cardBg = if (isDark) Color(0xFF141822) else Color(0xFFFFFFFF)
    val inputBorderColor = if (isDark) Color(0xFF1F2430) else Color(0xFFD1D5DB)

    val backgroundBrush = remember(isDark) {
        val color1 = if (isDark) Color(0xFF0F1219) else Color(0xFFF3F4F6)
        val color2 = if (isDark) Color(0xFF161B26) else Color(0xFFE2E8F0)
        val color3 = if (isDark) Color(0xFF2A1C2B) else Color(0xFFF1F5F9)
        Brush.verticalGradient(
            colors = listOf(color1, color2, color3)
        )
    }

    val textInputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFE94057),
        unfocusedBorderColor = inputBorderColor,
        focusedLabelColor = Color(0xFFE94057),
        unfocusedLabelColor = subTextColor,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        errorBorderColor = Color.Red,
        errorLabelColor = Color.Red
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Logo Icon & Headline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057))
                            )
                        )
                ) {
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = "Karal Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Добро пожаловать в KARAL",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Новый стандарт безопасности и приватного общения",
                    color = subTextColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Interactive Selector Cards
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Header With Selected Avatar
                    AvatarView(avatar, size = 76.dp, isOnline = true)
                    
                    Text(
                        text = "Выберите ваш аватар-символ:",
                        color = subTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Avatar Selection Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avatars = listOf("avatar_geek", "avatar_art", "avatar_space", "avatar_neon")
                        avatars.forEach { av ->
                             val isSelected = avatar == av
                             Box(
                                 contentAlignment = Alignment.Center,
                                 modifier = Modifier
                                     .size(54.dp)
                                     .clip(CircleShape)
                                     .background(if (isSelected) inputBorderColor else Color.Transparent)
                                     .clickable { avatar = av }
                             ) {
                                 AvatarView(av, size = 44.dp)
                                 if (isSelected) {
                                     Box(
                                         modifier = Modifier
                                             .fillMaxSize()
                                             .background(Color(0x40E94057), CircleShape)
                                     )
                                 }
                             }
                        }
                    }

                    HorizontalDivider(color = inputBorderColor)

                    // Username Input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (it.isNotBlank()) showNameError = false
                        },
                        label = { Text("Имя или Никнейм") },
                        singleLine = true,
                        isError = showNameError,
                        placeholder = { Text("например, Александр") },
                        colors = textInputFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input")
                    )

                    if (showNameError) {
                        Text(
                            text = "Пожалуйста, введите ваше имя",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    // User Biography Input
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Описание (Статус / О себе)") },
                        singleLine = true,
                        placeholder = { Text("Пару слов о себе...") },
                        colors = textInputFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Passcode Input
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinCode = it
                                showPinError = false
                            }
                        },
                        label = { Text("PIN-код безопасности (4 цифры)") },
                        singleLine = true,
                        isError = showPinError,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        placeholder = { Text("****") },
                        colors = textInputFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showPinError) {
                        Text(
                            text = "PIN-код должен содержать ровно 4 цифры",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val finalName = name.trim()
                    val finalPin = pinCode.trim()
                    
                    if (finalName.isEmpty()) {
                        showNameError = true
                    }
                    if (finalPin.length != 4) {
                        showPinError = true
                    }
                    
                    if (finalName.isNotEmpty() && finalPin.length == 4) {
                        viewModel.registerUser(finalName, bio, avatar, finalPin)
                        onRegistrationSuccess()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_registration_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF8A2387), Color(0xFFE94057))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = "Вход")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Создать профиль",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
