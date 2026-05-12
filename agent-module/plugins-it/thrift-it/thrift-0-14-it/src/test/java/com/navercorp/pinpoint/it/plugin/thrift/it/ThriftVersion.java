package com.navercorp.pinpoint.it.plugin.thrift.it;

/**
 * <a href="https://issues.apache.org/jira/browse/THRIFT-5274"/>Thrift 0.13.0 does not work with JDK8</a>
 */
public class ThriftVersion {
    // compatibility issue, ignore 0.23.0
    public static final String VERSION_14_22 = "org.apache.thrift:libthrift:[0.14.0,0.22.0]";
}
