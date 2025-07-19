package com.example.studybuddy.data.repo

import android.app.Application
import com.example.studybuddy.data.entities.StudyGroup
import com.example.studybuddy.data.StudyBuddyDatabase
import com.example.studybuddy.data.dao.OwnGroupDAO
import kotlinx.coroutines.flow.Flow

//room repository for groups
class OwnGroupRepository (application: Application){

    private var groupDAO: OwnGroupDAO = StudyBuddyDatabase.getDatabase(application).groupDAO()
    val allOwnGroups: Flow<List<StudyGroup>> = groupDAO.getAllOwnGroups()

    suspend fun insert(studyGroup: StudyGroup){
        groupDAO.createOwnGroup(studyGroup)
    }

    suspend fun delete(studyGroup: StudyGroup){
        groupDAO.deleteOwnGroup(studyGroup)
    }

    suspend fun update(studyGroup: StudyGroup){
        groupDAO.updateOwnGroup(studyGroup)
    }
}