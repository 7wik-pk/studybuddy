package com.example.studybuddy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studybuddy.R
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.strToGeopoint
import com.example.studybuddy.data.entities.testSessionsList
import com.example.studybuddy.standardFontSize
import com.example.studybuddy.ui.theme.Primary
import com.example.studybuddy.ui.theme.StudyBuddyTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.Locale

//displays a heading from the provided String
@Composable
fun DisplayHeading(
    headingText: String,
    textAlign: TextAlign = TextAlign.Center
){
    Text(
        text = headingText,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),

        style = TextStyle(
            textAlign = TextAlign.Unspecified,
            textDirection = TextDirection.ContentOrLtr,
            lineBreak = LineBreak.Heading,
            hyphens = Hyphens.Auto,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        ),

        textAlign = textAlign
    )
}

// need to make normal text func

//displays a textfield the user can write in, extra functionality to do something on value change is possible
@Composable
fun TextFieldTemplate(
    labelVal: String,
    icon : ImageVector,
    textFieldValue: MutableState<String>,
    modifier: Modifier = Modifier,
    isMandatory: Boolean = false,
    isError: Boolean = false,
    errSupportingText: String = "",
    singleLine: Boolean = false,
    isNumeric: Boolean = false,
    onValueChange: () -> Unit = {}
){

    var keyboardOptions: KeyboardOptions = KeyboardOptions.Default
    if (isNumeric)
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = {
            Row {
                Text(labelVal)
                if (isMandatory) {
                    Text(" *", color = Color.Red)
                }
            }
        },
        keyboardOptions = keyboardOptions,
        value = textFieldValue.value ,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Primary,
            focusedLabelColor = Primary,
            cursorColor =  Primary,

            ),
        onValueChange = {
            textFieldValue.value = it
            onValueChange()
        },
        leadingIcon = { Icon(icon, contentDescription = "leading icon") },
        singleLine = singleLine,
        isError = isError,
        supportingText = {
            if(isError){
                Text(errSupportingText)
            }
        }
    )
}

//a field for users to enter sensitive details like passwords
@Composable
fun PasswordFieldFunc(
    labelVal: String,
    iconId : ImageVector,
    textFieldValue: MutableState<String>,
    isError: Boolean = false,
    errSupportingText: String = ""
){

    val passwordVisible = remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Row {
                Text(labelVal)
                Text(" *", color = Color.Red)
            }
        },

        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        value = textFieldValue.value ,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Primary,
            focusedLabelColor = Primary,
            cursorColor =  Primary,

            ),

        onValueChange = {
            textFieldValue.value = it
        },
        visualTransformation = if (passwordVisible.value) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        leadingIcon = { Icon(iconId, contentDescription = "") },
        trailingIcon = {
            val imgIcon = if(passwordVisible.value)
            { Icons.Filled.Visibility }
            else{
                Icons.Filled.VisibilityOff
            }

            IconButton(onClick = {passwordVisible.value = !passwordVisible.value}) {
                Icon(imgIcon, contentDescription = "")
            }
        },
        isError = isError,
        supportingText = {
            if(isError){
                Text(errSupportingText)
            }
        }
    )
}

