buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(Libs.com_android_tools_build_gradle)
        classpath(Libs.kotlin_gradle_plugin)
        classpath(Libs.kotlin_serialization)
    }
}

plugins {
    buildSrcVersions
    id("org.jlleitschuh.gradle.ktlint").version(Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin)
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

buildSrcVersions {
    indent = "    "
}
