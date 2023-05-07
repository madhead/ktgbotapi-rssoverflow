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
    implementation(libs.jackson.databind)
    implementation(libs.dynamodb)
    implementation(libs.rome)
    implementation(libs.tgbotapi)
}
