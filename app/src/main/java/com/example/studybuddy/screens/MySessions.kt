package com.example.studybuddy.screens

import android.text.style.AlignmentSpan
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.ui.theme.Surface

//displays the sessions the user has created
@Composable
fun MySessions(
    navController: NavController,
    viewModel: StudyBuddyViewModel,
    modifier: Modifier = Modifier,
    isUpcoming : Boolean = true
) {
    val isLoading = remember { mutableStateOf(true) }
    val sessions by viewModel.allOwnSessions.collectAsState(emptyList())
    val filteredSessions : MutableList<StudySession> = remember { mutableListOf() }

    Surface {

        if (isUpcoming) {
            for (session in sessions) {
                if (isUpcomingSession(
                        session.sDate,
                        session.sTime
                    ) and (session !in filteredSessions)
                ) {
                    println("filteredSessions has upcoming" + session.sID)
                    filteredSessions.add(session)
                }

            }
            isLoading.value = false
        } else {
            for (session in sessions) {
                if (!isUpcomingSession(
                        session.sDate,
                        session.sTime
                    ) and (session !in filteredSessions)
                ) {
                    println("filteredSessions has past" + session.sID)
                    filteredSessions.add(session)
                }

            }
            isLoading.value = false
        }

        if (isLoading.value) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(100.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            Column(
                modifier = modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (filteredSessions.isEmpty())
                    if (isUpcoming)
                        Text(
                            modifier = Modifier.padding(bottom = 24.dp),
                            text = "You have not created any sessions, try creating one!"
                        )
                    else
                        Text(
                            modifier = Modifier.padding(bottom = 24.dp),
                            text = "It looks like you haven't created any sessions that have ended."
                        )

                // TODO test this whole thing in every layer - fbase read, viewModel, and here
                val sessionChoice = remember { mutableStateOf("") }
                LazySessionsColumn(filteredSessions.toList() ?: emptyList(), sessionChoice)
                if (sessionChoice.value.isNotEmpty()) {

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