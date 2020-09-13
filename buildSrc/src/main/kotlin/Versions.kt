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
    const val org_jetbrains_kotlinx_kotlinx_serialization: String = "1.0.0-RC"

    const val org_jetbrains_kotlinx_kotlinx_coroutines: String = "1.3.9"

    const val io_github_reactivecircus_flowbinding: String = "0.12.0"

    const val org_jetbrains_kotlin: String = "1.4.0"

    const val androidx_fragment: String = "1.2.5"

    const val androidx_test: String = "1.3.0"

    const val io_ktor: String = "1.4.0"

    const val com_android_tools_build_gradle: String = "4.0.1"

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "9.3.0" // available: "9.4.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.5"

    const val binary_compatibility_validator: String = "0.2.3"

    const val coroutines_test_extensions: String = "0.1.2"

    const val lifecycle_runtime_ktx: String = "2.2.0"

    const val gradle_pitest_plugin: String = "1.5.2"

    const val constraintlayout: String = "2.0.1"

    const val espresso_core: String = "3.3.0"

    const val lint_gradle: String = "27.0.1"

    const val viewbinding: String = "4.0.1"

    const val appcompat: String = "1.2.0"

    const val junit_ktx: String = "1.1.2"

    const val material: String = "1.2.1"

    const val ktlint: String = "0.37.2" // available: "0.38.1"

    const val aapt2: String = "4.0.1-6197926"

    const val mockk: String = "1.10.0"

    const val coil: String = "0.12.0"

    /**
     * Current version: "6.6.1"
     * See issue 19: How to update Gradle itself?
     * https://github.com/jmfayard/buildSrcVersions/issues/19
     */
    const val gradleLatestVersion: String = "6.6.1"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
    inline get() =
            id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
