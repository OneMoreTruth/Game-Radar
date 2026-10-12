package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.notification.NotificationHelper
import com.example.ui.navigation.AppNavigation
import com.example.ui.navigation.Screen
import com.example.ui.theme.GamerBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Android notification channels for price drops and releases
        NotificationHelper.initNotificationChannels(this)

        val targetGameId = intent?.getStringExtra("target_game_id")

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GamerBg),
                    color = GamerBg
                ) {
                    val navController = rememberNavController()
                    val viewModel: GameViewModel = viewModel()

                    LaunchedEffect(targetGameId) {
                        if (!targetGameId.isNullOrBlank()) {
                            navController.navigate(Screen.GameDetail.createRoute(targetGameId))
                        }
                    }

                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
