package com.example.tasktracker.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val activeTasks: Flow<List<Task>> = taskDao.getActiveTasks()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allLogs: Flow<List<TaskLog>> = taskDao.getAllLogs()

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun insertLog(taskId: Long, result: String): Long {
        return taskDao.insertLog(TaskLog(taskId = taskId, result = result))
    }

    suspend fun deleteLog(log: TaskLog) = taskDao.deleteLog(log)
    suspend fun updateLog(log: TaskLog) = taskDao.updateLog(log)
    suspend fun getLogById(logId: Long) = taskDao.getLogById(logId)
}
