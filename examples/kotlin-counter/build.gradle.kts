plugins {
    id("kotlin")
}

dependencies {
    implementation(project(":control-core"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
