import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("kotlin")
    jacoco
    alias(libs.plugins.vanniktech.maven.publish)
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

// ---- jacoco --- //

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule { limit { minimum = "0.9".toBigDecimal() } }
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// ---- end jacoco --- //

// ---- publishing --- //

group = "at.florianschuster.control"
version = System.getenv("libraryVersionTag")

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    coordinates(group.toString(), "control-core", version.toString())
    pom {
        name = "control-core"
        description = "coroutines flow based uni-directional architecture"
        inceptionYear = "2019"
        url = "https://github.com/floschu/control"
        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "floschu"
                name = "Florian Schuster"
                url = "https://github.com/floschu"
            }
        }
        scm {
            url = "https://github.com/floschu/control"
            connection = "scm:git@github.com:floschu/control.git"
            developerConnection = "scm:git@github.com:floschu/control.git"
        }
    }
}

// ---- end publishing --- //
