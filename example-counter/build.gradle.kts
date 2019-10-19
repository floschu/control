plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "at.florianschuster.control.counterexample"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":control-core"))

    implementation(Libs.appcompat)
    implementation(Libs.constraintlayout)
    implementation(Libs.lifecycle_runtime_ktx)
    implementation(Libs.corbind)
    implementation(Libs.corbind_core)

    testImplementation(project(":control-test"))
}
