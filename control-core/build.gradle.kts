plugins {
    id("kotlin")
    id("jacoco")
    id("info.solidsoft.pitest")
}

dependencies {
    api(Libs.kotlinx_coroutines_core)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutines_test_extensions)
}

tasks.compileTestKotlin {
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
    )
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        csv.isEnabled = false
    }
    classDirectories.setFrom(
        files(classDirectories.files.map { file ->
            fileTree(file) {
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
    targetClasses.add("at.florianschuster.control.*")
    mutationThreshold.set(100)
    excludedClasses.addAll(
        "at.florianschuster.control.DefaultTagKt**", // inline function
        "at.florianschuster.control.ExtensionsKt**", // too many inline collects

        // inlined invokeSuspend
        "at.florianschuster.control.ControllerImplementation\$stateJob\$1",
        "at.florianschuster.control.ControllerImplementation\$stateJob\$1\$2"
    )
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
