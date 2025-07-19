package com.example.studybuddy.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studybuddy.data.entities.StudyGroup
import com.example.studybuddy.data.entities.StudySession
import com.example.studybuddy.data.entities.User
import com.example.studybuddy.data.entities.geopointToString
import com.example.studybuddy.data.entities.getCurrentDateTimestamp
import com.example.studybuddy.data.entities.listToString
import com.example.studybuddy.data.entities.parseDateTimeToTimestamp
import com.example.studybuddy.data.entities.parseTimestamp
import com.example.studybuddy.data.entities.strToGeopoint
import com.example.studybuddy.data.entities.stringToList
import com.example.studybuddy.screens.CalculateDistance
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//the commands to interact with the firebase services
class FirebaseClient {

	// Firebase
	private val auth: FirebaseAuth = Firebase.auth
	private val firestore: FirebaseFirestore = Firebase.firestore
	private val usersCollection = firestore.collection("users")
	private val sessionsCollection = firestore.collection("sessions")
	private val groupsCollection = firestore.collection("groups")

	fun getLoggedInUserUID(): String? {
		return auth.currentUser?.uid
	}

	//sends an email to the user to reset password
	suspend fun sendResetPasswordEmail(email: String) : Boolean{
		var status:Boolean = true
		val users = fbGetUsersByEmail(email)
		if(users.isEmpty())
			return false
		auth.sendPasswordResetEmail(email).addOnFailureListener { e ->
			status = false
		}.await()
		return status
	}

    // Login with email and password
    suspend fun loginUser(email: String, password: String, wrongEmail: MutableState<Boolean>, wrongPassword: MutableState<Boolean>): User? {

        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
            if (uid != null) {
//                println("logged in user ID: $uid")
                val user = fbGetUsersByEmail(email)[0]
//				val user = fbGetUserByUID(uid)
//                println("logged in user: ${user.fName}, {${user.email}}")

                return user
            } else {
                wrongEmail.value = true
                wrongPassword.value = true
                return null
            }
        }

