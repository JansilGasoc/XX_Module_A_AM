package edu.ws2024.aXX.am

import edu.ws2024.aXX.am.GameRecord
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun RankingsPage(
    navController: NavHostController,
    rankings: MutableList<GameRecord>,
    recentGame: GameRecord?
) {
    val coroutineScope = rememberCoroutineScope()

    // Add recent game if not in list
    LaunchedEffect(recentGame) {
        recentGame?.let {
            if (!rankings.contains(it)) {
                coroutineScope.launch {
                    rankings.add(it)
                }
            }
        }
    }

    val sortedList = rankings.sortedByDescending { it.duration }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            ) {
                Button(onClick = { navController.navigate("home") }, modifier = Modifier.defaultMinSize(55.dp)) {
                    Text("Back")
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "Rankings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Table header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ranking", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text("player name", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(2f))
            Text("coin", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text("duration", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Table rows
        LazyColumn {
            itemsIndexed(sortedList) { index, record ->
                RankingItem(index, record)
                Divider()
            }
        }
    }
}

@Composable
fun RankingItem(index: Int, record: GameRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${index + 1}", modifier = Modifier.weight(1f))
        Text(record.playerName, modifier = Modifier.weight(2f))
        Text("${record.coins}", modifier = Modifier.weight(1f))
        Text("${record.duration} s", modifier = Modifier.weight(1f))
    }
}