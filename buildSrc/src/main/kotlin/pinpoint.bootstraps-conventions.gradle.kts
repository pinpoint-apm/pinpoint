plugins {
    `java-library`
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
    runtimeOnly(project(":pinpoint-bootstrap-java8"))
    runtimeOnly(project(":pinpoint-bootstrap-java9"))
    runtimeOnly(project(":pinpoint-bootstrap-java9-internal"))
}

val instrumentedJars by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    // If you want this configuration to share the same dependencies, otherwise omit this line
    extendsFrom(configurations["implementation"], configurations["runtimeOnly"])
}