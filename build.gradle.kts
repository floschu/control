buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")

        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.13.2")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.25.3")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.10")

        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.20")
    }
}

plugins {
    jacoco
    id("org.jlleitschuh.gradle.ktlint").version("11.6.1")
    `maven-publish`
    signing
    id("com.github.ben-manes.versions").version("0.47.0")
}

// ---- api-validation --- //

apply(plugin = "binary-compatibility-validator")

configure<kotlinx.validation.ApiValidationExtension> {
    ignoredProjects.addAll(
        listOf(
            "kotlin-counter",
            "android-counter",
            "android-compose",
            "android-github"
        )
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
        jcenter()
    }
}
