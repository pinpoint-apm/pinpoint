plugins {
    id("pinpoint.java-conventions")
    id("pinpoint.jdk7-dependency-management-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}
