package pinpoint.toolchain

plugins {
    id("pinpoint.java-library")
    id("pinpoint.dependency.java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
