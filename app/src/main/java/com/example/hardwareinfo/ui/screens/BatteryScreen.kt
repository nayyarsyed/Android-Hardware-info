package com.example.hardwareinfo.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hardwareinfo.ui.theme.RetroGreen
import com.example.hardwareinfo.ui.theme.RetroRed
import com.example.hardwareinfo.ui.theme.RetroYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class BatteryViewModel : ViewModel() {
    private val _batteryLevel = mutableStateOf(0)
    val batteryLevel: State<Int> = _batteryLevel

    private val _isCharging = mutableStateOf(false)
    val isCharging: State<Boolean> = _isCharging

    private val _batteryHistory = mutableStateListOf<Int>()
    val batteryHistory: List<Int> = _batteryHistory

    private val _temperature = mutableStateOf(0)
    val temperature: State<Int> = _temperature

    private val _voltage = mutableStateOf(0)
    val voltage: State<Int> = _voltage

    private val _technology = mutableStateOf("")
    val technology: State<String> = _technology

    private val _status = mutableStateOf("")
    val status: State<String> = _status

    private val _health = mutableStateOf("")
    val health: State<String> = _health

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                // Battery history is updated via broadcast receiver
                delay(30000) // Update every 30 seconds for history
            }
        }
    }

    fun updateBatteryInfo(context: Context) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (scale > 0) (level * 100 / scale) else 0

            _batteryLevel.value = batteryPct
            _isCharging.value = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
            _temperature.value = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10
            _voltage.value = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            _technology.value = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

            val statusInt = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            _status.value = when (statusInt) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Unknown"
            }

            val healthInt = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            _health.value = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }

            // Add to history if changed significantly
            if (_batteryHistory.isEmpty() || kotlin.math.abs(_batteryHistory.last() - batteryPct) >= 5) {
                _batteryHistory.add(batteryPct)
                if (_batteryHistory.size > 20) {
                    _batteryHistory.removeAt(0)
                }
            }
        }
    }
}

@Composable
fun BatteryScreen(viewModel: BatteryViewModel = viewModel()) {
    val context = LocalContext.current
    val batteryLevel by viewModel.batteryLevel
    val isCharging by viewModel.isCharging
    val batteryHistory = viewModel.batteryHistory
    val temperature by viewModel.temperature
    val voltage by viewModel.voltage
    val technology by viewModel.technology
    val status by viewModel.status
    val health by viewModel.health

    // Update battery info when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.updateBatteryInfo(context)
    }

    val batteryInfo = listOf(
        "Level" to "$batteryLevel%",
        "Status" to status,
        "Charging" to if (isCharging) "Yes" else "No",
        "Temperature" to "${temperature}°C",
        "Voltage" to "${voltage}mV",
        "Technology" to technology,
        "Health" to health
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 150.dp, start = 15.dp, end = 15.dp)
    ) {
        Text(
            text = "BATTERY INFO",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroGreen,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Level Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BATTERY: ${batteryLevel}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = when {
                        batteryLevel < 20 -> RetroRed
                        batteryLevel < 50 -> RetroYellow
                        else -> RetroGreen
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { batteryLevel / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        batteryLevel < 20 -> RetroRed
                        batteryLevel < 50 -> RetroYellow
                        else -> RetroGreen
                    }
                )

                if (isCharging) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CHARGING",
                        style = MaterialTheme.typography.bodyLarge,
                        color = RetroGreen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Historical Graph
                Text(
                    text = "DISCHARGE HISTORY",
                    style = MaterialTheme.typography.titleMedium,
                    color = RetroGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    BatteryGraph(data = batteryHistory)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(batteryInfo.size) { index ->
                val (label, value) = batteryInfo[index]
                InfoRow(label = label, value = value)
            }
        }
    }
}

@Composable
fun BatteryGraph(data: List<Int>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height

        // Draw grid lines
        val gridColor = Color.Gray.copy(alpha = 0.3f)
        for (i in 0..4) {
            val y = height * i / 4
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Draw graph
        if (data.size > 1) {
            val path = Path()
            val points = data.mapIndexed { index, value ->
                val x = width * index / (data.size - 1)
                val y = height - (height * value / 100f)
                Offset(x, y)
            }

            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }

            drawPath(
                path = path,
                color = RetroYellow,
                style = Stroke(width = 3f)
            )
        }
    }
}
