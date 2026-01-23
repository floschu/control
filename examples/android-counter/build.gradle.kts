import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("kotlin-android")
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val jenvContent = File(".java-version").readText().trim()

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(jenvContent))
    }
}

android {
    namespace = "at.florianschuster.control.counter"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "at.florianschuster.control.counter"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        val javaVersion = JavaVersion.toVersion(jenvContent)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
    sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
    packaging {
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":control-core"))
    implementation(project(":examples:kotlin-counter"))

    implementation(libs.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.core)

    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.junit.ktx)
}
