plugins {
    id("kotlin")
    id("jacoco")
    id("info.solidsoft.pitest")
    id("com.vanniktech.maven.publish")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("at.florianschuster.test:coroutines-test-extensions:0.1.2")
}

// ---- kotlin --- //

tasks.compileTestKotlin {
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xuse-experimental=kotlinx.coroutines.FlowPreview"
    )
}

// ---- end kotlin --- //

// ---- jacoco --- //

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "0.94".toBigDecimal() } }
    }
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
                // jacoco cannot handle inline functions properly
                exclude(
                    "at/florianschuster/control/DefaultTagKt.class",
                    "at/florianschuster/control/ExtensionsKt.class"
                )
            }
        })
    )
}

// ---- end jacoco --- //

// ---- pitest --- //

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

// ---- end pitest --- //

// ---- publishing --- //

version = System.getenv("libraryVersionTag")

// ---- end publishing --- //
