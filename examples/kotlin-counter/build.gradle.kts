plugins {
    id("kotlin")
}

dependencies {
    implementation(project(":control-core"))
    testImplementation(Libs.coroutines_test_extensions)
}
