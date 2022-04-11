package at.florianschuster.control.androidgithub

import at.florianschuster.control.androidgithub.model.Repository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class GithubApi(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    @Serializable
    private data class SearchResponse(val items: List<Repository>)

    private val httpClient = HttpClient(engineFactory = CIO) {
        install(ContentNegotiation) {
            json(json = Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
    }

    suspend fun search(
        query: String,
        page: Int
    ): List<Repository> = withContext(ioDispatcher) {
        val response = httpClient.get(
            urlString = "https://api.github.com/search/repositories"
        ) {
            parameter("q", query)
            parameter("page", page)
        }
        response.body<SearchResponse>().items
    }
}
