package com.example.tasktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tasktracker.data.Task
import com.example.tasktracker.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.activeTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val lastAction by viewModel.lastActionLog.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var newtaskName by remember { mutableStateOf("") }

    var taskToRename by remember { mutableStateOf<Task?>(null) }
    var renameText by remember { mutableStateOf("") }

    var showArchivedDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastAction) {
        if (lastAction != null) {
            val resultText = if (lastAction?.result == "DONE") "Lo hice" else "No lo hice"
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Registrado: $resultText",
                    actionLabel = "Deshacer",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoLastAction()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas Ocasionales") },
                actions = {
                    IconButton(onClick = { showArchivedDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = "Ver archivadas")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay tareas activas. Toca '+' para crear una.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.name,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            taskToRename = task
                                            renameText = task.name
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Renombrar", tint = MaterialTheme.colorScheme.secondary)
                                        }
                                        IconButton(onClick = {
                                            viewModel.archiveTask(task, true)
                                        }) {
                                            Icon(Icons.Default.Archive, contentDescription = "Archivar", tint = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dos botones separados para "Lo hice" y "No lo hice"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.recordLog(task.id, "DONE") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Lo hice")
                                    }
                                    Button(
                                        onClick = { viewModel.recordLog(task.id, "NOT_DONE") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
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

    // Diálogo para añadir tarea
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Tarea Ocasional") },
            text = {
                OutlinedTextField(
                    value = newtaskName,
                    onValueChange = { newtaskName = it },
                    label = { Text("Nombre de la tarea") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newtaskName.isNotBlank()) {
                        viewModel.addTask(newtaskName)
                        newtaskName = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para renombrar
    if (taskToRename != null) {
        AlertDialog(
            onDismissRequest = { taskToRename = null },
            title = { Text("Renombrar Tarea") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameText.isNotBlank() && taskToRename != null) {
                        viewModel.updateTask(taskToRename!!.copy(name = renameText.trim()))
                        taskToRename = null
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToRename = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de tareas archivadas
    if (showArchivedDialog) {
        val archivedTasks = allTasks.filter { it.isArchived }
        AlertDialog(
            onDismissRequest = { showArchivedDialog = false },
            title = { Text("Tareas Archivadas") },
            text = {
                if (archivedTasks.isEmpty()) {
                    Text("No hay tareas archivadas.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(archivedTasks) { t ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t.name)
                                Row {
                                    TextButton(onClick = { viewModel.archiveTask(t, false) }) {
                                        Text("Restaurar")
                                    }
                                    TextButton(onClick = { viewModel.deleteTask(t) }) {
                                        Text("Borrar", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchivedDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
