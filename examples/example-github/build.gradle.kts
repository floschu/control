import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlinx-serialization")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "at.florianschuster.control.githubexample"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    sourceSets["debug"].java.srcDir("src/debug/kotlin")
}

dependencies {
    implementation(project(":control-core"))

    implementation(Libs.appcompat)
    implementation(Libs.constraintlayout)
    implementation(Libs.flowbinding_android)
    implementation(Libs.flowbinding_core)
    implementation(Libs.flowbinding_recyclerview)
    implementation(Libs.fragment_ktx)
    implementation(Libs.kotlinx_serialization_runtime)
    implementation(Libs.lifecycle_runtime_ktx)
    implementation(Libs.material)
    implementation(Libs.retrofit)
    implementation(Libs.retrofit2_kotlinx_serialization_converter)
    debugImplementation(Libs.fragment_testing)

    testImplementation(Libs.coroutines_test_extensions)
    testImplementation(Libs.mockk)

    androidTestImplementation(Libs.core_ktx)
    androidTestImplementation(Libs.junit_ktx)
    androidTestImplementation(Libs.espresso_core)
    androidTestImplementation(Libs.androidx_test_rules)
    androidTestImplementation(Libs.androidx_test_runner)
    androidTestImplementation(Libs.coroutines_test_extensions)
    androidTestImplementation(Libs.mockk)
}
