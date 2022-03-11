plugins {
    `java-library`
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-bootstrap-java8"))
    api(project(":pinpoint-bootstrap-java9"))
    api(project(":pinpoint-bootstrap-java9-internal"))
}