        catch (e: Exception) {

            val users = fbGetUsersByEmail(email)

            if (users.isEmpty()) {
                wrongEmail.value = true
                wrongPassword.value = false
                return null
            }

            wrongEmail.value = false
            wrongPassword.value = true

            return null
        }

    }

    // Register a new user
    suspend fun registerUser(
        user: User
    ): String? {
        // Create user in Firebase Auth
        val result = auth.createUserWithEmailAndPassword(user.email, user.passwordHash).await()
        val uid = result.user?.uid

        if (uid != null) {
            user.uid = uid

            // Save to Firestore
            fbCreateUser(user)

            println("New user registered with UID: $uid")
            return uid
        } else {
            return null
        }
    }

    // Sign out
    fun signOut() {
        auth.signOut()
    }

    // Save user data to Firestore
    private suspend fun updateUser(user: User): Boolean {
        return try {
            val uid = user.uid.ifEmpty { auth.currentUser?.uid ?: return false }
            usersCollection.document(uid)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
            false
        }
    }

	//searches for sessions matching the query
    suspend fun fbSearchSessions(
        searchTerm: String? = null,
		descSearch: String? = null,
		date: String? = null,
		time: String? = null,
		interests: String? = null,
        distance: Int = 0,
        containsFriends: Boolean = false,
		userInAttendance: Boolean = false,
        inGroups: Boolean = false,
		user: User,
		userLocation: LatLng,
		futureSessionsOnly: Boolean = true,
		pastSessionsOnly: Boolean = false
    ): List<StudySession>{
		var query: Query = sessionsCollection

		if (user.uid.isEmpty()) {
			if (containsFriends) {
				val friends = fbGetFriends(user.uid)
				query = query.whereArrayContainsAny("attendees", friends)
			}
			if (inGroups){
				val groups = fbGetGroups(user.uid)
				query = query.whereIn("group", groups)
			}
		}

		if (user.uid.isNotEmpty() && userInAttendance) {

			query = query.whereArrayContains("attendees", fbGetUserDocRef(user.uid)!!)

		}

		if (!interests.isNullOrBlank())
			query = query.whereIn("fieldOfStudy", stringToList(interests))

		if (!date.isNullOrBlank() && !time.isNullOrBlank())
		{
			val ts = parseDateTimeToTimestamp(date, time)
			query = query.whereEqualTo("dateTime", ts)
		}

		if (futureSessionsOnly) {
			query = query.whereGreaterThan("dateTime", getCurrentDateTimestamp())
		}

		else if (pastSessionsOnly) {
			query = query.whereLessThan("dateTime", getCurrentDateTimestamp())
		}

		val sessions = mutableListOf<StudySession>()

		val sessionSnapshot = query
			.get()
			.addOnSuccessListener { result ->
				Log.i(TAG, "Retrieved snapshot of sessions")
			}
			.addOnFailureListener {
					exception ->
				Log.i(TAG, "get failed with ", exception)
			}
			.await()

		if(!sessionSnapshot.isEmpty){
			for (document in sessionSnapshot){
				val ts = document.getTimestamp("dateTime") ?: Timestamp(0,0)

				val res = parseTimestamp(ts)

				val dateStr = res.first
				val timeStr = res.second
				var match = (document.get("attendees") as? List<DocumentReference>)?.contains(usersCollection.document(user.uid)) != false
				if (!date.isNullOrBlank())
					match = date.equals(dateStr)
				if (!time.isNullOrBlank())
					match = time.equals(timeStr)
				match = document.getString("name")?.contains(searchTerm ?: "", ignoreCase = true) == true
				if (!match)
					match = !descSearch.isNullOrBlank() && document.getString("description")?.contains(descSearch, ignoreCase = true) == true
				if (distance > 0) {
					val location = document.getGeoPoint("location")
					match = CalculateDistance(userLocation,
							LatLng(location?.latitude ?: 0.0,
								location?.longitude ?: 0.0)) <= distance
				}
				if (match)
					sessions.add(fbGetSession(document.id)!!)
			}
		}

		return sessions
	}

	//gets the list of user's friends
	suspend fun fbGetFriends(uid: String): MutableList<DocumentReference> {
		val friends = mutableListOf<DocumentReference>()

		println("userID in firebase getter: $uid")

		val document = usersCollection
			.document(uid)
			.get()
			.addOnSuccessListener { result ->
				Log.i(TAG, "found a match for " + result.getString("email"))
			}
			.addOnFailureListener {
					exception ->
				Log.i(TAG, "get failed with ", exception)
			}
			.await()

		if(document.exists() && document.get("friends") != null){

			for (friend in document.get("friends") as List<DocumentReference>){
				friends.add(friend)
			}
		}
		return friends
	}

	//gets the user's document reference
	suspend fun fbGetUserDocRef(uid: String): DocumentReference? {

		val document = usersCollection
			.document(uid)
			.get()
			.addOnSuccessListener { result ->
				Log.i(TAG, "found a match for " + result.getString("email"))
			}
			.addOnFailureListener {
					exception ->
				Log.i(TAG, "get failed with ", exception)
			}
			.await()

		if(document.exists()){

			println("found docref for user ${document.get("fname")} ${document.get("lname")}")
			return document.reference

		}

		println("user of uid $uid not found")
		return null

	}

	//gets the list of groups which contain user
	suspend fun fbGetGroups(uid: String): MutableList<DocumentReference>{
		val groups = mutableListOf<DocumentReference>()

		println("userID in firebase getter: $uid")

		val groupSnapshot = groupsCollection
			.whereArrayContains("members", usersCollection.document(uid))
			.get()
			.addOnSuccessListener { result ->
				Log.i(TAG, "Retrieved snapshot of groups containing $uid")
			}
			.addOnFailureListener {
					exception ->
				Log.i(TAG, "get failed with ", exception)
			}
			.await()

		if(!groupSnapshot.isEmpty){

			for (document in groupSnapshot){
				groups.add(document.reference)
			}
		}
		return groups
	}

	// Creates a new user and returns the reference ID string
	// ---- WORKING ----
	suspend fun fbCreateUser(user: User){

        // todo convert profile picture to base 64
		// todo remove passwordHash and test

		println("fbCreateUser() user: $user")

        val userData = hashMapOf(
            "uid" to user.uid,
            "fname" to user.fName,
            "lname" to user.lName,
            "email" to user.email,
            "gender" to user.gender,
            "pronouns" to user.pronouns,
            "DOB" to user.dob,
            "interests" to user.interests,
            "city" to user.city,
            "picture" to user.profilePicturePath
        )
//        var id = ""
        usersCollection
            .document(user.uid)
            .set(userData)
            .await()
    }

	//updates an existing user and returns whether operation was successful
	// ---- WORKING ----
	suspend fun fbUpdateUser(user: User) : Boolean {

		// todo convert profile picture to base 64
		// todo remove passwordHash and test

		println("fbUpdateUser() user: $user")

		val userData = mapOf(
			"uid" to user.uid,
			"fname" to user.fName,
			"lname" to user.lName,
			"email" to user.email,
			"gender" to user.gender,
			"pronouns" to user.pronouns,
			"DOB" to user.dob,
			"interests" to user.interests,
			"city" to user.city,
			"passwordHash" to user.passwordHash,
			"picture" to user.profilePicturePath
		)

		try {
			usersCollection
				.document(user.uid)
				.update(userData)
				.await()

			return true
		}
		catch (e: Exception) {
			println("error encountered while updating user: ${e.message}")
			return false
		}
	}

	//deletes the user from the firestore database
	fun fbDeleteUser(uid: String){
		usersCollection.document(uid).delete()
	}

	//gets the users with a certain email address
    suspend fun fbGetUsersByEmail(email: String?): List<User>{
        val users = mutableListOf<User>()
        var query: Query = usersCollection

        if(!email.isNullOrBlank())
            query = query.whereEqualTo("email", email)

        query
            .get()
            .addOnSuccessListener { result ->
                for (document in result){
                    users.add(User(
                        uid = document.id,
                        fName = document.getString("fname") ?: "",
                        lName = document.getString("lname") ?: "",
                        email = document.getString("email") ?: "",
                        gender = document.getString("gender") ?: "",
                        pronouns = document.getString("pronouns") ?: "",
                        dob = document.getString("DOB") ?: "",
                        interests = document.getString("interests") ?: "",
                        city = document.getString("city") ?: "",
                        passwordHash = document.getString("passwordHash") ?: "",
                        profilePicturePath = document.getString("picture") ?: "",
						friendIDs = ""
                    ))
                }
            }
            .await()

        return users
    }

	//gets the user by their user id
    // ----- WORKING -----
    suspend fun fbGetUserByUID(uid: String): User? {

        var user: User? = null

        println("userID in firebase getter: $uid")

        val document = usersCollection
            .document(uid)
            .get()
            .addOnSuccessListener { result ->
                Log.i(TAG, "found a match for " + result.getString("email"))
            }
            .addOnFailureListener {
                    exception ->
                Log.i(TAG, "get failed with ", exception)
            }
            .await()

        if(document.exists()){

            user = User(
                uid = document.id,
                fName = document.getString("fname") ?: "",
                lName = document.getString("lname") ?: "",
                email = document.getString("email") ?: "",
                gender = document.getString("gender") ?: "",
                pronouns = document.getString("pronouns") ?: "",
                dob = document.getString("DOB") ?: "",
                interests = document.getString("interests") ?: "",
                city = document.getString("city") ?: "",
                passwordHash = document.getString("passwordHash") ?: "",
                profilePicturePath = document.getString("picture") ?: "",
				friendIDs = ""
            )

        }

        Log.i(TAG, "user uid after querying fbase: " + user?.uid)
        return user

    }

	//creates a group on firestore (not used)
    suspend fun fbCreateGroup(
        name: String,
        description: String,
        ownerUID: String,
        interests: List<String>,
    ): String{

        var data = hashMapOf(
            "name" to name,
            "description" to description,
            "owner" to "/users/" + ownerUID,
            "interests" to interests
        )

        val docRef = groupsCollection
            .add(data)
            .await()

        return docRef.id
    }

	//edits a group on firestore (not used)
	fun fbAlterGroup(
		groupID: String,
		name: String?,
		description: String?,
		ownerUID: String?,
		interests: List<String>?,
		sessionIDs: List<String>?,
		attendeeIDs: List<String>?
	){
		var data = hashMapOf<String, Any>()

		if (name != null)
			data.put("name", name)
		if (description != null)
			data.put("description", description)
		if (ownerUID != null)
			data.put("owner", usersCollection.document(ownerUID))
		if (interests != null)
			data.put("interests", interests)
		if (sessionIDs != null)
			data.put("sessions", sessionIDs.map{sessionsCollection.document(it)})
		if (attendeeIDs != null)
			data.put("attendees", attendeeIDs.map{usersCollection.document(it)})

		if (data.isNotEmpty()) {
			groupsCollection
				.document(groupID)
				.set(data, SetOptions.merge())
		}
	}

	//deletes a group from firestore (not used)
	fun fbDeleteGroup(
		groupID: String
	){
		groupsCollection
			.document(groupID)
			.delete()
	}

	//gets groups based on search query (not used)
	suspend fun fbGetGroups(
		name: String?,
		description: String?,
		ownerUID: String?,
		interests: List<String>?,
		sessionIDs: List<String>?,
		attendeeIDs: List<String>?
	): List<StudyGroup>{
		var query : Query =  groupsCollection

		if (name != null)
			query = query.whereEqualTo("name", name)
		if (description != null)
			query = query.whereEqualTo("description", description)
		if (ownerUID != null)
			query = query.whereEqualTo("owner", usersCollection.document(ownerUID))
		if (interests != null)
			query = query.whereArrayContainsAny("interests", interests)
		if (sessionIDs != null)
			query = query.whereArrayContainsAny("sessions", sessionIDs.map{sessionsCollection.document(it)})
		if (attendeeIDs != null)
			query = query.whereArrayContainsAny("attendees", attendeeIDs.map {usersCollection.document(it)})

		val groups = mutableListOf<StudyGroup>()

		query
			.get()
			.addOnSuccessListener { result->
				for (document in result){
					groups.add(StudyGroup(
						groupID = document.id,
						name = document.getString("name") ?: "",
						description = document.getString("description") ?: "",
						ownerUID = document.getString("owner")?.replace("/users/", "") ?: "",
						memberUIDs = listToString((document.get("attendees") as? List<String>)?.map{it.replace("/users/", "")} ?: listOf()),
						interests = listToString((document.get("interests") as? List<String>) ?: listOf()),
						sessionIDs = listToString((document.get("sessions") as? List<String>)?.map{it.replace("/sessions/", "")} ?: listOf())
					))
				}
			}
			.await()

		return groups
	}

	//gets a group based on id (not used)
	suspend fun fbGetGroup(groupID: String): StudyGroup?{
		var group: StudyGroup? = null

		groupsCollection
			.document(groupID)
			.get()
			.addOnSuccessListener { result ->
				group = StudyGroup(
					groupID = result.id,
					name = result.getString("name") ?: "",
					description = result.getString("description") ?: "",
					ownerUID = result.getString("owner")?.replace("/users/", "") ?: "",
					memberUIDs = listToString((result.get("attendees") as? List<String>)?.map{it.replace("/users/", "")} ?: listOf()),
					interests = listToString((result.get("interests") as? List<String>) ?: listOf()),
					sessionIDs = listToString((result.get("sessions") as? List<String>)?.map{it.replace("/sessions/", "")} ?: listOf())
				)
			}
			.await()

		return group
	}

	//creates a session on firestore database
	// ---- WORKING ----
	suspend fun fbCreateSession(
		session : StudySession
	): String{

		val gp = strToGeopoint(session.location)

		// TODO 24hr time

		val dateTime = parseDateTimeToTimestamp(date = session.sDate, time12 = session.sTime)

		val data = hashMapOf<String, Any>(
			"name" to session.name,
			"description" to session.description,
			"fieldOfStudy" to session.fieldOfStudy,
			"dateTime" to dateTime,
			"location" to gp,
			"ownerUID" to usersCollection.document(session.ownerUID),
			"capacity" to session.capacity
		)

		if (session.groupID.isNotBlank())
			data["group"] = groupsCollection.document(session.groupID)
		if (session.attendeeUIDs.isNotBlank())
		{
			data["attendees"] = stringToList(session.attendeeUIDs).map { usersCollection.document(it) }.distinct()
			if(session.attendeeUIDs == "")
				data["attendees"] = emptyList<DocumentReference>()
		}
		val docRef = sessionsCollection
			.add(data)
			.await()

		return docRef.id
	}

	//edits an existing session on firestore database
	// --NEED TO TEST BUT UI PENDING--
	fun fbAlterSession(
		session : StudySession
	) {
		val gp = strToGeopoint(session.location)

		val dateTime = parseDateTimeToTimestamp(session.sDate, session.sTime)

		val data = hashMapOf<String, Any>(
			"name" to session.name,
			"description" to session.description,
			"dateTime" to dateTime,
			"location" to gp,
			"ownerUID" to usersCollection.document(session.ownerUID),
			"capacity" to session.capacity
		)
		if (session.groupID.isNotBlank())
			data["group"] = groupsCollection.document(session.groupID)
		if (session.attendeeUIDs.isNotBlank()) {
			data["attendees"] = stringToList(session.attendeeUIDs).map { usersCollection.document(it) }.distinct()
		}
		else
		{
			val attendees: List<DocumentReference> = emptyList()
			data["attendees"] = attendees
		}

		if (data.isNotEmpty()) {
			sessionsCollection
				.document(session.sID)
				.set(data, SetOptions.merge())
		}
	}

	//deletes a session from the firestore database
	fun fbDeleteSession(session : StudySession){
		sessionsCollection
			.document(session.sID)
			.delete()
	}


	// NOT PERFECT, USE fbSearchSessions wherever possible
	// futureSessionsOnly overrides pastSessionsOnly
	suspend fun fbGetSessions(
		name: String? = null,
		description: String? = null,
		date: String? = null,
		time: String? = null,
		lat: Double? = null,
		long: Double? = null,
		capacity: Int? = null,
		ownerUID: String? = null,
		attendeeIDs: List<String>? = null
	): List<StudySession> = suspendCoroutine { continuation ->

		var query : Query = sessionsCollection

		if(!name.isNullOrBlank())
			query = query.whereEqualTo("name", name)
		if(!description.isNullOrBlank())
			query = query.whereEqualTo("description", description)

		if(!date.isNullOrBlank() && !time.isNullOrBlank())
		{
			val dateTime = parseDateTimeToTimestamp(date, time)
			query = query.whereEqualTo("dateTime", dateTime)
		}

		if(lat != null && long != null)
			query = query.whereEqualTo("location", GeoPoint(lat, long))
//		if(!time.isNullOrBlank())
//			query = query.whereEqualTo("time", time)
		if(capacity != null)
			query = query.whereEqualTo("capacity", capacity)
		if(!ownerUID.isNullOrBlank())
			query = query.whereEqualTo("ownerUID", usersCollection.document(ownerUID))
		if(!attendeeIDs.isNullOrEmpty())
			query = query.whereArrayContainsAny("attendees", attendeeIDs.map { usersCollection.document(it) })

		val sessions = mutableListOf<StudySession>()

		query
			.get()
			.addOnSuccessListener { result ->
//				println("Result fetched: " + result.size())
				for (document in result.documents){

					println("session found: ${document.getString("name") ?: ""}")

					val gp = document.getGeoPoint("location") ?: GeoPoint(0.0,0.0)
					val locStr = geopointToString(gp)

					var ownerDocRef: DocumentSnapshot?
					var attendeesDocRef: List<DocumentReference>?
					runBlocking {
						ownerDocRef = document.getDocumentReference("ownerUID")?.get()?.await()
						attendeesDocRef = document.get("attendees") as? List<DocumentReference>
					}

					val ownerUID2 = ownerDocRef?.id ?: ""

//					println(ownerUID2)
					val attendeeUIDs = mutableListOf<String>()

					if(attendeesDocRef != null)
						attendeesDocRef!!.forEach { att ->
//							println(att.id)
							attendeeUIDs.add(att.id)
						}
//							println(attendeesDocRef?.get(0)?.id)

					val ts = document.getTimestamp("dateTime") ?: Timestamp(0,0)

					val res = parseTimestamp(ts)

					val dateStr = res.first
					val timeStr = res.second

					val session = StudySession(
					 sID = document.id,
					 name = document.getString("name") ?: "",
					 description = document.getString("description") ?: "",
					 fieldOfStudy = document.getString("fieldOfStudy") ?: "",
					 sDate = dateStr,
					 sTime = timeStr,
					 location = locStr,
					 capacity = document.getLong("capacity")?.toInt() ?: 0,
					 attendeeUIDs = listToString(attendeeUIDs),
					 ownerUID = ownerUID2,
					 groupID = document.getString("group")?.replace("/groups/", "")  ?: ""
					)

					sessions.add(session)

				}

				continuation.resume(sessions)
			}

	}

	//gets session matching id
	// TODO pending test - check location str
	suspend fun fbGetSession(sessionID: String): StudySession?{
		var session: StudySession? = null

		println("sessionID in firebase getter: $sessionID")

		val document = sessionsCollection
			.document(sessionID)
			.get()
			.addOnSuccessListener { result ->
				Log.i(TAG, "found a match for " + result.getString("name"))
			}
			.addOnFailureListener {
					exception ->
				Log.i(TAG, "get failed with ", exception)
			}
			.await()

		if(document.exists()){
			val geoPoint =  document.getGeoPoint("location") ?: GeoPoint(0.0,0.0)
			val location = geopointToString(geoPoint)

			var ownerDocRef: DocumentSnapshot?
			runBlocking {
				ownerDocRef = document.getDocumentReference("ownerUID")?.get()?.await()
			}
			val ownerUID = ownerDocRef?.id ?: ""

			val ts = document.getTimestamp("dateTime") ?: Timestamp(0,0)

			val res = parseTimestamp(ts)

			val dateStr = res.first
			val timeStr = res.second

			session = StudySession(
				sID = document.id,
				name = document.getString("name") ?: "",
				description = document.getString("description") ?: "",
				fieldOfStudy = document.getString("fieldOfStudy") ?: "",
				sDate = dateStr,
				sTime = timeStr,
				location = location,
				capacity = document.getLong("capacity")?.toInt() ?: 0,
				attendeeUIDs = listToString((document.get("attendees") as? List<DocumentReference>)?.map { it.id }
					?: listOf()),
				groupID = document.getDocumentReference("group")?.id ?: "",
				ownerUID = ownerUID
			)

		}

		Log.i(TAG, "session sid after querying fbase: " + session?.sID)
		return session
	}
}





