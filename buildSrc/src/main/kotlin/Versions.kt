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
    const val org_jetbrains_kotlinx_kotlinx_serialization: String = "0.14.0"

    const val org_jetbrains_kotlinx_kotlinx_coroutines: String = "1.3.3"

    const val io_github_reactivecircus_flowbinding: String = "0.8.0"

    const val org_jetbrains_kotlin: String = "1.3.61"

    const val androidx_test: String = "1.2.0"

    const val com_android_tools_build_gradle: String = "3.5.3"

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "9.1.1"

    const val retrofit2_kotlinx_serialization_converter: String = "0.4.0"

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.7.0"

    const val com_jfrog_bintray_gradle_plugin: String = "1.8.4"

    const val lifecycle_runtime_ktx: String = "2.2.0"

    const val constraintlayout: String = "2.0.0-beta1"

    const val flow_extensions: String = "0.0.3"

    const val espresso_core: String = "3.2.0"

    const val fragment_ktx: String = "1.2.0"

    const val lint_gradle: String = "26.5.3"

    const val appcompat: String = "1.1.0"

    const val junit_ktx: String = "1.1.1"

    const val retrofit: String = "2.7.1"

    const val ktlint: String = "0.36.0"

    const val aapt2: String = "3.5.3-5435860"

    const val mockk: String = "1.9.3"

    /**
     * Current version: "5.6.3"
     * See issue 19: How to update Gradle itself?
     * https://github.com/jmfayard/buildSrcVersions/issues/19
     */
    const val gradleLatestVersion: String = "6.1.1"
}

/**
 * See issue #47: how to update buildSrcVersions itself
 * https://github.com/jmfayard/buildSrcVersions/issues/47
 */
val PluginDependenciesSpec.buildSrcVersions: PluginDependencySpec
    inline get() =
        id("de.fayard.buildSrcVersions").version(Versions.de_fayard_buildsrcversions_gradle_plugin)
