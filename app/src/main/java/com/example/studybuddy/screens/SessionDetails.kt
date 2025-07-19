package com.example.studybuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studybuddy.R
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.emptySession
import com.example.studybuddy.data.entities.listToString
import com.example.studybuddy.data.entities.strToGeopoint
import com.example.studybuddy.data.entities.stringToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//checks if session is in the future
fun isUpcomingSession(date: String, time: String): Boolean{
    if(date.isNotEmpty() and time.isNotEmpty()) {
        val dateTimeString = date + " " + time
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a")
        val dateTime = LocalDateTime.parse(dateTimeString, formatter)
        var current = LocalDateTime.now()
        if (dateTime > current)
            return true
    }
    return false
}

// TODO add are u sure prompt for delete and maybe save changes prompt
//displays the details of a session
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionDetailsPage(
    navController: NavController,
    sessionID: String,
    viewModel: StudyBuddyViewModel,
    onGoBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: androidx.compose.material.SnackbarHostState
) {

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.fetchSessionForSessionDetails(sessionID)
        }
    }

    Surface {

        Column(modifier =  Modifier.fillMaxSize().padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally){

            //Header maybe
            Spacer(modifier = Modifier.height(60.dp))
            //Need to overlay that red dot that they have
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(8.dp)
//                .size(width = 350.dp, height = 450.dp)
            ) {
//            Image(
//                painter = painterResource(R.drawable.map),
//                contentDescription = "An image of a map",
//                modifier = Modifier
//                    .size(width = 300.dp, height = 250.dp)
//                    .padding(4.dp)
//                    .align(Alignment.CenterHorizontally)
//            )

                if((viewModel.sessionDetailsObj != emptySession) && (viewModel.sessionDetailsObj != null) && (viewModel.sessionDetailsObj?.location != "")) {

                    println(viewModel.sessionDetailsObj)
                    val location = strToGeopoint(viewModel.sessionDetailsObj!!.location)
                    StaticMap(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        modifier = Modifier.height(300.dp).width(400.dp).padding(16.dp).align(Alignment.CenterHorizontally),
                        size = "300x250",
                        onclick = { }
                    )

                }

                Text(
                    modifier = Modifier
                        .padding(4.dp),
                    textAlign = TextAlign.Left,
                    text = "Name: ${viewModel.sessionDetailsObj!!.name}",
//                color =  Color.Black
                )

                var desc = viewModel.sessionDetailsObj!!.description
                if (desc == "")
                    desc = "-"

                Text(
                    modifier = Modifier
                        .padding(4.dp),
                    textAlign = TextAlign.Left,
                    text = "Description: $desc",
//                color =  Color.Black
                )
                Text(
                    modifier = Modifier
                        .padding(4.dp),
                    textAlign = TextAlign.Left,
                    text = "Field of Study: ${viewModel.sessionDetailsObj!!.fieldOfStudy}",
//                color =  Color.Black
                )
                // TODO change location coords to static map display and a geocoder response
                Text(
                    modifier = Modifier
                        .padding(4.dp),
                    textAlign = TextAlign.Left,
                    text = "Location: ${viewModel.revGeocoderResponseAddress.value}",
//                color =  Color.Black
                )
                Text(
                    modifier = Modifier
                        .padding(4.dp),
                    textAlign = TextAlign.Left,
                    text = "Date and Time: ${viewModel.sessionDetailsObj!!.sDate} at ${viewModel.sessionDetailsObj!!.sTime}",
//                color =  Color.Black
                )

                val attendees = stringToList(viewModel.sessionDetailsObj!!.attendeeUIDs).map { it.replace("/users/", "") }.toMutableList()
                val currentParticipants = remember { mutableIntStateOf(attendees.size) }

                Text(
                    modifier = Modifier
                        .padding(4.dp),
                    textAlign = TextAlign.Left,
                    text = "Current Participants: ${attendees.size}",
//                color =  Color.Black
                )

                Row(modifier = Modifier.fillMaxWidth())

                {
                    Text(
                        modifier = Modifier
                            .padding(4.dp),
                        textAlign = TextAlign.Left,
                        text = "Capacity: ${viewModel.sessionDetailsObj!!.capacity}",
//                    color =  Color.Black
                    )

                    Spacer(modifier=Modifier.size(4.dp))

                    if(attendees.size >= viewModel.sessionDetailsObj!!.capacity)
                        Text(text =  "Session is full!" , color = Color.Red, modifier = Modifier.padding(4.dp))

                }

                println("viewModel session details group name: ${viewModel.sessionDetailsGroupName}")

                if (!viewModel.sessionDetailsGroupName.isNullOrBlank())
                    Text(
                        modifier = Modifier
                            .padding(4.dp),
                        textAlign = TextAlign.Left,
                        text = "Group: ${viewModel.sessionDetailsGroupName}",
//                    color =  Color.Black
                    )
                Row(){
                    Column(horizontalAlignment = AbsoluteAlignment.Left){
                        if(isUpcomingSession(viewModel.sessionDetailsObj!!.sDate, viewModel.sessionDetailsObj!!.sTime)){
                            Row(){
                                Column(){
                                    TextButton(
                                        enabled = (currentParticipants.intValue >= 0) and (viewModel.loggedInUser.uid in attendees),
                                        onClick = {
                                            //leave class
                                            attendees.remove(viewModel.loggedInUser.uid)
                                            viewModel.updateSessionAttendees(viewModel.sessionDetailsObj!!.sID, listToString(attendees))
                                            currentParticipants.value = attendees.size
                                        }) {
                                        Text(text =  "Leave Session" )
                                    }
                                }
                                Column(){
                                    TextButton(
                                        enabled = (viewModel.loggedInUser.uid !in attendees) and (attendees.size < viewModel.sessionDetailsObj!!.capacity),
                                        onClick = {
                                            //join class
                                            if(viewModel.loggedInUser.uid !in attendees) {
                                                attendees.add(0, viewModel.loggedInUser.uid)
                                                attendees.removeAll { it.isEmpty() }
                                            }
                                            viewModel.updateSessionAttendees(viewModel.sessionDetailsObj!!.sID, listToString(attendees))
                                            currentParticipants.value = attendees.size

                                        }) {
                                        Text(text =  "Join Session" )
                                    }
                                }

                            }}
                    }
                    Column(horizontalAlignment = AbsoluteAlignment.Right){
                        TextButton(
                            onClick = {
                                navController.navigate("view-attendees")
                            }) {
                            Text(text =  "View Attendees" )
                        }

                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ){
                Column(){
                    // TODO test
                    if(isUpcomingSession(viewModel.sessionDetailsObj!!.sDate, viewModel.sessionDetailsObj!!.sTime)){
                        if (viewModel.loggedInUser.uid == viewModel.sessionDetailsObj!!.ownerUID) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Button",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clickable(onClick = {viewModel.sessionDetailsSessionID = sessionID
                                        onEdit() })
                            )}
                    }}
                Column(){
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = "Back Button",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable(onClick = {onGoBack() })
                    )}
                Column(){
                    if(isUpcomingSession(viewModel.sessionDetailsObj!!.sDate, viewModel.sessionDetailsObj!!.sTime)){
                        if (viewModel.loggedInUser.uid == viewModel.sessionDetailsObj!!.ownerUID) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Button",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clickable(onClick = {
                                        viewModel.sessionDetailsObj?.let {
                                            viewModel.deleteSessionRoomAndFirebase(it) }
                                        CoroutineScope(Dispatchers.Main).launch {
                                            snackbarHostState.showSnackbar("Deleting session..!")
                                        }
                                        onDelete() })
                            )
                        }}}}

            Spacer(modifier = Modifier.height(20.dp))
        }

    }

}