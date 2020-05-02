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
    const val org_jetbrains_kotlinx_kotlinx_serialization: String = "0.20.0"
    // available: "0.20.0-1.4-M1-release-99"

    const val org_jetbrains_kotlinx_kotlinx_coroutines: String = "1.3.5"
    // available: "1.3.5-1.4-M1-release-99"

    const val io_github_reactivecircus_flowbinding: String = "0.10.2" // available: "0.11.1"

    const val org_jetbrains_kotlinx: String = "0.14.3"

    const val org_jetbrains_kotlin: String = "1.3.71" // available: "1.3.72"

    const val androidx_fragment: String = "1.2.4"

    const val androidx_test: String = "1.2.0"

    const val com_android_tools_build_gradle: String = "3.6.3"

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "9.2.1"

    const val retrofit2_kotlinx_serialization_converter: String = "0.5.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

    const val info_solidsoft_pitest_gradle_plugin: String = "1.5.0"

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.4" // available: "1.8.5"

    const val lifecycle_runtime_ktx: String = "2.2.0"

    const val constraintlayout: String = "2.0.0-beta4"

    const val flow_extensions: String = "0.0.4"

    const val espresso_core: String = "3.2.0"

    const val lint_gradle: String = "26.6.3"

    const val appcompat: String = "1.1.0"

    const val junit_ktx: String = "1.1.1"

    const val material: String = "1.1.0"

    const val retrofit: String = "2.8.1"

    const val ktlint: String = "0.36.0"

    const val aapt2: String = "3.6.3-6040484"

    const val mockk: String = "1.9.3" // available: "1.10.0"

    /**
     * Current version: "5.6.4"
     * See issue 19: How to update Gradle itself?
     * https://github.com/jmfayard/buildSrcVersions/issues/19
     */
    const val gradleLatestVersion: String = "6.3"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
    inline get() =
        id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
