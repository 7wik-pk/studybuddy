package com.example.studybuddy.screens

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studybuddy.appName
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.managers.syncNow
import com.example.studybuddy.screens.drawer.DrawerContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//manages screen navigation
@Composable
fun MainScreen(viewModel: StudyBuddyViewModel) {

    val navController = rememberNavController()

    // Track whether user is logged in
    var isLoggedIn by remember { mutableStateOf(false) }

    // Start at login screen if not logged in
    val startDestination = if (isLoggedIn) "home" else "login"

    AppNavigation(
        navController = navController,
        startDestination = startDestination,
        viewModel = viewModel,
        onLoginSuccess = { isLoggedIn = true },
        onLogout = { isLoggedIn = false }
    )
}

//displays the main scaffold and navigates users where they need to go
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    viewModel: StudyBuddyViewModel,
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            if((navController.currentBackStackEntryAsState().value?.destination?.route == "home")
                or (navController.currentBackStackEntryAsState().value?.destination?.route == "find-sessions")
                or ((navController.currentBackStackEntryAsState().value?.destination?.route == "my-upcoming-sessions")))
            {
                val back = navController.currentBackStackEntryAsState().value?.destination?.route ?: "home"
                Box(modifier = Modifier.fillMaxSize()
                    .padding(bottom = 40.dp)) {
                    Column(modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom){
                        FloatingActionButton(
                            modifier = Modifier.padding(
                            ),
                            onClick = {
                                navController.navigate("create") {
                                    popUpTo(back)
                                }
                            },
                        ) {
                            Icon(Icons.Filled.Add, "Floating action button.")
                        }
                    }
                }
            }

        },
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
        topBar = {
            if ((navController.currentBackStackEntryAsState().value?.destination?.route != "login") and (navController.currentBackStackEntryAsState().value?.destination?.route != "register"))
            {
                CenterAlignedTopAppBar(
                    colors = TopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),

                    navigationIcon = {
                        androidx.compose.material3.IconButton(onClick = {
                            scope.launch {
                                scaffoldState.drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Content Drawer"
                            )
                        }
                    },

                    title = {
                        androidx.compose.material3.TextButton(onClick = {
                            navController.navigate("home")
                        }){
                            androidx.compose.material3.Text(appName, fontSize = 25.sp)
                        }

                    },

                    actions = {
                        androidx.compose.material3.IconButton(onClick = {
                            println("vm.loggedInUser: ${viewModel.loggedInUser}")
                            navController.navigate(
                                "profile/{userID}"
                                    .replace(
                                        oldValue = "{userID}",
                                        newValue = viewModel.loggedInUser.uid
                                    ))
                        }) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile"
                            )
                        }
                    }
                )
            }
            else{
                CenterAlignedTopAppBar(
                    colors = TopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    title = {
                        androidx.compose.material3.TextButton(onClick = {}){
                            androidx.compose.material3.Text(appName, fontSize = 25.sp, color = Color.Black)
                        }

                    },

                )
            }
        },

        drawerContent = {
            if (
                (navController.currentBackStackEntryAsState().value?.destination?.route != "login")
                &&
                (navController.currentBackStackEntryAsState().value?.destination?.route != "register")
                ) {
                DrawerContent (
                    navController = navController,
                    closeDrawer = {
                        scope.launch {
                            scaffoldState.drawerState.close()
                        }
                    },
                    onLogout = {
                        scope.launch {
                            onLogout()
                            syncNow(context)
                            delay(500)
                            viewModel.firebaseClient.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                            scaffoldState.drawerState.close()
                        }
                    },
                    modifier = Modifier
                )
            }
        },

        drawerBackgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("login") {
                Login(
                    snackbarHostState = scaffoldState.snackbarHostState,
                    navController = navController,

//                    firebaseClient = viewModel.firebaseClient,

                    viewModel = viewModel,

                    onLoginSuccess = {
                        onLoginSuccess()
                        navController.navigate("home") {
                            // Clear the back stack so users can't go back to login
                            popUpTo(0)
                        }
                    },

                    // TODO below funcs will be similar to login above. both should navigate back to login
                    onRegister = {
                        navController.navigate("register")
                    },

                    onForgotPW = {}
                )
            }

            // ###### ROUTES ######

            val innerPaddedMod = Modifier.padding(innerPadding)

            // ----------- nav drawer routes -----------

            composable("home") { HomeScreen(navController = navController, modifier = innerPaddedMod, viewModel = viewModel) }

            composable("find-sessions") { ExploreScreen(navController = navController, modifier = innerPaddedMod, viewModel) }

            composable("upcoming-sessions") { UpcomingSessions(navController = navController, viewModel, innerPaddedMod) }
            composable("past-sessions") { UpcomingSessions(navController = navController, viewModel, innerPaddedMod, isUpcoming = false) }

            composable("my-upcoming-sessions") { MySessions(navController = navController, viewModel, innerPaddedMod) }
            composable("my-past-sessions") { MySessions(navController = navController, viewModel, innerPaddedMod, isUpcoming= false) }
