package com.example.studybuddy.screens.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

//the contents of the nav drawer
@Composable
fun DrawerContent(
    navController: NavController,
    closeDrawer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
//        Spacer(modifier = Modifier.height(60.dp))

        // Home
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
                closeDrawer()
            }
        )

        // Find Sessions
        NavigationDrawerItem(
            label = { Text("Find Sessions") },
            selected = false,
            onClick = {
                navController.navigate("find-sessions") {
                    popUpTo("home")
                }
                closeDrawer()
            }
        )
        // My Schedule
        NavigationDrawerItem(
            label = { Text("Schedule") },
            selected = false,
            onClick = {
                navController.navigate("upcoming-sessions") {
                    popUpTo("home")
                }
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            label = { Text("History") },
            selected = false,
            onClick = { navController.navigate("past-sessions") {
                popUpTo("home")
            }
                closeDrawer() }
        )

        // My Sessions
        NavigationDrawerItem(
            label = { Text("My Upcoming Sessions") },
            selected = false,
            onClick = {
                navController.navigate("my-upcoming-sessions") {
                    popUpTo("home")
                }
                closeDrawer() }
        )
        NavigationDrawerItem(
            label = { Text("My Past Sessions") },
            selected = false,
            onClick = {
                navController.navigate("my-past-sessions") {
                    popUpTo("home")
                }
                closeDrawer() }
        )


        // Settings
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = {
                navController.navigate("settings") {
                    popUpTo("home")
                }
                closeDrawer()
            }
        )

        // Logout
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout
        )
    }
}


