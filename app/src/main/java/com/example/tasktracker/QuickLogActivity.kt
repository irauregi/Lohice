package com.example.tasktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasktracker.viewmodel.TaskViewModel

class QuickLogActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val tasks by viewModel.activeTasks.collectAsState()
                var lastRegisteredTaskName by remember { mutableStateOf<String?>(null) }
                var lastRegisteredResult by remember { mutableStateOf<String?>(null) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Registro Rápido") },
                            actions = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        if (lastRegisteredTaskName != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Registrado: $lastRegisteredTaskName", style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text = if (lastRegisteredResult == "DONE") "✓ Lo hice" else "✗ No lo hice",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            viewModel.undoLastAction()
                                            lastRegisteredTaskName = null
                                            lastRegisteredResult = null
                                        }) {
                                            Text("Deshacer")
                                        }
                                        Button(onClick = {
                                            lastRegisteredTaskName = null
                                            lastRegisteredResult = null
                                        }) {
                                            Text("Otro")
                                        }
                                    }
                                }
                            }
                        }

                        if (tasks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay tareas activas. Abre la app principal para crear tareas.")
                            }
                        } else {
                            Text("Selecciona el resultado de tu ocasión:", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(tasks) { task ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(task.name, style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.recordLog(task.id, "DONE")
                                                        lastRegisteredTaskName = task.name
                                                        lastRegisteredResult = "DONE"
                                                    },
                                                    modifier = Modifier.weight(1.5f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Text("Lo hice")
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.recordLog(task.id, "NOT_DONE")
                                                        lastRegisteredTaskName = task.name
                                                        lastRegisteredResult = "NOT_DONE"
                                                    },
                                                    modifier = Modifier.weight(1.5f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text("No lo hice")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
