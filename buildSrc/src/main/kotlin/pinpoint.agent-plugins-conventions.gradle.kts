plugins {
    `java-library`
}

dependencies {
    implementation(project(":pinpoint-agent-proxy-common"))
    implementation(project(":pinpoint-agent-proxy-apache-plugin"))
    implementation(project(":pinpoint-agent-proxy-app-plugin"))
    implementation(project(":pinpoint-agent-proxy-nginx-plugin"))
    implementation(project(":pinpoint-agent-proxy-user-plugin"))
}
