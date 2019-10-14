import kotlin.String
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version.
 */
object Versions {
    const val org_jetbrains_kotlinx_kotlinx_serialization: String = "0.13.0"

    const val org_jetbrains_kotlinx_kotlinx_coroutines: String = "1.3.2"

    const val ru_ldralighieri_corbind: String = "1.2.0-RC"

    const val org_jetbrains_kotlin: String = "1.3.50"

    const val com_android_tools_build_gradle: String = "3.5.1"

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "9.0.0"

    const val retrofit2_kotlinx_serialization_converter: String = "0.4.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.4"

    const val kotlin_flow_extensions: String = "0.0.2"

    const val lifecycle_runtime_ktx: String = "2.2.0-alpha04"

    const val lifecycle_extensions: String = "2.1.0"

    const val constraintlayout: String = "2.0.0-beta1"

    const val activity_ktx: String = "1.0.0"

    const val lint_gradle: String = "26.5.1"

    const val appcompat: String = "1.1.0"

    const val retrofit: String = "2.6.2"

    const val ktlint: String = "0.34.2" // available: "0.35.0"

    const val aapt2: String = "3.5.1-5435860"

    const val junit: String = "4.12"

    const val mockk: String = "1.9.3"

    /**
     * Current version: "5.6.2"
     * See issue 19: How to update Gradle itself?
     * https://github.com/jmfayard/buildSrcVersions/issues/19
     */
    const val gradleLatestVersion: String = "5.6.2"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
    inline get() =
            id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
