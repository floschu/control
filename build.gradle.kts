buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.0")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.7.1")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.31")

        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.31")
    }
}

plugins {
    jacoco
    id("org.jlleitschuh.gradle.ktlint").version("10.0.0")
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
