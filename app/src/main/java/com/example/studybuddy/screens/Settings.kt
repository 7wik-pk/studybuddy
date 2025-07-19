package com.example.studybuddy.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studybuddy.R
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.screens.DropDownSelectOneTemplate
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.studybuddy.screens.validPassword
import com.example.studybuddy.managers.syncNow
import kotlinx.coroutines.delay

//allows a user to customize their app settings
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: StudyBuddyViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: androidx.compose.material.SnackbarHostState
) {
    val user = viewModel.loggedInUser
    val theme = remember { mutableStateOf(viewModel.theme) }
    val fontSize = remember { mutableStateOf(viewModel.fontSize) }
    val contrast = remember { mutableStateOf(viewModel.contrast) }

    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }


    Surface {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Page title
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // ------------ Account Section ------------
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("profile/${user.uid}")
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.accountcircle),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "${user.fName} ${user.lName}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to Profile"
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ------------ Accessibility Section ------------
            Text("Accessibility", style = MaterialTheme.typography.titleMedium)
            DropDownSelectOneTemplate(
                labelVal = "Theme",
                options = mutableListOf("Light", "Dark", "System Default"),
                textFieldValue = theme
            )
            DropDownSelectOneTemplate(
                labelVal = "Font Size",
                options = mutableListOf("Small", "Medium", "Large"),
                textFieldValue = fontSize
            )
            DropDownSelectOneTemplate(
                labelVal = "Contrast",
                options = mutableListOf("Normal", "High"),
                textFieldValue = contrast
            )

            Button(
                onClick = {
                    viewModel.saveAccessibilitySettings(theme.value, fontSize.value, contrast.value)
                    Log.d("Settings", "Saved: theme=${theme.value}, fontSize=${fontSize.value}, contrast=${contrast.value}")
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Save Accessibility Settings")
            }

            // ------------ Location Section ------------
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        viewModel.useLocation = !viewModel.useLocation
                        viewModel.savePrivacySettings(viewModel.useLocation)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.useLocation,
                    onCheckedChange = {
                        viewModel.useLocation = it
                        viewModel.savePrivacySettings(it)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use My Location")
            }

            // ------------ Password Section ------------
            Divider(modifier = Modifier.padding(vertical = 8.dp))

//        val password = remember { mutableStateOf("") }
//        val confirmPassword = remember { mutableStateOf("") }
//
//        Text("Security", style = MaterialTheme.typography.titleMedium)
//
//        Text(
//            text = "Email: ${viewModel.loggedInUser.email}",
//            style = MaterialTheme.typography.bodyMedium,
//            color = Color.Gray,
//            modifier = Modifier.padding(vertical = 4.dp)
//        )
//
//        PasswordFieldFunc(
//            labelVal = "Password",
//            iconId = Icons.Default.Lock,
//            textFieldValue = password
//        )
//
//        PasswordFieldFunc(
//            labelVal = "Confirm Password",
//            iconId = Icons.Default.Lock,
//            textFieldValue = confirmPassword,
//            isError = password.value != confirmPassword.value,
//            errSupportingText = if (password.value != confirmPassword.value) "Passwords do not match" else ""
//        )
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        Button(
//            onClick = {
//                when {
//                    password.value.isBlank() || confirmPassword.value.isBlank() -> {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            snackbarHostState.showSnackbar("All fields are required")
//                        }
//                    }
//                    password.value != confirmPassword.value -> {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            snackbarHostState.showSnackbar("Two passwords do not match")
//                        }
//                    }
//                    else -> {
//                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
//
//                        if (user != null) {
//                            user.updatePassword(password.value)
//                                .addOnSuccessListener {
//                                    CoroutineScope(Dispatchers.Main).launch {
//                                        snackbarHostState.showSnackbar("Password updated successfully")
//                                    }
//                                }
//                                .addOnFailureListener { e ->
//                                    CoroutineScope(Dispatchers.Main).launch {
//                                        snackbarHostState.showSnackbar("Failed to update password: ${e.message}")
//                                    }
//                                }
//                        } else {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                snackbarHostState.showSnackbar("No user is logged in")
//                            }
//                        }
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Confirm change password")
//        }
            val currentPassword = remember { mutableStateOf("") }
            val currentPasswordValid = remember { mutableStateOf(true) }
            val currentPasswordError = remember { mutableStateOf("") }

            val newPassword = remember { mutableStateOf("") }
            val newPasswordValid = remember { mutableStateOf(true) }
            val newPasswordError = remember { mutableStateOf("") }

            val confirmPassword = remember { mutableStateOf("") }
            val confirmPasswordValid = remember { mutableStateOf(true) }
            val confirmPasswordError = remember { mutableStateOf("") }

            Text("Security", style = MaterialTheme.typography.titleMedium)

            Text("Email: ${viewModel.loggedInUser.email}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            PasswordFieldFunc(
                labelVal = "Current Password",
                iconId = Icons.Default.Lock,
                textFieldValue = currentPassword,
                isError = !currentPasswordValid.value,
                errSupportingText = currentPasswordError.value
            )

            val passwordError = validPassword(newPassword.value)
            if (newPassword.value.isNotBlank()) {
                if (passwordError == null) {
                    newPasswordValid.value = true
                    newPasswordError.value = ""
                } else {
                    newPasswordValid.value = false
                    newPasswordError.value = passwordError
                }
            }

            PasswordFieldFunc(
                labelVal = "New Password",
                iconId = Icons.Default.Lock,
                textFieldValue = newPassword,
                isError = !newPasswordValid.value,
                errSupportingText = newPasswordError.value
            )

            if (confirmPassword.value.isNotBlank()) {
                if (confirmPassword.value == newPassword.value) {
                    confirmPasswordValid.value = true
                    confirmPasswordError.value = ""
                } else {
                    confirmPasswordValid.value = false
                    confirmPasswordError.value = "Passwords do not match"
                }
            }

            PasswordFieldFunc(
                labelVal = "Confirm New Password",
                iconId = Icons.Default.Lock,
                textFieldValue = confirmPassword,
                isError = !confirmPasswordValid.value,
                errSupportingText = confirmPasswordError.value
            )

            Button(
                onClick = {
                    val email = viewModel.loggedInUser.email
                    val user = FirebaseAuth.getInstance().currentUser

                    if (
                        currentPassword.value.isBlank() ||
                        newPassword.value.isBlank() ||
                        confirmPassword.value.isBlank()
                    ) {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("All fields are required")
                        }
                        return@Button
                    }

                    if (newPassword.value != confirmPassword.value) {
                        confirmPasswordValid.value = false
                        confirmPasswordError.value = "Passwords do not match"
                        return@Button
                    }

                    val validation = validPassword(newPassword.value)
                    if (validation != null) {
                        newPasswordValid.value = false
                        newPasswordError.value = validation
                        return@Button
                    }

                    val credential = EmailAuthProvider.getCredential(email, currentPassword.value)

                    user?.reauthenticate(credential)
                        ?.addOnSuccessListener {
                            user.updatePassword(newPassword.value)
                                .addOnSuccessListener {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Password updated successfully")
                                        currentPassword.value = ""
                                        newPassword.value = ""
                                        confirmPassword.value = ""
                                        syncNow(navController.context)
                                        delay(500)
                                        FirebaseAuth.getInstance().signOut()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Failed to update password: ${e.message}")
                                    }
                                }
                        }
                        ?.addOnFailureListener {
                            currentPasswordValid.value = false
                            currentPasswordError.value = "Incorrect current password"
                        }

                },
                modifier = Modifier.fillMaxWidth()) {
                Text("Confirm change password")
            }

            // ------------ Remove account Section ------------

            Spacer(modifier = Modifier.height(24.dp))

            Text("Danger Zone", style = MaterialTheme.typography.titleMedium)

            ElevatedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("DELETE ACCOUNT (NON-REVERSIBLE)", color = Color.White)
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false

                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                user.delete()
                                    .addOnSuccessListener {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            snackbarHostState.showSnackbar("Account deleted successfully")
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        CoroutineScope(Dispatchers.Main).launch {
                                            snackbarHostState.showSnackbar("Failed to delete account: ${e.message}")
                                        }
                                    }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("No user is logged in")
                                }
                            }

                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

    }

}
