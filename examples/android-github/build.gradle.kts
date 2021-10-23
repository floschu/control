plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlinx-serialization")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "at.florianschuster.control.githubexample"
        minSdk = 23
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":control-core"))

    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("io.coil-kt:coil:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:1.2.0")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-core:1.2.0")
    implementation("io.github.reactivecircus.flowbinding:flowbinding-recyclerview:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    debugImplementation("androidx.fragment:fragment-testing:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.0")
    implementation("io.ktor:ktor-client-cio:1.6.4")
    implementation("io.ktor:ktor-client-json-jvm:1.6.4")
    implementation("io.ktor:ktor-client-logging-jvm:1.6.4")
    implementation("io.ktor:ktor-client-serialization-jvm:1.6.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("com.google.android.material:material:1.4.0")

    testImplementation("at.florianschuster.test:coroutines-test-extensions:0.1.2")
    testImplementation("io.mockk:mockk:1.12.0")

    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:core-ktx:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("at.florianschuster.test:coroutines-test-extensions:0.1.2")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("io.mockk:mockk-android:1.12.0")
}
