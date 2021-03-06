import kotlin.String

/**
 * Generated by https://github.com/jmfayard/buildSrcVersions
 *
 * Update this file with
 *   `$ ./gradlew buildSrcVersions`
 */
object Libs {
    const val kotlinx_serialization_core: String =
            "org.jetbrains.kotlinx:kotlinx-serialization-core:" +
            Versions.org_jetbrains_kotlinx_kotlinx_serialization

    /**
     * https://github.com/Kotlin/kotlinx.coroutines
     */
    const val kotlinx_coroutines_core: String = "org.jetbrains.kotlinx:kotlinx-coroutines-core:" +
            Versions.org_jetbrains_kotlinx_kotlinx_coroutines

    /**
     * https://github.com/reactivecircus/FlowBinding
     */
    const val flowbinding_android: String =
            "io.github.reactivecircus.flowbinding:flowbinding-android:" +
            Versions.io_github_reactivecircus_flowbinding

    /**
     * https://github.com/reactivecircus/FlowBinding
     */
    const val flowbinding_core: String = "io.github.reactivecircus.flowbinding:flowbinding-core:" +
            Versions.io_github_reactivecircus_flowbinding

    /**
     * https://github.com/reactivecircus/FlowBinding
     */
    const val flowbinding_recyclerview: String =
            "io.github.reactivecircus.flowbinding:flowbinding-recyclerview:" +
            Versions.io_github_reactivecircus_flowbinding

    /**
     * https://kotlinlang.org/
     */
    const val kotlin_scripting_compiler_embeddable: String =
            "org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/
     */
    const val kotlin_serialization_unshaded: String =
            "org.jetbrains.kotlin:kotlin-serialization-unshaded:" + Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/
     */
    const val kotlin_stdlib: String = "org.jetbrains.kotlin:kotlin-stdlib:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/
     */
    const val kotlin_gradle_plugin: String = "org.jetbrains.kotlin:kotlin-gradle-plugin:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://kotlinlang.org/
     */
    const val kotlin_serialization: String = "org.jetbrains.kotlin:kotlin-serialization:" +
            Versions.org_jetbrains_kotlin

    /**
     * https://developer.android.com/jetpack/androidx
     */
    const val fragment_ktx: String = "androidx.fragment:fragment-ktx:" + Versions.androidx_fragment

    /**
     * https://developer.android.com/jetpack/androidx
     */
    const val fragment_testing: String = "androidx.fragment:fragment-testing:" +
            Versions.androidx_fragment

    /**
     * https://developer.android.com/testing
     */
    const val core_ktx: String = "androidx.test:core-ktx:" + Versions.androidx_test

    /**
     * https://developer.android.com/testing
     */
    const val androidx_test_rules: String = "androidx.test:rules:" + Versions.androidx_test

    /**
     * https://developer.android.com/testing
     */
    const val androidx_test_runner: String = "androidx.test:runner:" + Versions.androidx_test

    /**
     * https://github.com/ktorio/ktor
     */
    const val ktor_client_cio: String = "io.ktor:ktor-client-cio:" + Versions.io_ktor

    /**
     * https://github.com/ktorio/ktor
     */
    const val ktor_client_json_jvm: String = "io.ktor:ktor-client-json-jvm:" + Versions.io_ktor

    /**
     * https://github.com/ktorio/ktor
     */
    const val ktor_client_logging_jvm: String = "io.ktor:ktor-client-logging-jvm:" +
            Versions.io_ktor

    /**
     * https://github.com/ktorio/ktor
     */
    const val ktor_client_serialization_jvm: String = "io.ktor:ktor-client-serialization-jvm:" +
            Versions.io_ktor

    /**
     * https://developer.android.com/studio
     */
    const val com_android_tools_build_gradle: String = "com.android.tools.build:gradle:" +
            Versions.com_android_tools_build_gradle

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String =
            "org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:" +
            Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin

    const val de_fayard_buildsrcversions_gradle_plugin: String =
            "de.fayard.buildSrcVersions:de.fayard.buildSrcVersions.gradle.plugin:" +
            Versions.de_fayard_buildsrcversions_gradle_plugin

    const val com_jfrog_bintray_gradle_plugin: String =
            "com.jfrog.bintray:com.jfrog.bintray.gradle.plugin:" +
            Versions.com_jfrog_bintray_gradle_plugin

    /**
     * https://github.com/Kotlin/binary-compatibility-validator
     */
    const val binary_compatibility_validator: String =
            "org.jetbrains.kotlinx:binary-compatibility-validator:" +
            Versions.binary_compatibility_validator

    /**
     * https://github.com/floschu/coroutines-test-extensions
     */
    const val coroutines_test_extensions: String =
            "at.florianschuster.test:coroutines-test-extensions:" +
            Versions.coroutines_test_extensions

    /**
     * https://developer.android.com/jetpack/androidx
     */
    const val lifecycle_runtime_ktx: String = "androidx.lifecycle:lifecycle-runtime-ktx:" +
            Versions.lifecycle_runtime_ktx

    /**
     * http://gradle-pitest-plugin.solidsoft.info/
     */
    const val gradle_pitest_plugin: String = "info.solidsoft.gradle.pitest:gradle-pitest-plugin:" +
            Versions.gradle_pitest_plugin

    /**
     * http://tools.android.com
     */
    const val constraintlayout: String = "androidx.constraintlayout:constraintlayout:" +
            Versions.constraintlayout

    /**
     * https://developer.android.com/testing
     */
    const val espresso_core: String = "androidx.test.espresso:espresso-core:" +
            Versions.espresso_core

    /**
     * https://developer.android.com/studio
     */
    const val lint_gradle: String = "com.android.tools.lint:lint-gradle:" + Versions.lint_gradle

    const val viewbinding: String = "androidx.databinding:viewbinding:" + Versions.viewbinding

    /**
     * https://developer.android.com/jetpack/androidx
     */
    const val appcompat: String = "androidx.appcompat:appcompat:" + Versions.appcompat

    /**
     * https://developer.android.com/testing
     */
    const val junit_ktx: String = "androidx.test.ext:junit-ktx:" + Versions.junit_ktx

    /**
     * https://github.com/material-components/material-components-android
     */
    const val material: String = "com.google.android.material:material:" + Versions.material

    /**
     * https://github.com/pinterest/ktlint
     */
    const val ktlint: String = "com.pinterest:ktlint:" + Versions.ktlint

    /**
     * https://developer.android.com/studio
     */
    const val aapt2: String = "com.android.tools.build:aapt2:" + Versions.aapt2

    /**
     * http://mockk.io
     */
    const val mockk: String = "io.mockk:mockk:" + Versions.mockk

    /**
     * https://github.com/coil-kt/coil
     */
    const val coil: String = "io.coil-kt:coil:" + Versions.coil
}
