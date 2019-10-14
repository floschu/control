import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = Config.jvmTarget.toString()
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = Config.jvmTarget.toString()
}

dependencies {
    api(Libs.kotlin_stdlib)
    api(Libs.kotlinx_coroutines_core)

    testImplementation(Libs.junit)
    testImplementation(Libs.kotlin_test)
    testImplementation(Libs.kotlinx_coroutines_test)
}

apply(from = "$rootDir/gradle/deploy.gradle")
