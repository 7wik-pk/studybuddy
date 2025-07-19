package com.example.studybuddy

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.screens.ExploreScreen
import com.example.studybuddy.screens.StaticMap

//loads individual screens for testing purposes, safe to ignore
@Composable
fun TestScreens(viewModel: StudyBuddyViewModel){
	val navController = rememberNavController()

	ExploreScreen(
		navController,
		viewModel = viewModel
	)
	//-37.9129499,145.1337072
	/*StaticMap(
		latitude = -37.9129499,
		longitude = 145.1337072
	)*/
}