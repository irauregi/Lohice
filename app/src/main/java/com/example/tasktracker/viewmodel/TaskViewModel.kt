package com.example.tasktracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.data.AppDatabase
import com.example.tasktracker.data.Task
import com.example.tasktracker.data.TaskLog
import com.example.tasktracker.data.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository

    val activeTasks: StateFlow<List<Task>>
    val allTasks: StateFlow<List<Task>>
    val allLogs: StateFlow<List<TaskLog>>

    private val _lastActionLog = MutableStateFlow<TaskLog?>(null)
    val lastActionLog: StateFlow<TaskLog?> = _lastActionLog

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        activeTasks = repository.activeTasks.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allTasks = repository.allTasks.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        allLogs = repository.allLogs.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun addTask(name: String) {
        viewModelScope.launch {
            val currentList = activeTasks.value
            val newTask = Task(name = name.trim(), orderIndex = currentList.size)
            repository.insertTask(newTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun archiveTask(task: Task, archived: Boolean) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isArchived = archived))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun recordLog(taskId: Long, result: String) {
        viewModelScope.launch {
            val logId = repository.insertLog(taskId, result)
            val log = repository.getLogById(logId)
            _lastActionLog.value = log
        }
    }

    fun undoLastAction() {
        val log = _lastActionLog.value ?: return
        viewModelScope.launch {
            repository.deleteLog(log)
            _lastActionLog.value = null
        }
    }

    fun deleteLog(log: TaskLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun updateLogResult(log: TaskLog, newResult: String) {
        viewModelScope.launch {
            repository.updateLog(log.copy(result = newResult))
        }
    }
}
