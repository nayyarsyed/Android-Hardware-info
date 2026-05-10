package com.example.hardwareinfo.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hardwareinfo.ui.theme.RetroBlue
import com.example.hardwareinfo.ui.theme.RetroGreen
import kotlinx.coroutines.launch

data class SensorData(
    val sensor: Sensor,
    val values: FloatArray = floatArrayOf(),
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorData

        if (sensor != other.sensor) return false
        if (!values.contentEquals(other.values)) return false
        if (accuracy != other.accuracy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sensor.hashCode()
        result = 31 * result + values.contentHashCode()
        result = 31 * result + accuracy
        return result
    }
}

class SensorsViewModel : ViewModel(), SensorEventListener {
    private val _sensors = mutableStateListOf<SensorData>()
    val sensors: List<SensorData> = _sensors

    private val _selectedSensor = mutableStateOf<SensorData?>(null)
    val selectedSensor: State<SensorData?> = _selectedSensor

    private lateinit var sensorManager: SensorManager

    fun initialize(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        loadSensors()
    }

    private fun loadSensors() {
        val deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        _sensors.clear()
        deviceSensors.forEach { sensor ->
            _sensors.add(SensorData(sensor))
        }
    }

    fun selectSensor(sensorData: SensorData) {
        // Unregister previous sensor
        _selectedSensor.value?.let { oldSensor ->
            sensorManager.unregisterListener(this, oldSensor.sensor)
        }

        _selectedSensor.value = sensorData

        // Register new sensor
        sensorManager.registerListener(
            this,
            sensorData.sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentSensor = _selectedSensor.value ?: return
        val updatedSensor = currentSensor.copy(
            values = event.values.clone(),
            accuracy = event.accuracy
        )
        _selectedSensor.value = updatedSensor

        // Update in list
        val index = _sensors.indexOfFirst { it.sensor.type == currentSensor.sensor.type }
        if (index != -1) {
            _sensors[index] = updatedSensor
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val index = _sensors.indexOfFirst { it.sensor.type == sensor.type }
        if (index != -1) {
            _sensors[index] = _sensors[index].copy(accuracy = accuracy)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun SensorsScreen(viewModel: SensorsViewModel = viewModel()) {
    val context = LocalContext.current
    val sensors = viewModel.sensors
    val selectedSensor by viewModel.selectedSensor

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 150.dp, start = 15.dp, end = 15.dp)
    ) {
        Text(
            text = "SENSORS",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroGreen,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AVAILABLE SENSORS (${sensors.size})",
            style = MaterialTheme.typography.titleLarge,
            color = RetroBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sensors) { sensorData ->
                SensorCard(
                    sensorData = sensorData,
                    isSelected = selectedSensor?.sensor?.type == sensorData.sensor.type,
                    onClick = { viewModel.selectSensor(sensorData) }
                )
            }
        }
    }
}

@Composable
fun SensorCard(
    sensorData: SensorData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sensorData.sensor.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Type: ${getSensorTypeName(sensorData.sensor.type)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Vendor: ${sensorData.sensor.vendor}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Version: ${sensorData.sensor.version}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (sensorData.values.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Values: ${sensorData.values.joinToString(", ") { "%.2f".format(it) }}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RetroGreen
                )

                Text(
                    text = "Accuracy: ${getAccuracyName(sensorData.accuracy)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getSensorTypeName(type: Int): String {
    return when (type) {
        Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
        Sensor.TYPE_MAGNETIC_FIELD -> "Magnetic Field"
        Sensor.TYPE_ORIENTATION -> "Orientation"
        Sensor.TYPE_GYROSCOPE -> "Gyroscope"
        Sensor.TYPE_LIGHT -> "Light"
        Sensor.TYPE_PRESSURE -> "Pressure"
        Sensor.TYPE_TEMPERATURE -> "Temperature"
        Sensor.TYPE_PROXIMITY -> "Proximity"
        Sensor.TYPE_GRAVITY -> "Gravity"
        Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
        Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "Relative Humidity"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "Ambient Temperature"
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "Magnetic Field Uncalibrated"
        Sensor.TYPE_GAME_ROTATION_VECTOR -> "Game Rotation Vector"
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "Gyroscope Uncalibrated"
        Sensor.TYPE_SIGNIFICANT_MOTION -> "Significant Motion"
        Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
        Sensor.TYPE_STEP_COUNTER -> "Step Counter"
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "Geomagnetic Rotation Vector"
        Sensor.TYPE_HEART_RATE -> "Heart Rate"
        else -> "Unknown ($type)"
    }
}

fun getAccuracyName(accuracy: Int): String {
    return when (accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
        SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
        else -> "Unknown"
    }
}
