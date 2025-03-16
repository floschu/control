import kotlinx.validation.ExperimentalBCVApi

plugins {
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

// ---- api-validation --- //

apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib { enabled = true }
    ignoredProjects.addAll(
        listOf(
            "kotlin-counter",
            "android-counter",
        ),
    )
}

// ---- end api-validation --- //
