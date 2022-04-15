package pinpoint.toolchain

import gradle.kotlin.dsl.accessors._f4124090576aed771f557cac1ca7fd48.java

plugins {
    id("pinpoint.java-library")
    id("pinpoint.dependency.java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
