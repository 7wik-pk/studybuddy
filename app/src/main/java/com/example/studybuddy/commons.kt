package com.example.studybuddy

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng

//Common contants and other data to be used across the application

const val appName = "StudyBuddy"
val standardFontSize = 20.sp
val standardSpacerHeight = 15.dp

val fieldsOfStudy_ = listOf(
	"IT",
	"Mobile App Development",
	"Cloud Computing",
	"Data Analysis",
	"Computer Networks",
	"Database Management"
)

val fieldsOfStudy = listOf(
	"Computer Science",
	"Medicine",
	"Engineering",
	"Psychology",
	"Economics",
	"Biology",
	"Physics",
	"Chemistry",
	"Mathematics",
	"History",
	"Philosophy",
	"Law",
	"Education",
	"Business Administration",
	"Art and Design",
	"Literature",
	"Political Science",
	"Sociology",
	"Environmental Science",
	"Linguistics",
	"Other"
)

val cities = listOf(
	"Sydney",
	"Melbourne",
	"Brisbane",
	"Perth",
	"Adelaide",
	"Gold Coast",
	"Newcastle",
	"Canberra",
	"Central Coast",
	"Wollongong",
	"Logan City",
	"Geelong",
	"Hobart",
	"Townsville",
	"Cairns",
	"Darwin",
	"Toowoomba",
	"Ballarat",
	"Bendigo",
	"Albury"
)

val cityCoordinates = hashMapOf(
	"Sydney" to LatLng(-33.8688, 151.2093),
	"Melbourne" to LatLng(-37.8136, 144.9631),
	"Brisbane" to LatLng(-27.4698, 153.0251),
	"Perth" to LatLng(-31.9505, 115.8605),
	"Adelaide" to LatLng(-34.9285, 138.6007),
	"Gold Coast" to LatLng(-28.0167, 153.4000),
	"Newcastle" to LatLng(-32.9283, 151.7817),
	"Canberra" to LatLng(-35.2809, 149.1300),
	"Central Coast" to LatLng(-33.4269, 151.3428),
	"Wollongong" to LatLng(-34.4278, 150.8931),
	"Logan City" to LatLng(-27.6386, 153.1094),
	"Geelong" to LatLng(-38.1499, 144.3617),
	"Hobart" to LatLng(-42.8821, 147.3272),
	"Townsville" to LatLng(-19.2590, 146.8169),
	"Cairns" to LatLng(-16.9186, 145.7781),
	"Darwin" to LatLng(-12.4634, 130.8456),
	"Toowoomba" to LatLng(-27.5598, 151.9507),
	"Ballarat" to LatLng(-37.5622, 143.8503),
	"Bendigo" to LatLng(-36.7570, 144.2794),
	"Albury" to LatLng(-36.0737, 146.9135)
)

val monashClayton = LatLng(-37.9105, 145.1347)