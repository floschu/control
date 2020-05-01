plugins {
    id("kotlin")
    id("jacoco")
    id("kotlinx-atomicfu")
    id("info.solidsoft.pitest")
}

dependencies {
    api(Libs.kotlin_stdlib)
    api(Libs.kotlinx_coroutines_core)
    testImplementation(Libs.mockk)
    testImplementation(Libs.flow_extensions)
}

tasks.jacocoTestReport {
    reports {
        html.isEnabled = false
        csv.isEnabled = false
        xml.isEnabled = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                // jacoco cannot handle inline functions
                exclude("at/florianschuster/control/DefaultTagKt.class")
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "0.9".toBigDecimal() } }
    }
}

pitest {
    // mutationThreshold.set(90) TODO enable
    excludedClasses.add("at.florianschuster.control.ExtensionsKt")
    excludedClasses.add("at.florianschuster.control.DefaultTagKt**")
}


apply(from = "$rootDir/gradle/deploy.gradle")
