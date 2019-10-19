import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
}

sourceSets["main"].java.srcDir("src/main/java")
sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("src/main/kotlin")
}

dependencies {
    api(Libs.kotlin_stdlib)
    api(Libs.kotlinx_coroutines_core)
    api(Libs.junit)
    api(Libs.kotlin_test)
    api(Libs.kotlinx_coroutines_test)
}

apply(from = "$rootDir/gradle/deploy.gradle")