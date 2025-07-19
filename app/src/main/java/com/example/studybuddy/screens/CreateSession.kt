package com.example.studybuddy.screens

import android.widget.Toast
import android.se.omapi.Session
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.latLngToString
import com.example.studybuddy.data.entities.strToLatLng
import com.example.studybuddy.fieldsOfStudy
import com.example.studybuddy.standardFontSize
import com.example.studybuddy.standardSpacerHeight
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//screen for creating and editing session details
@RequiresApi(0)
@Composable
fun CreateOrEditSession(
    navController: NavController,
    viewModel: StudyBuddyViewModel,
    modifier: Modifier = Modifier,
    session: StudySession? = null,
    onSuccess: () -> Unit,
    snackbarHostState: androidx.compose.material.SnackbarHostState
) {
    val sessionName = remember { mutableStateOf(session?.name ?: "") }
    val sessionDesc = remember { mutableStateOf(session?.description ?: "") }
    val fieldOfStudy = remember { mutableStateOf(session?.fieldOfStudy ?: "") }
//    val isRecurring = remember { mutableStateOf(false) }
    val sessionDate = remember { mutableStateOf(session?.sDate ?: "") }
    val sessionTime = remember { mutableStateOf(session?.sTime ?: "") }
    val sessionLocation = remember { mutableStateOf(strToLatLng(session?.location ?: latLngToString(viewModel.userLocation()))) }
    val sessionCapacity = remember { mutableStateOf(session?.capacity ?: 2) }
    // check if session?.ownerUID? in session?.attendeeUIDs? when editing
    val isOwnerAttending = remember { mutableStateOf(false) }
    val sessionGroupID = remember { mutableStateOf(session?.groupID ?: "") }
    val scope = rememberCoroutineScope()

    val isLoading = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {

        viewModel.revGeocode(latLngToString(sessionLocation.value))

    }

    val openMap = remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp)
    ) {
        if(openMap.value){
            Popup() {
                PinMap(
					modifier = modifier,
					userLocation = viewModel.userLocation(),
					sessionLocation = sessionLocation,
					edit = session != null,
                    onMapClick = { viewModel.revGeocode( latLngToString(sessionLocation.value) ) }
				)
                Box() {
                    Column(verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp)) {
                        ElevatedButton(
                            onClick = { openMap.value = false }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            if (session == null) {
                DisplayHeading("Create A New Session")
            } else {
                DisplayHeading("Edit Session")
            }

            TextFieldTemplate(
                "Session name",
                Icons.Filled.Class,
                sessionName,
                isMandatory = true,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(standardSpacerHeight))

            TextFieldTemplate(
                "Description of the session",
                Icons.Filled.ViewAgenda,
                sessionDesc
            )

            Spacer(modifier = Modifier.height(standardSpacerHeight))
            val fosOptions = mutableListOf(*fieldsOfStudy.toTypedArray())
            DropDownSelectOneTemplate(
                "Field of study",
                Icons.Filled.Science,
                fosOptions,
                fieldOfStudy
            )
            Text(
                text = "Set the location for this session",
                modifier = Modifier.padding(vertical = standardSpacerHeight),
                fontSize = standardFontSize,
                textAlign = TextAlign.Start
            )

            StaticMap(
				modifier = Modifier
                    .size(300.dp)
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally),
				latitude = if (sessionLocation.value.latitude == 0.0) viewModel.userLocation().latitude else sessionLocation.value.latitude,
				longitude = if (sessionLocation.value.longitude == 0.0) viewModel.userLocation().longitude else sessionLocation.value.longitude,
                onclick = {openMap.value = true}
			)

            Spacer(modifier = Modifier.height(8.dp))

            if (!openMap.value) {

                if((sessionLocation.value.latitude == 0.0) && (sessionLocation.value.longitude == 0.0))
                {
                    sessionLocation.value = viewModel.userLocation()
                }

                Text(text = "Selected location: ${viewModel.revGeocoderResponseAddress.value}")

            }

            Spacer(modifier = Modifier.height(8.dp))

//            val locationIp = remember { mutableStateOf("") }
            /*val locOptions = mutableListOf("-37.913850859475204,145.13345510955597", "-37.91284531140795,145.13368270961323", "-37.91187323233112,145.13256686449188", "-37.912555418506074,145.1327459720402")
//            TextFieldTemplate(
//                "Search for the location manually",
//                Icons.Filled.LocationOn,
//                locationIp
//            )
            DropDownSelectOneTemplate(
                "Search for the location manually",
                Icons.Filled.LocationOn,
                locOptions,
                sessionLocation
            )*/
            // TODO SESSION LOCATION SHOULD BE A MANDATORY FIELD
            // TODO: search for a valid location based on the provided input string - maybe using Retrofit
            // TODO: display map where user can drop a pin
            // store location as latitude/longitude coordinates

            // TODO: ask the user if this session should be part of a group they own
            // if yes, provide a list of all groups the user owns in a dropdown menu
            // otherwise, keep the groupID as -1 (means it's an independent session, not belonging to any groups)

            Spacer(modifier = Modifier.height(standardSpacerHeight))

            if (session == null) {

                CheckBoxTemplate(isOwnerAttending, "I will be attending this session")

                Spacer(modifier = Modifier.height(standardSpacerHeight))
            }
            val capacityString = remember { mutableStateOf( sessionCapacity.value.toString()) }
            val capacityError = remember { mutableStateOf(false)}
            TextFieldTemplate(
                "Maximum capacity for this session",
                Icons.Filled.People,
                capacityString,
                isNumeric = true,
                isError = capacityError.value,
                errSupportingText = "A minimum of 2 attendees are required"
            )
            sessionCapacity.value = if (capacityString.value.isEmpty()) 0 else capacityString.value.toInt()
            if(sessionCapacity.value < 2)
                {
                    capacityError.value = true
                }
                else
                    capacityError.value = false


            Spacer(modifier = Modifier.height(standardSpacerHeight))

            DatePickerTemplate(sessionDate, "Select session date", isMandatory = true)

            Spacer(modifier = Modifier.height(standardSpacerHeight))
            Spacer(modifier = Modifier.height(standardSpacerHeight))
            // similarly, time picker here
            // TODO: when working on A4 change to timePicker instead of datePicker - add new func to UIComponents
//            DatePickerTemplate(sessionTime, "Set session time", Icons.Filled.AccessTime, isMandatory = true)
            val timeOptions = mutableListOf("12:00 AM","12:30 AM","1:00 AM","1:30 AM","2:00 AM","2:30 AM","3:00 AM","3:30 AM","4:00 AM","4:30 AM","5:00 AM","5:30 AM","6:00 AM","6:30 AM","7:00 AM","7:30 AM","8:00 AM","8:30 AM","9:00 AM","9:30 AM","10:00 AM","10:30 AM","11:00 AM","11:30 AM","12:00 PM","12:30 PM","1:00 PM","1:30 PM","2:00 PM","2:30 PM","3:00 PM","3:30 PM","4:00 PM","4:30 PM","5:00 PM","5:30 PM","6:00 PM","6:30 PM","7:00 PM","7:30 PM","8:00 PM","8:30 PM","9:00 PM","9:30 PM","10:00 PM","10:30 PM","11:00 PM","11:30 PM")
            DropDownSelectOneTemplate(
                "Set session time",
                Icons.Filled.AccessTime,
                timeOptions,
                sessionTime,
                isMandatory = true
            )
            // update Firebase to store new/edited session details using vm
            // TODO remember to cast capacity back to int and add owner UID if checkbox boolean isOwnerAttending is true
            Spacer(modifier = Modifier.height(standardSpacerHeight))
            Spacer(modifier = Modifier.height(standardSpacerHeight))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Button(
                        enabled = !capacityError.value and sessionTime.value.isNotEmpty() and sessionDate.value.isNotEmpty() and sessionName.value.isNotEmpty(),
                        onClick = {
                            isLoading.value = true
                            if (session != null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Updating session..!")
                                }
                                // TODO test

                                val updateSession = StudySession(
                                    sID = session.sID,
                                    name = sessionName.value,
                                    description = sessionDesc.value,
                                    fieldOfStudy = fieldOfStudy.value,
                                    sDate = sessionDate.value,
                                    sTime = sessionTime.value,
                                    location = latLngToString(sessionLocation.value),
                                    capacity = sessionCapacity.value,
                                    attendeeUIDs = session.attendeeUIDs,
                                    ownerUID = session.ownerUID,
                                    groupID = session.groupID,
                                )
                                scope.launch {
                                    viewModel.updateSessionRoomAndFirebase(updateSession)
                                    onSuccess()
                                    isLoading.value = false
                                }

                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Registering session..!")
                                }
                                val newSession = StudySession(
                                    sID = sessionName.value,
                                    name = sessionName.value,
                                    description = sessionDesc.value,
                                    fieldOfStudy = fieldOfStudy.value,
                                    sDate = sessionDate.value,
                                    sTime = sessionTime.value,
                                    location = latLngToString(sessionLocation.value),
                                    capacity = sessionCapacity.value,
                                    attendeeUIDs = if (isOwnerAttending.value) viewModel.loggedInUser.uid else "",
                                    ownerUID = viewModel.loggedInUser.uid,
                                    groupID = sessionGroupID.value,
                                )

                                scope.launch {
                                    viewModel.insertSessionRoomAndFirebase(newSession)
                                    onSuccess()
                                    isLoading.value = false
                                }
                            }

                        }
                    )
                    {
                        if (isLoading.value) {
                        CircularProgressIndicator( color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp))
                    } else {
                        Text(text = if (session != null) "Save" else "Create")
                        }
                    }
                    if(session != null)
                    {
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("Deleting session..!")
                                }
                                viewModel.deleteSessionRoomAndFirebase(session)
                                navController.navigate("my-upcoming-sessions") {
                                    popUpTo("home")
                                }
                            }
                        ) {
                            Text(text = "Delete")
                        }
                    }
                }
            }
        }


    }
}
