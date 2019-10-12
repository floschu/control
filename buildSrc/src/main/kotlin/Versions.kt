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
    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "9.0.0"

    const val io_gitlab_arturbosch_detekt_gradle_plugin: String = "1.1.1"

    const val retrofit2_kotlinx_serialization_converter: String = "0.4.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.6.5"

    const val com_android_tools_build_gradle: String = "3.5.1"

    const val kotlinx_serialization_runtime: String = "0.13.0"

    const val kotlinx_coroutines_core: String = "1.3.2"

    const val kotlinx_coroutines_test: String = "1.3.2"

    const val ru_ldralighieri_corbind: String = "1.2.0-RC"

    const val lifecycle_runtime_ktx: String = "2.2.0-alpha04"

    const val lifecycle_extensions: String = "2.1.0"

    const val org_jetbrains_kotlin: String = "1.3.50"

    const val constraintlayout: String = "2.0.0-beta1"

    const val activity_ktx: String = "1.0.0"

    const val lint_gradle: String = "26.5.1"

    const val appcompat: String = "1.1.0"

    const val core_ktx: String = "1.1.0"

    const val retrofit: String = "2.6.2"

    const val ktlint: String = "0.34.2"

    const val aapt2: String = "3.5.1-5435860"

    const val junit: String = "4.12"

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
