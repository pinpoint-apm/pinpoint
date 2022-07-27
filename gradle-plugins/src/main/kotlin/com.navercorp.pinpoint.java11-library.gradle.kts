plugins {
    id("com.navercorp.pinpoint.java-library")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
