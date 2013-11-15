package com.nhn.pinpoint.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public enum ServiceType {

	UNDEFINED((short) -1, "UNDEFINED", true, false, false, HistogramSchema.NORMAL),
	
    UNKNOWN((short) 0, "UNKNOWN", false, true, false, HistogramSchema.NORMAL),
    UNKNOWN_CLOUD((short) 1, "UNKNOWN_CLOUD", false, true, false, HistogramSchema.NORMAL),
    // TODO: 이 client와 아래 user를 구분해서 사용하도록 web과 agent를 수정해야함.
    CLIENT((short) 2, "CLIENT", false, true, false, HistogramSchema.NORMAL),
    UNKNOWN_GROUP((short) 3, "UNKNOWN_GROUP", false, true, false, HistogramSchema.NORMAL),
    USER((short) 4, "USER", false, false, false, HistogramSchema.NORMAL),

    // WAS류 1000번 부터 시작
    TOMCAT((short) 1010, "TOMCAT", false, true, false, HistogramSchema.NORMAL),
    BLOC((short) 1020, "BLOC", false, true, false, HistogramSchema.NORMAL),
    
    /**
     * xxx_EXECUTE_QUERY만 server map통계정보에 집계된다.
     */
    // DB 2000
    UNKNOWN_DB((short) 2050, "UNKNOWN_DB", true, false, true, HistogramSchema.NORMAL),
    UNKNOWN_DB_EXECUTE_QUERY((short) 2051, "UNKNOWN_DB", true, true, true, HistogramSchema.NORMAL),

    MYSQL((short) 2100, "MYSQL", true, false, true, HistogramSchema.NORMAL),
    MYSQL_EXECUTE_QUERY((short) 2101, "MYSQL", true, true, true, HistogramSchema.NORMAL),

    MSSQL((short) 2200, "MSSQL", true, false, true, HistogramSchema.NORMAL),
    MSSQL_EXECUTE_QUERY((short) 2201, "MSSQL", true, true, true, HistogramSchema.NORMAL),

    ORACLE((short) 2300, "ORACLE", true, false, true, HistogramSchema.NORMAL),
    ORACLE_EXECUTE_QUERY((short) 2301, "ORACLE", true, true, true, HistogramSchema.NORMAL),

    CUBRID((short) 2400, "CUBRID", true, false, true, HistogramSchema.NORMAL),
    CUBRID_EXECUTE_QUERY((short) 2401, "CUBRID", true, true, true, HistogramSchema.NORMAL),

    // TODO internal method를 여기에 넣기 애매하긴 하나.. 일단 그대로 둠.
    INTERNAL_METHOD((short) 5000, "INTERNAL_METHOD", false, false, false, HistogramSchema.NORMAL),

    SPRING((short) 5050, "SPRING", false, false, false, HistogramSchema.NORMAL),
    SPRING_MVC((short) 5051, "SPRING", false, false, false, HistogramSchema.NORMAL),

    DBCP((short) 6050, "DBCP", false, false, false, HistogramSchema.NORMAL),

    // memory cache  8000
    MEMCACHED((short) 8050, "MEMCACHED", true, true, false, HistogramSchema.FAST),
    MEMCACHED_FUTURE_GET((short) 8051, "MEMCACHED", true, false, false, HistogramSchema.FAST),
    ARCUS((short) 8100, "ARCUS", true, true, true, HistogramSchema.FAST),
    ARCUS_FUTURE_GET((short) 8101, "ARCUS", true, false, true, HistogramSchema.FAST),

    // connector류
    HTTP_CLIENT((short) 9050, "HTTP_CLIENT", false, true, false, HistogramSchema.NORMAL),
    JDK_HTTPURLCONNECTOR((short) 9055, "JDK_HTTPCONNECTOR", false, true, false, HistogramSchema.NORMAL),
	NPC_CLIENT((short) 9060, "NPC_CLIENT", false, true, false, HistogramSchema.NORMAL);

    private final short code;
    private final String desc;
    private final boolean terminal;
    
    // TODO rpc 호출에 대해서만 통계정보를 남길 것이니, rpc()로 바꾸는건 어떨지???
    private final boolean recordStatistics;
    
    // DetinationId를 포함시켜 api를 출력하지 여부
    private final boolean includeDestinationId;
    private final HistogramSchema histogramSchema;

    ServiceType(short code, String desc, boolean terminal, boolean recordStatistics, boolean includeDestinationId, HistogramSchema histogramSchema) {
        this.code = code;
        this.desc = desc;
        this.terminal = terminal;
        this.recordStatistics = recordStatistics;
        this.includeDestinationId = includeDestinationId;
        this.histogramSchema = histogramSchema;
    }

    public static List<ServiceType> findDesc(String desc) {
        if (desc == null) {
            throw new NullPointerException("desc must not be null");
        }
        return STATISTICS_LOOKUP_TABLE.get(desc);
    }

    public boolean isInternalMethod() {
    	return this == INTERNAL_METHOD;
    }

    public boolean isRpcClient() {
        return code >= 9000 && code < 10000;
    }

    public boolean isIndexable() {
        return !terminal && !isRpcClient() && code > 1000;
    }

    // TODO rpc 호출에 대해서만 통계정보를 남길 것이니, isRpc()로 바꾸는건 어떨지???
    public boolean isRecordStatistics() {
        return recordStatistics;
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

    public boolean isIncludeDestinationId() {
        return includeDestinationId;
    }

    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

	public boolean isWas() {
		return code >= 1000 && code < 2000;
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
    private static final Map<String, List<ServiceType>> STATISTICS_LOOKUP_TABLE = new HashMap<String, List<ServiceType>>();

    static {
        initializeLookupTable();
        initializeStatisticsLookupTable();
    }

    private static void initializeStatisticsLookupTable() {
        ServiceType[] values = ServiceType.values();
        for (ServiceType serviceType : values) {
            if(serviceType.isRecordStatistics()) {
                List<ServiceType> serviceTypeList = STATISTICS_LOOKUP_TABLE.get(serviceType.getDesc());
                if (serviceTypeList == null) {
                    serviceTypeList = new ArrayList<ServiceType>();
                    serviceTypeList.add(serviceType);
                    STATISTICS_LOOKUP_TABLE.put(serviceType.getDesc(), serviceTypeList);
                } else {
                    serviceTypeList.add(serviceType);
                }
            }
        }
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
