package com.example.studybuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.studybuddy.cityCoordinates
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.testSessionsList
import kotlinx.coroutines.launch

//the first screen a user sees after logging in, displays their upcoming sessions and available
//sessions matching their interests and within 50km
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: StudyBuddyViewModel
){
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    val user = viewModel.loggedInUser
    val sessions by viewModel.sessions.collectAsState()


    LaunchedEffect(Unit) {
        scope.launch {

            viewModel.fetchSessions(
				interests = user.interests,
                distance = 50
			)
            viewModel.fetchLoggedInUsersUpcomingSessions()
            isLoading.value = false
        }
    }

    Surface {

        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(isLoading.value){
                CircularProgressIndicator(modifier = Modifier.padding(100.dp),color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            else if(viewModel.loggedInUserUpcomingSessions!!.isEmpty())
                Text("You have no upcoming sessions")
            else
                SessionsList("Upcoming Sessions", viewModel.loggedInUserUpcomingSessions!!, navController = navController)


            var availableSessions = mutableListOf<StudySession>()

            sessions.forEach {
                    s ->
//            println("contains: ${s.attendeeUIDs.contains(viewModel.loggedInUser.uid)}")
                if(!s.attendeeUIDs.contains(viewModel.loggedInUser.uid))
                    availableSessions.add(s)
            }
            if(isLoading.value)
            {
                CircularProgressIndicator(modifier = Modifier.padding(100.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            else
                SessionsList("Available Sessions", availableSessions, navController = navController)
        }

    }

}

//displays a list of sessions
@Composable
fun SessionsList(heading: String, sessions: List<StudySession>, modifier: Modifier = Modifier, navController: NavController){
    val selectedSession = remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10))
            .border(width = 2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.secondary),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(heading, modifier = Modifier.padding(top=8.dp), color = MaterialTheme.colorScheme.onSecondary)
        if (!sessions.isEmpty())
            LazySessionRow(sessions, selectedSession)
        else {
            Column(modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .height(240.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally){
                Text("No Sessions matching your criteria in your area", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        if (!selectedSession.value.isEmpty()){
            navController.navigate("session/{sessionID}".replace(
                oldValue = "{sessionID}",
                newValue = selectedSession.value
            ))
        }
    }
}