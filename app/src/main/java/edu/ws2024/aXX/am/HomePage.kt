package edu.ws2024.aXX.am
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomePage(navController: NavHostController) {
    var playerName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.4f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Go Skiing", style = MaterialTheme.typography.headlineLarge, color = Color.Black)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Player name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (playerName.isBlank()) {
                        Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate("game/$playerName")
                    }
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                ),
                shape = RectangleShape
            ) { Text("Start Game") }

            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { navController.navigate("rankings") },  modifier = Modifier
                .width(150.dp)
                .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                ),
                shape = RectangleShape) {
                Text("Rankings")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { navController.navigate("settings") },  modifier = Modifier
                .width(150.dp)
                .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                ),
                shape = RectangleShape) {
                Text("Settings")
            }
        }
    }
}