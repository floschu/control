package at.florianschuster.control.androidgithub

import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
    private data class SearchResponse(val items: List<Repo>)

    suspend fun search(query: String, page: Int): List<Repo> {
        val response = httpClient.get<SearchResponse>(
            "https://api.github.com/search/repositories"
        ) {
            url {
                parameter("q", query)
                parameter("page", page)
            }
        }
        return response.items
    }
}

@Serializable
internal data class Repo(
    val id: Int,
    @SerialName("full_name") val name: String,
    val description: String? = null
) {
    val webUri: Uri get() = Uri.parse("https://github.com/$name")
}
