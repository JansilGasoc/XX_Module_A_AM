package edu.ws2024.aXX.am

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.ws2024.aXX.am.HomePage

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GoSkiingApp() {
    val navController = rememberNavController()
    val rankings = remember { mutableStateListOf<GameRecord>() }
    var skierJacketColor by remember { mutableStateOf(Color.Red) }

    NavHost(navController, startDestination = "home") {
        composable("home") { HomePage(navController) }
        composable("game/{playerName}") { backStackEntry ->
            val playerName = backStackEntry.arguments?.getString("playerName") ?: "Player"
            GamePage(navController, playerName, skierJacketColor)
        }
        composable("rankings") {
            val recentGame = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<GameRecord>("recentGame")
            RankingsPage(navController, rankings, recentGame)
        }
        composable("settings") {
            SettingsPage(navController, skierJacketColor) { color -> skierJacketColor = color }
        }
    }
}