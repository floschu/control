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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
}

dependencies {
    implementation(project(":control-core"))

    implementation(Libs.appcompat)
    implementation(Libs.constraintlayout)
    implementation(Libs.lifecycle_runtime_ktx)
    implementation(Libs.flowbinding_android)
    implementation(Libs.flowbinding_core)
    implementation(Libs.flowbinding_recyclerview)
    implementation(Libs.activity_ktx)
    implementation(Libs.kotlinx_serialization_runtime)
    implementation(Libs.retrofit)
    implementation(Libs.retrofit2_kotlinx_serialization_converter)

    testImplementation(Libs.flow_extensions)
    testImplementation(Libs.mockk)
}
