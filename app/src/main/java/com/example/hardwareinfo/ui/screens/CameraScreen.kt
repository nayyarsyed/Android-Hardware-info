package com.example.hardwareinfo.ui.screens

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hardwareinfo.ui.theme.RetroBlue
import com.example.hardwareinfo.ui.theme.RetroGreen

data class CameraInfo(
    val id: String,
    val facing: String,
    val sensorSize: String,
    val focalLengths: String,
    val supportedSizes: String,
    val flashSupported: Boolean,
    val autoFocusSupported: Boolean,
    val videoStabilizationSupported: Boolean,
    val opticalStabilizationSupported: Boolean
)

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameras = remember {
        try {
            cameraManager.cameraIdList.map { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)

                val facing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }

                val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.let {
                    "${it.width} x ${it.height} mm"
                } ?: "Unknown"

                val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.joinToString(", ") { "%.1f".format(it) } ?: "Unknown"

                val supportedSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { map ->
                    val outputSizes = map.getOutputSizes(android.graphics.ImageFormat.JPEG)
                    outputSizes?.take(5)?.joinToString(", ") { "${it.width}x${it.height}" } ?: "Unknown"
                } ?: "Unknown"

                val flashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val autoFocusSupported = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.contains(CameraCharacteristics.CONTROL_AF_MODE_AUTO) ?: false
                val videoStabilizationSupported = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)?.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON) ?: false
                val opticalStabilizationSupported = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) ?: false

                CameraInfo(
                    id = cameraId,
                    facing = facing,
                    sensorSize = sensorSize,
                    focalLengths = focalLengths,
                    supportedSizes = supportedSizes,
                    flashSupported = flashSupported,
                    autoFocusSupported = autoFocusSupported,
                    videoStabilizationSupported = videoStabilizationSupported,
                    opticalStabilizationSupported = opticalStabilizationSupported
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 150.dp, start = 15.dp, end = 15.dp)
    ) {
        Text(
            text = "CAMERA INFO",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroGreen,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CAMERAS (${cameras.size})",
            style = MaterialTheme.typography.titleLarge,
            color = RetroBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cameras) { camera ->
                CameraCard(camera = camera)
            }
        }
    }
}

@Composable
fun CameraCard(camera: CameraInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Camera ${camera.id} (${camera.facing})",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(label = "Sensor Size", value = camera.sensorSize)
            InfoRow(label = "Focal Lengths", value = camera.focalLengths)
            InfoRow(label = "Supported Sizes", value = camera.supportedSizes)
            InfoRow(label = "Flash", value = if (camera.flashSupported) "Yes" else "No")
            InfoRow(label = "Auto Focus", value = if (camera.autoFocusSupported) "Yes" else "No")
            InfoRow(label = "Video Stabilization", value = if (camera.videoStabilizationSupported) "Yes" else "No")
            InfoRow(label = "Optical Stabilization", value = if (camera.opticalStabilizationSupported) "Yes" else "No")
        }
    }
}
