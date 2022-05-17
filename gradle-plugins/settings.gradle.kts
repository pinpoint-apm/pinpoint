rootProject.name="pinpoint-gradle-plugins"

pluginManagement {
    plugins {
    }
    resolutionStrategy {
    }
    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
    }
}

//dependencyResolutionManagement {
//    versionCatalogs {
//        create("libs") {
//            from("com.navercorp.pinpoint:pinpoint-gradle-plugins:2.4.0-SNAPSHOT")
//        }
//    }
//}