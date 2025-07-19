package com.example.studybuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studybuddy.data.dao.OwnGroupDAO
import com.example.studybuddy.data.dao.OwnSessionDAO
import com.example.studybuddy.data.entities.Member
import com.example.studybuddy.data.entities.StudyGroup
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.User

//room database to store user's created groups and sessions in a cache
@Database(entities = [User::class, StudyGroup::class, StudySession::class, Member::class], version = 2, exportSchema = false)
abstract class StudyBuddyDatabase: RoomDatabase(){
    abstract fun groupDAO(): OwnGroupDAO
    abstract fun sessionDAO(): OwnSessionDAO

    companion object{
        @Volatile
        private var INSTANCE: StudyBuddyDatabase? = null

        fun getDatabase(context: Context): StudyBuddyDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyBuddyDatabase::class.java,
                    "Database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}