plugins {
    id("kotlin")
}

dependencies {
    implementation(project(":control-core"))
    testImplementation("at.florianschuster.test:coroutines-test-extensions:0.1.2")
}
