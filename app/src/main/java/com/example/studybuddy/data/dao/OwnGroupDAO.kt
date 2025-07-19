package com.example.studybuddy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studybuddy.data.entities.StudyGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnGroupDAO{

    @Query("SELECT * FROM StudyGroup")
    fun getAllOwnGroups(): Flow<List<StudyGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createOwnGroup(studyGroup: StudyGroup)

    @Update
    suspend fun updateOwnGroup(studyGroup: StudyGroup)

    @Delete
    suspend fun deleteOwnGroup(studyGroup: StudyGroup)
}