plugins {
    id("pinpoint.java-library")
    id("pinpoint.java-dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
