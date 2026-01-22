import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.kover)
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()

    watchosX64()
    watchosArm64()
    watchosSimulatorArm64()

    linuxX64()
    linuxArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

// ---- code coverage --- //

kover {
    reports {
        filters { excludes { classes("*DefaultControllerTag*") } }
        verify {
            rule("line coverage") {
                bound {
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                    coverageUnits = CoverageUnit.LINE
                    minValue = 100
                }
            }
            rule("branch coverage") {
                bound {
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                    coverageUnits = CoverageUnit.BRANCH
                    minValue = 100
                }
            }
            rule("instruction coverage") {
                bound {
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                    coverageUnits = CoverageUnit.INSTRUCTION
                    minValue = 100
                }
            }
        }
    }
}

// ---- end code coverage --- //

// ---- publishing --- //

group = "at.florianschuster.control"
version = System.getenv("libraryVersionTag") ?: "local"

mavenPublishing {
    // Snapshots will be immediately available at:
    // https://s01.oss.sonatype.org/content/repositories/snapshots/at/florianschuster/control/
    publishToMavenCentral(automaticRelease = true)
    if (version != "local") signAllPublications()
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
