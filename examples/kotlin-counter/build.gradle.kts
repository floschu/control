plugins {
    id("kotlin")
}

dependencies {
    implementation(project(":control-core"))

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1")
}
