plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(libs.bundles.aws.lambda)
    implementation(libs.bundles.log4j)
}
