plugins {
    id("kotlin")
    id("jacoco")
    id("info.solidsoft.pitest")
    id("com.vanniktech.maven.publish")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")
}

// ---- jacoco --- //

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "0.95".toBigDecimal() } }
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            // jacoco cannot handle inline functions properly
            exclude(
                "at/florianschuster/control/DefaultTagKt*",
                "at/florianschuster/control/TakeUntilKt*",
            )
            // builders
            exclude(
                "at/florianschuster/control/ControllerKt*",
                "at/florianschuster/control/EffectControllerKt*",
            )
        }
    )
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// ---- end jacoco --- //

// ---- pitest --- //

pitest {
    targetClasses.add("at.florianschuster.control.*")
    mutationThreshold.set(100)
    excludedClasses.addAll(
        // inline function
        "at.florianschuster.control.DefaultTagKt**",
        "at.florianschuster.control.TakeUntilKt**",

        // builder
        "at.florianschuster.control.Controller**",
        "at.florianschuster.control.EffectController**",

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
