plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "at.florianschuster.control.counterexample"
        minSdk = 23
        targetSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:1.2.0")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-core:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    debugImplementation("androidx.fragment:fragment-testing:1.3.6")

    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:core-ktx:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("at.florianschuster.test:coroutines-test-extensions:0.1.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
}
