plugins {
    id("com.navercorp.pinpoint.java-library")
    id("com.navercorp.pinpoint.maven-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
