package com.example.tasktracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY orderIndex ASC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY isArchived ASC, orderIndex ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TaskLog>>

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getLogsForTask(taskId: Long): Flow<List<TaskLog>>

    @Insert
    suspend fun insertLog(log: TaskLog): Long

    @Delete
    suspend fun deleteLog(log: TaskLog)

    @Update
    suspend fun updateLog(log: TaskLog)

    @Query("SELECT * FROM task_logs WHERE id = :logId")
    suspend fun getLogById(logId: Long): TaskLog?
}
