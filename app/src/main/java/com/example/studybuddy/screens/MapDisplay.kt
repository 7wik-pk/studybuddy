package com.example.studybuddy.screens

import android.content.ContentValues.TAG
import android.location.Geocoder
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.strToLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_CYAN
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ...

//a map that can be dragged and zoomed, if provided a list of sessions will display markers for each session
//users can select one of those markers to be directed to that session's page
@Composable
fun interactiveMap(
    modifier: Modifier = Modifier,
    location: LatLng = LatLng(0.0,0.0),
    sessions: List<StudySession> = listOf<StudySession>(),
    sessionChoice: MutableState<String>? = null
){
    var isMapLoaded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            onMapLoaded = { isMapLoaded = true },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId("9b64f3d4c6529bf14e96aee1")
            },
            cameraPositionState = cameraPositionState
        ){
            sessions.forEach { session ->
                val location = strToLatLng(session.location)
                Marker(
                    state= MarkerState(position = location),
                    title= session.name,
                    snippet= "${session.description}\n${session.sDate} at ${session.sTime}",
                    onClick= {
                        if (sessionChoice != null)
                            sessionChoice.value = session.sID
                        true
                    }
                )
                println("added session marker ${session.sID} at $location")
            }
        }

        if (!isMapLoaded) {
            AnimatedVisibility(
                modifier = Modifier.matchParentSize(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentSize()
                )
            }
        }
    }
}

//a map that the user can drop pins on, used to allow user to select location when creating sessions
@Composable
fun PinMap(
    modifier: Modifier = Modifier,
    userLocation: LatLng,
    sessionLocation: MutableState<LatLng>,
    edit: Boolean = false,
    onMapClick: () -> Unit = {}
){
    var isMapLoaded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var markerSet by remember { mutableStateOf(edit)  }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            onMapLoaded = { isMapLoaded = true },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId("9b64f3d4c6529bf14e96aee1")
            },
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                sessionLocation.value = latLng
                markerSet = true
                onMapClick()
            }
        ) {
            Marker("You are here", MarkerState(userLocation),
                icon = BitmapDescriptorFactory.defaultMarker(HUE_CYAN))
            if (markerSet)
                Marker("Session will be here", MarkerState(sessionLocation.value))
        }

        if (!isMapLoaded) {
            AnimatedVisibility(
                modifier = Modifier.matchParentSize(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentSize()
                )
            }
        }
    }
}

//a static map from API call, useful when you just want to display an image of a location
@Composable
fun StaticMap(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    onclick: () -> Unit = {},
    size: String = "250x200"
){

    val url = "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=$latitude,$longitude" +
            "&markers=$latitude,$longitude" +
            "&zoom=15" +
            "&size=$size" +
            "&maptype=roadmap" +
            "&key=AIzaSyAGn4zJ3rts9DotLgBxiZGctCew2uEm6E4"
    Image(
        painter = rememberAsyncImagePainter(model = url),
        contentDescription = "Static Google Map",
        modifier = modifier
            .clickable{
                onclick()
            },

    )
}

//a helper function for calculating the distance between two points
fun CalculateDistance(
    point1: LatLng,
    point2: LatLng
): Double{
    val distance = computeDistanceBetween(point1, point2)
    Log.i(TAG, "distance between $point1 and $point2 is $distance")
    return distance / 1000
}