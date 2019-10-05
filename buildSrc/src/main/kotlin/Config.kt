import org.gradle.api.JavaVersion
import kotlin.String
import kotlin.Int

object Config {
    const val libVersion: String = "0.0.1"

    const val targetSdkVersion: Int = 29
    const val minSdkVersion: Int = 16

    val jvmTarget: JavaVersion = JavaVersion.VERSION_1_8
}
