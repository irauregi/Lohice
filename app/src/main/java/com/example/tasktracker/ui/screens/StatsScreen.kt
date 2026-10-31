package com.example.tasktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasktracker.viewmodel.TaskViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.activeTasks.collectAsState()
    val logs by viewModel.allLogs.collectAsState()

    var timeRange by remember { mutableStateOf("TODAY") } // TODAY, WEEK, ALL

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val weekStart = calendar.timeInMillis

    val filteredLogs = remember(logs, timeRange) {
        when (timeRange) {
            "TODAY" -> logs.filter { it.timestamp >= todayStart }
            "WEEK" -> logs.filter { it.timestamp >= weekStart }
            else -> logs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recuentos y Estadísticas") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = timeRange == "TODAY",
                    onClick = { timeRange = "TODAY" },
                    label = { Text("Hoy") }
                )
                FilterChip(
                    selected = timeRange == "WEEK",
                    onClick = { timeRange = "WEEK" },
                    label = { Text("Esta Semana") }
                )
                FilterChip(
                    selected = timeRange == "ALL",
                    onClick = { timeRange = "ALL" },
                    label = { Text("Todo el tiempo") }
                )
            }

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay tareas activas para mostrar recuentos.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks) { task ->
                        val taskLogs = filteredLogs.filter { it.taskId == task.id }
                        val doneCount = taskLogs.count { it.result == "DONE" }
                        val notDoneCount = taskLogs.count { it.result == "NOT_DONE" }
                        val totalCount = taskLogs.size

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(task.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem(label = "Lo hice", count = doneCount, color = MaterialTheme.colorScheme.primary)
                                    StatItem(label = "No lo hice", count = notDoneCount, color = MaterialTheme.colorScheme.error)
                                    StatItem(label = "Total", count = totalCount, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, color = color)
    }
}
