package com.example.studybuddy.data

import android.app.Application
import android.util.Log
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.studybuddy.MapBoxApiResponse
import com.example.studybuddy.cityCoordinates
import com.example.studybuddy.data.entities.StudyGroup
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.User
import com.example.studybuddy.data.entities.emptySession
import com.example.studybuddy.data.entities.emptyUser
import com.example.studybuddy.data.entities.latLngToString
import com.example.studybuddy.data.repo.OwnGroupRepository
import com.example.studybuddy.data.repo.OwnSessionRepository
import com.example.studybuddy.mapBoxRepository
import com.example.studybuddy.monashClayton
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

//the view model for the app
class StudyBuddyViewModel(application: Application) : AndroidViewModel(application) {

    val firebaseClient: FirebaseClient = FirebaseClient()

    //Rooms
    private val ownGroupRepository: OwnGroupRepository = OwnGroupRepository(application)
	private val ownSessionRepository: OwnSessionRepository = OwnSessionRepository(application)

	// viewModel objects to be displayed
    var loggedInUser by mutableStateOf(emptyUser)

    var profileDetailsUser: User? by mutableStateOf(emptyUser)

    var loggedInUserUpcomingSessions: List<StudySession>? by mutableStateOf(emptyList())
    var loggedInUserPastSessions: List<StudySession>? by mutableStateOf(emptyList())
    var sessionDetailsSessionID: String? by mutableStateOf("")
    var sessionDetailsObj: StudySession? by mutableStateOf(emptySession)
    var sessionDetailsGroupName: String? by mutableStateOf("")
    var loggedInUserOwnSessions : List<StudySession>? by mutableStateOf(emptyList())
    private val _sessions = MutableStateFlow<List<StudySession>>(emptyList())
    val sessions: StateFlow<List<StudySession>> = _sessions

    // Retrofit

    private val repository = mapBoxRepository()

    val _geocoderResponse: MutableState <MapBoxApiResponse> =
        mutableStateOf(MapBoxApiResponse())

    var geocoderResponseCoords = mutableListOf<LatLng>()

    val _revGeocoderResponse: MutableState <MapBoxApiResponse> =
        mutableStateOf(MapBoxApiResponse())

    val revGeocoderResponseAddress = mutableStateOf("")

    //gets city by entering details
    fun geocodeCity(q: String) {

        viewModelScope.launch {
            try {
                _geocoderResponse.value = MapBoxApiResponse()
                geocoderResponseCoords = mutableListOf()

                val responseReturned = repository.geocodeCity(q)
                _geocoderResponse.value = responseReturned

                println("geocoding $q gave ${responseReturned.features}")

                if(responseReturned.features.isNotEmpty()){
                    responseReturned.features.forEach { f ->
                        geocoderResponseCoords.add(LatLng(
                            f.properties.coordinates.latitude,
                            f.properties.coordinates.longitude
                        ))
                    }
                }

//                println(geocoderResponseCoords)

            } catch (e: Exception) {
                Log.i("Error ", "Response failed ${e.stackTraceToString()}")
            }
        }
    }

    //gets point by entering details
    fun geocodePoint(q: String) {

        viewModelScope.launch {
            try {
                _geocoderResponse.value = MapBoxApiResponse()
                geocoderResponseCoords = mutableListOf()

                val responseReturned = repository.geocode(q, latLngToString(userLocation()), type = "address")
                _geocoderResponse.value = responseReturned

                println("geocoding $q gave ${responseReturned.features}")

                if(responseReturned.features.isNotEmpty()){
                    responseReturned.features.forEach { f ->
                        geocoderResponseCoords.add(LatLng(
                            f.properties.coordinates.latitude,
                            f.properties.coordinates.longitude
                        ))
                    }
                }

//                println(geocoderResponseCoords)

            } catch (e: Exception) {
                Log.i("Error ", "Response failed ${e.stackTraceToString()}")
            }
        }

    }

    //gets location by entering latitude and longitude
    fun revGeocode(latLongStr: String) {

        viewModelScope.launch {

            try {

                _revGeocoderResponse.value = MapBoxApiResponse()

                val responseReturned = repository.revGeocode(latLongStr)
                println(responseReturned)

                _revGeocoderResponse.value = responseReturned

                if(responseReturned.features.isNotEmpty())
                    revGeocoderResponseAddress.value = responseReturned.features[0].properties.fullAddress

            } catch (e: Exception) {
                Log.i("Error ", "Response failed ${e.stackTraceToString()}")
            }

        }
    }

    // Accessibility Settings
    var theme by mutableStateOf("System Default")
        private set
    var fontSize by mutableStateOf("Medium")
        private set
    var contrast by mutableStateOf("Normal")
        private set
    var useLocation by mutableStateOf(false)

    var hasPendingSettingsUpdate by mutableStateOf(false)

    //gets the user location
    fun userLocation(): LatLng{
        return if(useLocation)
            monashClayton
        else
            cityCoordinates[loggedInUser.city] ?: monashClayton
    }

