package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.MainFeedScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val chatViewModel: ChatViewModel = viewModel()
      MyApplicationTheme(darkTheme = chatViewModel.isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = if (chatViewModel.isDarkTheme) Color(0xFF0F1219) else Color(0xFFF3F4F6)
        ) {
          val navController = rememberNavController()

          val startDest = if (chatViewModel.isUserRegistered) "feed" else "registration"

          NavHost(
            navController = navController,
            startDestination = startDest
          ) {
            composable("registration") {
              RegistrationScreen(
                viewModel = chatViewModel,
                onRegistrationSuccess = {
                  navController.navigate("feed") {
                    popUpTo("registration") { inclusive = true }
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }

            composable("feed") {
              MainFeedScreen(
                viewModel = chatViewModel,
                onNavigateToChat = { chatId ->
                  navController.navigate("conversation")
                },
                onNavigateToProfile = {
                  navController.navigate("profile")
                },
                modifier = Modifier.fillMaxSize()
              )
            }

            composable("conversation") {
              ConversationScreen(
                viewModel = chatViewModel,
                onNavigateBack = {
                  chatViewModel.selectChat(null)
                  navController.popBackStack()
                },
                modifier = Modifier.fillMaxSize()
              )
            }

            composable("profile") {
              UserProfileScreen(
                viewModel = chatViewModel,
                onNavigateBack = {
                  navController.popBackStack()
                },
                onLogout = {
                  navController.navigate("registration") {
                    popUpTo(0) { inclusive = true }
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }
  }
}
