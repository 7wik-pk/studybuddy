package com.example.studybuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.studybuddy.data.StudyBuddyViewModel
import com.example.studybuddy.data.entities.latLngToString
import com.example.studybuddy.data.entities.strToLatLng
import com.example.studybuddy.monashClayton

//a test function for retrofit
@Composable
fun MapBoxTest(vm: StudyBuddyViewModel) {

    val geo = remember { mutableStateOf("") }

    Column {

        TextFieldTemplate("enter geo text: ", Icons.Filled.LocationSearching, geo, onValueChange = {vm.geocodeCity(geo.value)})

        if (vm._geocoderResponse.value.features.isNotEmpty()) {
            vm.geocoderResponseCoords.forEach {
                coords -> Text(text = latLngToString(coords))
            }
        }

        // -37.9105,145.1347

        println("hi ${vm._revGeocoderResponse.value}")

    }

}