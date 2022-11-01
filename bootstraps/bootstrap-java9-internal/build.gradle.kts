plugins {
    id("com.navercorp.pinpoint.java11-library")
}

dependencies {
    api(project(":pinpoint-bootstrap-core"))
    api(project(":pinpoint-commons"))
    testImplementation(libs.log4j.api)
    testImplementation(libs.log4j.slf4j.impl)
    testImplementation(libs.log4j.core)
    testImplementation(libs.log4j.jcl)
    testImplementation(libs.mysql.connector.java)
    testImplementation(libs.commons.io)
}

description = "pinpoint-bootstrap-java9-internal"

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.9"
    targetCompatibility = "1.9"
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.loader=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.module=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED")
}