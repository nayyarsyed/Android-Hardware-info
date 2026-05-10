package com.example.hardwareinfo.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hardwareinfo.ui.theme.RetroGreen

@Composable
fun DeviceScreen() {
    val context = LocalContext.current

    val deviceInfo = listOf(
        "Model" to Build.MODEL,
        "Brand" to Build.BRAND,
        "Manufacturer" to Build.MANUFACTURER,
        "Device" to Build.DEVICE,
        "Product" to Build.PRODUCT,
        "Hardware" to Build.HARDWARE,
        "Board" to Build.BOARD,
        "Bootloader" to Build.BOOTLOADER,
        "Display" to Build.DISPLAY,
        "Fingerprint" to Build.FINGERPRINT,
        "Host" to Build.HOST,
        "ID" to Build.ID,
        "Tags" to Build.TAGS,
        "Type" to Build.TYPE,
        "User" to Build.USER,
        "Android Version" to Build.VERSION.RELEASE,
        "API Level" to Build.VERSION.SDK_INT.toString(),
        "Security Patch" to Build.VERSION.SECURITY_PATCH,
        "Codename" to Build.VERSION.CODENAME
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 150.dp, start = 15.dp, end = 15.dp)
    ) {
        Text(
            text = "DEVICE INFO",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroGreen,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(deviceInfo.size) { index ->
                val (label, value) = deviceInfo[index]
                InfoRow(label = label, value = value)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
