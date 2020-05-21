package at.florianschuster.control.androidgithub

import android.net.Uri
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class GithubApi(
    json: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    },
    private val httpClient: HttpClient = HttpClient(engineFactory = CIO) {
        install(feature = JsonFeature) {
            serializer = KotlinxSerializer(json = json)
        }
        install(feature = Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
    }
) {

    @Serializable
    private data class Result(val items: List<Repo>)

    /**
     * Returns either a [List] of [Repo] or an empty [List] if an error occurs.
     */
    suspend fun search(query: String, page: Int): List<Repo> = runCatching {
        httpClient.get<Result>("https://api.github.com/search/repositories") {
            url {
                parameter("q", query)
                parameter("page", page)
            }
        }
    }.fold(
        onSuccess = Result::items,
        onFailure = { error ->
            Log.e("GithubApi.repos", "with query = $query, page = $page", error)
            emptyList()
        }
    )
}

@Serializable
internal data class Repo(
    val id: Int,
    @SerialName("full_name") val name: String,
    val description: String? = null
) {
    val webUri: Uri get() = Uri.parse("https://github.com/$name")
}