//a dropdown menu where the user can only select one of the provided options
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownSelectOneTemplate(
    labelVal: String,
    iconId : ImageVector? = null,
    options: MutableList<String>,
    textFieldValue: MutableState<String>,
    isMandatory: Boolean = false
){
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(textFieldValue.value)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            state = textFieldState,
            readOnly = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
            ),
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Row {
                Text(labelVal)

                if (isMandatory){
                    Text(" *", color = Color.Red)
                }
            }},
            leadingIcon = {
                if (iconId != null) {
                    Icon(iconId, contentDescription = "")
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        expanded = false
                        textFieldValue.value = option
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

//a dropdown menu where the user can select many of the provided options
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownSelectMultipleTemplate(
    labelVal: String,
    iconId : ImageVector? = null,
    options: MutableList<String>,
    textFieldValue: MutableState<String>,
    modifier: Modifier = Modifier.size(200.dp)
) {
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()
    val selectedOptions = mutableListOf<String>()

    if (textFieldValue.value.isNotBlank()){
        selectedOptions.addAll(textFieldValue.value.toString().split(","))
        textFieldState.setTextAndPlaceCursorAtEnd(selectedOptions.joinToString(separator = ", "))
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            state = textFieldState,
            readOnly = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
            ),
            label = { Text(labelVal) },
            leadingIcon = {
                if (iconId != null) {
                    Icon(iconId, contentDescription = "")
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Box(modifier){
                LazyColumn {
                    items(options.size){index->
                        Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End
                            ){
                                Checkbox(
                                    checked = options[index] in selectedOptions,
                                    onCheckedChange = {
                                        if (it) selectedOptions.add(options[index])
                                        else selectedOptions.remove(options[index])
                                        textFieldValue.value = selectedOptions.joinToString(separator = ",")
                                        textFieldState.setTextAndPlaceCursorAtEnd(selectedOptions.joinToString(separator = ", "))
                                    })
                            }
                            Column( modifier = Modifier.wrapContentWidth(),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center){
                                Text(
                                    options[index]
                                )
                            }
                        }
                    }
                }
            }
        }

    }

}

//not in use
/*@Composable
fun DropDownTemplate(
    labelVal: String,
    iconId : ImageVector? = null,
    textFieldValue: MutableState<String>,
){
    if (iconId != null) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelVal) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            value = textFieldValue.value,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
            ),
            onValueChange = {
                textFieldValue.value = it
            },
            leadingIcon = { Icon(iconId, contentDescription = "") },
            trailingIcon = {
                IconButton(onClick = {

                }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "")
                }
            }
        )
    }
    else{
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(labelVal) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            value = textFieldValue.value,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
            ),
            onValueChange = {
                textFieldValue.value = it
            },
            trailingIcon = {
                IconButton(onClick = {

                }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "")
                }
            }
        )
    }
}*/

//displays a checkbox
@Composable
fun CheckBoxTemplate(
    checkedBox: MutableState<Boolean>,
    textToDisplay: String
){

    Row(
        verticalAlignment = Alignment.CenterVertically
    ){

        Checkbox(checked = checkedBox.value, onCheckedChange = {
            checkedBox.value = !checkedBox.value
        })

        Text(
            text = textToDisplay,
            style = TextStyle(
                fontSize = standardFontSize
            ),
            textAlign = TextAlign.Start
        )
    }

}

//displays a calendar for the user to select a date from
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerTemplate(
    inputDate: MutableState<String>,
    labelVal: String,
    fieldIcon: ImageVector = Icons.Filled.CalendarMonth,
    isMandatory: Boolean = false
) {
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    var showDatePicker by remember {
        mutableStateOf(false)
    }
    var selectedDate by remember {
        mutableStateOf(calendar.timeInMillis)
    }
    Column() {
        OutlinedTextField(
            value = inputDate.value,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Primary,
                focusedLabelColor = Primary,
                cursorColor = Primary,
            ),
            onValueChange = {},
            readOnly = true,
            label = {
                Row {
                    Text(labelVal)

                    if (isMandatory){
                        Text(" *", color = Color.Red)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            leadingIcon = {
                Icon(
                    fieldIcon,
                    contentDescription = "Select Date",
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .size(40.dp)
                )
            }
        )
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        selectedDate = datePickerState.selectedDateMillis!!
                        inputDate.value = formatter.format(Date(selectedDate))
                    }) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text(text = "Cancel")
                    }
                }
            ) //end of dialog
            { //still column scope
                DatePicker(
                    state = datePickerState
                )
            }
        }// end of if
    }
}

//displays the terms and conditions checkbox
@Composable
fun TermsConditionsElement(
    checkedBox: MutableState<Boolean>
){

    Row(){
        Checkbox(checked = checkedBox.value, onCheckedChange = {
            checkedBox.value = !checkedBox.value
        })

        val termsString = "By checking you agree to our Terms of Service and Privacy Policy"

        Text(
            text = termsString,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraLight
            )
        )
    }
}

