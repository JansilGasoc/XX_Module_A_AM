package edu.ws2024.aXX.am

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset

class GameState {
    var coins by mutableStateOf(10)
    var duration by mutableStateOf(0)
    var isPaused by mutableStateOf(false)
    var showQuitDialog by mutableStateOf(false)
    var showGameOver by mutableStateOf(false)
    var invincible by mutableStateOf(false)
    var slopeAngle by mutableStateOf(12f)
    var speedMultiplier by mutableStateOf(1f)
    var treeOffsetX by mutableStateOf(0f)
    var skierJumpY by mutableStateOf(0f)
    var jumping by mutableStateOf(false)

    val obstacles = mutableStateListOf<Offset>()
    val coinsList = mutableStateListOf<Offset>()

    fun reset() {
        coins = 10
        duration = 0
        isPaused = false
        showQuitDialog = false
        showGameOver = false
        invincible = false
        slopeAngle = 12f
        speedMultiplier = 1f
        treeOffsetX = 0f
        skierJumpY = 0f
        jumping = false
        obstacles.clear()
        coinsList.clear()
    }
}