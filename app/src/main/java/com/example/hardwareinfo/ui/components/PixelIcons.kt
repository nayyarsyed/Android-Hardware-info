package com.example.hardwareinfo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hardwareinfo.R
import com.example.hardwareinfo.ui.theme.RetroGreen

/**
 * Loads an 8-bit icon from the assets folder
 */
@Composable
fun PixelAssetIcon(fileName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(fileName) {
        try {
            context.assets.open(fileName).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
        )
    }
}

/**
 * 8-bit pixel art icon for Device
 */
@Composable
fun DeviceIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        PixelAssetIcon("8bit_device_icon.jpg", modifier = modifier.size(150.dp))
    }
}

/**
 * 8-bit pixel art icon for RAM
 */
@Composable
fun RamIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        PixelAssetIcon("8bit_RAM_icon.png", modifier = modifier.size(150.dp))
    }
}

/**
 * 8-bit pixel art icon for CPU
 */
@Composable
fun CpuIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        PixelAssetIcon("8bit_CPU_icon.png", modifier = modifier.size(150.dp))
    }
}

/**
 * 8-bit pixel art icon for Battery
 */
@Composable
fun BatteryIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        // Battery outline
        PixelAssetIcon("8bit_Battery_icon.png", modifier = modifier.size(150.dp))
    }
}

/**
 * 8-bit pixel art icon for Sensors
 */
@Composable
fun SensorsIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        // Sensor waves representation
       //  Box(modifier = Modifier.size(20.dp).background(color))
        PixelAssetIcon("8bit_sensor_icon.jpg", modifier = modifier.size(150.dp))

    }
}

/**
 * 8-bit pixel art icon for Camera
 */
@Composable
fun CameraIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        PixelAssetIcon("8bitcamera_icon.png", modifier = modifier.size(150.dp))
    }
}

/**
 * 8-bit pixel art icon for GPS
 */
@Composable
fun GpsIcon(modifier: Modifier = Modifier, color: Color = RetroGreen) {
    Box(modifier = modifier.size(150.dp)) {
        PixelAssetIcon("8bit_GPS_icon.png", modifier = modifier.size(150.dp))
    }
}
