plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "at.florianschuster.control.androidcounter"
    compileSdk = 33
    defaultConfig {
        applicationId = "at.florianschuster.control.androidcounter"
        minSdk = 23
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":control-core"))
    implementation(project(":examples:kotlin-counter"))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:1.2.0")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-core:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.0")
    debugImplementation("androidx.fragment:fragment-testing:1.6.0")

    androidTestImplementation(kotlin("test"))
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
}
