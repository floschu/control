plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
    jacoco
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

// ---- api-validation --- //

apiValidation {
    ignoredProjects.addAll(
        listOf(
            "kotlin-counter",
            "android-counter",
        ),
    )
}

// ---- end api-validation --- //

// ---- jacoco --- //

subprojects {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jacoco") {
                    useVersion("0.8.12")
                }
            }
        }
    }
}

// ---- end jacoco --- //
