package com.example.studybuddy.data.entities

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// utility func for conversion of lists to strings
fun stringToList(csString: String): List<String>{
    return csString.split(",")
}

fun listToString(list: List<String>): String{
    return list.joinToString(",")
}

fun geopointToString(gp: GeoPoint): String {
    return "${gp.latitude},${gp.longitude}"
}

fun strToGeopoint(str: String): GeoPoint {
    val tokens = str.split(",")
    return GeoPoint(tokens[0].toDouble(), tokens[1].toDouble())
}

fun strToLatLng(str: String): LatLng{
    val tokens = str.split(",").map { it.toDouble() }
    return LatLng(tokens[0], tokens[1])
}

fun latLngToString(latLng: LatLng): String{
    return "${latLng.latitude},${latLng.longitude}"
}

// utility func(s) for conversion of date/time to strings

fun timeStr12to24(time12Hour: String): String {

    val parts = time12Hour.split(" ")
    val timePart = parts[0]
    val period = parts[1].uppercase()

    val (hour, minute) = timePart.split(":").map { it.toInt() }

    val hour24 = when {
        period == "AM" && hour == 12 -> 0
        period == "AM" -> hour
        period == "PM" && hour == 12 -> 12
        period == "PM" -> hour + 12
        else -> hour
    }

    return String.format("%02d:%02d", hour24, minute)

}

fun timeStr24to12(time24Hour: String): String {
    val (hour, minute) = time24Hour.split(":").map { it.toInt() }

    val (hour12, period) = when (hour) {
        0 -> Pair(12, "AM")
        in 1..11 -> Pair(hour, "AM")
        12 -> Pair(12, "PM")
        in 13..23 -> Pair(hour - 12, "PM")
        else -> Pair(hour, "AM")
    }

    return String.format("%d:%02d %s", hour12, minute, period)
}

// must take time in 12hr format only
fun parseDateTimeToTimestamp(date: String, time12: String): Timestamp {

    val dateTimeString = date + "T" + timeStr12to24(time12)

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
    return Timestamp(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
}

fun getCurrentDateTimestamp(): Timestamp {
    return Timestamp(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
}

fun parseTimestamp(timestamp: Timestamp): Pair<String, String> {
    val date = timestamp.toDate()

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val dateString = dateFormatter.format(date)
    val time24String = timeFormatter.format(date)

    val time12 = timeStr24to12(time24String)

    return Pair(dateString, time12)
}

