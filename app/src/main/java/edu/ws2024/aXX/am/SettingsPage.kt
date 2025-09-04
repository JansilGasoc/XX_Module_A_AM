package edu.ws2024.aXX.am

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SettingsPage(
    navController: NavHostController,
    currentColor: Color,
    onColorSelected: (Color) -> Unit
) {
    // Represent jacket color as hue (0–360)
    var hue by remember { mutableFloatStateOf(0f) }

    // Convert hue → Color
    val selectedColor = Color.hsv(hue, 0.9f, 0.9f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 👇 Skier preview stacked in a Box
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Base skier (not tinted)
            Image(
                painter = painterResource(id = R.drawable.skiing_person_base),
                contentDescription = "Skier Base",
                modifier = Modifier.fillMaxSize()
            )

            // Jacket (tinted)
            Image(
                painter = painterResource(id = R.drawable.skiing_person_jacket),
                contentDescription = "Jacket",
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(
                    selectedColor,
                    blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                )
            )
        }

        Spacer(Modifier.height(48.dp))

        // Slider to adjust jacket color
        Slider(
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f,
            steps = 360,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(Modifier.height(32.dp))

        // Done button
        Button(
            onClick = {
                onColorSelected(selectedColor)
                navController.navigate("home")
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(60.dp)
        ) {
            Text("Done")
        }
    }
}