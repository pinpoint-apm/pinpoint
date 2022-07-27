plugins {
    `java-platform`
    `maven-publish`
}

group = "com.navercorp.pinpoint"
version = System.getProperty("version")

publishing {
    publications {
        create<MavenPublication>("pinpoint") {
            from(components["javaPlatform"])
        }
    }
}