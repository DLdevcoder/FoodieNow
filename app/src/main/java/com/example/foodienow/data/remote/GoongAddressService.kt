package com.example.foodienow.data.remote

import com.example.foodienow.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class GoongAutocompleteResponse(
    val predictions: List<GoongPrediction> = emptyList(),
    val status: String
)

@Serializable
data class GoongPrediction(
    @SerialName("place_id") val placeId: String,
    val description: String,
    @SerialName("structured_formatting") val structuredFormatting: GoongStructuredFormatting? = null
)

@Serializable
data class GoongStructuredFormatting(
    @SerialName("main_text") val mainText: String,
    @SerialName("secondary_text") val secondaryText: String? = null
)

@Serializable
data class GoongPlaceDetailResponse(
    val result: GoongPlaceDetailResult,
    val status: String
)

@Serializable
data class GoongPlaceDetailResult(
    val geometry: GoongGeometry,
    @SerialName("formatted_address") val formattedAddress: String? = null,
    val name: String? = null
)

@Serializable
data class GoongGeometry(
    val location: GoongLocation
)

@Serializable
data class GoongLocation(
    val lat: Double,
    val lng: Double
)

@Serializable
data class GoongGeocodeResponse(
    val results: List<GoongGeocodeResult> = emptyList(),
    val status: String
)

@Serializable
data class GoongGeocodeResult(
    @SerialName("formatted_address") val formattedAddress: String? = null,
    val geometry: GoongGeometry,
    val name: String? = null
)

@Singleton
class GoongAddressService @Inject constructor() {
    private val httpClient = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }
    private val goongApiKey = BuildConfig.GOONG_API_KEY

    suspend fun getAutocomplete(input: String): List<GoongPrediction> = withContext(Dispatchers.IO) {
        if (goongApiKey.isBlank() || input.isBlank()) return@withContext emptyList()
        try {
            val encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8.name())
            val url = "https://rsapi.goong.io/Place/AutoComplete?api_key=$goongApiKey&input=$encodedInput"
            val responseText: String = httpClient.get(url).body()
            val response = json.decodeFromString<GoongAutocompleteResponse>(responseText)
            if (response.status == "OK") {
                response.predictions
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPlaceDetail(placeId: String): GoongPlaceDetailResult? = withContext(Dispatchers.IO) {
        if (goongApiKey.isBlank() || placeId.isBlank()) return@withContext null
        try {
            val url = "https://rsapi.goong.io/Place/Detail?api_key=$goongApiKey&place_id=$placeId"
            val responseText: String = httpClient.get(url).body()
            val response = json.decodeFromString<GoongPlaceDetailResponse>(responseText)
            if (response.status == "OK") {
                response.result
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getReverseGeocode(lat: Double, lng: Double): GoongGeocodeResult? = withContext(Dispatchers.IO) {
        if (goongApiKey.isBlank()) return@withContext null
        try {
            val url = "https://rsapi.goong.io/Geocode?api_key=$goongApiKey&latlng=$lat,$lng"
            val responseText: String = httpClient.get(url).body()
            val response = json.decodeFromString<GoongGeocodeResponse>(responseText)
            if (response.status == "OK" && response.results.isNotEmpty()) {
                response.results.first()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
