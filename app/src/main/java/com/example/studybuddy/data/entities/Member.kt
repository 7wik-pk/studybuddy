package com.example.studybuddy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Member(
    @PrimaryKey
    val memberID: String,
    val fname: String,
    val lname: String,
    val email: String,
    val gender: String,
    val pronouns: String,
    val DOB: String,
    val profilePicturePath: String
)