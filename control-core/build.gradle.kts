import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("kotlin")
    id("jacoco")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }

sourceSets["main"].withConvention(KotlinSourceSet::class) { kotlin.srcDir("src/main/kotlin") }

tasks {
    getByName<JacocoReport>("jacocoTestReport") {
        reports {
            xml.isEnabled = true
            html.isEnabled = false
            csv.isEnabled = false
        }
    }
}

dependencies {
    api(Libs.kotlin_stdlib)
    api(Libs.kotlinx_coroutines_core)

    testImplementation(Libs.mockk)
    testImplementation(Libs.flow_extensions)
}

apply(from = "$rootDir/gradle/deploy.gradle")
