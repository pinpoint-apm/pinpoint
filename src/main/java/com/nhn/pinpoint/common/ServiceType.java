package com.nhn.pinpoint.common;

import com.nhn.pinpoint.common.util.RpcCodeRange;

import java.util.*;

/**
 * @author emeroad
 * @author netspider
 */
public enum ServiceType {

	/**
	 * 정의되지 않은 서비스 코드,
	 */
	UNDEFINED((short) -1, "UNDEFINED", true, false, false, HistogramSchema.NORMAL_SCHEMA),
	
	/**
	 * agent가 설치되지 않은 피호출자.
	 */
    UNKNOWN((short) 1, "UNKNOWN", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    
    /**
     * 사용자
     */
    USER((short) 2, "USER", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    
    /**
     * UNKNOWN의 그룹, UI에서만 사용함.
     */
    UNKNOWN_GROUP((short) 3, "UNKNOWN_GROUP", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    
    /**
     * TEST의 그룹 - 테스트 실행 시 사용
     */
    TEST((short) 5, "TEST", false, false, false, HistogramSchema.NORMAL_SCHEMA),

    // WAS류 1000번 부터 시작
    STAND_ALONE((short) 1000, "STAND_ALONE", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    // testcase를 돌릴때 사용하는 was로 정의 하자.
    TEST_STAND_ALONE((short) 1005, "TEST_STAND_ALONE", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    TOMCAT((short) 1010, "TOMCAT", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    BLOC((short) 1020, "BLOC", false, true, false, HistogramSchema.NORMAL_SCHEMA),

    /**
     * xxx_EXECUTE_QUERY만 server map통계정보에 집계된다.
     */
    // DB 2000
    UNKNOWN_DB((short) 2050, "UNKNOWN_DB", true, false, true, HistogramSchema.NORMAL_SCHEMA),
    UNKNOWN_DB_EXECUTE_QUERY((short) 2051, "UNKNOWN_DB", true, true, true, HistogramSchema.NORMAL_SCHEMA),

    MYSQL((short) 2100, "MYSQL", true, false, true, HistogramSchema.NORMAL_SCHEMA),
    MYSQL_EXECUTE_QUERY((short) 2101, "MYSQL", true, true, true, HistogramSchema.NORMAL_SCHEMA),

    MSSQL((short) 2200, "MSSQL", true, false, true, HistogramSchema.NORMAL_SCHEMA),
    MSSQL_EXECUTE_QUERY((short) 2201, "MSSQL", true, true, true, HistogramSchema.NORMAL_SCHEMA),

    ORACLE((short) 2300, "ORACLE", true, false, true, HistogramSchema.NORMAL_SCHEMA),
    ORACLE_EXECUTE_QUERY((short) 2301, "ORACLE", true, true, true, HistogramSchema.NORMAL_SCHEMA),

    CUBRID((short) 2400, "CUBRID", true, false, true, HistogramSchema.NORMAL_SCHEMA),
    CUBRID_EXECUTE_QUERY((short) 2401, "CUBRID", true, true, true, HistogramSchema.NORMAL_SCHEMA),

    // FIXME internal method를 여기에 넣기 애매하긴 하나.. 일단 그대로 둠.
    INTERNAL_METHOD((short) 5000, "INTERNAL_METHOD", false, false, false, HistogramSchema.NORMAL_SCHEMA),

    SPRING((short) 5050, "SPRING", false, false, false, HistogramSchema.NORMAL_SCHEMA),
    SPRING_MVC((short) 5051, "SPRING", false, false, false, HistogramSchema.NORMAL_SCHEMA),
    // FIXME 스프링 관련 코드들 어떻게 가져갈지 정리 필요
    SPRING_ORM_IBATIS((short) 5061, "SPRING", false, false, false, HistogramSchema.NORMAL_SCHEMA),
    
    IBATIS((short) 5500, "IBATIS", false, false, false, HistogramSchema.NORMAL_SCHEMA),
    MYBATIS((short) 5510, "MYBATIS", false, false, false, HistogramSchema.NORMAL_SCHEMA),
    
    DBCP((short) 6050, "DBCP", false, false, false, HistogramSchema.NORMAL_SCHEMA),

    // memory cache  8000
    MEMCACHED((short) 8050, "MEMCACHED", true, true, false, HistogramSchema.FAST_SCHEMA),
    MEMCACHED_FUTURE_GET((short) 8051, "MEMCACHED", true, false, false, HistogramSchema.FAST_SCHEMA),
    ARCUS((short) 8100, "ARCUS", true, true, true, HistogramSchema.FAST_SCHEMA),
    ARCUS_FUTURE_GET((short) 8101, "ARCUS", true, false, true, HistogramSchema.FAST_SCHEMA),

    // connector류
    HTTP_CLIENT((short) 9050, "HTTP_CLIENT", false, true, false, HistogramSchema.NORMAL_SCHEMA),
    JDK_HTTPURLCONNECTOR((short) 9055, "JDK_HTTPCONNECTOR", false, true, false, HistogramSchema.NORMAL_SCHEMA),
	NPC_CLIENT((short) 9060, "NPC_CLIENT", false, true, false, HistogramSchema.NORMAL_SCHEMA),
	NIMM_CLIENT((short) 9070, "NIMM_CLIENT", false, true, false, HistogramSchema.NORMAL_SCHEMA);

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
        return RpcCodeRange.isRpcRange(code);
    }

    public boolean isIndexable() {
        return !terminal && !isRpcClient() && code > 1000;
    }

    // TODO rpc 호출에 대해서만 통계정보를 남길 것이니, isRpc()로 바꾸는건 어떨지???
    public boolean isRecordStatistics() {
        return recordStatistics;
    }

	/**
	 * agent가 설치되어있지 않은 피호출자인가?
	 * 
	 * @return
	 */
    public boolean isUnknown() {
        return this == ServiceType.UNKNOWN; // || this == ServiceType.UNKNOWN_CLOUD;
    }
    
    /**
     * 사용자 또는 알 수 없는 호출자인가?
     * 
     * @return
     */
    public boolean isUser() {
    	return this == ServiceType.USER;
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
            return UNDEFINED;
        	//return UNKNOWN;
        }
        return serviceType;
    }

    private static final IntHashMap<ServiceType> CODE_LOOKUP_TABLE = new IntHashMap<ServiceType>(128);
    private static final Map<String, List<ServiceType>> STATISTICS_LOOKUP_TABLE = new HashMap<String, List<ServiceType>>(64);

    static {
        initializeLookupTable();
        initializeStatisticsLookupTable();
    }

    private static void initializeStatisticsLookupTable() {
        ServiceType[] values = ServiceType.values();
        final Map<String, List<ServiceType>> temp = new HashMap<String, List<ServiceType>>();
        for (ServiceType serviceType : values) {
            if(serviceType.isRecordStatistics()) {
                List<ServiceType> serviceTypeList = STATISTICS_LOOKUP_TABLE.get(serviceType.getDesc());
                if (serviceTypeList == null) {
                    serviceTypeList = new ArrayList<ServiceType>();
                    temp.put(serviceType.getDesc(), serviceTypeList);
                }
                serviceTypeList.add(serviceType);
            }
        }
        // 수정하지 못하도록 한다.
        for (Map.Entry<String, List<ServiceType>> entry : temp.entrySet()) {
            List<ServiceType> serviceTypes = Collections.unmodifiableList(entry.getValue());
            STATISTICS_LOOKUP_TABLE.put(entry.getKey(), serviceTypes);
        }

    }

    private static void initializeLookupTable() {
        ServiceType[] values = ServiceType.values();
        for (ServiceType serviceType : values) {
            ServiceType check = CODE_LOOKUP_TABLE.put(serviceType.code, serviceType);
            if (check != null) {
                throw new IllegalStateException("duplicated code found. code:" + serviceType.code);
            }
        }
    }
}
