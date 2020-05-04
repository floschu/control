plugins {
    id("kotlin")
    id("jacoco")
    id("info.solidsoft.pitest")
}

dependencies {
    api(project(":control-core"))
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutines_test_extensions)
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        csv.isEnabled = false
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "1".toBigDecimal() } }
    }
}

pitest {
    pitestVersion.set(Versions.gradle_pitest_plugin)
    targetClasses.add("at.florianschuster.control.test.*")
    mutationThreshold.set(100)
    threads.set(4)
    jvmArgs.add("-ea")
    avoidCallsTo.addAll(
        "kotlin.jvm.internal",
        "kotlin.ResultKt",
        "kotlinx.coroutines"
    )
    verbose.set(true)
}


apply(from = "$rootDir/gradle/deploy.gradle")
