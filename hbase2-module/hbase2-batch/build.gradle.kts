plugins {
    id("com.navercorp.pinpoint.java11-library")
}

dependencies {
    api(project(":pinpoint-batch"))
}

description = "pinpoint-hbase2-batch"
