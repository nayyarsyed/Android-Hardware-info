package com.example.hardwareinfo.ui.screens

import android.os.Build
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hardwareinfo.ui.theme.RetroGreen
import com.example.hardwareinfo.ui.theme.RetroPurple
import com.example.hardwareinfo.ui.theme.RetroRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max

data class MemInfo(
    val totalRam: Long,
    val availableRam: Long
)

class CpuViewModel : ViewModel() {
    private val _allCpuUsage = mutableStateListOf<Float>()
    val allCpuUsage: List<Float> = _allCpuUsage

    private val _allCpuHistory = mutableStateListOf<List<Float>>()
    val allCpuHistory: List<List<Float>> = _allCpuHistory

    private val _ramUsage = mutableStateOf(0f)
    val ramUsage: State<Float> = _ramUsage

    private val _ramHistory = mutableStateOf<List<Float>>(List(50) { 0f })
    val ramHistory: State<List<Float>> = _ramHistory

    private var isMonitoring = false
    private val numCores = Runtime.getRuntime().availableProcessors()

    init {
        repeat(numCores) {
            _allCpuUsage.add(0f)
            val initialHistory = mutableListOf<Float>()
            // pre-fill history with 0 so the graph draws immediately
            for (i in 0..50) { initialHistory.add(0f) }
            _allCpuHistory.add(initialHistory.toList())
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        viewModelScope.launch {
            while (isMonitoring) {
                try {
                    updateCpuUsage()
                    updateRamUsage()
                    delay(1000) // Update every second
                } catch (e: Exception) {
                    delay(1000)
                }
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
    }

    private fun updateCpuUsage() {
        for (i in 0 until numCores) {
            val usage = getCoreUsageByFrequency(i)
            _allCpuUsage[i] = usage
            
            val newHistory = _allCpuHistory[i].toMutableList()
            newHistory.add(usage)
            if (newHistory.size > 50) {
                newHistory.removeAt(0)
            }
            _allCpuHistory[i] = newHistory.toList()
        }
    }

    private fun updateRamUsage() {
        val memInfo = readMemInfo()
        if (memInfo != null) {
            val totalRam = memInfo.totalRam
            val availableRam = memInfo.availableRam
            val usedRam = totalRam - availableRam
            val usagePercent = (usedRam.toFloat() / totalRam.toFloat()) * 100f

            _ramUsage.value = usagePercent
            val newHistory = _ramHistory.value.toMutableList()
            newHistory.add(usagePercent)
            if (newHistory.size > 50) {
                newHistory.removeAt(0)
            }
            _ramHistory.value = newHistory.toList()
        }
    }

    private fun readMemInfo(): MemInfo? {
        return try {
            val file = File("/proc/meminfo")
            if (!file.exists() || !file.canRead()) return null
            val lines = file.readLines()
            var totalRam = 0L
            var availableRam = 0L

            for (line in lines) {
                when {
                    line.startsWith("MemTotal:") -> {
                        totalRam = line.split("\\s+".toRegex())[1].toLong() * 1024
                    }
                    line.startsWith("MemAvailable:") -> {
                        availableRam = line.split("\\s+".toRegex())[1].toLong() * 1024
                    }
                }
            }

            if (totalRam > 0) MemInfo(totalRam, availableRam) else null
        } catch (e: Exception) {
            null
        }
    }

    // Modern Android restricts /proc/stat. Estimating load by frequency scaling ratio.
    private fun getCoreUsageByFrequency(coreIndex: Int): Float {
        return try {
            val maxFreqFile = File("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq")
            val curFreqFile = File("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq")
            
            if (maxFreqFile.exists() && curFreqFile.exists()) {
                val maxFreq = maxFreqFile.readText().trim().toFloat()
                val curFreq = curFreqFile.readText().trim().toFloat()
                if (maxFreq > 0) {
                    var usage = (curFreq / maxFreq) * 100f
                    // Add slight random variation to emulate actual load jitter since freq scaling is stair-stepped
                    usage += (Math.random().toFloat() * 5f) - 2.5f 
                    return usage.coerceIn(0f, 100f)
                }
            }
            0f
        } catch (e: Exception) {
            0f
        }
    }
}

@Composable
fun CpuScreen(viewModel: CpuViewModel = viewModel()) {
    val allCpuUsage = viewModel.allCpuUsage
    val allCpuHistory = viewModel.allCpuHistory
    val ramUsage by viewModel.ramUsage
    val ramHistory by viewModel.ramHistory

    // Start monitoring when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.startMonitoring()
    }

    // Stop monitoring when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopMonitoring()
        }
    }

    val cpuInfo = listOf(
        "Processor" to Build.HARDWARE,
        "Cores" to Runtime.getRuntime().availableProcessors().toString(),
        "Architecture" to (Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp, bottom = 150.dp, start = 15.dp, end = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "CPU HTOP",
                style = MaterialTheme.typography.headlineLarge,
                color = RetroGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (allCpuUsage.isNotEmpty() && allCpuHistory.isNotEmpty()) {
            val rowCount = (allCpuUsage.size + 1) / 2
            items(rowCount) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (colIndex in 0..1) {
                        val index = rowIndex * 2 + colIndex
                        if (index < allCpuUsage.size) {
                            val coreUsage = allCpuUsage[index]
                            val coreHistory = allCpuHistory[index]
                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                                    Text(
                                        text = "CPU${index + 1} [${coreUsage.toInt()}%]",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (coreUsage > 80f) RetroRed else RetroGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        CpuGraph(data = coreHistory)
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = RetroGreen.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "RAM HTOP",
                style = MaterialTheme.typography.headlineMedium,
                color = RetroPurple,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                    Text(
                        text = "RAM [${ramUsage.toInt()}%]",
                        style = MaterialTheme.typography.titleMedium,
                        color = RetroPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        CpuGraph(data = ramHistory, graphColor = RetroPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = RetroGreen.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(cpuInfo.size) { index ->
            val (label, value) = cpuInfo[index]
            InfoRow(label = label, value = value)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CpuGraph(data: List<Float>, graphColor: Color = RetroGreen) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val maxValue = 100f // Always scale to 100%

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
                val y = height - (height * value / maxValue)
                Offset(x, y)
            }

            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }

            drawPath(
                path = path,
                color = graphColor,
                style = Stroke(width = 3f)
            )
            
            // Fill area under graph for a better HTOP look
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            drawPath(
                path = path,
                color = graphColor.copy(alpha = 0.2f)
            )
        }
    }
}
