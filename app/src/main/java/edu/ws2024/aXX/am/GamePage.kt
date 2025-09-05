package edu.ws2024.aXX.am

import android.content.Context
import edu.ws2024.aXX.am.AudioManager
import edu.ws2024.aXX.am.GameState
import edu.ws2024.aXX.am.VibrationManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GamePage(navController: NavHostController, playerName: String, jacketColor: Color) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val coroutineScope = rememberCoroutineScope()

    // Managers
    val audioManager = remember { AudioManager(context) }
    val vibrationManager = remember { VibrationManager(context) }
    val gameState = remember { GameState() }

    // Game constants
    val defaultSlopeAngle = 15f
    val skierWidth = with(density) { 120.dp.toPx() }
    val skierHeight = with(density) { 140.dp.toPx() }
    val obstacleSpeed = 420f
    val coinSpeed = 360f
    val treeSpeed = 340f
    val slopeHeightDp = 100.dp
    val slopeHeightPx = with(density) { slopeHeightDp.toPx() }
    val slopeBaseY = screenHeightPx - slopeHeightPx / 2f
    val skierBaseX = screenWidthPx / 2f

    // Start music and vibration
    LaunchedEffect(Unit) @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE) {
        vibrationManager.vibrate(180L)
        audioManager.startBgm()
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose { audioManager.release() }
    }

    // Gyroscope sensor for tilt controls
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    }

    DisposableEffect(sensorManager, gameState.isPaused) {
        val accel = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                if (gameState.isPaused) return
                val x = event.values[0]

                if (x > 2) {
                    gameState.slopeAngle = (gameState.slopeAngle - 0.6f).coerceAtLeast(0f)
                } else if (x < -2) {
                    gameState.slopeAngle = (gameState.slopeAngle + 0.6f).coerceAtMost(defaultSlopeAngle)
                }

                val factor = (gameState.slopeAngle / defaultSlopeAngle).coerceIn(0f, 1f)
                gameState.speedMultiplier = factor
                audioManager.setBgmVolume(factor, factor)
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accel, android.hardware.SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Duration timer and invincibility countdown
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (!gameState.isPaused && !gameState.showGameOver) {
                gameState.duration++
                if (gameState.invincible) {
                    if (gameState.coins > 0) gameState.coins-- else gameState.invincible = false
                }
            }
        }
    }

    // Spawn entities
    LaunchedEffect(Unit) {
        val frameMs = 15L
        var spawnTimer = 0L
        var nextSpawnDelay = Random.nextLong(1500, 2500)

        while (true) {
            if (!gameState.isPaused && !gameState.showGameOver) {
                spawnTimer += frameMs

                if (spawnTimer >= nextSpawnDelay) {
                    gameState.obstacles.add(Offset(screenWidthPx + 50f, slopeBaseY - 60f))
                    spawnTimer = 0L
                    nextSpawnDelay = Random.nextLong(1500, 2500)
                }

                if (Random.nextInt(0, 1000) < 8) {
                    gameState.coinsList.add(Offset(screenWidthPx + 50f, slopeBaseY - 40f))
                }
            }
            delay(frameMs)
        }
    }

    // Game loop for movement and collision detection
    LaunchedEffect(Unit) {
        val frameMs = 16L

        while (true) {
            if (!gameState.isPaused && !gameState.showGameOver) {
                val dt = frameMs / 900f

                // Tree movement
                gameState.treeOffsetX -= treeSpeed * gameState.speedMultiplier * dt
                if (gameState.treeOffsetX <= -screenWidthPx) gameState.treeOffsetX += screenWidthPx

                // Obstacles & coins movement
                gameState.obstacles.forEachIndexed { i, o ->
                    gameState.obstacles[i] = Offset(o.x - obstacleSpeed * gameState.speedMultiplier * dt, o.y)
                }
                gameState.coinsList.forEachIndexed { i, c ->
                    gameState.coinsList[i] = Offset(c.x - coinSpeed * gameState.speedMultiplier * dt, c.y)
                }

                // Remove off-screen entities
                gameState.obstacles.removeAll { it.x < -100 }
                gameState.coinsList.removeAll { it.x < -100 }

                // Collision detection
                val skierX = skierBaseX
                val skierY = slopeBaseY + gameState.skierJumpY

                // Coin collection
                gameState.coinsList.filter {
                    abs(it.x - skierX) < 64 && abs(it.y - skierY) < 64
                }.forEach {
                    gameState.coinsList.remove(it)
                    audioManager.playCoin()
                    gameState.coins++
                }

                // Obstacle collision
                val hitObstacle = gameState.obstacles.any {
                    abs(it.x - skierX) < 64 && abs(it.y - skierY) < 64
                }

                if (hitObstacle && !gameState.invincible) {
                    audioManager.playGameOver()
                    vibrationManager.vibrate(300)
                    gameState.isPaused = true
                    audioManager.pauseBgm()
                    gameState.showGameOver = true
                }
            }
            delay(frameMs)
        }
    }

    // Jump function
    fun doJump() {
        if (gameState.jumping || gameState.isPaused) return
        gameState.jumping = true

        coroutineScope.launch {
            audioManager.playJump()
            val steps = 26
            val peak = 280f
            val totalDuration = 780L
            val frame = (totalDuration / steps).coerceAtLeast(8L)

            for (i in 0..steps) {
                val t = i / steps.toFloat()
                gameState.skierJumpY = -sin(t * Math.PI).toFloat() * peak
                delay(frame)
            }

            gameState.skierJumpY = 0f
            gameState.jumping = false
        }
    }

    // Gesture handling
    var dragStart by remember { mutableStateOf<Offset?>(null) }

    val gestureModifier = Modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    if (!gameState.isPaused && !gameState.showGameOver) doJump()
                },
                onLongPress = {
                    if (!gameState.isPaused && gameState.coins > 0) {
                        gameState.invincible = true
                        gameState.coins--
                    }
                }
            )
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { dragStart = it },
                onDragEnd = { dragStart = null },
                onDragCancel = { dragStart = null }
            ) { change, _ ->
                if (gameState.isPaused) return@detectDragGestures
                val start = dragStart ?: return@detectDragGestures

                val dx = change.position.x - start.x
                val dy = change.position.y - start.y

                if (dy > 120f && abs(dx) < 150f) {
                    // Boost
                    gameState.speedMultiplier = 2.6f
                    coroutineScope.launch {
                        delay(380L)
                        gameState.speedMultiplier =
                            (gameState.slopeAngle / defaultSlopeAngle).coerceIn(0f, 1f)
                    }
                    dragStart = null
                }

                if (dx > 160f && abs(dy) < 120f) {
                    // Quit
                    gameState.isPaused = true
                    try {
                        audioManager.pauseBgm()
                    } catch (_: Exception) {
                    }
                    gameState.showQuitDialog = true
                    dragStart = null
                }
            }
        }

    // UI
    Box(modifier = Modifier
        .fillMaxSize()
        .then(gestureModifier)) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Trees
        val treeDpHeight = 400.dp
        val treeDpWidth = 600.dp

        Image(
            painter = painterResource(id = R.drawable.trees),
            contentDescription = null,
            modifier = Modifier
                .offset { IntOffset((gameState.treeOffsetX / 2f).toInt(), 0) }
                .width(treeDpWidth)
                .height(treeDpHeight)
                .align(Alignment.BottomStart),
            contentScale = ContentScale.FillBounds
        )

        Image(
            painter = painterResource(id = R.drawable.trees),
            contentDescription = null,
            modifier = Modifier
                .offset { IntOffset(((gameState.treeOffsetX + screenWidthPx) / 2f).toInt(), 0) }
                .width(treeDpWidth)
                .height(treeDpHeight)
                .align(Alignment.BottomStart),
            contentScale = ContentScale.FillBounds
        )
        Image(
            painter = painterResource(id = R.drawable.trees),
            contentDescription = null,
            modifier = Modifier
                .offset { IntOffset(((gameState.treeOffsetX + screenWidthPx) / 2f).toInt(), 0) }
                .width(treeDpWidth)
                .height(treeDpHeight)
                .align(Alignment.BottomStart),
            contentScale = ContentScale.FillBounds
        )

        // Slope (Trapezoid Shape)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(slopeHeightDp)
                .align(Alignment.BottomCenter)
        ) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 70f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(path = path, color = Color.White)
        }

        // Obstacles
        gameState.obstacles.forEach { o ->
            Image(
                painter = painterResource(id = R.drawable.obstacle),
                contentDescription = "Obstacle",
                modifier = Modifier
                    .offset { IntOffset(o.x.toInt(), o.y.toInt()) }
                    .size(54.dp)
                    .padding(bottom = 20.dp)
            )
        }

        // Coins
        gameState.coinsList.forEach { c ->
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = "Coin",
                modifier = Modifier

                    .offset { IntOffset(c.x.toInt(), c.y.toInt()) }
                    .size(48.dp)
                    .padding(bottom = 20.dp)
            )
        }

        // Skier
        Box(
            modifier = Modifier
                .padding(bottom = 40.dp)
                .offset {
                    IntOffset(
                        (skierBaseX - skierWidth / 2f).toInt(),
                        (slopeBaseY + gameState.skierJumpY - skierHeight / 2f).toInt()
                    )
                }
                .size(120.dp)

        ) {
            // Base skier (no tint)
            Image(
                painter = painterResource(id = R.drawable.skiing_person_base),
                contentDescription = "Skier Base",
                modifier = Modifier.fillMaxSize().
                padding(bottom= 20.dp)
            )

            // Jacket only (tinted)
            Image(
                painter = painterResource(id = R.drawable.skiing_person_jacket),
                contentDescription = "Jacket",
                modifier = Modifier.fillMaxSize().padding(bottom = 20.dp),

                colorFilter = ColorFilter.tint(
                    jacketColor,
                    blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                )
            )
        }

        // HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 20.dp)
                .padding(10.dp)
        ) {
            Text(text = playerName, color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = "coin",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${gameState.coins}", color = Color.Black)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "${gameState.duration} s", color = Color.Black)
            if (gameState.invincible) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Invincibility Mode", color = Color.Red)
            }
        }
        var showPauseDialog by remember { mutableStateOf(false) }
        // Pause/Play button
        FloatingActionButton(
            onClick = {
                gameState.isPaused = !gameState.isPaused
                if (gameState.isPaused) {
                    showPauseDialog = true
                    try { audioManager.pauseBgm() } catch (_: Exception) {}
                } else {
                    try { audioManager.startBgm() } catch (_: Exception) {}
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .padding(top = 20.dp)
                .background(color = Color.Transparent)
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = if (gameState.isPaused) R.drawable.play else R.drawable.pause),
                contentDescription = "pause/play"
            )
        }
        // Quit dialog
        if (gameState.isPaused && !gameState.showGameOver && !gameState.showQuitDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Game Suspended...",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(Color.Transparent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(26.dp)
                )
            }
        }
        // Game Over dialog
        if (gameState.showGameOver) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Game Over") },
                text = {
                    Column {
                        Text("Player: $playerName")
                        Text("Coins: ${gameState.coins}")
                        Text("Duration: ${gameState.duration} s")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        gameState.reset()
                        try {
                            audioManager.seekBgmToStart()
                            audioManager.startBgm()
                        } catch (_: Exception) {}
                    }) { Text("Restart") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val game = edu.ws2024.aXX.am.GameRecord(
                            id = Random.nextInt(),
                            playerName = playerName,
                            coins = gameState.coins,
                            duration = gameState.duration
                        )

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("recentGame", game)

                        navController.navigate("rankings")
                    }) { Text("Go To Rankings") }
                }
            )
        }
    }
}