//displays a list of sessions in a lazy column, user can click on them to do something
@Composable
fun LazySessionsColumn(
    sessionsList: List<StudySession>,
    sessionChoice: MutableState<String>? = null){

    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start)
    {
        items(sessionsList.size) { index ->

            val location = strToGeopoint(sessionsList[index].location)

//            println(sessionsList[index])

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colorScheme.onSecondaryContainer)
                    .wrapContentWidth()
                    .clickable {
                        if (sessionChoice != null)
                            sessionChoice.value = sessionsList[index].sID
                    },
                verticalAlignment = Alignment.CenterVertically
            ){
                StaticMap(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    modifier = Modifier.height(140.dp).width(200.dp).padding(4.dp),
                    onclick = {sessionChoice?.value = sessionsList[index].sID}
                )
                Column (
                    modifier = Modifier
                        .clip(RoundedCornerShape(10))
                        .background(MaterialTheme.colorScheme.onSecondaryContainer)
                        .padding(4.dp)
                        .wrapContentWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        sessionsList[index].name,
                        color = MaterialTheme.colorScheme.onSecondary,
                        softWrap = true,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        "Field of Study: ${sessionsList[index].fieldOfStudy}",
                        color = MaterialTheme.colorScheme.onSecondary,
                        softWrap = true
                    )
                    Text(
                        "Date and time: ${sessionsList[index].sDate} at ${sessionsList[index].sTime}",
                        color = MaterialTheme.colorScheme.onSecondary,
                        softWrap = true,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }
        }
    }
}

//displays a list of sessions in a lazy row, user can click on them to do something
@Composable
fun LazySessionRow(sessionsList: List<StudySession>,
                   sessionChoice: MutableState<String>? = null){
    LazyRow(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        items(sessionsList.size) { index ->
            val location = strToGeopoint(sessionsList[index].location)

            Column (
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colorScheme.onSecondaryContainer)
                    .wrapContentWidth()
                    .clickable {
                        if (sessionChoice != null)
                            sessionChoice.value = sessionsList[index].sID
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    sessionsList[index].name,
                    color = MaterialTheme.colorScheme.onSecondary,
                    softWrap = true,
                    modifier = Modifier.padding(top = 8.dp)
                )
                StaticMap(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    modifier = Modifier.size(200.dp),
                    onclick = { sessionChoice?.value = sessionsList[index].sID },
                    size = "300x240"
                )
                Text(
                    "Field of Study: ${sessionsList[index].fieldOfStudy}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    softWrap = true
                )
                Text(
                    "Date and time: ${sessionsList[index].sDate} at ${sessionsList[index].sTime}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    softWrap = true,
                    modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 8.dp)
                )
            }
        }
    }
}

//displays a list of profiles
@Composable
fun ProfileList(num: Int, content: String, friend: Boolean = false){
    LazyColumn (modifier = Modifier
        .background(MaterialTheme.colorScheme.inversePrimary)
        .padding(4.dp)) {
        items(num) { index ->
            Row (modifier = Modifier
                .padding(vertical = 2.dp)
                .background(
                    color = if (index % 2 == 0) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                ),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically) {
                if (friend){
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile Picture")
                }
                Column {
                    Text(
                        "$content: $index",
                        color = if(index%2 == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Subject: <subject>",
                        color = if(index%2 == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Row{
                    Icon(Icons.Default.AddComment, contentDescription = "Comments")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Options")
                }
            }
        }
    }
}

//sign in with google  (not used)
@Composable
fun GoogleSignIn(modifier: Modifier, text: String){
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {}
            .height(40.dp)
            .width(120.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){

        Image(painter = painterResource(id = R.drawable.google_icon) , contentDescription = "Google", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray))
    }

}

@Preview(showBackground = true)
@Composable
fun UIPreview() {
    StudyBuddyTheme {
//        LazySessionsColumn(testSessionsList)
        LazySessionRow(testSessionsList)
    }
}
