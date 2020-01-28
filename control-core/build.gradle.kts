plugins {
    id("kotlin")
    id("jacoco")
}

dependencies {
    api(Libs.kotlin_stdlib)
    api(Libs.kotlinx_coroutines_core)
    testImplementation(Libs.mockk)
    testImplementation(Libs.flow_extensions)
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = false
        csv.isEnabled = false
    }
}

apply(from = "$rootDir/gradle/deploy.gradle")
