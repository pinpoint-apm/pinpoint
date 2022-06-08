plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
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
}

tasks.publish {
    dependsOn("check")
}

group = "com.navercorp.pinpoint"