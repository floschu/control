package at.florianschuster.control.androidgithub

import android.net.Uri
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

internal interface GithubApi {

    @GET("search/repositories")
    suspend fun repos(@Query("q") query: String, @Query("page") page: Int): Result

    companion object {

        operator fun invoke(
            baseUrl: String = "https://api.github.com/",
            json: Json = Json {
                isLenient = true
                ignoreUnknownKeys = true
                serializeSpecialFloatingPointValues = true
            },
            mediaType: MediaType = MediaType.get("application/json")
        ): GithubApi {
            val retrofit = Retrofit.Builder().apply {
                baseUrl(baseUrl)
                addConverterFactory(json.asConverterFactory(mediaType))
            }.build()
            return retrofit.create(GithubApi::class.java)
        }
    }
}

@Serializable
internal data class Result(val items: List<Repo>)

@Serializable
internal data class Repo(
    val id: Int,
    @SerialName("full_name") val name: String,
    val description: String? = null
) {
    val webUri: Uri get() = Uri.parse("https://github.com/$name")
}
