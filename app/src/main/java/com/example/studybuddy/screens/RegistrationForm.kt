package com.example.studybuddy.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.studybuddy.R
import com.example.studybuddy.cities
import com.example.studybuddy.data.FirebaseClient
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.User
import com.example.studybuddy.fieldsOfStudy
import com.example.studybuddy.standardSpacerHeight
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

//ensures the password being entered conforms to standards
fun validPassword(password: String): String?
{
    if(password.length < 8)
    {
        return "Minimum 8 Character Password"
    }
    if(!password.matches(".*[A-Z].*".toRegex()))
    {
        return "Must Contain 1 Upper-case Character"
    }
    if(!password.matches(".*[a-z].*".toRegex()))
    {
        return "Must Contain 1 Lower-case Character"
    }
    if(!password.matches(".*[!@#\$%^&+=()].*".toRegex()))
    {
        return "Must Contain 1 Special Character (!@#\$%^&+=())"
    }

    return null
}


@Composable
// can be used for either registration or editing profile - user arg needs to be provided for edit behaviour
// TODO check if email already exists
fun RegistrationOrEditForm(
    navController: NavController,
    modifier: Modifier,
    studyBuddyViewModel: StudyBuddyViewModel? = null,
    onRegistrationSuccess: () -> Unit = {},
    onEditSuccess: () -> Unit = {},
    onGoBack: () -> Unit = {},
    user: User? = null,
    snackbarHostState: androidx.compose.material.SnackbarHostState
) {
    val firstNameValid = remember { mutableStateOf(true) }
    val lastNameValid = remember { mutableStateOf(true) }
    val emailValid = remember { mutableStateOf(true) }
    val passwordValid = remember { mutableStateOf(true) }
    val confirmPasswordValid = remember { mutableStateOf(true) }

    val firstName = remember { mutableStateOf(user?.fName ?: "") }
    val lastName = remember { mutableStateOf(user?.lName ?: "") }
    val gender = remember { mutableStateOf(user?.gender ?: "") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val city = remember { mutableStateOf(user?.city ?: "") }
    val interests = remember { mutableStateOf(user?.interests ?: "") }
    val pronouns = remember { mutableStateOf(user?.pronouns ?: "") }
    val dob = remember { mutableStateOf(user?.dob ?: "") }

    val checkedTAndCBox = remember { mutableStateOf(user != null) }

    val scope = rememberCoroutineScope()

    val isLoading = remember { mutableStateOf(false) }

    Surface {

        Surface (
            modifier = modifier
                .fillMaxSize()
                .padding(28.dp))
        {
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())){

//            Spacer(modifier = Modifier.height(100.dp))

                if (user == null) {
                    DisplayHeading(stringResource(R.string.reg_header), TextAlign.Center)
                }
                else{
                    DisplayHeading("Edit Profile", TextAlign.Center)
                }

                TextFieldTemplate(
                    labelVal = stringResource(R.string.first_name),
                    Icons.Filled.Person,
                    firstName,
                    isMandatory = true,
                    singleLine = true,
                    isError = !firstNameValid.value,
                    errSupportingText = "Cannot be empty!"
                )
                if(firstName.value.isNotEmpty()){
                    firstNameValid.value = true

                }

                Spacer(modifier = Modifier.height(standardSpacerHeight))

                TextFieldTemplate(
                    labelVal = stringResource(R.string.last_name),
                    Icons.Filled.Person,
                    lastName,
                    isMandatory = true,
                    singleLine = true,
                    isError = !lastNameValid.value,
                    errSupportingText = "Cannot be empty!"
                )
                if(lastName.value.isNotEmpty())
                {
                    lastNameValid.value = true
                }

                Spacer(modifier = Modifier.height(standardSpacerHeight))
                DatePickerTemplate(dob, stringResource(R.string.dob), isMandatory = false)
                Spacer(modifier = Modifier.height(standardSpacerHeight))

                Spacer(modifier = Modifier.height(standardSpacerHeight))

                val genderOptions = mutableListOf("Male", "Female", "Other")
                DropDownSelectOneTemplate(stringResource(R.string.gender), Icons.Filled.Person ,genderOptions, gender )
                Spacer(modifier = Modifier.height(standardSpacerHeight))
                Spacer(modifier = Modifier.height(standardSpacerHeight))
                val pronounsOptions = mutableListOf("she/her", "he/him", "they/them", "she/them", "he/them")
                DropDownSelectOneTemplate(stringResource(R.string.pronouns), Icons.Filled.Person ,pronounsOptions, pronouns )
                Spacer(modifier = Modifier.height(standardSpacerHeight))
                Spacer(modifier = Modifier.height(standardSpacerHeight))
                val emailString = remember { mutableStateOf("Cannot be empty!") }
                if (user == null) {
                    TextFieldTemplate(
                        labelVal = stringResource(R.string.email),
                        Icons.Filled.Email,
                        email,
                        isMandatory = true,
                        singleLine = true,
                        isError = !emailValid.value,
                        errSupportingText = emailString.value
                    )
                    if(email.value.isNotEmpty())
                    {
                        emailValid.value = true
                        if(!Patterns.EMAIL_ADDRESS.matcher(email.value).matches()){
                            emailValid.value = false
                            emailString.value = "Invalid email!"
                        }

                    }
                    Spacer(modifier = Modifier.height(standardSpacerHeight))
                    val passwordString = remember { mutableStateOf("Cannot be empty!") }
                    PasswordFieldFunc(
                        labelVal = stringResource(R.string.password),
                        Icons.Filled.Lock,
                        password,
                        isError = !passwordValid.value,
                        errSupportingText = passwordString.value
                    )
                    if(password.value.isNotEmpty())
                    {

                        if(validPassword(password.value) == null)
                        {
                            passwordValid.value = true
                        }
                        else
                        {
                            passwordValid.value = false
                            passwordString.value = validPassword(password.value).toString()
                        }


                    }
                    Spacer(modifier = Modifier.height(standardSpacerHeight))
                    val confirmPassWordString = remember { mutableStateOf("Cannot be empty!") }
                    PasswordFieldFunc(labelVal = "Confirm Password", Icons.Filled.Lock, confirmPassword, isError = !confirmPasswordValid.value,
                        errSupportingText = confirmPassWordString.value)
                    if(confirmPassword.value.isNotEmpty())
                    {
                        if(confirmPassword.value == password.value)
                        {
                            confirmPasswordValid.value = true
                        }
                        else
                        {
                            confirmPasswordValid.value = false
                            confirmPassWordString.value = "Passwords don't match!"
                        }
                    }
                    Spacer(modifier = Modifier.height(standardSpacerHeight))
                }
                val cityOptions = mutableListOf(*cities.toTypedArray())
                DropDownSelectOneTemplate(stringResource(R.string.city), Icons.Filled.LocationOn ,cityOptions, city )

                Spacer(modifier = Modifier.height(standardSpacerHeight))
                Spacer(modifier = Modifier.height(standardSpacerHeight))
                val fosOptions = mutableListOf(*fieldsOfStudy.toTypedArray())
                DropDownSelectMultipleTemplate(stringResource(R.string.fos), Icons.Filled.Search , fosOptions, interests )
                Spacer(modifier = Modifier.height(20.dp))


                if (user == null) {
                    TermsConditionsElement(checkedTAndCBox)
                }
                Column(modifier =  Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ){

                        Button(
                            onClick = onGoBack
                        ) {
                            Text("Go Back")
                        }

                        Button(
                            enabled = checkedTAndCBox.value,
                            onClick = {
                                if(firstName.value.isEmpty()) {
                                    firstNameValid.value = false

                                }
                                if(lastName.value.isEmpty()) {
                                    lastNameValid.value = false

                                }
                                if(email.value.isEmpty()) {
                                    emailValid.value = false

                                }
                                if(password.value.isEmpty()) {
                                    passwordValid.value = false

                                }
                                if(confirmPassword.value.isEmpty()) {
                                    confirmPasswordValid.value = false
                                }

                                if(user != null)
                                {
                                    println("editing user $user")
                                    if(lastNameValid.value and firstNameValid.value)
                                    {
                                        val text = "Updating user..!"
                                        CoroutineScope(Dispatchers.Main).launch {
                                            snackbarHostState.showSnackbar(text)
                                        }

                                        // TODO test

                                        val updatedUser = User(
                                            uid = user.uid,
                                            fName = firstName.value,
                                            lName = lastName.value,
                                            email = user.email,
                                            gender = gender.value,
                                            pronouns = pronouns.value,
                                            dob = dob.value,
                                            interests = interests.value,
                                            city = city.value,
                                            passwordHash = user.passwordHash,
                                            profilePicturePath = "",
                                            friendIDs = "",
                                        )

                                        scope.launch {
//                                        println("before fbUpdateUser() call")
                                            println("$studyBuddyViewModel")
                                            val res = studyBuddyViewModel!!.firebaseClient.fbUpdateUser(
                                                updatedUser
                                            )
//                                        println("after fbUpdateUser() call, res: $res")
                                            studyBuddyViewModel.loggedInUser = studyBuddyViewModel.firebaseClient.fbGetUserByUID(updatedUser.uid)!!
//                                        print("vm loggedInUser: ${studyBuddyViewModel?.loggedInUser}")
                                            onEditSuccess()
                                        }
                                    }
                                }
                                else {
                                    if (emailValid.value and confirmPasswordValid.value and passwordValid.value and lastNameValid.value and firstNameValid.value ){
                                        val text = "Registering user..!"
                                        val newUser = User(
                                            fName = firstName.value,
                                            lName = lastName.value,
                                            email = email.value,
                                            gender = gender.value,
                                            pronouns = pronouns.value,
                                            dob = dob.value,
                                            interests = interests.value,
                                            city = city.value,
                                            passwordHash = password.value,
                                            profilePicturePath = "",
                                            uid = email.value,
                                            friendIDs = ""
                                        )

                                        scope.launch {
                                            isLoading.value = true
                                            delay(500)
                                            studyBuddyViewModel?.firebaseClient?.registerUser(newUser)
                                            isLoading.value = false
                                            delay(500)
                                            snackbarHostState.showSnackbar(text)
                                            onRegistrationSuccess()
                                        }
                                    }
                                }
                            }
                        ) {
                            if (isLoading.value) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(2.dp)
                                )
                            } else {
                                Text(text = if (user != null) "Save" else "Register")
                            }
                        }
                    }

                }

            }

        }

    }

}

