package com.example.studybuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SocialDistance
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.fieldsOfStudy
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import kotlinx.coroutines.flow.toList
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

//displays a map and searchbar so user can find sessions
@Composable
fun ExploreScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: StudyBuddyViewModel
){
    val selectedSession = remember { mutableStateOf("") }
    val sessions by viewModel.sessions.collectAsState()

    interactiveMap(modifier, viewModel.userLocation(),
        sessions= sessions,
        selectedSession
        )
    if(!selectedSession.value.isEmpty()){
        navController.navigate("session/{sessionID}".replace(
            oldValue = "{sessionID}",
            newValue = selectedSession.value
        ))
    }
    Box(
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxHeight()
                    .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            SearchBar(modifier, viewModel, navController)
        }
    }
}

//allows the user to search for sessions with advanced search options
@Composable
fun SearchBar(modifier: Modifier = Modifier,
              viewModel: StudyBuddyViewModel,
              navController: NavController){

    val scope = rememberCoroutineScope()

    val user = viewModel.loggedInUser

    val searchQuery = remember { mutableStateOf("") }
    val containsFriends = remember {mutableStateOf(false)}
    val inGroup = remember {mutableStateOf(false)}
    val interests = remember { mutableStateOf(user.interests) }
    val distance = remember { mutableStateOf("0") }
    val searchGroups = remember {mutableStateOf(true)}
    val searchDescription = remember {mutableStateOf(true)}
    val date = remember { mutableStateOf("") }
    val time = remember { mutableStateOf("") }

    var advancedSearch by remember {mutableStateOf(false)}
    var expanded by remember {mutableStateOf(false)}

    val onFocus = rememberUpdatedState {
        focus: FocusState -> expanded = focus.isFocused
    }
    val focusRequester = remember { FocusRequester() }

    val sessions by viewModel.sessions.collectAsState()

    val fosOptions = mutableListOf(*fieldsOfStudy.toTypedArray())

    val selectedSession = remember { mutableStateOf("") }

    LaunchedEffect(searchQuery.value, expanded) {
        scope.launch {
            viewModel.fetchSessions(
				searchQuery = searchQuery.value,
				date = date.value,
				time = time.value,
				interests = interests.value,
				distance = distance.value.toInt(),
				containsFriends = containsFriends.value,
				inGroup = inGroup.value,
				searchDescription = searchDescription.value
			)
        }
    }

    var availableSessions = mutableListOf<StudySession>()

    sessions.forEach {
            s ->
//            println("contains: ${s.attendeeUIDs.contains(viewModel.loggedInUser.uid)}")
        if(!s.attendeeUIDs.contains(viewModel.loggedInUser.uid))
            availableSessions.add(s)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column (
            Modifier
                .fillMaxWidth(0.8f)
        ){
            val height = 64.dp
            TextFieldTemplate(
                labelVal = "Search",
                icon = Icons.Default.Search,
                textFieldValue = searchQuery,
                isMandatory = false,
                singleLine = true,
                modifier = modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged {onFocus.value(it)}
            )
            if (!searchQuery.value.isEmpty())
                expanded = true
            if (availableSessions.isEmpty())
                expanded = false

            //search results
            if (expanded){
                Popup(
                    alignment = Alignment.TopStart,
                    onDismissRequest = {expanded = false}
                ) {
                    Column(modifier=modifier
                        .offset(y=height)
                        .height(600.dp)) {
                        LazySessionsColumn(availableSessions,
                            selectedSession)
                        if (!selectedSession.value.isEmpty()){
                            navController.navigate("session/{sessionID}".replace(
                                oldValue = "{sessionID}",
                                newValue = selectedSession.value
                            ))
                        }

                    }
                }
            }
        }
        ElevatedButton(onClick = { advancedSearch = true }, modifier = Modifier.offset(y= (-4).dp)) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Advanced Search"
            )
        }
        //advanced search dropdown
        DropdownMenu(
            expanded = advancedSearch,
            onDismissRequest = {advancedSearch = false}
        ) {
            Row {
                Column(
                    Modifier.weight(0.5f)
                ) {
                    DropDownSelectMultipleTemplate(
                        labelVal = "Interests",
                        options = fosOptions,
                        textFieldValue = interests
                    )
                    CheckBoxTemplate(
                        checkedBox = searchDescription,
                        textToDisplay = "Search Session Description"
                    )
                }
                Column(
                    Modifier.weight(0.5f)
                ) {
                    TextFieldTemplate(
                        labelVal = "Distance",
                        icon = Icons.Default.SocialDistance,
                        textFieldValue = distance,
                        modifier = modifier,
                        singleLine = true
                    )
                    DatePickerTemplate(
                        inputDate = date,
                        labelVal = "Date",
                        isMandatory = false
                    )
                    val timeOptions = mutableListOf("12:00 AM","12:30 AM","1:00 AM","1:30 AM","2:00 AM","2:30 AM","3:00 AM","3:30 AM","4:00 AM","4:30 AM","5:00 AM","5:30 AM","6:00 AM","6:30 AM","7:00 AM","7:30 AM","8:00 AM","8:30 AM","9:00 AM","9:30 AM","10:00 AM","10:30 AM","11:00 AM","11:30 AM","12:00 PM","12:30 PM","1:00 PM","1:30 PM","2:00 PM","2:30 PM","3:00 PM","3:30 PM","4:00 PM","4:30 PM","5:00 PM","5:30 PM","6:00 PM","6:30 PM","7:00 PM","7:30 PM","8:00 PM","8:30 PM","9:00 PM","9:30 PM","10:00 PM","10:30 PM","11:00 PM","11:30 PM")
                    DropDownSelectOneTemplate(
                        "Time",
                        Icons.Filled.AccessTime,
                        timeOptions,
                        time
                    )
                }

            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    StudyBuddyTheme {
        //ExploreScreen()
    }
}