plugins {
    `java-library`
}

dependencies {
    implementation(project(":pinpoint-profiler-optional-jdk7"))
    implementation(project(":pinpoint-profiler-optional-jdk8"))
    implementation(project(":pinpoint-profiler-optional-jdk9"))
}
