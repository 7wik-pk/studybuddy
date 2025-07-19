package com.example.studybuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.ui.theme.Surface
import kotlinx.coroutines.launch

//shows the upcoming sessions a user is enrolled in
@Composable
fun UpcomingSessions(
    navController: NavController,
    viewModel: StudyBuddyViewModel,
    modifier: Modifier = Modifier,
    isUpcoming : Boolean = true
) {
    val isLoading = remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            if(isUpcoming)
                viewModel.fetchLoggedInUsersUpcomingSessions()
            else
                viewModel.fetchLoggedInUsersPastSessions()
            isLoading.value = false
        }
    }

    Surface (
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // DEBUG print
//        println("vm logged in user's upcoming: ${viewModel.loggedInUserUpcomingSessions?.size}")
//        viewModel.loggedInUserUpcomingSessions?.forEach{
//                s -> println(s.name)
//            println(s.attendeeUIDs)
//            println("${s.sDate} T ${s.sTime}")
//            println(getCurrentDateTimeString())
//        }
            if(isLoading.value){
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(100.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

            }
            else {
                if(isUpcoming and viewModel.loggedInUserUpcomingSessions!!.isEmpty())
                    Text(modifier = Modifier.padding(bottom = 24.dp), text = "You have no upcoming sessions")
                if(!isUpcoming and viewModel.loggedInUserPastSessions!!.isEmpty())
                    Text(modifier = Modifier.padding(bottom = 24.dp), text = "You have no past sessions")

                // TODO test this whole thing in every layer - fbase read, viewModel, and here
                val sessionChoice = remember { mutableStateOf("") }
                if(isUpcoming and viewModel.loggedInUserUpcomingSessions!!.isNotEmpty())
                    LazySessionsColumn(viewModel.loggedInUserUpcomingSessions ?: emptyList(), sessionChoice)
                else if(!isUpcoming and viewModel.loggedInUserPastSessions!!.isNotEmpty())
                    LazySessionsColumn(viewModel.loggedInUserPastSessions ?: emptyList(), sessionChoice)

                if(sessionChoice.value.isNotEmpty())
                {

                    navController.navigate(
                        "session/{sessionID}"
                            .replace(
                                oldValue = "{sessionID}",
                                newValue = sessionChoice.value
                            )
                    )
                }

            }

        }

    }


}