package com.example.studybuddy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StudyGroup(
    @PrimaryKey
    val groupID: String,

    val name: String,
    val description: String,
    val ownerUID: String,

    // memberUIDs should be a comma-separated string representation of all member UIDs
    // example: "uid1,uid2,uid3"
    val memberUIDs: String,

    // more comma-separated lists of interests and session IDs respectively
    val interests: String,
    val sessionIDs: String
)

