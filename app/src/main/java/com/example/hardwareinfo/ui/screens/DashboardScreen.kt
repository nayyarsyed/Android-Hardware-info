package com.example.hardwareinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hardwareinfo.ui.components.BatteryIcon
import com.example.hardwareinfo.ui.components.CameraIcon
import com.example.hardwareinfo.ui.components.CpuIcon
import com.example.hardwareinfo.ui.components.DeviceIcon
import com.example.hardwareinfo.ui.components.GpsIcon
import com.example.hardwareinfo.ui.components.SensorsIcon

import com.example.hardwareinfo.ui.theme.RetroBlue
import com.example.hardwareinfo.ui.theme.RetroCyan
import com.example.hardwareinfo.ui.theme.RetroGreen
import com.example.hardwareinfo.ui.theme.RetroYellow

@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 120.dp, bottom = 100.dp, start = 10.dp, end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HARDWARE INFO",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroGreen
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardCard(
                    title = "DEVICE",
                    icon = { DeviceIcon() },
                    color = RetroBlue,
                    onClick = { navController.navigate("device") }
                )
            }
//            item {
//                DashboardCard(
//                    title = "RAM",
//                    icon = { RamIcon() },
//                    color = RetroBlue,
//                    onClick = { /* Navigate to RAM screen */ }
//                )
//            }
            item {
                DashboardCard(
                    title = "CPU",
                    icon = { CpuIcon() },
                    color = RetroGreen,
                    onClick = { navController.navigate("cpu") }
                )
            }
            item {
                DashboardCard(
                    title = "BATTERY",
                    icon = { BatteryIcon(color = RetroYellow) },
                    color = RetroCyan,
                    onClick = { navController.navigate("battery") }
                )
            }
            item {
                DashboardCard(
                    title = "SENSORS",
                    icon = { SensorsIcon() },
                    color = RetroBlue,
                    onClick = { navController.navigate("sensors") }
                )
            }
            item {
                DashboardCard(
                    title = "CAMERA",
                    icon = { CameraIcon() },
                    color = RetroGreen,
                    onClick = { navController.navigate("camera") }
                )
            }
            item {
                DashboardCard(
                    title = "GPS",
                    icon = { GpsIcon() },
                    color = RetroYellow,
                    onClick = { navController.navigate("gps") }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: @Composable () -> Unit,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
