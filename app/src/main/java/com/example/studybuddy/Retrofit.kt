package com.example.studybuddy

import com.example.studybuddy.data.entities.strToLatLng
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


val mapBoxApiUrl = "https://api.mapbox.com/search/geocode/v6/"

private val mapBoxAccessToken = "pk.eyJ1IjoiN3dpay1wayIsImEiOiJjbWIwNHl6b3Ywcm16Mmpwc3QxbjZoZ2NjIn0.5VmZMxJULHATflcr7eoktg"

val mapBoxCountry = "au"
val geocodeLimit = 4

data class MapBoxApiResponse (

    val features: List<Feature> = ArrayList()
)

data class Feature (

    val properties: Property
)

data class Property(
    @SerializedName("full_address")
    val fullAddress: String,
    val name: String,
    val coordinates: Coordinates
)

data class Coordinates(
    val longitude: Double,
    val latitude: Double
)

interface RetrofitInterface {

    @GET("forward")
    suspend fun geocode(
        @Query("access_token") accessToken: String,
        @Query("q") q: String,
        @Query("limit") limit: Int,
        @Query("country") country: String,
        @Query("proximity") proximityToCoords: String,
        @Query("types") type: String,
    ): MapBoxApiResponse

    @GET("forward")
    suspend fun geocodeCity(
        @Query("access_token") accessToken: String,
        @Query("q") q: String,
        @Query("limit") limit: Int,
        @Query("country") country: String,
        @Query("types") type: String,
    ): MapBoxApiResponse

    @GET("reverse")
    suspend fun revGeocode(
        @Query("access_token") accessToken: String,
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double,
        @Query("limit") limit: Int,
        @Query("country") country: String
    ): MapBoxApiResponse

}

object MapBoxObject {

    private val client: OkHttpClient

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val mapService: RetrofitInterface by lazy{
        val retrofit = Retrofit.Builder()
            .baseUrl(mapBoxApiUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RetrofitInterface::class.java)
    }

//    val revgeocoderService: RetrofitInterface by lazy{
//        val retrofit = Retrofit.Builder()
//            .baseUrl(mapBoxRevGeocoderUrl)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        retrofit.create(RetrofitInterface::class.java)
//    }

}

class mapBoxRepository {
    private val mapService = MapBoxObject.mapService

    // q is the query string, results will be closest to proximityToCoords which is a str of the form "lat,long"
    suspend fun geocodeCity(q: String): MapBoxApiResponse {

//        val coords = strToLatLng(proximityToCoords)
//        val reversedLatLong = "${coords.longitude},${coords.latitude}"

        return mapService.geocodeCity(
            mapBoxAccessToken,
            q,
            1,
            mapBoxCountry,
            type = "place"
        )
    }

    suspend fun geocode(q: String, proximityToCoords: String, type: String): MapBoxApiResponse {

        val coords = strToLatLng(proximityToCoords)
        val reversedLatLong = "${coords.longitude},${coords.latitude}"

        return mapService.geocode(
            mapBoxAccessToken,
            q,
            1,
            mapBoxCountry,
            reversedLatLong,
            type = type
        )
    }

    // latLongStr is a string of the form "lat,long" - which are the coordinates to be reverse-geocoded
    suspend fun revGeocode(latLongStr: String): MapBoxApiResponse {

        val coords = strToLatLng(latLongStr)

        println("${coords.latitude} ${coords.longitude}")
        // -37.9105,145.1347

        return mapService.revGeocode(
            mapBoxAccessToken,
            lat = coords.latitude,
            long = coords.longitude,
            limit = 1,
            mapBoxCountry
        )

    }

}