package com.example.studybuddy.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.strToGeopoint
import com.example.studybuddy.data.entities.stringToList
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import kotlinx.coroutines.launch
//not sure what this is for
@Composable
fun LazySessionsList(
    attendeeList: List<String>,
    attendeeChoice: MutableState<String>? = null){

}

//displays a list of attendees
@Composable
fun AttendeesList(navController: NavController,
                  viewModel: StudyBuddyViewModel,
                  modifier: Modifier = Modifier
) {

    Surface {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Session Attendees", textAlign = TextAlign.Center)
            val attendeeList = stringToList(viewModel.sessionDetailsObj!!.attendeeUIDs)
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start)
            {
                items(attendeeList.size) { index ->
                    val scope = rememberCoroutineScope()
                    var nameAttendee = remember { mutableStateOf("") }
                    LaunchedEffect(Unit) {
                        scope.launch {
                            var user = viewModel.firebaseClient.fbGetUserByUID(attendeeList[index])
                            if (user != null) {
                                nameAttendee.value = (user.fName + " " + user.lName)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .clip(RoundedCornerShape(10))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                navController.navigate(
                                    "profile/{userID}"
                                        .replace(
                                            oldValue = "{userID}",
                                            newValue = attendeeList[index]
                                        )
                                )
                            },
                        horizontalArrangement = Arrangement.Start
                    ){
                        Column (modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(10)),
                            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                            Icon(Icons.Default.Person, contentDescription = "Person")
                        }
                        Column (
                            modifier = Modifier
                                .clip(RoundedCornerShape(10))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(4.dp)
                                .wrapContentWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                nameAttendee.value,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                softWrap = true,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                        }
                    }
                }
            }


        }
    }

}
