import gradle.kotlin.dsl.accessors._fa8ae7a11e714e4557697295cd0bbd61.publishing
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

plugins {
    `maven-publish`
}

publishing {
    publications.create<MavenPublication>("pinpoint") {
        from(components["java"])
        pom {
            name.set("pinpoint")
            description.set("Pinpoint APM, Application Performance Management tool for large-scale distributed systems")
            url.set("https://github.com/pinpoint-apm/pinpoint")
            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            inceptionYear.set("2012")

            scm {
                url.set("https://github.com/pinpoint-apm/pinpoint")
                connection.set("scm:git:git://github.com/pinpoint-apm/pinpoint.git")
                developerConnection.set("scm:git:ssh://git@github.com/pinpoint-apm/pinpoint.git")
            }
            developers {
                developer {
                    id.set("emeroad")
                    name.set("WoonDuk Kang")
                    email.set("emeroad@gamil.com")
                    organization.set("NAVER Corp.")
                    organizationUrl.set("http://www.naver.com")
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"

            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}