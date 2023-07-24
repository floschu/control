buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")

        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.4")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.13.2")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")

        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.0")
    }
}

plugins {
    jacoco
    id("org.jlleitschuh.gradle.ktlint").version("11.5.0")
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
