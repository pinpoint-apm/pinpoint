plugins {
    `version-catalog`
    `maven-publish`
}

catalog {
    // declare the aliases, bundles and versions in this block
    versionCatalog {
        library("pinpoint", "com.navercorp.pinpoint:pinpoint-version-catalog:$version")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}