plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(Config.targetSdkVersion)
    defaultConfig {
        targetSdkVersion(Config.targetSdkVersion)
        minSdkVersion(Config.minSdkVersion)
    }
    compileOptions {
        sourceCompatibility = Config.jvmTarget
        targetCompatibility = Config.jvmTarget
    }
    lintOptions {
        isAbortOnError = true
    }
}

dependencies {
    api(project(":control-core"))
    api(Libs.lifecycle_extensions)
}