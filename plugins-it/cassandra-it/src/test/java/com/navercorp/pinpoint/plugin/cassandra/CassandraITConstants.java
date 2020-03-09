package com.navercorp.pinpoint.plugin.cassandra;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import org.testcontainers.containers.CassandraContainer;

public final class CassandraITConstants {
    public static final String COMMONS_PROFILER = "com.navercorp.pinpoint:pinpoint-commons-profiler:" + Version.VERSION;
    public static final String CASSANDRA_TESTCONTAINER = "org.testcontainers:cassandra:" + TestcontainersOption.VERSION;

    // https://hub.docker.com/_/cassandra
    public static final String CASSANDRA_2_X_IMAGE = CassandraContainer.IMAGE + ":2.2.16";
    public static final String CASSANDRA_3_X_IMAGE = CassandraContainer.IMAGE + ":3.11.6";
}
