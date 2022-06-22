plugins {
    `java-platform`
    `maven-publish`
}

group = "com.navercorp.pinpoint"

publishing {
    publications {
        create<MavenPublication>("pinpoint") {
            from(components["javaPlatform"])
        }
    }
}