package com.example.tasktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasktracker.data.TaskLog
import com.example.tasktracker.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: TaskViewModel) {
    val logs by viewModel.allLogs.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val taskMap = remember(tasks) { tasks.associateBy { it.id } }

    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var filterType by remember { mutableStateOf("ALL") }

    var logToEdit by remember { mutableStateOf<TaskLog?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val filteredLogs = remember(logs, selectedTaskId, filterType) {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val weekStart = calendar.timeInMillis

        logs.filter { log ->
            val matchTask = selectedTaskId == null || log.taskId == selectedTaskId
            val matchTime = when (filterType) {
                "TODAY" -> log.timestamp >= todayStart
                "WEEK" -> log.timestamp >= weekStart
                else -> true
            }
            matchTask && matchTime
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historial y Registros") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("Todo") }
                )
                FilterChip(
                    selected = filterType == "TODAY",
                    onClick = { filterType = "TODAY" },
                    label = { Text("Hoy") }
                )
                FilterChip(
                    selected = filterType == "WEEK",
                    onClick = { filterType = "WEEK" },
                    label = { Text("Esta Semana") }
                )
            }

            val taskList = tasks
            var expandedTaskDropdown by remember { mutableStateOf(false) }
            val selectedTaskName = taskMap[selectedTaskId]?.name ?: "Todas las tareas"

            ExposedDropdownMenuBox(
                expanded = expandedTaskDropdown,
                onExpandedChange = { expandedTaskDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedTaskName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrar por tarea") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTaskDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedTaskDropdown,
                    onDismissRequest = { expandedTaskDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las tareas") },
                        onClick = {
                            selectedTaskId = null
                            expandedTaskDropdown = false
                        }
                    )
                    taskList.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name) },
                            onClick = {
                                selectedTaskId = t.id
                                expandedTaskDropdown = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            if (filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros para este filtro.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        val taskName = taskMap[log.taskId]?.name ?: "Tarea eliminada"
                        val isDone = log.result == "DONE"
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(taskName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = if (isDone) "✓ Lo hice" else "✗ No lo hice",
                                        color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = dateFormat.format(Date(log.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Row {
                                    IconButton(onClick = { logToEdit = log }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Corregir")
                                    }
                                    IconButton(onClick = { viewModel.deleteLog(log) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (logToEdit != null) {
        val currentLog = logToEdit!!
        val taskName = taskMap[currentLog.taskId]?.name ?: "Tarea"
        AlertDialog(
            onDismissRequest = { logToEdit = null },
            title = { Text("Corregir Registro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tarea: $taskName")
                    Text("Selecciona el resultado correcto:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateLogResult(currentLog, "DONE")
                                logToEdit = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentLog.result == "DONE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Lo hice")
                        }
                        Button(
                            onClick = {
                                viewModel.updateLogResult(currentLog, "NOT_DONE")
                                logToEdit = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentLog.result == "NOT_DONE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("No lo hice")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { logToEdit = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
