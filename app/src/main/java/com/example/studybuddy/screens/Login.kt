package com.example.studybuddy.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studybuddy.appName
import com.example.studybuddy.data.StudyBuddyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.studybuddy.managers.setupSettingWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

//allows the user to login using firebase authentication
@Composable
fun Login(
    navController: NavController,
    viewModel: StudyBuddyViewModel,
//    firebaseClient: FirebaseClient,
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    onForgotPW: () -> Unit,
    snackbarHostState: androidx.compose.material.SnackbarHostState
) {

//    val email = remember { mutableStateOf("sri@email.com") }
//    val pass = remember { mutableStateOf("thisPassword1!") }
//    val email = remember { mutableStateOf("lf@m.com") }
//    val email = remember { mutableStateOf("sk@m.com") }
//    val email = remember { mutableStateOf("bw@m.com") }
    val email = remember { mutableStateOf("") }
    val pass = remember { mutableStateOf("") }

    val wrongEmail = remember { mutableStateOf(false) }
    val wrongPassword = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val isLoading = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
            .padding(top = 40.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        DisplayHeading(
            "Welcome to $appName!\n\nLogin to view your scheduled sessions",
            TextAlign.Center
        )

        Spacer(modifier = Modifier.padding(8.dp))

        TextFieldTemplate(
            "Email ID",
            Icons.Filled.Email,
            email,
            isMandatory = true,
            singleLine = true,
            isError = wrongEmail.value,
            errSupportingText = "Invalid Email Address"
        )

        Spacer(modifier = Modifier.padding(8.dp))

        PasswordFieldFunc(
            "Password",
            Icons.Filled.Password,
            pass,
            isError = wrongPassword.value,
            errSupportingText = "Wrong Password"
        )

        TextButton(onClick = {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                wrongEmail.value = true
                return@TextButton
            }
            var resetEmail:Boolean = true
            scope.launch {
                resetEmail = viewModel.firebaseClient.sendResetPasswordEmail(email.value)
                val text = if(resetEmail) "Password reset email sent!" else "Couldn't reset, your email is not registered!"
                CoroutineScope(Dispatchers.Main).launch {
                    snackbarHostState.showSnackbar(text)
                }
            }

        }, modifier = Modifier.offset(x = 200.dp)) {
            Text(text = "Forgot Password?")
        }

        Column(
            modifier =  Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                enabled = (email.value.isNotEmpty() && pass.value.isNotEmpty()),

                onClick = {

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                        wrongEmail.value = true
                        return@Button
                    }

                    isLoading.value = true

                    scope.launch {
                        delay(500)
                        val loggedInUser = viewModel.firebaseClient.loginUser(email.value, pass.value, wrongEmail, wrongPassword)
                        isLoading.value = false
                        if (loggedInUser != null)
                        {
                            viewModel.loggedInUser = loggedInUser
                            println("logged in user id from login: ${viewModel.loggedInUser}")
                            viewModel.fetchUserSettings(loggedInUser.uid)
                            viewModel.fetchLoggedInUsersOwnSessions()
                            setupSettingWorkManager(context)
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Login Successful!")
                            }
                            onLoginSuccess()
                        }
                    }

                }
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator( color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp))
                } else {
                    Text("Login")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    onRegister()
                }
            ) {
                Text("Register New User")
            }
        }

    }
}
