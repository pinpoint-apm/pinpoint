plugins {
    `kotlin-dsl`
    `maven-publish`
    `version-catalog`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    implementation("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
}

publishing {
    repositories {
        maven {
            name = "ossrh"

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}

tasks.publish {
    dependsOn("check")
}

catalog {
    versionCatalog {
        version("spring", "5.3.13")
        library("spring-core", "org.codehaus.groovy", "groovy").versionRef("spring")
    }
}

group = "com.navercorp.pinpoint"