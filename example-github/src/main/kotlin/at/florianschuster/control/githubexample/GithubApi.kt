package at.florianschuster.control.githubexample

import android.net.Uri
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface GithubApi {

    @GET("search/repositories")
    suspend fun repos(@Query("q") query: String, @Query("page") page: Int): Result

    companion object {
        operator fun invoke(
            baseUrl: String = "https://api.github.com/",
            mediaType: MediaType = MediaType.get("application/json")
        ): GithubApi {
            val converterFactory = Json.nonstrict.asConverterFactory(mediaType)
            val retrofit = Retrofit.Builder().apply {
                baseUrl(baseUrl)
                addConverterFactory(converterFactory)
            }.build()
            return retrofit.create(GithubApi::class.java)
        }
    }
}

@Serializable
data class Result(val items: List<Repo>)

@Serializable
data class Repo(val id: Int, @SerialName("full_name") val name: String) {
    val webUri: Uri get() = Uri.parse("https://github.com/$name")
}