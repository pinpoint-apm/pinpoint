package com.navercorp.pinpoint.tools.network.thrift;

import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.bootstrap.config.util.ValueAnnotationProcessor;

import java.util.Properties;

public class ThriftTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";

    @Value("${profiler.collector.span.ip}")
    private String collectorSpanServerIp = DEFAULT_IP;
    @Value("${profiler.collector.span.port}")
    private int collectorSpanServerPort = 9996;

    @Value("${profiler.collector.stat.ip}")
    private String collectorStatServerIp = DEFAULT_IP;
    @Value("${profiler.collector.stat.port}")
    private int collectorStatServerPort = 9995;

    @Value("${profiler.collector.tcp.ip}")
    private String collectorTcpServerIp = DEFAULT_IP;
    @Value("${profiler.collector.tcp.port}")
    private int collectorTcpServerPort = 9994;

    public String getCollectorSpanServerIp() {
        return collectorSpanServerIp;
    }

    public int getCollectorSpanServerPort() {
        return collectorSpanServerPort;
    }

    public String getCollectorStatServerIp() {
        return collectorStatServerIp;
    }

    public int getCollectorStatServerPort() {
        return collectorStatServerPort;
    }

    public String getCollectorTcpServerIp() {
        return collectorTcpServerIp;
    }

    public int getCollectorTcpServerPort() {
        return collectorTcpServerPort;
    }

    public void read(Properties properties) {
        final ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(this, properties);
    }
}
