package pinpoint.toolchain

plugins {
    id("pinpoint.java-library")
    id("pinpoint.dependency.java7")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}