    // Firebase functions
    //sets profileDetailsUser so it can be used asynchronously
    fun fetchUserForProfileDetails(userID: String) = viewModelScope.launch(Dispatchers.IO) {
        profileDetailsUser = emptyUser
        profileDetailsUser = firebaseClient.fbGetUserByUID(userID)
        println("profileDetailsUser in vm after fbase fetch: $profileDetailsUser")
    }

    //sets sessionDetailsObj so it can be used asynchronously
    fun fetchSessionForSessionDetails(sessionID: String) = viewModelScope.launch(Dispatchers.IO) {
        sessionDetailsObj = emptySession
        sessionDetailsGroupName = ""
        sessionDetailsObj = firebaseClient.fbGetSession(sessionID)

        if(sessionDetailsObj!=null)
            revGeocode(sessionDetailsObj!!.location)

        println("sessionDetailsObj in vm after fbase fetch: $sessionDetailsObj")
//        if (sessionDetailsObj != null)
//            sessionDetailsGroupName = firebaseClient.fbGetGroup(sessionDetailsObj!!.groupID)?.name

    }

    //sets new attendees for a session
    fun updateSessionAttendees(sessionID: String, attendees: String) = viewModelScope.launch(Dispatchers.IO) {
        val session = firebaseClient.fbGetSession(sessionID)
        if(session != null) {
            val newSession = StudySession(
                sID = session.sID,
                name = session.name,
                description = session.description,
                fieldOfStudy = session.fieldOfStudy,
                sDate = session.sDate,
                sTime = session.sTime,
                location = session.location,
                capacity = session.capacity,
                attendeeUIDs = attendees,
                ownerUID = session.ownerUID,
                groupID = session.groupID
            )
            sessionDetailsObj = newSession
            if (session.ownerUID == loggedInUser.uid) {
                updateSessionRoomAndFirebase(newSession)
            } else
                firebaseClient.fbAlterSession(newSession)

        }

    }

    //fetches the upcoming sessions for the logged in user
    fun fetchLoggedInUsersUpcomingSessions() = viewModelScope.launch {
        loggedInUserUpcomingSessions = emptyList()

        loggedInUserUpcomingSessions = firebaseClient.fbSearchSessions(
            user = loggedInUser,
            userLocation = userLocation(),
            futureSessionsOnly = true,
            userInAttendance = true
        )

        println("logged in user's upcoming sessions size: ${loggedInUserUpcomingSessions?.size ?: 0}")

        // DEBUG: checking to see if firebase is reading attendeeUIDs properly
//        println("attendees in ${loggedInUserUpcomingSessions!![0].name} : ${loggedInUserUpcomingSessions!![0].attendeeUIDs}")

    }

    //fetches the past sessions for the logged in user
    fun fetchLoggedInUsersPastSessions() = viewModelScope.launch {
        loggedInUserPastSessions = emptyList()

        loggedInUserPastSessions = firebaseClient.fbSearchSessions(
            user = loggedInUser,
            userLocation = userLocation(),
            futureSessionsOnly = false,
            userInAttendance = true,
            pastSessionsOnly = true
        )

        println("logged in user's past sessions size: ${loggedInUserPastSessions?.size ?: 0}")

        // DEBUG: checking to see if firebase is reading attendeeUIDs properly
//        println("attendees in ${loggedInUserUpcomingSessions!![0].name} : ${loggedInUserUpcomingSessions!![0].attendeeUIDs}")

    }



    fun fetchLoggedInUsersOwnSessions() = viewModelScope.launch {

        loggedInUserOwnSessions = emptyList()
        loggedInUserOwnSessions = firebaseClient.fbGetSessions(
            ownerUID = loggedInUser.uid
        )
        deleteAllSessions()
        println("Length of own "+ loggedInUserOwnSessions!!.size)
        if(!loggedInUserOwnSessions.isNullOrEmpty())
        {
            for (session in loggedInUserOwnSessions!!)
            {
                insertOwnSession(session)
            }
        }

//        println("logged in user's upcoming sessions size: ${loggedInUserUpcomingSessions?.size ?: 0}")
    }

    // only fetches sessions that will begin in the future
    fun fetchSessions(
        searchQuery: String? = null,
        date: String? = null,
        time: String? = null,
        interests: String? = null,
        distance: Int = 0,
        containsFriends: Boolean = false,
        inGroup: Boolean = false,
        searchDescription: Boolean = true
    ) = viewModelScope.launch(Dispatchers.IO){
        var descSearch = if(searchDescription) searchQuery else ""

        val selectedSessions = firebaseClient.fbSearchSessions(
			searchTerm = searchQuery,
            descSearch = descSearch,
			date = date,
			time = time,
			interests = interests,
			distance = distance,
			containsFriends = containsFriends,
			inGroups = inGroup,
			user = loggedInUser,
            userLocation = userLocation(),
            futureSessionsOnly = true
		)

        _sessions.value = selectedSessions
    }

    // Combined Firebase and Room operations
    fun insertSessionRoomAndFirebase(studySession: StudySession)= viewModelScope.launch(Dispatchers.IO) {
        val docID = firebaseClient.fbCreateSession(studySession)
        studySession.sID = docID
        ownSessionRepository.insert(studySession)

    }

