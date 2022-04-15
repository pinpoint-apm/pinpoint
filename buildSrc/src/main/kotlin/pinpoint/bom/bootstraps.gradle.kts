package pinpoint.bom

plugins {
    `java-library`
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
    runtimeOnly(project(":pinpoint-bootstrap-java8"))
    runtimeOnly(project(":pinpoint-bootstrap-java9"))
    runtimeOnly(project(":pinpoint-bootstrap-java9-internal"))
}