plugins {
    `java-library`
}

dependencies {
    implementation(project(":pinpoint-agentsdk-async-plugin"))
    implementation(project(":pinpoint-common-servlet"))
    implementation(project(":pinpoint-external-plugins"))
}
