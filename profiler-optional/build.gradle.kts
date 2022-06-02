plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(project(":pinpoint-profiler-optional-jdk7"))
        api(project(":pinpoint-profiler-optional-jdk8"))
        api(project(":pinpoint-profiler-optional-jdk9"))
    }
}