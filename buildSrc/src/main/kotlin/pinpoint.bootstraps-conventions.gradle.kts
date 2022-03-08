plugins {
    `java-library`
}

dependencies {
    implementation(project(":pinpoint-bootstrap-core"))
    implementation(project(":pinpoint-bootstrap-java8"))
    implementation(project(":pinpoint-bootstrap-java9"))
    implementation(project(":pinpoint-bootstrap-java9-internal"))
}
