plugins {
    id("pinpoint.java-library")
    id("pinpoint.jdk7-dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}
