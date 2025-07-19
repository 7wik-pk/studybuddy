package com.example.studybuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studybuddy.R
import com.example.studybuddy.data.FirebaseClient
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.User
import com.example.studybuddy.data.entities.emptyUser
import com.example.studybuddy.data.entities.stringToList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// TODO: change everything to use current logged-in user's room data
//displays the user's details
@Composable
fun ProfileScreen(
    navController: NavController,
    onEdit: () -> Unit,
    userID: String,
    viewModel: StudyBuddyViewModel,
//    firebaseClient: FirebaseClient,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {

        LaunchedEffect(Unit) {
            scope.launch {
                viewModel.fetchUserForProfileDetails(userID)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(16.dp)
        ){
            Row (
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(userID == viewModel.loggedInUser.uid)
                {
                    IconButton(
                        enabled = (userID == viewModel.loggedInUser.uid),
                        onClick = { navController.navigate("settings") {}
                        }
                    )
                    {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    } }
                Image(
                    painter = painterResource(R.drawable.accountcircle),
                    contentDescription = "Profile Image"
                )
                if(userID == viewModel.loggedInUser.uid)
                {
                    IconButton(
                        enabled = (userID == viewModel.loggedInUser.uid),
                        onClick = { onEdit() }
                    ) {
                        Icon(Icons.Default.ModeEdit, contentDescription = "Edit details")
                    }}
            }

            Spacer(modifier = Modifier.padding(5.dp))
            DisplayHeading(viewModel.profileDetailsUser!!.fName + " "+ viewModel.profileDetailsUser!!.lName , textAlign = TextAlign.Center)
            Text(viewModel.profileDetailsUser!!.pronouns , textAlign = TextAlign.Center, color = Color.Gray)

            Spacer(modifier = Modifier.padding(20.dp))


            Column(
                modifier = Modifier
                    //.background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
            ){
                Row(modifier= Modifier.fillMaxWidth()) {
                    Text("First Name: ", color = Color.Gray, modifier= Modifier.weight(1f), fontSize = 24.sp )
                    Text(viewModel.profileDetailsUser!!.fName, textAlign = TextAlign.Center, modifier= Modifier.weight(1f), fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Row(modifier= Modifier.fillMaxWidth()) {
                    Text("Last Name: ", color = Color.Gray, modifier= Modifier.weight(1f), fontSize = 24.sp )
                    Text(viewModel.profileDetailsUser!!.lName, textAlign = TextAlign.Center, modifier= Modifier.weight(1f), fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Row(modifier= Modifier.fillMaxWidth()) {
                    Text("Email: ", color = Color.Gray, modifier= Modifier.weight(0.5f), fontSize = 24.sp )
                    Text(viewModel.profileDetailsUser!!.email, textAlign = TextAlign.Center, modifier= Modifier.weight(1f), fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Row(modifier= Modifier.fillMaxWidth()) {
                    Text("City: ", color = Color.Gray, modifier= Modifier.weight(1f), fontSize = 24.sp )
                    Text(viewModel.profileDetailsUser!!.city, textAlign = TextAlign.Center, modifier= Modifier.weight(1f), fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Row(modifier= Modifier.fillMaxWidth()) {
                    Text("Interests: ", color = Color.Gray, modifier= Modifier.weight(1f), fontSize = 24.sp )

                    val interestsLst = stringToList(viewModel.profileDetailsUser!!.interests)
                    val interestsDisp = interestsLst.joinToString(", ")

                    Text(interestsDisp, textAlign = TextAlign.Start, modifier= Modifier.weight(1f), fontSize = 20.sp, style = TextStyle.Default.copy(
                        lineBreak = LineBreak.Paragraph,
                        hyphens = Hyphens.None
                    ))
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Row(modifier= Modifier.fillMaxWidth()) {
                    Text("Gender: ", color = Color.Gray, modifier= Modifier.weight(1f), fontSize = 24.sp )
                    Text(viewModel.profileDetailsUser!!.gender, textAlign = TextAlign.Center, modifier= Modifier.weight(1f), fontSize = 24.sp)
                }

            }
        }

    }
}