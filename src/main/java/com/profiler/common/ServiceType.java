package com.profiler.common;

import java.util.HashMap;
import java.util.Map;

public enum ServiceType {

    UNKNOWN((short) 0, "UNKNOWN", false, Histogram.NORMAL),
    UNKNOWN_CLOUD((short) 1, "UNKNOWN_CLOUD", false, Histogram.NORMAL),

    // TODO internal method를 여기에 넣기 애매하긴 하나.. 일단 그대로 둠.
    INTERNAL_METHOD((short) 2, "INTERNAL_METHOD", false, Histogram.NORMAL),

    TOMCAT((short) 1001, "TOMCAT", false, Histogram.NORMAL),
    BLOC((short) 1002, "BLOC", false, Histogram.NORMAL),

    MEMCACHED((short) 2001, "MEMCACHED", true, Histogram.FAST),
    ARCUS((short) 2002, "ARCUS", true, Histogram.FAST),

    MYSQL((short) 3001, "MYSQL", true, Histogram.NORMAL),
    MSSQL((short) 3002, "MSSQL", true, Histogram.NORMAL),
    ORACLE((short) 3003, "ORACLE", true, Histogram.NORMAL),
    CUBRID((short) 3004, "CUBRID", true, Histogram.NORMAL),

    HTTP_CLIENT((short) 9001, "HTTP_CLIENT", false, Histogram.NORMAL);

    private final short code;
    private final String desc;
    private final boolean terminal;

    private final Histogram histogram;
    private short[] histogramSlots;                // response time histogram slots

    ServiceType(short code, String desc, boolean terminal, Histogram histogram) {
        this.code = code;
        this.desc = desc;
        this.terminal = terminal;
        this.histogram = histogram;
    }

    public static ServiceType parse(String desc) {
        ServiceType[] values = ServiceType.values();
        for (ServiceType type : values) {
            if (type.desc.equals(desc)) {
                return type;
            }
        }
        return UNKNOWN;
    }


    public boolean isInternalMethod() {
        return code == 2;
    }

    public boolean isRpcClient() {
        return code >= 9000 && code < 10000;
    }

    public boolean isIndexable() {
        return !terminal && !isRpcClient() && code > 1000;
    }

    public boolean isUnknown() {
        return this == ServiceType.UNKNOWN || this == ServiceType.UNKNOWN_CLOUD;
    }

    public short getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public short[] getHistogramSlots() {
        return histogramSlots;
    }

    @Override
    public String toString() {
        return desc;
    }

    public static ServiceType findServiceType(short code) {
        ServiceType serviceType = CODE_LOOKUP_TABLE.get(code);
        if (serviceType == null) {
            return UNKNOWN;
        }
        return serviceType;
    }


    private static final Map<Short, ServiceType> CODE_LOOKUP_TABLE = new HashMap<Short, ServiceType>();

    static {
        initializeLookupTable();
    }

    public static void initializeLookupTable() {
        ServiceType[] values = ServiceType.values();
        for (ServiceType serviceType : values) {
            ServiceType check = CODE_LOOKUP_TABLE.put(serviceType.code, serviceType);
            if (check != null) {
                throw new IllegalStateException("duplicated code found. code:" + serviceType.code);
            }
        }
    }
}
