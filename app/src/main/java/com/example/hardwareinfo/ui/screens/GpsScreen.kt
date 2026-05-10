package com.example.hardwareinfo.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.hardwareinfo.ui.theme.RetroBlue
import com.example.hardwareinfo.ui.theme.RetroGreen
import com.example.hardwareinfo.ui.theme.RetroRed

@Composable
fun GpsScreen() {
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationProviders = remember {
        if (hasLocationPermission) {
            try {
                locationManager.allProviders.map { provider ->
                    val locationProvider = locationManager.getProvider(provider)
                    LocationProviderInfo(
                        name = provider,
                        requiresNetwork = locationProvider?.requiresNetwork() ?: false,
                        requiresSatellite = locationProvider?.requiresSatellite() ?: false,
                        requiresCell = locationProvider?.requiresCell() ?: false,
                        hasMonetaryCost = locationProvider?.hasMonetaryCost() ?: false,
                        supportsAltitude = locationProvider?.supportsAltitude() ?: false,
                        supportsSpeed = locationProvider?.supportsSpeed() ?: false,
                        supportsBearing = locationProvider?.supportsBearing() ?: false,
                        powerRequirement = locationProvider?.powerRequirement ?: -1,
                        accuracy = locationProvider?.accuracy ?: -1,
                        isEnabled = locationManager.isProviderEnabled(provider)
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 150.dp, start = 15.dp, end = 15.dp)
    ) {
        Text(
            text = "GPS INFO",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroGreen,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasLocationPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RetroRed.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LOCATION PERMISSION REQUIRED",
                        style = MaterialTheme.typography.titleMedium,
                        color = RetroRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please grant location permission to view GPS information.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Text(
                text = "LOCATION PROVIDERS (${locationProviders.size})",
                style = MaterialTheme.typography.titleLarge,
                color = RetroBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(locationProviders.size) { index ->
                    val provider = locationProviders[index]
                    LocationProviderCard(provider = provider)
                }
            }
        }
    }
}

data class LocationProviderInfo(
    val name: String,
    val requiresNetwork: Boolean,
    val requiresSatellite: Boolean,
    val requiresCell: Boolean,
    val hasMonetaryCost: Boolean,
    val supportsAltitude: Boolean,
    val supportsSpeed: Boolean,
    val supportsBearing: Boolean,
    val powerRequirement: Int,
    val accuracy: Int,
    val isEnabled: Boolean
)

@Composable
fun LocationProviderCard(provider: LocationProviderInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (provider.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                RetroRed.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = provider.name.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (provider.isEnabled) RetroGreen else RetroRed
                )

                Text(
                    text = if (provider.isEnabled) "ENABLED" else "DISABLED",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (provider.isEnabled) RetroGreen else RetroRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(label = "Requires Network", value = if (provider.requiresNetwork) "Yes" else "No")
            InfoRow(label = "Requires Satellite", value = if (provider.requiresSatellite) "Yes" else "No")
            InfoRow(label = "Requires Cell", value = if (provider.requiresCell) "Yes" else "No")
            InfoRow(label = "Monetary Cost", value = if (provider.hasMonetaryCost) "Yes" else "No")
            InfoRow(label = "Supports Altitude", value = if (provider.supportsAltitude) "Yes" else "No")
            InfoRow(label = "Supports Speed", value = if (provider.supportsSpeed) "Yes" else "No")
            InfoRow(label = "Supports Bearing", value = if (provider.supportsBearing) "Yes" else "No")

            val powerReqText = when (provider.powerRequirement) {
                0 -> "No requirement"
                1 -> "Low power"
                2 -> "Medium power"
                3 -> "High power"
                else -> "Unknown"
            }
            InfoRow(label = "Power Requirement", value = powerReqText)

            val accuracyText = when (provider.accuracy) {
                1 -> "Fine"
                2 -> "Coarse"
                else -> "Unknown"
            }
            InfoRow(label = "Accuracy", value = accuracyText)
        }
    }
}
