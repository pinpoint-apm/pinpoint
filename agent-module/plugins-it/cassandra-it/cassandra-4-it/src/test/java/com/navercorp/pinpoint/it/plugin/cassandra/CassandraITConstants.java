package com.navercorp.pinpoint.it.plugin.cassandra;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;

public final class CassandraITConstants {
    public static final String COMMONS_PROFILER = "com.navercorp.pinpoint:pinpoint-commons-profiler:" + Version.VERSION;
    public static final String CASSANDRA_TESTCONTAINER = "org.testcontainers:cassandra:" + TestcontainersOption.VERSION;

    // https://hub.docker.com/_/cassandra
    public static final String CASSANDRA_3_X_IMAGE = "cassandra:3.11.16";

    public static final String TEST_KEYSPACE = "mykeyspace";
    public static final String TEST_TABLE = "mytable";
}
