package at.florianschuster.control.androidgithub.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Repository(
    val id: Int,
    @SerialName("full_name") val fullName: String,
    val description: String? = null,
    val owner: Owner,
    @SerialName("updated_at") val lastUpdated: String,
    @SerialName("html_url") val webUrl: String
) {

    @Serializable
    data class Owner(
        @SerialName("avatar_url") val avatarUrl: String
    )
}
