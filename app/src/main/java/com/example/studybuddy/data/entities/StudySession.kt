package com.example.studybuddy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StudySession(
    @PrimaryKey
    var sID: String,

    val name: String,
    val description: String,
    val fieldOfStudy: String,
    val sDate: String,
    val sTime: String,

    // Latitude/longitude coordinates of the form "lat,long"
    val location: String,

    val capacity: Int,

    // comma-separated list of UIDs of all the users attending this session
    // example: "uid1,uid2,uid3"
    val attendeeUIDs: String,

    val ownerUID: String,

    // empty string groupID means the session doesn't belong to any groups
    val groupID: String
)

// locations
val LTB = "-37.913850859475204, 145.13345510955597"
val Matheson = "-37.91284531140795, 145.13368270961323"
val CampusCenter = "-37.91187323233112, 145.13256686449188"
val Menzies = "-37.912555418506074, 145.1327459720402"

// Dummy/mock instances for testing

const val testSeshOwnerUID = "CWdjH4rtM3ZZoq3ayhkuexqhLAr2"

val testSesh1 = StudySession("1", "testSesh1", "desc", "IT", "23-05-2025", "11:00", LTB, 20, "", testSeshOwnerUID, "")
val testSesh2 = StudySession("2", "testSesh2", "desc", "Biology", "24-05-2025", "15:00", CampusCenter, 30, "", testSeshOwnerUID, "")
val testSesh3 = StudySession("3", "testSesh3", "desc", "Physics", "25-05-2025", "09:00", Matheson, 10, "", testSeshOwnerUID,"")
val testSesh4 = StudySession("4", "testSesh4", "desc", "Philosophy", "26-05-2025", "16:00", Menzies, 25, "", testSeshOwnerUID, "")

val testSessionsList = listOf( testSesh1, testSesh2, testSesh3, testSesh4 )

val emptySession = StudySession(
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    0,
    "",
    "",
    ""
)
