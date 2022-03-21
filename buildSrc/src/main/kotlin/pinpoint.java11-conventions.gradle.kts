plugins {
    id("pinpoint.java-conventions")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
