import org.gradle.api.JavaVersion
import kotlin.String
import kotlin.Int

object Config {
    const val version: String = "0.0.1"

    const val name: String = "control"
    const val description: String =
        "control - kotlin flow based unidirectional-data-flow architecture"
    const val group: String = "at.florianschuster.control"
    const val webUrl: String = "https://github.com/floschu/Reaktor"
    const val gitUrl: String = "https://github.com/floschu/Reaktor.git"

    const val targetSdkVersion: Int = 29
    const val minSdkVersion: Int = 16

    val jvmTarget: JavaVersion = JavaVersion.VERSION_1_8
}
