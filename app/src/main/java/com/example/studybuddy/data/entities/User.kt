package com.example.studybuddy.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    var uid: String,

    val fName: String,
    val lName: String,
    val email: String,
    val gender: String,
    val pronouns: String,
    val dob: String,

    // comma-separated list of interests
    // example: "drawing,history,computer-science"
    val interests: String,

    val city: String,
    val passwordHash: String,
    val profilePicturePath: String,
    val friendIDs: String
)

val emptyUser = User(
    uid = "null",
    fName = "",
    lName = "",
    email = "",
    gender = "",
    pronouns = "",
    dob = "",
    interests = "",
    city = "",
    passwordHash = "",
    profilePicturePath = "",
    friendIDs = ""
)