package at.florianschuster.control.androidgithub

import at.florianschuster.control.androidgithub.model.Repository
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
    private data class SearchResponse(val items: List<Repository>)

    suspend fun search(
        query: String,
        page: Int
    ): List<Repository> {
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
