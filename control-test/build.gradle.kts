import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }

sourceSets["main"].withConvention(KotlinSourceSet::class) { kotlin.srcDir("src/main/kotlin") }

dependencies {
    api(project(":control-core"))
    api(Libs.junit)
    api(Libs.kotlin_test)
    api(Libs.kotlinx_coroutines_test)
}

apply(from = "$rootDir/gradle/deploy.gradle")