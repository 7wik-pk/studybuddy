package com.example.studybuddy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studybuddy.data.entities.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnSessionDAO{
    @Query("SELECT * FROM StudySession")
    fun getAllOwnSessions(): Flow<List<StudySession>>

    @Query("DELETE FROM StudySession")
    fun deleteAllSessions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnSession(studySession: StudySession)

    @Update
    suspend fun updateOwnSession(studySession: StudySession)

    @Delete
    suspend fun deleteOwnSession(studySession: StudySession)
}