//            composable("my-schedule") { Schedule() }

            composable("settings") { SettingsScreen(navController = navController, viewModel = viewModel, modifier = innerPaddedMod, snackbarHostState = scaffoldState.snackbarHostState)}
            composable("view-attendees"){
                AttendeesList(navController, viewModel, innerPaddedMod)
            }

            // ----------- other screen routes

            composable("register") {
                RegistrationOrEditForm(

                    navController,

                    modifier = innerPaddedMod,

                    onRegistrationSuccess = {
                        navController.navigate("login") {
                            // Clear the back stack so users can't go back to login
                            popUpTo(0)
                        }
                    },

                    onEditSuccess = {
                        navController.popBackStack()
                    },

                    onGoBack = {
                        navController.popBackStack()
                    },

                    studyBuddyViewModel = viewModel,
                    snackbarHostState = scaffoldState.snackbarHostState
                )
            }

            composable(
                route = "edit"
            ) {
                val user = viewModel.loggedInUser

//              Pass the user to the composable
                RegistrationOrEditForm(
                    user = user, navController = navController,  modifier = innerPaddedMod,
                    onRegistrationSuccess = {
                        navController.popBackStack()
                    },
                    onEditSuccess = {
                        navController.popBackStack()
                    },
                    onGoBack = {
                        navController.popBackStack()
                    },
                    studyBuddyViewModel = viewModel,
                    snackbarHostState = scaffoldState.snackbarHostState
                )

            }

            // ----------- advanced routes for (1) edit screens (or) (2) detailed views of individual entities -----------

            // example: session details
//             composable(
//                 route = "session-details/{sessionID}",
//                 arguments = listOf(
//                     navArgument("sessionID") {type = NavType.StringType}
//                 )
//             ) {
//                navBackStackEntry ->
//
//                 val sID = navBackStackEntry.arguments?.getString("sessionID") ?: ""
//
//                 val sessionObj = testSesh1
//
//                 SessionDetails(navController, sessionObj)
//             }

            // edit session, edit profile and other such screens here...
            composable(
                route = "create"
            ) {
//                    // Create a User object
                val user = viewModel.loggedInUser
//                     Pass the user to the composable
                CreateOrEditSession(
                    navController = navController,
                    modifier = innerPaddedMod,
                    viewModel = viewModel,
                    snackbarHostState = scaffoldState.snackbarHostState,
                    onSuccess = {
                        navController.popBackStack()
                    })
            }

            composable(
                route = "profile/{userID}",
                arguments = listOf(
                    navArgument("userID") { type = NavType.StringType},
                )
            ) { backStackEntry ->

                // Extract the arguments from the backstack
                val userID = backStackEntry.arguments?.getString("userID") ?: ""

                // Pass the user to the composable
                ProfileScreen(

                    userID = userID, navController = navController, modifier = innerPaddedMod,

                    viewModel = viewModel,

//                    firebaseClient = viewModel.firebaseClient,

                    onEdit = {
                        navController.navigate(
                            "edit"
                        )
                    })
            }

            composable(
                route = "session/{sessionID}",
                arguments = listOf(
                    navArgument("sessionID") { type = NavType.StringType }
                )
            ){ navBackStackEntry ->

                val sessionID = navBackStackEntry.arguments?.getString("sessionID") ?: ""

                SessionDetailsPage(
                    navController,
                    sessionID,
                    viewModel = viewModel, modifier = innerPaddedMod,

                    onGoBack = {
                        navController.popBackStack()
                    },
                    onDelete = {
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    },
                    snackbarHostState = scaffoldState.snackbarHostState,
                    onEdit = {

                        // TODO screen for this link isn't implemented yet
                        navController.navigate(
                            "edit-session"
                        )

                    }
                )
            }

            composable(
                route = "edit-session"
            ){ navBackStackEntry ->
                CreateOrEditSession(navController, viewModel, modifier = innerPaddedMod, session = viewModel.sessionDetailsObj, onSuccess = {
                    navController.popBackStack()
                }, snackbarHostState = scaffoldState.snackbarHostState)
            }

        }
    }
}