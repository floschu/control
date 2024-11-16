buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.pitest.gradle.plugin)
        classpath(libs.binary.compat.validator)
        classpath(libs.maven.publish.plugin)
        classpath(libs.dokka.gradle.plugin)

        // examples
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.serialization)
    }
}

plugins {
    jacoco
    alias(libs.plugins.ktlint)
    `maven-publish`
    signing
}

// ---- api-validation --- //

apply(plugin = "binary-compatibility-validator")

configure<kotlinx.validation.ApiValidationExtension> {
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
                    useVersion("0.8.7")
                }
            }
        }
    }
}

// ---- end jacoco --- //

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
