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
    }
    compileOptions {
        sourceCompatibility = Config.jvmTarget
        targetCompatibility = Config.jvmTarget
    }
}

dependencies {
    implementation(project(":control-core"))

    implementation(Libs.appcompat)
    implementation(Libs.constraintlayout)
    implementation(Libs.lifecycle_runtime_ktx)
    implementation(Libs.corbind)
    implementation(Libs.corbind_core)
    implementation(Libs.corbind_recyclerview)
    implementation(Libs.activity_ktx)
    implementation(Libs.kotlinx_serialization_runtime)
    implementation(Libs.retrofit)
    implementation(Libs.retrofit2_kotlinx_serialization_converter)

    testImplementation(Libs.junit)
    testImplementation(Libs.kotlin_test)
    testImplementation(Libs.kotlinx_coroutines_test)
    testImplementation( Libs.mockk)
}
