package com.example.studybuddy.data.repo

import android.app.Application
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.StudyBuddyDatabase
import com.example.studybuddy.data.dao.OwnSessionDAO
import kotlinx.coroutines.flow.Flow

//room repository for sessions
class OwnSessionRepository (application: Application){

    private var ownSessionDAO: OwnSessionDAO = StudyBuddyDatabase.getDatabase(application).sessionDAO()
    val allOwnSessions: Flow<List<StudySession>> = ownSessionDAO.getAllOwnSessions()

    suspend fun insert(studySession: StudySession){
        ownSessionDAO.insertOwnSession(studySession)
    }

    suspend fun delete(studySession: StudySession){
        ownSessionDAO.deleteOwnSession(studySession)
    }

    suspend fun deleteAll(){
        ownSessionDAO.deleteAllSessions()
    }

    suspend fun update(studySession: StudySession){
        ownSessionDAO.updateOwnSession(studySession)
    }
}