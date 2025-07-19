package com.example.studybuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.screens.MainScreen
import com.example.studybuddy.ui.theme.DynamicStudyBuddyTheme


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: StudyBuddyViewModel by viewModels()

    @RequiresApi(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            DynamicStudyBuddyTheme(viewModel) {
                //user is directed to the main screen which handles navigation and screen management
                MainScreen(viewModel)
            }
        }
    }

}
