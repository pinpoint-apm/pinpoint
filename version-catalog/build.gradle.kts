plugins {
    `version-catalog`
    `maven-publish`
}

catalog {
    // declare the aliases, bundles and versions in this block
    versionCatalog {
        from(rootProject.files("gradle/libs.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}

group = "com.navercorp.pinpoint"