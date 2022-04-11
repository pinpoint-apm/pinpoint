plugins {
    id("pinpoint.java-conventions")
    id("pinpoint.java-dependency-management-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