    fun updateSessionRoomAndFirebase(studySession: StudySession)= viewModelScope.launch(Dispatchers.IO) {
        ownSessionRepository.update(studySession)
        firebaseClient.fbAlterSession(studySession)
    }

    fun deleteSessionRoomAndFirebase(studySession: StudySession)= viewModelScope.launch(Dispatchers.IO) {
        ownSessionRepository.delete(studySession)
        firebaseClient.fbDeleteSession(studySession)
        sessionDetailsObj = emptySession
    }

    // Room functions

    val ownGroups: Flow<List<StudyGroup>> = ownGroupRepository.allOwnGroups

    fun insertOwnGroup(studyGroup: StudyGroup) = viewModelScope.launch(Dispatchers.IO) {
        ownGroupRepository.insert(studyGroup)
    }

    fun updateOwnGroup(studyGroup: StudyGroup) = viewModelScope.launch(Dispatchers.IO) {
        ownGroupRepository.update(studyGroup)
    }

    fun deleteOwnGroup(studyGroup: StudyGroup) = viewModelScope.launch(Dispatchers.IO) {
        ownGroupRepository.delete(studyGroup)
    }

    val allOwnSessions: Flow<List<StudySession>> = ownSessionRepository.allOwnSessions

    fun insertOwnSession(studySession: StudySession) = viewModelScope.launch(Dispatchers.IO) {
        ownSessionRepository.insert(studySession)
    }

    fun updateOwnSession(studySession: StudySession) = viewModelScope.launch(Dispatchers.IO) {
        ownSessionRepository.update(studySession)
    }

    fun deleteOwnSession(studySession: StudySession) = viewModelScope.launch(Dispatchers.IO) {
        ownSessionRepository.delete(studySession)
    }

    fun deleteAllSessions() = viewModelScope.launch(Dispatchers.IO) {
        ownSessionRepository.deleteAll()
    }


//    fun saveAccessibilitySettings(newTheme: String, newFontSize: String, newContrast: String) {
//        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
//        val userId = user.uid
//
//        val settings = hashMapOf(
//            "theme" to newTheme,
//            "fontSize" to newFontSize,
//            "contrast" to newContrast
//        )
//
//        com.google.firebase.firestore.FirebaseFirestore.getInstance()
//            .collection("userSettings")
//            .document(userId)
//            .set(settings)
//            .addOnSuccessListener {
//                android.util.Log.d("Settings", "Accessibility settings saved to Firestore")
//            }
//            .addOnFailureListener { e ->
//                android.util.Log.e("Settings", "Error saving settings: ${e.message}")
//            }
//
//        theme = newTheme
//        fontSize = newFontSize
//        contrast = newContrast
//    }
//
//    fun savePrivacySettings(locationEnabled: Boolean) {
//        useLocation = locationEnabled
//
//        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
//        val userId = user.uid
//
//        com.google.firebase.firestore.FirebaseFirestore.getInstance()
//            .collection("userSettings")
//            .document(userId)
//            .update("useLocation", locationEnabled)
//            .addOnSuccessListener {
//                android.util.Log.d("Settings", "Location setting saved to Firestore: $locationEnabled")
//            }
//            .addOnFailureListener { e ->
//                val settings = hashMapOf("useLocation" to locationEnabled)
//                com.google.firebase.firestore.FirebaseFirestore.getInstance()
//                    .collection("userSettings")
//                    .document(userId)
//                    .set(settings)
//                    .addOnSuccessListener {
//                        android.util.Log.d("Settings", "Location setting initialized in Firestore")
//                    }
//                    .addOnFailureListener { ex ->
//                        android.util.Log.e("Settings", "Failed to save location setting: ${ex.message}")
//                    }
//            }
//    }

    fun fetchUserSettings(userId: String) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("userSettings")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    theme = document.getString("theme") ?: "System Default"
                    fontSize = document.getString("fontSize") ?: "Medium"
                    contrast = document.getString("contrast") ?: "Normal"
                    useLocation = document.getBoolean("useLocation") == true
                    Log.d("Settings", "Loaded settings: $theme, $fontSize, $contrast")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Settings", "Failed to load settings: ${e.message}")
            }
    }

    fun pendingSettings() {
        hasPendingSettingsUpdate = true
    }

    fun updateSettings() {
        hasPendingSettingsUpdate = false
    }

    fun saveAccessibilitySettings(newTheme: String, newFontSize: String, newContrast: String) {
        theme = newTheme
        fontSize = newFontSize
        contrast = newContrast

        val sharedPref = getApplication<Application>().getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString("theme", newTheme)
            .putString("fontSize", newFontSize)
            .putString("contrast", newContrast)
            .putBoolean("hasPendingSettingsUpdate", true)
            .apply()

        pendingSettings()
    }

    fun savePrivacySettings(locationEnabled: Boolean) {
        useLocation = locationEnabled

        val sharedPref = getApplication<Application>().getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putBoolean("useLocation", locationEnabled)
            .putBoolean("hasPendingSettingsUpdate", true)
            .apply()

        pendingSettings()
    }
}