import org.gradle.api.JavaVersion
import kotlin.String
import kotlin.Int

object Config {
    const val libVersion: String = "0.0.5"

    val jvmTarget: JavaVersion = JavaVersion.VERSION_1_8
}
